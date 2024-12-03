package lucee.transformer.dynamic.meta;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.Type;

import lucee.print;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.java.JavaObject;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.dynamic.meta.dynamic.ClazzDynamic;
import lucee.transformer.dynamic.meta.reflection.ClazzReflection;

public abstract class Clazz implements Serializable {

	public static final int VERSION = 3;

	private static final long serialVersionUID = 4236939474343760825L;
	private static Boolean allowReflection = null;

	public abstract List<Method> getMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException;

	public abstract List<Method> getDeclaredMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException;

	public abstract List<Constructor> getConstructors(int argumentLength) throws IOException;

	public abstract List<Constructor> getDeclaredConstructors(int argumentLength) throws IOException;

	public abstract Method getDeclaredMethod(String methodName, Class[] arguments, boolean nameCaseSensitive) throws IOException, NoSuchMethodException;

	public abstract Method getMethod(String methodName, Class[] arguments, boolean nameCaseSensitive) throws IOException, NoSuchMethodException;

	public abstract Constructor getConstructor(Class[] arguments) throws IOException, NoSuchMethodException;

	public abstract Constructor getDeclaredConstructor(Class[] arguments) throws IOException, NoSuchMethodException;

	public abstract Class getDeclaringClass();

	public abstract Type getDeclaringType();

	protected abstract String id();

	public static boolean allowReflection() {
		if (allowReflection == null) {
			allowReflection = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.allow.reflection", null), false);
		}
		return allowReflection;
	}

	public static Clazz getClazzReflection(Class clazz) {
		return new ClazzReflection(clazz);
	}

	public static Clazz getClazz(Class clazz, Resource root, Log log, boolean useReflection) {
		if (useReflection) return new ClazzReflection(clazz);
		return getClazz(clazz, root, log);
	}

	public static Clazz getClazz(Class clazz, Resource root, Log log) {
		try {
			return ClazzDynamic.getInstance(clazz, root, log);
		}
		catch (Exception e) {
			print.ds("xxxxxxxxxxxxxxxxx " + clazz);
			if (log != null) log.error("dynamic", e);
			if (allowReflection()) return new ClazzReflection(clazz);
			else throw new RuntimeException(e);
		}
	}

	private static Map<String, SoftReference<Pair<Method, Boolean>>> cachedMethods = new ConcurrentHashMap<>();
	private static RefInteger nirvana = new RefIntegerImpl();

	private static double cleanArgs = 0;
	private static double checkAccessibility = 0;
	private static double lmethods = 0;
	private static double cache = 0;
	private static double exact = 0;
	private static double like = 0;
	private static double convert = 0;
	private static double lclasses = 0;
	private static double lclasses2 = 0;
	private static int count = 0;

