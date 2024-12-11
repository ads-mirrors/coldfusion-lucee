/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.aprint;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.ParentThreasRefThread;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.config.ConfigAdmin;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.lock.LockManagerImpl;
import lucee.runtime.net.smtp.SMTPConnectionPool;
import lucee.runtime.op.Caster;
import lucee.runtime.schedule.Scheduler;
import lucee.runtime.schedule.SchedulerImpl;
import lucee.runtime.type.scope.storage.StorageScopeFile;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.dynamic.DynamicInvoker;

/**
 * own thread how check the main thread and his data
 */
public final class Controler extends ParentThreasRefThread {

	private static final long TIMEOUT = 50 * 1000;

	private static final ControllerState INACTIVE = new ControllerStateImpl(false);

	private int interval;
	private long lastMinuteInterval = System.currentTimeMillis() - (1000 * 59); // first after a second
	private long last5MinuteInterval = System.currentTimeMillis() - (1000 * 299); // first after a second
	private long last10SecondsInterval = System.currentTimeMillis() - (1000 * 9); // first after a second
	private long lastHourInterval = System.currentTimeMillis();

	private final Map contextes;
	// private ScheduleThread scheduleThread;
	private final ConfigServer configServer;
	// private final ShutdownHook shutdownHook;
	private ControllerState state;

	private boolean poolValidate;

	/**
	 * @param contextes
	 * @param interval
	 * @param run
	 */
	public Controler(ConfigServer configServer, Map contextes, int interval, ControllerState state) {
		this.contextes = contextes;
		this.interval = interval;
		this.state = state;
		this.configServer = configServer;
		this.poolValidate = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.datasource.pool.validate", null), true);
		// shutdownHook=new ShutdownHook(configServer);
		// Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	private static class ControlerThread extends ParentThreasRefThread {
		private Controler controler;
		private CFMLFactoryImpl[] factories;
		private boolean firstRun;
		private long done = -1;
		private Throwable t;
		private Log log;
		private long start;

		public ControlerThread(Controler controler, CFMLFactoryImpl[] factories, boolean firstRun, Log log) {
			this.start = System.currentTimeMillis();
			this.controler = controler;
			this.factories = factories;
			this.firstRun = firstRun;
			this.log = log;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			try {
				controler.control(factories, firstRun, log);
				done = System.currentTimeMillis() - start;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				this.t = t;
			}
			// long time=System.currentTimeMillis()-start;
			// if(time>10000) {
			// log.info("controller", "["+hashCode()+"] controller was running for "+time+"ms");
			// }
		}
	}

	@Override
	public void run() {
		// scheduleThread.start();
		boolean firstRun = true;
		boolean dump = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.dump.threads", null), false);
		long count = 0;
		List<ControlerThread> threads = new ArrayList<ControlerThread>();
		CFMLFactoryImpl factories[] = null;
		while (state.active()) {
			if (dump) dumpThreads();
			// sleep
			SystemUtil.wait(this, interval);
			if (!state.active()) break;

			factories = toFactories(factories, contextes);
			// start the thread that calls control
			ControlerThread ct = new ControlerThread(this, factories, firstRun, configServer.getLog("application"));
			ct.setName("ControllerThread:" + (++count));
			ct.start();
			threads.add(ct);

			if (threads.size() > 10 && lastMinuteInterval + 60000 < System.currentTimeMillis())
				configServer.getLog("application").info("controller", threads.size() + " active controller threads");

			// now we check all threads we have
			Iterator<ControlerThread> it = threads.iterator();
			long time;
			while (it.hasNext()) {
				ct = it.next();
				// print.e(ct.hashCode());
				time = System.currentTimeMillis() - ct.start;
				// done
				if (ct.done >= 0) {
					if (time > 10000) configServer.getLog("application").info("controller", "controller took " + ct.done + "ms to execute successfully.");
					it.remove();
				}
				// failed
				else if (ct.t != null) {
					addParentStacktrace(ct.t);
					configServer.getLog("application").log(Log.LEVEL_ERROR, "controler", ct.t);
					it.remove();
				}
				// stop it!
				else if (time > TIMEOUT) {
					SystemUtil.stop(ct);
					// print.e(ct.getStackTrace());
					if (!ct.isAlive()) {
						configServer.getLog("application").error("controller", "controller thread [" + ct.hashCode() + "] forced to stop after " + time + "ms");
						it.remove();
					}
					else {
						Throwable t = new Throwable();
						t.setStackTrace(ct.getStackTrace());

						configServer.getLog("application").log(Log.LEVEL_ERROR, "controler", "was not able to stop controller thread running for " + time + "ms", t);
					}
				}
			}
			if (factories.length > 0) firstRun = false;
		}
	}

