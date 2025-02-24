/**
 * Implements the CFML Function structToSorted
 */
package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public final class StructToSorted extends BIF {
	private static final long serialVersionUID = -7945612992641626477L;

	public static Struct call(PageContext pc, Struct base) throws PageException {
		return call(pc, base, "text", "asc", null);
	}

	public static Struct call(PageContext pc, Struct base, String sortType) throws PageException {
		return call(pc, base, sortType, "asc", null);
	}

	public static Struct call(PageContext pc, Struct base, String sortType, String sortOrder) throws PageException {
		return call(pc, base, sortType, sortOrder, null);
	}

	public static Struct call(PageContext pc, Struct base, String sortType, String sortOrder, String pathToSubElement) throws PageException {
		return ((Struct) StructSort._call(pc, "struct", base, sortType, sortOrder, pathToSubElement));
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 4) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]));
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]));
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "StructToSorted", 1, 4, args.length);
	}
}