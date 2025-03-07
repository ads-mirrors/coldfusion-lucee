package lucee.runtime.type.scope.jakarta;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload2.core.AbstractRequestContext;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Provides access to the request information needed for a request made to an HTTP servlet.
 */
public class JakartaServletRequestContext extends AbstractRequestContext<HttpServletRequest> {

	/**
	 * Constructs a context for this request.
	 *
	 * @param request The request to which this context applies.
	 */
	public JakartaServletRequestContext(final HttpServletRequest request) {
		super(request::getHeader, request::getContentLength, request);
	}

	/**
	 * Gets the character encoding for the request.
	 *
	 * @return The character encoding for the request.
	 */
	@Override
	public String getCharacterEncoding() {
		return getRequest().getCharacterEncoding();
	}

	/**
	 * Gets the content type of the request.
	 *
	 * @return The content type of the request.
	 */
	@Override
	public String getContentType() {
		return getRequest().getContentType();
	}

	/**
	 * Gets the input stream for the request.
	 *
	 * @return The input stream for the request.
	 * @throws IOException if a problem occurs.
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		return getRequest().getInputStream();
	}

}
