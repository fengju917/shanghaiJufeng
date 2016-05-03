package com.centerm.lklcpos.deviceinterface;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.centerm.lklcpos.LklcposApplication;
import com.centerm.lklcpos.util.Constant;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.bean.MagCardCmd;
import com.centerm.mid.bean.TrackData;
import com.centerm.mid.exception.CentermApiException.SocketReadTimeoutException;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.MagCardDevInf;

/**
 * 磁卡设备JS接口
 * 
 * @author Administrator
 * 
 */
public class MagCardDevJsIfc extends AbstractDevJsIfc {

	private static final Logger log = Logger.getLogger(MagCardDevJsIfc.class);

	private static final String cardno = "cardno";// 卡号数据

	private static final String TRACK_DATA = "trackData";// 磁道明文

	public static final int swipe_card_time_out = 180000;// 刷卡等待超时时间

	private MagCardDevInf magCardDev;
	private Thread swipecardThread = null; // 刷卡线程

	public MagCardDevJsIfc(Context context, Handler handler) throws Exception {
		super(context, handler);
	}

	@Override
	public void init() throws Exception {
		this.magCardDev = DeviceFactory.getMagCardDev();
	}

	/**
	 * 打开磁条卡设备
	 */
	@Override
	public String openDev() {
		log.info("正在打开磁条卡设备..");
		final JsResponse response = new JsResponse();
		try {
			if (this.magCardDev == null) {
				init();
			}
			this.magCardDev.open();
			isopen = true;
			log.info("磁条卡设备打开成功...");
			response.setSuc(true, "磁条卡设备打开成功..");
		} catch (Exception e) {
			log.error("磁条卡打开失败....", e);
			response.setSuc(false, e.getMessage());
		}
		return response.toJson();
	}

	/**
	 * 关闭磁条卡设备
	 */
	@Override
	public String closeDev() {
		final JsResponse response = new JsResponse();
		log.info("正在关闭磁条卡设备..");
		try {
			if (isopen) {
				if (is_gettingValue) {
					this.cancelSwipeCard();
				}
				this.magCardDev.close();
				magCardDev = null;
				isopen = false;
				is_gettingValue = false;
				response.setSuc(true, "磁条卡设备关闭成功...");
				response.addData("type", "success");
			}

		} catch (Exception e) {
			log.error("关闭磁卡设备异常....", e);
			magCardDev = null;
			isopen = false;
			is_gettingValue = false;
			response.setSuc(false, e.getMessage());
			response.addData("type", "exception");
		}
		return response.toJson();
	}

	/**
	 * 取消刷卡
	 * 
	 * @return
	 */
	public void cancelSwipeCard() {
		log.debug("正在取消刷卡.....");
		try {
			if (this.magCardDev != null && isopen)
				this.magCardDev.cancel();
			log.debug("取消刷卡成功..");
		} catch (Exception e) {
			log.error("取消刷卡失败...", e);
		}
	}

	public void playSwipeCardVoice(int result) {
		if (result == 1 || result == 0) {
			LklcposApplication.sounderPlayer.playSound(result);
		}
	}

	/**
	 * 鍒峰崱
	 * 
	 * @param callback
	 *            锲炶皟鍑芥暟鍚?
	 */
	public void swipeCard(final String callback) {
		final JsResponse response = new JsResponse();
		swipecardThread = new Thread() {
			public void run() {
				try {
					if (!isopen) {
						if (magCardDev == null) {
							init();
						}
						magCardDev.open();
						isopen = true;
					}
					is_gettingValue = true;
					magCardDev.setTimeout(swipe_card_time_out);
					int swipeResult = magCardDev.swipeCard();
					Log.i("ckh", "swipeResult == " + swipeResult);
					is_gettingValue = false;
					switch (swipeResult) {
					// 刷卡成功
					case 0:
						response.setSuc(true, "刷卡成功");
						break;
					case 0x89:
						response.setSuc(true, "刷卡成功");
						break;
					// 刷卡失败
					case 1:
						response.setSuc(false, "刷卡失败");
						response.addData("type", "timeout");
						break;
					// 刷卡取消
					case 2:
						return;
					default:
						response.setSuc(false, "刷卡失败");
						response.addData("type", "timeout");
						break;
					}

				} catch (SocketReadTimeoutException ex) {
					log.error("swipecard time out", ex);
					is_gettingValue = false;
					response.setSuc(false, ex.getMessage());
					response.addData("type", "timeout");
					response.addData("timeout", "1");
				} catch (IOException e) {
					is_gettingValue = false;
					response.setSuc(false, e.getMessage());
					response.addData("type", "exception");
					response.addData("exception", "ioException");
					log.error("刷卡失败response=" + response.toJson(), e);
				} catch (Exception e) {
					log.error("刷卡操作失败....", e);
					is_gettingValue = false;
					response.setSuc(false, e.getMessage());
					response.addData("type", "exception");
					log.error("刷卡失败response=" + response.toJson(), e);
				}
				String str = response.toJson();
				log.debug("刷卡str = 	" + str);
				handler.sendMessage(
						Utility.createCallbackMsg(Constant.msg.msg_swipe_card, callback, response.toJson()));
			};
		};
		swipecardThread.start();
	}

