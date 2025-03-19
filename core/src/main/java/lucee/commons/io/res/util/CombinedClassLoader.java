package lucee.commons.io.res.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import lucee.commons.lang.ClassLoaderDefault;
import lucee.commons.lang.ClassUtil;

public final class CombinedClassLoader extends ClassLoader implements ClassLoaderDefault {

	private final ClassLoader loader;
	private final ClassLoader core;

	public CombinedClassLoader(ClassLoader loader, ClassLoader core) {
		super(null); // null means it doesn't have a parent itself
		this.loader = loader;
		this.core = core;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			// Try loading with the primary (OSGi) class loader first
			return core.loadClass(name);
		}
		catch (ClassNotFoundException e) {
			// If not found, delegate to the secondary (main) class loader
			return loader.loadClass(name);
		}
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			// Try loading with the primary (OSGi) class loader first
			return core.loadClass(name);
		}
		catch (ClassNotFoundException e) {
			// If not found, delegate to the secondary (main) class loader
			return loader.loadClass(name);
		}
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve, Class<?> defaultValue) {
		Class<?> c;
		if (core instanceof ClassLoaderDefault) {
			c = ((ClassLoaderDefault) core).loadClass(name, resolve, defaultValue);
			if (c != null) return c;
		}
		else {
			if (ClassUtil.isClassAvailable(core, name)) {
				try {
					return core.loadClass(name);
				}
				catch (ClassNotFoundException e) {

				}
			}
		}

		if (loader instanceof ClassLoaderDefault) {
			c = ((ClassLoaderDefault) loader).loadClass(name, resolve, defaultValue);
			if (c != null) return c;
		}
		else {
			if (ClassUtil.isClassAvailable(loader, name)) {
				try {
					return loader.loadClass(name);
				}
				catch (ClassNotFoundException e) {

				}
			}
		}
		return defaultValue;
	}

	@Override
	public URL getResource(String name) {
		URL resource = core.getResource(name);
		if (resource == null) {
			resource = loader.getResource(name);
		}
		return resource;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream stream = core.getResourceAsStream(name);
		if (stream == null) {
			stream = loader.getResourceAsStream(name);
		}
		return stream;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		// Combine resources from both class loaders
		Enumeration<URL> coreResources = core.getResources(name);
		Enumeration<URL> loaderResources = loader.getResources(name);
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
		return Collections.enumeration(combinedResources.values());
	}
}
