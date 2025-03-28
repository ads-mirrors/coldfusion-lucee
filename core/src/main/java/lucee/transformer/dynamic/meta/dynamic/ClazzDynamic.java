package lucee.transformer.dynamic.meta.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.osgi.framework.Bundle;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.runtime.converter.JavaConverter.ObjectInputStreamImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.thread.ThreadUtil;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.Constructor;
import lucee.transformer.dynamic.meta.FunctionMember;
import lucee.transformer.dynamic.meta.Method;

public class ClazzDynamic extends Clazz {

	private static final long serialVersionUID = 862370302422701585L;
	private static final boolean DEBUG = true;
	private transient Class clazz;
	// private static Map<String, SoftReference<ClazzDynamic>> classes = new ConcurrentHashMap<>();
	private final Method[] methods;
	private final Method[] declaredMethods;
	private final Constructor[] constructors;
	private final Constructor[] declaredConstructors;

	private String clid;
	private String id;

	private static Map<ClassLoader, String> clids = new IdentityHashMap<>();
	private static String systemId;

	private static Map<Class, SoftReference<ClazzDynamic>> classes = new IdentityHashMap<>();
	// private static Map<String, SoftReference<ClazzDynamic>> classes = new ConcurrentHashMap<>();

	/*
	 * private static double generateClassLoderId = 0; private static double path = 0; private static
	 * double isFile = 0; private static double deserialize = 0; private static double put = 0; private
	 * static double neww = 0; private static double serialize = 0; private static double done = 0;
	 * private static int count = 0;
	 */

	public static ClazzDynamic getInstance(Class clazz, Resource dir, Log log) throws IOException {
		ClazzDynamic cd = null;
		Reference<ClazzDynamic> sr = classes.get(clazz);
		if (sr == null || (cd = sr.get()) == null) {
			synchronized (clazz) {
				sr = classes.get(clazz);
				if (sr == null || (cd = sr.get()) == null) {
					if (log != null) log.debug("dynamic", "extract metadata from [" + clazz.getName() + "]");
					cd = new ClazzDynamic(clazz, log);
					classes.put(clazz, new SoftReference<ClazzDynamic>(cd));
				}
			}
		}
		return cd;
	}

	public static String generateClassLoderId(Class<?> clazz) {
		ClassLoader cl = clazz.getClassLoader();
		String jv = HashUtil.create64BitHashAsString(System.getProperty("java.version"), Character.MAX_RADIX);
		if (cl == null) {
			if (systemId == null) systemId = "s" + jv;
			return systemId;
		}

		String id = clids.get(cl);
		if (id != null) return id;

		if (cl instanceof BundleClassLoader) {
			Bundle b = ((BundleClassLoader) cl).getBundle();
			id = "b" + HashUtil.create64BitHashAsString(b.getSymbolicName() + ":" + b.getVersion() + ":" + jv, Character.MAX_RADIX);
			clids.put(cl, id);
			return id;
		}
		if (cl instanceof PhysicalClassLoader) {
			id = "p" + HashUtil.create64BitHashAsString(((PhysicalClassLoader) cl).getDirectory().getAbsolutePath() + ":" + jv, Character.MAX_RADIX);
			clids.put(cl, id);
			return id;
		}

		ProtectionDomain protectionDomain = clazz.getProtectionDomain();
		CodeSource codeSource = protectionDomain.getCodeSource();
		if (codeSource != null && codeSource.getLocation() != null) {
			id = "j" + HashUtil.create64BitHashAsString(codeSource.getLocation().toString() + ":" + jv, Character.MAX_RADIX);
			clids.put(cl, id);
			return id;
		}
		return null;
	}

