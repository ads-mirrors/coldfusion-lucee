package lucee.transformer.dynamic.meta;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.Type;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.dynamic.DynamicClassLoader;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.dynamic.ClazzDynamic;
import lucee.transformer.dynamic.meta.reflection.ClazzReflection;

public abstract class Clazz implements Serializable {
	private static final boolean debug = false;
	public static final int VERSION = 5;

	private static final long serialVersionUID = 4236939474343760825L;
	private static Boolean allowReflection = null;

	private DynamicClassLoader dcl;

	public abstract List<Method> getMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException;

	public abstract List<Method> getDeclaredMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException;

	public abstract List<Constructor> getConstructors(int argumentLength) throws IOException;

	public abstract List<Constructor> getDeclaredConstructors(int argumentLength) throws IOException;

	public abstract Method getDeclaredMethod(String methodName, Class[] arguments, boolean nameCaseSensitive) throws IOException, NoSuchMethodException;

	public abstract Method getMethod(String methodName, Class[] arguments, boolean nameCaseSensitive) throws IOException, NoSuchMethodException;

	public abstract Method getMethod(String methodName, Class[] arguments, boolean nameCaseSensitive, Method defaultValue);

	public abstract Method getMethod(String methodName, Object[] args, boolean nameCaseSensitive, boolean convertArgument, boolean convertComparsion) throws NoSuchMethodException;

	public abstract Method getMethod(String methodName, Object[] args, boolean nameCaseSensitive, boolean convertArgument, boolean convertComparsion, Method defaultValue);

	public abstract Constructor getConstructor(Class[] arguments) throws IOException, NoSuchMethodException;

	public abstract Constructor getConstructor(Object[] args, boolean convertArgument, boolean convertComparsion) throws NoSuchMethodException;

	public abstract Constructor getConstructor(Object[] args, boolean convertArgument, boolean convertComparsion, Constructor defaultValue);

	public abstract Constructor getDeclaredConstructor(Class[] arguments) throws IOException, NoSuchMethodException;

	public abstract Class getDeclaringClass();

	public abstract Type getDeclaringType();

	public abstract String id();

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
			if (log != null) log.error("dynamic", e);
			if (allowReflection()) return new ClazzReflection(clazz);
			else throw new RuntimeException(e);
		}
	}

	private static RefInteger nirvana = new RefIntegerImpl();

	/*
	 * private static double cleanArgs = 0; private static double checkAccessibility = 0; private static
	 * double lmethods = 0; private static double cache = 0; private static double exact = 0; private
	 * static double like = 0; private static double convert = 0; private static double lclasses = 0;
	 * private static double lclasses2 = 0; private static int count = 0;
	 */

	private static Map<String, SoftReference<Pair<Method, Boolean>>> cachedMethods = new ConcurrentHashMap<>();

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

	public DynamicClassLoader getDynamicClassLoader(DynamicInvoker dynamicInvoker) {
		if (dcl == null) {
			dcl = dynamicInvoker.getCL(getDeclaringClass());
		}
		return dcl;
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