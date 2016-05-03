package com.centerm.lklcpos.activity;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.helper.DBOperation;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

public class OneShotResultActivity extends BaseActivity {

	private Logger log = Logger.getLogger(OneShotResultActivity.class);

	private TextView mchntnameTextView;
	private TextView meraddrTextView;
	private TextView mertelTextView;
	private TextView meridTextView;
	private TextView termidTextView;
	private TextView serialnumTextView;

	private Button confirmBtn;
	private Button resetBtn;

	private TextView failMsg;
	private Button failokBtn;

	private ParamConfigDao mParamConfigDao;

	private String rescode;
	String errMsg = null;

	public static boolean isActive = false; // 判断当前activity状态

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent = this.getIntent();
		Bundle b = intent.getExtras();
		// log.debug("bundle = " + b.keySet().toString());;
		mParamConfigDao = new ParamConfigDaoImpl(this);

		if (!StandbyService.isActive && !StandbyActivity.isUnbind) { // 第一次启动应用（待机服务未启动时），重置软键盘
			Log.i("ckh", "第一次OneShotResultActivity onCreate()绑定软键盘");
			StandbyActivity.isUnbind = true;
			InputmethodCtrl ctrl = InputmethodCtrl.getInstance();
			Display mDisplay = getWindowManager().getDefaultDisplay();
			int W = mDisplay.getWidth();
			ctrl.initialization(this, 0, 350 * W / 800, 800 * W / 800, 250 * W / 800, "com.centerm.lklcpos");
			ctrl.setAttrDirect(0, 350 * W / 800, 800 * W / 800, 250 * W / 800); // 输入法位置参数设置
			// switch (Utility.currentDevice()) {
			// case 1: // I代终端
			// ctrl.initialization(this, 0, 350, 800, 250,
			// "com.centerm.lklcpos");
			// ctrl.setAttrDirect(0, 350, 800, 250); //输入法位置参数设置
			// break;
			// case 2: // II代终端
			// ctrl.initialization(this, 0, 448, 1024, 320,
			// "com.centerm.lklcpos");
			// ctrl.setAttrDirect(0, 448, 1024, 320);
			// break;
			// }
		}
		Intent mIntent = new Intent(this, StandbyService.class);
		startService(mIntent);
		StandbyService.stopScreenTimer(); // 取消超时

