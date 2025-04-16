package lucee.runtime.security;

import lucee.commons.io.log.Log;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public interface SecretProvider {

	public void init(Config config, Struct properties, String name) throws PageException;

	public String getSecret(String key) throws PageException;

	public String getSecret(String key, String defaultValue);

	public boolean getSecretAsBoolean(String key) throws PageException;

	public boolean getSecretAsBoolean(String key, boolean defaultValue);

	public int getSecretAsInteger(String key) throws PageException;

	public int getSecretAsInteger(String key, int defaultValue);

	public boolean hasSecret(String key);

	public void refresh() throws PageException; // Force refresh of cached secrets

	public Log getLog();

	public String getName();

}