package lucee.transformer.dynamic.meta.reflection;

import java.io.IOException;

import org.objectweb.asm.Type;

import lucee.commons.lang.ExceptionUtil;
import lucee.transformer.dynamic.meta.Constructor;
import lucee.transformer.dynamic.meta.LegacyConstuctor;

class ConstructorReflection extends FunctionMemberReflection implements Constructor, LegacyConstuctor {

	private java.lang.reflect.Constructor constructor;

	public ConstructorReflection(java.lang.reflect.Constructor constructor) {
		super(constructor);
		this.constructor = constructor;
	}

	@Override
	public java.lang.reflect.Constructor getConstructor() {
		return constructor;
	}

	@Override
	public Object newInstance(Object... args) throws IOException {
		try {
			return constructor.newInstance(args);
		}
		catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	@Override
	public String getReturn() {
		return getReturnClass().getName();
	}

	@Override
	public Type getReturnType() {
		return Type.getType(getDeclaringClass());
	}

	@Override
	public Class getReturnClass() {
		return getDeclaringClass();
	}
}