	public static Method getMethodMatch(Clazz clazz, final Collection.Key methodName, Object[] args, boolean convertArgument, boolean convertComparsion)
			throws NoSuchMethodException, IOException, PageException {

		count++;
		if ((count % 500) == 0) {
			print.e("-------------------");
			print.e("cleanArgs:" + cleanArgs);
			print.e("checkAccessibility:" + checkAccessibility);
			print.e("lmethods:" + lmethods);
			print.e("cache:" + cache);
			print.e("exact:" + exact);
			print.e("like:" + like);
			print.e("convert:" + convert);
			print.e("lclasses:" + lclasses);
			print.e("lclasses2:" + lclasses2);
		}
		double start = SystemUtil.millis();

		args = Reflector.cleanArgs(args);

		cleanArgs += (SystemUtil.millis() - start);
		start = SystemUtil.millis();
		List<Method> methods = clazz.getMethods(methodName.getString(), false, args.length);

		lmethods += (SystemUtil.millis() - start);
		start = SystemUtil.millis();

		if (methods != null && methods.size() > 0) {
			Class[] clazzArgs = Reflector.getClasses(args);

			// cache
			StringBuilder sb = new StringBuilder(clazz.id()).append(methodName).append(';');
			for (Class cls: clazzArgs) {
				sb.append(cls.getName()).append(';');
			}

			String key = sb.toString();

			// get match from cache
			SoftReference<Pair<Method, Boolean>> sr = cachedMethods.get(key);
			if (sr != null) {
				Pair<Method, Boolean> p = sr.get();
				if (p != null) {
					// print.e("used cached match(" + p.getValue() + "):" + key + ":" + cachedMethods.size());
					// convert arguments
					if (p.getValue()) {
						// print.e("------- " + clazz.getDeclaringClass().getName() + ":" + methodName + " -----");
						Class[] trgArgs = p.getName().getArgumentClasses();
						for (int x = 0; x < trgArgs.length; x++) {
							if (args[x] != null) args[x] = Reflector.convert(args[x], Reflector.toReferenceClass(trgArgs[x]), nirvana);
						}
					}
					return p.getName();
				}
			}
			cache += (SystemUtil.millis() - start);
			start = SystemUtil.millis();

			Reflector.checkAccessibility(clazz.getDeclaringClass(), methodName);

			checkAccessibility += (SystemUtil.millis() - start);
			start = SystemUtil.millis();

			// exact comparsion
			outer: for (Method m: methods) {
				if (m != null) {
					Class[] parameterTypes = m.getArgumentClasses();
					for (int y = 0; y < parameterTypes.length; y++) {
						if (Reflector.toReferenceClass(parameterTypes[y]) != clazzArgs[y]) continue outer;
					}
					// print.e("exact match:" + key + ":");
					cachedMethods.put(key, new SoftReference<Pair<Method, Boolean>>(new Pair<Method, Boolean>(m, Boolean.FALSE)));
					return m;
				}
			}
			exact += (SystemUtil.millis() - start);
			start = SystemUtil.millis();

			// like comparsion
			outer: for (Method m: methods) {
				if (m != null) {
					Class[] parameterTypes = m.getArgumentClasses();
					for (int y = 0; y < parameterTypes.length; y++) {
						if (!Reflector.like(clazzArgs[y], Reflector.toReferenceClass(parameterTypes[y]))) continue outer;
					}
					// print.e("like match:" + key + ":");
					cachedMethods.put(key, new SoftReference<Pair<Method, Boolean>>(new Pair<Method, Boolean>(m, Boolean.FALSE)));
					return m;
				}
			}
			like += (SystemUtil.millis() - start);
			start = SystemUtil.millis();

			// convert comparsion
			Pair<Method, Object[]> result = null;
			int _rating = 0;
			if (convertComparsion) {
				outer: for (Method m: methods) {
					if (m != null) {
						RefInteger rating = (methods.size() > 1) ? new RefIntegerImpl(0) : null;
						Class[] parameterTypes = m.getArgumentClasses();
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
							result = new Pair<Method, Object[]>(m, newArgs);
						}
					}
				}
			}
			convert += (SystemUtil.millis() - start);
			start = SystemUtil.millis();
			if (result != null) {
				if (convertArgument) {
					Object[] newArgs = result.getValue();
					for (int x = 0; x < args.length; x++) {
						args[x] = newArgs[x];
					}
				}
				// print.e("conv match:" + key + ":");
				cachedMethods.put(key, new SoftReference<Pair<Method, Boolean>>(new Pair<Method, Boolean>(result.getName(), Boolean.TRUE)));
				return result.getName();
			}
		}
		Class[] classes = Reflector.getClasses(args);
		// StringBuilder sb=null;
		JavaObject jo;
		Class c;
		Constructor cc;
		for (int i = 0; i < classes.length; i++) {
			if (args[i] instanceof JavaObject) {
				jo = (JavaObject) args[i];
				c = jo.getClazz();
				cc = Reflector.getConstructorInstance(c, new Object[0], true).getConstructor(null);
				if (cc == null) {

					throw new NoSuchMethodException(
							"The " + Reflector.pos(i + 1) + " parameter of " + methodName + "(" + Reflector.getDspMethods(classes) + ") ia an object created "
									+ "by the createObject function (JavaObject/JavaProxy). This object has not been instantiated because it does not have a constructor "
									+ "that takes zero arguments. " + Constants.NAME
									+ " cannot instantiate it for you, please use the .init(...) method to instantiate it with the correct parameters first");

				}
			}
		}

		lclasses += (SystemUtil.millis() - start);
		start = SystemUtil.millis();
		/*
		 * the argument list contains objects created by createObject, that are no instantiated
		 * (first,third,10th) and because this object have no constructor taking no arguments, Lucee cannot
		 * instantiate them. you need first to instantiate this objects.
		 */

