package com.centerm.lklcpos.activity;

import java.util.HashMap;

import org.apache.log4j.Logger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.centerm.lklcpos.deviceinterface.PbocDevJsIfc;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.Utility;
import com.centerm.lklcpos.view.PbocWiget;
import com.lkl.farmerwithdrawals.R;

public class NonContactActivity extends BaseActivity {

	private Logger log = Logger.getLogger(NonContactActivity.class);

	private PbocDevJsIfc pbocDev; // PBOC流程接口
	private Handler pbocHandler = null;
	private HashMap<String, String> map;
	private PbocWiget pbocWiget;
	private Dialog interDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		TradeBaseActivity.isTransStatus = true; // 打开交易状态
		StandbyService.onOperate();
		setContentView(R.layout.noncontact);
		inititle();
		pbocWiget = new PbocWiget(this);
		pbocHandler = new Handler() {
			public void handleMessage(Message msg) {

				Bundle b = msg.getData();
				boolean result = b.getBoolean("result");
				String reason = b.getString("reason");

				log.info("reason = " + reason);

				if (Utility.isThatMsg(msg, "msg_check_card")) { // 检卡回调
					if (result) { // 根据卡类型选择PBOC流程
						String balance = "";
						map = new HashMap<String, String>();
						if ("0".equals(reason)) {
							pbocDev.readCardOfflineBalance((byte) 0x00, map);
						} else if ("1".equals(reason)) {
							pbocDev.readCardOfflineBalance((byte) 0x01, map);
						}
					} else { // 检卡异常
						DialogFactory.showTips(NonContactActivity.this, reason);
						overTranscation();
					}
				} else if (Utility.isThatMsg(msg, "msg_emv_interaction")) { // 内核请求交互
					if ("getAIDSelect".equals(reason)) { // 导入多应用选择
						log.info("读取余额回调 data:" + map.get("aidSelectData"));
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
				} else if (Utility.isThatMsg(msg, "msg_readCardOfflineBalance")) { // 返回结果
					String balance = reason;
					Intent intent = new Intent();
					intent.setClass(NonContactActivity.this, NonContactResultActivity.class);
					Bundle bundle = new Bundle();
					if (balance == null) { // 当读余额返回NULL时，说明交易终止
						DialogFactory.showTips(NonContactActivity.this, "交易终止");
						overTranscation();
						return;
					}
					bundle.putString("balance", balance);
					intent.putExtras(bundle);
					startActivity(intent);
					addActivityAnim();
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
			intent.setClass(this, OtherActivity.class);
			startActivity(intent);
		}
		lklcposActivityManager.removeAllActivityExceptOne(OtherActivity.class);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.i("ckh", "PBOC打开");
		if (!lklcposActivityManager.isRemoving) {
			pbocDev.searchCard(0x02 | 0x08, 60);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		try {
			pbocDev.closeDev();
		} catch (Exception e) {
			log.error("closeDev err ...", e);
			e.printStackTrace();
		}
		if (interDialog != null && interDialog.isShowing()) {
			interDialog.dismiss();
			interDialog = null;
		}
		super.onPause();
	}

}
