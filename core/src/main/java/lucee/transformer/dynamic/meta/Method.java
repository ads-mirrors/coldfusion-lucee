package lucee.transformer.dynamic.meta;

import java.io.IOException;

public interface Method extends FunctionMember {

	public String getDeclaringProviderRtnClassName();

	public Class getDeclaringProviderRtnClass();

	public String getDeclaringProviderRtnClassNameWithSameAccess();

	public Class getDeclaringProviderRtnClassWithSameAccess();

	public Object invoke(Object obj, Object... args) throws IOException;
}