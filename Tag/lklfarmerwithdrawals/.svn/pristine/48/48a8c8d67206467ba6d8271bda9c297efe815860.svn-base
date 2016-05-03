package com.centerm.lklcpos.provider;

import org.apache.log4j.Logger;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.centerm.comm.persistence.helper.DBOpenHelper;

public class LklcposProvider extends ContentProvider {

	private final Logger logger = Logger.getLogger(LklcposProvider.class);

	private DBOpenHelper openHelper;

	private static final String PARAM_CONFIG_TABLE = "param_config";

	public static final String AUTHORITY = "com.centerm.LklcposProvider1";

	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/param_config";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/param_config";

	private static final int PARAM_CONFIG = 1;
	private static final int PARAM_CONFIG_ID = 2;
	private static final int TRANSRECORD = 11;
	private static final int REVERSE = 21;
	private static final int SETTLEDATA = 31;

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	// 这里*代表匹配任意文本,#代表匹配任意数字
	static {
		sURIMatcher.addURI(AUTHORITY, PARAM_CONFIG_TABLE, PARAM_CONFIG);
		sURIMatcher.addURI(AUTHORITY, PARAM_CONFIG_TABLE + "/*", PARAM_CONFIG_ID);
	}

	@Override
	public boolean onCreate() {
		openHelper = DBOpenHelper.getInstance(getContext());
		logger.debug("LklcposProvider onCreate()...");
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int match = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = openHelper.getWritableDatabase();
		int rowsAffected = 0;
		switch (match) {
		case PARAM_CONFIG:
			rowsAffected = sqlDB.delete(PARAM_CONFIG_TABLE, selection, selectionArgs);
			break;
		case PARAM_CONFIG_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(PARAM_CONFIG_TABLE, "tagname ='" + id + "'", null);
			} else {
				rowsAffected = sqlDB.delete(PARAM_CONFIG_TABLE, selection + " and " + "tagname ='" + id + "'",
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = sURIMatcher.match(uri);
		switch (match) {
		case PARAM_CONFIG:
			break;
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}
		SQLiteDatabase sqlDB = openHelper.getWritableDatabase();
		long newID = sqlDB.insert(PARAM_CONFIG_TABLE, null, values);
		if (newID > 0) {
			Uri newUri = ContentUris.withAppendedId(uri, newID);
			getContext().getContentResolver().notifyChange(uri, null);
			return newUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.d("Xrh", uri.getPath());
		Log.d("Xrh", uri.getLastPathSegment());
		Log.d("Xrh", "11," + sURIMatcher.match(uri));

		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		switch (sURIMatcher.match(uri)) {
		case PARAM_CONFIG:
			qBuilder.setTables(PARAM_CONFIG_TABLE);
			break;
		case PARAM_CONFIG_ID:
			qBuilder.setTables(PARAM_CONFIG_TABLE);
			qBuilder.appendWhere("tagname = '" + uri.getLastPathSegment() + "'");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		Cursor c = qBuilder.query(openHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null,
				sortOrder, null);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		SQLiteDatabase sqlDB = openHelper.getWritableDatabase();
		int match = sURIMatcher.match(uri);
		logger.debug("match = " + match);
		int rowsAffected;
		switch (sURIMatcher.match(uri)) {
		case PARAM_CONFIG:
			rowsAffected = sqlDB.update(PARAM_CONFIG_TABLE, values, selection, selectionArgs);
			break;
		case PARAM_CONFIG_ID:
			String id = uri.getLastPathSegment();
			logger.debug("id =" + id);
			logger.debug("selection = " + selection);
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.update(PARAM_CONFIG_TABLE, values, " tagname = '" + id + "'", null);
			} else {
				rowsAffected = sqlDB.update(PARAM_CONFIG_TABLE, values, selection + " and " + "tagname='" + id + "'",
						selectionArgs);
			}
			rowsAffected = sqlDB.update(PARAM_CONFIG_TABLE, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

}
