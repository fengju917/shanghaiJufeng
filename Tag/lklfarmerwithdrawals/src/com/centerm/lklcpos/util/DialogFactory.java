package com.centerm.lklcpos.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.lkl.farmerwithdrawals.R;

public class DialogFactory {

	/**
	 * 方法描述：创建toast提示框
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-7-4 上午2:47:21
	 * @param ac
	 * @param tips
	 * @param flag
	 * @param time
	 * @return
	 */
	public static Toast createToast(Activity ac, String tips, String flag, int time) {
		Toast toast = new Toast(ac);
		TextView text = new TextView(ac);
		text.setGravity(Gravity.CENTER);
		text.setText(tips);
		text.setPadding(10, 3, 10, 3);
		text.setBackgroundResource(R.drawable.toast);
		if ("error".equals(flag)) {
			text.setTextColor(Color.WHITE);
		} else {
			text.setTextColor(Color.WHITE);
		}

		text.setTextSize(25);
		toast.setView(text);
		toast.setDuration(time);
		return toast;
	}

	/**
	 * 方法描述：显示提示框
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-7-4 上午2:55:18
	 * @param ac
	 * @param tips
	 */
	public static void showTips(Activity ac, String tips) {
		Toast toast = createToast(ac, tips, "", 3);
		toast.show();
	}

	public static Toast createToast(Context mContext, String tips, String flag, int time) {
		Toast toast = new Toast(mContext);
		TextView text = new TextView(mContext);
		text.setGravity(Gravity.CENTER);
		text.setText(tips);
		text.setPadding(10, 3, 10, 3);
		text.setBackgroundResource(R.drawable.toast);
		if ("error".equals(flag)) {
			text.setTextColor(Color.WHITE);
		} else {
			text.setTextColor(Color.WHITE);
		}

		text.setTextSize(25);
		toast.setView(text);
		toast.setDuration(time);
		return toast;
	}

	public static void showTips(Context mContext, String tips) {
		Toast toast = createToast(mContext, tips, "", 3);
		toast.show();
	}
}
