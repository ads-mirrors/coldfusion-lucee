package lucee.runtime.functions.string;

import lucee.commons.lang.SensitiveDataSanitizer;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class Sanitize extends BIF {

	private static final long serialVersionUID = -5130925318405461153L;

	public static String call(PageContext pc, String str) {
		return call(pc, str, null);
	}

	public static String call(PageContext pc, String str, String replacement) {
		return SensitiveDataSanitizer.sanitize(str, StringUtil.isEmpty(replacement) ? SensitiveDataSanitizer.DEFAULT_MASK : replacement);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]), null);
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));

		throw new FunctionException(pc, "Sanitize", 1, 2, args.length);
	}
}