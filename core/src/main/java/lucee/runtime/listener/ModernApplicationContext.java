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
package lucee.runtime.listener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.date.TimeZoneUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.ftp.FTPConnectionData;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.runtime.Component;
import lucee.runtime.ComponentPageImpl;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheConnectionImpl;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.component.Member;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.net.s3.Properties;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.regex.Regex;
import lucee.runtime.regex.RegexFactory;
import lucee.runtime.rest.RestSettingImpl;
import lucee.runtime.rest.RestSettings;
import lucee.runtime.tag.Query;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.CustomType;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFCustomType;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.UDFUtil;
import lucee.transformer.library.ClassDefinitionImpl;

/**
 * This class resolves the Application settings that are defined in Application.cfc via the this
 * reference, e.g. this.sessionManagement, this.localMode, etc.
 */
public class ModernApplicationContext extends ApplicationContextSupport {

	private static final long serialVersionUID = -8230105685329758613L;

	private static Map<String, CacheConnection> initCacheConnections = new ConcurrentHashMap<String, CacheConnection>();
	private static Object token = new SerializableObject();
	private static JavaSettings defaultJavaSettings;
	private static ClassLoader defaultClassLoader;

	private Component component;

	private String name = null;

	private boolean setClientCookies;
	private boolean setDomainCookies;
	private boolean setSessionManagement;
	private boolean setClientManagement;
	private TimeSpan applicationTimeout;
	private TimeSpan sessionTimeout;
	private TimeSpan clientTimeout;
	private TimeSpan requestTimeout;
	private int loginStorage = Scope.SCOPE_COOKIE;
	private int scriptProtect;
	private boolean typeChecking;
	private boolean allowCompression;
	private Object defaultDataSource;
	private boolean bufferOutput;
	private boolean suppressContent;
	private short sessionType;
	private short wstype;
	private boolean wsMaintainSession = false;
	private boolean sessionCluster;
	private boolean clientCluster;

	private String clientStorage;
	private String sessionStorage;
	private String secureJsonPrefix = "//";
	private boolean secureJson;
	private Mapping[] ctmappings;
	private Mapping[] cmappings;
	private DataSource[] dataSources;

	private lucee.runtime.net.s3.Properties s3;
	private FTPConnectionData ftp;
	private boolean triggerComponentDataMember;
	private Map<Integer, String> defaultCaches;
	private Map<Collection.Key, CacheConnection> cacheConnections;
	private boolean sameFormFieldAsArray;
	private boolean sameURLFieldAsArray;
	private boolean formUrlAsStruct;
	private Map<String, CustomType> customTypes;
	private boolean cgiScopeReadonly;
	private boolean preciseMath;
	private int returnFormat = UDF.RETURN_FORMAT_WDDX;
	private SessionCookieData sessionCookie;
	private AuthCookieData authCookie;
	private Object mailListener;
	private TagListener queryListener;
	private boolean fullNullSupport;
	private SerializationSettings serializationSettings;
	private boolean queryPSQ;
	private TimeSpan queryCachedAfter;
	private int queryVarUsage;
	private ProxyData proxyData;
	private String blockedExtForFileUpload;
	private int localMode;

	private int debugging;

	private Mapping[] mappings;
	private boolean initMappings;
	private boolean initCustomTypes;
	private boolean initMailListener;
	private boolean initQueryListener;
	private boolean initFullNullSupport;
	private boolean initCachedWithins;

	private boolean initApplicationTimeout;
	private boolean initSessionTimeout;
	private boolean initClientTimeout;
	private boolean initRequestTimeout;
	private boolean initSetClientCookies;
	private boolean initSetClientManagement;
	private boolean initSetDomainCookies;
	private boolean initSetSessionManagement;
	private boolean initScriptProtect;
	private boolean initTypeChecking;
	private boolean initAllowCompression;
	private boolean initDefaultAttributeValues;
	private boolean initClientStorage;
	private boolean initSecureJsonPrefix;
	private boolean initSecureJson;
	private boolean initSessionStorage;
	private boolean initSessionCluster;
	private boolean initClientCluster;
	private boolean initLoginStorage;
	private boolean initSessionType;
	private boolean initWS;
	private boolean initTriggerComponentDataMember;
	private boolean initDataSources;
	private boolean initCache;
	private boolean initCTMappings;
	private boolean initCMappings;
	private boolean initLocalMode;
	private boolean initBufferOutput;
	private boolean initSuppressContent;
	private boolean initS3;
	private boolean initFTP;
	private boolean ormEnabled;
	private ORMConfiguration ormConfig;
	private boolean initRestSetting;
	private RestSettings restSetting;
	private boolean initClassLoader;
	private boolean initClassLoaderBefore;
	private JavaSettings javaSettings;
	private ClassLoader cl;
	private Object ormDatasource;
	private Locale locale;
	private boolean initLocale;

	private boolean showDebug = false;
	private boolean showDoc = false;
	private boolean showMetric = false;
	private boolean showTest = false;

	private boolean initMonitor = false;

	private TimeZone timeZone;
	private boolean initTimeZone;
	private CharSet webCharset;
	private boolean initWebCharset;
	private CharSet resourceCharset;
	private boolean initResourceCharset;
	private boolean initCGIScopeReadonly;
	private boolean initPreciseMath;
	private boolean initReturnFormat;
	private boolean initSessionCookie;
	private boolean initAuthCookie;
	private boolean initSerializationSettings;
	private boolean initQueryPSQ;
	private boolean initQueryCacheAfter;
	private boolean initQueryVarUsage;
	private boolean initProxyData;
	private boolean initBlockedExtForFileUpload;
	private boolean initXmlFeatures;
	private boolean initRegex;

	private Struct xmlFeatures;

	private Resource antiSamyPolicyResource;

	private Resource[] restCFCLocations;

	private short scopeCascading = -1;

	private Server[] mailServers;
	private boolean initMailServer;

	private boolean initLog;

	private Map<Collection.Key, Pair<Log, Struct>> logs;

	private List<Resource> funcDirs;
	private boolean initFuncDirs = false;

	private boolean allowImplicidQueryCall;
	private boolean limitEvaluation;
	private Regex regex;

	public ModernApplicationContext(PageContext pc, Component cfc, RefBoolean throwsErrorWhileInit) {
		super(pc.getConfig());
		ConfigPro ci = ((ConfigPro) config);
		this.defaultDataSource = config.getDefaultDataSource();
		this.locale = config.getLocale();
		suppressContent = ci.isSuppressContent();
		this.sessionType = config.getSessionType();
		this.wstype = WS_TYPE_AXIS1;
		this.cgiScopeReadonly = ci.getCGIScopeReadonly();
		this.fullNullSupport = ci.getFullNullSupport();
		this.queryPSQ = ci.getPSQL();
		this.queryCachedAfter = ci.getCachedAfterTimeRange();
		this.queryVarUsage = ci.getQueryVarUsage();
		this.proxyData = config.getProxyData();

		this.sessionCluster = config.getSessionCluster();
		this.clientCluster = config.getClientCluster();
		this.sessionStorage = ci.getSessionStorage();
		this.clientStorage = ci.getClientStorage();
		this.allowImplicidQueryCall = config.allowImplicidQueryCall();
		this.limitEvaluation = ci.limitEvaluation();
		this.triggerComponentDataMember = config.getTriggerComponentDataMember();
		this.restSetting = config.getRestSetting();
		this.component = cfc;
		this.regex = ci.getRegex();
		this.preciseMath = ci.getPreciseMath();
		this.formUrlAsStruct = ci.getFormUrlAsStruct();
		this.returnFormat = ci.getReturnFormat();

		this.showDebug = ci.getShowDebug();
		this.showDoc = ci.getShowDoc();
		this.showMetric = ci.getShowMetric();
		this.showTest = ci.getShowTest();

		if (ci.hasDebugOptions(ConfigPro.DEBUG_DATABASE)) this.debugging += ConfigPro.DEBUG_DATABASE;
		if (ci.hasDebugOptions(ConfigPro.DEBUG_DUMP)) this.debugging += ConfigPro.DEBUG_DUMP;
		if (ci.hasDebugOptions(ConfigPro.DEBUG_EXCEPTION)) this.debugging += ConfigPro.DEBUG_EXCEPTION;
		if (ci.hasDebugOptions(ConfigPro.DEBUG_IMPLICIT_ACCESS)) this.debugging += ConfigPro.DEBUG_IMPLICIT_ACCESS;
		if (ci.hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE)) this.debugging += ConfigPro.DEBUG_QUERY_USAGE;
		if (ci.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) this.debugging += ConfigPro.DEBUG_TEMPLATE;
		if (ci.hasDebugOptions(ConfigPro.DEBUG_THREAD)) this.debugging += ConfigPro.DEBUG_THREAD;
		if (ci.hasDebugOptions(ConfigPro.DEBUG_TIMER)) this.debugging += ConfigPro.DEBUG_TIMER;
		if (ci.hasDebugOptions(ConfigPro.DEBUG_TRACING)) this.debugging += ConfigPro.DEBUG_TRACING;

