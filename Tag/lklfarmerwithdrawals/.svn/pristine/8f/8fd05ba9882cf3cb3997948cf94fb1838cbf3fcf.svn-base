package com.centerm.lklcpos.deviceinterface;

/*
 * 凭条格式化及打印
 */
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.dao.SettleDataDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.entity.SettleData;
import com.centerm.comm.persistence.entity.TransRecord;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.SettleDataDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.LklcposApplication;
import com.centerm.lklcpos.print.entity.PrintDataObject;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.TlvUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.exception.CentermApiException.IndicationException;
import com.centerm.mid.exception.CentermApiException.PrinterException;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.PrinterDevInf;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.StaticLayout;
import android.util.Log;

public class PrintDev {
	private static Logger logger = Logger.getLogger(PrintDev.class);

	private PrinterDevInf printdev;
	private Handler printHandler;
	private boolean isopen = false;
	private boolean isPrintDetails = false;

	public PrintDev() throws Exception {
		init();
	}

	public void init() throws Exception {
		this.printdev = DeviceFactory.getPrinterDev();
	}

	public void openDev() {
		try {
			this.printdev.open();
			isopen = true;
			Log.i("ckh", "设备开启，isopen == " + isopen);
			logger.debug("打印机设备打开成功...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("打开设备失败", e);
		}
	}

	public void close() {
		try {
			this.printdev.close();
			isopen = false;
			Log.i("ckh", "设备关闭，isopen == " + isopen);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("关闭设备异常", e);
		}
	}

	/*
	 * HashMap数据转化打印数据对象
	 * 
	 * 打印数据前，根据情况需配置的项： HashMap中 key -- “isReprints”，值“true”时，为重打印，否则默认非重打印（重打印）
	 * key -- "isSecond", 值“true”时，打印第二联，否则默认打印第一联（pos签购） key --
	 * "requestSettleData"，对应Value为结算时终端结算信息（结算信息） key -- "printDetails",
	 * 值“true”时，结算时，加打细明，值“false”时，加打3行空行
	 */
	private static List<PrintDataObject> getPrintDataObjects(Map<String, String> dataMap) {
		List<PrintDataObject> mList = new ArrayList<PrintDataObject>();
		String data = null;
		final int SALE = 1;
		final int VOID = 2;
		final int REFUND = 3;
		final int AUTH = 4;
		final int AUTH_COMP = 5;
		final int AUTH_VOID = 6;
		final int AUTH_COMP_VOID = 7;
		final int SETTLE = 8;
		final int CASH_UP_MODE = 9; // 现金充值
		final int TRANSFER_MODE = 10; // 指定账户圈存
		final int NON_TRANSFER_MODE = 11; // 非指定账户圈存
		final int CASH_UP_VOID_MODE = 12; // 现金充值撤销
		final int OFFLINE_REFUND_MODE = 13;
		final int OFFLINE_SALE_MODE = 14; // 电子现金脱机消费
		int CUR_MODE = 0;

		boolean isPosSalesSlip = false; // 判断是签购单还是结算单
		boolean isSale = false; // 标记是消费还是消费撤销
		String isPrintDetails = null; // 标记是否打印细明
		// if(!"true".equals(dataMap.get("printDetails")))
		isPrintDetails = dataMap.get("printDetails");

		String entrymode = dataMap.get("entrymode") == null ? "02" : dataMap.get("entrymode"); // pos输入方式(默认02)
		String transprocode = dataMap.get("transprocode");
		String msg_tp = dataMap.get("reserve1"); // 获取交易下发的消息类型
		String conditionmode = dataMap.get("conditionmode");
		if ("900000".equals(transprocode)) {
			CUR_MODE = SETTLE;
		} else if ("010000".equals(transprocode) && "00".equals(conditionmode) && "0210".equals(msg_tp)) {
			// 改成助农的处理代码
			CUR_MODE = SALE;
		} else if ("000000".equals(transprocode) && "06".equals(conditionmode)) {
			CUR_MODE = AUTH_COMP;
		} else if ("030000".equals(transprocode)) {
			CUR_MODE = AUTH;
		} else if ("200000".equals(transprocode) && "00".equals(conditionmode) && "0230".equals(msg_tp)) {
			CUR_MODE = REFUND;
		} else if ("200000".equals(transprocode) && "00".equals(conditionmode) && "0210".equals(msg_tp)) {
			CUR_MODE = VOID;
		} else if ("200000".equals(transprocode) && "06".equals(conditionmode) && "0210".equals(msg_tp)) {
			CUR_MODE = AUTH_COMP_VOID;
		} else if ("200000".equals(transprocode) && "06".equals(conditionmode) && "0110".equals(msg_tp)) {
			CUR_MODE = AUTH_VOID;
		} else if ("600000".equals(transprocode) && "0210".equals(msg_tp)) {
			CUR_MODE = TRANSFER_MODE;
		} else if ("620000".equals(transprocode) && "0210".equals(msg_tp)) {
			CUR_MODE = NON_TRANSFER_MODE;
		} else if ("630000".equals(transprocode) && "0210".equals(msg_tp)) {
			CUR_MODE = CASH_UP_MODE;
		} else if ("170000".equals(transprocode) && "0210".equals(msg_tp)) {
			CUR_MODE = CASH_UP_VOID_MODE;
		} else if ("000000".equals(transprocode) && "01".equals(conditionmode) && "0330".equals(msg_tp)) {
			CUR_MODE = OFFLINE_REFUND_MODE;
		} else if ("000000".equals(transprocode) && "00".equals(conditionmode) && "0330".equals(msg_tp)) {
			CUR_MODE = OFFLINE_SALE_MODE;
		}

		String exVersion = Utility.getVersion(LklcposApplication.lklcposAppContext);

		if (CUR_MODE != 0 && CUR_MODE != 8) { // 只在pos签购时打印
			mList.add(new PrintDataObject("", "助农取款凭条 ", "24", "bold", "true", ""));
		} else if (CUR_MODE == SETTLE) {
			if (isPrintDetails == null) // 打印明细的时候不打印
				mList.add(new PrintDataObject("", "结算总计单", "24", "bold", "true", ""));
		}
		if (isPrintDetails == null) { // 打印明细的时候不打印
			// 43域，商户名称
			ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(LklcposApplication.lklcposAppContext);
			mList.add(new PrintDataObject("商户名(MERCHANT NAME)", ""));
			mList.add(new PrintDataObject("", "  " + mParamConfigDao.get("mchntname"), "12"));

			// 42域，商户编号
			mList.add(new PrintDataObject("商户编号(MERCHANT NO)", ""));
			mList.add(new PrintDataObject("", "  " + mParamConfigDao.get("merid")));

			// 41域，终端号
			mList.add(new PrintDataObject("终端号(TERMIANL)", ""));
			mList.add(new PrintDataObject("", "  " + mParamConfigDao.get("termid")));
		}

		if (CUR_MODE != 0 && CUR_MODE != 8) { // 只在pos签购时打印
			mList.add(new PrintDataObject("操作员号(OPERATOR NO)", dataMap.get("loadparams").substring(0, 2)));
			mList.add(new PrintDataObject("收单机构", "拉卡拉"));
			mList.add(new PrintDataObject("卡号(CARD NO)", ""));

			String posInputMode = dataMap.get("posInputMode");
			if (posInputMode == null || "".equals(posInputMode)) {
				if (entrymode.startsWith("01")) { // 手输
					posInputMode = "   /M";
				} else if (entrymode.startsWith("02")) { // 刷卡
					posInputMode = "   /S";
				} else if (entrymode.startsWith("80")) { // Fallback
					posInputMode = "   /F";
				} else if (entrymode.startsWith("05")) { // 芯片卡
					posInputMode = "   /I";
				} else if (entrymode.startsWith("07") || entrymode.startsWith("9")) { // 非接触式
					posInputMode = "   /C";
				}
			}

			if (CUR_MODE == AUTH || CUR_MODE == OFFLINE_SALE_MODE) {
				mList.add(new PrintDataObject("", "  " + dataMap.get("priaccount") + posInputMode));
			} else {
				mList.add(
						new PrintDataObject("", "  " + Utility.formatCardno(dataMap.get("priaccount")) + posInputMode));
			}
			if (CUR_MODE != OFFLINE_SALE_MODE && CUR_MODE != OFFLINE_REFUND_MODE) {
				if (dataMap.get("settledata") != null) {
					mList.add(new PrintDataObject("卡类型(CARD TYPE)", dataMap.get("settledata").substring(2)));
				} else {
					mList.add(new PrintDataObject("卡类型(CARD TYPE)", ""));
				}
				System.out.println("settle=" + dataMap.get("settledata"));

			}
			String worktime = dataMap.get("expireddate");
			if ("null".equals(worktime)) {
				worktime = "";
			}
			if (!"".equals(worktime) && worktime != null) {
				if (worktime.length() >= 4) {
					worktime = worktime.substring(0, 2) + "/" + worktime.substring(2, 4);
				}
			}
			mList.add(new PrintDataObject("有效期(EXP DATE)", worktime));
			mList.add(new PrintDataObject("交易类型(TRANS TYPE)", ""));
			if (CUR_MODE == SALE) {
				// 2015年9月6日 (周日) 18:01 拉卡拉测试人员贾丽邮件需求提出
				// 2.凡是联机交易签购单交易类型需要显示为“消费”；脱机显示为“快速支付”。此处更改为助农
				mList.add(new PrintDataObject("", "助农取款 CASH WITHDRAWAL", "24"));
				// if (entrymode.startsWith("07") || entrymode.startsWith("9"))
				// {
				// mList.add(new PrintDataObject("", "快速消费(qPBOC)", "24"));
				// } else {
				// mList.add(new PrintDataObject("", "消费(SALE)", "24"));
				// }
			} else if (CUR_MODE == VOID) {
				mList.add(new PrintDataObject("", "消费撤销(VOID)", "24"));
			} else if (CUR_MODE == REFUND) {
				mList.add(new PrintDataObject("", "退货(REFUND)", "24"));
			} else if (CUR_MODE == AUTH) {
				mList.add(new PrintDataObject("", "预授权(AUTH)", "24"));
			} else if (CUR_MODE == AUTH_COMP) {
				mList.add(new PrintDataObject("", "预授权完成(AUTH COMPLETE)", "24"));
			} else if (CUR_MODE == AUTH_VOID) {
				mList.add(new PrintDataObject("", "预授权撤销(AUTH VOID)", "24"));
			} else if (CUR_MODE == AUTH_COMP_VOID) {
				mList.add(new PrintDataObject("", "预授权完成撤销(COMPLETE VOID)", "24"));
			} else if (CUR_MODE == TRANSFER_MODE) {
				mList.add(new PrintDataObject("", "电子现金指定账户圈存(EC LOAD)", "24"));
			} else if (CUR_MODE == NON_TRANSFER_MODE) {
				mList.add(new PrintDataObject("", "电子现金非指定账户圈存(EC LOAD)", "24"));
			} else if (CUR_MODE == CASH_UP_MODE) {
				mList.add(new PrintDataObject("", "电子现金现金充值(EC LOAD)", "24"));
			} else if (CUR_MODE == CASH_UP_VOID_MODE) {
				mList.add(new PrintDataObject("", "电子现金充值撤销(EC LOAD VOID)", "24"));
			} else if (CUR_MODE == OFFLINE_REFUND_MODE) {
				mList.add(new PrintDataObject("", "电子现金退货(EC LOAD)", "24"));
			} else if (CUR_MODE == OFFLINE_SALE_MODE) {
				if (entrymode.startsWith("05")) { // 电子现金消费（读卡）
					mList.add(new PrintDataObject("", "电子现金消费(EC LOAD)", "24"));
				} else { // 非接电子现金消费
					mList.add(new PrintDataObject("", "快速消费(EC LOAD)", "24"));
				}
			}
		}

		if (isPrintDetails == null) { // 打印明细的时候不打印
			data = dataMap.get("batchbillno");
			mList.add(new PrintDataObject("批次号(BATCH NO)", data.substring(0, 6)));
		}

		if (CUR_MODE != 0 && CUR_MODE != 8) { // 只在pos签购时打印
			data = dataMap.get("batchbillno");
			if (data.length() < 12) {
				data = Utility.addZeroForNum(data, 12);
			}
			mList.add(new PrintDataObject("凭证号(VOUCHER NO)", data.substring(6, 12)));
			mList.add(new PrintDataObject("授权码(AUTH NO)", dataMap.get("idrespcode")));
			mList.add(new PrintDataObject("检索参考号(REFER NO)", ""));
			mList.add(new PrintDataObject("", "  " + dataMap.get("refernumber"), "12"));
			mList.add(new PrintDataObject("", "终端程序版本号", "12"));
			mList.add(new PrintDataObject("", exVersion, "12"));
		}
		if (isPrintDetails == null) { // 打印明细的时候不打印
			mList.add(new PrintDataObject("日期/时间(DATE/TIME)", ""));
			String datetime = dataMap.get("translocaldate") + dataMap.get("translocaltime");
			datetime = Utility.printFormatDateTime(datetime);
			mList.add(new PrintDataObject("", "          " + datetime));
			if (CUR_MODE == SETTLE) {
				if ("95".equals(dataMap.get("respcode"))) {
					mList.add(new PrintDataObject("", "内卡对账不平"));
				} else if ("00".equals(dataMap.get("respcode"))) {
					mList.add(new PrintDataObject("", "内卡对账平"));
				}
			}
		}
		if (CUR_MODE != 0 && CUR_MODE != 8) {
//			String charge = dataMap.get("charge");
//			if (charge != null && !"".equals(charge)) {
//				mList.add(new PrintDataObject("手续费", ""));
//				mList.add(new PrintDataObject("", " RMB" + dataMap.get("charge")));
//			}
			mList.add(new PrintDataObject("交易金额(AMOUNT)", ""));
			mList.add(new PrintDataObject("", "RMB", "48", "bold", "", "nobreak"));
			data = Utility.unformatMount(dataMap.get("transamount"));
//			try {
//				if (null != charge && !"".equals(charge)) {
//					DecimalFormat df = new DecimalFormat("#0.00");
//					double sum = Double.valueOf(data);
//					double charge1 = Double.valueOf(charge);
//					double amout1 = sum - charge1;
//					data = Utility.unformatMount(df.format(amout1));
//				} else {
//					data = Utility.unformatMount(dataMap.get("transamount"));
//				}
//			} catch (Exception e) {
//				data = Utility.unformatMount(dataMap.get("transamount"));
//			}
			if (CUR_MODE == SALE || CUR_MODE == AUTH || CUR_MODE == AUTH_COMP || CUR_MODE == TRANSFER_MODE
					|| CUR_MODE == NON_TRANSFER_MODE || CUR_MODE == CASH_UP_MODE || CUR_MODE == OFFLINE_SALE_MODE) {
				mList.add(new PrintDataObject("", " " + data, "48", "bold", "", "nobreak"));
			} else if (CUR_MODE == VOID || CUR_MODE == AUTH_VOID || CUR_MODE == AUTH_COMP_VOID || CUR_MODE == REFUND
					|| CUR_MODE == CASH_UP_VOID_MODE || CUR_MODE == OFFLINE_REFUND_MODE) {
				mList.add(new PrintDataObject("", " -" + data, "48", "bold", "", "nobreak"));
			}
			mList.add(new PrintDataObject("", "元", "24", "bold", "", ""));
			if (CUR_MODE == AUTH_VOID || CUR_MODE == AUTH_COMP_VOID) {
				mList.add(new PrintDataObject("原授权号(AUTH NO)", dataMap.get("idrespcode")));
			}
			if (CUR_MODE == VOID || CUR_MODE == AUTH_COMP_VOID || CUR_MODE == OFFLINE_REFUND_MODE) {
				mList.add(new PrintDataObject("原凭证号(VOUCHER NO)", dataMap.get("batchbillno").substring(12, 18)));
			}

			mList.add(new PrintDataObject("", "备注", "", "", "", "nobreak"));
			mList.add(new PrintDataObject("(REFERENCE)", ""));
			if (CUR_MODE == NON_TRANSFER_MODE) {
				mList.add(new PrintDataObject("转入卡卡号", dataMap.get("adddataword").substring(4)));
			}
			String reserve4 = dataMap.get("reserve4");
			Log.d("printDev", "print data reserve4 = " + reserve4);
			logger.debug("print data reserve4 = " + reserve4);

			if (reserve4 != null && !"null".equals(reserve4)) {
				Map<String, String> map = TlvUtil.tlvToMap(reserve4);
				mList.add(new PrintDataObject("AID", map.get("4F")));
				if (CUR_MODE == OFFLINE_SALE_MODE) {
					mList.add(new PrintDataObject("TC", map.get("9F26")));
				} else {
					mList.add(new PrintDataObject("ARQC", map.get("9F99"))); // 构造tag
																				// 9F99，将arqc存放于reserve4字段中
				}
				mList.add(new PrintDataObject("TVR", map.get("95")));
				mList.add(new PrintDataObject("TSI", map.get("9B")));
				if (CUR_MODE == OFFLINE_SALE_MODE) {
					mList.add(new PrintDataObject("CSN", map.get("5F34")));
				}
				mList.add(new PrintDataObject("ATC", map.get("9F36")));
				if (CUR_MODE == OFFLINE_SALE_MODE) {
					mList.add(new PrintDataObject("UNPR NUM", map.get("9F37")));
					mList.add(new PrintDataObject("AIP", map.get("82")));
					mList.add(new PrintDataObject("TEMP CAP", map.get("9F33")));
					mList.add(new PrintDataObject("IAD", map.get("9F10")));
				}
				mList.add(new PrintDataObject("Appl Label", "", "", "", "", "nobreak"));
				try {
					String tag_50 = map.get("50");
					if (tag_50 != null && !"".equals(tag_50)) {
						mList.add(new PrintDataObject("",
								" " + new String(HexUtil.hexStringToByte(map.get("50")), "gbk")));
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("", e);
				}
				mList.add(new PrintDataObject("Appl Name", "", "", "", "", "nobreak"));
				try {
					String tag_9f12 = map.get("9F12");
					if (tag_9f12 != null && !"".equals(tag_9f12)) {
						mList.add(new PrintDataObject("", " " + new String(HexUtil.hexStringToByte(tag_9f12), "gbk")));
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("", e);
				}
				if (CUR_MODE == OFFLINE_SALE_MODE || CUR_MODE == CASH_UP_MODE) {
					mList.add(new PrintDataObject("卡片余额",
							Utility.unformatMount(map.get("9F79") == null ? map.get("9F5D") : map.get("9F79"))));
				}
			}
			if (CUR_MODE != OFFLINE_SALE_MODE && CUR_MODE != NON_TRANSFER_MODE) {
				mList.add(new PrintDataObject("", dataMap.get("adddataword")));
			}
			if ("true".equals(dataMap.get("isReprints"))) {
				mList.add(new PrintDataObject("", "        ***重打印票据***", "24", "bold", "", ""));
			}

			if (dataMap.get("isSecond") == null) {
				mList.add(new PrintDataObject("", "持卡人签名(CARDHOLDER SIGNATURE)"));
				mList.add(new PrintDataObject("", "\n\n\n本人已签字确认,视同已当面点清现金,验证钱币真伪"));
				mList.add(new PrintDataObject("", "--------------------------------"));
				// mList.add(new PrintDataObject("", exVersion));
				mList.add(new PrintDataObject("", "客服热线:400-766-6666"));
				mList.add(new PrintDataObject("", "-----------商户存根-------------"));
			} else if ("true".equals(dataMap.get("isSecond"))) {
				mList.add(new PrintDataObject("", "--------------------------------"));
				// mList.add(new PrintDataObject("", exVersion));
				mList.add(new PrintDataObject("", "客服热线:400-766-6666"));
				mList.add(new PrintDataObject("", "----------持卡人存根------------"));
			}
		} else {
			if (isPrintDetails == null) {
				mList.add(new PrintDataObject("", " "));
				mList.add(new PrintDataObject("", "====终端结算总计（SUM TOTAL）==="));
				mList.add(new PrintDataObject("", "--------------内卡--------------"));
				mList.add(new PrintDataObject("", " 交易类型     总笔数     总金额  "));
				String requestSettleData = dataMap.get("requestSettleData");
				if (requestSettleData != null && requestSettleData != "") {
					mList.add(new PrintDataObject("",
							" 助农取款" + Utility.printFillSpace(Utility.printInteger(requestSettleData.substring(0, 3)),
									10)
							+ Utility.printFillSpace(Utility.unformatMount(requestSettleData.substring(3, 15)), 10)));
					//
					// mList.add(new PrintDataObject("", " 退货
					// "+Utility.printFillSpace(Utility.printInteger(requestSettleData.substring(15,18)),10)
					// +"
					// "+Utility.printFillSpace(Utility.unformatMount(requestSettleData.substring(18,30)),10)));
					//
					// mList.add(new PrintDataObject("", " 转账
					// "+Utility.printFillSpace(Utility.printInteger(requestSettleData.substring(75,78)),10)
					// +"
					// "+Utility.printFillSpace(Utility.unformatMount(requestSettleData.substring(78,90)),10)));
					//
					// mList.add(new PrintDataObject("", " 预授权完成
					// "+Utility.printFillSpace(Utility.printInteger(requestSettleData.substring(105,108)),4)
					// +"
					// "+Utility.printFillSpace(Utility.unformatMount(requestSettleData.substring(108,120)),10)));
				}
				// 对账不平
				if ("95".equals(dataMap.get("respcode")) && !"true".equals(dataMap.get("isReprints"))) {
					mList.add(new PrintDataObject("", " "));
					mList.add(new PrintDataObject("", "====主机结算总计（SUM TOTAL）==="));
					mList.add(new PrintDataObject("", " 交易类型     总笔数     总金额  "));
					String settleData = dataMap.get("settledata");
					if (settleData != null && settleData != "") {
						mList.add(new PrintDataObject("",
								" 助农取款" + Utility.printFillSpace(Utility.printInteger(settleData.substring(0, 3)),
										10)
								+ Utility.printFillSpace(Utility.unformatMount(settleData.substring(3, 15)), 10)));

						// mList.add(new PrintDataObject("", " 退货
						// "+Utility.printFillSpace(Utility.printInteger(settleData.substring(15,18)),10)
						// +"
						// "+Utility.printFillSpace(Utility.unformatMount(settleData.substring(18,30)),10)));
						//
						// mList.add(new PrintDataObject("", " 转账
						// "+Utility.printFillSpace(Utility.printInteger(settleData.substring(75,78)),10)
						// +"
						// "+Utility.printFillSpace(Utility.unformatMount(settleData.substring(78,90)),10)));
						//
						// mList.add(new PrintDataObject("", " 预授权完成
						// "+Utility.printFillSpace(Utility.printInteger(settleData.substring(105,108)),4)
						// +"
						// "+Utility.printFillSpace(Utility.unformatMount(settleData.substring(108,120)),10)));
					}
				}
			}
			if ("true".equals(dataMap.get("isReprints")) && ("".equals(isPrintDetails) || isPrintDetails == null)) {
				mList.add(new PrintDataObject("", "        ***重打印票据***", "24", "bold", "", ""));
			}
			// 判断是否打印细明
			if ("true".equals(isPrintDetails) && !"true".equals(dataMap.get("isReprints"))) {
				// mList.add(new PrintDataObject("", ""));
				mList.add(new PrintDataObject("", "结算明细单", "24", "bold", "true", ""));
				mList.add(new PrintDataObject("", "交易明细单/TXN LIST"));
				mList.add(new PrintDataObject("", "凭证号  类型  卡号  金额  授权号"));
				mList.add(new PrintDataObject("", "VOUCHER TYPE  CARD   AMT  AUTH "));
				mList.add(new PrintDataObject("", "--------------------------------"));

				// 获取交易记录表数据
				SettleDataDao mSettleDataDao = new SettleDataDaoImpl(LklcposApplication.lklcposAppContext);
				List<SettleData> mSd = null;
				mSd = mSettleDataDao.getSettleData();
				if (mSd == null) {
					mList.add(new PrintDataObject("", " \n\n"));
				} else {
					int i = 0;
					for (SettleData mRecord : mSd) {
						i++;
						String transCode = mRecord.transprocode;
						String reserve1 = mRecord.reserve1;
						String serviceCode = mRecord.conditionmode;

						String transtype = null;
						if ("900000".equals(transCode)) {
							transtype = "结算";
						} else if ("010000".equals(transCode) && "00".equals(serviceCode)) {
							// 此处更改为助农
							transtype = "    S";
						} else if ("000000".equals(transCode) && "06".equals(serviceCode)) {
							transtype = "    P";
						} else if ("030000".equals(transCode)) {
							transtype = "预授权";
						} else if ("200000".equals(transCode) && "00".equals(serviceCode) && "0230".equals(reserve1)) {
							transtype = "    R";
						} else if ("200000".equals(transCode) && "00".equals(serviceCode) && "0210".equals(reserve1)) {
							transtype = "消费撤销";
						} else if ("200000".equals(transCode) && "06".equals(serviceCode) && "0210".equals(reserve1)) {
							transtype = "预授权完成撤销";
						} else if ("200000".equals(transCode) && "06".equals(serviceCode) && "0110".equals(reserve1)) {
							transtype = "预授权撤销";
						}

						String cardno = Utility.formatCardno(mRecord.getPriaccount());
						String mount = Utility.unformatMount(mRecord.getTransamount());
						String billno = mRecord.getBatchbillno().substring(6, 12);
						String idrespcode = mRecord.getIdrespcode();
						if ("".equals(idrespcode) || idrespcode == null || "null".equals(idrespcode)) {
							idrespcode = "";
						}

						mList.add(new PrintDataObject("",
								"" + billno + " " + transtype + " " + Utility.printFillSpace(cardno, 19) + ""));
						mList.add(new PrintDataObject("",
								"  " + Utility.printFillSpace(mount, 10) + "   " + idrespcode + " "));

					}
				}
			} /*
				 * else if("false".equals(isPrintDetails) &&
				 * !"true".equals(dataMap.get("isReprints"))){ //打印交易明细点击“取消”
				 * mList.add(new PrintDataObject("", " \n\n"));
				 * //原本为打印3行空格，现改需求，改为不打印 //modify by chenkehui @20130814 }
				 */
		}
		mList.add(new PrintDataObject("", " \n\n"));
		return mList;
	}

	private static List<PrintDataObject> getPrintDataALLTransObjects() {
		List<PrintDataObject> mList = new ArrayList<PrintDataObject>();
		int cardCNum = 0; // 内卡借记笔数
		int cardDNum = 0; // 内卡贷记笔数
		long cardCAcount = 0; // 内卡借记金额
		long cardDAcount = 0; // 内卡贷记金额

		int exCardCNum = 0; // 外卡借记笔数
		int exCardDNum = 0; // 外卡贷记笔数
		long exCardCAcount = 0; // 外卡借记金额
		long exCardDAcount = 0; // 外卡贷记金额

		mList.add(new PrintDataObject("", "当批次统计单", "24", "bold", "true", ""));
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(LklcposApplication.lklcposAppContext);
		mList.add(new PrintDataObject("商户名称(MERCHANT NAME)", ""));
		mList.add(new PrintDataObject("", "  " + mParamConfigDao.get("mchntname"), "24"));

		// 42域，商户编号
		mList.add(new PrintDataObject("商户编号(MERCHANT NO)", ""));
		mList.add(new PrintDataObject("", "  " + mParamConfigDao.get("merid")));

		// 41域，终端号
		mList.add(new PrintDataObject("终端号(TERMIANL)", ""));
		mList.add(new PrintDataObject("", "  " + mParamConfigDao.get("termid")));
		mList.add(new PrintDataObject("", "类型/TYPE  笔数/SUM   总金额/AMT"));
		mList.add(new PrintDataObject("", "--------------------------------"));
		TransRecordDao mTransRecordDao = new TransRecordDaoImpl();
		// 查询消费类型
		List<TransRecord> salelists = mTransRecordDao.getCountTransRecord("010000", "00", "0210");
		if (salelists != null) {
			if (salelists.size() > 0) {
				long saleAcount = 0;
				cardCNum += salelists.size();
				for (TransRecord saleRecord : salelists) {
					long transamount = Long.valueOf(saleRecord.transamount);
					cardCAcount += transamount;
					saleAcount += transamount;
				}
				mList.add(new PrintDataObject("", "助农取款"));
				mList.add(new PrintDataObject("", Utility.printFillSpace(String.valueOf(salelists.size()), 17)
						+ Utility.printFillSpace(Utility.unformatMount(String.valueOf(saleAcount)), 14)));
			}

		}
		// 查询消费消费类型
		List<TransRecord> voidlists = mTransRecordDao.getCountTransRecord("200000", "00", "0210");
		if (voidlists != null) {
			if (voidlists.size() > 0) {
				long voidAcount = 0;
				cardDNum += voidlists.size();
				for (TransRecord voidRecord : voidlists) {
					long transamount = Long.valueOf(voidRecord.transamount);
					cardDAcount += transamount;
					voidAcount += transamount;
				}
				mList.add(new PrintDataObject("", "消费撤销/VOID"));
				mList.add(new PrintDataObject("", Utility.printFillSpace(String.valueOf(voidlists.size()), 17)
						+ Utility.printFillSpace(Utility.unformatMount(String.valueOf(voidAcount)), 14)));
			}

		}
		// 查询预授权完成类型
		List<TransRecord> complists = mTransRecordDao.getCountTransRecord("000000", "06", "0210");
		if (complists != null) {
			if (complists.size() > 0) {
				long compAcount = 0;
				cardCNum += complists.size();
				for (TransRecord comRecord : complists) {
					long transamount = Long.valueOf(comRecord.transamount);
					cardCAcount += transamount;
					compAcount += transamount;
				}
				mList.add(new PrintDataObject("", "预授权完成/AUTH COMPLETE"));
				mList.add(new PrintDataObject("", Utility.printFillSpace(String.valueOf(complists.size()), 17)
						+ Utility.printFillSpace(Utility.unformatMount(String.valueOf(compAcount)), 14)));
			}

		}
		// 查询预授权完成撤销类型
		List<TransRecord> compvoidlists = mTransRecordDao.getCountTransRecord("200000", "06", "0210");
		if (compvoidlists != null) {
			if (compvoidlists.size() > 0) {
				long compvoidAcount = 0;
				cardDNum += compvoidlists.size();
				for (TransRecord compvoidRecord : compvoidlists) {
					long transamount = Long.valueOf(compvoidRecord.transamount);
					cardDAcount += transamount;
					compvoidAcount += transamount;
				}
				mList.add(new PrintDataObject("", "预授权完成撤销/COMPLETE VOID"));
				mList.add(new PrintDataObject("", Utility.printFillSpace(String.valueOf(compvoidlists.size()), 17)
						+ Utility.printFillSpace(Utility.unformatMount(String.valueOf(compvoidAcount)), 14)));
			}

		}
		// 查询退货类型
		List<TransRecord> refundlists = mTransRecordDao.getCountTransRecord("200000", "00", "0230");
		if (refundlists != null) {
			if (refundlists.size() > 0) {
				long refundAcount = 0;
				cardDNum += refundlists.size();
				for (TransRecord compvoidRecord : refundlists) {
					long transamount = Long.valueOf(compvoidRecord.transamount);
					cardDAcount += transamount;
					refundAcount += transamount;
				}
				mList.add(new PrintDataObject("", "退货/REFUND"));
				mList.add(new PrintDataObject("", Utility.printFillSpace(String.valueOf(refundlists.size()), 17)
						+ Utility.printFillSpace(Utility.unformatMount(String.valueOf(refundAcount)), 14)));
			}

		}
		mList.add(new PrintDataObject("内卡借记笔数", Utility.printFillSpace(String.valueOf(cardCNum), 4)));
		mList.add(new PrintDataObject("内卡借记金额",
				Utility.printFillSpace(Utility.unformatMount(String.valueOf(cardCAcount)), 18)));
		mList.add(new PrintDataObject("内卡贷记笔数", Utility.printFillSpace(String.valueOf(cardDNum), 4)));
		mList.add(new PrintDataObject("内卡贷记金额",
				Utility.printFillSpace(Utility.unformatMount(String.valueOf(cardDAcount)), 18)));
		mList.add(new PrintDataObject("外卡借记笔数", Utility.printFillSpace(String.valueOf(exCardCNum), 4)));
		mList.add(new PrintDataObject("外卡借记金额",
				Utility.printFillSpace(Utility.unformatMount(String.valueOf(exCardCAcount)), 18)));
		mList.add(new PrintDataObject("外卡贷记笔数", Utility.printFillSpace(String.valueOf(exCardDNum), 4)));
		mList.add(new PrintDataObject("外卡贷记金额",
				Utility.printFillSpace(Utility.unformatMount(String.valueOf(exCardDAcount)), 18)));
		mList.add(new PrintDataObject("", " \n\n\n"));
		return mList;
	}

	/**
	 * 打印数据对象转打印数据
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static byte[] getLakalaPrintData(List<PrintDataObject> printDataObjects) throws Exception {
		boolean isbold_set = false;
		boolean iscenter_set = false;
		boolean isfontSize_set = false;
		boolean islatestVersion = false;// 解决打印噪声的问题，由于旧版本不支持新指令，所以通过判断软件版本号来区分使用指令。
		try {
			byte[] m3V = DeviceFactory.getSystemDev().getSystemVersion();
			m3V = new byte[] { m3V[3], m3V[2] };
			islatestVersion = (m3V[0] * 256 + m3V[1]) > (0x07 * 256 + 0x09) ? true : false;
			Log.d("PrintDevJsIfc", "islatestVersion= " + islatestVersion);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("", e);
		}
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		// ous.write(new byte[]{0x1C,0x1B,0x33,35,0x1C});
		Log.i("PrintDev", "正在打印数据..");
		// todo 加入打印头LOGO部分的代码，可以找林星海或者罗班看看怎么打
		for (PrintDataObject printObj : printDataObjects) {
			String label = printObj.getLable();
			label = (label == null || label.equals("") || label.equals("null")) ? "" : (label + ":");
			String text = printObj.getText();
			text = (text == null || text.equals("null")) ? "" : text;
			String fontSize = printObj.getFontSize();
			String isbold = printObj.getIsbold();
			String iscenter = printObj.getIscenter();
			String iswordWrap = printObj.getIswordWrap(); // addby ld 20130426
			// ous.write(new byte[]{0x1C,0x1B,0x33,35,0x1C});

			// 是否居中
			if (iscenter != null && !iscenter.equals("") && !iscenter.equals("null")) {
				Log.d("PrintDevJsIfc", "iscenter.....................=" + iscenter);
				iscenter_set = true;
				if (islatestVersion) {
					ous.write(new byte[] { 0x1B, 0x61, 0x01 });
				} else {
					byte[] temp = ous.toByteArray();
					if (temp != null && temp.length > 0 && temp[temp.length - 1] == 0x1C) {
						ous.write(new byte[] { 0x1C, 0x1C, 0x1B, 0x61, 0x01, 0x1C });
					} else
						ous.write(new byte[] { 0x1C, 0x1B, 0x61, 0x01, 0x1C });
				}
			} else {
				if (iscenter_set) {
					if (islatestVersion) {
						ous.write(new byte[] { 0x1B, 0x61, 0x00 });
					} else {
						byte[] temp = ous.toByteArray();
						if (temp != null && temp.length > 0 && temp[temp.length - 1] == 0x1C) {
							ous.write(new byte[] { 0x1C, 0x1C, 0x1B, 0x61, 0x00, 0x1C });
						} else
							ous.write(new byte[] { 0x1C, 0x1B, 0x61, 0x00, 0x1C });
					}
					iscenter_set = false;
				}
			}
			// if()
			// 是否加粗
			if (isbold != null && !isbold.equals("") && !isbold.equals("null")) {
				Log.d("PrintDevJsIfc", "isbold.....................");
				isbold_set = true;
				if (islatestVersion) {
					ous.write(new byte[] { 0x1B, 0x45, 0x01 });
				} else {
					byte[] temp = ous.toByteArray();
					if (temp != null && temp.length > 0 && temp[temp.length - 1] == 0x1C) {
						ous.write(new byte[] { 0x1C, 0x1C, 0x1B, 0x45, 0x01, 0x1C });
					} else
						ous.write(new byte[] { 0x1C, 0x1B, 0x45, 0x01, 0x1C });
				}
			} else {
				if (isbold_set) {
					if (islatestVersion) {
						ous.write(new byte[] { 0x1B, 0x45, 0x00 });
					} else {
						byte[] temp = ous.toByteArray();
						if (temp != null && temp.length > 0 && temp[temp.length - 1] == 0x1C) {
							ous.write(new byte[] { 0x1C, 0x1C, 0x1B, 0x45, 0x00, 0x1C });
						} else
							ous.write(new byte[] { 0x1C, 0x1B, 0x45, 0x00, 0x1C });
					}
					isbold_set = false;
				}
			}

			// 倍高、倍宽
			if (fontSize != null && !fontSize.equals("") && !fontSize.equals("null")) {

				byte font = (byte) Integer.parseInt(fontSize);

				// 判断字号,有值的话给一个字号
				isfontSize_set = true;
				if (islatestVersion) {
					ous.write(new byte[] { 0x1B, 0x21, font });
				} else {
					byte[] temp = ous.toByteArray();
					if (temp != null && temp.length > 0 && temp[temp.length - 1] == 0x1C) {
						ous.write(new byte[] { 0x1C, 0x1C, 0x1B, 0x21, font, 0x1C });
					} else
						ous.write(new byte[] { 0x1C, 0x1B, 0x21, font, 0x1C });
				}
				// ous.write(new byte[]{0x1C,0x1B,0x45,0x01,0x1C});
			} else {
				if (isfontSize_set) {
					if (islatestVersion) {
						ous.write(new byte[] { 0x1B, 0x21, 0x00 });
					} else {
						byte[] temp = ous.toByteArray();
						if (temp != null && temp.length > 0 && temp[temp.length - 1] == 0x1C) {
							ous.write(new byte[] { 0x1C, 0x1C, 0x1B, 0x21, 0x00, 0x1C });
						} else
							ous.write(new byte[] { 0x1C, 0x1B, 0x21, 0x00, 0x1C });
					}
					isfontSize_set = false;
				}
				// ous.write(new byte[]{0x1C,0x1B,0x45,0x00,0x1C});
			}
			ous.write((label + text).getBytes("gb2312"));
			// 是否换行判断
			if (!"nobreak".equals(iswordWrap)) {// 如果换行处不为nobreak就不换行，否则不换行
												// addby ld 20130426
				ous.write(new byte[] { 0x0d });// 换行
			}
		}
		// ous.write(new byte[]{0x0d});
		// ous.write(new byte[]{0x0d});
		// ous.write(new byte[]{0x0d});
		byte[] temp = ous.toByteArray();
		return ous.toByteArray();
	}

	public int getPrintState() {
		int result = 0;
		Log.d("PrintDev", "开始检测打印机状态.......");
		try {
			init();
			Log.d("PrintDev", "打开打印机设备.....");
			printdev.open();
			Log.d("PrintDev", "获取打印机状态.........");
			printdev.getPrintState();
		} catch (IndicationException e) {
			int eventid = e.getEventId();
			if ((eventid & 0x80) != 0) {
				Log.w("PrintDev", "打印机过热..");
				logger.warn("打印机过热....", e);
				result = 2;
			} else if ((eventid & 0x10) != 0) {
				Log.w("PrintDev", "打印机高温....");
				logger.warn("打印机高温....", e);
				result = 3;
			} else if ((eventid & 0x20) != 0) {
				Log.w("PrintDev", "打印机缺纸....");
				logger.warn("打印机缺纸....", e);
				result = 4;
			} else {
				Log.w("PrintDev", "未知打印机异常.......");
				logger.warn("未知打印机异常....", e);
				result = 1;
			}
		} catch (Exception e) {
			Log.e("PrintDev", "打印机状态检测失败.......", e);
			logger.warn("打印机状态检测失败.......", e);
			result = 1;
		} finally {
			try {
				printdev.close();
			} catch (Exception e) {
				e.printStackTrace();
				logger.warn("打印机关闭异常", e);
			}
		}
		return result;
	}

	public void printBmp(final String bmppath) {
		final Bitmap bitmap = BitmapFactory.decodeFile(bmppath);
		if (bitmap == null) {
			Log.e("PrintDev", "print bit map error");
			return;
		}

		new Thread() {
			public void run() {
				try {
					if (isopen) {
						printdev.printBMP(bitmap);
					} else {
						throw new Exception("设备未打开..");
					}
				} catch (Exception e) {
					Log.e("PrintDev", "print bit map error", e);
					logger.error("print bit map error", e);
				}
			};
		}.start();
	}

	// 打印结算信息
	public void printDetaiData(Map<String, String> dataMap) throws Exception {
		// 获取交易记录表数据
		SettleDataDao mSettleDataDao = new SettleDataDaoImpl(LklcposApplication.lklcposAppContext);
		List<PrintDataObject> mList = new ArrayList<PrintDataObject>();
		List<SettleData> mSd = null;

		mList.add(new PrintDataObject("", "结算明细单", "24", "bold", "true", ""));
		mList.add(new PrintDataObject("", "交易明细单/TXN LIST"));
		mList.add(new PrintDataObject("", "凭证号  类型  卡号  金额  授权号"));
		mList.add(new PrintDataObject("", "VOUCHER TYPE  CARD   AMT  AUTH "));
		mList.add(new PrintDataObject("", "--------------------------------"));
		mSd = mSettleDataDao.getTXNData(); // 获取交易明细单
		if (mSd == null) {
			mList.add(new PrintDataObject("", " "));
			printdev.print(getLakalaPrintData(mList));
			printdev.close();
			init();
			printdev.open();
		} else { // 打印结算明细数据
			printDetails(mList, mSd);
		}
		mList.clear();
		if (mSd != null) {
			mSd.clear();
			mSd = null;
		}

		mList.add(new PrintDataObject("", "未成功上送/UNSUCCESSSFUL LIST"));
		mList.add(new PrintDataObject("", "凭证号    类型    卡号     金额"));
		mList.add(new PrintDataObject("", "VOUCHER   TYPE    CARD     AMT"));
		mList.add(new PrintDataObject("", "--------------------------------"));
		mSd = mSettleDataDao.getUnSuccesssFulData(); // 获取未成功上送的交易明细
		if (mSd == null) {
			mList.add(new PrintDataObject("", " "));
			printdev.print(getLakalaPrintData(mList));
			printdev.close();
			init();
			printdev.open();
		} else { // 打印结算明细数据
			printDetails(mList, mSd);
		}
		mList.clear();
		if (mSd != null) {
			mSd.clear();
			mSd = null;
		}

		mList.add(new PrintDataObject("", "上送后被平台拒绝/DENIED LIST"));
		mList.add(new PrintDataObject("", "凭证号    类型    卡号     金额"));
		mList.add(new PrintDataObject("", "VOUCHER   TYPE    CARD     AMT"));
		mList.add(new PrintDataObject("", "--------------------------------"));
		mSd = mSettleDataDao.getDeniedData(); // 获取上送被拒绝的交易明细
		if (mSd == null) {
			mList.add(new PrintDataObject("", " \n"));
			printdev.print(getLakalaPrintData(mList));
			printdev.close();
			init();
			printdev.open();
		} else { // 打印结算明细数据
			printDetails(mList, mSd);
		}
		mList.clear();
		if (mSd != null) {
			mSd.clear();
			mSd = null;
		}
	}

	// 分段打印明细数据（每20条开关一次）
	private void printDetails(List<PrintDataObject> mList, List<SettleData> mSd) throws Exception {
		StandbyService.stopStandby(); // 防止打印明细数据过大，而返回待机界面。
		int i = 0;
		for (SettleData mRecord : mSd) {
			i++;
			String transCode = mRecord.transprocode;
			String reserve1 = mRecord.reserve1;
			String serviceCode = mRecord.conditionmode;

			String transtype = null;
			if ("010000".equals(transCode) && "0210".equals(reserve1)) {
				transtype = "    S";
			} else if ("000000".equals(transCode) && "06".equals(serviceCode)) {
				transtype = "    P";
			} else if ("200000".equals(transCode) && "00".equals(serviceCode) && "0230".equals(reserve1)) {
				transtype = "    R";
			} else if ("000000".equals(transCode) && "0330".equals(reserve1)) {
				transtype = "    E";
			} else if (("600000".equals(transCode) && "0210".equals(reserve1))
					|| ("630000".equals(transCode) && "0210".equals(reserve1))
					|| ("620000".equals(transCode) && "0210".equals(reserve1))) {
				transtype = "    Q";
			}

			String cardno = Utility.formatCardno(mRecord.getPriaccount());
			String mount = Utility.unformatMount(mRecord.getTransamount());
			String billno = mRecord.getBatchbillno().substring(6, 12);
			String idrespcode = mRecord.getIdrespcode();
			if ("".equals(idrespcode) || idrespcode == null || "null".equals(idrespcode)) {
				idrespcode = "";
			}

			mList.add(new PrintDataObject("",
					"" + billno + " " + transtype + " " + Utility.printFillSpace(cardno, 19) + ""));
			mList.add(new PrintDataObject("", "  " + Utility.printFillSpace(mount, 10) + "   " + idrespcode + " "));
			if (i == mSd.size()) {
				mList.add(new PrintDataObject("", " \n\n"));
			}
			if (i % 20 == 0 || i == mSd.size()) {
				printdev.print(getLakalaPrintData(mList));
				mList.clear();
				printdev.close();
				init();
				printdev.open();
			}
		}
		StandbyService.startStandby();
	}

	/*
	 * 打印数据 参数：dataMap中key的定义按照原拉卡拉中mct.xml文件中对于域的key 其他key： key --
	 * “isReprints”，值“true”时，为重打印，否则默认非重打印（重打印） key -- "isSecond",
	 * 值“true”时，打印第二联，否则默认打印第一联（pos签购） key --
	 * "requestSettleData"，对应Value为结算时终端结算信息（结算信息） key -- "printDetails",
	 * 值“true”时，打印细明，否则默认不打印交易细明 key -- "printtype", 值“alltrans”时，打印当前批次统计单
	 * 
	 * callback,回调函数，重写isPrintSecond()用于弹出对话框是否打印第二联
	 * 重写isPrintSecond()用于在打印的时候弹出对话框是否打印明细
	 */
	public void printData(final Map<String, String> dataMap, final CallBack callback) {
		Log.i("PrintDev", "start  print....");
		new Thread() {
			public void run() {
				Looper.prepare();
				try {
					if (!isopen) {
						throw new Exception("设备未打开..");
					}
					// printdev.printBMP(BitmapFactory.decodeFile("file:///android_asset/images"))
					printdev.setWaitingTimeout(10000);
					if ("alltrans".equals(dataMap.get("printtype"))) { // 打印统计单
						printdev.print(getLakalaPrintData(getPrintDataALLTransObjects()));
					} else if ("true".equals(dataMap.get("printDetails"))
							&& !"true".equals(dataMap.get("isReprints"))) { // 打印明细
						printDetaiData(dataMap);
					} else {
						printdev.print(getLakalaPrintData(getPrintDataObjects(dataMap)));
					}
					logger.debug("print  success....");
					Log.i("PrintDev", "print  success....");
					callback.isPrintSecond();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("打印数据异常", e);
					StandbyService.startStandby(); // 防止打印异常不出现待机
					Bundle b = new Bundle();
					int exceptionCode = 0;
					if (e instanceof IndicationException) {
						try {
							int eventid = ((IndicationException) e).getEventId();
							if ((eventid & 0x80) != 0) {
								b.putString("exceptiontip", "打印机过热，请稍候再试！");
								exceptionCode = 0x31;
							} else if ((eventid & 0x10) != 0) {
								b.putString("exceptiontip", "打印机高温，请稍候再试！");
								exceptionCode = 0x31;
							} else if ((eventid & 0x20) != 0) {
								b.putString("exceptiontip", "打印机缺纸，请装纸继续...！");
								b.putSerializable("printdata", (Serializable) dataMap);
								exceptionCode = 0x30;
							} else {
								b.putString("exceptiontip", "未知打印机异常，请联系客服！");
								exceptionCode = 0x31;
							}
						} catch (Exception e1) {
							e.printStackTrace();
						}
					} else if (e instanceof PrinterException) {
						int eventid = ((PrinterException) e).getEventId();
						if ((eventid & 0x80) != 0) {
							b.putString("exceptiontip", "打印机过热，请稍候再试！");
							exceptionCode = 0x31;
						} else if ((eventid & 0x10) != 0) {
							b.putString("exceptiontip", "打印机高温，请稍候再试！");
							exceptionCode = 0x31;
						} else if ((eventid & 0x20) != 0) {
							b.putString("exceptiontip", "打印机缺纸，请装纸继续...！");
							b.putSerializable("printdata", (Serializable) dataMap);
							exceptionCode = 0x30;
						} else {
							b.putString("exceptiontip", "未知打印机异常，请联系客服！");
							exceptionCode = 0x31;
						}
					} else if (e instanceof NumberFormatException || e instanceof IndexOutOfBoundsException
							|| e instanceof IllegalArgumentException) {
						b.putString("exceptiontip", "报文解析异常，请联系客服！");
						exceptionCode = 0x32;
					}
					callback.printExcept(exceptionCode, b);
				}
				Looper.loop();
			};
		}.start();
	}

	/*
	 * 打印结束回调
	 * 
	 */
	public interface CallBack {
		void isPrintSecond();

		void printExcept(int code, Bundle b);
	}
}
