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
	 * Called when a partial text response is received from the AI service.
	 * <p>
	 * This method is invoked incrementally as chunks of the response become available.
	 *
	 * @param part The partial text response received from the AI service
	 * @param chunkIndex The sequential index of this chunk in the overall response
	 * @param isComplete Boolean indicating whether this is the final chunk of the response
	 * @throws PageException If an error occurs while processing the response chunk
	 */
	public void listen(String part, int chunkIndex, boolean isComplete) throws PageException;

	/**
	 * Called when a partial response with specific content type is received from the AI service.
	 * <p>
	 * This method is used for multipart responses where different content types may be streamed.
	 *
	 * @param part The partial response content
	 * @param contentType The MIME type of the content (e.g., "text/plain", "image/png")
	 * @param chunkIndex The sequential index of this chunk in the overall response
	 * @param partIndex The index of this part in the multipart response
	 * @param isComplete Boolean indicating whether this is the final chunk of this part
	 * @throws PageException If an error occurs while processing the response chunk
	 */
	public void listen(Object part, String contentType, int chunkIndex, int partIndex, boolean isComplete) throws PageException;

}