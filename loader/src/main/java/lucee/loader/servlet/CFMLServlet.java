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
package lucee.loader.servlet;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;

public class CFMLServlet extends AbsServlet {

	private static final long serialVersionUID = -1878214660283329587L;

	@Override
	public void init(final ServletConfig sg) throws ServletException {
		super.init(sg);
		engine = CFMLEngineFactory.getInstance(sg, this);

		// Register the shutdown filter
		ServletContext context = sg.getServletContext();
		FilterRegistration.Dynamic registration = context.addFilter("shutdownFilter", new ShutdownFilter(engine));

		if (registration != null) {
			registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
		}
	}

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		engine.serviceCFML(this, req, rsp);
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