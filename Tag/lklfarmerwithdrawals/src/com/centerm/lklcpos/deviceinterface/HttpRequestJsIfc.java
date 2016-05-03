package com.centerm.lklcpos.deviceinterface;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.iso8583.util.DataConverter;
import com.centerm.lklcpos.http.LklAndroidClient;
import com.centerm.lklcpos.util.Constant;
import com.centerm.lklcpos.util.ExPinPadException;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.HttpStatusException;
import com.centerm.lklcpos.util.MD5Util;
import com.centerm.lklcpos.util.NullConnectAddressException;
import com.centerm.lklcpos.util.SecurityUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.util.M3HexUtil;
import com.lakala.android.security.SecurityKit;
import com.lkl.farmerwithdrawals.R;

public class HttpRequestJsIfc {

	private static final Logger log = Logger.getLogger(HttpRequestJsIfc.class);

	private static final String TAG = "LogActivity";
	private static final String httpresponse = "httpresponse";

	private final int tpduLen = 5; // TPDU 压缩的字节长度
	private Context context = null;
	private Handler handler = null;
	private ParamConfigDao paramConfigDao = null; // 参数配置表
	private LklPacketHandle isopacket = null;

	private String connectMode = "0"; // 通讯模式, 0：3G专网, 1：公网, 2：其他

	private int timeout = 60; // 单位S

	private Bundle mBundle = new Bundle();
	private Map<String, String> activiteMap = new HashMap<String, String>();

	public HttpRequestJsIfc(Context context, Handler handler) throws Exception {
		this.context = context;
		this.handler = handler;
		if (this.paramConfigDao == null) {
			this.paramConfigDao = new ParamConfigDaoImpl(context);
		}
		if (this.isopacket == null) {
			this.isopacket = new LklPacketHandle(context, handler);
		}
		connectMode = paramConfigDao.get("connect_mode");
	}

	public int getDealtimeout() { // 交易超时时间,默认值60秒
		// int timeout = 60;
		try {
			String transTime = paramConfigDao.get("dealtimeout");
			if ("".equals(transTime) || transTime == null) {
				this.timeout = 60;
			} else {
				this.timeout = Integer.parseInt(transTime);
			}
		} catch (NumberFormatException e) {
			log.warn("从数据库获取交易超时时间失败,返回默认值[" + timeout + "]", e);
		}
		if (this.timeout < 60 || this.timeout > 120) {
			this.timeout = 60;
		}
		return this.timeout;
	}

