/**
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
package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public final class LiteralOrderedStruct extends BIF implements Function {

	private static final long serialVersionUID = 3030769464899375329L;

	public static Struct call(PageContext pc, Object[] objArr) throws PageException {
		return Struct_._call(objArr, "invalid argument for literal ordered struct, only named arguments are allowed like {name:\"value\",name2:\"value2\"}", Struct.TYPE_LINKED);

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 1) throw new FunctionException(pc, "LiteralOrderedStruct", 1, 1, args.length);
		return Struct_._call(Caster.toNativeArray(args[0]), "invalid argument for literal ordered struct, only named arguments are allowed like {name:\"value\",name2:\"value2\"}",
				Struct.TYPE_LINKED);
	}
}