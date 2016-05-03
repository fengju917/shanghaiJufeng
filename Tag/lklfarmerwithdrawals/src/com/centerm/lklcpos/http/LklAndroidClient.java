package com.centerm.lklcpos.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.lakala.android.security.CertInfoModual;
import com.lakala.android.security.SecurityKit;

/**
 * 拉卡拉Android客户端
 * 
 * @author Linpeng
 * @version 1.0
 */
public class LklAndroidClient {
	private static final String TAG = "LklAndroidClient";

	private static final Logger log = Logger.getLogger(LklAndroidClient.class);

	/**
	 * 拉卡拉互联网前置公钥证书初始存放路径，即：assets/cer/server.cer
	 */
	private static final String scerInit = "cer/server.cer";

	/**
	 * 单例对象
	 */
	private static LklAndroidClient instance = null;

	/**
	 * 拉卡拉互联网前置公钥证书路径
	 */
	private String scer = null;

	/**
	 * 客户端私钥证书路径
	 */
	private String p12 = null;

	/**
	 * 拉卡拉证书组件
	 */
	private SecurityKit kit = null;

	public synchronized static LklAndroidClient getInstance(Context context) throws Exception {
		if (instance == null) {
			try {
				instance = new LklAndroidClient(context);
			} catch (Exception e) {
				log.error("实例 LklAndroidClient 对象时出现错误...", e);

				throw e;
			}
		}

		return instance;
	}

	/**
	 * 初始化公钥证书、私钥证书等路径，并将“assets/cer”目录下的拉卡拉互联网前置公钥证书拷贝至设定目录中 默认拉卡拉互联网前置公钥证书路径：
	 * /data/data/<package name>/files/server.cer 默认客户端私钥证书路径：
	 * /data/data/<package name>/files/client.p12
	 * 
	 * @param context
	 * @throws Exception
	 */
	private LklAndroidClient(Context context) throws Exception {
		// 设置默认拉卡拉互联网前置公钥证书路径
		this.scer = context.getFilesDir().getAbsolutePath() + "/server.cer";
		// 并将“assets/cer”目录下的拉卡拉互联网前置公钥证书拷贝至设定目录中
		try {
			InputStream scerIn = context.getResources().getAssets().open(scerInit);
			byte[] scerBuffer = new byte[scerIn.available()];
			scerIn.read(scerBuffer);
			scerIn.close();
			creatScer(scerBuffer);
		} catch (IOException e) {
			log.error("open or read scer[" + scerInit + "] but catch IOException...", e);

			throw new Exception("open or read scer[" + scerInit + "] but catch IOException...");
		} catch (Exception e) {
			log.error("creatScer() but catch Exception...", e);
			throw e;
		}

		this.p12 = context.getFilesDir().getAbsolutePath() + "/client.p12";

		this.kit = new SecurityKit();
	}

	public String getScer() {
		return scer;
	}

	public String getP12() {
		return p12;
	}

	public static void setInstance(LklAndroidClient instance) {
		LklAndroidClient.instance = instance;
	}

	/**
	 * 获取向拉卡拉互联网前置发送的数据
	 * 
	 * @param msg
	 *            报文数据[in]
	 * @param password
	 *            客户端私钥证书密码[in]
	 * @return 向拉卡拉互联网前置发送的数据[out]
	 * @throws Exception
	 */
	public List<NameValuePair> getSendData(byte[] msg, String password) throws Exception {
		String endata = null;
		String signdata = null;
		String cname = null;
		List<NameValuePair> params = null;

		try {
			// 校验 scer、p12 是否存在
			FileReader p12FileReader = new FileReader(p12);
			p12FileReader.close();
			FileReader scerFileReader = new FileReader(scer);
			scerFileReader.close();

			CertInfoModual certInfoModual = kit.getCertInfoByKeyStore(p12, password);
			// 前置公钥加密数据
			byte[] crypto = kit.encryptByPublicKey(msg, scer);

			endata = Base64.encodeToString(crypto, Base64.DEFAULT);
			// 客户端私钥签名
			signdata = Base64.encodeToString(kit.sign(crypto, certInfoModual), Base64.DEFAULT);
			// 获取客户端私钥用户名
			cname = certInfoModual.getCommonName();

			params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("endata", endata));
			params.add(new BasicNameValuePair("signdata", signdata));
			params.add(new BasicNameValuePair("cname", cname));
		} catch (Exception e) {
			log.error("获取向拉卡拉互联网前置发送的数据时出现错误", e);

			throw e;
		}

