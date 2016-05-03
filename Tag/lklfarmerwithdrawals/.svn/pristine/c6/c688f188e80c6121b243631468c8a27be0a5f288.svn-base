package com.centerm.lklcpos.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.iso8583.util.DataConverter;
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/**
 * 操作授权界面 作者：cxy 时间：2013.06.20
 */
public class AuthorizeActivity extends BaseActivity implements OnClickListener {

	private EditText pswdET;
	private Button sumitBtn, cancelBtn;
	private ParamConfigDao mParamConfigDao;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authorize);
		init();
		setListener();
		/*
		 * try{ getWindow().setSoftInputMode(
		 * WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); Method
		 * setShowSoftInputOnFocus =
		 * pswdET.getClass().getMethod("setShowSoftInputOnFocus",
		 * boolean.class); setShowSoftInputOnFocus.setAccessible(true);
		 * setShowSoftInputOnFocus.invoke(pswdET, false); }catch (Exception e) {
		 * // TODO: handle exception e.printStackTrace(); }
		 */

		mParamConfigDao = new ParamConfigDaoImpl(this);
	}

	/**
	 * 初始化界面控件
	 */
	public void init() {
		inititle();
		pswdET = (EditText) findViewById(R.id.pswdET);
		pswdET.requestFocus();
		sumitBtn = (Button) findViewById(R.id.sumitBtn);
	}

	/**
	 * 设置界面按钮的事件监听
	 */
	public void setListener() {
		sumitBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.sumitBtn:
			loginCheckAction();
			break;
		}
	}

	/**
	 * 登陆操作的事件处理
	 */
	private void loginCheckAction() {
		// 获取用户名和密码
		// String mapPswd = Md5Util.getMd5String(pswdET.getText().toString());
		// //数据加密，明文转密文
		String mapPswd = pswdET.getText().toString();
		// 进行输入校验
		if (mapPswd == null || "".equals(mapPswd)) {
			DialogFactory.showTips(AuthorizeActivity.this, "密码不能为空，请输入！");
			return;
		}
		mapPswd = DataConverter.MD5EncodeToHex(mapPswd); // 单向MD5加密
		String adminPwd = mParamConfigDao.get("adminpwd");
		if (!adminPwd.equals(mapPswd)) {// 存储的管理员密码数据
			DialogFactory.showTips(AuthorizeActivity.this, "密码错误，请重新输入！");
			pswdET.setText("");
			return;
		}
		// 启动操作界面
		// pswdET.setText("");
		// Intent intent = new Intent(this, SwipeCardActivity.class);
		// this.startActivity(intent);
		setResult(RESULT_OK);
		lklcposActivityManager.removeActivity(this);
	}
}