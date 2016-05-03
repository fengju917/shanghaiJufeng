package com.centerm.lklcpos.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.security.cert.X509Certificate;

public class ExCertificateUtil extends CertificateUtil {

	public static PrivateKey getPrivateKey(String keyStorePath, String keyStoreType, String password, String alias,
			String keypass) throws Exception {
		KeyStore keyStore = getKeyStore(keyStorePath, keyStoreType, password);
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keypass.toCharArray());
		return privateKey;
	}

	public static Certificate getCertificate(String certificatePath, String certificateType) throws Exception {
		CertificateFactory certificateFactory = CertificateFactory.getInstance(certificateType);
		FileInputStream in = new FileInputStream(certificatePath);
		Certificate certificate = certificateFactory.generateCertificate(in);
		in.close();
		return certificate;
	}

	public static String getSerialNumber(String keyStorePath, String keyStoreType, String alias, String password)
			throws Exception {
		Certificate certificate = getCertificate(keyStorePath, keyStoreType, alias, password);
		X509Certificate xCertificate = X509Certificate.getInstance(certificate.getEncoded());
		return xCertificate.getSerialNumber().toString();
	}

	public static KeyStore getEmptyKeyStore(String keyStoreType) throws Exception {
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(null, null);
		return keyStore;
	}

	public static KeyStore makeEmptyKeyStore(String keyStorePath, String keyStoreType, String password)
			throws Exception {
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(null, null);
		FileOutputStream output = new FileOutputStream(keyStorePath);
		keyStore.store(output, password.toCharArray());
		output.close();
		return keyStore;
	}

	public static KeyStore makeEmptyKeyStore(String keyStorePath, String provider, String keyStoreType, String password)
			throws Exception {
		KeyStore keyStore = KeyStore.getInstance(keyStoreType, provider);
		keyStore.load(null, null);
		FileOutputStream output = new FileOutputStream(keyStorePath);
		keyStore.store(output, password.toCharArray());
		output.close();
		return keyStore;
	}

	public static List<Certificate> getCertificatesFromKeyStore(String keyStorePath, String keyStoreType,
			String password) throws Exception {
		List<Certificate> list = new ArrayList<Certificate>();
		KeyStore keyStore = getKeyStore(keyStorePath, keyStoreType, password);
		Enumeration e = keyStore.aliases();
		while (e.hasMoreElements()) {
			String alias = e.nextElement().toString();
			Certificate c = keyStore.getCertificate(alias);
			list.add(c);
		}
		return list;
	}

	public static KeyStore trustCertificate(String certificatePath, String keyStorePath, String keyStoreType,
			String alias, String password) throws Exception {
		Certificate certificate = getCertificate(certificatePath);
		KeyStore keyStore = getKeyStore(keyStorePath, keyStoreType, password);
		keyStore.setCertificateEntry(alias, certificate);
		FileOutputStream output = new FileOutputStream(keyStorePath);
		keyStore.store(output, password.toCharArray());
		output.close();
		return keyStore;
	}

	public static KeyStore trustCertificate(String iKeyStorePath, String iKeyStoreType, String iAlias, String iPassword,
			String oKeyStorePath, String oKeyStoreType, String oAlias, String oPassword) throws Exception {
		Certificate certificate = getCertificate(iKeyStorePath, iKeyStoreType, iAlias, iPassword);
		KeyStore keyStore = getKeyStore(oKeyStorePath, oKeyStoreType, oPassword);
		keyStore.setCertificateEntry(oAlias, certificate);
		FileOutputStream output = new FileOutputStream(oKeyStorePath);
		keyStore.store(output, oPassword.toCharArray());
		output.close();
		return keyStore;
	}

	public static KeyStore addPrivateCertificate(String iKeyStorePath, String iKeyStoreType, String iAlias,
			String iPassword, String oldPassword, String oKeyStorePath, String oKeyStoreType, String oAlias,
			String oPassword, String newPassword) throws Exception {
		KeyStore inKeyStore = getKeyStore(iKeyStorePath, iKeyStoreType, iPassword);
		KeyStore outKeyStore = getKeyStore(oKeyStorePath, oKeyStoreType, oPassword);
		Certificate[] chain = inKeyStore.getCertificateChain(iAlias);
		PrivateKey privateKey = (PrivateKey) inKeyStore.getKey(iAlias, oldPassword.toCharArray());
		outKeyStore.setKeyEntry(oAlias, privateKey, newPassword.toCharArray(), chain);
		FileOutputStream output = new FileOutputStream(oKeyStorePath);
		outKeyStore.store(output, oPassword.toCharArray());
		output.close();
		return outKeyStore;
	}

	public static KeyStore modifyPassword(String keyStorePath, String keyStoreType, String oldPassword,
			String newPassword) throws Exception {
		KeyStore keyStore = getKeyStore(keyStorePath, keyStoreType, oldPassword);
		FileOutputStream output = new FileOutputStream(keyStorePath);
		keyStore.store(output, newPassword.toCharArray());
		output.close();
		return keyStore;
	}

	public static KeyStore modifyPrivatePassword(String keyStorePath, String keyStoreType, String password,
			String alias, String oldPassword, String newPassword) throws Exception {
		KeyStore keyStore = getKeyStore(keyStorePath, keyStoreType, password);
		Certificate[] chain = keyStore.getCertificateChain(alias);
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, oldPassword.toCharArray());
		keyStore.setKeyEntry(alias, privateKey, newPassword.toCharArray(), chain);
		FileOutputStream output = new FileOutputStream(keyStorePath);
		keyStore.store(output, password.toCharArray());
		output.close();
		return keyStore;
	}

	public static boolean containsAlias(String keyStorePath, String keyStoreType, String alias, String password)
			throws Exception {
		KeyStore keyStore = getKeyStore(keyStorePath, keyStoreType, password);
		return keyStore.containsAlias(alias);
	}

	public static KeyStore deleteEntry(String keyStorePath, String keyStoreType, String alias, String password)
			throws Exception {
		KeyStore keyStore = getKeyStore(keyStorePath, keyStoreType, password);
		keyStore.deleteEntry(alias);
		FileOutputStream output = new FileOutputStream(keyStorePath);
		keyStore.store(output, password.toCharArray());
		output.close();
		return keyStore;
	}

}
