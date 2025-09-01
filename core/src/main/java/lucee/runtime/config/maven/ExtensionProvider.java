package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Version;
import org.xml.sax.SAXException;

import lucee.print;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.net.HTTPUtil;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.tag.Http;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

public class ExtensionProvider {

	private static final String EXTENSION_EXTENSION = "lex";
	private Repository[] repoSnapshots;
	private Repository[] repoReleases;
	private Repository[] repoMixed;
	private String group;
	private List<Repository> repos;

	public ExtensionProvider(Repository[] repoSnapshots, Repository[] repoReleases, Repository[] repoMixed, String group) {
		this.repoSnapshots = repoSnapshots;
		this.repoReleases = repoReleases;
		this.repoMixed = repoMixed;
		this.repos = MavenUpdateProvider.merge(repoSnapshots, repoReleases, repoMixed);
		this.group = group;
	}

	public ExtensionProvider(String group) {
		this.repoSnapshots = MavenUpdateProvider.getDefaultRepositorySnapshots();
		this.repoReleases = MavenUpdateProvider.getDefaultRepositoryReleases();
		this.repoMixed = MavenUpdateProvider.getDefaultRepositoryMixed();
		this.repos = MavenUpdateProvider.merge(repoSnapshots, repoReleases, repoMixed);
		this.group = group;
	}

	public ExtensionProvider() {
		this.repoSnapshots = MavenUpdateProvider.getDefaultRepositorySnapshots();
		this.repoReleases = MavenUpdateProvider.getDefaultRepositoryReleases();
		this.repoMixed = MavenUpdateProvider.getDefaultRepositoryMixed();
		this.repos = MavenUpdateProvider.merge(repoSnapshots, repoReleases, repoMixed);
		this.group = MavenUpdateProvider.DEFAULT_GROUP;
	}

	private Set<String> listAllProjects() throws IOException, InterruptedException {
		HtmlDirectoryScraper scraper = new HtmlDirectoryScraper();
		String strURL;
		Set<String> subfolders = new HashSet<>(), tmp;

		for (Repository r: repos) {
			strURL = (r.url.endsWith("/") ? r.url : (r.url + "/")) + group.replace('.', '/') + "/";
			tmp = readFromCache(r);
			if (tmp == null) {
				tmp = new HashSet<>();
				scraper.getSubfolderLinks(strURL, tmp);
			}
			copy(tmp, subfolders);
			storeToCache(r, tmp);
		}
		return subfolders;
	}

	public List<String> list() throws IOException, InterruptedException {
		List<String> artifacts = new ArrayList<>();
		for (String artifact: listAllProjects()) {
			if (artifact.endsWith("-extension")) artifacts.add(artifact);
		}
		Collections.sort(artifacts);
		return artifacts;
	}

	private void storeToCache(Repository repository, Set<String> subfolders) {
		try {
			Resource resLastmod = repository.cacheDirectory.getRealResource(HashUtil.create64BitHashAsString(group + "_lastmod", Character.MAX_RADIX));
			Resource resVersions = repository.cacheDirectory.getRealResource(HashUtil.create64BitHashAsString(group + "_versions", Character.MAX_RADIX));
			StringBuilder sb = new StringBuilder();
			for (String subfolder: subfolders) {
				sb.append(subfolder).append(',');
			}

			IOUtil.write(resVersions, sb.length() == 0 ? "" : sb.toString().substring(0, sb.length() - 1), CharsetUtil.UTF8, false);
			IOUtil.write(resLastmod, Caster.toString(System.currentTimeMillis()), CharsetUtil.UTF8, false);
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
		}
	}

	private Set<String> readFromCache(Repository repository) {
		try {
			Resource resLastmod = repository.cacheDirectory.getRealResource(HashUtil.create64BitHashAsString(group + "_lastmod", Character.MAX_RADIX));
			if (resLastmod.isFile()) {
				long lastmod = repository.timeout == Repository.TIMEOUT_NEVER ? Repository.TIMEOUT_NEVER : Caster.toLongValue(IOUtil.toString(resLastmod, CharsetUtil.UTF8), 0L);
				if (repository.timeout == Repository.TIMEOUT_NEVER || lastmod + repository.timeout > System.currentTimeMillis()) {
					Resource resVersions = repository.cacheDirectory.getRealResource(HashUtil.create64BitHashAsString(group + "_versions", Character.MAX_RADIX));
					String content = IOUtil.toString(resVersions, CharsetUtil.UTF8);
					Set<String> subfolders = new HashSet<>();
					if (content.length() > 0) {
						List<String> list = ListUtil.listToList(content, ',', true);
						for (String v: list) {
							subfolders.add(v.trim());
						}
					}
					return subfolders;
				}
			}
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
		}
		return null;
	}

	private static void copy(Set<String> from, Set<String> to) {
		for (String s: from) {
			to.add(s);
		}
	}

