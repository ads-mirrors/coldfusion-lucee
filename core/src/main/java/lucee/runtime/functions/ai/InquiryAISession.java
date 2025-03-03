package lucee.runtime.functions.ai;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.PageContext;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.ai.Response;
import lucee.runtime.ai.UDFAIResponseListener;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;

/**
 * implementation of the Function arrayAppend
 */
public final class InquiryAISession extends BIF {

	private static final long serialVersionUID = 4034033693139930644L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 2 || args.length > 3) throw new FunctionException(pc, "InquiryAISession", 2, 3, args.length);

		Object oSession = args[0];
		if (!(oSession instanceof AISession)) {
			throw new CasterException(oSession, AISession.class);
		}
		AISession ais = (AISession) oSession;

		String question = Caster.toString(args[1]);
		UDF listener = args.length > 2 ? Caster.toFunction(args[2]) : null;

		Response rsp;

		LogUtil.logx(pc.getConfig(), Log.LEVEL_INFO, "ai", "Submitting question to AI endpoint [" + ais.getEngine().getName() + "] from type [" + ais.getEngine().getLabel()
				+ "] with the following content: [" + question + "]", "ai", "application");

		if (listener != null) rsp = ais.inquiry(question, new UDFAIResponseListener(pc, listener));
		else rsp = ais.inquiry(question);
		return AIUtil.extractStringAnswer(rsp);
	}
}