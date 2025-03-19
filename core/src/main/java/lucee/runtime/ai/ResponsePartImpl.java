package lucee.runtime.ai;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * The ResponsePart class represents individual content parts within a multipart AI response. Each
 * part contains specific content with its own type and data, along with metadata.
 */
public final class ResponsePartImpl implements ResponsePart {

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

	private final String contentType;
	private final Object content;
	private final Struct metadata;
	private final int index;

	/**
	 * Creates a new ResponsePart with the specified parameters.
	 *
	 * @param contentType The MIME content type of this part
	 * @param content The content object (String, byte[], Struct, etc.)
	 * @param metadata Metadata about this part, or null if none is available
	 * @param index The zero-based index of this part in the overall response
	 */
	public ResponsePartImpl(String contentType, Object content, Struct metadata, int index) {
		this.contentType = contentType;
		this.content = content;
		this.metadata = metadata;
		this.index = index;
	}

	public ResponsePartImpl(String content) {
		this.contentType = CONTENT_TYPE_TEXT;
		this.content = content;
		this.metadata = new StructImpl();
		this.index = 0;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getAsString() {
		if (content instanceof String) {
			return (String) content;
		}
		else if (content instanceof byte[]) {
			try {
				return new String((byte[]) content, "UTF-8");
			}
			catch (Exception e) {
				return null;
			}
		}
		else if (content != null) {
			return content.toString();
		}
		return null;
	}

	@Override
	public byte[] getAsBinary() {
		if (content instanceof byte[]) {
			return (byte[]) content;
		}
		else if (content instanceof String) {
			try {
				return ((String) content).getBytes("UTF-8");
			}
			catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public Struct getAsStruct() {
		if (content instanceof Struct) {
			return (Struct) content;
		}
		return null;
	}

	@Override
	public Struct getMetadata() {
		return metadata;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public boolean isText() {
		return contentType != null && (contentType.startsWith("text/") || contentType.equals("application/json") || contentType.equals("application/xml"));
	}

	@Override
	public boolean isImage() {
		return contentType != null && contentType.startsWith("image/");
	}

	@Override
	public boolean isAudio() {
		return contentType != null && contentType.startsWith("audio/");
	}

	@Override
	public boolean isStructured() {
		return contentType != null && (contentType.equals("application/json") || contentType.equals("application/xml") || content instanceof Struct);
	}

	@Override
	public Object getContent() {
		return content;
	}
}