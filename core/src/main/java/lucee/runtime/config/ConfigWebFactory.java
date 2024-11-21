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
package lucee.runtime.config;

import static lucee.runtime.db.DatasourceManagerImpl.QOQ_DATASOURCE_NAME;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.ServletConfig;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.xml.sax.SAXException;

import lucee.commons.collection.MapFactory;
import lucee.commons.date.TimeZoneConstants;
import lucee.commons.date.TimeZoneUtil;
import lucee.commons.digest.HashUtil;
import lucee.commons.digest.MD5;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.log.LoggerAndSourceData;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.type.cfml.CFMLResourceProvider;
import lucee.commons.io.res.type.s3.DummyS3ResourceProvider;
import lucee.commons.io.res.type.zip.ZipResourceProvider;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.retirement.RetireOutputStream;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.URLDecoder;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AIEngineFactory;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheConnectionImpl;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.request.RequestCacheHandler;
import lucee.runtime.cache.tag.timespan.TimespanCacheHandler;
import lucee.runtime.cfx.customtag.CFXTagClass;
import lucee.runtime.cfx.customtag.JavaCFXTagClass;
import lucee.runtime.config.ConfigBase.Startup;
import lucee.runtime.config.gateway.GatewayMap;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceImpl;
import lucee.runtime.db.JDBCDriver;
import lucee.runtime.db.ParamSyntax;
import lucee.runtime.dump.ClassicHTMLDumpWriter;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.dump.DumpWriterEntry;
import lucee.runtime.dump.HTMLDumpWriter;
import lucee.runtime.dump.SimpleHTMLDumpWriter;
import lucee.runtime.dump.TextDumpWriter;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ConsoleExecutionLog;
import lucee.runtime.engine.DebugExecutionLog;
import lucee.runtime.engine.ExecutionLog;
import lucee.runtime.engine.ExecutionLogFactory;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.engine.ThreadQueueImpl;
import lucee.runtime.engine.ThreadQueuePro;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.functions.other.CreateUUID;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.gateway.GatewayEntryImpl;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.listener.JavaSettings;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.listener.ModernAppListener;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.monitor.ActionMonitor;
import lucee.runtime.monitor.ActionMonitorCollector;
import lucee.runtime.monitor.ActionMonitorFatory;
import lucee.runtime.monitor.ActionMonitorWrap;
import lucee.runtime.monitor.AsyncRequestMonitor;
import lucee.runtime.monitor.IntervallMonitor;
import lucee.runtime.monitor.IntervallMonitorWrap;
import lucee.runtime.monitor.Monitor;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.monitor.RequestMonitorPro;
import lucee.runtime.monitor.RequestMonitorProImpl;
import lucee.runtime.monitor.RequestMonitorWrap;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.mail.ServerImpl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.DummyORMEngine;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMConfigurationImpl;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.ConstructorInstance;
import lucee.runtime.regex.Regex;
import lucee.runtime.regex.RegexFactory;
import lucee.runtime.search.DummySearchEngine;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.dynamic.meta.Constructor;
import lucee.transformer.dynamic.meta.Method;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibException;

public final class ConfigWebFactory extends ConfigFactory {

	private static final String TEMPLATE_EXTENSION = "cfm";
	private static final String COMPONENT_EXTENSION = "cfc";
	private static final String COMPONENT_EXTENSION_LUCEE = "lucee";
	public static final boolean LOG = true;
	private static final int DEFAULT_MAX_CONNECTION = 100;
	public static final String DEFAULT_LOCATION = Constants.DEFAULT_UPDATE_URL.toExternalForm();
	public static final ClassDefinition DUMMY_ORM_ENGINE = new ClassDefinitionImpl(DummyORMEngine.class);;

	public static ConfigWebPro newInstanceSingle(CFMLEngine engine, CFMLFactoryImpl factory, ConfigServerImpl configServer, Resource configDirWeb, ServletConfig servletConfig,
			ConfigWebImpl existingToUpdate) throws PageException {

		Resource configDir = configServer.getConfigDir();

		LogUtil.logGlobal(configServer, Log.LEVEL_INFO, ConfigWebFactory.class.getName(),
				"===================================================================\n" + "WEB CONTEXT (SINGLE) (" + createLabel(configServer, servletConfig) + ")\n"
						+ "-------------------------------------------------------------------\n" + "- config:" + configDir + "\n" + "- webroot:"
						+ ReqRspUtil.getRootPath(servletConfig.getServletContext()) + "\n" + "- label:" + createLabel(configServer, servletConfig) + "\n"
						+ "===================================================================\n"

		);

		boolean doNew = configServer.getUpdateInfo().updateType != NEW_NONE;
		ConfigWebPro configWeb = existingToUpdate != null ? existingToUpdate.setInstance(factory, configServer, servletConfig, configDirWeb)
				: new ConfigWebImpl(factory, configServer, servletConfig, configDirWeb);
		factory.setConfig(configServer, configWeb);

		// createContextFiles(configDir, servletConfig, doNew);
		settings(configWeb, ThreadLocalPageContext.getLog(configWeb, "application"));
		((ThreadQueueImpl) configWeb.getThreadQueue()).setMode(configWeb.getQueueEnable() ? ThreadQueuePro.MODE_ENABLED : ThreadQueuePro.MODE_DISABLED);

		// call web.cfc for this context
		((CFMLEngineImpl) ConfigWebUtil.getEngine(configWeb)).onStart(configWeb, false);

		((GatewayEngineImpl) configWeb.getGatewayEngine()).autoStart();

		return configWeb;
	}

	private static String createLabel(ConfigServerImpl configServer, ServletConfig servletConfig) {
		String hash = SystemUtil.hash(servletConfig.getServletContext());
		Map<String, String> labels = configServer.getLabels();
		String label = null;
		if (labels != null) {
			label = labels.get(hash);
		}
		if (label == null) label = hash;
		return label;
	}

	/**
	 * reloads the Config Object
	 * 
	 * @param cs
	 * @param force
	 * @throws SAXException
	 * @throws ClassNotFoundException
	 * @throws PageException
	 * @throws IOException
	 * @throws TagLibException
	 * @throws FunctionLibException
	 * @throws BundleException
	 * @throws NoSuchAlgorithmException
	 */ // MUST
	public static void reloadInstance(CFMLEngine engine, ConfigServerImpl cs, ConfigWebImpl cwi, boolean force)
			throws PageException, IOException, TagLibException, FunctionLibException, BundleException {

		cwi.reload();
		settings(cwi, null);
		return;
	}

	/**
	 * @param cs
	 * @param config
	 * @param doc
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws FunctionLibException
	 * @throws TagLibException
	 * @throws PageException
	 * @throws BundleException
	 */
	synchronized static void load(ConfigServerImpl config, Struct root, boolean isReload, boolean doNew, boolean essentialOnly) throws IOException {
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigWebFactory.class.getName(), "start reading config");
		ThreadLocalConfig.register(config);
		boolean reload = false;
		// load PW
		try {

			if (createSaltAndPW(root, config, essentialOnly)) reload = true;
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_DEBUG, ConfigWebFactory.class.getName(), "fixed salt");

