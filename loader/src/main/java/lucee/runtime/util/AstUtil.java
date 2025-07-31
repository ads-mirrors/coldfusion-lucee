package lucee.runtime.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.servlet.ServletException;
import lucee.commons.io.res.Resource;
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

	private static PageContext createPageContext(CFMLEngine eng) throws PageException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ServletException {
		PageContext pc = eng.getThreadPageContext();
		if (pc == null) pc = createPageContext(eng, eng.getThreadConfig(), "/generated.cfm", "", -1);
		return pc;
	}

	private static PageContext createPageContext(CFMLEngine eng, final Config config, final String path, String qs, long timeout) throws PageException, IOException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ServletException {

		// create PageContext when we have a Web Context
		if (config instanceof ConfigWeb) {
			Class<?> clazz = eng.getClassUtil().loadClass("lucee.runtime.thread.ThreadUtil");
			Method method = clazz.getMethod("createPageContext",
					new Class[] { ConfigWeb.class, String.class, String.class, String.class, byte[].class, boolean.class, long.class });
			return (PageContext) method.invoke(null, new Object[] { config, "", path, qs, null, true, timeout });
		}

		// create PageContext when we have no Web Context
		Resource temp = eng.getSystemUtil().getTempDirectory();
		File contextRoot;
		if (temp instanceof File) contextRoot = (File) temp;
		else contextRoot = File.createTempFile("temp", "file").getParentFile();

		return eng.createPageContext(contextRoot, "localhost", path, qs, null, null, null, null, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, timeout, false);

	}

	private static final class DevNullOutputStream extends OutputStream implements Serializable {

		public static final DevNullOutputStream DEV_NULL_OUTPUT_STREAM = new DevNullOutputStream();

		/**
		 * Constructor of the class
		 */
		private DevNullOutputStream() {
		}

		@Override
		public void close() {
		}

		@Override
		public void flush() {
		}

		@Override
		public void write(byte[] b, int off, int len) {
		}

		@Override
		public void write(byte[] b) {
		}

		@Override
		public void write(int b) {
		}
	}
}
