package com.centerm.lklcpos.deviceinterface;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import android.util.Log;

import com.centerm.debug.util.HexUtil;
import com.centerm.mid.bean.ExternalPINpadUtil;
import com.centerm.mid.bean.ExternalPINpadUtil.ExternalPINPadVersion;
import com.centerm.mid.bean.SecurityUtil;
import com.centerm.mid.exception.CentermApiException.IndicationException;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.ExternalPINPadInf;
import com.centerm.mid.util.M3HexUtil;

/**
 * 根据业务，对基于ct-mid 的外置密码键盘API调用进行二次封装
 * 
 * @author Linpeng 2013-04-24
 * 
 */
public class ExPinPadUtil {

	private static final String TAG = "ExPinPadUtil";

	private final Logger logger = Logger.getLogger(ExPinPadUtil.class);

	/**
	 * ExternalPINPadInf 接口对象
	 */
	private static ExternalPINPadInf externalPadDev = null;

	/**
	 * 操作超时时间， 默认3秒
	 */
	private static int tmo = 3;

	/**
	 * 随机数
	 */
	private static byte[] random = null;

	/**
	 * TMK索引，默认值为0x00;
	 */
	private static byte mkey_id = (byte) 0x00;

	@SuppressWarnings("unused")
	private static final class C {

		public static final byte[] SK_UID = M3HexUtil.hexStringToByte("C97FFFC809D3010F6ACE8164755F3D81");
		public static final byte[] SK_SKEY = M3HexUtil.hexStringToByte("62778D97AE188039AFAE79A347CFE059");
		public static final byte[] SK_DKEY = M3HexUtil.hexStringToByte("534A24FD187915B597BE70F70319F0D4");
		public static final byte[] SK_TKEY = M3HexUtil.hexStringToByte("E1327F971705F7AE74BCB7049054D87D");
		public static final byte[] SK_PSW = M3HexUtil.hexStringToByte("6261313834633937");
		public static final byte[] SAK = M3HexUtil.hexStringToByte("881070B02FD7BD5CCFF9DA61220AF68B");

		public static final byte DIRECTORY_ZERO = (byte) 0x00;
		public static final byte DIRECTORY_ONE = (byte) 0x01;

		// public static final byte MKEY_ID = (byte) 0x0;
		public static final byte MAK_ID = (byte) 0x1;
		public static final byte PIK_ID = (byte) 0x1;
		public static final byte TDK_ID = (byte) 0x1;
		public static final byte OFFPIK_ID = (byte) 0x1C; // PIN密钥索引值范围0x00~0x1C
															// @20131227

		public static final byte DISP_MAK_MODE = (byte) (0x80 | 0x10 | 0x01);
		public static final byte DISP_PIK_MODE = (byte) (0x80 | 0x10 | 0x02);
		public static final byte DISP_TDK_MODE = (byte) (0x80 | 0x10 | 0x03);

		public static final byte[] USE_NO = new byte[] { (byte) 0xFF, (byte) 0xFF };

		public static final int DISP_MAK_FLAG = 1;
		public static final int DISP_PIK_FLAG = 2;
		public static final int DISP_TDK_FLAG = 3;
		public static final int DISP_OFFPIK_FLAG = 4;

		/**
		 * PIN输入的最小长度和最大长度
		 */
		public static final int PIN_MIN_LEN = 4;
		public static final int PIN_MAX_LEN = 8;

		/**
		 * 银行卡账号最小和最大长度限制
		 */
		public static final int CARDNO_MIN_LEN = 16;
		public static final int CARDNO_MAX_LEN = 19;

		public static final byte MAC_ALG_ANSI_X9_19 = (byte) (0x00 | 0x02); // X9.19算法，后补80
		public static final byte MAC_ALG_UNIPAY_ECB = (byte) (0x00 | 0x03); // 银联ECB算法

		public static final Map<Integer, String> ERR_LIST = new HashMap<Integer, String>() {

			private static final long serialVersionUID = 606835805123902960L;

			{
				put(0, "无错误");
				put(1, "PED 锁死");
				put(2, "不识别的指令");
				put(3, "参数错误");
				put(4, "目录错误");
				put(5, "AuthCode 错误");
				put(6, "重复设置 UID");
				put(7, "重复设置 PSW");
				put(8, "重复设置主密钥");
				put(9, "重复设置认证密钥");
				put(10, "认证失败");
				put(11, "工作密钥无效");
				put(12, "主密钥无效");
				put(13, "认证密钥无效");
				put(14, "两次输入的 PIN 不一致");
				put(15, "输入密码为空");
				put(16, "操作太频繁");
				put(17, "密钥非法（密码键盘内存在相同密钥）");
				put(255, "未知异常");
			}
		};
	}

	/**
	 * 构造函数
	 * 
	 * @param mkey_id
	 *            TMK索引[in]
	 * @throws Exception
	 */
	public ExPinPadUtil(final byte mkey_id) throws Exception {

		init(mkey_id);
	}

	/**
	 * 
	 * @param mkey_id
	 *            TMK索引[in]
	 * @param tmo
	 *            操作超时时间[in]
	 * @throws Exception
	 */
	public ExPinPadUtil(final byte mkey_id, final int tmo) throws Exception {

		init(mkey_id);

		ExPinPadUtil.tmo = tmo;
	}

