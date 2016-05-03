package com.centerm.lklcpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

/**
 * 输入原交易批次号
 */
public class InputTransBatchno extends TradeBaseActivity {

	private TextView info_lable;
	private EditText mEditText;
	private Button nextBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_offline_refund_info);
		inititle();
		info_lable = (TextView) findViewById(R.id.trans_info_lable);
		mEditText = (EditText) findViewById(R.id.trans_info);
		nextBtn = (Button) findViewById(R.id.next_button);

		info_lable.setText("请输入原交易批次号");
		mEditText.setHint("请输入原交易批次号");
		mEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
		nextBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String transInfo = mEditText.getText().toString(); // 获取文本呢
				if (null == transInfo || "".equals(transInfo)) { // 如果原交易批次号为空
					DialogFactory.showTips(InputTransBatchno.this, "原交易批次号输入不能为空");
				} else if (transInfo.length() > 6) {
					DialogFactory.showTips(InputTransBatchno.this, "原交易批次号位数不对");
				} else {
					transInfo = Utility.addZeroForNum(transInfo, 6);
					mTransaction.getDataMap().put("batchbillno", transInfo);
					Intent nextIntent = forward("1");
					nextIntent.putExtra("transaction", mTransaction);
					startActivity(nextIntent);
					addActivityAnim();
				}
			}
		});
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		mEditText.setText("");
		super.onStart();
	}

}
