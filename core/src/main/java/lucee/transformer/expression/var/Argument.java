package lucee.transformer.expression.var;

import lucee.transformer.expression.Expression;

public interface Argument extends Expression {

	public Expression getValue();

	/**
	 * return the uncasted value
	 * 
	 * @return
	 */
	public Expression getRawValue();

	public void setValue(Expression value, String type);

	// public Type writeOutValue(BytecodeContext bc, int mode) throws TransformerException {

	/**
	 * @return the type
	 */
	public String getStringType();

}