	/**
	 * 
	 * @param mkey_id
	 *            TMK索引[in]
	 * @throws Exception
	 */
	public void init(final byte mkey_id) throws Exception {

		logger.debug("init() start.mkey_id=[" + mkey_id + "]");

		if (ExPinPadUtil.externalPadDev != null) {
			Log.d(TAG, "ExPinPadUtil.externalPadDev is not null!");

		} else {

			// TMK索引的范围是0~9
			if (mkey_id < (byte) 0x0 || mkey_id >= (byte) 0xA) {
				throw new Exception("mkey_id[" + mkey_id + "]is invalid!");
			}

			ExecutorService executor = Executors.newSingleThreadExecutor();
			FutureTask<Void> future = (FutureTask<Void>) executor.submit(new Callable<Void>() {

				public Void call() throws Exception {

					// 获取ExternalPINPadInf 对象
					ExPinPadUtil.externalPadDev = DeviceFactory.getExternalPINPadDev();
					ExPinPadUtil.externalPadDev.open();
					// 选择目录
					ExPinPadUtil.externalPadDev.selectDirectory(ExPinPadUtil.C.DIRECTORY_ONE);
					// 取随机数
					ExPinPadUtil.random = externalPadDev.getRandom();

					// TMK索引
					ExPinPadUtil.mkey_id = mkey_id;

					return null;
				}
			});
			executor.execute(future);

			try {

				future.get(ExPinPadUtil.tmo, TimeUnit.SECONDS);

			} catch (InterruptedException e) {

				logger.error("init() catch InterruptedException.");

				future.cancel(true);
				this.release();

				throw e;

			} catch (ExecutionException e) {

				logger.error("init() catch ExecutionException.");

				future.cancel(true);

				throw e;

			} catch (TimeoutException e) {

				logger.error("init() catch TimeoutException. tmo=" + ExPinPadUtil.tmo);

				future.cancel(true);
				this.release();

				throw e;

			} catch (Exception e) {
				logger.error("init() catch Exception.");

				future.cancel(true);

				throw e;

			} finally {

				executor.shutdown();

			}
		}

		Log.v(TAG, "random :");
		Log.v(TAG, ExPinPadUtil.printLog(ExPinPadUtil.random));
		Log.v(TAG, "mkey_id:[" + ExPinPadUtil.mkey_id + "]");

		logger.debug("init() end.");

		return;
	}

