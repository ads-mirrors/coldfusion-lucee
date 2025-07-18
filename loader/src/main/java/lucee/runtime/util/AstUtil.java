package lucee.runtime.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Struct;

public class AstUtil {
	public static Struct astFromString(String cfmlCode) throws PageException {
		try {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			PageContext pc = createPageContext(eng);
			BIF bif = eng.getClassUtil().loadBIF(pc, "lucee.runtime.functions.ast.AstFromString");
			return eng.getCastUtil().toStruct(bif.invoke(pc, new Object[] { cfmlCode }));
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	public static Struct astFromPath(Object path) throws PageException {
		try {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			PageContext pc = createPageContext(eng);
			BIF bif = eng.getClassUtil().loadBIF(pc, "lucee.runtime.functions.ast.AstFromPath");
			return eng.getCastUtil().toStruct(bif.invoke(pc, new Object[] { path }));
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	private static PageContext createPageContext(CFMLEngine eng)
			throws PageException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		PageContext pc = eng.getThreadPageContext();
		if (pc == null) pc = createPageContext(eng, eng.getThreadConfig(), "/generated.cfm", "", -1);
		return pc;
	}

	private static PageContext createPageContext(CFMLEngine eng, final Config cw, final String path, String qs, long timeout)
			throws PageException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Class<?> clazz = eng.getClassUtil().loadClass("lucee.runtime.thread.ThreadUtil");

		Method method = clazz.getMethod("createPageContext", new Class[] { ConfigWeb.class, String.class, String.class, String.class, byte[].class, boolean.class, long.class });

		return (PageContext) method.invoke(null, new Object[] { cw, "", path, qs, null, true, timeout });

	}
}
