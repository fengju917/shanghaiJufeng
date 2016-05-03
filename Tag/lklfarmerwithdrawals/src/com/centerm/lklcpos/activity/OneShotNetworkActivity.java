package com.centerm.lklcpos.activity;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.deviceinterface.HttpRequestJsIfc;
import com.centerm.lklcpos.service.StandbyService;
import com.lkl.farmerwithdrawals.R;

public class OneShotNetworkActivity extends BaseActivity {

	private Logger log = Logger.getLogger(OneShotNetworkActivity.class);

	private ParamConfigDao mParamConfigDao;
	private HttpRequestJsIfc mHttpRequestJsIfc;

	private TextView lableText; // 显示当前交互状态
	private TextView transTimer; // 网络交互倒计时
	private ImageView animImageView;
	private AnimationDrawable animationDrawable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.network);
		mParamConfigDao = new ParamConfigDaoImpl(this);
		lableText = (TextView) findViewById(R.id.network_lable);
		transTimer = (TextView) findViewById(R.id.trans_timer);
		animImageView = (ImageView) findViewById(R.id.progress_image_anim);
		// animImageView.setBackgroundResource(R.anim.network_anim);
		// animationDrawable = (AnimationDrawable)
		// animImageView.getBackground();
		// animationDrawable.start();
		rotate(animImageView);
		transTimer.setVisibility(View.GONE);
		// lableText.setBackgroundColor(Color.argb(0x00, 0x7f, 0xff, 0x00));
		// lableText.setTextColor(Color.argb(0xbb, 0x98, 0xF5, 0xFF));

		try {
			mHttpRequestJsIfc = new HttpRequestJsIfc(OneShotNetworkActivity.this, handler);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}

		StandbyService.stopScreenTimer(); // 取消超时

		Intent intent = this.getIntent();

		Bundle b = intent.getExtras();
		log.debug("step = " + b.get("step"));
		if ("1".equals(b.get("step"))) {
			String validateCode = b.getString("validateCode");
			mHttpRequestJsIfc.merchantDownload(validateCode);
		} else if ("2".equals(b.get("step"))) {
			mHttpRequestJsIfc.merCertDownload();
		} else if ("3".equals(b.get("step"))) {
			mHttpRequestJsIfc.masterKeyDownload();
		} else {

		}
	}

	// 处理交互结果
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0x00:
				Intent intent00 = new Intent(OneShotNetworkActivity.this, OneShotResultActivity.class);
				// Bundle bundle = msg.getData();
				intent00.putExtras(msg.getData());
				startActivity(intent00);
				addActivityAnim();
				intent00 = null;
				lklcposActivityManager.removeActivity(OneShotNetworkActivity.this);
				break;
			case 0x01:
				break;

			// 以下商终信息下载处理
			case 0xa1:// 通知
				// Toast.makeText(OneShotNetworkActivity.this, "商终信息下载",
				// 0).show();
				lableText.setText("正在开通,请稍候...");
				break;
			case 0xa2:
				break;
			case 0xa0:// 获取成功
				mHttpRequestJsIfc.merCertDownload();
				break;

			// 以下证书下载处理
			case 0xb1:// 通知
				// Toast.makeText(OneShotNetworkActivity.this, "证书下载",
				// 0).show();
				lableText.setText("正在开通,请稍候...");
				break;
			case 0xb2:
				break;
			case 0xb0:
				// Toast.makeText(OneShotNetworkActivity.this, "证书下载成功",
				// 0).show();
				mHttpRequestJsIfc.masterKeyDownload();
				Intent bIntent = new Intent(OneShotNetworkActivity.this, OneShotResultActivity.class);
				startActivity(bIntent);
				addActivityAnim();
				bIntent = null;
				lklcposActivityManager.removeActivity(OneShotNetworkActivity.this);

				// Intent intent1 = new Intent(OneShotNetworkActivity.this,
				// OneShotNetworkActivity.class);
				// Bundle bundle = new Bundle();
				// bundle.putString("step", "3");
				// intent1.putExtras(bundle);
				// startActivity(intent1);
				// addActivityAnim();
				// intent = null;
				// lklcposActivityManager
				// .removeActivity(OneShotNetworkActivity.this);
				break;
			// 以下主密钥下载处理
			case 0xc1:// 通知
				// Toast.makeText(OneShotNetworkActivity.this, "主密钥下载",
				// 0).show();
				lableText.setText("正在开通,请稍候...");
				break;
			case 0xc0:
				// Toast.makeText(OneShotNetworkActivity.this, "主密钥下载成功",
				// 0).show();
				Intent cIntent = new Intent(OneShotNetworkActivity.this, OneShotResultActivity.class);
				// Bundle bundle = new Bundle();
				// bundle.putString("step", "3");
				// intent1.putExtras(bundle);
				startActivity(cIntent);
				addActivityAnim();
				cIntent = null;
				lklcposActivityManager.removeActivity(OneShotNetworkActivity.this);
				break;

			// 以下异常处理
			case 0xe0:
				// Toast.makeText(OneShotNetworkActivity.this, "商终信息下载异常",
				// 0).show();
				Intent eIntent = new Intent(OneShotNetworkActivity.this, OneShotResultActivity.class);
				Bundle bundle = msg.getData();
				eIntent.putExtras(bundle);
				startActivity(eIntent);
				addActivityAnim();
				eIntent = null;
				lklcposActivityManager.removeActivity(OneShotNetworkActivity.this);

				break;
			default:
				break;
			}
		}
	};

	private String mesauthcode;

	private String tpdu;

	private class DealResultThread extends Thread {
		private Bundle b;

		public DealResultThread(Bundle bundle) {
			this.b = bundle;
		}

		@Override
		public void run() {
			Intent intent = new Intent(OneShotNetworkActivity.this, OneShotMerchantActivity.class);
			startActivity(intent);
			addActivityAnim();
			intent = null;
			lklcposActivityManager.removeActivity(OneShotNetworkActivity.this);

			if (b.getBoolean("isSuc")) {
			}
		}
	}

	// 屏蔽HOME、返回键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 旋转动画
	 * 
	 * @param view
	 */
	private void rotate(View view) {
		Animation a = new RotateAnimation(0, 360000 * 5, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		a.setDuration(5000000);
		// a.setInterpolator(new AccelerateInterpolator());
		// LinearInterpolator lin = new LinearInterpolator();
		a.setInterpolator(new LinearInterpolator());
		a.setFillAfter(true);
		view.startAnimation(a);
	}
}
