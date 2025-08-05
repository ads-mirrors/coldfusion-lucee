package lucee.loader.servlet.javax;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class HttpServletResponseJakarta extends ServletResponseJakarta implements HttpServletResponse {

	private javax.servlet.http.HttpServletResponse rsp;

	public HttpServletResponseJakarta(javax.servlet.http.HttpServletResponse rsp) {
		super(rsp);
		this.rsp = rsp;
	}

	@Override
	public void addCookie(Cookie cookie) {
		rsp.addCookie(Javax.toCookie(cookie));
	}

	@Override
	public boolean containsHeader(String name) {
		return rsp.containsHeader(name);
	}

	@Override
	public String encodeURL(String url) {
		return rsp.encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(String url) {
		return rsp.encodeRedirectURL(url);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		rsp.sendError(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		rsp.sendError(sc);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		rsp.sendRedirect(location);
	}

	public void sendRedirect(String location, int status, boolean clearBuffer) throws IOException {
		rsp.sendRedirect(location);
		rsp.setStatus(status);
		if (clearBuffer) {
			rsp.resetBuffer();
		}
	}

	@Override
	public void setDateHeader(String name, long date) {
		rsp.setDateHeader(name, date);
	}

	@Override
	public void addDateHeader(String name, long date) {
		rsp.addDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value) {
		rsp.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		rsp.addHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		rsp.setIntHeader(name, value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		rsp.addIntHeader(name, value);
	}

	@Override
	public void setStatus(int sc) {
		rsp.setStatus(sc);
	}

	@Override
	public int getStatus() {
		return rsp.getStatus();
	}

	@Override
	public String getHeader(String name) {
		return rsp.getHeader(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return rsp.getHeaders(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return rsp.getHeaderNames();
	}
}
