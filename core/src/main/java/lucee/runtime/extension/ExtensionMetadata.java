package lucee.runtime.extension;

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
