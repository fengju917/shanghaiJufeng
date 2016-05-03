package com.centerm.lklcpos.activity;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

public class NonContactResultActivity extends BaseActivity {

	private Logger log = Logger.getLogger(NonContactResultActivity.class);

	private TextView result_text;
	private TextView banlance_text;
	private Button confirmBtn;
	private LinearLayout lablebg;
	private ImageView iconImageView;
	private TextView lableTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.show_result);
		inititle();
		Bundle b = this.getIntent().getExtras();

		String balance = b.getString("balance");

		log.info("balance == " + balance);

		result_text = (TextView) this.findViewById(R.id.result_text);
		banlance_text = (TextView) this.findViewById(R.id.result_massage_text);
		confirmBtn = (Button) findViewById(R.id.ok_button);
		lablebg = (LinearLayout) findViewById(R.id.lablebg);
		iconImageView = (ImageView) findViewById(R.id.lableicon);
		lableTextView = (TextView) findViewById(R.id.lablemessage);

		lablebg.setBackgroundResource(R.drawable.topbg_grey);
		iconImageView.setBackgroundResource(R.drawable.syue_icon);
		lableTextView.setText(R.string.consume_search_text);

		result_text.setText("账户余额：");
		banlance_text.setText("RMB " + (balance == null ? "" : Utility.unformatMount(balance)));
		banlance_text.setTextSize(42);

		confirmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int count = lklcposActivityManager.activityCount(OtherActivity.class);
				if (count == 0) {
					Intent intent = new Intent();
					intent.setClass(NonContactResultActivity.this, OtherActivity.class);
					startActivity(intent);
				}
				lklcposActivityManager.removeAllActivityExceptOne(OtherActivity.class);
				outActivityAnim();
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		// return super.onKeyDown(keyCode, event);

		log.debug("keyCode = [" + keyCode + "]");
		return true;
	}

}
