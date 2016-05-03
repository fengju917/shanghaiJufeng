package com.centerm.lklcpos.deviceinterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.iso8583.util.DataConverter;
import com.centerm.lklcpos.LklcposApplication;
import com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc.GetPinBack;
import com.centerm.lklcpos.util.Constant;
import com.centerm.lklcpos.util.ExPinPadException;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.SecurityUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.bean.PINPadUtil.GetMacCmd;
import com.centerm.mid.bean.PINPadUtil.GetPINCmd;
import com.centerm.mid.bean.PINPadUtil.GetPINPadVersion;
import com.centerm.mid.exception.CentermApiException.IndicationException;
import com.centerm.mid.exception.CentermApiException.SocketReadCancelException;
import com.centerm.mid.exception.CentermApiException.SocketReadTimeoutException;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.PINPadDevInf;
import com.centerm.mid.util.KeyEventCallBack;
import com.centerm.mid.util.M3HexUtil;

/**
 * 内置密码键盘JS接口
 * 
 * @author Administrator
 *
 */
public class PinPadDevJsIfc extends AbstractDevJsIfc implements PinPadInterface {

	private static final Logger log = Logger.getLogger(PinPadDevJsIfc.class);

	private static final String pinKeyData = "pinKeyData";

	private static final String randData = "randData";

	private static final String macData = "macData";

	private static final String encryptData = "encryptData";

	private static final String psamData = "psamData";

	private static final String keycodedata = "keycodedata";

	private static final String pinpadmsg = "pinpadmsg";

	private static final String callback_type = "callback_type";

	private static final String key_press_type = "key_press_type";

	private static final String get_pin_type = "get_pin_type";

	private static final int input_timeout = 180000;

	private boolean uiConfirmBtnClick = false;

	public static final byte EX_PIK_ID = (byte) 0x03;
	public static final byte EX_MAK_ID = (byte) 0x03;
	public static final byte EX_TDK_ID = (byte) 0x03;
	public static final byte EX_OFFPIK_ID = (byte) 0x1C;

	private PINPadDevInf pindPadDev;

	public PinPadDevJsIfc(Context context, Handler handler) throws Exception {
		super(context, handler);
	}

	@Override
	public void init() throws Exception {
		this.pindPadDev = DeviceFactory.getPINPadDev();
		this.pindPadDev.setInputTimeOut(input_timeout);
	}

	public int confirmClick() {
		if (this.pindPadDev != null) {
			if (isopen) {
				try {
					this.pindPadDev.confirmGetPin();
					uiConfirmBtnClick = true;
					return 1;
				} catch (Exception e) {
					log.error("confirm get pin error", e);
				}
			}
		}
		return 0;
	}

	/**
	 * 打开设备 true 打开成功 false 打开失败
	 */
	@Override
	public String openDev() {
		final JsResponse response = new JsResponse();
		try {
			if (this.pindPadDev == null) {
				init();
			}
			this.pindPadDev.open();
			isopen = true;
			response.setSuc(true, "内置密码键盘设备打开成功...");
			log.info("内置密码键盘设备打开成功...");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.error("内置密码键盘设备打开异常...");
			response.setSuc(false, e.getMessage());
			sendException(0xe1, "内置密码键盘设备打开异常...");
		}
		return response.toJson();
	}

	/**
	 * 关闭设备
	 */
	@Override
	public String closeDev() {
		final JsResponse response = new JsResponse();
		try {
			if (!isopen) {
				throw new Exception("设备未打开");
			}
			if (is_gettingValue) {
				this.cancelGetPin();
			}
			this.pindPadDev.close();
			this.pindPadDev = null;
			isopen = false;
			is_gettingValue = false;
			response.setSuc(true, "内置密码键盘设备关闭成功...");
			log.info("内置密码键盘设备关闭成功...");
		} catch (Exception e) {
			log.error(e.getMessage());
			response.setSuc(false, e.getMessage());
		}
		return response.toJson();
	}

