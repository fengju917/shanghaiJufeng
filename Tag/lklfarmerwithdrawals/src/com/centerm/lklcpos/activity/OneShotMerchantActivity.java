package com.centerm.lklcpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.lkl.farmerwithdrawals.R;

public class OneShotMerchantActivity extends BaseActivity {

	private EditText merid;
	private EditText termid;
	private EditText mchntname;

	private Button backBtn;
	private Button nextBtn;

	private ParamConfigDao mParamConfigDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oneshot_merchant_layout);

		TextView transType = (TextView) findViewById(R.id.transType);
		transType.setText("自助开通");

		merid = (EditText) this.findViewById(R.id.merid);
		termid = (EditText) this.findViewById(R.id.termid);
		mchntname = (EditText) this.findViewById(R.id.mchntname);
		nextBtn = (Button) this.findViewById(R.id.next_btn);
		backBtn = (Button) this.findViewById(R.id.back);

		mParamConfigDao = new ParamConfigDaoImpl(this);
		String meridText = mParamConfigDao.get("merid");
		String termidText = mParamConfigDao.get("termid");
		String mchntnameText = mParamConfigDao.get("mchntname");

		merid.setText(meridText);
		termid.setText(termidText);
		mchntname.setText(mchntnameText);

		merid.setCursorVisible(false);
		merid.setFocusable(false);
		merid.setFocusableInTouchMode(false);
		termid.setCursorVisible(false);
		termid.setFocusable(false);
		termid.setFocusableInTouchMode(false);
		mchntname.setCursorVisible(false);
		mchntname.setFocusable(false);
		mchntname.setFocusableInTouchMode(false);

		// 下一步
		nextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(OneShotMerchantActivity.this, OneShotNetworkActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("step", "2");
				intent.putExtras(bundle);
				startActivity(intent);
				addActivityAnim();
				intent = null;
			}

		});

		// 返回
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeActivity();
				outActivityAnim();
			}
		});
	}
}
