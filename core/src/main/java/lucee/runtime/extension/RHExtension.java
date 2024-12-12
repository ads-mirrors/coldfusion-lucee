/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
 */
package lucee.runtime.extension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.Info;
import lucee.print;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigAdmin;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.Constants;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.functions.conversion.DeserializeJSON;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.BundleFile;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.osgi.VersionRange;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

/**
 * Extension completely handled by the engine and not by the Install/config.xml
 */
public class RHExtension implements Serializable {

	public static final short INSTALL_OPTION_NOT = 0;
	public static final short INSTALL_OPTION_IF_NECESSARY = 1;
	public static final short INSTALL_OPTION_FORCE = 2;

	public static final short ACTION_NONE = 0;
	public static final short ACTION_COPY = 1;
	public static final short ACTION_MOVE = 2;

	private static final long serialVersionUID = 2904020095330689714L;

	private static final BundleDefinition[] EMPTY_BD = new BundleDefinition[0];

	public static final int RELEASE_TYPE_ALL = 0;
	public static final int RELEASE_TYPE_SERVER = 1;
	public static final int RELEASE_TYPE_WEB = 2;

	private static final ExtensionResourceFilter LEX_FILTER = new ExtensionResourceFilter("lex");

	private static Set<String> metadataFilesChecked = new HashSet<>();
	private static Map<String, RHExtension> instances = new ConcurrentHashMap<>();

	private ExtensionMetadata metadata;
	private Resource extensionFile;

	// may not exist, only used for init
	private String _id;
	private String _version;
	private Config config;

