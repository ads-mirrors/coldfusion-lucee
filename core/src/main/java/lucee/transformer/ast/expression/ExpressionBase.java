package lucee.transformer.ast.expression;

import lucee.runtime.type.Struct;
import lucee.transformer.Context;
import lucee.transformer.TransformerException;
import lucee.transformer.ast.AstFactory;
import lucee.transformer.ast.NodeBase;
import lucee.transformer.expression.Expression;

public abstract class ExpressionBase extends NodeBase implements Expression {

	public ExpressionBase(AstFactory factory) {
		super(factory);
	}

	@Override
	public Class<?> writeOut(Context bc, int mode) throws TransformerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeOut(Struct sct) {
		super.writeOut(sct);
	}

}
