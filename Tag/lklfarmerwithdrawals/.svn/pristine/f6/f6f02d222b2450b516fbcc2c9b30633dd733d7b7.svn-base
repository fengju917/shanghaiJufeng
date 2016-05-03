package com.centerm.lklcpos.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.lkl.farmerwithdrawals.R;

public class NoticeActivity extends Activity {
	private Button yes_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notice);
		yes_btn = (Button) findViewById(R.id.yes_btn);
		yes_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(NoticeActivity.this, MenuSpaceActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

}