			// reload when an old version of xml got updated
			if (reload) {
				root = reload(root, config, null);
				reload = false;
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, null, t);
		}

		config.setLastModified();

		Log log = config.getLog("application");
		_loadFilesystem(config, root, doNew, log); // load this before execute any code, what for example loadxtension does (json)

		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_DEBUG, ConfigWebFactory.class.getName(), "loaded filesystem");

		if (!essentialOnly) {
			_loadExtensionBundles(config, root, log);
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_DEBUG, ConfigWebFactory.class.getName(), "loaded extension");

		}
		else {
			_loadExtensionDefinition(config, root, log);
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_DEBUG, ConfigWebFactory.class.getName(), "loaded extension definitions");
		}

		if (!essentialOnly) {

			settings(config, log);
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_DEBUG, ConfigWebFactory.class.getName(), "loaded settings2");

			((CFMLEngineImpl) config.getEngine()).touchMonitor(config);
		}
		config.setLoadTime(System.currentTimeMillis());
	}

	private static boolean createSaltAndPW(Struct root, Config config, boolean essentialOnly) {
		if (root == null) return false;

		// salt
		String salt = getAttr(root, "adminSalt");
		if (StringUtil.isEmpty(salt, true)) salt = getAttr(root, "salt");
		boolean rtn = false;
		if (StringUtil.isEmpty(salt, true) || !Decision.isUUId(salt)) {
			// create salt
			root.setEL("salt", salt = CreateUUID.invoke());
			rtn = true;
		}

		// no password yet
		if (!essentialOnly && config instanceof ConfigServer && StringUtil.isEmpty(root.get("hspw", ""), true) && StringUtil.isEmpty(root.get("adminhspw", ""), true)
				&& StringUtil.isEmpty(root.get("pw", ""), true) && StringUtil.isEmpty(root.get("adminpw", ""), true) && StringUtil.isEmpty(root.get("password", ""), true)
				&& StringUtil.isEmpty(root.get("adminpassword", ""), true)) {
			ConfigServer cs = (ConfigServer) config;
			Resource pwFile = cs.getConfigDir().getRealResource("password.txt");
			if (pwFile.isFile()) {
				try {
					String pw = IOUtil.toString(pwFile, (Charset) null);
					if (!StringUtil.isEmpty(pw, true)) {
						pw = pw.trim();
						String hspw = new PasswordImpl(Password.ORIGIN_UNKNOW, pw, salt).getPassword();
						root.setEL("hspw", hspw);
						pwFile.delete();
						rtn = true;
					}
				}
				catch (IOException e) {
					LogUtil.logGlobal(cs, "application", e);
				}
			}
			else {
				LogUtil.log(config, Log.LEVEL_DEBUG, "application", "no password set and no password file found at [" + pwFile + "]");
			}
		}
		return rtn;
	}

	private static Struct reload(Struct root, ConfigImpl config, ConfigServerImpl cs) throws PageException, IOException, ConverterException {
		// store as json
		JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
		String str = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_ROW, true);
		IOUtil.write(config.getConfigFile(), str, CharsetUtil.UTF8, false);
		root = ConfigWebFactory.loadDocumentCreateIfFails(config.getConfigFile(), "web");
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigWebFactory.class.getName(), "reloading configuration");
		return root;
	}

	public static ResourceProvider loadDefaultResourceProvider(ConfigImpl config, Struct root, Log log) {
		try {
			Array defaultProviders = ConfigWebUtil.getAsArray("defaultResourceProvider", root);

			// Default Resource Provider
			if (defaultProviders != null && defaultProviders.size() > 0) {
				Struct defaultProvider = Caster.toStruct(defaultProviders.getE(defaultProviders.size()));
				ClassDefinition defProv = getClassDefinition(defaultProvider, "", config.getIdentification());

				String strDefaultProviderComponent = getAttr(defaultProvider, "component");
				if (StringUtil.isEmpty(strDefaultProviderComponent)) strDefaultProviderComponent = getAttr(defaultProvider, "class");

				// class
				if (defProv.hasClass()) {
					return toDefaultResourceProvider(defProv.getClazz(), toArguments(defaultProvider, "arguments", true, false));
				}

				// component
				else if (!StringUtil.isEmpty(strDefaultProviderComponent)) {
					strDefaultProviderComponent = strDefaultProviderComponent.trim();
					Map<String, String> args = toArguments(defaultProvider, "arguments", true, false);
					args.put("component", strDefaultProviderComponent);
					return toDefaultResourceProvider(CFMLResourceProvider.class, args);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return null;
	}

	private static ResourceProvider toDefaultResourceProvider(Class defaultProviderClass, Map arguments) throws ClassException {
		Object o = ClassUtil.loadInstance(defaultProviderClass);
		if (o instanceof ResourceProvider) {
			ResourceProvider rp = (ResourceProvider) o;
			rp.init(null, arguments);
			return rp;
		}
		else throw new ClassException("object [" + Caster.toClassName(o) + "] must implement the interface " + ResourceProvider.class.getName());
	}

	public static void loadResourceProvider(ConfigImpl config, Struct root, Log log) {
		try {
			Array providers = ConfigWebUtil.getAsArray("resourceProviders", root);

			// Resource Provider
			boolean hasZip = false, hasS3 = false;
			if (providers != null && providers.size() > 0) {
				ClassDefinition prov;
				String strProviderCFC;
				String strProviderScheme;
				ClassDefinition httpClass = null;
				Map httpArgs = null;
				boolean hasHTTPs = false;
				Iterator<?> pit = providers.getIterator();
				Struct provider;
				while (pit.hasNext()) {
					provider = Caster.toStruct(pit.next(), null);
					if (provider == null) continue;

					try {
						prov = getClassDefinition(provider, "", config.getIdentification());
						strProviderCFC = getAttr(provider, "component");
						if (StringUtil.isEmpty(strProviderCFC)) strProviderCFC = getAttr(provider, "class");

						// ignore OLD S3 extension from 4.0
						if ("lucee.extension.io.resource.type.s3.S3ResourceProvider".equals(prov.getClassName())
								|| "lucee.commons.io.res.type.s3.S3ResourceProvider".equals(prov.getClassName()))
							continue;

						strProviderScheme = getAttr(provider, "scheme");
						// class
						if (prov.hasClass() && !StringUtil.isEmpty(strProviderScheme)) {
							strProviderScheme = strProviderScheme.trim().toLowerCase();
							config.addResourceProvider(strProviderScheme, prov, toArguments(provider, "arguments", true, false));

							// patch for user not having
							if ("http".equalsIgnoreCase(strProviderScheme)) {
								httpClass = prov;
								httpArgs = toArguments(provider, "arguments", true, false);
							}
							else if ("https".equalsIgnoreCase(strProviderScheme)) hasHTTPs = true;
							else if ("zip".equalsIgnoreCase(strProviderScheme)) hasZip = true;
							else if ("s3".equalsIgnoreCase(strProviderScheme)) hasS3 = true;
						}

						// cfc
						else if (!StringUtil.isEmpty(strProviderCFC) && !StringUtil.isEmpty(strProviderScheme)) {
							strProviderCFC = strProviderCFC.trim();
							strProviderScheme = strProviderScheme.trim().toLowerCase();
							Map<String, String> args = toArguments(provider, "arguments", true, false);
							args.put("component", strProviderCFC);
							config.addResourceProvider(strProviderScheme, new ClassDefinitionImpl(CFMLResourceProvider.class), args);
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}

				// adding https when not exist
				if (!hasHTTPs && httpClass != null) {
					config.addResourceProvider("https", httpClass, httpArgs);
				}

			}
			// adding zip when not exist
			if (!hasZip) {
				Map<String, String> args = new HashMap<>();
				args.put("lock-timeout", "1000");
				args.put("case-sensitive", "1000");
				config.addResourceProvider("zip", new ClassDefinitionImpl<>(ZipResourceProvider.class), args);
			}

			if (!hasS3) {
				ClassDefinition s3Class = new ClassDefinitionImpl(DummyS3ResourceProvider.class);
				Map<String, String> args = new HashMap<>();
				args.put("lock-timeout", "10000");
				config.addResourceProvider("s3", s3Class, args);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
	}

	public static <T> ClassDefinition<T> getClassDefinition(Struct data, String prefix, Identification id) throws PageException {
		String attrName;
		String cn;

		// FUTURE remove
		if (StringUtil.isEmpty(prefix)) {
			cn = getAttr(data, "class");
			attrName = "class";
		}
		else {
			if (prefix.endsWith("-")) prefix = prefix.substring(0, prefix.length() - 1);
			cn = getAttr(data, prefix + "Class");
			attrName = prefix + "Class";
		}

		// proxy jar library no longer provided, so if still this class name is used ....
		if (cn != null && "com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cn)) {
			data.set(attrName, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		}

		ClassDefinition<T> cd = ClassDefinitionImpl.toClassDefinitionImpl(data, prefix, true, id);
		return cd;
	}

	public static HashMap<String, Class<CacheHandler>> loadCacheHandler(ConfigImpl config, Struct root, Log log) {
		HashMap<String, Class<CacheHandler>> cacheHandlers = new HashMap<String, Class<CacheHandler>>();

		try {

			// first of all we make sure we have a request and timespan cachehandler
			addCacheHandler(cacheHandlers, "request", new ClassDefinitionImpl(RequestCacheHandler.class));
			addCacheHandler(cacheHandlers, "timespan", new ClassDefinitionImpl(TimespanCacheHandler.class));

			Struct handlers = ConfigWebUtil.getAsStruct("cacheHandlers", root);
			if (handlers != null) {
				ClassDefinition cd;
				String strId;
				Iterator<Entry<Key, Object>> it = handlers.entryIterator();
				Entry<Key, Object> entry;
				Struct handler;
				while (it.hasNext()) {
					try {
						entry = it.next();

						handler = Caster.toStruct(entry.getValue(), null);
						if (handler == null) continue;

						cd = getClassDefinition(handler, "", config.getIdentification());
						strId = entry.getKey().getString();
						if (cd.hasClass() && !StringUtil.isEmpty(strId)) {
							strId = strId.trim().toLowerCase();
							try {
								addCacheHandler(cacheHandlers, strId, cd);
							}
							catch (Throwable t) {
								ExceptionUtil.rethrowIfNecessary(t);
								log.error("Cache-Handler", t);
							}
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}
		}
		catch (Throwable th) {
			ExceptionUtil.rethrowIfNecessary(th);
			log(config, log, th);
		}
		return cacheHandlers;
	}

	private static void addCacheHandler(HashMap<String, Class<CacheHandler>> cacheHandlers, String id, ClassDefinition<CacheHandler> cd) throws ClassException, BundleException {
		Class<CacheHandler> clazz = cd.getClazz();
		Object o = ClassUtil.loadInstance(clazz); // just try to load and forget afterwards
		if (o instanceof CacheHandler) {
			cacheHandlers.put(id, clazz);
		}
		else throw new ClassException("object [" + Caster.toClassName(o) + "] must implement the interface " + CacheHandler.class.getName());
	}

	public static Map<String, AIEngineFactory> loadAI(ConfigImpl config, Struct root, Log log, Map<String, AIEngineFactory> defaultValue) {
		try {
			// we only load this for the server context
			Struct ai = ConfigWebUtil.getAsStruct(root, false, "ai");
			if (ai != null) {

				ClassDefinition<AIEngine> cd;
				String strId;
				Iterator<Entry<Key, Object>> it = ai.entryIterator();
				Entry<Key, Object> entry;
				Struct data, custom;
				String _default;
				Map<String, AIEngineFactory> engines = new HashMap<>();

				while (it.hasNext()) {
					try {
						entry = it.next();
						data = Caster.toStruct(entry.getValue(), null);
						if (data == null) continue;
						strId = entry.getKey().getString();

						cd = getClassDefinition(data, "", config.getIdentification());
						if (cd.hasClass() && !StringUtil.isEmpty(strId)) {
							strId = strId.trim().toLowerCase();

							custom = Caster.toStruct(data.get(KeyConstants._custom, null), null);
							if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._properties, null), null);
							if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._arguments, null), null);
							_default = Caster.toString(data.get(KeyConstants._default, null), null);
							engines.put(strId, AIEngineFactory.load(config, cd, custom, strId, _default, false));
						}
					}
					catch (Exception e) {
						log(config, log, e);
					}
				}
				return engines;
			}
		}
		catch (Exception ex) {
			log(config, log, ex);
		}
		return defaultValue;
	}

	public static DumpWriterEntry[] loadDumpWriter(ConfigImpl config, Struct root, Log log, DumpWriterEntry[] defaultValue) {
		try {
			Array writers = ConfigWebUtil.getAsArray("dumpWriters", root);

			Struct sct = new StructImpl();

			boolean hasPlain = false;
			boolean hasRich = false;

			if (writers != null && writers.size() > 0) {
				ClassDefinition cd;
				String strName;
				String strDefault;
				Class clazz;
				int def = HTMLDumpWriter.DEFAULT_NONE;
				Iterator<?> it = writers.getIterator();
				Struct writer;
				while (it.hasNext()) {
					try {
						writer = Caster.toStruct(it.next(), null);
						if (writer == null) continue;

						cd = getClassDefinition(writer, "", config.getIdentification());
						strName = getAttr(writer, "name");
						strDefault = getAttr(writer, "default");
						clazz = cd.getClazz(null);
						if (clazz != null && !StringUtil.isEmpty(strName)) {
							if (StringUtil.isEmpty(strDefault)) def = HTMLDumpWriter.DEFAULT_NONE;
							else if ("browser".equalsIgnoreCase(strDefault)) def = HTMLDumpWriter.DEFAULT_RICH;
							else if ("console".equalsIgnoreCase(strDefault)) def = HTMLDumpWriter.DEFAULT_PLAIN;
							sct.put(strName, new DumpWriterEntry(def, strName, (DumpWriter) ClassUtil.loadInstance(clazz)));
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}
			else {
				// print.err("yep");
				if (!hasRich) sct.setEL(KeyConstants._html, new DumpWriterEntry(HTMLDumpWriter.DEFAULT_RICH, "html", new HTMLDumpWriter()));
				if (!hasPlain) sct.setEL(KeyConstants._text, new DumpWriterEntry(HTMLDumpWriter.DEFAULT_PLAIN, "text", new TextDumpWriter()));

				sct.setEL(KeyConstants._classic, new DumpWriterEntry(HTMLDumpWriter.DEFAULT_NONE, "classic", new ClassicHTMLDumpWriter()));
				sct.setEL(KeyConstants._simple, new DumpWriterEntry(HTMLDumpWriter.DEFAULT_NONE, "simple", new SimpleHTMLDumpWriter()));

			}
			Iterator<Object> it = sct.valueIterator();
			java.util.List<DumpWriterEntry> entries = new ArrayList<DumpWriterEntry>();
			while (it.hasNext()) {
				entries.add((DumpWriterEntry) it.next());
			}
			return entries.toArray(new DumpWriterEntry[entries.size()]);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return defaultValue;
	}

	static Map<String, String> toArguments(Struct coll, String name, boolean decode, boolean lowerKeys) throws PageException {
		Map<String, String> map = new HashMap<>();
		Object obj = coll.get(name, null);
		if (obj == null) return map;

		if (Decision.isStruct(obj)) {
			Iterator<Entry<Key, Object>> it = Caster.toStruct(obj).entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				map.put(lowerKeys ? e.getKey().getLowerString() : e.getKey().getString(), Caster.toString(e.getValue())); // TODO remove need to cast to string
			}
		}
		if (Decision.isString(obj)) {
			String[] arr = ListUtil.toStringArray(ListUtil.listToArray(Caster.toString(obj), ';'), null);

			int index;
			String str;
			for (int i = 0; i < arr.length; i++) {
				str = arr[i].trim();
				if (StringUtil.isEmpty(str)) continue;
				index = str.indexOf(':');
				if (index == -1) map.put(lowerKeys ? str.toLowerCase() : str, "");
				else {
					String k = dec(str.substring(0, index).trim(), decode);
					if (lowerKeys) k = k.toLowerCase();
					map.put(k, dec(str.substring(index + 1).trim(), decode));
				}
			}
			return map;
		}
		return map;
	}

	@Deprecated
	public static Struct cssStringToStruct(String attributes, boolean decode, boolean lowerKeys) {
		Struct sct = new StructImpl();
		if (StringUtil.isEmpty(attributes, true)) return sct;
		String[] arr = ListUtil.toStringArray(ListUtil.listToArray(attributes, ';'), null);

		int index;
		String str;
		for (int i = 0; i < arr.length; i++) {
			str = arr[i].trim();
			if (StringUtil.isEmpty(str)) continue;
			index = str.indexOf(':');
			if (index == -1) sct.setEL(lowerKeys ? str.toLowerCase() : str, "");
			else {
				String k = dec(str.substring(0, index).trim(), decode);
				if (lowerKeys) k = k.toLowerCase();
				sct.setEL(k, dec(str.substring(index + 1).trim(), decode));
			}
		}
		return sct;
	}

	private static String dec(String str, boolean decode) {
		if (!decode) return str;
		return URLDecoder.decode(str, false);
	}

	public static ConfigListener loadListener(ConfigServerImpl config, Struct root, Log log, ConfigListener defaultValue) {
		try {
			Struct listener = ConfigWebUtil.getAsStruct("listener", root);
			ClassDefinition cd = listener != null ? getClassDefinition(listener, "", config.getIdentification()) : null;
			String strArguments = getAttr(listener, "arguments");
			if (strArguments == null) strArguments = "";

			if (cd != null && cd.hasClass()) {
				try {
					Object obj = ClassUtil.loadInstance(cd.getClazz(), new Object[] { strArguments }, null);
					if (obj instanceof ConfigListener) {
						ConfigListener cl = (ConfigListener) obj;
						return cl;
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, log, t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return defaultValue;
	}

	private static void settings(ConfigPro config, Log log) {
		try {
			doCheckChangesInLibraries(config);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
	}

	public static IdentificationServerImpl loadId(ConfigServerImpl config, Struct root, Log log, IdentificationServerImpl defaultValue) {
		try {

			// Security key
			Resource res = config.getConfigDir().getRealResource("id");
			String securityKey = null;
			try {
				if (!res.exists()) {
					res.createNewFile();
					IOUtil.write(res, securityKey = UUID.randomUUID().toString(), SystemUtil.getCharset(), false);
				}
				else {
					securityKey = IOUtil.toString(res, SystemUtil.getCharset());
				}
			}
			catch (Exception ioe) {
				LogUtil.logGlobal(config, ConfigWebFactory.class.getName(), ioe);
			}
			if (StringUtil.isEmpty(securityKey)) securityKey = UUID.randomUUID().toString();

			// API Key
			String apiKey = null;
			String str = root != null ? getAttr(root, "apiKey") : null;
			if (!StringUtil.isEmpty(str, true)) apiKey = str.trim();
			return new IdentificationServerImpl(config, securityKey, apiKey);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			LogUtil.logGlobal(config, ConfigWebFactory.class.getName(), t);
		}
		return defaultValue;
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 */
	public static int loadSecurity(ConfigImpl config, Struct root, Log log) {
		try {
			Struct security = ConfigWebUtil.getAsStruct("security", root);
			int vu = ConfigPro.QUERY_VAR_USAGE_UNDEFINED;
			if (security != null) {
				vu = AppListenerUtil.toVariableUsage(getAttr(security, "variableUsage"), ConfigPro.QUERY_VAR_USAGE_UNDEFINED);
				if (vu == ConfigPro.QUERY_VAR_USAGE_UNDEFINED) vu = AppListenerUtil.toVariableUsage(getAttr(security, "varUsage"), ConfigPro.QUERY_VAR_USAGE_UNDEFINED);
			}
			if (vu == ConfigPro.QUERY_VAR_USAGE_UNDEFINED) {
				vu = ConfigPro.QUERY_VAR_USAGE_IGNORE;
			}
			return vu;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			t.printStackTrace();
			log(config, log, t);
		}
		return ConfigPro.QUERY_VAR_USAGE_IGNORE;
	}

	private static Resource[] _loadFileAccess(Config config, Array fileAccesses, Log log) {
		if (fileAccesses.size() == 0) return new Resource[0];
		java.util.List<Resource> reses = new ArrayList<Resource>();
		String path;
		Resource res;
		Iterator<?> it = fileAccesses.getIterator();
		Struct fa;
		while (it.hasNext()) {
			try {
				fa = Caster.toStruct(it.next(), null);
				if (fa == null) continue;

				path = getAttr(fa, "path");
				if (!StringUtil.isEmpty(path)) {
					res = config.getResource(path);
					if (res.isDirectory()) reses.add(res);
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log(config, log, t);
			}
		}
		// temp directory should be always accessible, even when access is local
		reses.add(config.getTempDirectory());
		return reses.toArray(new Resource[reses.size()]);
	}

	private static SecurityManagerImpl _toSecurityManager(Struct el) {
		SecurityManagerImpl sm = new SecurityManagerImpl(_attr(el, "setting", SecurityManager.VALUE_YES), _attr(el, "file", SecurityManager.VALUE_ALL),
				_attr(el, "direct_java_access", SecurityManager.VALUE_YES), _attr(el, "mail", SecurityManager.VALUE_YES), _attr(el, "datasource", SecurityManager.VALUE_YES),
				_attr(el, "mapping", SecurityManager.VALUE_YES), _attr(el, "remote", SecurityManager.VALUE_YES), _attr(el, "custom_tag", SecurityManager.VALUE_YES),
				_attr(el, "cfx_setting", SecurityManager.VALUE_YES), _attr(el, "cfx_usage", SecurityManager.VALUE_YES), _attr(el, "debugging", SecurityManager.VALUE_YES),
				_attr(el, "search", SecurityManager.VALUE_YES), _attr(el, "scheduled_task", SecurityManager.VALUE_YES), _attr(el, "tag_execute", SecurityManager.VALUE_YES),
				_attr(el, "tag_import", SecurityManager.VALUE_YES), _attr(el, "tag_object", SecurityManager.VALUE_YES), _attr(el, "tag_registry", SecurityManager.VALUE_YES),
				_attr(el, "cache", SecurityManager.VALUE_YES), _attr(el, "gateway", SecurityManager.VALUE_YES), _attr(el, "orm", SecurityManager.VALUE_YES),
				_attr2(el, "access_read", SecurityManager.ACCESS_PROTECTED), _attr2(el, "access_write", SecurityManager.ACCESS_PROTECTED));
		return sm;
	}

	public static SecurityManagerImpl _toSecurityManagerSingle(Struct el) {
		SecurityManagerImpl sm = (SecurityManagerImpl) SecurityManagerImpl.getOpenSecurityManager();
		sm.setAccess(SecurityManager.TYPE_ACCESS_READ, _attr2(el, "access_read", SecurityManager.ACCESS_PROTECTED));
		sm.setAccess(SecurityManager.TYPE_ACCESS_WRITE, _attr2(el, "access_write", SecurityManager.ACCESS_PROTECTED));
		sm.setAccess(SecurityManager.TYPE_REMOTE, _attr(el, "remote", SecurityManager.VALUE_YES));
		return sm;
	}

	private static short _attr(Struct el, String attr, short _default) {
		return SecurityManagerImpl.toShortAccessValue(getAttr(el, attr), _default);
	}

	private static short _attr2(Struct el, String attr, short _default) {
		String strAccess = getAttr(el, attr);
		if (StringUtil.isEmpty(strAccess)) return _default;
		strAccess = strAccess.trim().toLowerCase();
		if ("open".equals(strAccess)) return SecurityManager.ACCESS_OPEN;
		if ("protected".equals(strAccess)) return SecurityManager.ACCESS_PROTECTED;
		if ("close".equals(strAccess)) return SecurityManager.ACCESS_CLOSE;
		return _default;
	}

	static String createMD5FromResource(String resource) throws IOException {
		InputStream is = null;
		try {
			is = InfoImpl.class.getResourceAsStream(resource);
			byte[] barr = IOUtil.toBytes(is);
			return MD5.getDigestAsString(barr);
		}
		finally {
			IOUtil.close(is);
		}
	}

	static String createContentFromResource(Resource resource) throws IOException {
		return IOUtil.toString(resource, (Charset) null);
	}

	static void createFileFromResourceCheckSizeDiffEL(String resource, Resource file) {
		try {
			createFileFromResourceCheckSizeDiff(resource, file);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, ConfigWebFactory.class.getName(), resource);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, ConfigWebFactory.class.getName(), file + "");
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigWebFactory.class.getName(), t);
		}
	}

	/**
	 * creates a File and his content froma a resurce
	 * 
	 * @param resource
	 * @param file
	 * @throws IOException
	 */
	static void createFileFromResourceCheckSizeDiff(String resource, Resource file) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtil.copy(InfoImpl.class.getResourceAsStream(resource), baos, true, false);
		byte[] barr = baos.toByteArray();

		if (file.exists()) {
			long trgSize = file.length();
			long srcSize = barr.length;
			if (srcSize == trgSize) return;

			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigWebFactory.class.getName(), "update file:" + file);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigWebFactory.class.getName(), " - source:" + srcSize);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigWebFactory.class.getName(), " - target:" + trgSize);

		}
		else file.createNewFile();
		IOUtil.copy(new ByteArrayInputStream(barr), file, true);
	}

	private static void doCheckChangesInLibraries(ConfigPro config) {
		// create current hash from libs
		TagLib[] tlds = config.getTLDs();
		FunctionLib flds = config.getFLDs();

		StringBuilder sb = new StringBuilder();

		// charset
		sb.append(config.getTemplateCharset().name()).append(';');

		// dot notation upper case
		_getDotNotationUpperCase(sb, config.getMappings());
		_getDotNotationUpperCase(sb, config.getCustomTagMappings());
		_getDotNotationUpperCase(sb, config.getComponentMappings());
		_getDotNotationUpperCase(sb, config.getFunctionMappings());
		_getDotNotationUpperCase(sb, config.getTagMappings());
		// _getDotNotationUpperCase(sb,config.getServerTagMapping());
		// _getDotNotationUpperCase(sb,config.getServerFunctionMapping());

		// suppress ws before arg
		sb.append(config.getSuppressWSBeforeArg());
		sb.append(';');

		// externalize strings
		sb.append(config.getExternalizeStringGTE());
		sb.append(';');

		// function output
		sb.append(config.getDefaultFunctionOutput());
		sb.append(';');

		// preserve Case
		sb.append(config.preserveCase());
		sb.append(';');

		// full null support
		// sb.append(config.getFull Null Support()); // no longer a compiler switch
		// sb.append(';');

		// fusiondebug or not (FD uses full path name)
		sb.append(config.allowRequestTimeout());
		sb.append(';');

		// tld
		for (int i = 0; i < tlds.length; i++) {
			sb.append(tlds[i].getHash());
		}
		// fld
		sb.append(flds.getHash());

		if (!(config instanceof ConfigServer)) {
			boolean hasChanged = false;

			try {
				String hashValue = HashUtil.create64BitHashAsString(sb.toString());
				// check and compare lib version file
				Resource libHash = config.getConfigDir().getRealResource("lib-hash");
				if (!libHash.exists()) {
					libHash.createNewFile();
					IOUtil.write(libHash, hashValue, SystemUtil.getCharset(), false);
					hasChanged = true;
				}
				else if (!IOUtil.toString(libHash, SystemUtil.getCharset()).equals(hashValue)) {
					IOUtil.write(libHash, hashValue, SystemUtil.getCharset(), false);
					hasChanged = true;
				}
			}
			catch (IOException e) {
			}
			// change Compile type
			if (hasChanged) {

				try {
					// first we delete the physical classes
					if (config.getClassDirectory().isDirectory()) config.getClassDirectory().remove(true);

					// now we force the pagepools to flush
					flushPageSourcePool(config.getMappings());
					flushPageSourcePool(config.getCustomTagMappings());
					flushPageSourcePool(config.getComponentMappings());
					flushPageSourcePool(config.getFunctionMappings());
					flushPageSourcePool(config.getTagMappings());
				}
				catch (IOException e) {
					e.printStackTrace(config.getErrWriter());
				}
			}
		}
		else {
			((ConfigServerImpl) config).setLibHash(HashUtil.create64BitHashAsString(sb.toString()));
		}

	}

	private static void flushPageSourcePool(Mapping... mappings) {
		for (int i = 0; i < mappings.length; i++) {
			if (mappings[i] instanceof MappingImpl) ((MappingImpl) mappings[i]).flush(); // FUTURE make "flush" part of the interface
		}
	}

	private static void flushPageSourcePool(Collection<Mapping> mappings) {
		Iterator<Mapping> it = mappings.iterator();
		Mapping m;
		while (it.hasNext()) {
			m = it.next();
			if (m instanceof MappingImpl) ((MappingImpl) m).flush(); // FUTURE make "flush" part of the interface
		}
	}

	private static void _getDotNotationUpperCase(StringBuilder sb, Mapping... mappings) {
		for (int i = 0; i < mappings.length; i++) {
			sb.append(((MappingImpl) mappings[i]).getDotNotationUpperCase()).append(';');
		}
	}

	private static void _getDotNotationUpperCase(StringBuilder sb, Collection<Mapping> mappings) {
		Iterator<Mapping> it = mappings.iterator();
		Mapping m;
		while (it.hasNext()) {
			m = it.next();
			sb.append(((MappingImpl) m).getDotNotationUpperCase()).append(';');
		}
	}

	/**
	 * load mappings from XML Document
	 * 
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 * @throws IOException
	 */
	public static Mapping[] loadMappings(ConfigImpl config, Struct root, Log log) {
		Map<String, Mapping> mappings = MapFactory.<String, Mapping>getConcurrentMap();
		try {
			Struct _mappings = Caster.toStruct(root.get("mappings", null), null);
			if (_mappings == null) _mappings = Caster.toStruct(root.get("CFMappings", null), null);
			if (_mappings == null) _mappings = ConfigWebUtil.getAsStruct("mappings", root);
			else {
				root.setEL("mappings", _mappings);
			}

			// alias CFMappings

			Mapping tmp;

			boolean finished = true;
			boolean hasServerContext = false;
			boolean hasWebContext = false;
			if (_mappings != null) {
				Iterator<Entry<Key, Object>> it = _mappings.entryIterator();
				Entry<Key, Object> e;
				Struct el;
				while (it.hasNext()) {
					try {
						e = it.next();
						el = Caster.toStruct(e.getValue(), null);
						if (el == null) continue;

						String virtual = e.getKey().getString();
						String physical = getAttr(el, "physical");
						String archive = getAttr(el, "archive");
						String strListType = getAttr(el, "listenerType");
						if (StringUtil.isEmpty(strListType)) strListType = getAttr(el, "listener-type");
						if (StringUtil.isEmpty(strListType)) strListType = getAttr(el, "listenertype");

						String strListMode = getAttr(el, "listenerMode");
						if (StringUtil.isEmpty(strListMode)) strListMode = getAttr(el, "listener-mode");
						if (StringUtil.isEmpty(strListMode)) strListMode = getAttr(el, "listenermode");

						boolean readonly = toBoolean(getAttr(el, "readonly"), false);
						boolean hidden = toBoolean(getAttr(el, "hidden"), false);
						boolean toplevel = toBoolean(getAttr(el, "toplevel"), true);

						{
							if ("/lucee-server/".equalsIgnoreCase(virtual) || "/lucee-server-context/".equalsIgnoreCase(virtual)) {
								hasServerContext = true;
							}
							else if ("/lucee/".equalsIgnoreCase(virtual)) {
								hasWebContext = true;
							}
						}

						// lucee
						if ("/lucee/".equalsIgnoreCase(virtual)) {
							if (StringUtil.isEmpty(strListType, true)) strListType = "modern";
							if (StringUtil.isEmpty(strListMode, true)) strListMode = "curr2root";
							toplevel = true;
						}

						int listenerMode = ConfigWebUtil.toListenerMode(strListMode, -1);
						int listenerType = ConfigWebUtil.toListenerType(strListType, -1);
						ApplicationListener listener = ConfigWebUtil.loadListener(listenerType, null);
						if (listener != null || listenerMode != -1) {
							// type
							if (listener == null) listener = ConfigWebUtil.loadListener(ConfigWebUtil.toListenerType(config.getApplicationListener().getType(), -1), null);
							if (listener == null) listener = new ModernAppListener();

							// mode
							if (listenerMode == -1) {
								listenerMode = config.getApplicationListener().getMode();
							}
							listener.setMode(listenerMode);

						}

						// physical!=null &&
						if ((physical != null || archive != null)) {

							short insTemp = inspectTemplate(el);

							int insTempSlow = Caster.toIntValue(getAttr(el, "inspectTemplateIntervalSlow"), ConfigPro.INSPECT_INTERVAL_UNDEFINED);
							int insTempFast = Caster.toIntValue(getAttr(el, "inspectTemplateIntervalFast"), ConfigPro.INSPECT_INTERVAL_UNDEFINED);

							if ("/lucee/".equalsIgnoreCase(virtual) || "/lucee".equalsIgnoreCase(virtual) || "/lucee-server/".equalsIgnoreCase(virtual)
									|| "/lucee-server-context".equalsIgnoreCase(virtual))
								insTemp = ConfigPro.INSPECT_AUTO;

							String primary = getAttr(el, "primary");
							boolean physicalFirst = primary == null || !"archive".equalsIgnoreCase(primary);

							tmp = new MappingImpl(config, virtual, physical, archive, insTemp, insTempSlow, insTempFast, physicalFirst, hidden, readonly, toplevel, false, false,
									listener, listenerMode, listenerType);
							mappings.put(tmp.getVirtualLowerCase(), tmp);
							if (virtual.equals("/")) {
								finished = true;
								// break;
							}
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}

			// set default lucee-server-context
			{
				if (!hasServerContext) {
					ApplicationListener listener = ConfigWebUtil.loadListener(ApplicationListener.TYPE_MODERN, null);
					listener.setMode(ApplicationListener.MODE_CURRENT2ROOT);

					tmp = new MappingImpl(config, "/lucee-server", "{lucee-server}/context/", null, ConfigPro.INSPECT_AUTO, ConfigPro.INSPECT_INTERVAL_UNDEFINED,
							ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, false, true, true, false, false, listener, ApplicationListener.MODE_CURRENT2ROOT,
							ApplicationListener.TYPE_MODERN);
					mappings.put(tmp.getVirtualLowerCase(), tmp);
				}
				if (!hasWebContext) {
					ApplicationListener listener = ConfigWebUtil.loadListener(ApplicationListener.TYPE_MODERN, null);
					listener.setMode(ApplicationListener.MODE_CURRENT2ROOT);

					tmp = new MappingImpl(config, "/lucee", "{lucee-config}/context/", "{lucee-config}/context/lucee-context.lar", ConfigPro.INSPECT_AUTO,
							ConfigPro.INSPECT_INTERVAL_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, false, true, true, false, false, listener,
							ApplicationListener.MODE_CURRENT2ROOT, ApplicationListener.TYPE_MODERN);
					mappings.put(tmp.getVirtualLowerCase(), tmp);
				}
			}

			if (!finished) {
				tmp = new MappingImpl(config, "/", "/", null, ConfigPro.INSPECT_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, true,
						true, true, false, false, null, -1, -1);
				mappings.put("/", tmp);
			}
			// config.setMappings((Mapping[]) mappings.toArray(new
			// Mapping[mappings.size()]));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return mappings.values().toArray(new Mapping[mappings.size()]);
	}

	private static short inspectTemplate(Struct data) {
		String strInsTemp = getAttr(data, "inspectTemplate");
		if (StringUtil.isEmpty(strInsTemp)) strInsTemp = getAttr(data, "inspect");
		if (StringUtil.isEmpty(strInsTemp)) {
			Boolean trusted = Caster.toBoolean(getAttr(data, "trusted"), null);
			if (trusted != null) {
				if (trusted.booleanValue()) return ConfigPro.INSPECT_AUTO;
				return ConfigPro.INSPECT_ALWAYS;
			}
			return ConfigPro.INSPECT_UNDEFINED;
		}
		if (StringUtil.isEmpty(strInsTemp)) {
			strInsTemp = SystemUtil.getSystemPropOrEnvVar("lucee.inspect.template", null);
		}

		return ConfigWebUtil.inspectTemplate(strInsTemp, ConfigPro.INSPECT_UNDEFINED);
	}

	public static lucee.runtime.rest.Mapping[] loadRestMappings(ConfigImpl config, Struct root, Log log) {
		Map<String, lucee.runtime.rest.Mapping> mappings = new HashMap<String, lucee.runtime.rest.Mapping>();
		try {
			boolean hasAccess = true;// MUST
			// ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_REST);
			Struct el = ConfigWebUtil.getAsStruct("rest", root);

			Array _mappings = ConfigWebUtil.getAsArray("mapping", el);

			// first get mapping defined in server admin (read-only)
			boolean hasDefault = false;
			lucee.runtime.rest.Mapping tmp;

			// get current mappings
			if (hasAccess && _mappings != null) {
				Iterator<?> it = _mappings.getIterator();
				while (it.hasNext()) {
					try {
						el = Caster.toStruct(it.next());
						if (el == null) continue;

						String physical = getAttr(el, "physical");
						String virtual = getAttr(el, "virtual");
						boolean readonly = toBoolean(getAttr(el, "readonly"), false);
						boolean hidden = toBoolean(getAttr(el, "hidden"), false);
						boolean _default = toBoolean(getAttr(el, "default"), false);
						if (physical != null) {
							tmp = new lucee.runtime.rest.Mapping(config, virtual, physical, hidden, readonly, _default);
							if (_default) hasDefault = true;
							mappings.put(tmp.getVirtual(), tmp);
						}

					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}

			// set default if not exist
			if (!hasDefault) {
				Resource rest = config.getConfigDir().getRealResource("rest");
				rest.mkdirs();
				tmp = new lucee.runtime.rest.Mapping(config, "/default-set-by-lucee", rest.getAbsolutePath(), true, true, true);
				mappings.put(tmp.getVirtual(), tmp);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		lucee.runtime.rest.Mapping[] arr = mappings.values().toArray(new lucee.runtime.rest.Mapping[mappings.size()]);

		// make sure only one is default
		boolean hasDefault = false;
		lucee.runtime.rest.Mapping m;
		for (int i = 0; i < arr.length; i++) {
			m = arr[i];
			if (m.isDefault()) {
				if (hasDefault) m.setDefault(false);
				hasDefault = true;
			}
		}
		return arr;
	}

	public static Map<String, LoggerAndSourceData> loadLoggers(ConfigImpl config, Struct root) {
		config.clearLoggers(Boolean.FALSE);
		Set<String> existing = new HashSet<>();

		Map<String, LoggerAndSourceData> loggerMap = new HashMap<String, LoggerAndSourceData>();
		try {
			// loggers
			Struct loggers = ConfigWebUtil.getAsStruct("loggers", root);
			String name, tmp;
			Map<String, String> appenderArgs, layoutArgs;
			ClassDefinition cdAppender, cdLayout;
			int level = Log.LEVEL_ERROR;
			boolean readOnly = false;
			Iterator<Entry<Key, Object>> itt = loggers.entryIterator();
			Entry<Key, Object> entry;
			Struct child;
			while (itt.hasNext()) {
				try {
					entry = itt.next();
					child = Caster.toStruct(entry.getValue(), null);
					if (child == null) continue;

					name = entry.getKey().getString();

					// appender
					cdAppender = getClassDefinition(child, "appender", config.getIdentification());
					if (!cdAppender.hasClass()) {
						tmp = StringUtil.trim(getAttr(child, "appender"), "");
						cdAppender = config.getLogEngine().appenderClassDefintion(tmp);
					}
					else if (!cdAppender.isBundle()) {
						cdAppender = config.getLogEngine().appenderClassDefintion(cdAppender.getClassName());
					}
					appenderArgs = toArguments(child, "appenderArguments", true, false);

					// layout
					cdLayout = getClassDefinition(child, "layout", config.getIdentification());
					if (!cdLayout.hasClass()) {
						tmp = StringUtil.trim(getAttr(child, "layout"), "");
						cdLayout = config.getLogEngine().layoutClassDefintion(tmp);
					}
					else if (!cdLayout.isBundle()) {
						cdLayout = config.getLogEngine().layoutClassDefintion(cdLayout.getClassName());
					}
					layoutArgs = toArguments(child, "layoutArguments", true, false);

					String strLevel = getAttr(child, "level");
					if (StringUtil.isEmpty(strLevel, true)) strLevel = getAttr(child, "logLevel");
					level = LogUtil.toLevel(StringUtil.trim(strLevel, ""), Log.LEVEL_ERROR);
					readOnly = Caster.toBooleanValue(getAttr(child, "readOnly"), false);
					// ignore when no appender/name is defined
					if (cdAppender.hasClass() && !StringUtil.isEmpty(name)) {
						existing.add(name.toLowerCase());
						if (cdLayout.hasClass()) {
							addLogger(config, loggerMap, name, level, cdAppender, appenderArgs, cdLayout, layoutArgs, readOnly, false);
						}
						else addLogger(config, loggerMap, name, level, cdAppender, appenderArgs, null, null, readOnly, false);
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					LogUtil.logGlobal(config, ConfigWebFactory.class.getName(), t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			LogUtil.logGlobal(config, ConfigWebFactory.class.getName(), t);
		}
		return loggerMap;
	}

	public static LoggerAndSourceData addLogger(Config config, Map<String, LoggerAndSourceData> loggers, String name, int level, ClassDefinition appender,
			Map<String, String> appenderArgs, ClassDefinition layout, Map<String, String> layoutArgs, boolean readOnly, boolean dyn) throws PageException {
		LoggerAndSourceData existing = loggers.get(name.toLowerCase());
		String id = LoggerAndSourceData.id(name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly);

		if (existing != null) {
			if (existing.id().equals(id)) {
				return existing;
			}
			existing.close();
		}

		LoggerAndSourceData las = new LoggerAndSourceData(config, id, name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly, dyn);
		loggers.put(name.toLowerCase(), las);
		return las;
	}

	public static ExecutionLogFactory loadExeLog(ConfigImpl config, Struct root, Log log) {
		try {
			Struct el = ConfigWebUtil.getAsStruct("executionLog", root);
			boolean hasChanged = false;
			String val = Caster.toString(config.getExecutionLogEnabled());
			try {
				Resource contextDir = config.getConfigDir();
				Resource exeLog = contextDir.getRealResource("exeLog");

				if (!exeLog.exists()) {
					exeLog.createNewFile();
					IOUtil.write(exeLog, val, SystemUtil.getCharset(), false);
					hasChanged = true;
				}
				else if (!IOUtil.toString(exeLog, SystemUtil.getCharset()).equals(val)) {
					IOUtil.write(exeLog, val, SystemUtil.getCharset(), false);
					hasChanged = true;
				}
			}
			catch (IOException e) {
				log(config, log, e);
			}

			if (hasChanged) {
				try {
					if (config.getClassDirectory().exists()) config.getClassDirectory().remove(true);
				}
				catch (IOException e) {
					log(config, log, e);
				}
			}

			// class
			String strClass = getAttr(el, "class");
			Class clazz;
			if (!StringUtil.isEmpty(strClass)) {
				try {
					if ("console".equalsIgnoreCase(strClass)) clazz = ConsoleExecutionLog.class;
					else if ("debug".equalsIgnoreCase(strClass)) clazz = DebugExecutionLog.class;
					else {
						ClassDefinition cd = el != null ? getClassDefinition(el, "", config.getIdentification()) : null;

						Class c = cd != null ? cd.getClazz() : null;
						if (c != null && (ClassUtil.newInstance(c) instanceof ExecutionLog)) {
							clazz = c;
						}
						else {
							clazz = ConsoleExecutionLog.class;
							LogUtil.logGlobal(config, Log.LEVEL_ERROR, ConfigWebFactory.class.getName(),
									"class [" + strClass + "] must implement the interface " + ExecutionLog.class.getName());
						}
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), ConfigWebFactory.class.getName(), t);
					clazz = ConsoleExecutionLog.class;
				}
				if (clazz != null)
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigWebFactory.class.getName(), "loaded ExecutionLog class " + clazz.getName());

				// arguments
				Map<String, String> args = toArguments(el, "arguments", true, false);
				if (args == null) args = toArguments(el, "classArguments", true, false);

				return new ExecutionLogFactory(clazz, args);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return new ExecutionLogFactory(ConsoleExecutionLog.class, new HashMap<String, String>());
	}

	/**
	 * loads datasource settings from XMl DOM
	 * 
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 * @throws BundleException
	 * @throws ClassNotFoundException
	 */
	public static Map<String, DataSource> loadDataSources(ConfigImpl config, Struct root, Log log) {
		Map<String, DataSource> datasources = new HashMap<String, DataSource>();
		try {

			// When set to true, makes JDBC use a representation for DATE data that
			// is compatible with the Oracle8i database.
			System.setProperty("oracle.jdbc.V8Compatible", "true");

			// Default query of query DB
			try {
				setDatasource(config, datasources, QOQ_DATASOURCE_NAME,
						new ClassDefinitionImpl("org.hsqldb.jdbcDriver", "org.lucee.hsqldb", "2.7.2.jdk8", config.getIdentification()), "hypersonic-hsqldb", "", -1,
						"jdbc:hsqldb:mem:tempQoQ;sql.regular_names=false;sql.enforce_strict_size=false;sql.enforce_types=false;", "sa", "", null, DEFAULT_MAX_CONNECTION, -1, -1,
						60000, 0, 0, 0, true, true, DataSource.ALLOW_ALL, false, false, null, new StructImpl(), "", ParamSyntax.DEFAULT, false, false, false, false);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log.error("Datasource", t);
			}

			SecurityManager sm = config.getSecurityManager();
			short access = sm.getAccess(SecurityManager.TYPE_DATASOURCE);
			int accessCount = -1;
			if (access == SecurityManager.VALUE_YES) accessCount = -1;
			else if (access == SecurityManager.VALUE_NO) accessCount = 0;
			else if (access >= SecurityManager.VALUE_1 && access <= SecurityManager.VALUE_10) {
				accessCount = access - SecurityManager.NUMBER_OFFSET;
			}

			// Databases
			// Struct parent = ConfigWebUtil.getAsStruct("dataSources", root);

			// Data Sources
			Struct dataSources = ConfigWebUtil.getAsStruct(root, false, "dataSources");
			if (accessCount == -1) accessCount = dataSources.size();
			if (dataSources.size() < accessCount) accessCount = dataSources.size();

			// if(hasAccess) {
			JDBCDriver jdbc;
			ClassDefinition cd;
			String id;
			Iterator<Entry<Key, Object>> it = dataSources.entryIterator();
			Entry<Key, Object> e;
			Struct dataSource;
			while (it.hasNext()) {
				e = it.next();
				dataSource = Caster.toStruct(e.getValue(), null);
				if (dataSource == null) continue;

				if (dataSource.containsKey(KeyConstants._database)) {
					try {
						// do we have an id?
						jdbc = config.getJDBCDriverById(getAttr(dataSource, "id"), null);
						if (jdbc != null && jdbc.cd != null) {
							cd = jdbc.cd;
						}
						else {
							cd = getClassDefinition(dataSource, "", config.getIdentification());
						}

						// we have no class
						if (!cd.hasClass()) {
							jdbc = config.getJDBCDriverById(getAttr(dataSource, "type"), null);
							if (jdbc != null && jdbc.cd != null) {
								cd = jdbc.cd;
							}
						}
						// we only have a class
						else if (!cd.isBundle()) {
							jdbc = config.getJDBCDriverByClassName(cd.getClassName(), null);
							if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) cd = jdbc.cd;
						}

						// still no bundle!
						if (!cd.isBundle()) cd = patchJDBCClass(config, cd);
						int idle = Caster.toIntValue(getAttr(dataSource, "idleTimeout"), -1);
						if (idle == -1) idle = Caster.toIntValue(getAttr(dataSource, "connectionTimeout"), -1);
						int defLive = 15;
						if (idle > 0) defLive = idle * 5;// for backward compatibility

						String dsn = getAttr(dataSource, "connectionString");
						if (StringUtil.isEmpty(dsn, true)) dsn = getAttr(dataSource, "dsn");
						if (StringUtil.isEmpty(dsn, true)) dsn = getAttr(dataSource, "connStr");
						if (StringUtil.isEmpty(dsn, true)) dsn = getAttr(dataSource, "url");
						if (StringUtil.isEmpty(dsn, true)) {
							if (jdbc == null && cd.hasClass()) {
								jdbc = config.getJDBCDriverByClassName(cd.getClassName(), null);
							}
							if (jdbc != null) {
								dsn = jdbc.connStr;
							}

						}
						setDatasource(config, datasources, e.getKey().getString(), cd, getAttr(dataSource, "host"), getAttr(dataSource, "database"),
								Caster.toIntValue(getAttr(dataSource, "port"), -1), dsn, getAttr(dataSource, "username"), ConfigWebUtil.decrypt(getAttr(dataSource, "password")),
								null, Caster.toIntValue(getAttr(dataSource, "connectionLimit"), DEFAULT_MAX_CONNECTION), idle,
								Caster.toIntValue(getAttr(dataSource, "liveTimeout"), defLive), Caster.toIntValue(getAttr(dataSource, "minIdle"), 0),
								Caster.toIntValue(getAttr(dataSource, "maxIdle"), 0), Caster.toIntValue(getAttr(dataSource, "maxTotal"), 0),
								Caster.toLongValue(getAttr(dataSource, "metaCacheTimeout"), 60000), toBoolean(getAttr(dataSource, "blob"), true),
								toBoolean(getAttr(dataSource, "clob"), true), Caster.toIntValue(getAttr(dataSource, "allow"), DataSource.ALLOW_ALL),
								toBoolean(getAttr(dataSource, "validate"), false), toBoolean(getAttr(dataSource, "storage"), false), getAttr(dataSource, "timezone"),
								ConfigWebUtil.getAsStruct(dataSource, true, "custom"), getAttr(dataSource, "dbdriver"), ParamSyntax.toParamSyntax(dataSource, ParamSyntax.DEFAULT),
								toBoolean(getAttr(dataSource, "literalTimestampWithTSOffset"), false), toBoolean(getAttr(dataSource, "alwaysSetTimeout"), false),
								toBoolean(getAttr(dataSource, "requestExclusive"), false), toBoolean(getAttr(dataSource, "alwaysResetConnections"), false)

						);
					}
					catch (Throwable th) {
						ExceptionUtil.rethrowIfNecessary(th);
						log.error("Datasource", th);
					}
				}
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return datasources;
	}

	private static ClassDefinition patchJDBCClass(ConfigImpl config, ClassDefinition cd) {
		// PATCH for MySQL driver that did change the className within the same extension, JDBC extension
		// expect that the className does not change.
		if ("org.gjt.mm.mysql.Driver".equals(cd.getClassName()) || "com.mysql.jdbc.Driver".equals(cd.getClassName()) || "com.mysql.cj.jdbc.Driver".equals(cd.getClassName())) {
			JDBCDriver jdbc = config.getJDBCDriverById("mysql", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("com.mysql.cj.jdbc.Driver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("com.mysql.jdbc.Driver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("org.gjt.mm.mysql.Driver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			ClassDefinitionImpl tmp = new ClassDefinitionImpl("com.mysql.cj.jdbc.Driver", "com.mysql.cj", null, config.getIdentification());
			if (tmp.getClazz(null) != null) return tmp;

			tmp = new ClassDefinitionImpl("com.mysql.jdbc.Driver", "com.mysql.jdbc", null, config.getIdentification());
			if (tmp.getClazz(null) != null) return tmp;
		}
		if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cd.getClassName())) {
			JDBCDriver jdbc = config.getJDBCDriverById("mssql", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			ClassDefinitionImpl tmp = new ClassDefinitionImpl("com.microsoft.sqlserver.jdbc.SQLServerDriver", cd.getName(), cd.getVersionAsString(), config.getIdentification());
			if (tmp.getClazz(null) != null) return tmp;
		}

		return cd;
	}

	public static JDBCDriver[] loadJDBCDrivers(ConfigImpl config, Struct root, Log log) {
		Map<String, JDBCDriver> map = new HashMap<String, JDBCDriver>();
		try {
			// first add the server drivers, so they can be overwritten

			// jdbcDrivers
			Struct jdbcDrivers = ConfigWebUtil.getAsStruct("jdbcDrivers", root);
			Iterator<Entry<Key, Object>> it = jdbcDrivers.entryIterator();
			Entry<Key, Object> e;
			ClassDefinition cd;
			String label, id, connStr;
			while (it.hasNext()) {
				try {
					e = it.next();
					Struct driver = Caster.toStruct(e.getValue(), null);
					if (driver == null) continue;

					// class definition
					driver.setEL(KeyConstants._class, e.getKey().getString());
					cd = getClassDefinition(driver, "", config.getIdentification());
					if (StringUtil.isEmpty(cd.getClassName()) && !StringUtil.isEmpty(cd.getName())) {
						try {
							Bundle bundle = OSGiUtil.loadBundle(cd.getName(), cd.getVersion(), config.getIdentification(), null, false);
							String cn = JDBCDriver.extractClassName(bundle);
							cd = new ClassDefinitionImpl(cn, cd.getName(), cd.getVersionAsString(), config.getIdentification());
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
						}
					}

					label = getAttr(driver, "label");
					id = getAttr(driver, "id");
					connStr = getAttr(driver, "connectionString");
					// check if label exists
					if (StringUtil.isEmpty(label)) {
						if (log != null) log.error("Datasource", "missing label for jdbc driver [" + cd.getClassName() + "]");
						continue;
					}

					// check if it is a bundle
					if (!cd.isBundle()) {
						if (log != null) log.error("Datasource", "jdbc driver [" + label + "] does not describe a bundle");
						continue;
					}
					map.put(cd.toString(), new JDBCDriver(label, id, connStr, cd));
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, log, t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return map.values().toArray(new JDBCDriver[map.size()]);
	}

	public static Map<String, ClassDefinition> loadCacheDefintions(ConfigImpl config, Struct root, Log log) {
		Map<String, ClassDefinition> map = new HashMap<String, ClassDefinition>();
		try {

			// first add the server drivers, so they can be overwritten
			ClassDefinition cd;

			Array caches = ConfigWebUtil.getAsArray("cacheClasses", root);
			if (caches != null) {
				Iterator<?> it = caches.getIterator();
				Struct cache;
				while (it.hasNext()) {
					try {
						cache = Caster.toStruct(it.next());
						if (cache == null) continue;
						cd = getClassDefinition(cache, "", config.getIdentification());

						// check if it is a bundle
						if (!cd.isBundle()) {
							log.error("Cache", "[" + cd + "] does not have bundle info");
							continue;
						}
						map.put(cd.getClassName(), cd);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return map;
	}

	public static Map<Integer, String> loadCacheDefaultConnectionNames(ConfigImpl config, Struct root, Log log) {
		Map<Integer, String> names = new HashMap<>();
		try {
			Struct defaultCache = ConfigWebUtil.getAsStruct("cache", root);

			// default cache
			for (int i = 0; i < ConfigPro.CACHE_TYPES_MAX.length; i++) {
				try {
					String def = getAttr(defaultCache, "default" + StringUtil.ucFirst(ConfigPro.STRING_CACHE_TYPES_MAX[i]));
					if (StringUtil.isEmpty(def, true)) def = getAttr(root, "cacheDefault" + StringUtil.ucFirst(ConfigPro.STRING_CACHE_TYPES_MAX[i]));

					if (!StringUtil.isEmpty(def, true)) {
						names.put(ConfigPro.CACHE_TYPES_MAX[i], def.trim());
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, log, t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return names;
	}

	public static Map<String, CacheConnection> loadCacheCacheConnections(ConfigImpl config, Struct root, Log log) {
		Map<String, CacheConnection> caches = new HashMap<String, CacheConnection>();
		try {

			// cache connections
			Struct conns = ConfigWebUtil.getAsStruct("caches", root);

			// if(hasAccess) {
			ClassDefinition cd;
			Key name;
			CacheConnection cc;
			// Class cacheClazz;
			// caches
			{
				Iterator<Entry<Key, Object>> it = conns.entryIterator();
				Entry<Key, Object> entry;
				Struct data;
				while (it.hasNext()) {
					try {
						entry = it.next();
						name = entry.getKey();
						data = Caster.toStruct(entry.getValue(), null);
						cd = getClassDefinition(data, "", config.getIdentification());
						if (!cd.isBundle()) {
							ClassDefinition _cd = config.getCacheDefinition(cd.getClassName());
							if (_cd != null) cd = _cd;
						}

						{
							Struct custom = ConfigWebUtil.getAsStruct(data, true, "custom");
							// Workaround for old EHCache class definitions
							if (cd.getClassName() != null && cd.getClassName().endsWith(".EHCacheLite")) {
								cd = new ClassDefinitionImpl("org.lucee.extension.cache.eh.EHCache");
								if (!custom.containsKey("distributed")) custom.setEL("distributed", "off");
								if (!custom.containsKey("asynchronousReplicationIntervalMillis")) custom.setEL("asynchronousReplicationIntervalMillis", "1000");
								if (!custom.containsKey("maximumChunkSizeBytes")) custom.setEL("maximumChunkSizeBytes", "5000000");

							} //
							else if (cd.getClassName() != null
									&& (cd.getClassName().endsWith(".extension.io.cache.eh.EHCache") || cd.getClassName().endsWith("lucee.runtime.cache.eh.EHCache"))) {
										cd = new ClassDefinitionImpl("org.lucee.extension.cache.eh.EHCache");
									}
							cc = new CacheConnectionImpl(config, name.getString(), cd, custom, Caster.toBooleanValue(getAttr(data, "readOnly"), false),
									Caster.toBooleanValue(getAttr(data, "storage"), false));
							if (!StringUtil.isEmpty(name)) {
								caches.put(name.getLowerString(), cc);
							}
							else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_ERROR, ConfigWebFactory.class.getName(), "missing cache name");

						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}

			// call static init once per driver
			{
				// group by classes
				final Map<ClassDefinition, List<CacheConnection>> _caches = new HashMap<ClassDefinition, List<CacheConnection>>();
				{
					Iterator<Entry<String, CacheConnection>> it = caches.entrySet().iterator();
					Entry<String, CacheConnection> entry;
					List<CacheConnection> list;
					while (it.hasNext()) {
						try {
							entry = it.next();
							cc = entry.getValue();
							if (cc == null) continue;// Jira 3196 ?!
							list = _caches.get(cc.getClassDefinition());
							if (list == null) {
								list = new ArrayList<CacheConnection>();
								_caches.put(cc.getClassDefinition(), list);
							}
							list.add(cc);
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
							log(config, log, t);
						}
					}
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return caches;
	}

	private static String getMD5(Struct data, String cacheDef, String parentMD5) {
		try {
			return MD5.getDigestAsString(new StringBuilder().append(data.toString()).append(':').append(cacheDef).append(':').append(parentMD5).toString());
		}
		catch (IOException e) {
			return "";
		}
	}

	public static GatewayMap loadGatewayEL(ConfigImpl config, Struct root, Log log) {
		try {
			return loadGateway(config, root, log);
		}
		catch (Exception e) {
			log(config, log, e);
			return new GatewayMap();
		}
	}

	public static GatewayMap loadGateway(final ConfigImpl config, Struct root, Log log) {
		GatewayMap mapGateways = new GatewayMap();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_GATEWAY);
		GatewayEntry ge;
		// cache connections
		Struct gateways = ConfigWebUtil.getAsStruct("gateways", root);

		String id;
		// caches
		if (hasAccess) {
			try {
				Iterator<Entry<Key, Object>> it = gateways.entryIterator();
				Entry<Key, Object> e;
				Struct eConnection;
				while (it.hasNext()) {
					try {
						e = it.next();
						eConnection = Caster.toStruct(e.getValue(), null);
						if (eConnection == null) continue;
						id = e.getKey().getLowerString();

						ge = new GatewayEntryImpl(id, getClassDefinition(eConnection, "", config.getIdentification()), getAttr(eConnection, "cfcPath"),
								getAttr(eConnection, "listenerCFCPath"), getAttr(eConnection, "startupMode"), ConfigWebUtil.getAsStruct(eConnection, true, "custom"),
								Caster.toBooleanValue(getAttr(eConnection, "readOnly"), false));

						if (!StringUtil.isEmpty(id)) {
							mapGateways.put(id.toLowerCase(), ge);
						}
						else {
							LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_ERROR, ConfigWebFactory.class.getName(), "missing id");

						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log(config, log, t);
			}
		}
		return mapGateways;
	}

	private static void setDatasource(ConfigImpl config, Map<String, DataSource> datasources, String datasourceName, ClassDefinition cd, String server, String databasename,
			int port, String dsn, String user, String pass, TagListener listener, int connectionLimit, int idleTimeout, int liveTimeout, int minIdle, int maxIdle, int maxTotal,
			long metaCacheTimeout, boolean blob, boolean clob, int allow, boolean validate, boolean storage, String timezone, Struct custom, String dbdriver, ParamSyntax ps,
			boolean literalTimestampWithTSOffset, boolean alwaysSetTimeout, boolean requestExclusive, boolean alwaysResetConnections)
			throws BundleException, ClassException, SQLException {

		datasources.put(datasourceName.toLowerCase(),
				new DataSourceImpl(config, datasourceName, cd, server, dsn, databasename, port, user, pass, listener, connectionLimit, idleTimeout, liveTimeout, minIdle, maxIdle,
						maxTotal, metaCacheTimeout, blob, clob, allow, custom, false, validate, storage,
						StringUtil.isEmpty(timezone, true) ? null : TimeZoneUtil.toTimeZone(timezone, null), dbdriver, ps, literalTimestampWithTSOffset, alwaysSetTimeout,
						requestExclusive, alwaysResetConnections, ThreadLocalPageContext.getLog(config, "application")));

	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 * @throws IOException
	 */
	public static Mapping[] loadCustomTagsMappings(ConfigImpl config, Struct root, Log log) {
		Mapping[] mappings = null;
		try {
			// Struct customTag = ConfigWebUtil.getAsStruct("customTag", root);
			Array ctMappings = ConfigWebUtil.getAsArray("customTagMappings", root);
			boolean hasDefault = false;

			// Web Mapping
			if (ctMappings.size() > 0) {
				Iterator<Object> it = ctMappings.valueIterator();
				List<Mapping> list = new ArrayList<>();
				Struct ctMapping;
				while (it.hasNext()) {
					try {
						ctMapping = Caster.toStruct(it.next(), null);
						if (ctMapping == null) continue;

						String virtual = createVirtual(ctMapping);
						String physical = getAttr(ctMapping, "physical");
						String archive = getAttr(ctMapping, "archive");
						boolean readonly = toBoolean(getAttr(ctMapping, "readonly"), false);
						boolean hidden = toBoolean(getAttr(ctMapping, "hidden"), false);
						if ("{lucee-web}/customtags/".equals(physical) || "{lucee-server}/customtags/".equals(physical)) continue;
						if ("{lucee-config}/customtags/".equals(physical)) hasDefault = true;
						short inspTemp = inspectTemplate(ctMapping);
						int insTempSlow = Caster.toIntValue(getAttr(ctMapping, "inspectTemplateIntervalSlow"), -1);
						int insTempFast = Caster.toIntValue(getAttr(ctMapping, "inspectTemplateIntervalFast"), -1);

						String primary = getAttr(ctMapping, "primary");

						boolean physicalFirst = StringUtil.isEmpty(archive, true) || !"archive".equalsIgnoreCase(primary);
						list.add(new MappingImpl(config, virtual, physical, archive, inspTemp, insTempSlow, insTempFast, physicalFirst, hidden, readonly, true, false, true, null,
								-1, -1));
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
				if (!hasDefault) {
					list.add(new MappingImpl(config, "/default", "{lucee-config}/customtags/", null, ConfigPro.INSPECT_NEVER, -1, -1, true, false, true, true, false, true, null,
							-1, -1));
				}
				mappings = list.toArray(new Mapping[list.size()]);
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		if (mappings == null) {
			mappings = new Mapping[] { new MappingImpl(config, "/default-customtags", "{lucee-config}/customtags/", null, ConfigPro.INSPECT_UNDEFINED,
					ConfigPro.INSPECT_INTERVAL_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, true, true, true, false, true, null, -1, -1) };
		}
		return mappings;
	}

	private static Object toKey(Mapping m) {
		if (!StringUtil.isEmpty(m.getStrPhysical(), true)) return m.getVirtual() + ":" + m.getStrPhysical().toLowerCase().trim();
		return (m.getVirtual() + ":" + m.getStrPhysical() + ":" + m.getStrArchive()).toLowerCase();
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws ExpressionException
	 * @throws TagLibException
	 * @throws FunctionLibException
	 */
	private static void _loadFilesystem(ConfigServerImpl config, Struct root, boolean doNew, Log log) {
		try {
			Resource configDir = config.getConfigDir();

			String strDefaultFLDDirectory = null;
			String strDefaultTLDDirectory = null;
			String strDefaultFuncDirectory = null;
			String strDefaultTagDirectory = null;
			String strFuncDirectory = null;
			String strTagDirectory = null;

			// only read in server context
			strDefaultFLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.fld", null);
			strDefaultTLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.tld", null);
			strDefaultFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.function", null);
			strDefaultTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.tag", null);
			if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.fld", null);
			if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.tld", null);
			if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.function", null);
			if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.tag", null);
			strFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.additional.function", null);
			strTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.additional.tag", null);

			Struct fileSystem = ConfigWebUtil.getAsStruct("fileSystem", root);

			// get library directories
			if (fileSystem != null) {
				if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "tldDirectory"));
				if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "flddirectory"));
				if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "tagDirectory"));
				if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "functionDirectory"));
				if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "tldDefaultDirectory"));
				if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "fldDefaultDirectory"));
				if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "tagDefaultDirectory"));
				if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "functionDefaultDirectory"));
				if (StringUtil.isEmpty(strTagDirectory)) strTagDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "tagAddionalDirectory"));
				if (StringUtil.isEmpty(strFuncDirectory)) strFuncDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "functionAddionalDirectory"));
			}

			// set default directories if necessary
			if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = "{lucee-config}/library/fld/";
			if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = "{lucee-config}/library/tld/";
			if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = "{lucee-config}/library/function/";
			if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = "{lucee-config}/library/tag/";

			// init TLDS
			{
				ConfigServerImpl cs = config;
				config.setTLDs(ConfigWebUtil.duplicate(new TagLib[] { cs.coreTLDs }, false));
			}

			// TLD Dir
			if (!StringUtil.isEmpty(strDefaultTLDDirectory)) {
				Resource tld = ConfigWebUtil.getFile(config, configDir, strDefaultTLDDirectory, FileUtil.TYPE_DIR);
				if (tld != null) config.setTldFile(tld);
			}

			// Tag Directory
			List<Path> listTags = new ArrayList<Path>();
			if (!StringUtil.isEmpty(strDefaultTagDirectory)) {
				Resource dir = ConfigWebUtil.getFile(config, configDir, strDefaultTagDirectory, FileUtil.TYPE_DIR);
				createTagFiles(config, configDir, dir, doNew);
				listTags.add(new Path(strDefaultTagDirectory, dir));
			}
			// addional tags
			Map<String, String> mapTags = new LinkedHashMap<String, String>();
			if (!StringUtil.isEmpty(strTagDirectory) || !mapTags.isEmpty()) {
				String[] arr = ListUtil.listToStringArray(strTagDirectory, ',');
				for (String str: arr) {
					mapTags.put(str, "");
				}
				for (String str: mapTags.keySet()) {
					try {
						str = str.trim();
						if (StringUtil.isEmpty(str)) continue;
						Resource dir = ConfigWebUtil.getFile(config, configDir, str, FileUtil.TYPE_DIR);
						listTags.add(new Path(str, dir));
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}
			config.setTagDirectory(listTags);

			// FUNCTIONS

			// Init flds
			{
				ConfigServerImpl cs = config;
				config.setFLDs(cs.coreFLDs.duplicate(false));

			}

			// FLDs
			if (!StringUtil.isEmpty(strDefaultFLDDirectory)) {
				Resource fld = ConfigWebUtil.getFile(config, configDir, strDefaultFLDDirectory, FileUtil.TYPE_DIR);
				if (fld != null) config.setFldFile(fld);
			}

			// Function files (CFML)
			List<Path> listFuncs = new ArrayList<Path>();
			if (!StringUtil.isEmpty(strDefaultFuncDirectory)) {
				Resource dir = ConfigWebUtil.getFile(config, configDir, strDefaultFuncDirectory, FileUtil.TYPE_DIR);
				createFunctionFiles(config, configDir, dir, doNew);
				listFuncs.add(new Path(strDefaultFuncDirectory, dir));
				// if (dir != null) config.setFunctionDirectory(dir);
			}
			// function additonal
			Map<String, String> mapFunctions = new LinkedHashMap<String, String>();
			if (!StringUtil.isEmpty(strFuncDirectory) || !mapFunctions.isEmpty()) {
				String[] arr = ListUtil.listToStringArray(strFuncDirectory, ',');
				for (String str: arr) {
					mapFunctions.put(str, "");
				}
				for (String str: mapFunctions.keySet()) {
					try {
						str = str.trim();
						if (StringUtil.isEmpty(str)) continue;
						Resource dir = ConfigWebUtil.getFile(config, configDir, str, FileUtil.TYPE_DIR);
						listFuncs.add(new Path(str, dir));
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}
			config.setFunctionDirectory(listFuncs);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
	}

	private static void createTagFiles(Config config, Resource configDir, Resource dir, boolean doNew) {
		if (config instanceof ConfigServer) {

			// Dump
			create("/resource/library/tag/", new String[] { "Dump." + COMPONENT_EXTENSION }, dir, doNew);

			/*
			 * Resource sub = dir.getRealResource("lucee/dump/skins/");
			 * create("/resource/library/tag/lucee/dump/skins/",new String[]{
			 * "text."+CFML_TEMPLATE_MAIN_EXTENSION ,"simple."+CFML_TEMPLATE_MAIN_EXTENSION
			 * ,"modern."+CFML_TEMPLATE_MAIN_EXTENSION ,"classic."+CFML_TEMPLATE_MAIN_EXTENSION
			 * ,"pastel."+CFML_TEMPLATE_MAIN_EXTENSION },sub,doNew);
			 */
			Resource f;
			Resource build = dir.getRealResource("build");
			// /resource/library/tag/build/jquery
			Resource jquery = build.getRealResource("jquery");
			if (!jquery.isDirectory()) jquery.mkdirs();
			String[] names = new String[] { "jquery-1.12.4.min.js" };
			for (int i = 0; i < names.length; i++) {
				try {
					f = jquery.getRealResource(names[i]);
					if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/tag/build/jquery/" + names[i], f);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, null, t);
				}
			}

			// AJAX
			// AjaxFactory.deployTags(dir, doNew);

		}
	}

	private static void createFunctionFiles(Config config, Resource configDir, Resource dir, boolean doNew) {

		if (config instanceof ConfigServer) {
			Resource f = dir.getRealResource("writeDump." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/writeDump." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("dump." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/dump." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("location." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/location." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("threadJoin." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/threadJoin." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("threadTerminate." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/threadTerminate." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("throw." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/throw." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("trace." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/trace." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("queryExecute." + TEMPLATE_EXTENSION);
			// if (!f.exists() || doNew)
			// createFileFromResourceEL("/resource/library/function/queryExecute."+TEMPLATE_EXTENSION, f);
			if (f.exists())// FUTURE add this instead if(updateType=NEW_FRESH || updateType=NEW_FROM4)
				delete(dir, "queryExecute." + TEMPLATE_EXTENSION);

			f = dir.getRealResource("transactionCommit." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/transactionCommit." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("transactionRollback." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/transactionRollback." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("transactionSetsavepoint." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/transactionSetsavepoint." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("writeLog." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/writeLog." + TEMPLATE_EXTENSION, f);

			// AjaxFactory.deployFunctions(dir, doNew);

		}
	}

	private static void copyContextFiles(Resource src, Resource trg) {
		// directory
		if (src.isDirectory()) {
			if (trg.exists()) trg.mkdirs();
			Resource[] children = src.listResources();
			for (int i = 0; i < children.length; i++) {
				copyContextFiles(children[i], trg.getRealResource(children[i].getName()));
			}
		}
		// file
		else if (src.isFile()) {
			if (src.lastModified() > trg.lastModified()) {
				try {
					if (trg.exists()) trg.remove(true);
					trg.createFile(true);
					src.copyTo(trg, false);
				}
				catch (IOException e) {
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigWebFactory.class.getName(), e);
				}
			}

		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 */
	public static URL loadUpdate(ConfigImpl config, Struct root, Log log) {
		try {
			// Server
			if (root != null) {
				ConfigServerImpl cs = (ConfigServerImpl) config;

				String location = getAttr(root, "updateLocation");
				if (!StringUtil.isEmpty(location, true)) {
					location = location.trim();
					if ("http://update.lucee.org".equals(location)) location = DEFAULT_LOCATION;
					if ("http://snapshot.lucee.org".equals(location) || "https://snapshot.lucee.org".equals(location)) location = DEFAULT_LOCATION;
					if ("http://release.lucee.org".equals(location) || "https://release.lucee.org".equals(location)) location = DEFAULT_LOCATION;
					return HTTPUtil.toURL(location, HTTPUtil.ENCODED_AUTO);
				}

			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return Constants.DEFAULT_UPDATE_URL;
	}

	public static RemoteClient[] loadRemoteClients(ConfigImpl config, Struct root, Log log) {
		java.util.List<RemoteClient> list = new ArrayList<RemoteClient>();
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_REMOTE);

			Struct _clients = ConfigWebUtil.getAsStruct("remoteClients", root);

			Array clients = null;
			Struct client;

			if (hasAccess && _clients != null) clients = ConfigWebUtil.getAsArray("remoteClient", _clients);

			if (clients != null) {
				Iterator<?> it = clients.getIterator();
				while (it.hasNext()) {
					try {
						client = Caster.toStruct(it.next(), null);
						if (client == null) continue;

						// type
						String type = getAttr(client, "type");
						if (StringUtil.isEmpty(type)) type = "web";
						// url
						String url = getAttr(client, "url");
						String label = getAttr(client, "label");
						if (StringUtil.isEmpty(label)) label = url;
						String sUser = getAttr(client, "serverUsername");
						String sPass = ConfigWebUtil.decrypt(getAttr(client, "serverPassword"));
						String aPass = ConfigWebUtil.decrypt(getAttr(client, "adminPassword"));
						String aCode = ConfigWebUtil.decrypt(getAttr(client, "securityKey"));
						// if(aCode!=null && aCode.indexOf('-')!=-1)continue;
						String usage = getAttr(client, "usage");
						if (usage == null) usage = "";

						String pUrl = getAttr(client, "proxyServer");
						int pPort = Caster.toIntValue(getAttr(client, "proxyPort"), -1);
						String pUser = getAttr(client, "proxyUsername");
						String pPass = ConfigWebUtil.decrypt(getAttr(client, "proxyPassword"));
						ProxyData pd = null;
						if (!StringUtil.isEmpty(pUrl, true)) {
							pd = new ProxyDataImpl();
							pd.setServer(pUrl);
							if (!StringUtil.isEmpty(pUser)) {
								pd.setUsername(pUser);
								pd.setPassword(pPass);
							}
							if (pPort > 0) pd.setPort(pPort);
						}
						list.add(new RemoteClientImpl(label, type, url, sUser, sPass, aPass, pd, aCode, usage));

					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}

				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}

		return list.toArray(new RemoteClient[list.size()]);
	}

	public static PrintStream loadErr(ConfigImpl config, Struct root, Log log) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			String err = null;
			err = SystemUtil.getSystemPropOrEnvVar("lucee.system.err", null);
			if (StringUtil.isEmpty(err)) err = getAttr(root, "systemErr");
			return toPrintStream(config, err, true);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return null;
	}

	public static PrintStream loadOut(ConfigImpl config, Struct root, Log log) {
		try {

			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			String out = null;
			// sys prop or env var
			out = SystemUtil.getSystemPropOrEnvVar("lucee.system.out", null);
			if (StringUtil.isEmpty(out)) out = getAttr(root, "systemOut");
			return toPrintStream(config, out, false);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return null;
	}

	private static PrintStream toPrintStream(Config config, String streamtype, boolean iserror) {
		if (!StringUtil.isEmpty(streamtype)) {
			streamtype = streamtype.trim();
			// null
			if ("null".equalsIgnoreCase(streamtype)) {
				return new PrintStream(DevNullOutputStream.DEV_NULL_OUTPUT_STREAM);
			}
			// class
			else if (StringUtil.startsWithIgnoreCase(streamtype, "class:")) {
				String classname = streamtype.substring(6);
				try {

					return (PrintStream) ClassUtil.loadInstance((PageContext) null, classname);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
			// file
			else if (StringUtil.startsWithIgnoreCase(streamtype, "file:")) {
				String strRes = streamtype.substring(5);
				try {
					strRes = ConfigWebUtil.translateOldPath(strRes);
					Resource res = ConfigWebUtil.getFile(config, config.getConfigDir(), strRes, ResourceUtil.TYPE_FILE);
					if (res != null) return new PrintStream(res.getOutputStream(), true);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
			else if (StringUtil.startsWithIgnoreCase(streamtype, "log")) {
				try {
					CFMLEngineFactory factory = ConfigWebUtil.getCFMLEngineFactory(config);
					Resource root = ResourceUtil.toResource(factory.getResourceRoot());
					Resource log = root.getRealResource("context/logs/" + (iserror ? "err" : "out") + ".log");
					if (!log.isFile()) {
						log.getParentResource().mkdirs();
						log.createNewFile();
					}
					return new PrintStream(new RetireOutputStream(log, true, 5, null));
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
		}
		return iserror ? CFMLEngineImpl.CONSOLE_ERR : CFMLEngineImpl.CONSOLE_OUT;

	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 */
	public static TimeZone loadTimezone(ConfigImpl config, Struct root, Log log, TimeZone defaultValue) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			// timeZone
			String strTimeZone = null;
			strTimeZone = getAttr(root, new String[] { "timezone", "thisTimezone" });

			if (!StringUtil.isEmpty(strTimeZone)) return TimeZone.getTimeZone(strTimeZone);
			else {
				TimeZone def = TimeZone.getDefault();
				if (def == null) {
					def = TimeZoneConstants.EUROPE_LONDON;
				}
				return def;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return defaultValue;
	}

	public static Locale loadLocale(ConfigImpl config, Struct root, Log log, Locale defaultValue) {
		if (ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)) {
			try {
				// locale
				String strLocale = getAttr(root, new String[] { "locale", "thisLocale" });
				if (!StringUtil.isEmpty(strLocale)) return Caster.toLocale(strLocale, defaultValue);

			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log(config, log, t);
			}
		}
		return defaultValue;
	}

	public static ClassDefinition loadWS(ConfigImpl config, Struct root, Log log, ClassDefinition defaultValue) {
		try {
			Struct ws = ConfigWebUtil.getAsStruct("webservice", root);
			ClassDefinition cd = ws != null ? getClassDefinition(ws, "", config.getIdentification()) : null;
			if (cd != null && !StringUtil.isEmpty(cd.getClassName())) {
				return cd;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return defaultValue;
	}

	public static ClassDefinition loadORMClass(ConfigImpl config, Struct root, Log log) {

		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM);
			Struct orm = ConfigWebUtil.getAsStruct("orm", root);

			// engine

			ClassDefinition cd = null;
			if (orm != null) {
				cd = getClassDefinition(orm, "engine", config.getIdentification());
				if (cd == null || cd.isClassNameEqualTo(DummyORMEngine.class.getName()) || cd.isClassNameEqualTo("lucee.runtime.orm.hibernate.HibernateORMEngine"))
					cd = getClassDefinition(orm, "", config.getIdentification());

				if (cd != null && (cd.isClassNameEqualTo(DummyORMEngine.class.getName()) || cd.isClassNameEqualTo("lucee.runtime.orm.hibernate.HibernateORMEngine"))) cd = null;
			}

			if (cd == null || !cd.hasClass()) {
				cd = DUMMY_ORM_ENGINE;
			}
			return cd;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return DUMMY_ORM_ENGINE;
	}

	public static ORMConfiguration loadORMConfig(ConfigImpl config, Struct root, Log log, ORMConfiguration defaultValue) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM);
			Struct orm = ConfigWebUtil.getAsStruct("orm", root);

			// config
			ORMConfiguration def = null;
			ORMConfiguration ormConfig = root == null ? def : ORMConfigurationImpl.load(config, null, orm, config.getRootDirectory(), def);
			return ormConfig;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return defaultValue;
	}

	public static short loadJava(ConfigImpl config, Struct root, Log log, short defaultValue) {
		try {

			String strCompileType = getAttr(root, "compileType");
			if (!StringUtil.isEmpty(strCompileType)) {
				strCompileType = strCompileType.trim().toLowerCase();
				if (strCompileType.equals("after-startup")) {
					return Config.RECOMPILE_AFTER_STARTUP;
				}
				else if (strCompileType.equals("always")) {
					return Config.RECOMPILE_ALWAYS;
				}
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return defaultValue;
	}

	public static JavaSettings loadJavaSettings(ConfigImpl config, Struct root, Log log, JavaSettings defaultValue) {
		try {

			Resource lib = config.getLibraryDirectory();
			Resource[] libs = lib.listResources(ExtensionResourceFilter.EXTENSION_JAR_NO_DIR);

			Struct javasettings = ConfigWebUtil.getAsStruct(root, false, "javasettings");

			if (javasettings != null && javasettings.size() > 0) {
				JavaSettings js = JavaSettingsImpl.getInstance(config, javasettings, libs);
				return js;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return defaultValue;
	}

	public static Struct loadConstants(ConfigImpl config, Struct root, Log log, Struct defaultValue) {
		try {
			Struct constants = ConfigWebUtil.getAsStruct("constants", root);

			// Constants
			Struct sct = null;
			if (sct == null) sct = new StructImpl();
			Key name;
			if (constants != null) {
				Iterator<Entry<Key, Object>> it = constants.entryIterator();
				Struct con;
				Entry<Key, Object> e;
				while (it.hasNext()) {
					try {
						e = it.next();
						con = Caster.toStruct(it.next(), null);
						if (con == null) continue;

						name = e.getKey();
						if (StringUtil.isEmpty(name)) continue;
						sct.setEL(name, e.getValue());

					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}

				}
			}
			return sct;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return defaultValue;
	}

	public static void log(Config config, Log log, Throwable e) {
		try {
			if (log != null) log.error("configuration", e);
			else {
				LogUtil.logGlobal(config, ConfigWebFactory.class.getName(), e);
			}
		}
		catch (Throwable th) {
			ExceptionUtil.rethrowIfNecessary(th);
			th.printStackTrace();
		}
	}

	public static Map<String, Startup> loadStartupHook(ConfigImpl config, Struct root, Log log) {
		Map<String, Startup> startups = new HashMap<>();
		try {
			Array children = ConfigWebUtil.getAsArray("startupHooks", root);

			if (children == null || children.size() == 0) return startups;

			Iterator<?> it = children.getIterator();
			Struct child;
			while (it.hasNext()) {
				try {
					child = Caster.toStruct(it.next());
					if (child == null) continue;
					// component
					String cfc = Caster.toString(child.get(KeyConstants._component, null), null);
					if (!StringUtil.isEmpty(cfc, true)) {
						// TODO start hook
						continue;
					}

					// class
					ClassDefinition cd = getClassDefinition(child, "", config.getIdentification());
					ConfigBase.Startup existing = startups.get(cd.getClassName());

					if (existing != null) {
						if (existing.cd.equals(cd)) continue;
						try {
							Method fin = Reflector.getMethod(existing.instance.getClass(), "finalize", new Class[0], null);
							if (fin != null) {
								fin.invoke(existing.instance, new Object[0]);
							}
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
						}
					}
					Class clazz = cd.getClazz();

					Constructor constr = Reflector.getConstructor(clazz, new Class[] { Config.class }, null);
					if (constr != null) startups.put(cd.getClassName(), new ConfigBase.Startup(cd, constr.newInstance(new Object[] { config })));
					else startups.put(cd.getClassName(), new ConfigBase.Startup(cd, ClassUtil.loadInstance(clazz)));
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, log, t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return startups;
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws IOException
	 */
	public static void loadMail(ConfigImpl config, Struct root, Log log) { // does no init values

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);

		// Send partial
		try {
			String strSendPartial = getAttr(root, "mailSendPartial");
			if (!StringUtil.isEmpty(strSendPartial) && hasAccess) {
				config.setMailSendPartial(toBoolean(strSendPartial, false));
			}
			else {
				config.setMailSendPartial(false);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
			config.setMailSendPartial(false);
		}

		// User set
		try {
			String strUserSet = getAttr(root, "mailUserSet");
			if (!StringUtil.isEmpty(strUserSet) && hasAccess) {
				config.setUserSet(toBoolean(strUserSet, true));
			}
			else {
				config.setUserSet(true);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
			config.setUserSet(true);
		}

		// Spool Interval
		try {
			String strSpoolInterval = getAttr(root, "mailSpoolInterval");
			if (!StringUtil.isEmpty(strSpoolInterval) && hasAccess) {
				config.setMailSpoolInterval(Caster.toIntValue(strSpoolInterval, 30));
			}
			else {
				config.setMailSpoolInterval(30);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
			config.setMailSpoolInterval(30);
		}

		// Encoding
		try {
			String strEncoding = getAttr(root, "mailDefaultEncoding");
			if (!StringUtil.isEmpty(strEncoding, true) && hasAccess) {
				config.setMailDefaultEncoding(strEncoding);
			}
			else {
				config.setMailDefaultEncoding(CharsetUtil.UTF8);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
			config.setMailDefaultEncoding(CharsetUtil.UTF8);
		}

		// Spool Enable
		try {
			String strSpoolEnable = getAttr(root, "mailSpoolEnable");
			if (!StringUtil.isEmpty(strSpoolEnable) && hasAccess) {
				config.setMailSpoolEnable(toBoolean(strSpoolEnable, true));
			}
			else {
				config.setMailSpoolEnable(true);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
			config.setMailSpoolEnable(true);
		}

		// Timeout
		try {
			String strTimeout = getAttr(root, "mailConnectionTimeout");
			if (!StringUtil.isEmpty(strTimeout) && hasAccess) {
				config.setMailTimeout(Caster.toIntValue(strTimeout, 30));
			}
			else {
				config.setMailTimeout(30);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
			config.setMailTimeout(30);
		}

		// Servers
		List<Server> servers = new ArrayList<Server>();
		try {
			int index = 0;
			// Server[] servers = null;
			Array elServers = ConfigWebUtil.getAsArray("mailServers", root);

			// TODO get mail servers from env var
			if (hasAccess) {
				Iterator<?> it = elServers.getIterator();
				Struct el;
				int i = -1;
				while (it.hasNext()) {
					try {
						el = Caster.toStruct(it.next(), null);
						if (el == null) continue;
						i++;
						servers.add(i,
								new ServerImpl(Caster.toIntValue(getAttr(el, "id"), i + 1), getAttr(el, "smtp"), Caster.toIntValue(getAttr(el, "port"), 25),
										getAttr(el, "username"), ConfigWebUtil.decrypt(getAttr(el, "password")), toLong(getAttr(el, "life"), 1000 * 60 * 5),
										toLong(getAttr(el, "idle"), 1000 * 60 * 1), toBoolean(getAttr(el, "tls"), false), toBoolean(getAttr(el, "ssl"), false),
										toBoolean(getAttr(el, "reuseConnection"), true), ServerImpl.TYPE_GLOBAL));

					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}
			config.setMailServers(servers.toArray(new Server[servers.size()]));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
			config.setMailServers(servers.toArray(new Server[servers.size()]));
		}
	}

	public static void loadMonitors(ConfigImpl config, Struct root, Log log) {
		try {
			// only load in server context
			ConfigServerImpl configServer = (ConfigServerImpl) config;
			Struct parent = ConfigWebUtil.getAsStruct("monitoring", root);
			Array children = ConfigWebUtil.getAsArray("monitor", parent);

			java.util.List<IntervallMonitor> intervalls = new ArrayList<IntervallMonitor>();
			java.util.List<RequestMonitor> requests = new ArrayList<RequestMonitor>();
			java.util.List<MonitorTemp> actions = new ArrayList<MonitorTemp>();
			String strType, name;
			ClassDefinition cd;
			boolean _log, async;
			short type;
			Iterator<?> it = children.getIterator();
			Struct el;
			while (it.hasNext()) {
				try {
					el = Caster.toStruct(it.next(), null);
					if (el == null) continue;

					cd = getClassDefinition(el, "", config.getIdentification());
					strType = getAttr(el, "type");
					name = getAttr(el, "name");
					async = Caster.toBooleanValue(getAttr(el, "async"), false);
					_log = Caster.toBooleanValue(getAttr(el, "log"), true);

					if ("request".equalsIgnoreCase(strType)) type = IntervallMonitor.TYPE_REQUEST;
					else if ("action".equalsIgnoreCase(strType)) type = Monitor.TYPE_ACTION;
					else type = IntervallMonitor.TYPE_INTERVAL;

					if (cd.hasClass() && !StringUtil.isEmpty(name)) {
						name = name.trim();
						try {
							Class clazz = cd.getClazz();
							Object obj;
							ConstructorInstance constr = Reflector.getConstructorInstance(clazz, new Object[] { configServer });
							if (constr.getConstructor(null) != null) obj = constr.invoke();
							else obj = ClassUtil.newInstance(clazz);
							LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), Log.LEVEL_INFO, ConfigWebFactory.class.getName(),
									"loaded " + (strType) + " monitor [" + clazz.getName() + "]");
							if (type == IntervallMonitor.TYPE_INTERVAL) {
								IntervallMonitor m = obj instanceof IntervallMonitor ? (IntervallMonitor) obj : new IntervallMonitorWrap(obj);
								m.init(configServer, name, _log);
								intervalls.add(m);
							}
							else if (type == Monitor.TYPE_ACTION) {
								ActionMonitor am = obj instanceof ActionMonitor ? (ActionMonitor) obj : new ActionMonitorWrap(obj);
								actions.add(new MonitorTemp(am, name, _log));
							}
							else {
								RequestMonitorPro m = new RequestMonitorProImpl(obj instanceof RequestMonitor ? (RequestMonitor) obj : new RequestMonitorWrap(obj));
								if (async) m = new AsyncRequestMonitor(m);
								m.init(configServer, name, _log);
								LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), Log.LEVEL_INFO, ConfigWebFactory.class.getName(),
										"initialize " + (strType) + " monitor [" + clazz.getName() + "]");
								requests.add(m);
							}
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
							LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), ConfigWebFactory.class.getName(), t);
						}
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, log, t);
				}
			}
			configServer.setRequestMonitors(requests.toArray(new RequestMonitor[requests.size()]));
			configServer.setIntervallMonitors(intervalls.toArray(new IntervallMonitor[intervalls.size()]));
			ActionMonitorCollector actionMonitorCollector = ActionMonitorFatory.getActionMonitorCollector(configServer, actions.toArray(new MonitorTemp[actions.size()]));
			configServer.setActionMonitorCollector(actionMonitorCollector);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 * @throws PageException
	 */
	public static ClassDefinition<SearchEngine> loadSearchClass(ConfigImpl config, Struct root, Log log) {
		try {
			Struct search = ConfigWebUtil.getAsStruct("search", root);

			// class
			ClassDefinition<SearchEngine> cd = search != null ? getClassDefinition(search, "engine", config.getIdentification()) : null;
			if (cd == null || !cd.hasClass() || "lucee.runtime.search.lucene.LuceneSearchEngine".equals(cd.getClassName())) {
				cd = new ClassDefinitionImpl(DummySearchEngine.class);
			}

			return cd;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return new ClassDefinitionImpl(DummySearchEngine.class);
	}

	public static String loadSearchDir(ConfigImpl config, Struct root, Log log) {
		try {
			Struct search = ConfigWebUtil.getAsStruct("search", root);

			// directory
			String dir = search != null ? getAttr(search, "directory") : null;
			if (StringUtil.isEmpty(dir)) {
				dir = "{lucee-web}/search/";
			}

			return dir;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return "{lucee-web}/search/";
	}

	public static int loadDebugOptions(ConfigImpl config, Struct root, Log log) {
		int options = 0;
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);

			// debug options
			String strDebugOption = SystemUtil.getSystemPropOrEnvVar("lucee.debugging.options", null);
			String[] debugOptions = StringUtil.isEmpty(strDebugOption) ? null : ListUtil.listToStringArray(strDebugOption, ',');

			String str = getAttr(root, "debuggingDatabase");
			if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowDatabase");
			if (StringUtil.isEmpty(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingDatabase", null);
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_DATABASE;
			}
			else if (debugOptions != null && extractDebugOption("database", debugOptions)) options += ConfigPro.DEBUG_DATABASE;

			str = getAttr(root, "debuggingException");
			if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowException");
			if (StringUtil.isEmpty(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingException", null);
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_EXCEPTION;
			}
			else if (debugOptions != null && extractDebugOption("exception", debugOptions)) options += ConfigPro.DEBUG_EXCEPTION;

			str = getAttr(root, "debuggingTemplate");
			if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowTemplate");
			if (StringUtil.isEmpty(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingTemplate", null);
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_TEMPLATE;
			}
			else if (debugOptions != null && extractDebugOption("template", debugOptions)) options += ConfigPro.DEBUG_TEMPLATE;

			str = getAttr(root, "debuggingDump");
			if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowDump");
			if (StringUtil.isEmpty(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingDump", null);
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_DUMP;
			}
			else if (debugOptions != null && extractDebugOption("dump", debugOptions)) options += ConfigPro.DEBUG_DUMP;

			str = getAttr(root, "debuggingTracing");
			if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowTracing");
			if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowTrace");
			if (StringUtil.isEmpty(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingTracing", null);
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_TRACING;
			}
			else if (debugOptions != null && extractDebugOption("tracing", debugOptions)) options += ConfigPro.DEBUG_TRACING;

			str = getAttr(root, "debuggingTimer");
			if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowTimer");
			if (StringUtil.isEmpty(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingTimer", null);
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_TIMER;
			}
			else if (debugOptions != null && extractDebugOption("timer", debugOptions)) options += ConfigPro.DEBUG_TIMER;

			str = getAttr(root, "debuggingImplicitAccess");
			if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowImplicitAccess");
			if (StringUtil.isEmpty(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingImplicitAccess", null);
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_IMPLICIT_ACCESS;
			}
			else if (debugOptions != null && extractDebugOption("implicit-access", debugOptions)) options += ConfigPro.DEBUG_IMPLICIT_ACCESS;

			str = getAttr(root, "debuggingQueryUsage");
			if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowQueryUsage");
			if (StringUtil.isEmpty(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingQueryUsage", null);
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_QUERY_USAGE;
			}
			else if (debugOptions != null && extractDebugOption("queryUsage", debugOptions)) options += ConfigPro.DEBUG_QUERY_USAGE;

			str = getAttr(root, "debuggingThread");
			if (StringUtil.isEmpty(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingThread", null);
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_THREAD;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return options;
	}

	private static boolean extractDebugOption(String name, String[] values) {
		for (String val: values) {
			if (StringUtil.emptyIfNull(val).trim().equalsIgnoreCase(name)) return true;
		}
		return false;
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 */
	public static Map<String, CFXTagClass> loadCFX(ConfigImpl config, Struct root, Log log) {
		Map<String, CFXTagClass> map = MapFactory.<String, CFXTagClass>getConcurrentMap();
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CFX_SETTING);

			if (hasAccess) {
				System.setProperty("cfx.bin.path", config.getConfigDir().getRealResource("bin").getAbsolutePath());

				// Java CFX Tags
				Struct cfxs = ConfigWebUtil.getAsStruct("cfx", root);
				Iterator<Entry<Key, Object>> it = cfxs.entryIterator();
				Struct cfxTag;
				Entry<Key, Object> entry;
				while (it.hasNext()) {
					try {
						entry = it.next();
						cfxTag = Caster.toStruct(entry.getValue(), null);
						if (cfxTag == null) continue;

						String type = getAttr(cfxTag, "type");
						if (type != null) {
							// Java CFX Tags
							if ("java".equalsIgnoreCase(type)) {
								String name = entry.getKey().getString();
								ClassDefinition cd = getClassDefinition(cfxTag, "", config.getIdentification());
								if (!StringUtil.isEmpty(name) && cd.hasClass()) {
									map.put(name.toLowerCase(), new JavaCFXTagClass(name, cd));
								}
							}
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}

			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return map;
	}

	/**
	 * loads the bundles defined in the extensions
	 * 
	 * @param cs
	 * @param config
	 * @param doc
	 * @param log
	 */
	private static void _loadExtensionBundles(ConfigServerImpl config, Struct root, Log log) {
		Log deployLog = config.getLog("deploy");
		if (deployLog != null) log = deployLog;
		try {
			Array children = ConfigWebUtil.getAsArray("extensions", root);
			String md5 = CollectionUtil.md5(children);
			if (md5.equals(config.getExtensionsMD5())) {
				return;
			}

			boolean firstLoad = config.getExtensionsMD5() == null;

			try {
				RHExtension.removeDuplicates(children);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log(config, log, t);
			}

			String strBundles;
			List<RHExtension> extensions = new ArrayList<RHExtension>();
			RHExtension rhe;

			Iterator<Object> it = children.valueIterator();
			Entry<Key, Object> e;
			Struct child;
			String id;
			Set<Resource> installedFiles = new HashSet<>();
			Set<String> installedIds = new HashSet<>();
			// load and install extension if necessary
			while (it.hasNext()) {
				child = Caster.toStruct(it.next(), null);
				if (child == null) continue;
				id = Caster.toString(child.get(KeyConstants._id, null), null);
				BundleInfo[] bfsq;
				try {
					String res = Caster.toString(child.get(KeyConstants._resource, null), null);
					if (StringUtil.isEmpty(res)) res = Caster.toString(child.get(KeyConstants._path, null), null);
					if (StringUtil.isEmpty(res)) res = Caster.toString(child.get(KeyConstants._url, null), null);

					if (StringUtil.isEmpty(id) && StringUtil.isEmpty(res)) continue;

					// we force a new installation if we have switched from single to multi mode, because extension can
					// act completely different if that is the case
					rhe = RHExtension.installExtension(config, id, Caster.toString(child.get(KeyConstants._version, null), null), res, false);
					if (rhe.getStartBundles()) {
						if (!firstLoad) {
							rhe.deployBundles(config, true);
						}
						else {
							try {
								BundleInfo[] bundles = rhe.getBundles();
								if (bundles != null) {
									for (BundleInfo bi: bundles) {
										OSGiUtil.loadBundleFromLocal(bi.getSymbolicName(), bi.getVersion(), null, false, null);
									}
								}
							}
							catch (Exception ex) {
								rhe.deployBundles(config, true);
							}
						}
					}

					extensions.add(rhe);
					installedFiles.add(rhe.getExtensionFile());
					installedIds.add(rhe.getId());
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, log, t);
					continue;
				}
			}

			// uninstall extensions no longer used
			Resource[] installed = RHExtension.getExtensionInstalledDir(config).listResources(new ExtensionResourceFilter("lex"));
			if (installed != null) {
				for (Resource r: installed) {
					if (!installedFiles.contains(r)) {

						// is maybe a diff version installed?
						RHExtension ext = RHExtension.getInstance(config, r);
						if (!installedIds.contains(ext.getId())) {
							if (log != null) log.info("extension",
									"Found the extension [" + ext + "] in the installed folder that is not present in the configuration in any version, so we will uninstall it");
							ConfigAdmin._removeRHExtension(config, ext, null, true);
							if (log != null) log.info("extension", "removed extension [" + ext + "]");
						}
						else {
							if (log != null) log.info("extension", "Found the extension [" + ext
									+ "] in the installed folder that is in a different version in the configuraton, so we delete that extension file.");
							r.delete();
						}

					}
				}
			}
			// set
			config.setExtensions(extensions.toArray(new RHExtension[extensions.size()]), md5);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
	}

	private static void _loadExtensionDefinition(ConfigServerImpl config, Struct root, Log log) {

		try {
			Log deployLog = config.getLog("deploy");
			if (deployLog != null) log = deployLog;
			Array children = ConfigWebUtil.getAsArray("extensions", root);
			// set
			Map<String, String> child;
			Struct childSct;
			String id;
			Iterator<Object> it = children.valueIterator();
			List<ExtensionDefintion> extensions = null;
			while (it.hasNext()) {
				childSct = Caster.toStruct(it.next(), null);
				if (childSct == null) continue;
				child = Caster.toStringMap(childSct, null);

				if (child == null) continue;
				id = Caster.toString(childSct.get(KeyConstants._id, null), null);

				try {
					if (extensions == null) extensions = new ArrayList<>();
					extensions.add(RHExtension.toExtensionDefinition(config, id, child));
				}
				catch (Exception e) {
					log(config, log, e);
				}
			}
			if (extensions != null) config.setExtensionDefinitions(extensions);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
	}

	public static RHExtensionProvider[] loadExtensionProviders(ConfigImpl config, Struct root, Log log) {
		Map<RHExtensionProvider, String> providers = new LinkedHashMap<RHExtensionProvider, String>();
		try {
			// providers
			Array xmlProviders = ConfigWebUtil.getAsArray("extensionProviders", root);
			String strProvider;

			for (int i = 0; i < Constants.RH_EXTENSION_PROVIDERS.length; i++) {
				providers.put(Constants.RH_EXTENSION_PROVIDERS[i], "");
			}
			if (xmlProviders != null) {
				Iterator<?> it = xmlProviders.valueIterator();
				String url;
				while (it.hasNext()) {
					url = Caster.toString(it.next(), null);
					if (StringUtil.isEmpty(url, true)) continue;

					try {
						providers.put(new RHExtensionProvider(url.trim(), false), "");
					}
					catch (MalformedURLException e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), ConfigWebFactory.class.getName(), e);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return providers.keySet().toArray(new RHExtensionProvider[providers.size()]);
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 * @throws IOException
	 */
	public static Mapping[] loadComponentMappings(ConfigImpl config, Struct root, Log log) {
		Mapping[] mappings = null;
		try {
			boolean hasSet = false;

			// Web Mapping
			Array compMappings = ConfigWebUtil.getAsArray("componentMappings", root);
			hasSet = false;
			boolean hasDefault = false;
			if (compMappings.size() > 0) {
				Iterator<Object> it = compMappings.valueIterator();
				List<Mapping> list = new ArrayList<>();
				Struct cMapping;
				while (it.hasNext()) {
					try {
						cMapping = Caster.toStruct(it.next(), null);
						if (cMapping == null) continue;

						String virtual = createVirtual(cMapping);
						String physical = getAttr(cMapping, "physical");
						String archive = getAttr(cMapping, "archive");
						boolean readonly = toBoolean(getAttr(cMapping, "readonly"), false);
						boolean hidden = toBoolean(getAttr(cMapping, "hidden"), false);
						if ("{lucee-web}/components/".equals(physical) || "{lucee-server}/components/".equals(physical)) continue;
						if ("{lucee-config}/components/".equals(physical)) hasDefault = true;

						String strListMode = getAttr(cMapping, "listenerMode");
						if (StringUtil.isEmpty(strListMode)) strListMode = getAttr(cMapping, "listener-mode");
						if (StringUtil.isEmpty(strListMode)) strListMode = getAttr(cMapping, "listenermode");
						int listMode = ConfigWebUtil.toListenerMode(strListMode, -1);

						String strListType = getAttr(cMapping, "listenerType");
						if (StringUtil.isEmpty(strListType)) strListMode = getAttr(cMapping, "listener-type");
						if (StringUtil.isEmpty(strListType)) strListMode = getAttr(cMapping, "listenertype");
						int listType = ConfigWebUtil.toListenerType(strListType, -1);

						short inspTemp = inspectTemplate(cMapping);
						int insTempSlow = Caster.toIntValue(getAttr(cMapping, "inspectTemplateIntervalSlow"), -1);
						int insTempFast = Caster.toIntValue(getAttr(cMapping, "inspectTemplateIntervalFast"), -1);

						String primary = getAttr(cMapping, "primary");

						boolean physicalFirst = archive == null || !"archive".equalsIgnoreCase(primary);
						hasSet = true;
						list.add(new MappingImpl(config, virtual, physical, archive, inspTemp, insTempSlow, insTempFast, physicalFirst, hidden, readonly, true, false, true, null,
								listMode, listType));
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, log, t);
					}
				}
				if (!hasDefault) {
					list.add(new MappingImpl(config, "/default", "{lucee-config}/components/", null, ConfigPro.INSPECT_NEVER, -1, -1, true, false, true, true, false, true, null,
							-1, -1));
				}
				mappings = list.toArray(new Mapping[list.size()]);
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}

		if (mappings == null) {
			mappings = new Mapping[] { new MappingImpl(config, "/default-component", "{lucee-config}/components/", null, ConfigPro.INSPECT_UNDEFINED,
					ConfigPro.INSPECT_INTERVAL_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, true, true, true, false, true, null, -1, -1) };
		}
		return mappings;

	}

	public static void loadProxy(ConfigServerImpl config, Struct root, Log log) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
			Struct proxy = ConfigWebUtil.getAsStruct("proxy", root);

			// proxy server
			boolean enabled = Caster.toBooleanValue(getAttr(proxy, "enabled"), true);
			String server = getAttr(proxy, "server");
			String username = getAttr(proxy, "username");
			String password = getAttr(proxy, "password");
			int port = Caster.toIntValue(getAttr(proxy, "port"), -1);

			// includes/excludes
			Set<String> includes = proxy != null ? ProxyDataImpl.toStringSet(getAttr(proxy, "includes")) : null;
			Set<String> excludes = proxy != null ? ProxyDataImpl.toStringSet(getAttr(proxy, "excludes")) : null;

			if (enabled && hasAccess && !StringUtil.isEmpty(server)) {
				ProxyDataImpl pd = (ProxyDataImpl) ProxyDataImpl.getInstance(server, port, username, password);
				pd.setExcludes(excludes);
				pd.setIncludes(includes);
				config.setProxyData(pd);

			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
	}

	public static boolean loadError(ConfigImpl config, Struct root, Log log, boolean defaultValue) {
		try {
			// Struct error = ConfigWebUtil.getAsStruct("error", root);
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);

			// status code
			Boolean bStausCode = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.status.code", null), null);
			if (bStausCode == null) bStausCode = Caster.toBoolean(getAttr(root, "errorStatusCode"), null);

			if (bStausCode != null && hasAccess) {
				return bStausCode.booleanValue();
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return defaultValue;
	}

	public static Regex loadRegex(ConfigImpl config, Struct root, Log log, Regex defaultValue) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			String strType = getAttr(root, "regexType");
			int type = StringUtil.isEmpty(strType) ? RegexFactory.TYPE_UNDEFINED : RegexFactory.toType(strType, RegexFactory.TYPE_UNDEFINED);

			if (hasAccess && type != RegexFactory.TYPE_UNDEFINED) {
				return RegexFactory.toRegex(type, null);
			}
			else return RegexFactory.toRegex(RegexFactory.TYPE_PERL, null);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, log, t);
		}
		return defaultValue;
	}

	/**
	 * cast a string value to a boolean
	 * 
	 * @param value String value represent a booolean ("yes", "no","true" aso.)
	 * @param defaultValue if can't cast to a boolean is value will be returned
	 * @return boolean value
	 */
	private static boolean toBoolean(String value, boolean defaultValue) {

		if (value == null || value.trim().length() == 0) return defaultValue;

		try {
			return Caster.toBooleanValue(value.trim());
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	public static long toLong(String value, long defaultValue) {

		if (value == null || value.trim().length() == 0) return defaultValue;
		long longValue = Caster.toLongValue(value.trim(), Long.MIN_VALUE);
		if (longValue == Long.MIN_VALUE) return defaultValue;
		return longValue;
	}

	public static String getAttr(Struct data, String name) {
		String v = ConfigWebUtil.getAsString(name, data, null);
		if (v == null) {
			return null;
		}
		if (StringUtil.isEmpty(v)) return "";
		return ConfigWebUtil.replaceConfigPlaceHolder(v);
	}

	public static String getAttr(Struct data, String name, String alias) {
		String v = ConfigWebUtil.getAsString(name, data, null);
		if (v == null) v = ConfigWebUtil.getAsString(alias, data, null);
		if (v == null) return null;
		if (StringUtil.isEmpty(v)) return "";
		return ConfigWebUtil.replaceConfigPlaceHolder(v);
	}

	public static String getAttr(Struct data, String[] names) {
		String v;
		for (String name: names) {
			v = ConfigWebUtil.getAsString(name, data, null);
			if (!StringUtil.isEmpty(v)) return ConfigWebUtil.replaceConfigPlaceHolder(v);
		}
		return null;

	}

	public static class Path {
		public final String str;
		public final Resource res;

		public Path(String str, Resource res) {
			this.str = str;
			this.res = res;
		}

		public boolean isValidDirectory() {
			return res.isDirectory();
		}
	}

	public static class MonitorTemp {

		public final ActionMonitor am;
		public final String name;
		public final boolean log;

		public MonitorTemp(ActionMonitor am, String name, boolean log) {
			this.am = am;
			this.name = name;
			this.log = log;
		}

	}
}