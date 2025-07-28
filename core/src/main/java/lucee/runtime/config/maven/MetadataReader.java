package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.library.function.FunctionLibEntityResolver;
import lucee.transformer.library.function.FunctionLibException;

public final class MetadataReader extends DefaultHandler {

	private XMLReader xmlReader;
	private Stack<String> tree = new Stack<>();
	private StringBuilder content = new StringBuilder();
	private boolean insideVersion;

	private Repository repository;
	private String group;
	private String artifact;
	private List<Version> versions;

	MetadataReader(Repository repository, String group, String artifact) {
		this.repository = repository;
		this.group = group;
		this.artifact = artifact;
	}

	public List<Version> read() throws IOException, GeneralSecurityException, SAXException {
		// cache read
		List<Version> versionsFromCache = readFromCache();
		if (versionsFromCache != null) {
			return versionsFromCache;
		}

		this.versions = new ArrayList<>();

		// Updated URL with correct parameter names and no classifier filter
		URL url = new URL(repository.url + group.replace('.', '/') + '/' + artifact + "/maven-metadata.xml");
		HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
		if (rsp != null) {
			int sc = rsp.getStatusCode();
			if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + repository.url + "], status code [" + sc + "]");
		}
		else {
			throw new IOException("unable to invoke [" + repository.url + "], no response.");
		}

		Reader r = null;
		try {
			init(new InputSource(r = IOUtil.getReader(rsp.getContentAsStream(), (Charset) null)));
		}
		finally {
			IOUtil.close(r);
		}
		storeToCache();
		return versions;

	}

	private void storeToCache() {
		try {
			Resource resLastmod = repository.cacheDirectory.getRealResource("lastmod");
			Resource resVersions = repository.cacheDirectory.getRealResource("versions");
			StringBuilder sb = new StringBuilder();
			for (Version v: versions) {
				sb.append(v.toString()).append(',');
			}

			IOUtil.write(resVersions, sb.toString().substring(0, sb.length() - 1), CharsetUtil.UTF8, false);
			IOUtil.write(resLastmod, Caster.toString(System.currentTimeMillis()), CharsetUtil.UTF8, false);
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
		}
	}

	private List<Version> readFromCache() {
		try {
			Resource resLastmod = repository.cacheDirectory.getRealResource("lastmod");
			if (resLastmod.isFile()) {
				long lastmod = repository.timeout == Repository.TIMEOUT_NEVER ? Repository.TIMEOUT_NEVER : Caster.toLongValue(IOUtil.toString(resLastmod, CharsetUtil.UTF8), 0L);
				if (repository.timeout == Repository.TIMEOUT_NEVER || lastmod + repository.timeout > System.currentTimeMillis()) {
					Resource resVersions = repository.cacheDirectory.getRealResource("versions");
					String content = IOUtil.toString(resVersions, CharsetUtil.UTF8);
					List<String> list = ListUtil.listToList(content, ',', true);
					List<Version> versions = new ArrayList<>();
					for (String v: list) {
						versions.add(OSGiUtil.toVersion(v.trim()));
					}
					return versions;
				}
			}
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
		}
		return null;
	}

	/**
	 * Generelle Initialisierungsmetode der Konstruktoren.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param is InputStream auf die TLD.
	 * @throws SAXException
	 * @throws IOException
	 * @throws FunctionLibException
	 */
	private void init(InputSource is) throws SAXException, IOException {
		xmlReader = XMLUtil.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);
		xmlReader.setEntityResolver(new FunctionLibEntityResolver());
		xmlReader.parse(is);

	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		if (tree.size() == 3 && "versions".equals(tree.peek()) && "version".equals(name)) {
			insideVersion = true;
		}
		tree.add(qName);
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		if (insideVersion) {
			insideVersion = false;
			try {
				versions.add(OSGiUtil.toVersion(content.toString().trim()));
			}
			catch (BundleException e) {
				LogUtil.log("MavenReader", e);
			}
		}
		tree.pop();
		content = new StringBuilder();
	}

	@Override
	public void characters(char ch[], int start, int length) {
		content.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}
}