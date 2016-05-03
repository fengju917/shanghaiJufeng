package com.centerm.lklcpos.activity;

import org.apache.log4j.Logger;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.settings.activity.SettingMainActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.DialogMessage;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

public class OneShotWelcomeActivity extends BaseActivity {

	private Button backBtn;
	private Button nextBtn;
	private TextView enterSettingsBtn;

	// add by txb 20150128 解决自助开通页面连续点击报停止运行bug
	boolean isEnterOneShotBtnEnable = true;
	// add end
	private EditText validateText;
	private Logger log = Logger.getLogger(OneShotWelcomeActivity.class);
	private InputmethodCtrl ctrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oneshot_welcome_layout);

		enterSettingsBtn = (TextView) findViewById(R.id.enterSettingsBtn);
		nextBtn = (Button) findViewById(R.id.nextBtn);
		validateText = (EditText) findViewById(R.id.validateText);
		if (!StandbyService.isActive && !StandbyActivity.isUnbind) { // 第一次启动应用（待机服务未启动时），重置软键盘
			Log.i("ckh", "第一次OneShotWelcomeActivity onCreate()绑定软键盘");
			StandbyActivity.isUnbind = true;
			InputmethodCtrl ctrl = InputmethodCtrl.getInstance();
			// ctrl.initialization(this, 0, 350, 800, 250,
			// "com.centerm.lklcpos");
			// ctrl.setAttrDirect(0, 350, 800, 250); //输入法位置参数设置
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
		StandbyService.stopScreenTimer();// 取消屏幕超时机制
		// 激活
		nextBtn.setOnClickListener(new View.OnClickListener() {
			private String validateCode;

			@Override
			public void onClick(View v) {
				validateCode = validateText.getText().toString();
				if (validateCode == null || "".equals(validateCode)) {
					DialogFactory.showTips(OneShotWelcomeActivity.this, "激活码不能为空,请输入");
					return;
				}
				if (validateCode.length() != 12) {
					DialogFactory.showTips(OneShotWelcomeActivity.this, "请输入12位激活码");
					return;
				}
				log.debug("验证码 ,validateCode =" + validateCode);
				Intent intent = new Intent(OneShotWelcomeActivity.this, OneShotNetworkActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("validateCode", validateCode);
				bundle.putString("step", "1");
				intent.putExtras(bundle);
				startActivity(intent);
				addActivityAnim();
				intent = null;
			}
		});

		// 进入设置项
		enterSettingsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Toast.makeText(OneShotWelcomeActivity.this, "进入设置",
				// 0).show();
				Intent intent = new Intent(OneShotWelcomeActivity.this, OperpswdActivity.class);
				startActivityForResult(intent, 2);
				addActivityAnim();
				intent = null;
			}
		});

		ctrl = InputmethodCtrl.getInstance();
	}

	/*
	 * // 权限验证返回结果处理 protected void onActivityResult(int requestCode, int
	 * resultCode, Intent data) { switch (requestCode) { case 0: if (resultCode
	 * == RESULT_OK){ } break; case 1: break; case 2: if (resultCode ==
	 * RESULT_OK){ Intent intent = new Intent(OneShotWelcomeActivity.this,
	 * SettingMainActivity.class); startActivity(intent); addActivityAnim(); }
	 * break; default: break; } }
	 */

	@Override
	protected void onResume() {
		ctrl.setInputMode123();
		super.onResume();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("OneShotWelcomeActivity", "keyCode = " + keyCode);

		switch (keyCode) {

		case KEYCODE_HOME:
		case KeyEvent.KEYCODE_HOME:
		case KeyEvent.KEYCODE_BACK:
			Intent intent = new Intent();
			intent.setAction("com.lkl.farmer.dailog");
			startActivity(intent);
			addActivityAnim();
			intent = null;
			break;
		default:
			break;
		}
		return true;
	}
	/*
	 * @Override protected void onDestroy(){ super.onDestroy(); }
	 * 
	 * @Override protected void onStart() { // TODO Auto-generated method stub
	 * super.onStart(); StandbyService.isOneShotStatus = true; if
	 * (TradeBaseActivity.isTransStatus) { // 关闭交易状态，状态控制 Log.i("ckh",
	 * "关闭终端交易状态"); TradeBaseActivity.isTransStatus = false;
	 * //StandbyService.onOperate(); } StandbyService.onOperate(); }
	 * 
	 * @Override protected void onPause() { // TODO Auto-generated method stub
	 * if (StandbyActivity.isUnbind) { Log.i("ckh",
	 * "OneShotWelcomeActivity onPause()重置输入模式");
	 * InputmethodCtrl.getInstance().setInputMode123(); } super.onPause(); }
	 * 
	 * @Override protected void onStop() { // TODO Auto-generated method stub
	 * StandbyService.isOneShotStatus = false; //add by txb 20150128
	 * isEnterOneShotBtnEnable = true; //add end StandbyService.onOperate(); if
	 * (StandbyActivity.isUnbind) { Log.i("ckh", "OneShotWelcomeActivity解绑输入法");
	 * InputmethodCtrl.getInstance().unestablish(this); StandbyActivity.isUnbind
	 * = false; } super.onStop(); }
	 */

}
