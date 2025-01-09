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
/**
 * Implements the CFML Function listfindnocase
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

@Deprecated
public final class ListFindNoCase extends BIF {

	private static final long serialVersionUID = 8596474187680730966L;

	@Deprecated
	public static double call(PageContext pc, String list, String value) {
		return Caster.toDoubleValue(ListFindNoCaseNumber.call(pc, list, value));
	}

	@Deprecated
	public static double call(PageContext pc, String list, String value, String delimter) {
		return Caster.toDoubleValue(ListFindNoCaseNumber.call(pc, list, value, delimter));
	}

	@Deprecated
	public static double call(PageContext pc, String list, String value, String delimter, boolean includeEmptyFields) {
		return Caster.toDoubleValue(ListFindNoCaseNumber.call(pc, list, value, delimter, includeEmptyFields));
	}

	@Deprecated
	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		return new ListFindNoCaseNumber().invoke(pc, args);
	}
}