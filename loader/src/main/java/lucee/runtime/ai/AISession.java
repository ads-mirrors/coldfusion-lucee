package lucee.runtime.ai;

import lucee.runtime.exp.PageException;

/**
 * The AISession interface defines methods for managing an interactive session with an AI service.
 * <p>
 * This interface provides functionality to send messages to an AI service, retrieve responses,
 * manage conversation history, and control session parameters.
 */
public interface AISession {

	/**
	 * Sends a message to the AI service and retrieves the response.
	 * <p>
	 * This method adds the message to the conversation history and obtains a response from the
	 * underlying AI engine.
	 *
	 * @param message The message text to send to the AI service
	 * @return A Response object containing the AI's reply and any additional metadata
	 * @throws PageException If the message cannot be sent, the response cannot be received, or any
	 *             other communication error occurs
	 */
	public Response inquiry(String message) throws PageException;

	/**
	 * Sends a message to the AI service with a listener for streaming or asynchronous responses.
	 * <p>
	 * This variation allows for processing responses as they arrive, rather than waiting for the
	 * complete response before proceeding.
	 *
	 * @param message The message text to send to the AI service
	 * @param listener An AIResponseListener implementation that will be notified as response chunks are
	 *            received
	 * @return A Response object containing the complete AI reply and any additional metadata
	 * @throws PageException If the message cannot be sent, the response cannot be received, or any
	 *             other communication error occurs
	 */
	public Response inquiry(String message, AIResponseListener listener) throws PageException;

	public String getSystemMessage();

	/**
	 * Retrieves the full conversation history for this session.
	 * <p>
	 * The returned array contains all previous exchanges between the user and AI, up to the
	 * conversation size limit.
	 *
	 * @return An array of Conversation objects representing the message history
	 */
	public Conversation[] getHistory();

	/**
	 * Returns the unique identifier for this session.
	 *
	 * @return String containing the session ID
	 */
	public String getId();

	/**
	 * Returns the AIEngine instance that created this session.
	 *
	 * @return The parent AIEngine instance
	 */
	public AIEngine getEngine();

	/**
	 * Returns the maximum number of messages to retain in the conversation history.
	 *
	 * @return Integer representing the conversation history size limit
	 */
	public int getConversationSizeLimit();

	/**
	 * Returns the temperature setting for this session.
	 * <p>
	 * Temperature controls the randomness of AI responses, with higher values producing more varied
	 * outputs and lower values producing more deterministic outputs.
	 *
	 * @return Double representing the temperature value
	 */
	public Double getTemperature();

	/**
	 * Returns the socket timeout setting for this session.
	 * <p>
	 * This determines how long the session will wait for data from the AI service before timing out.
	 *
	 * @return Socket timeout in milliseconds, or a default value if using engine defaults
	 */
	public int getSocketTimeout();

	/**
	 * Returns the connection timeout setting for this session.
	 * <p>
	 * This determines how long the session will wait when establishing a connection to the AI service
	 * before timing out.
	 *
	 * @return Connection timeout in milliseconds, or a default value if using engine defaults
	 */
	public int getConnectTimeout();

	/**
	 * Releases resources associated with this session.
	 * <p>
	 * This method should be called when the session is no longer needed to ensure proper cleanup of any
	 * resources held by the session, such as network connections or memory.
	 *
	 * @throws PageException If an error occurs while releasing resources
	 */
	public void release() throws PageException;
}