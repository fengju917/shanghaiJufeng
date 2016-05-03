package com.centerm.lklcpos.settings.activity;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc;
import com.centerm.lklcpos.deviceinterface.PinPadDevJsIfc;
import com.centerm.lklcpos.deviceinterface.PinPadInterface;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.util.M3HexUtil;
import com.lkl.farmerwithdrawals.R;

public class OperLoadDownMak extends BaseActivity {
	private static Logger logger = Logger.getLogger(OperLoadDownMak.class);

	private EditText mkeyidEditText;
	private EditText premakEditText;
	private EditText nextmakEditText;
	private Button saveButton;
	private String pinpadType;
	private String tmkKeyId_SetOff;
	private String tmkKeyId; // 目前终端正在使用的主密钥索引值
	private ParamConfigDao mParamConfigDao;
	private PinPadInterface pinPadDev = null;

	private InputmethodCtrl ctrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.operloaddownmak);

		inititle();

		mkeyidEditText = (EditText) findViewById(R.id.mkey_edit);
		premakEditText = (EditText) findViewById(R.id.premak_edit);
		nextmakEditText = (EditText) findViewById(R.id.nextmak_edit);
		saveButton = (Button) findViewById(R.id.save);

		ctrl = InputmethodCtrl.getInstance();
		premakEditText.setOnFocusChangeListener(focusChangeListener);
		nextmakEditText.setOnFocusChangeListener(focusChangeListener);

		mParamConfigDao = new ParamConfigDaoImpl(this);
		pinpadType = mParamConfigDao.get("pinpadType");
		if ("0".equals(pinpadType)) {
			try {
				pinPadDev = new ExPinPadDevJsIfc(this, null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
			}
		} else {
			try {
				pinPadDev = new PinPadDevJsIfc(this, null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
			}
		}

		tmkKeyId_SetOff = mParamConfigDao.get("tmkKeyId_setOff");
		if ("".equals(tmkKeyId_SetOff) || tmkKeyId_SetOff == null) {
			tmkKeyId_SetOff = "1";
		}
		tmkKeyId = mParamConfigDao.get("newmkeyid");
		if ("".equals(tmkKeyId) || tmkKeyId == null) {
			tmkKeyId = "1";
		}

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String keystr = mkeyidEditText.getText().toString();
				String makstr = premakEditText.getText().toString() + nextmakEditText.getText().toString();
				if (keystr == null || "".equals(keystr)) {
					DialogFactory.showTips(OperLoadDownMak.this, "主密钥索引不能为空");
					return;
				}
				int id = Integer.valueOf(keystr);
				if (id >= 10 || id < 0) {
					DialogFactory.showTips(OperLoadDownMak.this, "主密钥索引值不合法");
					return;
				}
				if (makstr == null || "".equals(makstr)) {
					DialogFactory.showTips(OperLoadDownMak.this, "主密钥不能为空");
					return;
				}
				if (makstr.length() != 32) {
					DialogFactory.showTips(OperLoadDownMak.this, "主密钥长度必须为32");
					return;
				}

				byte mkeyid = (byte) (Integer.parseInt(keystr) + Integer.parseInt(tmkKeyId_SetOff)); // 加上偏移量

				byte[] tmk = M3HexUtil.hexStringToByte(makstr);
				try {
					pinPadDev.operDownloadMkey(mkeyid, tmk);

					if (mkeyid == Byte.valueOf(tmkKeyId)) { // 判断是否需要重新签到
						Utility.setSignStatus(OperLoadDownMak.this, false);// 标志签退操作
						DialogFactory.showTips(OperLoadDownMak.this, "手工输入主密钥成功,请签到");
					} else {
						DialogFactory.showTips(OperLoadDownMak.this, "手工输入主密钥成功");
					}
					lklcposActivityManager.removeActivity(OperLoadDownMak.this);
					outActivityAnim();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("", e);
					DialogFactory.showTips(OperLoadDownMak.this, "手工输入主密钥失败！");
				}
			}
		});
	}

	private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if (hasFocus) {
				ctrl.setInputModePinyin();
			} else {
				ctrl.setInputMode123();
			}
		}
	};

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		ctrl.setInputMode123();
		super.onPause();
	}
}
