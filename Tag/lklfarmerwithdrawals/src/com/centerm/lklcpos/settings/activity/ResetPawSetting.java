/**
 * 
 */
package com.centerm.lklcpos.settings.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.iso8583.util.DataConverter;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-7-26
 * 
 */
public class ResetPawSetting extends BaseActivity {

	private EditText firstEditText;
	private EditText secondEditText;
	private Button saveButton;
	private ParamConfigDao mParamConfigDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resetpwd_settings_layout);
		inititle();
		firstEditText = (EditText) this.findViewById(R.id.first_pwd_edit);
		secondEditText = (EditText) this.findViewById(R.id.second_paw_edit);
		saveButton = (Button) this.findViewById(R.id.save);
		mParamConfigDao = new ParamConfigDaoImpl(this);

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String firstPwd = firstEditText.getText().toString();
				String secondPwd = secondEditText.getText().toString();
				String tipString = null;
				if ("".equals(firstPwd) || "".equals(secondPwd)) {
					tipString = ResetPawSetting.this.getResources().getString(R.string.tip_reset_null);
				} else if (firstPwd.length() < 6) {
					DialogFactory.showTips(ResetPawSetting.this, "新密码不能少于6位");
				} else if (!firstPwd.equals(secondPwd)) {
					tipString = ResetPawSetting.this.getResources().getString(R.string.tip_reset_unsuccess);
				} else {
					int ret = mParamConfigDao.update("adminpwd", DataConverter.MD5EncodeToHex(firstPwd));
					if (ret == 1)
						tipString = ResetPawSetting.this.getResources().getString(R.string.tip_reset_success);
				}
				DialogFactory.showTips(ResetPawSetting.this, tipString);
				if (ResetPawSetting.this.getResources().getString(R.string.tip_reset_success).equals(tipString)) {
					lklcposActivityManager.removeActivity(ResetPawSetting.this);
					outActivityAnim();
				}
			}
		});
	}

}
