package com.centerm.lklcpos.activity;

/*
 * 网络交互组件
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.dao.ReverseDao;
import com.centerm.comm.persistence.dao.ScriptNotityDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.entity.Reverse;
import com.centerm.comm.persistence.entity.ScriptNotity;
import com.centerm.comm.persistence.entity.SettleData;
import com.centerm.comm.persistence.entity.TransRecord;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.ReverseDaoImpl;
import com.centerm.comm.persistence.impl.ScriptNotityDaoImpl;
import com.centerm.comm.persistence.impl.SettleDataDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.iso8583.util.DataConverter;
import com.centerm.lklcpos.deviceinterface.EMVTAG;
import com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc;
import com.centerm.lklcpos.deviceinterface.HttpRequestJsIfc;
import com.centerm.lklcpos.deviceinterface.PbocDevJsIfc;
import com.centerm.lklcpos.deviceinterface.PbocDevJsIfc.PbocCallBack;
import com.centerm.lklcpos.deviceinterface.PinPadDevJsIfc;
import com.centerm.lklcpos.deviceinterface.PinPadInterface;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.DialogMessage;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.TlvUtil;
import com.centerm.lklcpos.util.TransactionUtility;
import com.centerm.lklcpos.util.Utility;
import com.centerm.lklcpos.view.PbocWiget;
import com.centerm.mid.exception.CentermApiException;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.PINPadDevInf;
import com.lkl.farmerwithdrawals.R;

public class NetworkActivity extends TradeBaseActivity {
	private static final Logger log = Logger.getLogger(NetworkActivity.class);

	private final int SALE_MODE = 1; // 消费
	private final int VOID_MODE = 2; // 消费撤销
	private final int INQU_MODE = 3; // 余额查询
	private final int SIGN_MODE = 4; // 签到
	private final int SETTLE_MODE = 5; // 签退‘结算
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
	private final int OFFLINE_REFUND_MODE = 18; // 脱机退货(校验)
	private final int OFFLINE_MODE = 19; // 脱机退货
	private final int AUTO_UP_OFFLINE_MODE = 20; // 自动脱机上送
	private final int ANTI_ACTIVE = 21; // 反激活

	private int CUR_MODE = 0; // 当前交易标记

	// private final int SHOW_OFFLINE_SALEUP = 0xA1; // 界面提示“脱机上送”
	private final int AFTERTRANS_START_OFFLINESALEUP = 0xB1; // 联机交易之后，开始脱机上送

	private boolean isSaveRecord = false; // 是否需要保存到交易表

	private TextView lableText; // 显示当前交互状态
	private TextView transTimer; // 网络交互倒计时
	private ImageView animImageView;
	private AnimationDrawable animationDrawable;

	private String transCode; // 交易码，配置文件对应中mctCode，用于交易组解包和区分不同的交易流程
	private Map<String, String> dataMap; // 组包Map
	private Map<String, String> resultMap; // 解包Map
	private String tpdu; // tpdu
	private long systraceno; // pos流水号
	private String billno; // 票据号/凭证号
	private String terminalid; // 终端号
	private String acceptoridcode; // 商户号
	private String batchno; // 批次号
	private String loadparams; // 60域数据
	private String entrymode; // 22域
	private String baseStationInfo; // 57域
	private String mesauthcode; // 64域
	private String reversetimes; // 冲正次数
	private int sendtimes = 3; // 消息发送次数
	private String resultCode = null; // 拉卡拉后台返回码
	private int timeout = 60;
	private int time;
	private int number = 1; // 上送笔数
	private boolean hasLoadParams = false; // 标记签到之后是否有下发，若有，则onStop()方法中不再次保存
	private boolean hasPin = false; // 判断是否带pin
	private boolean isICTrans = false; // 判断是否IC卡交易
	private boolean isPBOCStep = false; // 判断是否执行PBOC流程
	private boolean isAutoResult = false; // 判断是否自动
	private boolean isTransSucess = false; // 标志交易是否成功，判断条件返回39域=00

	private Timer timer = null;
	private TimerTask timerTask = null;

	private ParamConfigDao mParamConfigDao;
	private ReverseDao mReverseDao;
	private Reverse transReverse;
	private TransRecordDao mTransRecordDao;
	private SettleDataDaoImpl settleDataDao;
	private ScriptNotityDao mScriptNotitiesDao;
	private HttpRequestJsIfc mHttpRequestJsIfc;

	private PbocWiget pbocWiget;
	private PinPadInterface pinPadDev = null;
	private PbocDevJsIfc pbocDev;
	private ProgressDialog mDialog;
	private Dialog interDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.network);
		Log.i("ckh", "NetWorkActivity onCreate() start...");
		lableText = (TextView) findViewById(R.id.network_lable);
		transTimer = (TextView) findViewById(R.id.trans_timer);
		animImageView = (ImageView) findViewById(R.id.progress_image_anim);
		rotate(animImageView);
		// animImageView.setBackgroundResource(R.anim.network_anim);
		// animationDrawable = (AnimationDrawable)
		// animImageView.getBackground();
		// animationDrawable.start();

		mParamConfigDao = new ParamConfigDaoImpl(this);
		mReverseDao = new ReverseDaoImpl(this);
		mTransRecordDao = new TransRecordDaoImpl(this);
		settleDataDao = new SettleDataDaoImpl(this);
		mScriptNotitiesDao = new ScriptNotityDaoImpl(this);
		try {
			mHttpRequestJsIfc = new HttpRequestJsIfc(this, mHandler); // 实例化HttpRequestJSIfc对象
			timeout = mHttpRequestJsIfc.getDealtimeout();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("", e);
		}
		// 初始化数据
		dataMap = mTransaction.getDataMap(); // 交易流程中传递的map对象
		transCode = mTransaction.getMctCode(); // 本交易对应mct文件中交易码
		terminalid = mParamConfigDao.get("termid"); // 终端号
		acceptoridcode = mParamConfigDao.get("merid"); // 商户号
		reversetimes = mParamConfigDao.get("reversetimes"); // 重发次数
		if ("".equals(reversetimes) || reversetimes == null) { // 如果冲正次数为空，则默认值
			reversetimes = "3";
		}
		baseStationInfo = Utility.getBaseStationInfo(NetworkActivity.this); // 获取基站信息
		loadparams = mParamConfigDao.get("operatorcode") + mParamConfigDao.get("operatorpwd");
		// 60域用法一：管理员+密码
		tpdu = mParamConfigDao.get("tpdu");// 初始化数据库的时候保存默认值

		if (!"".equals(dataMap.get("pindata")) && dataMap.get("pindata") != null) {
			hasPin = true;
		}
		String posInputType = dataMap.get("posInputType");
		if (!"051".equals(posInputType) && !"071".equals(posInputType)) { // 非IC卡时，不保存卡序列信息
			dataMap.put("seqnumber", null);
		}
		if (!"".equals(posInputType) && posInputType != null) {
			if ("011".equals(posInputType)) {
				if (hasPin) {
					entrymode = "011";
				} else {
					entrymode = "012";
				}
			} else if ("021".equals(posInputType)) {
				if (hasPin) {
					entrymode = "021";
				} else {
					entrymode = "022";
				}
			} else if ("051".equals(posInputType)) {
				isICTrans = true;
				if (hasPin) {
					entrymode = "051";
				} else {
					entrymode = "052";
				}
				loadparams = loadparams + "50"; // 60域用法二
			} else if ("801".equals(posInputType)) {
				if (hasPin) {
					entrymode = "801";
				} else {
					entrymode = "802";
				}
				loadparams = loadparams + "52"; // 60域用法二
			} else if ("071".equals(posInputType)) { // QPBOC联机
				// isICTrans = true;
				if (hasPin) {
					entrymode = "071";
				} else {
					entrymode = "072";
				}
				loadparams = loadparams + "60";
			}
		}

		mesauthcode = "0000000100000010000000110000010000000101000001100000011100001000"; // 临时使用12345678的二进制字符串表示，报文计算时会替换

		try {
			pbocDev = new PbocDevJsIfc(NetworkActivity.this, pbocHandler);
			pbocDev.openDev();
			pbocWiget = new PbocWiget(NetworkActivity.this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("", e);
		}

		// 判断当前交易
		if ("002302".equals(transCode)) { // 消费
			CUR_MODE = SALE_MODE;
			isSaveRecord = true;
			if (isICTrans) {
				isPBOCStep = true;
				if ("071".equals(posInputType)) {
					pbocDev.rfPreProcess((byte) 0x01, dataMap.get("transamount"));
				} else {
					pbocDev.startProcTransfer(0x01, 0, dataMap);
				}
			}

		} else if ("002301".equals(transCode)) { // 余额查询
			CUR_MODE = INQU_MODE;
			if (isICTrans) {
				isPBOCStep = true;
				pbocDev.startProcTransfer(0x05, 0, dataMap);

			}

		} else if ("002303".equals(transCode)) { // 消费撤销
			CUR_MODE = VOID_MODE;
			isSaveRecord = true;

		} else if ("002308".equals(transCode)) { // 签到
			CUR_MODE = SIGN_MODE;
			putSignInfo(dataMap);
			mHttpRequestJsIfc.transactionRequest(transCode, dataMap);// 发送请求
			startTimer();
			return;

		} else if ("002309".equals(transCode)) { // 签退、结算
			CUR_MODE = SETTLE_MODE;

		} else if ("900004".equals(transCode)) { // 下载证书
			CUR_MODE = DOWNLOADCER_MODE;
			mHttpRequestJsIfc.handleHttpDownload(acceptoridcode, terminalid, dataMap.get("capwd"));
			startTimer();
			return;

		} else if ("002313".equals(transCode)) { // 预授权
			CUR_MODE = AUTH_MODE;
			isSaveRecord = true;
			if (isICTrans) {
				isPBOCStep = true;
				pbocDev.startProcTransfer(0x0C, 0, dataMap);

			}

		} else if ("002314".equals(transCode)) { // 预授权完成
			CUR_MODE = AUTH_COMPLETE_MODE;
			isSaveRecord = true;

		} else if ("002315".equals(transCode)) { // 预授权完成撤销
			CUR_MODE = COMPLETE_VOID_MODE;
			isSaveRecord = true;

		} else if ("002316".equals(transCode)) { // 预授权撤销
			CUR_MODE = AUTH_VOID_MODE;
			isSaveRecord = true;

		} else if ("002317".equals(transCode)) { // 退货
			CUR_MODE = REFUND_MODE;
			isSaveRecord = true;
		} else if ("002319".equals(transCode)) { // IC公钥下载
			CUR_MODE = ICCADOWN_MODE;
			mHttpRequestJsIfc.transactionRequest(transCode, putCaDownInfo());
			startTimer();
			return;
		} else if ("002320".equals(transCode)) { // IC参数下载
			CUR_MODE = ICPARAMDOWN_MODE;
			mHttpRequestJsIfc.transactionRequest(transCode, putParamDownInfo());
			startTimer();
			return;
		} else if ("002322".equals(transCode)) { // 指定账户圈存
			CUR_MODE = TRANSFER_MODE;
			isSaveRecord = true;
			isPBOCStep = true;
			pbocDev.startProcTransfer(0x0E, 0, dataMap);

		} else if ("002323".equals(transCode)) { // 非指定账户圈存
			CUR_MODE = NON_TRANSFER_MODE;
			isSaveRecord = true;
			isPBOCStep = true;
			pbocDev.startProcTransfer(0x0F, 0, dataMap);

		} else if ("002321".equals(transCode)) { // 现金充值
			CUR_MODE = CASH_UP_MODE;
			isSaveRecord = true;
			isPBOCStep = true;
			dataMap.put("transType", "002321"); // 电子现金交易不输入密码
			pbocDev.startProcTransfer(0x10, 0, dataMap);

		} else if ("002324".equals(transCode)) { // 现金充值撤销
			CUR_MODE = CASH_UP_VOID_MODE;
			isSaveRecord = true;
			isPBOCStep = true;
			pbocDev.startProcTransfer(0x11, 0, dataMap);

		} else if ("002325".equals(transCode)) { // 脱机脱货
			CUR_MODE = OFFLINE_REFUND_MODE;
			isSaveRecord = true;
		} else if ("002326".equals(transCode)) { // 自动上送脱机消费
			CUR_MODE = AUTO_UP_OFFLINE_MODE;
		} else if ("002329".equals(transCode)) { // 反激活
			CUR_MODE = ANTI_ACTIVE;
			dataMap.remove("seqnumber");
			log.info("传递的数据" + dataMap);
		}

		if (isPBOCStep) { // PBOC时，不显示
			lableText.setVisibility(View.INVISIBLE);
			animImageView.setVisibility(View.INVISIBLE);
			mDialog = new DialogMessage(NetworkActivity.this).createProgressDialog("正在校验卡片，请稍候（勿拔卡）...");
			return;
		} else {
			startTimer();
		}
		startTrans();

		Log.i("ckh", "NetWorkActivity onCreate() end.");
	}

	private void startTrans() {
		log.debug("startTrans() run......");
		if (hasScriptNotity() && CUR_MODE != AUTO_UP_OFFLINE_MODE) { // 自动上送脱机交易的时候不上送脚本处理结果
			doScriptNotity(); // 脚本处理结果通知上送
		} else if (hasReversal() && CUR_MODE != AUTO_UP_OFFLINE_MODE) { // 自动上送脱机交易的时候不上送冲正
			doTransReversal(); // 冲正处理
		} else if (CUR_MODE == SETTLE_MODE) {
			beforeSettleNotityUp(); // 结算前需上送脱机消费、TC上送、AAC\ARPC上送
		} else {
			doTransactionRequest(); // 交易请求处理
		}
	}

	// 开启计时
	private void startTimer() {
		if (timer != null) {
			timer.cancel();
			timerTask.cancel();
			timer = null;
			timerTask = null;
		}
		time = timeout;
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				Message timerMessage = Message.obtain();
				Bundle data = new Bundle();
				data.putString("second", String.valueOf(time));
				timerMessage.setData(data);
				timerHandler.sendMessage(timerMessage);
				if (time == 0) {
					return;
				}
				time--;
			}
		};
		timer.schedule(timerTask, 0, 1000);
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timerTask.cancel();
		}
		timer = null;
		timerTask = null;
	}

	// 更新计时器
	private Handler timerHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			transTimer.setText(msg.getData().getString("second"));
		}

	};

	// 处理交易交互结（不包括脚本上送、冲正、TC、AAC\ARPC等）
	private Handler mHandler = new Handler() {
		private boolean flag = false;

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0x10:
				startTimer();

				break;
			case 0x01:
				if (CUR_MODE == SIGN_MODE) {
					if (getIntent().getBooleanExtra("isAuto", false)) {
						lableText.setText("正在自动签到，请稍等......");
					} else {
						lableText.setText("正在签到，请稍等......");
					}
				} else if (CUR_MODE == DOWNLOADCER_MODE) {
					lableText.setText("正在下载证书，请稍等......");
				} else if (CUR_MODE == ICCADOWN_MODE) {
					lableText.setText("正在更新公钥，请稍等......");
				} else if (CUR_MODE == ICPARAMDOWN_MODE) {
					lableText.setText("正在更新IC卡参数，请稍等......");
				} else {
					flag = true;
					lableText.setText("正在发送数据，请稍等......");
				}
				break;
			case 0x02:
				if (flag) {
					flag = false;
					lableText.setText("正在接收数据，请稍等......");
				}
				break;
			case 0x00: // 接收到数据
				/*
				 * Bundle对象中字段说明："isSuc" Boolean 拉卡拉前置返回是否成功 "resMap"
				 * Map<String, String> 接收拉卡拉前置的数据解包成map对象 "retCode" String
				 * 网络交互返回码，可能是拉卡拉前置返回，也可能是本地错误码 "errMsg" String 网络交互返回错误信息
				 */
				stopTimer();

				Bundle b = msg.getData();
				DealResultThread dealResultThread = new DealResultThread(b);
				dealResultThread.start();
				break;
			case AFTERTRANS_START_OFFLINESALEUP:
				doOfflineSaleUp();
				break;
			case 0xe1:
				dealException("密码键盘导入工作密钥失败！"); // 发散工作密钥失败
				break;
			default:
				break;
			}
		}
	};

	private class DealResultThread extends Thread {
		private Bundle b;

		public DealResultThread(Bundle bundle) {
			this.b = bundle;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (b.getBoolean("isSuc")) {
				resultMap = (Map<String, String>) b.getSerializable("resMap");
				// 下载证书成功
				if (CUR_MODE == DOWNLOADCER_MODE) {
					mTransaction.getResultMap().put("respcode", "3030");
					Intent nextIntent = forward("1");
					nextIntent.putExtra("transaction", mTransaction);
					startActivity(nextIntent);
					// addActivityAnim();
					return;
				}
				resultCode = resultMap.get("respcode");
				// 处理44域数据
				try {
					afterSuc(resultMap);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.error("", e);
					mHandler.sendEmptyMessage(0xe1);
					return;
				}

				if ("3030".equals(resultCode) || "3935".equals(resultCode)) {// 拉卡拉返回“00”或者“95”
					switch (CUR_MODE) {
					case SIGN_MODE:
						try {
							loadMkeyAndWkeyForOffPin();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							log.error("", e);
							e.printStackTrace();
							mHandler.sendEmptyMessage(0xe1);
						}
						int ret = successSignOper(resultMap);
						if (ret != 0) { // 有需要更新（公钥或者AID参数）
							if (ret == 1 || ret == 4) { // 需要更新公钥(或者都需要更新)
								CUR_MODE = ICCADOWN_MODE;
								mHttpRequestJsIfc.transactionRequest("002319", putCaDownInfo());
							} else if (ret == 2) { // 需要更新AID参数
								CUR_MODE = ICPARAMDOWN_MODE;
								mHttpRequestJsIfc.transactionRequest("002320", putParamDownInfo());
							}
							return;
						}
						// 判断是否为自动签到,如果是自动签到返回签到结果
						if (getIntent().getBooleanExtra("isAuto", false)) { // 判断是否自动签到
							isAutoResult = true;
						}
						break;
					case SALE_MODE:
						if (!isPBOCStep) {
							saveSettleRecordToDB(dataMap, resultMap);
							Utility.setPrintStatus(NetworkActivity.this, "strans"); // 标记有交易打印状态
						}
						break;
					case VOID_MODE:
						deleteSettleRecordFromDB(new String(HexUtil.hexStringToByte(dataMap.get("batchbillno"))));
						Utility.setPrintStatus(NetworkActivity.this, "strans"); // 标记有交易打印状态
						break;
					case INQU_MODE:
						break;
					case SETTLE_MODE:
						// 将商户信息等结算信息存配置表中的requestSettleData字段中
						resultMap.put("requestSettleData", getSettledata()); // 结算成功，用于打印内卡信息

						saveSettleDataToDB(dataMap, resultMap); // 保存结算信息到参数表，用于重打印结算

						// modify for 结算成功断电，重启状态判断有概率出现结算状态和打印状态都出现的bug
						Utility.settleSucessful(NetworkActivity.this);

						break;
					case AUTH_MODE:
						if (!isPBOCStep)
							Utility.setPrintStatus(NetworkActivity.this, "strans"); // 标记有交易打印状态
						break;
					case AUTH_VOID_MODE:
						Utility.setPrintStatus(NetworkActivity.this, "strans"); // 标记有交易打印状态
						break;
					case AUTH_COMPLETE_MODE:
						saveSettleRecordToDB(dataMap, resultMap);
						Utility.setPrintStatus(NetworkActivity.this, "strans"); // 标记有交易打印状态
						break;
					case COMPLETE_VOID_MODE:
						deleteSettleRecordFromDB(new String(HexUtil.hexStringToByte(dataMap.get("batchbillno"))));
						Utility.setPrintStatus(NetworkActivity.this, "strans"); // 标记有交易打印状态
						break;
					case REFUND_MODE:
						saveSettleRecordToDB(dataMap, resultMap);
						Utility.setPrintStatus(NetworkActivity.this, "strans"); // 标记有交易打印状态
						break;

					case ICCADOWN_MODE:
						doSucessCaDown(resultMap); // 公钥下载成功
						return;
					case ICPARAMDOWN_MODE:
						doSucessParamDown(resultMap);
						return;

					case OFFLINE_REFUND_MODE: // 脱机退货（校验）转脱机消费上送(脱机退货)
						CUR_MODE = OFFLINE_MODE;
						Map<String, String> offlineMap = OfflineTransInfo(dataMap, 1);
						// 脱机消费需打印原凭证号（通过脱机退货（校验）+脱机退货上送两个报文，构造字段类型“批次号+凭证号+原凭证号”）
						String allBillno = new String(HexUtil.hexStringToByte(dataMap.get("batchbillno"))).substring(6,
								12);
						dataMap.put("batchbillno", HexUtil.bcd2str((batchno + billno + allBillno).getBytes()));
						//
						mHttpRequestJsIfc.transactionRequest("002326", offlineMap);
						return;
					case OFFLINE_MODE:
						saveSettleRecordToDB(dataMap, resultMap);
						Utility.setPrintStatus(NetworkActivity.this, "strans");
						break;

					case CASH_UP_VOID_MODE:
						isPBOCStep = false; // 电子现金充值撤销不下发脚本
						deleteSettleRecordFromDB(new String(HexUtil.hexStringToByte(dataMap.get("batchbillno"))));
						Utility.setPrintStatus(NetworkActivity.this, "strans");
						break;
					case ANTI_ACTIVE:
						isAutoResult = true;
						break;
					default:
						break;
					}
					if (isSaveRecord && !isPBOCStep) { // 保存交易成功数据
						TransRecord record = MapObjToTransBean(dataMap, resultMap);
						mTransRecordDao.save(record);
					}
				} else { // 拉卡拉返回码非“00”或者"95"
					if (getIntent().getBooleanExtra("isAuto", false)) { // 判断是否自动
						isAutoResult = false;
					}
				}
				mTransaction.setResultMap(resultMap);
			} else {
				if (getIntent().getBooleanExtra("isAuto", false)) {
					isAutoResult = false;
				}
				String retcode = b.getString("retCode");
				if (retcode != null || "".equals(retcode)) {
					mTransaction.getResultMap().put("Http_rescode", retcode);
				} else {
					mTransaction.getResultMap().put("Http_rescode", "");
				}
				mTransaction.getResultMap().put("errMsg", b.getString("errMsg"));
			}

			if (isPBOCStep) {
				doIcPboc(b);
			} else {
				nextStep(b);// 反激活前置返回后进行下一步
			}
		}
	};

	// 联机交易完成 （需要删冲正、脱机上送）
	private void nextStep(Bundle b) {
		String isNeedString = b.getString("isNeedReverse"); // 是否清除冲正
		if (!"true".equals(isNeedString) && isSaveRecord) {
			mReverseDao.deleteAll();
		}

		// 联机交易之后上送脱机交易
		if (!mTransRecordDao.getTransRecordsByStatuscode("OF").isEmpty() // 有脱机记录
				&& CUR_MODE != SETTLE_MODE // 非结算
				&& CUR_MODE != DOWNLOADCER_MODE // 非证书下载
		) {
			mHandler.sendEmptyMessage(AFTERTRANS_START_OFFLINESALEUP);
			// doOfflineSaleUp();
			return;
		}

		transactionForward();
	}

	// 交易流程前进到下一界面
	private void transactionForward() {
		if (getIntent().getBooleanExtra("isAuto", false)) { // 判断是否自动(签到、自动脱机上送)
			if (isAutoResult) {
				Bundle bundle = new Bundle();
				for (String key : resultMap.keySet()) {
					bundle.putString(key, resultMap.get(key));
				}
				setResult(RESULT_OK, new Intent().putExtras(bundle));
			} else {
				if (CUR_MODE == ANTI_ACTIVE) {
					Bundle bundle = new Bundle();
					for (String key : resultMap.keySet()) {
						bundle.putString(key, resultMap.get(key));
					}
					setResult(RESULT_CANCELED, new Intent().putExtras(bundle));
				} else {
					setResult(RESULT_CANCELED);
				}
			}
			lklcposActivityManager.removeActivity(NetworkActivity.this);
			return;
		}

		Intent nextIntent = forward("1");
		if (null != nextIntent) {
			nextIntent.putExtra("transaction", mTransaction);
			startActivity(nextIntent);
			lklcposActivityManager.removeActivity(NetworkActivity.this);
		} else {
			DialogFactory.showTips(NetworkActivity.this, "程序发生异常");
			return;
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 屏蔽掉网络交互时，界面超时
		TradeBaseActivity.isHttp = true;
		StandbyService.onOperate();
		Log.i("ckh", "NetWorkActivity onStart()...");
	}

	// 判断是否含有脚本结果通知
	private boolean hasScriptNotity() {
		List<ScriptNotity> mScriptNotities = mScriptNotitiesDao.getEntities();
		if (mScriptNotities.isEmpty()) {
			return false;
		}
		mScriptNotities = null;
		return true;
	}

	// 脚本处理结果上送
	private void doScriptNotity() {
		List<ScriptNotity> mScriptNotities = mScriptNotitiesDao.getEntities();
		if (!mScriptNotities.isEmpty()) { // 脚本结果通知处理
			final int maxTimes = sendtimes;
			HttpRequestJsIfc scriptNotityHttpReq = null;
			for (final ScriptNotity mSN : mScriptNotities) {
				final int times = Integer.parseInt(mSN.getReversetimes()) + 1;
				if (times <= maxTimes) { // 需要重发
					lableText.setText("正在第[" + number + "]笔\n第[" + times + "]次脚本结果通知，请稍等......");
					TransHandler scriptNotityHandler = new TransHandler() {

						@Override
						void afterReviced(Message msg) {
							// TODO Auto-generated method stub
							Bundle b = msg.getData();
							if (b.getBoolean("isSuc")) {
								Map<String, String> map = (Map<String, String>) b.getSerializable("resMap");
								String rescode = map.get("respcode");
								if ("3030".equals(rescode)) {
									number++; // 计数
									mScriptNotitiesDao.delete(mSN);
								} else {
									if (times == maxTimes) {
										number++; // 计数
										mScriptNotitiesDao.delete(mSN);
									} else {
										mScriptNotitiesDao.update(mSN.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
									}
								}
							} else {
								if (times == maxTimes) {
									number++; // 计数
									mScriptNotitiesDao.delete(mSN);
								} else {
									mScriptNotitiesDao.update(mSN.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
								}
							}
							doScriptNotity(); // 判断是否还需脱机消费上送
						}

					};
					try {
						scriptNotityHttpReq = new HttpRequestJsIfc(this, scriptNotityHandler);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						log.error("", e);
						return;
					}

					Map<String, String> infoMap = putICScriptNotityInfo(mSN);
					scriptNotityHttpReq.transactionRequest("002318", infoMap);
					break;
				} else {
					mScriptNotitiesDao.delete(mSN);
					doScriptNotity(); // 判断是否还需上送
				}
			}
		} else { // 无脚本信息
			number = 1; // 置1
			if (hasReversal()) {
				doTransReversal(); // 冲正处理
			} else if (CUR_MODE == SETTLE_MODE) {
				beforeSettleNotityUp();
			} else {
				doTransactionRequest(); // 发起交易请求
			}
		}
	}

	// 判断是否冲正
	private boolean hasReversal() {
		List<Reverse> mReverses = mReverseDao.getEntities();
		if (mReverses.isEmpty()) {
			return false;
		}
		mReverses = null;
		return true;
	}

	// 冲正
	private void doTransReversal() {
		List<Reverse> mReverses = mReverseDao.getEntities();
		if (!mReverses.isEmpty()) { // 冲正处理
			final int maxTimes = Integer.valueOf(reversetimes);
			for (final Reverse mR : mReverses) {
				final int times = Integer.parseInt(mR.getReversetimes()) + 1;

				if (times <= maxTimes) { // 需要冲正
					lableText.setText("正在进行第" + times + "次冲正，请稍等......");
					HttpRequestJsIfc reversalHttpReq = null;
					Handler reversalHandler;
					reversalHandler = new Handler() {

						@Override
						public void handleMessage(Message msg) {
							// TODO Auto-generated method stub
							super.handleMessage(msg);
							switch (msg.what) {
							case 0x10:
								startTimer();
								break;
							case 0x01:
								break;
							case 0x00:
								stopTimer();
								Bundle b = msg.getData();
								if (b.getBoolean("isSuc")) {
									Map<String, String> map = (Map<String, String>) b.getSerializable("resMap");
									String rescode = map.get("respcode");
									if ("3030".equals(rescode) || "3132".equals(rescode) || "3235".equals(rescode)) {
										lableText.setText("冲正成功");
										mReverseDao.delete(mR); // 冲正成功，清除冲正数据
									} else {
										lableText.setText("冲正不成功");
										if (times == maxTimes) {
											DialogFactory.showTips(NetworkActivity.this, "冲正失败");
											mReverseDao.delete(mR); // 冲正次数上限，不再冲正，清除数据
										} else {
											mReverseDao.update(mR.id, "reversetimes", String.valueOf((times))); // 冲正失败，更新冲正次数
										}
									}
								} else {
									if (times == maxTimes) {
										DialogFactory.showTips(NetworkActivity.this, "冲正失败");
										mReverseDao.delete(mR); // 冲正次数上限，不再冲正，清除数据
									} else {
										mReverseDao.update(mR.id, "reversetimes", String.valueOf((times))); // 冲正失败，更新冲正次数
									}
								}
								doTransReversal(); // 判断是否还需冲正
								break;

							default:
								break;
							}

						}

					};
					try {
						reversalHttpReq = new HttpRequestJsIfc(this, reversalHandler);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						log.error("", e);
						return;
					}
					Map<String, String> infoMap = putReverseInfo(mR);
					reversalHttpReq.transactionRequest("002304", infoMap);
					break;
				} else { // add by chenkehui for 设置冲正次数为0时，不执行冲正 @20130814
					mReverseDao.delete(mR); // 冲正次数上限，不再冲正，清除数据
					doTransReversal(); // 判断是否还需冲正
				}
			}
		} else { // 无冲正信息
			if (CUR_MODE == SETTLE_MODE) {
				beforeSettleNotityUp();
			} else {
				doTransactionRequest();
			}
		}
	}

	// 结算前依次上送脱机消费、TC上送、AAC\ARPC上送
	private void beforeSettleNotityUp() {
		if (!mTransRecordDao.getTransRecordsByStatuscode("OF").isEmpty()) {
			doOfflineSaleUp();
		} else if (!mTransRecordDao.getTransRecordsByStatuscode("TC").isEmpty()) {
			doTCUp();
		} else if (!mTransRecordDao.getTransRecordsByStatuscode("AACorARPC").isEmpty()) {
			doAACorARPCUp();
		} else {
			doTransactionRequest(); // 交易请求处理
		}
	}

	// 脱机消费上送
	private void doOfflineSaleUp() {
		List<TransRecord> mRecord = mTransRecordDao.getTransRecordsByStatuscode("OF");
		if (!mRecord.isEmpty()) {
			final int maxTimes = sendtimes;
			for (final TransRecord mR : mRecord) {
				final int times = Integer.parseInt(mR.getReversetimes()) + 1;
				if (times <= maxTimes) {
					lableText.setText("正在第[" + number + "]笔\n第[" + times + "]次脱机消费上送，请稍等......");
					HttpRequestJsIfc offlineSaleHttpReq = null;
					TransHandler offlineSaleTransHandler = new TransHandler() {

						@Override
						void afterReviced(Message msg) {
							// TODO Auto-generated method stub
							Bundle b = msg.getData();
							if (b.getBoolean("isSuc")) {
								Map<String, String> map = (Map<String, String>) b.getSerializable("resMap");
								String rescode = map.get("respcode");
								if ("3030".equals(rescode)) { // 交易成功
									number++; // 计数
									mTransRecordDao.update(mR.id, "statuscode", "");
								} else { // 交易被拒绝
									if (times == maxTimes) {
										number++; // 计数
										settleDataDao.update(mR.batchbillno, "statuscode", "2"); // 脱机消费上送被拒绝
										mTransRecordDao.update(mR.id, "statuscode", "");
									} else {
										mTransRecordDao.update(mR.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
									}
								}
							} else { // 交易上送不成功
								if (times == maxTimes) {
									number++; // 计数
									settleDataDao.update(mR.batchbillno, "statuscode", "1"); // 脱机消费上送不成功
									mTransRecordDao.update(mR.id, "statuscode", "");
								} else {
									mTransRecordDao.update(mR.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
								}
							}
							doOfflineSaleUp(); // 判断是否还需脱机消费上送
						}
					};

					try {
						offlineSaleHttpReq = new HttpRequestJsIfc(this, offlineSaleTransHandler);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						log.error("", e);
						return;
					}
					Map<String, String> infoMap = OfflineTransInfo(TransactionUtility.transformToMap(mR), 0);
					offlineSaleHttpReq.transactionRequest("002326", infoMap);
					break;
				} else {
					number++; // 计数
					settleDataDao.update(mR.batchbillno, "statuscode", "1"); // 脱机消费上送不成功
					mTransRecordDao.update(mR.id, "statuscode", "");
					doOfflineSaleUp(); // 判断是否还需脱机消费上送
				}
			}
		} else if (CUR_MODE == SETTLE_MODE) { // 结算前脱机上送完成之后
			number = 1;
			beforeSettleNotityUp(); // 循环进入
		} else { // 除结算外，其他所有联机交易之后上送脱机交易之后
			transactionForward();
		}
	}

	// TC上送
	private void doTCUp() {
		List<TransRecord> mRecord = mTransRecordDao.getTransRecordsByStatuscode("TC");
		if (!mRecord.isEmpty()) {
			final int maxTimes = sendtimes;
			for (final TransRecord mR : mRecord) {
				final int times = Integer.parseInt(mR.getReversetimes()) + 1;

				if (times <= maxTimes) {
					lableText.setText("正在第[" + number + "]笔\n第[" + times + "]次交易证书上送，请稍等......");
					HttpRequestJsIfc ctHttpReq = null;
					TransHandler tcTransHandler = new TransHandler() {

						@Override
						void afterReviced(Message msg) {
							// TODO Auto-generated method stub
							Bundle b = msg.getData();
							if (b.getBoolean("isSuc")) {
								Map<String, String> map = (Map<String, String>) b.getSerializable("resMap");
								String rescode = map.get("respcode");
								if ("3030".equals(rescode)) {
									number++; // 计数
									mTransRecordDao.update(mR.id, "statuscode", "");
								} else {
									if (times == maxTimes) {
										number++; // 计数
										mTransRecordDao.update(mR.id, "statuscode", "");
									} else {
										mTransRecordDao.update(mR.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
									}
								}
							} else {
								if (times == maxTimes) {
									number++; // 计数
									mTransRecordDao.update(mR.id, "statuscode", "");
								} else {
									mTransRecordDao.update(mR.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
								}
							}
							doTCUp();
						}
					};

					try {
						ctHttpReq = new HttpRequestJsIfc(this, tcTransHandler);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						log.error("", e);
						return;
					}
					Map<String, String> infoMap = putAACInfo(mR);
					ctHttpReq.transactionRequest("002327", infoMap);
					break;
				} else {
					number++; // 计数
					mTransRecordDao.update(mR.id, "statuscode", "");
					doTCUp();
				}
			}
		} else {
			number = 1;
			beforeSettleNotityUp(); // 循环进入
		}
	}

	// AAC\ARPC上送
	private void doAACorARPCUp() {
		List<TransRecord> mRecord = mTransRecordDao.getTransRecordsByStatuscode("AACorARPC");
		if (!mRecord.isEmpty()) {
			final int maxTimes = sendtimes;
			for (final TransRecord mR : mRecord) {
				final int times = Integer.parseInt(mR.getReversetimes()) + 1;
				if (times <= maxTimes) {
					lableText.setText("正在第[" + number + "]笔\n第[" + times + "]次交易信息上送，请稍等......");
					HttpRequestJsIfc ctHttpReq = null;
					TransHandler tcTransHandler = new TransHandler() {

						@Override
						void afterReviced(Message msg) {
							// TODO Auto-generated method stub
							Bundle b = msg.getData();
							if (b.getBoolean("isSuc")) {
								Map<String, String> map = (Map<String, String>) b.getSerializable("resMap");
								String rescode = map.get("respcode");
								if ("3030".equals(rescode)) {
									number++; // 计数
									if ("AC".equals(mR.statuscode)) { // 交易拒绝AAC交易，不保存流水
										mTransRecordDao.delete(mR);
									} else {
										mTransRecordDao.update(mR.id, "statuscode", "");
									}
								} else {
									if (times == maxTimes) {
										number++; // 计数
										if ("AC".equals(mR.statuscode)) { // 交易拒绝AAC交易，不保存流水
											mTransRecordDao.delete(mR);
										} else {
											mTransRecordDao.update(mR.id, "statuscode", "");
										}
									} else {
										mTransRecordDao.update(mR.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
									}
								}
							} else {
								if (times == maxTimes) {
									number++; // 计数
									if ("AC".equals(mR.statuscode)) { // 交易拒绝AAC交易，不保存流水
										mTransRecordDao.delete(mR);
									} else {
										mTransRecordDao.update(mR.id, "statuscode", "");
									}
								} else {
									mTransRecordDao.update(mR.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
								}
							}
							doAACorARPCUp();
						}
					};

					try {
						ctHttpReq = new HttpRequestJsIfc(this, tcTransHandler);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						log.error("", e);
						return;
					}
					Map<String, String> infoMap = putAACorARPCInfo(mR);
					ctHttpReq.transactionRequest("002328", infoMap);
					break;
				} else {
					number++; // 计数
					if ("AC".equals(mR.statuscode)) { // 交易拒绝AAC交易，不保存流水
						mTransRecordDao.delete(mR);
					} else {
						mTransRecordDao.update(mR.id, "statuscode", "");
					}
					doAACorARPCUp();
				}
			}
		} else {
			number = 1;
			beforeSettleNotityUp(); // 循环进入
		}
	}

	// 自定义一个通用消息上送Handler，用于脚本结果通知上送、冲正上送、TC上送、AAC\ARPC上送
	private abstract class TransHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0x10:
				startTimer();
				break;
			case 0x01:
				break;
			case 0x00:
				stopTimer();
				afterReviced(msg);
				break;
			}
		}

		// 接收到返回报文之后处理操作
		abstract void afterReviced(Message msg);
	}

	// 发起交易请求
	private void doTransactionRequest() {
		log.info("----------------发起交易请求");
		switch (CUR_MODE) { // 不同交易组包
		case SALE_MODE:
			putSaleInfo(dataMap);
			break;
		case INQU_MODE:
			putInquInfo(dataMap);
			break;
		case VOID_MODE:
			putVoidInfo(dataMap);
			break;
		case SIGN_MODE:
			break;
		case SETTLE_MODE:
			putSettleInfo(dataMap);
			break;
		case DOWNLOADCER_MODE:
			break;
		case AUTH_MODE:
			putAuthInfo(dataMap);
			break;
		case AUTH_COMPLETE_MODE:
			putAuthCompleteInfo(dataMap);
			break;
		case COMPLETE_VOID_MODE:
			putCompleteVoidInfo(dataMap);
			break;
		case AUTH_VOID_MODE:
			putAuthVoidInfo(dataMap);
			break;
		case REFUND_MODE:
			putRefundInfo(dataMap);
			break;
		case TRANSFER_MODE:
			putTransferInfo(dataMap);
			break;
		case NON_TRANSFER_MODE:
			putNonTransferInfo(dataMap);
			break;
		case CASH_UP_MODE:
			putCashUpInfo(dataMap);
			break;
		case CASH_UP_VOID_MODE:
			putCashUpViodInfo(dataMap);
			break;
		case OFFLINE_REFUND_MODE:
			putOfflineRefundInfo(dataMap);
			break;
		case ANTI_ACTIVE:
			putAntiActiveInfo(dataMap);
			break;
		case AUTO_UP_OFFLINE_MODE: // 发起脱机消费上送
			mHandler.sendEmptyMessage(AFTERTRANS_START_OFFLINESALEUP);
			return;
		}

		if (isPBOCStep) { // 获取冲正上送报文中F55数据
			log.debug("L1184 获取冲正55F");
			dataMap.put("reversalF55", getReversalF55());
		}

		if (CUR_MODE != SIGN_MODE && CUR_MODE != DOWNLOADCER_MODE) {

			if (isSaveRecord && CUR_MODE != REFUND_MODE && CUR_MODE != OFFLINE_REFUND_MODE && CUR_MODE != ANTI_ACTIVE) { // 发起交易前，保存冲正数据，退货是不冲正，不保存冲正数据
				transReverse = MapObjToReverse(dataMap);
				mReverseDao.save(transReverse);
			}
			log.debug("L1193 发起交易");
			mHttpRequestJsIfc.transactionRequest(transCode, dataMap);
		}
	}

	// 仅获取POS流水号
	private void getSystraceN0() {
		String systracenoStr = mParamConfigDao.get("systraceno");
		if ("".equals(systracenoStr) || systracenoStr == null) {
			systracenoStr = "000000";
		}

		systraceno = Long.valueOf(systracenoStr) + 1; // POS流水号
		if (systraceno > 999999) {
			systraceno = 1;
		}
		mParamConfigDao.update("systraceno", String.valueOf(systraceno));
	}

	// 获取凭证号和POS流水号,并更新凭证号和pos流水号
	private void getNumberForPack() {
		batchno = mParamConfigDao.get("batchno"); // 批次号
		// batchno = "100000"; 在测试环境下需要设置较大值才能对账平衡
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
			systraceno = 1;
		}
		if (Integer.valueOf(billno) + 1 > 999999) {
			billno = Utility.addZeroForNum(String.valueOf(1), 6);
		} else {
			billno = Utility.addZeroForNum(String.valueOf(Integer.valueOf(billno) + 1), 6);
		}

		// 数据库更新凭证号和POS流水号
		Map<String, String> map = new HashMap<String, String>();
		map.put("systraceno", String.valueOf(systraceno));
		map.put("billno", billno);
		log.debug("systraceno: " + systraceno + "    billno: " + billno);
		mParamConfigDao.update(map);
	}

	// 脚本通知
	private Map<String, String> putICScriptNotityInfo(ScriptNotity mSN) {
		Map<String, String> infoMap = new HashMap<String, String>();
		infoMap.put("headerdata", tpdu);
		infoMap.put("priaccount", mSN.priaccount);
		infoMap.put("transprocode", "100000");
		infoMap.put("transamount", mSN.transamount);
		getNumberForPack();
		infoMap.put("systraceno", addZeroLeft(systraceno));
		infoMap.put("translocaldate", mSN.translocaldate);
		infoMap.put("entrymode", mSN.entrymode);
		infoMap.put("seqnumber", mSN.seqnumber);
		infoMap.put("receivemark", mSN.reserve2);
		infoMap.put("refernumber", mSN.refernumber);
		infoMap.put("idrespcode", mSN.idrespcode);
		infoMap.put("terminalid", mSN.terminalid);
		infoMap.put("acceptoridcode", mSN.acceptoridcode);
		infoMap.put("transcurrcode", mSN.transcurrcode);
		infoMap.put("icdata", mSN.icdata); // ic卡数据域
		infoMap.put("adddatapri", baseStationInfo); // F57基站信息
		infoMap.put("loadparams", HexUtil.bcd2str(mSN.loadparams.getBytes())); // 60域
		String newvoucherno = batchno + billno + mSN.batchbillno.substring(6, 12);
		infoMap.put("batchbillno", HexUtil.bcd2str(newvoucherno.getBytes())); // 批次号票据号
		infoMap.put("mesauthcode", mesauthcode); // 64域校验码

		infoMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		infoMap.put("msg_tp", "0500"); // 防串包，专用添加域
		return infoMap;
	}

	// 组装冲正
	private Map<String, String> putReverseInfo(Reverse mReverse) {
		// TODO Auto-generated method stub
		Map<String, String> infoMap = new HashMap<String, String>();
		infoMap.put("headerdata", tpdu);
		infoMap.put("priaccount", mReverse.priaccount);
		infoMap.put("transprocode", mReverse.transprocode);
		infoMap.put("transamount", mReverse.transamount);
		infoMap.put("systraceno", mReverse.systraceno);
		infoMap.put("translocaltime", mReverse.translocaltime);
		infoMap.put("translocaldate", mReverse.translocaldate);
		infoMap.put("expireddate", mReverse.expireddate);
		infoMap.put("entrymode", mReverse.entrymode);
		infoMap.put("seqnumber", mReverse.seqnumber);
		infoMap.put("conditionmode", mReverse.conditionmode);
		if (!"600000".equals(mReverse.transprocode) && !"620000".equals(mReverse.transprocode)
				&& !"630000".equals(mReverse.transprocode) && !"170000".equals(mReverse.transprocode)) {
			infoMap.put("track2data", mReverse.track2data);
			infoMap.put("track3data", mReverse.track3data);
		}
		infoMap.put("idrespcode", mReverse.idrespcode == null ? null : HexUtil.bcd2str(mReverse.idrespcode.getBytes()));
		infoMap.put("terminalid", HexUtil.bcd2str(mReverse.terminalid.getBytes()));
		infoMap.put("acceptoridcode", HexUtil.bcd2str(mReverse.acceptoridcode.getBytes()));
		infoMap.put("transcurrcode", HexUtil.bcd2str(mReverse.transcurrcode.getBytes()));
		infoMap.put("icdata", mReverse.icdata); // ic卡数据域
		infoMap.put("loadparams", HexUtil.bcd2str(mReverse.loadparams.getBytes()));
		infoMap.put("batchbillno", HexUtil.bcd2str(mReverse.batchbillno.getBytes()));
		infoMap.put("mesauthcode", mesauthcode); // 64域校验码
		infoMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		infoMap.put("msg_tp", "0400"); // 防串包，专用添加域
		return infoMap;
	}

	// 签到
	private void putSignInfo(Map<String, String> dataMap) {
		getNumberForPack();
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "910000");
		dataMap.put("systraceno", addZeroLeft(systraceno));// 不足六位左补0
		dataMap.put("conditionmode", "00");
		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes()));
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes()));
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes()));
		dataMap.put("msg_tp", "0820");
	}

	// 反激活
	private void putAntiActiveInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "500002");
		String appCount = "1"; // 应用个数
		String termid = mParamConfigDao.get("termid"); // 终端号
		log.info("终端号:" + termid);
		String merid = mParamConfigDao.get("merid"); // 商户号
		log.info("商户号:" + merid);
		String adddataword = appCount + merid + termid; // 附加数据
		dataMap.put("adddataword", HexUtil.bcd2str(adddataword.getBytes()));
	}

	// 组装消费dataMap
	private void putSaleInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		// 主账号
		dataMap.put("transprocode", "010000"); // 交易处理码，此次更改为助农交易处理码
		amountToBCD(dataMap); // 交易金额
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号

		// dataMap.put("translocaltime", Utility.getTransLocalTime()); // 交易本地时间
		// dataMap.put("translocaldate", Utility.getTransLocalDate()); // 交易本地日期

		dataMap.put("expireddate", "3010"); // 卡有效期(贷记卡手输卡号时输入)

		dataMap.put("entrymode", entrymode); // 22域
		// IC卡号时，卡片序列号
		dataMap.put("conditionmode", "00");
		// 二磁道信息
		// 三磁道信息

		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes()));
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes()));
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码

		// dataMap.put("pindata", value); //输入密码时，获得pin数据
		if (hasPin) {
			dataMap.put("secctrlinfo", "2600000000000000"); // 安全控制信息（参照webView）
		}
		dataMap.put("adddatapri", baseStationInfo);
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // 管理员密码

		String batchbillno = batchno + billno;
		// dataMap.put("batchbillno", batchbillno); //批次号票据号
		dataMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes()));

		dataMap.put("mesauthcode", mesauthcode); // 64域校验码
		// 52域 bin pinkey
		pinDataToBinString(dataMap);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0200");
	}

	// 查询
	private void putInquInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "300000"); // 交易处理码
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号
		// dataMap.put("translocaltime", Utility.getTransLocalTime()); // 交易本地时间
		// dataMap.put("translocaldate", Utility.getTransLocalDate()); // 交易本地日期
		dataMap.put("entrymode", entrymode);
		// dataMap.put("seqnumber", value); //卡片序列号,IC卡存在
		dataMap.put("conditionmode", "00");
		// dataMap.put("terminalid", terminalid); // 终端号
		// dataMap.put("acceptoridcode", acceptoridcode); // 商户编号
		// dataMap.put("transcurrcode", "156"); // 货币代码
		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes()));
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes()));
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码

		if (hasPin) {
			dataMap.put("secctrlinfo", "2600000000000000"); // 安全控制信息（参照webView）
		}
		// dataMap.put("icdata", value); //IC卡数据域
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes()));

		String batchbillno = batchno + billno;
		// dataMap.put("batchbillno", batchbillno); //批次号票据号
		dataMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes()));

		dataMap.put("mesauthcode", mesauthcode);

		// 52域 bin pinkey
		pinDataToBinString(dataMap);
		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0200");
	}

	// 消费撤销
	private void putVoidInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "200000"); // 交易处理码
		String batstr = dataMap.get("batchbillno").substring(0, 6);
		String vouStr = dataMap.get("batchbillno").substring(6, 12);
		TransRecord mTransRecord = mTransRecordDao.getConsumeByCondition(batstr, vouStr);
		dataMap.put("transamount", mTransRecord.transamount); // 获取原交易金额
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno));
		// Pos流水号
		/*
		 * dataMap.put("translocaltime", Utility.getTransLocalTime()); //交易本地时间
		 * dataMap.put("translocaldate", Utility.getTransLocalDate()); //交易本地日期
		 */dataMap.put("entrymode", entrymode);
		// dataMap.put("seqnumber", value); //卡片序列号,IC卡存在
		dataMap.put("conditionmode", "00");
		// 有授权码则上送
		if (!"".equals(mTransRecord.idrespcode) && mTransRecord.idrespcode != null) {
			dataMap.put("idrespcode", HexUtil.bcd2str(mTransRecord.idrespcode.getBytes()));
		}
		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes()));
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes()));
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码

		if (hasPin) {
			dataMap.put("secctrlinfo", "2600000000000000"); // 安全控制信息（参照webView）
		}
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // 管理员密码

		String newvoucherno = batstr + billno + vouStr;
		// dataMap.put("batchbillno", newvoucherno); // 批次号票据号
		dataMap.put("batchbillno", HexUtil.bcd2str(newvoucherno.getBytes())); // 批次号票据号
		dataMap.put("mesauthcode", mesauthcode);
		// 52域 bin pinkey
		pinDataToBinString(dataMap);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0200");
	}

	// 签退、结算
	private void putSettleInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "900000"); // 交易处理码
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号
		dataMap.put("conditionmode", "00");
		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // 管理员密码

		String batchbillno = batchno + billno;
		dataMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes()));
		String settledata = getSettledata(); // 计算63域结算信息
		dataMap.put("settledata", HexUtil.bcd2str(settledata.getBytes()));
		dataMap.put("mesauthcode", mesauthcode);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0820");
	}

	// 预授权
	private void putAuthInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "030000"); // 交易处理码
		amountToBCD(dataMap);
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号
		// dataMap.put("translocaltime", Utility.getTransLocalTime()); // 交易本地时间
		// dataMap.put("translocaldate", Utility.getTransLocalDate()); // 交易本地日期
		// dataMap.put("expireddate", expireddate); //卡有效期
		dataMap.put("entrymode", entrymode); // 22域pos输入方式
		// dataMap.put("seqnumber"); //IC卡序列号
		dataMap.put("conditionmode", "06");
		// dataMap.put("terminalid", terminalid); // 终端号
		// dataMap.put("acceptoridcode", acceptoridcode); // 商户编号
		// dataMap.put("transcurrcode", "156"); // 货币代码

		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码

		if (hasPin) {
			dataMap.put("secctrlinfo", "2600000000000000"); // 安全控制信息（参照webView）
		}
		// 52域 bin pinkey
		pinDataToBinString(dataMap);
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes()));
		String batchbillno = batchno + billno;

		dataMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes())); // 批次号票据号

		dataMap.put("mesauthcode", mesauthcode);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0100");
	}

	// 预授权完成
	private void putAuthCompleteInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "000000"); // 交易处理码
		amountToBCD(dataMap);
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号
		// dataMap.put("translocaltime", Utility.getTransLocalTime()); // 交易本地时间
		// dataMap.put("translocaldate", Utility.getTransLocalDate()); // 交易本地日期

		dataMap.put("entrymode", entrymode); // 22域pos输入方式
		// dataMap.put("seqnumber"); //IC卡序列号
		dataMap.put("conditionmode", "06");
		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码

		if (hasPin) {
			dataMap.put("secctrlinfo", "2600000000000000"); // 安全控制信息（参照webView）
		}
		// 52域 bin pinkey
		pinDataToBinString(dataMap);
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes()));
		String batchbillno = batchno + billno;
		// dataMap.put("batchbillno", batchbillno); //批次号票据号
		dataMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes())); // 批次号票据号
		dataMap.put("mesauthcode", mesauthcode);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0200");
	}

	// 预授权完成撤销
	private void putCompleteVoidInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "200000"); // 交易处理码
		String batstr = dataMap.get("batchbillno").substring(0, 6);
		String vouStr = dataMap.get("batchbillno").substring(6, 12);
		TransRecord mTransRecord = mTransRecordDao.getConsumeByCondition(batstr, vouStr);
		dataMap.put("transamount", mTransRecord.transamount); // 获取原交易金额
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号
		// dataMap.put("translocaltime", Utility.getTransLocalTime()); //交易本地时间
		// dataMap.put("translocaldate", Utility.getTransLocalDate()); //交易本地日期
		// dataMap.put("expireddate", expireddate); //卡有效期
		dataMap.put("entrymode", entrymode); // 22域pos输入方式
		// dataMap.put("seqnumber"); //IC卡序列号
		dataMap.put("conditionmode", "06");
		// 有授权码则上送
		if (!"".equals(mTransRecord.idrespcode) || mTransRecord.idrespcode != null) {
			dataMap.put("idrespcode", mTransRecord.idrespcode);
		}
		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码

		if (hasPin) {
			dataMap.put("secctrlinfo", "2600000000000000"); // 安全控制信息（参照webView）
		}
		// 52域 bin pinkey
		pinDataToBinString(dataMap);
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // 管理员密码
		String batchbillno = batchno + billno + vouStr;
		dataMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes())); // 批次号票据号
		dataMap.put("mesauthcode", mesauthcode);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0200");
	}

	// 预授权撤销
	private void putAuthVoidInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "200000"); // 交易处理码
		amountToBCD(dataMap); // 输入原交易金额
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号
		// dataMap.put("translocaltime", Utility.getTransLocalTime()); //交易本地时间
		// dataMap.put("translocaldate", Utility.getTransLocalDate()); //交易本地日期

		dataMap.put("entrymode", entrymode); // 22域pos输入方式
		// dataMap.put("seqnumber"); //IC卡序列号
		dataMap.put("conditionmode", "06");
		// 授权码
		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码
		if (hasPin) {
			dataMap.put("secctrlinfo", "2600000000000000"); // 安全控制信息（参照webView）
		}
		// 52域 bin pinkey
		pinDataToBinString(dataMap);
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // 管理员密码
		String batchbillno = batchno + billno;
		dataMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes())); // 批次号票据号
		dataMap.put("mesauthcode", mesauthcode);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0100");
	}

	// 联机退货
	private void putRefundInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "200000"); // 交易处理码
		amountToBCD(dataMap); // 交易金额
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号
		dataMap.put("translocaltime", Utility.getTransLocalTime()); // 交易本地时间
		dataMap.put("translocaldate", Utility.getTransLocalDate()); // 交易本地日期
		// dataMap.put("expireddate", expireddate); //卡有效期
		dataMap.put("entrymode", entrymode); // 22域pos输入方式
		// dataMap.put("seqnumber"); //IC卡序列号
		dataMap.put("conditionmode", "00");
		// 37域原系统参考号
		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // 管理员密码
		String batchbillno = batchno + billno + "000000" + dataMap.get("oldTransDate");
		dataMap.put("batchbillno", batchbillno); // 批次号票据号
		dataMap.put("mesauthcode", mesauthcode);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0220");
	}

	// 公钥下载
	private Map<String, String> putCaDownInfo() {
		Map<String, String> cadownMap = new HashMap<String, String>();
		cadownMap.put("headerdata", tpdu);
		cadownMap.put("transprocode", "200000"); // 交易处理码
		getSystraceN0();
		cadownMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号
		cadownMap.put("translocaltime", Utility.getTransLocalTime()); // 交易本地时间
		cadownMap.put("translocaldate", Utility.getTransLocalDate()); // 交易本地日期
		cadownMap.put("track2data", mParamConfigDao.get("caversion"));
		cadownMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		cadownMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes()));// 商户编号
		cadownMap.put("mesauthcode", mesauthcode);

		cadownMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		cadownMap.put("msg_tp", "0500");
		return cadownMap;
	}

	// IC卡参数下载
	private Map<String, String> putParamDownInfo() {
		Map<String, String> paramdownMap = new HashMap<String, String>();
		paramdownMap.put("headerdata", tpdu);
		paramdownMap.put("transprocode", "300000"); // 交易处理码
		getSystraceN0();
		paramdownMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号
		paramdownMap.put("translocaltime", Utility.getTransLocalTime()); // 交易本地时间
		paramdownMap.put("translocaldate", Utility.getTransLocalDate()); // 交易本地日期
		paramdownMap.put("track2data", mParamConfigDao.get("paramversion"));
		paramdownMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes()));
		paramdownMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes()));
		paramdownMap.put("mesauthcode", mesauthcode);

		paramdownMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		paramdownMap.put("msg_tp", "0500");

		return paramdownMap;
	}

	// 指定账户圈存
	private void putTransferInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "600000"); // 交易处理码
		amountToBCD(dataMap); // 交易金额
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号

		dataMap.put("entrymode", entrymode); // 22域pos输入方式
		dataMap.put("conditionmode", "91"); // 服务点条件码

		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码
		// 52域 bin pinkey
		pinDataToBinString(dataMap);
		if (hasPin) {
			dataMap.put("pincapturecode", HexUtil.bcd2str("06".getBytes())); // F26,参考930上送固定值
			dataMap.put("secctrlinfo", "2600000000000000"); // 53域安全控制信息
		}
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // 管理员密码
		String batchbillno = batchno + billno;
		dataMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes())); // 批次号票据号
		dataMap.put("mesauthcode", mesauthcode);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0200");
	}

	// 非指定账户圈存
	private void putNonTransferInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "620000"); // 交易处理码
		amountToBCD(dataMap); // 交易金额
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号

		dataMap.put("entrymode", entrymode); // 22域pos输入方式
		dataMap.put("conditionmode", "91"); // 服务点条件码

		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		// 48域 附加信息
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码
		// 52域 bin pinkey
		pinDataToBinString(dataMap);
		if (hasPin) {
			dataMap.put("pincapturecode", HexUtil.bcd2str("06".getBytes())); // F26,参考930上送固定值
			dataMap.put("secctrlinfo", "2600000000000000"); // 53域安全控制信息
		}
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(((String) loadparams.subSequence(0, 6)).getBytes())); // 管理员密码
		String batchbillno = batchno + billno;
		dataMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes())); // 批次号票据号
		dataMap.put("mesauthcode", mesauthcode);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0200");
	}

	// 现金充值
	private void putCashUpInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "630000"); // 交易处理码
		amountToBCD(dataMap); // 交易金额
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号

		dataMap.put("entrymode", entrymode); // 22域pos输入方式
		dataMap.put("conditionmode", "91"); // 服务点条件码

		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码
		// 52域 bin pinkey
		pinDataToBinString(dataMap);
		if (hasPin) {
			dataMap.put("secctrlinfo", "2600000000000000"); // 53域安全控制信息
		}
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // 管理员密码
		String batchbillno = batchno + billno;
		dataMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes())); // 批次号票据号
		dataMap.put("mesauthcode", mesauthcode);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0200");
	}

	// 现金充值撤销
	private void putCashUpViodInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "170000"); // 交易处理码
		String batstr = dataMap.get("batchbillno").substring(0, 6);
		String vouStr = dataMap.get("batchbillno").substring(6, 12);
		TransRecord mTransRecord = mTransRecordDao.getConsumeByCondition(batstr, vouStr);
		amountToBCD(dataMap); // 交易金额
		getNumberForPack();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号

		dataMap.put("entrymode", entrymode); // 22域pos输入方式
		dataMap.put("conditionmode", "91"); // 服务点条件码
		dataMap.put("refernumber", mTransRecord.refernumber); // 37域检索参考号
		if (!"".equals(mTransRecord.idrespcode) || mTransRecord.idrespcode != null) { // 38域授权号，有则上送
			dataMap.put("idrespcode", mTransRecord.idrespcode);
		}
		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码
		// 52域 bin pinkey
		pinDataToBinString(dataMap);
		if (hasPin) {
			dataMap.put("secctrlinfo", "2600000000000000"); // 53域安全控制信息
		}
		dataMap.put("adddatapri", baseStationInfo); // F57基站信息
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // 管理员密码
		String newvoucherno = batstr + billno + vouStr;
		dataMap.put("batchbillno", HexUtil.bcd2str(newvoucherno.getBytes())); // 批次号票据号
		dataMap.put("mesauthcode", mesauthcode);

		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0200");
	}

	// 脱机退货(校验)
	private void putOfflineRefundInfo(Map<String, String> dataMap) {
		dataMap.put("headerdata", tpdu);
		dataMap.put("transprocode", "280000"); // 交易处理码
		amountToBCD(dataMap); // 交易金额
		getSystraceN0();
		dataMap.put("systraceno", addZeroLeft(systraceno)); // Pos流水号
		dataMap.put("translocaltime", Utility.getTransLocalTime()); // 交易本地时间
		dataMap.put("translocaldate", dataMap.get("oldTransDate"));
		dataMap.put("entrymode", entrymode); // 22域pos输入方式
		dataMap.put("conditionmode", "00"); // 服务点条件码
		dataMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // 终端号
		dataMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // 商户编号
		dataMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes())); // 货币代码
		dataMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // 管理员密码
		String transInfo = dataMap.get("batchbillno") + Utility.addZeroForNum(dataMap.get("oldterminalid"), 14);
		dataMap.put("batchbillno", HexUtil.bcd2str(transInfo.getBytes())); // 原批次号+原票据号+原终端号
		dataMap.put("mesauthcode", mesauthcode);
		dataMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		dataMap.put("msg_tp", "0220");
	}

	// 脱机消费上送type:0（包括脱机退货type:1）
	private Map<String, String> OfflineTransInfo(Map<String, String> dataMap, int type) {
		Map<String, String> infoMap = new HashMap<String, String>();
		infoMap.put("headerdata", tpdu);
		infoMap.put("priaccount", dataMap.get("priaccount"));
		infoMap.put("transprocode", "000000");
		infoMap.put("transamount", dataMap.get("transamount"));

		getNumberForPack();
		infoMap.put("systraceno", addZeroLeft(systraceno)); // F11Pos流水号

		infoMap.put("translocaltime", Utility.getTransLocalTime()); // 交易本地时间
		infoMap.put("translocaldate", Utility.getTransLocalDate()); // 交易本地日期
		infoMap.put("expireddate", "null".equals(dataMap.get("expireddate")) ? null : dataMap.get("expireddate"));
		infoMap.put("entrymode", dataMap.get("entrymode")); // F22
		infoMap.put("seqnumber", dataMap.get("seqnumber"));
		if (type == 0) {
			infoMap.put("conditionmode", "00"); // F25
			infoMap.put("adddataword", HexUtil.bcd2str("300".getBytes())); // F48
			infoMap.put("loadparams", HexUtil.bcd2str(dataMap.get("loadparams").getBytes())); // F60
		} else if (type == 1) {
			infoMap.put("conditionmode", "01"); // F25
			infoMap.put("adddataword", HexUtil.bcd2str("301".getBytes())); // F48
			infoMap.put("loadparams", HexUtil.bcd2str(loadparams.getBytes())); // F60
		}
		infoMap.put("terminalid", HexUtil.bcd2str(terminalid.getBytes())); // F41
																			// 终端号
		infoMap.put("acceptoridcode", HexUtil.bcd2str(acceptoridcode.getBytes())); // F42
																					// 商户编号
		infoMap.put("transcurrcode", HexUtil.bcd2str("156".getBytes()));
		infoMap.put("icdata", dataMap.get("icdata")); // F55
		if (type == 0) {
			infoMap.put("batchbillno", HexUtil.bcd2str(dataMap.get("batchbillno").getBytes()));
		} else {
			String batchbillno = batchno + billno;
			infoMap.put("batchbillno", HexUtil.bcd2str(batchbillno.getBytes())); // F62
		}
		infoMap.put("mesauthcode", mesauthcode);
		infoMap.put("udf_fld", "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
		infoMap.put("msg_tp", "0320");
		return infoMap;
	}

	// TC上送
	private Map<String, String> putAACInfo(TransRecord tr) {
		Map<String, String> infoMap = new HashMap<String, String>();
		infoMap.put("headerdata", tpdu);
		infoMap.put("priaccount", tr.priaccount);
		infoMap.put("transprocode", "000000");
		getSystraceN0();
		infoMap.put("systraceno", addZeroLeft(systraceno)); // F11Pos流水号
		infoMap.put("translocaltime", tr.translocaltime); // 原交易时间
		infoMap.put("translocaldate", tr.translocaldate); // 原交易日期
		infoMap.put("refernumber", HexUtil.bcd2str(tr.refernumber.getBytes()));
		infoMap.put("terminalid", HexUtil.bcd2str(tr.terminalid.getBytes()));
		infoMap.put("acceptoridcode", HexUtil.bcd2str(tr.acceptoridcode.getBytes()));
		infoMap.put("icdata", tr.reserve3);
		infoMap.put("loadparams", HexUtil.bcd2str(tr.loadparams.getBytes()));
		infoMap.put("mesauthcode", mesauthcode); // 64域校验码
		infoMap.put("udf_fld", "01");
		return infoMap;
	}

	// AAC\ARPC上送
	private Map<String, String> putAACorARPCInfo(TransRecord tr) {
		Map<String, String> infoMap = new HashMap<String, String>();
		infoMap.put("headerdata", tpdu);
		infoMap.put("priaccount", tr.priaccount);
		infoMap.put("transprocode", "400000");
		infoMap.put("transamount", tr.transamount);
		getNumberForPack();
		infoMap.put("systraceno", addZeroLeft(systraceno)); // F11Pos流水号
		infoMap.put("translocaltime", tr.translocaltime); // 原交易时间
		infoMap.put("translocaldate", tr.translocaldate); // 原交易日期
		infoMap.put("entrymode", tr.entrymode);
		infoMap.put("seqnumber", tr.seqnumber);
		infoMap.put("refernumber", HexUtil.bcd2str(tr.refernumber.getBytes()));
		infoMap.put("terminalid", HexUtil.bcd2str(tr.terminalid.getBytes()));
		infoMap.put("acceptoridcode", HexUtil.bcd2str(tr.acceptoridcode.getBytes()));
		if (("000000".equals(tr.transprocode) && "0210".equals(tr.reserve1))
				|| ("030000".equals(tr.transprocode) && "0110".equals(tr.conditionmode))) {
			infoMap.put("adddataword", HexUtil.bcd2str("100".getBytes())); // F48
																			// 完整PBOC流程的只有消费、预授权交易
		}
		infoMap.put("icdata", tr.reserve3);
		infoMap.put("settledata",
				"AC".equals(tr.statuscode) ? HexUtil.bcd2str("01".getBytes()) : HexUtil.bcd2str("02".getBytes()));
		infoMap.put("mesauthcode", mesauthcode); // 64域校验码
		infoMap.put("udf_fld", "01");
		return infoMap;
	}

	// 公钥下载成功之后操作
	private void doSucessCaDown(Map<String, String> cadownMap) {
		String cadata = cadownMap.get("loadparams"); // 60域公钥数据
		final String updataCode = cadownMap.get("updatecode");// 28域后续标识
		log.info("公钥下载成功 updataCode = " + updataCode);
		final String caversion = cadownMap.get("track2data"); // 公钥版本号
		log.debug("公钥下载成功：cadata = " + cadata + "\nupdataCode = " + updataCode + "\ncaversion = " + caversion);

		boolean isFirstUpdata = "00".equals(mParamConfigDao.get("caversion")) ? true : false;
		if (isFirstUpdata) {
			pbocDev.clearAllCa(); // 第一次更新公钥需清除当前
		}
		pbocDev.updataICCA(cadata, new PbocCallBack() {

			@Override
			public void sucessDownLoad() { // 更新成功
				// TODO Auto-generated method stub
				sucessUpdata(1, caversion, updataCode);
			}

			@Override
			public void exceptionDownLoad() {
				// TODO Auto-generated method stub
				failDownLoad();
			}
		});

	}

	// EMV参数下载成功之后操作
	private void doSucessParamDown(Map<String, String> paramdataMap) {
		String paramdata = paramdataMap.get("loadparams"); // 60域AID参数数据
		final String updataCode = paramdataMap.get("updatecode");// 28域后续标识
		log.info("EMV参数下载成功 updataCode = " + updataCode);
		final String paramversion = paramdataMap.get("track2data"); // AID参数版本号
		log.debug("参数下载成功：paramdata = " + paramdata + "\nupdataCode = " + updataCode + "\nparamversion = "
				+ paramversion);
		boolean isFirstUpdata = "00".equals(mParamConfigDao.get("paramversion")) ? true : false;
		if (isFirstUpdata) {
			log.debug("更新AID参数前，先清除参数");
			pbocDev.clearAllAID(); // 第一次更新AID参数，需清除原先的参数
		}
		pbocDev.updataICParam(paramdata, new PbocCallBack() {

			@Override
			public void sucessDownLoad() {
				// TODO Auto-generated method stub
				log.debug("更新AID paramersion AID参数版本 = " + paramversion);
				sucessUpdata(2, paramversion, updataCode);
			}

			@Override
			public void exceptionDownLoad() {
				// TODO Auto-generated method stub
				failDownLoad();
			}
		});
	}

	// 公钥、参数更新成功，type 1为公钥，2为参数
	private void sucessUpdata(int type, String version, String updataCode) {
		log.debug("更新成功");
		if (type == 1) {
			mParamConfigDao.update("caversion", version); // 更新公钥新版本号到数据库
		} else if (type == 2) {
			mParamConfigDao.update("paramversion", version); // 更新参数新版本号到数据库
		}
		if ("0".equals(updataCode)) { // 判断后续是否继续更新

			String updataStatus = mParamConfigDao.get("updatastatus");
			if (type == 1) { // 公钥更新完成，改变更新状态
				if ("4".equals(updataStatus)) { // 公钥更新完成，继续更新AID参数
					mParamConfigDao.update("updatastatus", "2");
					CUR_MODE = ICPARAMDOWN_MODE;
					mHttpRequestJsIfc.transactionRequest("002320", putParamDownInfo());
					return;
				} else if ("1".equals(updataStatus) || "".equals(updataStatus)) {
					mParamConfigDao.update("updatastatus", "0");
				}
			} else if (type == 2) {
				if ("4".equals(updataStatus)) { // AID参数更新完成，继续更新公钥
					mParamConfigDao.update("updatastatus", "1");
					CUR_MODE = ICCADOWN_MODE;
					mHttpRequestJsIfc.transactionRequest("002319", putCaDownInfo());
					return;
				} else if ("2".equals(updataStatus) || "".equals(updataStatus)) {
					mParamConfigDao.update("updatastatus", "0");
				}
			}

			try {
				pbocDev.closeDev();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("", e);
			}

			// 更新完成，设置显示结果为成功
			if (getIntent().getBooleanExtra("isAuto", false)) { // 判断是否自动签到
				setResult(RESULT_OK);
				lklcposActivityManager.removeActivity(NetworkActivity.this);
				return;
			}
			Map<String, String> sucessMap = new HashMap<String, String>();
			sucessMap.put("respcode", "3030");
			mTransaction.setResultMap(sucessMap);
			Intent nextIntent = forward("1");
			if (null != nextIntent) {
				nextIntent.putExtra("transaction", mTransaction);
				startActivity(nextIntent);
				lklcposActivityManager.removeActivity(NetworkActivity.this);
			} else {
				DialogFactory.showTips(NetworkActivity.this, "程序发生异常");
				return;
			}
		} else { // 后续继续更新
			if (type == 1) {
				mHttpRequestJsIfc.transactionRequest("002319", putCaDownInfo());
			} else if (type == 2) {
				mHttpRequestJsIfc.transactionRequest("002320", putParamDownInfo());
			}
		}
	}

	// 公钥、参数更新失败
	private void failDownLoad() {
		try {
			pbocDev.closeDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("", e);
		}

		// 更新异常，设置显示结果为失败
		Map<String, String> failMap = new HashMap<String, String>();
		failMap.put("Http_rescode", "ERR");
		failMap.put("errMsg", "更新失败，请重新更新");
		mTransaction.setResultMap(failMap);

		Intent nextIntent = forward("1");
		if (null != nextIntent) {
			nextIntent.putExtra("transaction", mTransaction);
			startActivity(nextIntent);
			lklcposActivityManager.removeActivity(NetworkActivity.this);
		} else {
			DialogFactory.showTips(NetworkActivity.this, "程序发生异常");
			return;
		}
	}

	private String addZeroLeft(long systraceno) {
		return Utility.addZeroForNum(String.valueOf(systraceno), 6);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		TradeBaseActivity.isHttp = false;
		StandbyService.onOperate();
		Log.i("ckh", "NetWorkActivity onPause()...");
		// animationDrawable.stop(); // 关闭动画

		try {
			pbocDev.closeDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("", e);
		}

		super.onPause();
	}

	// 屏蔽HOME、返回键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 获取结算信息
	 */
	public String getSettledata() {
		List<SettleData> settleDatas = null;
		settleDatas = settleDataDao.getSettleData();
		String str = null;
		int saleNum = 0;
		long saleAmt = 0;
		int compNum = 0;
		long compAmt = 0;
		int refNum = 0;
		long refAmt = 0;
		int transferNum = 0;
		long transferAmt = 0;

		if (settleDatas == null) {
			return fillzero("0", 126, true);
		}
		for (SettleData settledata : settleDatas) {
			// 计算消费（包括脱机消费）助农处理码010000
			if ("010000".equals(settledata.transprocode) && "00".equals(settledata.conditionmode)
					&& ("0210".equals(settledata.reserve1) || "0330".equals(settledata.reserve1))) {
				saleNum++;
				saleAmt += Long.valueOf(settledata.transamount);
			}

			// 计算预授权
			if ("000000".equals(settledata.transprocode) && "06".equals(settledata.conditionmode)
					&& "0210".equals(settledata.reserve1)) {
				compNum++;
				compAmt += Long.valueOf(settledata.transamount);
			}

			// 计算退货 (包括电子现金脱机退货)
			if (("200000".equals(settledata.transprocode) && "00".equals(settledata.conditionmode)
					&& "0230".equals(settledata.reserve1))
					|| ("000000".equals(settledata.transprocode) && "01".equals(settledata.conditionmode)
							&& "0330".equals(settledata.reserve1))) {
				refNum++;
				refAmt += Long.valueOf(settledata.transamount);
			}

			// 计算转账（包括指定账户圈存、电子现金充值、非指定账户圈存）
			if (("600000".equals(settledata.transprocode) && "0210".equals(settledata.reserve1))
					|| ("630000".equals(settledata.transprocode) && "0210".equals(settledata.reserve1))
					|| ("620000".equals(settledata.transprocode) && "0210".equals(settledata.reserve1))) {
				transferNum++;
				transferAmt += Long.valueOf(settledata.transamount);
			}
		}

		str = fillzero(String.valueOf(saleNum), 3, true) + fillzero(String.valueOf(saleAmt), 12, true)
				+ fillzero(String.valueOf(refNum), 3, true) + fillzero(String.valueOf(refAmt), 12, true)
				+ fillzero("0", 45, true) + fillzero(String.valueOf(transferNum), 3, true)
				+ fillzero(String.valueOf(transferAmt), 12, true) + fillzero("0", 15, true)
				+ fillzero(String.valueOf(compNum), 3, true) + fillzero(String.valueOf(compAmt), 12, true)
				+ fillzero("0", 6, true);
		return str;
	}

	private String fillzero(String str, int num, boolean isLeft) {
		StringBuffer zeroStr = new StringBuffer();
		if (str.length() < num) {
			int length = num - str.length();
			while (length > 0) {
				zeroStr.append("0");
				length--;
			}
		}
		return isLeft ? zeroStr.toString() + str : str + zeroStr.toString();
	}

	/**
	 * 将Map数据转化为冲正对象
	 * 
	 * @param hashmap
	 * @return
	 */
	public Reverse MapObjToReverse(Map<String, String> dataMap) {
		Reverse mReverse = new Reverse();
		mReverse.setPriaccount(dataMap.get("priaccount"));
		mReverse.setTransprocode(dataMap.get("transprocode"));
		mReverse.setTransamount(dataMap.get("transamount"));
		mReverse.setSystraceno(dataMap.get("systraceno"));
		mReverse.setTranslocaltime(dataMap.get("translocaltime"));
		mReverse.setTranslocaldate(dataMap.get("translocaldate"));
		mReverse.setExpireddate(dataMap.get("expireddate"));
		mReverse.setEntrymode(dataMap.get("entrymode"));
		mReverse.setSeqnumber(dataMap.get("seqnumber"));
		mReverse.setConditionmode(dataMap.get("conditionmode"));
		mReverse.setTrack2data(dataMap.get("track2data"));
		mReverse.setTrack3data(dataMap.get("track3data"));
		if (dataMap.get("idrespcode") != null) {
			mReverse.setIdrespcode(new String(HexUtil.hexStringToByte(dataMap.get("idrespcode"))));
		}
		mReverse.setTerminalid(new String(HexUtil.hexStringToByte(dataMap.get("terminalid"))));
		mReverse.setAcceptoridcode(new String(HexUtil.hexStringToByte(dataMap.get("acceptoridcode"))));
		mReverse.setTranscurrcode(new String(HexUtil.hexStringToByte(dataMap.get("transcurrcode"))));
		mReverse.setIcdata(dataMap.get("reversalF55")); // 冲正上送报文F55数据
		mReverse.setLoadparams(new String(HexUtil.hexStringToByte(dataMap.get("loadparams"))));
		mReverse.setBatchbillno(new String(HexUtil.hexStringToByte(dataMap.get("batchbillno"))));
		mReverse.setReversetimes("0"); // 冲正次数

		return mReverse;
	}

	/**
	 * 将Map数据转化为脚本通知对象
	 * 
	 * @param hashmap
	 * @return
	 */
	public ScriptNotity MapObjToScriptNotity(Map<String, String> dataMap, Map<String, String> resMap,
			String scriptNotityF55) {
		ScriptNotity scriptNotity = new ScriptNotity();
		scriptNotity.setPriaccount(dataMap.get("priaccount"));
		scriptNotity.setTransprocode(dataMap.get("transprocode"));
		scriptNotity.setTransamount(dataMap.get("transamount"));
		scriptNotity.setSystraceno(dataMap.get("systraceno"));
		scriptNotity.setTranslocaltime(dataMap.get("translocaltime"));
		scriptNotity.setTranslocaldate(resMap.get("translocaldate"));
		scriptNotity.setExpireddate(dataMap.get("expireddate"));
		scriptNotity.setEntrymode(dataMap.get("entrymode")); // F22
		scriptNotity.setSeqnumber(dataMap.get("seqnumber")); // F23
		scriptNotity.setConditionmode(dataMap.get("conditionmode"));
		scriptNotity.setReserve2(resMap.get("receivemark")); // F32
		scriptNotity.setRefernumber(resMap.get("refernumber")); // F37
		scriptNotity.setIdrespcode(resMap.get("idrespcode"));
		scriptNotity.setTerminalid(dataMap.get("terminalid"));
		scriptNotity.setAcceptoridcode(dataMap.get("acceptoridcode"));
		scriptNotity.setTranscurrcode(dataMap.get("transcurrcode"));
		scriptNotity.setIcdata(scriptNotityF55);
		scriptNotity.setLoadparams(loadparams); // 脚本通知中60域需要 60.3、60.4子域
		scriptNotity.setBatchbillno(dataMap.get("batchbillno"));
		scriptNotity.setReversetimes("0"); // 重发次数
		return scriptNotity;
	}

	/**
	 * 将Map数据转化为交易对象 modify by chenkehui for 8583jar-ic
	 * 
	 * @param hashmap
	 * @return
	 */
	public TransRecord MapObjToTransBean(Map<String, String> dataobj, Map<String, String> resobj) {
		TransRecord record = null;
		try {
			record = new TransRecord();
			String id = resobj.get("id");
			if (id != null && !"".equals(id)) {
				record.setId(Integer.parseInt(resobj.get("id")));
			} else {
				id = dataobj.get("id");
				if (id != null && !"".equals(id)) {
					record.setId(Integer.parseInt(dataobj.get("id")));
				}
			}

			record.setPriaccount(
					resobj.get("priaccount") != null ? resobj.get("priaccount") : dataobj.get("priaccount")); // 主账号
			record.setTransprocode(
					resobj.get("transprocode") != null ? resobj.get("transprocode") : dataobj.get("transprocode")); // 交易处理码

			record.setTransamount(
					resobj.get("transamount") != null ? resobj.get("transamount") : dataobj.get("transamount")); // 交易金额

			record.setSystraceno(
					resobj.get("systraceno") != null ? resobj.get("systraceno") : dataobj.get("systraceno")); // pos流水号
			record.setTranslocaltime(resobj.get("translocaltime") != null ? resobj.get("translocaltime")
					: dataobj.get("translocaltime")); // 时间
			record.setTranslocaldate(resobj.get("translocaldate") != null ? resobj.get("translocaldate")
					: dataobj.get("translocaldate")); // 日期
			record.setExpireddate(
					resobj.get("expireddate") != null ? resobj.get("expireddate") : dataobj.get("expireddate")); // 卡有效期（14域）
			record.setEntrymode(resobj.get("entrymode") != null ? resobj.get("entrymode") : dataobj.get("entrymode")); // POS输入方式(22域)
			record.setSeqnumber(resobj.get("seqnumber") != null ? resobj.get("seqnumber") : dataobj.get("seqnumber"));
			record.setConditionmode(
					resobj.get("conditionmode") != null ? resobj.get("conditionmode") : dataobj.get("conditionmode"));
			record.setUpdatecode(
					resobj.get("updatecode") != null ? resobj.get("updatecode") : dataobj.get("updatecode"));
			record.setTrack2data(
					resobj.get("track2data") != null ? resobj.get("track2data") : dataobj.get("track2data"));
			record.setTrack3data(
					resobj.get("track3data") != null ? resobj.get("track3data") : dataobj.get("track3data"));
			record.setRefernumber(resobj.get("refernumber") != null
					? new String(HexUtil.hexStringToByte(resobj.get("refernumber"))) : dataobj.get("refernumber"));
			record.setIdrespcode(resobj.get("idrespcode") != null
					? new String(HexUtil.hexStringToByte(resobj.get("idrespcode"))) : dataobj.get("idrespcode"));
			record.setRespcode(resobj.get("respcode") != null
					? new String(HexUtil.hexStringToByte(resobj.get("respcode"))) : dataobj.get("respcode"));
			record.setTerminalid(resobj.get("terminalid") != null
					? new String(HexUtil.hexStringToByte(resobj.get("terminalid"))) : dataobj.get("terminalid"));
			record.setAcceptoridcode(resobj.get("acceptoridcode") != null
					? new String(HexUtil.hexStringToByte(resobj.get("acceptoridcode")))
					: dataobj.get("acceptoridcode"));
			record.setAcceptoridname(resobj.get("acceptoridname") != null
					? new String(HexUtil.hexStringToByte(resobj.get("acceptoridname")), "gbk")
					: dataobj.get("acceptoridname"));
			record.setAddrespkey(
					resobj.get("addrespkey") != null ? resobj.get("addrespkey") : dataobj.get("addrespkey"));
			record.setAdddataword(resobj.get("adddataword") != null
					? new String(HexUtil.hexStringToByte(resobj.get("adddataword")), "gbk")
					: dataobj.get("adddataword"));
			record.setTranscurrcode(resobj.get("transcurrcode") != null
					? new String(HexUtil.hexStringToByte(resobj.get("transcurrcode"))) : dataobj.get("transcurrcode"));
			record.setPindata(resobj.get("pindata") != null ? resobj.get("pindata") : dataobj.get("pindata"));
			record.setSecctrlinfo(
					resobj.get("secctrlinfo") != null ? resobj.get("secctrlinfo") : dataobj.get("secctrlinfo"));
			record.setBalanceamount(resobj.get("balanceamount") != null
					? new String(HexUtil.hexStringToByte(resobj.get("balanceamount"))) : dataobj.get("balanceamount"));
			record.setIcdata(resobj.get("icdata") != null ? resobj.get("icdata") : dataobj.get("icdata"));
			record.setAdddatapri(
					resobj.get("adddatapri") != null ? resobj.get("adddatapri") : dataobj.get("adddatapri"));
			record.setPbocdata(resobj.get("pbocdata") != null ? resobj.get("pbocdata") : dataobj.get("pbocdata"));
			record.setLoadparams(new String(HexUtil.hexStringToByte(dataobj.get("loadparams"))));
			record.setCardholderid(
					resobj.get("cardholderid") != null ? resobj.get("cardholderid") : dataobj.get("cardholderid"));
			record.setBatchbillno(
					resobj.get("batchbillno") != null ? new String(HexUtil.hexStringToByte(resobj.get("batchbillno")))
							: new String(HexUtil.hexStringToByte(dataobj.get("batchbillno"))));
			record.setSettledata(resobj.get("settledata") != null
					? new String(HexUtil.hexStringToByte(resobj.get("settledata")), "gbk") : dataobj.get("settledata"));
			record.setMesauthcode(resobj.get("mesauthcode") != null ? resobj.get("mesauthcode") : null); // 接收拉卡拉前置的结算数据
			record.setStatuscode(
					resobj.get("statuscode") != null ? resobj.get("statuscode") : dataobj.get("statuscode"));
			record.setReversetimes("0");
			record.setReserve1(resobj.get("msg_tp")); // 保留字段1，保存交易应答的消息类型
			record.setReserve2(resobj.get("reserve2") != null ? resobj.get("reserve2") : dataobj.get("reserve2")); // 受理方标识码
			record.setReserve3(dataobj.get("reserve3")); // AAC、ARPC、TC上送报文中F55
			record.setReserve4(resobj.get("reserve4") != null ? resobj.get("reserve4") : dataobj.get("reserve4"));
			record.setReserve5(resobj.get("reserve5") != null ? resobj.get("reserve5") : dataobj.get("reserve5"));
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}
		return record;
	}

	// 将Map数据转化为结算明细表记录对象
	public SettleData MapObjToSettleBean(Map<String, String> dataobj, Map<String, String> resobj) {
		SettleData record = new SettleData();
		record.priaccount = resobj.get("priaccount") != null ? resobj.get("priaccount") : dataobj.get("priaccount");// 卡号
		record.batchbillno = resobj.get("batchbillno") != null
				? new String(HexUtil.hexStringToByte(resobj.get("batchbillno")))
				: new String(HexUtil.hexStringToByte(dataobj.get("batchbillno")));// 批次号、凭证号
		record.transamount = resobj.get("transamount") != null ? resobj.get("transamount") : dataobj.get("transamount");// 金额
		record.idrespcode = resobj.get("idrespcode") != null
				? new String(HexUtil.hexStringToByte(resobj.get("idrespcode"))) : dataobj.get("idrespcode");// 授权号
		record.conditionmode = resobj.get("conditionmode") != null ? resobj.get("conditionmode")
				: dataobj.get("conditionmode");// 服务条件码
		record.transprocode = resobj.get("transprocode") != null ? resobj.get("transprocode")
				: dataobj.get("transprocode");// 交易处理码
		record.reserve1 = resobj.get("msg_tp"); // 保留字段1，保存交易下发的消息类型
		return record;
	}

	// 消费、预授权完成、退货、电子现金圈存中除电子现金撤销交易外，成功时，保存数据到结算明细表
	public void saveSettleRecordToDB(Map<String, String> dataobj, Map<String, String> resobj) {
		SettleData settleData = MapObjToSettleBean(dataobj, resobj);
		settleDataDao.save(settleData);
	}

	// 反交易时，从结算明细表中删除已撤销的交易记录
	public int deleteSettleRecordFromDB(String batchbillno) {
		int ret = 0;
		List<SettleData> sDatas = settleDataDao.getSettleData();
		if (sDatas == null || batchbillno.length() != 18) {
			return -1;
		}
		String oldbillno = batchbillno.substring(12, 18);
		for (SettleData sData : sDatas) {
			if (oldbillno.equals(sData.batchbillno.subSequence(6, 12))) {
				ret = settleDataDao.delete(sData);
			}
		}
		return ret;
	}

	// 公共的：处理44域发散工作密钥（pik、mac）
	private void afterSuc(Map<String, String> resMap) throws Exception {

		String key = resMap.get("addrespkey");
		if (key != null && !"".equals(key)) {
			String pinkeybin = key.substring(0, 128);
			String mackeybin = key.substring(128, 192);
			String pinpadType = mParamConfigDao.get("pinpadType");
			Log.i("ckh", "pinpadType == " + pinpadType);
			if ("0".equals(pinpadType)) {
				Log.i("ckh", "外置密码键盘");
				pinPadDev = new ExPinPadDevJsIfc(NetworkActivity.this, null);
			} else {
				Log.i("ckh", "内置密码键盘");
				pinPadDev = new PinPadDevJsIfc(NetworkActivity.this, null);
			}
			pinPadDev.openDevice();

			pinPadDev.dispersePinKey(pinkeybin);

			pinPadDev.disperseMacKey(mackeybin);

			pinPadDev.closeDevice();
		}
	}

	// 注入脱机PIN专用TMK(索引0x07)和PWK(索引0x1F)，用于脱机PIN输入
	private void loadMkeyAndWkeyForOffPin() throws Exception {
		pinPadDev.loadOffMkeyAndWkey();
	}

	private void dealException(String tip) {
		DialogFactory.showTips(NetworkActivity.this, tip);
		if (WebViewActivity.isCallbyThrid) {
			lklcposActivityManager.removeAllActivityExceptOne(WebViewActivity.class);
		} else {
			int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(NetworkActivity.this, MenuSpaceActivity.class);
				NetworkActivity.this.startActivity(intent);
			}
			lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
			outActivityAnim();
		}
	}

	private int successSignOper(Map<String, String> resMap) {

		// 更新系统时间
		String timeString = resMap.get("translocaltime");
		String dateString = resMap.get("translocaldate");
		if (dateString != null && !"".equals(dateString) && timeString != null && !"".equals(timeString)) {
			setSystemClock(dateString, timeString);
		}

		// 下装参数,批次号，票据号,pos流水号
		String resLoadparams = resultMap.get("loadparams");
		if (resLoadparams != null && "".equals(resLoadparams) && resLoadparams.length() >= 18) {
			hasLoadParams = true; // 标志为true，有下发参数，onStop()中不再保存下发参数
			String posno = resLoadparams.substring(0, 6);
			String billno = resLoadparams.substring(6, 12);
			String batchno = resLoadparams.substring(12, 18);
			log.warn("签到下装批次号：" + batchno);
			Map<String, String> loadMap = new HashMap<String, String>();
			loadMap.put("batchno", batchno);
			loadMap.put("systraceno", String.valueOf(Long.valueOf(posno) - 1));// 读取时，都是+1再用，故先-1存储
			loadMap.put("billno", billno);
			mParamConfigDao.update(loadMap);
		}

		if (!Utility.getSignStatus(NetworkActivity.this)) { // 第一次签到成功
			Utility.setSignStatus(NetworkActivity.this, true); // 设置为签到
			String settleno = mParamConfigDao.get("settlebatchno");
			if (!"".equals(settleno) && settleno != null) {
				mParamConfigDao.update("settlebatchno", ""); // 签到成功，设置为无结算信息
				settleDataDao.deleteOtherBatch(batchno); // 删除非本批次的结算明细信息（因为结算表中有可能包含本批次未结算的脱机交易信息）
			}
		}

		// 28域更新标志处理
		String updatastatus = mParamConfigDao.get("updatastatus");
		if ("".equals(updatastatus)) { // 判断是否更新中断
			mParamConfigDao.update("updatastatus", "4");
			return 4;
		}

		if (!"0".equals(updatastatus)) {
			return Integer.valueOf(updatastatus);
		}

		String updatecode = resMap.get("updatecode"); // 报文返回F28
		if (updatecode == null || "".equals(updatecode)) {
			log.warn("F28 更新标志为空, 不更新");
			return 0;
		}
		log.debug("F28更新标志：" + updatecode);
		mParamConfigDao.update("updatastatus", updatecode);
		return Integer.valueOf(updatecode);
	}

	private boolean setSystemClock(String dateStr, String timeStr) {
		boolean falg = false;
		// String year = mParamConfigDao.get("curyear");
		String year = new SimpleDateFormat("yyyy").format(new Date());
		log.debug("当前年份:" + year);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = null;
		try {
			date = format.parse(year + dateStr + timeStr);
			falg = SystemClock.setCurrentTimeMillis(date.getTime());
		} catch (java.text.ParseException e) {
			e.printStackTrace();
			log.error("", e);
			falg = false;
		}
		try {
			PINPadDevInf pinpad = DeviceFactory.getPINPadDev();
			pinpad.open();
			Long pinpadTime = pinpad.getRTCTime();
			Long curtime = date.getTime();
			log.debug("获取RCT当前时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(pinpad.getRTCTime())));
			if (pinpadTime < curtime) {
				pinpad.setRTCTime(date.getTime());
			}
			log.debug("设置RCT时间之后：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(pinpad.getRTCTime())));
			pinpad.close();
		} catch (Exception e) {
			log.error("设置RTC时间发生异常", e);
			if (e instanceof CentermApiException.IndicationException) {
				int devid = ((CentermApiException.IndicationException) e).getDevId();
				int errorID = ((CentermApiException.IndicationException) e).getEventId();
				log.error("devID : " + devid);
				log.error("errorID : " + errorID);
				log.error("异常退出 : devid: " + Integer.toHexString(devid & 0xFF) + "   errorID: "
						+ Integer.toHexString(errorID & 0xFF));
			}
			falg = false;
		}
		return falg;
	}

	// 处理数据流中传递金额格式
	private void amountToBCD(Map<String, String> dataMap) {
		String amount = dataMap.get("transamount");
		amount = Utility.formatMount(amount);
		dataMap.put("transamount", amount);
	}

	// 处理52域数据
	private void pinDataToBinString(Map<String, String> dataMap) {
		String pinkey = dataMap.get("pindata");
		byte[] pin = HexUtil.hexStringToByte(pinkey); // String to byte[]
		pinkey = pin == null ? null : DataConverter.byteToBinaryString(pin); // byte[]
																				// to
		// binString(二进制字符串)
		dataMap.put("pindata", pinkey);
	}

	private void saveSettleDataToDB(Map<String, String> dataMap, Map<String, String> resultMap) {
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put("settlebatchno", new String(HexUtil.hexStringToByte(dataMap.get("batchbillno"))));
		configMap.put("translocaldate", resultMap.get("translocaldate"));
		configMap.put("translocaltime", resultMap.get("translocaltime"));
		configMap.put("requestSettleData", new String(HexUtil.hexStringToByte(dataMap.get("settledata"))));
		configMap.put("respcode", new String(HexUtil.hexStringToByte(resultMap.get("respcode"))));
		configMap.put("settledata", resultMap.get("settledata") == null ? null
				: new String(HexUtil.hexStringToByte(resultMap.get("settledata"))));

		mParamConfigDao.update(configMap);
	}

	private Handler pbocHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			boolean result = bundle.getBoolean("result");
			String reason = bundle.getString("reason");
			if (Utility.isThatMsg(msg, "msg_rf_pre_progress")) {
				if (result) {
					pbocDev.rfStartProc(dataMap);
				} else {
					DialogFactory.showTips(NetworkActivity.this, reason);
					overTranscation();
				}
			} else if (Utility.isThatMsg(msg, "msg_start_pboc")) { // PBOC完整流程，（startProc）回调
				if (result) {
					byte[] F55taglist = null;
					try {
						switch (CUR_MODE) {
						case SALE_MODE:
							F55taglist = EMVTAG.getLakalaF55UseModeOneForOnlineSale();
							break;
						case AUTH_MODE:
						case INQU_MODE:
							F55taglist = EMVTAG.getLakalaF55UseModeOne();
							break;
						case NON_TRANSFER_MODE:
						case CASH_UP_MODE:
						case TRANSFER_MODE:
							F55taglist = EMVTAG.getLakalaTransferF55Tag();
							break;
						case CASH_UP_VOID_MODE:
							F55taglist = EMVTAG.getLakalaCashValueVoidF55Tag();
							break;
						default:
							F55taglist = EMVTAG.getNullTag();
							break;
						}
					} catch (Exception e) {
						// TODO: handle exception
						log.error("获取F55数据TagList异常", e);
					}
					if ("071".equals(dataMap.get("posInputType"))) {
						pbocDev.rfkernelProc(dataMap);
					} else {
						pbocDev.kernelProc(F55taglist, mTransaction.getDataMap());
					}
				} else {
					DialogFactory.showTips(NetworkActivity.this, reason);
					overTranscation();
				}
			} else if (Utility.isThatMsg(msg, "msg_emv_interaction")) { // 内核请求交互
				if ("getOfflinePin".equals(reason)) { // 导入脱机pin
					interDialog = pbocWiget.InputOffPINDailog(dataMap.get("priaccount"), new PbocWiget.ClickBack() {

						@Override
						public void loadData(String data) {
							// TODO Auto-generated method stub
							log.debug("脱机密码：" + data);
							pbocDev.pinByUser(data);
						}

						@Override
						public void loadDataCancel() {
							// TODO Auto-generated method stub
							log.debug("取消脱机pin输入");
							pbocDev.pinByUser(null); // 取消导入PIN为空
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
			} else if (Utility.isThatMsg(msg, "msg_cashup_kernelProc") || Utility.isThatMsg(msg, "msg_kernel_pboc")) { // kernelProc
																														// 回调
				if (interDialog != null && interDialog.isShowing()) {
					interDialog.dismiss();
					interDialog = null;
				}
				Bundle b = bundle.getBundle("bundle");
				mDialog.dismiss();
				mDialog = null;
				if (result) {
					startTrans(); // 开始发起交易报文
					lableText.setVisibility(View.VISIBLE);
					animImageView.setVisibility(View.VISIBLE);
				} else {
					if (!"".equals(reason))
						DialogFactory.showTips(NetworkActivity.this, reason);
					overTranscation();
				}
			} else if (Utility.isThatMsg(msg, "msg_inputOnlineRespData")) { // PBOC完整流程第三步
				Bundle b = bundle.getBundle("bundle");
				if (result) {
					try {
						pbocDev.completeProc(b, EMVTAG.getNullTag());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						log.error("", e);
					}
				} else { // 执行PBOC第三步，0x85 交易终止
					try {
						pbocDev.closeDev();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						log.error("", e);
					}
					log.debug("NetworkActivity PBOC处理inputOnline接口退出");
					if ("3030".equals(mTransaction.getResultMap().get("respcode"))) {
						mTransaction.getResultMap().put("respcode", null);
						mTransaction.getResultMap().put("Http_rescode", "ERR");
						mTransaction.getResultMap().put("errMsg", "交易拒绝");

						// 平台交易成功，内核拒绝引发冲正
						b.putString("isNeedReverse", "true");
					}
					nextStep(b);
				}
			} else if (Utility.isThatMsg(msg, "msg_comple_pboc")) { // PBOC完整流程第四步
				Bundle b = bundle.getBundle("bundle");
				if (result) {
					// 判断是否有脚本结果
					String scriptExeResponse = bundle.getString("scriptExeResponse");
					String tagDF31 = TlvUtil.tlvToMap(scriptExeResponse).get("DF31");
					log.debug("发卡行脚本结果 Tag DF31：" + tagDF31);
					if (isTransSucess && (CUR_MODE == TRANSFER_MODE || CUR_MODE == NON_TRANSFER_MODE
							|| CUR_MODE == CASH_UP_MODE)) { // 圈存3个交易需验证脚本
						if (tagDF31 == null || "".equals(tagDF31)) { // 无脚本下发
							b.putString("isNeedReverse", "true"); // 引发冲正
							mTransaction.getResultMap().put("respcode", null);
							mTransaction.getResultMap().put("Http_rescode", "ERR");
							mTransaction.getResultMap().put("errMsg", "无发卡行脚本执行结果，交易拒绝");
							nextStep(b);
							return;
						}
					}
					if (resultMap == null) {
						log.warn("resultMap == null");
					}
					String ic55data = resultMap.get("icdata");
					boolean hasScriptData = haveScriptData(ic55data);
					// 保存发卡行脚本结果
					// mod by txb 20141031 返回结果包含有发卡行脚本时时才执行脚本结果上送
					if (isTransSucess && hasScriptData) { // 交易联机成功（指拉卡拉平台F39返回00）
						ScriptNotity record = MapObjToScriptNotity(dataMap, mTransaction.getResultMap(),
								scriptExeResponse);
						mScriptNotitiesDao.save(record);
					}
					// 读取内核数据，F55用法一，用于CT或AAC、ARPC上送
					String f55Data = bundle.getString("F55Data");
					dataMap.put("reserve3", f55Data);

					// 圈存交易需验证脚本是否执行成功，若脚本执行失败，交易则为失败
					String tag95 = TlvUtil.tlvToMap(scriptExeResponse).get("95");
					log.debug("TVR Tag 95：" + tag95);

					int completeResult = bundle.getInt("completeResult");
					if (completeResult == 129 && transferScriptSuc(tag95)) { // 交易接受
						log.debug("交易接受");
						// IC卡交易接受，结算时需上送CT或者ARPC
						// 发卡行认证是否执行成功Tag 95 第5字节b7=1，结算时上送ARPC
						if (!arpcSucess(tag95)) { // ARPC（发卡行认证）执行失败,需上送ARPC
							dataMap.put("statuscode", "AR");
						} else { // 上送TC
							dataMap.put("statuscode", "TC");
						}

						if (CUR_MODE != INQU_MODE) { // 除余额查询之外的完整PBOC流程，都有打印
							getKernelDataForPrint(dataMap); // 读取需要打印的数据
							Utility.setPrintStatus(NetworkActivity.this, "strans"); // 标记有未打印状态
						}
						if (CUR_MODE != AUTH_MODE && CUR_MODE != INQU_MODE) { // 完整PBOC流程中除了预授权其他都需要参与结算
							saveSettleRecordToDB(dataMap, resultMap);
						}

					} else if (completeResult == 130 || completeResult == 133 || !transferScriptSuc(tag95)) { // 交易拒绝
						log.debug("交易拒绝"); // 交易拒绝原因有二：1、内核拒绝，2脚本执行失败（或者不存在脚本）

						// IC卡交易拒绝，结算时需上送AAC
						dataMap.put("statuscode", "AC");

						if (isTransSucess) {
							mTransaction.getResultMap().put("respcode", null);
							mTransaction.getResultMap().put("Http_rescode", "ERR");
							mTransaction.getResultMap().put("errMsg", "交易拒绝");

							// 平台交易成功，内核拒绝引发冲正
							if (completeResult == 130 || completeResult == 133) {
								b.putString("isNeedReverse", "true");
							}
						}
					} else {
						log.error("PBOC流程结束 completeResult == " + completeResult);
					}

					if (isSaveRecord && isTransSucess) { // 保存交易报文成功数据
						TransRecord transRecord = MapObjToTransBean(dataMap, resultMap);
						mTransRecordDao.save(transRecord);
					}
				} else {
					log.error("PBOC流程结束,读取数据异常");
					if (isTransSucess) {
						mTransaction.getResultMap().put("respcode", null);
						mTransaction.getResultMap().put("Http_rescode", "ERR");
						mTransaction.getResultMap().put("errMsg", "读写卡片信息失败");
						b.putString("isNeedReverse", "true");
					}
				}
				nextStep(b);
			}
		}

	};

	private void doIcPboc(Bundle b) {

		int result = b.getBoolean("isOnLine") ? 1 : 0;
		isTransSucess = false;
		String resIcdata = null;
		if (b.getBoolean("isSuc")) {
			resultMap = (Map<String, String>) b.getSerializable("resMap");
			resIcdata = resultMap.get("icdata");
			if ("3030".equals(resultMap.get("respcode"))) {
				isTransSucess = true;
			}
			Log.i("ckh", "返回的55域数据：" + resIcdata + "\n39域：" + resultMap.get("respcode"));
		} else {
			resIcdata = "";
		}
		pbocDev.inputOnlineRespData(b, result, isTransSucess, resIcdata);
	}

	// 获取冲正上送报文中F55数据
	private String getReversalF55() {
		String reversalF55 = null;
		try {
			if (CUR_MODE == SALE_MODE || CUR_MODE == AUTH_MODE) {
				reversalF55 = pbocDev.readEMVData(EMVTAG.getLakalaF55UseModeTwo());
			} else {
				reversalF55 = pbocDev.readEMVData(EMVTAG.getReversalF55Tag());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("读取内核中冲正上送报文中F55数据异常");
			e.printStackTrace();
		}
		return reversalF55;
	}

	// 结束交易流程，返回到主菜单
	private void overTranscation() {
		int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
		if (count == 0) {
			Intent intent = new Intent();
			intent.setClass(this, MenuSpaceActivity.class);
			startActivity(intent);
		}
		lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
	}

	// 判断圈存交易是否脚本执行成功
	private boolean transferScriptSuc(String TVR) {
		if (CUR_MODE == TRANSFER_MODE || CUR_MODE == NON_TRANSFER_MODE || CUR_MODE == CASH_UP_MODE) {
			if (scriptSucess(TVR)) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	// 判断终端脚本是否执行成功
	private boolean scriptSucess(String TVR) {
		if (TVR == null || "".equals(TVR))
			return false;
		String byte5 = DataConverter.byteToBinaryString(DataConverter.hexStringToByte(TVR.substring(8)));
		if ("0".equals(byte5.substring(3, 4))) {
			log.debug("脚本tag 72 执行成功");
			return true;
		} else { // tag72 脚本执行失败
			log.debug("脚本tag 72 执行失败");
			return false;
		}
	}

	// 判断ARPC是否执行失败（发卡行认证）
	private boolean arpcSucess(String TVR) {
		if (TVR == null || "".equals(TVR))
			return false;
		String byte5 = DataConverter.byteToBinaryString(DataConverter.hexStringToByte(TVR.substring(8)));
		if ("0".equals(byte5.substring(1, 2))) {
			log.debug("ARPC认证成功");
			return true;
		} else { // ARPC执行失败,需上送ARPC
			log.debug("ARPC认证失败");
			return false;
		}
	}

	// 读取用于打印凭条所要求的内核数据
	private void getKernelDataForPrint(Map<String, String> map) {
		try {
			String printData = pbocDev.readEMVData(EMVTAG.getkernelDataForPrint());
			String arqc = dataMap.get("arqc");
			if (!"".equals(arqc) && arqc != null) {
				log.info("arqc tag替换前：" + arqc);
				arqc = arqc.replaceFirst("9F26", "9F99");
				log.info("arqc tag替换后：" + arqc);
				printData = printData + arqc;
			}
			map.put("reserve4", printData); // 将要打印的数据存放在预留字段4中
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("", e);
		}
	}

	/**
	 * 判断有没有55域脚本结果数据
	 * 
	 * @return
	 */
	public boolean haveScriptData(String F55Data) {
		try {
			Map<String, String> icMap = TlvUtil.tlvToMap(F55Data);
			String tag71 = icMap.get("71");
			String tag72 = icMap.get("72");
			if ((null == tag71 || "".equals(tag71)) && (null == tag72 || "".equals(tag72))) {
				log.debug("no script");
				return false;
			}
			return true;
		} catch (Exception e) {
			log.error("判断脚本出现异常", e);
		}
		return false;
	}

	/**
	 * 旋转动画
	 * 
	 * @param view
	 */
	private void rotate(View view) {
		Animation a = new RotateAnimation(0, 360000 * 5, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		a.setDuration(5000000);
		// a.setInterpolator(new AccelerateInterpolator());
		// LinearInterpolator lin = new LinearInterpolator();
		a.setInterpolator(new LinearInterpolator());
		a.setFillAfter(true);
		view.startAnimation(a);
	}
}
