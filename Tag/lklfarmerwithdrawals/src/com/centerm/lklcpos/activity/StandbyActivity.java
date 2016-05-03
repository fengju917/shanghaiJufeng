package com.centerm.lklcpos.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.TextView;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.deviceinterface.MagCardDevJsIfc;
import com.centerm.lklcpos.deviceinterface.PbocDevJsIfc;
import com.centerm.lklcpos.deviceinterface.PrintDev;
import com.centerm.lklcpos.deviceinterface.PrintDev.CallBack;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.transaction.entity.ComponentNode;
import com.centerm.lklcpos.transaction.entity.Shortcut;
import com.centerm.lklcpos.transaction.entity.Transaction;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.DialogMessage;
import com.centerm.lklcpos.util.InterruptPower;
import com.centerm.lklcpos.util.TransactionUtility;
import com.centerm.lklcpos.util.Utility;
import com.centerm.lklcpos.view.PbocWiget;
import com.lkl.farmerwithdrawals.R;

/*
 * 待机界面
 * 2013-05-28
 */
public class StandbyActivity extends BaseActivity {

	private Logger logger = Logger.getLogger(StandbyActivity.class);

	public static String VERSION;
	private static final int UP_TIME = 1;
	private TextView versionText;
	private TextView timeText;
	private Handler timeHandler;
	private MagCardDevJsIfc magCardDev = null;
	private MagCardDevJsIfc magcard = null;

	private PbocDevJsIfc pbocDev = null;
	private Handler pbocHandler;
	private PbocWiget pbocWiget;
	private ProgressDialog mDialog;
	private Dialog interDialog = null;

