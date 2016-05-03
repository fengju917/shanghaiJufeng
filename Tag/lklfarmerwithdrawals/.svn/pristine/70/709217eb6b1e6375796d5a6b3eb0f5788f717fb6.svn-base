package com.centerm.lklcpos.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.crypto.Cipher;

public class CertificateUtil {

	private static final int CACHE_SIZE = 2048;// 锟侥硷拷锟斤拷取锟斤拷锟斤拷锟斤拷锟叫?
	private static final int MAX_ENCRYPT_BLOCK = 117;// 锟斤拷锟斤拷募锟斤拷锟斤拷芸锟?
	private static final int MAX_DECRYPT_BLOCK = 128;// 锟斤拷锟斤拷募锟斤拷锟斤拷芸锟?

	public static final String PROVIDER_SUN = "SUN";// JKS锟斤拷锟酵碉拷锟结供锟斤拷
	public static final String PROVIDER_BC = "BC";// BKS锟斤拷锟酵碉拷锟结供锟斤拷
	public static final String PROVIDER_SUNJCE = "SunJCE";// JCEKS锟斤拷锟酵碉拷锟结供锟斤拷
	public static final String PROVIDER_SUNJSSE = "SunJSSE";// PKCS12锟斤拷锟酵碉拷锟结供锟斤拷

	public static final String KEYSTORE_JKS = "JKS";// JKS锟斤拷锟酵碉拷锟杰匡拷
	public static final String KEYSTORE_BKS = "BKS";// BKS锟斤拷锟酵碉拷锟杰匡拷
	public static final String KEYSTORE_PKCS12 = "PKCS12";// PKCS12锟斤拷锟酵碉拷锟杰匡拷

	public static final String CERTIFICATE_X509 = "X.509";// X.509锟斤拷锟斤拷d锟侥癸拷钥证锟斤拷

	public static PrivateKey getPrivateKey(String keyStorePath, String keyStoreType, String alias, String password)
			throws Exception {
		KeyStore keyStore = getKeyStore(keyStorePath, keyStoreType, password);
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
		return privateKey;
	}

	public static KeyStore getKeyStore(String keyStorePath, String keyStoreType, String password) throws Exception {
		FileInputStream in = new FileInputStream(keyStorePath);
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(in, password.toCharArray());
		in.close();
		return keyStore;
	}

	public static PublicKey getPublicKey(String certificatePath) throws Exception {
		Certificate certificate = getCertificate(certificatePath);
		PublicKey publicKey = certificate.getPublicKey();
		return publicKey;
	}

	public static PublicKey getPublicKey(String keyStorePath, String keyStoreType, String alias, String password)
			throws Exception {
		Certificate certificate = getCertificate(keyStorePath, keyStoreType, alias, password);
		PublicKey publicKey = certificate.getPublicKey();
		return publicKey;
	}

	public static Certificate getCertificate(String certificatePath) throws Exception {
		CertificateFactory certificateFactory = CertificateFactory.getInstance(CERTIFICATE_X509);
		FileInputStream in = new FileInputStream(certificatePath);
		Certificate certificate = certificateFactory.generateCertificate(in);
		in.close();
		return certificate;
	}

	public static Certificate getCertificate(String keyStorePath, String keyStoreType, String alias, String password)
			throws Exception {
		KeyStore keyStore = getKeyStore(keyStorePath, keyStoreType, password);
		Certificate certificate = keyStore.getCertificate(alias);
		return certificate;
	}

	public static byte[] encryptByPrivateKey(byte[] data, String keyStorePath, String keyStoreType, String alias,
			String password) throws Exception {
		PrivateKey privateKey = getPrivateKey(keyStorePath, keyStoreType, alias, password);
		Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
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

	public static void encryptFileByPrivateKey(String srcFilePath, String destFilePath, String keyStorePath,
			String keyStoreType, String alias, String password) throws Exception {
		PrivateKey privateKey = getPrivateKey(keyStorePath, keyStoreType, alias, password);
		Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		File srcFile = new File(srcFilePath);
		FileInputStream in = new FileInputStream(srcFile);
		File destFile = new File(destFilePath);
		if (!destFile.getParentFile().exists()) {
			destFile.getParentFile().mkdirs();
		}
		destFile.createNewFile();
		OutputStream out = new FileOutputStream(destFile);
		byte[] data = new byte[MAX_ENCRYPT_BLOCK];
		byte[] encryptedData; // 锟斤拷锟杰匡拷
		while (in.read(data) != -1) {
			encryptedData = cipher.doFinal(data);
			out.write(encryptedData, 0, encryptedData.length);
			out.flush();
		}
		out.close();
		in.close();
	}

	public static byte[] decryptByPrivateKey(byte[] encryptedData, String keyStorePath, String keyStoreType,
			String alias, String password) throws Exception {
		PrivateKey privateKey = getPrivateKey(keyStorePath, keyStoreType, alias, password);
		Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		int inputLen = encryptedData.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}

	public static byte[] encryptByPublicKey(byte[] data, String keyStorePath, String keyStoreType, String alias,
			String password) throws Exception {
		PublicKey publicKey = getPublicKey(keyStorePath, keyStoreType, alias, password);
		Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
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

}