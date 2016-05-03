package com.centerm.lklcpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

public class DownLoadCerActivity extends TradeBaseActivity {

	private TextView accidTextView;
	private TextView termidTextView;
	private EditText cerpwdEdit;
	private Button okButton;

	private ParamConfigDao mParamConfigDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.downloadcer_layout);
		inititle();

		accidTextView = (TextView) findViewById(R.id.contac_no_text);
		termidTextView = (TextView) findViewById(R.id.term_no_text);
		cerpwdEdit = (EditText) findViewById(R.id.cerpwd_text);
		okButton = (Button) findViewById(R.id.ok_button);

		mParamConfigDao = new ParamConfigDaoImpl(this);

		accidTextView.setText(mParamConfigDao.get("merid"));
		termidTextView.setText(mParamConfigDao.get("termid"));

		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String pswd = cerpwdEdit.getText().toString();
				if (null == pswd || "".equals(pswd)) { // 如果私钥为null或者空
					DialogFactory.showTips(DownLoadCerActivity.this, "私钥密码不能为空，请重填!");
					return; // 结束onclick函数执行
				}
				mTransaction.getDataMap().put("capwd", pswd);
				Intent mIntent = forward("1");
				mIntent.putExtra("transaction", mTransaction);
				startActivity(mIntent);
				addActivityAnim();
				// lklcposActivityManager.removeActivity(DownLoadCerActivity.this);
			}
		});
	}

}
