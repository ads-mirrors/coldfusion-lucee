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
package lucee.runtime.rest;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lucee.commons.lang.mimetype.MimeType;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.RequestListener;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.PageContextUtil;

public final class RestRequestListener implements RequestListener {

	private final Mapping mapping;
	private final String path;
	private final int format;
	private final Struct matrix;
	private final Result defaultValue;
	private Result result;
	private final List<MimeType> accept;
	private final MimeType contentType;
	private final boolean hasFormatExtension;

	public RestRequestListener(Mapping mapping, String path, Struct matrix, int format, boolean hasFormatExtension, List<MimeType> accept, MimeType contentType,
			Result defaultValue) {
		this.mapping = mapping;
		this.path = path;
		this.format = format;
		this.hasFormatExtension = hasFormatExtension;
		this.matrix = matrix;
		this.defaultValue = defaultValue;
		this.accept = accept;
		this.contentType = contentType;
	}

	@Override
	public PageSource execute(PageContext pc, PageSource requestedPage) throws PageException {
		result = mapping.getResult(pc, path, matrix, format, hasFormatExtension, accept, contentType, defaultValue);
		HttpServletRequest req = pc.getHttpServletRequest();
		req.setAttribute("client", "lucee-rest-1-0");
		req.setAttribute("rest-path", path);
		req.setAttribute("rest-result", result);

		if (result == null) {
			List<String> sources = mapping.listSources(pc);

			String msg = "no rest service for [" + path + "] found";
			String addDetail;
			if (mapping.isDefault()) addDetail = " in the matching default mapping at [" + mapping.getPhysical().getAbsolutePath() + "], available targets are ["
					+ ListUtil.listToListEL(sources, ", ") + "]";
			else addDetail = " in the matching mapping [" + mapping.getVirtual() + "] at [" + mapping.getPhysical().getAbsolutePath() + "], available targets are ["
					+ ListUtil.listToListEL(sources, ", ") + "]";

			if (PageContextUtil.show(pc)) {
				RestUtil.setStatus(pc, 404, msg + addDetail, true);

			}
			else {
				RestUtil.setStatus(pc, 404, msg, true);

			}
			ThreadLocalPageContext.getLog(pc, "rest").info("REST", msg + addDetail);
			return null;
		}

		return result.getSource().getPageSource();
	}

	/**
	 * @return the result
	 */
	public Result getResult() {
		return result;
	}
}