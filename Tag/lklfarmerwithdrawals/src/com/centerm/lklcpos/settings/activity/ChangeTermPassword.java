package com.centerm.lklcpos.settings.activity;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.iso8583.util.DataConverter;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.mid.util.M3Utility;
import com.lkl.farmerwithdrawals.R;

/**
 * 修改终端密码
 * 
 * @author Tianxiaobo
 *
 */
public class ChangeTermPassword extends BaseActivity implements OnClickListener {

	private static final Logger log = Logger.getLogger(ChangeTermPassword.class);
	// 公共组件
	private InputmethodCtrl ctrl = null; // 输入法控制类

	// 数据库操作接口
	private ParamConfigDao paramDao = null;

	// 修改系统管理员密码
	private EditText administratorOldPasswordEt = null; // 系统管理员旧密码
	private EditText administratorNewPasswordEt = null; // 系统管理员新密码
	private EditText administratroNewConfirmPasswordEt = null; // 系统管理员新密码确认
	private Button administratorBut = null; // 修改系统管理员密码按钮

	// 修改主管操作员密码
	private EditText adminNewPasswordEt = null; // 主管操作员密码
	private EditText adminNewConfirmPasswordEt = null; // 主管操作员密码确认
	private Button adminBut = null; // 修改管理员密码

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.setContentView(R.layout.change_term_password);
		inititle(); // 调用父类中的方法，初始化返回按钮
		paramDao = new ParamConfigDaoImpl(this);
		ctrl = InputmethodCtrl.getInstance();
		// 设置标题栏内容为终端参数设置
		TextView transType = (TextView) super.findViewById(R.id.transType);
		transType.setText(R.string.change_password); // 设置为终端通讯参数设置
		transType.setVisibility(View.VISIBLE); // 设置交易类型控件可见
		initComponent();
	}

	/**
	 * 方法描述：组件初始化
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-11-1 上午9:45:29
	 */
	public void initComponent() {
		// 修改系统管理员密码
		administratorOldPasswordEt = (EditText) super.findViewById(R.id.change_administrator_old_passwordEt);
		administratorNewPasswordEt = (EditText) super.findViewById(R.id.change_administrator_new_passwordEt);
		administratroNewConfirmPasswordEt = (EditText) super.findViewById(
				R.id.change_administrator_new_confirm_passwordEt);
		administratorBut = (Button) super.findViewById(R.id.change_administrator_passwordBut);
		// 修改主管操作员密码
		adminNewPasswordEt = (EditText) super.findViewById(R.id.change_admin_new_passwordEt);
		adminNewConfirmPasswordEt = (EditText) super.findViewById(R.id.change_admin_new_confirm_passwordEt);
		adminBut = (Button) super.findViewById(R.id.change_admin_passwordBut);

		setListener(); // 设置监听事件
	}

	public void setListener() {
		administratorBut.setOnClickListener(this);
		adminBut.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		initComponent();
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	/**
	 * 单击事件处理函数，用case进行区分点击那个按钮
	 */
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.change_administrator_passwordBut: // 修改系统管理员密码
			String oldAdministratorPassword = paramDao.get("operpswd");
			String oldAdministratorPasswordInput = administratorOldPasswordEt.getText().toString().trim();
			String newAdministratorPasswordInput = administratorNewPasswordEt.getText().toString().trim();
			String newAdministratorPasswordConfirmInput = administratroNewConfirmPasswordEt.getText().toString().trim();
			if (this.isEmpty(oldAdministratorPasswordInput) || oldAdministratorPasswordInput.length() != 8) {
				DialogFactory.showTips(this, "旧密码为空或者长度不足8位");
				return;
			}
			if (this.isEmpty(newAdministratorPasswordInput) || newAdministratorPasswordInput.length() != 8) {
				DialogFactory.showTips(this, "新密码为空或者长度不足8位");
				return;
			}
			if (this.isEmpty(newAdministratorPasswordConfirmInput)
					|| newAdministratorPasswordConfirmInput.length() != 8) {
				DialogFactory.showTips(this, "确认新密码为空或者长度不足8位");
				return;
			}
			if (!newAdministratorPasswordConfirmInput.equals(newAdministratorPasswordInput)) {
				DialogFactory.showTips(this, "两次输入的新密码不一致");
				return;
			}
			log.debug("txb 旧的密码为" + oldAdministratorPassword);
			log.debug("txb输入的旧密码为:" + DataConverter.MD5EncodeToHex(oldAdministratorPasswordInput));
			if (!DataConverter.MD5EncodeToHex(oldAdministratorPasswordInput)
					.equalsIgnoreCase(oldAdministratorPassword)) { // 旧密码验证失败
				DialogFactory.showTips(this, "旧密码输入不正确");
				return;
			} else {
				int result = paramDao.update("operpswd", DataConverter.MD5EncodeToHex(newAdministratorPasswordInput));
				if (result == 1) {
					DialogFactory.showTips(this, "系统管理员密码修改成功");
				} else {
					DialogFactory.showTips(this, "系统管理员密码修改失败");
				}
				administratorOldPasswordEt.setText(""); // 清空编辑框
				administratorNewPasswordEt.setText(""); // 清空编辑框
				administratroNewConfirmPasswordEt.setText(""); // 清空编辑框
			}
			lklcposActivityManager.removeActivity(this);
			outActivityAnim();
			M3Utility.sync();
			break;
		case R.id.change_admin_passwordBut: // 修改主管操作员密码
			String adminNewPassword = adminNewPasswordEt.getText().toString().trim();
			String adminNewConfirmPassword = adminNewConfirmPasswordEt.getText().toString().trim();
			if (this.isEmpty(adminNewPassword) || adminNewPassword.length() != 6) {
				DialogFactory.showTips(this, "新密码为空或者长度不足6位");
				return;
			}
			if (this.isEmpty(adminNewPassword) || adminNewPassword.length() != 6) {
				DialogFactory.showTips(this, "确认新密码为空或者长度不足6位");
				return;
			}
			if (!adminNewPassword.equals(adminNewConfirmPassword)) {
				DialogFactory.showTips(this, "两次输入的新密码不一致");
				return;
			}
			int result = paramDao.update("adminpwd", DataConverter.MD5EncodeToHex(adminNewPassword));
			if (result == 1) {
				DialogFactory.showTips(this, "主管操作员密码修改成功");
			} else {
				DialogFactory.showTips(this, "主管操作员密码修改失败");
			}
			adminNewPasswordEt.setText(""); // 清空编辑框
			adminNewConfirmPasswordEt.setText(""); // 清空编辑框
			lklcposActivityManager.removeActivity(this);
			outActivityAnim();
			M3Utility.sync();
			break;
		default:
			return;
		}
	}

	/**
	 * 方法描述：判空操作
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-1-4 下午7:17:18
	 * @param val
	 * @return
	 */
	public boolean isEmpty(String val) {
		if (null == val || "".equals(val)) {
			return true;
		} else {
			return false;
		}
	}
}
