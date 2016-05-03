package com.centerm.lklcpos.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;

import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-7-16
 * 
 */
public class DialogMessage {

	private Context context;

	public DialogMessage(Context context) {
		this.context = context;
	}

	// 弹出提示框
	public Dialog alert(CharSequence title, CharSequence message, DialogInterface.OnClickListener confirmLinstener,
			DialogInterface.OnClickListener cancelLinstener) {

		AlertDialog.Builder builder = new Builder(context, R.style.DialogStyle);
		builder.setTitle(title);
		builder.setMessage(message);
		if (cancelLinstener != null) {
			builder.setNegativeButton("取消", cancelLinstener);
		}
		builder.setPositiveButton("确定", confirmLinstener);

		Dialog dialog = builder.create();
		dialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});
		dialog.getWindow().setGravity(Gravity.CENTER);
		dialog.show();
		dialog.setCancelable(false);
		return dialog;
	}

	// 弹出进度框
	public ProgressDialog createProgressDialog(String tip) {
		ProgressDialog dialog = null;
		dialog = new ProgressDialog(context, R.style.ProgressDialogStyle);
		dialog.setMessage(tip);
		dialog.show();
		dialog.setCancelable(false);
		dialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});
		return dialog;
	}
}
