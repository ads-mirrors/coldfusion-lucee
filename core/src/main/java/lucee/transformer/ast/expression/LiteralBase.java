package lucee.transformer.ast.expression;

import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.ast.AstFactory;
import lucee.transformer.ast.statement.StatementBase;
import lucee.transformer.expression.literal.Literal;

public abstract class LiteralBase extends StatementBase implements Literal {

	public LiteralBase(AstFactory factory) {
		super(factory);
	}

	@Override
	public void writeOut(Struct sct) {
		super.writeOut(sct);

		sct.setEL(KeyConstants._type, "Literal");
	}
}
