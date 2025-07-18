package lucee.transformer.statement;

import lucee.transformer.Context;
import lucee.transformer.Node;
import lucee.transformer.TransformerException;

/**
 * A single Statement
 */
public interface Statement extends Node {

	/**
	 * @return returns the parent statement
	 */
	public Statement getParent();

	/**
	 * sets parent statement to statement
	 * 
	 * @param parent the parent statement
	 */
	public void setParent(Statement parent);

	public boolean hasFlowController();

	public void setHasFlowController(boolean has);

	/**
	 * write out the statement to adapter
	 * 
	 * @param c
	 * @throws TransformerException
	 */
	public void writeOut(Context c) throws TransformerException;

}