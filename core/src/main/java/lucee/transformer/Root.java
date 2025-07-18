
package lucee.transformer;

import lucee.transformer.statement.IFunction;

public interface Root extends Body {

	public int addFunction(IFunction function);

	public byte[] execute(String className) throws TransformerException;

	public String registerJavaFunctionName(String functionName);

}