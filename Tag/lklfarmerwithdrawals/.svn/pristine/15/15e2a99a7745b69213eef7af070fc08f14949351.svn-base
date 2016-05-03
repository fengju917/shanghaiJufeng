package com.centerm.lklcpos.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/**
 * 输入交易日期组件
 * 
 * @author Tianxiaobo
 * 
 */
public class InputTransDate extends TradeBaseActivity {

	private EditText mEditText;
	private Button nextBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_trans_date);
		inititle();
		mEditText = (EditText) findViewById(R.id.transdate);
		nextBtn = (Button) findViewById(R.id.nextDateBtn);
		nextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String transDate = mEditText.getText().toString(); // 获取文本呢
				if (null == transDate || "".equals(transDate)) { // 如果参考号为空
					DialogFactory.showTips(InputTransDate.this, "日期输入不能为空");
					mEditText.setFocusable(true);
				} else if (transDate.length() != 4 || !InputTransDate.this.isvalidDate(transDate)) {
					DialogFactory.showTips(InputTransDate.this, "输入的日期格式不符合要求，请重新输入");
					mEditText.setText("");
				} else {
					mTransaction.getDataMap().put("oldTransDate", transDate);
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

	/**
	 * 方法描述:判断输入的日期是否为合法日期
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-7-5 上午11:42:52
	 * @param date
	 * @return boolean
	 */
	public boolean isvalidDate(String date) {
		boolean flag = false;
		Date dateObj = new Date();
		int month = 0;
		int day = 0;
		if (null != date && !"".equals(date)) {
			month = Integer.parseInt(date.substring(0, 2)); // 月份
			day = Integer.parseInt(date.substring(2)); // 日期
		}
		String year = new SimpleDateFormat("yyyy").format(dateObj);
		int yearInt = Integer.parseInt(year);
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			if (month > 0 && month < 13 && day > 0 && day < 32) {
				flag = true;
			} else {
				flag = false;
			}
			break;
		case 2:
			if ((yearInt % 4 == 0 && yearInt % 100 != 0) || (yearInt % 400 == 0)) { // 如果为闰年
																					// 2月29天
				if (month > 0 && month < 13 && day > 0 && day < 30) {
					flag = true;
				} else {
					flag = false;
				}
			} else { // 平年2月28天
				if (month > 0 && month < 13 && day > 0 && day < 29) {
					flag = true;
				} else {
					flag = false;
				}
			}
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			if (month > 0 && month < 13 && day > 0 && day < 31) {
				flag = true;
			} else {
				flag = false;
			}
			break;
		default:
			flag = false;
			break;
		}
		return flag;
	}

}
