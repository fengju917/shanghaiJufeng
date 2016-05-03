package com.centerm.lklcpos.activity;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.centerm.lklcpos.deviceinterface.PbocDevJsIfc;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.lklcpos.view.PbocWiget;
import com.lkl.farmerwithdrawals.R;

public class NonContactReadLogActivity extends BaseActivity {

	private Logger log = Logger.getLogger(NonContactReadLogActivity.class);

	private PbocDevJsIfc pbocDev; // PBOC流程接口
	private Handler pbocHandler = null;
	private HashMap<String, String> map;
	private PbocWiget pbocWiget;
	private Dialog interDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.noncontact);
		inititle();
		pbocWiget = new PbocWiget(this);
		pbocHandler = new Handler() {
			public void handleMessage(Message msg) {
				Bundle b = msg.getData();
				boolean result = b.getBoolean("result");
				String reason = b.getString("reason");
				log.info("result = " + result);
				if (Utility.isThatMsg(msg, "msg_check_card")) { // 检卡回调
					if (result) { // 根据卡类型选择PBOC流程
						byte[] cardLog = null;
						map = new HashMap<String, String>();
						if ("0".equals(reason)) {
							pbocDev.readCardLog((byte) 0x00, map);
						} else if ("1".equals(reason)) {
							pbocDev.readCardLog((byte) 0x01, map);
						}
					} else { // 检卡异常
						DialogFactory.showTips(NonContactReadLogActivity.this, reason + "， 交易终止");
						overTranscation();
					}
				} else if (Utility.isThatMsg(msg, "msg_emv_interaction")) { // 内核交互
					if ("getAIDSelect".equals(reason)) { // 导入多应用选择
						interDialog = pbocWiget.createSelectedDailog(map.get("aidSelectData"),
								new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfAIDSelect(data);
								// dataMap.put("AIDIndex", data); //
								// 将用户选择的应用索引值存放在dataMap中
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfAIDSelect(null);
							}

						});
					} else if ("oneAIDSelect".equals(reason)) { // 单应用确认
						interDialog = pbocWiget.createMessageDailog(map.get("oneAIDSelectData"),
								new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfMessage((byte) 1);
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfMessage((byte) 0);
							}

						});
					}
				} else if (Utility.isThatMsg(msg, "msg_readCardLog")) {
					if (result) {
						JSONArray array = new JSONArray();
						byte[] cardLog = null;
						cardLog = reason == null ? null : HexUtil.hexStringToByte(reason);
						if (cardLog == null) {
							DialogFactory.showTips(NonContactReadLogActivity.this, "读卡失败，交易终止");
							return;
						} else {
							if ((cardLog[0] & 0x85) == 0x85) {
								log.debug("end事件，交易终止");
								DialogFactory.showTips(NonContactReadLogActivity.this, "交易终止");
								overTranscation();
								return;
							} else if ((cardLog[0] & 0x80) == 0x80) {
								log.debug("end事件，执行成功");
								int index = 0;
								try {
									for (int i = 0; i + 45 < cardLog.length;) {
										byte[] temp = new byte[45];
										System.arraycopy(cardLog, i + 1, temp, 0, 45);
										i += 45;
										array.put(convertLog(temp));
									}
								} catch (JSONException e) {
									log.error("获取卡片日志异常", e);
								}
							}
						}

						Intent intent = new Intent();
						intent.setClass(NonContactReadLogActivity.this, NonContactShowLogActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("cardlog", array.toString());
						intent.putExtras(bundle);
						startActivity(intent);
						addActivityAnim();
						log.debug("cardlog =" + array.toString());
					} else {
						DialogFactory.showTips(NonContactReadLogActivity.this, reason);
						overTranscation();
					}
				}
			}
		};

		try {
			pbocDev = new PbocDevJsIfc(this, pbocHandler);
		} catch (Exception e) {
			log.error("pbocDev init err ...", e);
		}

	}

	private void overTranscation() {
		int count = lklcposActivityManager.activityCount(OtherActivity.class);
		if (count == 0) {
			Intent intent = new Intent();
			intent.setClass(this, MenuSpaceActivity.class);
			startActivity(intent);
		}
		lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.i("ckh", "PBOC打开");
		try {
			pbocDev.openDev();
		} catch (Exception e) {
			log.error("pbocDev open err ...", e);
			e.printStackTrace();
		}
		pbocDev.resetPboc();
		pbocDev.searchCard(0x02 | 0x08, 60);
		super.onResume();
	}

	@Override
	protected void onPause() {
		pbocDev.stopSearchCard();
		try {
			pbocDev.closeDev();
		} catch (Exception e) {
			log.error("closeDev open err ...", e);
			e.printStackTrace();
		}
		if (interDialog != null && interDialog.isShowing()) {
			interDialog.dismiss();
			interDialog = null;
		}
		super.onPause();
	}

	private JSONObject convertLog(byte[] cardlog) throws JSONException {
		log.debug("cardlog = " + HexUtil.bcd2str(cardlog));
		JSONObject obj = new JSONObject();
		byte[] translocaldate = new byte[3];
		byte[] translocaltime = new byte[3];
		byte[] transamount = new byte[6];
		byte[] otheramount = new byte[6];
		byte[] termcountrycode = new byte[2];
		byte[] transcurrcode = new byte[2];
		byte[] acceptoridname = new byte[20];
		byte[] transtype = new byte[1];
		byte[] apptranscount = new byte[2];

		System.arraycopy(cardlog, 0, translocaldate, 0, 3);
		System.arraycopy(cardlog, 3, translocaltime, 0, 3);
		System.arraycopy(cardlog, 6, transamount, 0, 6);
		System.arraycopy(cardlog, 12, otheramount, 0, 6);
		System.arraycopy(cardlog, 18, termcountrycode, 0, 2);
		System.arraycopy(cardlog, 20, transcurrcode, 0, 2);
		System.arraycopy(cardlog, 22, acceptoridname, 0, 20);
		System.arraycopy(cardlog, 42, transtype, 0, 1);
		System.arraycopy(cardlog, 43, apptranscount, 0, 2);

		obj.put("translocaldate", Utility.formatDate(HexUtil.bcd2str(translocaldate))); // 交易日期
		obj.put("translocaltime", Utility.formatTime(HexUtil.bcd2str(translocaltime))); // 交易时间
		obj.put("transamount", Utility.unformatMount(HexUtil.bcd2str(transamount))); // 授权金额
																						// 6byte
		obj.put("otheramount", Utility.unformatMount(HexUtil.bcd2str(otheramount))); // 其他金额
																						// 6bytes
		obj.put("termcountrycode", HexUtil.bcd2str(termcountrycode)); // 终端国家代码
																		// 2bytes
		obj.put("transcurrcode", HexUtil.bcd2str(transcurrcode)); // 交易货币代码
																	// 2bytes
		obj.put("acceptoridname", HexUtil.bcd2str(acceptoridname)); // 商户名称
																	// 20bytes
		obj.put("transtype", HexUtil.bcd2str(transtype)); // 交易类型 1bytes
		obj.put("apptranscount", HexUtil.bcd2str(apptranscount)); // 应用交易计数器
																	// 2bytes
		try {
			obj.put("acceptoridname", new String(acceptoridname, "gbk")); // 商户名称
																			// 20bytes
		} catch (UnsupportedEncodingException e) {
			log.warn("", e);
		}
		obj.put("transtype", HexUtil.bcd2str(transtype)); // 交易类型
		return obj;
	}

}
