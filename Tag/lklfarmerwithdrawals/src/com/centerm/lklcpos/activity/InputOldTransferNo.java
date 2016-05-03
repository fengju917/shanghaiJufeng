package com.centerm.lklcpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/**
 * 输入原交易参考号组件
 * 
 * @author Tianxiaobo
 * 
 */
public class InputOldTransferNo extends TradeBaseActivity {

	private EditText transferno = null;
	private Button nextBut = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.input_transfer_no);
		inititle();

		transferno = (EditText) findViewById(R.id.transferno);
		nextBut = (Button) findViewById(R.id.nextTransBtn);
		nextBut.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				String transferNo = transferno.getText().toString(); // 获取文本呢
				if (null == transferNo || "".equals(transferNo)) { // 如果参考号为空
					DialogFactory.showTips(InputOldTransferNo.this, "参考号输入不能为空");
				} else if (transferNo.length() != 12) {
					DialogFactory.showTips(InputOldTransferNo.this, "原交易参考号必须为12位");
				} else {
					mTransaction.getDataMap().put("refernumber", transferNo);
					Intent nextIntent = forward("1");
					nextIntent.putExtra("transaction", mTransaction);
					startActivity(nextIntent);
					addActivityAnim();
				}
			}
		});
		/*
		 * transferno .setOnEditorActionListener(new
		 * TextView.OnEditorActionListener() {
		 * 
		 * @Override public boolean onEditorAction(TextView arg0, int actionId,
		 * KeyEvent arg2) { // TODO Auto-generated method stub
		 * InputMethodManager inputMethodManager = (InputMethodManager)
		 * getSystemService(Context.INPUT_METHOD_SERVICE);
		 * 
		 * inputMethodManager.hideSoftInputFromWindow(
		 * InputOldTransferNo.this.getCurrentFocus() .getWindowToken(),
		 * 
		 * InputMethodManager.HIDE_NOT_ALWAYS); nextBut.requestFocus(); return
		 * true; } });
		 */
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		transferno.setText("");
		super.onStart();
	}
}
