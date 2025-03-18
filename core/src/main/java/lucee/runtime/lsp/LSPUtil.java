package lucee.runtime.lsp;

import java.io.File;

import jakarta.servlet.ServletException;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.log.Log;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.thread.SerializableCookie;

public final class LSPUtil {

	public static String formatLSPMessage(String message) {
		return String.format("Content-Length: %d\r\n\r\n%s", message.length(), message);
	}

	public static PageContext createPageContext(boolean register) throws ServletException {
		return CFMLEngineFactory.getInstance().createPageContext(new File("."), "localhost", "/", "", SerializableCookie.COOKIES0, null, null, null,
				DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, -1, register);
	}

	/**
	 * unregister temporary PageContext and register again any PageContext that was already there (just
	 * in case)
	 * 
	 * @param pc
	 * @param previousPC
	 */
	public static void releasePageContext(PageContext pc, PageContext previousPC) {
		if (pc != null) CFMLEngineFactory.getInstance().releasePageContext(pc, true);
		if (previousPC != null) CFMLEngineFactory.getInstance().registerThreadPageContext(previousPC);
	}

	public static Log getLog(Config config) {
		if (config == null) config = CFMLEngineFactory.getInstance().getThreadConfig();

		try {
			Log log = config.getLog("lsp");
			if (log == null) log = config.getLog(LSPEndpointFactory.DEFAULT_LOG);
			if (log != null) return log;
		}
		catch (Exception e) {
			Log log = config.getLog(LSPEndpointFactory.DEFAULT_LOG);
			log.error("lsp", e);
			return log;
		}
		return null;
	}
}
