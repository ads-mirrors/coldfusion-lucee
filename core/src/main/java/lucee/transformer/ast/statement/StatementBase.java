package lucee.transformer.ast.statement;

import lucee.transformer.Context;
import lucee.transformer.TransformerException;
import lucee.transformer.ast.AstFactory;
import lucee.transformer.ast.NodeBase;
import lucee.transformer.statement.Statement;

public abstract class StatementBase extends NodeBase implements Statement {

	public StatementBase(AstFactory factory) {
		super(factory);
	}

	private Statement parent;
	private boolean has;

	@Override
	public Statement getParent() {
		return parent;
	}

	@Override
	public void setParent(Statement parent) {
		this.parent = parent;
	}

	@Override
	public boolean hasFlowController() {
		return has;
	}

	@Override
	public void setHasFlowController(boolean has) {
		this.has = has;
	}

	@Override
	public void writeOut(Context c) throws TransformerException {
		// TODO Auto-generated method stub

	}

}
