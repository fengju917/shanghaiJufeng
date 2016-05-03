package com.centerm.lklcpos.activity;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

public class NonContactShowLogActivity extends BaseActivity {

	private static Logger log = Logger.getLogger(NonContactShowLogActivity.class);

	private JSONArray array = null;
	private JSONObject obj = null;
	private int currentPage = 0;
	private boolean chageColor = true;

	private LayoutInflater inflater;
	private LinearLayout itmeLayout;
	private TextView count, page;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.noncontactshowlog);
		Bundle b = this.getIntent().getExtras();

		String cardlog = b.getString("cardlog");
		LinearLayout noitem = (LinearLayout) this.findViewById(R.id.noitem);
		LinearLayout dataitem = (LinearLayout) this.findViewById(R.id.dataitem);

		try {
			array = new JSONArray(cardlog);
		} catch (JSONException e) {
			log.error("", e);
		}
		Button backBtn = (Button) this.findViewById(R.id.back);
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				lklcposActivityManager.removeAllActivityExceptOne(OtherActivity.class);
				outActivityAnim();
			}
		});

		if (array.length() == 0) {
			noitem.setVisibility(View.VISIBLE);
			dataitem.setVisibility(View.GONE);
			Button back_confirm = (Button) this.findViewById(R.id.back_confirm);
			back_confirm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.setClass(NonContactShowLogActivity.this, OtherActivity.class);
					startActivity(intent);
					lklcposActivityManager.removeAllActivityExceptOne(OtherActivity.class);
					outActivityAnim();
				}
			});
		} else {
			noitem.setVisibility(View.GONE);
			dataitem.setVisibility(View.VISIBLE);

			inflater = this.getLayoutInflater();
			itmeLayout = (LinearLayout) this.findViewById(R.id.itmeLayout);
			count = (TextView) this.findViewById(R.id.count);
			page = (TextView) this.findViewById(R.id.page);

			Button returnitem = (Button) this.findViewById(R.id.returnitem);
			Button nextitem = (Button) this.findViewById(R.id.nextitem);
			returnitem.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO 上一条
					if (currentPage > 0) {
						currentPage--;
					} else {
						DialogFactory.showTips(NonContactShowLogActivity.this, "已经是第一条数据");
					}
					try {
						obj = array.getJSONObject(currentPage);
						showLog(obj, currentPage + 1);
						count.setText("共" + array.length() + "条");
					} catch (JSONException e) {
						log.error("", e);
					}
				}
			});
			nextitem.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO 下一条
					if (currentPage < array.length() - 1) {
						currentPage++;
					} else {
						DialogFactory.showTips(NonContactShowLogActivity.this, "已经是最后一条数据");
					}

					try {
						obj = array.getJSONObject(currentPage);
						showLog(obj, currentPage + 1);
						count.setText("共" + array.length() + "条");
					} catch (JSONException e) {
						log.error("", e);
					}
				}
			});

			try {
				obj = array.getJSONObject(0);
				showLog(obj, currentPage + 1);
				count.setText("共" + array.length() + "条");
			} catch (JSONException e) {
				log.warn("", e);
			}
		}
	}

	private void showLog(JSONObject obj, int num) {
		try {
			itmeLayout.removeAllViews();
			itmeLayout.addView(viewAdd("交易日期", obj.getString("translocaldate")));
			itmeLayout.addView(viewAdd("交易时间", obj.getString("translocaltime")));
			itmeLayout.addView(viewAdd("授权金额", obj.getString("transamount")));
			itmeLayout.addView(viewAdd("其他金额", obj.getString("otheramount")));
			itmeLayout.addView(viewAdd("终端国家代码", obj.getString("termcountrycode")));
			itmeLayout.addView(viewAdd("交易货币代码", obj.getString("transcurrcode")));
			itmeLayout.addView(viewAdd("商户名称", obj.getString("acceptoridname")));
			itmeLayout.addView(viewAdd("交易类型", typeMap.get(obj.getString("transtype"))));
			itmeLayout.addView(viewAdd("应用交易计数器", obj.getString("apptranscount")));
		} catch (JSONException e) {
			log.error("", e);
		}
		page.setText("当前记录第" + num + "条");
	}

	private View viewAdd(String key, String value) {

		View view = inflater.inflate(R.layout.confirminfo_item, null);
		TextView keyView = (TextView) view.findViewById(R.id.key);
		keyView.setText(key);
		TextView valueView = (TextView) view.findViewById(R.id.value);
		valueView.setText(value);
		if (chageColor) {
			keyView.setBackgroundColor(getResources().getColor(R.color.changetrue));
			valueView.setBackgroundColor(getResources().getColor(R.color.changetrue));
			chageColor = false;
		} else {
			keyView.setBackgroundColor(getResources().getColor(R.color.changefalse));
			valueView.setBackgroundColor(getResources().getColor(R.color.changefalse));
			chageColor = true;
		}
		return view;

	}

	private Map<String, String> typeMap = new HashMap<String, String>() {
		{
			put("31", "余额查询");
			put("00", "消费");
			put("03", "预授权");
			put("60", "指定账户圈存");
			put("62", "非指定账户圈存");
			put("63", "现金充值");
			put("17", "现金充值撤销");
			put("28", "脱机退货");
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		// return super.onKeyDown(keyCode, event);
		log.debug("keyCode = [" + keyCode + "]");
		return true;
	}
}
