package lucee.transformer.dynamic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassLoaderDefault;
import lucee.commons.lang.ExtendableClassLoader;

/**
 * Directory ClassLoader
 */
public final class DynamicClassLoader extends ClassLoader implements ExtendableClassLoader, ClassLoaderDefault {

	static {
		boolean res = registerAsParallelCapable();
	}
	private Resource directory;

	private final Map<String, String> loadedClasses = new ConcurrentHashMap<>();
	private final Map<String, String> allLoadedClasses = new ConcurrentHashMap<>(); // this includes all renames
	private final Map<String, String> unavaiClasses = new ConcurrentHashMap<>();

	private final Map<String, SoftReference<Object>> instances = new ConcurrentHashMap<>();

	private static final AtomicLong counter = new AtomicLong(Long.MAX_VALUE - 1);
	private static long _start = 0L;
	private static String start = Long.toString(_start, Character.MAX_RADIX);
	private static final Object countToken = new Object();

	private static final long MAX_AGE = 30 * 60 * 60 * 1000;

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

	/**
	 * Constructor of the class
	 * 
	 * @param directory
	 * @param parent
	 * @throws IOException
	 */
	public DynamicClassLoader(Resource directory, Log log) throws IOException {
		this(directory, (ClassLoader[]) null, true, log);
	}

	public DynamicClassLoader(ClassLoader parent, Resource directory, Log log) {
		super(parent);

		try {
			if (!directory.exists()) directory.mkdirs();
			if (!directory.isDirectory()) throw new IOException("Resource [" + directory + "] is not a directory");
			if (!directory.canRead()) throw new IOException("Access denied to [" + directory + "] directory");
			this.directory = directory; // we only store it when okay
		}
		catch (Exception e) {
			if (log != null) log.error("dynamic", e);
		}
	}

	public DynamicClassLoader(Resource directory, ClassLoader[] parentClassLoaders, boolean includeCoreCL, Log log) {
		super(parentClassLoaders == null || parentClassLoaders.length == 0 ? directory.getClass().getClassLoader() : parentClassLoaders[0]);

		// parents.add(new TP().getClass().getClassLoader());
		// if (includeCoreCL) parents.add(CFMLEngineImpl.class.getClassLoader());

		// check directory
		try {
			if (!directory.exists()) directory.mkdirs();
			if (!directory.isDirectory()) throw new IOException("Resource [" + directory + "] is not a directory");
			if (!directory.canRead()) throw new IOException("Access denied to [" + directory + "] directory");
			this.directory = directory; // we only store it when okay
		}
		catch (Exception e) {
			if (log != null) log.error("dynamic", e);
		}
	}

	public Object loadInstance(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		SoftReference<Object> ref = instances.get(name);
		Object value;
		if (ref != null && (value = ref.get()) != null) {
			return value;
		}
		Class<?> clazz = loadClass(name, false, true);
		value = clazz.getConstructor().newInstance();
		instances.put(name, new SoftReference<Object>(value));
		return value;
	}

