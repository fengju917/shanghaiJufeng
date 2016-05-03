package com.centerm.lklcpos.activity;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.lklcpos.deviceinterface.PbocDevJsIfc;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.DialogMessage;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.lklcpos.view.PbocWiget;
import com.lkl.farmerwithdrawals.R;

/**
 * 电子现金插、挥卡组件，不支持刷卡，暂只支持插卡
 */
public class TransferSwipeCardActivity extends TradeBaseActivity {

	private static final Logger logger = Logger.getLogger(TransferSwipeCardActivity.class);

	private EditText swipcardET;
	private Button nextBtn;
	private TextView swipMsg;
	private PbocDevJsIfc pbocDev; // PBOC流程接口
	private Handler pbocHandler = null;
	private PbocWiget pbocWiget;
	private Dialog interDialog = null;
	private ProgressDialog mDialog;
	private Dialog dialog;
	private Handler handler = null;
	private String mctCodem;
	private String lastCardNum = null;
	private Map<String, String> f48infoMap = new HashMap<String, String>(); // 非指定账户圈存

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transferswipecard);
		init();

		pbocHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Bundle b = msg.getData();
				boolean result = b.getBoolean("result");
				String reason = b.getString("reason");

				if (Utility.isThatMsg(msg, "msg_check_card")) { // 检卡回调
					if (result) { // 读卡信息
						StandbyService.stopStandby();
						if ("002323".equals(mctCodem)) {
							pbocDev.readICCard(f48infoMap);
						} else {
							pbocDev.readICCard(mTransaction.getDataMap());
						}
						mDialog = new DialogMessage(TransferSwipeCardActivity.this)
								.createProgressDialog("正在读卡信息，请稍候（勿拔卡）...");
					} else { // 检卡异常
						if ("检卡超时".equals(reason)) {
							reSearchCard(reason);
						} else {
							overTranscation();
						}
					}
				} else if (Utility.isThatMsg(msg, "msg_read_card")) { // 读取卡片信息
					mDialog.dismiss();

					/** add for 纯电子现金卡，可进行与后台账户无关的联机交易 @20140220 start */
					if ("纯电子现金卡，不可联机".equals(reason)) {
						if ("002321".equals(mctCodem) || "002323".equals(mctCodem) || "002324".equals(mctCodem)) { // 纯电子现金卡：现金充值、非指定账户圈存、现金充值撤销是可联机的
							result = true;
						}
					}
					/** add for end */

					if (result) {
						if ("002323".equals(mctCodem)) {
							String inputCard = f48infoMap.get("priaccount");
							showCardno(inputCard);
							mTransaction.getDataMap().put("adddataword",
									HexUtil.bcd2str(("0510" + inputCard).getBytes()));
						} else {
							showCardno(mTransaction.getDataMap().get("priaccount"));
						}
						Intent nextIntent = forward("1");
						nextIntent.putExtra("transaction", mTransaction);
						startActivity(nextIntent);
						addActivityAnim();
					} else {
						StandbyService.startStandby();
						String tip = null;
						if ("fallback".equals(reason)) { // fallback
							if ("002321".equals(mctCodem) || // 电子现金交易不支持fallback
									"002322".equals(mctCodem) || "002323".equals(mctCodem) || "002324".equals(mctCodem)
									|| "002325".equals(mctCodem)) {
								tip = "读取IC卡失败，请拔卡！";
							} else {
								tip = "读取IC卡错误，请刷卡！";
							}
						} else {
							tip = "读取IC卡失败，请拔卡！";
						}
						if (interDialog != null && interDialog.isShowing()) {
							interDialog.dismiss();
							interDialog = null;
						}
						dialog = new DialogMessage(TransferSwipeCardActivity.this).alert("提示", tip,
								new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								reSearchCard("fallback");
							}
						}, null);
						dialog.show();
					}
				} else if (Utility.isThatMsg(msg, "msg_emv_interaction")) { // 内核请求交互
					if ("getAIDSelect".equals(reason)) { // 导入多应用选择
						Map<String, String> map = null;
						if ("002323".equals(mctCodem)) {
							map = f48infoMap;
						} else {
							map = mTransaction.getDataMap();
						}
						interDialog = pbocWiget.createSelectedDailog(map.get("aidSelectData"),
								new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								mTransaction.getDataMap().put("AIDIndex", data); // 将用户选择的应用索引值存放在dataMap中
								pbocDev.loadResultOfAIDSelect(data);
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfAIDSelect(null);
							}

						});
					} else if ("oneAIDSelect".equals(reason)) { // 单应用确认
						Map<String, String> map = null;
						if ("002323".equals(mctCodem)) {
							map = f48infoMap;
						} else {
							map = mTransaction.getDataMap();
						}
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
				}
			}
		};

		try {
			pbocDev = new PbocDevJsIfc(this, pbocHandler);
			// magcard.swipeCard("");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}

		nextBtn = (Button) findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				nextAction();
			}
		});
		nextBtn.setVisibility(View.GONE);
	}

	// IC卡插卡
	private void reSearchCard(String type) {
		if ("".equals(type) || type == null || "检卡超时".equals(type)) {
			pbocDev.searchCard(0x02, 60);
		} else if ("fallback".equals(type)) {
			try {
				pbocDev.closeDev();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
			}
			pbocDev.searchCard(0x02, 60);
		}
	}

	private void overTranscation() {
		if ("002321".equals(mctCodem) || // 电子现金交易结束，返回圈存界面
				"002322".equals(mctCodem) || "002323".equals(mctCodem) || "002324".equals(mctCodem)
				|| "002325".equals(mctCodem)) {
			int count = lklcposActivityManager.activityCount(TransferMenuActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(this, TransferMenuActivity.class);
				startActivity(intent);
			}
			lklcposActivityManager.removeAllActivityExceptOne(TransferMenuActivity.class);
		} else {
			int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(this, MenuSpaceActivity.class);
				startActivity(intent);
			}
			lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
		}
	}

	/*
	 * 插卡时，读卡成功，回显卡号
	 */
	private void showCardno(String carno) {
		if (carno.length() > 15 && carno.length() < 20) {
			String cardNm = "";
			int j = 0, num = 0;
			if (carno.length() % 4 == 0) {
				num = (int) carno.length() / 4;
			} else {
				num = (int) carno.length() / 4 + 1;
			}
			for (int i = 0; i < num; i++) {
				if (j + 4 < carno.length()) {
					cardNm = cardNm + carno.substring(j, j + 4) + "  ";
				} else {
					cardNm = cardNm + carno.substring(j, carno.length());
				}

				j = j + 4;
			}
			swipcardET.setText(cardNm);
		}
	}

	/**
	 * 刷卡成功，回显卡号之后进入下一步
	 */
	public void updateCardNo(String carno) {
		if (carno.length() > 15 && carno.length() < 20) {

			showCardno(carno);

			if (nextBtn.isShown()) {
				nextBtn.setVisibility(View.GONE);
			}

			if (mctCodem.equals("002324")) { // 检验现金充值撤销卡号是否一致
				if (lastCardNum != null) {
					if (!lastCardNum.equals(mTransaction.getDataMap().get("priaccount"))) {
						DialogFactory.showTips(TransferSwipeCardActivity.this, "卡号与原交易卡号不同，请重新刷卡！");
						clearTackData();
						overTranscation();
						return;
					}
				}
			}

			Intent nextIntent = forward("1");
			nextIntent.putExtra("transaction", mTransaction);
			startActivity(nextIntent);
			addActivityAnim();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		swipcardET.setText("");
		if (!lklcposActivityManager.isRemoving) {
			try {
				pbocDev.openDev();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
			}
			if (dialog == null || !dialog.isShowing()) {
				pbocDev.resetPboc();
				pbocDev.searchCard(0x02, 60);
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
		if (interDialog != null && interDialog.isShowing()) {
			interDialog.dismiss();
			interDialog = null;
		}
		StandbyService.startStandby();
		pbocDev.stopSearchCard();
		try {
			pbocDev.closeDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * 初始化界面控件
	 */
	public void init() {
		inititle();
		pbocWiget = new PbocWiget(this);
		mctCodem = mTransaction.getMctCode();
		lastCardNum = mTransaction.getDataMap().get("oldpriaccount");
		swipcardET = (EditText) findViewById(R.id.swipcardET);
		swipcardET.setKeyListener(null);
		swipMsg = (TextView) findViewById(R.id.transfer_swip_message);
		if ("002323".equals(mctCodem)) {
			swipMsg.setText("请插卡(转入卡)");
		}
	}

	// 当卡号不准确时，清除磁道信息
	public void clearTackData() {
		mTransaction.getDataMap().put("priaccount", "");
		mTransaction.getDataMap().put("track2data", "");
		mTransaction.getDataMap().put("track3data", "");

	}

	/**
	 * 进入下一步界面
	 */
	private void nextAction() {
		Intent nextIntent = forward("1");
		nextIntent.putExtra("transaction", mTransaction);
		startActivity(nextIntent);
		addActivityAnim();
	}

}