	private ClazzDynamic(Class clazz, Log log) throws IOException {
		this.clazz = clazz;
		Map<String, FunctionMember> members = getFunctionMembers(clazz, log);

		LinkedList<Method> tmpMethods = new LinkedList<>();
		LinkedList<Method> tmpDeclaredMethods = new LinkedList<>();
		LinkedList<Constructor> tmpConstructors = new LinkedList<>();
		LinkedList<Constructor> tmpDeclaredConstructors = new LinkedList<>();
		for (FunctionMember fm: members.values()) {
			if (fm instanceof Method) {
				if (clazz.getName().equals(fm.getDeclaringClassName())) tmpDeclaredMethods.add((Method) fm);
				if (fm.isPublic()) tmpMethods.add((Method) fm);
			}
			else if (fm instanceof Constructor) {
				if (clazz.getName().equals(fm.getDeclaringClassName())) tmpDeclaredConstructors.add((Constructor) fm);
				if (fm.isPublic()) tmpConstructors.add((Constructor) fm);
			}
		}
		methods = tmpMethods.toArray(new Method[tmpMethods.size()]);
		declaredMethods = tmpDeclaredMethods.toArray(new Method[tmpDeclaredMethods.size()]);
		constructors = tmpConstructors.toArray(new Constructor[tmpConstructors.size()]);
		declaredConstructors = tmpDeclaredConstructors.toArray(new Constructor[tmpDeclaredConstructors.size()]);

	}

	@Override
	public Class getDeclaringClass() {
		return this.clazz;
	}

	@Override
	public Type getDeclaringType() {
		return Type.getType(this.clazz);
	}

	@Override
	public String id() {
		if (id == null) {
			if (clid == null) {
				clid = generateClassLoderId(clazz);
			}
			id = clid + ":" + clazz.getName();
		}
		return id;
	}

