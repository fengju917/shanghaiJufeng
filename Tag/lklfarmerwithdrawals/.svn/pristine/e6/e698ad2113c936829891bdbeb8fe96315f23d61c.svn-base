/**
 * 2012-6-28
 * @author 詹惠菁
 * 加解密模块程序
 */
package com.lakala.android.security;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.crypto.Cipher;
import org.apache.commons.lang.StringUtils;

/**
 * 证书组件
 * 
 * @author 詹惠菁
 * @version 1.0
 */
public class SecurityKit {

	/**
	 * 证书类型X509
	 */
	private static final String CERT_TYPE = "X.509";

	/**
	 * 密钥库类型PCKS12
	 */
	private static final String STORE_TYPE = "PKCS12";

	/**
	 * 客户端私钥
	 */
	private PrivateKey privateKey;

	/**
	 * 客户端证书
	 */
	private X509Certificate certificate;

	/**
	 * RSA最大加密明文大小
	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/**
	 * RSA最大解密密文大小
	 */
	private static final int MAX_DECRYPT_BLOCK = 128;

	/**
	 * 由KeyStore获得私钥
	 * 
	 * @param keyStorePath
	 *            密钥库路径
	 * @param alias
	 *            别名
	 * @param password
	 *            密码
	 * @return PrivateKey 私钥
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public CertInfoModual getCertInfoByKeyStore(String certificatePath, String password) throws Exception {
		// 获得密钥库
		KeyStore ks = getKeyStore(certificatePath, password);
		CertInfoModual certInfo = new CertInfoModual();
		Enumeration keyenum = ks.aliases();
		String keyAlias = null;
		if (privateKey == null) {
			// 获得密钥库
			if (keyenum.hasMoreElements()) {
				keyAlias = (String) keyenum.nextElement();
			}
			privateKey = (PrivateKey) ks.getKey(keyAlias, password.toCharArray());
			certInfo.setPrivateKey(privateKey);
		}
		if (certificate == null) {
			certificate = (X509Certificate) ks.getCertificate(keyAlias);
		}
		// 获取证书subject信息
		String info = certificate.getSubjectX500Principal().getName();
		String[] commonName = info.split(",");
		for (int i = 0; i < commonName.length; i++) {
			if (commonName[i].startsWith("CN=")) {
				certInfo.setCommonName(commonName[i].substring(3, commonName[i].length()));
			}
		}
		return certInfo;
	}

	/**
	 * 由Certificate获得公钥
	 * 
	 * @param certificatePath
	 *            证书路径
	 * @return PublicKey 公钥
	 * @throws Exception
	 */
	public PublicKey getPublicKeyByCertificate(String certificatePath) throws Exception {
		// 获得证书
		Certificate certificate = getCertificate(certificatePath);
		// 获得公钥
		return certificate.getPublicKey();

	}

	/**
	 * 获得Certificate
	 * 
	 * @param certificatePath
	 *            证书路径
	 * @return Certificate 证书
	 * @throws Exception
	 */
	private X509Certificate getCertificate(String certificatePath) throws Exception {
		// 实例化证书工厂
		CertificateFactory certificateFactory = CertificateFactory.getInstance(CERT_TYPE);
		// 取得证书文件流
		FileInputStream in = new FileInputStream(certificatePath);
		// 生成证书
		Certificate certificate = certificateFactory.generateCertificate(in);
		// 关闭证书文件流
		in.close();
		return (X509Certificate) certificate;
	}

	/**
	 * 获得KeyStore
	 * 
	 * @param keyStorePath
	 *            密钥库路径
	 * @param password
	 *            密码
	 * @return KeyStore 密钥库
	 * @throws Exception
	 */
	private KeyStore getKeyStore(String keyStorePath, String password) throws Exception {
		if (StringUtils.isEmpty(keyStorePath) || StringUtils.isEmpty(password)) {
			return null;
		}
		// 实例化密钥库
		KeyStore ks = KeyStore.getInstance(STORE_TYPE);
		// 获得密钥库文件流
		FileInputStream fis = new FileInputStream(keyStorePath);
		// 加载密钥库
		ks.load(fis, password.toCharArray());
		// 关闭密钥库文件流
		fis.close();
		return ks;
	}

	/**
	 * 公钥加密
	 * 
	 * @param data
	 *            待加密数据
	 * @param certPath
	 *            服务器公钥文件路径
	 * @return byte[] 加密数据
	 * @throws Exception
	 */
	public byte[] encryptByPublicKey(byte[] data, String certPath) throws Exception {

		// 取得公钥
		PublicKey publicKey = getPublicKeyByCertificate(certPath);
		if (publicKey == null) {
			return null;
		}
		// 对数据加密
		Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	/**
	 * 私钥解密
	 * 
	 * @param data
	 *            待解密数据
	 * @param certificatePath
	 *            客户端证书路径
	 * @param alias
	 *            别名
	 * @param password
	 *            密码
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	public byte[] decryptByPrivateKey(byte[] data, String certificatePath, String password) throws Exception {
		if (privateKey == null) {
			// 取得私钥
			privateKey = getCertInfoByKeyStore(certificatePath, password).getPrivateKey();
		}
		// 对数据加密
		Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}

	/**
	 * 签名
	 * 
	 * @param keyStorePath
	 *            密钥库路径
	 * @param alias
	 *            别名
	 * @param password
	 *            密码
	 * @return byte[] 签名
	 * @throws Exception
	 */
	public byte[] sign(byte[] data, CertInfoModual certInfoModual) throws Exception {
		// 构建签名，由证书指定签名算法
		Signature signature = Signature.getInstance("SHA1withRSA");
		if (privateKey == null) {
			// 获取私钥
			privateKey = certInfoModual.getPrivateKey();
		}
		// 初始化签名，由私钥构建
		signature.initSign(privateKey);
		signature.update(data);
		return signature.sign();
	}

	/**
	 * 验证签名
	 * 
	 * @param data
	 *            数据
	 * @param sign
	 *            签名
	 * @param certificatePath
	 *            证书路径
	 * @return boolean 验证通过为真
	 * @throws Exception
	 */
	public boolean verify(byte[] data, byte[] signdata, String certPath) throws Exception {
		// 获取证书
		X509Certificate x509Certificate = getCertificate(certPath);
		// 由证书构建签名
		Signature signature = Signature.getInstance("SHA1withRSA");
		// 由证书初始化签名，实际上是使用了证书中的公钥
		signature.initVerify(x509Certificate);
		signature.update(data);
		return signature.verify(signdata);
	}

}