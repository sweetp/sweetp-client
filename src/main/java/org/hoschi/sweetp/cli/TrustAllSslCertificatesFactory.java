package org.hoschi.sweetp.cli;

import org.apache.http.conn.ssl.SSLSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/*
 * Trust all ssl certificates, including self signed ones.
 * http://stackoverflow.com/questions/2642777/trusting-all-certificates-using-httpclient-over-https
 */
final class TrustAllSslCertificatesFactory extends SSLSocketFactory {
	private SSLContext sslContext = SSLContext.getInstance("TLS");

	static SSLSocketFactory getInstance()
			throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException, KeyManagementException,
			UnrecoverableKeyException {
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);

		SSLSocketFactory sf = new TrustAllSslCertificatesFactory(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		return sf;
	}

	private TrustAllSslCertificatesFactory(KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super(truststore);

		TrustManager tm = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain,
										   String authType) throws
					CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
										   String authType) throws
					CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sslContext.init(null, new TrustManager[]{tm}, null);
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
							   boolean autoClose)
			throws IOException {
		return sslContext.getSocketFactory()
				.createSocket(socket, host, port, autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}

}