	/**
	 * 取刷卡数据
	 * 
	 * @param callback
	 * @throws Exception
	 */
	public void getSwipeCardData(final String callback, final Map<String, String> dataMap) throws Exception {
		final JsResponse response = new JsResponse();
		MagCardCmd cmd = new MagCardCmd();
		// PinPadDevJsIfc pinPad = new
		// PinPadDevJsIfc(super.context,super.handler);
		cmd.setMode(MagCardCmd.MODE_NONE_ENCRYPT);
		TrackData trackData;
		try {
			trackData = magCardDev.getData(cmd);
			// pinPad.openDev(); //打开密码键盘
			String cardNo = trackData.getCardNo();
			if (cardNo.length() > 19) { // 如果存在不规则内容
				cardNo = cardNo.split("[:;<>=]")[0];
			}
			log.debug("*****磁卡数据为：" + trackData.getCardNo());
			response.addData(cardno, cardNo);

			dataMap.put("priaccount", cardNo); // 获得卡号

			String trackPacketData = HexUtil.bcd2str(trackData.getPacket());
			// log.debug("磁道明文组包后:"+trackPacketData);
			String track_2_data = new String(trackData.getSecondTrackData());
			log.debug("track_2_data = " + track_2_data);
			String track_3_data = new String(trackData.getThirdTrackData());
			log.debug("track_3_data = " + track_3_data);
			// response.addData(TRACK_DATA, trackPacketData);
			track_2_data = track_2_data.replaceAll("[:;<>=]", "D"); // 替换二磁道信息中出现的特殊符号
			log.debug("替换掉=之后字符串为:" + track_2_data);
			String TDB2 = track_2_data.substring(track_2_data.length() - 17, track_2_data.length() - 1); // 截取从倒数第二位开始16个字符内容
			log.debug("TDB2 = " + TDB2);
			// TDB2 =
			// HexUtil.bcd2str(SecurityUtil.encrype3Des(M3HexUtil.hexStringToByte("11111111111111111111111111111111"),
			// M3HexUtil.hexStringToByte(TDB2))); //对数据进行加密
			// TDB2 = pinPad.getEncryptTrackData(TDB2);
			log.debug("加密后TDB2 = " + TDB2);
			track_2_data = track_2_data.substring(0, track_2_data.length() - 17) + TDB2
					+ track_2_data.substring(track_2_data.length() - 1);// 拼接加密之后的字符串
			log.debug("加密后二磁道信息为：" + track_2_data);
			response.addData("track_2_data", track_2_data); // 附加track_2_data到json字符串中

			dataMap.put("track2data", track_2_data); // 获取2磁道信息

			if (track_3_data != null && !"".equals(track_3_data)) {// add by xrh
																	// 20130424
																	// 澧炲姞if鍒ゆ柇鍏煎娌℃湁涓夌淇℃伅镄勫崱
				track_3_data = track_3_data.replaceAll("[:;<>=]", "D"); // 替换三磁道信息中出现的特殊符号
				log.debug("替换掉=之后字符串为:" + track_3_data);

				String TDB3 = track_3_data.substring(track_3_data.length() - 17, track_3_data.length() - 1); // 截取从倒数第二位开始向左16个字符

				log.debug("TDB3 = " + TDB3);
				// TDB3 =
				// HexUtil.bcd2str(SecurityUtil.encrype3Des(M3HexUtil.hexStringToByte("11111111111111111111111111111111"),
				// M3HexUtil.hexStringToByte(TDB3))); //对数据进行加密
				// TDB3 = pinPad.getEncryptTrackData(TDB3);
				log.debug("加密后TDB3 = " + TDB3);
				track_3_data = track_3_data.substring(0, track_3_data.length() - 17) + TDB3
						+ track_3_data.substring(track_3_data.length() - 1);// 拼接加密之后的字符串
				log.debug("加密后三磁道信息为：" + track_3_data);

				response.addData("track_3_data", track_3_data); // 附件track_3_data到json字符串

				dataMap.put("track3data", track_3_data); // 获取3磁道信息
			}
			log.debug("二磁道数据内容为：" + track_2_data + ";length = " + track_2_data.length());
			log.debug("三磁道数据内容为：" + track_3_data + ";length = " + track_3_data.length());

			response.addData(TRACK_DATA, trackPacketData);
			response.setSuc(true, "取刷卡数据成功");

			// 校验是否为fallback，false按普通磁条卡上送
			try {
				String posInputType = dataMap.get("posInputType");
				if ("801".equals(posInputType)) {
					if (track_2_data != null && track_2_data.contains("D")) {
						String serviceCode = track_2_data.substring(track_2_data.indexOf("D") + 5,
								track_2_data.indexOf("D") + 6);
						if (!"2".equals(serviceCode) && !"6".equals(serviceCode)) {
							dataMap.put("posInputType", "021");
						}
					}
				} else {
					// 判断是否先刷IC卡（非fallback，刷卡）
					if (track_2_data != null && track_2_data.contains("D")) {
						String serviceCode = track_2_data.substring(track_2_data.indexOf("D") + 5,
								track_2_data.indexOf("D") + 6);
						if ("2".equals(serviceCode) || "6".equals(serviceCode)) { // 先刷IC卡
							response.addData("type", "exception");
							response.addData("exception", "showTip"); // 需要提示请插卡
							response.setSuc(false, "不可先刷IC卡");
							handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_swipe_card, callback,
									response.toJson()));
							return;
						}
					}
					dataMap.put("posInputType", "021");
				}
			} catch (Exception e) {
				// TODO: handle exception
				dataMap.put("posInputType", "021");
				e.printStackTrace();
			}
		} catch (SocketReadTimeoutException ex) {
			log.error("get swipe card data time out", ex);
			response.setSuc(false, "取刷卡数据超时");
		} catch (Exception e) {
			log.error("get swipecard data error", e);
			response.setSuc(false, "取刷卡数据失败");
		} finally {
			try {
				this.closeDev();
				// pinPad.closeDev(); //关闭密码键盘
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_swipe_card_data, callback, response.toJson()));
	}
}
