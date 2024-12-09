package lucee.runtime.type.scope;

import lucee.runtime.type.StructImpl;

public final class RequestStandalone extends ScopeSupport implements RequestPro {

	public RequestStandalone() {
		super("request", SCOPE_REQUEST, StructImpl.TYPE_SYNC);
	}

}