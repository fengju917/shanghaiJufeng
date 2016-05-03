package com.centerm.lklcpos.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.CoreConnectionPNames;

public class HttpsUtil {
	public static final String SECURITY_MTMS_AGREEMENT = "TLS";
	public static final String SECURITY_MANAGER_ALGORITHM = "X509";
	public static final String SECURITY_KEYSTORE_TYPE = "PKCS12";

	public static DefaultHttpClient getHttpClient(Scheme httpScheme, Scheme httpsScheme, int timeout) {
		// HostnameVerifier hostnameVerifier =
		// org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(httpScheme);
		if (httpsScheme != null)
			registry.register(httpsScheme);
		DefaultHttpClient client = new DefaultHttpClient();
		SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
		DefaultHttpClient httpsclient = new DefaultHttpClient(mgr, client.getParams());
		httpsclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		httpsclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
		// HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		return httpsclient;
	}

	public static Scheme getHttpScheme(int port) {
		return new Scheme("http", PlainSocketFactory.getSocketFactory(), port);
	}

	public static Scheme getHttpsSchemeBoth(String keyStorePath, String storepass, int port) throws Exception {
		Scheme sch = null;
		KeyManagerFactory keyManager = KeyManagerFactory.getInstance(SECURITY_MANAGER_ALGORITHM);
		TrustManagerFactory trustManager = TrustManagerFactory.getInstance(SECURITY_MANAGER_ALGORITHM);
		KeyStore keyStore = ExCertificateUtil.getKeyStore(keyStorePath, SECURITY_KEYSTORE_TYPE, storepass);
		KeyStore trustStore = ExCertificateUtil.getKeyStore(keyStorePath, SECURITY_KEYSTORE_TYPE, storepass);
		keyManager.init(keyStore, storepass.toCharArray());
		trustManager.init(trustStore);
		SSLSocketFactory socketFactory = new SSLSocketFactory(SECURITY_MTMS_AGREEMENT, keyStore, storepass, trustStore,
				null, null);
		socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		sch = new Scheme("https", socketFactory, port);
		return sch;
	}

	public static Scheme getHttpsScheme(String keyStorePath, String storepass, int port, String cerpath,
			String serverCn) throws Exception {
		Scheme sch = null;
		KeyManagerFactory keyManager = KeyManagerFactory.getInstance(SECURITY_MANAGER_ALGORITHM);
		KeyStore keyStore = ExCertificateUtil.getKeyStore(keyStorePath, ExCertificateUtil.KEYSTORE_PKCS12, storepass);
		keyManager.init(keyStore, storepass.toCharArray());
		EasySSLSocketFactory socketFactory = new EasySSLSocketFactory(keyManager, cerpath, serverCn);
		sch = new Scheme("https", socketFactory, port);
		return sch;
	}

	public static HttpURLConnection getHttpURLConnection(URL url) throws IOException {
		HttpURLConnection conn = null;
		String host = android.net.Proxy.getDefaultHost();
		if (null != host) {
			int port = android.net.Proxy.getDefaultPort();
			SocketAddress vAddress = new InetSocketAddress(host, port);
			java.net.Proxy vProxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, vAddress);
			conn = (HttpURLConnection) url.openConnection(vProxy);
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}
		return conn;
	}

	public static HttpsURLConnection getHttpsURLConnection(URL url, SSLContext sslContext) throws IOException {
		HttpsURLConnection conn = null;
		String host = android.net.Proxy.getDefaultHost();
		if (null != host) {
			int port = android.net.Proxy.getDefaultPort();
			SocketAddress vAddress = new InetSocketAddress(host, port);
			java.net.Proxy vProxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, vAddress);
			conn = (HttpsURLConnection) url.openConnection(vProxy);
		} else {
			conn = (HttpsURLConnection) url.openConnection();
		}
		conn.setSSLSocketFactory(sslContext.getSocketFactory());
		conn.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		return conn;
	}

	public static SSLContext getSSLContext(String keyStorePath, String storepass) throws Exception {
		SSLContext sslContext = null;
		sslContext = SSLContext.getInstance(SECURITY_MTMS_AGREEMENT);
		KeyManagerFactory keyManager = KeyManagerFactory.getInstance(SECURITY_MANAGER_ALGORITHM);
		TrustManagerFactory trustManager = TrustManagerFactory.getInstance(SECURITY_MANAGER_ALGORITHM);
		KeyStore keyStore = ExCertificateUtil.getKeyStore(keyStorePath, SECURITY_KEYSTORE_TYPE, storepass);
		KeyStore trustStore = ExCertificateUtil.getKeyStore(keyStorePath, SECURITY_KEYSTORE_TYPE, storepass);
		keyManager.init(keyStore, storepass.toCharArray());
		trustManager.init(trustStore);
		sslContext.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);
		return sslContext;
	}

}
