package com.centerm.lklcpos.activity;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.entity.SettleData;
import com.centerm.comm.persistence.entity.TransRecord;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.SettleDataDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.deviceinterface.EMVTAG;
import com.centerm.lklcpos.deviceinterface.MagCardDevJsIfc;
import com.centerm.lklcpos.deviceinterface.PbocDevJsIfc;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.DialogMessage;
import com.centerm.lklcpos.util.Utility;
import com.centerm.lklcpos.view.PbocWiget;
import com.lkl.farmerwithdrawals.R;

/**
 * 刷卡界面 作者：cxy 时间：2013.06.21
 */
public class SwipeCardActivity extends TradeBaseActivity {

	private static final Logger log = Logger.getLogger(SwipeCardActivity.class);

	private EditText swipcardET;
	private Button nextBtn;
	private TextView swipMsg;
	private MagCardDevJsIfc magCardDev;
	private MagCardDevJsIfc magcard; // 磁条卡设备

	private PbocDevJsIfc pbocDev; // PBOC流程接口
	private Handler pbocHandler = null;
	private PbocWiget pbocWiget;
	private Dialog interDialog = null;

	private Handler handler = null;
	private String mctCode;
	private String lastCardNum = null;
	private String pretemp = ""; // 存储上次输入的数据;
	private boolean isHandInputCardNum;
	private ProgressDialog mDialog;
	private Dialog dialog;
	private Map<String, String> dataMap = null;

