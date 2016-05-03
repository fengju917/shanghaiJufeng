package com.centerm.lklcpos.activity;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.LklcposActivityManager;
import com.centerm.lklcpos.deviceinterface.PrintDev;
import com.centerm.lklcpos.deviceinterface.PrintDev.CallBack;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.transaction.entity.Shortcut;
import com.centerm.lklcpos.transaction.entity.Transaction;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.DialogMessage;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui
 * @da2013-7-2 基础父类
 */
public class BaseActivity extends Activity implements OnEditorActionListener {

	private static Logger log = Logger.getLogger(BaseActivity.class);

	public final String mainMuneAction = "com.lkl.farmer.mainMenu";

	public LklcposActivityManager lklcposActivityManager;
	private Map<String, String> dataMap;
	private PrintDev printDevBase = null;
	private Context context = null;
	private Shortcut mShortcut = null;
	private boolean isAutoUp = false;

	protected final static int KEYCODE_HOME = 211; // 兼容一期机器的HOME键

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		lklcposActivityManager = LklcposActivityManager.getActivityManager();
		lklcposActivityManager.addActivity(this);
	}

	/**
	 * 打印机异常处理的dialog提示
	 * 
	 * @param context
	 * @param printDev
	 * @param b
	 * @return
	 */
	public Dialog creatExceptDialog(final Context context, final Bundle b) {
		StandbyService.stopStandby(); // 弹出缺纸异常时，暂停待机功能
		Dialog dialog = new DialogMessage(context).alert("警告", "发现打印机缺纸，请装纸后继续打印！",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						StandbyService.startStandby();
						dialog.dismiss();
						exceptOperate(context, b);
					}

				}, null);
		return dialog;

	}

	private void exceptOperate(final Context ac, final Bundle bundle) {
		// PrintDev printDevBase = null;
		this.context = ac;

		// openPrintDev();
		try {
			printDevBase = new PrintDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int ret = printDevBase.getPrintState();
		if (ret == 4) {
			creatExceptDialog(ac, bundle);
			return;
		} else if (ret == 0) {
			// 无异常
		} else {
			DialogFactory.showTips(getApplicationContext(), "打印机异常，请稍候再试！");
			return;
		}

		openPrintDev();
		isAutoUp = bundle.getBoolean("isAutoUp", false); // 标志是否发起自动脱机上送
		dataMap = (Map<String, String>) bundle.getSerializable("printdata");
		dataMap.put("isReprints", "true");
		if ("900000".equals(dataMap.get("transprocode"))) { // 结算数据
			if (dataMap.containsKey("printDetails")) {
				dataMap.remove("printDetails");
			}
			printDevBase.printData(dataMap, new CallBack() {

				@Override
				public void isPrintSecond() { // 打印完成结算总结单弹出是否打印结算明细单
					printDevBase.close();
					if ("settle".equals(Utility.getPrintStatus(getApplicationContext()))) {
						Utility.setPrintStatus(getApplicationContext(), "");
					}
					gotoMenuSpace();
				}

				@Override
				public void printExcept(int code, Bundle b) {
					printDevBase.close();
					if (code == 0x30) {
						creatExceptDialog(context, b);
					} else {
						DialogFactory.showTips(context, "打印机异常请稍候再试！");
					}
				}
			});

		} else { // 非结算数据
			if (dataMap.containsKey("isSecond")) {
				dataMap.remove("isSecond");
			}
			printDevBase.printData(dataMap, new CallBack() {

				@Override
				public void isPrintSecond() { // 打印完成POS签购单商户联弹出是否打印第二联
					// TODO Auto-generated method stub
					if ("alltrans".equals(dataMap.get("printtype")) || "true".equals(dataMap.get("printDetails"))) {// 打印总计单和结算明细单
						gotoMenuSpace();
					} else {
						gotoPrintAgainDialog(dataMap.get("transprocode"));
					}
				}

				@Override
				public void printExcept(int code, Bundle b) {
					printDevBase.close();
					if (code == 0x30) {
						creatExceptDialog(context, b);
					} else {
						DialogFactory.showTips(context, "打印机异常请稍候再试！");
						if (isAutoUp) { // 自动发起脱机消费上送
							autoOfflineSaleUp();
						} else {
							gotoMenuSpace();
						}
					}
				}
			});
		}
		// printDevBase.close();
	}

	private void gotoMenuSpace() {
		printDevBase.close();
		if (WebViewActivity.isCallbyThrid) {
			callByThirdSaleOver();
		} else {
			int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(this, MenuSpaceActivity.class);
				startActivity(intent);
				addActivityAnim();
			}
			lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
		}

	}

	private void callByThirdSaleOver() {
		Bundle retBundle = new Bundle();
		retBundle.putString("msg_tp", "02100");
		retBundle.putString("proc_cd", "000000");
		retBundle.putBoolean("isSuc", true);

		Intent retIntent = new Intent();
		retIntent.putExtras(retBundle);
		WebViewActivity.installActivity.setIntent(retIntent);
		lklcposActivityManager.removeAllActivityExceptOne(WebViewActivity.class);
	}

	// add by xrh @20131014
	protected void gotoOneshotWelcome() { // 回到一键激活欢迎页
		int count = lklcposActivityManager.activityCount(OneShotWelcomeActivity.class);
		Intent intent = new Intent();
		intent.setClass(this, OneShotWelcomeActivity.class);
		startActivity(intent);
		addActivityAnim();
		outActivityAnim();
	}

	private void gotoPrintAgainDialog(String transprocode) {
		printDevBase.close();
		Intent mIntent = new Intent();
		mIntent.setClass(this, PrintAgianDialog.class);
		mIntent.putExtra("code", transprocode); // 交易码
		if ("900000".equals(transprocode)) {
			startActivityForResult(mIntent, 0x51);
		} else {
			startActivityForResult(mIntent, 0x50);
		}
		outActivityAnim();
	}

	// 自动脱机交易上送
	private void autoOfflineSaleUp() {
		startActivityForResult(Utility.autoUpOfflineSale(), 0x52); // 自动发起脱机上送
	}

	private void openPrintDev() {
		try {
			if (printDevBase != null) {
				printDevBase.close();
			}
			printDevBase = null;
			printDevBase = new PrintDev();
			printDevBase.openDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("", e);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (requestCode == 0x51) {
			openPrintDev();
			if (resultCode == 0x03) {
				dataMap.put("printDetails", "true"); // 打印交易明细
				printDevBase.printData(dataMap, new CallBack() {
					@Override
					public void isPrintSecond() {
						// TODO Auto-generated method stub
						if ("settle".equals(Utility.getPrintStatus(getApplicationContext()))) {
							Utility.setPrintStatus(getApplicationContext(), "");
						}
						/*
						 * if (Utility.getSettleStatus(getApplicationContext()))
						 * { // 若是结算状态，自动签退
						 * Utility.autoSingOut(getApplicationContext()); }
						 */
						gotoMenuSpace();
					}

					@Override
					public void printExcept(int code, Bundle b) {
						printDevBase.close();
						if (code == 0x30) {
							creatExceptDialog(context, b);
						} else {
							DialogFactory.showTips(context, "打印机异常请稍候再试！");
						}
					}
				});
			} else {
				if ("settle".equals(Utility.getPrintStatus(getApplicationContext()))) {
					Utility.setPrintStatus(getApplicationContext(), "");
				}
				/*
				 * if (Utility.getSettleStatus(getApplicationContext())) {
				 * Utility.autoSingOut(getApplicationContext()); }
				 */
				dataMap.put("printDetails", "false"); // 不打印交易明细
				printDevBase.printData(dataMap, new CallBack() {
					@Override
					public void isPrintSecond() {
						// TODO Auto-generated method stub
						gotoMenuSpace();
					}

					@Override
					public void printExcept(int code, Bundle b) {
						printDevBase.close();
						if (code == 0x30) {
							creatExceptDialog(context, b);
						} else {
							DialogFactory.showTips(context, "打印机异常请稍候再试！");
						}
					}
				});
			}
		} else if (requestCode == 0x50) {
			openPrintDev();
			if (resultCode == 0x02) {
				dataMap.put("isSecond", "true"); // 设置打印第二联
				printDevBase.printData(dataMap, new CallBack() {
					@Override
					public void isPrintSecond() {
						// TODO Auto-generated method stub
						if ("strans".equals(Utility.getPrintStatus(getApplicationContext()))) {
							Utility.setPrintStatus(getApplicationContext(), "");
						}
						if (isAutoUp) { // 自动发起脱机消费上送
							autoOfflineSaleUp();
						} else {
							gotoMenuSpace();
						}
					}

					@Override
					public void printExcept(int code, Bundle b) {
						printDevBase.close();
						if (code == 0x30) {
							creatExceptDialog(context, b);
						} else {
							DialogFactory.showTips(context, "打印机异常请稍候再试！");
							if (isAutoUp) { // 自动发起脱机消费上送
								autoOfflineSaleUp();
							} else {
								gotoMenuSpace();
							}
						}
					}
				});
			} else {
				if ("strans".equals(Utility.getPrintStatus(getApplicationContext()))) {
					Utility.setPrintStatus(getApplicationContext(), "");
				}
				if (isAutoUp) { // 自动发起脱机消费上送
					autoOfflineSaleUp();
				} else {
					gotoMenuSpace();
				}
			}

		} else if (requestCode == 0x52) { // 自动脱机上送返回
			DialogFactory.showTips(this, "自动上送累计笔数完成");
			gotoMenuSpace();
		} else { // 权限验证返回结果处理
			switch (requestCode) {
			case 0xAA: // 结算
				TradeBaseActivity.isTransStatus = false;// add for bug14717
														// 验证管理员权限时，超时无对话框弹出问题
														// by chenkehui
														// @2013.07.27
				StandbyService.onOperate();
				if (resultCode == RESULT_OK) { // 权限验证成功，开始判断签到状态
					if (Utility.getSignStatus(context)) {

						// 判断是否结算完成
						if (Utility.getSettleStatus(context) && !mShortcut.isNative()
								&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
							DialogFactory.showTips(context, "有未清算的数据，请先执行清算操作！");
							return;
						}

						if (mShortcut.isJudgeMaxTransRecords()) {
							if (Utility.isMaxCount(context)) {
								DialogFactory.showTips(context, "当批次交易笔数已达上限，请先结算！");
								return;
							}
						}
						startActivity(mShortcut.getmIntent());
						addActivityAnim();
					} else {
						startActivityForResult(Utility.autoSign(), 0xAB);
						addActivityAnim();
					}
				} else {
				}

				break;
			case 0xAB: // 签到
				if (resultCode == RESULT_OK) { // 自动签到成功
					// DialogFactory.showTips(MenuSpaceActivity.this, "自动签到成功");
					// 判断是否结算完成
					if (Utility.getSettleStatus(context) && !mShortcut.isNative()
							&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
						DialogFactory.showTips(context, "有未清算的数据，请先执行清算操作！");
						return;
					}

					if (mShortcut.isJudgeMaxTransRecords()) {
						if (Utility.isMaxCount(context)) {
							DialogFactory.showTips(context, "当批次交易笔数已达上限，请先结算！");
							return;
						}
					}
					startActivity(mShortcut.getmIntent());
					addActivityAnim();
				} else {
					DialogFactory.showTips(context, "自动签到失败！");
				}
				break;
			case 0xAC: // 只需要验证权限
				if (resultCode == RESULT_OK) {
					// 判断是否结算完成
					if (Utility.getSettleStatus(context) && !mShortcut.isNative()
							&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
						DialogFactory.showTips(context, "有未清算的数据，请先执行清算操作！");
						return;
					}
					if (mShortcut.isJudgeMaxTransRecords()) {
						if (Utility.isMaxCount(context)) {
							DialogFactory.showTips(context, "当批次交易笔数已达上限，请先结算！");
							return;
						}
					}
					startActivity(mShortcut.getmIntent());
					addActivityAnim();
				}
				break;
			}
		}
		// printDevBase.close();
		// printDevBase = null;
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 初始化每个子activity的title布局back按钮
	 * 
	 * @author zhouhui
	 */
	public void inititle() {
		// back返回按钮
		Button back = (Button) findViewById(R.id.back);
		String goback_title = getIntent().getStringExtra("goback_title");
		if (goback_title == null || "".equals(goback_title)) {
			back.setVisibility(View.GONE);
		}

		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				removeActivity();
				outActivityAnim();
			}
		});

		TextView transType = (TextView) findViewById(R.id.transType);
		Transaction ts = getIntent().getParcelableExtra("transaction");
		if (ts != null) {
			String key = ts.getMctCode();
			if (!"".equals(key) && key != null) {
				transType.setText(typeMap.get(key));
			}
		} else {
			transType.setVisibility(View.GONE);
		}
		// 版本号
		/*
		 * String vernum = StandbyActivity.VERSION; TextView version =
		 * (TextView) findViewById(R.id.version); version.setText(vernum);
		 */
	}

	public static Map<String, String> typeMap = new HashMap<String, String>() {
		{
			put("002301", "余额查询");
			put("002302", "助农取款");
			put("002303", "消费撤销");
			put("002308", "签到");
			put("002309", "结算");
			put("002313", "预授权");
			put("002314", "预授权完成");
			put("002315", "预授权完成撤销");
			put("002316", "预授权撤销 ");
			put("002317", "退货 ");
			put("002322", "指定账户圈存");
			put("002323", "非指定账户圈存");
			put("002321", "现金充值");
			put("002324", "现金充值撤销");
			put("002325", "脱机退货");
		}
	};

	/**
	 * 左侧加入动画
	 */
	public void addActivityAnim() {
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	/**
	 * 右侧载出动画
	 */
	public void outActivityAnim() {
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	protected void removeActivity() {
		LklcposActivityManager.getActivityManager().removeActivity(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (printDevBase != null) {
			printDevBase.close();
		}
		super.onStop();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	/**
	 * add by txb 20130725 监听内置密码键盘的确认按钮，不让其响应
	 */
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (!WebViewActivity.isCallbyThrid && event.getDeviceId() == 2) {
			log.debug("待机服务重置计时");
			StandbyService.onOperate();
		}

		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_ENTER:
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (getCurrentFocus() != null) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					// imm.toggleSoftInput(0,
					// InputMethodManager.HIDE_NOT_ALWAYS);
					imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				}
			}

			return true;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		/** add by xrh @2013.10.14 begin */
		ParamConfigDao dao = new ParamConfigDaoImpl(this);
		if (!"1".equals(dao.get("enabled"))) { // 未激活状态
			switch (keyCode) {
			case KEYCODE_HOME:
			case KeyEvent.KEYCODE_HOME:
				gotoOneshotWelcome();
				break;
			case KeyEvent.KEYCODE_BACK:
				lklcposActivityManager.removeActivity(this);
				outActivityAnim();
				break;
			default:
				break;
			}
			return true;
		}
		/** add by xrh @2013.10.14 end */
		/** add by chenkehui @2013.07.07 begin */
		// 对应用中的所有继承BaseActivity的界面中hong键、back键做待机响应
		if (!WebViewActivity.isCallbyThrid && ((keyCode == KeyEvent.KEYCODE_HOME || keyCode == KEYCODE_HOME)
				|| keyCode == KeyEvent.KEYCODE_BACK)) {
			StandbyService.onOperate();
		}
		/** add by chenkehui @2013.07.07 end */

		if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KEYCODE_HOME) {
			int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(this, MenuSpaceActivity.class);
				startActivity(intent);
				addActivityAnim();
			}
			lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
			outActivityAnim();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			lklcposActivityManager.removeActivity(this);
			outActivityAnim();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 对状态进行判断
	 */
	public void judgeState(final Context context, Shortcut mShortcut) {
		this.context = context;
		this.mShortcut = mShortcut;
		// 支持重复签到
		if ("transcation/T910000.xml".equals(mShortcut.getFilePath())) {
			startActivity(mShortcut.getmIntent());
			addActivityAnim();
			return;
		}

		// 判断是否点击结算按钮，进行提示并且进行状态的判断
		if ("transcation/T900000.xml".equals(mShortcut.getFilePath())) {
			Dialog dialog = new DialogMessage(this).alert("提示", "确定进行结算？", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (Utility.getSignStatus(context)) {
						Shortcut settle = new Shortcut(context, "transcation/T900000.xml");
						startActivity(settle.getmIntent());
						Utility.setSettleStatus(context, true);
					} else {
						DialogFactory.showTips(context, "请先签到再执行结算操作！");
					}
				}
			}, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			});
			return;
		}

		// 判断是否需要验证权限
		if (mShortcut.getPower() != null) {
			Intent authorityIntent = new Intent();
			if (mShortcut.getPower().equals("oper")) { // 调用验证运维人员密码
				authorityIntent.setClass(context, OperpswdActivity.class);
			} else if (mShortcut.getPower().equals("admin")) { // 调用验证管理员密码
				Log.i("ckh", "开启终端交易状态");
				TradeBaseActivity.isTransStatus = true; // add for bug14717
														// 验证管理员权限时，超时无对话框弹出问题
														// by chenkehui
														// @2013.07.27
				StandbyService.onOperate();
				authorityIntent.setClass(context, AuthorizeActivity.class);
			}
			if (mShortcut.isJudgestate()) {// 既需要验证权限，又要判定签到状态
				startActivityForResult(authorityIntent, 0xAA);
			} else {
				startActivityForResult(authorityIntent, 0xAC);
			}
			addActivityAnim();

		} else if (mShortcut.isJudgestate()) { // 只需要验证签到状态

			if (Utility.getSignStatus(context)) {
				if (!"002308".equals(mShortcut.getmTransaction().getMctCode())) {
					// 判断是否结算完成
					if (Utility.getSettleStatus(context) && !mShortcut.isNative()
							&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
						DialogFactory.showTips(context, "有未清算的数据，请先执行清算操作！");
						return;
					}

					if (mShortcut.isJudgeMaxTransRecords()) {
						if (Utility.isMaxCount(context)) {
							DialogFactory.showTips(context, "当批次交易笔数已达上限，请先结算！");
							return;
						}
					}
					startActivity(mShortcut.getmIntent());
					addActivityAnim();
				} else {
					DialogFactory.showTips(context, "终端已签到！");
				}

			} else {
				if (!mShortcut.getmTransaction().getMctCode().equals("002308")) {
					startActivityForResult(Utility.autoSign(), 0xAB);
					addActivityAnim();
				} else {
					// 判断是否结算完成
					if (Utility.getSettleStatus(context) && !mShortcut.isNative()
							&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
						DialogFactory.showTips(context, "有未清算的数据，请先执行清算操作！");
						return;
					}

					if (mShortcut.isJudgeMaxTransRecords()) {
						if (Utility.isMaxCount(context)) {
							DialogFactory.showTips(context, "当批次交易笔数已达上限，请先结算！");
							return;
						}
					}
					Intent mIntent = mShortcut.getmIntent();
					if (mIntent != null) {
						startActivity(mIntent);
						addActivityAnim();
					}
				}
			}
		} else {
			// 判断是否结算完成
			if (Utility.getSettleStatus(context) && !mShortcut.isNative()
					&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
				DialogFactory.showTips(context, "有未清算的数据，请先执行清算操作！");
				return;
			}

			if (mShortcut.isJudgeMaxTransRecords()) {
				if (Utility.isMaxCount(context)) {
					DialogFactory.showTips(context, "当批次交易笔数已达上限，请先结算！");
					return;
				}
			}
			Intent mIntent = mShortcut.getmIntent();
			if (mIntent != null) {
				startActivity(mIntent);
				addActivityAnim();
			}
		}

	}

	// add by txb 用于同一处理功能未开通的提示信息
	public void suchFunctionNotOpen(View v) {
		DialogFactory.showTips(this, getApplication().getResources().getString(R.string.such_function_hasnot_opened));
	}
}
