package com.centerm.lklcpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-8-28
 * 
 */
public class InputCardValidDate extends TradeBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_cardvaliddate);
		inititle();

		final EditText validdate = (EditText) findViewById(R.id.validdate);
		Button nextBtn = (Button) findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String date = validdate.getText().toString();
				if (date.length() == 4) {// 有效期只核对逻辑有效性，具体的值提交给服务器去判断
					if (Integer.valueOf(date.substring(2, 4)) > 0 && Integer.valueOf(date.substring(2, 4)) < 13) {
						Log.i("ckh", "date == " + date);
						mTransaction.getDataMap().put("expireddate", date);
						Intent nextIntent = forward("1");
						nextIntent.putExtra("transaction", mTransaction);
						startActivity(nextIntent);
						addActivityAnim();
					} else {
						DialogFactory.showTips(InputCardValidDate.this, "有效期月份不正确！");
					}
				} else if ("".equals(date)) {
					mTransaction.getDataMap().put("expireddate", date);
					Intent nextIntent = forward("1");
					nextIntent.putExtra("transaction", mTransaction);
					startActivity(nextIntent);
					addActivityAnim();
				} else {
					DialogFactory.showTips(InputCardValidDate.this, "有效期位数不正确！");
				}
				validdate.setText("");
			}
		});
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		mTransaction.getDataMap().put("expireddate", "");
		super.onStart();
	}

}
