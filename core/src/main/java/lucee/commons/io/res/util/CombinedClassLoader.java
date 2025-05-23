package lucee.commons.io.res.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.ClassLoaderDefault;
import lucee.commons.lang.DirectoryProvider;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.ExtendableClassLoader;
import lucee.commons.lang.PhysicalClassLoader;

public final class CombinedClassLoader extends ClassLoader implements ClassLoaderDefault, DirectoryProvider, ExtendableClassLoader {

	private final PhysicalClassLoader core;
	private final PhysicalClassLoader loader;

	public CombinedClassLoader(PhysicalClassLoader loader, PhysicalClassLoader core) {
		super(null); // null means it doesn't have a parent itself
		this.core = core;
		this.loader = loader;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			// Try loading with the primary (OSGi) class loader first
			return loader.loadClass(name);
		}
		catch (ClassNotFoundException | NoClassDefFoundError e1) {
			// If not found, delegate to the secondary (main) class loader
			try {
				return core.loadClass(name);
			}
			catch (ClassNotFoundException | NoClassDefFoundError e2) {
				// System ClassLoader
				try {
					return Class.forName(name);
				}
				catch (ClassNotFoundException | NoClassDefFoundError e3) {
					ExceptionUtil.initCauseEL(e3, e2);
					ExceptionUtil.initCauseEL(e2, e1);
					throw e3;
				}
			}
		}

	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			// Try loading with the primary (OSGi) class loader first
			return loader.loadClass(name);
		}
		catch (ClassNotFoundException | NoClassDefFoundError e1) {
			// If not found, delegate to the secondary (main) class loader
			try {
				return core.loadClass(name);
			}
			catch (ClassNotFoundException | NoClassDefFoundError e2) {
				// System ClassLoader
				try {
					return Class.forName(name);
				}
				catch (ClassNotFoundException | NoClassDefFoundError e3) {
					ExceptionUtil.initCauseEL(e3, e2);
					ExceptionUtil.initCauseEL(e2, e1);
					throw e3;
				}
			}
		}
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve, Class<?> defaultValue) {
		Class<?> c;
		// if (loader instanceof ClassLoaderDefault) {
		c = loader.loadClass(name, resolve, defaultValue);
		if (c != null) return c;
		/*
		 * } else { // if (ClassUtil.isClassAvailable(core, name)) { try { return loader.loadClass(name); }
		 * catch (ClassNotFoundException e) {
		 * 
		 * } // } }
		 */

		// if (core instanceof ClassLoaderDefault) {
		c = core.loadClass(name, resolve, defaultValue);
		if (c != null) return c;
		/*
		 * } else { // if (ClassUtil.isClassAvailable(loader, name)) { try { return core.loadClass(name); }
		 * catch (ClassNotFoundException e) {
		 * 
		 * } // } }
		 */

		try {
			return Class.forName(name);
		}
		catch (ClassNotFoundException | NoClassDefFoundError e) {
		}

		return defaultValue;
	}

	@Override
	public URL getResource(String name) {
		URL resource = loader.getResource(name);
		if (resource == null) {
			resource = core.getResource(name);
		}
		if (resource == null) {
			resource = ClassLoader.getSystemClassLoader().getResource(name);
		}

		return resource;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream stream = loader.getResourceAsStream(name);
		if (stream == null) {
			stream = core.getResourceAsStream(name);
		}
		if (stream == null) {
			stream = ClassLoader.getSystemClassLoader().getResourceAsStream(name);
		}
		return stream;
	}

	@Override
	public Class<?> loadClass(String name, byte[] barr) throws UnmodifiableClassException, IOException {
		try {
			return loader.loadClass(name, barr);
		}
		catch (Exception e1) {
			return core.loadClass(name, barr);
		}
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		// Combine resources from both class loaders
		Enumeration<URL> coreResources = loader.getResources(name);
		Enumeration<URL> loaderResources = core.getResources(name);
		Enumeration<URL> systemResources = ClassLoader.getSystemClassLoader().getResources(name);
		URL url;
		Map<String, URL> combinedResources = new HashMap<>();
		while (loaderResources.hasMoreElements()) {
			url = loaderResources.nextElement();
			combinedResources.put(url.toExternalForm(), url);
		}
		while (coreResources.hasMoreElements()) {
			url = coreResources.nextElement();
			combinedResources.put(url.toExternalForm(), url);
		}
		while (systemResources.hasMoreElements()) {
			url = systemResources.nextElement();
			combinedResources.put(url.toExternalForm(), url);
		}
		return Collections.enumeration(combinedResources.values());
	}

	@Override
	public Resource getDirectory() {
		return loader.getDirectory();
	}

}
