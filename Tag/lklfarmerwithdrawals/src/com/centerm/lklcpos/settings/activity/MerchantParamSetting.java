package com.centerm.lklcpos.settings.activity;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-7-25 商户参数设置页面
 */
public class MerchantParamSetting extends BaseActivity {
	private EditText contactNumEditText;
	private EditText termNumEditText;
	private EditText contactNameEditText;
	private Button saveButton;
	private ParamConfigDao mParamConfigDao;

	private InputmethodCtrl ctrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_settings_layout);
		inititle();

		contactNumEditText = (EditText) this.findViewById(R.id.contact_num);
		termNumEditText = (EditText) this.findViewById(R.id.term_num);
		contactNameEditText = (EditText) this.findViewById(R.id.contact_name_text);
		saveButton = (Button) this.findViewById(R.id.save);

		mParamConfigDao = new ParamConfigDaoImpl(this);
		contactNumEditText.setText(mParamConfigDao.get("merid"));
		termNumEditText.setText(mParamConfigDao.get("termid"));
		contactNameEditText.setText(mParamConfigDao.get("mchntname"));

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 商户号设置判定
				String contactString = contactNumEditText.getText().toString();
				if ("".equals(contactString) || contactString == null) {
					DialogFactory.showTips(MerchantParamSetting.this, "商户号不能设置为空！");
					return;
				} else {
					if (contactString.length() != 15) {
						DialogFactory.showTips(MerchantParamSetting.this, "商户号位数格式不对请检查！");
						return;
					}
				}
				// 终端号设置判定
				String term = termNumEditText.getText().toString();
				if ("".equals(term) || term == null) {
					DialogFactory.showTips(MerchantParamSetting.this, "终端号不能设置为空！");
					return;
				} else {
					if (term.length() != 8) {
						DialogFactory.showTips(MerchantParamSetting.this, "终端号位数格式不对请检查！");
						return;
					}
				}

				Map<String, String> map = new HashMap<String, String>();
				map.put("merid", contactNumEditText.getText().toString());
				map.put("termid", termNumEditText.getText().toString());
				map.put("mchntname", contactNameEditText.getText().toString());
				int ret = mParamConfigDao.update(map);
				if (ret == 3) {
					DialogFactory.showTips(MerchantParamSetting.this, "保存成功!");
					lklcposActivityManager.removeActivity(MerchantParamSetting.this);
					outActivityAnim();
				} else {
					DialogFactory.showTips(MerchantParamSetting.this, "保存失败!");
				}
			}
		});

		ctrl = InputmethodCtrl.getInstance();
		contactNameEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					ctrl.setInputModePinyin();
				} else {
					ctrl.setInputMode123();
				}
			}
		});
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		ctrl.setInputMode123();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// 在onresume方法中初始化输入法
		super.onResume();
	}

}
