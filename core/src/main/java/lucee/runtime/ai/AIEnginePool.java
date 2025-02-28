package lucee.runtime.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;

public class AIEnginePool {

	private Map<String, AIEngine> instances = new ConcurrentHashMap<>();

	public AIEngine getEngine(Config config, String name) throws PageException {
		// get existing instance
		AIEngine aie = instances.get(name);
		if (aie != null) return aie;

		// loading new instance
		AIEngineFactory factory = ((ConfigPro) config).getAIEngineFactory(name.toLowerCase());
		if (factory == null) {
			throw new ApplicationException(ExceptionUtil.similarKeyMessage(((ConfigPro) config).getAIEngineFactoryNames(), name, "source", "sources", "ai pool", true));
		}

		try {
			aie = AIEngineFactory.getInstance(config, factory);
			if (aie != null) {
				instances.put(name, aie);
				return aie;
			}
		}
		catch (Exception e) {
			ApplicationException ae = new ApplicationException("Cannot create and instance of the AI engine [" + name + "]; " + e.getMessage());
			ExceptionUtil.initCauseEL(ae, e);
			throw ae;
		}
		throw new ApplicationException("there is no matching engine for the name [" + name + "] found");
	}

}