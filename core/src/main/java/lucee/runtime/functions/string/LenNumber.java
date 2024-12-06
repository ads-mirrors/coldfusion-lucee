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
 * Implements the CFML Function len
 */
package lucee.runtime.functions.string;

import java.util.List;
import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;

public final class LenNumber implements Function {

	private static final long serialVersionUID = 4609496925485611639L;

	public static Number call(PageContext pc, Object obj) throws FunctionException {
		int len = invoke(obj, -1);
		if (len == -1) throw new FunctionException(pc, "len", 1, "object", "this type  [" + Caster.toTypeName(obj) + "] is not supported for returning the len");
		return Caster.toNumber(pc, len);
	}

	public static int invoke(Object obj, int defaultValue) {
		if (obj instanceof CharSequence) return ((CharSequence) obj).length();
		if (obj instanceof Query) return ((Query) obj).getRecordcount();
		if (obj instanceof Collection) return ((Collection) obj).size();
		if (obj instanceof Map) return ((Map) obj).size();
		if (obj instanceof List) return ((List) obj).size();
		if (obj instanceof Object[]) return ((Object[]) obj).length;
		if (obj instanceof short[]) return ((short[]) obj).length;
		if (obj instanceof int[]) return ((int[]) obj).length;
		if (obj instanceof float[]) return ((float[]) obj).length;
		if (obj instanceof double[]) return ((double[]) obj).length;
		if (obj instanceof long[]) return ((long[]) obj).length;
		if (obj instanceof char[]) return ((char[]) obj).length;
		if (obj instanceof boolean[]) return ((boolean[]) obj).length;
		if (obj instanceof byte[]) return ((byte[]) obj).length;
		String str = Caster.toString(obj, null);
		if (str != null) return str.length();

		return defaultValue;
	}
}