		return params;
	}

	// 使用前置公钥进行加密
	public byte[] encrypeByPubKey(byte[] msg) throws Exception {
		return kit.encryptByPublicKey(msg, scer);
	}

	public String getCname(String password) throws Exception {
		// 校验 scer、p12 是否存在
		FileReader p12FileReader = new FileReader(p12);
		p12FileReader.close();
		FileReader scerFileReader = new FileReader(scer);
		scerFileReader.close();
		CertInfoModual certInfoModual = kit.getCertInfoByKeyStore(p12, password);
		return certInfoModual.getCommonName();
	}

	// 对数据进行签名并返回签名值
	public String getSignData(byte[] msg, String password) throws Exception {
		// 校验 scer、p12 是否存在
		FileReader p12FileReader = new FileReader(p12);
		p12FileReader.close();
		FileReader scerFileReader = new FileReader(scer);
		scerFileReader.close();
		CertInfoModual certInfoModual = kit.getCertInfoByKeyStore(p12, password);
		// byte[] crypto = kit.encryptByPublicKey(msg, scer);
		return Base64.encodeToString(kit.sign(msg, certInfoModual), Base64.DEFAULT);
	}

	/**
	 * 验签拉卡拉互联网前置返回的数据，若成功则解密返回的8583报文数据
	 * 
	 * @param lklRetData
	 *            拉卡拉互联网前置返回的数据[in]
	 * @param password
	 *            客户端私钥证书密码[in]
	 * @param retMsg
	 *            解密后的8583报文数据[out]
	 * @return 验签结果: 成功 true 失败 false
	 * @throws Exception
	 */
	public boolean getRetMsg(String lklRetData, String password, String retMsg) throws Exception {
		retMsg = null;
		boolean v = false;

		try {
			// 校验 scer、p12 是否存在
			FileReader p12FileReader = new FileReader(p12);
			p12FileReader.close();
			FileReader scerFileReader = new FileReader(scer);
			scerFileReader.close();

			JSONObject object = new JSONObject(lklRetData);
			String retdata = object.getString("data");
			String retsign = object.getString("signData");
			byte[] retbyte = Base64.decode(retdata, Base64.DEFAULT);
			byte[] retsignbyte = Base64.decode(retsign, Base64.DEFAULT);
			Log.d(TAG, "retdata:" + retbyte);
			Log.d(TAG, "retsign:" + retsignbyte);

			// 拉卡拉互联网前置公钥验签
			v = kit.verify(retbyte, retsignbyte, scer);
			if (v) {
				Log.d(TAG, "验签成功");

				// 客户端私钥解密
				retMsg = new String(kit.decryptByPrivateKey(retbyte, p12, password));
				Log.d(TAG, "RetMsg:" + retMsg);
			} else {
				Log.d(TAG, "验签失败");
			}
		} catch (JSONException e) {
			log.error("格式化拉卡拉互联网前置返回的数据时，出现 JSONException 错误...", e);

			throw new Exception("格式化拉卡拉互联网前置返回的数据时，出现 JSONException 错误...");
		} catch (Exception e) {
			log.error("验签拉卡拉互联网前置返回的数据或解密返回的8583报文数据时出现错误", e);

			throw e;
		}

		return v;
	}

	/**
	 * 在指定路径下（重新）生成密钥证书文件
	 * 
	 * @param cerPath
	 *            密钥证书路径[in]
	 * @param buffer
	 *            密钥证书数据[in]
	 * @throws Exception
	 */
	private void creatCer(String cerPath, byte[] buffer) throws Exception {
		try {
			FileOutputStream fout = new FileOutputStream(cerPath);
			fout.write(buffer);
			fout.close();
		} catch (FileNotFoundException e) {
			log.error("open or create [" + cerPath + "], but throw FileNotFoundException...", e);

			throw new Exception("open or create [" + cerPath + "], but throw FileNotFoundException...");
		} catch (IOException e) {
			log.error("write or close [" + cerPath + "], but throw IOException...", e);

			throw new Exception("write or close [" + cerPath + "], but throw IOException...");
		}
		return;
	}

	/**
	 * 在 /data/data/<package name>/files/ 目录下（重新）生成客户端私钥证书
	 * 
	 * @param buffer
	 *            客户端私钥证书数据[in]
	 * @throws Exception
	 */
	public void creatClientP12(byte[] buffer) throws Exception {
		creatCer(p12, buffer);
	}

	public int deleteClientP12() throws IOException {

		File p12File = new File(p12);
		boolean flag = p12File.delete();

		Log.d("delete p12", "flag = " + flag);
		return p12File.exists() ? -1 : 1;
	}

	/**
	 * 在 /data/data/<package name>/files/ 目录下（重新）生成拉卡拉互联网前置公钥证书
	 * 
	 * @param buffer
	 *            拉卡拉互联网前置公钥证书数据[in]
	 * @throws Exception
	 */
	public void creatScer(byte[] buffer) throws Exception {
		creatCer(scer, buffer);
	}
}