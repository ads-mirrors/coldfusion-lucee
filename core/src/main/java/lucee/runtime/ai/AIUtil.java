package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;

import lucee.commons.io.res.ContentType;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.util.AI;

public final class AIUtil {

	private static final Key CREATED_AT = KeyImpl.init("createdAt");
	private static final Key STATUS_DETAILS = KeyImpl.init("statusDetails");
	public static final AI INSTANCE = new AIImpl();

	public static PageException toException(AIEngine engine, String msg, String type, String code, int statusCode) {
		String appendix = "";
		if ("model_not_found".equals(code) || msg.equals("invalid_model") || msg.indexOf("models") != -1) {

			String models = AIUtil.getModelNamesAsStringList(engine);
			if (!StringUtil.isEmpty(models, true)) {
				appendix = " Available model names are [" + models + "]";
			}
		}

		PageException ae = new ApplicationException(msg + appendix + "; type:" + type + "; code:" + code + "; status-code:" + statusCode);
		ae.setErrorCode(code);
		return ae;
	}

	public static void valdate(AIEngine aie, int connectTimeout, int socketTimeout) throws PageException {
		AISession session = aie.createSession("keep the answer short", null, -1, -1D, connectTimeout <= 0 ? aie.getConnectTimeout() : connectTimeout,
				socketTimeout <= 0 ? aie.getSocketTimeout() : socketTimeout);
		session.inquiry("ping");
	}

	public static List<String> getModelNames(AIEngine aie) {
		List<AIModel> models = aie.getModels(null);
		List<String> names = new ArrayList<>();
		if (models != null) {
			for (AIModel m: models) {
				names.add(m.getName());
			}
			Collections.sort(names);
		}
		return names;
	}

	public static String findModelName(AIEngine aie, String name) {
		List<AIModel> models = aie.getModels(null);
		if (models != null) {
			for (AIModel m: models) {
				if (m.getName().equalsIgnoreCase(name)) return m.getName();
				if (m.getName().equalsIgnoreCase(name + ":latest")) return m.getName();
				if (m.getLabel().equalsIgnoreCase(name)) return m.getName();
			}
		}
		return null;
	}

	public static String getModelNamesAsStringList(AIEngine aie) {
		return getModelNamesAsStringList(getModelNames(aie));
	}

