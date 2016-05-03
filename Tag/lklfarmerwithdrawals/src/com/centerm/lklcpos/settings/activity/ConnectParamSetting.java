/**
 * 
 */
package com.centerm.lklcpos.settings.activity;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-7-25
 * 
 */
public class ConnectParamSetting extends BaseActivity {

	private EditText connectEditText;
	private EditText downloadEditText;

	private EditText tpudEditText;
	private EditText tranTimeEditText;
	private EditText flushesTimesEditText;

	private Button saveButton;
	private ParamConfigDao mParamConfigDao;

	// add by txb 用于选择网络模式
	private Spinner spiner = null;
	private String curTransUrl = null; // 当前交易地址
	private String curDownloadUrl = null; // 当前下载地址

	private InputmethodCtrl ctrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_settings_layout);
		inititle();

		connectEditText = (EditText) this.findViewById(R.id.connect_address_edit);
		downloadEditText = (EditText) this.findViewById(R.id.download_address_edti);

		tpudEditText = (EditText) this.findViewById(R.id.tpud_edit);
		tranTimeEditText = (EditText) this.findViewById(R.id.tran_timeout_edit);
		flushesTimesEditText = (EditText) this.findViewById(R.id.flushes_times_edit);

		saveButton = (Button) this.findViewById(R.id.save);

		ctrl = InputmethodCtrl.getInstance();
		connectEditText.setOnFocusChangeListener(focusChangeListener);
		downloadEditText.setOnFocusChangeListener(focusChangeListener);

		mParamConfigDao = new ParamConfigDaoImpl(this);
		connectEditText.setText(mParamConfigDao.get("transIp"));
		downloadEditText.setText(mParamConfigDao.get("caIp"));

		tpudEditText.setText(mParamConfigDao.get("tpdu"));
		tranTimeEditText.setText(mParamConfigDao.get("dealtimeout"));
		flushesTimesEditText.setText(mParamConfigDao.get("reversetimes"));

		spiner = (Spinner) super.findViewById(R.id.connect_mode); // 通讯方式
		curTransUrl = mParamConfigDao.get("transIp");
		curDownloadUrl = mParamConfigDao.get("caIp");

		// 根据当前的url决定什么那个处于选中状态
		if ((curTransUrl.equals(mParamConfigDao.get("test_3gapn_tranUrl"))
				&& curDownloadUrl.equals(mParamConfigDao.get("test_3gapn_certUrl")))
				|| (curTransUrl.equals(mParamConfigDao.get("produce_3gapn_tranUrl"))
						&& curDownloadUrl.equals(mParamConfigDao.get("produce_3gapn_certUrl")))) { // 3G-APN服务器
			spiner.setSelection(0);
			connectEditText.setTextColor(Color.GRAY);
			downloadEditText.setTextColor(Color.GRAY);
			connectEditText.setCursorVisible(false); // 设置输入框中的光标不可见
			connectEditText.setFocusable(false); // 无焦点
			connectEditText.setFocusableInTouchMode(false); // 触摸时也得不到焦点
			downloadEditText.setCursorVisible(false);
			downloadEditText.setFocusable(false);
			downloadEditText.setFocusableInTouchMode(false);
		} else if ((curTransUrl.equals(mParamConfigDao.get("test_public_tranUrl"))
				&& curDownloadUrl.equals(mParamConfigDao.get("test_public_certUrl")))
				|| (curTransUrl.equals(mParamConfigDao.get("produce_public_tranUrl"))
						&& curDownloadUrl.equals(mParamConfigDao.get("produce_public_certUrl")))) {// 公网服务器
			spiner.setSelection(1);
			connectEditText.setTextColor(Color.GRAY);
			downloadEditText.setTextColor(Color.GRAY);
			connectEditText.setCursorVisible(false); // 设置输入框中的光标不可见
			connectEditText.setFocusable(false); // 无焦点
			connectEditText.setFocusableInTouchMode(false); // 触摸时也得不到焦点
			downloadEditText.setCursorVisible(false);
			downloadEditText.setFocusable(false);
			downloadEditText.setFocusableInTouchMode(false);
		} else {
			spiner.setSelection(2);
			connectEditText.setCursorVisible(true); // 设置输入框中的光标不可见
			connectEditText.setFocusable(true); // 无焦点
			connectEditText.setFocusableInTouchMode(true); // 触摸时也得不到焦点
			downloadEditText.setCursorVisible(true);
			downloadEditText.setFocusable(true);
			downloadEditText.setFocusableInTouchMode(true);
			connectEditText.setTextColor(Color.BLACK);
			downloadEditText.setTextColor(Color.BLACK);
		}

		spiner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() { // 通讯方式选择监听器
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				switch (arg2) {
				case 0:
					// 赋值当前文本框为选择对应的ip信息
					// if("200021".equals(mParamConfigDao.get("fid"))){ //测试
					if ("test".equals(mParamConfigDao.get("env"))) { // 测试
						connectEditText.setText(mParamConfigDao.get("test_3gapn_tranUrl"));
						downloadEditText.setText(mParamConfigDao.get("test_3gapn_certUrl"));
					} else {
						connectEditText.setText(mParamConfigDao.get("produce_3gapn_tranUrl"));
						downloadEditText.setText(mParamConfigDao.get("produce_3gapn_certUrl"));
					}
					connectEditText.setCursorVisible(false); // 设置输入框中的光标不可见
					connectEditText.setFocusable(false); // 无焦点
					connectEditText.setFocusableInTouchMode(false); // 触摸时也得不到焦点
					downloadEditText.setCursorVisible(false);
					downloadEditText.setFocusable(false);
					downloadEditText.setFocusableInTouchMode(false);
					connectEditText.setTextColor(Color.GRAY);
					downloadEditText.setTextColor(Color.GRAY);
					break;
				case 1:
					// if("200021".equals(mParamConfigDao.get("fid"))){ //测试公网
					if ("test".equals(mParamConfigDao.get("env"))) { // 测试
						connectEditText.setText(mParamConfigDao.get("test_public_tranUrl"));
						downloadEditText.setText(mParamConfigDao.get("test_public_certUrl"));
					} else {
						connectEditText.setText(mParamConfigDao.get("produce_public_tranUrl"));
						downloadEditText.setText(mParamConfigDao.get("produce_public_certUrl"));
					}
					connectEditText.setCursorVisible(false); // 设置输入框中的光标不可见
					connectEditText.setFocusable(false); // 无焦点
					connectEditText.setFocusableInTouchMode(false); // 触摸时也得不到焦点
					downloadEditText.setCursorVisible(false);
					downloadEditText.setFocusable(false);
					downloadEditText.setFocusableInTouchMode(false);
					connectEditText.setTextColor(Color.GRAY);
					downloadEditText.setTextColor(Color.GRAY);
					break;
				case 2:
					connectEditText.setText(mParamConfigDao.get("transIp"));
					downloadEditText.setText(mParamConfigDao.get("caIp"));
					connectEditText.setCursorVisible(true); // 设置输入框中的光标不可见
					connectEditText.setFocusable(true); // 无焦点
					connectEditText.setFocusableInTouchMode(true); // 触摸时也得不到焦点
					downloadEditText.setCursorVisible(true);
					downloadEditText.setFocusable(true);
					downloadEditText.setFocusableInTouchMode(true);
					connectEditText.setTextColor(Color.BLACK);
					downloadEditText.setTextColor(Color.BLACK);
					break;
				default:
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String connectString = connectEditText.getText().toString();
				if ("".equals(connectString) || connectString == null) {
					DialogFactory.showTips(ConnectParamSetting.this, "连接地址不能设置为空！");
					return;
				}
				String downloadString = downloadEditText.getText().toString();
				if ("".equals(downloadString) || downloadString == null) {
					DialogFactory.showTips(ConnectParamSetting.this, "证书下载地址不能设置为空！");
					return;
				}

				int time = 0;
				// 对交易超时进行判定
				String transTime = tranTimeEditText.getText().toString();
				if (!"".equals(transTime) && transTime != null) {
					time = Integer.valueOf(tranTimeEditText.getText().toString());
					if (time > 120) {
						DialogFactory.showTips(ConnectParamSetting.this, "交易超时不能设置超过120秒！");
						return;
					} else if (time < 60) {
						DialogFactory.showTips(ConnectParamSetting.this, "交易超时不能设置低于60秒！");
						return;
					}
				} else {
					DialogFactory.showTips(ConnectParamSetting.this, "交易超时不能设置为空！");
					return;
				}
				// 冲正次数设置判定
				String flushesTimes = flushesTimesEditText.getText().toString();
				if ("".equals(flushesTimes) || flushesTimes == null) {
					DialogFactory.showTips(ConnectParamSetting.this, "冲正次数不能设置为空！");
					return;
				}

				// TPDU
				String tpduString = tpudEditText.getText().toString();
				if ("".equals(tpduString) || tpduString == null) {
					DialogFactory.showTips(ConnectParamSetting.this, "TPDU不能设置为空！");
					return;
				} else if (tpduString.length() != 10) {
					DialogFactory.showTips(ConnectParamSetting.this, "TPDU设置长度不等于10位！");
					return;
				}

				Map<String, String> map = new HashMap<String, String>();
				map.put("transIp", connectEditText.getText().toString());
				map.put("caIp", downloadEditText.getText().toString());

				map.put("tpdu", tpduString);
				map.put("dealtimeout", tranTimeEditText.getText().toString());
				map.put("reversetimes", flushesTimesEditText.getText().toString());

				int ret = mParamConfigDao.update(map);
				if (ret == 5) {
					DialogFactory.showTips(ConnectParamSetting.this,
							ConnectParamSetting.this.getResources().getString(R.string.tip_save_success));
					lklcposActivityManager.removeActivity(ConnectParamSetting.this);
					outActivityAnim();
				} else if (ret != -1) {
					DialogFactory.showTips(ConnectParamSetting.this,
							ConnectParamSetting.this.getResources().getString(R.string.tip_save_unsuccess));
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
