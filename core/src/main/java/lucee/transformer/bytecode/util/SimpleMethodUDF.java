package lucee.transformer.bytecode.util;

import java.io.IOException;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.UDF;

public final class SimpleMethodUDF extends SimpleMethodSupport {

	private ClassLoader cl;
	private UDF udf;

	public SimpleMethodUDF(ClassLoader cl, UDF udf) {
		this.cl = cl;
		this.udf = udf;
	}

	@Override
	public String getName() {
		return udf.getFunctionName();
	}

	@Override
	public Class[] getParameterClasses() throws IOException {
		try {
			FunctionArgument[] args = udf.getFunctionArguments();
			if (args == null) return new Class[0];
			Class[] classes = new Class[args.length];
			int index = 0;
			for (FunctionArgument fa: args) {
				classes[index++] = Caster.cfTypeToClass(null, cl, fa.getTypeAsString());
			}
			return classes;
		}
		catch (PageException e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	@Override
	public Class[] getParameterClasses(Class defaultValue) {
		FunctionArgument[] args = udf.getFunctionArguments();
		if (args == null) return new Class[0];
		Class[] classes = new Class[args.length];
		int index = 0;
		for (FunctionArgument fa: args) {
			try {
				classes[index] = Caster.cfTypeToClass(null, cl, fa.getTypeAsString());
			}
			catch (Exception e) {
				classes[index] = defaultValue;
			}
			index++;
		}
		return classes;
	}

	@Override
	public Class getReturnClass() throws IOException {
		try {
			return Caster.cfTypeToClass(null, cl, udf.getReturnTypeAsString());
		}
		catch (PageException e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	@Override
	public Class getReturnClass(Class defaultValue) {
		try {
			return Caster.cfTypeToClass(null, cl, udf.getReturnTypeAsString());
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public String getReturnType() throws IOException {
		return udf.getReturnTypeAsString();
	}
}