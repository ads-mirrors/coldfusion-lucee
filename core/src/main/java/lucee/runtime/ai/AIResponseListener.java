package lucee.runtime.ai;

import lucee.runtime.exp.PageException;

/**
 * The AIResponseListener interface defines methods for handling streaming responses from AI
 * services.
 * <p>
 * This interface enables real-time processing of partial responses as they arrive from the AI
 * service, allowing for immediate display, processing, or other actions without waiting for the
 * complete response.
 */
public interface AIResponseListener {

	/**
	 * Called when a partial response is received from the AI service.
	 * <p>
	 * This method is invoked incrementally as chunks of the response become available, enabling
	 * streaming processing of AI responses. Implementations can use this to display text as it's
	 * generated, process content incrementally, or perform other real-time operations.
	 *
	 * @param part The partial text response received from the AI service
	 * @param chunkIndex The sequential index of this chunk in the overall response
	 * @param isComplete Boolean indicating whether this is the final chunk of the response
	 * @throws PageException If an error occurs while processing the response chunk
	 */
	public void listen(String part, int chunkIndex, boolean isComplete) throws PageException;

}