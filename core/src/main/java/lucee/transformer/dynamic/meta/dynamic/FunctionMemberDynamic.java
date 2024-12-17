package lucee.transformer.dynamic.meta.dynamic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.StringUtil;
import lucee.runtime.converter.JavaConverter.ObjectInputStreamImpl;
import lucee.runtime.exp.PageRuntimeException;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.FunctionMember;

abstract class FunctionMemberDynamic implements FunctionMember {

	private static final long serialVersionUID = 4596750766856217930L;

	protected String name;
	protected int access;
	protected int classAccess;

	protected transient Type declaringType;
	protected transient Class declaringClass;

	protected transient Type declaringProviderType;
	protected transient Class declaringProviderClass;

	protected transient Type declaringProviderRtnType;
	protected transient Class declaringProviderRtnClass;

	protected transient Type declaringProviderTypeWithSameAccess;
	protected transient Class declaringProviderClassWithSameAccess;

	protected transient Type declaringProviderRtnTypeWithSameAccess;
	protected transient Class declaringProviderRtnClassWithSameAccess;

	protected transient Type rtnType;
	protected transient String rtnName;
	protected transient Class rtnClass;
	protected transient Type[] argTypes;
	protected transient String[] argNames;
	protected transient Class[] argClasses;

	protected transient Type[] expTypes;
	protected transient String[] expNames;

	private String classPath;
	private String className;

	public FunctionMemberDynamic(String name) {
		this.name = name;
	}

	// loaded by object evaluation
	public FunctionMemberDynamic() {

	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();

		out.writeObject(name);
		out.writeInt(access);
		out.writeInt(classAccess);

		// declaring class
		out.writeObject(ASMUtil.getDescriptor(declaringType));
		// declaring provider class
		out.writeObject(declaringProviderType == null ? null : ASMUtil.getDescriptor(declaringProviderType));
		out.writeObject(declaringProviderRtnType == null ? null : ASMUtil.getDescriptor(declaringProviderRtnType));

		out.writeObject(declaringProviderTypeWithSameAccess == null ? null : ASMUtil.getDescriptor(declaringProviderTypeWithSameAccess));
		out.writeObject(declaringProviderRtnTypeWithSameAccess == null ? null : ASMUtil.getDescriptor(declaringProviderRtnTypeWithSameAccess));

		// return
		out.writeObject(ASMUtil.getDescriptor(rtnType));

		// arguments
		String[] arr = new String[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			arr[i] = ASMUtil.getDescriptor(argTypes[i]);
		}
		out.writeObject(arr);

		// exception
		arr = new String[expTypes.length];
		for (int i = 0; i < expTypes.length; i++) {
			arr[i] = ASMUtil.getDescriptor(expTypes[i]);
		}
		out.writeObject(arr);

	}

	// protected transient Type declaringType;
	// protected transient Type rtnType;
	// protected transient Type[] argTypes;
	// protected transient Type[] expTypes;
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		ClassLoader cl = ((ObjectInputStreamImpl) in).getClassLoader();
		in.defaultReadObject();

		// name
		this.name = (String) in.readObject();
		// access
		this.access = in.readInt();
		// inInterface
		this.classAccess = in.readInt();

		// declaring class
		this.declaringType = Type.getType((String) in.readObject());
		this.declaringClass = Clazz.toClass(cl, this.declaringType);
		// declaring class provider
		Object obj = in.readObject();
		if (obj != null) {
			this.declaringProviderType = Type.getType((String) obj);
			this.declaringProviderClass = Clazz.toClass(cl, this.declaringProviderType);
		}

		obj = in.readObject();
		if (obj != null) {
			this.declaringProviderRtnType = Type.getType((String) obj);
			this.declaringProviderRtnClass = Clazz.toClass(cl, this.declaringProviderRtnType);
		}

		obj = in.readObject();
		if (obj != null) {
			this.declaringProviderTypeWithSameAccess = Type.getType((String) obj);
			this.declaringProviderClassWithSameAccess = Clazz.toClass(cl, this.declaringProviderTypeWithSameAccess);
		}
		obj = in.readObject();
		if (obj != null) {
			this.declaringProviderRtnTypeWithSameAccess = Type.getType((String) obj);
			this.declaringProviderRtnClassWithSameAccess = Clazz.toClass(cl, this.declaringProviderRtnTypeWithSameAccess);
		}

		// declaring class
		this.rtnType = Type.getType((String) in.readObject());

		// arguments
		String[] arr = (String[]) in.readObject();
		this.argTypes = new Type[arr.length];
		for (int i = 0; i < argTypes.length; i++) {
			argTypes[i] = Type.getType(arr[i]);
		}

