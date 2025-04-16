package lucee.runtime.security;

import java.util.Date;

import org.osgi.framework.BundleException;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpRow;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.SimpleValue;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.KeyConstants;

public class SecretProviderFactory {

	public static SecretProvider getInstance(Config config, String name, Struct data) throws PageException, ClassException, BundleException {
		ClassDefinition<SecretProvider> cd;

		cd = ConfigFactoryImpl.getClassDefinition(data, "", config.getIdentification());
		if (cd.hasClass()) {

			Struct custom = Caster.toStruct(data.get(KeyConstants._custom, null), null);
			if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._properties, null), null);
			if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._arguments, null), null);
			String _default = Caster.toString(data.get(KeyConstants._default, null), null);
			return getInstance(config, cd, custom, name, _default);
		}
		throw new ApplicationException("class defintion is invalid");
	}

	public static SecretProvider getInstance(Config config, ClassDefinition<? extends SecretProvider> cd, Struct properties, String name, String _default)
			throws PageException, ClassException, BundleException {
		// String id = createId(cd, properties, name, _default);

		SecretProvider sp = (SecretProvider) ClassUtil.loadInstance(cd.getClazz());
		LogUtil.logx(config, Log.LEVEL_TRACE, "secret-provider-factory", "create Secret Provider instance [" + cd.toString() + "]", "application");
		sp.init(config, properties, name);
		return sp;
	}

	public static class Ref implements SimpleValue, Dumpable, Castable {

		private SecretProvider sp;
		private String key;

		public Ref(SecretProvider sp, String key) {
			this.sp = sp;
			this.key = key;
		}

		@Override
		public Boolean castToBoolean(Boolean defaultValue) {
			String str = castToString(null);
			if (str == null) return defaultValue;
			return Caster.toBoolean(str, defaultValue);
		}

		@Override
		public boolean castToBooleanValue() throws PageException {
			return Caster.toBooleanValue(castToString());
		}

		@Override
		public DateTime castToDateTime() throws PageException {
			return Caster.toDate(castToString());
		}

		@Override
		public DateTime castToDateTime(DateTime defaultValue) {
			String str = castToString(null);
			if (str == null) return defaultValue;
			return Caster.toDate(str, false, null, defaultValue);
		}

		@Override
		public double castToDoubleValue() throws PageException {
			return Caster.toDoubleValue(castToString());
		}

		@Override
		public double castToDoubleValue(double defaultValue) {
			String str = castToString(null);
			if (str == null) return defaultValue;
			return Caster.toDoubleValue(str, defaultValue);
		}

		@Override
		public String castToString() throws PageException {
			if (LogUtil.doesTrace(sp.getLog())) {
				sp.getLog().log(Log.LEVEL_TRACE, "secret", "read secret [" + key + "] from provider [" + sp.getName() + "] at " + LogUtil.caller(null, ""));
			}
			return sp.getSecret(key);
		}

		@Override
		public String castToString(String defaultValue) {
			if (LogUtil.doesTrace(sp.getLog())) {
				sp.getLog().log(Log.LEVEL_TRACE, "secret", "read secret [" + key + "] from provider [" + sp.getName() + "] at " + LogUtil.caller(null, ""));
			}
			return sp.getSecret(key, defaultValue);
		}

		@Override
		public String toString() {
			String str = sp.getSecret(key, null);
			if (str == null) return super.toString();
			return str;
		}

		@Override
		public int compareTo(String other) throws PageException {
			return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), other);
		}

		@Override
		public int compareTo(boolean other) throws PageException {
			return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), other);
		}

		@Override
		public int compareTo(double other) throws PageException {
			return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), other);
		}

		@Override
		public int compareTo(DateTime other) throws PageException {
			return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), (Date) other);
		}

		@Override
		public DumpData toDumpData(PageContext pc, int maxlevel, DumpProperties props) {
			DumpTable table = new DumpTable("secret", "#90b800", "#cad1b0", "#000000");
			table.appendRow(new DumpRow(0, new SimpleDumpData(obfuscate(castToString(null)))));
			table.setTitle("Secret");
			return table;
		}

		public Ref touch() throws PageException {
			castToString();
			return this;
		}

	}

	private static String obfuscate(String secret) {
		String val;
		if (StringUtil.isEmpty(secret, true)) val = "***";
		else if (secret.length() < 6) {
			StringBuilder sb = new StringBuilder(secret.length());
			for (int i = secret.length(); i >= 0; i--) {
				sb.append('*');
			}
			val = sb.toString();
		}
		else {
			StringBuilder sb = new StringBuilder(secret.length());
			for (int i = secret.length(); i > 1; i--) {
				sb.append('*');
			}
			val = secret.substring(0, 2) + sb.toString() + secret.substring(secret.length() - 2);
		}
		return val;
	}
}
