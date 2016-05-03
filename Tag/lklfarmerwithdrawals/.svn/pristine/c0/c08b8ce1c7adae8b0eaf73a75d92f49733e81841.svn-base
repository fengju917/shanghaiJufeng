package com.centerm.lklcpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.lklcpos.LklcposActivityManager;
import com.lkl.farmerwithdrawals.R;

/*
 * 提示退出Dialog
 */
public class DialogActivity extends BaseActivity implements View.OnClickListener {
	public String ACTION_FINISH = "com.centerm.androidlklpos.finish";
	private TextView messageTextView;
	private ImageView cancelImageView;
	private ImageView comfirmImageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog);

		messageTextView = (TextView) findViewById(R.id.dialog_message);
		cancelImageView = (ImageView) findViewById(R.id.cancel);
		comfirmImageView = (ImageView) findViewById(R.id.comfirm);
		messageTextView.setText(R.string.message_text);
		cancelImageView.setOnClickListener(this);
		comfirmImageView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.cancel:
			lklcposActivityManager.removeActivity(this);
			break;
		case R.id.comfirm:
			// MenuSpaceActivity.isHasMenuSpace = false;
			// note by txb 20130705
			Log.i("ckh", "已确认退出收单应用");
			Intent intent = new Intent();
			intent.setAction(ACTION_FINISH);
			sendBroadcast(intent);
			lklcposActivityManager.removeAllActivity();
			System.out.println("removeNum:" + LklcposActivityManager.getActivityManager().getActivityNum());
			// System.exit(0);
			// DialogActivity.this.finish();
			break;
		}
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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
