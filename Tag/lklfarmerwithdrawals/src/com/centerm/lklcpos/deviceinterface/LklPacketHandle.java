package com.centerm.lklcpos.deviceinterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.iso8583.IsoMessage;
import com.centerm.iso8583.MessageFactory;
import com.centerm.iso8583.bean.FormatInfo;
import com.centerm.iso8583.bean.FormatInfoFactory;
import com.centerm.iso8583.enums.IsoMessageMode;
import com.centerm.iso8583.parse.IsoConfigParser;
import com.centerm.iso8583.util.DataConverter;
import com.centerm.lklcpos.util.ExPinPadException;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.Utility;

public class LklPacketHandle {

	private static final Logger log = Logger.getLogger(LklPacketHandle.class);

	private final String FILENAME = "signInfo"; // 文件名称

	private Context context = null;
	private Handler handler = null;

	private static final String filePath = "conf/mct.xml";

	public LklPacketHandle(Context context, Handler handler) throws Exception {
		this.context = context;
		this.handler = handler;
	}

	/**
	 * 去除两字节报文长度
	 * 
	 * @param message
	 * @return
	 */
	public byte[] subMessLen(byte[] message) {
		byte[] msg = new byte[message.length - 2];
		System.arraycopy(message, 2, msg, 0, msg.length);
		return msg;
	}

	/**
	 * 添加两字节 报文长度
	 * 
	 * @param message
	 * @return
	 */
	public byte[] addMessageLen(byte[] message) {
		int iLen = message.length;
		byte[] targets = new byte[] { (byte) (iLen / 256), (byte) (iLen % 256) };
		byte[] msg = new byte[iLen + 2];

		System.arraycopy(targets, 0, msg, 0, 2); // 拷贝长度
		System.arraycopy(message, 0, msg, 2, iLen); // 拷贝报文
		return msg;
	}

	/**
	 * 获取是否已经签到标识
	 * 
	 * @return
	 */
	public boolean getSignSymbol() {
		boolean flag = false;
		SharedPreferences share = this.context.getSharedPreferences(FILENAME, 0);
		flag = share.getBoolean("signSymbol", false);
		return flag;
	}

	public JsResponse handleReturnPacket(String jsonStr, String transCode, JsResponse response) throws JSONException {

		JSONObject obj = new JSONObject(jsonStr);
		String respcode = obj.getString("respcode");
		String addrespkey = obj.getString("addrespkey"); // 更新密钥

		// 开始工作密钥注入
		byte[] key = DataConverter.binaryStrToBytes(addrespkey);
		byte[] pinKey = new byte[16];
		byte[] macKey = new byte[8];
		System.arraycopy(key, 0, pinKey, 0, 16);
		System.arraycopy(key, 16, macKey, 0, 8); // 拷贝长度

		System.out.println("密钥-----------------------------------");
		for (int i = 0; i < key.length; i++) {
			System.out.print(HexUtil.byteToHex(key[i]) + "\t");
			if ((i + 1) % 16 == 0) { // 够16个字节就换行
				System.out.println();
			}
		}
		/*
		 * byte[] checkValue =
		 * SecurityUtil.encrype3Des(M3HexUtil.hexStringToByte(
		 * "4D84FA7168A914B62431986718C442D8"),
		 * M3HexUtil.hexStringToByte("0000000000000000")); //注入PIK boolean
		 * pikFlag = this.pinPadDev.downloadKey(new byte[]{(byte)0x81}, (byte)0,
		 * (byte)0, new byte[]{(byte)0xFF,(byte)0xFF}, pinKey, checkValue);
		 * //注入MAK boolean makFlag = this.pinPadDev.downloadKey(new
		 * byte[]{(byte)0x82}, (byte)0, (byte)1, new
		 * byte[]{(byte)0xFF,(byte)0xFF}, macKey, checkValue);
		 * 
		 * if (pikFlag&&makFlag) { System.out.println("注入工作密钥成功"); }else {
		 * System.out.println("注入工作密钥失败"); }
		 */

		// String ret1 = expinPadDev.dispersePik(new String(pinKey));
		// String ret2 = expinPadDev.dispersePik(new String(macKey));

		if ("00".equals(respcode)) {

		} else {
			response.setSuc(false, "交易失败");
			response.addData("errcode", respcode);
		}
		System.out.println(response.toJson());
		return response;
	}

	/**
	 * 功能描述：用于进行8583解包操作，将解析的报文转换为json字符串
	 * 
	 * @param message
	 *            需要进行解析的报文内容
	 * @return 8583json形式
	 */
	public Map<String, String> unPack(byte[] message, String transCode) {
		IsoConfigParser xmlParser = new IsoConfigParser();
		FormatInfoFactory formatInfoFactory = null;
		System.out.print("解包后的数据： " + DataConverter.bytesToHexStringForPrint(message));
		try {
			// FileInputStream fis = new
			// FileInputStream(context.getResources().getString(R.string.baseSystemUri)+filePath);
			// //add by xrh @20130710
			// formatInfoFactory = xmlParser.parseFromInputStream(fis);//add by
			// xrh @20130710
			formatInfoFactory = xmlParser.parseFromInputStream(this.context.getAssets().open(filePath));// note
																										// by
																										// xrh
																										// @20130710
		} catch (IOException e) {
			log.debug("解包过程中解析mct.xml文件出错" + e.toString());
		}
		FormatInfo formatInfo = formatInfoFactory.getFormatInfo(transCode, IsoMessageMode.UNPACK); // 获取解包配置
		Map<String, String> map = MessageFactory.getIso8583Message().unPackTrns(message, formatInfo); // 进行解包
		log.debug("解析的报文为：" + map);

		return map;
	}

