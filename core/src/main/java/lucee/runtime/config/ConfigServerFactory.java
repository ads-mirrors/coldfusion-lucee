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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.BundleException;
import org.xml.sax.SAXException;

import lucee.print;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactory;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalConfigServer;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.tag.TagLibException;

public final class ConfigServerFactory extends ConfigFactory {

	public static final String[] CONFIG_FILE_NAMES = new String[] { ".CFConfig.json", "config.json" };

	/**
	 * creates a new ServletConfig Impl Object
	 * 
	 * @param engine
	 * @param initContextes
	 * @param contextes
	 * @param configDir
	 * @return new Instance
	 * @throws SAXException
	 * @throws ClassNotFoundException
	 * @throws PageException
	 * @throws IOException
	 * @throws TagLibException
	 * @throws FunctionLibException
	 * @throws BundleException
	 * @throws ConverterException
	 */
	public static ConfigServerImpl newInstance(CFMLEngineImpl engine, Map<String, CFMLFactory> initContextes, Map<String, CFMLFactory> contextes, Resource configDir,
			ConfigServerImpl existing, boolean essentialOnly)
			throws SAXException, ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException, ConverterException {
		if (ThreadLocalPageContext.insideServerNewInstance()) throw new ApplicationException("already inside server.newInstance");
		try {
			ThreadLocalPageContext.insideServerNewInstance(true);
			boolean isCLI = SystemUtil.isCLICall();
			if (isCLI) {
				Resource logs = configDir.getRealResource("logs");
				logs.mkdirs();
				Resource out = logs.getRealResource("out");
				Resource err = logs.getRealResource("err");
				ResourceUtil.touch(out);
				ResourceUtil.touch(err);
				if (logs instanceof FileResource) {
					SystemUtil.setPrintWriter(SystemUtil.OUT, new PrintWriter((FileResource) out));
					SystemUtil.setPrintWriter(SystemUtil.ERR, new PrintWriter((FileResource) err));
				}
				else {
					SystemUtil.setPrintWriter(SystemUtil.OUT, new PrintWriter(IOUtil.getWriter(out, "UTF-8")));
					SystemUtil.setPrintWriter(SystemUtil.ERR, new PrintWriter(IOUtil.getWriter(err, "UTF-8")));
				}
			}
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigServerFactory.class.getName(),
					"===================================================================\n" + "SERVER CONTEXT\n"
							+ "-------------------------------------------------------------------\n" + "- config:" + configDir + "\n" + "- loader-version:"
							+ SystemUtil.getLoaderVersion() + "\n" + "- core-version:" + engine.getInfo().getVersion() + "\n"
							+ "===================================================================\n"

			);
			UpdateInfo ui = getNew(engine, configDir, essentialOnly, UpdateInfo.NEW_NONE);
			boolean doNew = ui.updateType != NEW_NONE;

			Resource configFileOld = configDir.getRealResource("lucee-server.xml");

			// config file
			Resource configFileNew = getConfigFile(configDir, true);

			boolean hasConfigOld = false;
			boolean hasConfigNew = configFileNew.exists() && configFileNew.length() > 0;

			if (!hasConfigNew) {
				LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigServerFactory.class.getName(),
						"has no json server context config [" + configFileNew + "]");
				hasConfigOld = configFileOld.exists() && configFileOld.length() > 0;
				LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigServerFactory.class.getName(),
						"has " + (hasConfigOld ? "" : "no ") + "xml server context config [" + configFileOld + "]");
			}
			ConfigServerImpl config = existing != null ? existing : new ConfigServerImpl(engine, initContextes, contextes, configDir, configFileNew, ui, essentialOnly, doNew);
			ThreadLocalConfigServer.register(config);
			// translate to new
			if (!hasConfigNew) {
				if (hasConfigOld) {
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigServerFactory.class.getName(), "convert server context xml config to json");
					try {
						translateConfigFile(config, configFileOld, configFileNew, "multi", true);
					}
					catch (IOException e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigServerFactory.class.getName(), e);
						throw e;
					}
					catch (ConverterException e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigServerFactory.class.getName(), e);
						throw e;
					}
					catch (SAXException e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigServerFactory.class.getName(), e);
						throw e;
					}
				}
				// create config file
				else {
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigServerFactory.class.getName(),
							"create new server context json config file [" + configFileNew + "]");
					createConfigFile("server", configFileNew);
					hasConfigNew = true;
				}
			}
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigServerFactory.class.getName(), "load config file");
			Struct root = loadDocumentCreateIfFails(configFileNew, "server");
			config.setRoot(root);
			// admin mode
			load(config, root, false, doNew, essentialOnly);

			if (!essentialOnly) {
				createContextFiles(configDir, config, doNew);
				((CFMLEngineImpl) ConfigWebUtil.getEngine(config)).onStart(config, false);
			}

			return config;
		}
		finally {
			ThreadLocalPageContext.insideServerNewInstance(false);
			ThreadLocalConfigServer.release();
		}
	}

	public static Resource getConfigFile(Resource configDir, boolean server) throws IOException {
		if (server) {
			// lucee.base.config
			String customCFConfig = SystemUtil.getSystemPropOrEnvVar("lucee.base.config", null);
			Resource configFile = null;
			if (!StringUtil.isEmpty(customCFConfig, true)) {

				configFile = ResourcesImpl.getFileResourceProvider().getResource(customCFConfig.trim());

				if (configFile.isFile()) {
					LogUtil.log(Log.LEVEL_INFO, "deploy", "config", "using config File : " + configFile);
					return configFile;
				}
				throw new IOException(
						"the config file [" + configFile + "] defined with the environment variable [LUCEE_BASE_CONFIG] or system property [-Dlucee.base.config] does not exist.");
			}
		}
		Resource res;
		for (String cf: CONFIG_FILE_NAMES) {
			res = configDir.getRealResource(cf);
			if (res.isFile()) return res;
		}

		// default location
		return configDir.getRealResource(CONFIG_FILE_NAMES[0]);
	}

	public static boolean isConfigFileName(String fileName) {
		for (String fn: CONFIG_FILE_NAMES) {
			if (fn.equalsIgnoreCase(fileName)) return true;
		}
		return false;
	}

	/**
	 * reloads the Config Object
	 * 
	 * @param configServer
	 * @throws SAXException
	 * @throws ClassNotFoundException
	 * @throws PageException
	 * @throws IOException
	 * @throws TagLibException
	 * @throws FunctionLibException
	 * @throws BundleException
	 */
	public static void reloadInstance(CFMLEngine engine, ConfigServerImpl configServer)
			throws ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException {
		boolean quick = CFMLEngineImpl.quick(engine);
		Resource configFile = configServer.getConfigFile();
		if (configFile == null) return;
		if (second(configServer.getLoadTime()) > second(configFile.lastModified())) {
			if (!configServer.getConfigDir().getRealResource("password.txt").isFile()) return;
		}
		int iDoNew = getNew(engine, configServer.getConfigDir(), quick, UpdateInfo.NEW_NONE).updateType;
		boolean doNew = iDoNew != NEW_NONE;
		Struct root = loadDocumentCreateIfFails(configFile, "server");
		configServer.setRoot(root);
		load(configServer, root, true, doNew, quick);
		((CFMLEngineImpl) ConfigWebUtil.getEngine(configServer)).onStart(configServer, true);
	}

	private static long second(long ms) {
		return ms / 1000;
	}

	/**
	 * @param configServer
	 * @param doc
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws FunctionLibException
	 * @throws TagLibException
	 * @throws PageException
	 * @throws BundleException
	 */
	static void load(ConfigServerImpl configServer, Struct root, boolean isReload, boolean doNew, boolean essentialOnly)
			throws ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException {
		ConfigBase.onlyFirstMatch = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.mapping.first", null), true); // changed behaviour in 6.0
		ConfigWebFactory.load(configServer, root, isReload, doNew, essentialOnly);
		loadLabel(configServer, root);
	}

	private static void loadLabel(ConfigServerImpl configServer, Struct root) {
		Array children = ConfigWebUtil.getAsArray("labels", "label", root);

		Map<String, String> labels = new HashMap<String, String>();
		if (children != null) {
			Iterator<?> it = children.getIterator();
			Struct data;
			while (it.hasNext()) {
				data = Caster.toStruct(it.next(), null);
				if (data == null) continue;
				String id = ConfigWebUtil.getAsString("id", data, null);
				String name = ConfigWebUtil.getAsString("name", data, null);
				if (id != null && name != null) {
					labels.put(id, name);
				}
			}
		}
		configServer.setLabels(labels);
	}

	private static void createContextFiles(Resource configDir, ConfigServer config, boolean doNew) {
		print.e("server.createContextFiles:" + configDir);

		// context
		{
			Resource contextDir = configDir.getRealResource("context");
			// lucee-admin (only deploy if enabled)
			if (Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.admin.enabled", "true"), true)) {
				Resource f = contextDir.getRealResource("lucee-admin.lar");
				if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/lucee-admin.lar", f);
				else ConfigWebFactory.createFileFromResourceCheckSizeDiffEL("/resource/context/lucee-admin.lar", f);
			}

			create("/resource/context/",
					new String[] { "lucee-context.lar", "lucee-doc.lar", "component-dump.cfm", "Application.cfc", "form.cfm", "graph.cfm", "wddx.cfm", "admin.cfm" }, contextDir,
					doNew);
		}

		// Plugin
		if (doNew) {
			Resource pluginDir = configDir.getRealResource("context/admin/plugin");
			create("/resource/context/admin/plugin/", new String[] { "Plugin.cfc" }, pluginDir, doNew);
		}
		// Plugin Note
		if (doNew) {
			Resource note = configDir.getRealResource("context/admin/plugin/Note");
			create("/resource/context/admin/plugin/Note/", new String[] { "language.xml", "overview.cfm", "Action.cfc" }, note, doNew);
		}

		// customtags
		if (doNew) {
			Resource ctDir = configDir.getRealResource("customtags");
			if (!ctDir.exists()) ctDir.mkdirs();
		}

		// gateway
		if (doNew) {
			Resource gwDir = configDir.getRealResource("components/lucee/extension/gateway/");
			create("/resource/context/gateway/", new String[] { "TaskGateway.cfc", "DummyGateway.cfc", "DirectoryWatcher.cfc", "DirectoryWatcherListener.cfc", "WatchService.cfc",
					"MailWatcher.cfc", "MailWatcherListener.cfc", "AsynchronousEvents.cfc", "AsynchronousEventsListener.cfc" }, gwDir, doNew);
		}

		// error
		if (doNew) {
			Resource errorDir = configDir.getRealResource("context/templates/error");
			create("/resource/context/templates/error/", new String[] { "error.cfm", "error-neo.cfm", "error-public.cfm" }, errorDir, doNew);
		}

		// display
		if (doNew) {
			Resource displayDir = configDir.getRealResource("context/templates/display");
			if (!displayDir.exists()) displayDir.mkdirs();
		}

		// Debug
		if (doNew) {
			Resource debug = configDir.getRealResource("context/admin/debug");
			create("/resource/context/admin/debug/", new String[] { "Debug.cfc", "Field.cfc", "Group.cfc", "Classic.cfc", "Simple.cfc", "Modern.cfc", "Comment.cfc" }, debug,
					doNew);
		}

		// Info
		if (doNew) {
			Resource info = configDir.getRealResource("context/admin/info");
			create("/resource/context/admin/info/", new String[] { "Info.cfc" }, info, doNew);
		}

		// DB Drivers types
		if (doNew) {
			Resource typesDir = configDir.getRealResource("context/admin/dbdriver/types");
			create("/resource/context/admin/dbdriver/types/", new String[] { "IDriver.cfc", "Driver.cfc", "IDatasource.cfc", "IDriverSelector.cfc", "Field.cfc" }, typesDir, doNew);
		}

		if (doNew) {
			Resource dbDir = configDir.getRealResource("context/admin/dbdriver");
			create("/resource/context/admin/dbdriver/", new String[] { "Other.cfc" }, dbDir, doNew);
		}

		// Cache Drivers
		if (doNew) {
			Resource cDir = configDir.getRealResource("context/admin/cdriver");
			create("/resource/context/admin/cdriver/", new String[] { "Cache.cfc", "RamCache.cfc", "Field.cfc", "Group.cfc" }, cDir, doNew);
		}

		// AI Drivers
		if (doNew) {
			Resource aiDir = configDir.getRealResource("context/admin/aidriver");
			create("/resource/context/admin/aidriver/", new String[] { "AI.cfc", "Gemini.cfc", "OpenAI.cfc", "Field.cfc", "Group.cfc" }, aiDir, doNew);
		}

		Resource wcdDir = configDir.getRealResource("web-context-deployment/admin");
		try {
			ResourceUtil.deleteEmptyFolders(wcdDir);
		}
		catch (IOException e) {
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), ConfigServerFactory.class.getName(), e);
		}

		// Mail Server Drivers
		if (doNew) {
			Resource msDir = configDir.getRealResource("context/admin/mailservers");
			create("/resource/context/admin/mailservers/",
					new String[] { "Other.cfc", "GMail.cfc", "GMX.cfc", "iCloud.cfc", "Yahoo.cfc", "Outlook.cfc", "MailCom.cfc", "MailServer.cfc" }, msDir, doNew);
		}
		// Gateway Drivers
		if (doNew) {
			Resource gDir = configDir.getRealResource("context/admin/gdriver");
			create("/resource/context/admin/gdriver/",
					new String[] { "TaskGatewayDriver.cfc", "AsynchronousEvents.cfc", "DirectoryWatcher.cfc", "MailWatcher.cfc", "Gateway.cfc", "Field.cfc", "Group.cfc" }, gDir,
					doNew);
		}
		// Logging/appender
		if (doNew) {
			Resource app = configDir.getRealResource("context/admin/logging/appender");
			create("/resource/context/admin/logging/appender/",
					new String[] { "DatasourceAppender.cfc", "ConsoleAppender.cfc", "ResourceAppender.cfc", "Appender.cfc", "Field.cfc", "Group.cfc" }, app, doNew);
		}
		// Logging/layout
		if (doNew) {
			Resource lay = configDir.getRealResource("context/admin/logging/layout");
			create("/resource/context/admin/logging/layout/", new String[] { "DatadogLayout.cfc", "ClassicLayout.cfc", "HTMLLayout.cfc", "PatternLayout.cfc", "XMLLayout.cfc",
					"JsonLayout.cfc", "Layout.cfc", "Field.cfc", "Group.cfc" }, lay, doNew);
		}
		// Security / SSL
		Resource secDir = configDir.getRealResource("security");
		Resource res = create("/resource/security/", "cacerts", secDir, false);
		if (SystemUtil.getSystemPropOrEnvVar("lucee.use.lucee.SSL.TrustStore", "").equalsIgnoreCase("true"))
			System.setProperty("javax.net.ssl.trustStore", res.toString());/* JAVJAK */
		// Allow using system proxies
		if (!SystemUtil.getSystemPropOrEnvVar("lucee.disable.systemProxies", "").equalsIgnoreCase("true")) System.setProperty("java.net.useSystemProxies", "true"); // it defaults
																																									// to false

	}

}