	public static RHExtension getInstance(Config config, Resource ext, RHExtension defaultValue) {
		try {
			return getInstance(config, ext);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static RHExtension getInstance(Config config, ExtensionDefintion ext) throws PageException {
		Resource src = ext.getSource(null);
		if (src != null) {
			RHExtension instance = instances.get(src.getAbsolutePath());
			if (instance == null) {
				if (ext.getId() != null) {
					instance = new RHExtension(config, src, ext.getId(), ext.getVersion()).asyncInit();
				}
				else instance = new RHExtension(config, src).asyncInit();
				instances.put(src.getAbsolutePath(), instance);
			}
			return instance;
		}
		return getInstance(config, ext.getId(), ext.getVersion());
	}

	public static RHExtension getInstance(Config config, Resource ext) {
		RHExtension instance = instances.get(ext.getAbsolutePath());
		if (instance == null) {
			instance = new RHExtension(config, ext).asyncInit();
			instances.put(ext.getAbsolutePath(), instance);
		}
		return instance;
	}

	public static RHExtension getInstance(Config config, String id, String version) throws PageException {
		return getInstance(config, new ExtensionDefintion(id, version).setSource(config, getExtensionInstalledFile(config, id, version, false)));
	}

	private RHExtension(Config config, Resource ext) {
		this.config = config;
		this.extensionFile = ext;
	}

	private RHExtension(Config config, String id, String version) throws PageException {
		this.config = config;
		this._id = id;
		this._version = version;
		this.extensionFile = getExtensionInstalledFile(config, id, version, false);
		// softLoaded = false;
	}

	private RHExtension(Config config, Resource ext, String id, String version) {
		this.config = config;
		this.extensionFile = ext;
		this._id = id;
		this._version = version;
	}

	private RHExtension asyncInit() {
		ThreadUtil.getThread(() -> {
			try {
				getMetadata();
				// if (addTo != null) addTo.put(md.getId() + ":" + md.getVersion(), this);

			}
			catch (Exception e) {
				print.e(e);
				LogUtil.log("extension", e);
			}
		}, true).start();
		return this;
	}

	public ExtensionMetadata getMetadata() {
		if (metadata == null) {
			synchronized (this) {
				if (metadata == null) {
					print.e("> " + extensionFile + " " + Thread.currentThread().getId());
					ExtensionMetadata tmp = new ExtensionMetadata();
					if (_id != null && _version != null) {
						// do we have usefull meta data?
						Struct data = getMetaData(config, _id, _version, (Struct) null);
						if (data != null && data.containsKey("startBundles")) {
							try {
								readManifestConfig(config, tmp, _id, data, extensionFile.getAbsolutePath(), null);

								return metadata = tmp;
							}
							catch (InvalidVersion iv) {
								throw new PageRuntimeException(iv);
							}
							catch (ApplicationException ae) {
							}
						}
					}

					// init from file
					try {
						init(config, tmp, extensionFile);
					}
					catch (Exception e) {
						throw new PageRuntimeException(Caster.toPageException(e)); // MUST improve exception handling, no runtime
					}
					print.e("< " + extensionFile + " " + Thread.currentThread().getId());
					if (Thread.currentThread().getId() == 1L) print.ds();
					return metadata = tmp;
				}
			}
		}
		return this.metadata;
	}

	private static void init(Config config, ExtensionMetadata metadata, Resource extensionFile) throws PageException, IOException, BundleException, ConverterException {
		// make sure the config is registerd with the thread
		if (ThreadLocalPageContext.getConfig() == null) ThreadLocalConfig.register(config);
		// is it a web or server context?

		load(config, metadata, extensionFile);
		// write metadata to XML
		Resource mdf = getMetaDataFile(config, metadata._getId(), metadata._getVersion());
		if (!metadataFilesChecked.contains(mdf.getAbsolutePath()) && !mdf.isFile()) {
			Struct data = new StructImpl(Struct.TYPE_LINKED);
			populate(metadata, data, true);
			storeMetaData(mdf, data);
			metadataFilesChecked.add(mdf.getAbsolutePath()); // that way we only have to check this once
		}
	}

	public static RHExtension installExtension(ConfigPro config, String id, String version, String resource, boolean force) throws PageException, IOException {

		// get installed res
		Resource res = StringUtil.isEmpty(version) ? null : getExtensionInstalledFile(config, id, version, false);
		boolean installed = (res != null && res.isFile());

		if (!installed) {
			if (!StringUtil.isEmpty(resource) && (res = ResourceUtil.toResourceExisting(config, resource, null)) != null) {
				return DeployHandler.deployExtension(config, new ExtensionDefintion(id, version).setSource(config, res), null, false, true, true, new RefBooleanImpl());
			}
			else if (!StringUtil.isEmpty(id)) {
				return DeployHandler.deployExtension(config, new ExtensionDefintion(id, version), null, false, true, true, new RefBooleanImpl()); // MUSTT
			}
			else {
				throw new IOException("cannot install extension based on the given data [id:" + id + ";version:" + version + ";resource:" + resource + "]");
			}
		}
		// if forced we also install if it already is
		else if (force) {
			return DeployHandler.deployExtension(config, res, false, true, RHExtension.ACTION_NONE);
		}
		return getInstance(config, new ExtensionDefintion(id, version).setSource(config, res));
	}

	public static boolean isInstalled(Config config, String id, String version) throws PageException {
		Resource res = getExtensionInstalledFile(config, id, version, false);
		return res != null && res.isFile();
	}

	/**
	 * copy the extension resource file to the installed folder
	 * 
	 * @param ext
	 * @return
	 * @throws PageException
	 * @throws ConverterException
	 * @throws IOException
	 */
	public Resource copyToInstalled(Config config) throws PageException, ConverterException, IOException {
		if (extensionFile == null) throw new IOException("no extension file defined");
		if (!extensionFile.isFile()) throw new IOException("given extension file [" + extensionFile + "] does not exist");

		addToAvailable(config, extensionFile);
		return act(config, extensionFile, RHExtension.ACTION_COPY);
	}

	/**
	 * copy the extension resource file to the installed folder
	 * 
	 * @param ext
	 * @return
	 * @throws PageException
	 * @throws ConverterException
	 * @throws IOException
	 */
	public Resource moveToInstalled(Config config) throws PageException, ConverterException, IOException {
		if (extensionFile == null) throw new IOException("no extension file defined");
		if (!extensionFile.isFile()) throw new IOException("given extension file [" + extensionFile + "] does not exist");

		addToAvailable(config, extensionFile);
		return act(config, extensionFile, RHExtension.ACTION_MOVE);
	}

	public static void storeMetaData(Config config, String id, String version, Struct data) throws ConverterException, IOException {
		storeMetaData(getMetaDataFile(config, id, version), data);
	}

	private static void storeMetaData(Resource file, Struct data) throws ConverterException, IOException {
		JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
		String str = json.serialize(null, data, SerializationSettings.SERIALIZE_AS_ROW, true);
		ResourceUtil.createParentDirectoryIfNecessary(file);

		IOUtil.write(file, str, CharsetUtil.UTF8, false);
	}

	// copy the file to extension dir if it is not already there
	private Resource act(Config config, Resource ext, short action) throws PageException {
		Resource trg;
		Resource trgDir;
		try {
			trg = getExtensionInstalledFile(config, getId(), getVersion(), false);
			trgDir = trg.getParentResource();
			trgDir.mkdirs();
			if (!ext.getParentResource().equals(trgDir)) {
				if (trg.exists()) trg.delete();
				if (action == ACTION_COPY) {
					ext.copyTo(trg, false);
				}
				else if (action == ACTION_MOVE) {
					ResourceUtil.moveTo(ext, trg, true);
				}
				this.extensionFile = trg;
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return trg;
	}

	public void addToAvailable(Config config) {
		addToAvailable(config, getExtensionFile());
	}

	private void addToAvailable(Config config, Resource ext) {
		if (ext == null || ext.length() == 0 || getId() == null) return;
		Log logger = ThreadLocalPageContext.getLog(config, "deploy");
		Resource res;
		if (config instanceof ConfigWeb) {
			res = ((ConfigWeb) config).getConfigServerDir().getRealResource("extensions/");
		}
		else {
			res = config.getConfigDir().getRealResource("extensions/");
		}

		// parent exist?
		if (!res.isDirectory()) {
			logger.warn("extension", "directory [" + res + "] does not exist");
			return;
		}
		res = res.getRealResource("available/");

		// exist?
		if (!res.isDirectory()) {
			try {
				res.createDirectory(true);
			}
			catch (IOException e) {
				logger.error("extension", e);
				return;
			}
		}
		res = res.getRealResource(getId() + "-" + getVersion() + ".lex");
		if (res.length() == ext.length()) return;
		try {
			ResourceUtil.copy(ext, res);
			logger.info("extension", "copy [" + getId() + ":" + getVersion() + "] to [" + res + "]");
		}
		catch (IOException e) {
			logger.error("extension", e);
		}
	}

	public static Manifest getManifestFromFile(Config config, Resource file) throws IOException {
		ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(file.getInputStream()));
		ZipEntry entry;
		Manifest manifest = null;

		try {
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF")) {
					manifest = toManifest(config, zis, null);
				}
				zis.closeEntry();
				if (manifest != null) return manifest;
			}
		}
		finally {
			IOUtil.close(zis);
		}
		return null;

	}

	private static void load(Config config, ExtensionMetadata metadata, Resource ext) throws IOException, BundleException, ApplicationException {

		// print.e("-------" + ext);
		// long start = System.currentTimeMillis();
		// JarFile jf = new JarFile(ResourceUtil.toFile(ext));
		// jf.getManifest();
		// print.e("jarfile:" + (System.currentTimeMillis() - start));
		// start = System.currentTimeMillis();

		metadata.setType(config instanceof ConfigWeb ? "web" : "server");

		// no we read the content of the zip
		ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(ext.getInputStream()));
		ZipEntry entry;
		Manifest manifest = null;
		String _img = null;
		String path;
		String fileName, sub;
		String type;
		List<BundleInfo> bundles = new ArrayList<BundleInfo>();
		List<String> jars = new ArrayList<String>();
		List<String> flds = new ArrayList<String>();
		List<String> tlds = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		List<String> functions = new ArrayList<String>();
		List<String> contexts = new ArrayList<String>();
		List<String> configs = new ArrayList<String>();
		List<String> webContexts = new ArrayList<String>();
		List<String> applications = new ArrayList<String>();
		List<String> components = new ArrayList<String>();
		List<String> plugins = new ArrayList<String>();
		List<String> gateways = new ArrayList<String>();
		List<String> archives = new ArrayList<String>();

		try {
			while ((entry = zis.getNextEntry()) != null) {
				path = entry.getName();
				fileName = fileName(entry);
				sub = subFolder(entry);
				type = metadata.getType();
				if (!entry.isDirectory() && path.equalsIgnoreCase("META-INF/MANIFEST.MF")) {
					manifest = toManifest(config, zis, null);
				}
				else if (!entry.isDirectory() && path.equalsIgnoreCase("META-INF/logo.png")) {
					_img = toBase64(zis, null);
				}

				// jars
				else if (!entry.isDirectory() && (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles")
						|| startsWith(path, type, "bundle") || startsWith(path, type, "lib") || startsWith(path, type, "libs")) && (StringUtil.endsWithIgnoreCase(path, ".jar"))) {

							jars.add(fileName);
							BundleInfo bi = BundleInfo.getInstance(config, fileName, zis, false);
							if (bi.isBundle()) bundles.add(bi);
						}

				// flds
				else if (!entry.isDirectory() && startsWith(path, type, "flds") && (StringUtil.endsWithIgnoreCase(path, ".fld") || StringUtil.endsWithIgnoreCase(path, ".fldx")))
					flds.add(fileName);

				// tlds
				else if (!entry.isDirectory() && startsWith(path, type, "tlds") && (StringUtil.endsWithIgnoreCase(path, ".tld") || StringUtil.endsWithIgnoreCase(path, ".tldx")))
					tlds.add(fileName);

				// archives
				else if (!entry.isDirectory() && (startsWith(path, type, "archives") || startsWith(path, type, "mappings")) && StringUtil.endsWithIgnoreCase(path, ".lar"))
					archives.add(fileName);

				// event-gateway
				else if (!entry.isDirectory() && (startsWith(path, type, "event-gateways") || startsWith(path, type, "eventGateways"))
						&& (StringUtil.endsWithIgnoreCase(path, "." + Constants.getCFMLComponentExtension())))
					gateways.add(sub);

				// tags
				else if (!entry.isDirectory() && startsWith(path, type, "tags")) tags.add(sub);

				// functions
				else if (!entry.isDirectory() && startsWith(path, type, "functions")) functions.add(sub);

				// context
				else if (!entry.isDirectory() && startsWith(path, type, "context") && !StringUtil.startsWith(fileName(entry), '.')) contexts.add(sub);

				// web contextS
				else if (!entry.isDirectory() && (startsWith(path, type, "webcontexts") || startsWith(path, type, "web.contexts")) && !StringUtil.startsWith(fileName(entry), '.'))
					webContexts.add(sub);

				// config
				else if (!entry.isDirectory() && startsWith(path, type, "config") && !StringUtil.startsWith(fileName(entry), '.')) configs.add(sub);

				// applications
				else if (!entry.isDirectory() && (startsWith(path, type, "web.applications") || startsWith(path, type, "applications") || startsWith(path, type, "web"))
						&& !StringUtil.startsWith(fileName(entry), '.'))
					applications.add(sub);

				// components
				else if (!entry.isDirectory() && (startsWith(path, type, "components")) && !StringUtil.startsWith(fileName(entry), '.')) components.add(sub);

				// plugins
				else if (!entry.isDirectory() && (startsWith(path, type, "plugins")) && !StringUtil.startsWith(fileName(entry), '.')) plugins.add(sub);

				zis.closeEntry();
			}
		}
		finally {
			IOUtil.close(zis);
		}
		// print.e("zip:" + (System.currentTimeMillis() - start));
		// start = System.currentTimeMillis();
		// read the manifest
		if (manifest == null) throw new ApplicationException("The Extension [" + ext + "] is invalid,no Manifest file was found at [META-INF/MANIFEST.MF].");
		readManifestConfig(config, metadata, manifest, ext.getAbsolutePath(), _img);

		metadata.setJars(jars.toArray(new String[jars.size()]));
		metadata.setFlds(flds.toArray(new String[flds.size()]));
		metadata.setTlds(tlds.toArray(new String[tlds.size()]));
		metadata.setTags(tags.toArray(new String[tags.size()]));
		metadata.setFunctions(functions.toArray(new String[functions.size()]));
		metadata.setEventGateways(gateways.toArray(new String[gateways.size()]));
		metadata.setFunctions(archives.toArray(new String[archives.size()]));

		metadata.setContexts(contexts.toArray(new String[contexts.size()]));
		metadata.setConfigs(configs.toArray(new String[configs.size()]));
		metadata.setWebContexts(webContexts.toArray(new String[webContexts.size()]));
		metadata.setApplications(applications.toArray(new String[applications.size()]));
		metadata.setComponents(components.toArray(new String[components.size()]));
		metadata.setPlugins(plugins.toArray(new String[plugins.size()]));
		metadata.setBundles(bundles.toArray(new BundleInfo[bundles.size()]));

	}

