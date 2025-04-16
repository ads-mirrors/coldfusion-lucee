package lucee.runtime.functions.ai;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

/**
 * implementation of the Function arrayAppend
 */
public final class CreateAISession extends BIF {

	private static final long serialVersionUID = 5632258425708603692L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 4) throw new FunctionException(pc, "CreateAISession", 1, 4, args.length);

		String nameAI = Caster.toString(args[0]);
		String systemMessage = args.length > 1 ? Caster.toString(args[1]) : null;
		int limit = args.length > 2 ? Caster.toIntValue(args[2]) : -1;
		double temp = args.length > 3 ? Caster.toDoubleValue(args[3]) : -1D;
		if (temp > 1D) throw new FunctionException(pc, "CreateAISession", "4th", "temperature",
				"temperature must be between 0.0 and 1.0 (inclusive). Lower values (0.0-0.3) produce more focused, deterministic responses, while higher values create more varied output. Values less than 0 will use the default defined with the configuration of the nedpoint.",
				null);
		if (nameAI.startsWith("id:")) return ((PageContextImpl) pc).createAISessionById(nameAI.substring(3), systemMessage, null, limit, temp, -1, -1);

		if (nameAI.startsWith("default:")) nameAI = ((PageContextImpl) pc).getNameFromDefault(nameAI.substring(8));
		return ((PageContextImpl) pc).createAISession(nameAI, systemMessage, null, limit, temp, -1, -1);
	}
}