	private Handler handler = null;
	private Map<String, String> map = new HashMap<String, String>();
	private ParamConfigDao parConfig;
	private TransRecordDao transRecord;
	private PrintDev printDev = null;
	private Map<String, String> lastTransMap, settleDataMap;
	private boolean isPrinting = false;
	private boolean isStatusing = false;
	private boolean isStopSwipe = false; // 标记（IC卡检卡成功，关闭刷卡监听）,是否重监听
	public static boolean isUnbind = false; // 第一次重启需绑定软键盘服务，onDestory中解绑

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.standby);
		if (!StandbyService.isActive) { // 第一次启动应用（待机服务未启动时），重置软键盘
			Log.i("ckh", "第一次StandbyActivity onCreate()绑定软键盘");
			isUnbind = true;
			InputmethodCtrl ctrl = InputmethodCtrl.getInstance();
			Display mDisplay = getWindowManager().getDefaultDisplay();
			int W = mDisplay.getWidth();
			ctrl.initialization(this, 0, 350 * W / 800, 800 * W / 800, 250 * W / 800, "com.centerm.lklcpos");
			ctrl.setAttrDirect(0, 350 * W / 800, 800 * W / 800, 250 * W / 800); // 输入法位置参数设置
			// switch (Utility.currentDevice()) {
			// case 1: // I代终端
			// ctrl.initialization(this, 0, 350, 800, 250,
			// "com.centerm.lklcpos");
			// ctrl.setAttrDirect(0, 350, 800, 250); //输入法位置参数设置
			// break;
			// case 2: // II代终端
			// ctrl.initialization(this, 0, 448, 1024, 320,
			// "com.centerm.lklcpos");
			// ctrl.setAttrDirect(0, 448, 1024, 320);
			// break;
			// }
			StandbyService.isStandByStatus = true; // 第一次启动应用的时候，是处于待机状态
		}

		Intent mIntent = new Intent(this, StandbyService.class);// 启动待机服务
		startService(mIntent);

		parConfig = new ParamConfigDaoImpl(StandbyActivity.this);
		transRecord = new TransRecordDaoImpl(StandbyActivity.this);

		versionText = (TextView) findViewById(R.id.version);
		timeText = (TextView) findViewById(R.id.time);
		VERSION = getVersion();
		versionText.setText(VERSION);
		setTimeView();

		// 注册一个接收是否未安装配置文件的广播接受器
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.centerm.lklcpos.posparam");
		registerReceiver(mNoPosparamReceiver, filter);

		// 初始化刷卡设备
		this.handler = new MainActivityHandle(this);
		try {
			this.magCardDev = new MagCardDevJsIfc(StandbyActivity.this, this.handler);
		} catch (Exception e) {
			e.printStackTrace();
		}

		timeHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case UP_TIME:
					setTimeView();
					break;

				default:
					break;
				}
			}
		};

		new TimeThread().start();

		pbocHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Bundle b = msg.getData();
				boolean result = b.getBoolean("result");
				String reason = b.getString("reason");

				if (Utility.isThatMsg(msg, "msg_check_card")) { // 检卡回调
					if (result) {
						clearTackData();
						isStopSwipe = true;
						magcard.cancelSwipeCard(); // 检卡成功，停止刷卡监听
						pbocDev.readICCard(map);
						mDialog = new DialogMessage(StandbyActivity.this).createProgressDialog("正在读卡信息，请稍候（勿拔卡）...");
					} else { // 检卡异常
						if ("检卡超时".equals(reason)) {
							reSeachCard(reason);
						} else {
							gotoMenuSpaceFormStandby();
						}
					}
				} else if (Utility.isThatMsg(msg, "msg_read_card")) { // 读取卡片信息
					mDialog.dismiss();
					if (result) {
						// 插卡待机消费
						icSaleStatusDetection(StandbyActivity.this);
					} else {
						String tip = null;
						if ("fallback".equals(reason)) { // fallback
							tip = "读取IC卡错误，请点击确定后刷卡！";
						} else {
							tip = reason + "，请拔卡！";
						}
						if (interDialog != null && interDialog.isShowing()) {
							interDialog.dismiss();
							interDialog = null;
						}
						Dialog dialog = new DialogMessage(StandbyActivity.this).alert("提示", tip,
								new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								reSeachCard("fallback");
							}
						}, null);
						dialog.show();
					}
				} else if (Utility.isThatMsg(msg, "msg_emv_interaction")) { // 内核请求交互
					if ("getAIDSelect".equals(reason)) { // 导入多应用选择
						interDialog = pbocWiget.createSelectedDailog(map.get("aidSelectData"),
								new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								map.put("AIDIndex", data); // 将用户选择的应用索引值存放在dataMap中
								pbocDev.loadResultOfAIDSelect(data);
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfAIDSelect(null);
							}
						});
					} else if ("oneAIDSelect".equals(reason)) { // 单应用确认
						interDialog = pbocWiget.createMessageDailog(map.get("oneAIDSelectData"),
								new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfMessage((byte) 1);
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfMessage((byte) 0);
							}

						});
					} else if ("Special CAPK not Found".equals(reason)) {
						interDialog = pbocWiget.createTipShowDialog(new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfMessage((byte) 1);
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfMessage((byte) 0);
							}
						});
					}
				}
			}
		};
		// 初始化IC卡读卡设备
		try {
			this.pbocDev = new PbocDevJsIfc(StandbyActivity.this, pbocHandler);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.pbocWiget = new PbocWiget(this);

		isStatusing = stateDetection();// 执行状态的检测
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.i("ckh", "onResume");
		this.magcard = magCardDev;
		this.magcard.openDev();
		if (!isStatusing) {
			reSwipeCard(); // standby参数用于区分为待机刷卡
			reSeachCard("");
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.i("ckh", "onPause");
		if (isUnbind) {
			Log.i("ckh", "onPause()重置输入模式");
			InputmethodCtrl.getInstance().setInputMode123();
			StandbyService.isStandByStatus = false;
		}
		if (magcard != null) {
			magcard.closeDev();
			magcard = null;
		}

		try {
			pbocDev.closeDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onPause();
	}

	public void clearTackData() {
		map.put("priaccount", "");
		map.put("track2data", "");
		map.put("track3data", "");

	}

	private class TimeThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			do {
				try {
					Thread.sleep(1000);
					Message msg = new Message();
					msg.what = UP_TIME;
					timeHandler.sendMessage(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (!isPrinting) {
			StandbyService.isStandByStatus = false;
			StandbyService.onOperate();
			if (event.getAction() == MotionEvent.ACTION_UP) {
				Intent intent = new Intent(StandbyActivity.this, MenuSpaceActivity.class);
				startActivity(intent);
				addActivityAnim();
				lklcposActivityManager.removeActivity(this);
			}
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 用于状态的检测(断电，结算，凭条)
	 */
	private boolean stateDetection() {
		boolean printStatus = false; // 是否有打印状态
		boolean settleStatus = false; // 是否有结算状态
		// 检测是否为第一次启动
		if (InterruptPower.getInterruptPower().isFirst()) {
			Utility.setSignStatus(StandbyActivity.this, false);// 标志签退操作
		}
		// 检测凭条是否打印完成的情况
		String printsymbol = null;
		try {
			printsymbol = parConfig.get("printsymbol");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		if ("".equals(printsymbol) || printsymbol == null) {// 既无未打印完的交易凭证，也无未打印完的结算信息
			printStatus = false;
		} else if ("strans".equals(printsymbol)) { // 有交易凭证未打印完成
			logger.info("有交易凭证未打印完成 printsymbol =" + printsymbol);
			if (transRecord.getLastTransRecord() != null) {// 获取最后一条交易记录
				printStatus = true;
				openPrintDev(); // 打开打印机设备
				Dialog dialog = new DialogMessage(StandbyActivity.this).alert("提示", "有未打印完成的交易凭条！",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								printLastTransRecord();// 打印最后一笔记录
							}
						}, null);
				dialog.show();
			} else {
				logger.warn("标识有未打印凭条，但没有数据");
				Utility.setPrintStatus(StandbyActivity.this, "");
			}
		} else if ("settle".equals(printsymbol)) { // 有未打印完的结算信息
			String settlebatchno = parConfig.get("settlebatchno");
			if (settlebatchno != null && !"".equals(settlebatchno)) {
				printStatus = true;
				openPrintDev();
				Dialog dialog = new DialogMessage(StandbyActivity.this).alert("提示", "有未打印完成的结算信息！",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								printSettleData();
							}
						}, null);
				dialog.show();
			} else {
				logger.warn("标示有未打印结算凭条，但没有结算数据");
				Utility.setPrintStatus(StandbyActivity.this, "");
			}

		}
		// 检测是否有未完成的结算操作
		if (Utility.getSettleStatus(StandbyActivity.this)) {
			settleStatus = true;
			Dialog dialog = new DialogMessage(StandbyActivity.this).alert("提示", "有未完成的结算操作,请先执行结算！",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							reSwipeCard(); // 提示框消失，可刷卡。
							reSeachCard("");
						}
					}, null);
			dialog.show();
		} else {
			settleStatus = false;
		}

		return settleStatus || printStatus;
	}

	private void openPrintDev() {
		try {
			printDev = new PrintDev();
			printDev.openDev();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
	}

	/**
	 * 打印结算数据
	 */
	private void printSettleData() {
		isPrinting = true;
		settleDataMap = new HashMap<String, String>();
		settleDataMap.put("transprocode", "900000");
		settleDataMap.put("batchbillno", parConfig.get("settlebatchno"));
		settleDataMap.put("translocaldate", parConfig.get("translocaldate"));
		settleDataMap.put("translocaltime", parConfig.get("translocaltime"));
		settleDataMap.put("requestSettleData", parConfig.get("requestSettleData"));
		settleDataMap.put("settledata", parConfig.get("settledata"));
		settleDataMap.put("respcode", parConfig.get("respcode"));
		settleDataMap.put("isReprints", "true");
		printDev.printData(settleDataMap, new CallBack() {

			@Override
			public void isPrintSecond() {
				isPrinting = false;
				printDev.close();
				/*
				 * modify by chenkehui for 需求更改：重打印结算时，不弹出是否打印明细 @20130814
				 */
				// Intent intent = new Intent();
				// intent.putExtra("code", "900000");
				// intent.setClass(StandbyActivity.this,
				// PrintAgianDialog.class);
				// startActivityForResult(intent, 2);
				// addActivityAnim();

				Utility.setPrintStatus(StandbyActivity.this, "");
				/*
				 * if (Utility.getSettleStatus(getApplicationContext())) {
				 * //若是结算状态，自动签退 Utility.autoSingOut(getApplicationContext()); }
				 */
				reSwipeCard(); // 打印完之后可刷卡
				reSeachCard("");
			}

			@Override
			public void printExcept(int code, Bundle b) {
				printDev.close();
				isPrinting = false;
				if (code == 0x30) {
					creatExceptDialog(StandbyActivity.this, b);
				} else {
					DialogFactory.showTips(StandbyActivity.this, "打印机异常请稍候再试！");
				}
			}
		});
	}

	/**
	 * 打印上一笔方法
	 */
	private void printLastTransRecord() {
		isPrinting = true;
		lastTransMap = TransactionUtility.transformToMap(transRecord.getLastTransRecord());
		if (lastTransMap != null) {
			lastTransMap.put("isReprints", "true");
			printDev.printData(lastTransMap, new CallBack() {
				@Override
				public void isPrintSecond() {
					/**
					 * 若isPrintting = false， 弹出是否打印第二联过程会有一小段时间间隔，
					 * 一直触摸屏幕会导致返回主菜单，故去掉该代码
					 */
					// isPrinting = false;
					printDev.close();
					Intent intent = new Intent();
					intent.setClass(StandbyActivity.this, PrintAgianDialog.class);
					startActivityForResult(intent, 1);
					addActivityAnim();
				}

				@Override
				public void printExcept(int code, Bundle b) {
					isPrinting = false;
					printDev.close();
					if (code == 0x30) {
						creatExceptDialog(StandbyActivity.this, b);
					} else {
						DialogFactory.showTips(StandbyActivity.this, "打印机异常请稍候再试！");
					}
				}
			});
		}
	}

	private void setTimeView() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = formatter.format(new java.util.Date());
		timeText.setText(dateStr);
	}

	public String getVersion() {
		try {
			String verson = com.centerm.lklcpos.util.Config.getInstance(this).getConfig("printAppVersionId", "");
			// String verson = parConfig.get("printAppVersionId");
			if ("".equals(verson) || verson == null) {
				return "";
			} else {
				return verson.substring(4, verson.length());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
			return "";
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.standby, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (isPrinting) { // event.getDeviceId()==2 ||
			return true;
		}
		if (!isPrinting) {
			if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK) {
				StandbyService.isStandByStatus = false;
				StandbyService.onOperate();
				Intent intent = new Intent(StandbyActivity.this, MenuSpaceActivity.class);
				startActivity(intent);
				addActivityAnim();
				lklcposActivityManager.removeActivity(this);
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	// IC卡检卡
	public void reSeachCard(String reason) {
		if ("".equals(reason) || reason == null || "检卡超时".equals(reason)) {
			pbocDev.searchCard(0x02, 60);
		} else if ("fallback".equals(reason)) {
			try {
				pbocDev.closeDev();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
			}
			pbocDev.searchCard(0x02, 60);
		}
		if (isStopSwipe) {
			isStopSwipe = false;
			reSwipeCard();
		}
	}

	// 进入主菜单
	private void gotoMenuSpaceFormStandby() {
		Intent intent = new Intent(StandbyActivity.this, MenuSpaceActivity.class);
		startActivity(intent);
		addActivityAnim();
	}

	// 待机界面插IC卡消费判断
	private void icSaleStatusDetection(Context context) {
		boolean isSigned = Utility.getSignStatus(context);
		if (isSigned) { // 如果已经签到
			StandbyService.isStandByStatus = false;
			StandbyService.onOperate(); // 待机状态设为false
			if (Utility.getSettleStatus(context)) {
				DialogFactory.showTips(context, "请先结算再执行待机消费操作！");
				gotoMenuSpaceFormStandby();
			} else if (Utility.isMaxCount(context)) {
				DialogFactory.showTips(context, "当批次交易笔数已达上限，请先结算！");
				gotoMenuSpaceFormStandby();
			} else {
				startStandbySale(context, this.map, "transcation/T000002.xml");
			}
		} else { // 否则执行自动签到
			autoSign();
		}
	}

	// add by txb
	/**
	 * 重新刷卡
	 */
	public void reSwipeCard() {
		magcard.swipeCard("standby");
	}

	/**
	 * 刷卡成功，获取刷卡返回数据
	 */
	public void getSwipeCardData() {

		try {
			if (Utility.getSettleStatus(StandbyActivity.this)) {
				DialogFactory.showTips(StandbyActivity.this, "有未结算操作，请先结算！");
				reSwipeCard();
			} else {
				clearTackData();
				magcard.getSwipeCardData("swipecard", this.map);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}

	}

	public MagCardDevJsIfc getMagcard() {
		return magcard;
	}

	public void setMagcard(MagCardDevJsIfc magcard) {
		this.magcard = magcard;
	}

	public Map<String, String> getMap() {
		return map;
	}

	/**
	 * 方法描述：执行自动签到操作
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-7-6 下午10:34:23
	 */
	public void autoSign() {
		Transaction autoSignTransaction = new Transaction();
		autoSignTransaction.setMctCode("002308");
		List<ComponentNode> componentNodeList = new ArrayList<ComponentNode>();
		ComponentNode progressNode = new ComponentNode();
		progressNode.setComponentId("1");
		progressNode.setComponentName("com.lkl.farmer.progress");
		componentNodeList.add(progressNode);
		autoSignTransaction.setComponentNodeList(componentNodeList);

		Intent autoSignIntent = new Intent();
		autoSignIntent.setAction(progressNode.getComponentName());
		autoSignIntent.putExtra("transaction", autoSignTransaction);
		autoSignIntent.putExtra("isAuto", true);
		startActivityForResult(autoSignIntent, 0x1A);
		addActivityAnim();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 0x1A) {
			switch (resultCode) {
			// 自动签到成功
			case Activity.RESULT_OK:
				StandbyService.isStandByStatus = false; // 待机状态设为false
				StandbyService.onOperate();
				// 启动待机消费流程
				if (Utility.getSettleStatus(StandbyActivity.this)) {
					DialogFactory.showTips(StandbyActivity.this, "请先结算再执行待机消费操作！");
					Intent intent = new Intent(StandbyActivity.this, MenuSpaceActivity.class);
					startActivity(intent);
					addActivityAnim();
				} else if (Utility.isMaxCount(StandbyActivity.this)) {
					DialogFactory.showTips(StandbyActivity.this, "当批次交易笔数已达上限，请先结算！");
					Intent intent = new Intent(StandbyActivity.this, MenuSpaceActivity.class);
					startActivity(intent);
					addActivityAnim();
				} else {
					startStandbySale(this, this.map, "transcation/T000002.xml");
				}
				lklcposActivityManager.removeActivity(this);
				break;
			// 自动签到失败
			case Activity.RESULT_CANCELED:
				TradeBaseActivity.isTransStatus = false;
				StandbyService.isStandByStatus = false;
				StandbyService.onOperate();
				gotoMenuSpaceFormStandby();
				DialogFactory.showTips(this, "自动签到失败！");
				break;
			default:
				break;
			}
		} else if (requestCode == 1) {
			openPrintDev();
			if (resultCode != RESULT_CANCELED) {
				lastTransMap.put("isReprints", "true");
				lastTransMap.put("isSecond", "true");
				printDev.printData(lastTransMap, new CallBack() {

					@Override
					public void isPrintSecond() {
						isPrinting = false;
						printDev.close();
						Utility.setPrintStatus(StandbyActivity.this, "");

						isStatusing = false;
						reSwipeCard(); // 打印完之后可刷卡
						reSeachCard("");
					}

					@Override
					public void printExcept(int code, Bundle b) {
						isPrinting = false;
						printDev.close();
						if (code == 0x30) {
							creatExceptDialog(StandbyActivity.this, b);
						} else {
							DialogFactory.showTips(StandbyActivity.this, "打印机异常请稍候再试！");
						}
					}
				});
			} else {
				isPrinting = false;
				printDev.close();
				Utility.setPrintStatus(StandbyActivity.this, "");

				isStatusing = false;
				// reSwipeCard(); //打印完之后可刷卡
			}
		} else if (requestCode == 2) {
			openPrintDev();
			if (resultCode != RESULT_CANCELED) {
				settleDataMap.put("isReprints", "true");
				settleDataMap.put("printDetails", "true");
				printDev.printData(settleDataMap, new CallBack() {

					@Override
					public void isPrintSecond() {
						printDev.close();
						isPrinting = false;
						Utility.setPrintStatus(StandbyActivity.this, "");
						isStatusing = false;
						reSwipeCard(); // 打印完之后可刷卡
						reSeachCard("");
						/*
						 * if (Utility.getSettleStatus(getApplicationContext()))
						 * { //若是结算状态，自动签退
						 * Utility.autoSingOut(getApplicationContext()); }
						 */
					}

					@Override
					public void printExcept(int code, Bundle b) {
						printDev.close();
						isPrinting = false;
						if (code == 0x30) {
							creatExceptDialog(StandbyActivity.this, b);
						} else {
							DialogFactory.showTips(StandbyActivity.this, "打印机异常请稍候再试！");
						}
					}
				});
			} else {
				settleDataMap.put("isReprints", "true");
				settleDataMap.put("printDetails", "false");
				printDev.printData(settleDataMap, new CallBack() {

					@Override
					public void isPrintSecond() {
						printDev.close();
						isPrinting = false;
						Utility.setPrintStatus(StandbyActivity.this, "");
						isStatusing = false;
						reSwipeCard(); // 打印完之后可刷卡
						reSeachCard("");
						/*
						 * if (Utility.getSettleStatus(getApplicationContext()))
						 * { //若是结算状态，自动签退
						 * Utility.autoSingOut(getApplicationContext()); }
						 */
					}

					@Override
					public void printExcept(int code, Bundle b) {
						printDev.close();
						isPrinting = false;
						if (code == 0x30) {
							creatExceptDialog(StandbyActivity.this, b);
						} else {
							DialogFactory.showTips(StandbyActivity.this, "打印机异常请稍候再试！");
						}
					}
				});
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 方法描述：启动待机消费配置组件
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-7-6 下午10:36:09
	 * @param mContext
	 * @param map
	 * @param filepath
	 */
	public void startStandbySale(Context mContext, Map<String, String> map, String filepath) {
		Log.i("ckh", "StandbyActivity map =" + map.toString());
		Shortcut saleShortcut = new Shortcut(mContext, filepath);
		Transaction mtranscation = saleShortcut.getmTransaction();
		mtranscation.setDataMap(map);
		Intent saleIntent = saleShortcut.getmIntent();
		saleIntent.putExtra("transaction", mtranscation);
		saleIntent.putExtra("isFormStandby", true);
		startActivity(saleIntent);
		addActivityAnim();
		lklcposActivityManager.removeActivity(this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		StandbyService.isStandByStatus = false; // 复位，取消待机状态
		StandbyService.onOperate();
		logger.info("离开待机界面 isStandByStatus：" + StandbyService.isStandByStatus + "\nisStopStandby："
				+ StandbyService.isStopStandby + "\nTradeBaseActivity.isHttp" + TradeBaseActivity.isHttp);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (isUnbind) {
			InputmethodCtrl.getInstance().unestablish(this);
			StandbyActivity.isUnbind = false;
		}
		try {
			pbocDev.closeDev();
			pbocHandler = null;
			pbocDev = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}

		this.handler = null;
		this.magCardDev = null;
		unregisterReceiver(mNoPosparamReceiver);
		Log.i("ckh", "StandbyActivity onDestroy()");
		super.onDestroy();
	}

	/**
	 * 
	 */
	private BroadcastReceiver mNoPosparamReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ("com.centerm.lklcpos.posparam".equals(intent.getAction())) {
				Dialog dialog = new DialogMessage(StandbyActivity.this).alert("提示", "未安装配置文件，请进入设置手动设置参数！",
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}, null);
				dialog.show();
			}
		}
	};
}
