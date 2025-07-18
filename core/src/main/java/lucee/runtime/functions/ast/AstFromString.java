package lucee.runtime.functions.ast;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.transformer.util.SourceCode;

public final class AstFromString extends BIF {

	private static final long serialVersionUID = 5520676005160311790L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 1) throw new FunctionException(pc, "AstFromString", 1, 1, args.length);

		String content = Caster.toString(args[0]);
		if (content == null) content = "";
		return ((PageContextImpl) pc).transform(new SourceCode(null, content, false));
	}
}