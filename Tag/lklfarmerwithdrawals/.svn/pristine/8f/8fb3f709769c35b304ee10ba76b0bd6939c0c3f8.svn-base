package com.centerm.lklcpos.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.centerm.lklcpos.service.StandbyService;
import com.lkl.farmerwithdrawals.R;

public class PrintAgianDialog extends BaseActivity {
	private Button cancel;
	private Button comfirm;
	private String codeString;
	private TextView firTextView;
	// private TextView secTextView;
	// private TextView thrTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.printagian);

		codeString = getIntent().getStringExtra("code");

		cancel = (Button) findViewById(R.id.cancelbutton);
		comfirm = (Button) findViewById(R.id.comfirmbutton);
		firTextView = (TextView) findViewById(R.id.firsttext);
		// secTextView = (TextView) findViewById(R.id.sceondtext);
		// thrTextView = (TextView) findViewById(R.id.thirdtext);

		cancel.setOnClickListener(mListener);
		comfirm.setOnClickListener(mListener);

		/*
		 * codeString = "002302"; if ("002302".equals(codeString) ||
		 * "002303".equals(codeString)){
		 * firTextView.setText(R.string.printagian_fir);
		 * secTextView.setText(R.string.printagian_sec);
		 * thrTextView.setText(R.string.wenhao); } else if
		 * ("002309".equals(codeString)){
		 * firTextView.setText(R.string.printagian_text); }
		 */
		if ("900000".equals(codeString)) {
			firTextView.setText(R.string.printagian_text);
		} else {
			firTextView.setText("是否打印第二联(客户存根)?");
			// firTextView.setText(R.string.printagian_fir
			// + R.string.printagian_sec + R.string.wenhao);
			// secTextView.setText(R.string.printagian_sec);
			// thrTextView.setText(R.string.wenhao);
		}

		StandbyService.stopStandby(); // 弹出是否打印第二联时，关闭待机
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		StandbyService.startStandby(); // 开启待机
		super.onPause();
	}

	private View.OnClickListener mListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.cancelbutton:
				setResult(RESULT_CANCELED);// 取消按钮返回结果为cancel，activity捕获下
				lklcposActivityManager.removeActivity(PrintAgianDialog.this);
				break;
			case R.id.comfirmbutton:
				if ("900000".equals(codeString)) {
					setResult(0x03); // 结算时，返回码0x03
				} else {
					setResult(0x02); // 消费和消费撤销时，返回码0x02
				}
				lklcposActivityManager.removeActivity(PrintAgianDialog.this);
				break;
			}
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
