package lucee.runtime.ai;

import java.util.List;

import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

/**
 * The AIEngine interface defines the core functionalities required for an AI engine client
 * endpoint, including initialization, session creation, and configuration management.
 * 
 * This interface serves as a connection point to various AI service providers, allowing for the
 * creation of AI sessions and management of communication parameters.
 */
public interface AIEngine {
	/**
	 * Default value (-1) indicating that the system should use the engine's default connection timeout
	 */
	public static final int DEFAULT_CONNECT_TIMEOUT = -1;

	/** Default value (-1) indicating that the system should use the engine's default socket timeout */
	public static final int DEFAULT_SOCKET_TIMEOUT = -1;

	/**
	 * Initializes the AI engine with the specified factory and configuration properties.
	 *
	 * @param properties A Struct containing configuration properties for this engine instance
	 * @return The initialized AIEngine instance
	 * @throws PageException If initialization fails due to invalid properties or connection issues
	 */
	AIEngine init(ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default, String id) throws PageException;

	/**
	 * Returns a unique identifier for this engine instance, generated based on the factory and
	 * properties.
	 * 
	 * @return String containing the unique engine ID
	 */
	public String getId();

	/**
	 * Creates an AI session with an optional initial message.
	 * 
	 * @param initialMessage The initial message to send to the AI service. If null, uses any default
	 *            message specified in the initialization properties
	 * @return A new AISession instance
	 * @throws PageException If session creation fails or the message cannot be sent
	 */
	public AISession createSession(String initialMessage) throws PageException;

	/**
	 * Creates an AI session with customized parameters.
	 * 
	 * @param initialMessage The initial message to send to the AI service. If null, no initial message
	 *            is sent
	 * @param limit Maximum number of messages to retain in conversation history. If 0, uses the
	 *            engine's default
	 * @param temp Temperature setting for response generation (controls randomness). If 0, uses the
	 *            engine's default
	 * @param connectTimeout Timeout in milliseconds for establishing connection to the AI service. Use
	 *            0 for no timeout, or DEFAULT_CONNECT_TIMEOUT (-1) to use the engine's default
	 * @param socketTimeout Timeout in milliseconds for waiting on data from the AI service. Use 0 for
	 *            no timeout, or DEFAULT_SOCKET_TIMEOUT (-1) to use the engine's default
	 * @return A new AISession instance with the specified parameters
	 * @throws PageException If session creation fails or parameters are invalid
	 */
	public AISession createSession(String initialMessage, int limit, double temp, int connectTimeout, int socketTimeout) throws PageException;

	/**
	 * Returns the current socket timeout setting for this engine.
	 * 
	 * @return Socket timeout in milliseconds, or DEFAULT_SOCKET_TIMEOUT if using the default
	 */
	public int getSocketTimeout();

	/**
	 * Returns the current connection timeout setting for this engine.
	 * 
	 * @return Connection timeout in milliseconds, or DEFAULT_CONNECT_TIMEOUT if using the default
	 */
	public int getConnectTimeout();

	/**
	 * Returns the human-readable label for this engine instance.
	 * 
	 * @return String containing the engine's display label
	 */
	public String getLabel();

	/**
	 * Returns the model identifier specified for this engine.
	 * 
	 * @return String containing the model identifier
	 */
	public String getModel();

	/**
	 * Retrieves all available AI models supported by this engine.
	 * <p>
	 * This method may not be supported by all AI service providers.
	 * 
	 * @return List of available AIModel objects
	 * @throws PageException If the operation is not supported or fails due to connection issues
	 */
	public List<AIModel> getModels() throws PageException;

	/**
	 * Attempts to retrieve all available AI models, returning a default value if the operation is not
	 * supported.
	 * 
	 * @param defaultValue Default list of AIModel objects to return if the operation is not supported
	 * @return List of available AIModel objects or the provided defaultValue
	 */
	public List<AIModel> getModels(List<AIModel> defaultValue);

	/**
	 * Returns the default conversation history size limit for this engine.
	 * 
	 * @return Maximum number of messages to retain in conversation history
	 */
	public int getConversationSizeLimit();

	/**
	 * Returns the default temperature setting for this engine.
	 * <p>
	 * Temperature controls the randomness of AI responses, with higher values producing more varied
	 * outputs and lower values producing more deterministic outputs.
	 * 
	 * @return Default temperature value as a Double
	 */
	public Double getTemperature();

	public String getDefault();

	public String getName();

	public ClassDefinition<? extends AIEngine> getClassDefinition();

	public Struct getProperties();
}