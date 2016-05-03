package com.centerm.lklcpos.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
 * 管理菜单
 * 
 * @author Tianxiaobo
 *
 */
public class ChangePasswordActivity extends BaseActivity {

	private EditText oldpassword = null; // 旧密码
	private EditText newpassword = null; // 新密码
	private EditText newconfirmpassword = null; // 新密码确认
	private Button saveBut = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.change_password);
		inititle(); // 初始化title标题

		// 获取组件并添加处理事件
		oldpassword = (EditText) super.findViewById(R.id.oldpassword);
		newpassword = (EditText) super.findViewById(R.id.newpassword);
		newconfirmpassword = (EditText) super.findViewById(R.id.newconfirmpassword);
		/*
		 * newconfirmpassword.setOnEditorActionListener(new
		 * TextView.OnEditorActionListener() {
		 * 
		 * @Override public boolean onEditorAction(TextView arg0, int actionId,
		 * KeyEvent arg2) { // TODO Auto-generated method stub
		 * InputMethodManager inputMethodManager = (InputMethodManager)
		 * getSystemService(Context.INPUT_METHOD_SERVICE);
		 * 
		 * inputMethodManager.hideSoftInputFromWindow(
		 * ChangePasswordActivity.this.getCurrentFocus() .getWindowToken(),
		 * 
		 * InputMethodManager.HIDE_NOT_ALWAYS); saveBut.requestFocus(); return
		 * true; } });
		 */
		saveBut = (Button) super.findViewById(R.id.save); // 保存按钮
		saveBut.setOnClickListener(new SaveOnclickListernerImpl());
	}

	// 内部类处理保存按钮事件内容
	private class SaveOnclickListernerImpl implements OnClickListener {
		public void onClick(View v) {
			String oldPassword = oldpassword.getText().toString(); // 原来的密码
			String newPassword = newpassword.getText().toString(); // 新密码
			String newconfirmPassword = newconfirmpassword.getText().toString(); // 新密码确认
			ParamConfigDao paramDao = new ParamConfigDaoImpl(ChangePasswordActivity.this);
			String adminpwd = paramDao.get("adminpwd"); // 获取数据库存储的管理员密码
			Log.i("ckh", "adminpwd == " + adminpwd + "    DataConverter.MD5EncodeToHex(oldPassword)== "
					+ DataConverter.MD5EncodeToHex(oldPassword));
			if (null == oldPassword || "".equals(oldPassword) // 执行判空操作
					|| null == newPassword || "".equals(newPassword) || null == newconfirmPassword
					|| "".equals(newconfirmPassword)) {
				DialogFactory.showTips(ChangePasswordActivity.this, "数据填写不完整，请重填");
			} else if (newPassword.length() < 6) {
				DialogFactory.showTips(ChangePasswordActivity.this, "新密码不少于6位");
			} else if (!newPassword.equals(newconfirmPassword)) {
				DialogFactory.showTips(ChangePasswordActivity.this, "两次新密码确认输入不一致");
			} else if (!adminpwd.equalsIgnoreCase(DataConverter.MD5EncodeToHex(oldPassword))) { // 更新数据库中的密码
				DialogFactory.showTips(ChangePasswordActivity.this, "旧密码校验失败");
			} else {
				int result = paramDao.update("adminpwd", DataConverter.MD5EncodeToHex(newPassword));
				if (1 == result) {
					DialogFactory.showTips(ChangePasswordActivity.this, "密码修改成功");
					lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
				} else {
					DialogFactory.showTips(ChangePasswordActivity.this, "密码修改失败");
				}
				oldpassword.setText("");
				newpassword.setText("");
				newconfirmpassword.setText("");
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
