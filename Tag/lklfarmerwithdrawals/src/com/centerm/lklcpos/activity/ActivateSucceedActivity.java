package com.centerm.lklcpos.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.lkl.farmerwithdrawals.R;

public class ActivateSucceedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_activate_succeed);
		Intent intent = getIntent();
		boolean result = intent.getBooleanExtra("result", false);
		if (result) {
			handler.sendEmptyMessageDelayed(1, 2000);
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				finish();
				break;
			}

		};
	};
}
