package lucee.runtime.functions.system;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import lucee.commons.lang.StringUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public final class GetPropertyFile implements Function {
	
	public static Struct call(PageContext pc, String fileName, String encoding) throws PageException {
		if (StringUtil.isEmpty(encoding)) encoding = "UTF-8"; // TODO
		try {
			Resource res = ResourceUtil.toResourceNotExisting(pc, fileName);
			if (!res.isFile()) throw new ApplicationException("File ["+ fileName + "] is not a file");
			
			Properties props = new Properties();
			try (InputStream is = res.getInputStream()) {
				props.load(is);
			}
			Enumeration<Object> keys = props.keys();
			Struct sct = new StructImpl(Struct.TYPE_LINKED);

			while(keys.hasMoreElements()){
				String key = (String)keys.nextElement();
				sct.set(Caster.toKey(key), props.getProperty(key));
			}
			return sct;
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}
}
