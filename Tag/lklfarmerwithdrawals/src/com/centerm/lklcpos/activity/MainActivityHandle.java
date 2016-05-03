package com.centerm.lklcpos.activity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.util.Constant;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.Utility;

public class MainActivityHandle extends Handler {
	private static Logger logger = Logger.getLogger(MainActivityHandle.class);

	private Activity activity = null;

	public MainActivityHandle(SwipeCardActivity activity) {
		this.activity = activity;
	}

	public MainActivityHandle(StandbyActivity activity) {
		this.activity = activity;
	}

	public MainActivityHandle(RforIcSwipeCard activity) {
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (this.activity instanceof StandbyActivity) { // 为待机操作示例初始化
			try {
				if (isThatMsg(msg, Constant.msg.msg_swipe_card)) {
					String jsonStr = msg.getData().getString(Constant.msg.jsresponse);
					String state = getSwipeState(jsonStr);
					if (state.equals("1")) { // 刷卡成功
						((StandbyActivity) activity).getSwipeCardData();
						((StandbyActivity) activity).getMagcard().playSwipeCardVoice(0);
					} else { // 刷卡失败分为等待超时和刷卡出错
						String timeout = this.getSwipeTimeout(jsonStr);
						String type = this.getSwipeType(jsonStr);
						if ("1".equals(timeout) && "timeout".equals(type)) { // 1表示等待超时
																				// 不播放声音和提示框
							((StandbyActivity) activity).reSwipeCard(); // 重新开启刷卡设备
						} else if ("exception".equals(type)) { // 如果刷卡设备发生未知异常,关闭刷卡设备，重新开启
							String exception = this.getSwipeException(jsonStr);
							((StandbyActivity) activity).getMagcard().playSwipeCardVoice(1);
							if ("showTip".equals(exception)) { // 提示“请先插卡”
								DialogFactory.showTips((StandbyActivity) activity, "请先插卡");
							}
							((StandbyActivity) activity).getMagcard().closeDev(); // 关闭刷卡设备
							((StandbyActivity) activity).getMagcard().openDev(); // 打开刷卡设备
							((StandbyActivity) activity).reSwipeCard(); // 重新开启刷卡设备
						} else { // 表示刷卡出错
							((StandbyActivity) activity).getMagcard().playSwipeCardVoice(1);
							DialogFactory.showTips(activity, "刷卡失败,请重刷");
							((StandbyActivity) activity).reSwipeCard(); // 重新开启刷卡设备
						}
					}
				}
				if (isThatMsg(msg, Constant.msg.msg_swipe_card_data)) {
					String jsonStr = msg.getData().getString(Constant.msg.jsresponse);
					String state = getSwipeState(jsonStr);
					String type = getSwipeType(jsonStr);
					if (state.equals("1")) { // 数据获取成功
						// 刷卡成功，判断卡号是否合法
						String carno = getCardNo(jsonStr);
						if (!"".equals(carno) && carno != null) {
							if (carno.length() > 19 || carno.length() < 16) {
								DialogFactory.showTips(this.activity, "无效卡号！");
								((StandbyActivity) activity).reSwipeCard();
								return;
							}
						}
						// 刷卡数据抓取成功，需要执行如下操作，判断是否签到，如果未签到则自动签到，否则直接进入交易
						boolean isSigned = Utility.getSignStatus(this.activity);
						if (isSigned) { // 如果已经签到
							StandbyService.isStandByStatus = false;
							StandbyService.onOperate(); // 待机状态设为false
							if (Utility.getSettleStatus((StandbyActivity) activity)) {
								DialogFactory.showTips(this.activity, "请先结算再执行待机消费操作！");
								Intent intent = new Intent(this.activity, MenuSpaceActivity.class);
								((StandbyActivity) activity).startActivity(intent);
								((StandbyActivity) activity).addActivityAnim();
							} else if (Utility.isMaxCount(this.activity)) {
								DialogFactory.showTips(this.activity, "当批次交易笔数已达上限，请先结算！");
								Intent intent = new Intent(this.activity, MenuSpaceActivity.class);
								((StandbyActivity) activity).startActivity(intent);
								((StandbyActivity) activity).addActivityAnim();
							} else {
								((StandbyActivity) activity).startStandbySale((StandbyActivity) activity,
										((StandbyActivity) activity).getMap(), "transcation/T000002.xml");
							}
						} else { // 否则执行自动签到
							((StandbyActivity) activity).autoSign();
						}
					} else {
						((StandbyActivity) activity).reSwipeCard();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("", e);
			}
		} else if (this.activity instanceof SwipeCardActivity) { // 刷卡设备activity实例
			Map<String, String> map = new HashMap<String, String>();
			try {
				if (isThatMsg(msg, Constant.msg.msg_swipe_card)) {
					String jsonStr = msg.getData().getString(Constant.msg.jsresponse);
					String state = getSwipeState(jsonStr);
					if (state.equals("1")) { // 刷卡成功
						((SwipeCardActivity) activity).getSwipeCardData();
						((SwipeCardActivity) activity).getMagcard().playSwipeCardVoice(0);
					} else { // 刷卡失败
						String timeout = this.getSwipeTimeout(jsonStr);
						String type = this.getSwipeType(jsonStr);
						if ("1".equals(timeout) && "timeout".equals(type)) { // 1表示等待超时
																				// 不播放声音和提示框
							((SwipeCardActivity) activity).reSwipeCard(); // 重新开启刷卡设备
						} else if ("exception".equals(type)) { // 如果刷卡设备发生未知异常,关闭刷卡设备，重新开启
							String exception = this.getSwipeException(jsonStr);
							if (!"ioException".equals(exception)) {
								((SwipeCardActivity) activity).getMagcard().playSwipeCardVoice(1);
							}
							if ("showTip".equals(exception)) { // 提示“请先插卡”
								DialogFactory.showTips((SwipeCardActivity) activity, "请先插卡");
							}
							((SwipeCardActivity) activity).getMagcard().closeDev(); // 关闭刷卡设备
							((SwipeCardActivity) activity).getMagcard().openDev(); // 打开刷卡设备
							((SwipeCardActivity) activity).reSwipeCard(); // 重新开启刷卡设备
						} else { // 表示刷卡出错
							((SwipeCardActivity) activity).getMagcard().playSwipeCardVoice(1);
							DialogFactory.showTips((SwipeCardActivity) activity, "刷卡失败,请重刷");
							((SwipeCardActivity) activity).reSwipeCard(); // 重新开启刷卡设备
							((SwipeCardActivity) activity).updateCardNo("");
						}

					}
				}
				if (isThatMsg(msg, Constant.msg.msg_swipe_card_data)) {
					String jsonStr = msg.getData().getString(Constant.msg.jsresponse);
					String state = getSwipeState(jsonStr);
					if (state.equals("1")) {
						// add by txb 修改刷卡后数据不立即显示问题
						final String carno = getCardNo(jsonStr);
						if (!"".equals(carno) && carno != null) {
							if (carno.length() > 19 || carno.length() < 16) {
								DialogFactory.showTips(this.activity, "无效卡号！");
								((SwipeCardActivity) activity).clearTackData();
								((SwipeCardActivity) activity).reSwipeCard();
								return;
							}
						}
						((SwipeCardActivity) activity).updateCardNo(carno);
						// ((SwipeCardActivity) activity).reSwipeCard(); //
						// 刷卡成功，继续执行刷卡监听操作
					} else {
						((SwipeCardActivity) activity).reSwipeCard();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("", e);
			}
		} else if (this.activity instanceof RforIcSwipeCard) { // 刷卡设备activity实例
			Map<String, String> map = new HashMap<String, String>();
			try {
				if (isThatMsg(msg, Constant.msg.msg_swipe_card)) {
					String jsonStr = msg.getData().getString(Constant.msg.jsresponse);
					String state = getSwipeState(jsonStr);
					if (state.equals("1")) { // 刷卡成功
						((RforIcSwipeCard) activity).getSwipeCardData();
						((RforIcSwipeCard) activity).getMagcard().playSwipeCardVoice(0);
					} else { // 刷卡失败
						String timeout = this.getSwipeTimeout(jsonStr);
						String type = this.getSwipeType(jsonStr);
						if ("1".equals(timeout) && "timeout".equals(type)) { // 1表示等待超时
																				// 不播放声音和提示框
							((RforIcSwipeCard) activity).reSwipeCard(); // 重新开启刷卡设备
						} else if ("exception".equals(type)) { // 如果刷卡设备发生未知异常,关闭刷卡设备，重新开启
							String exception = this.getSwipeException(jsonStr);
							if (!"ioException".equals(exception)) {
								((RforIcSwipeCard) activity).getMagcard().playSwipeCardVoice(1);
							}
							if ("showTip".equals(exception)) { // 提示“请先插卡”
								DialogFactory.showTips((RforIcSwipeCard) activity, "请先插卡");
							}
							((RforIcSwipeCard) activity).getMagcard().closeDev(); // 关闭刷卡设备
							((RforIcSwipeCard) activity).getMagcard().openDev(); // 打开刷卡设备
							((RforIcSwipeCard) activity).reSwipeCard(); // 重新开启刷卡设备
						} else { // 表示刷卡出错
							((RforIcSwipeCard) activity).getMagcard().playSwipeCardVoice(1);
							DialogFactory.showTips((RforIcSwipeCard) activity, "刷卡失败,请重刷");
							((RforIcSwipeCard) activity).reSwipeCard(); // 重新开启刷卡设备
							((RforIcSwipeCard) activity).updateCardNo("");
						}

					}
				}
				if (isThatMsg(msg, Constant.msg.msg_swipe_card_data)) {
					String jsonStr = msg.getData().getString(Constant.msg.jsresponse);
					String state = getSwipeState(jsonStr);
					if (state.equals("1")) {
						// add by txb 修改刷卡后数据不立即显示问题
						final String carno = getCardNo(jsonStr);
						if (!"".equals(carno) && carno != null) {
							if (carno.length() > 19 || carno.length() < 16) {
								DialogFactory.showTips(this.activity, "无效卡号！");
								((RforIcSwipeCard) activity).clearTackData();
								((RforIcSwipeCard) activity).reSwipeCard();
								return;
							}
						}
						((RforIcSwipeCard) activity).updateCardNo(carno);
						// ((RforIcSwipeCard) activity).reSwipeCard(); //
						// 刷卡成功，继续执行刷卡监听操作
					} else {
						((RforIcSwipeCard) activity).reSwipeCard();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("", e);
			}
		}
	}

	private static boolean isThatMsg(Message message, String msg) {
		return msg.equals((message.getData().getString(Constant.msg.msg)));
	}

	// 获取刷卡卡号
	private String getCardNo(String str) {
		String jsonStr = str;
		try {
			JSONObject json = new JSONObject(jsonStr);
			Iterator it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if (key.equals("retData")) {
					jsonStr = String.valueOf(json.get(key));
					break;
				}
			}
			json = new JSONObject(jsonStr);
			it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if (key.equals("cardno")) {
					jsonStr = String.valueOf(json.get(key));
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return jsonStr;
	}

	// 获取刷卡状态标志:1成功，0失败
	private String getSwipeState(String str) {
		String jsonStr = str;
		try {
			JSONObject json = new JSONObject(jsonStr);
			Iterator it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if (key.equals("retStatus")) {
					jsonStr = String.valueOf(json.get(key));
					break;
				}
			}
			json = new JSONObject(jsonStr);
			it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if (key.equals("state")) {
					jsonStr = String.valueOf(json.get(key));
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return jsonStr;
	}

	// 获取超时标识
	private String getSwipeTimeout(String str) {
		String jsonStr = str;
		try {
			JSONObject json = new JSONObject(jsonStr);
			Iterator it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if (key.equals("retData")) {
					jsonStr = String.valueOf(json.get(key));
					break;
				}
			}
			json = new JSONObject(jsonStr);
			it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if (key.equals("timeout")) {
					jsonStr = String.valueOf(json.get(key));
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return jsonStr;
	}

	// 获取异常标识
	private String getSwipeException(String str) {
		String jsonStr = str;
		try {
			JSONObject json = new JSONObject(jsonStr);
			Iterator it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if (key.equals("retData")) {
					jsonStr = String.valueOf(json.get(key));
					break;
				}
			}
			json = new JSONObject(jsonStr);
			it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if (key.equals("exception")) {
					jsonStr = String.valueOf(json.get(key));
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return jsonStr;
	}

	// 获取超时标识
	private String getSwipeType(String str) {
		String jsonStr = str;
		try {
			JSONObject json = new JSONObject(jsonStr);
			Iterator it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if (key.equals("retData")) {
					jsonStr = String.valueOf(json.get(key));
					break;
				}
			}
			json = new JSONObject(jsonStr);
			it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				if (key.equals("type")) {
					jsonStr = String.valueOf(json.get(key));
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return jsonStr;
	}
}
