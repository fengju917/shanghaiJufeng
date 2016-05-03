package com.centerm.lklcpos.settings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.activity.OneShotNetworkActivity;
import com.centerm.lklcpos.deviceinterface.HttpRequestJsIfc;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

public class OneShotActivity extends BaseActivity {

	private EditText term_sn;
	private Button nextBtn;
	private Button backBtn;

	private ParamConfigDao mParamConfigDao;
	private HttpRequestJsIfc mHttpRequestJsIfc;
	// private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oneshot_settings_layout);
		term_sn = (EditText) this.findViewById(R.id.term_sn);
		nextBtn = (Button) this.findViewById(R.id.oneshot_ok_btn);
		backBtn = (Button) this.findViewById(R.id.back);

		term_sn.setText(Utility.getLklCposSN());
		term_sn.setCursorVisible(false);
		term_sn.setFocusable(false);
		term_sn.setFocusableInTouchMode(false);
		TextView transType = (TextView) findViewById(R.id.transType);
		transType.setText("自助开通");

		mParamConfigDao = new ParamConfigDaoImpl(this);
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(OneShotActivity.this, OneShotNetworkActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("step", "1");
				intent.putExtras(bundle);
				startActivity(intent);
				addActivityAnim();
				intent = null;
				// try {
				// mHttpRequestJsIfc = new HttpRequestJsIfc(
				// OneShotActivity.this, handler);
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				// mHttpRequestJsIfc.merchantDownload();
			}
		});

		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeActivity();
				outActivityAnim();
			}
		});

		// 处理交互结果
		// handler = new Handler() {
		// @Override
		// public void handleMessage(Message msg) {
		// // TODO Auto-generated method stub
		// super.handleMessage(msg);
		// switch (msg.what) {
		// case 0x00:
		// break;
		// case 0x01:
		// break;
		// //以下商终信息下载处理
		// case 0xa1:
		// Toast.makeText(OneShotActivity.this, "商终信息下载", 0).show();
		// break;
		// default:
		// break;
		// }
		// }
		// };
	}

	// 屏蔽HOME、返回键
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// // TODO Auto-generated method stub
	//// if (keyCode == KeyEvent.KEYCODE_BACK
	//// || keyCode == KeyEvent.KEYCODE_HOME) {
	//// return true;
	//// }
	// return super.onKeyDown(keyCode, event);
	// }
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// ParamConfigDao dao = new ParamConfigDaoImpl(this);
	// if (!"1".equals(dao.get("enabled"))) { // 未激活状态
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_HOME:
	// int count = lklcposActivityManager
	// .activityCount(OneShotWelcomeActivity.class);
	// if (count == 0) {
	// Intent intent = new Intent();
	// intent.setClass(this, OneShotWelcomeActivity.class);
	// startActivity(intent);
	// addActivityAnim();
	// }
	// lklcposActivityManager
	// .removeAllActivityExceptOne(OneShotWelcomeActivity.class);
	// outActivityAnim();
	// break;
	// case KeyEvent.KEYCODE_BACK:
	// lklcposActivityManager.removeActivity(this);
	// outActivityAnim();
	// break;
	// default:
	// break;
	// }
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }
}