	/*
	 * private GetPINCmd getpinCmd(byte[] random){ GetPINCmd gpc = new
	 * GetPINCmd(); gpc.setwKeyId((byte) 0);
	 * gpc.setEncryMode(GetPINCmd.MODE_ISO_ENCRYPT_ZERO);
	 * gpc.setNeedCardCalc(GetPINCmd.MODE_NEED_CARD);
	 * gpc.setInputTimes(GetPINCmd.MODE_INPUT_TIMES_ONCE);
	 * gpc.setPINEncryMode(GetPINCmd.MODE_SELECT_PIN_MODE_PROCESSKEY);
	 * gpc.setCancelable(false); gpc.setMin((byte) 0x06); gpc.setMax((byte)
	 * 0x06); gpc.setRandom(random); //按拉卡拉需求，默认16个0
	 * gpc.setCardNo("0000000000000000".getBytes()); return gpc; }
	 */
	public GetPINCmd getpinCmd(String cardNo) { // 卡号参与计算
		Log.i("ckh", "cardNo == " + cardNo);
		// byte[] random = new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		GetPINCmd gpc = new GetPINCmd();
		gpc.setwKeyId(EX_PIK_ID); // 指定PIN加密使用的工作密钥编号
		gpc.setEncryMode(GetPINCmd.MODE_ISO_ENCRYPT_ZERO);
		gpc.setNeedCardCalc(GetPINCmd.MODE_NEED_CARD);
		gpc.setInputTimes(GetPINCmd.MODE_INPUT_TIMES_ONCE);
		gpc.setPINEncryMode(GetPINCmd.MODE_SELECT_PIN_MODE_WKEY); // 使用工作密钥加密
		gpc.setCancelable(true);
		gpc.setMin((byte) 0x04);
		gpc.setMax((byte) 0x08);
		// gpc.setRandom(random);
		if (cardNo.length() < 16) { // 兼容卡号长度小于16位
			cardNo = Utility.fillBackChar(cardNo, 'F');
		}
		cardNo = "0000" + cardNo.substring(3, 15); // 构造拉卡拉PAN计算方法
		gpc.setCardNo(cardNo.getBytes());
		return gpc;
	}

	public String cancelGetPin() {
		final JsResponse response = new JsResponse();
		try {
			log.debug("正在取消pin输入, is_gettingValue =" + is_gettingValue);
			if (is_gettingValue)
				pindPadDev.cancelGetPin();
			response.setSuc(true, "取消PIN输入成功!");
		} catch (Exception e) {
			log.error("cancel pin input error", e);
			response.setSuc(false, "cancel pin input error");
		}
		return response.toJson();
	}

