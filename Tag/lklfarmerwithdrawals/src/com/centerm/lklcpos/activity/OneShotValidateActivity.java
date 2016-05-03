package com.centerm.lklcpos.activity;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

public class OneShotValidateActivity extends BaseActivity {

	private Logger log = Logger.getLogger(OneShotValidateActivity.class);

	private Button nextBtn;
	private EditText validateText;
	private String validateCode;

	private InputmethodCtrl ctrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oneshot_validate);
		nextBtn = (Button) findViewById(R.id.btn_id_confirm);
		validateText = (EditText) findViewById(R.id.text_id_validate);

		nextBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				validateCode = validateText.getText().toString();
				if (validateCode == null || "".equals(validateCode)) {
					DialogFactory.showTips(OneShotValidateActivity.this, "激活码不能为空,请输入");
					return;
				}
				if (validateCode.length() != 12) {
					DialogFactory.showTips(OneShotValidateActivity.this, "请输入12位激活码");
					return;
				}
				log.debug("验证码 ,validateCode =" + validateCode);

				Intent intent = new Intent(OneShotValidateActivity.this, OneShotNetworkActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("validateCode", validateCode);
				bundle.putString("step", "1");
				intent.putExtras(bundle);
				startActivity(intent);
				addActivityAnim();
				intent = null;
			}
		});

		ctrl = InputmethodCtrl.getInstance();
	}

	@Override
	protected void onResume() {
		ctrl.setInputMode123();
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return super.onKeyDown(keyCode, event);
	}

}
