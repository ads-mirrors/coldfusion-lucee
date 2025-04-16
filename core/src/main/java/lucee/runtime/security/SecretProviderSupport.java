package lucee.runtime.security;

import lucee.commons.io.log.Log;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public abstract class SecretProviderSupport implements SecretProvider {

	private Log log;
	private String name;
	private Config config;

	@Override
	public void init(Config config, Struct properties, String name) throws PageException {
		this.log = config.getLog("application");
		this.name = name;
		this.config = config;
	}

	@Override
	public boolean getSecretAsBoolean(String key) throws PageException {
		return Caster.toBooleanValue(getSecret(key));
	}

	@Override
	public boolean getSecretAsBoolean(String key, boolean defaultValue) {
		return Caster.toBooleanValue(getSecret(key, null), defaultValue);
	}

	@Override
	public int getSecretAsInteger(String key) throws PageException {
		return Caster.toIntValue(getSecret(key));
	}

	@Override
	public int getSecretAsInteger(String key, int defaultValue) {
		return Caster.toIntValue(getSecret(key, null), defaultValue);
	}

	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public String getName() {
		return name;
	}

	public Config getConfig() {
		return config;
	}

}