	/**
	 * 循环读取按键,直到确认键按下
	 * 
	 * @param callback
	 *            按键消息处理函数名
	 */
	public void getPinWithMethodOne(final String cardno, final String amt, final GetPinBack callback) {
		final JsResponse response = new JsResponse();
		response.addData(callback_type, get_pin_type);
		if (!isopen) {
			return;
		}
		// final GetPINCmd gpc = getpinCmd(HexUtil.hexStringToByte(random));
		final GetPINCmd gpc = getpinCmd(cardno); // note 20130402

		new Thread() {
			public void run() {
				String pin = "";
				try {
					if (!isopen) {
						throw new Exception("设备未打开");
					}
					is_gettingValue = true;
					byte[] pinData = pindPadDev.getPin(gpc, (new KeyEventCallBack() {
						@Override
						public void excute(byte pinlength, String msg) {
							playKeyPressVoice();
							Log.i("ckh", "pinlength == " + pinlength + "      msg == " + msg);
							if ("输入长度不够".equals(msg)) {
								sendException(0x03, "密码不足");
								return;
							}
							Message pinMsg = Message.obtain();
							pinMsg.what = 0x01;
							Bundle data = new Bundle();
							data.putInt("pinlength", (int) pinlength);
							pinMsg.setData(data);
							if (handler != null) {
								handler.sendMessage(pinMsg);
							}
						}
					}));
					if (pinData == null) {
						is_gettingValue = false;
						sendException(0x02, "获取内置密码输入为NULL");
						return;
					}
					if (pinData != null) {
						pin = HexUtil.bcd2str(pinData);
						response.addData(pinKeyData, pin);
						response.setSuc(true, "获取PIN成功");
					}
					is_gettingValue = false;
					if (!uiConfirmBtnClick) {
						playKeyPressVoice();
					}
				} catch (SocketReadCancelException e) {
					// 按键被取消
					return;
				} catch (SocketReadTimeoutException e) {
					log.error(e.getMessage(), e);
					// sendException(0xe2, "内置密码键盘输入超时");
					return;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					response.setSuc(false, "获取按键失败");
					response.addData("type", "error");
					if (e instanceof IndicationException) {
						IndicationException indicationException = (IndicationException) e;
						Log.e("LogActivity",
								"[" + indicationException.getDevId() + "|" + indicationException.getEventId() + ":"
										+ ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");
					}
					sendException(0xe1, "安全模块错误");
					return;
				}
				is_gettingValue = false;
				uiConfirmBtnClick = false;
				callback.onGetPin(pin);
				/*
				 * handler.sendMessage(Utility.createCallbackMsg(
				 * Constant.msg.msg_pin_key, callback, response.toJson()));
				 */
			}

		}.start();
	}

	/**
	 * 获取psamData
	 * 
	 * @return
	 */
	public String getPSAM() {
		JsResponse response = new JsResponse();
		try {
			if (!isopen) {
				throw new Exception("设备未打开");
			}
			// String psam = this.pindPadDev.getp
			String psam = "";
			response.setSuc(true, "PSAM获取成功");
			response.addData(psamData, psam);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setSuc(false, e.getMessage());
		}
		return response.toJson();
	}

	public void playKeyPressVoice() {
		LklcposApplication.sounderPlayer.playSound(2);
	}

	/**
	 * 获取随机数
	 * 
	 * @return
	 */
	public String getRand() {
		String rand = null;
		log.info("开始获取随机数...");
		try {
			if (!isopen) {
				return null;
			}
			rand = HexUtil.bcd2str(this.pindPadDev.getRandom());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		// log.info("获取随机数成功: 随机数= "+rand);
		return rand;
	}

	/**
	 * 获取随机数
	 * 
	 * @return
	 */
	public String getEncryptTrackData(String trackData, String radom) {
		log.info("开始 加密磁道信息");
		String encryptTrackData = null;
		try {
			if (!isopen) {
				return null;
			}
			ByteArrayOutputStream edBout = new ByteArrayOutputStream();
			edBout.write(0x01);
			edBout.write(0x00);
			edBout.write(HexUtil.hexStringToByte(radom));
			edBout.write(HexUtil.hexStringToByte(trackData));
			encryptTrackData = HexUtil.bcd2str(this.pindPadDev.encryptData(edBout.toByteArray()));
			// log.debug("磁道信息加密成功,密文 ="+encryptTrackData);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return encryptTrackData;
	}

	/**
	 * 获取随机数
	 * 
	 * @return
	 */
	public String getMac(String macBlock, String random) {
		String mac = null;
		log.info("开始 获取MAC信息...macblaock=" + macBlock + " random=" + random);
		try {
			if (!isopen) {
				return null;
			}
			byte[] randomByte = HexUtil.hexStringToByte(random);
			byte[] macBlockByte = macBlock.getBytes();
			macBlockByte = SecurityUtil.fillBytes(macBlockByte);
			mac = HexUtil.bcd2str(this.pindPadDev.getMac(getMacCmd(randomByte, macBlockByte)));
			mac = mac.substring(0, 8);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return mac;
	}

	/********************************************************************************************/
	/***********************************
	 * add by xrh 20130402
	 **************************************/

	/********************************************************************************************/
	/**
	 * 加密磁道信息
	 * 
	 * @return
	 */
	public String getEncryptTrackData(String trackData) {
		log.info("开始 加密磁道信息");
		String radom = "0000000000000000"; // 密钥分散随机数为16个0
		String encryptTrackData = null;
		try {
			if (!isopen) {
				return null;
			}
			ByteArrayOutputStream edBout = new ByteArrayOutputStream();
			edBout.write(0x00);
			edBout.write(0x02); // 指定TDK密钥索引号为02
			// edBout.write(HexUtil.hexStringToByte(radom));
			edBout.write(HexUtil.hexStringToByte(trackData));
			log.debug("传入的数据为:" + DataConverter.bytesToHexStringForPrint(edBout.toByteArray()));
			encryptTrackData = HexUtil.bcd2str(this.pindPadDev.encryptData(edBout.toByteArray()));
			log.debug("磁道信息加密成功,密文 =" + encryptTrackData);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return encryptTrackData;
	}

	/**
	 * 加密磁道信息 ,每次只加密8个字节
	 * 
	 * @return
	 */
	public String getEncryptTrackData8Byte(String trackData) {
		log.info("开始 加密磁道信息");
		String radom = "0000000000000000"; // 密钥分散随机数为16个0
		byte[] head = null;
		byte[] data = null;
		String encryptTrackData = null;
		try {
			if (!isopen) {
				return null;
			}
			ByteArrayOutputStream edBout = new ByteArrayOutputStream();
			edBout.write(0x00);
			edBout.write(0x02); // 指定TDK密钥索引号为02
			head = HexUtil.hexStringToByte("00020000000000000000");
			data = HexUtil.hexStringToByte(trackData);
			edBout.write(HexUtil.hexStringToByte(radom));
			edBout.write(HexUtil.hexStringToByte(trackData));
			encryptTrackData = HexUtil.bcd2str(this.pindPadDev.encryptData8Byte(head, data));
			log.debug("磁道信息加密成功,密文 =" + encryptTrackData);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return encryptTrackData;
	}

	/**
	 * 获取MAC
	 * 
	 * @return
	 */
	public byte[] getMac(byte[] macBlock) {
		String macEncrpted = null;
		byte[] ret = null;
		log.info("开始 获取MAC信息...macblaock=" + DataConverter.bytesToHexString(macBlock));
		try {
			log.debug("isopen = " + isopen);
			if (!isopen) {
				openDev(); // 打开密码键盘
			}
			if (!isopen) { // 如果打开失败，返回null
				return null;

			}
			// byte[] randomByte = HexUtil.hexStringToByte(random);
			byte[] randomByte = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
			log.debug("random = " + DataConverter.bytesToHexString(randomByte));
			ret = this.pindPadDev.getMac(getMacCmd(randomByte, macBlock));
			// byte[] macBlockByte = HexUtil.hexStringToByte(macBlock);
			// macEncrpted
			// =HexUtil.bcd2str(this.pindPadDev.getMac(getMacCmd(randomByte,
			// macBlock,GetMacCmd.TYPE_X919)));
			// log.debug("macEncrypted = " + macEncrpted);
			// macEncrpted = macEncrpted.substring(0, 8);
			this.closeDev(); // mac计算完成，关闭密码键盘
			log.debug("计算MAC后关闭密码键盘isopen=" + isopen);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (e instanceof IndicationException) {
				IndicationException indicationException = (IndicationException) e;
				Log.e("LogActivity", "[" + indicationException.getDevId() + "|" + indicationException.getEventId() + ":"
						+ ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");
			}
		}
		return ret;
	}

	/**
	 * 获取mac计算数据封装对象
	 * 
	 * @param random
	 *            随机数参与密钥分散
	 * @param macBlock
	 *            macBlock
	 * @param methodSelect
	 *            使用算法，有ANSI919 和银联ECB可供选择
	 * @return
	 */
	public GetMacCmd getMacCmd(byte[] random, byte[] macBlock) {
		GetMacCmd gmc = new GetMacCmd();
		gmc.setwKeyID((byte) EX_MAK_ID); // 设置MAK工作密钥索引
		gmc.setMode(GetMacCmd.MODE_DEFAULT);
		gmc.setRandom(random);
		gmc.setType(GetMacCmd.TYPE_CUP_ECB);
		gmc.setData(macBlock);
		return gmc;
	}

	public String disperseMak(String mackey) {

		return null;
	}

	public String dispersePik(String pinkey) {

		return null;
	}

	public void operDownloadMkey(byte mkeyid, byte[] tmk) throws Exception {
		Log.i("ckh", "注入的主密钥索引值：" + mkeyid);
		boolean flag1 = downloadKey(new byte[] { (byte) 0xC0 }, (byte) mkeyid, (byte) 0xFF,
				new byte[] { (byte) 0xFF, (byte) 0xFF }, tmk, new byte[] { 0x19, 0x20, (byte) 0x2B, (byte) 0x01 });
		if (!flag1) {
			throw new ExPinPadException("主密钥注入失败");
		}
	}

	/**
	 * 进行住密钥和工作密钥注入
	 * 
	 * @param mode
	 *            指明密钥下载类型
	 * @param MKeyIndex
	 *            主密钥索引
	 * @param WKeyIndex
	 *            工作密钥索引
	 * @param useCount
	 *            使用次数，大端存储模式0x1234 ,0x12,0x34
	 * @param key
	 *            密钥数据，已使用住密钥加密 16字节
	 * @param checkValue
	 *            校验值 4字节
	 * @return
	 */
	public boolean downloadKey(byte[] mode, byte MKeyIndex, byte WKeyIndex, byte[] useCount, byte[] key,
			byte[] checkValue) {
		boolean flag = false;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bos.write(mode); // 写入模式
			bos.write(new byte[] { (byte) MKeyIndex, (byte) WKeyIndex }); // 写入MkeyIndex和WKeyIndex
			bos.write(useCount); // 写入密码使用次数
			bos.write(key); // 写入密钥值
			bos.write(checkValue);
			this.openDev(); // 打开密码键盘设备
			this.pindPadDev.downloadKEK_WK(bos.toByteArray());
			this.closeDev();
			flag = true;
		} catch (Exception e) {
			log.debug(e.toString(), e);
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return flag;
	}

	/********************************************************************************************/
	@Override
	public void display(String ShowDataLineOne, String ShowDataLineTwo,
			com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc.GetPinBack callback) {
		// TODO Auto-generated method stub
		Message displayMsg = Message.obtain();
		displayMsg.what = 0x02;
		Bundle b = new Bundle();
		b.putString("ShowDataLineOne", ShowDataLineOne);
		b.putString("ShowDataLineTwo", ShowDataLineTwo);
		displayMsg.setData(b);
		if (handler != null) {
			handler.sendMessage(displayMsg);
		}
	}

	@Override
	public void openDevice() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeDevice() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void disperseMacKey(String mackey) throws Exception {
		// TODO Auto-generated method stub
		String mackeyhexStr = DataConverter.bytesToHexString(DataConverter.binaryStrToBytes(mackey)); // 把二进制串转换为16进制字符串
		Log.i("ckh", "发散MAC工作密钥：" + mackeyhexStr);
		mackeyhexStr = mackeyhexStr + mackeyhexStr;
		byte[] MAK = HexUtil.hexStringToByte(mackeyhexStr);
		boolean flag3 = downloadKey(new byte[] { (byte) 0xC2 }, (byte) getTmkKeyId(), EX_MAK_ID,
				new byte[] { (byte) 0xFF, (byte) 0xFF }, MAK, new byte[] { 0x23, (byte) 0xDF, (byte) 0xF4, 0x58 });
		if (!flag3) {
			Log.i("ckh", "Mac工作密钥发散失败");
			throw new ExPinPadException("Mac工作密钥发散失败");
		}
	}

	@Override
	public void dispersePinKey(String pinkey) throws Exception {
		// TODO Auto-generated method stub
		String pinkeyhexStr = DataConverter.bytesToHexString(DataConverter.binaryStrToBytes(pinkey)); // 把二进制串转换为16进制字符串
		Log.i("ckh", "发散PIN工作密钥：" + pinkeyhexStr);
		byte[] PIK = HexUtil.hexStringToByte(pinkeyhexStr);
		Log.i("ckh", "发散PIN工作密钥的索引值：" + getTmkKeyId());
		boolean flag2 = downloadKey(new byte[] { (byte) 0xC1 }, (byte) getTmkKeyId(), EX_PIK_ID,
				new byte[] { (byte) 0xFF, (byte) 0xFF }, PIK,
				new byte[] { (byte) 0xB4, (byte) 0xC5, (byte) 0xA4, (byte) 0xA1 });
		if (!flag2) {
			Log.i("ckh", "Pin工作密钥发散失败");
			throw new ExPinPadException("Pin工作密钥发散失败");
		}
	}

	// 获取主密钥索引值
	private byte getTmkKeyId() {
		byte tmkkeyid = 0x01;
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(context);
		String tmkkeyidstr = mParamConfigDao.get("newmkeyid");
		if ("".equals(tmkkeyidstr) || tmkkeyidstr == null) {
			Log.i("ckh", "获取主密钥索引失败！默认为1");
			return tmkkeyid;
		}
		tmkkeyid = Byte.valueOf(tmkkeyidstr);
		return tmkkeyid;
	}

	/*
	 * 发送异常
	 */
	private void sendException(int what, String tip) {
		Message msg = Message.obtain();
		msg.what = what;
		Bundle bundle = new Bundle();
		bundle.putString("exceptiontip", tip);
		msg.setData(bundle);
		if (handler != null) {
			handler.sendMessage(msg);
		}
	}

	@Override
	public void openOffDev() {
		// TODO Auto-generated method stub
		openDev();
	}

	@Override
	public void loadOffMkeyAndWkey() throws Exception {
		// TODO Auto-generated method stub
		openDev();

		// 往内置密码键盘注入脱机PIN专用主密钥（索引0x07）
		byte[] tmk = new byte[] { 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12,
				0x12, 0x12 };
		operDownloadMkey((byte) 0x07, tmk);

		// 往内置密码键盘注入脱机PIN专用pin工作密钥（索引0x1F）
		byte[] pin = new byte[] { 0x19, (byte) 0x89, 0x11, 0x26, 0x19, (byte) 0x89, 0x11, 0x26, 0x19, (byte) 0x89, 0x11,
				0x26, 0x19, (byte) 0x89, 0x11, 0x26 };
		boolean flag = downloadKey(new byte[] { (byte) 0xC1 }, (byte) 0x07, EX_OFFPIK_ID,
				new byte[] { (byte) 0xFF, (byte) 0xFF }, pin,
				new byte[] { (byte) 0xB4, (byte) 0xC5, (byte) 0xA4, (byte) 0xA1 });
		log.debug("脱机pin工作密钥注入：" + flag);
		closeDev();
	}

	@Override
	public void getOffPin(final String cardno, String amt, final GetPinBack callback) {
		// TODO Auto-generated method stub
		getPinWithMethodTwo(cardno, amt, new GetPinBack() {

			@Override
			public void onGetPin(String pin) {
				// TODO Auto-generated method stub
				String offpinblock = null; // 脱机密码
				if (!"".equals(pin) && pin != null) {
					String TMKey = "12121212121212121212121212121212";
					String pinSecKey = "19891126198911261989112619891126";

					byte[] tmk = SecurityUtil.hexStringToByte(TMKey.toUpperCase()); // 主密钥明文
					byte[] pinseckey = SecurityUtil.hexStringToByte(pinSecKey.toUpperCase()); // PINKEY密文
					byte[] pinsec = SecurityUtil.hexStringToByte(pin.toUpperCase()); // PIN密文
					// (3DES解密)
					byte[] pinkey = SecurityUtil.decrypt3Des(tmk, pinseckey); // PINKEY明文
					byte[] pinblocksec = SecurityUtil.decrypt3Des(pinkey, pinsec); // PINBLOCK密文
					// 卡号截取(拉卡拉方式)
					byte[] cn = SecurityUtil.hexStringToByte("0000" + cardno.substring(3, 15));
					// 异或
					byte[] pinblockModel1 = null;
					try {
						pinblockModel1 = SecurityUtil.xor(cn, pinblocksec); // PINBLOCK明文模式一
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						log.error("", e);
					}

					if (pinblockModel1 != null) {
						log.debug("blockModel1 ： " + SecurityUtil.bcd2str(pinblockModel1));
						offpinblock = "2" + SecurityUtil.bcd2str(pinblockModel1).substring(1);
					}
				} else if (pin == null) { // 异常情况或者超时
					return;
				}
				log.debug("blockModel2 ： " + offpinblock);
				callback.onGetPin(offpinblock); // 脱机密码回调
			}

			@Override
			public void onEnter() {
				// TODO Auto-generated method stub

			}
		});
	}

	public void getPinWithMethodTwo(final String cardno, final String amt, final GetPinBack callback) {
		if (!isopen) {
			return;
		}
		final GetPINCmd gpc = getoffpinCmd(cardno);
		new Thread() {
			public void run() {
				String pin = "";
				try {
					if (!isopen) {
						throw new Exception("设备未打开");
					}
					is_gettingValue = true;
					byte[] pinData = pindPadDev.getPin(gpc, (new KeyEventCallBack() {
						@Override
						public void excute(byte pinlength, String msg) {
							playKeyPressVoice();
							Log.i("ckh", "pinlength == " + pinlength + "      msg == " + msg);
							if ("输入长度不够".equals(msg)) {
								sendException(0x03, "密码不足");
								return;
							}
							Message pinMsg = Message.obtain();
							pinMsg.what = 0x01;
							Bundle data = new Bundle();
							data.putInt("pinlength", (int) pinlength);
							pinMsg.setData(data);
							if (handler != null) {
								handler.sendMessage(pinMsg);
							}
						}
					}));
					if (pinData == null) {
						is_gettingValue = false;
						log.debug("内置密码取消监听输密");
						sendException(0x02, "获取内置密码输入为NULL");
						return;
					}
					if (pinData != null) {
						pin = HexUtil.bcd2str(pinData);
					}
					is_gettingValue = false;
					if (!uiConfirmBtnClick) {
						playKeyPressVoice();
					}
				} catch (SocketReadCancelException e) {
					// 按键被取消
					return;
				} catch (SocketReadTimeoutException e) {
					pin = null;
					log.error(e.getMessage(), e);
					// sendException(0xe2, "内置密码键盘输入超时");
					return;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					pin = null;
					if (e instanceof IndicationException) {
						IndicationException indicationException = (IndicationException) e;
						Log.e("LogActivity",
								"[" + indicationException.getDevId() + "|" + indicationException.getEventId() + ":"
										+ ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");
					}
					sendException(0xe1, "安全模块错误");
					return;
				}
				is_gettingValue = false;
				uiConfirmBtnClick = false;
				callback.onGetPin(pin);
			}
		}.start();
	}

	public GetPINCmd getoffpinCmd(String cardNo) { // 卡号参与计算
		GetPINCmd gpc = new GetPINCmd();
		gpc.setwKeyId(EX_OFFPIK_ID); // 指定OFFPIN加密使用的工作密钥编号
		gpc.setEncryMode(GetPINCmd.MODE_ISO_ENCRYPT_ZERO);
		gpc.setNeedCardCalc(GetPINCmd.MODE_NEED_CARD);
		gpc.setInputTimes(GetPINCmd.MODE_INPUT_TIMES_ONCE);
		gpc.setPINEncryMode(GetPINCmd.MODE_SELECT_PIN_MODE_WKEY); // 使用工作密钥加密
		gpc.setCancelable(true);
		gpc.setMin((byte) 0x04);
		gpc.setMax((byte) 0x08);
		cardNo = "0000" + cardNo.substring(3, 15); // 构造拉卡拉PAN计算方法
		gpc.setCardNo(cardNo.getBytes());
		return gpc;
	}

	@Override
	public void closeOffDev() {
		// TODO Auto-generated method stub
		closeDev();
	}

	/**
	 * 获取PINPAD版本
	 * 
	 * @return
	 */
	public GetPINPadVersion getVersion() {
		GetPINPadVersion ver = null;
		try {
			this.openDev();
			ver = pindPadDev.getPinpadVer();
			pindPadDev.close();
		} catch (Exception e) {
			log.error("获取PINPAD版本异常", e);
		}
		return ver;
	}
}
