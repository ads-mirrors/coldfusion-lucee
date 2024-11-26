package lucee.runtime.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.runtime.osgi.BundleInfo;

public class ExtensionMetadata {
	private static final String[] EMPTY = new String[0];

	private String id;
	private String version;
	private int releaseType;
	private String name;
	private String symbolicName;
	private String description;
	private String type;
	private boolean trial;
	private String image;
	private boolean startBundles;
	private BundleInfo[] bundles;

	private String[] jars;
	private String[] flds;
	private String[] tlds;
	private String[] tags;
	private String[] functions;
	private String[] archives;
	private String[] applications;
	private String[] components;
	private String[] plugins;
	private String[] contexts;
	private String[] configs;
	private String[] webContexts;
	private String[] categories;
	private String[] gateways;

	private List<Map<String, String>> caches;
	private String cachesRaw;

	private List<Map<String, String>> cacheHandlers;
	private String cacheHandlersRaw;

	private List<Map<String, String>> orms;
	private String ormsRaw;

	private List<Map<String, String>> webservices;
	private String webservicesRaw;

	private List<Map<String, String>> monitors;
	private String monitorsRaw;

	private List<Map<String, String>> resources;
	private String resourcesRaw;

	private List<Map<String, String>> searchs;
	private String searchsRaw;

	private List<Map<String, String>> amfs;
	private String amfsRaw;

	private List<Map<String, String>> jdbcs;
	private String jdbcsRaw;

	private List<Map<String, String>> startupHooks;
	private String startupHooksRaw;

	private List<Map<String, String>> mappings;
	private String mappingsRaw;

	private List<Map<String, Object>> eventGatewayInstances;
	private String eventGatewayInstancesRaw;

	public List<Map<String, Object>> getEventGatewayInstances() {
		return eventGatewayInstances;
	}

	public String getEventGatewayInstancesRaw() {
		return eventGatewayInstancesRaw;
	}