		Class[] trgArgs = Reflector.getClasses(args);
		String strTrgArgs = Reflector.getDspMethods(trgArgs);
		StringBuilder msg = new StringBuilder();
		msg.append("No matching method for ").append(lucee.runtime.type.util.Type.getName(clazz.getDeclaringClass())).append(".").append(methodName).append("(").append(strTrgArgs)
				.append(") found. ");
		if (methods.size() > 0) {
			msg.append("there are similar methods with the same name, but diferent arguments:\n ");
			Class[] srcArgs;
			String strSrcArgs;
			for (Method m: methods) {
				srcArgs = m.getArgumentClasses();
				strSrcArgs = Reflector.getDspMethods(srcArgs);
				if (strSrcArgs.equals(strTrgArgs)) {
					ClassLoader srcClassLoader = null;
					ClassLoader trgClassLoader = null;
					int index = -1;
					for (int i = 0; i < srcArgs.length; i++) {
						if (srcArgs[i].getClassLoader() == trgArgs[i].getClassLoader()) continue;
						index = i;
						srcClassLoader = srcArgs[i].getClassLoader();
						trgClassLoader = trgArgs[i].getClassLoader();
						break;
					}
					String srcClassLoaderName = "Bootstrap ClassLoader";
					if (srcClassLoader instanceof PhysicalClassLoader) {
						srcClassLoaderName = "PhysicalClassLoader loaded at " + ((PhysicalClassLoader) srcClassLoader).getBirthplace();
					}
					else if (srcClassLoader != null) {
						srcClassLoaderName = srcClassLoader.toString();
					}
					String trgClassLoaderName = "Bootstrap ClassLoader";
					if (trgClassLoader instanceof PhysicalClassLoader) {
						trgClassLoaderName = "PhysicalClassLoader loaded at " + ((PhysicalClassLoader) trgClassLoader).getBirthplace();
					}
					else if (trgClassLoader != null) {
						trgClassLoaderName = srcClassLoader.toString();
					}

					if (index != -1) throw new NoSuchMethodException("Found a matching method for [" + lucee.runtime.type.util.Type.getName(clazz.getDeclaringClass()) + "("
							+ strSrcArgs + ")], but the classes were loaded by different class loaders. \n" + "The " + FunctionException.toStringBadArgumentPosition(index + 1)
							+ " argument [" + lucee.runtime.type.util.Type.getName(srcArgs[index]) + "] was loaded by the class loader [" + trgClassLoaderName
							+ "], but the provided argument is from the class loader [" + srcClassLoaderName + "]."
							+ " Ensure that both classes are loaded by the same class loader to avoid conflicts.");

				}
				msg.append(methodName).append('(').append(strSrcArgs).append(");\n");
			}

		}
		else {
			msg.append("there are no methods with this name.");
		}
		lclasses2 += (SystemUtil.millis() - start);
		start = SystemUtil.millis();
		throw new NoSuchMethodException(msg.toString());
	}

	public static Constructor getConstructorMatch(Clazz clazz, Object[] args, boolean convertArgument, boolean convertComparsion) throws NoSuchMethodException, IOException {
		args = Reflector.cleanArgs(args);
		List<Constructor> constructors = clazz.getConstructors(args.length);
		if (constructors != null && constructors.size() > 0) {
			Class[] clazzArgs = Reflector.getClasses(args);
			// exact comparsion
			outer: for (Constructor c: constructors) {
				if (c != null) {

					Class[] parameterTypes = c.getArgumentClasses();
					for (int y = 0; y < parameterTypes.length; y++) {
						if (Reflector.toReferenceClass(parameterTypes[y]) != clazzArgs[y]) continue outer;
					}
					return c;
				}
			}
			// like comparsion
			outer: for (Constructor c: constructors) {
				if (c != null) {
					Class[] parameterTypes = c.getArgumentClasses();
					for (int y = 0; y < parameterTypes.length; y++) {
						if (!Reflector.like(clazzArgs[y], Reflector.toReferenceClass(parameterTypes[y]))) continue outer;
					}
					return c;
				}
			}
			// convert comparsion
			Pair<Constructor, Object[]> result = null;
			int _rating = 0;
			if (convertComparsion) {
				outer: for (Constructor c: constructors) {
					if (c != null) {
						RefInteger rating = (constructors.size() > 1) ? new RefIntegerImpl(0) : null;
						Class[] parameterTypes = c.getArgumentClasses();
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
							result = new Pair<Constructor, Object[]>(c, newArgs);
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

			// Exception
			StringBuilder msg = new StringBuilder("No matching Constructor for ").append(clazz.getDeclaringClass().getName()).append("(")
					.append(Reflector.getDspMethods(Reflector.getClasses(args))).append(") found.\n").append("We have the following constructors:\n");
			for (Constructor c: constructors) {
				msg.append("- <init>(").append(Reflector.getDspMethods(c.getArgumentClasses())).append(")\n");
			}
			throw new NoSuchMethodException(msg.toString());
		}

		throw new NoSuchMethodException(
				"No matching Constructor for " + clazz.getDeclaringClass().getName() + "(" + Reflector.getDspMethods(Reflector.getClasses(args)) + ") found.");
	}

	public static String id(FunctionMember fm) {
		// public java.lang.String java.lang.String.toString()
		StringBuilder sb = new StringBuilder();

		// name
		sb.append(fm.getName())

				.append('(');

		// arguments;
		String[] args = fm.getArguments();
		if (args != null && args.length > 0) {
			for (String cn: args) {
				sb.append(cn).append(';');
			}
		}

		sb.append(")");

		return sb.toString();
	}

	public static String getAccessModifierAsString(FunctionMember fm) {
		if (fm.isPublic()) return "public";
		if (fm.isProtected()) return "protected";
		if (fm.isPrivate()) return "private";

		return "";
	}

	public static ClassLoader getClassLoader(Class clazz) {
		ClassLoader tmp = clazz.getClassLoader();
		if (tmp == null) tmp = ClassLoader.getSystemClassLoader();
		return tmp;
	}

	public static Type[] toTypes(Class[] classes) {
		Type[] types = new Type[classes.length];
		for (int i = 0; i < classes.length; i++) {
			types[i] = Type.getType(classes[i]);
		}
		return types;
	}

	public static String toTypeNames(Class[] classes) {
		StringBuilder sb = new StringBuilder();
		String del = "";
		for (int i = 0; i < classes.length; i++) {
			sb.append(del).append(Caster.toTypeName(classes[i]));
			del = ",";
		}
		return sb.toString();
	}

	public static boolean isPrimitive(Type type) {
		int sort = type.getSort();
		return sort >= Type.BOOLEAN && sort <= Type.DOUBLE;
	}

	/**
	 * Compares two FunctionMember objects based on their access modifiers. The comparison is done in
	 * the order of access levels from most restrictive to least restrictive: private (1), default (2),
	 * protected (3), and public (4).
	 *
	 * @param left the first FunctionMember to compare
	 * @param right the second FunctionMember to compare
	 * @return a negative integer, zero, or a positive integer if the access level of the left
	 *         FunctionMember is less than, equal to, or greater than the access level of the right
	 *         FunctionMember.
	 */
	public static int compareAccess(FunctionMember left, FunctionMember right) {
		int l = 1, r = 1;

		if (left.isPublic()) l = 4;
		else if (left.isProtected()) l = 3;
		else if (left.isDefault()) l = 2;

		if (right.isPublic()) r = 4;
		else if (right.isProtected()) r = 3;
		else if (right.isDefault()) r = 2;

		return l - r;

	}

	public static int getAccessModifier(FunctionMember fm) {
		if (fm.isPublic()) return 4;
		if (fm.isProtected()) return 3;
		if (fm.isPrivate()) return 1;
		return 2;
	}

	public static int getAccessModifier(int reflectionAccess) {
		if (Modifier.isPublic(reflectionAccess)) return 4;
		if (Modifier.isProtected(reflectionAccess)) return 3;
		if (Modifier.isPrivate(reflectionAccess)) return 1;
		return 2;
	}

	public static Class toClass(ClassLoader cl, Type type) throws ClassException {
		return ClassUtil.loadClass(cl, ASMUtil.getClassName(type));
	}

	public static String getPackagePrefix() {
		return "lucee/invoc/wrap/v" + VERSION + "/";
	}
}
