package lucee.transformer;

import lucee.runtime.type.Struct;

public interface Node {

	/**
	 * @return the startLine
	 */
	public Position getStart();

	/**
	 * sets the start line value.
	 * 
	 * @param startLine The start line position to set.
	 */
	public void setStart(Position startLine);

	/**
	 * @return the endLine
	 */
	public Position getEnd();

	/**
	 * sets the start line value.
	 * 
	 * @param endLine The end line position to set.
	 */
	public void setEnd(Position endLine);

	public Factory getFactory();

	public void dump(Struct sct);
}