	public Object loadInstance(String name, byte[] barr) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, UnmodifiableClassException, IOException {
		SoftReference<Object> ref = instances.get(name);
		Object value;
		if (ref != null && (value = ref.get()) != null) {
			return value;
		}
		Class<?> clazz = loadClass(name, barr);
		value = clazz.getConstructor().newInstance();
		instances.put(name, new SoftReference<Object>(value));
		return value;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false, true);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass(name, resolve, true);
	}

	@Override
	public Class<?> loadClass(String className, byte[] barr) throws UnmodifiableClassException, IOException {
		Class<?> clazz = findLoadedClass(className);
		if (clazz == null) {
			// store file
			write(className, barr);
			synchronized (SystemUtil.createToken("DynamicClassLoader:load", className)) {
				clazz = findLoadedClass(className);
				if (clazz == null) {
					return _loadClass(className, barr);
				}
			}
		}
		return clazz;
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve, Class<?> defaultValue) {
		return loadClass(name, resolve, true, defaultValue);
	}

	private Class<?> loadClass(String name, boolean resolve, boolean loadFromFS, Class<?> defaultValue) {
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			synchronized (SystemUtil.createToken("DynamicClassLoader:load", name)) {
				c = findLoadedClass(name);
				if (c == null) {
					ClassLoader pcl = getParent();
					if (pcl instanceof ClassLoaderDefault) {
						c = ((ClassLoaderDefault) pcl).loadClass(name, resolve, null);
					}
					else {
						try {
							c = pcl.loadClass(name);
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
		if (c == null) throw new ClassNotFoundException(name);
		return c;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {// if(name.indexOf("sub")!=-1)print.ds(name);
		byte[] barr = read(name);
		synchronized (SystemUtil.createToken("DynamicClassLoader:load", name)) {
			return _loadClass(name, barr);
		}
	}

	private Class<?> _loadClass(String name, byte[] barr) {
		Class<?> clazz = defineClass(name, barr, 0, barr.length);
		if (clazz != null) {
			loadedClasses.put(name, "");
			allLoadedClasses.put(name, "");

			resolveClass(clazz);
		}
		return clazz;
	}

	private void write(String className, byte[] barr) throws IOException {
		if (directory != null) {
			synchronized (SystemUtil.createToken("DynamicClassLoader:file", className)) {
				Resource classFile = directory.getRealResource(className.replace('.', '/') + ".class");
				classFile.getParentResource().createDirectory(true);
				IOUtil.write(classFile, barr);
			}
		}
	}

	private byte[] read(String className) throws ClassNotFoundException {
		if (directory != null) {
			synchronized (SystemUtil.createToken("DynamicClassLoader:file", className)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					Resource res = directory.getRealResource(className.replace('.', '/').concat(".class"));
					IOUtil.copy(res, baos, false);
					byte[] barr = baos.toByteArray();
					if (barr.length == 0) {
						this.unavaiClasses.put(className, "");
						throw new ClassNotFoundException("Class [" + className + "] is invalid or doesn't exist [parent:" + getParent() + "]");
					}
					return barr;
				}
				catch (IOException e) {
					this.unavaiClasses.put(className, "");
					throw new ClassNotFoundException("Class [" + className + "] is invalid or doesn't exist [parent:" + getParent() + "]", e);
				}
				finally {
					IOUtil.closeEL(baos); // nice to have but not really needed
				}
			}
		}
		throw new ClassNotFoundException("Class [" + className + "] not found (memory mode)");
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream is = super.getResourceAsStream(name);
		if (is != null) return is;

		Resource f = _getResource(name);
		if (f != null) {
			try {
				return IOUtil.toBufferedInputStream(f.getInputStream());
			}
			catch (IOException e) {
			}
		}
		return null;
	}

	@Override
	public URL getResource(String name) {
		return null;
	}

	public int getSize(boolean includeAllRenames) {
		return includeAllRenames ? allLoadedClasses.size() : loadedClasses.size();
	}

	/**
	 * returns matching File Object or null if file not exust
	 * 
	 * @param name
	 * @return matching file
	 */
	public Resource _getResource(String name) {
		Resource f = directory == null ? null : directory.getRealResource(name);
		if (f != null && f.isFile()) return f;
		return null;
	}

	public boolean hasClass(String className) {
		return isClassLoaded(className) || hasResource(className.replace('.', '/').concat(".class"));
	}

	public boolean isClassLoaded(String className) {
		if (loadedClasses.containsKey(className)) return true;
		return findLoadedClass(className) != null;
	}

	public boolean hasResource(String name) {
		return _getResource(name) != null;
	}

	public void cleanup() {
		if (directory.isDirectory()) {
			if (ResourceUtil.deleteFileOlderThan(directory, System.currentTimeMillis() - MAX_AGE, null)) {
				try {
					ResourceUtil.deleteEmptyFolders(directory);
				}
				catch (IOException e) {
				}
			}
		}
	}
}
