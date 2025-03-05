package lucee.loader.servlet.javax;

import java.util.EventListener;

import jakarta.servlet.ServletContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextListenerJavax implements ServletContextListener {

	private ServletContext sc;
	private jakarta.servlet.ServletContextListener listener;

	public static <T extends EventListener> javax.servlet.ServletContextListener getinstance(jakarta.servlet.ServletContext sc, T t) {
		if (t instanceof javax.servlet.ServletContextListener) return (javax.servlet.ServletContextListener) t;

		return new ServletContextListenerJavax(sc, (jakarta.servlet.ServletContextListener) t);
	}

	public ServletContextListenerJavax(jakarta.servlet.ServletContext sc, jakarta.servlet.ServletContextListener listener) {
		this.sc = sc;
		this.listener = listener;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		listener.contextInitialized(new ServletContextEventImpl(sc, sce));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		listener.contextDestroyed(new ServletContextEventImpl(sc, sce));
	}

	private static class ServletContextEventImpl extends jakarta.servlet.ServletContextEvent {

		private static final long serialVersionUID = -6301294044223510600L;
		private ServletContext src;
		private ServletContextEvent sce;

		public ServletContextEventImpl(ServletContext source, ServletContextEvent sce) {
			super(source);
			this.src = source;
			this.sce = sce;
		}

		@Override
		public ServletContext getServletContext() {
			return this.src;
		}

		@Override
		public Object getSource() {
			return src;
		}

		@Override
		public String toString() {
			return sce.toString();
		}

	}

}
