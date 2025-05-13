package lucee.runtime.mvn;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.mvn.POMReader.Dependency;
import lucee.runtime.op.Caster;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class MavenUtil {
	private static Map<String, String> sysprops;
	private static Object token = new SerializableObject();

	private static final RequestConfig DEFAULT_REQUEST_CONFIG;
	public static final int CONNECTION_TIMEOUT = 5000;
	public static final int READ_TIMEOUT_HEAD = 5000;
	public static final int READ_TIMEOUT_GET = 5000;
	public static final long ARTIFACT_UNAVAILABLE_CACHE_DURATION = Caster.toLongValue(SystemUtil.getSystemPropOrEnvVar("lucee.maven.negative.cache.duration", null), 60000L * 15L);

	private static final DateTimeFormatter MAVEN_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

	// Static initialization block
	static {
		DEFAULT_REQUEST_CONFIG = RequestConfig.custom()

				.setRedirectsEnabled(true) // allow redirect
				.setMaxRedirects(5) // max 5 redirects
				.setSocketTimeout(10000) // The maximum period of inactivity between data packets once the connection is established.
				.setConnectTimeout(5000) // The time it takes to establish a TCP connection to the server (the initial handshake).
				.setConnectionRequestTimeout(5000) // The time to wait for a connection from the connection manager/pool.
				.build();
	}
	private static Map<Repository, AtomicInteger> ranking = new ConcurrentHashMap<>();
	private static boolean initLocalRepository;
	private static Resource localRepository;

	public static Map<String, String> getProperties(Map<String, String> rawProperties, POM parent) throws IOException {
		Map<String, String> properties = parent != null ? parent.getProperties() : new LinkedHashMap<>();

		int size = properties == null ? 0 : properties.size();
		if (rawProperties != null) size += rawProperties.size();

		Map<String, String> newProperties = new HashMap<>(size);

		// copy data from parent
		if (properties != null) {
			for (Entry<String, String> e: properties.entrySet()) {
				newProperties.put(e.getKey(), e.getValue());
			}
		}

		// add new data
		if (rawProperties != null) {
			for (Entry<String, String> e: rawProperties.entrySet()) {
				newProperties.put(e.getKey(), e.getValue());
			}
		}
		return newProperties;
	}

	public static Map<String, String> getSystemProperties() {
		if (sysprops == null) {
			synchronized (token) {
				if (sysprops == null) {
					Properties props = System.getProperties();
					sysprops = new HashMap<>(props.size());
					for (String name: props.stringPropertyNames()) {
						sysprops.put(name, props.getProperty(name));
					}
				}
			}
		}
		return sysprops;
	}

	public static Collection<Repository> getRepositories(List<POMReader.Repository> rawRepositories, POM current, POM parent, Map<String, String> properties,
			Collection<Repository> initRepositories) throws IOException {

		Map<String, Repository> repositories = new LinkedHashMap<>();// linked so we keep the order

		// repos defined in POM
		if (rawRepositories != null) {
			for (POMReader.Repository rep: rawRepositories) {
				Repository r = new Repository(

						resolvePlaceholders(current, rep.id, properties),

						resolvePlaceholders(current, rep.name, properties),

						resolvePlaceholders(current, rep.url, properties)

				);
				repositories.put(r.getUrl(), r);
			}
		}

		// get parent repos
		if (parent != null) {
			Collection<Repository> reps = parent.getRepositories();
			if (reps != null) {
				for (Repository r: reps) {
					repositories.put(r.getUrl(), r); // TODO clone?
				}
			}
		}
		// add default repos
		else if (initRepositories != null) {
			for (Repository r: initRepositories) {
				repositories.put(r.getUrl(), r);
			}
		}
		return repositories.values();

	}

	public static List<POM> getDependencies(List<POMReader.Dependency> rawDependencies, POM current, POM parent, Map<String, String> properties, Resource localDirectory,
			boolean management, Log log) throws IOException {
		List<POM> dependencies = new ArrayList<>();
		List<POM> parentDendencyManagement = null;

		ExecutorService executor = ThreadUtil.createExecutorService(Runtime.getRuntime().availableProcessors());

		if (parent != null) {
			parentDendencyManagement = current.getDependencyManagement();
			List<POM> tmp = parent.getDependencies();
			if (tmp != null) {
				for (POM pom: tmp) {
					dependencies.add(pom); // TODO clone?
				}
			}
		}
		if (rawDependencies != null) {
			List<Future<POM>> futures = new ArrayList<>();
			for (POMReader.Dependency rd: rawDependencies) {
				GAVSO gavso = getDependency(rd, parent, current, properties, parentDendencyManagement, management);
				if (gavso == null) continue;

				Future<POM> future = executor.submit(() -> {
					POM p = POM.getInstance(localDirectory, current.getRepositories(), gavso.g, gavso.a, gavso.v, gavso.s, gavso.o, gavso.c, current.getDependencyScope(),
							current.getDependencyScopeManagement(), log);
					p.initXML();
					return p;
				});
				futures.add(future);
			}
			try {
				for (Future<POM> future: futures) {
					dependencies.add(future.get()); // Wait for init to complete
				}
			}
			catch (Exception e) {
				throw ExceptionUtil.toIOException(e);
			}
		}
		executor.shutdown();
		return dependencies;
	}

	public static GAVSO getDependency(POMReader.Dependency rd, POM parent, POM current, Map<String, String> properties, List<POM> parentDendencyManagement, boolean management)
			throws IOException {
		POM pdm = null;// TODO move out of here so multiple loop elements can profit
		boolean loadedPDM = false;
		String g = resolvePlaceholders(current, rd.groupId, properties);
		String a = resolvePlaceholders(current, rd.artifactId, properties);
		// scope
		String s = rd.scope;
		if (s == null && parentDendencyManagement != null) {
			pdm = getDependency(parentDendencyManagement, g, a);
			loadedPDM = true;
			if (pdm != null) {
				s = pdm.getScopeAsString();
			}
		}
		if (s != null) s = resolvePlaceholders(current, s, properties);

		// scope allowed?
		if (!allowed(management ? current.getDependencyScopeManagement() : current.getDependencyScope(), toScope(s, POM.SCOPE_COMPILE))) {
			return null;
		}

		// version
		String v = rd.version;
		if (v != null) {
			v = resolvePlaceholders(current, v, properties);
		}
		else {
			if (!loadedPDM) {
				pdm = getDependency(parentDendencyManagement, g, a);
				loadedPDM = true;
			}
			if (pdm != null) {
				v = pdm.getVersion();
				v = resolvePlaceholders(pdm, v, pdm.getProperties());
			}

			if (v == null) {
				throw new IOException("could not find version for dependency [" + g + ":" + a + "] in [" + current + "]");
			}
		}

		// PATCH TODO better solution for this
		if (v != null && v.startsWith("[")) {
			v = v.substring(1, v.indexOf(','));
		}

		// optional
		String o = rd.optional;
		if (o == null && parentDendencyManagement != null) {
			if (!loadedPDM) {
				pdm = getDependency(parentDendencyManagement, g, a);
				loadedPDM = true;
			}
			if (pdm != null) {
				o = pdm.getOptionalAsString();
			}
		}
		if (o != null) s = resolvePlaceholders(current, o, properties);
		return new GAVSO(g, a, v, s, o, null);
		// p = POM.getInstance(localDirectory, g, a, v, s, o, current.getDependencyScope(),
		// current.getDependencyScopeManagement());

		// dependencies.add(p);
	}

	public static class GAVSO implements Serializable {
		public final String g;// groupId
		public final String a;// artifactId
		public final String v;// version
		public final String s;// scope
		public final String o;// optional
		public final String c;// checksum

		public GAVSO(String g, String a, String v) {
			this.g = g;
			this.a = a;
			this.v = v;
			this.s = null;
			this.o = null;
			this.c = null;
		}

		public GAVSO(String g, String a, String v, String s, String o, String c) {
			this.g = g;
			this.a = a;
			this.v = v;
			this.s = s;
			this.o = o;
			this.c = c;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			if (!StringUtil.isEmpty(g, true)) sb.append(g);
			if (!StringUtil.isEmpty(a, true)) {
				if (sb.length() > 0) sb.append(':');
				sb.append(a);
			}
			if (!StringUtil.isEmpty(v, true)) {
				if (sb.length() > 0) sb.append(':');
				sb.append(v);
			}
			if (!StringUtil.isEmpty(s, true)) {
				if (sb.length() > 0) sb.append(':');
				sb.append(s);
			}
			if (!StringUtil.isEmpty(o, true)) {
				if (sb.length() > 0) sb.append(':');
				sb.append(o);
			}
			return sb.toString();
		}

		private String toGAV() {
			StringBuilder sb = new StringBuilder();

			if (!StringUtil.isEmpty(g, true)) sb.append(g);
			if (!StringUtil.isEmpty(a, true)) {
				if (sb.length() > 0) sb.append(':');
				sb.append(a);
			}
			if (!StringUtil.isEmpty(v, true)) {
				if (sb.length() > 0) sb.append(':');
				sb.append(v);
			}
			return sb.toString();
		}

		private String toGA() {
			StringBuilder sb = new StringBuilder();

			if (!StringUtil.isEmpty(g, true)) sb.append(g);
			if (!StringUtil.isEmpty(a, true)) {
				if (sb.length() > 0) sb.append(':');
				sb.append(a);
			}
			return sb.toString();
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof GAVSO)) return false;
			return toGAV().equals(((GAVSO) other).toGAV());
		}

		/**
		 * same endpoint with different versions
		 * 
		 * @param other
		 * @return
		 */
		public boolean same(GAVSO other) {
			return toGA().equals(other.toGA());
		}

		public Struct populate(Struct sct) {
			if (!StringUtil.isEmpty(g, true)) sct.setEL(KeyConstants._groupId, g);
			if (!StringUtil.isEmpty(a, true)) sct.setEL(KeyConstants._artifactId, a);
			if (!StringUtil.isEmpty(v, true)) sct.setEL(KeyConstants._version, v);
			if (!StringUtil.isEmpty(s, true)) sct.setEL(KeyConstants._scope, s);
			if (!StringUtil.isEmpty(o, true)) sct.setEL(KeyConstants._optional, o);
			return sct;
		}
	}

	public static boolean allowed(int allowedScopes, int scope) {
		return (allowedScopes & scope) != 0;
	}

	private static POM getDependency(List<POM> dependencies, String groupId, String artifactId) {
		if (dependencies != null) {

			for (POM pom: dependencies) {
				if (pom.getGroupId().equals(groupId) && pom.getArtifactId().equals(artifactId)) {
					return pom;
				}
			}
			for (POM pom: dependencies) {
				try {
					List<POM> dependencyManagement = pom.getDependencyManagement();
					if (dependencyManagement != null) {
						for (POM p: dependencyManagement) {
							if (p.getGroupId().equals(groupId) && p.getArtifactId().equals(artifactId)) {
								return p;
							}
						}
					}
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static List<POM> getDependencyManagement(List<POMReader.Dependency> rawDependencies, POM current, POM parent, Map<String, String> properties, Resource localDirectory,
			Log log) throws IOException {

		List<POM> dependencies = new ArrayList<>();

		if (parent != null) {
			List<POM> deps = parent.getDependencyManagement();
			if (deps != null) {
				for (POM pom: deps) {
					dependencies.add(pom); // TODO clone?
				}
			}
		}

		if (rawDependencies != null) {
			for (Dependency rd: rawDependencies) {
				GAVSO gavso = null;
				try {
					gavso = getDependency(rd, parent, current, properties, null, true);
				}
				catch (IOException ioe) {
					LogUtil.log(null, "mvn", ioe, Log.LEVEL_WARN, "application");
				}
				if (gavso == null) continue;
				POM p = POM.getInstance(localDirectory, current.getRepositories(), gavso.g, gavso.a, gavso.v, gavso.s, gavso.o, gavso.c, current.getDependencyScope(),
						current.getDependencyScopeManagement(), false, log);
				dependencies.add(p);
			}
		}
		return dependencies;
	}

	public static String resolvePlaceholders(POM pom, String value, Map<String, String> properties) throws IOException {
		boolean modifed;
		while (value != null && value.contains("${")) {
			modifed = false;
			if (pom != null && value != null && value.contains("${project.")) {
				String placeholder = value.substring(value.indexOf("${project.") + 10, value.indexOf("}"));

				if ("groupId".equals(placeholder)) {
					value = pom.getGroupId();
					modifed = true;
				}
				else if ("artifactId".equals(placeholder)) {
					value = pom.getArtifactId();
					modifed = true;
				}
				else if ("version".equals(placeholder)) {
					value = pom.getVersion();
					modifed = true;
				}
				else if ("scope".equals(placeholder) && pom.getScopeUnresolved() != null) {
					value = pom.getScopeUnresolved();
					modifed = true;
				}
				else if ("optional".equals(placeholder)) {
					value = pom.getOptionaUnresolved();
					modifed = true;
				}
				// TODO is there more?
			}

			// Resolve placeholders using properties
			if (value != null && value.contains("${")) {
				String placeholder = value.substring(value.indexOf("${") + 2, value.indexOf("}"));
				String val = properties.get(placeholder);
				if (val != null && !val.equals(value)) {
					modifed = true;
					value = val;
				}
			}

			if (value != null && value.contains("${")) {
				String placeholder = value.substring(value.indexOf("${") + 2, value.indexOf("}"));
				String resolvedValue = MavenUtil.getSystemProperties().get(placeholder);
				if (resolvedValue != null && !resolvedValue.equals(value)) {
					modifed = true;
					value = resolvedValue;
				}
			}
			if (!modifed) break;
		}
		if (value != null && value.indexOf("${") != -1) {
			throw new IOException("Cannot resolve [" + value + "] for [" + pom + "], available properties are [" + ListUtil.toList(properties.keySet(), ", ") + "]");
		}
		return value;
	}

	public static boolean hasPlaceholders(String str) {
		return str != null && str.indexOf("${") != -1;
	}

	public static void downloadAsync(POM pom, Collection<Repository> repositories, String type, Log log) {
		ThreadUtil.getThread(() -> {
			try {
				download(pom, repositories, type, log);
			}
			catch (IOException e) {
			}
		}, true).start();
	}

	public static void download(POM pom, Collection<Repository> repositories, String type, Log log) throws IOException {
		Resource res = pom.getArtifact(type);

		// file is empty or does not exist
		if (!res.isFile()) {
			synchronized (SystemUtil.createToken("mvn", res.getAbsolutePath())) {
				// file is empty or does not exist
				if (!res.isFile()) {

					Resource localRepo = getLocalRepository();
					if (localRepo != null) {
						Resource tmp = pom.getArtifact(localRepo, type);
						// found one in local maven
						if (tmp.isFile()) {
							tmp.copyTo(res, false);
							return;
						}
					}

					// it did already check for file, but it was not found
					Resource lastUpdated = res.getParentResource().getRealResource(res.getName() + ".lastUpdated");
					if (lastUpdated.isFile()) {

						// try again? TODO read lastModified from file, there is an entry for every single endpoint
						if ((lastUpdated.lastModified() + ARTIFACT_UNAVAILABLE_CACHE_DURATION) < System.currentTimeMillis()) {
							lastUpdated.remove(true);
							download(pom, repositories, type, log);
							return;
						}
						throw new IOException("Failed to download [" + pom + "] ");
					}

					String scriptName = pom.getGroupId().replace('.', '/') + "/" + pom.getArtifactId() + "/" + pom.getVersion() + "/" + pom.getArtifactId() + "-" + pom.getVersion()
							+ "." + type;
					StringBuilder info = null;
					try {
						if (repositories == null || repositories.isEmpty()) repositories = pom.getRepositories();

						if (repositories == null || repositories.size() == 0) {
							IOException ioe = new IOException("Failed to download java artifact [" + pom.toString() + "] for type [" + type + "]");
							// "Failed to download java artifact [" + pom.toString() + "] for type [" + type + "], attempted
							// endpoint(s): [" + sb + "]");
							// if (cause != null) ExceptionUtil.initCauseEL(ioe, cause);
							throw ioe;
						}
						// url = pom.getArtifact(type, repositories);
						//////// if (log != null) log.info("maven", "download [" + url + "]");
						URL url;
						CloseableHttpClient httpClient;
						for (Repository r: sort(repositories)) {
							url = null;
							httpClient = null;
							try {
								url = new URL(r.getUrl() + scriptName);
								httpClient = HttpClients.createDefault();

								HttpGet request = new HttpGet(url.toExternalForm());
								request.setConfig(DEFAULT_REQUEST_CONFIG);
								HttpResponse response = httpClient.execute(request);
								HttpEntity entity = response.getEntity();
								int sc = response.getStatusLine().getStatusCode();
								// print.e(type + " >> " + url + " >> " + sc);
								if (sc == 200) {
									if (entity != null) {
										Exception ex = null;
										InputStream is = null;
										Resource tmp = SystemUtil.getTempFile(type, false);
										try {
											is = entity.getContent();
											IOUtil.copy(is, tmp, false);
										}
										catch (IOException e) {
											ex = e;
										}
										finally {
											IOUtil.closeEL(is);
											HTTPUtil.validateDownload(url, response, tmp, pom.getChecksum(), true, ex);
											tmp.moveTo(res);
										}
										deleteLastUpdated(res);
										AtomicInteger rank = ranking.get(r);
										if (rank != null) rank.incrementAndGet();
										else {
											rank = new AtomicInteger(1);
											ranking.put(r, rank);
										}
										return;
									}
								}
								else {
									if (info == null) info = createInfo();
									info.append(r).append(".error=").append('\n');
									info.append(r).append(".lastUpdated=").append(System.currentTimeMillis()).append('\n');
									EntityUtils.consume(entity); // Ensure the response entity is fully consumed
									// throw new IOException("Failed to download: " + url + " for [" + pom + "] - " +
									// response.getStatusLine().getStatusCode());
								}
							}
							catch (Exception e) {
								if (info == null) info = createInfo();
								info.append(r).append(".error=").append('\n');
								info.append(r).append(".lastUpdated=").append(System.currentTimeMillis()).append('\n');
							}
							finally {
								if (httpClient != null) httpClient.close();
							}
						}
					}
					catch (IOException ioe) {
						createLastUpdated(res, info);
						IOException ex = new IOException("Failed to download: " + pom + "");
						ExceptionUtil.initCauseEL(ex, ioe);
						// MUST add again ResourceUtil.deleteEmptyFoldersInside(pom.getLocalDirectory());
						throw ex;
					}

					createLastUpdated(res, info);
					throw new IOException("Failed to download [" + pom + "] ");
				}
			}
		}
	}

	private static StringBuilder createInfo() {
		return new StringBuilder("#NOTE: This is a Maven Resolver internal implementation file (created by Lucee), its format can be changed without prior notice.\n#")
				.append(ZonedDateTime.now().format(MAVEN_DATE_FORMATTER)).append('\n');
	}

	private static void createLastUpdated(Resource res, StringBuilder info) throws IOException {
		Resource lastUpdated = res.getParentResource().getRealResource(res.getName() + ".lastUpdated");
		// print.e(lastUpdated);
		// print.e(info);
		IOUtil.write(lastUpdated, info.toString(), CharsetUtil.UTF8, false);
	}

	private static void deleteLastUpdated(Resource res) {
		Resource lastUpdated = res.getParentResource().getRealResource(res.getName() + ".lastUpdated");
		lastUpdated.delete();
	}

	private static Repository[] sort(Collection<Repository> repositories) {
		// Convert collection to array for sorting
		Repository[] result = repositories.toArray(new Repository[0]);
		AtomicInteger defaultValue = new AtomicInteger(0);
		// Sort the array based on the ranking map
		Arrays.sort(result, (repo1, repo2) -> {
			// Get the count for each repository, defaulting to 0 if not in the map
			int count1 = ranking.getOrDefault(repo1, defaultValue).get();
			int count2 = ranking.getOrDefault(repo2, defaultValue).get();

			// Sort in descending order (highest count first)
			return Integer.compare(count2, count1);
		});

		return result;
	}

	public static POM toPOM(Resource localDirectory, Collection<Repository> repositories, POMReader.Dependency dependency, Map<String, String> properties, int dependencyScope,
			int dependencyScopeManagement, Log log) throws IOException {

		return POM.getInstance(localDirectory, repositories,

				resolvePlaceholders(null, dependency.groupId, properties),

				resolvePlaceholders(null, dependency.artifactId, properties),

				resolvePlaceholders(null, dependency.version, properties),

				null, null, null,

				dependencyScope, dependencyScopeManagement,

				log

		);
	}

	public static int toScopes(String scopes, int defaultValue) {
		if (StringUtil.isEmpty(scopes, true)) return defaultValue;

		int rtn = 0;
		for (String scope: ListUtil.listToStringArray(scopes, ',')) {
			rtn += toScope(scope, 0);
		}
		if (rtn > 0) return rtn;

		return defaultValue;
	}

	public static int toScope(String scope, int defaultValue) {
		if ("compile".equals(scope)) return POM.SCOPE_COMPILE;
		if ("test".equals(scope)) return POM.SCOPE_TEST;
		if ("provided".equals(scope)) return POM.SCOPE_PROVIDED;
		if ("runtime".equals(scope)) return POM.SCOPE_RUNTIME;
		if ("system".equals(scope)) return POM.SCOPE_SYSTEM;
		if ("import".equals(scope)) return POM.SCOPE_IMPORT;
		return defaultValue;
	}

	public static String toScope(int scope, String defaultValue) {
		switch (scope) {
		case POM.SCOPE_COMPILE:
			return "compile";
		case POM.SCOPE_TEST:
			return "test";
		case POM.SCOPE_PROVIDED:
			return "provided";
		case POM.SCOPE_RUNTIME:
			return "runtime";
		case POM.SCOPE_SYSTEM:
			return "system";
		case POM.SCOPE_IMPORT:
			return "import";
		default:
			return defaultValue;
		}
	}

	public static List<GAVSO> toGAVSOs(Object obj, List<GAVSO> defaultValue) {
		// array
		List<GAVSO> list = new ArrayList<>();
		Object[] arr = Caster.toNativeArray(obj, null);
		if (arr != null) {
			GAVSO tmp;
			for (Object o: arr) {
				tmp = toGAVSO(o, null);
				if (tmp != null) list.add(tmp);
			}
			return list;
		}
		// struct
		Struct el = Caster.toStruct(obj, null);
		if (el != null) {
			GAVSO tmp = toGAVSO(el, null);
			if (tmp != null) list.add(tmp);
			return list;
		}

		// gradle style?
		String str = Caster.toString(obj, null);
		if (!StringUtil.isEmpty(str)) {
			GAVSO tmp;
			for (String s: ListUtil.listToStringArray(str, ',')) {
				tmp = toGAVSO(s, null);
				if (tmp != null) list.add(tmp);
			}
			return list;
		}
		return list;
	}

	public static List<GAVSO> toGAVSOs(Object obj) throws ApplicationException {
		// array
		List<GAVSO> list = new ArrayList<>();
		Object[] arr = Caster.toNativeArray(obj, null);
		if (arr != null) {
			for (Object o: arr) {
				list.add(toGAVSO(o));
			}
			return list;
		}
		// struct
		Struct el = Caster.toStruct(obj, null);
		if (el != null) {
			list.add(toGAVSO(el));
			return list;
		}

		// gradle style?
		String str = Caster.toString(obj, null);
		if (!StringUtil.isEmpty(str)) {
			for (String s: ListUtil.listToStringArray(str, ',')) {
				list.add(toGAVSO(s));
			}
			return list;
		}
		return list;
	}

	public static GAVSO toGAVSO(Object obj, GAVSO defaultValue) {
		Struct el = Caster.toStruct(obj, null, false);
		if (el != null) {
			String g = Caster.toString(el.get(KeyConstants._groupId, null), null);
			if (StringUtil.isEmpty(g)) g = Caster.toString(el.get(KeyConstants._g, null), null);
			String a = Caster.toString(el.get(KeyConstants._artifactId, null), null);
			if (StringUtil.isEmpty(a)) a = Caster.toString(el.get(KeyConstants._a, null), null);

			if (!StringUtil.isEmpty(g) && !StringUtil.isEmpty(a)) {
				String v = Caster.toString(el.get(KeyConstants._version, null), null);
				if (StringUtil.isEmpty(v)) v = Caster.toString(el.get(KeyConstants._v, null), null);

				if (!MavenUtil.isValidVersion(v)) return defaultValue;
				return new GAVSO(g, a,

						v,

						Caster.toString(el.get(KeyConstants._scope, null), null),

						Caster.toString(el.get(KeyConstants._optional, null), null),

						Caster.toString(el.get(KeyConstants._checksum, null), null));
			}
			return defaultValue;
		}
		// gradle style?
		String str = Caster.toString(obj, null);
		if (!StringUtil.isEmpty(str)) {
			String[] arr = ListUtil.listToStringArray(str, ':');
			if (arr.length > 1 && arr.length < 7) {
				if (arr.length > 2 && !MavenUtil.isValidVersion(arr[2].trim())) return defaultValue;
				return new GAVSO(

						arr[0].trim(), // group

						arr[1].trim(), // artifact

						arr.length > 2 ? arr[2].trim() : null, // version

						arr.length > 3 ? arr[3].trim() : null, // scope

						arr.length > 4 ? arr[4].trim() : null, // optional

						arr.length > 5 ? arr[5].trim() : null // checksum

				);
			}
		}

		return defaultValue;
	}

	public static GAVSO toGAVSO(Object obj) throws ApplicationException {
		Struct el = Caster.toStruct(obj, null);
		if (el != null) {
			String g = Caster.toString(el.get(KeyConstants._groupId, null), null);
			if (StringUtil.isEmpty(g)) g = Caster.toString(el.get(KeyConstants._g, null), null);
			if (StringUtil.isEmpty(g)) throw new ApplicationException("Missing required field: groupId. Ensure that the 'groupId' key is present and not empty.");

			String a = Caster.toString(el.get(KeyConstants._artifactId, null), null);
			if (StringUtil.isEmpty(a)) a = Caster.toString(el.get(KeyConstants._a, null), null);
			if (StringUtil.isEmpty(a)) throw new ApplicationException("Missing required field: artifactId. Ensure that the 'artifactId' key is present and not empty.");

			String v = Caster.toString(el.get(KeyConstants._version, null), null);
			if (StringUtil.isEmpty(v)) v = Caster.toString(el.get(KeyConstants._v, null), null);
			if (StringUtil.isEmpty(v)) throw new ApplicationException("Missing required field: version. Ensure that the 'version' key is present and not empty.");

			if (!MavenUtil.isValidVersion(v)) throw new ApplicationException("maven version [" + v + "]is invalid");

			return new GAVSO(g, a, v,

					Caster.toString(el.get(KeyConstants._scope, null), null),

					Caster.toString(el.get(KeyConstants._optional, null), null),

					Caster.toString(el.get(KeyConstants._checksum, null), null)

			);

		}
		// gradle style?
		String str = Caster.toString(obj, null);
		if (!StringUtil.isEmpty(str)) {
			String[] arr = ListUtil.listToStringArray(str, ':');
			if (arr.length > 1 && arr.length < 7) {
				if (arr.length > 2 && !MavenUtil.isValidVersion(arr[2].trim())) throw new ApplicationException("maven version [" + arr[2].trim() + "]is invalid");
				return new GAVSO(

						arr[0].trim(), // group

						arr[1].trim(), // artifact

						arr.length > 2 ? arr[2].trim() : null, // version

						arr.length > 3 ? arr[3].trim() : null, // scope

						arr.length > 4 ? arr[4].trim() : null, // optional

						arr.length > 5 ? arr[5].trim() : null // checksum

				);
			}
			throw new ApplicationException("Invalid Maven data in string [" + str + "]. " + "Expected format: '<group>:<artifact>:<version>[:<scope>[:<optional>]]'.");
		}
		throw new ApplicationException("Unable to parse Maven data from the provided input of type [" + Caster.toTypeName(obj) + "]. " + "Supported formats are: "
				+ "1) A Struct with keys 'groupId', 'artifactId', and 'version' (optionally 'scope' and 'optional'), or "
				+ "2) A String in the format 'group:artifact:version[:scope[:optional]]'. " + "Ensure the input conforms to one of these formats.");

	}

	public static String toString(GAVSO[] gavsos) {
		StringBuilder sb = new StringBuilder();
		for (GAVSO g: gavsos) {
			if (sb.length() > 0) sb.append(',');
			sb.append(g.toString());
		}
		return sb.toString();
	}

	public static boolean isValidVersion(String version) {
		if (StringUtil.isEmpty(version)) return false;

		// Basic version pattern
		String versionPattern = "^(\\d+)" + // Major (required)
				"(?:\\.(\\d+))?" + // Minor (optional)
				"(?:\\.(\\d+))?" + // Micro (optional)
				"(?:[-.]?" + // Separator for qualifier (optional)
				"([A-Za-z0-9_-]+(?:[-._][A-Za-z0-9_-]+)*))?" + // Qualifier
				"$";

		return version.matches(versionPattern);
	}

	public static Resource getLocalRepository() {
		if (!initLocalRepository) {
			String userHome = System.getProperty("user.home");

			if (!StringUtil.isEmpty(userHome, true)) {
				ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
				Resource rep = frp.getResource(userHome).getRealResource(".m2/repository");
				if (rep.isDirectory()) localRepository = rep;
			}
			initLocalRepository = true;
		}
		return localRepository;
	}

}
