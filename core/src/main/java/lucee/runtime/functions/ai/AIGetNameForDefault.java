package lucee.runtime.functions.ai;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public class AIGetNameForDefault extends BIF {

	private static final long serialVersionUID = 2001310963870279236L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 1) throw new FunctionException(pc, "AIGetNameForDefault", 1, 1, args.length);

		return ((PageContextImpl) pc).getNameFromDefault(Caster.toString(args[0]));
	}

}