		// exception
		arr = (String[]) in.readObject();
		this.expTypes = new Type[arr.length];
		for (int i = 0; i < expTypes.length; i++) {
			expTypes[i] = Type.getType(arr[i]);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public static FunctionMemberDynamic createInstance(Class declaringClass, String name, int access, String descriptor, String[] exceptions, int classAccess) {
		FunctionMemberDynamic fm;
		if ("<init>".equals(name)) {
			fm = new ConstructorDynamic();
		}
		else {
			fm = new MethodDynamic(declaringClass, name);
		}

		fm.classAccess = classAccess;

		// declaring class
		fm.declaringClass = declaringClass;
		fm.declaringType = Type.getType(declaringClass);

		// return
		fm.rtnType = Type.getReturnType(descriptor);

		// arguments
		fm.argTypes = Type.getArgumentTypes(descriptor);
		if (fm.argTypes == null) fm.argTypes = new Type[0];

		// exceptions

		if (exceptions != null) {
			fm.expTypes = new Type[exceptions.length];
			for (int i = 0; i < exceptions.length; i++) {
				fm.expTypes[i] = Type.getObjectType(exceptions[i]);
			}
		}
		else {
			fm.expTypes = new Type[0];
		}

		// access
		fm.access = access;

		return fm;
	}

	@Override
	public String getDeclaringClassName() {
		return ASMUtil.getClassName(declaringType);
	}

	@Override
	public Class getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public String getDeclaringProviderClassName() {
		return getDeclaringProviderClass().getName();
	}

	@Override
	public Class getDeclaringProviderClass() {
		return getDeclaringProviderClass(false);
	}

	public Class getDeclaringProviderClass(boolean onlyPublic) {
		if (declaringProviderClass == null && (!onlyPublic || isDeclaringClassPublic())) {
			return getDeclaringClass();
		}
		return declaringProviderClass;
	}

	public String getDeclaringProviderRtnClassName() {
		return getDeclaringProviderClass().getName();
	}

	public Class getDeclaringProviderRtnClass() {
		return getDeclaringProviderClass(false);
	}

	public Class getDeclaringProviderRtnClass(boolean onlyPublic) {
		if (declaringProviderRtnClass == null && (!onlyPublic || isDeclaringClassPublic())) {
			return getReturnClass();
		}
		return declaringProviderRtnClass;
	}

	public String getDeclaringProviderRtnClassNameWithSameAccess() {
		return getDeclaringProviderRtnClassWithSameAccess().getName();
	}

	public Class getDeclaringProviderRtnClassWithSameAccess() {
		return getDeclaringProviderRtnClassWithSameAccess(false);
	}

	public Class getDeclaringProviderRtnClassWithSameAccess(boolean onlyPublic) {
		if (declaringProviderRtnClassWithSameAccess == null && (!onlyPublic || isDeclaringClassPublic())) {
			return getReturnClass();
		}
		return declaringProviderRtnClassWithSameAccess;
	}

	public void setDeclaringProviderClass(Class declaringProviderClass, Type declaringProviderType, Class declaringProviderRtnClass, Type declaringProviderRtnType) {
		this.declaringProviderClass = declaringProviderClass;
		this.declaringProviderType = declaringProviderType;
		this.declaringProviderRtnClass = declaringProviderRtnClass;
		this.declaringProviderRtnType = declaringProviderRtnType;
	}

	public void setDeclaringProviderClassWithSameAccess(Class declaringProviderClassWithSameAccess, Type declaringProviderTypeWithSameAccess,
			Class declaringProviderRtnClassWithSameAccess, Type declaringProviderRtnTypeWithSameAccess) {
		this.declaringProviderClassWithSameAccess = declaringProviderClassWithSameAccess;
		this.declaringProviderTypeWithSameAccess = declaringProviderTypeWithSameAccess;
		this.declaringProviderRtnClassWithSameAccess = declaringProviderRtnClassWithSameAccess;
		this.declaringProviderRtnTypeWithSameAccess = declaringProviderRtnTypeWithSameAccess;
	}

	@Override
	public String getDeclaringProviderClassNameWithSameAccess() {
		return getDeclaringProviderClassWithSameAccess().getName();
	}

	@Override
	public Class getDeclaringProviderClassWithSameAccess() {
		return getDeclaringProviderClassWithSameAccess(false);
	}

	public Class getDeclaringProviderClassWithSameAccess(boolean onlyPublic) {
		if (declaringProviderClassWithSameAccess == null && (!onlyPublic || isDeclaringClassPublic())) {
			return getDeclaringClass();
		}
		return declaringProviderClassWithSameAccess;
	}

	@Override
	public boolean isPublic() {
		return (access & Opcodes.ACC_PUBLIC) != 0;
	}

	@Override
	public boolean isProtected() {
		return (access & Opcodes.ACC_PROTECTED) != 0;
	}

	@Override
	public boolean isPrivate() {
		return (access & Opcodes.ACC_PRIVATE) != 0;
	}

	@Override
	public boolean isDefault() {
		return (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE)) == 0;
		// return !(isPublic() || isProtected() || isPrivate());
	}

