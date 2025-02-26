package lucee.runtime.functions.system;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import lucee.commons.lang.StringUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class SetPropertyString implements Function {

	public static String call(PageContext pc, String fileName, String property, String value, String encoding) throws PageException {
		if (StringUtil.isEmpty(encoding)) encoding = "UTF-8"; // TODO
		try {
			Resource res = ResourceUtil.toResourceNotExisting(pc, fileName);
			if (!res.isFile()) throw new ApplicationException("File ["+ fileName + "] is not a file");

			Properties props = new Properties();
			try (InputStream is = res.getInputStream()) {
				props.load(is);
			}
			props.setProperty(property, value);
			pc.getConfig().getSecurityManager().checkFileLocation(res);
			
			try (OutputStream os = res.getOutputStream(false)) {
				props.store(os, null);
			}

		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return null; // indicate success
	}
}
