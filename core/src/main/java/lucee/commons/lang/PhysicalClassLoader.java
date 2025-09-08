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
package lucee.commons.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.CombinedClassLoader;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil.ClassLoading;
import lucee.runtime.PageSourcePool;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.listener.JavaSettings;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ClassRenamer;

/**
 * Directory ClassLoader
 */
public final class PhysicalClassLoader extends URLClassLoader implements ExtendableClassLoader, ClassLoaderDefault, ClassLoading, DirectoryProvider {

	static {
		boolean res = registerAsParallelCapable();
	}

	private static RC rc = new RC();

	private static Map<String, PhysicalClassLoader> classLoaders = new ConcurrentHashMap<>();

	private final Resource directory;
	private ConfigPro config;
	private final ClassLoader addionalClassLoader;
	private final Collection<Resource> resources;

	private Map<String, byte[]> loadedClasses = new ConcurrentHashMap<String, byte[]>();
	private Map<String, byte[]> allLoadedClasses = new ConcurrentHashMap<String, byte[]>(); // this includes all renames
	private Map<String, String> unavaiClasses = new ConcurrentHashMap<String, String>();

	private PageSourcePool pageSourcePool;

	private boolean rpc;

	private String birthplace;

	public final String id;

	private static final AtomicLong counter = new AtomicLong(0);
	private static long _start = 0L;
	private static String start = Long.toString(_start, Character.MAX_RADIX);
	private static Object countToken = new Object();

	public static String uid() {
		long currentCounter = counter.incrementAndGet(); // Increment and get atomically
		if (currentCounter < 0) {
			synchronized (countToken) {
				currentCounter = counter.incrementAndGet();
				if (currentCounter < 0) {
					counter.set(0L);
					currentCounter = 0L;
					start = Long.toString(++_start, Character.MAX_RADIX);
				}
			}
		}
		if (_start == 0L) return Long.toString(currentCounter, Character.MAX_RADIX);
		return start + "_" + Long.toString(currentCounter, Character.MAX_RADIX);
	}

	public static PhysicalClassLoader getPhysicalClassLoader(Config c, Resource directory, boolean reload) throws IOException {
		String key = HashUtil.create64BitHashAsString(directory.getAbsolutePath());

		PhysicalClassLoader rpccl = reload ? null : classLoaders.get(key);
		if (rpccl == null) {
			synchronized (SystemUtil.createToken("PhysicalClassLoader", key)) {
				rpccl = reload ? null : classLoaders.get(key);
				if (rpccl == null) {
					// if we have a reload, clear the existing before set a new one
					if (reload) {
						PhysicalClassLoader existing = classLoaders.get(key);
						if (existing != null) existing.clear();
					}
					classLoaders.put(key, rpccl = new PhysicalClassLoader(key, c, new ArrayList<Resource>(), directory, SystemUtil.getCoreClassLoader(), null, null, false));
				}
			}
		}
		return rpccl;
	}

	public static CombinedClassLoader getRPCClassLoader(Config c, BundleClassLoader bcl, boolean reload) throws IOException {
		return CombinedClassLoader.getInstance(getRPCClassLoader(c, null, bcl, SystemUtil.getCoreClassLoader(), reload),
				getRPCClassLoader(c, null, bcl, SystemUtil.getLoaderClassLoader(), reload), reload);
	}

	public static CombinedClassLoader getRPCClassLoader(Config c, JavaSettings js, boolean reload) throws IOException {
		return CombinedClassLoader.getInstance(getRPCClassLoader(c, js, null, SystemUtil.getCoreClassLoader(), reload),
				getRPCClassLoader(c, js, null, SystemUtil.getLoaderClassLoader(), reload), reload);
	}