	public void setEventGatewayInstances(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			eventGatewayInstances = RHExtension.toSettingsObj(logger, str);
			eventGatewayInstancesRaw = str;
		}
		if (eventGatewayInstances == null) eventGatewayInstances = new ArrayList<Map<String, Object>>();
	}

	public List<Map<String, String>> getMappings() {
		return mappings;
	}

	public String getMappingsRaw() {
		return mappingsRaw;
	}

	public void setMapping(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			mappings = RHExtension.toSettings(logger, str);
			mappingsRaw = str;
		}
		if (mappings == null) mappings = new ArrayList<Map<String, String>>();
	}

	public List<Map<String, String>> getStartupHooks() {
		return startupHooks;
	}

	public String getStartupHooksRaw() {
		return startupHooksRaw;
	}

	public void setStartupHook(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			startupHooks = RHExtension.toSettings(logger, str);
			startupHooksRaw = str;
		}
		if (startupHooks == null) startupHooks = new ArrayList<Map<String, String>>();
	}

	public List<Map<String, String>> getJdbcs() {
		return jdbcs;
	}

	public String getJdbcsRaw() {
		return jdbcsRaw;
	}

	public void setJDBC(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			jdbcs = RHExtension.toSettings(logger, str);
			jdbcsRaw = str;
		}
		if (jdbcs == null) jdbcs = new ArrayList<Map<String, String>>();
	}

	public List<Map<String, String>> getAMFs() {
		return amfs;
	}

	public String getAMFsRaw() {
		return amfsRaw;
	}

	public void setAMF(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			amfs = RHExtension.toSettings(logger, str);
			amfsRaw = str;
		}
		if (amfs == null) amfs = new ArrayList<Map<String, String>>();
	}

	public List<Map<String, String>> getSearchs() {
		return searchs;
	}

	public String getSearchsRaw() {
		return searchsRaw;
	}

	public void setSearch(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			searchs = RHExtension.toSettings(logger, str);
			searchsRaw = str;
		}
		if (searchs == null) searchs = new ArrayList<Map<String, String>>();
	}

	public List<Map<String, String>> getResources() {
		return resources;
	}

	public String getResourcesRaw() {
		return resourcesRaw;
	}

	public void setResource(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			resources = RHExtension.toSettings(logger, str);
			resourcesRaw = str;
		}
		if (resources == null) resources = new ArrayList<Map<String, String>>();

	}

	public List<Map<String, String>> getMonitors() {
		return monitors;
	}

	public String getMonitorsRaw() {
		return monitorsRaw;
	}

	public void setMonitor(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			monitors = RHExtension.toSettings(logger, str);
			monitorsRaw = str;
		}
		if (monitors == null) monitors = new ArrayList<Map<String, String>>();
	}

	public List<Map<String, String>> getWebservices() {
		return webservices;
	}

	public String getWebservicesRaw() {
		return webservicesRaw;
	}

	public void setWebservice(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			webservices = RHExtension.toSettings(logger, str);
			webservicesRaw = str;
		}
		if (webservices == null) webservices = new ArrayList<Map<String, String>>();
	}

	public void setORM(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			orms = RHExtension.toSettings(logger, str);
			ormsRaw = str;
		}
		if (orms == null) orms = new ArrayList<Map<String, String>>();
	}

	public List<Map<String, String>> getOrms() {
		return orms;
	}

	public String getOrmsRaw() {
		return ormsRaw;
	}

	public List<Map<String, String>> getCacheHandlers() {
		return cacheHandlers;
	}

	public String getCacheHandlersRaw() {
		return cacheHandlersRaw;
	}

	public void setCacheHandler(String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			cacheHandlers = RHExtension.toSettings(logger, str);
			cacheHandlersRaw = str;
		}
		if (cacheHandlers == null) cacheHandlers = new ArrayList<Map<String, String>>();
	}

	public List<Map<String, String>> getCaches() {
		return caches;
	}

	public String getCachesRaw() {
		return cachesRaw;
	}

	public void setCaches(String raw, Log log) {
		if (!StringUtil.isEmpty(raw, true)) {
			caches = RHExtension.toSettings(log, raw);
			cachesRaw = raw;
		}
		if (caches == null) caches = new ArrayList<Map<String, String>>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSymbolicName() {
		return StringUtil.isEmpty(symbolicName) ? getId() : symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public int getReleaseType() {
		return releaseType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setReleaseType(int releaseType) {
		this.releaseType = releaseType;
	}

	public boolean isTrial() {
		return trial;
	}

	public void setTrial(boolean trial) {
		this.trial = trial;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public boolean isStartBundles() {
		return startBundles;
	}

	public void setStartBundles(boolean startBundles) {
		this.startBundles = startBundles;
	}

	public BundleInfo[] getBundles() {
		return bundles;
	}

	public BundleInfo[] getBundles(BundleInfo[] defaultValue) {
		return bundles;
	}

	public void setBundles(BundleInfo[] bundles) {
		this.bundles = bundles;
	}

	public String[] getEventGateways() {
		return gateways == null ? EMPTY : gateways;
	}

	public void setEventGateways(String[] gateways) {
		this.gateways = gateways;
	}

	public String[] getCategories() {
		return categories == null ? EMPTY : categories;
	}

	public void setCategories(String[] categories) {
		this.categories = categories;
	}

	public String[] getWebContexts() {
		return webContexts == null ? EMPTY : webContexts;
	}

	public void setWebContexts(String[] webContexts) {
		this.webContexts = webContexts;
	}

	public String[] getConfigs() {
		return configs == null ? EMPTY : configs;
	}

	public void setConfigs(String[] configs) {
		this.configs = configs;
	}

	public String[] getContexts() {
		return contexts == null ? EMPTY : contexts;
	}

	public void setContexts(String[] contexts) {
		this.contexts = contexts;
	}

	public String[] getPlugins() {
		return plugins == null ? EMPTY : plugins;
	}

	public void setPlugins(String[] plugins) {
		this.plugins = plugins;
	}

	public String[] getComponents() {
		return components == null ? EMPTY : components;
	}

	public void setComponents(String[] components) {
		this.components = components;
	}

	public String[] getApplications() {
		return applications == null ? EMPTY : applications;
	}

	public void setApplications(String[] applications) {
		this.applications = applications;
	}

	public String[] getArchives() {
		return archives == null ? EMPTY : archives;
	}

	protected void setArchives(String[] archives) {
		this.archives = archives;
	}

	public String[] getFlds() {
		return flds == null ? EMPTY : flds;
	}

	protected void setFlds(String[] flds) {
		this.flds = flds;
	}

	public String[] getTlds() {
		return tlds == null ? EMPTY : tlds;
	}

	protected void setTlds(String[] tlds) {
		this.tlds = tlds;
	}

	public String[] getTags() {
		return tags == null ? EMPTY : tags;
	}

	protected void setTags(String[] tags) {
		this.tags = tags;
	}

	public String[] getFunctions() {
		return functions == null ? EMPTY : functions;
	}

	protected void setFunctions(String[] functions) {
		this.functions = functions;
	}

	public String[] getJars() {
		return jars == null ? EMPTY : jars;
	}

	protected void setJars(String[] jars) {
		this.jars = jars;
	}
}