	@Override
	public Method getDeclaredMethod(String methodName, Class[] arguments, boolean nameCaseSensitive) throws IOException, NoSuchMethodException {
		Type[] types = toTypes(arguments);
		outer: for (FunctionMember fm: declaredMethods) {
			if (/* clazz.getName().equals(fm.getDeclaringClassName()) && */ (nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName()))) {
				Type[] args = ((FunctionMemberDynamic) fm).getArgumentTypes();
				if (types.length == args.length) {
					for (int i = 0; i < args.length; i++) {
						if (!types[i].equals(args[i])) continue outer;
					}
					return (Method) fm;
				}
			}
		}
		throw new NoSuchMethodException("no matching method for " + methodName + "(" + Clazz.toTypeNames(arguments) + ") found");
	}

	@Override
	public Method getMethod(String methodName, Class[] arguments, boolean nameCaseSensitive) throws IOException, NoSuchMethodException {
		Method m = getMethod(methodName, arguments, nameCaseSensitive, null);
		if (m == null) throw new NoSuchMethodException("no matching method for " + methodName + "(" + Clazz.toTypeNames(arguments) + ") found");
		return m;
	}

	@Override
	public Method getMethod(String methodName, Class[] arguments, boolean nameCaseSensitive, Method defaultValue) {
		Type[] types = toTypes(arguments);
		outer: for (FunctionMember fm: methods) {
			if (/* fm.isPublic() && */ (nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName()))) {
				Type[] args = ((FunctionMemberDynamic) fm).getArgumentTypes();
				if (types.length == types.length) {
					for (int i = 0; i < args.length; i++) {
						if (!types[i].equals(args[i])) continue outer;
					}
					return (Method) fm;
				}
			}
		}
		return defaultValue;
	}

	@Override
	public Constructor getConstructor(Class[] arguments) throws IOException, NoSuchMethodException {
		Type[] types = toTypes(arguments);
		outer: for (FunctionMember fm: constructors) {
			if (/* fm.isPublic() && */clazz.getName().equals(fm.getDeclaringClassName())) {
				Type[] args = ((FunctionMemberDynamic) fm).getArgumentTypes();
				if (types.length == args.length) {
					for (int i = 0; i < args.length; i++) {
						if (!types[i].equals(args[i])) continue outer;
					}
					return (Constructor) fm;
				}
			}
		}
		throw new NoSuchMethodException("no matching constructor for (" + Clazz.toTypeNames(arguments) + ") found");
	}

	@Override
	public Constructor getDeclaredConstructor(Class[] arguments) throws IOException, NoSuchMethodException {
		Type[] types = toTypes(arguments);
		outer: for (FunctionMember fm: declaredConstructors) {
			// if (clazz.getName().equals(fm.getDeclaringClassName())) {
			Type[] args = ((FunctionMemberDynamic) fm).getArgumentTypes();
			if (types.length == args.length) {
				for (int i = 0; i < args.length; i++) {
					if (!types[i].equals(args[i])) continue outer;
				}
				return (Constructor) fm;
			}
			// }
		}
		throw new NoSuchMethodException("no matching constructor for (" + Clazz.toTypeNames(arguments) + ") found");
	}

	@Override
	public List<Method> getMethods(String methodName, boolean nameCaseSensitive, int argumentLength) {
		List<Method> list = new LinkedList<>();
		for (Method fm: methods) {
			if (/* fm.isPublic() && */

			(argumentLength == fm.getArgumentCount() || argumentLength < 0) &&

					(methodName == null || (nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName())))

			) {
				list.add(fm);
			}
		}
		return list;
	}

	private static RefInteger nirvana = new RefIntegerImpl();
	private Map<String, SoftReference<Pair<Method, Boolean>>> cachedMethods;

	@Override
	public Method getMethod(String methodName, Object[] args, boolean nameCaseSensitive, boolean convertArgument, boolean convertComparsion) throws NoSuchMethodException {
		Method method = getMethod(methodName, args, nameCaseSensitive, convertArgument, convertComparsion, null);
		if (method != null) return method;
		if (DEBUG) {
			StringBuilder sb = new StringBuilder();
			for (Method m: getMethods(null, true, -1)) {
				sb.append(m.toString()).append(";");
			}
			throw new NoSuchMethodException("No matching method for " + clazz.getName() + "." + methodName + "(" + Reflector.getDspMethods(Reflector.getClasses(args))
					+ ") found. Available methods are [" + sb + "]");
		}
		throw new NoSuchMethodException("No matching method for " + clazz.getName() + "." + methodName + "(" + Reflector.getDspMethods(Reflector.getClasses(args)) + ") found.");
	}

	@Override
	public Method getMethod(String methodName, Object[] args, boolean nameCaseSensitive, boolean convertArgument, boolean convertComparsion, Method defaultValue) {

		// like
		Class[] parameterTypes;
		outer: for (Method fm: methods) {
			if ((args.length == fm.getArgumentCount()) && (nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName()))) {
				parameterTypes = fm.getArgumentClasses();
				for (int y = 0; y < parameterTypes.length; y++) {
					if (!Reflector.toReferenceClass(parameterTypes[y]).isAssignableFrom(args[y] == null ? Object.class : args[y].getClass())) continue outer;
				}
				return fm;
			}
		}

		// in case there are no arguments the code below will not find any match, nothing to convert
		if (args.length == 0) return defaultValue;

		// cache
		StringBuilder sb = new StringBuilder(100).append(methodName).append(';'); // append(id()).
		for (Object arg: args) {
			sb.append((arg == null ? Object.class : arg.getClass()).getName()).append(';');
		}
		String key = sb.toString();

		// get match from cache
		if (cachedMethods != null) {
			SoftReference<Pair<Method, Boolean>> sr = cachedMethods.get(key);
			if (sr != null) {
				Pair<Method, Boolean> p = sr.get();
				if (p != null) {
					// print.e("used cached match(" + p.getValue() + "):" + key + ":" + cachedMethods.size());
					// convert arguments
					if (p.getValue()) {
						Class[] trgArgs = p.getName().getArgumentClasses();
						for (int x = 0; x < trgArgs.length; x++) {
							if (args[x] != null) {
								// we can ignore a fail here, because this was done before, otherwisse it would not be in the cache
								args[x] = Reflector.convert(args[x], Reflector.toReferenceClass(trgArgs[x]), nirvana, null);
							}
						}
					}
					return p.getName();
				}
			}
		}

		// convert comparsion
		Pair<Method, Object[]> result = null;
		int _rating = 0;
		if (convertComparsion) {
			outer: for (Method fm: methods) {
				if ((args.length == fm.getArgumentCount()) && ((nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName())))) {
					RefInteger rating = (methods.length > 1) ? new RefIntegerImpl(0) : null;
					parameterTypes = fm.getArgumentClasses();
					Object[] newArgs = new Object[args.length];

					for (int y = 0; y < parameterTypes.length; y++) {
						try {
							newArgs[y] = Reflector.convert(args[y], Reflector.toReferenceClass(parameterTypes[y]), rating);
						}
						catch (PageException e) {
							continue outer;
						}
					}
					if (result == null || rating.toInt() > _rating) {
						if (rating != null) _rating = rating.toInt();
						result = new Pair<Method, Object[]>(fm, newArgs);
					}
				}
			}
		}

		if (result != null) {
			if (convertArgument) {
				Object[] newArgs = result.getValue();
				for (int x = 0; x < args.length; x++) {
					args[x] = newArgs[x];
				}
			}
			if (cachedMethods == null) cachedMethods = new ConcurrentHashMap<>();
			cachedMethods.put(key, new SoftReference<Pair<Method, Boolean>>(new Pair<Method, Boolean>(result.getName(), Boolean.TRUE)));
			return result.getName();
		}
		return defaultValue;
	}

	@Override
	public List<Method> getDeclaredMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException {
		List<Method> list = new LinkedList<>();
		for (Method fm: declaredMethods) {
			if ((argumentLength < 0 || argumentLength == fm.getArgumentCount()) &&

					(methodName == null || (nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName())))

			/* &&clazz.getName().equals(fm.getDeclaringClassName()) */

			) {
				list.add(fm);
			}
		}
		return list;
	}

	@Override
	public List<Constructor> getConstructors(int argumentLength) throws IOException {
		List<Constructor> list = new LinkedList<>();

		for (Constructor fm: constructors) {
			if (/* fm.isPublic() && */

			(argumentLength < 0 || argumentLength == fm.getArgumentCount()) &&

					clazz.getName().equals(fm.getDeclaringClassName())

			) {
				list.add(fm);
			}
		}
		return list;
	}

	@Override
	public Constructor getConstructor(Object[] args, boolean convertArgument, boolean convertComparsion) throws NoSuchMethodException {
		Constructor constructor = getConstructor(args, convertArgument, convertComparsion, null);
		if (constructor != null) return constructor;

		throw new NoSuchMethodException("No matching Constructor for " + clazz.getName() + "(" + Reflector.getDspMethods(Reflector.getClasses(args)) + ") found.");
	}

	@Override
	public Constructor getConstructor(Object[] args, boolean convertArgument, boolean convertComparsion, Constructor defaultValue) {
		// like
		Class[] parameterTypes;
		outer: for (Constructor fm: constructors) {
			if ((args.length == fm.getArgumentCount()) && clazz.getName().equals(fm.getDeclaringClassName())) {
				parameterTypes = fm.getArgumentClasses();
				for (int y = 0; y < parameterTypes.length; y++) {
					if (!Reflector.toReferenceClass(parameterTypes[y]).isAssignableFrom(args[y] == null ? Object.class : args[y].getClass())) continue outer;
				}
				return fm;
			}
		}

		// in case there are no arguments the code below will not find any match, nothing to convert
		if (args.length == 0) return defaultValue;

		// convert comparsion
		Pair<Constructor, Object[]> result = null;
		int _rating = 0;
		if (convertComparsion) {
			outer: for (Constructor fm: constructors) {
				if ((args.length == fm.getArgumentCount()) && clazz.getName().equals(fm.getDeclaringClassName())) {
					RefInteger rating = (constructors.length > 1) ? new RefIntegerImpl(0) : null;
					parameterTypes = fm.getArgumentClasses();
					Object[] newArgs = new Object[args.length];
					for (int y = 0; y < parameterTypes.length; y++) {
						try {
							newArgs[y] = Reflector.convert(args[y], Reflector.toReferenceClass(parameterTypes[y]), rating);
						}
						catch (PageException e) {
							continue outer;
						}
					}
					if (result == null || rating.toInt() > _rating) {
						if (rating != null) _rating = rating.toInt();
						result = new Pair<Constructor, Object[]>(fm, newArgs);
					}
					// return new ConstructorInstance(constructors[i],newArgs);
				}
			}
		}

		if (result != null) {
			if (convertArgument) {
				Object[] newArgs = result.getValue();
				for (int x = 0; x < args.length; x++) {
					args[x] = newArgs[x];
				}
			}
			return result.getName();
		}
		return defaultValue;
	}

	@Override
	public List<Constructor> getDeclaredConstructors(int argumentLength) throws IOException {
		List<Constructor> list = new LinkedList<>();
		for (Constructor fm: declaredConstructors) {
			if ((argumentLength < 0 || argumentLength == fm.getArgumentCount())

			/* && clazz.getName().equals(fm.getDeclaringClassName()) */

			) {
				list.add(fm);
			}
		}
		return list;
	}

	private static Map<String, FunctionMember> getFunctionMembers(final Class clazz, Log log) throws IOException {
		final Map<String, String> classes = new ConcurrentHashMap<>();
		return _getFunctionMembers(classes, clazz, log);
	}

	private static Map<String, FunctionMember> _getFunctionMembers(Map<String, String> classes, Class clazz_, Log log) throws IOException {
		final Class clazz = clazz_.isArray() ? Object.class : clazz_;
		final Map<String, FunctionMember> members = new LinkedHashMap<>();
		Map<String, FunctionMember> existing = membersCollection.get(clazz);

		if (existing != null) {
			/*
			 * if (true) { print.e("ex-->" + clazz.getName()); for (Entry<String, FunctionMember> e:
			 * existing.entrySet()) { if (e.getValue().getDeclaringClass() != clazz &&
			 * !Reflector.isInstaneOf(clazz, e.getValue().getDeclaringClass(), true) &&
			 * e.getValue().getDeclaringClass() != Object.class) print.e("- " + e.getValue()); }
			 * 
			 * }
			 */

			for (Entry<String, FunctionMember> e: existing.entrySet()) {
				members.put(e.getKey(), e.getValue());
			}
			return members;
		}
		// print.e("ne-->" + clazz.getName());

		final String classPath = clazz.getName().replace('.', '/') + ".class";
		final ClassLoader cl = getClassLoader(clazz);
		final RefInteger classAccess = new RefIntegerImpl();
		ClassReader classReader;

		try {
			classReader = new ClassReader(cl.getResourceAsStream(classPath));
		}
		catch (IOException ioe) {
			if ("Class not found".equals(ioe.getMessage())) {
				IOException tmp = new IOException("unable to load class path [" + classPath + "]");
				ExceptionUtil.initCauseEL(tmp, ioe);
				ioe = tmp;
			}
			throw ioe;
		}

		// Create a ClassVisitor to visit the methods
		ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {

			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				classAccess.setValue(access);
				if (superName != null) {
					try {
						// add(members, _getFunctionMembers(clid,
						// cl.loadClass(ASMUtil.getClassName(Type.getObjectType(superName))), log));
						if (!classes.containsKey(superName)) {
							// print.e("->" + superName);
							classes.put(superName, "");

							add(members, _getFunctionMembers(classes, cl.loadClass(ASMUtil.getClassName(Type.getObjectType(superName))), log));
						}
					}
					catch (IllegalArgumentException iae) {
						String v = ASMUtil.getJavaVersionFromException(iae, null);
						if (v != null) {
							throw new RuntimeException("The class [" + superName + "] was compiled with Java version [" + v + "], "
									+ "which is not supported by the current version of ASM. The highest supported version is ["
									+ ASMUtil.toStringVersionFromBytceodeVersion(ASMUtil.getMaxVersion(), "unknown") + "]. ");

						}
						throw iae;
					}
					catch (RuntimeException re) {
						throw re;
					}
					catch (Exception e) {
						if (log != null) log.error("dynamic", e);
					}
				}
				if (interfaces != null && interfaces.length > 0) {
					for (String interf: interfaces) {
						try {
							if (!classes.containsKey(interf)) {
								// print.e("=>" + interf);
								classes.put(interf, "");
								// add(members, _getFunctionMembers(clid,
								// cl.loadClass(ASMUtil.getClassName(Type.getObjectType(interf))), log));

								add(members, _getFunctionMembers(classes, cl.loadClass(ASMUtil.getClassName(Type.getObjectType(interf))), log));
							}
						}
						catch (Exception e) {
							if (log != null) log.error("dynamic", e);
						}
					}
				}
				// print.e("-- " + name);
				// print.e(members);

				super.visit(version, access, name, signature, superName, interfaces);
			}

			private void add(Map<String, FunctionMember> members, Map<String, FunctionMember> add) {
				for (Entry<String, FunctionMember> e: add.entrySet()) {
					members.put(e.getKey(), e.getValue());
				}
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

				FunctionMemberDynamic fmCurrent = FunctionMemberDynamic.createInstance(clazz, name, access, descriptor, exceptions, classAccess.toInt());
				String id = Clazz.id(fmCurrent);
				// if (name.toLowerCase().indexOf("next") != -1 && name.toLowerCase().indexOf("node") == -1) {
				// print.e("======>" + clazz.getName() + ":" + name);
				// print.e(members.keySet());
				// }
				FunctionMember parent = members.get(id);
				if (parent instanceof FunctionMemberDynamic) {
					FunctionMemberDynamic fmParent = (FunctionMemberDynamic) parent;
					// java.lang.Appendable

					Class tmpClass = fmParent.getDeclaringProviderClass(true);
					if (tmpClass != null) {
						Class rtnClass = null;
						if (fmParent instanceof Method) {

							Class tmpRtn = fmParent.getDeclaringProviderRtnClass(true);
							rtnClass = tmpRtn;
						}

						/*
						 * if (name.equals("toLocalDateTime")) { print.e("xxxxxxxxxxxxxxx"); print.e(clazz + ":" + name +
						 * ":" + descriptor); // print.e(Clazz.getAccessModifier(fmCurrent)); //
						 * print.e(Clazz.getAccessModifier(fmParent)); print.e("curr: " +
						 * Clazz.getAccessModifierAsString(fmCurrent)); print.e("parr: " +
						 * Clazz.getAccessModifierAsString(fmParent)); print.e("DeclaringClassName: " +
						 * fmCurrent.getDeclaringClassName()); print.e("getDeclaringProviderClassName: " +
						 * fmCurrent.getDeclaringProviderClassNameWithSameAccess()); // print.e("-----------------" +
						 * Clazz.compareAccess(fmd, fm)); }
						 */

						Type tmpType = tmpClass != null ? Type.getType(tmpClass) : null;
						Type rtnType = rtnClass != null ? Type.getType(rtnClass) : null;

						if (Clazz.compareAccess(fmParent, fmCurrent) >= 0) fmCurrent.setDeclaringProviderClassWithSameAccess(tmpClass, tmpType, rtnClass, rtnType);
						fmCurrent.setDeclaringProviderClass(tmpClass, tmpType, rtnClass, rtnType);

						/*
						 * if (name.equals("nextElement")) { print.e(fm.getDeclaringProviderClassName());
						 * print.e(fm.getDeclaringProviderClassNameWithSameAccess()); }
						 */
					}
				}
				members.put(id, fmCurrent);

				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};
		// Start visiting the class
		classReader.accept(visitor, 0);
		membersCollection.put(clazz, cloneIt(members));
		return members;
	}

	private static Map<String, FunctionMember> _getFunctionMembers(Map<String, FunctionMember> inputMembers, String clid, final Class clazz, Log log) throws IOException {
		String key = clid + ":" + clazz.getName();

		// print.e("-->" + clazz.getName());
		// if (inputMembers != null) {
		// if (inputMembers.size() < 20) print.e(inputMembers.keySet());
		// else print.e(inputMembers.size());
		// }

		if (inputMembers == null) inputMembers = new LinkedHashMap<>();
		final Map<String, FunctionMember> members = inputMembers;
		Map<String, FunctionMember> existing = membersCollectionOld.get(key);

		if (existing != null) {
			for (Entry<String, FunctionMember> e: existing.entrySet()) {
				members.put(e.getKey(), e.getValue());
			}
			return members;
		}

		final String classPath = clazz.getName().replace('.', '/') + ".class";
		final ClassLoader cl = getClassLoader(clazz);
		final RefInteger classAccess = new RefIntegerImpl();
		ClassReader classReader;

		try {
			classReader = new ClassReader(cl.getResourceAsStream(classPath));
		}
		catch (IOException ioe) {
			if ("Class not found".equals(ioe.getMessage())) {
				IOException tmp = new IOException("unable to load class path [" + classPath + "]");
				ExceptionUtil.initCauseEL(tmp, ioe);
				ioe = tmp;
			}
			throw ioe;
		}

		// Create a ClassVisitor to visit the methods
		ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {

			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				classAccess.setValue(access);
				if (superName != null) {
					try {
						// add(members, _getFunctionMembers(clid,
						// cl.loadClass(ASMUtil.getClassName(Type.getObjectType(superName))), log));
						_getFunctionMembers(members, clid, cl.loadClass(ASMUtil.getClassName(Type.getObjectType(superName))), log);
					}
					catch (IllegalArgumentException iae) {
						String v = ASMUtil.getJavaVersionFromException(iae, null);
						if (v != null) {
							throw new RuntimeException("The class [" + superName + "] was compiled with Java version [" + v + "], "
									+ "which is not supported by the current version of ASM. The highest supported version is ["
									+ ASMUtil.toStringVersionFromBytceodeVersion(ASMUtil.getMaxVersion(), "unknown") + "]. ");

						}
						throw iae;
					}
					catch (RuntimeException re) {
						throw re;
					}
					catch (Exception e) {
						if (log != null) log.error("dynamic", e);
					}
				}
				if (interfaces != null && interfaces.length > 0) {
					for (String interf: interfaces) {
						try {
							// add(members, _getFunctionMembers(clid,
							// cl.loadClass(ASMUtil.getClassName(Type.getObjectType(interf))), log));
							_getFunctionMembers(members, clid, cl.loadClass(ASMUtil.getClassName(Type.getObjectType(interf))), log);
						}
						catch (Exception e) {
							if (log != null) log.error("dynamic", e);
						}
					}
				}
				// print.e("-- " + name);
				// print.e(members);

				super.visit(version, access, name, signature, superName, interfaces);
			}

			private void add(Map<String, FunctionMember> members, Map<String, FunctionMember> add) {
				for (Entry<String, FunctionMember> e: add.entrySet()) {
					members.put(e.getKey(), e.getValue());
				}
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

				FunctionMemberDynamic fmCurrent = FunctionMemberDynamic.createInstance(clazz, name, access, descriptor, exceptions, classAccess.toInt());
				String id = Clazz.id(fmCurrent);
				// if (name.toLowerCase().indexOf("next") != -1 && name.toLowerCase().indexOf("node") == -1) {
				// print.e("======>" + clazz.getName() + ":" + name);
				// print.e(members.keySet());
				// }
				FunctionMember parent = members.get(id);
				if (parent instanceof FunctionMemberDynamic) {
					FunctionMemberDynamic fmParent = (FunctionMemberDynamic) parent;
					// java.lang.Appendable

					Class tmpClass = fmParent.getDeclaringProviderClass(true);
					if (tmpClass != null) {
						Class rtnClass = null;
						if (fmParent instanceof Method) {

							Class tmpRtn = fmParent.getDeclaringProviderRtnClass(true);
							rtnClass = tmpRtn;
						}

						/*
						 * if (name.equals("toLocalDateTime")) { print.e("xxxxxxxxxxxxxxx"); print.e(clazz + ":" + name +
						 * ":" + descriptor); // print.e(Clazz.getAccessModifier(fmCurrent)); //
						 * print.e(Clazz.getAccessModifier(fmParent)); print.e("curr: " +
						 * Clazz.getAccessModifierAsString(fmCurrent)); print.e("parr: " +
						 * Clazz.getAccessModifierAsString(fmParent)); print.e("DeclaringClassName: " +
						 * fmCurrent.getDeclaringClassName()); print.e("getDeclaringProviderClassName: " +
						 * fmCurrent.getDeclaringProviderClassNameWithSameAccess()); // print.e("-----------------" +
						 * Clazz.compareAccess(fmd, fm)); }
						 */

						Type tmpType = tmpClass != null ? Type.getType(tmpClass) : null;
						Type rtnType = rtnClass != null ? Type.getType(rtnClass) : null;

						if (Clazz.compareAccess(fmParent, fmCurrent) >= 0) fmCurrent.setDeclaringProviderClassWithSameAccess(tmpClass, tmpType, rtnClass, rtnType);
						fmCurrent.setDeclaringProviderClass(tmpClass, tmpType, rtnClass, rtnType);

						/*
						 * if (name.equals("nextElement")) { print.e(fm.getDeclaringProviderClassName());
						 * print.e(fm.getDeclaringProviderClassNameWithSameAccess()); }
						 */
					}
				}
				members.put(id, fmCurrent);

				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};
		// Start visiting the class
		classReader.accept(visitor, 0);
		membersCollectionOld.put(key, cloneIt(members));
		return members;
	}

	private static Map<String, FunctionMember> cloneIt(Map<String, FunctionMember> members) {
		Map<String, FunctionMember> cloned = new LinkedHashMap<>();
		for (Entry<String, FunctionMember> e: members.entrySet()) {
			cloned.put(e.getKey(), e.getValue());
		}
		return cloned;
	}

	private static Map<String, Map<String, FunctionMember>> membersCollectionOld = new ConcurrentHashMap<>();
	private static Map<Class, Map<String, FunctionMember>> membersCollection = new IdentityHashMap<>();

	public static void serialize(Serializable o, OutputStream os) throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(os);
			oos.writeObject(o);
		}
		finally {
			IOUtil.close(oos, os);
		}
	}

	private static Object deserializeAsync(ClassLoader cl, InputStream is, String path) throws TimeoutException, InterruptedException, ExecutionException {
		return ThreadUtil.createExecutorService().submit(() -> {
			ObjectInputStream ois = null;
			Object o;
			try {
				ois = new ObjectInputStreamImpl(cl, is);
				o = ois.readObject();
			}
			finally {
				IOUtil.close(ois);
			}
			return o;
		}).get(10, TimeUnit.MILLISECONDS);
	}

	private static Object deserialize(ClassLoader cl, InputStream is) throws IOException, ClassNotFoundException {

		ObjectInputStream ois = null;
		Object o = null;
		try {
			ois = new ObjectInputStreamImpl(cl, is);
			o = ois.readObject();
		}
		finally {
			IOUtil.close(ois);
		}
		return o;
	}
}
