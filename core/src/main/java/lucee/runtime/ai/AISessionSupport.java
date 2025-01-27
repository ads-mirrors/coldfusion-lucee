package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.config.RequestConfig.Builder;

import lucee.runtime.exp.PageException;
import lucee.runtime.functions.other.CreateUniqueId;

public abstract class AISessionSupport implements AISession {

	protected static final AIResponseListener DEV_NULL_LISTENER = new DevNullAIResponseListener();

	private String id;
	private AIEngine engine;
	List<Conversation> history = new ArrayList<>();

	private int socketTimeout;
	private int connectTimeout;

	public AISessionSupport(AIEngine engine, int connectTimeout, int socketTimeout) {
		this.engine = engine;

		if (socketTimeout < 0) this.socketTimeout = engine.getSocketTimeout();
		else this.socketTimeout = socketTimeout;

		if (connectTimeout < 0) this.connectTimeout = engine.getConnectTimeout();
		else this.connectTimeout = connectTimeout;
	}

	@Override
	public final int getConnectTimeout() {
		return connectTimeout;
	}

	@Override
	public final int getSocketTimeout() {
		return socketTimeout;
	}

	@Override
	public final AIEngine getEngine() {
		return engine;
	}

	@Override
	public final Conversation[] getHistory() {
		return history.toArray(new Conversation[history.size()]);
	}

	protected final List<Conversation> getHistoryAsList() {
		return history;
	}

	@Override
	public final String getId() {
		if (id == null) {
			id = CreateUniqueId.invoke();
		}
		return id;
	}

	@Override
	public Response inquiry(String message) throws PageException {
		return inquiry(message, null);
	}

	public static Builder setTimeout(Builder builder, AISession session) {
		if (session.getConnectTimeout() > 0) builder.setConnectTimeout(session.getConnectTimeout());
		if (session.getSocketTimeout() > 0) builder.setSocketTimeout(session.getSocketTimeout());
		return builder;
	}

	public static Builder setTimeout(Builder builder, int connectTimeout, int socketTimeout) {
		if (connectTimeout > 0) builder.setConnectTimeout(connectTimeout);
		if (socketTimeout > 0) builder.setSocketTimeout(socketTimeout);
		return builder;
	}
}
