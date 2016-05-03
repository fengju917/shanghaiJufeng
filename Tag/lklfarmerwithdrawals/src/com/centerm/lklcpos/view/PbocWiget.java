package com.centerm.lklcpos.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc;
import com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc.GetPinBack;
import com.centerm.lklcpos.deviceinterface.PinPadDevJsIfc;
import com.centerm.lklcpos.deviceinterface.PinPadInterface;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.DialogMessage;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

public class PbocWiget {
	private final int OFFPIN_TIMER = 0x14;
	private PinPadInterface pinPadDev = null;
	private Activity activity;

	private boolean isCancel = false;

	public PbocWiget(Activity activity) {
		this.activity = activity;
	}

	// 支持内外置输入脱机PIN
	@SuppressLint("ResourceAsColor")
	public Dialog InputOffPINDailog(final String cardno, final ClickBack clickBack) {
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(activity);
		String pinpadType = mParamConfigDao.get("pinpadType");

		if ("0".equals(pinpadType)) { // 外置密码键盘
			String tip = "请输入脱机密码";
			final String timerTip = Utility.strFillSpace(tip, 85, false);
			AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.DialogStyle);
			builder.setTitle(tip).setMessage("请在外置密码键盘中输入脱机密码")
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							isCancel = true; // 取消
							pinPadDev.closeOffDev();
							clickBack.loadDataCancel();

						}
					}).setOnKeyListener(new OnKeyListener() {

						@Override
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							// TODO Auto-generated method stub
							if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK) {
								return true;
							}
							return false;
						}
					});
			final Dialog ex_dialog = builder.create();
			ex_dialog.setCancelable(false);
			ex_dialog.show();

			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					super.handleMessage(msg);
					switch (msg.what) {
					case OFFPIN_TIMER:
						if (ex_dialog.isShowing())
							ex_dialog.setTitle(timerTip + msg.getData().getString("dialog_second"));
						break;
					default:
						break;
					}
				}
			};
			new Thread() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					for (int i = 60; i >= 1; i--) {
						Message mMessage = Message.obtain();
						Bundle bundle = new Bundle();
						bundle.putString("dialog_second", String.valueOf(i));
						mMessage.what = OFFPIN_TIMER;
						mMessage.setData(bundle);
						handler.sendMessage(mMessage);
						try {
							sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (isCancel || !ex_dialog.isShowing()) {
							return;
						}
					}
					pinPadDev.closeOffDev();
					if (ex_dialog.isShowing()) {
						ex_dialog.dismiss();
						clickBack.loadDataCancel(); // 计时超时
					}
				}
			}.start();

			try {
				pinPadDev = new ExPinPadDevJsIfc(activity, handler);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pinPadDev.openOffDev();
			pinPadDev.getOffPin(cardno, null, new GetPinBack() {

				@Override
				public void onGetPin(String pin) {
					// TODO Auto-generated method stub
					Log.d("ckh", "onGetOffPin pin == " + pin);
					if (pin == null || "".equals(pin)) {
						pin = "entry";
					}
					ex_dialog.dismiss();
					pinPadDev.closeOffDev();
					clickBack.loadData(pin);
				}

				@Override
				public void onEnter() {
					// TODO Auto-generated method stub
				}
			});
			return ex_dialog;

		} else { // 内置密码键盘
			Log.d("ckh", "neizhi 密码键盘打开了---------------");
			final char[] star = new char[] { '*', '*', '*', '*', '*', '*', '*', '*', '*', '*', '*', '*' };
			final EditText inputServer = new EditText(this.activity);
			String tip = "请输入脱机密码";
			final String timerTip = Utility.strFillSpace(tip, 85, false);
			inputServer.setTextColor(R.color.white);
			inputServer.setKeyListener(null);
			AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
			builder.setTitle(tip).setView(inputServer).setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					isCancel = true; // 取消
					pinPadDev.closeOffDev();
					clickBack.loadDataCancel();
				}
			}).setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK) {
						return true;
					}
					return false;
				}
			});
			final Dialog in_dialog = builder.create();
			in_dialog.getContext().setTheme(R.style.DialogStyle);
			in_dialog.setCancelable(false);
			in_dialog.show();
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					super.handleMessage(msg);
					switch (msg.what) {
					case OFFPIN_TIMER: // 超时倒计时
						if (in_dialog.isShowing())
							in_dialog.setTitle(timerTip + msg.getData().getString("dialog_second"));
						break;
					case 0x01: // 回显"*"号
						int pinlength = msg.getData().getInt("pinlength");
						if (pinlength == 0) {
							inputServer.setText("");
						} else {
							inputServer.setText(star, 0, pinlength);
						}
						break;
					case 0x02: // 内置密码按取消键
						isCancel = true; // 取消
						pinPadDev.closeOffDev();
						if (in_dialog.isShowing()) {
							in_dialog.dismiss();
							clickBack.loadDataCancel();
						}
						break;
					case 0x03: // 密码不足
						DialogFactory.showTips(activity, "密码不足");
						break;
					default:
						break;
					}
				}
			};
			new Thread() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					for (int i = 60; i >= 1; i--) {
						Message mMessage = Message.obtain();
						Bundle bundle = new Bundle();
						bundle.putString("dialog_second", String.valueOf(i));
						mMessage.what = OFFPIN_TIMER;
						mMessage.setData(bundle);
						handler.sendMessage(mMessage);
						try {
							sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (isCancel || !in_dialog.isShowing()) {
							return;
						}
					}
					pinPadDev.closeOffDev();
					if (in_dialog.isShowing()) {
						Log.d("ckh", "431 neizhi 密码键盘打开了---------------");
						in_dialog.dismiss();
						clickBack.loadDataCancel(); // 计时超时
					}
				}
			}.start();

			try {
				pinPadDev = new PinPadDevJsIfc(activity, handler);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pinPadDev.openOffDev();
			pinPadDev.getOffPin(cardno, null, new GetPinBack() {

				@Override
				public void onGetPin(String pin) {
					// TODO Auto-generated method stub
					Log.d("ckh", "onGetOffPin pin == " + pin);
					if (pin == null || "".equals(pin)) {
						pin = "entry";
					}
					in_dialog.dismiss();
					pinPadDev.closeOffDev();
					clickBack.loadData(pin);
				}

				@Override
				public void onEnter() {
					// TODO Auto-generated method stub
				}
			});
			return in_dialog;
		}
	}

	public interface ClickBack {
		public void loadData(String data);

		public void loadDataCancel();
	}

	// 弹出多应用选择框
	public Dialog createSelectedDailog(String aidData, final ClickBack clickBack) {
		Dialog dialog = null;
		if (aidData == null)
			return dialog;
		String[] items = new String[(aidData.length() - 2) / 80];
		try {
			String times = new String(HexUtil.hexStringToByte(aidData.substring(0, 2)), "gbk");
			for (int i = 1; i <= (aidData.length() - 2) / 80; i++) {
				items[i - 1] = new String(HexUtil.hexStringToByte(aidData.substring((i - 1) * 80 + 2, i * 80 + 2)),
						"gbk");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.e("PbocWiget", "多应用选择，应用名解析异常");
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this.activity).setTitle("请选择应用：") // title

				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clickBack.loadData(String.valueOf(which + 1));
					}
				}).setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						if (keyCode == KeyEvent.KEYCODE_HOME) {
							return true;
						} else if (keyCode == KeyEvent.KEYCODE_BACK) {
							clickBack.loadDataCancel();
							dialog.dismiss();
							return true;
						}
						return false;
					}
				});

		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.getContext().setTheme(R.style.DialogStyle);
		dialog.show();
		new DialogTimer(dialog, "请选择应用：").start();
		return dialog;
	}

	// 弹出提示信息
	public Dialog createMessageDailog(String msgData, final ClickBack clickBack) {
		Dialog dialog = null;
		if (msgData == null)
			return dialog;
		try {
			String isShowMsg = msgData.substring(0, 2); // 是否显示标志
			String timeOut = msgData.substring(2, 4); // 提示超时时间
			int titleLen = Integer.parseInt(msgData.substring(4, 6), 16); // title长度
			String titleData = msgData.substring(6, 6 + titleLen * 2);
			if (titleData != null && !"".equals(titleData)) {
				titleData = new String(HexUtil.hexStringToByte(titleData), "gbk"); // titlen内容
			}
			int dataLen = Integer.parseInt(msgData.substring(6 + titleLen * 2, 8 + titleLen * 2), 16); // 显示内容长度
			String data = new String(HexUtil.hexStringToByte(msgData.substring(8 + titleLen * 2)), "gbk"); // 显示内容

			AlertDialog.Builder builder = new AlertDialog.Builder(this.activity)
					.setTitle(titleData != null && !"".equals(titleData) ? titleData : "单应用确认").setMessage(data)
					.setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							clickBack.loadDataCancel();
						}
					}).setPositiveButton(R.string.comfirm_text, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							clickBack.loadData(null);
						}
					}).setOnKeyListener(new OnKeyListener() {

						@Override
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							// TODO Auto-generated method stub
							if (keyCode == KeyEvent.KEYCODE_HOME) {
								return true;
							} else if (keyCode == KeyEvent.KEYCODE_BACK) {
								clickBack.loadDataCancel();
								dialog.dismiss();
								return true;
							}
							return false;
						}
					});
			dialog = builder.create();
			dialog.getContext().setTheme(R.style.DialogStyle);
			dialog.setCancelable(false);
			dialog.show();
			new DialogTimer(dialog, titleData != null && !"".equals(titleData) ? titleData : "单应用确认").start();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.e("PbocWiget", "提示信息解析异常");
		}
		return dialog;
	}

	// 弹出是否使用电子现金功能提示
	public Dialog createSaleAppSelectDailog(final ClickBack clickBack) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
		builder.setTitle("提示").setMessage("卡片支持电子现金功能，确认是否使用?").setNegativeButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						clickBack.loadDataCancel();
					}
				});
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				clickBack.loadData(null);
			}
		});
		builder.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});
		dialog = builder.create();
		dialog.getContext().setTheme(R.style.DialogStyle);
		dialog.setCancelable(false);
		dialog.show();
		new DialogTimer(dialog, "提示").start();
		return dialog;
	}

	// 弹出提示“Special CAPK not Found”
	public Dialog createTipShowDialog(final ClickBack clickBack) {
		Dialog dialog = new DialogMessage(this.activity).alert("提示", "Special CAPK not Found",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						clickBack.loadData(null);
					}
				}, null);
		dialog.show();
		new DialogTimer(dialog, "提示").start();
		return dialog;
	}

	class DialogTimer {
		private Dialog dialog;
		private String tip;

		public DialogTimer(Dialog dialog, String title) {
			this.dialog = dialog;

			this.tip = Utility.strFillSpace(title, 85, false);
		}

		private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case OFFPIN_TIMER: // 超时倒计时
					if (dialog.isShowing())
						dialog.setTitle(tip + msg.getData().getString("dialog_second"));
					break;
				}
			}
		};
		private Thread dialogThread = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (int i = 60; i >= 1; i--) {
					if (!dialog.isShowing()) {
						return;
					}
					Message mMessage = Message.obtain();
					Bundle bundle = new Bundle();
					bundle.putString("dialog_second", String.valueOf(i));
					mMessage.what = OFFPIN_TIMER;
					mMessage.setData(bundle);
					handler.sendMessage(mMessage);
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				dialog.dismiss();

			}
		};

		public void start() {
			dialogThread.start();
		}
	}

}
