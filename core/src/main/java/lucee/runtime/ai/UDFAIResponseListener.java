package lucee.runtime.ai;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.UDF;

public class UDFAIResponseListener implements AIResponseListener {

	private UDF listener;
	private PageContext pc;

	public UDFAIResponseListener(PageContext pc, UDF listener) {
		this.pc = pc;
		this.listener = listener;
	}

	@Override
	public void listen(String part, int chunkIndex, boolean isComplete) throws PageException {
		listener.call(pc, new Object[] { part, chunkIndex, isComplete }, true);
	}
}