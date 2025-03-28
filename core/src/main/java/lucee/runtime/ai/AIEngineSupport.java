package lucee.runtime.ai;

import java.util.List;

import lucee.commons.io.log.LogUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public abstract class AIEngineSupport implements AIEngine {

	public static final String DEFAULT_USERAGENT = "Lucee (AI Request)";
	private String id;
	private ClassDefinition<? extends AIEngine> cd;
	private Struct properties;
	private String name;
	private String _default;

	@Override
	public AIEngine init(ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default, String id) throws PageException {
		this.cd = cd;
		this.properties = properties;
		this.name = name;
		this._default = _default;
		this.id = id;
		return this;
	}

	@Override
	public String getDefault() {
		return _default;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ClassDefinition<? extends AIEngine> getClassDefinition() {
		return cd;
	}

	@Override
	public Struct getProperties() {
		return properties;
	}

	public final AISession createSession(String inialMessage) throws PageException {
		return createSession(inialMessage, null, -1, -1D, -1, -1);
	}

	public final AISession createSession(String inialMessage, Conversation[] history) throws PageException {
		return createSession(inialMessage, history, -1, -1D, -1, -1);
	}

	@Override
	public String getId() {
		return id;
	}

	public static void log(Exception e) {
		LogUtil.log("ai", "ai", e);
	}

	@Override
	public List<AIModel> getModels(List<AIModel> defaultValue) {
		try {
			return getModels();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}
}
