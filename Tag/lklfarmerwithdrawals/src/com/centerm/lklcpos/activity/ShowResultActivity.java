﻿package com.centerm.lklcpos.activity;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc;
import com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc.GetPinBack;
import com.centerm.lklcpos.deviceinterface.PinPadDevJsIfc;
import com.centerm.lklcpos.deviceinterface.PinPadInterface;
import com.centerm.lklcpos.deviceinterface.PrintDev;
import com.centerm.lklcpos.deviceinterface.PrintDev.CallBack;
import com.centerm.lklcpos.http.ErrInfo;
import com.centerm.lklcpos.settings.activity.SettingMainActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.TimeUtils;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

public class ShowResultActivity extends TradeBaseActivity {
	private static Logger logger = Logger.getLogger(ShowResultActivity.class);

	private final int SALE_MODE = 1; // 消费
	private final int VOID_MODE = 2; // 消费撤销
	private final int INQU_MODE = 3; // 余额查询
	private final int SIGN_MODE = 4; // 签到
	private final int SETTLE_MODE = 5; // 结算
	private final int DOWNLOADCER_MODE = 6; // 下载证书
	private final int AUTH_MODE = 7; // 预授权
	private final int AUTH_COMPLETE_MODE = 8; // 预授权完成
	private final int AUTH_VOID_MODE = 9; // 预授权撤销
	private final int COMPLETE_VOID_MODE = 10; // 预授权完成撤销
	private final int REFUND_MODE = 11; // 退货

	private final int ICCADOWN_MODE = 12; // ic卡公钥
	private final int ICPARAMDOWN_MODE = 13; // ic卡参数下载

	private final int TRANSFER_MODE = 14; // 指定账户圈存
	private final int NON_TRANSFER_MODE = 15; // 非指定账户圈存
	private final int CASH_UP_MODE = 16; // 现金充值
	private final int CASH_UP_VOID_MODE = 17; // 现金充值撤销
	private final int OFFLINE_REFUND_MODE = 18; // 脱机退货

	private int CUR_MODE = 0; // 当前组件显示什么交易的结果
	private boolean isSuccess;

	private RelativeLayout lablebg;
	private ImageView iconImageView;
	private ImageView resultImageView;
	private TextView lableTextView;
	private TextView resultTextView;
	private TextView result_massage;
	private TextView trad_type;
	private TextView failmassage;
	private TextView trade_time, errorcode_tv;
	private Button confirmButton;
	private Button gobackButton;

	private String transCode;
	private String rescode;
	private PrintDev mPrintDev;
	// private ExPinPadDevJsIfc mExPinPadDev;
	private PinPadInterface pinPadDev = null;
	private ParamConfigDao mParamConfigDao;
	private String pinpadType;

	private Map<String, String> map;
	private Map<String, String> printMap;

	private boolean isClientPrint = false; // 标记是否需要询问答应客户联
	private boolean isCalled = false; // 判断是否第三方调用
	private boolean hasPrint = false; // 标记交易成功时是否需要打印
	private boolean isAutoUp = false; // 标记需自动发起脱机上送

	String errMsg = null; // 交易失败原因

