package com.centerm.lklcpos.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.util.EncodingUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.entity.ParamConfig;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.OneShotResultActivity;
import com.centerm.lklcpos.activity.OneShotWelcomeActivity;
import com.centerm.lklcpos.activity.TradeBaseActivity;
import com.centerm.lklcpos.util.MD5Util;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.ScreenTouchListener;
import com.centerm.mid.inf.SystemDevInf;
import com.centerm.mid.util.ScreenOnTouchListsner;
import com.lkl.farmerwithdrawals.R;

/*
 * 超时服务
 * 2013-06-03
 */
public class StandbyService extends Service {
	public String ACTION_FINISH = "com.centerm.androidlklpos.finish";
	public static boolean isStandByStatus = false;
	public static boolean isStopStandby = false;
	public static boolean isActive = false;
	public static boolean isOneShotStatus = false; // 处于一键激活界面，停止超时
	private static final int START_STANDBY = 0x11;
	private static final int TRANS_TIMEOUT = 0x12;
	private static Logger log = Logger.getLogger(StandbyService.class);
	public static long time_out;
	private static Timer timer;
	private static TimerTask timerTask;
	private ScreenTouchListener scrrenTouch;
	private static Handler mHandler;
	private ParamConfigDao mParamConfigDao;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("ckh", "StandbyService onCreate()...");
		Log.i("ckh", "StandbyService new DB");
		isActive = true;
		mParamConfigDao = new ParamConfigDaoImpl(this);
		time_out = getTimeout() * 1000;// 默认60秒，要从数据库获取实际值
		// 关闭多点触摸
		try {
			SystemDevInf sysdef = DeviceFactory.getSystemDev();
			sysdef.closeMultiTouch();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("多点触摸关闭异常...........................", e);
		}

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case START_STANDBY:
					Intent mIntent = new Intent();
					if (!"1".equals(mParamConfigDao.get("enabled"))) { // 未激活
						mIntent.setClass(StandbyService.this, OneShotWelcomeActivity.class);
						mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						PendingIntent intent = PendingIntent.getActivity(StandbyService.this, 0, mIntent,
								PendingIntent.FLAG_CANCEL_CURRENT);
						try {
							intent.send();
						} catch (CanceledException e) {
							log.warn("Service 启动Activity 异常", e);
							e.printStackTrace();
						}
					}
					// mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					else {
						// isStandByStatus = true;
						// mIntent.setAction("com.lkl.farmer.Standby");
					}
					// startActivity(mIntent);
					// LklcposActivityManager.getActivityManager().removeAllActivityExceptOne(StandbyActivity.class);
					break;
				case TRANS_TIMEOUT:
					Intent transdialogIntent = new Intent();
					transdialogIntent.setAction("com.lkl.farmer.timeoutdialog");
					transdialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(transdialogIntent);
					break;
				default:
					break;
				}
			}
		};
		// 屏幕监听器
		try {
			scrrenTouch = DeviceFactory.getScreenTouchListener();
		} catch (Exception e) {
			log.warn("初始化屏幕触控", e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 启动屏幕监听
		ScanTouchEventThread mScanTouchEventThread = new ScanTouchEventThread();
		mScanTouchEventThread.start();

		// 注册一个接收退出应用程序的广播接受器
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_FINISH);// com.centerm.androidlklpos.finish
		registerReceiver(mFinishReceiver, filter);

		Thread updataPosParamThread = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				long begin = System.currentTimeMillis();
				posparamToSqlite();
				sysparamToSqlite();
				// readYear();
				log.debug("应用启动同步参数耗时:" + (System.currentTimeMillis() - begin) + "ms");
			}

		};
		updataPosParamThread.start();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		log.info("StandbyService服务 onDestroy()...");
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		log.info("StandbyService服务 onStart()...");
		super.onStart(intent, startId);
	}

	public static synchronized void startScreenTimer() {

		if (!TradeBaseActivity.isTransStatus) { // 待机超时设置为0或空时，无待机功能处理
			if (time_out == 0) {
				return;
			}
		}

		if (TradeBaseActivity.isHttp || isStandByStatus || isStopStandby || isOneShotStatus) {
			log.debug("isHttp =" + TradeBaseActivity.isHttp + "| isStandByStatus =" + isStandByStatus
					+ "| isStopStandby =" + isStopStandby + "| isOneShotStatus =" + isOneShotStatus);
			return;
		}
		if (timer != null) {
			timer.cancel();
			timerTask.cancel();
			timer = null;
			timerTask = null;
		}
		timer = new Timer();

		if (!TradeBaseActivity.isTransStatus) {// 判断是否为交易状态
			timerTask = new TimerTask() {
				@Override
				public void run() {
					Message message = Message.obtain();
					message.what = START_STANDBY;
					mHandler.sendMessage(message);
				}
			};
			timer.schedule(timerTask, time_out);
		} else {
			timerTask = new TimerTask() {
				@Override
				public void run() {
					Message message = Message.obtain();
					message.what = TRANS_TIMEOUT;
					mHandler.sendMessage(message);
				}
			};
			timer.schedule(timerTask, 50 * 1000); // 交易弹出dialog时间，暂为50秒
		}
	}

	public static synchronized void stopScreenTimer() {
		if (timer != null) {
			timer.cancel();
			timerTask.cancel();
		}
		timer = null;
		timerTask = null;
	}

	public static void onOperate() {
		if (StandbyService.isActive) {
			stopScreenTimer();
			startScreenTimer(); // note xrh 20130403
		}
	}

	private class ScanTouchEventThread extends Thread {
		@Override
		public void run() {
			try {
				scrrenTouch.setOnTouchListsner(new ScreenOnTouchListsner() {
					@Override
					public void onTouchEvent(int eventid) {
						if (OneShotResultActivity.isActive) { // 开通未确认状态
							log.debug("处在开通未确认状态！");
							return;
						}
						onOperate();
					}
				});
			} catch (Exception e1) {
				log.error("register touch listener failed.....", e1);
			}
		}
	}

	private int getTimeout() {
		int timeout = 60;
		try {
			String standbytimeout = mParamConfigDao.get("standbytimeout");
			if ("".equals(standbytimeout) || standbytimeout == null) {
				timeout = 0;
			} else {
				timeout = Integer.parseInt(standbytimeout);
			}
		} catch (NumberFormatException e) {
			log.warn("从数据库获取待机超时时间失败,返回默认值[" + timeout + "]", e);
			timeout = 0;
		}
		return timeout;
	}

	private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ACTION_FINISH.equals(intent.getAction())) {
				unregisterReceiver(this);
				try {
					scrrenTouch.close();
				} catch (Exception e) {
					log.warn("关闭屏幕触控", e);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				log.info("待机服务接收到结束广播...");
				stopScreenTimer();
				stopSelf();
				isActive = false;
				System.exit(0);
			}
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void readYear() {
		File inifile = null;
		String res = "";
		try {
			inifile = new File("data/public/year.ini");
			FileInputStream fin = new FileInputStream(inifile);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			log.info("读取出来年份:" + res);
			fin.close();
		} catch (Exception e) {
			// TODO: handle exception
			res = "2014";
			log.error("读取年份异常");
		}

		if (mParamConfigDao.isExist("curyear")) {
			mParamConfigDao.update("curyear", res);
		} else {
			mParamConfigDao.save("curyear", res);
		}
	}

	public void posparamToSqlite() {
		long startime = System.currentTimeMillis();
		File xmlfile = null;
		try {
			xmlfile = new File(getResources().getString(R.string.baseSystemUri) + "conf/posparam.xml");
			Log.i("ckh", "xmlfile.exists() == " + xmlfile.exists());
			if (!xmlfile.exists()) {
				log.warn("posparam.xml不存在,参数不同步...");
				Log.i("ckh", "!xmlfile.exists()...");
				if ("1".equals(mParamConfigDao.get("posparamsymbol"))) {
					Log.i("ckh", "dialog");
					mParamConfigDao.update("posparamsymbol", "0");
					Intent intent = new Intent();
					intent.setAction("com.centerm.lklcpos.posparam");
					sendBroadcast(intent);
				}
				// createXml();
				return;
			}
		} catch (Exception e) {
			log.error("[posparamToSqlite], exception ...", e);
			// TODO: handle exception
			return;
		}

		ParamConfigDaoImpl paramConfigDao = new ParamConfigDaoImpl(this);
		String md5 = paramConfigDao.get("paramXmlMd5"); // 获取数据库中保存的MD5值

		if (md5 != null) {
			try {
				String md5String = MD5Util.getFileMD5(xmlfile);
				if (md5.equals(md5String)) {
					log.info("[posparamToSqlite],文件MD5值与数据库一致,不需要同步");
					return;
				}
			} catch (IOException e) {
				log.error("文件MD5值计算异常...", e);
				e.printStackTrace();
			}
		}
		Log.i("ckh", "开始同步配置到sqlite数据库 .....");
		log.info("开始同步配置到sqlite数据库 .....");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream ins = null;
			ins = new FileInputStream(new File(getResources().getString(R.string.baseSystemUri) + "conf/posparam.xml"));
			Document doc = builder.parse(ins);
			Node root = doc.getDocumentElement();

			List<ParamConfig> list = new ArrayList<ParamConfig>();
			if (root.hasChildNodes()) {
				NodeList rlist = root.getChildNodes();

				for (int i = 0, j = rlist.getLength(); i < j; i++) {
					Node childNode = rlist.item(i);
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						String key = childNode.getNodeName();
						String value = childNode.getTextContent();
						log.debug("[posparamToSqlite],开始同步数据 key =" + key + "&& value =" + value);
						if ("termid".equals(key) || "merid".equals(key) || "mchntname".equals(key)
								|| "dealtimeout".equals(key) || "reconntimes".equals(key) || "tpdu".equals(key)
								|| "batchno".equals(key) || "billno".equals(key) || "transIp".equals(key)
								|| "caIp".equals(key) || "pinpadType".equals(key) || "fid".equals(key)
								|| "MerchantSev".equals(key) || "MasterKeySev".equals(key)) {

							ParamConfig config = new ParamConfig();
							config.setTagname(key);
							config.setTagval(value);
							list.add(config);

							// if("200021".equals(paramConfigDao.get("fid"))){
							// //测试
							if ("test".equals(paramConfigDao.get("env"))) { // 测试
								if ("transIp".equals(key)) {
									list.add(new ParamConfig("test_3gapn_tranUrl", value));
								}
								if ("caIp".equals(key)) {
									list.add(new ParamConfig("test_3gapn_certUrl", value));
								}
								if ("MerchantSev".equals(key)) {
									list.add(new ParamConfig("test_3gapn_mercUrl", value));
								}
							} else {
								if ("transIp".equals(key)) {
									list.add(new ParamConfig("produce_3gapn_tranUrl", value));
								}
								if ("caIp".equals(key)) {
									list.add(new ParamConfig("produce_3gapn_certUrl", value));
								}
								if ("MerchantSev".equals(key)) {
									list.add(new ParamConfig("proc_3gapn_mercUrl", value));
								}
							}
							// if (paramConfigDao.isExist(key)) {
							// paramConfigDao.update(key, value);
							// } else {
							// paramConfigDao.save(key, value);
							// }
						}
					}
				}
			}

			int num = paramConfigDao.syncXmlUpdate(list);
			log.info("同步XML参数成功数量 = " + num);

			if (ins != null) {
				ins.close();
				ins = null;
			}

			try {
				String fileMd5 = MD5Util.getFileMD5(xmlfile);
				int saveResult = 0;
				if (paramConfigDao.isExist("paramXmlMd5")) {
					saveResult = paramConfigDao.update("paramXmlMd5", fileMd5);
				} else {
					saveResult = paramConfigDao.save("paramXmlMd5", fileMd5);
				}
				log.debug("[posparamToSqlite] , saveResult=[" + saveResult + "] | 更新MD5值 =[" + fileMd5 + "]");
			} catch (IOException e1) {
				log.error("计算posparam.xml文件MD5值失败..");
			}

			Log.i("ckh", "同步配置到sqlite数据库结束 .....");
			log.info("[posparamToSqlite] ,同步posparam.xml耗时：" + (System.currentTimeMillis() - startime));
		} catch (Exception e) {
			log.error("同步配置到sqlite数据库发生异常.....", e);
			e.printStackTrace();
		}
	}

	public void sysparamToSqlite() {
		long startime = System.currentTimeMillis();
		InputStream ins = null;

		try {
			ins = getResources().getAssets().open("conf/sysparam.xml");
			if (ins == null) {
				log.debug("sysparam is not exists ...");
				return;
			}
		} catch (IOException e2) {
			log.error("[sysparamToSqlite], 获取conf/sysparam.xml 异常 ", e2);
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		ParamConfigDaoImpl paramConfigDao = new ParamConfigDaoImpl(this);
		String md5 = paramConfigDao.get("sysparamXmlMd5"); // 获取数据库中保存的MD5值

		if (md5 != null) {
			try {
				String md5String = MD5Util.getInputStreamMD5(ins);

				if (ins != null) {
					ins.close();
					ins = null;
				}

				if (md5.equals(md5String)) {
					log.info("[sysparamToSqlite],文件MD5值与数据库一致,不需要同步");
					return;
				}
			} catch (IOException e) {
				log.error("[sysparamToSqlite],获取MD5异常", e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Log.i("ckh", "开始同步配置到sqlite数据库 .....");
		log.info("开始同步配置到sqlite数据库 .....");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			ins = getResources().getAssets().open("conf/sysparam.xml");
			Document doc = builder.parse(ins);
			Node root = doc.getDocumentElement();

			List<ParamConfig> list = new ArrayList<ParamConfig>();
			if (root.hasChildNodes()) {
				NodeList rlist = root.getChildNodes();

				for (int i = 0, j = rlist.getLength(); i < j; i++) {
					Node childNode = rlist.item(i);
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						String key = childNode.getNodeName();
						String value = childNode.getTextContent();
						log.debug("[sysparamToSqlite],开始同步数据 key =" + key + "&& value =" + value);
						if ("dealtimeout".equals(key) || "logParentpath".equals(key) || "standbytimeout".equals(key)
								|| "logpath".equals(key) || "rootLogLevel".equals(key) || "islogFile".equals(key)
								|| "logLevel".equals(key) || "swipecard_timeout".equals(key)
								|| "pinpad_timeout".equals(key) || "pswdlength".equals(key) || "httpEncode".equals(key)
								|| "printAppVersionId".equals(key) || "tmkKeyId_setOff".equals(key)) {

							ParamConfig config = new ParamConfig();
							config.setTagname(key);
							config.setTagval(value);
							list.add(config);

							// if (paramConfigDao.isExist(key)) {
							// paramConfigDao.update(key, value);
							// } else {
							// paramConfigDao.save(key, value);
							// }
						}
					}
				}
			}

			int num = paramConfigDao.syncXmlUpdate(list);
			log.info("同步XML参数成功数量 = " + num);

			try {
				if (ins != null) {
					ins.close();
					ins = null;
				}
				ins = getResources().getAssets().open("conf/sysparam.xml");

			} catch (IOException e2) {
				log.error("[sysparamToSqlite], 获取conf/sysparam.xml 异常 ", e2);
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				String fileMd5 = MD5Util.getInputStreamMD5(ins);
				if (ins != null) {
					ins.close();
					ins = null;
				}
				int saveResult = 0;
				if (paramConfigDao.isExist("sysparamXmlMd5")) {
					saveResult = paramConfigDao.update("sysparamXmlMd5", fileMd5);
				} else {
					saveResult = paramConfigDao.save("sysparamXmlMd5", fileMd5);
				}
				log.debug("[sysparamToSqlite] , saveResult=[" + saveResult + "] | 更新MD5值 =[" + fileMd5 + "]");

			} catch (IOException e1) {
				log.error("计算sysparam.xml文件MD5值失败..");
			}

			Log.i("ckh", "同步配置到sqlite数据库结束 .....");
			log.info("同步sysparam.xml耗时：" + (System.currentTimeMillis() - startime));
		} catch (Exception e) {
			log.error("同步配置到sqlite数据库发生异常.....", e);
			e.printStackTrace();
		}
	}

	// 停止待机
	public static void stopStandby() {
		isStopStandby = true;
		log.info("待机服务暂停...");
		onOperate();

	}

	// 开启待机
	public static void startStandby() {
		isStopStandby = false;
		log.info("待机服务重启...");
		onOperate();

	}
}
