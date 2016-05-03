package com.centerm.comm.persistence.impl;

import org.apache.log4j.Logger;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;

public class HykTransRecordDaoImpl {

	private final Logger logger = Logger.getLogger(HykTransRecordDaoImpl.class);

	private static final String URL_PATH = "content://com.centerm.LklhykProvider/transrecord";
	private Context context;
	private ContentResolver resolver;

	public HykTransRecordDaoImpl(Context context) {
		this.context = context;
		resolver = context.getContentResolver();

	}

	public synchronized int getTransCount() {
		int count = 0;
		Uri uri = Uri.parse(URL_PATH);
		Cursor cursor;

		isAvilible(context, "com.centerm.lklhyk");
		try {
			cursor = resolver.query(uri, null, "", new String[] {}, null);
			count = cursor.getCount();
			logger.debug("发卡通未结算交易记录数 ：" + cursor.getCount() + "条");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	private boolean isAvilible(Context context, String packageName) {
		final PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo = null;
		try {
			packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageInfo == null) {
			System.out.println("没有安装");
		} else {
			System.out.println("已经安装");
		}
		return true;
	}
}
