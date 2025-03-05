package lucee.loader.servlet.javax;

import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Map;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

public class ServletConfigJakarta implements ServletConfig, Javax {

	private javax.servlet.ServletConfig config;
	private ServletContextJakarta context;
	private static Map<javax.servlet.ServletConfig, ServletConfigJakarta> configs = new IdentityHashMap<>();

	public static ServletConfig getInstance(javax.servlet.ServletConfig servletConfig) {
		ServletConfigJakarta c = configs.get(servletConfig);
		if (c == null) {
			synchronized (configs) {
				c = configs.get(servletConfig);
				if (c == null) {
					c = new ServletConfigJakarta(servletConfig);
					configs.put(servletConfig, c);
				}
			}
		}
		return c;
	}

	private ServletConfigJakarta(javax.servlet.ServletConfig config) {
		this.config = config;
	}

	@Override
	public String getServletName() {
		return config.getServletName();
	}

	@Override
	public ServletContext getServletContext() {
		if (context == null) {
			synchronized (config) {
				if (context == null) {
					context = new ServletContextJakarta(config.getServletContext());
				}
			}
		}
		return context;
	}

	@Override
	public String getInitParameter(String name) {
		return config.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return config.getInitParameterNames();
	}

	@Override
	public Object getJavaxInstance() {
		return config;
	}

}
