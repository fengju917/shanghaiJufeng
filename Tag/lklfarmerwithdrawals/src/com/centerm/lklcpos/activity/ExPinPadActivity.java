﻿package com.centerm.lklcpos.activity;

import java.util.Map;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc;
import com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc.GetPinBack;
import com.centerm.lklcpos.deviceinterface.PinPadDevJsIfc;
import com.centerm.lklcpos.deviceinterface.PinPadInterface;
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/*
 * 外接键盘输密处理组件
 * 获取pin，存放在dataMap中对应key为”pindata“的value中。
 * 下一个组件：条件值：”1“，网络交互组件；
 * 			      条件值：”2“，返回主菜单；
 */
public class ExPinPadActivity extends TradeBaseActivity {
	private Logger logger = Logger.getLogger(ExPinPadActivity.class);

	private final int HAS_EXCEPTION = 0xe1;
	private final int TIME_OUT = 0xe2; // 超时
	private final int UPDATA_UI = 0x01; // 更新UI显示“*”
	private final int FINISH_THIS = 0x02; // finsh当前界面
	private final int LEN_NO_ENOUTH = 0x03; // 密码不足
	private final int PIN_NULL = 0x04; // 密码为空
	private PinPadInterface pinPadDev = null;
	private String cardno; // 银行卡账号[in]
	private String amt; // 交易金额[in]
	private Handler mHandler;
	private boolean isKeyDownBack = false; // 判断是否按了返回键
	private boolean isTimeOut = false; // 判断是否超时
	private ParamConfigDao mParamConfigDao;
	private String pinpadType;
	private EditText editText;
	private char[] star = null;
	private String transCode; // mct.xml中对于交易的编码
	private TextView card_no, money;
	private LinearLayout money_ll;
	/*
	 * 防止交易结束，activity栈finish各个activity时， 内置密码键盘重新open、getPIn、close导致异常
	 */
	// public static boolean isGoMenuSpace;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// isGoMenuSpace = false;

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case TIME_OUT:
					isTimeOut = true;
				case HAS_EXCEPTION:
					exPinPadExceptionDeal(msg.getData().getString("exceptiontip"));
					break;
				case UPDATA_UI:
					int pinlength = msg.getData().getInt("pinlength");
					if (pinlength == 0) {
						editText.setText("");
					} else {
						editText.setText(star, 0, pinlength);
					}
					break;
				case LEN_NO_ENOUTH:
					DialogFactory.showTips(ExPinPadActivity.this, "密码不足");
					break;

				case PIN_NULL:
					DialogFactory.showTips(ExPinPadActivity.this, "密码不为空，请输入密码");
					break;

				case FINISH_THIS:
					if (!isKeyDownBack) {
						lklcposActivityManager.removeActivity(ExPinPadActivity.this);
						outActivityAnim();
					}
					break;
				default:
					break;
				}
			}
		};

		mParamConfigDao = new ParamConfigDaoImpl(this);
		pinpadType = mParamConfigDao.get("pinpadType");
		if ("0".equals(pinpadType)) {
			setContentView(R.layout.expinpad);
			try {
				pinPadDev = new ExPinPadDevJsIfc(this, mHandler);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
			}
		} else {
			setContentView(R.layout.pinpad);
			card_no = (TextView) findViewById(R.id.card_no);
			money = (TextView) findViewById(R.id.money);
			money_ll = (LinearLayout)findViewById(R.id.money_ll);
			String code = mTransaction.getMctCode();
			if ("002301".equals(code)) {// 余额操作请求
				money_ll.setVisibility(View.INVISIBLE);
			}
			try {
				pinPadDev = new PinPadDevJsIfc(this, mHandler);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
			}
			editText = (EditText) findViewById(R.id.input_mima);
			editText.setKeyListener(null);
			star = new char[] { '*', '*', '*', '*', '*', '*', '*', '*', '*', '*', '*', '*' };
		}
		transCode = mTransaction.getMctCode();
		inititle();
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if ("0".equals(pinpadType) && event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getDeviceId() == 2) {
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if ("D".equals(mTransaction.getProperties())) {
			if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KEYCODE_HOME) {
				isKeyDownBack = true;
				try {
					pinPadDev.closeDev();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("", e);
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isTimeOut = false;
		isKeyDownBack = false;
		if (!"0".equals(pinpadType)) {
			editText.setText("");
		}
		if (!lklcposActivityManager.isRemoving) {
			pinPadDev.openDev();
			cardno = mTransaction.getDataMap().get("priaccount");
			amt = mTransaction.getDataMap().get("transamount");
			card_no.setText(cardno);
			money.setText(amt);
			System.out.println("priaccount:" + cardno);
			listenerGetPin();
		}
	}

	// 监听输入密码
	private void listenerGetPin() {
		pinPadDev.getPinWithMethodOne(cardno, amt, new GetPinBack() {

			@Override
			public void onGetPin(String pin) {
				// TODO Auto-generated method stub
				if (isKeyDownBack || isTimeOut) {
					isKeyDownBack = false;
					isTimeOut = false;
				} else {
					if ("002322".equals(transCode) || "002323".equals(transCode)) { // 指定账户圈存和非指定账户圈存密码不可为空
						if (pin == null || "".equals(pin)) {
							mHandler.sendEmptyMessage(0x04);
							listenerGetPin();
							return;
						}
					}
					mTransaction.getDataMap().put("pindata", pin);
					Intent mIntent = forward("1");
					mIntent.putExtra("transaction", mTransaction);
					ExPinPadActivity.this.startActivity(mIntent);
					addActivityAnim();
				}
			}

			@Override
			public void onEnter() {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		isKeyDownBack = true;
		pinPadDev.closeDev();
		// isGoMenuSpace = true;
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	// 异常处理
	private void exPinPadExceptionDeal(String tip) {
		DialogFactory.showTips(ExPinPadActivity.this, tip);
		if (WebViewActivity.isCallbyThrid) {
			lklcposActivityManager.removeAllActivityExceptOne(WebViewActivity.class);
		} else {
			int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(ExPinPadActivity.this, MenuSpaceActivity.class);
				ExPinPadActivity.this.startActivity(intent);
			}
			lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
			outActivityAnim();
		}
	}
}
