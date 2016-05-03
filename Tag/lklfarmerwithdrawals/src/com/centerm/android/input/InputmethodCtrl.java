package com.centerm.android.input;

import com.henision.asura.IAsuraService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

public class InputmethodCtrl {
	private IAsuraService asuraService = null;

	private int mInputX = 0;
	private int mInputY = 0;
	private int mInputW = 0;
	private int mInputH = 0;

	private String mPackedname = null;

	public static final int INPUTMETHOD_TYPE_123 = 0;
	public static final int INPUTMETHOD_TYPE_PINYIN = 1;
	public static final int INPUTMETHOD_TYPE_HANDWRITING = 2;
	public static final int INPUTMETHOD_TYPE_ABC = 3;
	public static final int INPUTMETHOD_TYPE_123_ABC = 4;
	public static final int INPUTMETHOD_TYPE_123_ABC_NOSYM = 5;
	public static final int INPUTMETHOD_TYPE_PINYIN_NOSYM = 6;
	public static final int INPUTMETHOD_TYPE_ABC_NOSYM = 7;
	public static final int INPUTMETHOD_TYPE_ABC_123 = 8;
	public static final int INPUTMETHOD_TYPE_ABC_123_NOSYM = 9;
	public static final int INPUTMETHOD_TYPE_HANDWRITING_NOSYM = 10;
	public static final int INPUTMETHOD_TYPE_PINYIN_HANDWRITING = 11;
	public static final int INPUTMETHOD_TYPE_PINYIN_HANDWRITING_NOSYM = 12;
	public static final int INPUTMETHOD_TYPE_PINYIN_HANDWRITING_123 = 13;
	public static final int INPUTMETHOD_TYPE_PINYIN_HANDWRITING_123_NOSYM = 14;
	public static final int INPUTMETHOD_TYPE_ABC_CAP = 15;
	public static final int INPUTMETHOD_TYPE_123_ABC_CAP = 16;
	public static final int INPUTMETHOD_TYPE_123_ABC_NOSYM_CAP = 17;
	public static final int INPUTMETHOD_TYPE_ABC_NOSYM_CAP = 18;
	public static final int INPUTMETHOD_TYPE_ABC_123_CAP = 19;
	public static final int INPUTMETHOD_TYPE_ABC_123_NOSYM_CAP = 20;

	private static InputmethodCtrl mImc = null;

	public InputmethodCtrl() {
		android.util.Log.d("hover", "InputmethodCtrl create()");
	}

	private void establish(Activity activity) {
		activity.bindService(new Intent(IAsuraService.class.getName()), serConn, Context.BIND_AUTO_CREATE);
	}