	@Override
	public boolean isStatic() {
		return (access & Opcodes.ACC_STATIC) != 0;
	}

	@Override
	public boolean isAbstract() {
		return (access & Opcodes.ACC_ABSTRACT) != 0;
	}

	@Override
	public boolean isFinal() {
		return (access & Opcodes.ACC_FINAL) != 0;
	}

	@Override
	public boolean isNative() {
		return (access & Opcodes.ACC_NATIVE) != 0;
	}

	@Override
	public boolean inInterface() {
		return (classAccess & Opcodes.ACC_INTERFACE) != 0;
	}

	public boolean isDeclaringClassPublic() {
		return (classAccess & Opcodes.ACC_PUBLIC) != 0;
	}

	public int classAccess() {
		return classAccess;
	}

	public String getReturn() {
		if (rtnName == null) {
			rtnName = ASMUtil.getClassName(rtnType);
		}
		return rtnName;
	}

	public Type getReturnType() {
		return rtnType;
	}

	public Class getReturnClass() {
		if (rtnClass == null) {
			synchronized (this) {
				if (rtnClass == null) {
					try {
						rtnClass = Clazz.toClass(Clazz.getClassLoader(this.declaringClass), rtnType);
					}
					catch (ClassException e) {
						throw new PageRuntimeException(e);
					}
				}
			}
		}
		return rtnClass;
	}

	@Override
	public String[] getArguments() {
		if (argNames == null) {
			synchronized (this) {
				if (argNames == null) {
					String[] arguments = new String[argTypes.length];
					for (int i = 0; i < argTypes.length; i++) {
						arguments[i] = ASMUtil.getClassName(argTypes[i]);
					}
					argNames = arguments;
				}
			}
		}
		return argNames;
	}

	@Override
	public Class[] getArgumentClasses() {
		if (argClasses == null) {
			synchronized (this) {
				if (argClasses == null) {
					ClassLoader cl = Clazz.getClassLoader(this.declaringClass);
					Class[] tmp = new Class[argTypes.length];
					for (int i = 0; i < argTypes.length; i++) {
						try {
							tmp[i] = Clazz.toClass(cl, argTypes[i]);
						}
						catch (ClassException e) {
							throw new PageRuntimeException(e);
						}
					}
					argClasses = tmp;
				}
			}
		}
		return argClasses;
	}

	@Override
	public Type[] getArgumentTypes() {
		return argTypes;
	}

	@Override
	public int getArgumentCount() {
		return argTypes.length;
	}

	@Override
	public String[] getExceptions() {
		if (expNames == null) {
			synchronized (this) {
				if (expNames == null) {
					String[] exceptions = new String[expTypes.length];
					for (int i = 0; i < expTypes.length; i++) {
						exceptions[i] = ASMUtil.getClassName(expTypes[i]);
					}
					expNames = exceptions;
				}
			}
		}
		return expNames;
	}

	public Type[] getExceptionTypes() {
		return expTypes;
	}

	@Override
	public String toString() {
		// public java.lang.String java.lang.String.toString()
		StringBuilder sb = new StringBuilder();

		// access modifier
		String am = Clazz.getAccessModifierAsString(this);
		if (!StringUtil.isEmpty(am)) sb.append(am).append(' ');

		// final
		if (isFinal()) sb.append("final ");
		else if (isAbstract()) sb.append("abstract ");

		// native
		if (isNative()) sb.append("native ");

		// static
		if (isStatic()) sb.append("static ");

		// return type
		sb.append(ASMUtil.getClassName(getReturnType())).append(' ');

		// declaring class
		sb.append(getDeclaringClassName()).append('.');

		// name
		sb.append(name)

				.append('(');

		// arguments;
		if (argTypes != null && argTypes.length > 0) {
			String del = "";
			for (Type t: argTypes) {
				sb.append(del).append(ASMUtil.getClassName(t));
				del = ",";
			}
		}

		sb.append(")");

		// throws
		if (expTypes != null && expTypes.length > 0) {
			String del = "";
			sb.append(" throws ");
			for (Type t: expTypes) {
				sb.append(del).append(ASMUtil.getClassName(t));
				del = ",";
			}
		}

		return sb.toString();
	}

	@Override
	public String getClassPath() {
		if (classPath == null) {
			synchronized (this) {
				if (classPath == null) {
					classPath = FunctionMember.createClassPath(this);
				}
			}
		}
		return classPath;
	}

	@Override
	public String getClassName() {
		if (className == null) {
			synchronized (this) {
				if (className == null) {
					className = getClassPath().replace('/', '.');
				}
			}
		}
		return className;
	}

}
