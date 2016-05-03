﻿package com.centerm.lklcpos.activity;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.centerm.android.adapter.MyAdapterMoney;
import com.centerm.android.adapter.ViewHolder;
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
import com.centerm.lklcpos.util.SharePreferenceUtils;
import com.centerm.lklcpos.util.Utility;
import com.centerm.lklcpos.view.PbocWiget;
import com.lkl.farmerwithdrawals.R;

/*
 * 输入金额组件
 */
public class InputMoneyActivity extends TradeBaseActivity {
	private static Logger logger = Logger.getLogger(InputMoneyActivity.class);

	private EditText mEditText;
	private Button mButton;
	private Map<String, String> dataMap;

	private PbocDevJsIfc pboc;
	private PbocWiget pbocWiget;
	private Handler handler = null;
	private ProgressDialog mDialog;
	private Dialog dialog;
	private String batchno; // 批次号
	private String billno; // 凭证号
	private Long systraceno; // POS流水号
	private ParamConfigDao mParamConfigDao;
	private TransRecordDao mTransRecordDao;
	private SettleDataDaoImpl settleDataDao;
	private GridView gv_money;
	private RadioButton rb_other_money;
	private ImageView iv_attertion;
	private boolean ISSELECT;
	boolean isChanged = false; // 标记间隔变化
	String acount = null;
	int l = 4; // 改变前的长度
	private int[] selectmoney = new int[] { 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000 };
	ViewHolder lasttag = null;

