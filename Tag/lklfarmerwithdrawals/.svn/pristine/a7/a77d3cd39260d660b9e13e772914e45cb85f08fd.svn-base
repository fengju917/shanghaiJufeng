package com.centerm.lklcpos.deviceinterface;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.iso8583.util.DataConverter;
import com.centerm.lklcpos.util.Constant;
import com.centerm.lklcpos.util.ExPinPadException;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.SecurityUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.exception.CentermApiException;
import com.centerm.mid.exception.CentermApiException.IndicationException;
import com.centerm.mid.util.M3HexUtil;

public class ExPinPadDevJsIfc extends AbstractDevJsIfc implements PinPadInterface {

	private static final String pinKeyData = "pinKeyData";

	private static final Logger log = Logger.getLogger(ExPinPadDevJsIfc.class);

	private ExPinPadUtil exPinPadUtil;

	public ExPinPadDevJsIfc(Context context, Handler handler) throws Exception {
		super(context, handler);

	}

	public void init() throws Exception {

	}

	/**
	 * 打开设备前，先判断是否更新密钥索引值。 若有更新，则先new一个设备，一便可将其release 获取主密钥索引值
	 */
	public byte getMkeyId(Context context) {
		byte mkeyid = 0x00;
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(context);
		if ("1".equals(mParamConfigDao.get("mkeyidsymbol"))) {
			mParamConfigDao.update("mkeyidsymbol", "0");
			byte oldmkeyid = (byte) Integer.parseInt(mParamConfigDao.get("oldmkeyid"));
			Log.i("ckh", "getMkeyId oldmkeyid == " + oldmkeyid);
			if (this.exPinPadUtil != null) {
				this.exPinPadUtil.release();
			}
			try {
				this.exPinPadUtil = new ExPinPadUtil(oldmkeyid);
				this.exPinPadUtil.clearWorkKeyId(); // 清除原工作密钥
			} catch (Exception e) {
				log.error("", e);
				// TODO Auto-generated catch block
				if (e instanceof IndicationException) {
					IndicationException indicationException = (IndicationException) e;
					Log.e("LogActivity", "[" + indicationException.getDevId() + "|" + indicationException.getEventId()
							+ ":" + ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");
				}
				e.printStackTrace();
				sendException(0xe1, "外接密码键盘设备打开异常");
			}
		}
		String keystr = mParamConfigDao.get("newmkeyid");
		if ("".equals(keystr) || keystr == null) {
			mkeyid = 0x01;
		}
		mkeyid = (byte) Integer.parseInt(keystr);
		Log.i("ckh", "getMkeyId newmkeyid == " + mkeyid);
		return mkeyid;
	}

	/**
	 * 打开设备 true 打开成功 false 打开失败
	 */
	@Override
	public String openDev() {
		final JsResponse response = new JsResponse();
		byte mkeyid = getMkeyId(context);
		try {
			if (this.exPinPadUtil != null) {
				this.exPinPadUtil.release();
			}
			this.exPinPadUtil = new ExPinPadUtil(mkeyid);
			isopen = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.error("外接密码键盘设备打开异常...");
			sendException(0xe1, "外接密码键盘设备打开异常...");
		}

		return response.toJson();
	}

	@Override
	public String closeDev() {
		final JsResponse response = new JsResponse();
		log.info("正在关闭外接密码键盘设备..");
		// log.error("is_gettingValue="+is_gettingValue+"正在关闭外接密码键盘设备..");
		try {
			if (isopen) {
				if (is_gettingValue) {
					log.error("调用cancelGetPin  and closeDev。。。。。");
					this.exPinPadUtil.cancelGetPin();
				}
				isopen = false;
				is_gettingValue = false;
				response.setSuc(true, "外接密码键盘设备关闭成功...");
				response.addData("type", "success");
			}

		} catch (Exception e) {
			log.error("关闭外接密码键盘设备异常....", e);
			exPinPadUtil = null;
			isopen = false;
			is_gettingValue = false;
			// sendException(0xe1, "关闭外接密码键盘设备异常....");
		}
		return response.toJson();
	}

	/**
	 * 取消PIN输入
	 * 
	 */
	public String cancelGetPin() {
		final JsResponse response = new JsResponse();
		// log.error("is_gettingValue="+is_gettingValue+"调用cancelGetPin。。。。。");
		try {
			log.debug("正在取消pin输入, is_gettingValue =" + is_gettingValue);
			if (is_gettingValue) {
				exPinPadUtil.cancelGetPin();
			}

			is_gettingValue = false;
			response.setSuc(true, "取消PIN输入成功!");
		} catch (Exception e) {
			log.error("cancel pin input error", e);
			// response.setSuc(false,"cancel pin input error");
			sendException(0xe1, "cancel pin input error!");
		}
		return response.toJson();
	}
	/*
	 * noted by Linpeng 20130502 public String getVersion() { final JsResponse
	 * response = new JsResponse(); ExternalPINPadVersion version = null; //
	 * Log.d(TAG, "getVersion() start..."); try { version =
	 * externalPadDev.getVersion(); String str = ""; str += "硬件版本 : " + new
	 * String(version.getHardware()); str += "\n产品系列ID：" + new
	 * String(version.getSn()); str += "\n设备序列号：" + new String(version.getId());
	 * str += "\n应用初始标签：" + new String(version.getAx()); str += "\n初始化记录：" + new
	 * String(version.getNo()); str += "\n固件版本号：" + new
	 * String(version.getFirm()); System.out.println(str);
	 * 
	 * JSONObject obj = new JSONObject(); obj.put("hardware", new
	 * String(version.getHardware())); obj.put("SN", new
	 * String(version.getSn())); obj.put("ID", new String(version.getId()));
	 * obj.put("AX", new String(version.getAx())); obj.put("NO", new
	 * String(version.getNo())); obj.put("firm", new String(version.getFirm()));
	 * 
	 * response.setSuc(true,obj.toString()); log.info("获取外键密码键盘设备版本成功..."); }
	 * catch (Exception e) { response.setSuc(false,e.getMessage());
	 * log.error("获取外接密码键盘设备版本异常..."); } return response.toJson(); }
	 */

	public void getPinWithMethodOne(final String cardno, final String amt, final GetPinBack callback) {
		final JsResponse response = new JsResponse();
		Log.i("ckh", "getPinWithMethodOne isopen == " + isopen);
		if (!isopen) {
			return;
		}
		new Thread() {
			public void run() {
				try {
					exPinPadUtil.getPinWithMethodOnePre();
				} catch (Exception e1) {
					log.error("外接密码键盘预处理异常..", e1);
					response.setSuc(false, "获取外接密码键盘按键失败");
					response.addData("type", "error");
					if (e1 instanceof InterruptedException || e1 instanceof TimeoutException) {
						// response.setSuc(false,"获取外接密码超时");
						// response.addData("type", "timeout");
						sendException(0xe2, "外接密码键盘连接超时");
					}
					// handler.sendMessage(Utility.createCallbackMsg(
					// Constant.msg.msg_pin_key, callback,
					// response.toJson()));
					return;
				}
				String pin = "";
				try {
					if (!isopen) {
						throw new Exception("外接密码键盘设备未打开");
					}

					// handler.sendMessage(Utility.createMesage(Constant.msg.msg_getpin_begin));
					// //通知外接密码键盘打开

					is_gettingValue = true; // add 20130605
					byte[] pinblock = exPinPadUtil.getPinWithMethodOne(cardno, amt, 60);

					// handler.sendMessage(Utility.createMesage(Constant.msg.msg_getpin_end));
					// //通知外接密码键盘结束

					if (pinblock == null) {
						// log.error("pinblock ==null");
						response.setSuc(false, "获取外接密码输入为NULL");
						response.addData("type", "");
					}
					if (pinblock != null) {
						is_gettingValue = true;
						pin = M3HexUtil.bcd2str(pinblock);
						response.addData(pinKeyData, pin);
						response.setSuc(true, "获取PIN成功");
					}
					is_gettingValue = false;

				} catch (Exception e) {
					is_gettingValue = false;
					// handler.sendMessage(Utility.createMesage(Constant.msg.msg_getpin_end));
					// //通知外接密码键盘结束

					if (e instanceof TimeoutException) {
						log.error("外接密码键盘PIN输入超时....", e);
						response.setSuc(false, "获取外接密码PIN输入超时");
						response.addData("type", "timeout");
						sendException(0xe2, "外接密码键盘PIN输入超时....");
						return;
					}
					if (e instanceof IndicationException) {
						IndicationException indicationException = (IndicationException) e;
						log.error("ExPinPadDevJsIfc[" + indicationException.getDevId() + "|"
								+ indicationException.getEventId() + ":"
								+ ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");
					}
					if (e instanceof CentermApiException.IndicationException) {
						int devid = ((CentermApiException.IndicationException) e).getDevId();
						int errorID = ((CentermApiException.IndicationException) e).getEventId();
						log.error("devID : " + devid);
						log.error("errorID : " + errorID);
						log.error("异常退出 : devid: " + Integer.toHexString(devid & 0xFF) + "   errorID: "
								+ Integer.toHexString(errorID & 0xFF));
					}
				}
				is_gettingValue = false;
				callback.onGetPin(pin);

			}
		}.start();
	}

	/**
	 * 发散MAK
	 * 
	 * @param mackey
	 *            MAK密文[in]
	 * @return
	 */
	public String disperseMak(String mackey) {
		if (mackey == null || mackey.length() != 64) {
			log.error("mackey密钥长度错误[" + mackey + "]");
			return null;
		}
		final JsResponse response = new JsResponse();
		try {
			log.debug("正在发散MAK: " + mackey);
			byte[] mac = DataConverter.binaryStrToBytes(mackey);
			log.debug("待发散的MACKEY=[" + HexUtil.bcd2str(mac) + "]");
			exPinPadUtil.disperseMak(mac);
			log.info("发散mak成功..");
			response.setSuc(true, "发散mak成功!");
		} catch (Exception e) {
			log.error("disperseMak error", e);
			// response.setSuc(false,"disperseMak error");
			sendException(0xe1, "disperseMak error!");
		}
		return response.toJson();
	}

	/**
	 * 发散PIK
	 * 
	 * @param pinkey
	 *            PIK密文[in]
	 * @return
	 */
	public String dispersePik(String pinkey) {
		if (pinkey == null || pinkey.length() != 128) {
			log.error("pinkey密钥长度错误[" + pinkey + "]");
			return null;
		}
		final JsResponse response = new JsResponse();
		try {
			pinkey = pinkey.trim();
			log.debug("正在发散PIN: " + pinkey);
			byte[] pin = DataConverter.binaryStrToBytes(pinkey);
			byte[] pintemp = new byte[16];
			System.arraycopy(pin, 0, pintemp, 0, 16);
			log.debug("待发散的PINKEY=[" + HexUtil.bcd2str(pintemp) + "]");
			exPinPadUtil.dispersePik(pintemp);
			log.info("发散PIN成功....");
			response.setSuc(true, "发散PIN成功!");
		} catch (Exception e) {
			if (e instanceof IndicationException) {
				IndicationException indicationException = (IndicationException) e;
				Log.e("LogActivity", "[" + indicationException.getDevId() + "|" + indicationException.getEventId() + ":"
						+ ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");
			}
			log.error("dispersePik error", e);
			// response.setSuc(false,"dispersePik error");
			sendException(0xe1, "dispersePik error!");
		}
		return response.toJson();
	}

	public byte[] getMac(byte[] mab) throws ExPinPadException {
		log.debug("计算MAC开始,macbolck=[" + HexUtil.bcd2str(mab) + "]");
		// if(this.exPinPadUtil==null){
		try {
			byte mkeyid = getMkeyId(context);
			if (this.exPinPadUtil != null) {
				this.exPinPadUtil.release();
			}
			this.exPinPadUtil = new ExPinPadUtil(mkeyid);
		} catch (Exception e) {
			log.error("初始化exPinPadUtil工具发生异常", e);
		}
		// }
		byte[] mac = null;
		try {
			mac = exPinPadUtil.getMac(mab);
		} catch (Exception e) {
			log.error("计算MAC发生异常....", e);
			log.error("mab=[" + HexUtil.bcd2str(mab) + "]");
			log.error("mac=[" + HexUtil.bcd2str(mac) + "]");
		}
		byte[] newmac = new byte[8];
		System.arraycopy(mac, 0, newmac, 0, 8);

		// newmac = new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		// //错误mac,测试使用
		return newmac;
	}

	public void display(final String ShowDataLineOne, final String ShowDataLineTwo, final GetPinBack callback) {
		final JsResponse response = new JsResponse();
		if (!isopen) {
			return;
		}
		new Thread() {
			public void run() {
				try {
					log.debug("正在外接密码键盘显示...");
					is_gettingValue = true;
					exPinPadUtil.display(ShowDataLineOne, ShowDataLineTwo);
					/*
					 * String line1= Utility.strFillSpace(ShowDataLineOne, 32,
					 * false); String line2=
					 * Utility.strFillSpace(ShowDataLineTwo, 32, false);
					 * exPinPadUtil.display(line1,line2);
					 */
					is_gettingValue = false;
					response.setSuc(true, "显示成功!");
					callback.onEnter();
				} /*
					 * catch (Exception e) { is_gettingValue = false ; if (e
					 * instanceof TimeoutException){ sendException(0xe2,
					 * "外接密码键盘显示超时！"); } else { sendException(0xe1,
					 * "外接密码键盘显示异常"); }
					 */
				catch (InterruptedException e) {
					log.error("display() catch InterruptedException.");
					is_gettingValue = false;
					sendException(0xe1, "外接密码键盘显示异常");

				} catch (ExecutionException e) {
					log.error("display() catch ExecutionException.");
					is_gettingValue = false;
					sendException(0xe3, "");

				} catch (TimeoutException e) {
					log.error("display() catch TimeoutException.");
					is_gettingValue = false;
					sendException(0xe2, "外接密码键盘显示超时！");

				} catch (Exception e) {
					log.error("display() catch Exception.");
					is_gettingValue = false;
					sendException(0xe1, "外接密码键盘显示异常");
				}
			}
		}.start();
		is_gettingValue = false;

		return;
	}

	/*
	 * 手动输入主密钥
	 */
	public void operDownloadMkey(byte mkeyid, final byte[] tmk) throws Exception {
		// 先实例化一个外置密码键盘
		byte locakmkeyid = 0x00;
		try {
			locakmkeyid = getMkeyId(context);
			if (this.exPinPadUtil != null) {
				this.exPinPadUtil.release();
			}
			this.exPinPadUtil = new ExPinPadUtil(locakmkeyid);

			// close单例外置密码键盘
			this.exPinPadUtil.release();

			// 重新实例化一个指定的主密钥索引的外置密码键盘
			this.exPinPadUtil = new ExPinPadUtil(mkeyid);

			// 下载主密钥
			exPinPadUtil.downloadMkey(tmk);

			// close指定主密钥索引的外置密码键盘
			this.exPinPadUtil.release();
			this.exPinPadUtil = new ExPinPadUtil(locakmkeyid);
		} catch (Exception e) {
			log.error("", e);
			// TODO: handle exception
			if (this.exPinPadUtil != null) {
				this.exPinPadUtil.release();
				this.exPinPadUtil = new ExPinPadUtil(locakmkeyid);
			}
			throw e;
		}
	}

	/*
	 * 获取到密码回调
	 */
	public interface GetPinBack {
		void onGetPin(String pin);

		void onEnter();
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

	/**
	 * add by chenkehui
	 * 
	 * @throws Exception
	 */
	public void openDevice() throws Exception {

		byte mkeyid = getMkeyId(context);
		if (this.exPinPadUtil != null) {
			this.exPinPadUtil.release();
		}
		this.exPinPadUtil = new ExPinPadUtil(mkeyid);
		Log.i("ckh", "外接密码键盘实例化成功  mkeyid == " + mkeyid);
		isopen = true;
	}

	public void closeDevice() throws Exception {
		if (isopen) {
			if (is_gettingValue) {
				this.exPinPadUtil.cancelGetPin();
			}
			isopen = false;
			is_gettingValue = false;
		}
	}

	public void disperseMacKey(String mackey) throws Exception {
		if (mackey == null || mackey.length() != 64) {
			return;
		}
		log.debug("正在发散MAK: " + mackey);
		byte[] mac = DataConverter.binaryStrToBytes(mackey);
		log.debug("待发散的MACKEY=[" + HexUtil.bcd2str(mac) + "]");
		exPinPadUtil.disperseMak(mac);
		log.info("发散mak成功..");
	}

	public void dispersePinKey(String pinkey) throws Exception {
		if (pinkey == null || pinkey.length() != 128) {
			return;
		}
		pinkey = pinkey.trim();
		log.debug("正在发散PIN: " + pinkey);
		byte[] pin = DataConverter.binaryStrToBytes(pinkey);
		byte[] pintemp = new byte[16];
		System.arraycopy(pin, 0, pintemp, 0, 16);
		log.debug("待发散的PINKEY=[" + HexUtil.bcd2str(pintemp) + "]");
		exPinPadUtil.dispersePik(pintemp);
		log.info("发散PIN成功....");
	}

	// ====================== add for get offline pin by chenkehui @20131219
	// =============================
	/**
	 * 将获取脱机PIN所用的主密钥(占用0x07)和0FFPIN工作密钥导入设备
	 * 
	 * @throws Exception
	 */
	@Override
	public void loadOffMkeyAndWkey() throws Exception {
		// TODO Auto-generated method stub
		byte locakmkeyid = 0x00;
		try {
			locakmkeyid = getMkeyId(context);
			if (this.exPinPadUtil != null) {
				this.exPinPadUtil.release();
			}
			this.exPinPadUtil = new ExPinPadUtil(locakmkeyid);

			// close单例外置密码键盘
			this.exPinPadUtil.release();

			// 重新实例化一个指定的主密钥索引的外置密码键盘
			this.exPinPadUtil = new ExPinPadUtil((byte) 0x07);

			// 导入主密钥
			exPinPadUtil.downloadMkey(new byte[] { 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12,
					0x12, 0x12, 0x12, 0x12, 0x12 });

			// 发散脱机PWKey
			String pinkey = "19891126198911261989112619891126";
			byte[] pin = SecurityUtil.hexStringToByte(pinkey);
			byte[] pintemp = new byte[16];
			System.arraycopy(pin, 0, pintemp, 0, 16);
			log.debug("待发散的OFFPINKEY=[" + HexUtil.bcd2str(pintemp) + "]");
			exPinPadUtil.disperseOffPik(pintemp);

			// close指定主密钥索引的外置密码键盘
			this.exPinPadUtil.release();
			this.exPinPadUtil = new ExPinPadUtil(locakmkeyid);
		} catch (Exception e) {
			// TODO: handle exception
			log.error("下发脱机pin密钥异常");
			if (this.exPinPadUtil != null) {
				this.exPinPadUtil.release();
				this.exPinPadUtil = new ExPinPadUtil(locakmkeyid);
			}
			throw e;
		}
	}

	@Override
	public void openOffDev() {
		// TODO Auto-generated method stub
		try {
			openDev();
			if (this.exPinPadUtil != null) {
				this.exPinPadUtil.release();
			}
			this.exPinPadUtil = new ExPinPadUtil((byte) 0x07);
			log.debug("打开密码键盘设备(索引7 脱机pin专用)成功");
			isopen = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			sendException(0xe1, "外接密码键盘设备打开异常...");
		}

	}

	public void getPinWithMethodTwo(final String cardno, final String amt, final GetPinBack callback) {
		if (!isopen) {
			return;
		}
		new Thread() {
			public void run() {
				try {
					exPinPadUtil.getPinWithMethodTwoPre();
				} catch (Exception e1) {
					log.error("外接密码键盘预处理异常..", e1);
					if (e1 instanceof InterruptedException || e1 instanceof TimeoutException) {
						sendException(0xe2, "外接密码键盘连接超时");
					}
					return;
				}
				String pin = "";
				try {
					if (!isopen) {
						throw new Exception("外接密码键盘设备未打开");
					}
					is_gettingValue = true; // add 20130605
					byte[] pinblock = exPinPadUtil.getPinWithMethodTwo(cardno, amt, 60);

					if (pinblock != null) {
						is_gettingValue = true;
						pin = M3HexUtil.bcd2str(pinblock);
					} else {
						pin = null;
					}
					is_gettingValue = false;

				} catch (Exception e) {
					log.error("", e);
					is_gettingValue = false;
					pin = null;
					if (e instanceof TimeoutException) {
						log.error("外接密码键盘PIN输入超时....", e);
						sendException(0xe2, "外接密码键盘PIN输入超时....");
						return;
					}
					if (e instanceof IndicationException) {
						IndicationException indicationException = (IndicationException) e;
						log.error("ExPinPadDevJsIfc[" + indicationException.getDevId() + "|"
								+ indicationException.getEventId() + ":"
								+ ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");
					}
					if (e instanceof CentermApiException.IndicationException) {
						int devid = ((CentermApiException.IndicationException) e).getDevId();
						int errorID = ((CentermApiException.IndicationException) e).getEventId();
						log.error("devID : " + devid);
						log.error("errorID : " + errorID);
						log.error("异常退出 : devid: " + Integer.toHexString(devid & 0xFF) + "   errorID: "
								+ Integer.toHexString(errorID & 0xFF));
					}
				}
				is_gettingValue = false;
				callback.onGetPin(pin);

			}
		}.start();
	}

	@Override
	public void getOffPin(final String cardno, String amt, final GetPinBack callback) {
		// TODO Auto-generated method stub
		getPinWithMethodTwo(cardno, amt, new GetPinBack() {

			@Override
			public void onGetPin(String pin) {
				// TODO Auto-generated method stub

				String offpinblock = null; // 脱机密码
				if (!"".equals(pin) && pin != null) { // 直接按确定空密码时，pin="";
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
						log.error("", e);
						e.printStackTrace();
					}

					if (pinblockModel1 != null) {
						log.debug("blockModel1 ： " + SecurityUtil.bcd2str(pinblockModel1));
						offpinblock = "2" + SecurityUtil.bcd2str(pinblockModel1).substring(1);
					}
				} else if (pin == null) { // 未输入pin，未点击确定,超时情况
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

	@Override
	public void closeOffDev() {
		// TODO Auto-generated method stub
		try {
			if (isopen) {
				if (is_gettingValue) {
					log.error("调用cancelGetPin  and closeDev。。。。。");
					this.exPinPadUtil.cancelGetPin();
				}
				if (this.exPinPadUtil != null) {
					this.exPinPadUtil.release();
				}
				isopen = false;
				is_gettingValue = false;
			}

		} catch (Exception e) {
			log.error("关闭外接密码键盘设备异常....", e);
			if (this.exPinPadUtil != null) {
				this.exPinPadUtil.release();
			}
			exPinPadUtil = null;
			isopen = false;
			is_gettingValue = false;
		}
	}
}
