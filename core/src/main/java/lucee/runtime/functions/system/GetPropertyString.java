package lucee.runtime.functions.system;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lucee.commons.lang.StringUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class GetPropertyString implements Function {
	
	public static String call(PageContext pc, String fileName, String property, String encoding) throws PageException {
		if (StringUtil.isEmpty(encoding)) encoding = "UTF-8"; // TODO
		try {
			Resource res = ResourceUtil.toResourceNotExisting(pc, fileName);
			if (!res.isFile()) throw new ApplicationException("File ["+ fileName + "] is not a file");

			Properties props = new Properties();
			try (InputStream is = res.getInputStream()) {
				props.load(is);
			}
			return props.getProperty(property, "");
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}
}
