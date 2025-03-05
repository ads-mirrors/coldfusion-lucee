package lucee.runtime.config;

import java.io.IOException;
import java.util.Collection;

import org.osgi.framework.BundleException;
import org.xml.sax.SAXException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lucee.commons.lock.KeyLock;
import lucee.runtime.CIPage;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.compiler.CFMLCompilerImpl;
import lucee.runtime.debug.DebuggerPool;
import lucee.runtime.exp.PageException;
import lucee.runtime.gateway.GatewayEngine;
import lucee.runtime.monitor.ActionMonitorCollector;
import lucee.runtime.net.rpc.WSHandler;
import lucee.runtime.tag.TagHandlerPool;
import lucee.runtime.writer.CFMLWriter;

//FUTURE add to Config
public interface ConfigWebPro extends ConfigWeb, ConfigPro {

	public Mapping getApplicationMapping(String type, String virtual, String physical, String archive, boolean physicalFirst, boolean ignoreVirtual);

	public Collection<Mapping> getServerFunctionMappings();

	public Mapping getServerFunctionMapping(String mappingName);

	public Collection<Mapping> getServerTagMappings();

	public Mapping getServerTagMapping(String mappingName);

	public WSHandler getWSHandler() throws PageException;

	public GatewayEngine getGatewayEngine() throws PageException;

	public CFMLCompilerImpl getCompiler();

	public Mapping getApplicationMapping(String type, String virtual, String physical, String archive, boolean physicalFirst, boolean ignoreVirtual,
			boolean checkPhysicalFromWebroot, boolean checkArchiveFromWebroot);

	public Mapping[] getApplicationMappings();

	public boolean isApplicationMapping(Mapping mapping);

	public CIPage getBaseComponentPage(PageContext pc) throws PageException;

	public void resetBaseComponentPage();

	public ActionMonitorCollector getActionMonitorCollector();

	public KeyLock<String> getContextLock();

	public void releaseCacheHandlers(PageContext pc);

	public DebuggerPool getDebuggerPool();

	public CFMLWriter getCFMLWriter(PageContext pc, HttpServletRequest req, HttpServletResponse rsp);

	public TagHandlerPool getTagHandlerPool();

	public String getHash();

	public void updatePassword(boolean server, String passwordOld, String passwordNew) throws PageException, IOException, SAXException, BundleException;

	public Password updatePasswordIfNecessary(boolean server, String passwordRaw);

	public void resetServerFunctionMappings();

	// public boolean isSingle();

	public ServletConfig getServletConfig();

	public void setIdentification(IdentificationWeb id);

	public void checkMappings();
}
