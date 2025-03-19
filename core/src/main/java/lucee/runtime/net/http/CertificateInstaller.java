/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;

public final class CertificateInstaller {

	private static Map<String, String> installed = new WeakHashMap();

	private String host;
	private int port;
	private char[] passphrase;
	private Resource source;
	private TrustManagerFactory tmf;
	private SavingTrustManager tm;
	private SSLContext context;
	private KeyStore ks;

	public CertificateInstaller(Resource source, String host, int port) throws IOException, KeyStoreException, GeneralSecurityException {
		this(source, host, port, "changeit".toCharArray());
	}

	public CertificateInstaller(Resource source, String host, int port, char[] passphrase) throws IOException, KeyStoreException, GeneralSecurityException {
		this.source = source;
		this.host = host;
		this.port = port;
		this.passphrase = passphrase;

		ks = null;
		InputStream in = source.getInputStream();
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(in, passphrase);
		}
		finally {
			IOUtil.close(in);
		}

		context = SSLContext.getInstance("SSL");
		tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		tm = new SavingTrustManager(defaultTrustManager);
		context.init(null, new TrustManager[] { tm }, null);

		IOException e = checkCertificate(context, host, port);
		if (tm.chain == null) {
			if (e == null) {
				throw new IOException("Could not obtain server certificate chain");
			}
			else {
				throw new IOException("Could not obtain server certificate chain, [ " + e + " ]");
			}
		}
	}

	public void installAll(boolean force) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		String key = host + ":" + port;
		if (force || !installed.containsKey(key)) {
			synchronized (SystemUtil.createToken("CertificateInstaller", key)) {
				if (force || !installed.containsKey(key)) {
					for (int i = 0; i < tm.chain.length; i++) {
						install(i);
					}
					installed.put(key, "");
				}
			}
		}
	}

	private void install(int index) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {

		X509Certificate cert = tm.chain[index];
		String alias = host + "-" + (index + 1);
		ks.setCertificateEntry(alias, cert);

		OutputStream os = source.getOutputStream();
		try {
			ks.store(os, passphrase);
		}
		finally {
			IOUtil.close(os);
		}
	}

	/**
	 * checks if a certificate is installed for given host:port
	 * 
	 * @param context
	 * @param host
	 * @param port
	 * @return
	 */
	public static IOException checkCertificate(SSLContext context, String host, int port) {
		SSLSocketFactory factory = context.getSocketFactory();

		try {
			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
			socket.setSoTimeout(10000);
			socket.startHandshake();
			socket.close();
			return null;
		}
		catch (IOException e) {
			return e;
		}
	}

	public X509Certificate[] getCertificates() {
		return tm.chain;
	}

	public static List<X509Certificate> getAllCertificates(Resource source) throws GeneralSecurityException, IOException {
		KeyStore ks = null;
		InputStream in = source.getInputStream();
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(in, "changeit".toCharArray());
		}
		finally {
			IOUtil.close(in);
		}

		List<X509Certificate> list = new ArrayList<>();
		Enumeration<String> aliases = ks.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			Certificate cert = ks.getCertificate(alias);
			if (cert instanceof X509Certificate) {
				list.add((X509Certificate) cert);
			}
		}
		return list; // Adjust return based on method implementation
	}

	private static class SavingTrustManager implements X509TrustManager {

		private final X509TrustManager tm;
		private X509Certificate[] chain;

		SavingTrustManager(X509TrustManager tm) {
			this.tm = tm;
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			tm.checkServerTrusted(chain, authType);
		}
	}

	/*
	 * public static void main(String[] args) throws Exception { //String host="jira.jboss.org";
	 * 
	 * String host="sso.vogel.de"; int port=443; char[] passphrase="changeit".toCharArray();
	 * 
	 * ResourceProvider frp = ResourcesImpl.getFileResourceProvider(); Resource source =
	 * frp.getResource("/Users/mic/Temp/cacerts");
	 * 
	 * 
	 * CertificateInstaller util = new CertificateInstaller(source,host,port,passphrase);
	 * util.printCertificates(); util.installAll();
	 * 
	 * }
	 */

}