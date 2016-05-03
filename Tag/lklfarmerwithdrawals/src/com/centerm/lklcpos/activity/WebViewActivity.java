package com.centerm.lklcpos.activity;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.LklcposActivityManager;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.transaction.entity.Shortcut;
import com.centerm.lklcpos.transaction.entity.Transaction;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.InterruptPower;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

public class WebViewActivity extends BaseActivity {

	private Logger logger = Logger.getLogger(WebViewActivity.class);

	private String ACTION_FINISH = "com.centerm.androidlklpos.finish";
	private Bundle mBundle;
	private boolean outaSignIsUnSuccess;
	private boolean isNoAMT;
	private boolean hasPrint;
	private boolean hasSettle;
	private boolean isMaxNum;
	private boolean isClosed;
	public static WebViewActivity installActivity = null;
	public static boolean isCallbyThrid = false; // 标记是否为第三方调用
	private ParamConfigDao parConfig;

	private ImageView image_progress;
	private TextView loadingTextView;
	private Handler handler;
	private Intent thridCalledIntent;

	private boolean load_end = false;
	private boolean isNext = false;
	private boolean isCancel = false;
	private AnimationDrawable animationDrawable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// super.onCreate(savedInstanceState);
		//svn
		/*
		 * //在onresume方法中初始化输入法 InputmethodCtrl ctrl =
		 * InputmethodCtrl.getInstance(); ctrl.initialization(this, 0, 350, 800,
		 * 250, "com.centerm.lklcpos"); ctrl.setAttrDirect(0, 350, 800, 250);
		 * //输入法位置参数设置 ctrl.setInputMode123();
		 */
		if (null == this.getIntent().getExtras()) {// 是否为第三方调用
			super.onCreate(savedInstanceState);
			// add by xrh @20131013 begin
			parConfig = new ParamConfigDaoImpl(WebViewActivity.this);
			String enabled = parConfig.get("enabled");// 0为未激活，1为激活
			Log.d("Xrh", "是否开通激活，enabled = " + enabled);
			if (!"1".equals(enabled)) {
				logger.info("---------------终端还未开通---------------");
				Intent intent = new Intent(WebViewActivity.this, OneShotWelcomeActivity.class);
				startActivity(intent);
			} else { //
				if (!"1".equals(parConfig.get("enabled2"))) { // 已开通但未确认
					logger.info("-----------------终端已开通但未确认-------------");
					Intent intent = new Intent(WebViewActivity.this, OneShotResultActivity.class);
					Bundle b = new Bundle();
					b.putBoolean("isSuc", true);
					intent.putExtras(b);
					startActivity(intent);
				} else {
					logger.info("-------------终端已开通并确认---------------");
					isCallbyThrid = false;
					Intent standbyIntent = new Intent();
					// standbyIntent.setClass(this, StandbyActivity.class);
					// 开通成功则进入主菜单界面
					standbyIntent.setClass(this, NoticeActivity.class);

					startActivity(standbyIntent);
				}
			}
			lklcposActivityManager.removeActivity(this);
		} else {
			isCallbyThrid = true;
			installActivity = this;
			setTheme(R.style.FullTransparent);
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.loading);
			image_progress = (ImageView) findViewById(R.id.loading_progress);
			loadingTextView = (TextView) findViewById(R.id.loading_lable);
			image_progress.setBackgroundResource(R.anim.network_anim);
			animationDrawable = (AnimationDrawable) image_progress.getBackground();
			animationDrawable.start();

			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					super.handleMessage(msg);
					switch (msg.what) {
					case 0:
						isNext = true;
						break;
					case 1:
						isCancel = true;
						break;
					case 2:
						load_end = true;
					default:
						break;
					}
					if (isNext && load_end) {
						/*
						 * WebViewActivity.this.startActivity(thridCalledIntent)
						 * ; WebViewActivity.this.addActivityAnim();
						 */
						this.post(intentRunnable);
					}
					if (isCancel && load_end) {
						WebViewActivity.this.finish();
						installActivity = null;
					}
				}
			};

			// add by xrh 第三方调用判断开通状态
			parConfig = new ParamConfigDaoImpl(WebViewActivity.this);
			if (!"1".equals(parConfig.get("enabled")) || !"1".equals(parConfig.get("enabled2"))) {
				Message msg = Message.obtain();
				isClosed = true;
				msg.what = 1;
				handler.sendMessage(msg);
			} else {

				mBundle = getIntent().getExtras();
				String amt = mBundle.getString("amt");
				if ("".equals(amt) || amt == null || !isMoney(amt)) {
					isNoAMT = true;
					Message msg = Message.obtain();
					msg.what = 1;
					handler.sendMessage(msg);

				} else {
					parConfig = new ParamConfigDaoImpl(WebViewActivity.this);
					judgeState();
				}
			}

			Thread loadThread = new Thread() {
				public void run() {
					try {
						sleep(1000);
						Message msg = Message.obtain();
						msg.what = 2;
						handler.handleMessage(msg);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			loadThread.start();
		}
	}

	private boolean isMoney(String money) { // 验证金额合法性
		// String
		// regu="^(([1-9]{1}\\d{0,6})(\\.(\\d){1,2})?)|(([0]{1})(\\.(\\d){1,2}))$";
		String regu = "^(([1-9]{1}\\d{0,6})(\\.(\\d){1,2})?)|([0]\\.(([1-9](\\d)?)|([0][1-9])))$";

		return money.matches(regu);
	}

	private void thirdConsume() {

		mBundle = getIntent().getExtras();
		String proc_cd = mBundle.getString("proc_cd");
		String amt = mBundle.getString("amt");
		DecimalFormat df = new DecimalFormat("######0.00");
		amt = df.format(Double.valueOf(amt));
		/*
		 * if ("0.00".equals(amt) || amt.length()>10) { isNoAMT = true; Message
		 * msg = Message.obtain(); msg.what = 1; handler.sendMessage(msg);
		 * return; }
		 */

		Intent standbyIntent = new Intent(WebViewActivity.this, StandbyService.class);
		startService(standbyIntent);

		if ("000000".equals(proc_cd)) { // 第三方调用消费
			thridCalledIntent = getSaleIntent(amt);
			Message msg = Message.obtain();
			msg.what = 0;
			handler.sendMessage(msg);
		}
	}

	private Intent getSaleIntent(String amt) {
		Shortcut mShortcut = new Shortcut(this, "transcation/D000000.xml");
		Intent mIntent = mShortcut.getmIntent();
		Transaction mTransaction = mShortcut.getmTransaction();
		mTransaction.setProperties("D");
		mTransaction.getDataMap().put("transamount", amt);
		mIntent.putExtra("transaction", mTransaction);
		return mIntent;
	}

	private Runnable intentRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			WebViewActivity.this.startActivity(thridCalledIntent);
			WebViewActivity.this.addActivityAnim();
		}
	};

	/**
	 * 对状态进行判断
	 */
	private void judgeState() {

		// 检测是否为第一次启动
		if (InterruptPower.getInterruptPower().isFirst()) {
			Utility.setSignStatus(WebViewActivity.this, false);// 标志签退操作
		}

		Message msg = Message.obtain();
		// 检测凭条是否打印完成的情况
		String printsymbol = null;
		try {
			printsymbol = parConfig.get("printsymbol");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		if ("".equals(printsymbol) || printsymbol == null) {// 既无未打印完的交易凭证，也无未打印完的结算信息

		} else if ("strans".equals(printsymbol)) { // 有交易凭证未打印完成
			hasPrint = true;
			msg.what = 1;
			handler.sendMessage(msg);
			return;

		} else if ("settle".equals(printsymbol)) { // 有未打印完的结算信息
			hasPrint = true;
			msg.what = 1;
			handler.sendMessage(msg);
			return;
		}

		if (!Utility.getSignStatus(WebViewActivity.this)) { // 未签到执行自动签到操作
			startActivityForResult(Utility.autoSign(), 0);
			if (isCallbyThrid) {
				animationDrawable.stop();
				image_progress.setVisibility(View.GONE);
				loadingTextView.setVisibility(View.GONE);
			}
			return;
		}

		// 检测是否有未完成的结算操作
		if (Utility.getSettleStatus(WebViewActivity.this)) {
			hasSettle = true;
			msg.what = 1;
			handler.sendMessage(msg);
			return;
		}

		if (Utility.isMaxCount(WebViewActivity.this)) {
			isMaxNum = true;
			msg.what = 1;
			handler.sendMessage(msg);
		} else {
			thirdConsume();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 0) {
			TradeBaseActivity.isTransStatus = false;
			Message msg = Message.obtain();
			if (resultCode == RESULT_OK) {// 自动签到成功
				DialogFactory.showTips(WebViewActivity.this, "自动签到成功");
				// 检测是否有未完成的结算操作
				if (Utility.getSettleStatus(WebViewActivity.this)) {
					hasSettle = true;
					msg.what = 1;
					handler.sendMessage(msg);
					return;
				}
				// 判断是否达到当批次最大交易条数
				if (Utility.isMaxCount(WebViewActivity.this)) {
					// DialogFactory.showTips(WebViewActivity.this,
					// "当批次交易笔数已达上限，请先结算！");
					isMaxNum = true;
					msg.what = 1;
					handler.sendMessage(msg);
				} else {
					thirdConsume();
				}
			} else {
				outaSignIsUnSuccess = true;
				msg.what = 1;
				handler.sendMessage(msg);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		logger.debug("webviewActivity finish ...");
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			logger.debug("bundle =" + bundle.keySet());
			if (!"02100".equals(bundle.get("msg_tp"))) {
				bundle.putString("msg_tp", "02100");
				if (isClosed) {
					bundle.putString("reason", "终端未开通");
				} else if (isNoAMT) {
					bundle.putString("reason", "无效金额");
				} else if (outaSignIsUnSuccess) {
					bundle.putString("reason", "自动签到失败");
				} else if (hasPrint) {
					bundle.putString("reason", "有未打印完信息");
				} else if (hasSettle) {
					bundle.putString("reason", "有未结算信息");
				} else if (isMaxNum) {
					bundle.putString("reason", "已达当批次交易笔数上限");
				} else {
					bundle.putString("reason", "交易取消");
				}
				Intent intent = new Intent();
				intent.putExtras(bundle);
				setResult(RESULT_CANCELED, intent);
			}
			Intent intent = new Intent();
			intent.setAction(ACTION_FINISH);
			sendBroadcast(intent);
			logger.debug("webviewActivity finish 发送结束广播 ...");
		}
		super.finish();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.i("ckh", "WebViewActivity onRestart()");

		Bundle retBundle = getIntent().getExtras();
		if (retBundle != null) {
			if (retBundle.getBoolean("isSuc")) {
				setResult(RESULT_OK, getIntent());
			} else {
				setResult(RESULT_CANCELED, getIntent());
			}
		}
		LklcposActivityManager.getActivityManager().removeAllActivity();
		installActivity = null;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		// InputmethodCtrl.getInstance().unestablish(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (isNoAMT || outaSignIsUnSuccess || hasPrint || hasSettle || isMaxNum) {
			System.exit(0);
		}
		super.onDestroy();
		Log.i("ckh", "WebViewActivity onDestroy()...");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i("ckh", "WebViewActivity onStart()...");
	}
}