	// 下载证书
	public void handleHttpDownload(final String merId, final String termId, final String password) {
		new Thread() {
			public void run() {
				messageSendProgress(0x10); // 发送消息到networkactivity开始倒计时

				// String uriAPI = "http://"
				// +paramConfigDao.get("caIp")+"/LaKaLaNetSecurity/MerCertDownloadSev";
				String uriAPI = null;
				try {
					uriAPI = paramConfigDao.get("caIp");
					if (uriAPI == null || "".equals(uriAPI)) {
						throw new NullConnectAddressException("下载地址未设置，请先设置");
					}
				} catch (Exception e) {
					// TODO: handle exception
					log.warn("下载证书地址未设置", e);
					e.printStackTrace();
					dealException(e, mBundle);
					messageSendResult(mBundle);
					return;
				}

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("merId", merId));// "860010030210008"
				params.add(new BasicNameValuePair("termId", termId)); // "01000007"
				String pwd = MD5Util.getStringMD5(password).toUpperCase();
				params.add(new BasicNameValuePair("password", pwd));

				try {
					String result = postUrl(uriAPI, params, "utf-8");
					JSONObject object = new JSONObject(result);
					String retcode = object.getString("code");
					String retdata = object.getString("data");
					String retmessage = object.getString("message");
					String retsign = object.getString("signData");
					log.debug("code=[" + retcode + "]");
					log.debug("data=[" + retdata + "]");
					log.debug("message=[" + retmessage + "]");
					log.debug("signData=[" + retsign + "]");
					if ("0000".equals(retcode)) {// 成功
						Log.i("ckh", "downloadcer success");
						byte[] retbyte = Base64.decode(retdata, Base64.DEFAULT);
						byte[] retsignbyte = Base64.decode(retsign, Base64.DEFAULT);
						LklAndroidClient.getInstance(context).creatClientP12(retbyte);// 保存证书
						paramConfigDao.save("capwd", password);
						log.info("证书下载成功....");
						mBundle.putBoolean("isSuc", true);
						LklAndroidClient.setInstance(null);
					} else { // 失败
						Log.i("ckh", "downloadcer fail");
						log.error("证书下载错误原因:[" + retmessage + "]");
						mBundle.putBoolean("isSuc", false);
						mBundle.putString("retCode", retcode);
						mBundle.putString("errMsg", retmessage);
					}
				} catch (Exception e) {
					Log.e("ckh", "下载证书发生异常..");
					log.error("下载证书发生异常.", e);
					e.printStackTrace();
					dealException(e, mBundle);
				}
				messageSendResult(mBundle);
			}
		}.start();
	}

	// 向前置发起http请求并响应返回结果
	/*
	 * public String postUrl(String url, List<NameValuePair> params, String
	 * encoding) throws Exception{ Log.i("ckh", "http请求地址:[" + url + "]");
	 * Log.i("ckh", "http请求信息:[" + params.toString() + "]"); URL urlobj=new
	 * URL(url); Log.i("ckh", "http请求端口:["+urlobj.getPort()+"]");
	 * 
	 * HttpPost request = new HttpPost(url); HttpEntity entity = new
	 * UrlEncodedFormEntity(params, Config.getInstance(
	 * context).getConfig("httpEncode", encoding)); request.setEntity(entity);
	 * 
	 * HttpClient client = HttpsUtil.getHttpClient(
	 * HttpsUtil.getHttpScheme(urlobj.getPort()), null, getDealtimeout()*1000);
	 * //HttpConnectionParams.setConnectionTimeout(client.getParams(), 15*1000);
	 * //client.getParams().setIntParameter(
	 * HttpConnectionParams.CONNECTION_TIMEOUT, 2*1000);//设置连接超时时间 try {
	 * HttpResponse response = client.execute(request);
	 * log.debug("http请求返回response状态码:[" +
	 * response.getStatusLine().getStatusCode() + "]");
	 * 
	 * if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	 * HttpEntity resEntity = response.getEntity(); return
	 * EntityUtils.toString(resEntity); } else { throw new
	 * HttpStatusException("HTTP返回错误状态,状态码[" +
	 * response.getStatusLine().getStatusCode() + "]"); } } catch (Exception e)
	 * { log.error("HTTP ConnectTimeoutException...", e); throw e; } finally {
	 * // request.abort(); //
	 * client.getConnectionManager().closeExpiredConnections(); //
	 * client.getConnectionManager().shutdown(); } }
	 */

	private static String end = "\r\n";
	private static String twoHyphens = "--";
	private static String boundary = "---------------------------7da2137580612";

	public String postUrl(String url, List<NameValuePair> nameparams, String encoding) throws Exception {
		URL urlobj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) urlobj.openConnection();
		/* 允许Input、Output，不使用Cache */
		conn.setDoInput(true);
		conn.setDoOutput(true);
		HashMap<String, String> params = new HashMap<String, String>();

		for (int i = 0; i < nameparams.size(); i++) {
			params.put(nameparams.get(i).getName(), nameparams.get(i).getValue());
		}

		String formField = addFormField(params);

		if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
			conn.setRequestProperty("Connection", "close");
		}
		conn.setUseCaches(false);
		int timeout = getDealtimeout() * 1000;
		conn.setReadTimeout(timeout);
		conn.setConnectTimeout(timeout);

		/* 设置传送的method=POST */
		conn.setRequestMethod("POST");
		/* setRequestProperty */
		conn.setRequestProperty("Charset", encoding);
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		OutputStream out = conn.getOutputStream();
		out.write(formField.toString().getBytes(encoding));// 发送表单字段数据
		out.write((twoHyphens + boundary + twoHyphens + end).getBytes(encoding));// 数据结束标志
		out.flush();
		/* 取得Response内容 */
		InputStream in = conn.getInputStream();
		int ch;
		StringBuffer b = new StringBuffer();
		try {
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				while ((ch = in.read()) != -1) {
					b.append((char) ch);
				}
			} else {
				throw new HttpStatusException("HTTP返回错误状态,状态码[" + conn.getResponseCode() + "]");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			in.close();
			out.close();
			conn.disconnect();
			Log.w("httprequest", "close connection");
		}
		return new String(b.toString().getBytes("ISO8859-1"), encoding);
	}

	private static String addFormField(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(twoHyphens + boundary + end);
			sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + end);
			sb.append(end);
			sb.append(entry.getValue() + end);
		}
		return sb.toString();
	}

	/*
	 * 用于原生态
	 */
	private void dealException(Exception e, Bundle b) {
		try {
			// Thread.sleep(1000);
			String retCode = Constant.tips.tip_err_code;
			String errMsg = context.getResources().getString(R.string.tip_net_error);
			if (e instanceof JSONException) {
				errMsg = context.getResources().getString(R.string.tip_data_error);
			} else if (e instanceof HttpStatusException) {
				errMsg = e.getMessage();
			} else if (e instanceof ConnectException) {
				errMsg = context.getResources().getString(R.string.tip_connect_timeout);
				;
			} else if (e instanceof SocketException) {
				errMsg = context.getResources().getString(R.string.tip_connect_timeout);
			} else if (e instanceof SocketTimeoutException) {
				errMsg = context.getResources().getString(R.string.tip_socket_timeout);
				// retCode = "timeout";
				b.putString("isNeedReverse", "true"); // 响应超时，保存冲正数据
			} else if (e instanceof ConnectTimeoutException) {
				errMsg = context.getResources().getString(R.string.tip_connect_timeout);
			} else if (e instanceof FileNotFoundException) {
				errMsg = context.getResources().getString(R.string.tip_file_notfound);
			} else if (e instanceof ExPinPadException) {
				errMsg = "外接密码键盘异常";
			} else if (e instanceof NullConnectAddressException) {
				errMsg = e.getMessage();
			} else {
				errMsg = "其他错误!";
			}
			b.putBoolean("isSuc", false);
			b.putString("retCode", retCode);
			b.putString("errMsg", errMsg);
		} catch (Exception e2) {
			log.error("处理异常时发生错误..", e2);
			e2.printStackTrace();
		}
	}

	// 发起交易请求
	public void transactionRequest(final String transCode, final Map<String, String> dataMap) {
		final JsResponse response = new JsResponse();
		new Thread() {
			public void run() {
				messageSendProgress(0x10); // 发送消息到activity开始倒计时
				messageSendProgress(0x01); // 发送数据
				// handler.sendMessage(Utility.createMesage(Constant.msg.msg_http_begin));

				// String uriAPI =
				// "http://10.7.111.12:9111/LaKaLaNetSecurity/PadPosSev";
				// String uriAPI = "http://"
				// +paramConfigDao.get("transIp")+"/LaKaLaNetSecurity/PadPosSev";
				String uriAPI = null;
				try {
					uriAPI = paramConfigDao.get("transIp");// 交易地址

					if (uriAPI == null || "".equals(uriAPI)) {
						throw new NullConnectAddressException("连接地址未设置，请先设置");
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					dealException(e, mBundle);
					messageSendResult(mBundle);
					return;
				}
				log.debug("http请求交易地址:[" + uriAPI + "]");
				byte[] msgData = null;
				try {
					msgData = isopacket.pack(transCode, dataMap);
					System.out.println("dataMap的内容为：");
					System.out.println(dataMap.toString());
					System.out.println("msgData的内容为：");
				} catch (Exception e1) {
					log.error("组包过程发生异常.... transCode=[" + transCode + "]&&dataMap=[" + dataMap + "]", e1);
					e1.printStackTrace();
					dealException(e1, mBundle);
					messageSendResult(mBundle);
					return;
				}

				byte[] msg = isopacket.addMessageLen(msgData);
				log.debug("请求发送的报文=[" + HexUtil.bcd2str(msg) + "]");

				List<NameValuePair> params = null;
				try {
					String pwd = paramConfigDao.get("capwd"); // 数据库获取私钥密码
					log.debug("capwd =  [" + pwd + "]");
					params = LklAndroidClient.getInstance(context).getSendData(msg, pwd);
				} catch (Exception e) {
					dealException(e, mBundle);
					messageSendResult(mBundle);
					log.error("数据加密异常", e);
					e.printStackTrace();
					return;
				}

				try {
					messageSendProgress(0x02); // 接收数据
					String strResult = postUrl(uriAPI, params, "utf-8");
					log.debug("接收前置返回的交易数据：[" + strResult + "]");
					JSONObject object = new JSONObject(strResult);
					String retcode = object.getString("code");
					String retdata = object.getString("data");
					String retmessage = object.getString("message");
					String retsign = object.getString("signData");
					if ("0000".equals(retcode)) {// 成功
						byte[] retbyte = Base64.decode(retdata, Base64.DEFAULT);
						byte[] retsignbyte = Base64.decode(retsign, Base64.DEFAULT);
						SecurityKit kit = new SecurityKit();
						boolean v = kit.verify(retbyte, retsignbyte, LklAndroidClient.getInstance(context).getScer());
						if (v) {
							log.info("交易代码[" + transCode + "]返回的报文验签成功!");
							Log.d(TAG, new String(retbyte));
							byte[] pack = isopacket.subMessLen(retbyte);

							log.debug("print return packet begin ...");
							Log.d("ckh",
									"交易transCode=[" + transCode + "]," + "\r\n返回的报文=[" + HexUtil.bcd2str(pack) + "]");
							log.debug("交易transCode=[" + transCode + "]," + "\r\n返回的报文=[" + HexUtil.bcd2str(pack) + "]");
							log.debug("print return packet end ...");

							Map<String, String> resmap = isopacket.unPack(pack, "002311");// 解包

							Log.i("ckh", "39域 rescode = " + resmap.get("respcode"));

							// 串包判断,IC卡联调时再打开，原因消费撤销无法验证通过问题。
							/*
							 * if (judgeResultMsg (dataMap, resmap)) {
							 * log.error("联机交易接收报文串包!"); log.error(
							 * "接收报文ResultMap == "+resmap.toString());
							 * mBundle.putString("retCode",
							 * Constant.tips.tip_err_code);
							 * mBundle.putString("errMsg", "接收数据异常");
							 * mBundle.putBoolean("isSuc", false);
							 * mBundle.putString("isNeedReverse", "true");
							 * //返回数据串包，保存冲正数据
							 * 
							 * messageSendResult(mBundle); return; }
							 */

							if (!"002308".equals(transCode)) { // 非签到交易，39域返回00时，校验mac

								if ("00".equals(resmap.get("respcode"))) {
									if (getMac(pack).equals(resmap.get("mesauthcode"))) {
										mBundle.putBoolean("isSuc", true);
										mBundle.putSerializable("resMap", (Serializable) resmap);

									} else {
										mBundle.putString("retCode", Constant.tips.tip_err_code);
										mBundle.putString("errMsg", "返回报文mac校验失败！");
										mBundle.putBoolean("isSuc", false);
										mBundle.putString("isNeedReverse", "true"); // 返回数据mac检验失败，保存冲正数据
									}
								} else { // 接到数据，但是交易处理不成功，不进行mac校验
									/*
									 * if ("68".equals(resmap.get("respcode"))){
									 * mBundle.putString("isNeedReverse",
									 * "true"); //接收超时，保存冲正数据 }
									 */ // 后台返回68不需要做冲正处理
									if ("002329".equals(transCode)) {// 反激活
										resmap.put("type", "selfopen");
									}
									mBundle.putBoolean("isSuc", true);
									mBundle.putSerializable("resMap", (Serializable) resmap);
								}
							} else { // 签到，不需校验
								mBundle.putBoolean("isSuc", true);
								mBundle.putSerializable("resMap", (Serializable) resmap);
							}
						} else {
							mBundle.putString("retCode", Constant.tips.tip_err_code);
							mBundle.putString("errMsg", "终端验签不通过");
							mBundle.putString("isNeedReverse", "true");
							mBundle.putBoolean("isSuc", false);
							log.error("报文成功返回但验签不通过:" + retmessage);
						}
						mBundle.putBoolean("isOnLine", true); // PBOC流程中是否联机成功
					} else { // 失败
						if ("0006".equals(retcode)) {
							mBundle.putString("isNeedReverse", "true"); // 接收POSP返回失败，保存冲正数据
						}
						log.error("前置返回错误原因:" + retmessage);
						mBundle.putBoolean("isSuc", false);
						mBundle.putString("retCode", retcode);
						mBundle.putString("errMsg", retmessage);
						mBundle.putBoolean("isOnLine", false); // PBOC流程中是否联机成功
					}

				} catch (Exception e) {
					dealException(e, mBundle);
					e.printStackTrace();
					log.error("", e);
				}
				messageSendResult(mBundle);
			}
		}.start();
	}

	private String getMac(byte[] responce) {
		String ret = "";

		try {
			byte[] macBlock = new byte[responce.length - 8];
			System.arraycopy(responce, tpduLen, macBlock, 0, responce.length - 8 - tpduLen);
			// log.info("responce="+DataConverter.bytesToHexString(responce));
			// log.info("macBlock="+DataConverter.bytesToHexString(macBlock));

			String macBloacStr = DataConverter.bytesToHexString(macBlock);
			macBloacStr = DataConverter.addZeroRightToMod16Equal0(macBloacStr); // 瀵筸acBlock杩涜琛ヤ綅鎿崭綔锛岀洿鍒板叾闀垮害瀵?6鍙栨ā涓?
			byte[] macBlockByte = DataConverter.getStringXor(macBloacStr); // 瀵筸acBlock杩涜阃愪釜8瀛楄妭寮傛垨杩愮畻
			byte[] macInfo = null;
			if ("0".equals(Utility.getPinPadDevSymbol(context))) {
				macInfo = isopacket.getMac(macBlockByte);
			} else {
				macInfo = isopacket.getMacFromPinPadDev(macBlockByte);
			}
			// log.info("macInfo="+DataConverter.bytesToHexString(macInfo));
			ret = DataConverter.byteToBinaryString(macInfo);
		} catch (Exception e) {
			log.error("计算mac失败...", e);
			e.printStackTrace();
		}
		return ret;
	}

	private void messageSendResult(Bundle mBundle) {
		Message mMessage = Message.obtain();
		mMessage.setData(mBundle);
		mMessage.what = 0x00;
		handler.sendMessage(mMessage);
	}

	private void messageSendProgress(int what) {
		Message mMessage = Message.obtain();
		mMessage.what = what;
		handler.sendMessage(mMessage);
	}

	private boolean judgeResultMsg(Map<String, String> data, Map<String, String> res) {
		if (!data.get("terminalid").equals(res.get("terminalid"))
				|| !data.get("acceptoridcode").equals(res.get("acceptoridcode"))
				|| !data.get("systraceno").equals(res.get("systraceno"))
				|| !data.get("transprocode").equals(res.get("transprocode"))
				|| !map.get(data.get("msg_tp")).equals(res.get("msg_tp"))) {

			return true;
		}
		return false;
	}

	public final static Map<String, String> map = new HashMap<String, String>() {
		{
			put("0820", "0830");
			put("0200", "0210");
			put("0100", "0110");
			put("0400", "0410");
			put("0220", "0230");
			put("0320", "0330");
			put("0500", "0510");
			put("0620", "0630");
		}
	};

	/*********** 以下为一键激活功能添加 *********************/

	/**
	 * 一键激活第一步 下载商终信息
	 */
	public void merchantDownload(final String confirmCode) {
		activiteMap.clear();
		new Thread() {
			public void run() {
				String uriAPI = null;
				messageSendProgress(0xa1);
				try {
					log.debug("通讯模式 , connectMode=" + connectMode);
					log.debug("测试环境, env=" + paramConfigDao.get("env"));

					if ("0".equals(connectMode)) { // 3G
						// if("200021".equals(paramConfigDao.get("fid"))){ //测试
						if ("test".equals(paramConfigDao.get("env"))) { // 测试
							uriAPI = paramConfigDao.get("test_3gapn_actUrl");
						} else {
							uriAPI = paramConfigDao.get("produce_3gapn_actUrl");
						}
					} else if ("1".equals(connectMode)) { // 公网
						if ("test".equals(paramConfigDao.get("env"))) { // 测试
							uriAPI = paramConfigDao.get("test_public_actUrl");
						} else {
							uriAPI = paramConfigDao.get("produce_public_actUrl");
						}
					} else {
						uriAPI = paramConfigDao.get("OneKeyActivate");
					}
					if (uriAPI == null || "".equals(uriAPI)) {
						throw new NullConnectAddressException("商终信息下载地址未设置，请先设置");
					}
					log.debug("商终下载地址 = [" + uriAPI + "]");
				} catch (Exception e) {
					log.warn("商终信息下载地址未设置", e);
					e.printStackTrace();
					dealExcepForOneShot(e, mBundle);
					messageSendResult(mBundle);
					return;
				}

				List<NameValuePair> params = new ArrayList<NameValuePair>();

				String crypt = Utility.getCryptData(); // 随机密钥
				String sn = Utility.getLklCposSN();

				try {
					byte[] sncry = LklAndroidClient.getInstance(context).encrypeByPubKey(sn.getBytes("UTF-8"));// SecurityUtil.encrype3DES(cryptData,//
																												// sn);

					byte[] cryptData = LklAndroidClient.getInstance(context).encrypeByPubKey(crypt.getBytes());

					params.add(new BasicNameValuePair("trmseq", sn)); // 设备序列号
					// params.add(new BasicNameValuePair("sn", Base64
					// .encodeToString(sncry, Base64.DEFAULT))); // 设备序列号

					params.add(new BasicNameValuePair("cryptData", Base64.encodeToString(cryptData, Base64.DEFAULT))); // 随机密钥
				} catch (Exception e1) {
					log.error("数据加密失败", e1);
					e1.printStackTrace();
					dealExcepForOneShot(e1, mBundle);
					messageSendResult(mBundle);
					return;
				}

				params.add(new BasicNameValuePair("transCode", "0001"));
				String fid = paramConfigDao.get("fid");
				params.add(new BasicNameValuePair("trmman", fid)); // 升腾207
																	// ,新大陆85
				params.add(new BasicNameValuePair("channel", "PADPOS")); // NETPOS
																			// 、PADPOS
				params.add(new BasicNameValuePair("apptyp", "00"));
				params.add(new BasicNameValuePair("confirmCode", confirmCode));

				log.debug("厂商标识fid=" + fid);
				for (NameValuePair nameValuePair : params) {
					log.debug("参数 = " + nameValuePair.getName() + "||" + nameValuePair.getValue());
				}
				try {
					String strResult = "{}";
					try {
						strResult = postUrl(uriAPI, params, "utf-8");
					} catch (Exception e) {
						e.printStackTrace();
						log.error("下载商终钥异常", e);
						mBundle.putString("retCode", "E001");
						messageSendResult(mBundle);
						return;
					}
					// Thread.sleep(2000);
					// String strResult =
					// "{\"code\":\"0000\",\"data\":{\"certPwd\":\"111111\",\"merId\":\"860010030210008\",\"merName\":\"测试\",\"tmk\":\"\",\"termId\":\"02000006\"},\"message\":\"交易成功\",\"signData\":\"\"}";
					log.debug("前置返回商终数据=" + strResult + "");
					JSONObject object = new JSONObject(strResult);
					String retcode = object.getString("code");
					log.info("返回码：" + retcode);
					String retdata = object.getString("data");
					log.info("返回数据：" + retdata);
					String retmessage = object.getString("message");
					log.info("返回信息：" + retmessage);
					String retsign = object.getString("signData");

					if ("0000".equals(retcode)) {
						log.debug("data = " + retdata);
						JSONArray list = new JSONArray(retdata);
						JSONObject data = list.getJSONObject(0);// new
																// JSONObject(retdata);

						String tpdu = object.getString("tpdu");
						String batchNo = object.getString("batchNo");
						String voucherNo = object.getString("voucherNo");
						String getCertUrl = object.getString("getCertUrl");
						String tranUrl = object.getString("tranUrl");

						String merId = data.getString("merId");
						String merName = data.getString("merName");
						String termId = data.getString("termId");
						String certPwd = data.getString("certPwd");
						String merAddr = data.getString("merAddr");
						String merTel = data.getString("merTel");

						String pubGetCertUrl = object.getString("pubGetCertUrl"); // 公网
						String pubTranUrl = object.getString("pubTranUrl"); // 公网

						byte[] capwd = Base64.decode(certPwd, Base64.DEFAULT);
						log.debug("capwd = " + HexUtil.bcd2str(capwd));
						log.debug("crypt = " + crypt);

						byte[] pwd = SecurityUtil.decrypt3Des(crypt.getBytes(), capwd);

						int len = pwd[0] & 0xFF;
						String pwdtext = HexUtil.bcd2str(pwd).substring(2, len + 2);
						log.debug("pwd = " + HexUtil.bcd2str(pwd));
						log.debug("pwdtext = " + pwdtext);

						Map<String, String> dataMap = new HashMap<String, String>();
						dataMap.put("merid", merId);
						dataMap.put("mchntname", merName); // 商户名称
						dataMap.put("termid", termId);
						dataMap.put("capwd", pwdtext);

						dataMap.put("tpdu", tpdu); // TPDU
						// dataMap.put("transIp",tranUrl); //交易地址
						dataMap.put("batchno", batchNo); // 批次号
						dataMap.put("billno", voucherNo); // 凭证号
						// dataMap.put("caIp",getCertUrl); //证书地址
						dataMap.put("merAddr", merAddr); // 商户地址
						dataMap.put("merTel", merTel); // 商户电话

						if ("0".equals(connectMode)) {
							dataMap.put("transIp", tranUrl); // 交易地址
							dataMap.put("caIp", getCertUrl); // 证书地址
						} else if ("1".equals(connectMode)) {
							dataMap.put("transIp", pubTranUrl); // 公网交易地址
							dataMap.put("caIp", pubGetCertUrl); // 公网证书地址
						}

						// if("200021".equals(paramConfigDao.get("fid"))){ //测试
						if ("test".equals(paramConfigDao.get("env"))) { // 测试
							dataMap.put("test_3gapn_tranUrl", tranUrl); // 交易地址
							dataMap.put("test_3gapn_certUrl", getCertUrl);// 证书地址

							dataMap.put("test_public_tranUrl", pubTranUrl); // 交易地址
							dataMap.put("test_public_certUrl", pubGetCertUrl);// 证书地址
						} else {
							dataMap.put("produce_3gapn_tranUrl", tranUrl);
							dataMap.put("produce_3gapn_certUrl", getCertUrl);

							dataMap.put("produce_public_tranUrl", pubTranUrl);
							dataMap.put("produce_public_certUrl", pubGetCertUrl);
						}
						int result = paramConfigDao.save(dataMap);

						activiteMap.putAll(dataMap);

						for (Iterator<String> it = object.keys(); it.hasNext();) {
							String key = it.next().toString();
							activiteMap.put(key, object.getString(key));
						}
						// saveShared_prefs(dataMap);

						String tmk = data.getString("tmk");
						log.debug("tmk = " + tmk);
						String pinpadType = paramConfigDao.get("pinpadType");
						PinPadInterface pinPadDev = null;
						if ("0".equals(pinpadType)) {
							try {
								pinPadDev = new ExPinPadDevJsIfc(context, null);
							} catch (Exception e) {
							}
						} else {
							try {
								pinPadDev = new PinPadDevJsIfc(context, null);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						String newmkeyid = paramConfigDao.get("newmkeyid");
						log.debug("主密钥索引 = " + newmkeyid);
						byte mkeyid = (byte) Integer.parseInt(newmkeyid); // 主密钥索引
						byte[] pmk = M3HexUtil.hexStringToByte(tmk);

						byte[] mKey = SecurityUtil.decrypt3Des(crypt.getBytes(), pmk);
						log.debug("mKey =" + HexUtil.bcd2str(mKey));
						try {
							pinPadDev.operDownloadMkey(mkeyid, mKey);
						} catch (Exception e) {
							log.error("主密钥注入失败", e);
							throw new ExPinPadException("主密钥注入失败");
						}

					} else {
						mBundle.putBoolean("isSuc", false);
						mBundle.putString("retCode", retcode);
						mBundle.putString("errMsg", retmessage);
						messageSendResult(mBundle);
						return;
					}
				} catch (Exception e) {
					dealExcepForOneShot(e, mBundle);
					messageSendResult(mBundle);
					log.error("下载商终信息失败", e);
					e.printStackTrace();
					return;
				}
				log.info("---------------下载商终信息成功--------------");
				messageSendProgress(0xa0);
			}
		}.start();
	}

	/**
	 * 一键激活第2步 下载证书
	 */
	public void merCertDownload() {
		new Thread() {
			public void run() {
				messageSendProgress(0xb1); // 发送消息到networkactivity开始倒计时
				String uriAPI = null;
				try {
					uriAPI = paramConfigDao.get("caIp");
					if (uriAPI == null || "".equals(uriAPI)) {
						throw new NullConnectAddressException("证书下载地址未设置，请先设置");
					}
					log.debug("下载证书地址 = [" + uriAPI + "]");
				} catch (Exception e) {
					log.warn("下载证书地址未设置", e);
					e.printStackTrace();
					dealExcepForOneShot(e, mBundle);
					messageSendResult(mBundle);
					return;
				}
				String merId = paramConfigDao.get("merid");
				log.info("----------------------merid:" + merId);
				String termId = paramConfigDao.get("termid");
				String password = paramConfigDao.get("capwd");

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("merId", merId));// "860010030210008"
				params.add(new BasicNameValuePair("termId", termId)); // "01000007"
				String pwd = MD5Util.getStringMD5(password).toUpperCase();
				params.add(new BasicNameValuePair("password", pwd));
				// log.debug("params1=" +merId );
				// log.debug("params1=" + termId);
				// log.debug("params1=" + password);

				try {
					String result = "{}";
					try {
						result = postUrl(uriAPI, params, "utf-8");
					} catch (Exception e) {
						e.printStackTrace();
						log.error("下载证书异常", e);
						mBundle.putString("retCode", "E001");
						messageSendResult(mBundle);
						return;
						// throw new Exception("证书下载失败,请联系客服!");
					}
					// Thread.sleep(2000);
					// String result = "
					// {\"code\":\"0000\",\"data\":\"MIIGOQIBAzCCBf8GCSqGSIb3DQEHAaCCBfAEggXsMIIF6DCCAucGCSqGSIb3DQEHBqCCAtgwggLU\\r\\nAgEAMIICzQYJKoZIhvcNAQcBMBwGCiqGSIb3DQEMAQYwDgQI\\/G\\/BDdMFecsCAggAgIICoK3CtdW3\\r\\nnkJdMN+LneqjAO52ok4ACYn7+W32\\/LB1nFqVc1bt+I4EJRBYteVeDyaNyRbmmvBeLn4w1gDEs7gV\\r\\nZ4B\\/iveJD5U0+8iVQGb0qOuq\\/nA5BzofcPbHle\\/5Rs6Nzumvgs\\/54BJIM3+tKL6\\/pVXfq7AoL3nF\\r\\nD4c+v5gvk21gwk4h+exXQm4khue2ibywcbYoa3x9+dRS42We6ZO8kKN1ZwI6zONEw88dUUJbAm46\\r\\nkmm7oUc\\/v6wZZ6ML7GryDwFqzVVu2DlNqInKa1C31IieFGqNfeSwCR6RVWLJBScSXlzRIWZ9dzhO\\r\\noaxWgvho1RqOk\\/OdBkd87irPqyv0WKoMurRsDjZq\\/qvyk57EqTUKRYg4ScCBSrlcB5RFebYm538a\\r\\nuUcz9L3ozG2sgiaZs5Aqa1BU86B0dVmoxPO28R6ya3\\/FZPZwtYVoV2ZPP3m5FuL8QVa6e9a4P5FL\\r\\nQgOJ4p4lHgesm0MlwGGiTVcHLATph2rhB37cNl4Gmx+PceqjGyj\\/cv\\/tg8k2EjdNKpBM\\/t1sdyGS\\r\\naFfqXgCiKKohbMWm0EzztaDtW6Q4Wz50xdL3PjbuhAiuSk2jxBbjCNuieBNzpJhdFZzEy31fTbDi\\r\\nclCM+SlAK8vQU63aya1wjZIG1o5vouPAVseJze8eYOs8RyLrq1nFmMiC1fi0Gth7h8MUEFl6dGMV\\r\\nfpYC\\/W7gWm3Ro6I3V+9g9yU5KwJPApcqb8oen1S6UTUSgqGA6n6C27OIv+TKJpHk6R1neldMVoB5\\r\\naiKtGCUCjoc\\/VTPKB2StmMqs82ZJeMoE17ksWuUrN0dOVB2Qi3Dauc4Mc7vXpL8NKfq8TxA9Fa+M\\r\\n7ILDpOkHj+heY0hF4cnFAy\\/cPjXxWrp7V9w3JVisyAyDiYaZexsaATCCAvkGCSqGSIb3DQEHAaCC\\r\\nAuoEggLmMIIC4jCCAt4GCyqGSIb3DQEMCgECoIICpjCCAqIwHAYKKoZIhvcNAQwBAzAOBAiiSrFn\\r\\nle8PCwICCAAEggKAcXuKYUbzoDjgskjbwnP2S1YKFTqxXfw810K6GZqyf7iOLEc\\/fpaGrV3DQqfq\\r\\nX+nvSgGj10am\\/BsTavDIWc3jbNvWXdFVKJtQ0WpqGuEF5RzlA6luqUZD5ye4eXyj7ZgGdIkgZGlV\\r\\nLNFrbMkhz5M\\/+lHSJE0hDvQM0l47FxQzmnXc5+lZGPnCiNTPiFd6IMic3YpA\\/U2G6hveNo\\/Zkoyr\\r\\n8E3FqTAPqsk5BzTSAET2uddVj+joRI7PKtHyl1ERPwmV9Qqe0AfbBso\\/Ls6rCdTsdqVTbe1SHyPm\\r\\nlt1+UbBupF8puuQNk88Pgktmus0qk2\\/h13\\/NzfhnP+PKDagywlA3XYYRXaC6H6KBlr1AogpzXHqi\\r\\n+SXJDSEUFQfpLkwgTZrZ1d+Slea1GcZjvWYIrqYfNg5pNtFQHf9t1KbEyXziD7X+X8yTAiF1NsTP\\r\\nBy7yUyGPuhdU3h0GpmakNU8\\/bGAsyrXdmqW++9LygKUVjGonQayIIW0IfoY9mvsrUfmj0paMbFIy\\r\\n5zAvXSfpabidIg66KL89w8tNCAAmI7HMPPyOL2qQhfYW9Y53OkJMObNe4x\\/a55DgIqCgUyMfLUpu\\r\\nsKpLD2edxptuIMOucqI7M8CdFqNQoWZoEYxhjpvHEy5K4VWs2FbwO0\\/3jkjm3hwiGMSN1yZRZCyR\\r\\nz9TZO4joZ00TbQ0l+wQOXoAwXC0nQSD0SD72AlrgTQ+tPOAZaSWOhJNsyudHzN2NcRczhkEMBUCJ\\r\\nLiXeGK9sNBQJUrW0yKIPBxJeiuNFLDzjm8pP4RgEzl9cxoKo7qjzOodLRglfgTI5xqVKyPNfVz9h\\r\\nGZiTigmBqGwfZXUtdvCceVA3nIwLQEOJezElMCMGCSqGSIb3DQEJFTEWBBR2Q2y0r01cD2mEFZ4p\\r\\ngpNm27eO9zAxMCEwCQYFKw4DAhoFAAQUnj8MBcN6SxCuK3elkJpkLMG0PloECB1EgBF8+xwKAgII\\r\\nAA==\\r\\n\",\"message\":\"\",\"signData\":\"yGs\\/o4nTDPAFAo57hGEXsg==\\r\\n\"}";

					log.debug("前置返回证书数据 =" + result);
					JSONObject object = new JSONObject(result);
					String retcode = object.getString("code");
					String retdata = object.getString("data");
					String retmessage = object.getString("message");
					String retsign = object.getString("signData");
					// log.debug("code=[" + retcode + "]");
					// log.debug("data=[" + retdata + "]");
					// log.debug("message=[" + retmessage + "]");
					// log.debug("signData=[" + retsign + "]");
					if ("0000".equals(retcode)) {// 成功
						Log.i("ckh", "downloadcer success");
						byte[] retbyte = Base64.decode(retdata, Base64.DEFAULT);
						byte[] retsignbyte = Base64.decode(retsign, Base64.DEFAULT);
						LklAndroidClient.getInstance(context).creatClientP12(retbyte);// 保存证书
						paramConfigDao.save("capwd", password);
						log.info("证书下载成功....");
						mBundle.putBoolean("isSuc", true);
						LklAndroidClient.setInstance(null);

						activiteMap.put("certData", retdata);
						Utility.saveShared_prefs(context, activiteMap);
					} else { // 失败
						Log.i("ckh", "downloadcer fail");
						log.error("证书下载错误原因:[" + retmessage + "]");
						mBundle.putBoolean("isSuc", false);
						mBundle.putString("retCode", retcode);
						mBundle.putString("errMsg", retmessage);
						messageSendResult(mBundle);
						return;
					}
				} catch (Exception e) {
					Log.e("ckh", "下载证书发生异常..");
					e.printStackTrace();
					dealExcepForOneShot(e, mBundle);
					messageSendResult(mBundle);
					return;
				}
				// messageSendProgress(0xb0);
				log.info("---------------下载证书成功--------------");
				messageSendResult(mBundle);
			}
		}.start();
	}

	/**
	 * 一键激活第3步 下载主密钥
	 */
	public void masterKeyDownload() {
		new Thread() {
			public void run() {
				String uriAPI = null;
				messageSendProgress(0xc1);
				try {
					uriAPI = paramConfigDao.get("MasterKeySev");
					if (uriAPI == null || "".equals(uriAPI)) {
						throw new NullConnectAddressException("主密钥下载地址未设置，请先设置");
					}
					log.debug("下载主密钥地址 = [" + uriAPI + "]");
				} catch (Exception e) {
					Log.i("ckh", "主密钥下载地址未设置。。。");
					e.printStackTrace();
					dealExcepForOneShot(e, mBundle);
					messageSendResult(mBundle);
					return;
				}

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				String crypt = Utility.getCryptData(); // 随机密钥
				try {
					byte[] cryptData = LklAndroidClient.getInstance(context).encrypeByPubKey(crypt.getBytes("UTF-8"));
					params.add(new BasicNameValuePair("cryptData", Base64.encodeToString(cryptData, Base64.DEFAULT)));
					String pwd = paramConfigDao.get("capwd");
					String signData = LklAndroidClient.getInstance(context).getSignData(cryptData, pwd);// coder.sign(cryptoByte,
																										// certInfoModual);
					params.add(new BasicNameValuePair("signData", signData));
					String cname = LklAndroidClient.getInstance(context).getCname(pwd);
					params.add(new BasicNameValuePair("cname", cname)); // 860010030210008-50000004
				} catch (Exception e1) {
					log.error("数据加密失败", e1);
					e1.printStackTrace();
					mBundle.putBoolean("isSuc", false);
					mBundle.putString("errMsg", "数据安全加密失败");
					messageSendResult(mBundle);
					return;
				}
				String merid = paramConfigDao.get("merid");
				String termid = paramConfigDao.get("termid");

				params.add(new BasicNameValuePair("transCode", "0002"));
				params.add(new BasicNameValuePair("merId", merid)); // 860010030210008
				params.add(new BasicNameValuePair("termId", termid)); // 50000004
				params.add(new BasicNameValuePair("channel", "PADPOS")); // NETPOS
																			// 、PADPOS

				log.debug("随机密钥= " + crypt);
				for (NameValuePair nameValuePair : params) {
					log.debug("参数 = " + nameValuePair.getName() + "||" + nameValuePair.getValue());
				}

				try {
					String strResult = "{}";
					try {
						strResult = postUrl(uriAPI, params, "utf-8");
					} catch (Exception e1) {
						e1.printStackTrace();
						log.error("下载主密钥异常", e1);
						throw e1;
						// throw new Exception("主密钥下载失败,请联系客服!");
					}
					// Thread.sleep(2000);
					// String strResult
					// ="{\"code\":\"0000\",\"data\":{\"certPwd\":\"\",\"merId\":\"\",\"merName\":\"\",\"pmk\":\"869E86A702918F3D51BCEC040BF4FD3D\",\"termId\":\"\"},\"message\":\"交易成功\",\"signData\":\"\"}";
					log.debug("前置返回主密钥数据 =" + strResult);
					JSONObject object = new JSONObject(strResult);
					String retcode = object.getString("code");
					String retdata = object.getString("data");
					String retmessage = object.getString("message");
					String retsign = object.getString("signData");

					if ("0000".equals(retcode)) {
						// JSONObject data = new JSONObject(retdata);
						JSONArray list = new JSONArray(retdata);
						JSONObject data = list.getJSONObject(0);
						String tmk = data.getString("tmk");
						log.debug("tmk = " + tmk);
						String pinpadType = paramConfigDao.get("pinpadType");
						PinPadInterface pinPadDev = null;
						if ("0".equals(pinpadType)) {
							try {
								pinPadDev = new ExPinPadDevJsIfc(context, null);
							} catch (Exception e) {
								log.error("", e);
							}
						} else {
							try {
								pinPadDev = new PinPadDevJsIfc(context, null);
							} catch (Exception e) {
								e.printStackTrace();
								log.error("", e);
							}
						}
						String newmkeyid = paramConfigDao.get("newmkeyid");
						log.debug("主密钥索引 = " + newmkeyid);
						// String tmkKeyId_SetOff =
						// paramConfigDao.get("tmkKeyId_setOff");
						// tmkKeyId_SetOff =
						// paramConfigDao.get("tmkKeyId_setOff");
						// if ("".equals(tmkKeyId_SetOff) || tmkKeyId_SetOff ==
						// null) {
						// tmkKeyId_SetOff = "1";
						// }
						byte mkeyid = (byte) Integer.parseInt(newmkeyid); // 主密钥索引
						byte[] pmk = M3HexUtil.hexStringToByte(tmk);

						byte[] mKey = SecurityUtil.decrypt3Des(crypt.getBytes(), pmk);
						log.debug("mKey =" + HexUtil.bcd2str(mKey));

						try {
							pinPadDev.operDownloadMkey(mkeyid, mKey);
						} catch (Exception e) {
							throw new ExPinPadException("主密钥注入失败");
						}
						// log.debug("主密钥索引 = " + newmkeyid );
						// log.debug("主密钥索引偏移 = " + tmkKeyId_SetOff);
						mBundle.putBoolean("isSuc", true);
					} else {
						mBundle.putBoolean("isSuc", false);
						mBundle.putString("retCode", retcode);
						mBundle.putString("errMsg", retmessage);
					}
				} catch (Exception e) {
					dealExcepForOneShot(e, mBundle);
					log.error("下载主密钥信息失败", e);
					e.printStackTrace();
				}
				messageSendResult(mBundle);
				// messageSendProgress(0xc0);
			}
		}.start();
	}

	/*
	 * 用于原生态
	 */
	private void dealExcepForOneShot(Exception e, Bundle b) {
		try {
			String retCode = "E099";
			if (e instanceof JSONException) {
				retCode = "E005";
			} else if (e instanceof HttpStatusException) {
				retCode = "E001";
			} else if (e instanceof ConnectException) {
				retCode = "E001";
			} else if (e instanceof SocketException) {
				retCode = "E001";
			} else if (e instanceof SocketTimeoutException) {
				retCode = "E001";
			} else if (e instanceof ConnectTimeoutException) {
				retCode = "E001";
			} else if (e instanceof FileNotFoundException) {
				retCode = "E002";
			} else if (e instanceof ExPinPadException) {
				retCode = "E003";
			} else if (e instanceof NullConnectAddressException) {
				retCode = "E004";
			}
			b.putBoolean("isSuc", false);
			b.putString("retCode", retCode);
		} catch (Exception e1) {
			log.error("处理异常时发生错误..", e1);
			e1.printStackTrace();
		}
	}
}
