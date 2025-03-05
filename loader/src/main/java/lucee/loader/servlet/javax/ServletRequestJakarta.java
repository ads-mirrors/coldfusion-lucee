package lucee.loader.servlet.javax;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

// Inner class for ServletRequestJakarta
public class ServletRequestJakarta implements ServletRequest, Javax {

	private javax.servlet.ServletRequest req;

	public ServletRequestJakarta(javax.servlet.ServletRequest req) {
		if (req == null) throw new NullPointerException();
		this.req = req;
	}

	@Override
	public Object getAttribute(String name) {
		return req.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return req.getAttributeNames();
	}

	@Override
	public String getCharacterEncoding() {
		return req.getCharacterEncoding();
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		req.setCharacterEncoding(env);
	}

	@Override
	public int getContentLength() {
		return req.getContentLength();
	}

	@Override
	public long getContentLengthLong() {
		return req.getContentLengthLong();
	}

	@Override
	public String getContentType() {
		return req.getContentType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new ServletInputStreamJakarta(req.getInputStream());
	}

	@Override
	public String getParameter(String name) {
		return req.getParameter(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return req.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String name) {
		return req.getParameterValues(name);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return req.getParameterMap();
	}

	@Override
	public String getProtocol() {
		return req.getProtocol();
	}

	@Override
	public String getScheme() {
		return req.getScheme();
	}

	@Override
	public String getServerName() {
		return req.getServerName();
	}

	@Override
	public int getServerPort() {
		return req.getServerPort();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return req.getReader();
	}

	@Override
	public String getRemoteAddr() {
		return req.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		return req.getRemoteHost();
	}

	@Override
	public void setAttribute(String name, Object o) {
		req.setAttribute(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		req.removeAttribute(name);
	}

	@Override
	public Locale getLocale() {
		return req.getLocale();
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return req.getLocales();
	}

	@Override
	public boolean isSecure() {
		return req.isSecure();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return new RequestDispatcherJakarta(req.getRequestDispatcher(path));
	}

	@Override
	public int getRemotePort() {
		return req.getRemotePort();
	}

	@Override
	public String getLocalName() {
		return req.getLocalName();
	}

	@Override
	public String getLocalAddr() {
		return req.getLocalAddr();
	}

	@Override
	public int getLocalPort() {
		return req.getLocalPort();
	}

	@Override
	public ServletContext getServletContext() {
		return new ServletContextJakarta(req.getServletContext());
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return new AsyncContextJakarta(req.startAsync());
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		throw new RuntimeException("method [startAsync] is not supported");
	}

	@Override
	public boolean isAsyncStarted() {
		return req.isAsyncStarted();
	}

	@Override
	public boolean isAsyncSupported() {
		return req.isAsyncSupported();
	}

	@Override
	public AsyncContext getAsyncContext() {
		return new AsyncContextJakarta(req.getAsyncContext());
	}

	@Override
	public DispatcherType getDispatcherType() {
		return DispatcherType.valueOf(req.getDispatcherType().name());
	}

	@Override
	public Object getJavaxInstance() {
		return req;
	}

	@Override
	public String getRequestId() {
		// Return a default value since this method doesn't exist in javax
		return "";
	}

	@Override
	public String getProtocolRequestId() {
		// Return a default value since this method doesn't exist in javax
		return "";
	}

	@Override
	public ServletConnection getServletConnection() {
		// Return null since this interface doesn't exist in javax
		return null;
	}
}