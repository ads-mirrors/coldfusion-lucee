package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.ext.function.Function;

public class SessionExists implements Function {
	private static final long serialVersionUID = -5243745566257724777L;

	public static boolean call(PageContext pc) {
		return ((PageContextImpl) pc).hasCFSession();
	}
}