	public void unestablish(Activity activity) {
		try {
			activity.unbindService(serConn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initialization(Activity activity, int x, int y, int w, int h) {
		android.util.Log.d("hover", "setAttribute " + " x " + x + " y " + y + " w " + w + " h " + h);
		setAttr(x, y, w, h);
		mPackedname = null;
		establish(activity);
	}

	public void initialization(Activity activity, int x, int y, int w, int h, String packedname) {
		android.util.Log.d("hover", "setAttribute " + " x " + x + " y " + y + " w " + w + " h " + h);
		setAttr(x, y, w, h);
		mPackedname = packedname;
		establish(activity);
	}

	public void setInputMode123() {
		android.util.Log.d("hover", "setInputMode123 ");
		setInputMode(INPUTMETHOD_TYPE_123);
	}

	public void setInputModeHandwriting() {
		android.util.Log.d("hover", "setInputModeHandwriting ");
		setInputMode(INPUTMETHOD_TYPE_HANDWRITING);
	}

	public void setInputModeHandwritingNosym() {
		android.util.Log.d("hover", "setInputModeHandwritingNosym ");
		setInputMode(INPUTMETHOD_TYPE_HANDWRITING_NOSYM);
	}

	public void setInputModePinyinHandwriting123() {
		android.util.Log.d("hover", "setInputModeHandwriting ");
		setInputMode(INPUTMETHOD_TYPE_PINYIN_HANDWRITING_123);
	}

	public void setInputModePinyinHandwriting123Nosym() {
		android.util.Log.d("hover", "setInputModeHandwritingNosym ");
		setInputMode(INPUTMETHOD_TYPE_PINYIN_HANDWRITING_123_NOSYM);
	}

	public void setInputModePinyinHandwriting() {
		android.util.Log.d("hover", "setInputModePinyinHandwriting ");
		setInputMode(INPUTMETHOD_TYPE_PINYIN_HANDWRITING);
	}

	public void setInputModePinyinHandwritingNosym() {
		android.util.Log.d("hover", "setInputModePinyinHandwritingNosym ");
		setInputMode(INPUTMETHOD_TYPE_PINYIN_HANDWRITING_NOSYM);
	}

	public void setInputModeAbc() {
		android.util.Log.d("hover", "setInputMode123 ");
		setInputMode(INPUTMETHOD_TYPE_ABC);
	}

	public void setInputModeAbcNosym() {
		android.util.Log.d("hover", "setInputMode123 ");
		setInputMode(INPUTMETHOD_TYPE_ABC_NOSYM);
	}

	public void setInputMode123Abc() {
		android.util.Log.d("hover", "setInputMode123ABC ");
		setInputMode(INPUTMETHOD_TYPE_123_ABC);
	}

	public void setInputMode123AbcNosym() {
		android.util.Log.d("hover", "setInputMode123ABCNOSYM ");
		setInputMode(INPUTMETHOD_TYPE_123_ABC_NOSYM);
	}

	public void setInputModeAbc123() {
		android.util.Log.d("hover", "setInputModeAbc123 ");
		setInputMode(INPUTMETHOD_TYPE_ABC_123);
	}

	public void setInputModeAbc123Nosym() {
		android.util.Log.d("hover", "setInputModeAbc123Nosym ");
		setInputMode(INPUTMETHOD_TYPE_ABC_123_NOSYM);
	}

	public void setInputModePinyin() {
		android.util.Log.d("hover", "setInputModePinyin ");
		setInputMode(INPUTMETHOD_TYPE_PINYIN);
	}

	public void setInputModePinyinNosym() {
		android.util.Log.d("hover", "setInputModePinyinNosym ");
		setInputMode(INPUTMETHOD_TYPE_PINYIN_NOSYM);
	}

	public void setInputModeAbcCap() {
		android.util.Log.d("hover", "setInputMode123 ");
		setInputMode(INPUTMETHOD_TYPE_ABC_CAP);
	}

	public void setInputModeAbcNosymCap() {
		android.util.Log.d("hover", "setInputMode123 ");
		setInputMode(INPUTMETHOD_TYPE_ABC_NOSYM_CAP);
	}

	public void setInputMode123AbcCap() {
		android.util.Log.d("hover", "setInputMode123ABC ");
		setInputMode(INPUTMETHOD_TYPE_123_ABC_CAP);
	}

	public void setInputMode123AbcNosymCap() {
		android.util.Log.d("hover", "setInputMode123ABCNOSYM ");
		setInputMode(INPUTMETHOD_TYPE_123_ABC_NOSYM_CAP);
	}

	public void setInputModeAbc123Cap() {
		android.util.Log.d("hover", "setInputModeAbc123 ");
		setInputMode(INPUTMETHOD_TYPE_ABC_123_CAP);
	}

	public void setInputModeAbc123NosymCap() {
		android.util.Log.d("hover", "setInputModeAbc123Nosym ");
		setInputMode(INPUTMETHOD_TYPE_ABC_123_NOSYM_CAP);
	}

	private void setInputMode(int mode) {
		try {
			asuraService.setInputMode(mode);
		} catch (Exception e) {
			Log.e("hover", e.getMessage(), e);
		}
	}

	private void setAttr(int x, int y, int w, int h) {
		mInputX = x;
		mInputY = y;
		mInputW = w;
		mInputH = h;
	}

	public void setAttrDirect(int x, int y, int w, int h) {
		if (asuraService != null) {
			try {
				asuraService.setAttribute(x, y, w, h);
			} catch (Exception e) {
				Log.e("hover", e.getMessage(), e);
			}
		}
	}

	private ServiceConnection serConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v("hover", "onServiceConnected() called");
			asuraService = IAsuraService.Stub.asInterface(service);

			try {
				asuraService.setAttribute(mInputX, mInputY, mInputW, mInputH);
				if (mPackedname != null) {
					asuraService.registApp(mPackedname);
				}
			} catch (Exception e) {
				Log.e("hover", e.getMessage(), e);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.v("hover", "onServiceDisconnected()");
			asuraService = null;
		}
	};

	public static InputmethodCtrl getInstance() {
		if (mImc == null) {
			mImc = new InputmethodCtrl();
		}
		return mImc;
	}
}