		if (b != null && b.getBoolean("isSuc")) { // 成功
			Intent transintent = new Intent(OneShotResultActivity.this, ActivateSucceedActivity.class);
			transintent.putExtra("result", true);
			startActivity(transintent);
			setContentView(R.layout.oneshot_showresult);

			LinearLayout failure = (LinearLayout) findViewById(R.id.selfopen_failure);
			failure.setVisibility(View.GONE);
			LinearLayout success = (LinearLayout) findViewById(R.id.selfopen_success);
			success.setVisibility(View.VISIBLE);

			mchntnameTextView = (TextView) findViewById(R.id.text_id_mchntname);
			meraddrTextView = (TextView) findViewById(R.id.text_id_meraddr);
			mertelTextView = (TextView) findViewById(R.id.text_id_mertel);
			meridTextView = (TextView) findViewById(R.id.text_id_merid);
			termidTextView = (TextView) findViewById(R.id.text_id_termid);
			serialnumTextView = (TextView) findViewById(R.id.text_id_serialnum);

			confirmBtn = (Button) findViewById(R.id.btn_id_confirm);
			resetBtn = (Button) findViewById(R.id.btn_id_reset);
			//隐藏返回按键
			FrameLayout title_ll = (FrameLayout)findViewById(R.id.lkl_title);
			Button back_btn = (Button)title_ll.findViewById(R.id.back);
			back_btn.setVisibility(View.GONE);
			
			String merName = mParamConfigDao.get("mchntname"); // 商户名称
			String merAddr = mParamConfigDao.get("merAddr"); // 商户地址
			String merTel = mParamConfigDao.get("merTel"); // 电话
			String merId = mParamConfigDao.get("merid"); // 商户号
			String termId = mParamConfigDao.get("termid"); // 终端号
			String serialNum = Utility.getLklCposSN();

			mchntnameTextView.setText(merName);
			meraddrTextView.setText(merAddr);
			mertelTextView.setText(merTel);
			meridTextView.setText(merId);
			termidTextView.setText(termId);
			serialnumTextView.setText(serialNum);

			mParamConfigDao.save("enabled", "1"); // 置为激活成功
			mParamConfigDao.save("enabled2", "0"); // 开通成功未确认
			mParamConfigDao.save("adminpwd", "E10ADC3949BA59ABBE56E057F20F883E"); // 重置主管操作员密码

			// log.debug("merName =" + merName);
			// log.debug("merName =" + merAddr);
			// log.debug("merName =" + merTel);
			// log.debug("merName =" + merId);
			// log.debug("merName =" + termId);
			// log.debug("merName =" + serialNum);
			transintent.putExtra("result", true);

		} else {

			setContentView(R.layout.oneshot_showresult);
			LinearLayout failure = (LinearLayout) findViewById(R.id.selfopen_failure);
			failure.setVisibility(View.VISIBLE);
			LinearLayout success = (LinearLayout) findViewById(R.id.selfopen_success);
			success.setVisibility(View.GONE);

			failMsg = (TextView) findViewById(R.id.fail_msg);
			failokBtn = (Button) findViewById(R.id.btn_id_failok);

			rescode = b.getString("retCode");
			failMsg.setText("错误码:" + rescode + ",请联系客服！");

			// 开通失败，重置数据库
			DBOperation dboper = new DBOperation(OneShotResultActivity.this);
			int flag0 = dboper.rebuildTable();
			log.debug("开通失败，重置数据库  " + flag0);
			Utility.clearShared_prefs(OneShotResultActivity.this);

			// failokBtn.setOnClickListener(new View.OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// gotoOneshotWelcome();
			// }
			// });
			System.out.println(failMsg + "错误吗" + rescode);
			// failMsg.setText("激活失败,错误码:" + rescode);
			failokBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent1 = new Intent(OneShotResultActivity.this, OneShotWelcomeActivity.class);
					startActivity(intent1);
					finish();
				}
			});
		}

		confirmBtn = (Button) findViewById(R.id.btn_id_confirm);
		resetBtn = (Button) findViewById(R.id.btn_id_reset);
		resetBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mParamConfigDao.save("mchntname", "");
				// mParamConfigDao.save("merAddr", "");
				// mParamConfigDao.save("merTel", "");
				// mParamConfigDao.save("merid", "");
				// mParamConfigDao.save("termid", "");
				// mParamConfigDao.save("enabled", "0"); //

				// 重置数据库
				DBOperation dboper = new DBOperation(OneShotResultActivity.this);
				int flag0 = dboper.rebuildTable();
				Utility.clearShared_prefs(OneShotResultActivity.this);
				DialogFactory.showTips(OneShotResultActivity.this, "重置成功,请联系客服!");
				gotoOneshotWelcome();
			}
		});
		confirmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				StandbyService.onOperate(); // 开启计时
				mParamConfigDao.save("enabled2", "1"); // 确认开通 成功
				mParamConfigDao.save("signsymbol", "0"); // 设置签退状态;

				// mParamConfigDao.save("enabled", "1"); //置为激活成功
				// mParamConfigDao.save("adminpwd",
				// "E10ADC3949BA59ABBE56E057F20F883E"); //重置主管操作员密码
				//
				if ("1".equals(mParamConfigDao.get("enabled"))) { // 激活成功
					int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
					log.debug("count =" + count);
					// if (count == 0) {
					Intent intent = new Intent();
					intent.setClass(OneShotResultActivity.this, NoticeActivity.class);

					startActivity(intent);
					// }
					DialogFactory.showTips(OneShotResultActivity.this, "开通成功!");
					// lklcposActivityManager
					// .removeAllActivityExceptOne(MenuSpaceActivity.class);
					outActivityAnim();
				} else {
					gotoOneshotWelcome();
				}
			}
		});

	}

	@Override
	protected void onResume() {
		isActive = true;
		super.onResume();
	}

	@Override
	protected void onPause() {
		isActive = false;
		// TODO Auto-generated method stub
		if (StandbyActivity.isUnbind) {
			Log.i("ckh", "OneShotWelcomeActivity onPause()重置输入模式");
			InputmethodCtrl.getInstance().setInputMode123();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (StandbyActivity.isUnbind) {
			Log.i("ckh", "OneShotResultActivity解绑输入法");
			InputmethodCtrl.getInstance().unestablish(this);
			StandbyActivity.isUnbind = false;
		}
		super.onDestroy();
	}

	// 屏蔽HOME、返回键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// 屏蔽内置密码键盘
	public boolean dispatchKeyEvent(KeyEvent event) {
		log.debug("keyCode=" + event.getKeyCode());
		if (!"1".equals(mParamConfigDao.get("enabled")) || !"1".equals(mParamConfigDao.get("enabled2"))) { // 未激活状态
			return true;
		}
		return super.dispatchKeyEvent(event);
	}
}
