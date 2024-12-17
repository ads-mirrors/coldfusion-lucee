package lucee.transformer.dynamic.meta;

import java.io.Serializable;

import org.objectweb.asm.Type;

import lucee.commons.digest.HashUtil;

public interface FunctionMember extends Serializable {

	public abstract String getName();

	public abstract String getDeclaringProviderClassNameWithSameAccess();

	public abstract Class getDeclaringProviderClassWithSameAccess();

	public abstract String getDeclaringProviderClassName();

	public abstract Class getDeclaringProviderClass();

	public abstract String getDeclaringClassName();

	public abstract Class getDeclaringClass();

	public abstract boolean isPublic();

	public abstract boolean isProtected();

	public abstract boolean isPrivate();

	public abstract boolean isDefault();

	public abstract boolean isStatic();

	public abstract boolean isAbstract();

	public abstract boolean isFinal();

	public abstract boolean isNative();

	public abstract String[] getArguments();

	public abstract int getArgumentCount();

	public abstract Class[] getArgumentClasses();

	public abstract Type[] getArgumentTypes();

	public abstract String[] getExceptions();

	public abstract boolean inInterface();

	public abstract String getClassPath();

	public abstract String getClassName();

	public static String createClassPath(FunctionMember fm) {
		StringBuilder sbClassPath = new StringBuilder();
		// sbClassPath.append(getDeclaringClassName().replace('.', '/')).append('/').append(isConstr ?
		// "____init____" : fm.getName());
		sbClassPath.append(fm.getDeclaringClassName().replace('.', '/')).append('/');
		if (fm.getName() == null) sbClassPath.append("____init____");
		else sbClassPath.append(fm.getName());

		if (fm.getArgumentCount() > 0) {
			StringBuilder sbArgs = new StringBuilder();
			for (String arg: fm.getArguments()) {
				sbArgs.append(':').append(arg);
			}
			sbClassPath.append('_').append(HashUtil.create64BitHashAsString(sbArgs, Character.MAX_RADIX));
		}
		return Clazz.getPackagePrefix() + sbClassPath.toString();
	}
}
