package com.centerm.lklcpos.activity;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.ReverseDaoImpl;
import com.centerm.comm.persistence.impl.SettleDataDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

/*
 * 清除流水数据dialog
 */
public class ClearDataDialogActivity extends DialogActivity {

	private Logger log = Logger.getLogger(ClearDataDialogActivity.class);

	private TextView mTextView;
	private TransRecordDaoImpl mTransRecordDao;
	private ReverseDaoImpl mReverseDao;
	private SettleDataDaoImpl settleDataDao;
	private ParamConfigDao mParamConfigDao;

	private String tag = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (getIntent().hasExtra("clear")) {
			tag = getIntent().getStringExtra("clear");
		}
		mTextView = (TextView) findViewById(R.id.dialog_message);

		if ("trade".equals(tag)) {
			mTextView.setText("确认清除流水数据？");
		} else if ("flushes".equals(tag)) {
			mTextView.setText("确认清除冲正数据？");
		} else if ("settle".equals(tag)) {
			mTextView.setText("确认清除结算状态？");
		} else if ("selfopen".equals(tag)) {
			mTextView.setText("确认商户信息注销？");
		} else if ("reOpen".equals(tag)) {
			mTextView.setText("重复开通将重置商终信息,是否继续？");
		}

		mTransRecordDao = new TransRecordDaoImpl(this);
		mReverseDao = new ReverseDaoImpl(this);
		settleDataDao = new SettleDataDaoImpl(this);
		mParamConfigDao = new ParamConfigDaoImpl(this);
	}

	@Override
	public void onClick(View v) {

		Intent i = new Intent();
		Bundle b = new Bundle();

		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.cancel:
			lklcposActivityManager.removeActivity(ClearDataDialogActivity.this);
			break;
		case R.id.comfirm:
			if ("trade".equals(tag)) {
				mTransRecordDao.deleteAll();
				settleDataDao.delete(); // 清空结算明细表
			} else if ("flushes".equals(tag)) {
				mReverseDao.deleteAll();
			} else if ("settle".equals(tag)) {
				Utility.setSettleStatus(ClearDataDialogActivity.this, false);
			} else if ("selfopen".equals(tag)) {
				b.putString("type", "selfopen");
			} else if ("reOpen".equals(tag)) {
				b.putString("type", "reOpen");
			}
			i.putExtras(b);
			setResult(RESULT_OK, i);
			lklcposActivityManager.removeActivity(ClearDataDialogActivity.this);
			break;
		}
	}

}
