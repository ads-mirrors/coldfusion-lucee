package lucee.runtime.ai;

import lucee.runtime.exp.PageException;

public final class DevNullAIResponseListener implements AIResponseListener {

	public static final AIResponseListener INSTANCE = new DevNullAIResponseListener();

	@Override
	public void listen(String part, int chunkIndex, boolean isComplete) {
		// do nothing
	}

	@Override
	public void listen(Object part, String contentType, int chunkIndex, int partIndex, boolean isComplete) throws PageException {
		// do nothing
	}

}