	private static void readManifestConfig(Config config, ExtensionMetadata metadata, Manifest manifest, String label, String _img) throws ApplicationException {
		boolean isWeb = config instanceof ConfigWeb;
		metadata.setType(isWeb ? "web" : "server");
		Log logger = ThreadLocalPageContext.getLog(config, "deploy");
		Info info = ConfigUtil.getEngine(config).getInfo();

		Attributes attr = manifest.getMainAttributes();

		metadata.setSymbolicName(StringUtil.unwrap(attr.getValue("symbolic-name")));
		metadata.setName(StringUtil.unwrap(attr.getValue("name")), label);
		label = metadata.getName();
		metadata.setVersion(StringUtil.unwrap(attr.getValue("version")), label);
		label += " : " + metadata._getVersion();
		metadata.setId(StringUtil.unwrap(attr.getValue("id")), label);
		metadata.setDescription(StringUtil.unwrap(attr.getValue("description")));
		metadata.setTrial(Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("trial")), false));
		if (_img == null) _img = StringUtil.unwrap(attr.getValue("image"));
		metadata.setImage(_img);
		String cat = StringUtil.unwrap(attr.getValue("category"));
		if (StringUtil.isEmpty(cat, true)) cat = StringUtil.unwrap(attr.getValue("categories"));
		metadata.setCategories(cat);
		metadata.setMinCoreVersion(StringUtil.unwrap(attr.getValue("lucee-core-version")), info);
		metadata.setMinLoaderVersion(StringUtil.unwrap(attr.getValue("lucee-loader-version")), info);
		metadata.setStartBundles(Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("start-bundles")), true));

		metadata.setAMF(StringUtil.unwrap(attr.getValue("amf")), logger);
		metadata.setResource(StringUtil.unwrap(attr.getValue("resource")), logger);
		metadata.setSearch(StringUtil.unwrap(attr.getValue("search")), logger);
		metadata.setORM(StringUtil.unwrap(attr.getValue("orm")), logger);
		metadata.setWebservice(StringUtil.unwrap(attr.getValue("webservice")), logger);
		metadata.setMonitor(StringUtil.unwrap(attr.getValue("monitor")), logger);
		metadata.setCaches(StringUtil.unwrap(attr.getValue("cache")), logger);
		metadata.setCacheHandler(StringUtil.unwrap(attr.getValue("cache-handler")), logger);
		metadata.setJDBC(StringUtil.unwrap(attr.getValue("jdbc")), logger);
		metadata.setStartupHook(StringUtil.unwrap(attr.getValue("startup-hook")), logger);
		metadata.setMaven(StringUtil.unwrap(attr.getValue("maven")), logger);
		metadata.setMapping(StringUtil.unwrap(attr.getValue("mapping")), logger);
		metadata.setEventGatewayInstances(StringUtil.unwrap(attr.getValue("event-gateway-instance")), logger);
	}

	private static void readManifestConfig(Config config, ExtensionMetadata metadata, String id, Struct data, String label, String _img) throws ApplicationException {
		boolean isWeb = config instanceof ConfigWeb;
		metadata.setType(isWeb ? "web" : "server");

		Log logger = ThreadLocalPageContext.getLog(config, "deploy");
		Info info = ConfigUtil.getEngine(config).getInfo();

		metadata.setSymbolicName(ConfigFactoryImpl.getAttr(data, "symbolicName", "symbolic-name"));
		metadata.setName(ConfigFactoryImpl.getAttr(data, "name"), label);
		label = metadata.getName();
		metadata.setVersion(ConfigFactoryImpl.getAttr(data, "version"), label);
		label += " : " + metadata._getVersion();
		metadata.setId(StringUtil.isEmpty(id) ? ConfigFactoryImpl.getAttr(data, "id") : id, label);
		metadata.setDescription(ConfigFactoryImpl.getAttr(data, "description"));
		metadata.setTrial(Caster.toBooleanValue(ConfigFactoryImpl.getAttr(data, "trial"), false));
		if (_img == null) _img = ConfigFactoryImpl.getAttr(data, "image");
		metadata.setImage(_img);
		String cat = ConfigFactoryImpl.getAttr(data, "category");
		if (StringUtil.isEmpty(cat, true)) cat = ConfigFactoryImpl.getAttr(data, "categories");
		metadata.setCategories(cat);
		metadata.setMinCoreVersion(ConfigFactoryImpl.getAttr(data, "luceeCoreVersion", "lucee-core-version"), info);
		metadata.setMinLoaderVersion(ConfigFactoryImpl.getAttr(data, "luceeCoreVersion", "lucee-loader-version"), info);
		metadata.setStartBundles(Caster.toBooleanValue(ConfigFactoryImpl.getAttr(data, "startBundles", "start-bundles"), true));

		metadata.setAMF(ConfigFactoryImpl.getAttr(data, "amf"), logger);
		metadata.setResource(ConfigFactoryImpl.getAttr(data, "resource"), logger);
		metadata.setSearch(ConfigFactoryImpl.getAttr(data, "search"), logger);
		metadata.setORM(ConfigFactoryImpl.getAttr(data, "orm"), logger);
		metadata.setWebservice(ConfigFactoryImpl.getAttr(data, "webservice"), logger);
		metadata.setMonitor(ConfigFactoryImpl.getAttr(data, "monitor"), logger);
		metadata.setCaches(ConfigFactoryImpl.getAttr(data, "cache"), logger);
		metadata.setCacheHandler(ConfigFactoryImpl.getAttr(data, "cacheHandler", "cache-handler"), logger);
		metadata.setJDBC(ConfigFactoryImpl.getAttr(data, "jdbc"), logger);
		metadata.setStartupHook(ConfigFactoryImpl.getAttr(data, "startup-hook"), logger);
		metadata.setMaven(ConfigFactoryImpl.getAttr(data, "maven"), logger);
		metadata.setMapping(ConfigFactoryImpl.getAttr(data, "mapping"), logger);
		metadata.setEventGatewayInstances(ConfigFactoryImpl.getAttr(data, "eventGatewayInstance", "event-gateway-instance"), logger);
	}

	public void validate(Config config) throws ApplicationException {
		validate(ConfigUtil.getEngine(config).getInfo());
	}

	public void validate(Info info) throws ApplicationException {
		VersionRange minCoreVersion = getMetadata().getMinCoreVersion();
		if (minCoreVersion != null && !minCoreVersion.isWithin(info.getVersion())) {
			throw new InvalidVersion("The Extension [" + getMetadata().getName() + "] cannot be loaded, " + Constants.NAME + " Version must be at least ["
					+ minCoreVersion.toString() + "], version is [" + info.getVersion().toString() + "].");
		}
		if (getMetadata().getMinLoaderVersion() > SystemUtil.getLoaderVersion()) {
			throw new InvalidVersion("The Extension [" + getMetadata().getName() + "] cannot be loaded, " + Constants.NAME + " Loader Version must be at least ["
					+ getMetadata().getMinLoaderVersion() + "], update the Lucee.jar first.");
		}
	}

	public boolean isValidFor(Info info) {
		VersionRange minCoreVersion = getMetadata().getMinCoreVersion();
		if (minCoreVersion != null && !minCoreVersion.isWithin(info.getVersion())) {
			return false;
		}
		if (getMetadata().getMinLoaderVersion() > SystemUtil.getLoaderVersion()) {
			return false;
		}
		return true;
	}

	public void deployBundles(Config config, boolean load) throws IOException, BundleException {
		// no we read the content of the zip
		ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(extensionFile.getInputStream()));
		ZipEntry entry;
		String path;
		String fileName;
		String type;
		try {
			while ((entry = zis.getNextEntry()) != null) {
				path = entry.getName();
				fileName = fileName(entry);
				type = getMetadata().getType();
				// jars
				if (!entry.isDirectory() && (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles")
						|| startsWith(path, type, "bundle") || startsWith(path, type, "lib") || startsWith(path, type, "libs")) && (StringUtil.endsWithIgnoreCase(path, ".jar"))) {

					Object obj = ConfigAdmin.installBundle(config, zis, fileName, getVersion(), false, false);
					// jar is not a bundle, only a regular jar
					if (!(obj instanceof BundleFile)) {
						Resource tmp = (Resource) obj;
						Resource tmpJar = tmp.getParentResource().getRealResource(ListUtil.last(path, "\\/"));
						tmp.moveTo(tmpJar);
						ConfigAdmin.updateJar(config, tmpJar, false);
					}
					else if (load) {
						OSGiUtil.loadBundle((BundleFile) obj);
					}
				}

				zis.closeEntry();
			}
		}
		finally {
			IOUtil.close(zis);
		}
	}

	public static Resource getExtensionInstalledFile(Config config, String id, String version, boolean validate) throws ApplicationException {
		String fileName = toHash(id, version, "lex");
		Resource res = getExtensionInstalledDir(config).getRealResource(fileName);
		if (validate && !res.exists()) throw new ApplicationException("Extension [" + fileName + "] was not found at [" + res + "]");
		return res;
	}

	private static Struct getMetaData(Config config, String id, String version, Struct defaultValue) {
		Resource file = getMetaDataFile(config, id, version);
		if (file.isFile()) {
			try {
				return Caster.toStruct(new JSONExpressionInterpreter().interpret(null, IOUtil.toString(file, CharsetUtil.UTF8)));
			}
			catch (Exception e) {
			}
		}
		return defaultValue;
	}

	public static Resource getMetaDataFile(Config config, String id, String version) {
		String fileName = toHash(id, version, "mf");
		return getExtensionInstalledDir(config).getRealResource(fileName);
	}

	public static String toHash(String id, String version, String ext) {
		if (ext == null) ext = "lex";
		return HashUtil.create64BitHashAsString(id + version, Character.MAX_RADIX) + "." + ext;
	}

	public static Resource getExtensionInstalledDir(Config config) {
		return ((ConfigPro) config).getExtensionInstalledDir();
	}

	private static int getPhysicalExtensionCount(Config config) {
		final RefInteger count = new RefIntegerImpl(0);
		getExtensionInstalledDir(config).list(new ResourceNameFilter() {
			@Override
			public boolean accept(Resource res, String name) {
				if (StringUtil.endsWithIgnoreCase(name, ".lex")) count.plus(1);
				return false;
			}
		});
		return count.toInt();
	}

	public static void correctExtensions(Config config) throws PageException {
		// reduce the amount of extension stored in available
		{
			int max = 2;
			Resource dir = ((ConfigPro) config).getExtensionAvailableDir();
			Resource[] resources = dir.listResources(LEX_FILTER);
			if (resources.length < 60) return;
			Map<String, List<Pair<RHExtension, Resource>>> map = new HashMap<>();
			RHExtension ext;
			List<Pair<RHExtension, Resource>> versions;
			if (resources != null) {
				for (Resource r: resources) {
					ext = getInstance(config, r);
					versions = map.get(ext.getId());
					if (versions == null) map.put(ext.getId(), versions = new ArrayList<>());
					versions.add(new Pair<RHExtension, Resource>(ext, r));
				}
			}

			for (Entry<String, List<Pair<RHExtension, Resource>>> entry: map.entrySet()) {
				if (entry.getValue().size() > max) {
					List<Pair<RHExtension, Resource>> list = entry.getValue();
					Collections.sort(list, new Comparator<Pair<RHExtension, Resource>>() {
						@Override
						public int compare(Pair<RHExtension, Resource> l, Pair<RHExtension, Resource> r) {
							try {
								return OSGiUtil.compare(OSGiUtil.toVersion(r.getName().getVersion()), OSGiUtil.toVersion(l.getName().getVersion()));
							}
							catch (BundleException e) {
								return 0;
							}
						}
					});
					int count = 0;
					for (Pair<RHExtension, Resource> pair: list) {
						if (++count > max) {
							if (!pair.getValue().delete()) ResourceUtil.deleteOnExit(pair.getValue());
						}
					}

				}
			}
		}

		if (config instanceof ConfigWebPro) return;
		// extension defined in xml
		RHExtension[] xmlArrExtensions = ((ConfigPro) config).getRHExtensions();
		if (xmlArrExtensions.length == getPhysicalExtensionCount(config)) return; // all is OK
		RHExtension ext;
		Map<String, RHExtension> xmlExtensions = new HashMap<>();
		for (int i = 0; i < xmlArrExtensions.length; i++) {
			ext = xmlArrExtensions[i];
			xmlExtensions.put(ext.getId(), ext);
		}

		// Extension defined in filesystem
		Resource[] resources = getExtensionInstalledDir(config).listResources(LEX_FILTER);

		if (resources == null || resources.length == 0) return;
		int rt;
		RHExtension xmlExt;
		for (int i = 0; i < resources.length; i++) {
			ext = getInstance(config, resources[i]);
			xmlExt = xmlExtensions.get(ext.getId());
			if (xmlExt != null && (xmlExt.getVersion() + "").equals(ext.getVersion() + "")) continue;
			rt = ext.getMetadata().getReleaseType();
			ConfigAdmin._updateRHExtension((ConfigPro) config, resources[i], true, true, RHExtension.ACTION_MOVE);
		}
	}

	public static BundleDefinition[] toBundleDefinitions(String strBundles) {
		if (StringUtil.isEmpty(strBundles, true)) return EMPTY_BD;

		String[] arrStrs = toArray(strBundles);
		BundleDefinition[] arrBDs;
		if (!ArrayUtil.isEmpty(arrStrs)) {
			arrBDs = new BundleDefinition[arrStrs.length];
			int index;
			for (int i = 0; i < arrStrs.length; i++) {
				index = arrStrs[i].indexOf(':');
				if (index == -1) arrBDs[i] = new BundleDefinition(arrStrs[i].trim());
				else {
					try {
						arrBDs[i] = new BundleDefinition(arrStrs[i].substring(0, index).trim(), arrStrs[i].substring(index + 1).trim());
					}
					catch (BundleException e) {
						throw new PageRuntimeException(e);// should not happen
					}
				}
			}
		}
		else arrBDs = EMPTY_BD;
		return arrBDs;
	}

	public void populate(Struct el, boolean full) {
		populate(getMetadata(), el, full);
	}

	public static void populate(ExtensionMetadata metadata, Struct el, boolean full) {

		String id = metadata._getId();
		String name = metadata.getName();
		if (StringUtil.isEmpty(name)) name = id;

		if (!full) el.clear();

		el.setEL("id", id);
		el.setEL("name", name);
		el.setEL("version", metadata._getVersion());

		if (!full) return;

		// newly added
		// start bundles (IMPORTANT:this key is used to reconize a newer entry, so do not change)
		el.setEL("startBundles", Caster.toString(metadata.isStartBundles()));

		// release type
		el.setEL("releaseType", toReleaseType(metadata.getReleaseType(), "all"));

		// Description
		if (StringUtil.isEmpty(metadata.getDescription())) el.setEL("description", toStringForAttr(metadata.getDescription()));
		else el.removeEL(KeyImpl.init("description"));

		// Trial
		el.setEL("trial", Caster.toString(metadata.isTrial()));

		// Image
		if (StringUtil.isEmpty(metadata.getImage())) el.setEL("image", toStringForAttr(metadata.getImage()));
		else el.removeEL(KeyImpl.init("image"));

		// Categories
		String[] cats = metadata.getCategories();
		if (!ArrayUtil.isEmpty(cats)) {
			StringBuilder sb = new StringBuilder();
			for (String cat: cats) {
				if (sb.length() > 0) sb.append(',');
				sb.append(toStringForAttr(cat).replace(',', ' '));
			}
			el.setEL("categories", sb.toString());
		}
		else el.removeEL(KeyImpl.init("categories"));

		// core version
		VersionRange minCoreVersion = metadata.getMinCoreVersion();
		if (minCoreVersion != null) el.setEL("luceeCoreVersion", toStringForAttr(minCoreVersion.toString()));
		else el.removeEL(KeyImpl.init("luceeCoreVersion"));

		// loader version
		if (metadata.getMinLoaderVersion() > 0) el.setEL("loaderVersion", Caster.toString(metadata.getMinLoaderVersion()));
		else el.removeEL(KeyImpl.init("loaderVersion"));

		// amf
		if (!StringUtil.isEmpty(metadata.getAMFsRaw())) el.setEL("amf", toStringForAttr(metadata.getAMFsRaw()));
		else el.removeEL(KeyImpl.init("amf"));

		// resource
		if (!StringUtil.isEmpty(metadata.getResourcesRaw())) el.setEL("resource", toStringForAttr(metadata.getResourcesRaw()));
		else el.removeEL(KeyImpl.init("resource"));

		// search
		if (!StringUtil.isEmpty(metadata.getSearchsRaw())) el.setEL("search", toStringForAttr(metadata.getSearchsRaw()));
		else el.removeEL(KeyImpl.init("search"));

		// orm
		if (!StringUtil.isEmpty(metadata.getOrmsRaw())) el.setEL("orm", toStringForAttr(metadata.getOrmsRaw()));
		else el.removeEL(KeyImpl.init("orm"));

		// webservice
		if (!StringUtil.isEmpty(metadata.getWebservicesRaw())) el.setEL("webservice", toStringForAttr(metadata.getWebservicesRaw()));
		else el.removeEL(KeyImpl.init("webservice"));

		// monitor
		if (!StringUtil.isEmpty(metadata.getMonitorsRaw())) el.setEL("monitor", toStringForAttr(metadata.getMonitorsRaw()));
		else el.removeEL(KeyImpl.init("monitor"));

		// cache
		if (!StringUtil.isEmpty(metadata.getCachesRaw())) el.setEL("cache", toStringForAttr(metadata.getCachesRaw()));
		else el.removeEL(KeyImpl.init("cache"));

		// cache-handler
		if (!StringUtil.isEmpty(metadata.getCacheHandlersRaw())) el.setEL("cacheHandler", toStringForAttr(metadata.getCacheHandlersRaw()));
		else el.removeEL(KeyImpl.init("cacheHandler"));

		// jdbc
		if (!StringUtil.isEmpty(metadata.getJdbcsRaw())) el.setEL("jdbc", toStringForAttr(metadata.getJdbcsRaw()));
		else el.removeEL(KeyImpl.init("jdbc"));

		// startup-hook
		if (!StringUtil.isEmpty(metadata.getStartupHooksRaw())) el.setEL("startupHook", toStringForAttr(metadata.getStartupHooksRaw()));
		else el.removeEL(KeyImpl.init("startupHook"));

		// maven
		if (!StringUtil.isEmpty(metadata.getMavenRaw())) el.setEL("maven", toStringForAttr(metadata.getMavenRaw()));
		else el.removeEL(KeyImpl.init("maven"));

		// mapping
		if (!StringUtil.isEmpty(metadata.getMappingsRaw())) el.setEL("mapping", toStringForAttr(metadata.getMappingsRaw()));
		else el.removeEL(KeyImpl.init("mapping"));

		// event-gateway-instances
		if (!StringUtil.isEmpty(metadata.getEventGatewayInstancesRaw())) el.setEL("eventGatewayInstances", toStringForAttr(metadata.getEventGatewayInstancesRaw()));
		else el.removeEL(KeyImpl.init("eventGatewayInstances"));
	}

	private static String toStringForAttr(String str) {
		if (str == null) return "";
		return str;
	}

	private static String[] toArray(String str) {
		if (StringUtil.isEmpty(str, true)) return new String[0];
		return ListUtil.listToStringArray(str.trim(), ',');
	}

	public static Query toQuery(Config config, List<RHExtension> children, Query qry) throws PageException {
		Log log = ThreadLocalPageContext.getLog(config, "deploy");
		if (qry == null) qry = createQuery();
		Iterator<RHExtension> it = children.iterator();
		while (it.hasNext()) {
			try {
				it.next().populate(qry); // ,i+1
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log.error("extension", t);
			}
		}
		return qry;
	}

	public static Query toQuery(Config config, RHExtension[] children, Query qry) throws PageException {
		Log log = ThreadLocalPageContext.getLog(config, "deploy");
		if (qry == null) qry = createQuery();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				try {
					if (children[i] != null) children[i].populate(qry); // ,i+1
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log.log(Log.LEVEL_WARN, "extension", t);
				}
			}
		}
		return qry;
	}

	private static Query createQuery() throws DatabaseException {
		return new QueryImpl(
				new Key[] { KeyConstants._id, KeyConstants._version, KeyConstants._name, KeyConstants._symbolicName, KeyConstants._type, KeyConstants._description,
						KeyConstants._image, KeyConstants._releaseType, KeyConstants._trial, KeyConstants._categories, KeyConstants._startBundles, KeyConstants._bundles,
						KeyConstants._flds, KeyConstants._tlds, KeyConstants._tags, KeyConstants._functions, KeyConstants._contexts, KeyConstants._webcontexts,
						KeyConstants._config, KeyConstants._applications, KeyConstants._components, KeyConstants._plugins, KeyConstants._eventGateways, KeyConstants._archives },
				0, "Extensions");
	}

	private void populate(Query qry) throws PageException {
		int row = qry.addRow();
		ExtensionMetadata md = getMetadata();
		qry.setAt(KeyConstants._id, row, md._getId());
		qry.setAt(KeyConstants._name, row, md.getName());
		qry.setAt(KeyConstants._symbolicName, row, md.getSymbolicName());
		qry.setAt(KeyConstants._image, row, md.getImage());
		qry.setAt(KeyConstants._type, row, md.getType());
		qry.setAt(KeyConstants._description, row, md.getDescription());
		qry.setAt(KeyConstants._version, row, md._getVersion() == null ? null : md._getVersion().toString());
		qry.setAt(KeyConstants._trial, row, md.isTrial());
		qry.setAt(KeyConstants._releaseType, row, toReleaseType(md.getReleaseType(), "all"));
		// qry.setAt(JARS, row,Caster.toArray(getJars()));
		qry.setAt(KeyConstants._flds, row, Caster.toArray(md.getFlds()));
		qry.setAt(KeyConstants._tlds, row, Caster.toArray(md.getTlds()));
		qry.setAt(KeyConstants._functions, row, Caster.toArray(md.getFunctions()));
		qry.setAt(KeyConstants._archives, row, Caster.toArray(md.getArchives()));
		qry.setAt(KeyConstants._tags, row, Caster.toArray(md.getTags()));
		qry.setAt(KeyConstants._contexts, row, Caster.toArray(md.getContexts()));
		qry.setAt(KeyConstants._webcontexts, row, Caster.toArray(md.getWebContexts()));
		qry.setAt(KeyConstants._config, row, Caster.toArray(md.getConfigs()));
		qry.setAt(KeyConstants._eventGateways, row, Caster.toArray(md.getEventGateways()));
		qry.setAt(KeyConstants._categories, row, Caster.toArray(md.getCategories()));
		qry.setAt(KeyConstants._applications, row, Caster.toArray(md.getApplications()));
		qry.setAt(KeyConstants._components, row, Caster.toArray(md.getComponents()));
		qry.setAt(KeyConstants._plugins, row, Caster.toArray(md.getPlugins()));
		qry.setAt(KeyConstants._startBundles, row, Caster.toBoolean(md.isStartBundles()));

		BundleInfo[] bfs = md.getBundles();
		Query qryBundles = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._version }, bfs == null ? 0 : bfs.length, "bundles");
		if (bfs != null) {
			for (int i = 0; i < bfs.length; i++) {
				qryBundles.setAt(KeyConstants._name, i + 1, bfs[i].getSymbolicName());
				if (bfs[i].getVersion() != null) qryBundles.setAt(KeyConstants._version, i + 1, bfs[i].getVersionAsString());
			}
		}
		qry.setAt(KeyConstants._bundles, row, qryBundles);
	}

	public Struct toStruct() throws PageException {
		ExtensionMetadata md = getMetadata();
		Struct sct = new StructImpl();
		sct.set(KeyConstants._id, md._getId());
		sct.set(KeyConstants._symbolicName, md.getSymbolicName());
		sct.set(KeyConstants._name, md.getName());
		sct.set(KeyConstants._image, md.getImage());
		sct.set(KeyConstants._description, md.getDescription());
		sct.set(KeyConstants._version, md._getVersion() == null ? null : md._getVersion().toString());
		sct.set(KeyConstants._trial, md.isTrial());
		sct.set(KeyConstants._releaseType, toReleaseType(md.getReleaseType(), "all"));
		// sct.set(JARS, row,Caster.toArray(getJars()));
		try {
			sct.set(KeyConstants._flds, Caster.toArray(md.getFlds()));
			sct.set(KeyConstants._tlds, Caster.toArray(md.getTlds()));
			sct.set(KeyConstants._functions, Caster.toArray(md.getFunctions()));
			sct.set(KeyConstants._archives, Caster.toArray(md.getArchives()));
			sct.set(KeyConstants._tags, Caster.toArray(md.getTags()));
			sct.set(KeyConstants._contexts, Caster.toArray(md.getContexts()));
			sct.set(KeyConstants._webcontexts, Caster.toArray(md.getWebContexts()));
			sct.set(KeyConstants._config, Caster.toArray(md.getConfigs()));
			sct.set(KeyConstants._eventGateways, Caster.toArray(md.getEventGateways()));
			sct.set(KeyConstants._categories, Caster.toArray(md.getCategories()));
			sct.set(KeyConstants._applications, Caster.toArray(md.getApplications()));
			sct.set(KeyConstants._components, Caster.toArray(md.getComponents()));
			sct.set(KeyConstants._plugins, Caster.toArray(md.getPlugins()));
			sct.set(KeyConstants._startBundles, Caster.toBoolean(md.isStartBundles()));

			BundleInfo[] bfs = md.getBundles();
			Query qryBundles = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._version }, bfs == null ? 0 : bfs.length, "bundles");
			if (bfs != null) {
				for (int i = 0; i < bfs.length; i++) {
					qryBundles.setAt(KeyConstants._name, i + 1, bfs[i].getSymbolicName());
					if (bfs[i].getVersion() != null) qryBundles.setAt(KeyConstants._version, i + 1, bfs[i].getVersionAsString());
				}
			}
			sct.set(KeyConstants._bundles, qryBundles);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return sct;
	}

	public String getId() {
		if (metadata == null && _id != null) return _id;
		return getMetadata()._getId();
	}

	public String getVersion() {
		if (metadata == null && _version != null) return _version;
		return getMetadata()._getVersion();
	}

	private static Manifest toManifest(Config config, InputStream is, Manifest defaultValue) {
		try {
			Charset cs = config.getResourceCharset();
			String str = IOUtil.toString(is, cs);
			if (StringUtil.isEmpty(str, true)) return defaultValue;
			str = str.trim() + "\n";
			return new Manifest(new ByteArrayInputStream(str.getBytes(cs)));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	private static String toBase64(InputStream is, String defaultValue) {
		try {
			byte[] bytes = IOUtil.toBytes(is);
			if (ArrayUtil.isEmpty(bytes)) return defaultValue;
			return Caster.toB64(bytes, defaultValue);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static List<Map<String, String>> toSettings(Log log, String str) {
		List<Map<String, String>> list = new ArrayList<>();
		_toSettings(list, log, str, true);
		return list;
	}

	public static List<Map<String, Object>> toSettingsObj(Log log, String str) {
		List<Map<String, Object>> list = new ArrayList<>();
		_toSettings(list, log, str, false);
		return list;
	}

	private static void _toSettings(List list, Log log, String str, boolean valueAsString) {
		try {
			Object res = DeserializeJSON.call(null, str);
			// only a single row
			if (!Decision.isArray(res) && Decision.isStruct(res)) {
				_toSetting(list, Caster.toMap(res), valueAsString);
				return;
			}
			// multiple rows
			if (Decision.isArray(res)) {
				List tmpList = Caster.toList(res);
				Iterator it = tmpList.iterator();
				while (it.hasNext()) {
					_toSetting(list, Caster.toMap(it.next()), valueAsString);
				}
				return;
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log.error("Extension Installation", t);
		}

		return;
	}

	private static void _toSetting(List list, Map src, boolean valueAsString) throws PageException {
		Entry e;
		Iterator<Entry> it = src.entrySet().iterator();
		Map map = new HashMap();
		while (it.hasNext()) {
			e = it.next();
			map.put(Caster.toString(e.getKey()), valueAsString ? Caster.toString(e.getValue()) : e.getValue());
		}
		list.add(map);
	}

	private static boolean startsWith(String path, String type, String name) {
		return StringUtil.startsWithIgnoreCase(path, name + "/") || StringUtil.startsWithIgnoreCase(path, type + "/" + name + "/");
	}

	private static String fileName(ZipEntry entry) {
		String name = entry.getName();
		int index = name.lastIndexOf('/');
		if (index == -1) return name;
		return name.substring(index + 1);
	}

	private static String subFolder(ZipEntry entry) {
		String name = entry.getName();
		int index = name.indexOf('/');
		if (index == -1) return name;
		return name.substring(index + 1);
	}

	public Resource getExtensionFile() {
		if (!extensionFile.exists()) {
			Config c = ThreadLocalPageContext.getConfig();
			if (c != null) {
				Resource res = DeployHandler.getExtension(c, new ExtensionDefintion(this, getId(), getVersion()), null);
				if (res != null && res.exists()) {
					try {
						IOUtil.copy(res, extensionFile);
					}
					catch (IOException e) {
						res.delete();
					}
				}
			}
		}
		return extensionFile;
	}

	@Override
	public boolean equals(Object objOther) {
		if (objOther == this) return true;

		if (objOther instanceof RHExtension) {
			RHExtension other = (RHExtension) objOther;

			if (!getId().equals(other.getId())) return false;
			if (!getMetadata().getName().equals(other.getMetadata().getName())) return false;
			if (!getVersion().equals(other.getVersion())) return false;
			if (getMetadata().isTrial() != other.getMetadata().isTrial()) return false;
			return true;
		}
		if (objOther instanceof ExtensionDefintion) {
			ExtensionDefintion ed = (ExtensionDefintion) objOther;
			if (!ed.getId().equalsIgnoreCase(getId())) return false;
			if (ed.getVersion() == null || getVersion() == null) return true;
			return ed.getVersion().equalsIgnoreCase(getVersion());
		}
		return false;
	}

	public static String toReleaseType(int releaseType, String defaultValue) {
		if (releaseType == RELEASE_TYPE_WEB) return "web";
		if (releaseType == RELEASE_TYPE_SERVER) return "server";
		if (releaseType == RELEASE_TYPE_ALL) return "all";
		return defaultValue;
	}

	public static int toReleaseType(String releaseType, int defaultValue) {
		if ("web".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_WEB;
		if ("server".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_SERVER;
		if ("all".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_ALL;
		if ("both".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_ALL;
		return defaultValue;
	}

	public static List<ExtensionDefintion> toExtensionDefinitions(String str) {
		// first we split the list
		List<ExtensionDefintion> rtn = new ArrayList<ExtensionDefintion>();
		if (StringUtil.isEmpty(str)) return rtn;

		String[] arr = ListUtil.trimItems(ListUtil.listToStringArray(str, ','));
		if (ArrayUtil.isEmpty(arr)) return rtn;
		ExtensionDefintion ed;
		for (int i = 0; i < arr.length; i++) {
			ed = toExtensionDefinition(arr[i]);
			if (ed != null) rtn.add(ed);
		}
		return rtn;
	}

	// TODO call public static ExtensionDefintion toExtensionDefinition(String id, Map<String, String>
	// data)
	public static ExtensionDefintion toExtensionDefinition(String s) {
		if (StringUtil.isEmpty(s, true)) return null;
		s = s.trim();

		String[] arrr;
		int index;
		arrr = ListUtil.trimItems(ListUtil.listToStringArray(s, ';'));
		ExtensionDefintion ed = new ExtensionDefintion();
		String name;
		Resource res;
		Config c = ThreadLocalPageContext.getConfig();
		for (String ss: arrr) {
			res = null;
			index = ss.indexOf('=');
			if (index != -1) {
				name = ss.substring(0, index).trim();
				ed.setParam(name, ss.substring(index + 1).trim());
				if ("path".equalsIgnoreCase(name) && c != null) {
					res = ResourceUtil.toResourceExisting(c, ss.substring(index + 1).trim(), null);
				}
			}
			else if (ed.getId() == null || Decision.isUUId(ed.getId())) {
				if (c == null || Decision.isUUId(ss) || (res = ResourceUtil.toResourceExisting(ThreadLocalPageContext.getConfig(), ss.trim(), null)) == null) ed.setId(ss);
			}

			if (res != null && res.isFile()) {

				Resource trgDir = c.getLocalExtensionProviderDirectory();
				Resource trg = trgDir.getRealResource(res.getName());
				if (!res.equals(trg) && !trg.isFile()) {
					try {
						IOUtil.copy(res, trg);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (!trg.isFile()) continue;

				try {
					return getInstance(c, trg).toExtensionDefinition();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return ed;
	}

	public static ExtensionDefintion toExtensionDefinition(Config config, String id, Map<String, String> data) {
		if (data == null || data.size() == 0) return null;

		ExtensionDefintion ed = new ExtensionDefintion();

		// validate id
		if (Decision.isUUId(id)) {
			ed.setId(id);
		}

		String name;
		Resource res;
		config = ThreadLocalPageContext.getConfig(config);
		for (Entry<String, String> entry: data.entrySet()) {
			name = entry.getKey().trim();
			if (!"id".equalsIgnoreCase(name)) ed.setParam(name, entry.getValue().trim());
			if ("path".equalsIgnoreCase(name) || "url".equalsIgnoreCase(name) || "resource".equalsIgnoreCase(name)) {
				res = ResourceUtil.toResourceExisting(config, entry.getValue().trim(), null);

				if (ed.getId() == null && res != null && res.isFile()) {

					Resource trgDir = config.getLocalExtensionProviderDirectory();
					Resource trg = trgDir.getRealResource(res.getName());
					if (!res.equals(trg) && !trg.isFile()) {
						try {
							IOUtil.copy(res, trg);
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (!trg.isFile()) continue;

					try {
						return getInstance(config, trg).toExtensionDefinition();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
		if (ed.getId() == null) return null;
		return ed;

	}

	public static List<RHExtension> toRHExtensions(List<ExtensionDefintion> eds) throws PageException {
		try {
			final List<RHExtension> rtn = new ArrayList<RHExtension>();
			Iterator<ExtensionDefintion> it = eds.iterator();
			ExtensionDefintion ed;
			while (it.hasNext()) {
				ed = it.next();
				if (ed != null) rtn.add(ed.toRHExtension());
			}
			return rtn;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static class InvalidVersion extends ApplicationException {

		private static final long serialVersionUID = 8561299058941139724L;

		public InvalidVersion(String message) {
			super(message);
		}

	}

	public ExtensionDefintion toExtensionDefinition() {
		ExtensionMetadata md = getMetadata();
		ExtensionDefintion ed = new ExtensionDefintion(this, getId(), getVersion());
		ed.setParam("symbolic-name", md.getSymbolicName());
		ed.setParam("description", md.getDescription());
		if (extensionFile != null) ed.setSource(null, extensionFile);
		return ed;
	}

	@Override
	public String toString() {
		ExtensionMetadata md = getMetadata();
		ExtensionDefintion ed = new ExtensionDefintion(this, getId(), getVersion());
		ed.setParam("symbolic-name", md.getSymbolicName());
		ed.setParam("description", md.getDescription());
		return ed.toString();
	}

	public static void removeDuplicates(Array arrExtensions) throws PageException, BundleException {
		Iterator<Entry<Key, Object>> it = arrExtensions.entryIterator();
		Entry<Key, Object> e;
		Struct child;
		String id, version;
		Map<String, Pair<Version, Key>> existing = new HashMap<>();
		List<Integer> toremove = null;
		Pair<Version, Key> pair;
		while (it.hasNext()) {
			e = it.next();
			child = Caster.toStruct(e.getValue(), null);
			if (child == null) continue;
			id = Caster.toString(child.get(KeyConstants._id, null), null);
			if (StringUtil.isEmpty(id)) continue;
			pair = existing.get(id);
			version = Caster.toString(child.get(KeyConstants._version, null), null);
			if (StringUtil.isEmpty(version)) continue;
			Version nv = OSGiUtil.toVersion(version);
			if (pair != null) {
				if (toremove == null) toremove = new ArrayList<>();
				toremove.add(Caster.toInteger(OSGiUtil.isNewerThan(pair.getName(), nv) ? e.getKey() : pair.getValue()));

			}
			existing.put(id, new Pair<Version, Key>(nv, e.getKey()));
		}

		if (toremove != null) {
			int[] removes = ArrayUtil.toIntArray(toremove);
			Arrays.sort(removes);
			for (int i = removes.length - 1; i >= 0; i--) {
				arrExtensions.removeE(removes[i]);
			}
		}
	}

}