	/**
	 * 功能描述：组包，根据传入的map格式的数据源和交易码组包
	 * 
	 * @param transCode
	 *            交易码，比如余额查询为“T100001”
	 * @param jsonStr
	 *            报文数据源，比如{"card_no":"62284882910309810214","pwd":"123456"}；
	 * @return
	 * @throws Exception
	 * @throws FileNotFoundException
	 */
	public byte[] pack(String transCode, Map<String, String> map) throws Exception {

		byte[] messageData = null;
		FormatInfoFactory formatInfoFactory = null;
		IsoConfigParser xmlParser = new IsoConfigParser();
		try {
			// FileInputStream fis = new
			// FileInputStream(context.getResources().getString(R.string.baseSystemUri)+filePath);
			// //add by xrh @20130710
			// formatInfoFactory = xmlParser.parseFromInputStream(fis);//add by
			// xrh @20130710
			formatInfoFactory = xmlParser.parseFromInputStream(this.context.getAssets().open(filePath));// note
																										// by
																										// xrh
																										// @20130710
		} catch (IOException e) {
			e.printStackTrace();
			log.error("组包过程中解析mct.xml文件模块出错" + e.toString());
		}
		FormatInfo formatInfo = formatInfoFactory.getFormatInfo(transCode, IsoMessageMode.PACK); // 根据交易码获取对应的报文格式控制对象
		IsoMessage message = null;
		try {
			message = MessageFactory.getIso8583Message().packTrns(map, formatInfo);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("组包过程出错!", e);
			throw new Exception("上送报文错误!");
		}
		System.out.print("发送的数据： " + DataConverter.bytesToHexStringForPrint(message.getAllMessageByteData()));
		messageData = message.getAllMessageByteData(); // 按照字节数组获取报文内容

		if (map.get("udf_fld") != null && !map.get("udf_fld").startsWith("00")) {// 不为管理类交易，需要计算MAC

			Log.i("LogActivity", "需要MAC计算");

			String macFilterStr = formatInfoFactory.getMabInfo(transCode, IsoMessageMode.PACK); // 获取mac过滤字符串
			String macBlock = message.getMacBlock(macFilterStr); // 获取macBlock
			macBlock = DataConverter.addZeroRightToMod16Equal0(macBlock); // 对macBlock进行补位操作，直到其长度对16取模为0
			log.debug("补零后的内容为：" + macBlock);
			byte[] macBlockByte = DataConverter.getStringXor(macBlock); // 对macBlock进行逐个8字节异或运算
			log.debug("MAC计算加密因子为macBlockByte = " + DataConverter.bytesToHexString(macBlockByte));
			byte[] macInfo = null;
			if ("0".equals(Utility.getPinPadDevSymbol(context))) {
				macInfo = this.getMac(macBlockByte);
			} else {
				macInfo = this.getMacFromPinPadDev(macBlockByte);
			}
			// byte[] macInfo = this.getMac(macBlockByte); //计算mac校验值
			// byte[] macInfo = this.getMacFromPinPadDev(macBlockByte);
			// //计算mac校验值
			byte[] messageDataTemp = message.getAllMessageByteData(); // 按照字节数组获取报文内容
			messageData = new byte[messageDataTemp.length]; // 重新构建一个字节数组用来存放更换mac后的报文信息
			System.arraycopy(messageDataTemp, 0, messageData, 0, messageDataTemp.length - 8);
			System.arraycopy(macInfo, 0, messageData, messageDataTemp.length - 8, 8);
		}
		return messageData;
	}

	// 内置密码键盘计算MAC值
	public byte[] getMacFromPinPadDev(byte[] macBlockByte) throws Exception {
		PinPadDevJsIfc pinPadDev = new PinPadDevJsIfc(context, null);
		byte[] mac = null;
		mac = pinPadDev.getMac(macBlockByte);
		byte[] newmac = new byte[8];
		System.arraycopy(mac, 0, newmac, 0, 8);
		return newmac;
	}

	/**
	 * 外置密码计算MAC值 该方法用于根据指定的密钥计算MAC的值
	 * 
	 * @param macBlock
	 * @param Random
	 * @param methodSelect
	 * @return
	 * @throws Exception
	 */
	public byte[] getMac(byte[] macBlockByte) throws Exception {
		try {
			byte mkeyid = getMkeyId(context);
			ExPinPadUtil mExPinPadUtil = new ExPinPadUtil(mkeyid);
			byte[] mac = null;
			mac = mExPinPadUtil.getMac(macBlockByte);
			byte[] newmac = new byte[8];
			System.arraycopy(mac, 0, newmac, 0, 8);
			return newmac;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("ckh", "LklPacketHandle.java 计算MAC发生异常....");
			throw new ExPinPadException(e.getMessage());
		}
	}

	public byte getMkeyId(Context context) {
		byte mkeyid = 0x01;
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl();
		if ("1".equals(mParamConfigDao.get("mkeyidsymbol"))) {
			mParamConfigDao.update("mkeyidsymbol", "0");
			byte oldmkeyid = (byte) Integer.parseInt(mParamConfigDao.get("oldmkeyid"));
			try {
				ExPinPadUtil mExPinPadUtil = new ExPinPadUtil(oldmkeyid);
				mExPinPadUtil.release();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("ckh", "获取旧密钥索引时，发生异常");
			}
		}
		String keystr = mParamConfigDao.get("newmkeyid");
		if ("".equals(keystr) || keystr == null) {
			return mkeyid;
		}
		mkeyid = (byte) Integer.parseInt(mParamConfigDao.get("newmkeyid"));
		return mkeyid;
	}

}
