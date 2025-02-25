/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.loader.servlet.jakarta;

import java.io.IOException;
import java.util.EnumSet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;

public class CFMLServlet extends AbsServlet {

	private static final long serialVersionUID = -1878214660283329587L;
	private HttpServletJavax myself;

	@Override
	public void init(final ServletConfig sg) throws ServletException {
		super.init(sg);
		myself = new HttpServletJavax(this);
		try {
			engine = CFMLEngineFactory.getInstance(ServletConfigJavax.getInstance(sg), this);

			// Register the shutdown filter
			ServletContext context = sg.getServletContext();
			FilterRegistration.Dynamic registration = context.addFilter("shutdownFilter", new ShutdownFilter(engine));

			if (registration != null) {
				registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
			}
		}
		catch (ServletExceptionJavax e) {
			throw (ServletException) e.getJakartaInstance();
		}
		catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		try {
			engine.serviceCFML(myself, new HttpServletRequestJavax(req), new HttpServletResponseJavax(rsp));
		}
		catch (ServletExceptionJavax e) {
			throw (ServletException) e.getJakartaInstance();
		}
		catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	private static class ShutdownFilter implements Filter {

		private final CFMLEngine engine;

		public ShutdownFilter(CFMLEngine engine) {
			this.engine = engine;
		}

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			// Initialization if needed
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			// Pass the request along the filter chain
			chain.doFilter(request, response);
		}

		@Override
		public void destroy() {
			// This is called when the filter is being taken out of service
			if (engine != null) {
				engine.reset();
			}
		}
	}
}