	public void release() {

		logger.debug("release() start.");

		try {
			ExPinPadUtil.externalPadDev.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ExPinPadUtil.externalPadDev = null;
		ExPinPadUtil.random = null;
		ExPinPadUtil.mkey_id = (byte) 0x0;

		logger.debug("release() end.");

		return;
	}

	/**
	 * 取消PIN输入
	 * 
	 * @throws Exception
	 */
	public void cancelGetPin() throws Exception {

		logger.debug("cancelGetPin() start.");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<Void> future = (FutureTask<Void>) executor.submit(new Callable<Void>() {

			public Void call() throws Exception {

				ExPinPadUtil.externalPadDev.cancelGetPin();

				return null;
			}
		});
		executor.execute(future);

		try {

			future.get(ExPinPadUtil.tmo, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			logger.error("cancelGetPin() catch InterruptedException.");

			future.cancel(true);
			this.release();

			throw e;

		} catch (ExecutionException e) {
			logger.error("cancelGetPin() catch ExecutionException.");

			future.cancel(true);

			throw e;

		} catch (TimeoutException e) {
			logger.error("cancelGetPin() catch TimeoutException.tmo=" + ExPinPadUtil.tmo);

			future.cancel(true);
			this.release();

			throw e;

		} catch (Exception e) {
			logger.error("cancelGetPin() catch Exception.");

			future.cancel(true);

			throw e;

		} finally {

			executor.shutdown();

		}

		logger.debug("cancelGetPin() end.");

		return;
	}

	/**
	 * 获取密码键盘信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public ExternalPINPadVersion getVersion() throws Exception {

		ExternalPINPadVersion version = null;

		logger.debug("getVersion() start.");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<ExternalPINPadVersion> future = (FutureTask<ExternalPINPadVersion>) executor
				.submit(new Callable<ExternalPINPadVersion>() {

					public ExternalPINPadVersion call() throws Exception {

						return ExPinPadUtil.externalPadDev.getVersion();

					}

				});
		executor.execute(future);

		try {

			version = future.get(ExPinPadUtil.tmo, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			logger.error("getVersion() catch InterruptedException.");

			future.cancel(true);
			this.release();

			throw e;

		} catch (ExecutionException e) {
			logger.error("getVersion() catch ExecutionException.");

			future.cancel(true);

			throw e;

		} catch (TimeoutException e) {
			logger.error("getVersion() catch TimeoutException.tmo=" + ExPinPadUtil.tmo);

			future.cancel(true);
			this.release();

			throw e;

		} catch (Exception e) {
			logger.error("getVersion() catch Exception.");

			future.cancel(true);

			throw e;

		} finally {

			executor.shutdown();

		}

		logger.debug("getVersion() end.");

		return version;
	}

	/**
	 * 发散PIN/MAC/DATA加密的工作密钥
	 * 
	 * @param wkey
	 *            工作密钥密文[in]
	 * @param dispWkeyFlag
	 *            发散工作密钥类型[in]
	 * @throws Exception
	 */
	private void disperseWkey(final byte[] wkey, final int dispWkeyFlag) throws Exception {

		logger.debug("disperseWkey() start.dispWkeyFlag=[" + dispWkeyFlag + "]");

		Log.v(TAG, "wkey:");
		Log.v(TAG, ExPinPadUtil.printLog(wkey));

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<Void> future = (FutureTask<Void>) executor.submit(new Callable<Void>() {

			public Void call() throws Exception {

				byte mKeyID = ExPinPadUtil.mkey_id;
				byte wKeyID;
				byte mode;
				byte[] useNo = ExPinPadUtil.C.USE_NO;
				Log.d("txb>>>", "密钥值为" + HexUtil.bcd2str(wkey));
				if (dispWkeyFlag == ExPinPadUtil.C.DISP_MAK_FLAG) {
					wKeyID = ExPinPadUtil.C.MAK_ID;
					mode = ExPinPadUtil.C.DISP_MAK_MODE;
					Log.d("txb>>>>>", "下发MAK");
				} else if (dispWkeyFlag == ExPinPadUtil.C.DISP_PIK_FLAG) {
					wKeyID = ExPinPadUtil.C.PIK_ID;
					mode = ExPinPadUtil.C.DISP_PIK_MODE;
					Log.d("txb>>>>>", "下发PIK");
				} else if (dispWkeyFlag == ExPinPadUtil.C.DISP_TDK_FLAG) {
					wKeyID = ExPinPadUtil.C.TDK_ID;
					mode = ExPinPadUtil.C.DISP_TDK_MODE;
					Log.d("txb>>>>>", "下发TDK");
				} else if (dispWkeyFlag == ExPinPadUtil.C.DISP_OFFPIK_FLAG) {
					wKeyID = ExPinPadUtil.C.OFFPIK_ID;
					mode = ExPinPadUtil.C.DISP_PIK_MODE;
					Log.d("txb>>>>>", "下发脱机PIN");
				} else {
					String errMsg = TAG + "|The dispWkeyFlag[" + dispWkeyFlag + "] in disperseWkey() is illegal!!!";

					logger.debug(errMsg);

					throw new Exception(errMsg);
				}

				// 设置 authcode
				ByteArrayOutputStream datas = new ByteArrayOutputStream();
				datas.write(mKeyID);
				datas.write(wKeyID);
				datas.write(mode);
				datas.write(wkey);
				datas.write(useNo);
				byte[] authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SK_DKEY, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				Log.v(TAG, "mKeyID:");
				Log.v(TAG, " " + mKeyID);
				Log.v(TAG, "random:");
				Log.v(TAG, ExPinPadUtil.printLog(random));
				Log.v(TAG, "authCode:");
				Log.v(TAG, ExPinPadUtil.printLog(authCode));

				// 发散工作密钥
				ExPinPadUtil.externalPadDev.disperseWkey(mKeyID, wKeyID, mode, wkey, useNo, authCode);
				Log.d("txb>>>>", "密钥下发成功");

				return null;
			}
		});
		executor.execute(future);

		try {

			future.get(ExPinPadUtil.tmo, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			logger.error("disperseWkey() catch InterruptedException.");

			future.cancel(true);
			this.release();

			throw e;

		} catch (ExecutionException e) {
			logger.error("disperseWkey() catch ExecutionException.");

			future.cancel(true);

			throw e;

		} catch (TimeoutException e) {
			logger.error("disperseWkey() catch TimeoutException.tmo=" + ExPinPadUtil.tmo);

			future.cancel(true);
			this.release();

			throw e;

		} catch (Exception e) {
			logger.error("disperseWkey() catch Exception.");
			if (e instanceof IndicationException) {
				IndicationException indicationException = (IndicationException) e;
				Log.e("LogActivity", "[" + indicationException.getDevId() + "|" + indicationException.getEventId() + ":"
						+ ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");
			}

			future.cancel(true);

			throw e;

		} finally {

			executor.shutdown();

		}

		logger.debug("disperseWkey() end.");

		return;
	}

	/**
	 * 发散MAK密文
	 * 
	 * @param mak
	 *            MAK密文[in]
	 * @throws Exception
	 */
	public void disperseMak(byte[] mak) throws Exception {

		logger.debug("disperseMak() start.");

		Log.v(TAG, "mak:");
		Log.v(TAG, ExPinPadUtil.printLog(mak));

		// 拉卡拉的8583报文使用的MAK是单倍长密钥
		if (mak.length != 8) {
			String errMsg = TAG + "|The length of mak[" + ExPinPadUtil.printLog(mak) + "|" + mak.length
					+ "] is illegal!!!";

			logger.debug(errMsg);

			throw new Exception(errMsg);
		}

		// 单倍的 mak 扩展为双倍长
		byte[] wkey = new byte[mak.length * 2];
		System.arraycopy(mak, 0, wkey, 0, mak.length);
		System.arraycopy(mak, 0, wkey, mak.length, mak.length);

		this.disperseWkey(wkey, ExPinPadUtil.C.DISP_MAK_FLAG);

		logger.debug("disperseMak() end.");

		return;
	}

	/**
	 * 发散PIK密文
	 * 
	 * @param pik
	 *            PIK密文[in]
	 * @throws Exception
	 */
	public void dispersePik(byte[] pik) throws Exception {

		logger.debug("dispersePik() start.");

		Log.v(TAG, "pik:");
		Log.v(TAG, ExPinPadUtil.printLog(pik));

		// 拉卡拉的8583报文使用的PIK是双倍长密钥
		if (pik.length != 16) {

			String errMsg = TAG + "|The length of pik[" + ExPinPadUtil.printLog(pik) + "|" + pik.length
					+ "] is illegal!!!";

			logger.debug(errMsg);

			throw new Exception(errMsg);
		}

		this.disperseWkey(pik, ExPinPadUtil.C.DISP_PIK_FLAG);

		logger.debug("dispersePik() end.");

		return;
	}

	/**
	 * 发散脱机PIK密文
	 * 
	 * @param pik
	 *            脱机PIK密文[in]
	 * @throws Exception
	 */
	public void disperseOffPik(byte[] pik) throws Exception {

		logger.debug("disperseOffPik() start.");

		Log.v(TAG, "offpik:");
		Log.v(TAG, ExPinPadUtil.printLog(pik));

		// 拉卡拉的8583报文使用的PIK是双倍长密钥
		if (pik.length != 16) {

			String errMsg = TAG + "|The length of offpik[" + ExPinPadUtil.printLog(pik) + "|" + pik.length
					+ "] is illegal!!!";

			logger.debug(errMsg);

			throw new Exception(errMsg);
		}

		this.disperseWkey(pik, ExPinPadUtil.C.DISP_OFFPIK_FLAG);

		logger.debug("disperseOffPik() end.");

		return;
	}

	private static String byteToHex(byte b) {
		return ("" + "0123456789ABCDEF".charAt(0xf & b >> 4) + "0123456789ABCDEF".charAt(b & 0xf));
	}

	public static String printLog(byte[] log) throws NullPointerException {
		String msg = "";
		try {
			for (int i = 0; i < log.length; i++) {
				msg += ExPinPadUtil.byteToHex(log[i]) + " ";
				if ((i + 1) % 16 == 0) { // 够16个字节就换行
					msg += "\r\n";
				}
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
		}

		return msg;
	}

	public static String getDevErrMsg(int devErrCode) {

		return ExPinPadUtil.C.ERR_LIST.get(devErrCode);
	}

	/*
	 * 
	 */
	public void getPinWithMethodOnePre() throws Exception {

		logger.debug("getPinWithMethodOnePre() start.");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<Void> future = (FutureTask<Void>) executor.submit(new Callable<Void>() {

			public Void call() throws Exception {

				try {
					// 设置 authcode
					ByteArrayOutputStream datas = new ByteArrayOutputStream();
					datas.write(ExPinPadUtil.C.PIK_ID);
					datas.write((byte) 0x81);
					datas.write(new byte[] { 0x00, 0x00 });
					byte[] authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SK_SKEY, datas.toByteArray(),
							ExPinPadUtil.random, ExPinPadUtil.C.SK_UID);
					// 选择PIK
					ExPinPadUtil.externalPadDev.selectPIK(ExPinPadUtil.C.PIK_ID, (byte) 0x81, new byte[] { 0x00, 0x00 },
							authCode);
				} catch (Exception e) {
					if (e instanceof IndicationException) {
						IndicationException indicationException = (IndicationException) e;
						Log.e(TAG, "[" + indicationException.getDevId() + "|" + indicationException.getEventId() + ":"
								+ ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");

					}

					throw e;

				}

				return null;
			}
		});
		executor.execute(future);

		try {

			future.get(ExPinPadUtil.tmo, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			logger.error("getPinWithMethodOnePre() catch InterruptedException.");

			future.cancel(true);

			this.release();

			throw e;

		} catch (ExecutionException e) {
			logger.error("getPinWithMethodOnePre() catch ExecutionException.");

			future.cancel(true);

			throw e;

		} catch (TimeoutException e) {
			logger.error("getPinWithMethodOnePre() catch TimeoutException.tmo=" + ExPinPadUtil.tmo);

			future.cancel(true);

			this.release();

			throw e;

		} catch (Exception e) {
			logger.error("getPinWithMethodOnePre() catch Exception.");

			future.cancel(true);

			throw e;

		} finally {

			executor.shutdown();

		}

		logger.debug("getPinWithMethodOnePre() end.");

		return;
	}

	/**
	 * 
	 * @param cardno
	 *            银行卡账号[in]
	 * @param amt
	 *            交易金额[in]
	 * @param time_out
	 *            输密等待超时时限[in]
	 * @return
	 * @throws Exception
	 */
	public byte[] getPinWithMethodOne(final String cardno, final String amt, final int time_out) throws Exception {
		byte[] pinblock = null;

		logger.debug("getPinWithMethodOne() start.");

		Log.v(TAG, "cardno=[" + cardno + "]");
		Log.v(TAG, "amt=[" + amt + "]");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<byte[]> future = (FutureTask<byte[]>) executor.submit(new Callable<byte[]>() {

			public byte[] call() throws Exception {
				byte[] pinblock = null;

				// 分散工作密钥
				byte[] wkey = SecurityUtil.decrypt3Des(ExPinPadUtil.C.SK_TKEY, ExPinPadUtil.random);
				// 卡号加密
				byte[] enCardno = SecurityUtil.encryptDes(wkey, ExPinPadUtil.getPanByLklFormat(cardno).getBytes());
				Log.v(TAG, "enCardno:");
				Log.v(TAG, ExPinPadUtil.printLog(enCardno));
				// 格式化键盘屏幕显示
				byte[] tips = null;
				if (amt != null && !"".equals(amt)) {
					tips = SecurityUtil.encryptDes(wkey, ExternalPINpadUtil.formartStr("RMB: " + amt));
				} else {
					tips = SecurityUtil.encryptDes(wkey, ExternalPINpadUtil.formartStr("请输入密码: "));
				}
				// 获取密码密文
				pinblock = ExPinPadUtil.externalPadDev.getPinWithMethodOne(ExPinPadUtil.C.PIK_ID, (byte) 0x80,
						ExPinPadUtil.C.PIN_MIN_LEN, ExPinPadUtil.C.PIN_MAX_LEN, enCardno, null, tips);
				Log.v(TAG, "pinblock:");
				Log.v(TAG, ExPinPadUtil.printLog(pinblock));

				return pinblock;
			}
		});
		executor.execute(future);

		try {

			pinblock = future.get(time_out, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			logger.error("getPinWithMethodOne() catch InterruptedException.");

			this.cancelGetPin();
			future.cancel(true);

			throw e;

		} catch (ExecutionException e) {
			logger.error("getPinWithMethodOne() catch ExecutionException.");

			this.cancelGetPin();
			future.cancel(true);

			throw e;

		} catch (TimeoutException e) {
			logger.error(
					"getPinWithMethodOne() catch TimeoutException.tmo=" + ExPinPadUtil.tmo + ",time_out=" + time_out);

			this.cancelGetPin();
			future.cancel(true);

			throw e;

		} catch (Exception e) {
			logger.error("getPinWithMethodOne() catch Exception.");

			this.cancelGetPin();
			future.cancel(true);

			throw e;

		} finally {

			executor.shutdown();

		}

		logger.debug("getPinWithMethodOne() end.");

		return pinblock;
	}

	/**
	 * 获取参与PIN计算的PAN
	 * 
	 * @param cardno
	 *            账号[in]
	 * @return
	 * @throws Exception
	 */
	private static String getPanByLklFormat(String cardno) throws Exception {

		String pan = null;

		if (cardno.length() >= ExPinPadUtil.C.CARDNO_MIN_LEN && cardno.length() <= ExPinPadUtil.C.CARDNO_MAX_LEN) {
			pan = "0000" + cardno.substring(3, 15);
		} else {
			String errMsg = TAG + "|The length of cardno[" + cardno + "] is illegal!";

			throw new Exception(errMsg);
		}

		return pan;
	}

	/**
	 * 计算MAC
	 * 
	 * @param mab
	 *            参与计算MAC的8字节数据[in]
	 * @return 8字节MAC
	 * @throws Exception
	 */
	public byte[] getMac(final byte[] mab) throws Exception {

		byte[] mac = null;

		logger.debug("getMac() start.");

		Log.v(TAG, "mab:");
		Log.v(TAG, ExPinPadUtil.printLog(mab));

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<byte[]> future = (FutureTask<byte[]>) executor.submit(new Callable<byte[]>() {

			public byte[] call() throws Exception {

				byte[] mac = null;

				// 传送加密数据
				// TODO:按照CK30开发手册，参数1应该采用“传输密钥索引”，这里采用默认值0x00
				ExPinPadUtil.externalPadDev.tranEncryptData((byte) 0x00, (byte) (0x00 | 0x0), mab);
				Log.v(TAG, "tranEncryptData success!");
				// 设置 authcode
				byte mode = (byte) 0x80;
				ByteArrayOutputStream datas = new ByteArrayOutputStream();
				datas.write(ExPinPadUtil.C.MAK_ID);
				datas.write(mode);
				datas.write(ExPinPadUtil.C.MAC_ALG_UNIPAY_ECB);
				byte[] authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SK_SKEY, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				Log.v(TAG, "authCode:");
				Log.v(TAG, ExPinPadUtil.printLog(authCode));
				// 获取mac
				mac = ExPinPadUtil.externalPadDev.getMac(ExPinPadUtil.C.MAK_ID, mode, ExPinPadUtil.C.MAC_ALG_UNIPAY_ECB,
						null, authCode);
				Log.v(TAG, "mac:");
				Log.v(TAG, ExPinPadUtil.printLog(mac));

				return mac;
			}
		});
		executor.execute(future);

		try {
			mac = future.get(ExPinPadUtil.tmo, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("getMac() catch InterruptedException.");

			future.cancel(true);
			this.release();

			throw e;
		} catch (ExecutionException e) {
			logger.error("getMac() catch ExecutionException.");

			future.cancel(true);

			throw e;
		} catch (TimeoutException e) {
			logger.error("getMac() catch TimeoutException.tmo=" + ExPinPadUtil.tmo);

			future.cancel(true);
			this.release();

			throw e;
		} catch (Exception e) {
			logger.error("getMac() catch Exception.");

			future.cancel(true);

			throw e;
		} finally {
			executor.shutdown();
		}

		logger.debug("getMac() end.");

		return mac;
	}

	/**
	 * 
	 * @param ShowDataLineOne
	 *            第一行待显示信息[in]
	 * @param ShowDataLineTwo
	 *            第二行待显示信息[in]
	 * @throws Exception
	 */
	public void display(final String ShowDataLineOne, final String ShowDataLineTwo) throws Exception {

		logger.debug("display() start.");
		Log.v(TAG, "ShowDataLineOne=[" + ShowDataLineOne + "]ShowDataLineTwo=[" + ShowDataLineTwo + "]");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<Void> future = (FutureTask<Void>) executor.submit(new Callable<Void>() {

			public Void call() throws Exception {

				// 分散工作密钥
				byte[] wkey = SecurityUtil.decrypt3Des(ExPinPadUtil.C.SK_TKEY, ExPinPadUtil.random);

				byte[] tipsLineOne = SecurityUtil.encryptDes(wkey,
						ExternalPINpadUtil.formartStr(ShowDataLineOne, false));
				Log.v(TAG, "tipsLineOne:");
				Log.v(TAG, ExPinPadUtil.printLog(tipsLineOne));
				// 设置第一行 authcode
				ByteArrayOutputStream datasLineOne = new ByteArrayOutputStream();
				byte lineOneNum = (byte) 0x00;
				datasLineOne.write(lineOneNum);
				datasLineOne.write(tipsLineOne);
				byte[] authCodeLineOne = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SK_SKEY,
						datasLineOne.toByteArray(), ExPinPadUtil.random, ExPinPadUtil.C.SK_UID);
				Log.v(TAG, "authCodeLineOne:");
				Log.v(TAG, ExPinPadUtil.printLog(authCodeLineOne));
				// 显示第一行
				ExPinPadUtil.externalPadDev.display(lineOneNum, tipsLineOne, authCodeLineOne);

				byte[] tipsLineTwo = SecurityUtil.encryptDes(wkey,
						ExternalPINpadUtil.formartStr(ShowDataLineTwo, false));
				Log.v(TAG, "tipsLineTwo:");
				Log.v(TAG, ExPinPadUtil.printLog(tipsLineTwo));
				// 设置第二行 authcode
				ByteArrayOutputStream datasLineTwo = new ByteArrayOutputStream();
				byte lineTwoNum = (byte) 0x81;
				datasLineTwo.write(lineTwoNum);
				datasLineTwo.write(tipsLineTwo);
				byte[] authCodeLineTwo = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SK_SKEY,
						datasLineTwo.toByteArray(), ExPinPadUtil.random, ExPinPadUtil.C.SK_UID);
				Log.v(TAG, "authCodeLineTwo:");
				Log.v(TAG, ExPinPadUtil.printLog(authCodeLineTwo));
				// 显示第二行
				ExPinPadUtil.externalPadDev.display(lineTwoNum, tipsLineTwo, authCodeLineTwo);

				return null;
			}
		});
		executor.execute(future);

		try {
			future.get((60 + 5), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("display() catch InterruptedException.");

			// this.cancelGetPin();
			future.cancel(true);

			throw e;
		} catch (ExecutionException e) {
			logger.error("display() catch ExecutionException.");

			// this.cancelGetPin();
			future.cancel(true);

			throw (Exception) e;
		} catch (TimeoutException e) {
			logger.error("display() catch TimeoutException.tmo=" + tmo);

			// this.cancelGetPin();
			future.cancel(true);
			this.release();

			throw e;
		} catch (Exception e) {
			logger.error("display() catch Exception.");

			this.cancelGetPin();
			future.cancel(true);

			throw e;
		} finally {
			executor.shutdown();
		}

		logger.debug("display() end.");

		return;
	}

	/**
	 * 下载主密钥
	 * 
	 */
	public void downloadMkey(final byte[] tmk) throws Exception {

		Log.v(TAG, "Enter downloadMkey...");
		Log.v(TAG, ExPinPadUtil.printLog(tmk));

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<Void> future = (FutureTask<Void>) executor.submit(new Callable<Void>() {

			public Void call() throws Exception {

				byte mKeyID = ExPinPadUtil.mkey_id;
				Log.i("ckh", "下载主密钥 mkeyid == " + mKeyID);
				byte mode;
				byte cno;
				ByteArrayOutputStream datas = new ByteArrayOutputStream();
				byte[] authCode = null;

				// 下载主密钥(PIK)
				mode = (byte) 0x82;
				cno = (byte) 0x01;
				datas.reset();
				datas.write(mKeyID);
				datas.write(mode);
				datas.write(cno);
				datas.write(tmk);
				authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SAK, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				ExPinPadUtil.externalPadDev.downloadMkey(mKeyID, mode, cno, null, tmk, authCode);

				cno = (byte) 0x02;
				datas.reset();
				datas.write(mKeyID);
				datas.write(mode);
				datas.write(cno);
				datas.write(tmk);
				authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SAK, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				ExPinPadUtil.externalPadDev.downloadMkey(mKeyID, mode, cno, null, tmk, authCode);

				cno = (byte) 0x03;
				datas.reset();
				datas.write(mKeyID);
				datas.write(mode);
				datas.write(cno);
				datas.write(tmk);
				authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SAK, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				ExPinPadUtil.externalPadDev.downloadMkey(mKeyID, mode, cno, null, tmk, authCode);

				// ------------------- 分割线 -------------------
				// 下载主密钥(MAK)
				mode = (byte) 0x81;
				cno = (byte) 0x01;
				datas.reset();
				datas.write(mKeyID);
				datas.write(mode);
				datas.write(cno);
				datas.write(tmk);
				authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SAK, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				ExPinPadUtil.externalPadDev.downloadMkey(mKeyID, mode, cno, null, tmk, authCode);

				cno = (byte) 0x02;
				datas.reset();
				datas.write(mKeyID);
				datas.write(mode);
				datas.write(cno);
				datas.write(tmk);
				authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SAK, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				ExPinPadUtil.externalPadDev.downloadMkey(mKeyID, mode, cno, null, tmk, authCode);

				cno = (byte) 0x03;
				datas.reset();
				datas.write(mKeyID);
				datas.write(mode);
				datas.write(cno);
				datas.write(tmk);
				authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SAK, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				ExPinPadUtil.externalPadDev.downloadMkey(mKeyID, mode, cno, null, tmk, authCode);

				// ------------------- 分割线 -------------------
				// 下载主密钥(TDK)
				mode = (byte) 0x80;
				cno = (byte) 0x01;
				datas.reset();
				datas.write(mKeyID);
				datas.write(mode);
				datas.write(cno);
				datas.write(tmk);
				authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SAK, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				ExPinPadUtil.externalPadDev.downloadMkey(mKeyID, mode, cno, null, tmk, authCode);

				cno = (byte) 0x02;
				datas.reset();
				datas.write(mKeyID);
				datas.write(mode);
				datas.write(cno);
				datas.write(tmk);
				authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SAK, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				ExPinPadUtil.externalPadDev.downloadMkey(mKeyID, mode, cno, null, tmk, authCode);

				cno = (byte) 0x03;
				datas.reset();
				datas.write(mKeyID);
				datas.write(mode);
				datas.write(cno);
				datas.write(tmk);
				authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SAK, datas.toByteArray(), random,
						ExPinPadUtil.C.SK_UID);
				ExPinPadUtil.externalPadDev.downloadMkey(mKeyID, mode, cno, null, tmk, authCode);

				return null;
			}
		});
		executor.execute(future);

		try {

			future.get(ExPinPadUtil.tmo, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			logger.error("downloadMkey() catch InterruptedException.");

			future.cancel(true);
			this.release();

			throw e;

		} catch (ExecutionException e) {
			logger.error("downloadMkey() catch ExecutionException.");

			future.cancel(true);

			throw e;

		} catch (TimeoutException e) {
			logger.error("downloadMkey() catch TimeoutException.tmo=" + ExPinPadUtil.tmo);

			future.cancel(true);
			this.release();

			throw e;

		} catch (Exception e) {
			logger.error("downloadMkey() catch Exception.");

			future.cancel(true);

			throw e;

		} finally {

			executor.shutdown();

		}

		Log.v(TAG, "Out downloadMkey...");

		return;
	}

	public void clearWorkKeyId() throws Exception {
		logger.debug("clearWorkKeyId() start");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<Void> future = (FutureTask<Void>) executor.submit(new Callable<Void>() {

			public Void call() throws Exception {

				byte wKeyID;
				byte mode;

				// 清除PIN工作密钥===============================

				mode = (byte) 0x80;
				wKeyID = (byte) 0x1;
				// 设置 authcode
				ByteArrayOutputStream pinDatas = new ByteArrayOutputStream();
				pinDatas.write(mode);
				pinDatas.write(wKeyID);

				byte[] pinAuthCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SK_SKEY, pinDatas.toByteArray(),
						random, ExPinPadUtil.C.SK_UID);

				Log.v(TAG, "清除PIN工作密钥start"); // 清除PIN工作密钥
				try {
					ExPinPadUtil.externalPadDev.clearWKey(mode, wKeyID, pinAuthCode);
				} catch (Exception e) {
					logger.error("清除PIN工作密钥excep", e);

					if (e instanceof IndicationException) {
						IndicationException indicationException = (IndicationException) e;
						Log.e("LogActivity",
								"[" + indicationException.getDevId() + "|" + indicationException.getEventId() + ":"
										+ ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");
					}
					e.printStackTrace();
				}
				Log.v(TAG, "清除PIN工作密钥end");

				// 清除Mac工作密钥===============================

				mode = (byte) 0x81;
				wKeyID = (byte) 0x1;
				// 设置 authcode
				ByteArrayOutputStream macDatas = new ByteArrayOutputStream();
				macDatas.write(mode);
				macDatas.write(wKeyID);

				byte[] macAuthCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SK_SKEY, macDatas.toByteArray(),
						random, ExPinPadUtil.C.SK_UID);

				Log.v(TAG, "清除Mac工作密钥start");
				ExPinPadUtil.externalPadDev.clearWKey(mode, wKeyID, macAuthCode);
				Log.v(TAG, "清除Mac工作密钥end");

				return null;
			}
		});
		executor.execute(future);

		try {

			future.get(ExPinPadUtil.tmo, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			logger.error("disperseWkey() catch InterruptedException.");

			future.cancel(true);
			this.release();

			throw e;

		} catch (ExecutionException e) {
			logger.error("disperseWkey() catch ExecutionException.");

			future.cancel(true);

			throw e;

		} catch (TimeoutException e) {
			logger.error("disperseWkey() catch TimeoutException.tmo=" + ExPinPadUtil.tmo);

			future.cancel(true);
			this.release();

			throw e;

		} catch (Exception e) {
			logger.error("disperseWkey() catch Exception.");

			future.cancel(true);

			throw e;

		} finally {

			executor.shutdown();

		}

		logger.debug("disperseWkey() end.");

		return;
	}

	/**
	 * add for offline pin by chenkehui @20131219
	 */
	public void getPinWithMethodTwoPre() throws Exception {

		logger.debug("getPinWithMethodOnePre() start.");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<Void> future = (FutureTask<Void>) executor.submit(new Callable<Void>() {

			public Void call() throws Exception {

				try {
					// 设置 authcode
					ByteArrayOutputStream datas = new ByteArrayOutputStream();
					datas.write(ExPinPadUtil.C.OFFPIK_ID);
					datas.write((byte) 0x81);
					datas.write(new byte[] { 0x00, 0x00 });
					byte[] authCode = ExternalPINpadUtil.getAuthCode(ExPinPadUtil.C.SK_SKEY, datas.toByteArray(),
							ExPinPadUtil.random, ExPinPadUtil.C.SK_UID);
					// 选择PIK
					ExPinPadUtil.externalPadDev.selectPIK(ExPinPadUtil.C.OFFPIK_ID, (byte) 0x81,
							new byte[] { 0x00, 0x00 }, authCode);
				} catch (Exception e) {
					if (e instanceof IndicationException) {
						IndicationException indicationException = (IndicationException) e;
						Log.e(TAG, "[" + indicationException.getDevId() + "|" + indicationException.getEventId() + ":"
								+ ExPinPadUtil.getDevErrMsg(indicationException.getEventId()) + "]");

					}

					throw e;

				}

				return null;
			}
		});
		executor.execute(future);

		try {

			future.get(ExPinPadUtil.tmo, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			logger.error("getPinWithMethodTwoPre() catch InterruptedException.");

			future.cancel(true);

			this.release();

			throw e;

		} catch (ExecutionException e) {
			logger.error("getPinWithMethodTwoPre() catch ExecutionException.");

			future.cancel(true);

			throw e;

		} catch (TimeoutException e) {
			logger.error("getPinWithMethodTwoPre() catch TimeoutException.tmo=" + ExPinPadUtil.tmo);

			future.cancel(true);

			this.release();

			throw e;

		} catch (Exception e) {
			logger.error("getPinWithMethodTwoPre() catch Exception.");

			future.cancel(true);

			throw e;

		} finally {

			executor.shutdown();

		}

		logger.debug("getPinWithMethodTwoPre() end.");

		return;
	}

	/**
	 * add for offline pin by chenkehui @20131219
	 * 
	 * @param cardno
	 *            银行卡账号[in]
	 * @param amt
	 *            交易金额[in]
	 * @param time_out
	 *            输密等待超时时限[in]
	 * @return
	 * @throws Exception
	 */
	public byte[] getPinWithMethodTwo(final String cardno, final String amt, final int time_out) throws Exception {
		byte[] pinblock = null;

		logger.debug("getPinWithMethodTwo() start.");

		Log.v(TAG, "cardno=[" + cardno + "]");
		Log.v(TAG, "amt=[" + amt + "]");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<byte[]> future = (FutureTask<byte[]>) executor.submit(new Callable<byte[]>() {

			public byte[] call() throws Exception {
				byte[] pinblock = null;

				// 分散工作密钥
				byte[] wkey = SecurityUtil.decrypt3Des(ExPinPadUtil.C.SK_TKEY, ExPinPadUtil.random);
				// 卡号加密
				byte[] enCardno = SecurityUtil.encryptDes(wkey, ExPinPadUtil.getPanByLklFormat(cardno).getBytes());
				Log.v(TAG, "enCardno:");
				Log.v(TAG, ExPinPadUtil.printLog(enCardno));
				// 格式化键盘屏幕显示
				byte[] tips = null;
				if (amt != null && !"".equals(amt)) {
					tips = SecurityUtil.encryptDes(wkey, ExternalPINpadUtil.formartStr("RMB: " + amt));
				} else {
					tips = SecurityUtil.encryptDes(wkey, ExternalPINpadUtil.formartStr("请输入密码: "));
				}
				// 获取密码密文
				pinblock = ExPinPadUtil.externalPadDev.getPinWithMethodOne(ExPinPadUtil.C.OFFPIK_ID, (byte) 0x80,
						ExPinPadUtil.C.PIN_MIN_LEN, ExPinPadUtil.C.PIN_MAX_LEN, enCardno, null, tips);
				Log.v(TAG, "pinblock:");
				Log.v(TAG, ExPinPadUtil.printLog(pinblock));

				return pinblock;
			}
		});
		executor.execute(future);

		try {

			pinblock = future.get(time_out, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			logger.error("getPinWithMethodTwo() catch InterruptedException.");

			this.cancelGetPin();
			future.cancel(true);

			throw e;

		} catch (ExecutionException e) {
			logger.error("getPinWithMethodTwo() catch ExecutionException.");

			this.cancelGetPin();
			future.cancel(true);

			throw e;

		} catch (TimeoutException e) {
			logger.error(
					"getPinWithMethodTwo() catch TimeoutException.tmo=" + ExPinPadUtil.tmo + ",time_out=" + time_out);

			this.cancelGetPin();
			future.cancel(true);

			throw e;

		} catch (Exception e) {
			logger.error("getPinWithMethodTwo() catch Exception.");

			this.cancelGetPin();
			future.cancel(true);

			throw e;

		} finally {

			executor.shutdown();

		}

		logger.debug("getPinWithMethodTwo() end.");

		return pinblock;
	}
}
