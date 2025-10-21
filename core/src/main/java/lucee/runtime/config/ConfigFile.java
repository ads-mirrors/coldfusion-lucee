package lucee.runtime.config;

import java.io.IOException;
import java.nio.charset.Charset;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public class ConfigFile {
	public static void write(Resource configFile, Struct root) throws IOException, ConverterException {
		write(configFile, root, CharsetUtil.UTF8);
	}

	public static void write(Resource configFile, Struct root, Charset charset) throws IOException, ConverterException {
		synchronized (SystemUtil.createToken("ConfigFile", ResourceUtil.getNormalizedPathEL(configFile))) {
			LogUtil.logGlobal((Config) null, Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "writing the config file [" + configFile + "]");
			_write(configFile, root);
		}
	}

	public static Struct read(Resource configFile) throws PageException, IOException {
		return read(configFile, CharsetUtil.UTF8);
	}

	public static Struct read(Resource configFile, Charset charset) throws PageException, IOException {
		synchronized (SystemUtil.createToken("ConfigFile", ResourceUtil.getNormalizedPathEL(configFile))) {
			LogUtil.logGlobal((Config) null, Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "read the config file [" + configFile + "]");
			return _read(configFile);
		}
	}

	public static Struct reload(Resource configFile, Struct root) throws IOException, ConverterException, PageException {
		synchronized (SystemUtil.createToken("ConfigFile", ResourceUtil.getNormalizedPathEL(configFile))) {
			LogUtil.logGlobal((Config) null, Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "reloading the config file [" + configFile + "]");
			_write(configFile, root);
			return _read(configFile);
		}
	}

	private static void _write(Resource configFile, Struct root) throws IOException, ConverterException {
		JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
		String str = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_ROW, true);
		IOUtil.write(configFile, str, CharsetUtil.UTF8, false, true);

	}

	private static Struct _read(Resource configFile) throws PageException, IOException {
		return Caster.toStruct(new JSONExpressionInterpreter().interpret(null, IOUtil.toString(configFile, CharsetUtil.UTF8)));
	}

}
