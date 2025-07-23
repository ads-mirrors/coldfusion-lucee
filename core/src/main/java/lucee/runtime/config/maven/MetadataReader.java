package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

	private String listProvider;
	private String group;
	private String artifact;

	private final Collection<Version> versions;

	MetadataReader(String listProvider, String group, String artifact, Collection<Version> versions) {
		this.listProvider = listProvider;
		this.group = group;
		this.artifact = artifact;
		this.versions = versions == null ? new HashSet<>() : versions;
	}

	public Collection<Version> read() throws IOException, GeneralSecurityException, SAXException {
		listProvider = listProvider.trim();
		if (!listProvider.endsWith("/")) listProvider += "/";

		// Updated URL with correct parameter names and no classifier filter
		URL url = new URL(listProvider + group.replace('.', '/') + '/' + artifact + "/maven-metadata.xml");
		HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
		if (rsp != null) {
			int sc = rsp.getStatusCode();
			if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + listProvider + "], status code [" + sc + "]");
		}
		else {
			throw new IOException("unable to invoke [" + listProvider + "], no response.");
		}

		Reader r = null;
		try {
			init(new InputSource(r = IOUtil.getReader(rsp.getContentAsStream(), (Charset) null)));
		}
		finally {
			IOUtil.close(r);
		}

		// store(cacheFile, versions);
		return versions;

	}

	private static void store(Resource cacheFile, List<Version> versions) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (Version v: versions) {
			sb.append(v.toString()).append(',');
		}
		cacheFile.getParentResource().mkdirs();
		IOUtil.write(cacheFile, sb.toString().substring(0, sb.length() - 1), CharsetUtil.UTF8, false);
	}

	private static List<Version> read(Resource cacheFile) throws IOException, BundleException {
		String content = IOUtil.toString(cacheFile, CharsetUtil.UTF8);
		List<String> list = ListUtil.listToList(content, ',', true);
		List<Version> versions = new ArrayList<>();
		for (String v: list) {
			versions.add(OSGiUtil.toVersion(v.trim()));
		}

		return versions;
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