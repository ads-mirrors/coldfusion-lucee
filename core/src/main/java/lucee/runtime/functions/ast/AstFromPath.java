package lucee.runtime.functions.ast;

import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class AstFromPath extends BIF {

	private static final long serialVersionUID = -339402382613945736L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 1) throw new FunctionException(pc, "AstFromPath", 1, 1, args.length);

		Resource res = Caster.toResource(pc, args[0], true, true);
		PageSource ps = pc.toPageSource(res, null);
		if (ps == null) throw new FunctionException(pc, "AstFromPath", 1, "path", "cannot map [" + res + "] to a mapping", null);
		return ((PageContextImpl) pc).transform(ps);
	}
}