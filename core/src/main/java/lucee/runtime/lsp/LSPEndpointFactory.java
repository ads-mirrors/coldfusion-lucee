package lucee.runtime.lsp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.Cookie;

import lucee.print;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public class LSPEndpointFactory {
	private static final int DEFAULT_LSP_PORT = 2089; // Common LSP port
	private static final String DEFAULT_COMPONENT = "org.lucee.cfml.lsp.LSPEndpoint";
	private static final long TIMEOUT = 3000;
	private ServerSocket serverSocket;
	private ExecutorService executor;
	private volatile boolean running = true;
	private CFMLEngine engine;
	private int port;
	private String cfcPath;
	private Log log;
	private static LSPEndpointFactory instance;

	private LSPEndpointFactory(Config config) {
		// setup config and utils
		engine = CFMLEngineFactory.getInstance();
		log = getLog(config);
		port = engine.getCastUtil().toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.lsp.port", null), DEFAULT_LSP_PORT);
		cfcPath = engine.getCastUtil().toString(SystemUtil.getSystemPropOrEnvVar("lucee.lsp.component", null), DEFAULT_COMPONENT);
		if (Util.isEmpty(cfcPath, true)) cfcPath = DEFAULT_COMPONENT;

		log.debug("lsp", "LSP server port: " + port);
		log.debug("lsp", "LSP server component endpoint: " + cfcPath);
	}

	public static LSPEndpointFactory init(Config config) throws IOException {
		print.e("---- LSPEndpointFactory ----");
		if (Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.lsp.enabled", null), false)) {
			synchronized (SystemUtil.createToken("LSPEndpointFactory", "init")) {
				if (instance != null) {
					instance.stop();
				}
				instance = new LSPEndpointFactory(config).start();
			}
			print.e("- init");
		}
		return instance;
	}

	private LSPEndpointFactory start() throws IOException {
		log.debug("lsp", "starting LSP server");
		try {
			serverSocket = new ServerSocket(port);
		}
		catch (IOException e) {
			error("lsp", e);
			throw e;
		}
		executor = Executors.newCachedThreadPool();

		// Start listening thread
		Thread listenerThread = new Thread(() -> {
			while (running) {
				try {
					Socket clientSocket = serverSocket.accept();
					executor.submit(() -> handleClient(clientSocket));
				}
				catch (IOException e) {
					if (running) {
						error("lsp", e);
					}
				}
			}
		}, "LSP-Listener");

		listenerThread.setDaemon(true);
		listenerThread.start();

		log.debug("lsp", "LSP server started");
		return this;
	}

	private void stop() throws IOException {
		running = false;
		if (executor != null) {
			executor.shutdown();
		}
		if (serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();
		}
	}

	public static LSPEndpointFactory getInstance(Config config) {
		return instance;
	}

	private void handleClient(Socket clientSocket) {

		log.debug("lsp", "LSP server handle client");
		try {
			// Get input/output streams
			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			OutputStream out = clientSocket.getOutputStream();
			StringBuilder buffer = new StringBuilder();

			while (!clientSocket.isClosed()) {
				// Read incoming data into buffer
				char[] cbuf = new char[1024];
				int len;
				while ((len = reader.read(cbuf)) != -1) {
					buffer.append(cbuf, 0, len);

					// Process complete messages in the buffer
					while (true) {
						// Look for Content-Length header
						String content = buffer.toString();
						int headerIndex = content.indexOf("Content-Length: ");
						if (headerIndex == -1) break;

						// Parse content length
						int lengthStart = headerIndex + 16;
						int lengthEnd = content.indexOf("\r\n", lengthStart);
						if (lengthEnd == -1) break;

						int contentLength = Integer.parseInt(content.substring(lengthStart, lengthEnd));

						// Find start of JSON content
						int contentStart = content.indexOf("\r\n\r\n", lengthEnd);
						if (contentStart == -1) break;
						contentStart += 4;

						// Check if we have the complete message
						if (buffer.length() < contentStart + contentLength) break;

						// Extract the JSON message
						String jsonMessage = content.substring(contentStart, contentStart + contentLength);

						// Here you would call your component to handle the message
						String response = processMessage(jsonMessage);

						// Send response using LSP format
						if (response != null) {
							String header = "Content-Length: " + response.length() + "\r\n\r\n";
							out.write(header.getBytes());
							out.write(response.getBytes());
							out.flush();
						}

						// Remove processed message from buffer
						buffer.delete(0, contentStart + contentLength);
					}
				}
			}
		}
		catch (Exception e) {
			error("lsp", e);
		}
		finally {
			engine.getIOUtil().closeSilent(clientSocket);
		}
	}

	private String processMessage(String jsonMessage) {
		try {
			log.info("lsp", "Received message: " + jsonMessage);
			PageContext pc = createPageContext((ConfigWeb) engine.getThreadConfig());
			Component cfc = engine.getCreationUtil().createComponentFromName(pc, cfcPath);

			String response = engine.getCastUtil().toString(cfc.call(pc, "execute", new Object[] { jsonMessage }));
			log.info("lsp", "response from component [" + cfcPath + "]: " + response);

			return response;
		}
		catch (Exception e) {
			error("lsp", e);
			return null;
		}
	}

	private void error(String type, Exception e) {
		// TODO remove the print out
		System.err.println(type);
		e.printStackTrace();
		log.error(type, e);
	}

	public static Log getLog(Config config) {
		if (config == null) config = CFMLEngineFactory.getInstance().getThreadConfig();
		if (config instanceof ConfigServer) {
			// we only log to config Server if there is no web context
			Config cw = CFMLEngineFactory.getInstance().getThreadConfig();
			if (cw != null) config = cw;
		}
		if (config == null) return null;
		try {
			Log log = config.getLog("lsp");
			if (log == null) log = config.getLog("application");
			if (log != null) return log;
		}
		catch (Exception e) {
			Log log = config.getLog("application");
			log.error("lsp", e);
			return log;
		}
		return null;
	}

	public static PageContext createPageContext(final ConfigWeb cw) throws PageException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		return createPageContext(cw, baos, "/", "", TIMEOUT);
	}

	private static PageContext createPageContext(final ConfigWeb cw, final OutputStream os, final String path, String qs, long timeout) throws PageException {
		try {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			Class<?> clazz = eng.getClassUtil().loadClass("lucee.runtime.thread.ThreadUtil");
			Class<?> clazzPairArray = eng.getClassUtil().loadClass("lucee.commons.lang.Pair[]");

			Method method = clazz.getMethod("createPageContext", new Class[] { ConfigWeb.class, OutputStream.class, String.class, String.class, String.class, Cookie[].class,
					clazzPairArray, byte[].class, clazzPairArray, Struct.class, boolean.class, long.class });

			return (PageContext) method.invoke(null, new Object[] { cw, os, "", path, qs, new Cookie[0], null, null, null, null, true, timeout });
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	public static void releasePageContext(PageContext pc) {
		CFMLEngineFactory.getInstance().releasePageContext(pc, true);
	}

}