	public static void dumpThreadPositions(String path) throws IOException {
		Resource target = ResourcesImpl.getFileResourceProvider().getResource(path);

		StackTraceElement[] stes;
		StackTraceElement ste, n1, n2, n3, n4, n5, n6;
		String line;
		for (Entry<Thread, StackTraceElement[]> e: Thread.getAllStackTraces().entrySet()) {
			stes = e.getValue();
			if (stes == null || stes.length == 0) continue;
			ste = null;
			n1 = null;
			n2 = null;
			n3 = null;
			n4 = null;
			n5 = null;
			n6 = null;

			for (int i = 0; i < stes.length; i++) {
				if (stes[i].getLineNumber() > 0) {
					ste = stes[i];
					if (i + 1 < stes.length) n1 = stes[i + 1];
					if (i + 2 < stes.length) n2 = stes[i + 2];
					if (i + 3 < stes.length) n3 = stes[i + 3];
					if (i + 4 < stes.length) n4 = stes[i + 4];
					if (i + 5 < stes.length) n5 = stes[i + 5];
					if (i + 6 < stes.length) n6 = stes[i + 6];
					break;
				}
			}
			if (ste == null) continue;
			// print.e(stes);
			line = "{\"stack\":[\"" + ste.getClassName() + ":" + ste.getLineNumber() + "." + ste.getMethodName() + "\"" +

					(n1 == null ? "" : ",\"" + n1.getClassName() + ":" + n1.getLineNumber() + "." + n1.getMethodName() + "\"") +

					(n2 == null ? "" : ",\"" + n2.getClassName() + ":" + n2.getLineNumber() + "." + n2.getMethodName() + "\"") +

					(n3 == null ? "" : ",\"" + n3.getClassName() + ":" + n3.getLineNumber() + "." + n3.getMethodName() + "\"") +

					(n4 == null ? "" : ",\"" + n4.getClassName() + ":" + n4.getLineNumber() + "." + n4.getMethodName() + "\"") +

					(n5 == null ? "" : ",\"" + n5.getClassName() + ":" + n5.getLineNumber() + "." + n5.getMethodName() + "\"") +

					(n6 == null ? "" : ",\"" + n6.getClassName() + ":" + n6.getLineNumber() + "." + n6.getMethodName() + "\"") +

					"],\"thread\":\"" + e.getKey().getName() + "\",\"id\":" + e.getKey().getId() + ",\"time\":" + System.currentTimeMillis() + "}\n";
			IOUtil.write(target, line, CharsetUtil.UTF8, true);
		}

	}

	private static void dumpThreads() {
		aprint.e("==================== THREAD DUMP " + new Date() + " ====================");
		for (Entry<Thread, StackTraceElement[]> e: Thread.getAllStackTraces().entrySet()) {
			aprint.e(e.getKey().getName() + ":" + e.getKey().getId() + " " + e.getKey().getState());
			aprint.e(ExceptionUtil.getStacktrace(e.getValue()));
			aprint.e("------------------------------------------------------------------");
		}
		aprint.e("==================================================================");

	}

	public static void main(String[] args) throws IOException {
		dumpThreadPositions("/Users/mic/Tmp3/tmp/data.jsonl");
	}

	private void control(CFMLFactoryImpl[] factories, boolean firstRun, Log log) {
		long now = System.currentTimeMillis();
		boolean do10Seconds = last10SecondsInterval + 10000 < now;
		if (do10Seconds) last10SecondsInterval = now;

		boolean doMinute = lastMinuteInterval + 60000 < now;
		if (doMinute) lastMinuteInterval = now;

		boolean do5Minute = last5MinuteInterval + 300000 < now;
		if (do5Minute) last5MinuteInterval = now;

		boolean doHour = (lastHourInterval + (1000 * 60 * 60)) < now;
		if (doHour) lastHourInterval = now;

		// every 10 seconds
		if (do10Seconds) {
			// deploy extensions, archives ...
			try {
				DeployHandler.deploy(configServer, configServer.getLog("deploy"), false);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (log != null) log.error("controler", t);
			}
		}
		// every minute
		if (doMinute) {
			// deploy extensions, archives ...
			/*
			 * try { DeployHandler.deploy(configServer, configServer.getLog("deploy"), false); } catch
			 * (Throwable t) { ExceptionUtil.rethrowIfNecessary(t); if (log != null) log.error("controler", t);
			 * }
			 */
			try {
				ConfigAdmin.checkForChangesInConfigFile(configServer);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (log != null) log.error("controler", t);
			}
		}

		// every 5 minutes
		if (do5Minute) {
			try {
				System.gc();
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (log != null) log.error("controler", t);
			}
		}

		for (int i = 0; i < factories.length; i++) {
			control(factories[i], do10Seconds, doMinute, doHour, firstRun, log);
		}

		if (firstRun) {

			try {
				RHExtension.correctExtensions(configServer);
			}
			catch (Exception e) {
				if (log != null) log.error("controler", e);
			}

			// loading all versions from Maven (if it can be reached)
			try {
				new MavenUpdateProvider().list();
			}
			catch (Exception e) {
				if (log != null) log.error("controler", e);
			}
		}
	}

