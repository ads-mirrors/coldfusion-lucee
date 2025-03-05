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
package lucee.loader.servlet.javax;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lucee.loader.engine.CFMLEngineFactory;

public class RestServlet extends AbsServlet {

	private static final long serialVersionUID = 1555107078656945805L;
	private HttpServletJakarta myself;

	@Override
	public void init(final ServletConfig sg) throws ServletException {
		super.init(sg);
		myself = new HttpServletJakarta(this);
		try {
			engine = CFMLEngineFactory.getInstance(ServletConfigJakarta.getInstance(sg), this);
		}
		catch (ServletExceptionJakarta e) {
			throw (ServletException) e.getJavaxInstance();
		}
		catch (jakarta.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		try {
			engine.serviceRest(myself, new HttpServletRequestJakarta(req), new HttpServletResponseJakarta(rsp));
		}
		catch (ServletExceptionJakarta e) {
			throw (ServletException) e.getJavaxInstance();
		}
		catch (jakarta.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}
}