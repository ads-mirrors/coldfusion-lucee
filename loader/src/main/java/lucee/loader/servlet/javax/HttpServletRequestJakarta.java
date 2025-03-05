package lucee.loader.servlet.javax;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

public class HttpServletRequestJakarta extends ServletRequestJakarta implements HttpServletRequest {

	private javax.servlet.http.HttpServletRequest req;

	public HttpServletRequestJakarta(javax.servlet.http.HttpServletRequest req) {
		super(req);
		this.req = req;
	}

	@Override
	public String getAuthType() {
		return req.getAuthType();
	}

	@Override
	public Cookie[] getCookies() {
		javax.servlet.http.Cookie[] cookies = req.getCookies();
		if (cookies == null) return null;
		Cookie[] jakartaCookies = new Cookie[cookies.length];
		for (int i = 0; i < cookies.length; i++) {
			javax.servlet.http.Cookie c = cookies[i];
			jakartaCookies[i] = new Cookie(c.getName(), c.getValue());
			if (c.getComment() != null) jakartaCookies[i].setComment(c.getComment());
			if (c.getDomain() != null) jakartaCookies[i].setDomain(c.getDomain());
			jakartaCookies[i].setHttpOnly(c.isHttpOnly());
			jakartaCookies[i].setMaxAge(c.getMaxAge());
			if (c.getPath() != null) jakartaCookies[i].setPath(c.getPath());
			jakartaCookies[i].setSecure(c.getSecure());
			jakartaCookies[i].setVersion(c.getVersion());
		}
		return jakartaCookies;
	}

	@Override
	public long getDateHeader(String name) {
		return req.getDateHeader(name);
	}

	@Override
	public String getHeader(String name) {
		return req.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return req.getHeaders(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return req.getHeaderNames();
	}

	@Override
	public int getIntHeader(String name) {
		return req.getIntHeader(name);
	}

	@Override
	public String getMethod() {
		return req.getMethod();
	}

	@Override
	public String getPathInfo() {
		return req.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return req.getPathTranslated();
	}

	@Override
	public String getContextPath() {
		return req.getContextPath();
	}

	@Override
	public String getQueryString() {
		return req.getQueryString();
	}

	@Override
	public String getRemoteUser() {
		return req.getRemoteUser();
	}

	@Override
	public boolean isUserInRole(String role) {
		return req.isUserInRole(role);
	}

	@Override
	public Principal getUserPrincipal() {
		return req.getUserPrincipal();
	}

	@Override
	public String getRequestedSessionId() {
		return req.getRequestedSessionId();
	}

	@Override
	public String getRequestURI() {
		return req.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		return req.getRequestURL();
	}

	@Override
	public String getServletPath() {
		return req.getServletPath();
	}

	@Override
	public HttpSession getSession(boolean create) {
		javax.servlet.http.HttpSession sess = req.getSession(create);
		if (sess == null && !create) {
			return null; // can be null, what is okay
		}
		return new HttpSessionJakarta(sess);
	}

	@Override
	public HttpSession getSession() {
		return getSession(false);
	}

	@Override
	public String changeSessionId() {
		return req.changeSessionId();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return req.isRequestedSessionIdValid();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return req.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return req.isRequestedSessionIdFromURL();
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		throw new RuntimeException("the method [authenticate] is not supported");
	}

	@Override
	public void login(String username, String password) throws ServletException {
		try {
			req.login(username, password);
		}
		catch (javax.servlet.ServletException e) {
			throw new ServletExceptionJakarta(e);
		}
	}

	@Override
	public void logout() throws ServletException {
		try {
			req.logout();
		}
		catch (javax.servlet.ServletException e) {
			throw new ServletExceptionJakarta(e);
		}
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		try {
			Collection<javax.servlet.http.Part> javaxParts = req.getParts();
			Collection<Part> jakartaParts = new ArrayList<>();
			for (javax.servlet.http.Part javaxPart: javaxParts) {
				jakartaParts.add(new PartJakarta(javaxPart));
			}
			return jakartaParts;
		}
		catch (javax.servlet.ServletException e) {
			throw new ServletExceptionJakarta(e);
		}
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		try {
			return new PartJakarta(req.getPart(name));
		}
		catch (javax.servlet.ServletException e) {
			throw new ServletExceptionJakarta(e);
		}
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		throw new RuntimeException("method [upgrade] is not supported");

	}

}