	private static PhysicalClassLoader getRPCClassLoader(Config c, JavaSettings js, BundleClassLoader bcl, ClassLoader parent, boolean reload) throws IOException {
		String key = js == null ? "orphan" : ((JavaSettingsImpl) js).id();

		if (parent == null) parent = SystemUtil.getCoreClassLoader();
		if (parent instanceof PhysicalClassLoader) {
			key += ":" + ((PhysicalClassLoader) parent).id;
		}
		else {
			key += ":" + parent.getClass().getName() + parent.hashCode();
		}

		if (bcl != null) {
			key += ":" + bcl;
		}
		key = HashUtil.create64BitHashAsString(key);

		PhysicalClassLoader rpccl = reload ? null : classLoaders.get(key);
		if (rpccl == null) {
			synchronized (SystemUtil.createToken("PhysicalClassLoader", key)) {
				rpccl = reload ? null : classLoaders.get(key);
				if (rpccl == null) {
					// if we have a reload, clear the existing before set a new one
					if (reload) {
						PhysicalClassLoader existing = classLoaders.get(key);
						if (existing != null) existing.clear();
					}
					List<Resource> resources;
					if (js == null) {
						resources = new ArrayList<Resource>();
					}
					else {
						resources = toSortedList(((JavaSettingsImpl) js).getAllResources());
					}
					Resource dir = storeResourceMeta(c, key, js, resources);
					// (Config config, String key, JavaSettings js, Collection<Resource> _resources)
					classLoaders.put(key, rpccl = new PhysicalClassLoader(key, c, resources, dir, parent, bcl, null, true));
				}
			}
		}
		return rpccl;
	}

	private PhysicalClassLoader(String key, Config c, List<Resource> resources, Resource directory, ClassLoader parentClassLoader, ClassLoader addionalClassLoader,
			PageSourcePool pageSourcePool, boolean rpc) throws IOException {
		super(doURLs(resources), parentClassLoader == null ? (parentClassLoader = SystemUtil.getCoreClassLoader()) : parentClassLoader);
		this.resources = resources;

		config = (ConfigPro) c;
		this.addionalClassLoader = addionalClassLoader;
		this.birthplace = ExceptionUtil.getStacktrace(new Throwable(), false);
		this.pageSourcePool = pageSourcePool;

		// check directory
		if (!directory.exists()) directory.mkdirs();
		if (!directory.isDirectory()) throw new IOException("Resource [" + directory + "] is not a directory");
		if (!directory.canRead()) throw new IOException("Access denied to [" + directory + "] directory");
		this.directory = directory;
		this.rpc = rpc;
		id = key;
	}

	public String getBirthplace() {
		return birthplace;
	}

