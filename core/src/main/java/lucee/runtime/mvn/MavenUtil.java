package lucee.runtime.mvn;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lucee.commons.digest.Hash;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
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

public final class MavenUtil {
	private static Map<String, String> sysprops;
	private static Object token = new SerializableObject();

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
			Repository defaultRepository) throws IOException {
		Map<String, Repository> repositories = new LinkedHashMap<>();
		repositories.put(defaultRepository.getUrl(), defaultRepository);
		if (parent != null) {
			Collection<Repository> reps = parent.getRepositories();
			if (reps != null) {
				for (Repository r: reps) {
					repositories.put(r.getUrl(), r); // TODO clone?
				}
			}
		}
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

		String g = resolvePlaceholders(current, rd.groupId, properties);
		String a = resolvePlaceholders(current, rd.artifactId, properties);

		// scope
		String s = rd.scope;
		if (s == null && parentDendencyManagement != null) {
			if (pdm == null) pdm = getDendency(parentDendencyManagement, g, a);
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
		if (v == null) {
			pdm = getDendency(parentDendencyManagement, g, a);

			if (pdm != null) {
				v = pdm.getVersion();
			}
			if (v == null) {
				throw new IOException("could not find version for dependency [" + g + ":" + a + "] in [" + current + "]");
			}
		}
		v = resolvePlaceholders(current, v, properties);
		// PATCH TODO better solution for this
		if (v != null && v.startsWith("[")) {
			v = v.substring(1, v.indexOf(','));
		}

		// optional
		String o = rd.optional;
		if (o == null && parentDendencyManagement != null) {
			if (pdm == null) pdm = getDendency(parentDendencyManagement, g, a);
			if (pdm != null) {
				o = pdm.getOptionalAsString();
			}
		}
		if (o != null) o = resolvePlaceholders(current, o, properties);
		return new GAVSO(g, a, v, s, o, null).setDependency(GAVSO.ORIGIN_DEPENDENCY);
		// p = POM.getInstance(localDirectory, g, a, v, s, o, current.getDependencyScope(),
		// current.getDependencyScopeManagement());

		// dependencies.add(p);
	}

	public static class GAVSO implements Serializable {

		public static int ORIGIN_DEPENDENCY = 1;

		public final String g;// groupId
		public final String a;// artifactId
		public final String v;// version
		public final String s;// scope
		public final String o;// optional
		public final String c;// checksum
		private int d;// dependency

		public GAVSO(String g, String a, String v) {
			this.g = g;
			this.a = a;
			this.v = v;
			this.s = null;
			this.o = null;
			this.c = null;
		}

		public GAVSO setDependency(int d) {
			this.d = d;
			return this;
		}

		public int getDependency() {
			return d;
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
			if (!StringUtil.isEmpty(c, true)) sct.setEL(KeyConstants._checksum, c);
			return sct;
		}

		/**
		 * same group and artifact id, but version MAY differ
		 * 
		 * @param gavso
		 * @return
		 */
		public boolean equalID(GAVSO other) {
			if (!g.equalsIgnoreCase(other.g)) return false;
			if (!a.equalsIgnoreCase(other.a)) return false;

			return true;
		}

		public boolean equalIDAndVersion(GAVSO other) {
			if (!v.equalsIgnoreCase(other.v)) return false;
			return equalID(other);
		}
	}

	public static boolean allowed(int allowedScopes, int scope) {
		return (allowedScopes & scope) != 0;
	}

	private static POM getDendency(List<POM> dependencies, String groupId, String artifactId) {
		if (dependencies != null) {
			for (POM pom: dependencies) {
				if (pom.getGroupId().equals(groupId) && pom.getArtifactId().equals(artifactId)) return pom;
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
						current.getDependencyScopeManagement(), log);
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

	public static void download(POM pom, Collection<Repository> repositories, String type, Log log) throws IOException {
		Resource res = pom.getArtifact(type);
		if (!res.isFile()) {
			synchronized (SystemUtil.createToken("mvn", res.getAbsolutePath())) {
				if (!res.isFile()) {
					try {
						URL url = pom.getArtifactAsURL(type, repositories);
						if (log != null) log.info("maven", "download [" + url + "]");
						try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
							HttpGet request = new HttpGet(url.toExternalForm());
							HttpResponse response = httpClient.execute(request);
							HttpEntity entity = response.getEntity();
							int sc = response.getStatusLine().getStatusCode();
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
								}
							}
							else {
								EntityUtils.consume(entity); // Ensure the response entity is fully consumed
								throw new IOException("Failed to download: " + url + " for [" + pom + "] - " + response.getStatusLine().getStatusCode());
							}
						}
					}
					catch (IOException ioe) {
						IOException ex = new IOException("Failed to download [" + type + "], because no local copy found at [" + res + "].");
						ExceptionUtil.initCauseEL(ex, ioe);
						// MUST add again ResourceUtil.deleteEmptyFoldersInside(pom.getLocalDirectory());
						throw ex;
					}
				}
			}
		} // TODO handle not 200
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
		return toScopes(ListUtil.listToStringArray(scopes, ','), defaultValue);
	}

	public static int toScopes(String[] scopes, int defaultValue) {
		if (scopes.length == 0) return defaultValue;

		int rtn = 0;
		for (String scope: scopes) {
			rtn += toScope(scope, 0);
		}
		if (rtn > 0) return rtn;

		return defaultValue;
	}

	public static int toScopes(String scopes) throws IOException {
		if (StringUtil.isEmpty(scopes, true)) throw new IOException("there is no scope defined");
		return toScopes(ListUtil.listToStringArray(scopes, ','));
	}

	public static int toScopes(String[] scopes) throws IOException {
		if (scopes.length == 0) throw new IOException("there is no scope defined");

		int rtn = 0, tmp;
		for (String scope: scopes) {
			tmp = toScope(scope, 0);
			if (tmp == 0) throw new IOException("scope [" + scope + "] is not a supported scope, valid scope names are [compile,test,provided,runtime,system,import]");
			rtn += tmp;
		}
		return rtn;
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
		Struct el = Caster.toStruct(obj, null);
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

	public static String createChecksum(Resource res, String algorithm) throws IOException {
		if ("md5".equalsIgnoreCase(algorithm)) return "md5:" + Hash.md5(res);
		else if ("sha1".equalsIgnoreCase(algorithm)) return "sha1:" + Hash.sha1(res);
		else if ("sha256".equalsIgnoreCase(algorithm)) return "sha256:" + Hash.sha256(res);
		else if ("sha512".equalsIgnoreCase(algorithm)) return "sha512:" + Hash.sha512(res);

		throw new IOException("Invalid checksum algorithm '" + algorithm + "'. Only the following algorithms are supported: md5, sha1, sha256, sha512");
	}
}
