package lucee.runtime.functions.ai;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.PageContext;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;

/**
 * implementation of the Function arrayAppend
 */
public final class SerializeAISession extends BIF {

	private static final long serialVersionUID = 4339397129245369313L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 1) throw new FunctionException(pc, "SerializeAISession", 1, 1, args.length);

		Object oSession = args[0];
		if (!(oSession instanceof AISession)) {
			throw new CasterException(oSession, AISession.class);
		}
		AISession ais = (AISession) oSession;

		LogUtil.logx(pc.getConfig(), Log.LEVEL_INFO, "ai", "serialize session for [" + ais.getEngine().getName() + "] from type [" + ais.getEngine().getLabel() + "]", "ai",
				"application");

		return AIUtil.serialize(ais);
	}
}