package com.centerm.android.dialog;

import android.app.AlertDialog;
import android.content.Context;

public class PrintDialog extends AlertDialog.Builder {
	private static PrintDialog printdiaglog;

	 PrintDialog(Context context) {
		super(context);
		setCancelable(false);
	}

	public static PrintDialog getPrinDialog(Context ctx) {
		if (printdiaglog == null) {
			return printdiaglog = new PrintDialog(ctx);
		}
		return printdiaglog;
	}

}
