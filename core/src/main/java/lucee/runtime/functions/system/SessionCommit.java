package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;

public final class SessionCommit implements Function {
	private static final long serialVersionUID = -2243745577257724777L;

	public static String call(PageContext pc) throws PageException {
		((PageContextImpl) pc).sessionScope().touchAfterRequest(pc);
		return null;
	}
}