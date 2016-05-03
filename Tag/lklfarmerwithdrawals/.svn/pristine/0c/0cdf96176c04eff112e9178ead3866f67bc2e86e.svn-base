package com.centerm.lklcpos.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lkl.farmerwithdrawals.R;

/**
 * 提示信息显示工具类 作者：刘志祥 时间：2012.09.02
 */
public class TipUtil {

	/**
	 * 显示文本提示对话框
	 * 
	 * @param context
	 *            Context实例
	 * @param msg
	 *            提示信息
	 * @return Dialog
	 */
	public static AlertDialog showAlertDialog(Context context, String msg) {
		AlertDialog.Builder build = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Panel);
		build.setTitle("提示");
		build.setMessage(msg);
		build.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = build.create();
		dialog.setCancelable(false);
		dialog.show();
		return dialog;
	}

	/**
	 * 显示列表提示对话框
	 * 
	 * @param context
	 *            Context实例
	 * @param title
	 *            对话框标题
	 * @param items
	 *            字符串数组
	 * @return Dialog
	 */
	public static AlertDialog showAlertDialog(Context context, String title, String[] items) {
		AlertDialog.Builder build = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Panel);
		build.setTitle(title);
		build.setItems(items, null);
		build.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = build.create();
		dialog.setCancelable(false);
		dialog.show();
		return dialog;
	}

	/**
	 * 显示确认对话框，确认后关闭当前Activity
	 * 
	 * @param context
	 *            Context实例
	 * @param msg
	 *            提示信息
	 * @return Dialog
	 */
	public static AlertDialog showConfirmDialog(Context context, String msg) {
		final Activity activity = (Activity) context;
		AlertDialog.Builder build = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Light_Panel);
		build.setTitle("提示");
		build.setMessage(msg);
		build.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				activity.finish();
			}
		});
		build.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = build.create();
		dialog.setCancelable(false);
		dialog.show();
		return dialog;
	}

	/**
	 * 显示进度条提示对话框
	 * 
	 * @param context
	 *            Context实例
	 * @param msg
	 *            提示信息
	 * @return ProgressDialog
	 */
	public static ProgressDialog showProgressDialog(Context context, String msg) {
		ProgressDialog dialog = new ProgressDialog(context, android.R.style.Theme_DeviceDefault_Light_Panel);
		dialog.setMessage(msg);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.show();
		return dialog;
	}

	/**
	 * 显示进度条提示对话框
	 * 
	 * @param context
	 *            Context实例
	 * @param iconId
	 *            图标ID
	 * @param title
	 *            对话框标题
	 * @param max
	 *            进度条最大长度
	 * @return ProgressDialog
	 */
	public static ProgressDialog showProgressDialog(Context context, int iconId, String title, int max) {
		ProgressDialog progressDialog = new ProgressDialog(context, android.R.style.Theme_DeviceDefault_Light_Panel);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle(title);
		progressDialog.setIcon(iconId);
		progressDialog.setIndeterminate(false);
		progressDialog.setMax(max);
		progressDialog.show();
		return progressDialog;
	}

	/**
	 * 显示Toast提示信息
	 * 
	 * @param context
	 *            Context实例
	 * @param msg
	 *            提示信息
	 * @param length
	 *            Toast.LENGTH_LONG或Toast.LENGTH_SHORT
	 */
	public static void showToastMsg(Context context, String msg, int length) {
		LinearLayout view = new LinearLayout(context);
		view.setOrientation(LinearLayout.HORIZONTAL);
		view.setGravity(Gravity.CENTER);
		view.setBackgroundResource(R.drawable.bg_tip);
		ImageView image = new ImageView(context);
		image.setImageResource(R.drawable.tip);
		TextView text = new TextView(context);
		text.setText(msg);
		text.setGravity(Gravity.CENTER_VERTICAL);
		text.setTextColor(Color.WHITE);
		text.setTextSize(24);
		view.addView(image, 0);
		view.addView(text, 1);
		Toast toast = new Toast(context);
		toast.setDuration(length);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setView(view);
		toast.show();
	}

}
