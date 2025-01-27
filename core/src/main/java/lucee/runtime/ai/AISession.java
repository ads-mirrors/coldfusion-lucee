package lucee.runtime.ai;

import lucee.runtime.exp.PageException;

public interface AISession {

	/**
	 * Invokes the AI session with the specified request.
	 *
	 * @param req the Request object containing the questions to be processed.
	 * @return a Response object containing the answers from the AI engine.
	 * @throws PageException if an error occurs during invocation.
	 */
	Response inquiry(String message) throws PageException;

	Response inquiry(String message, AIResponseListener listener) throws PageException;

	public Conversation[] getHistory();

	public String getId();

	public AIEngine getEngine();

	public int getSocketTimeout();

	public int getConnectTimeout();

	public void release() throws PageException;
}