		initAntiSamyPolicyResource(pc);
		if (antiSamyPolicyResource == null) this.antiSamyPolicyResource = ((ConfigPro) config).getAntiSamyPolicy();
		// read scope cascading
		initScopeCascading();
		initSameFieldAsArray(pc);
		initWebCharset(pc);
		initAllowImplicidQueryCall();
		initLimitEvaluation();

		pc.addPageSource(component.getPageSource(), true);
		try {

			/////////// ORM /////////////////////////////////
			reinitORM(pc);

			throwsErrorWhileInit.setValue(false);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throwsErrorWhileInit.setValue(true);
			pc.removeLastPageSource(true);
		}
	}

	public void initScopeCascading() {
		Object o = get(component, KeyConstants._scopeCascading, null);
		if (o != null) {
			scopeCascading = ConfigWebUtil.toScopeCascading(Caster.toString(o, null), (short) -1);
		}
		else {
			Boolean b = Caster.toBoolean(get(component, KeyConstants._searchImplicitScopes, null), null);
			if (b != null) scopeCascading = ConfigWebUtil.toScopeCascading(b);
		}

	}

	private void initAllowImplicidQueryCall() {
		Object o = get(component, KeyConstants._searchQueries, null);
		if (o == null) o = get(component, KeyConstants._searchResults, null);

		if (o != null) allowImplicidQueryCall = Caster.toBooleanValue(o, allowImplicidQueryCall);
	}

	private void initLimitEvaluation() {
		Object o = get(component, KeyConstants._security, null);

		if (o instanceof Struct) {
			Struct sct = (Struct) o;
			o = sct.get(KeyConstants._limitEvaluation, null);
			if (o != null) limitEvaluation = Caster.toBooleanValue(o, limitEvaluation);

		}
	}

	@Override
	public short getScopeCascading() {
		if (scopeCascading == -1) return config.getScopeCascadingType();
		return scopeCascading;
	}

	@Override
	public void setScopeCascading(short scopeCascading) {
		this.scopeCascading = scopeCascading;
	}

	@Override
	public void reinitORM(PageContext pc) throws PageException {

		// datasource
		Object o = get(component, KeyConstants._datasource, null);
		if (o != null) {
			this.ormDatasource = this.defaultDataSource = AppListenerUtil.toDefaultDatasource(pc.getConfig(), o, ThreadLocalPageContext.getLog(pc, "application"));
		}

		// default datasource
		o = get(component, KeyConstants._defaultdatasource, null);
		if (o != null) {
			this.defaultDataSource = AppListenerUtil.toDefaultDatasource(pc.getConfig(), o, ThreadLocalPageContext.getLog(pc, "application"));
		}

		// ormenabled
		o = get(component, KeyConstants._ormenabled, null);
		if (o != null && Caster.toBooleanValue(o, false)) {
			this.ormEnabled = true;

			// settings
			o = get(component, KeyConstants._ormsettings, null);
			Struct settings;
			if (o instanceof Struct) settings = (Struct) o;
			else settings = new StructImpl();
			AppListenerUtil.setORMConfiguration(pc, this, settings);
		}
	}

	@Override
	public boolean hasName() {
		return true;// !StringUtil.isEmpty(getName());
	}

	@Override
	public String getName() {
		if (this.name == null) {
			this.name = Caster.toString(get(component, KeyConstants._name, ""), "");
		}
		return name;
	}

	@Override
	public int getLoginStorage() {
		if (!initLoginStorage) {
			String str = null;
			Object o = get(component, KeyConstants._loginStorage, null);
			if (o != null) {
				str = Caster.toString(o, null);
				if (str != null) loginStorage = AppListenerUtil.translateLoginStorage(str, loginStorage);
			}
			initLoginStorage = true;
		}
		return loginStorage;
	}

	@Override
	public TimeSpan getApplicationTimeout() {
		if (!initApplicationTimeout) {
			Object o = get(component, KeyConstants._applicationTimeout, null);
			if (o != null) applicationTimeout = Caster.toTimespan(o, config.getApplicationTimeout());
			else applicationTimeout = config.getApplicationTimeout();
			initApplicationTimeout = true;
		}
		return applicationTimeout;
	}

	@Override
	public TimeSpan getSessionTimeout() {
		if (!initSessionTimeout) {
			Object o = get(component, KeyConstants._sessionTimeout, null);
			if (o != null) sessionTimeout = Caster.toTimespan(o, config.getSessionTimeout());
			else sessionTimeout = config.getSessionTimeout();
			initSessionTimeout = true;
		}
		return sessionTimeout;
	}

	@Override
	public TimeSpan getClientTimeout() {
		if (!initClientTimeout) {
			Object o = get(component, KeyConstants._clientTimeout, null);
			if (o != null) clientTimeout = Caster.toTimespan(o, config.getClientTimeout());
			else clientTimeout = config.getClientTimeout();
			initClientTimeout = true;
		}
		return clientTimeout;
	}

	@Override
	public TimeSpan getRequestTimeout() {
		if (!initRequestTimeout) {
			Object o = get(component, KeyConstants._requestTimeout, null);
			if (o == null) o = get(component, KeyConstants._timeout, null);
			if (o != null) requestTimeout = Caster.toTimespan(o, config.getRequestTimeout());
			else requestTimeout = config.getRequestTimeout();
			initRequestTimeout = true;
		}
		return requestTimeout;
	}

	@Override
	public void setRequestTimeout(TimeSpan requestTimeout) {
		this.requestTimeout = requestTimeout;
		initRequestTimeout = true;
	}

	@Override
	public boolean isSetClientCookies() {
		if (!initSetClientCookies) {
			Object o = get(component, KeyConstants._setClientCookies, null);
			if (o != null) setClientCookies = Caster.toBooleanValue(o, config.isClientCookies());
			else setClientCookies = config.isClientCookies();
			initSetClientCookies = true;
		}
		return setClientCookies;
	}

	@Override
	public boolean isSetClientManagement() {
		if (!initSetClientManagement) {
			Object o = get(component, KeyConstants._clientManagement, null);
			if (o != null) setClientManagement = Caster.toBooleanValue(o, config.isClientManagement());
			else setClientManagement = config.isClientManagement();
			initSetClientManagement = true;
		}
		return setClientManagement;
	}

	@Override
	public boolean isSetDomainCookies() {
		if (!initSetDomainCookies) {
			Object o = get(component, KeyConstants._setDomainCookies, null);
			if (o != null) setDomainCookies = Caster.toBooleanValue(o, config.isDomainCookies());
			else setDomainCookies = config.isDomainCookies();
			initSetDomainCookies = true;
		}
		return setDomainCookies;
	}

	@Override
	public boolean isSetSessionManagement() {
		if (!initSetSessionManagement) {
			Object o = get(component, KeyConstants._sessionManagement, null);
			if (o != null) setSessionManagement = Caster.toBooleanValue(o, config.isSessionManagement());
			else setSessionManagement = config.isSessionManagement();
			initSetSessionManagement = true;
		}
		return setSessionManagement;
	}

	@Override
	public String getClientstorage() {
		if (!initClientStorage) {
			String str = Caster.toString(get(component, KeyConstants._clientStorage, null), null);
			if (!StringUtil.isEmpty(str)) clientStorage = str;
			initClientStorage = true;
		}
		return clientStorage;
	}

	@Override
	public int getScriptProtect() {
		if (!initScriptProtect) {
			String str = null;
			Object o = get(component, KeyConstants._scriptProtect, null);
			if (o != null) {
				str = Caster.toString(o, null);
				if (str != null) scriptProtect = AppListenerUtil.translateScriptProtect(str, 0);
				else scriptProtect = config.getScriptProtect();
			}
			else scriptProtect = config.getScriptProtect();
			initScriptProtect = true;
		}
		return scriptProtect;
	}

	@Override
	public boolean getTypeChecking() {
		if (!initTypeChecking) {
			Boolean b = Caster.toBoolean(get(component, KeyConstants._typeChecking, null), null);
			if (b != null) typeChecking = b.booleanValue();
			else typeChecking = ((ConfigPro) config).getTypeChecking();
			initTypeChecking = true;
		}
		return typeChecking;
	}

	@Override
	public boolean getAllowCompression() {
		if (!initAllowCompression) {
			Boolean b = Caster.toBoolean(get(component, KeyConstants._compression, null), null);
			if (b != null) allowCompression = b.booleanValue();
			else allowCompression = ((ConfigPro) config).allowCompression();
			initAllowCompression = true;
		}
		return allowCompression;
	}

	@Override
	public void setAllowCompression(boolean allowCompression) {
		this.allowCompression = allowCompression;
		initAllowCompression = true;
	}

	@Override
	public String getSecureJsonPrefix() {
		if (!initSecureJsonPrefix) {
			Object o = get(component, KeyConstants._secureJsonPrefix, null);
			if (o != null) secureJsonPrefix = Caster.toString(o, secureJsonPrefix);
			initSecureJsonPrefix = true;
		}
		return secureJsonPrefix;
	}

	@Override
	public boolean getSecureJson() {
		if (!initSecureJson) {
			Object o = get(component, KeyConstants._secureJson, null);
			if (o != null) secureJson = Caster.toBooleanValue(o, secureJson);
			initSecureJson = true;
		}
		return secureJson;
	}

	@Override
	public String getSessionstorage() {
		if (!initSessionStorage) {
			String str = Caster.toString(get(component, KeyConstants._sessionStorage, null), null);
			if (!StringUtil.isEmpty(str)) sessionStorage = str;
			initSessionStorage = true;
		}
		return sessionStorage;
	}

	@Override
	public boolean getSessionCluster() {
		if (!initSessionCluster) {
			Object o = get(component, KeyConstants._sessionCluster, null);
			if (o != null) sessionCluster = Caster.toBooleanValue(o, sessionCluster);
			initSessionCluster = true;
		}
		return sessionCluster;
	}

	@Override
	public boolean getClientCluster() {
		if (!initClientCluster) {
			Object o = get(component, KeyConstants._clientCluster, null);
			if (o != null) clientCluster = Caster.toBooleanValue(o, clientCluster);
			initClientCluster = true;
		}
		return clientCluster;
	}

	@Override
	public short getSessionType() {
		if (!initSessionType) {
			String str = null;
			Object o = get(component, KeyConstants._sessionType, null);
			if (o != null) {
				str = Caster.toString(o, null);
				if (str != null) sessionType = AppListenerUtil.toSessionType(str, sessionType);
			}
			initSessionType = true;
		}
		return sessionType;
	}

	@Override
	public short getWSType() {
		initWS();
		return wstype;
	}

	@Override
	public boolean getWSMaintainSession() {
		initWS();
		return wsMaintainSession;
	}

	@Override
	public void setWSMaintainSession(boolean wsMaintainSession) {
		initWS = true;
		this.wsMaintainSession = wsMaintainSession;
	}

	public void initWS() {
		if (!initWS) {
			Object o = get(component, KeyConstants._wssettings, null);
			if (o == null) o = get(component, KeyConstants._wssetting, null);
			if (o instanceof Struct) {
				Struct sct = (Struct) o;

				// type
				o = sct.get(KeyConstants._type, null);
				if (o instanceof String) {
					wstype = AppListenerUtil.toWSType(Caster.toString(o, null), WS_TYPE_AXIS1);
				}

				// MaintainSession
				o = sct.get("MaintainSession", null);
				if (o != null) {
					wsMaintainSession = Caster.toBooleanValue(o, false);
				}
			}
			initWS = true;
		}
	}

	@Override
	public void setWSType(short wstype) {
		initWS = true;
		this.wstype = wstype;
	}

	@Override
	public boolean getTriggerComponentDataMember() {
		if (!initTriggerComponentDataMember) {
			Boolean b = null;
			Object o = get(component, KeyConstants._InvokeImplicitAccessor, null);
			if (o == null) o = get(component, KeyConstants._triggerDataMember, null);
			if (o != null) {
				b = Caster.toBoolean(o, null);
				if (b != null) triggerComponentDataMember = b.booleanValue();
			}
			initTriggerComponentDataMember = true;
		}
		return triggerComponentDataMember;
	}

	@Override
	public void setTriggerComponentDataMember(boolean triggerComponentDataMember) {
		initTriggerComponentDataMember = true;
		this.triggerComponentDataMember = triggerComponentDataMember;
	}

	@Override
	public boolean getSameFieldAsArray(int scope) {
		return Scope.SCOPE_URL == scope ? sameURLFieldAsArray : sameFormFieldAsArray;
	}

	public void initSameFieldAsArray(PageContext pc) {
		ApplicationContextSupport ac = (ApplicationContextSupport) pc.getApplicationContext();
		boolean oldForm = ac.getSameFieldAsArray(Scope.SCOPE_FORM);
		boolean oldURL = ac.getSameFieldAsArray(Scope.SCOPE_URL);
		boolean oldMerge = ac.getFormUrlAsStruct();

		// Form
		Object o = get(component, KeyConstants._sameformfieldsasarray, null);
		if (o != null && Decision.isBoolean(o)) sameFormFieldAsArray = Caster.toBooleanValue(o, false);

		// URL
		o = get(component, KeyConstants._sameurlfieldsasarray, null);
		if (o != null && Decision.isBoolean(o)) sameURLFieldAsArray = Caster.toBooleanValue(o, false);

		// merge
		o = get(component, KeyConstants._formUrlAsStruct, null);
		if (o != null && Decision.isBoolean(o)) {
			formUrlAsStruct = Caster.toBooleanValue(o, true);
		}

		if (oldForm != sameFormFieldAsArray || oldMerge != formUrlAsStruct) pc.formScope().reinitialize(this);
		if (oldURL != sameURLFieldAsArray || oldMerge != formUrlAsStruct) pc.urlScope().reinitialize(this);

	}

	public void initWebCharset(PageContext pc) {
		initCharset();
		Charset cs = getWebCharset();
		// has defined a web charset
		if (cs != null) {
			if (!cs.equals(config.getWebCharset())) {
				ReqRspUtil.setContentType(pc.getHttpServletResponse(), "text/html; charset=" + cs.name());
			}
		}

	}

	@Override
	public String getDefaultCacheName(int type) {
		initCache();
		return defaultCaches.get(type);
	}

	@Override
	public Server[] getMailServers() {
		initMailServers();
		return mailServers;
	}

	private void initMailServers() {
		if (!initMailServer) {
			Key key;
			Object oMail = get(component, key = KeyConstants._mail, null);
			if (oMail == null) oMail = get(component, key = KeyConstants._mails, null);
			if (oMail == null) oMail = get(component, key = KeyConstants._mailServer, null);
			if (oMail == null) oMail = get(component, key = KeyConstants._mailServers, null);
			if (oMail == null) oMail = get(component, key = KeyConstants._smtpServerSettings, null);

			Array arrMail = Caster.toArray(oMail, null);
			// we also support a single struct instead of an array of structs
			if (arrMail == null) {
				Struct sctMail = Caster.toStruct(get(component, key, null), null);
				if (sctMail != null) {
					arrMail = new ArrayImpl();
					arrMail.appendEL(sctMail);
				}
			}
			if (arrMail != null) {
				mailServers = AppListenerUtil.toMailServers(config, arrMail, null);
			}
			initMailServer = true;
		}
	}

	@Override
	public void setMailServers(Server[] servers) {
		this.mailServers = servers;
		this.initMailServer = true;
	}

	@Override
	public CacheConnection getCacheConnection(String cacheName, CacheConnection defaultValue) {
		initCache();
		return cacheConnections.get(KeyImpl.init(cacheName));
	}

	@Override
	public Key[] getCacheConnectionNames() {
		initCache();
		Set<Key> set = cacheConnections.keySet();
		return set.toArray(new Key[set.size()]);
	}

	private void initCache() {
		if (!initCache) {
			boolean hasResource = false;
			if (defaultCaches == null) defaultCaches = new ConcurrentHashMap<Integer, String>();
			if (cacheConnections == null) cacheConnections = new ConcurrentHashMap<Collection.Key, CacheConnection>();
			Struct sctDefCache = Caster.toStruct(get(component, KeyConstants._defaultcache, null), null);
			if (sctDefCache == null) sctDefCache = Caster.toStruct(get(component, KeyConstants._cache, null), null);

			// Default
			if (sctDefCache != null) {
				// Function
				initDefaultCache(sctDefCache, Config.CACHE_TYPE_FUNCTION, KeyConstants._function);
				// Query
				initDefaultCache(sctDefCache, Config.CACHE_TYPE_QUERY, KeyConstants._query);
				// Template
				initDefaultCache(sctDefCache, Config.CACHE_TYPE_TEMPLATE, KeyConstants._template);
				// Object
				initDefaultCache(sctDefCache, Config.CACHE_TYPE_OBJECT, KeyConstants._object);
				// INCLUDE
				initDefaultCache(sctDefCache, Config.CACHE_TYPE_INCLUDE, KeyConstants._include);
				// Resource
				if (initDefaultCache(sctDefCache, Config.CACHE_TYPE_RESOURCE, KeyConstants._resource)) hasResource = true;
				// HTTP
				if (initDefaultCache(sctDefCache, Config.CACHE_TYPE_HTTP, KeyConstants._http)) hasResource = true;
				// File
				if (initDefaultCache(sctDefCache, Config.CACHE_TYPE_FILE, KeyConstants._file)) hasResource = true;
				// Webservice
				if (initDefaultCache(sctDefCache, Config.CACHE_TYPE_WEBSERVICE, KeyConstants._webservice)) hasResource = true;
			}
			// check alias inmemoryfilesystem
			if (!hasResource) {
				String str = Caster.toString(get(component, KeyConstants._inmemoryfilesystem, null), null);
				if (!StringUtil.isEmpty(str, true)) {
					defaultCaches.put(Config.CACHE_TYPE_RESOURCE, str.trim());
				}
			}

			// cache definitions
			Struct sctCache = Caster.toStruct(get(component, KeyConstants._cache, null), null);
			if (sctCache != null) {
				Iterator<Entry<Key, Object>> it = sctCache.entryIterator();

				_initCache(cacheConnections, it, false);

			}
			initCache = true;
		}
	}

	private void _initCache(Map<Key, CacheConnection> cacheConnections, Iterator<Entry<Key, Object>> it, boolean sub) {
		Entry<Key, Object> e;
		Struct sct;
		CacheConnection cc;
		while (it.hasNext()) {
			e = it.next();

			if (!sub && KeyConstants._function.equals(e.getKey()) || KeyConstants._query.equals(e.getKey()) || KeyConstants._template.equals(e.getKey())
					|| KeyConstants._object.equals(e.getKey()) || KeyConstants._include.equals(e.getKey()) || KeyConstants._resource.equals(e.getKey())
					|| KeyConstants._http.equals(e.getKey()) || KeyConstants._file.equals(e.getKey()) || KeyConstants._webservice.equals(e.getKey()))
				continue;

			if (!sub && KeyConstants._connections.equals(e.getKey())) {
				Struct _sct = Caster.toStruct(e.getValue(), null);
				if (_sct != null) _initCache(cacheConnections, _sct.entryIterator(), true);
				continue;

			}

			sct = Caster.toStruct(e.getValue(), null);
			if (sct == null) continue;

			cc = toCacheConnection(config, e.getKey().getString(), sct, null);

			if (cc != null) {
				cacheConnections.put(e.getKey(), cc);
				Key def = Caster.toKey(sct.get(KeyConstants._default, null), null);
				if (def != null) {
					String n = e.getKey().getString().trim();
					if (KeyConstants._function.equals(def)) defaultCaches.put(Config.CACHE_TYPE_FUNCTION, n);
					else if (KeyConstants._query.equals(def)) defaultCaches.put(Config.CACHE_TYPE_QUERY, n);
					else if (KeyConstants._template.equals(def)) defaultCaches.put(Config.CACHE_TYPE_TEMPLATE, n);
					else if (KeyConstants._object.equals(def)) defaultCaches.put(Config.CACHE_TYPE_OBJECT, n);
					else if (KeyConstants._include.equals(def)) defaultCaches.put(Config.CACHE_TYPE_INCLUDE, n);
					else if (KeyConstants._resource.equals(def)) defaultCaches.put(Config.CACHE_TYPE_RESOURCE, n);
					else if (KeyConstants._http.equals(def)) defaultCaches.put(Config.CACHE_TYPE_HTTP, n);
					else if (KeyConstants._file.equals(def)) defaultCaches.put(Config.CACHE_TYPE_FILE, n);
					else if (KeyConstants._webservice.equals(def)) defaultCaches.put(Config.CACHE_TYPE_WEBSERVICE, n);
				}
			}
		}
	}

	private boolean initDefaultCache(Struct data, int type, Key key) {
		Object o = data.get(key, null);
		boolean hasResource = false;
		if (o != null) {
			String name;
			Struct sct;
			CacheConnection cc;

			if (!StringUtil.isEmpty(name = Caster.toString(o, null), true)) {
				defaultCaches.put(type, name.trim());
				hasResource = true;
			}
			else if ((sct = Caster.toStruct(o, null)) != null) {
				cc = toCacheConnection(config, key.getString(), sct, null);
				if (cc != null) {
					cacheConnections.put(key, cc);
					defaultCaches.put(type, key.getString());
					hasResource = true;
				}
			}
		}
		return hasResource;
	}

	public static CacheConnection toCacheConnection(Config config, String name, Struct data, CacheConnection defaultValue) {
		try {
			return toCacheConnection(config, name, data);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static CacheConnection toCacheConnection(Config config, String name, Struct data) {
		// class definition
		ClassDefinition cd = ClassDefinitionImpl.toClassDefinitionImpl(data, null, true, config.getIdentification());

		CacheConnectionImpl cc = new CacheConnectionImpl(config, name, cd, Caster.toStruct(data.get(KeyConstants._custom, null), null),
				Caster.toBooleanValue(data.get(KeyConstants._readonly, null), false), Caster.toBooleanValue(data.get(KeyConstants._storage, null), false));
		String id = cc.id();
		CacheConnection icc = initCacheConnections.get(id);
		if (icc != null) return icc;
		try {
			Method m = cd.getClazz().getMethod("init", new Class[] { Config.class, String[].class, Struct[].class });
			if (Modifier.isStatic(m.getModifiers())) m.invoke(null, new Object[] { config, new String[] { cc.getName() }, new Struct[] { cc.getCustom() } });
			else LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_ERROR, ModernApplicationContext.class.getName(),
					"method [init(Config,String[],Struct[]):void] for class [" + cd.toString() + "] is not static");
		}
		catch (Exception e) {
		}
		initCacheConnections.put(id, cc);
		return cc;

	}

	@Override
	public void setDefaultCacheName(int type, String cacheName) {
		if (StringUtil.isEmpty(cacheName, true)) return;

		initCache();
		defaultCaches.put(type, cacheName.trim());
	}

	@Override
	public void setCacheConnection(String cacheName, CacheConnection cc) {
		if (StringUtil.isEmpty(cacheName, true)) return;
		initCache();
		cacheConnections.put(KeyImpl.init(cacheName), cc);
	}

	@Override
	public Object getMailListener() {
		if (!initMailListener) {
			Struct mail = Caster.toStruct(get(component, KeyConstants._mail, null), null);
			if (mail != null) mailListener = mail.get(KeyConstants._listener, null);

			initMailListener = true;
		}
		return mailListener;
	}

	@Override
	public TagListener getQueryListener() {
		if (!initQueryListener) {
			Struct query = Caster.toStruct(get(component, KeyConstants._query, null), null);
			if (query != null) queryListener = Query.toTagListener(query.get(KeyConstants._listener, null), null);
			initQueryListener = true;
		}
		return queryListener;
	}

	@Override
	public SerializationSettings getSerializationSettings() {
		if (!initSerializationSettings) {
			Struct sct = Caster.toStruct(get(component, KeyConstants._serialization, null), null);
			if (sct != null) {
				serializationSettings = SerializationSettings.toSerializationSettings(sct);
			}
			else serializationSettings = SerializationSettings.DEFAULT;
			initSerializationSettings = true;
		}
		return serializationSettings;
	}

	@Override
	public void setSerializationSettings(SerializationSettings settings) {
		serializationSettings = settings;
		initSerializationSettings = true;
	}

	@Override
	public Mapping[] getMappings() {
		if (!initMappings) {
			Object o = get(component, KeyConstants._mappings, null);
			if (o != null) {
				mappings = AppListenerUtil.toMappingsIgnoreInvalid(config, o, getSource());
			}
			initMappings = true;
		}
		return mappings;
	}

	@Override
	public Mapping[] getCustomTagMappings() {
		if (!initCTMappings) {
			Object o = get(component, KeyConstants._customtagpaths, null);
			if (o != null) ctmappings = AppListenerUtil.toCustomTagMappings(config, o, getSource(), ctmappings);
			initCTMappings = true;
		}
		return ctmappings;
	}

	@Override
	public Mapping[] getComponentMappings() {
		if (!initCMappings) {
			Object o = get(component, KeyConstants._componentpaths, null);
			if (o != null) cmappings = AppListenerUtil.toComponentMappings(config, o, getSource(), cmappings);
			initCMappings = true;
		}
		return cmappings;
	}

	@Override
	public List<Resource> getFunctionDirectories() {
		if (!initFuncDirs) {
			Object o = get(component, KeyConstants._functionpaths, null);
			if (o != null) funcDirs = AppListenerUtil.loadResources(config, null, o, true);
			initFuncDirs = true;
		}
		return funcDirs;
	}

	@Override
	public void setFunctionDirectories(List<Resource> resources) {
		funcDirs = resources;
		initFuncDirs = true;
	}

	@Override
	public int getLocalMode() {
		if (!initLocalMode) {
			Object o = get(component, KeyConstants._localMode, null);
			if (o != null) localMode = AppListenerUtil.toLocalMode(o, config.getLocalMode());
			else localMode = config.getLocalMode();
			initLocalMode = true;
		}
		return localMode;
	}

	@Override
	public Locale getLocale() {
		if (!initLocale) {
			Object o = get(component, KeyConstants._locale, null);
			if (o != null) {
				String str = Caster.toString(o, null);
				if (!StringUtil.isEmpty(str)) locale = LocaleFactory.getLocale(str, locale);
			}
			initLocale = true;
		}
		return locale;
	}

	private void initMonitor() {
		synchronized (KeyConstants._monitoring) {
			if (!initMonitor) {
				ConfigPro cp = (ConfigPro) config;
				Struct sct = Caster.toStruct(get(component, KeyConstants._monitoring, null), null);
				if (sct != null) {
					showDebug = Caster.toBooleanValue(sct.get(KeyConstants._showDebug, null), showDebug);
					showDoc = Caster.toBooleanValue(sct.get(KeyConstants._showDoc, null), showDoc);
					showMetric = Caster.toBooleanValue(sct.get(KeyConstants._showMetric, null), showMetric);
					showTest = Caster.toBooleanValue(sct.get(KeyConstants._showTest, null), showTest);

					// Database
					Boolean b = Caster.toBoolean(sct.get("debuggingDatabase", null), null);
					if (b != null) {
						if (b.booleanValue()) {
							if (!hasDebugOptionsNoInit(ConfigPro.DEBUG_DATABASE)) debugging += ConfigPro.DEBUG_DATABASE;
						}
						else {
							if (hasDebugOptionsNoInit(ConfigPro.DEBUG_DATABASE)) debugging -= ConfigPro.DEBUG_DATABASE;
						}
					}

					// Exception
					b = Caster.toBoolean(sct.get("debuggingException", null), null);
					if (b != null) {
						if (b.booleanValue()) {
							if (!hasDebugOptionsNoInit(ConfigPro.DEBUG_EXCEPTION)) debugging += ConfigPro.DEBUG_EXCEPTION;
						}
						else {
							if (hasDebugOptionsNoInit(ConfigPro.DEBUG_EXCEPTION)) debugging -= ConfigPro.DEBUG_EXCEPTION;
						}
					}

					// Dump
					b = Caster.toBoolean(sct.get("debuggingDump", null), null);
					if (b != null) {
						if (b.booleanValue()) {
							if (!hasDebugOptionsNoInit(ConfigPro.DEBUG_DUMP)) debugging += ConfigPro.DEBUG_DUMP;
						}
						else {
							if (hasDebugOptionsNoInit(ConfigPro.DEBUG_DUMP)) debugging -= ConfigPro.DEBUG_DUMP;
						}
					}

					// Tracing
					b = Caster.toBoolean(sct.get("debuggingTracing", null), null);
					if (b != null) {
						if (b.booleanValue()) {
							if (!hasDebugOptionsNoInit(ConfigPro.DEBUG_TRACING)) debugging += ConfigPro.DEBUG_TRACING;
						}
						else {
							if (hasDebugOptionsNoInit(ConfigPro.DEBUG_TRACING)) debugging -= ConfigPro.DEBUG_TRACING;
						}
					}

					// Timer
					b = Caster.toBoolean(sct.get("debuggingTimer", null), null);
					if (b != null) {
						if (b.booleanValue()) {
							if (!hasDebugOptionsNoInit(ConfigPro.DEBUG_TIMER)) debugging += ConfigPro.DEBUG_TIMER;
						}
						else {
							if (hasDebugOptionsNoInit(ConfigPro.DEBUG_TIMER)) debugging -= ConfigPro.DEBUG_TIMER;
						}
					}

					// ImplicitAccess
					b = Caster.toBoolean(sct.get("debuggingImplicitAccess", null), null);
					if (b != null) {
						if (b.booleanValue()) {
							if (!hasDebugOptionsNoInit(ConfigPro.DEBUG_IMPLICIT_ACCESS)) debugging += ConfigPro.DEBUG_IMPLICIT_ACCESS;
						}
						else {
							if (hasDebugOptionsNoInit(ConfigPro.DEBUG_IMPLICIT_ACCESS)) debugging -= ConfigPro.DEBUG_IMPLICIT_ACCESS;
						}
					}

					// QueryUsage
					b = Caster.toBoolean(sct.get("debuggingQueryUsage", null), null);
					if (b != null) {
						if (b.booleanValue()) {
							if (!hasDebugOptionsNoInit(ConfigPro.DEBUG_QUERY_USAGE)) debugging += ConfigPro.DEBUG_QUERY_USAGE;
						}
						else {
							if (hasDebugOptionsNoInit(ConfigPro.DEBUG_QUERY_USAGE)) debugging -= ConfigPro.DEBUG_QUERY_USAGE;
						}
					}

					// Thread
					b = Caster.toBoolean(sct.get("debuggingThread", null), null);
					if (b != null) {
						if (b.booleanValue()) {
							if (!hasDebugOptionsNoInit(ConfigPro.DEBUG_THREAD)) debugging += ConfigPro.DEBUG_THREAD;
						}
						else {
							if (hasDebugOptionsNoInit(ConfigPro.DEBUG_THREAD)) debugging -= ConfigPro.DEBUG_THREAD;
						}
					}

					// Template
					b = Caster.toBoolean(sct.get("debuggingTemplate", null), null);
					if (b != null) {
						if (b.booleanValue()) {
							if (!hasDebugOptionsNoInit(ConfigPro.DEBUG_TEMPLATE)) debugging += ConfigPro.DEBUG_TEMPLATE;
						}
						else {
							if (hasDebugOptionsNoInit(ConfigPro.DEBUG_TEMPLATE)) debugging -= ConfigPro.DEBUG_TEMPLATE;
						}
					}
				}
				initMonitor = true;
			}
		}
	}

	@Override
	public boolean getShowDoc() {
		if (!initMonitor) initMonitor();
		return showDoc;
	}

	@Override
	public boolean getShowDebug() {
		if (!initMonitor) initMonitor();
		return showDebug;
	}

	@Override
	public boolean getShowMetric() {
		if (!initMonitor) initMonitor();
		return showMetric;
	}

	@Override
	public boolean getShowTest() {
		if (!initMonitor) initMonitor();
		return showTest;
	}

	@Override
	public void setShowDebug(boolean b) {
		if (!initMonitor) initMonitor();
		showDebug = b;
	}

	@Override
	public void setShowDoc(boolean b) {
		if (!initMonitor) initMonitor();
		showDoc = b;
	}

	@Override
	public void setShowMetric(boolean b) {
		if (!initMonitor) initMonitor();
		showMetric = b;
	}

	@Override
	public void setShowTest(boolean b) {
		if (!initMonitor) initMonitor();
		showTest = b;
	}

	@Override
	public int getDebugOptions() {
		if (!initMonitor) initMonitor();
		return debugging;
	}

	@Override
	public boolean hasDebugOptions(int option) {
		if (!initMonitor) initMonitor();
		return (debugging & option) > 0;
	}

	public boolean hasDebugOptionsNoInit(int option) {
		return (debugging & option) > 0;
	}

	@Override
	public void setDebugOptions(int option) {
		if (!hasDebugOptions(option)) debugging += option;
	}

	@Override
	public void remDebugOptions(int option) {
		if (hasDebugOptions(option)) debugging -= option;
	}

	@Override
	public TimeZone getTimeZone() {
		if (!initTimeZone) {
			Object o = get(component, KeyConstants._timezone, null);
			if (o != null) {
				String str = Caster.toString(o, null);
				if (!StringUtil.isEmpty(str)) timeZone = TimeZoneUtil.toTimeZone(str, config.getTimeZone());
				else timeZone = config.getTimeZone();
			}
			else timeZone = config.getTimeZone();
			initTimeZone = true;
		}
		return timeZone;
	}

	@Override
	public Charset getWebCharset() {
		if (!initWebCharset) initCharset();
		return CharsetUtil.toCharset(webCharset);
	}

	public CharSet getWebCharSet() {
		if (!initWebCharset) initCharset();
		return webCharset;
	}

	@Override
	public Resource getAntiSamyPolicyResource() {
		return antiSamyPolicyResource;
	}

	@Override
	public void setAntiSamyPolicyResource(Resource res) {
		antiSamyPolicyResource = res;
	}

	public void initAntiSamyPolicyResource(PageContext pc) {
		Struct sct = Caster.toStruct(get(component, KeyConstants._security, null), null);
		if (sct != null) {
			Resource tmp = ResourceUtil.toResourceExisting(pc, Caster.toString(sct.get("antisamypolicy", null), null), true, null);
			if (tmp != null) antiSamyPolicyResource = tmp;
		}
	}

	@Override
	public Charset getResourceCharset() {
		if (!initResourceCharset) initCharset();
		return CharsetUtil.toCharset(resourceCharset);
	}

	public CharSet getResourceCharSet() {
		if (!initResourceCharset) initCharset();
		return resourceCharset;
	}

	/**
	 * @return webcharset if it was defined, otherwise null
	 */
	private CharSet initCharset() {
		webCharset = ((ConfigPro) config).getWebCharSet();
		resourceCharset = ((ConfigPro) config).getResourceCharSet();
		Object o = get(component, KeyConstants._charset, null);
		if (o != null) {
			Struct sct = Caster.toStruct(o, null);
			if (sct != null) {
				CharSet web = CharsetUtil.toCharSet(Caster.toString(sct.get(KeyConstants._web, null), null), null);
				if (web != null) webCharset = web;

				CharSet res = CharsetUtil.toCharSet(Caster.toString(sct.get(KeyConstants._resource, null), null), null);
				if (res != null) resourceCharset = res;

				initWebCharset = true;
				initResourceCharset = true;
				return web;
			}
		}
		else {

		}
		initWebCharset = true;
		initResourceCharset = true;
		return null;
	}

	@Override
	public boolean getBufferOutput() {
		if (!initBufferOutput) {
			Object o = get(component, KeyConstants._bufferOutput, null);
			if (o != null) bufferOutput = Caster.toBooleanValue(o, ((ConfigPro) config).getBufferOutput());
			else bufferOutput = ((ConfigPro) config).getBufferOutput();
			initBufferOutput = true;
		}
		return bufferOutput;
	}

	@Override
	public boolean getSuppressContent() {
		if (!initSuppressContent) {
			Object o = get(component, KeyConstants._suppressRemoteComponentContent, null);
			if (o != null) suppressContent = Caster.toBooleanValue(o, suppressContent);
			initSuppressContent = true;
		}
		return suppressContent;
	}

	@Override
	public void setSuppressContent(boolean suppressContent) {
		this.suppressContent = suppressContent;
		initSuppressContent = true;
	}

	@Override
	public lucee.runtime.net.s3.Properties getS3() {
		if (!initS3) {
			Object o = get(component, KeyConstants._s3, null);
			if (o != null && Decision.isStruct(o)) s3 = AppListenerUtil.toS3(Caster.toStruct(o, null));
			initS3 = true;
		}
		return s3;
	}

	@Override
	public FTPConnectionData getFTP() {
		if (!initFTP) {
			Object o = get(component, KeyConstants._ftp, null);
			if (o != null && Decision.isStruct(o)) ftp = AppListenerUtil.toFTP(Caster.toStruct(o, null));
			initFTP = true;
		}
		return ftp;
	}

	@Override
	public String getDefaultDataSource() {
		throw new PageRuntimeException(new DeprecatedException("this method is no longer supported!"));
	}

	@Override
	public Object getDefDataSource() {
		return defaultDataSource;
	}

	@Override
	public DataSource[] getDataSources() {
		if (!initDataSources) {
			Object o = get(component, KeyConstants._datasources, null);
			// if "this.datasources" does not exists, check if "this.datasource" exists and contains a struct
			/*
			 * if(o==null){ o = get(component,KeyConstants._datasource,null); if(!Decision.isStruct(o)) o=null;
			 * }
			 */

			if (o != null) dataSources = AppListenerUtil.toDataSources(config, o, dataSources, ThreadLocalPageContext.getLog(config, "application"));

			initDataSources = true;
		}
		return dataSources;
	}

	@Override
	public boolean isORMEnabled() {
		return this.ormEnabled;
	}

	@Override
	public String getORMDatasource() {
		throw new PageRuntimeException(new DeprecatedException("this method is no longer supported!"));
	}

	@Override
	public Object getORMDataSource() {
		return ormDatasource;
	}

	@Override
	public ORMConfiguration getORMConfiguration() {
		return ormConfig;
	}

	public Component getComponent() {
		return component;
	}

	public Object getCustom(Key key) {
		try {
			ComponentSpecificAccess cw = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, component);
			return cw.get(key, null);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		return null;
	}

	private static Object get(Component app, Key name, String defaultValue) {
		Member mem = app.getMember(Component.ACCESS_PRIVATE, name, true, false);
		if (mem == null) return defaultValue;
		return mem.getValue();
	}

	//////////////////////// SETTERS /////////////////////////

	@Override
	public void setApplicationTimeout(TimeSpan applicationTimeout) {
		initApplicationTimeout = true;
		this.applicationTimeout = applicationTimeout;
	}

	@Override
	public void setSessionTimeout(TimeSpan sessionTimeout) {
		initSessionTimeout = true;
		this.sessionTimeout = sessionTimeout;
	}

	@Override
	public void setClientTimeout(TimeSpan clientTimeout) {
		initClientTimeout = true;
		this.clientTimeout = clientTimeout;
	}

	@Override
	public void setClientstorage(String clientstorage) {
		initClientStorage = true;
		this.clientStorage = clientstorage;
	}

	@Override
	public void setSessionstorage(String sessionstorage) {
		initSessionStorage = true;
		this.sessionStorage = sessionstorage;
	}

	@Override
	public void setCustomTagMappings(Mapping[] customTagMappings) {
		initCTMappings = true;
		this.ctmappings = customTagMappings;
	}

	@Override
	public void setComponentMappings(Mapping[] componentMappings) {
		initCMappings = true;
		this.cmappings = componentMappings;
	}

	@Override
	public void setMappings(Mapping[] mappings) {
		initMappings = true;
		this.mappings = mappings;
	}

	@Override
	public void setMailListener(Object mailListener) {
		initMailListener = true;
		this.mailListener = mailListener;
	}

	@Override
	public void setQueryListener(TagListener listener) {
		initQueryListener = true;
		this.queryListener = listener;
	}

	@Override
	public void setDataSources(DataSource[] dataSources) {
		initDataSources = true;
		this.dataSources = dataSources;
	}

	@Override
	public void setLoginStorage(int loginStorage) {
		initLoginStorage = true;
		this.loginStorage = loginStorage;
	}

	@Override
	public void setDefaultDataSource(String datasource) {
		this.defaultDataSource = datasource;
	}

	@Override
	public void setDefDataSource(Object datasource) {
		this.defaultDataSource = datasource;
	}

	@Override
	public void setScriptProtect(int scriptrotect) {
		initScriptProtect = true;
		this.scriptProtect = scriptrotect;
	}

	@Override
	public void setTypeChecking(boolean typeChecking) {
		initTypeChecking = true;
		this.typeChecking = typeChecking;
	}

	@Override
	public void setSecureJson(boolean secureJson) {
		initSecureJson = true;
		this.secureJson = secureJson;
	}

	@Override
	public void setSecureJsonPrefix(String secureJsonPrefix) {
		initSecureJsonPrefix = true;
		this.secureJsonPrefix = secureJsonPrefix;
	}

	@Override
	public void setSetClientCookies(boolean setClientCookies) {
		initSetClientCookies = true;
		this.setClientCookies = setClientCookies;
	}

	@Override
	public void setSetClientManagement(boolean setClientManagement) {
		initSetClientManagement = true;
		this.setClientManagement = setClientManagement;
	}

	@Override
	public void setSetDomainCookies(boolean setDomainCookies) {
		initSetDomainCookies = true;
		this.setDomainCookies = setDomainCookies;
	}

	@Override
	public void setSetSessionManagement(boolean setSessionManagement) {
		initSetSessionManagement = true;
		this.setSessionManagement = setSessionManagement;
	}

	@Override
	public void setLocalMode(int localMode) {
		initLocalMode = true;
		this.localMode = localMode;
	}

	@Override
	public void setLocale(Locale locale) {
		initLocale = true;
		this.locale = locale;
	}

	@Override
	public void setTimeZone(TimeZone timeZone) {
		initTimeZone = true;
		this.timeZone = timeZone;
	}

	@Override
	public void setWebCharset(Charset webCharset) {
		initWebCharset = true;
		this.webCharset = CharsetUtil.toCharSet(webCharset);
	}

	@Override
	public void setResourceCharset(Charset resourceCharset) {
		initResourceCharset = true;
		this.resourceCharset = CharsetUtil.toCharSet(resourceCharset);
	}

	@Override
	public void setBufferOutput(boolean bufferOutput) {
		initBufferOutput = true;
		this.bufferOutput = bufferOutput;
	}

	@Override
	public void setSessionType(short sessionType) {
		initSessionType = true;
		this.sessionType = sessionType;
	}

	@Override
	public void setClientCluster(boolean clientCluster) {
		initClientCluster = true;
		this.clientCluster = clientCluster;
	}

	@Override
	public void setSessionCluster(boolean sessionCluster) {
		initSessionCluster = true;
		this.sessionCluster = sessionCluster;
	}

	@Override
	public void setS3(Properties s3) {
		initS3 = true;
		this.s3 = s3;
	}

	@Override
	public void setFTP(FTPConnectionData ftp) {
		initFTP = true;
		this.ftp = ftp;
	}

	@Override
	public void setORMEnabled(boolean ormEnabled) {
		this.ormEnabled = ormEnabled;
	}

	@Override
	public void setORMConfiguration(ORMConfiguration ormConfig) {
		this.ormConfig = ormConfig;
	}

	@Override
	public void setORMDatasource(String ormDatasource) {
		this.ormDatasource = ormDatasource;
	}

	@Override
	public void setORMDataSource(Object ormDatasource) {
		this.ormDatasource = ormDatasource;
	}

	@Override
	public Resource getSource() {
		return component.getPageSource().getResource();
	}

	@Override
	public RestSettings getRestSettings() {
		initRest();
		return restSetting;
	}

	@Override
	public Resource[] getRestCFCLocations() {
		initRest();
		return restCFCLocations;
	}

	private void initRest() {
		if (!initRestSetting) {
			Object o = get(component, KeyConstants._restsettings, null);
			if (o != null && Decision.isStruct(o)) {
				Struct sct = Caster.toStruct(o, null);

				// cfclocation
				Object obj = sct.get(KeyConstants._cfcLocation, null);
				if (obj == null) obj = sct.get(KeyConstants._cfcLocations, null);
				List<Resource> list = AppListenerUtil.loadResources(config, null, obj, true);
				restCFCLocations = list == null ? null : list.toArray(new Resource[list.size()]);

				// skipCFCWithError
				boolean skipCFCWithError = Caster.toBooleanValue(sct.get(KeyConstants._skipCFCWithError, null), restSetting.getSkipCFCWithError());

				// returnFormat
				int returnFormat = Caster.toIntValue(sct.get(KeyConstants._returnFormat, null), restSetting.getReturnFormat());

				restSetting = new RestSettingImpl(skipCFCWithError, returnFormat);

			}
			initRestSetting = true;
		}
	}

	@Override
	public void setJavaSettings(JavaSettings javaSettings) {
		initClassLoader = false;
		this.javaSettings = javaSettings;
	}

	@Override
	public ClassLoader getRPCClassLoader() throws IOException {
		if (!initClassLoader) {
			// PATCH to avoid cycle
			if (initClassLoaderBefore) {
				return getDefaultClassLoader(config);
			}
			initClassLoaderBefore = true;
			cl = getDefaultClassLoader(config);
			Object o = javaSettings != null ? null : get(component, KeyConstants._javasettings, null);
			if (javaSettings != null || (o != null && Decision.isStruct(o))) {
				if (javaSettings == null) javaSettings = JavaSettingsImpl.getInstance(config, Caster.toStruct(o, null), null);
				cl = ((ConfigPro) config).getRPCClassLoader(false, javaSettings, cl);
			}

			initClassLoader = true;
			initClassLoaderBefore = false;
		}
		return cl;
	}

	@Override
	public JavaSettings getJavaSettings() {
		try {
			getRPCClassLoader();
		}
		catch (IOException e) {
		}
		if (javaSettings == null) return ((ConfigPro) config).getJavaSettings();
		return javaSettings;
	}

	public static ClassLoader getDefaultClassLoader(ConfigWeb config) throws IOException {
		if (defaultClassLoader == null) {
			synchronized (token) {
				if (defaultClassLoader == null) {
					defaultClassLoader = ((ConfigPro) config).getRPCClassLoader(false, ((ConfigPro) config).getJavaSettings(), null);
				}
			}
		}
		return defaultClassLoader;
	}

	@Override
	public Map<Collection.Key, Object> getTagAttributeDefaultValues(PageContext pc, String tagClassName) {
		if (!initDefaultAttributeValues) {
			// this.tag.<tagname>.<attribute-name>=<value>
			Struct sct = Caster.toStruct(get(component, KeyConstants._tag, null), null);
			if (sct != null) {
				setTagAttributeDefaultValues(pc, sct);
			}
			initDefaultAttributeValues = true;
		}
		return super.getTagAttributeDefaultValues(pc, tagClassName);
	}

	@Override
	public void setTagAttributeDefaultValues(PageContext pc, Struct sct) {
		initDefaultAttributeValues = true;
		super.setTagAttributeDefaultValues(pc, sct);
	}

	@Override
	public CustomType getCustomType(String strType) {
		if (!initCustomTypes) {
			if (customTypes == null) customTypes = new HashMap<String, CustomType>();

			// this.type.susi=function(any value){};
			Struct sct = Caster.toStruct(get(component, KeyConstants._type, null), null);
			if (sct != null) {
				Iterator<Entry<Key, Object>> it = sct.entryIterator();
				Entry<Key, Object> e;
				UDF udf;
				while (it.hasNext()) {
					e = it.next();
					udf = Caster.toFunction(e.getValue(), null);
					if (udf != null) customTypes.put(e.getKey().getLowerString(), new UDFCustomType(udf));
				}
			}
			initCustomTypes = true;
		}
		return customTypes.get(strType.trim().toLowerCase());
	}

	@Override
	public Object getCachedWithin(int type) {
		if (!initCachedWithins) {
			Struct sct = Caster.toStruct(get(component, KeyConstants._cachedWithin, null), null);
			if (sct != null) {
				Iterator<Entry<Key, Object>> it = sct.entryIterator();
				Entry<Key, Object> e;
				Object v;
				int k;
				while (it.hasNext()) {
					e = it.next();
					k = AppListenerUtil.toCachedWithinType(e.getKey().getString(), -1);
					v = e.getValue();
					if (k != -1 && !StringUtil.isEmpty(v)) setCachedWithin(k, v);
				}
			}
			sct = null;
			// also support this.tag.include... as second chance
			if (super.getCachedWithin(Config.CACHEDWITHIN_INCLUDE) == null) {
				sct = Caster.toStruct(get(component, KeyConstants._tag, null), null);
				if (sct != null) {
					Object obj = sct.get(KeyConstants._include, null);
					if (Decision.isCastableToStruct(obj)) {
						Struct tmp = Caster.toStruct(obj, null);
						obj = tmp == null ? null : tmp.get("cachedWithin", null);
						if (!StringUtil.isEmpty(obj)) setCachedWithin(Config.CACHEDWITHIN_INCLUDE, obj);
					}
				}
			}

			// also support this.tag.function... as second chance
			if (super.getCachedWithin(Config.CACHEDWITHIN_FUNCTION) == null) {
				if (sct == null) sct = Caster.toStruct(get(component, KeyConstants._tag, null), null);
				if (sct != null) {
					Object obj = sct.get(KeyConstants._function, null);
					if (Decision.isCastableToStruct(obj)) {
						Struct tmp = Caster.toStruct(obj, null);
						obj = tmp == null ? null : tmp.get("cachedWithin", null);
						if (!StringUtil.isEmpty(obj)) setCachedWithin(Config.CACHEDWITHIN_FUNCTION, obj);
					}
				}
			}

			initCachedWithins = true;
		}
		return super.getCachedWithin(type);
	}

	@Override
	public boolean getCGIScopeReadonly() {
		if (!initCGIScopeReadonly) {
			Object o = get(component, KeyConstants._CGIReadOnly, null);
			if (o != null) cgiScopeReadonly = Caster.toBooleanValue(o, cgiScopeReadonly);
			initCGIScopeReadonly = true;
		}
		return cgiScopeReadonly;
	}

	@Override
	public void setCGIScopeReadonly(boolean cgiScopeReadonly) {
		initCGIScopeReadonly = true;
		this.cgiScopeReadonly = cgiScopeReadonly;
	}

	@Override
	public boolean getFormUrlAsStruct() {
		return formUrlAsStruct;
	}

	@Override
	public String getBlockedExtForFileUpload() {
		if (!initBlockedExtForFileUpload) {
			Object o = get(component, KeyConstants._blockedExtForFileUpload, null);
			blockedExtForFileUpload = Caster.toString(o, null);
			initBlockedExtForFileUpload = true;
		}
		return blockedExtForFileUpload;
	}

	@Override
	public SessionCookieData getSessionCookie() {
		if (!initSessionCookie) {
			Struct sct = Caster.toStruct(get(component, KeyConstants._sessioncookie, null), null);
			if (sct != null) sessionCookie = AppListenerUtil.toSessionCookie(config, sct);
			initSessionCookie = true;
		}
		return sessionCookie;
	}

	@Override
	public AuthCookieData getAuthCookie() {
		if (!initAuthCookie) {
			Struct sct = Caster.toStruct(get(component, KeyConstants._authcookie, null), null);
			if (sct != null) authCookie = AppListenerUtil.toAuthCookie(config, sct);
			initAuthCookie = true;
		}
		return authCookie;
	}

	@Override
	public void setSessionCookie(SessionCookieData data) {
		sessionCookie = data;
		initSessionCookie = true;
	}

	@Override
	public void setAuthCookie(AuthCookieData data) {
		authCookie = data;
		initAuthCookie = true;
	}

	@Override
	public void setLoggers(Map<Key, Pair<Log, Struct>> logs) {
		this.logs = logs;
		initLog = true;
	}

	@Override
	public Log getLog(String name) {
		if (!initLog) initLog();
		Pair<Log, Struct> pair = logs.get(KeyImpl.init(StringUtil.emptyIfNull(name)));
		if (pair == null) return null;
		return pair.getName();
	}

	@Override
	public Struct getLogMetaData(String name) {
		if (!initLog) initLog();
		Pair<Log, Struct> pair = logs.get(KeyImpl.init(StringUtil.emptyIfNull(name)));
		if (pair == null) return null;
		return (Struct) pair.getValue().duplicate(false);
	}

	@Override
	public java.util.Collection<Collection.Key> getLogNames() {
		if (!initLog) initLog();
		return logs.keySet();
	}

	private void initLog() {
		try {
			// appender
			Object oLogs = get(component, KeyConstants._logs, null);
			if (oLogs == null) oLogs = get(component, KeyConstants._log, null);
			Struct sct = Caster.toStruct(oLogs, null);
			logs = initLog(ThreadLocalPageContext.getConfig(config), sct);
			initLog = true;
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}

	public static void releaseInitCacheConnections() {
		if (initCacheConnections != null) {
			for (CacheConnection cc: initCacheConnections.values()) {
				CacheUtil.releaseEL(cc);
			}
		}
	}

	@Override
	public boolean getFullNullSupport() {
		if (!initFullNullSupport) {
			Boolean b = Caster.toBoolean(get(component, KeyConstants._nullSupport, null), null);
			if (b == null) b = Caster.toBoolean(get(component, KeyConstants._enableNULLSupport, null), null);
			if (b != null) fullNullSupport = b.booleanValue();

			initFullNullSupport = true;
		}
		return fullNullSupport;
	}

	@Override
	public void setFullNullSupport(boolean fullNullSupport) {
		this.fullNullSupport = fullNullSupport;
		this.initFullNullSupport = true;
	}

	@Override
	public boolean getPreciseMath() {
		if (!initPreciseMath) {
			Boolean b = Caster.toBoolean(get(component, KeyConstants._preciseMath, null), null);
			if (b == null) b = Caster.toBoolean(get(component, KeyConstants._precisionEvaluate, null), null);
			if (b != null) preciseMath = b.booleanValue();

			initPreciseMath = true;
		}
		return preciseMath;
	}

	@Override
	public void setPreciseMath(boolean preciseMath) {
		this.preciseMath = preciseMath;
		this.initPreciseMath = true;
	}

	@Override
	public int getReturnFormat() {
		if (!initReturnFormat) {
			String str = Caster.toString(get(component, KeyConstants._returnFormat, null), null);
			if (!StringUtil.isEmpty(str, true)) {
				setReturnFormat(UDFUtil.toReturnFormat(str.trim(), -1));
			}
			initReturnFormat = true;
		}
		return returnFormat;
	}

	@Override
	public void setReturnFormat(int returnFormat) {
		if (ComponentPageImpl.isValid(returnFormat)) {
			this.returnFormat = returnFormat;
			this.initReturnFormat = true;
		}

	}

	@Override
	public boolean getQueryPSQ() {
		if (!initQueryPSQ) {
			Struct qry = Caster.toStruct(get(component, KeyConstants._query, null), null);
			if (qry != null) {
				Boolean b = Caster.toBoolean(qry.get(KeyConstants._psq, null), null);
				if (b == null) b = Caster.toBoolean(qry.get(KeyConstants._preservesinglequote, null), null);
				if (b != null) queryPSQ = b.booleanValue();
			}
			initQueryPSQ = true;
		}
		return queryPSQ;
	}

	@Override
	public void setQueryPSQ(boolean psq) {
		this.queryPSQ = psq;
		this.initQueryPSQ = true;
	}

	@Override
	public TimeSpan getQueryCachedAfter() {
		if (!initQueryCacheAfter) {
			Struct qry = Caster.toStruct(get(component, KeyConstants._query, null), null);
			if (qry != null) {
				TimeSpan ts = Caster.toTimespan(qry.get(KeyConstants._cachedAfter, null), null);
				if (ts != null) queryCachedAfter = ts;
			}
			initQueryCacheAfter = true;
		}
		return queryCachedAfter;
	}

	@Override
	public void setQueryCachedAfter(TimeSpan ts) {
		this.queryCachedAfter = ts;
		this.initQueryCacheAfter = true;
	}

	@Override
	public int getQueryVarUsage() {
		if (!initQueryVarUsage) {
			Struct sct = Caster.toStruct(get(component, KeyConstants._query, null), null);
			if (sct == null) sct = Caster.toStruct(get(component, KeyConstants._security, null), null);
			if (sct != null) {
				String str = Caster.toString(sct.get(KeyConstants._varusage, null), null);
				if (StringUtil.isEmpty(str)) str = Caster.toString(sct.get(KeyConstants._variableusage, null), null);
				if (!StringUtil.isEmpty(str)) queryVarUsage = AppListenerUtil.toVariableUsage(str, queryVarUsage);
			}
			initQueryVarUsage = true;
		}
		return queryVarUsage;
	}

	@Override
	public void setQueryVarUsage(int varUsage) {
		this.queryVarUsage = varUsage;
		this.initQueryVarUsage = true;
	}

	@Override
	public ProxyData getProxyData() {
		if (!initProxyData) {
			Struct sct = Caster.toStruct(get(component, KeyConstants._proxy, null), null);
			proxyData = ProxyDataImpl.toProxyData(sct, null);
			initProxyData = true;
		}
		return proxyData;
	}

	@Override
	public void setProxyData(ProxyData data) {
		this.proxyData = data;
		this.initProxyData = true;
	}

	@Override
	public Struct getXmlFeatures() {
		if (!initXmlFeatures) {
			Struct sct = Caster.toStruct(get(component, KeyConstants._xmlFeatures, null), null);
			if (sct != null) xmlFeatures = sct;
			initXmlFeatures = true;
		}
		return xmlFeatures;
	}

	@Override
	public void setXmlFeatures(Struct xmlFeatures) {
		this.xmlFeatures = xmlFeatures;
	}

	@Override
	public boolean getLimitEvaluation() {
		return limitEvaluation;
	}

	@Override
	public void setLimitEvaluation(boolean limitEvaluation) {
		this.limitEvaluation = limitEvaluation;
	}

	@Override
	public boolean getAllowImplicidQueryCall() {
		return allowImplicidQueryCall;
	}

	@Override
	public void setAllowImplicidQueryCall(boolean allowImplicidQueryCall) {
		this.allowImplicidQueryCall = allowImplicidQueryCall;
	}

	@Override
	public Regex getRegex() {
		if (!initRegex) {

			Struct sct = Caster.toStruct(get(component, KeyConstants._regex, null), null);
			boolean has = false;
			if (sct != null) {
				String str = Caster.toString(sct.get(KeyConstants._engine, null), null);
				if (StringUtil.isEmpty(str, true)) str = Caster.toString(sct.get(KeyConstants._type, null), null);
				if (StringUtil.isEmpty(str, true)) str = Caster.toString(sct.get(KeyConstants._dialect, null), null);
				if (!StringUtil.isEmpty(str, true)) {
					int type = RegexFactory.toType(str, -1);
					if (type != -1) {
						Regex tmp = RegexFactory.toRegex(type, null);
						if (tmp != null) {
							has = true;
							regex = tmp;
						}
					}
				}
			}
			if (!has) {
				Boolean res = Caster.toBoolean(get(component, KeyConstants._useJavaAsRegexEngine, null), null);
				if (res != null) regex = RegexFactory.toRegex(res.booleanValue());
			}
			initRegex = true;
		}
		return regex;
	}

	@Override
	public void setRegex(Regex regex) {
		this.regex = regex;
	}
}