	private String batchno; // 批次号
	private String billno; // 凭证号
	private Long systraceno; // POS流水号
	private ParamConfigDao mParamConfigDao;
	private TransRecordDao mTransRecordDao;
	private SettleDataDaoImpl settleDataDao;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swipecard);
		this.handler = new MainActivityHandle(this);
		init();

		pbocHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Bundle b = msg.getData();
				boolean result = b.getBoolean("result");
				String reason = b.getString("reason");

				if (Utility.isThatMsg(msg, "msg_rf_pre_progress")) { // 预处理接口回调
					if (result) { // 检卡
						pbocDev.cashup_rfStartProc(dataMap);
					} else { // 预处理失败，不支持非接
						DialogFactory.showTips(SwipeCardActivity.this, reason);
						overTranscation();
					}
				} else if (Utility.isThatMsg(msg, "msg_check_card")) { // 检卡回调
					if (result) {
						StandbyService.stopStandby(); // 插卡之后不做超时处理
						mDialog = new DialogMessage(SwipeCardActivity.this).createProgressDialog("正在读卡信息，请稍候（勿拔卡）...");
						if ("002302".equals(mctCode)) {
							pbocDev.cashup_startProc(dataMap, 1);
						} else if ("002303".equals(mctCode)) {
							if ("0".equals(reason)) {
								pbocDev.cashup_startProc(dataMap, 1);
							} else {
								String transamount = mTransaction.getDataMap().get("transamount");
								pbocDev.resetRfPboc();
								pbocDev.rfPreProcess((byte) 0x01, mTransaction.getDataMap().get("transamount"));
								// pbocDev.cashup_rfStartProc(dataMap);
							}
						} else {
							pbocDev.readICCard(dataMap);
						}
					} else { // 检卡异常
						if ("检卡超时".equals(reason)) {
							reSearchCard(reason);
						} else {
							overTranscation();
						}
					}
				} else if (Utility.isThatMsg(msg, "msg_read_card") || Utility.isThatMsg(msg, "msg_cashup_startproc")) { // 读取卡片信息
					mDialog.dismiss();
					if (result) {
						showCardno(dataMap.get("priaccount"));
						if ("1".equals(dataMap.get("isUseCashFun"))) { // 使用电子现金功能
							pbocDev.cashup_kernelProc(dataMap);
						} else {
							if ("002303".equals(mctCode)) {
								String card = dataMap.get("priaccount");
								System.err.println("card ->>>>>>>" + card);
								if ("offline".equals(reason)) {
									showCardno(dataMap.get("priaccount"));
									pbocDev.rfkernelProc(dataMap);
								} else if ("online".equals(reason)) {
									showCardno(dataMap.get("priaccount"));
									dataMap.put("posInputType", "071");
								}
								gotoNext("1");
								return;
							}
							if ("002323".equals(mctCode) && "051".equals(dataMap.get("posInputType"))) {
								DialogFactory.showTips(SwipeCardActivity.this, "读卡成功，请拨转出卡");
							}
							gotoNext("1");
						}
					} else {
						StandbyService.startStandby(); // 读卡失败，开启超时计时
						String tip = null;
						if ("fallback".equals(reason)) { // fallback
							tip = "读取IC卡错误，请刷卡！";
						} else {
							tip = "读取IC卡失败，请拔卡！";
						}
						if (interDialog != null && interDialog.isShowing()) {
							interDialog.dismiss();
							interDialog = null;
						}
						dialog = new DialogMessage(SwipeCardActivity.this).alert("提示", tip,
								new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								reSearchCard("fallback");
							}
						}, null);
						dialog.show();
					}
				} else if (Utility.isThatMsg(msg, "msg_cashup_kernelProc")) { // kernelProc接口回调
					if (result) {
						mDialog.dismiss();
						if ("offline".equals(reason)) {
							// 普通消费 交易成功，1保存交易流水表，2配置打印信息
							getKernelDataForPrint(dataMap);
							saveDataToDB(dataMap);
							mTransaction.setMctCode("000002"); // 转电子现金普通消费流程
							mTransaction.setResultMap(makeResultMap());
							Utility.setPrintStatus(SwipeCardActivity.this, "strans");
							gotoNext("2");
						} else if ("online".equals(reason)) {
							gotoNext("1");
						}
					} else {
						DialogFactory.showTips(SwipeCardActivity.this, reason);
						overTranscation();
					}
				} else if (Utility.isThatMsg(msg, "msg_emv_interaction")) { // 内核请求交互
					if ("getAIDSelect".equals(reason)) { // 导入多应用选择
						interDialog = pbocWiget.createSelectedDailog(dataMap.get("aidSelectData"),
								new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								dataMap.put("AIDIndex", data); // 将用户选择的应用索引值存放在dataMap中
								pbocDev.loadResultOfAIDSelect(data);
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfAIDSelect(null);
							}
						});
					} else if ("oneAIDSelect".equals(reason)) { // 单应用确认
						interDialog = pbocWiget.createMessageDailog(dataMap.get("oneAIDSelectData"),
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
					} else if ("CashSaleSelect".equals(reason)) { // 是否使用电子现金应用确认
						interDialog = pbocWiget.createSaleAppSelectDailog(new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								dataMap.put("isUseCashFun", "1"); // 使用电子现金功能
								pbocDev.loadResultOfMessage((byte) 1);
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								dataMap.put("isUseCashFun", "0"); // 不使用电子现金功能
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

		try {
			magCardDev = new MagCardDevJsIfc(this, handler);
			pbocDev = new PbocDevJsIfc(this, pbocHandler);
			// magcard.swipeCard("");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}

		SharedPreferences mSharedPreferences = getSharedPreferences("menu_settings", 0);
		isHandInputCardNum = mSharedPreferences.getBoolean("handinputcarnum", true);
		// setListener();
		nextBtn = (Button) findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				nextAction();
			}
		});
		nextBtn.setVisibility(View.GONE);
	}

	private void overTranscation() {
		if ("002321".equals(mctCode) || // 电子现金交易结束，返回圈存界面
				"002322".equals(mctCode) || "002323".equals(mctCode) || "002324".equals(mctCode)
				|| "002325".equals(mctCode)) {
			int count = lklcposActivityManager.activityCount(TransferMenuActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(this, TransferMenuActivity.class);
				startActivity(intent);
			}
			lklcposActivityManager.removeAllActivityExceptOne(TransferMenuActivity.class);
		} else {
			int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(this, MenuSpaceActivity.class);
				startActivity(intent);
			}
			lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
		}
	}

	private void gotoNext(String condition) {
		Intent nextIntent = forward(condition);
		nextIntent.putExtra("transaction", mTransaction);
		startActivity(nextIntent);
		addActivityAnim();
	}

	/**
	 * 重新刷卡
	 */
	public void reSwipeCard() {
		magcard.swipeCard("");
	}

	/**
	 * 刷卡成功，获取刷卡返回数据
	 */
	public void getSwipeCardData() {
		try {
			magcard.getSwipeCardData("", dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}
	}

	/*
	 * 插卡时，读卡成功，回显卡号
	 */
	private void showCardno(String carno) {
		if (carno.length() > 15 && carno.length() < 20) {
			String cardNm = "";
			int j = 0, num = 0;
			if (carno.length() % 4 == 0) {
				num = (int) carno.length() / 4;
			} else {
				num = (int) carno.length() / 4 + 1;
			}
			for (int i = 0; i < num; i++) {
				if (j + 4 < carno.length()) {
					cardNm = cardNm + carno.substring(j, j + 4) + "  ";
				} else {
					cardNm = cardNm + carno.substring(j, carno.length());
				}

				j = j + 4;
			}
			swipcardET.setText(cardNm);
		}
	}

	/**
	 * 刷卡成功，回显卡号之后进入下一步
	 */
	public void updateCardNo(String carno) {
		if (carno.length() > 15 && carno.length() < 20) {

			showCardno(carno);

			if (nextBtn.isShown()) {
				nextBtn.setVisibility(View.GONE);
			}
			if (mctCode.equals("002303") || mctCode.equals("002315")) {
				if (lastCardNum != null) {
					if (!lastCardNum.equals(dataMap.get("priaccount"))) {
						DialogFactory.showTips(SwipeCardActivity.this, "卡号与原交易卡号不同，请重新刷卡！");
						clearTackData();
						reSwipeCard();
						return;
					}
				}
			}

			Intent nextIntent = forward("1");
			nextIntent.putExtra("transaction", mTransaction);
			startActivity(nextIntent);
			addActivityAnim();
		}
	}

	private void reSearchCard(String type) {
		if ("".equals(type) || type == null || "检卡超时".equals(type)) {
			if ("002303".equals(mctCode)) { // 消费撤销支持非接
				pbocDev.searchCard(0x02 | 0x08, 60);
			} else {
				pbocDev.searchCard(0x02, 60);
			}
		} else if ("fallback".equals(type)) {
			try {
				pbocDev.closeDev();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("", e);
			}
			if ("002303".equals(mctCode)) { // 消费撤销支持非接
				pbocDev.searchCard(0x02 | 0x08, 60);
			} else {
				pbocDev.searchCard(0x02, 60);
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		swipcardET.setText("");
		pretemp = "";
		clearTackData();
		if (!lklcposActivityManager.isRemoving) {
			this.magcard = magCardDev;
			this.magcard.openDev();
			reSwipeCard();
			Log.i("ckh", "刷卡打开");
			if (dialog == null || !dialog.isShowing()) {
				if ("002303".equals(mctCode)) { // 消费撤销支持非接
					pbocDev.searchCard(0x02 | 0x08, 60);
				} else {
					pbocDev.searchCard(0x02, 60);
				}
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
		if (interDialog != null && interDialog.isShowing()) {
			interDialog.dismiss();
			interDialog = null;
		}
		if (magcard != null) {
			Log.i("ckh", "刷卡关闭");
			// this.handler = null;
			magcard.closeDev();
			magcard = null;
		}

		try {
			StandbyService.startStandby();
			pbocDev.stopSearchCard();
			pbocDev.closeDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("关闭pboc出现异常");
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		this.handler = null;
		this.magCardDev = null;
		super.onDestroy();
	}

	/**
	 * 手输入卡号格式化
	 * 
	 * @param carno
	 * @author Xrh @20130815
	 * @return
	 */
	public String formatCardNo(String cardno) {
		log.debug("待格式化卡号 ：cardno = " + cardno);
		cardno = cardno.replaceAll(" ", "");
		log.debug("待格式化卡号 ：cardno = " + cardno);

		if (cardno.indexOf("-") != -1) {
			return cardno;
		}

		int size = ((cardno.length()) % 4 == 0) ? ((cardno.length()) / 4) : ((cardno.length()) / 4 + 1);

		String card = "";

		for (int i = 0; i < size; i++) {
			int endIndex = (i + 1) * 4;
			if ((i + 1) == size) {
				endIndex = cardno.length();
			}
			if (i == 0) {
				card += cardno.substring(i, endIndex);
			} else {
				card += "  " + cardno.substring(i * 4, endIndex);
			}
		}

		log.debug("格式化后卡号 ：card = " + card);
		return card;
	}

	/**
	 * 初始化界面控件
	 */
	public void init() {
		inititle();
		mParamConfigDao = new ParamConfigDaoImpl(this);
		settleDataDao = new SettleDataDaoImpl(this);
		mTransRecordDao = new TransRecordDaoImpl(this);

		pbocWiget = new PbocWiget(this);
		dataMap = mTransaction.getDataMap();
		lastCardNum = dataMap.get("oldpriaccount");

		mctCode = mTransaction.getMctCode();
		swipcardET = (EditText) findViewById(R.id.swipcardET);
		nextBtn = (Button) findViewById(R.id.nextBtn);
		// swipMsg = (TextView) findViewById(R.id.swipe_message);
		if (mctCode.equals("002316") || mctCode.equals("002314")) { // 判断是不是预授权撤销和预授权完成，让其支持手动输入卡号
			SharedPreferences mSharedPreferences = SwipeCardActivity.this.getSharedPreferences("menu_settings", 0);
			if (mSharedPreferences.getBoolean("handinputcarnum", true)) {
				swipcardET.setHint("请刷卡或者插IC卡或者手输卡号");
				swipcardET.setInputType(InputType.TYPE_CLASS_NUMBER);
				swipcardET.addTextChangedListener(new TextWatcher() {
					String sub = "", temp = "";
					int end = 0;

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						if (!nextBtn.isShown()) {
							if (s.length() > 0) {
								nextBtn.setVisibility(View.VISIBLE);
							}
						} else {
							if (s.length() == 0) {
								nextBtn.setVisibility(View.GONE);
							}
						}
						log.debug("onTextChanged     s      = " + s);
						log.debug("onTextChanged     start  = " + start);
						log.debug("onTextChanged     before = " + before);
						log.debug("onTextChanged     count  = " + count);

						sub = s.toString().replaceAll(" ", "");
						log.debug("sub = " + sub + "  || temp = " + temp + "&& s =temp" + (temp.equals(s.toString())));
						log.debug("s" + s.length() + "|| temp" + temp.length());
						//
						// swipcardET.setSelection(s.length());
						if (s != null && !"".equals(sub) && !temp.trim().equals(s.toString().trim())) {
							temp = formatCardNo(sub.toString());
							swipcardET.setText(temp);
							// swipcardET.setSelection(temp.length());

							int cursor = start;
							log.debug("pretemp=" + pretemp + "||s=" + s);
							if (pretemp.length() > s.toString().length()) { // 删除字符
								log.debug("删除>> " + cursor);
								if (start == 6 || start == 12 || start == 18 || start == 24) {
									cursor -= 2;
									log.debug("cursor -2");
								}

								swipcardET.setSelection(cursor);
							} else {
								log.debug("增加>> " + cursor);
								if (start == 4 || start == 10 || start == 16 || start == 22) {
									cursor += 2;
									log.debug("cursor +2");
								}
								log.debug("cursor = " + cursor);
								swipcardET.setSelection(cursor + 1);
							}

							pretemp = temp;

						}

					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						// TODO Auto-generated method stub
						// System.out.println("beforeTextChanged:" + s);
					}

					@Override
					public void afterTextChanged(Editable s) {

					}
				});
				swipcardET.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						swipcardET.setFocusableInTouchMode(true);
						// swipcardET.setText("");
						swipcardET.setCursorVisible(true);

						clearTackData();
					}
				});
			} else {
				swipcardET.setKeyListener(null);
			}
		} else {
			if ("002323".equals(mctCode)) {
				swipMsg.setText("请刷卡或者插卡(转出卡)");
			}
			swipcardET.setKeyListener(null);
		}
	}

	// 当卡号不准确时，清除磁道信息
	public void clearTackData() {
		dataMap.put("priaccount", "");
		dataMap.put("track2data", "");
		dataMap.put("track3data", "");
	}

	/**
	 * 进入下一步界面
	 */
	private void nextAction() {

		if ("".equals(swipcardET.getText().toString()) || swipcardET.getText().toString() == null) {
			if (mctCode.equals("002316") || mctCode.equals("002314")) {
				DialogFactory.showTips(SwipeCardActivity.this, "请刷卡或者手动输入卡号！");
			} else {
				DialogFactory.showTips(SwipeCardActivity.this, "请刷卡！");
			}
			return;
		}
		if (swipcardET.getText().toString().length() > 27 || swipcardET.getText().toString().length() < 22) {
			DialogFactory.showTips(SwipeCardActivity.this, "位数不对！");
			return;
		}
		if (mctCode.equals("002316") || mctCode.equals("002314")) {
			String cardNum = swipcardET.getText().toString().replace("  ", "");
			dataMap.put("priaccount", cardNum);
			/** delete by chenkehui @20130808 */
			// mTransaction.getDataMap().put("track2data", "");
			// mTransaction.getDataMap().put("track3data", "");
		}
		if (mctCode.equals("002303") || mctCode.equals("002315")) {
			if (lastCardNum != null) {
				if (!lastCardNum.equals(dataMap.get("priaccount"))) {
					DialogFactory.showTips(SwipeCardActivity.this, "卡号与原交易卡号不同，请重新刷卡！");
					clearTackData();
					return;
				}
			}
		}
		// 手输卡号，pos输入方式
		dataMap.put("posInputType", "011");

		Intent nextIntent = null;
		SharedPreferences mSharedPreferences = SwipeCardActivity.this.getSharedPreferences("menu_settings", 0);
		if (mSharedPreferences.getBoolean("handinputcarnum", true)
				&& (mctCode.equals("002316") || mctCode.equals("002314"))) {
			nextIntent = forward("2");
		} else {
			nextIntent = forward("1");
		}
		nextIntent.putExtra("transaction", mTransaction);
		startActivity(nextIntent);
		addActivityAnim();
	}

	public MagCardDevJsIfc getMagcard() {
		return magcard;
	}

	public void setMagcard(MagCardDevJsIfc magcard) {
		this.magcard = magcard;
	}

	/** 以下为电子现金普通消费代码 **/
	// 读取用于打印凭条所要求的内核数据
	private void getKernelDataForPrint(Map<String, String> map) {
		try {
			String printData = pbocDev.readEMVData(EMVTAG.getkernelDataForPrint());
			Log.i("ckh", "reserve4 == " + printData);
			map.put("reserve4", printData); // 将要打印的数据存放在预留字段4中
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("", e);
		}
	}

	/**
	 * 交易成功为脱机消费构造显示结果界面所需字段，打印数据
	 * 
	 * @param resMap
	 */
	private Map<String, String> makeResultMap() {
		Map<String, String> resMap = new HashMap<String, String>();
		resMap.put("priaccount", dataMap.get("priaccount"));
		resMap.put("transprocode", "000000");
		resMap.put("reserve1", "0330");
		resMap.put("batchbillno", batchno + billno);
		resMap.put("transamount", Utility.formatMount(dataMap.get("transamount")));
		resMap.put("conditionmode", "00");
		resMap.put("respcode", "3030");
		resMap.put("loadparams", mParamConfigDao.get("operatorcode"));
		resMap.put("translocaldate", Utility.getTransLocalDate());
		resMap.put("translocaltime", Utility.getTransLocalTime());
		resMap.put("entrymode", "051");
		resMap.put("posInputMode", "   /EC");
		resMap.put("reserve4", dataMap.get("reserve4"));
		return resMap;
	}

	/**
	 * 将脱机消费数据保存到数据库
	 * 
	 * @param dataobj
	 */
	public void saveDataToDB(Map<String, String> dataobj) {
		TransRecord transRecord = MapObjToTransBean(dataobj); // 交易流水表记录
		mTransRecordDao.save(transRecord);
		SettleData settleData = MapObjToSettleBean(dataobj); // 结算表记录
		settleDataDao.save(settleData);
	}

	private void getNumberForPack() {
		batchno = mParamConfigDao.get("batchno"); // 批次号
		billno = mParamConfigDao.get("billno");
		String systracenoStr = mParamConfigDao.get("systraceno");

		if ("".equals(batchno) || batchno == null) { // 对批次号、凭证号、流水号判空处理
			batchno = "000001";
		} else if (batchno.length() < 6) {
			batchno = Utility.addZeroForNum(batchno, 6);
		}
		if ("".equals(billno) || billno == null) {
			billno = "000001";
		} else if (billno.length() < 6) {
			billno = Utility.addZeroForNum(billno, 6);
		}
		if ("".equals(systracenoStr) || systracenoStr == null) {
			systracenoStr = "000000";
		}

		systraceno = Long.valueOf(systracenoStr) + 1; // POS流水号
		if (systraceno > 999999) {
			systraceno = (long) 1;
		}

		mParamConfigDao.update("systraceno", String.valueOf(systraceno));
		if (Integer.valueOf(billno) + 1 > 999999) {
			billno = Utility.addZeroForNum(String.valueOf(1), 6);
		} else {
			billno = Utility.addZeroForNum(String.valueOf(Integer.valueOf(billno) + 1), 6);
		}
		mParamConfigDao.update("billno", billno);
	}

	/**
	 * 将Map数据转化为交易对象 modify by chenkehui for 8583jar-ic
	 * 
	 * @param hashmap
	 * @return
	 */
	public TransRecord MapObjToTransBean(Map<String, String> dataobj) {
		TransRecord record = null;
		try {
			record = new TransRecord();
			record.setPriaccount(dataobj.get("priaccount")); // 主账号 F2
			record.setTransprocode("000000"); // 交易处理码 F3
			record.setTransamount(Utility.formatMount(dataobj.get("transamount"))); // 交易金额
																					// F4
			getNumberForPack();
			record.setSystraceno(String.valueOf(systraceno)); // pos流水号(11域)
			record.setTranslocaldate(Utility.getTransLocalDate());
			record.setTranslocaltime(Utility.getTransLocalTime());
			record.setExpireddate(dataobj.get("expireddate")); // 卡有效期（14域）
			record.setEntrymode("052"); // POS输入方式(22域)
			record.setSeqnumber(dataobj.get("seqnumber")); // F23
			record.setConditionmode("00"); // F25
			record.setTerminalid(mParamConfigDao.get("termid")); // F41
			record.setAcceptoridcode(mParamConfigDao.get("merid")); // F42
			record.setAdddataword("300"); // F48
			record.setTranscurrcode("156"); // F49
			record.setIcdata(dataobj.get("icdata")); // F55
			record.setLoadparams(mParamConfigDao.get("operatorcode") + mParamConfigDao.get("operatorpwd") + "50"); // F60
			record.setBatchbillno(batchno + billno); // F62
			record.setReserve4(dataobj.get("reserve4"));
			record.setStatuscode("OF"); // offline脱机标志
			record.setReserve1("0330"); // 保留字段1，保存交易下发的消息类型
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}
		return record;
	}

	// 将Map数据转化为结算明细表记录对象
	public SettleData MapObjToSettleBean(Map<String, String> dataobj) {
		SettleData record = new SettleData();
		record.priaccount = dataobj.get("priaccount");// 卡号
		record.batchbillno = batchno + billno;
		record.transamount = Utility.formatMount(dataobj.get("transamount"));// 金额
		record.conditionmode = "00";// 服务条件码
		record.transprocode = "000000";// 交易处理码
		record.reserve1 = "0330"; // 保留字段1，保存交易下发的消息类型
		return record;
	}
}