	public boolean isRPC() {
		return rpc;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false, true);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass(name, resolve, true);
	}

	public Boolean isClassAvailable(ClassLoader loader, String className) {
		if (allLoadedClasses.containsKey(className)) return true;
		if (unavaiClasses.containsKey(className)) return false;

		return null;
		// String resourcePath = className.replace('.', '/').concat(".class");
		// return loader.getResource(resourcePath) != null;
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve, Class<?> defaultValue) {
		return loadClass(name, resolve, true, defaultValue);
	}

	@Override
	public Class<?> loadClass(String className, Class defaultValue) {
		return loadClass(className, false, true, defaultValue);
	}

	@Override
	public Class<?> loadClass(String className, Class defaultValue, Set<Throwable> exceptions) {
		try {
			return loadClass(className);
		}
		catch (Exception e) {
			if (exceptions != null) {
				exceptions.add(e);
			}
			return defaultValue;
		}
	}

	private Class<?> loadClass(String name, boolean resolve, boolean loadFromFS, Class<?> defaultValue) {
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			synchronized (SystemUtil.createToken("PhysicalClassLoader:load", name)) {
				c = findLoadedClass(name);
				if (c == null) {
					ClassLoader pcl = getParent();
					if (pcl instanceof ClassLoaderDefault) {
						c = ((ClassLoaderDefault) pcl).loadClass(name, resolve, null);
					}
					else {
						try {
							c = super.loadClass(name, resolve);
						}
						catch (Exception e) {
						}
					}

					if (c == null && addionalClassLoader != null) {
						try {
							c = addionalClassLoader.loadClass(name);
						}
						catch (Exception e) {
						}
					}

					if (c == null) {
						if (loadFromFS) {
							try {
								c = findClass(name);
							}
							catch (ClassNotFoundException e) {
								return defaultValue;
							}
						}
						else return defaultValue;
					}
				}
			}
		}
		if (resolve) resolveClass(c);
		return c;
	}

	private Class<?> loadClass(String name, boolean resolve, boolean loadFromFS) throws ClassNotFoundException {
		Class<?> c = loadClass(name, resolve, loadFromFS, null);
		if (c == null) {
			throw new ClassNotFoundException(name);
		}
		return c;
	}

	@Override
	public Class<?> loadClass(String name, byte[] barr) throws UnmodifiableClassException {
		// Class<?> clazz = null;
		Class<?> clazz = findLoadedClass(name);

		synchronized (SystemUtil.createToken("PhysicalClassLoader:load", name)) {
			if (clazz == null) return _loadClass(name, barr, false);
			return rename(clazz, barr);
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (super.findResource(name.replace('.', '/').concat(".class")) != null) {
			return super.findClass(name);
		}

		if (addionalClassLoader != null) {
			// boolean true in case it returns TRUE or null
			if (!Boolean.FALSE.equals(isClassAvailable(addionalClassLoader, name))) {
				try {
					return addionalClassLoader.loadClass(name);
				}
				catch (ClassNotFoundException e) {
					LogUtil.trace("physical-classloader", e);
				}
			}
		}

		synchronized (SystemUtil.createToken("PhysicalClassLoader:load", name)) {
			Resource res = directory.getRealResource(name.replace('.', '/').concat(".class"));
			if (!res.isFile()) {
				// if (cnfe != null) throw cnfe;
				throw new ClassNotFoundException("Class [" + name + "] is invalid or doesn't exist");
			}
			return _loadClass(name, read(name), false);
		}
	}

	private byte[] read(String name) throws ClassNotFoundException {
		Resource res = directory.getRealResource(name.replace('.', '/').concat(".class"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			IOUtil.copy(res, baos, false);
		}
		catch (IOException e) {
			this.unavaiClasses.put(name, "");
			throw new ClassNotFoundException("Class [" + name + "] is invalid or doesn't exist", e);
		}
		finally {
			IOUtil.closeEL(baos);
		}
		return baos.toByteArray();
	}

	private byte[] read(String name, byte[] defaultValue) {
		Resource res = directory.getRealResource(name.replace('.', '/').concat(".class"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			IOUtil.copy(res, baos, false);
		}
		catch (IOException e) {
			this.unavaiClasses.put(name, "");
			return defaultValue;
		}
		finally {
			IOUtil.closeEL(baos);
		}
		return baos.toByteArray();
	}

	private Class<?> rename(Class<?> clazz, byte[] barr) {
		String newName = clazz.getName() + "$" + uid();
		return _loadClass(newName, ClassRenamer.rename(barr, newName), true);
	}

	private Class<?> _loadClass(String name, byte[] barr, boolean rename) {
		try {
			Class<?> clazz = defineClass(name, barr, 0, barr.length);

			if (clazz != null) {
				if (!rename) loadedClasses.put(name, barr);
				allLoadedClasses.put(name, barr);

				resolveClass(clazz);
			}
			return clazz;
		}
		catch (ClassFormatError cfe) {
			if (!ASMUtil.isValidBytecode(barr)) throw new RuntimeException("given bytcode for [" + name + "] is not valid");
			throw cfe;
		}
	}

	public Resource[] getJarResources() {
		return resources.toArray(new Resource[resources.size()]);
	}

	public boolean hasJarResources() {
		return resources.isEmpty();
	}

	public int getSize(boolean includeAllRenames) {
		return includeAllRenames ? allLoadedClasses.size() : loadedClasses.size();
	}

	/*
	 * @Override public URL getResource(String name) { URL r = super.getResource(name); if (r != null)
	 * return r; print.e("xx ====>" + name);
	 * 
	 * Resource f = _getResource(name);
	 * 
	 * if (f != null) { return ResourceUtil.toURL(f, null); } return null; }
	 */

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream is = super.getResourceAsStream(name);
		if (is != null) return is;

		if (name.endsWith(".class")) {
			// MUST store the barr in a less memory intensive way
			String className = name.substring(0, name.length() - 6).replace('/', '.').replace('\\', '.');
			byte[] barr = allLoadedClasses.get(className);
			if (barr != null) return new ByteArrayInputStream(barr);
		}

		URL url = super.getResource(name);
		if (url != null) {
			try {
				return IOUtil.toBufferedInputStream(url.openStream());
			}
			catch (IOException e) {
				LogUtil.trace("physical-classloader", e);
			}
		}

		Resource f = _getResource(name);
		if (f != null) {
			try {
				return IOUtil.toBufferedInputStream(f.getInputStream());
			}
			catch (IOException e) {
				LogUtil.trace("physical-classloader", e);
			}
		}
		return null;
	}

	/**
	 * returns matching File Object or null if file not exust
	 * 
	 * @param name
	 * @return matching file
	 */
	public Resource _getResource(String name) {
		Resource f = directory.getRealResource(name);
		if (f != null && f.isFile()) return f;
		return null;
	}

	public boolean hasClass(String className) {
		return hasResource(className.replace('.', '/').concat(".class"));
	}

	public boolean isClassLoaded(String className) {
		return findLoadedClass(className) != null;
	}

	public boolean hasResource(String name) {
		return _getResource(name) != null;
	}

	/**
	 * @return the directory
	 */
	@Override
	public Resource getDirectory() {
		return directory;
	}

	public void clear() {
		clear(true);
	}

	public void clear(boolean clearPagePool) {
		if (clearPagePool && pageSourcePool != null) pageSourcePool.clearPages(this);
		this.loadedClasses.clear();
		this.allLoadedClasses.clear();
		this.unavaiClasses.clear();
	}

	private static Resource storeResourceMeta(Config config, String key, JavaSettings js, Collection<Resource> _resources) throws IOException {
		Resource dir = config.getClassDirectory().getRealResource("RPC/" + key);
		if (!dir.exists()) {
			ResourceUtil.createDirectoryEL(dir, true);
			Resource file = dir.getRealResource("classloader-resources.json");
			Struct root = new StructImpl();
			root.setEL(KeyConstants._resources, _resources);
			JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
			try {
				String str = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_COLUMN, null);
				IOUtil.write(file, str, CharsetUtil.UTF8, false);
			}
			catch (ConverterException e) {
				throw ExceptionUtil.toIOException(e);
			}

		}
		return dir;
	}

	/**
	 * removes memory based appendix from class name, for example it translates
	 * [test.test_cfc$sub2$cf$5] to [test.test_cfc$sub2$cf]
	 * 
	 * @param name
	 * @return
	 * @throws ApplicationException
	 */
	public static String substractAppendix(String name) throws ApplicationException {
		if (name.endsWith("$cf")) return name;
		int index = name.lastIndexOf('$');
		if (index != -1) {
			name = name.substring(0, index);
		}
		if (name.endsWith("$cf")) return name;
		throw new ApplicationException("could not remove appendix from [" + name + "]");
	}

	@Override
	public void finalize() throws Throwable {
		try {
			clear();
		}
		catch (Exception e) {
			LogUtil.log(config, "classloader", e);
		}
		super.finalize();
	}

	public static List<Resource> toSortedList(Collection<Resource> resources) {
		List<Resource> list = new ArrayList<Resource>();
		if (resources != null) {
			for (Resource r: resources) {
				if (r != null) list.add(r);
			}
		}
		java.util.Collections.sort(list, rc);
		return list;
	}

	public static List<Resource> toSortedList(Resource[] resources) {
		List<Resource> list = new ArrayList<Resource>();
		if (resources != null) {
			for (Resource r: resources) {
				if (r != null) list.add(r);
			}
		}
		java.util.Collections.sort(list, rc);
		return list;
	}

	private static URL[] doURLs(Collection<Resource> reses) throws IOException {
		List<URL> list = new ArrayList<URL>();
		for (Resource r: reses) {
			if ("jar".equalsIgnoreCase(ResourceUtil.getExtension(r, null)) || r.isDirectory()) list.add(doURL(r));
		}
		return list.toArray(new URL[list.size()]);
	}

	private static URL doURL(Resource res) throws IOException {
		if (!(res instanceof FileResource)) {
			return ResourceUtil.toFile(res).toURL();
		}
		return ((FileResource) res).toURL();
	}

	private static class RC implements Comparator<Resource> {

		@Override
		public int compare(Resource l, Resource r) {
			return l.getAbsolutePath().compareTo(r.getAbsolutePath());
		}
	}

}
