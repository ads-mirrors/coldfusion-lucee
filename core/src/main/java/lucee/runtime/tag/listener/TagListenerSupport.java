package lucee.runtime.tag.listener;

import java.io.Serializable;

public abstract class TagListenerSupport implements TagListener, Serializable {

	public static Object toCFML(TagListener tl, Object defaultValue) {
		if (tl == null) return defaultValue;

		if (tl instanceof ComponentTagListener) return ((ComponentTagListener) tl).getComponent();
		else if (tl instanceof UDFTagListener) return ((UDFTagListener) tl).getStruct();
		else return defaultValue;
	}
}
