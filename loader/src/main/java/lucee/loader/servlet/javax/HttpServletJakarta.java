package lucee.loader.servlet.javax;

import java.io.IOException;
import java.util.Enumeration;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpServletJakarta extends HttpServlet implements Javax {

	private static final long serialVersionUID = -7101834437952117424L;
	private javax.servlet.http.HttpServlet servlet;

	public HttpServletJakarta(javax.servlet.http.HttpServlet servlet) {
		if (servlet == null) throw new NullPointerException();
		this.servlet = servlet;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new RuntimeException("the method [doGet] is not supported");
	}

	@Override
	public int hashCode() {
		return servlet.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return servlet.equals(obj);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new RuntimeException("the method [clone] is not supported");

	}

	@Override
	public String toString() {
		return servlet.toString();
	}

	@Override
	protected void finalize() throws Throwable {

	}

	@Override
	protected long getLastModified(HttpServletRequest req) {
		throw new RuntimeException("the method [getLastModified] is not supported");

	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new RuntimeException("the method [doHead] is not supported");

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new RuntimeException("the method [doPost] is not supported");

	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new RuntimeException("the method [doPut] is not supported");

	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new RuntimeException("the method [doDelete] is not supported");

	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new RuntimeException("the method [doOptions] is not supported");

	}

	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new RuntimeException("the method [doTrace] is not supported");

	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new RuntimeException("the method [service] is not supported");

	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		throw new RuntimeException("the method [service] is not supported");

	}

	@Override
	public void destroy() {
		servlet.destroy();
	}

	@Override
	public String getInitParameter(String name) {
		return servlet.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return servlet.getInitParameterNames();
	}

	@Override
	public ServletConfig getServletConfig() {
		return ServletConfigJakarta.getInstance(servlet.getServletConfig());
	}

	@Override
	public ServletContext getServletContext() {
		return getServletConfig().getServletContext();
	}

	@Override
	public String getServletInfo() {
		return servlet.getServletInfo();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {

			servlet.init((javax.servlet.ServletConfig) ((ServletConfigJakarta) config).getJavaxInstance());
		}
		catch (javax.servlet.ServletException e) {
			throw new ServletExceptionJakarta(e);
		}
	}

	@Override
	public void init() throws ServletException {
		try {
			servlet.init();
		}
		catch (javax.servlet.ServletException e) {
			throw new ServletExceptionJakarta(e);
		}
	}

	@Override
	public void log(String msg) {
		servlet.log(msg);
	}

	@Override
	public void log(String message, Throwable t) {
		servlet.log(message, t);
	}

	@Override
	public String getServletName() {
		return servlet.getServletName();
	}

	@Override
	public Object getJavaxInstance() {
		return servlet;
	}

}