	// private Transaction mTransaction;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i("ckh", "ConsumeActivity OnCreate()......");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consume_layout);
		inititle();
		dataMap = mTransaction.getDataMap();
		gv_money = (GridView) findViewById(R.id.gv_money);
		gv_money.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gv_money.setAdapter(new MyAdapterMoney(selectmoney));
		rb_other_money = (RadioButton) findViewById(R.id.other_btn);
		iv_attertion = (ImageView) findViewById(R.id.im_attetion);
		mEditText = (EditText) findViewById(R.id.input_money);
		mEditText.setVisibility(View.GONE);
		iv_attertion.setVisibility(View.GONE);
		mEditText.setText("0.00");
		String showother = SharePreferenceUtils.getPrefString(getApplicationContext(), "showother", "0");
		if("0".equals(showother)){
			rb_other_money.setVisibility(View.INVISIBLE);
		}
		else{
			rb_other_money.setVisibility(View.VISIBLE);
		}
		gv_money.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mEditText.setText(selectmoney[arg2] + ".00");
				if (!ISSELECT) {
					if (lasttag != null) {
						lasttag.bt.setBackgroundResource(R.drawable.input_password);
						lasttag = null;
					}
					ViewHolder current = (ViewHolder) arg1.getTag();
					current.bt.setBackgroundResource(R.drawable.select_money_bg);
					lasttag = current;
					current = null;
				}

			}
		});
		rb_other_money.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				ISSELECT = true;
				if (lasttag != null) {
					lasttag.bt.setBackgroundResource(R.drawable.input_password);
				}
				mEditText.setVisibility(View.VISIBLE);
				iv_attertion.setVisibility(View.VISIBLE);
				rb_other_money.setTextColor(Color.WHITE);
			}
		});
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (s.toString().length() > 10) {
					DialogFactory.showTips(InputMoneyActivity.this, "已超金额上限 ，请核对再输！");
					return;
				}
				if (s.toString().length() >= 0 && s.toString().length() <= 10) {
					isChanged = !isChanged;
					if (isChanged) {
						acount = Utility.unformatMount(s.toString().replace(".", ""));
						if (acount != null && !"".equals(acount)) {
							if ("0.00".equals(acount)) {
								mEditText.setText("0.00");
								isChanged = false;
								l = 4;
								Selection.setSelection(mEditText.getText(), 4);
								return;
							}

							mEditText.setText(acount);
							if (l == 0) {
								Selection.setSelection(mEditText.getText(), 4);
							} else if (l >= acount.length()) {
								Selection.setSelection(mEditText.getText(), start);
								if (s.toString().length() <= 3) {
									if (start == 1) {
										Selection.setSelection(mEditText.getText(), start);
									} else {
										Selection.setSelection(mEditText.getText(), start + 1);
									}
								}
							} else if (l < acount.length()) {
								Selection.setSelection(mEditText.getText(), start + 1);
							}
							l = acount.length();
						}
					}

				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		mParamConfigDao = new ParamConfigDaoImpl(this);
		settleDataDao = new SettleDataDaoImpl(this);
		mTransRecordDao = new TransRecordDaoImpl(this);

		pbocWiget = new PbocWiget(this);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Bundle b = msg.getData();
				boolean result = b.getBoolean("result");
				String reason = b.getString("reason");

				if (Utility.isThatMsg(msg, "msg_start_pboc")) {
					if (result) {
						if ("1".equals(dataMap.get("isUseCashFun"))) { // 使用电子现金功能
							pboc.cashup_kernelProc(dataMap);
						} else {
							gotoNext("1");
						}
					} else {
						DialogFactory.showTips(InputMoneyActivity.this, "交易终止");
						overTranscation();
					}
				} else if (Utility.isThatMsg(msg, "msg_emv_interaction")) { // 内核请求交互
					if ("CashSaleSelect".equals(reason)) { // 是否使用电子现金应用确认
						dialog = pbocWiget.createSaleAppSelectDailog(new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								dataMap.put("isUseCashFun", "1"); // 使用电子现金功能
								pboc.loadResultOfMessage((byte) 1);
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								dataMap.put("isUseCashFun", "0"); // 不使用电子现金功能
								pboc.loadResultOfMessage((byte) 0);
							}

						});
					} else if ("Special CAPK not Found".equals(reason)) {
						dialog = pbocWiget.createTipShowDialog(new PbocWiget.ClickBack() {

							@Override
							public void loadData(String data) {
								// TODO Auto-generated method stub
								pboc.loadResultOfMessage((byte) 1);
							}

							@Override
							public void loadDataCancel() {
								// TODO Auto-generated method stub
								pboc.loadResultOfMessage((byte) 0);
							}

						});
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
							Utility.setPrintStatus(InputMoneyActivity.this, "strans");
							gotoNext("2");
						} else if ("online".equals(reason)) {
							gotoNext("1");
						}
					} else {
						DialogFactory.showTips(InputMoneyActivity.this, "交易终止");
						overTranscation();
					}
				}
			}

		};
		try {
			pboc = new PbocDevJsIfc(this, handler);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}

		mButton = (Button) findViewById(R.id.ok_button);
		mButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!"0.00".equals(mEditText.getText().toString())) {
					String transamount = mEditText.getText().toString();
//					DecimalFormat df = new DecimalFormat("#0.00");
					// 将交易金额保存在transaction中传递
					try {
						double f = Double.valueOf(transamount);
						if(f % 100 != 0){
							DialogFactory.showTips(InputMoneyActivity.this, "请输入金额必须为100元的整数倍！");
							return;
						}						
//						double charge = f * 5 / 1000 > 1 ? f * 5 / 1000 : 1;
//						System.out.println("charge=" + df.format(charge));
//						// 添加手续费功能
//						mTransaction.getDataMap().put("charge", df.format(charge));
//
//						double sum = f + charge;
//						
//						mTransaction.getDataMap().put("transamount", df.format(sum));
					} catch (Exception e) {
						e.printStackTrace();
//						System.out.println("---计算手续费出错---");
//						mTransaction.getDataMap().put("charge", "");
//						mTransaction.getDataMap().put("transamount", transamount);
//						return;
					}
					mTransaction.getDataMap().put("transamount", transamount);
					// 待机插卡消费走此流程
					if (getIntent().getBooleanExtra("isFormStandby", false)
							&& "051".equals(dataMap.get("posInputType"))) {
						mButton.setClickable(false);
						mDialog = new DialogMessage(InputMoneyActivity.this)
								.createProgressDialog("              请稍候（勿拔卡）...              ");
						pboc.startProcTransfer(0x01, 0x01, dataMap);
						StandbyService.stopStandby();
						return;
					}
					// 通过主界面点击消费
					Intent nextIntent = forward("1");
					nextIntent.putExtra("transaction", mTransaction);
					startActivity(nextIntent);
					addActivityAnim();
				} else {
					DialogFactory.showTips(InputMoneyActivity.this, "请输入金额！");
				}

			}
		});

		/*
		 * mEditText.setOnKeyListener(new View.OnKeyListener() {
		 * 
		 * @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
		 * // TODO Auto-generated method stub if (keyCode ==
		 * KeyEvent.KEYCODE_ENTER) { if (event.getAction()==KeyEvent.ACTION_UP)
		 * { InputMethodManager imm = (InputMethodManager)
		 * getSystemService(Context.INPUT_METHOD_SERVICE);
		 * imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0); Thread
		 * waitThread = new Thread() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub try
		 * { sleep(20); mButton.requestFocus(); } catch (InterruptedException e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); } }
		 * 
		 * }; waitThread.start(); } return true; } return false; } });
		 */

		// mEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
		// KeyEvent.KEYCODE_ENTER));

		/*
		 * mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		 * 
		 * @Override public void onFocusChange(View v, boolean hasFocus) { //
		 * TODO Auto-generated method stub if (!hasFocus) { InputMethodManager
		 * imm = (InputMethodManager)
		 * getSystemService(Context.INPUT_METHOD_SERVICE);
		 * imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0); } } });
		 */
		/*
		 * mEditText .setOnEditorActionListener(new
		 * TextView.OnEditorActionListener() {
		 * 
		 * @Override public boolean onEditorAction(TextView arg0, int actionId,
		 * KeyEvent arg2) { // TODO Auto-generated method stub
		 * InputMethodManager inputMethodManager = (InputMethodManager)
		 * getSystemService(Context.INPUT_METHOD_SERVICE);
		 * 
		 * inputMethodManager.hideSoftInputFromWindow(
		 * InputMoneyActivity.this.getCurrentFocus() .getWindowToken(),
		 * 
		 * InputMethodManager.HIDE_NOT_ALWAYS); mButton.requestFocus(); return
		 * true; } });
		 */

	}

	private void overTranscation() {

		int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
		if (count == 0) {
			Intent intent = new Intent();
			intent.setClass(this, MenuSpaceActivity.class);
			startActivity(intent);
		}
		lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.i("ckh", "InputMoneyActivity onStart........");
		isChanged = false; // 标记间隔变化
		acount = null;
		l = 4; // 改变前的长度
		super.onStart();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getIntent().getBooleanExtra("isFormStandby", false)) {
				StandbyService.onOperate();
				Intent intent = new Intent();
				intent.setAction(mainMuneAction);
				startActivity(intent);
				outActivityAnim();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		mEditText.requestFocus();
		mButton.setClickable(true);
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
		try {
			StandbyService.startStandby();
			mButton.setClickable(true);
			pboc.closeDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}
		super.onPause();
	}

	private void gotoNext(String condition) {
		Intent nextIntent = forward(condition);
		nextIntent.putExtra("transaction", mTransaction);
		startActivity(nextIntent);
		addActivityAnim();
	}

	/** 以下为电子现金普通消费代码 **/
	// 读取用于打印凭条所要求的内核数据
	private void getKernelDataForPrint(Map<String, String> map) {
		try {
			String printData = pboc.readEMVData(EMVTAG.getkernelDataForPrint());
			Log.i("ckh", "reserve4 == " + printData);
			map.put("reserve4", printData); // 将要打印的数据存放在预留字段4中
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
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
}