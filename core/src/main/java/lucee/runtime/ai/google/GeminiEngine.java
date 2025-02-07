package lucee.runtime.ai.google;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.loader.util.Util;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AIEngineFactory;
import lucee.runtime.ai.AIEngineSupport;
import lucee.runtime.ai.AIModel;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AISessionSupport;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public class GeminiEngine extends AIEngineSupport {
	// https://ai.google.dev/gemini-api/docs/get-started/tutorial?lang=rest&hl=de
	public static final String TYPE_REG = "generateContent?";
	public static final String TYPE_STREAM = "streamGenerateContent?alt=sse&";

	private static final String DEFAULT_URL = "https://generativelanguage.googleapis.com/v1/";

	public static final String CHAT = "models/{model}:{cttype}key={apikey}";
	public static final String MODELS = "models/?key={apikey}";

	private static final long DEFAULT_TIMEOUT = 3000L;
	private static final String DEFAULT_CHARSET = "UTF-8";
	// private static final String DEFAULT_MODEL = "gemini-1.5-flash";
	private static final String DEFAULT_LOCATION = "us-central1";
	private static final int DEFAULT_CONVERSATION_SIZE_LIMIT = 100;

	Struct properties;
	String apikey;
	private int socketTimeout;
	private int connectTimeout;
	String location;
	String charset;
	ProxyData proxy = null;
	Map<String, String> formfields = null;
	String model;
	String systemMessage;
	String baseURL = null;
	private int conversationSizeLimit = DEFAULT_CONVERSATION_SIZE_LIMIT;
	public Double temperature = null;

	@Override
	public AIEngine init(AIEngineFactory factory, Struct properties) throws PageException {
		super.init(factory);
		this.properties = properties;

		// base URL
		String str = Caster.toStringTrim(properties.get(KeyConstants._URL, null), null);
		if (!Util.isEmpty(str, true)) {
			baseURL = str;
			if (!baseURL.endsWith("/")) baseURL += '/';
		}
		else baseURL = DEFAULT_URL;

		// api key
		str = Caster.toStringTrim(properties.get("apikey", null), null);
		if (Util.isEmpty(str, true)) {
			throw new ApplicationException("the property [apikey] is required for the AI Engine Gemini!");
		}
		apikey = str;

		// conversation Size Limit
		conversationSizeLimit = Caster.toIntValue(properties.get("conversationSizeLimit", null), DEFAULT_CONVERSATION_SIZE_LIMIT);

		// temperature
		temperature = Caster.toDouble(properties.get(KeyConstants._temperature, null), null);
		if (temperature != null && (temperature < 0D || temperature > 1D)) {
			throw new ApplicationException("temperature has to be a number between 0 and 1, now it is [" + temperature + "]");
		}

		// location
		location = Caster.toStringTrim(properties.get(KeyConstants._location, null), DEFAULT_LOCATION);
		if (Util.isEmpty(location, true)) location = DEFAULT_LOCATION;

		// timeout
		connectTimeout = Caster.toIntValue(properties.get("connectTimeout", null), DEFAULT_CONNECT_TIMEOUT);
		if (connectTimeout <= 0) connectTimeout = DEFAULT_CONNECT_TIMEOUT;

		socketTimeout = Caster.toIntValue(properties.get("socketTimeout", null), DEFAULT_SOCKET_TIMEOUT);
		if (socketTimeout <= 0) socketTimeout = DEFAULT_SOCKET_TIMEOUT;

		// charset
		charset = Caster.toStringTrim(properties.get(KeyConstants._charset, null), DEFAULT_CHARSET);
		if (Util.isEmpty(charset, true)) charset = DEFAULT_CHARSET;

		// model
		model = Caster.toStringTrim(properties.get(KeyConstants._model, null), null);
		if (Util.isEmpty(model, true)) {
			// nice to have
			String appendix = "";
			try {
				appendix = " Available models for this engine are [" + AIUtil.getModelNamesAsStringList(this) + "]";
			}
			catch (PageException pe) {
			}

			throw new ApplicationException("the property [model] is required for a OpenAI Engine!." + appendix);
		}

		// message
		systemMessage = Caster.toStringTrim(properties.get(KeyConstants._message, null), null);

		return this;

	}

	public URL toURL(String base, String scriptName, String cttype) throws PageException {
		scriptName = StringUtil.replace(scriptName, "{location}", location, false);
		scriptName = StringUtil.replace(scriptName, "{apikey}", apikey, false);
		scriptName = StringUtil.replace(scriptName, "{model}", model, false);
		if (cttype != null) scriptName = StringUtil.replace(scriptName, "{cttype}", cttype, false);
		try {
			return new URL(base + scriptName);
		}
		catch (MalformedURLException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public AISession createSession(String inialMessage, int limit, double temp, int connectTimeout, int socketTimeout) {
		return new GeminiSession(this, StringUtil.isEmpty(inialMessage, true) ? systemMessage : inialMessage.trim(), limit, temp, connectTimeout, socketTimeout);
	}

	@Override
	public String getLabel() {
		return "Gemini";
	}

	@Override
	public String getModel() {
		return model;
	}

	@Override
	public int getSocketTimeout() {
		return socketTimeout;
	}

	@Override
	public int getConnectTimeout() {
		return connectTimeout;
	}

	@Override
	public List<AIModel> getModels() throws PageException {
		try {
			HttpGet get = new HttpGet(toURL(baseURL, MODELS, null).toExternalForm());
			get.setHeader("Content-Type", AIUtil.createJsonContentType(charset));

			RequestConfig config = AISessionSupport.setTimeout(RequestConfig.custom(), this.getConnectTimeout(), this.getSocketTimeout()).build();
			get.setConfig(config);

			try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(get)) {

				HttpEntity responseEntity = response.getEntity();
				org.apache.http.Header ct = responseEntity.getContentType();
				MimeType mt = MimeType.getInstance(ct.getValue());

				String t = mt.getType() + "/" + mt.getSubtype();
				String cs = mt.getCharset() != null ? mt.getCharset().toString() : charset;

				if ("application/json".equals(t)) {
					String rawStr = EntityUtils.toString(responseEntity, charset);
					Struct raw = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, rawStr));

					Struct err = Caster.toStruct(raw.get(KeyConstants._error, null), null);
					if (err != null) {
						throw AIUtil.toException(this, Caster.toString(err.get(KeyConstants._message)), Caster.toString(err.get(KeyConstants._type, null), null),
								Caster.toString(err.get(KeyConstants._code, null), null), AIUtil.getStatusCode(response));
					}

					Array data = Caster.toArray(raw.get("models"));
					List<AIModel> list = new ArrayList<>();
					Iterator<Object> it = data.valueIterator();
					while (it.hasNext()) {
						list.add(new GeminiModel(Caster.toStruct(it.next()), charset));
					}
					return list;
				}
				throw new ApplicationException("Unsupported mime type [" + t + "], only [application/json] is supported");
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public int getConversationSizeLimit() {
		return conversationSizeLimit;
	}

	@Override
	public Double getTemperature() {
		return temperature;
	}
}