	public static String getModelNamesAsStringList(List<String> models) {
		StringBuilder sb = new StringBuilder();
		for (String name: models) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(name);
		}
		return sb.toString();
	}

	public static Struct getMetaData(AIEngine aie, boolean addModelsInfo, boolean addFilesInfo) throws PageException {

		Struct meta = new StructImpl();

		meta.set(KeyConstants._label, aie.getLabel());
		meta.set(KeyConstants._model, aie.getModel());
		meta.set(KeyConstants._id, aie.getId());
		if (aie.getTemperature() != null) meta.set(KeyConstants._temperature, aie.getTemperature());
		meta.set("connectTimeout", aie.getConnectTimeout());
		meta.set("conversationSizeLimit", aie.getConversationSizeLimit());
		meta.set(KeyConstants._name, aie.getName());

		// models
		if (addModelsInfo) {
			List<AIModel> models = aie.getModels();
			Query qry = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._label, KeyConstants._description, KeyConstants._custom }, models.size(), "models");
			int row = 0;
			for (AIModel m: models) {
				row++;
				qry.setAt(KeyConstants._name, row, m.getName());
				qry.setAt(KeyConstants._label, row, m.getLabel());
				qry.setAt(KeyConstants._description, row, m.getDescription());
				qry.setAt(KeyConstants._custom, row, m.asStruct());
			}
			meta.set(KeyConstants._models, qry);
		}

		// files
		if (addFilesInfo && aie instanceof AIEngineFile) {
			AIEngineFile aief = (AIEngineFile) aie;
			List<AIFile> files = aief.listFiles();

			// String status, String statusDetails
			Query qry = new QueryImpl(new Key[] { KeyConstants._object, KeyConstants._id, KeyConstants._purpose, KeyConstants._filename, KeyConstants._bytes, CREATED_AT,
					KeyConstants._status, STATUS_DETAILS }, files.size(), "files");
			int row = 0;
			for (AIFile f: files) {
				row++;
				qry.setAt(KeyConstants._object, row, f.getObject());
				qry.setAt(KeyConstants._id, row, f.getId());
				qry.setAt(KeyConstants._purpose, row, f.getPurpose());
				qry.setAt(KeyConstants._filename, row, f.getFilename());
				qry.setAt(KeyConstants._bytes, row, f.getBytes());
				qry.setAt(CREATED_AT, row, f.getCreatedAt());
				qry.setAt(KeyConstants._status, row, f.getStatus());
				qry.setAt(STATUS_DETAILS, row, f.getStatusDetails());
			}
			meta.set(KeyConstants._files, qry);
		}
		return meta;
	}

	private static final String getCharset(ContentType ct) {
		String charset = null;
		if (ct != null) charset = ct.getCharset();
		if (!StringUtil.isEmpty(charset)) return charset;

		PageContext pc = ThreadLocalPageContext.get();
		if (pc != null) return pc.getWebCharset().name();
		return "ISO-8859-1";
	}

	public static void addConversation(AISession session, List<Conversation> history, Conversation conversation) {
		history.add(conversation);
		while (history.size() > session.getConversationSizeLimit()) {
			history.remove(0);
		}
	}

	public static String createJsonContentType(String charset) {
		if (StringUtil.isEmpty(charset, true)) return "application/json";
		return "application/json; charset=" + charset.trim();
	}

	public static int getStatusCode(HTTPResponse response) {
		if (response != null) {
			return response.getStatusCode();
		}
		return -1;
	}

	public static int getStatusCode(CloseableHttpResponse response) {
		if (response != null) {
			StatusLine sl = response.getStatusLine();
			if (sl != null) {
				return sl.getStatusCode();
			}
		}
		return -1;
	}

	public static String extractStringAnswer(Response rsp) {
		if (rsp.isMultiPart()) {
			StringBuilder sb = new StringBuilder();
			String a;
			for (Part rp: rsp.getAnswers()) {
				a = rp.getAsString();
				if (a != null) sb.append(a);
			}
			return sb.toString();
		}
		return rsp.getAnswer();

	}

	public static List<Part> getAnswersFromAnswer(Response rsp) {
		List<Part> parts = new ArrayList<>();
		parts.add(new ResponsePartImpl(rsp.getAnswer()));
		return parts;
	}

	private static class AIImpl implements AI {

		@Override
		public void valdate(AIEngine aie, int connectTimeout, int socketTimeout) throws PageException {
			AIUtil.valdate(aie, connectTimeout, socketTimeout);
		}

		@Override
		public List<String> getModelNames(AIEngine aie) {
			return AIUtil.getModelNames(aie);
		}

		@Override
		public String findModelName(AIEngine aie, String name) {
			return AIUtil.findModelName(aie, name);
		}

		@Override
		public String getModelNamesAsStringList(AIEngine aie) {
			return AIUtil.getModelNamesAsStringList(aie);
		}

		@Override
		public Struct getMetaData(AIEngine aie, boolean addModelsInfo, boolean addFilesInfo) throws PageException {
			return AIUtil.getMetaData(aie, addModelsInfo, addFilesInfo);
		}

		@Override
		public String extractStringAnswer(Response rsp) {
			return AIUtil.extractStringAnswer(rsp);
		}

		@Override
		public List<Part> getAnswersFromAnswer(Response rsp) {
			return AIUtil.getAnswersFromAnswer(rsp);
		}

	}

	public static String serialize(AISession ais) throws PageException {
		Struct data = new StructImpl(StructImpl.TYPE_LINKED);

		data.setEL(KeyConstants._temperature, ais.getTemperature());
		data.setEL(KeyConstants._limit, ais.getConversationSizeLimit());
		data.setEL(KeyConstants._connectionTimeout, ais.getConnectTimeout());
		data.setEL("socketTimeout", ais.getSocketTimeout());
		if (ais instanceof AISessionSupport) data.setEL("systemMessage", ((AISessionSupport) ais).getSystemMessage()); // TODO add to interface
		Request req;
		Response rsp;
		Struct sct;
		Array arr = CFMLEngineFactory.getInstance().getCreationUtil().createArray();
		data.setEL(KeyConstants._history, arr);

		for (Conversation c: ais.getHistory()) {
			sct = new StructImpl(StructImpl.TYPE_LINKED);
			arr.append(sct);

			// request
			req = c.getRequest();
			if (req.isMultiPart()) throw new ApplicationException("serialising multi part request is not supported yet");
			sct.set(KeyConstants._question, req.getQuestion());

			// response
			rsp = c.getResponse();
			if (rsp.isMultiPart()) throw new ApplicationException("serialising multi part response is not supported yet");
			sct.set(KeyConstants._answer, rsp.getAnswer());
		}

		return CFMLEngineFactory.getInstance().getCastUtil().fromStructToJsonString(data, false);
	}

	public static Conversation[] toConversations(Array raw) throws PageException {
		Iterator<Object> it = raw.valueIterator();
		Struct sct;
		Conversation[] conversations = new Conversation[raw.size()];
		int index = 0;
		while (it.hasNext()) {
			sct = Caster.toStruct(it.next());
			conversations[index++] = new ConversationImpl(

					new RequestSupport(Caster.toString(sct.get(KeyConstants._question))),

					new SimpleResponse(Caster.toString(sct.get(KeyConstants._answer)))

			);
		}
		return conversations;
	}
}
