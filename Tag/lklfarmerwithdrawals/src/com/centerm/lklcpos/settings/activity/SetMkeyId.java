package com.centerm.lklcpos.settings.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

public class SetMkeyId extends BaseActivity {

	private EditText editText;
	// private Spinner spinner;
	private TextView curType;
	private Button saveButton;
	private List<String> list = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private String oldmkeyid;
	private String newmkeyid;
	private String pinpadType;
	private String editPinpadType;
	private String tmkKeyId_SetOff;
	private boolean isChangeSignStatu = false;
	private ParamConfigDao mParamConfigDao;

	private String[] connectModes = new String[] { "外置密码键盘", "内置密码键盘" };
	private Dialog dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setmkeyid);

		inititle();

		editText = (EditText) findViewById(R.id.mkeyid_edit);
		// spinner = (Spinner) findViewById(R.id.spinner_type);
		saveButton = (Button) findViewById(R.id.save);
		curType = (TextView) findViewById(R.id.selectModeTv);

		mParamConfigDao = new ParamConfigDaoImpl(this);
		tmkKeyId_SetOff = mParamConfigDao.get("tmkKeyId_setOff");
		if ("".equals(tmkKeyId_SetOff) || tmkKeyId_SetOff == null) {
			tmkKeyId_SetOff = "0";
		}
		pinpadType = mParamConfigDao.get("pinpadType");
		Log.d("pinpadType", "jf:" + pinpadType);
		if ("".equals(pinpadType) || pinpadType == null) {
			pinpadType = "1";
		}
		if ("1".equals(pinpadType)) {
			curType.setText("内置密码键盘");
		} else {
			curType.setText("外置密码键盘");
		}

		editPinpadType = pinpadType;

		newmkeyid = mParamConfigDao.get("newmkeyid"); // 新索引

		if ("".equals(newmkeyid) || newmkeyid == null) {
			newmkeyid = "3";
		}

		String showkeyid = String.valueOf(Integer.valueOf(newmkeyid) - Integer.valueOf(tmkKeyId_SetOff));
		editText.setText(showkeyid);
		saveButton.setOnClickListener(listener);
	}

	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String mkeyid = editText.getText().toString();
			if ("".equals(mkeyid) || mkeyid == null) {
				DialogFactory.showTips(SetMkeyId.this, "主密钥索引不能为空");
				return;
			}
			int id = Integer.valueOf(mkeyid);
			if (id > 5 || id < 0) {
				DialogFactory.showTips(SetMkeyId.this, "主密钥索引值不合法");
				return;
			}

			id = id + Integer.valueOf(tmkKeyId_SetOff); // 加上偏移量保存
			String editmkeyid = String.valueOf(id);
			if (!newmkeyid.equals(editmkeyid)) { // 终端主密钥索引值设置改变
				mParamConfigDao.update("newmkeyid", editmkeyid);
				isChangeSignStatu = true;
				mParamConfigDao.update("oldmkeyid", newmkeyid);
				mParamConfigDao.update("mkeyidsymbol", "1"); // 密钥索引发生改变，记录标记
			}
			if (!pinpadType.equals(editPinpadType)) { // 密码键盘类型设置改变
				isChangeSignStatu = true;
				mParamConfigDao.update("pinpadType", editPinpadType);
			}
			if (isChangeSignStatu) {
				Utility.setSignStatus(SetMkeyId.this, false);// 标志签退操作
				DialogFactory.showTips(SetMkeyId.this, "保存成功，已更改设置，请签到！");
				isChangeSignStatu = false;
			} else {
				DialogFactory.showTips(SetMkeyId.this, "保存成功，未更改设置");
			}
			lklcposActivityManager.removeActivity(SetMkeyId.this);
			outActivityAnim();
		}
	};

	/**
	 * 方法描述：显示内外置选择框
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-12-19 上午10:17:35
	 * @param v
	 */
	public void showSelectMode(View v) {
		AlertDialog.Builder connectModeDialog = new AlertDialog.Builder(SetMkeyId.this);
		connectModeDialog.setTitle("密码键盘类型");
		connectModeDialog.setSingleChoiceItems(connectModes, Integer.valueOf(editPinpadType),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						editPinpadType = String.valueOf(which);
						curType.setText(connectModes[which]);
						dialog.dismiss();
					}

				});
		dialog = connectModeDialog.show();
		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_HOME) {
					return true;
				}
				return false;
			}
		});
		return;
	}
}