	private String treatype;// 交易类型

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Log.i("ckh", "ShowResultActivity onCreate()...");
		isCalled = "D".equals(mTransaction.getProperties());
		transCode = mTransaction.getMctCode();
		treatype = typeMap.get(transCode);
		map = mTransaction.getResultMap();
		// 获取当前的日期
		// Date nowDateShort = TimeUtils.getNowDateShort();
		// 判断交易类别
		if ("002302".equals(transCode) || "000001".equals(transCode) || "000002".equals(transCode)) { // 消费交易(电子现金消费)
			CUR_MODE = SALE_MODE;
			hasPrint = true;
			if ("000001".equals(transCode) || "000002".equals(transCode)) { // 脱机消费之后需判断是需发起自动上送
				if (Utility.isAutoUp(this)) {
					isAutoUp = true;
				}
			}
		} else if ("002301".equals(transCode)) { // 余额查询
			CUR_MODE = INQU_MODE;
			hasPrint = true;
		} else if ("002303".equals(transCode)) { // 消费撤销
			CUR_MODE = VOID_MODE;
			hasPrint = true;
		} else if ("002308".equals(transCode)) { // 签到
			CUR_MODE = SIGN_MODE;
		} else if ("002309".equals(transCode)) { // 结算、签退
			CUR_MODE = SETTLE_MODE;
			hasPrint = true;
		} else if ("900004".equals(transCode)) { // 下载证书
			CUR_MODE = DOWNLOADCER_MODE;
		} else if ("002313".equals(transCode)) { // 预授权
			CUR_MODE = AUTH_MODE;
			hasPrint = true;
		} else if ("002314".equals(transCode)) { // 预授权完成
			CUR_MODE = AUTH_COMPLETE_MODE;
			hasPrint = true;
		} else if ("002315".equals(transCode)) { // 预授权完成撤销
			CUR_MODE = COMPLETE_VOID_MODE;
			hasPrint = true;
		} else if ("002316".equals(transCode)) { // 预授权撤销
			CUR_MODE = AUTH_VOID_MODE;
			hasPrint = true;
		} else if ("002317".equals(transCode)) { // 退货
			CUR_MODE = REFUND_MODE;
			hasPrint = true;
		} else if ("002319".equals(transCode)) { // IC公钥下载
			CUR_MODE = ICCADOWN_MODE;
		} else if ("002320".equals(transCode)) { // IC参数下载
			CUR_MODE = ICPARAMDOWN_MODE;
		} else if ("002322".equals(transCode)) { // 指定账户圈存
			CUR_MODE = TRANSFER_MODE;
			hasPrint = true;
		} else if ("002323".equals(transCode)) { // 非指定账户圈存
			CUR_MODE = NON_TRANSFER_MODE;
			hasPrint = true;
		} else if ("002321".equals(transCode)) { // 电子现金充值
			CUR_MODE = CASH_UP_MODE;
			hasPrint = true;
		} else if ("002324".equals(transCode)) { // 现金充值撤销
			CUR_MODE = CASH_UP_VOID_MODE;
			hasPrint = true;
		} else if ("002325".equals(transCode)) {
			CUR_MODE = OFFLINE_REFUND_MODE;
			hasPrint = true;
		}
		// 判断交易是否成功
		rescode = mTransaction.getResultMap().get("respcode");
		if (rescode != null && !"".equals(rescode)) {
			rescode = new String(HexUtil.hexStringToByte(rescode));
		}
		Log.i("ckh", "Trans rescode == " + rescode);
		if ("00".equals(rescode) || "95".equals(rescode)) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}

		if (isSuccess) { // 交易成功
			if (!hasPrint) { // 非打印界面
				setContentView(R.layout.show_result);
				inititle();

				lablebg = (RelativeLayout) findViewById(R.id.lablebg);
				iconImageView = (ImageView) findViewById(R.id.lableicon);
				resultImageView = (ImageView) findViewById(R.id.result_image);
				lableTextView = (TextView) findViewById(R.id.lablemessage);
				resultTextView = (TextView) findViewById(R.id.result_text);
				result_massage = (TextView) findViewById(R.id.result_massage_text);
				confirmButton = (Button) findViewById(R.id.ok_button);

				resultImageView.setBackgroundResource(R.drawable.activate_ok);
				confirmButton.setOnClickListener(mListener);
				// if (CUR_MODE == INQU_MODE) { // 余额查询
				// mParamConfigDao = new ParamConfigDaoImpl(this);
				// pinpadType = mParamConfigDao.get("pinpadType");
				// lablebg.setBackgroundResource(R.drawable.topbg_grey);
				// iconImageView.setBackgroundResource(R.drawable.syue_icon);
				// lableTextView.setText(R.string.consume_search_text);
				// if ("0".equals(pinpadType)) {
				// resultTextView.setText(R.string.inqu_suc_result);
				// result_massage.setText(R.string.inqu_massage);
				// }
				// } else
				if (CUR_MODE == SIGN_MODE) { // 签到
					lablebg.setBackgroundResource(R.drawable.topbg);
					iconImageView.setBackgroundResource(R.drawable.sqiandao_icon);
					lableTextView.setText(R.string.registration_text);
					resultTextView.setText(R.string.sign_suc_result);
				} else if (CUR_MODE == DOWNLOADCER_MODE) { // 下载证书
					lablebg.setBackgroundResource(R.drawable.topbg_red);
					iconImageView.setBackgroundResource(R.drawable.sguanli_icon);
					lableTextView.setText(R.string.download_certificate_text);
					resultTextView.setText(R.string.success_downloadcer);

					mParamConfigDao = new ParamConfigDaoImpl(this);

					if (!"1".equals(mParamConfigDao.get("enabled"))) { // 状态未激活
						mParamConfigDao.save("enabled", "1"); // 置为激活成功
						mParamConfigDao.save("enabled2", "1"); // 确认开通成功
						mParamConfigDao.save("adminpwd", "E10ADC3949BA59ABBE56E057F20F883E"); // 重置主管操作员密码
					}
				} else if (CUR_MODE == ICCADOWN_MODE) { // IC卡公钥下载
					lablebg.setBackgroundResource(R.drawable.topbg_red);
					iconImageView.setBackgroundResource(R.drawable.sguanli_icon);
					lableTextView.setText("公钥更新");
					resultTextView.setText("公钥更新成功");
				} else if (CUR_MODE == ICPARAMDOWN_MODE) { // EMV交易参数下载
					lablebg.setBackgroundResource(R.drawable.topbg_red);
					iconImageView.setBackgroundResource(R.drawable.sguanli_icon);
					lableTextView.setText("参数更新");
					resultTextView.setText("参数更新成功");
				}
			} else { // 结算
				if ("002309".equals(transCode)) {
					// setContentView(R.layout.show_print_result);
					printData();
					setContentView(R.layout.show_result);
					resultTextView = (TextView) findViewById(R.id.result_text);
					result_massage = (TextView) findViewById(R.id.result_massage_text);

					if (CUR_MODE == SETTLE_MODE && "00".equals(rescode)) { // 结算，对账成功
						resultTextView.setText(R.string.settle_suc_result);
						result_massage.setText(R.string.settle_massage);
					} else if (CUR_MODE == SETTLE_MODE && "95".equals(rescode)) { // 结算，对账不平
						resultTextView.setText(R.string.settle_suc_nomatch_result);
						result_massage.setText(R.string.settle_massage);
					}
				}
				if ("002302".equals(transCode)) {// 助农取款
					printData();
					setContentView(R.layout.show_success_result);
					TextView trad_type = (TextView) findViewById(R.id.trad_type);
					trad_type.setText(treatype);

					TextView proof_number = (TextView) findViewById(R.id.proof_number);
					TextView card_no = (TextView) findViewById(R.id.card_no);
					TextView money_number = (TextView) findViewById(R.id.money_number);
					// TextView charge_money = (TextView)
					// findViewById(R.id.charge_money);
					TextView trade_time2 = (TextView) findViewById(R.id.trade_time);
					Button ok_button = (Button) findViewById(R.id.ok_button);
					try {
						// Button print_receipt = (Button)
						// findViewById(R.id.print_receipt);
						String batchbillno = mTransaction.getDataMap().get("batchbillno");
						batchbillno = new String(HexUtil.hexStringToByte(batchbillno));
						if(batchbillno.length() > 6){
							batchbillno = batchbillno.substring(batchbillno.length()-6, batchbillno.length());
						}
						proof_number.setText(batchbillno);
						card_no.setText(mTransaction.getDataMap().get("priaccount"));
						money_number.setText(Utility.unformatMount(mTransaction.getDataMap().get("transamount")));
						// charge_money.setText(mTransaction.getDataMap().get("charge"));
						// String strmoney =
						// Utility.unformatMount(mTransaction.getDataMap().get("transamount"));
						// String charge =
						// mTransaction.getDataMap().get("charge");
						// String tempMoney = strmoney;
						// try {
						// DecimalFormat df = new DecimalFormat("#0.00");
						// double f = Double.valueOf(strmoney);
						// if (charge != null && !"".equals(charge)) {
						// Double c = Double.valueOf(charge);
						// double a = f - c;
						// tempMoney = df.format(a);
						// System.out.println("a=" + a);
						// System.out.println("ma=" + df.format(a));
						// }
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
						money_number.setText(Utility.unformatMount(mTransaction.getDataMap().get("transamount")));
						String datetime = Utility.getTimeStamp();
						trade_time2.setText(datetime);
					} catch (Exception e) {
						e.printStackTrace();
					}
					inititle();
					ok_button.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent1 = new Intent(ShowResultActivity.this, MenuSpaceActivity.class);// 跳转到主菜单界面
							startActivity(intent1);
							finish();
						}
					});
					// // TODO 填充交易成功后的数据
					// print_receipt.setOnClickListener(new OnClickListener() {
					//
					// @Override
					// public void onClick(View v) {
					// try {
					// printData();
					// } catch (Exception e) {
					// logger.error("打印出现异常", e);
					// }
					// }
					// });
				}
				if ("002301".equals(transCode)) {// 余额查询
					setContentView(R.layout.show_success_query);
					TextView trad_type = (TextView) findViewById(R.id.trad_type);
					trad_type.setText(treatype);
					TextView card_no = (TextView) findViewById(R.id.card_no);
					TextView proof_number = (TextView) findViewById(R.id.proof_number);
					result_massage = (TextView) findViewById(R.id.money_balance);
					TextView trade_time2 = (TextView) findViewById(R.id.trade_time);
					card_no.setText(map.get("priaccount"));
					String batchbillno = mTransaction.getDataMap().get("batchbillno");
					batchbillno = new String(HexUtil.hexStringToByte(batchbillno));
					if(batchbillno.length() > 6){
						batchbillno = batchbillno.substring(batchbillno.length()-6, batchbillno.length());
					}
					proof_number.setText(batchbillno);
					// money_balance.setText(map.get("transamount"));
					String datetime = Utility.getTimeStamp();
					trade_time2.setText(datetime);

					Button ok_button = (Button) findViewById(R.id.ok_button);
					// Button print_receipt = (Button)
					// findViewById(R.id.print_receipt);

					ok_button.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent1 = new Intent(ShowResultActivity.this, MenuSpaceActivity.class);// 跳转到主菜单界面
							startActivity(intent1);
							finish();
						}
					});
					// // TODO 填充交易成功后的数据
					// print_receipt.setOnClickListener(new OnClickListener() {
					//
					// @Override
					// public void onClick(View v) {
					// try {
					// printData();
					// } catch (Exception e) {
					// logger.error("打印出现异常", e);
					// }
					// }
					// });

				}
				// Log.i("ckh", "idrespcode == " + map.get("idrespcode"));
				// resultTextView = (TextView) findViewById(R.id.result_text);
				// result_massage = (TextView)
				// findViewById(R.id.result_massage_text);
				//
				// if (CUR_MODE == SETTLE_MODE && "3030".equals(rescode)) { //
				// 结算，对账成功
				// resultTextView.setText(R.string.settle_suc_result);
				// result_massage.setText(R.string.settle_massage);
				// } else if (CUR_MODE == SETTLE_MODE && "3935".equals(rescode))
				// { // 结算，对账不平
				// resultTextView.setText(R.string.settle_suc_nomatch_result);
				// result_massage.setText(R.string.settle_massage);
				// } else { // 除结算以外需打印交易
				// resultTextView.setText(R.string.sale_suc_result);
				// // result_massage.setText(R.string.sale_massage);
				//
				// }

				// if (hasPrint) { // 需要打印
				// try {
				// printData();
				// } catch (Exception e) {
				// logger.error("打印出现异常", e);
				// }
				// }
			}
		} else {
			if ("002302".equals(transCode)) {// 助农消费
				setContentView(R.layout.show_fail_result);
				trade_time = (TextView) findViewById(R.id.trade_time);
				trad_type = (TextView) findViewById(R.id.trad_type);
				errorcode_tv = (TextView) findViewById(R.id.errorcode_tv);
				failmassage = (TextView) findViewById(R.id.fail_massage);
				confirmButton = (Button) findViewById(R.id.ok_button);
				inititle();
				confirmButton.setOnClickListener(mListener);

				List<ErrInfo> errInfos = Utility.getErrInfoFromFile(this);
				if ("".equals(rescode) || rescode == null) {
					rescode = map.get("Http_rescode"); // 如果返回码为空，去获取拉卡拉前置返回码
					errMsg = map.get("errMsg");
				} else {
					errMsg = map.get("errMsg");
				}
				errorcode_tv.setText(rescode);
				// 获取交易时间
				String trade_time_1 = Utility.getTimeStamp();
				trade_time.setText(trade_time_1);
				// String code = getString(R.string.fail_code_text)+rescode;

				trad_type.setText(treatype);
				if (errMsg == null || "".equals(errMsg)) {
					for (ErrInfo mErrInfo : errInfos) {
						if (rescode.equals(mErrInfo.getErrcode())) {
							errMsg = mErrInfo.getTip_info();
						}
					}
				}
				System.out.println("错误信息222222222:" + errMsg);
				failmassage.setText(errMsg);
			}else if ("002301".equals(transCode)) {// 余额查询
				setContentView(R.layout.show_fail_query);
				trade_time = (TextView) findViewById(R.id.trade_time);
				trad_type = (TextView) findViewById(R.id.trad_type);
				failmassage = (TextView) findViewById(R.id.fail_massage);
				confirmButton = (Button) findViewById(R.id.ok_button);
				inititle();
				confirmButton.setOnClickListener(mListener);

				List<ErrInfo> errInfos = Utility.getErrInfoFromFile(this);
				if ("".equals(rescode) || rescode == null) {
					rescode = map.get("Http_rescode"); // 如果返回码为空，去获取拉卡拉前置返回码
					errMsg = map.get("errMsg");
				}
				trade_time.setText(Utility.getTimeStamp());
				// String code = getString(R.string.fail_code_text)+rescode;
				trad_type.setText(treatype);
				if (errMsg == null || "".equals(errMsg)) {
					for (ErrInfo mErrInfo : errInfos) {
						if (rescode.equals(mErrInfo.getErrcode())) {
							errMsg = mErrInfo.getTip_info();
						}
					}
				}
				System.out.println("333333:" + errMsg);
				failmassage.setText(errMsg);
			}
			else {		
				setContentView(R.layout.show_fail_result);
				LinearLayout tip_type = (LinearLayout) findViewById(R.id.tip_type);
				LinearLayout tip_time = (LinearLayout) findViewById(R.id.tip_time);
				tip_type.setVisibility(View.GONE);
				tip_time.setVisibility(View.GONE);
				errorcode_tv = (TextView)findViewById(R.id.errorcode_tv);
				failmassage = (TextView)findViewById(R.id.fail_massage);
				confirmButton = (Button)findViewById(R.id.ok_button);
				confirmButton.setOnClickListener(mListener);
				List<ErrInfo> errInfos = Utility.getErrInfoFromFile(this);
				if ("".equals(rescode) || rescode == null) {
					rescode = map.get("Http_rescode");			//如果返回码为空，去获取拉卡拉前置返回码
					errMsg = map.get("errMsg");
				}
				String code = getString(R.string.fail_code_text)+rescode;
				errorcode_tv.setText(code);
				if (errMsg == null || "".equals(errMsg)){
					for (ErrInfo mErrInfo : errInfos) {
						if (rescode.equals(mErrInfo.getErrcode())) {
							errMsg = mErrInfo.getTip_info();
						}
					}
				}
				failmassage.setText(errMsg);	
			}
		}

		// gobackButton = (Button) findViewById(R.id.back);
		// gobackButton.setVisibility(View.GONE);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i("ckh", "ShowResultActivity onStart()....");
		if (isSuccess) {
			if (CUR_MODE == INQU_MODE) { // 余额查询成功，（不需要打印，打开外置键盘显示余额）

				if ("0".equals(pinpadType)) {
					try {
						pinPadDev = new ExPinPadDevJsIfc(this, expinpadhandler);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.error("", e);
					}
				} else {
					try {
						pinPadDev = new PinPadDevJsIfc(this, expinpadhandler);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.error("", e);
					}
				}

				pinPadDev.openDev();
				String showDataLineTwo = null;
				String amount = new String(HexUtil.hexStringToByte(map.get("balanceamount")));
				String mount_symbol = String.valueOf(amount.charAt(13));
				String balanceamount = amount.substring(14);

				if ("C".equals(mount_symbol) || "000000000000".equals(balanceamount)) {
					showDataLineTwo = "RMB " + Utility.unformatMount(balanceamount);
				} else {
					showDataLineTwo = "RMB -" + Utility.unformatMount(balanceamount);
				}
				pinPadDev.display("BALANCE:", showDataLineTwo, new GetPinBack() {

					@Override
					public void onGetPin(String pin) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onEnter() {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						msg.what = 0x00;
						expinpadhandler.sendMessage(msg);
					}
				});
			}
		}
	}

	private void printData() {
		// Intent intent2 = new Intent(ShowResultActivity.this,
		// ShowPrintResult.class);
		// startActivity(intent2);
		if ("000001".equals(transCode) || "000002".equals(transCode)) {
			printMap = mTransaction.getResultMap();
		} else {
			printMap = printMap(mTransaction.getDataMap(), map);
		}
		/*
		 * if (CUR_MODE==SETTLE_MODE) { Utility.setPrintStatus(this, "settle");
		 * //标记有结算打印状态 } else { Utility.setPrintStatus(this, "strans");
		 * //标记有交易打印状态 }
		 */
		try {
			mPrintDev = new PrintDev();
			mPrintDev.openDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("打开打印机异常", e);
		}
		if (CUR_MODE == SETTLE_MODE) {

			mPrintDev.printData(printMap, new CallBack() {

				@Override
				public void isPrintSecond() {
					// TODO Auto-generated method stub
					mPrintDev.close();
					if (!isClientPrint) { // 打印到细明时回调
						Utility.setPrintStatus(ShowResultActivity.this, ""); // 打印成功结算总结单，打印状态恢复
						gotoPrintAgianDialog();
					} else {
						overTransaction();
					}
				}

				@Override
				public void printExcept(int code, Bundle b) {
					// TODO Auto-generated method stub
					mPrintDev.close();
					if (code == 0x30) {
						creatExceptDialog(ShowResultActivity.this, b);
					} else {
						if (code == 0x32) {
							DialogFactory.showTips(ShowResultActivity.this,
									b.getString("exceptiontip", "报文解析异常，请联系客服！"));
							Utility.setPrintStatus(ShowResultActivity.this, ""); // 打印机异常关掉打印状态
						} else if (code == 0x31) {
							DialogFactory.showTips(ShowResultActivity.this, "打印机异常请稍候再试！");
						}
						overTransaction();
					}
				}
			});

		} else {
			mPrintDev.printData(printMap, new CallBack() {

				@Override
				public void isPrintSecond() {
					// TODO Auto-generated method stub
					mPrintDev.close();
					if (!isClientPrint) { // 第一联打印结束时，回调该方法
						Utility.setPrintStatus(ShowResultActivity.this, ""); // 打印成功商户联，打印状态恢复
						gotoPrintAgianDialog();
					} else {
						if (isAutoUp) { // 自动发起脱机消费上送
							autoOfflineSaleUp();
						} else {
							overTransaction();
						}
					}
				}

				@Override
				public void printExcept(int code, Bundle b) {
					// TODO Auto-generated method stub
					mPrintDev.close();
					if (code == 0x30) {
						b.putBoolean("isAutoUp", isAutoUp);
						creatExceptDialog(ShowResultActivity.this, b);
					} else {
						if (code == 0x32) {
							DialogFactory.showTips(ShowResultActivity.this,
									b.getString("exceptiontip", "报文解析异常，请联系客服！"));
							Utility.setPrintStatus(ShowResultActivity.this, ""); // 打印机异常关掉打印状态
						} else if (code == 0x31) {
							DialogFactory.showTips(ShowResultActivity.this, "打印机异常请稍候再试！");
						}
						if (isAutoUp) { // 自动发起脱机消费上送
							autoOfflineSaleUp();
						} else {
							overTransaction();
						}
					}
				}
			});
		}
	}

	private Handler expinpadhandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0xe1:
			case 0xe2:
				DialogFactory.showTips(ShowResultActivity.this, msg.getData().getString("exceptiontip"));
				overTransaction();
				break;
			case 0x00:
			case 0xe3:
				overTransaction();
				break;

			case 0x02: // 显示余额
				Bundle bundle = msg.getData();
				String showDataLineOne = bundle.getString("ShowDataLineOne");
				String showDataLineTwo = bundle.getString("ShowDataLineTwo");
				if ("".equals(showDataLineTwo) || showDataLineTwo == null) {
					showDataLineTwo = "余额显示异常";
				}
				if ("".equals(showDataLineOne) || showDataLineOne == null) {
					showDataLineOne = "查询成功";
				}
				// resultTextView.setText("账户余额：");
				result_massage.setText(showDataLineTwo);
				result_massage.setTextSize(42);
				break;
			default:
				break;
			}
		}

	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("ckh", "ShowResultActivity onActivityResult()......");
		if (requestCode == 0x01) {
			if (resultCode == 0x03) {
				try {
					mPrintDev = new PrintDev();
					mPrintDev.openDev();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("打开设备失败", e);
					e.printStackTrace();
				}
				Utility.setPrintStatus(this, "settle"); // 标记有结算打印状态

				printMap.put("printDetails", "true"); // 打印交易明细
				mPrintDev.printData(printMap, new CallBack() {
					@Override
					public void isPrintSecond() {
						// TODO Auto-generated method stub
						mPrintDev.close();
						Utility.setPrintStatus(ShowResultActivity.this, "");
						overTransaction();
					}

					@Override
					public void printExcept(int code, Bundle b) {
						// TODO Auto-generated method stub
						mPrintDev.close();
						if (code == 0x30) {
							creatExceptDialog(ShowResultActivity.this, b);
						} else {
							DialogFactory.showTips(ShowResultActivity.this, "打印机异常请稍候再试！");
							overTransaction();
						}
					}
				});
			} else {
				overTransaction();
			}
		} else if (requestCode == 0x00) {
			if (resultCode == 0x02) {
				try {
					mPrintDev = new PrintDev();
					mPrintDev.openDev();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("打开打印机异常", e);
				}
				Utility.setPrintStatus(this, "strans"); // 标记有交易打印状态
				printMap.put("isSecond", "true"); // 设置打印第二联
				mPrintDev.printData(printMap, new CallBack() {
					@Override
					public void isPrintSecond() {
						// TODO Auto-generated method stub
						mPrintDev.close();
						Utility.setPrintStatus(ShowResultActivity.this, "");
						if (isAutoUp) { // 自动发起脱机消费上送
							autoOfflineSaleUp();
						} else {
							overTransaction();
						}
					}

					@Override
					public void printExcept(int code, Bundle b) {
						// TODO Auto-generated method stub
						mPrintDev.close();
						if (code == 0x30) {
							b.putBoolean("isAutoUp", isAutoUp);
							creatExceptDialog(ShowResultActivity.this, b);
						} else {
							DialogFactory.showTips(ShowResultActivity.this, "打印机异常请稍候再试！");
							if (isAutoUp) { // 自动发起脱机消费上送
								autoOfflineSaleUp();
							} else {
								overTransaction();
							}
						}
					}
				});
			} else {
				if (isAutoUp) { // 自动发起脱机消费上送
					autoOfflineSaleUp();
				} else {
					overTransaction();
				}
			}
		} else if (requestCode == 0x02) {
			DialogFactory.showTips(ShowResultActivity.this, "自动上送累计笔数完成");
			overTransaction();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.i("ckh", "ShowResultActivity onStop()....");
		if (isSuccess) {
			if (CUR_MODE == INQU_MODE) {
				try {
					pinPadDev.closeDev();
					pinPadDev = null;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("关闭打印机异常", e);
				}
			}
		}
		super.onStop();
	}

	private View.OnClickListener mListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// add by xrh @2013.10.14
			ParamConfigDao dao = new ParamConfigDaoImpl(ShowResultActivity.this);
			if (!"1".equals(dao.get("enabled"))) { // 未激活状态
				// gotoOneshotWelcome();
				int count = lklcposActivityManager.activityCount(SettingMainActivity.class);
				if (count == 0) {
					Intent intent = new Intent();
					intent.setClass(ShowResultActivity.this, SettingMainActivity.class);
					startActivity(intent);
					addActivityAnim();
				}
				lklcposActivityManager.removeAllActivityExceptOne(SettingMainActivity.class);
				outActivityAnim();
				return;
			}
			overTransaction();
		}
	};

	// 自动脱机交易上送
	private void autoOfflineSaleUp() {
		Log.i("ckh", "开始自动上送脱机交易");
		startActivityForResult(Utility.autoUpOfflineSale(), 0x02); // 自动发起脱机上送
		addActivityAnim();
	}

	// 交易结束，返回到交易入口菜单
	private void overTransaction() {
		if ("D".equals(mTransaction.getProperties())) { // 第三方调用返回交易结果
			callByThirdSaleOver();
			return;
		} else if ("settings".equals(mTransaction.getProperties())) { // 返回设置主菜单
			lklcposActivityManager.removeActivityTo(SettingMainActivity.class);
		} else if ("other".equals(mTransaction.getProperties())) { // 返回其他
			lklcposActivityManager.removeActivityTo(OtherActivity.class);
		} else if ("transfer".equals(mTransaction.getProperties())) { // 返回圈存菜单
			lklcposActivityManager.removeActivityTo(TransferMenuActivity.class);
		} else { // 返回主菜单
			int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(this, MenuSpaceActivity.class);
				startActivity(intent);
			}
			lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
		}
		outActivityAnim();
	}

	private void callByThirdSaleOver() {
		Bundle retBundle = new Bundle();
		retBundle.putString("msg_tp", "02100");
		retBundle.putString("proc_cd", "000000");

		if (!"00".equals(rescode)) { // 调用失败
			retBundle.putString("reason", errMsg);
			retBundle.putBoolean("isSuc", false);
		} else {
			retBundle.putBoolean("isSuc", true);
		}
		Intent retIntent = new Intent();
		retIntent.putExtras(retBundle);
		WebViewActivity.installActivity.setIntent(retIntent);
		lklcposActivityManager.removeAllActivityExceptOne(WebViewActivity.class);
	}

	private void gotoPrintAgianDialog() {
		System.out.println("进入到第二联的打印..");
		isClientPrint = true;
		Intent mIntent = new Intent();
		mIntent.setClass(ShowResultActivity.this, PrintAgianDialog.class);
		mIntent.putExtra("code", printMap.get("transprocode")); // 交易码
		if (CUR_MODE == SETTLE_MODE) {
			startActivityForResult(mIntent, 0x01);
		} else {
			startActivityForResult(mIntent, 0x00);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private Map<String, String> printMap(Map<String, String> dataMap, Map<String, String> resultMap) {
		Map<String, String> desMap = new HashMap<String, String>();
		desMap.put("reserve1", resultMap.get("msg_tp"));
		desMap.put("priaccount",
				resultMap.get("priaccount") != null ? resultMap.get("priaccount") : dataMap.get("priaccount"));
		desMap.put("transprocode",
				resultMap.get("transprocode") != null ? resultMap.get("transprocode") : dataMap.get("transprocode"));
		desMap.put("conditionmode",
				resultMap.get("conditionmode") != null ? resultMap.get("conditionmode") : dataMap.get("conditionmode"));
		desMap.put("acceptoridcode", resultMap.get("acceptoridcode") != null ? resultMap.get("acceptoridcode")
				: dataMap.get("acceptoridcode"));
		desMap.put("respcode", resultMap.get("respcode"));

		String terminalid = resultMap.get("terminalid") != null ? resultMap.get("terminalid")
				: dataMap.get("terminalid");
		desMap.put("terminalid", terminalid == null ? null : new String(HexUtil.hexStringToByte(terminalid)));

		String loadparams = resultMap.get("loadparams") != null ? resultMap.get("loadparams")
				: dataMap.get("loadparams");
		desMap.put("loadparams", loadparams == null ? null : new String(HexUtil.hexStringToByte(loadparams)));
		desMap.put("expireddate",
				resultMap.get("expireddate") != null ? resultMap.get("expireddate") : dataMap.get("expireddate"));

		String batchbillno = resultMap.get("batchbillno") != null ? resultMap.get("batchbillno")
				: dataMap.get("batchbillno");
		desMap.put("batchbillno", batchbillno == null ? null : new String(HexUtil.hexStringToByte(batchbillno)));

		String idrespcode = resultMap.get("idrespcode") != null ? resultMap.get("idrespcode")
				: dataMap.get("idrespcode");
		desMap.put("idrespcode", idrespcode == null ? null : new String(HexUtil.hexStringToByte(idrespcode)));

		String refernumber = resultMap.get("refernumber") != null ? resultMap.get("refernumber")
				: dataMap.get("refernumber");
		desMap.put("refernumber", refernumber == null ? null : new String(HexUtil.hexStringToByte(refernumber)));
		desMap.put("translocaldate", resultMap.get("translocaldate") != null ? resultMap.get("translocaldate")
				: dataMap.get("translocaldate"));
		desMap.put("translocaltime", resultMap.get("translocaltime") != null ? resultMap.get("translocaltime")
				: dataMap.get("translocaltime"));
		desMap.put("transamount",
				resultMap.get("transamount") != null ? resultMap.get("transamount") : dataMap.get("transamount"));
		String adddataword = resultMap.get("adddataword") != null ? resultMap.get("adddataword")
				: dataMap.get("adddataword");

		try {
			desMap.put("adddataword",
					adddataword == null ? null : new String(HexUtil.hexStringToByte(adddataword), "gbk"));
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
		}
		desMap.put("requestSettleData", resultMap.get("requestSettleData")); // 本地结算信息
		String respcode = resultMap.get("respcode") != null ? resultMap.get("respcode") : dataMap.get("respcode");
		desMap.put("respcode", respcode == null ? null : new String(HexUtil.hexStringToByte(respcode)));
		String settledata = resultMap.get("settledata");
		try {
			desMap.put("settledata",
					settledata == null ? null : new String(HexUtil.hexStringToByte(settledata), "gbk"));
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
			e.printStackTrace();
		} // 前置返回结算信息

		desMap.put("entrymode", dataMap.get("entrymode"));
		desMap.put("reserve4", dataMap.get("reserve4"));
		return desMap;
	}

}
