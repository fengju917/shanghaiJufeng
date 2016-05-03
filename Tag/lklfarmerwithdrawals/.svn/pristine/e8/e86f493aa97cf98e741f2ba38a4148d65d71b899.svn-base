package com.centerm.lklcpos.settings.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.centerm.lklcpos.activity.BaseActivity;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-8-28
 *
 */
public class HandInputCarNum extends BaseActivity {

	private RadioButton on;
	private RadioButton off;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.handinput_carnum);

		inititle();

		on = (RadioButton) findViewById(R.id.on);
		off = (RadioButton) findViewById(R.id.off);

		SharedPreferences mSharedPreferences = HandInputCarNum.this.getSharedPreferences("menu_settings", 0);
		if (mSharedPreferences.getBoolean("handinputcarnum", true)) {
			on.setChecked(true);
		} else {
			off.setChecked(true);
		}
		final SharedPreferences.Editor mEditor = mSharedPreferences.edit();

		RadioGroup check_set = (RadioGroup) findViewById(R.id.check_set);
		check_set.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == on.getId()) {
					mEditor.putBoolean("handinputcarnum", true);
				} else if (checkedId == off.getId()) {
					mEditor.putBoolean("handinputcarnum", false);
				}
				mEditor.commit();
			}
		});
	}

}
