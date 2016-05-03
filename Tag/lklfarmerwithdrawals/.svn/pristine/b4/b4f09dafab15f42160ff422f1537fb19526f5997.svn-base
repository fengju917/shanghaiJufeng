package com.centerm.lklcpos.deviceinterface;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.centerm.lklcpos.LklcposApplication;
import com.centerm.lklcpos.util.Constant;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.ICCardDevInf;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * IC卡设备操作JS接口
 * 
 * @author Administrator
 *
 */
public class ICCardDevJsIfc extends AbstractDevJsIfc {

	private static final Logger log = Logger.getLogger(ICCardDevJsIfc.class);

	private static final int iccard_insert_timeout = 60000;

	private int icard_timeout = iccard_insert_timeout;

	private boolean searchFlag = false;

	private ICcardDevUtil iccard = ICcardDevUtil.getInstance();

	public ICCardDevJsIfc(Context context, Handler handler) throws Exception {
		super(context, handler);
	}

	@Override
	public void init() throws Exception {
		// iccard = ICcardDevUtil.getInstance();
	}

	@Override
	public String openDev() throws Exception {
		log.info("正在打开IC卡设备..");
		final JsResponse response = new JsResponse();
		try {
			this.iccard.open();
			isopen = true;
			log.info("IC卡设备打开成功...");
			response.setSuc(true, "IC卡设备打开成功..");
		} catch (Exception e) {
			log.error("IC卡打开失败....", e);
			response.setSuc(false, e.getMessage());
		}
		return response.toJson();
	}

	public String sendBatch(String cmdjson) {
		log.debug("正在处理IC卡批处理指令...............");
		final JsResponse response = new JsResponse();
		try {
			JSONArray jsonArr = new JSONArray(cmdjson);
			if (jsonArr.length() == 0) {
				throw new JSONException("指令集合为空....");
			}
			for (int i = 0; i < jsonArr.length(); i++) {
				JSONObject cmdObj = jsonArr.getJSONObject(i);
				String cmd = cmdObj.getString("cmd");
				String apdu_r = HexUtil.bcd2str(this.iccard.send(HexUtil.hexStringToByte(cmd)));
				response.addData(cmd, apdu_r);
				if ("1".equals(cmdObj.getString("isMustSuc"))) {// 该指令必须执行成功,否则后续指令不执行
					if (!apdu_r.endsWith("9000")) {// 执行失败
						throw new CmdExcuteFailException("指令[" + cmd + "],执行失败！");
					}
				}
				response.addData(cmd, apdu_r);
			}
		} catch (JSONException e) {
			log.error("", e);
			e.printStackTrace();
			response.setSuc(false, "指令格式有误.....");
		} catch (CmdExcuteFailException e) {
			e.printStackTrace();
			response.setSuc(false, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuc(false, "指令执行异常.....");
		}
		return response.toJson();

	}

	public String send(String command) throws Exception {
		log.info("正在发送APDU指令..");
		try {
			byte[] apduResponse = this.iccard.send(HexUtil.hexStringToByte(command));
			log.debug("send apdu  response ============" + HexUtil.bcd2str(apduResponse));
			return HexUtil.bcd2str(apduResponse);
		} catch (Exception e) {
			log.error("发送APDU指令异常....", e);
			return null;
		}
	}

	public void stopSearch() {
		log.debug("停止寻卡..........................");
		this.searchFlag = false;
	}

	// TODO 搜寻IC卡接口需要修改,先模拟假接口
	public void searchIc() {
		log.debug("正在搜索IC卡...................................");
		final JsResponse response = new JsResponse();
		new Thread() {
			public void run() {
				searchFlag = true;
				while (searchFlag) {
					try {
						if (iccard.status() == 1) {
							log.debug("IC卡搜索成功..................");
							response.setSuc(true, "IC搜索成功...");
							searchFlag = false;
							break;
						}
					} catch (Exception e) {
						log.error("", e);
						e.printStackTrace();
						response.setSuc(false, "IC搜索失败...");
						searchFlag = false;
						break;
					}
				}
				log.debug("寻卡结束.........................................");
			};
		}.start();
	}

	public String cardReset() throws Exception {
		final JsResponse response = new JsResponse();
		log.info("正在复位IC卡设备..");
		try {
			byte[] resetResponse = this.iccard.cardReset();
			log.debug("reset ic dev response ============" + HexUtil.bcd2str(resetResponse));
			response.setSuc(true, "IC设备复位成功...");
			response.addData("resetResponse", HexUtil.bcd2str(resetResponse));

		} catch (Exception e) {
			log.error("复位ic卡设备异常....", e);
			response.setSuc(false, e.getMessage());
		}
		return response.toJson();
	}

	@Override
	public String closeDev() throws Exception {
		final JsResponse response = new JsResponse();
		log.info("正在关闭IC卡设备..");
		this.searchFlag = false; // 停止搜索
		try {
			this.iccard.close();
			isopen = false;
			response.setSuc(true, "IC设备关闭成功...");
		} catch (Exception e) {
			log.error("关闭ic卡设备异常....", e);
			response.setSuc(false, e.getMessage());
		}
		return response.toJson();
	}

	private static class CmdExcuteFailException extends Exception {
		public CmdExcuteFailException(String msg) {
			super(msg);
		}
	}

	public static class ICcardDevUtil {
		private static ICcardDevUtil icUtil = null;
		private ICCardDevInf iccard = null;

		private ICcardDevUtil() {

		}

		public static ICcardDevUtil getInstance() {
			if (icUtil == null) {
				icUtil = new ICcardDevUtil();
			}
			return icUtil;
		}

		public void open() throws Exception {
			if (this.iccard == null) {
				this.iccard = DeviceFactory.getICCardDev();
			}
			this.iccard.open();
		}

		public int status() throws Exception {
			return this.iccard.status();
		}

		public byte[] cardReset() throws Exception {
			return this.iccard.cardReset();
		}

		public byte[] send(byte[] apdu_rq) throws Exception {
			return this.iccard.send(apdu_rq);
		}

		public void close() throws Exception {
			this.iccard.close();
			this.iccard = null;
		}
	}
}