	public List<Version> list(String artifact) throws IOException, GeneralSecurityException, SAXException {
		MavenUpdateProvider mup = new MavenUpdateProvider(this.repoSnapshots, this.repoReleases, this.repoMixed, this.group, artifact);
		return mup.list();
	}

	public Map<String, Object> detail(String artifact, Version version) throws PageException, IOException, GeneralSecurityException, SAXException {
		MavenUpdateProvider mup;
		mup = new MavenUpdateProvider(this.repoSnapshots, this.repoReleases, this.repoMixed, this.group, artifact);
		Map<String, Object> detail = mup.detail(version, EXTENSION_EXTENSION, false);

		if (detail != null) return detail;
		throw new ApplicationException("there is no endpoint for [" + this.group + ":" + artifact + ":" + version + "]");
	}

	public Map<String, Object> detail(String artifact, Version version, Map<String, Object> defaultValue) {
		MavenUpdateProvider mup;
		Map<String, Object> detail = null;
		mup = new MavenUpdateProvider(this.repoSnapshots, this.repoReleases, this.repoMixed, this.group, artifact);
		try {
			detail = mup.detail(version, EXTENSION_EXTENSION, false);
			if (detail != null) return detail;
		}
		catch (Exception e) {
		}

		return defaultValue;
	}

	public InputStream get(String artifact, Version version) throws PageException, IOException, GeneralSecurityException, SAXException {
		Map<String, Object> detail = detail(artifact, version);
		if (detail != null) {
			URL url = HTTPUtil.toURL(Caster.toString(detail.get(EXTENSION_EXTENSION), null), Http.ENCODED_NO, null);
			if (url != null) {
				URLConnection connection = url.openConnection();

				// Set reasonable timeouts
				connection.setConnectTimeout(5000); // 5 seconds
				connection.setReadTimeout(60000); // 60 seconds

				// Set a user agent to avoid blocks
				connection.setRequestProperty("User-Agent", "Lucee Extension Provider 1.0");

				return connection.getInputStream();
			}
		}
		throw new ApplicationException("there is no [" + EXTENSION_EXTENSION + "] artifact for [" + this.group + ":" + artifact + ":" + version + "]");
	}

	private void listOld(List<String> list, Map<String, Map<Version, Map<String, Object>>> map)
			throws IOException, InterruptedException, GeneralSecurityException, SAXException, PageException {
		MavenUpdateProvider mup;
		Set<String> potencial = listAllProjects();
		List<Version> versions;
		Map<Version, Map<String, Object>> artifacts;
		outer: for (String artifact: potencial) {
			// if (artifact.indexOf("extension") == -1) continue;
			mup = new MavenUpdateProvider(this.repoSnapshots, this.repoReleases, this.repoMixed, this.group, artifact);
			versions = mup.list();
			if (!ArrayUtil.isEmpty(versions)) {
				artifacts = null;

				for (Version v: versions) {
					// if (v.toString().indexOf("-SNAPSHOT") == 0) continue;
					Map<String, Object> data = mup.detail(v, EXTENSION_EXTENSION, false);

					if (data != null) {
						if (list != null) {
							list.add(artifact);
							continue outer;
						}

						if (artifacts == null) artifacts = new HashMap<>();
						artifacts.put(v, data);
					}
					else {
						break;
					}
				}
				if (artifacts != null) {
					map.put(artifact, artifacts);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {

		ExtensionProvider ep = new ExtensionProvider(new Repository[] {}, new Repository[] {},
				new Repository[] { new Repository("Maven Release Repository", "https://cdn.lucee.org/", Repository.TIMEOUT_5SECONDS) }, "org.lucee");
		ep = new ExtensionProvider();
		long start = System.currentTimeMillis();
		List<String> list = ep.list();

		print.e("list-artifacts:" + (System.currentTimeMillis() - start));
		print.e(list);

		start = System.currentTimeMillis();
		List<Version> versions = ep.list("mysql-jdbc-extension");
		print.e("list-versions:" + (System.currentTimeMillis() - start));
		print.e(versions);

		start = System.currentTimeMillis();
		Map<String, Object> detail = ep.detail("mssql-jdbc-extension", OSGiUtil.toVersion("6.5.4"));
		print.e("detail:" + (System.currentTimeMillis() - start));
		print.e(detail);

		start = System.currentTimeMillis();
		InputStream is = ep.get("mysql-jdbc-extension", versions.get(versions.size() - 1));
		byte[] bytes = IOUtil.toBytes(is);
		print.e("get:" + (System.currentTimeMillis() - start));
		print.e(bytes.length);

		// read all projects

		try {
			// List<String> subfolders = scraper.getSubfolderLinks(url);

			// System.out.println("Found " + subfolders.size() + " subfolders:");
			// for (String folder: subfolders) {
			// System.out.println(" " + folder);
			// }

		}
		catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		// MavenUpdateProvider mup = new MavenUpdateProvider(MavenUpdateProvider.DEFAULT_GROUP, "axis");

		// print.e(mup.list());
	}
}