	private void control(CFMLFactoryImpl cfmlFactory, boolean do10Seconds, boolean doMinute, boolean doHour, boolean firstRun, Log log) {
		try {
			boolean isRunning = cfmlFactory.getUsedPageContextLength() > 0;
			if (isRunning) {
				cfmlFactory.checkTimeout();
			}
			ConfigWeb config = null;

			if (firstRun) {
				config = cfmlFactory.getConfig();
				ThreadLocalConfig.register(config);

				checkOldClientFile(config, log);

				try {
					checkTempDirectorySize(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
				try {
					checkCacheFileSize(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
				try {
					cfmlFactory.getScopeContext().clearUnused();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
			}

			if (config == null) {
				config = cfmlFactory.getConfig();
			}
			ThreadLocalConfig.register(config);
			if (do10Seconds) {
				// try{DeployHandler.deploy(config);}catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);}
			}

			// every Minute
			if (doMinute) {
				if (config == null) {
					config = cfmlFactory.getConfig();
				}
				ThreadLocalConfig.register(config);

				LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_TRACE, Controler.class.getName(), "Running background Controller maintenance (every minute).");

				try {
					Scheduler scheduler = config.getScheduler();
					if (scheduler != null) ((SchedulerImpl) scheduler).startIfNecessary();
				}
				catch (Exception e) {
					if (log != null) log.error("controler", e);
				}

				// double check templates
				try {
					((ConfigWebPro) config).getCompiler().checkWatched();
				}
				catch (Exception e) {
					if (log != null) log.error("controler", e);
				}

				// deploy extensions, archives ...
				try {
					DeployHandler.deploy(config, ThreadLocalPageContext.getLog(config, "deploy"), false);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}

				// clear unused DB Connections
				try {
					for (DatasourceConnPool pool: ((ConfigPro) config).getDatasourceConnectionPools()) {
						try {
							pool.evict();
						}
						catch (Exception ex) {
							if (log != null) log.error("controler", ex);
						}
					}
				}
				catch (Exception e) {
					if (log != null) log.error("controler", e);
				}

				// Clear unused http connections
				try {
					HTTPEngine4Impl.closeIdleConnections();
				}
				catch (Exception e) {
					if (log != null) log.error("controler", e);
				}

				// clear all unused scopes
				try {
					cfmlFactory.getScopeContext().clearUnused();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
				// Memory usage
				// clear Query Cache
				/*
				 * try{ ConfigWebUtil.getCacheHandlerFactories(config).query.clean(null);
				 * ConfigWebUtil.getCacheHandlerFactories(config).include.clean(null);
				 * ConfigWebUtil.getCacheHandlerFactories(config).function.clean(null);
				 * //cfmlFactory.getDefaultQueryCache().clearUnused(null); }catch(Throwable
				 * t){ExceptionUtil.rethrowIfNecessary(t);}
				 */

				try {
					doCheckMappings(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
				try {
					doClearMailConnections();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
				// clean LockManager
				if (cfmlFactory.getUsedPageContextLength() == 0) try {
					((LockManagerImpl) config.getLockManager()).clean();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}

				try {
					ConfigAdmin.checkForChangesInConfigFile(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}

			}
			// every hour
			if (doHour) {
				if (config == null) {
					config = cfmlFactory.getConfig();
				}

				LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_TRACE, Controler.class.getName(), "Running background Controller maintenance (every hour).");

				ThreadLocalConfig.register(config);

				// check temp directory
				try {
					checkTempDirectorySize(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
				// check cache directory
				try {
					checkCacheFileSize(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}

				// clean up dynclasses
				try {
					DynamicInvoker di = DynamicInvoker.getExistingInstance();
					if (di != null) di.cleanup();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (log != null) log.error("controler", t);
		}
		finally {
			ThreadLocalConfig.release();
		}
	}

	private CFMLFactoryImpl[] toFactories(CFMLFactoryImpl[] factories, Map contextes) {
		if (factories == null || factories.length != contextes.size()) factories = (CFMLFactoryImpl[]) contextes.values().toArray(new CFMLFactoryImpl[contextes.size()]);

		return factories;
	}

	private void doClearMailConnections() {
		SMTPConnectionPool.closeSessions();
	}

	private void checkOldClientFile(ConfigWeb config, Log log) {
		ExtensionResourceFilter filter = new ExtensionResourceFilter(".script");

		// move old structured file in new structure
		try {
			Resource dir = config.getClientScopeDir(), trgres;
			Resource[] children = dir.listResources(filter);
			if (children == null) return;
			String src, trg;
			int index;
			for (int i = 0; i < children.length; i++) {
				src = children[i].getName();
				index = src.indexOf('-');

				trg = StorageScopeFile.getFolderName(src.substring(0, index), src.substring(index + 1), false);
				trgres = dir.getRealResource(trg);
				if (!trgres.exists()) {
					trgres.createFile(true);
					ResourceUtil.copy(children[i], trgres);
				}
				// children[i].moveTo(trgres);
				children[i].delete();

			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (log != null) log.error("controler", t);
		}
	}

	private void checkCacheFileSize(ConfigWeb config) {
		checkSize(config, config.getCacheDir(), config.getCacheDirSize(), new ExtensionResourceFilter(".cache"));
	}

	private void checkTempDirectorySize(ConfigWeb config) {
		checkSize(config, config.getTempDirectory(), 1024 * 1024 * 1024, null);
	}

	private void checkSize(ConfigWeb config, Resource dir, long maxSize, ResourceFilter filter) {
		if (dir == null || !dir.exists()) return;
		Resource res = null;
		int count = ArrayUtil.size(filter == null ? dir.list() : dir.list(filter));
		long size = ResourceUtil.getRealSize(dir, filter);
		LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_DEBUG, Controler.class.getName(),
				"Checking size of directory [" + dir + "]. Current size [" + size + "]. Max size [" + maxSize + "].");

		int len = -1;

		if (count > 100000 || size > maxSize) {
			LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_WARN, Controler.class.getName(),
					"Removing files from directory [" + dir + "]. Current size [" + size + "]. Max size [" + maxSize + "]. Number of files [" + count + "]");
		}

		while (count > 100000 || size > maxSize) {
			Resource[] files = filter == null ? dir.listResources() : dir.listResources(filter);
			if (len == files.length) break;// protect from inifinti loop
			len = files.length;
			for (int i = 0; i < files.length; i++) {
				if (res == null || res.lastModified() > files[i].lastModified()) {
					res = files[i];
				}
			}
			if (res != null) {
				size -= res.length();
				try {
					res.remove(true);
					count--;
				}
				catch (Exception e) {
					LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_ERROR, Controler.class.getName(), "cannot remove resource [" + res.getAbsolutePath() + "]");
					break;
				}
			}
			res = null;
		}

	}

	private void doCheckMappings(ConfigWeb config) {
		lucee.runtime.config.ConfigWebImpl d;
		if (config instanceof ConfigWebPro) {
			((ConfigWebPro) config).checkMappings();
		}
		else {
			for (Mapping mapping: config.getMappings()) {
				mapping.check();
			}
		}
	}

	public void close() {
		state = INACTIVE;
		SystemUtil.notify(this);
	}

	static class ExpiresFilter implements ResourceFilter {

		private long time;
		private boolean allowDir;

		public ExpiresFilter(long time, boolean allowDir) {
			this.allowDir = allowDir;
			this.time = time;
		}

		@Override
		public boolean accept(Resource res) {

			if (res.isDirectory()) return allowDir;

			// load content
			String str = null;
			try {
				str = IOUtil.toString(res, "UTF-8");
			}
			catch (IOException e) {
				return false;
			}

			int index = str.indexOf(':');
			if (index != -1) {
				long expires = Caster.toLongValue(str.substring(0, index), -1L);
				// check is for backward compatibility, old files have no expires date inside. they do ot expire
				if (expires != -1) {
					if (expires < System.currentTimeMillis()) {
						return true;
					}
					str = str.substring(index + 1);
					return false;
				}
			}
			// old files not having a timestamp inside
			else if (res.lastModified() <= time) {
				return true;

			}
			return false;
		}
	}
}
