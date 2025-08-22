package lucee.runtime.config.maven;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Version;

import lucee.print;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

public class ExtenionProvider {

	private Repository[] repoSnapshots;
	private Repository[] repoReleases;
	private String group;
	private List<Repository> repos;

	public ExtenionProvider(Repository[] repoSnapshots, Repository[] repoReleases, String group) {
		this.repoSnapshots = repoSnapshots;
		this.repoReleases = repoReleases;
		this.repos = MavenUpdateProvider.merge(repoSnapshots, repoReleases);
		this.group = group;
	}

	public ExtenionProvider(String group) {
		this.repoSnapshots = MavenUpdateProvider.getDefaultRepositorySnapshots();
		this.repoReleases = MavenUpdateProvider.getDefaultRepositoryReleases();
		this.repos = MavenUpdateProvider.merge(repoSnapshots, repoReleases);
		this.group = group;
	}

	public ExtenionProvider() {
		this.repoSnapshots = MavenUpdateProvider.getDefaultRepositorySnapshots();
		this.repoReleases = MavenUpdateProvider.getDefaultRepositoryReleases();
		this.repos = MavenUpdateProvider.merge(repoSnapshots, repoReleases);
		this.group = MavenUpdateProvider.DEFAULT_GROUP;
	}

	private Set<String> listPotencial() throws IOException, InterruptedException {
		HtmlDirectoryScraper scraper = new HtmlDirectoryScraper();
		String strURL;
		Set<String> subfolders = new HashSet<>(), tmp;
		for (Repository r: repos) {
			strURL = (r.url.endsWith("/") ? r.url : (r.url + "/")) + group.replace('.', '/') + "/";
			print.e(strURL);
			tmp = readFromCache(r);
			if (tmp == null) {
				print.e("------- new --------");
				tmp = new HashSet<>();
				scraper.getSubfolderLinks(strURL, tmp);
			}
			copy(tmp, subfolders);
			storeToCache(r, tmp);
		}
		return subfolders;
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
					print.e("------- from cache --------");
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

	public void list() throws Exception {
		MavenUpdateProvider mup;
		Set<String> potencial = listPotencial();
		List<Version> versions;
		for (String p: potencial) {
			mup = new MavenUpdateProvider(this.repoSnapshots, this.repoReleases, MavenUpdateProvider.DEFAULT_GROUP, p);

			versions = mup.list("lex");
			if (!ArrayUtil.isEmpty(versions)) {
				print.e("------- " + p + " --------");
				print.e(versions);
			}

		}

		print.e(potencial);
	}

	public static void main(String[] args) throws Exception {

		ExtenionProvider ep = new ExtenionProvider();
		// ep.listPotencial();
		ep.list();
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
