package com.centerm.lklcpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.iso8583.util.DataConverter;
import com.centerm.lklcpos.settings.activity.SettingMainActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

public class OperpswdActivity extends BaseActivity {

	private EditText mEditText;
	private Button mButton;
	private ParamConfigDao mParamConfigDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.operpswd_layout);

		inititle();
		mParamConfigDao = new ParamConfigDaoImpl(this);
		mEditText = (EditText) findViewById(R.id.input_operpswd);
		mButton = (Button) findViewById(R.id.ok_button);
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 获取用户名和密码
				// String mapPswd =
				// Md5Util.getMd5String(pswdET.getText().toString());
				// //数据加密，明文转密文
				String mapPswd = mEditText.getText().toString();
				// 进行输入校验
				if (mapPswd == null || "".equals(mapPswd)) {
					DialogFactory.showTips(OperpswdActivity.this, "密码不能为空，请输入！");
					return;
				}
				mapPswd = DataConverter.MD5EncodeToHex(mapPswd); // 单向MD5加密
				String adminPwd = mParamConfigDao.get("operpswd");
				if (!adminPwd.equals(mapPswd)) {// 存储的管理员密码数据
					DialogFactory.showTips(OperpswdActivity.this, "密码错误，请重新输入！");
					mEditText.setText("");
					return;
				}
				// 启动操作界面
				// mEditText.setText("");
				Intent intent = new Intent(OperpswdActivity.this, SettingMainActivity.class);
				startActivity(intent);
				setResult(RESULT_OK);
				lklcposActivityManager.removeActivity(OperpswdActivity.this);
			}

		});
		/*
		 * mEditText .setOnEditorActionListener(new
		 * TextView.OnEditorActionListener() {
		 * 
		 * @Override public boolean onEditorAction(TextView arg0, int actionId,
		 * KeyEvent arg2) { // TODO Auto-generated method stub
		 * InputMethodManager inputMethodManager = (InputMethodManager)
		 * getSystemService(Context.INPUT_METHOD_SERVICE);
		 * 
		 * inputMethodManager.hideSoftInputFromWindow(
		 * OperpswdActivity.this.getCurrentFocus() .getWindowToken(),
		 * 
		 * InputMethodManager.HIDE_NOT_ALWAYS); mButton.requestFocus(); return
		 * true; } });
		 */

	}

}
