package com.centerm.lklcpos.activity;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.entity.SettleData;
import com.centerm.comm.persistence.entity.TransRecord;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.SettleDataDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.deviceinterface.EMVTAG;
import com.centerm.lklcpos.deviceinterface.PbocDevJsIfc;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.DialogMessage;
import com.centerm.lklcpos.util.Utility;
import com.centerm.lklcpos.view.PbocWiget;
import com.lkl.farmerwithdrawals.R;

/**
 * 电子现金普通消费，插卡
 */
public class IcSwipeCardActivity extends TradeBaseActivity {

	private static final Logger logger = Logger.getLogger(IcSwipeCardActivity.class);

	private EditText swipcardET;
	private Button nextBtn;

	private PbocDevJsIfc pbocDev; // PBOC流程接口
	private Handler pbocHandler = null;
	private PbocWiget pbocWiget;
	private ProgressDialog mDialog;
	private Dialog interDialog = null;
	private Dialog dialog;
	private String mctCodem;
	private Map<String, String> dataMap;

	private ParamConfigDao mParamConfigDao;
	private TransRecordDao mTransRecordDao;
	private SettleDataDaoImpl settleDataDao;

	private String batchno; // 批次号
	private String billno; // 凭证号
	private Long systraceno; // POS流水号

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transferswipecard);
		init();

		pbocHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Bundle b = msg.getData();
				boolean result = b.getBoolean("result");
				String reason = b.getString("reason");

				if (Utility.isThatMsg(msg, "msg_check_card")) { // 检卡回调
					if (result) { // startProc
						StandbyService.stopStandby();
						pbocDev.cashup_startProc(dataMap, 1);
						mDialog = new DialogMessage(IcSwipeCardActivity.this)
								.createProgressDialog("正在读卡信息，请稍候（勿拔卡）...");
					} else { // 检卡异常
						if ("检卡超时".equals(reason)) {
							reSearchCard(reason);
						} else {
							overTranscation();
						}
					}
				} else if (Utility.isThatMsg(msg, "msg_cashup_startproc")) { // startProc接口回调
					if (result) {
						showCardno(dataMap.get("priaccount"));
						pbocDev.cashup_kernelProc(dataMap);
					} else {
						StandbyService.startStandby();
						mDialog.dismiss();
						String tip = null;
						if ("fallback".equals(reason)) { // fallback
							DialogFactory.showTips(IcSwipeCardActivity.this, "读取IC卡错误，交易终止");
							overTranscation();
						} else {
							tip = "读取IC卡失败，请拔卡！";
							if (interDialog != null && interDialog.isShowing()) {
								interDialog.dismiss();
								interDialog = null;
							}
							dialog = new DialogMessage(IcSwipeCardActivity.this).alert("提示", tip,
									new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									reSearchCard("fallback");
								}
							}, null);
							dialog.show();
						}
					}
				} else if (Utility.isThatMsg(msg, "msg_cashup_kernelProc")) { // kernelProc接口回调
					if (result) {
						mDialog.dismiss();
						if ("offline".equals(reason)) {
							// 普通消费 交易成功，1保存交易流水表，2配置打印信息
							getKernelDataForPrint(dataMap);
							saveDataToDB(dataMap);
							mTransaction.setResultMap(makeResultMap());
							Utility.setPrintStatus(IcSwipeCardActivity.this, "strans");
							gotoNext("1");
						} else if ("online".equals(reason)) {
							if (!Utility.getSignStatus(IcSwipeCardActivity.this)) {
								DialogFactory.showTips(IcSwipeCardActivity.this, "交易联机，请先签到");
								overTranscation();
								return;
							}
							mTransaction.setMctCode("002302"); // 转消费流程
							gotoNext("2");
						}
					} else {
						DialogFactory.showTips(IcSwipeCardActivity.this, reason);
						overTranscation();
					}
				} else if (Utility.isThatMsg(msg, "msg_emv_interaction")) { // 内核请求交互
					if ("getOfflinePin".equals(reason)) { // 导入脱机pin
						interDialog = pbocWiget.InputOffPINDailog(dataMap.get("priaccount"), new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								pbocDev.pinByUser(data);
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								pbocDev.pinByUser(null); // 取消导入PIN为空
							}
						});
					} else if ("getAIDSelect".equals(reason)) { // 导入多应用选择
						interDialog = pbocWiget.createSelectedDailog(dataMap.get("aidSelectData"),
								new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								pbocDev.loadResultOfAIDSelect(data);
								// dataMap.put("AIDIndex", data); //
								// 将用户选择的应用索引值存放在dataMap中
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
			pbocDev = new PbocDevJsIfc(this, pbocHandler);
			// magcard.swipeCard("");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}

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

	// IC卡插卡
	private void reSearchCard(String type) {
		if ("".equals(type) || type == null || "检卡超时".equals(type)) {
			pbocDev.searchCard(0x02, 60);
		} else if ("fallback".equals(type)) {
			try {
				pbocDev.closeDev();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
			}
			pbocDev.searchCard(0x02, 60);
		}
	}

	private void gotoNext(String condition) {
		Intent nextIntent = forward(condition);
		nextIntent.putExtra("transaction", mTransaction);
		startActivity(nextIntent);
		addActivityAnim();
	}

	private void overTranscation() {
		lklcposActivityManager.removeActivityTo(OtherActivity.class);
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

			Intent nextIntent = forward("1");
			nextIntent.putExtra("transaction", mTransaction);
			startActivity(nextIntent);
			addActivityAnim();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		swipcardET.setText("");
		if (!lklcposActivityManager.isRemoving) {
			try {
				pbocDev.openDev();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
			}
			pbocDev.resetPboc();
			pbocDev.searchCard(0x02, 60);
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
		StandbyService.startStandby();
		pbocDev.stopSearchCard();
		try {
			pbocDev.closeDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * 初始化界面控件
	 */
	public void init() {
		inititle();
		mctCodem = mTransaction.getMctCode();
		dataMap = mTransaction.getDataMap();
		swipcardET = (EditText) findViewById(R.id.swipcardET);
		swipcardET.setKeyListener(null);

		mParamConfigDao = new ParamConfigDaoImpl(this);
		settleDataDao = new SettleDataDaoImpl(this);
		mTransRecordDao = new TransRecordDaoImpl(this);

		pbocWiget = new PbocWiget(this);
	}

	// 当卡号不准确时，清除磁道信息
	public void clearTackData() {
		mTransaction.getDataMap().put("priaccount", "");
		mTransaction.getDataMap().put("track2data", "");
		mTransaction.getDataMap().put("track3data", "");

	}

	/**
	 * 进入下一步界面
	 */
	private void nextAction() {
		Intent nextIntent = forward("1");
		nextIntent.putExtra("transaction", mTransaction);
		startActivity(nextIntent);
		addActivityAnim();
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
			logger.error("", e);
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

	// 读取用于打印凭条所要求的内核数据
	private void getKernelDataForPrint(Map<String, String> map) {
		try {
			String printData = pbocDev.readEMVData(EMVTAG.getkernelDataForPrint());
			Log.i("ckh", "reserve4 == " + printData);
			map.put("reserve4", printData); // 将要打印的数据存放在预留字段4中
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}
	}
}