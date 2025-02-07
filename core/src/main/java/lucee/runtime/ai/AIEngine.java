package lucee.runtime.ai;

import java.util.List;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

/**
 * The AIEngine interface defines the core functionalities required for an AI engine, including
 * initialization, invocation, and lifecycle management methods.
 */
public interface AIEngine {
	public static final int DEFAULT_CONNECT_TIMEOUT = -1;
	public static final int DEFAULT_SOCKET_TIMEOUT = -1;

	/**
	 * Initializes the AI engine with the specified properties.
	 *
	 * @param properties a Struct containing the properties for initialization.
	 * @return an instance of the AIEngine after initialization.
	 * @throws PageException if an error occurs during initialization.
	 */
	AIEngine init(AIEngineFactory factory, Struct properties) throws PageException;

	public String getId();

	/**
	 * 
	 * @param inialMessage inital message to send to the AI, set null for no message
	 * @param limit conversion history limit, when 0 the setting from the config is used
	 * @param temp temperature, when 0 the setting from the config is used
	 * @param timeout connection/read timeout for the calls to AI, set 0 for not timeout and -1 to use
	 *            the default defined with the driver.
	 * @return the session created
	 */
	public AISession createSession(String inialMessage, int limit, double temp, int connectTimeout, int socketTimeout) throws PageException;

	public AIEngineFactory getFactory();

	public int getSocketTimeout();

	public int getConnectTimeout();

	public String getLabel();

	public String getModel();

	public List<AIModel> getModels() throws PageException;

	public int getConversationSizeLimit();

	public Double getTemperature();

}
