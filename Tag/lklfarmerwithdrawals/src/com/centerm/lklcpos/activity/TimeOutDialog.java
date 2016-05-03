package com.centerm.lklcpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.lkl.farmerwithdrawals.R;

public class TimeOutDialog extends BaseActivity implements View.OnClickListener {
	private final int DIALOG_TIMER = 0x13;
	private boolean isCancel = false;
	private boolean isComfirm = false;
	private TextView dialogmessage;
	private TextView dialogtimer;
	private TextView dialogmessage2;
	private ImageView cancelImageView;
	private ImageView comfirmImageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog);
		dialogmessage = (TextView) findViewById(R.id.dialog_message);
		dialogtimer = (TextView) findViewById(R.id.dialog_timer);
		dialogmessage2 = (TextView) findViewById(R.id.dialog_message2);
		cancelImageView = (ImageView) findViewById(R.id.cancel);
		comfirmImageView = (ImageView) findViewById(R.id.comfirm);

		dialogmessage.setText(R.string.transdialogmessage);
		dialogmessage2.setText(R.string.transdialogmessage2);
		cancelImageView.setOnClickListener(this);
		comfirmImageView.setOnClickListener(this);

		dialogTimerThread.start();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.cancel:
			// ExPinPadActivity.isGoMenuSpace = false;
			isCancel = true;
			lklcposActivityManager.removeActivity(this);
			break;
		case R.id.comfirm:
			isComfirm = true;
			if (WebViewActivity.isCallbyThrid) {
				lklcposActivityManager.removeAllActivityExceptOne(WebViewActivity.class);
			} else {
				overTranscation();
			}
			break;
		}
	}

	private Handler dialogTimerHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case DIALOG_TIMER:
				dialogtimer.setText(msg.getData().getString("dialog_second"));
				break;

			default:
				break;
			}
		}

	};

	private Thread dialogTimerThread = new Thread() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			for (int i = 10; i >= 1; i--) {
				Message mMessage = Message.obtain();
				Bundle bundle = new Bundle();
				bundle.putString("dialog_second", String.valueOf(i));
				mMessage.what = DIALOG_TIMER;
				mMessage.setData(bundle);
				dialogTimerHandler.sendMessage(mMessage);
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (isCancel || isComfirm) {
					return;
				}
			}
			if (WebViewActivity.isCallbyThrid) {
				lklcposActivityManager.removeAllActivityExceptOne(WebViewActivity.class);
			} else {
				overTranscation();
			}
		}

	};

	private void overTranscation() {
		Log.i("ckh", "TimeOutDialog overTranscation()");
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl();
		if (!"1".equals(mParamConfigDao.get("enabled"))) { // δ����
			int count = lklcposActivityManager.activityCount(OneShotWelcomeActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(this, OneShotWelcomeActivity.class);
				startActivity(intent);
			}
			lklcposActivityManager.removeAllActivityExceptOne(OneShotWelcomeActivity.class);
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

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		isCancel = true;
		super.onPause();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_ENTER:
			return true;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
