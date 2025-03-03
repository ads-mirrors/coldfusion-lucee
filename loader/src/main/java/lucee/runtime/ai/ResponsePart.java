package lucee.runtime.ai;

import lucee.runtime.type.Struct;

/**
 * The ResponsePart interface defines methods for accessing individual content parts within a
 * multipart AI response. Each part represents a distinct piece of content with its own content type
 * and data.
 */
public interface ResponsePart {

	/**
	 * Common content type constants
	 */
	public static final String CONTENT_TYPE_TEXT = "text/plain";
	public static final String CONTENT_TYPE_HTML = "text/html";
	public static final String CONTENT_TYPE_MARKDOWN = "text/markdown";
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String CONTENT_TYPE_IMAGE = "image/png";
	public static final String CONTENT_TYPE_SVG = "image/svg+xml";
	public static final String CONTENT_TYPE_AUDIO = "audio/mpeg";
	public static final String CONTENT_TYPE_BINARY = "application/octet-stream";

	/**
	 * Returns the MIME content type of this response part.
	 *
	 * @return String representing the MIME content type
	 */
	public String getContentType();

	/**
	 * Returns the content of this part as a String. This is appropriate for text-based content types.
	 *
	 * @return String content of the part, or null if it cannot be represented as a string
	 */
	public String getAsString();

	/**
	 * Returns the content of this part as binary data. This is appropriate for binary content types
	 * like images or audio.
	 *
	 * @return byte array containing the binary data, or null if not available as binary
	 */
	public byte[] getAsBinary();

	/**
	 * Returns the content as a structured object. This is appropriate for JSON or other structured
	 * content.
	 *
	 * @return Struct representing the structured content, or null if not applicable
	 */
	public Struct getAsStruct();

	/**
	 * Returns metadata about this specific part. This may include part-specific information like
	 * dimensions for images, duration for audio, or other attributes.
	 *
	 * @return Struct containing metadata about this part, or null if no metadata is available
	 */
	public Struct getMetadata();

	/**
	 * Returns the index of this part in the overall response.
	 *
	 * @return Zero-based index of this part
	 */
	public int getIndex();

	/**
	 * Indicates whether this part contains text content.
	 *
	 * @return boolean indicating if the content type is text-based
	 */
	public boolean isText();

	/**
	 * Indicates whether this part contains image content.
	 *
	 * @return boolean indicating if the content is an image
	 */
	public boolean isImage();

	/**
	 * Indicates whether this part contains audio content.
	 *
	 * @return boolean indicating if the content is audio
	 */
	public boolean isAudio();

	/**
	 * Indicates whether this part contains structured data (JSON, etc).
	 *
	 * @return boolean indicating if the content is structured data
	 */
	public boolean isStructured();

	/**
	 * Returns the raw content object.
	 *
	 * @return The raw content object stored in this part
	 */
	public Object getContent();
}