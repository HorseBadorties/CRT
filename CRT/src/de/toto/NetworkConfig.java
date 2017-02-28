package de.toto;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class NetworkConfig {
	
	private static boolean configurationDone = false;
	
	public static void doConfig() {
		if (configurationDone) return;		
		
		configurationDone = true;
		
		//check if we are behind "the" firewall...		
		if (!atWork()) return;
		
		System.setProperty("http.proxyHost", "149.213.3.254");
		System.setProperty("http.proxyPort", "3128");
		System.setProperty("https.proxyHost", "149.213.3.254");
		System.setProperty("https.proxyPort", "3128");
		
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
	        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
	        SSLContext.setDefault(ctx);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static boolean atWork() {
		return "080064".equals(System.getProperty("user.name"));
	}

	private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
