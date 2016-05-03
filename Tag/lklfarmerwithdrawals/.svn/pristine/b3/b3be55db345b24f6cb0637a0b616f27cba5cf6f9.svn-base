package com.centerm.lklcpos.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import android.util.Log;

public class EasySSLSocketFactory implements SocketFactory, LayeredSocketFactory {

	private Logger log = Logger.getLogger(EasySSLSocketFactory.class);

	private SSLContext sslcontext = null;

	private KeyManagerFactory keyManagerFacoty;

	private String certifacetePath;

	private String cn;

	public EasySSLSocketFactory(KeyManagerFactory factory, String certifacetePath, String cn) {
		this.keyManagerFacoty = factory;
		this.certifacetePath = certifacetePath;
		this.cn = cn;
		try {
			sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(keyManagerFacoty.getKeyManagers(),
					new TrustManager[] { new EasyX509TrustManager(null, this.certifacetePath, this.cn) }, null);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}

	}

	private SSLContext getSSLContext() throws IOException {

		return this.sslcontext;
	}

	public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort,
			HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
		int soTimeout = HttpConnectionParams.getSoTimeout(params);

		InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
		SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

		if ((localAddress != null) || (localPort > 0)) {
			// we need to bind explicitly
			if (localPort < 0) {
				localPort = 0; // indicates "any"
			}
			InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);
			sslsock.bind(isa);
		}

		sslsock.connect(remoteAddress, connTimeout);
		sslsock.setSoTimeout(soTimeout);
		return sslsock;

	}

	public Socket createSocket() throws IOException {
		return getSSLContext().getSocketFactory().createSocket();
	}

	public boolean isSecure(Socket socket) throws IllegalArgumentException {
		return true;
	}

	public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
			throws IOException, UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(EasySSLSocketFactory.class));
	}

	public int hashCode() {
		return EasySSLSocketFactory.class.hashCode();
	}
}

class EasyX509TrustManager implements X509TrustManager {

	private X509TrustManager standardTrustManager = null;

	private static final Logger log = Logger.getLogger(EasyX509TrustManager.class);

	private String certifacatePath;

	private String cn;

	public EasyX509TrustManager(KeyStore keystore, String cerPath, String cn)
			throws NoSuchAlgorithmException, KeyStoreException {
		super();

		this.certifacatePath = cerPath;
		this.cn = cn;
		TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		factory.init(keystore);
		TrustManager[] trustmanagers = factory.getTrustManagers();
		if (trustmanagers.length == 0) {
			throw new NoSuchAlgorithmException("no trust manager found");
		}
		this.standardTrustManager = (X509TrustManager) trustmanagers[0];
	}

	/**
	 * 服务端校验客户端证书策略，终端可以忽略
	 */
	public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
		standardTrustManager.checkClientTrusted(certificates, authType);
	}

	/**
	 * 终端校验服务端证书策略 1.校验域名 2.证书验签
	 */
	public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
		if (certificates == null || certificates.length == 0) {
			throw new CertificateException("server certificates empty..");
		}
		X509Certificate certificate = certificates[0];
		FileInputStream cerIns;
		try {
			cerIns = new FileInputStream(new File(certifacatePath));
			certificate.verify(CertificateFactory.getInstance("X.509").generateCertificate(cerIns).getPublicKey());
			String subject = certificate.getSubjectDN().getName();
			String subjectCn = getCnField(subject);
			if (this.cn == null || this.cn.equals("") || subjectCn == null || subjectCn.equals("")) {
				throw new CertificateException("cn field null");
			} else {
				if (!subjectCn.equals(this.cn)) {
					throw new CertificateException("certifacate cn info error");
				}
			}
		} catch (Exception e) {
			log.error("verify server certificate error", e);
			throw new CertificateException("certicicate server fail");
		}
	}

	/**
	 * 获取域名
	 * 
	 * @param subject
	 * @return
	 */
	private String getCnField(String subject) {
		try {
			String[] subjectField = subject.split(",");
			for (int i = 0; i < subjectField.length; i++) {
				if (subjectField[i].split("=")[0].equals("CN")) {
					return subjectField[i].split("=")[1];
				}
			}
		} catch (Exception e) {
			log.error("find cn filed error", e);
			return null;
		}
		return null;

	}

	public X509Certificate[] getAcceptedIssuers() {
		return this.standardTrustManager.getAcceptedIssuers();
	}
}