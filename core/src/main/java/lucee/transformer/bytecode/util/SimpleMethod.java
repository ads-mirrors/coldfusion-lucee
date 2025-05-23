package lucee.transformer.bytecode.util;

import java.io.IOException;

public interface SimpleMethod {
	public String getName();

	public Class[] getParameterClasses() throws IOException;

	public Class[] getParameterClasses(Class defaultValue);

	public Class getReturnClass() throws IOException;

	public Class getReturnClass(Class defaultValue);

	public String getReturnType() throws IOException;

	public String hash();
}