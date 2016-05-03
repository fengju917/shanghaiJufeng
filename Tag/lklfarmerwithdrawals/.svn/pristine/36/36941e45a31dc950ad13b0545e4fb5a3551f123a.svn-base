package com.centerm.lklcpos.deviceinterface;

import org.apache.log4j.Logger;

import com.centerm.lklcpos.util.Constant;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.RFIDDevInf;

import android.content.Context;
import android.os.Handler;

/**
 * 射频卡设备操作JS接口
 * 
 * @author Administrator
 *
 */
public class RFCardDevJsIfc extends AbstractDevJsIfc {

	private static final Logger log = Logger.getLogger(RFCardDevJsIfc.class);

	private static final int rfcard_insert_timeout = 60000;

	private int rfcard_timeout = rfcard_insert_timeout;

	private RFIDDevInf rfcard;

	private boolean searchFlag = false;

	public RFCardDevJsIfc(Context context, Handler handler) throws Exception {
		super(context, handler);
	}

	@Override
	public void init() throws Exception {
		rfcard = DeviceFactory.getRFIDDev();
	}

	@Override
	public String openDev() throws Exception {
		log.info("正在打开RF卡设备..");
		final JsResponse response = new JsResponse();
		try {
			if (this.rfcard == null) {
				init();
			}
			this.rfcard.open();
			isopen = true;
			log.info("RF卡设备打开成功...");
			response.setSuc(true, "RF卡设备打开成功..");
		} catch (Exception e) {
			log.error("RF卡打开失败....", e);
			response.setSuc(false, e.getMessage());
		}
		return response.toJson();
	}

	public void stopSearch() {
		log.debug("停止寻卡..........................");
		this.searchFlag = false;
	}

	// TODO 搜寻RF卡接口需要修改,先模拟假接口
	public void searchRf() {
		log.debug("正在搜索RF卡...................................");
		final JsResponse response = new JsResponse();
		new Thread() {
			public void run() {
				searchFlag = true;
				while (searchFlag) {
					try {
						if (rfcard.status() == 1) {
							log.debug("RF卡搜索成功..................");
							response.setSuc(true, "IC搜索成功...");
							searchFlag = false;
							break;
						}
					} catch (Exception e) {
						log.error("", e);
						e.printStackTrace();
						response.setSuc(false, "RF卡搜索失败...");
						searchFlag = false;
						break;
					}
				}
				log.debug("RF卡寻卡结束.........................................");
			};
		}.start();
	}

	public String send(String command) throws Exception {
		final JsResponse response = new JsResponse();
		log.info("正在发送APDU指令..");
		try {
			if (isopen) {
				byte[] apduResponse = this.rfcard.send(HexUtil.hexStringToByte(command));
				log.debug("send apdu  response ============" + HexUtil.bcd2str(apduResponse));
				response.setSuc(true, "指令执行成功...");
				response.addData("apduResponse", HexUtil.bcd2str(apduResponse));
			} else {
				throw new Exception("RF设备未打开，请先打开设备...");
			}

		} catch (Exception e) {
			log.error("发送APDU指令异常....", e);
			response.setSuc(false, e.getMessage());
		}
		return response.toJson();
	}

	public String cardReset() throws Exception {
		final JsResponse response = new JsResponse();
		log.info("正在复位RF卡设备..");
		try {
			if (isopen) {
				byte[] resetResponse = this.rfcard.reset();
				log.debug("reset ic dev response ============" + HexUtil.bcd2str(resetResponse));
				response.setSuc(true, "RF设备复位成功...");
				response.addData("resetResponse", HexUtil.bcd2str(resetResponse));
			} else {
				throw new Exception("RF设备未打开，请先打开设备...");
			}

		} catch (Exception e) {
			log.error("复位RF卡设备异常....", e);
			response.setSuc(false, e.getMessage());
		}
		return response.toJson();
	}

	@Override
	public String closeDev() throws Exception {
		final JsResponse response = new JsResponse();
		log.info("正在关闭RF卡设备..");
		this.searchFlag = false; // 停止搜索
		try {
			if (isopen) {
				this.rfcard.close();
				rfcard = null;
				isopen = false;
			}
			response.setSuc(true, "rf设备关闭成功...");
		} catch (Exception e) {
			log.error("关闭RF卡设备异常....", e);
			response.setSuc(false, e.getMessage());
		}
		return response.toJson();
	}

}
