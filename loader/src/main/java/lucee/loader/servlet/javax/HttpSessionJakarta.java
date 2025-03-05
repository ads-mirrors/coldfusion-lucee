package lucee.loader.servlet.javax;

import java.util.Enumeration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

public class HttpSessionJakarta implements HttpSession {

	private javax.servlet.http.HttpSession session;

	public HttpSessionJakarta(javax.servlet.http.HttpSession session) {
		if (session == null) throw new NullPointerException();
		this.session = session;
	}

	@Override
	public long getCreationTime() {
		return session.getCreationTime();
	}

	@Override
	public String getId() {
		return session.getId();
	}

	@Override
	public long getLastAccessedTime() {
		return session.getLastAccessedTime();
	}

	@Override
	public ServletContext getServletContext() {
		return new ServletContextJakarta(session.getServletContext());
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		session.setMaxInactiveInterval(interval);
	}

	@Override
	public int getMaxInactiveInterval() {
		return session.getMaxInactiveInterval();
	}

	@Override
	public Object getAttribute(String name) {
		return session.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return session.getAttributeNames();
	}

	@Override
	public void setAttribute(String name, Object value) {
		session.setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		session.removeAttribute(name);
	}

	@Override
	public void invalidate() {
		session.invalidate();
	}

	@Override
	public boolean isNew() {
		return session.isNew();
	}
}