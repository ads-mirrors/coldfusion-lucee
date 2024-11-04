package lucee.runtime.functions.mvn;

import java.io.IOException;
import java.util.Iterator;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.mvn.POM;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class MavenLoad implements Function {

	private static final long serialVersionUID = -7512374040201658763L;

	public static Array call(PageContext pc, Object input) throws PageException {
		Array array = null;
		if (Decision.isArray(input)) {
			array = Caster.toArray(input);
		}
		else if (Decision.isStruct(input)) {
			array = new ArrayImpl();
			array.append(Caster.toStruct(input));
		}
		else {
			throw new FunctionException(pc, "MavenLoad", 1, "input", "Invalid type [" + Caster.toTypeName(input)
					+ "]. Input must be a struct formatted as [{\"groupId\" : \"org.lucee\", \"artifactId\" : \"lucee\", \"version\" : \"7.0.0.0\"}] or an array containing multiple such structs.");
		}

		Iterator<Object> it = array.valueIterator();
		Struct sct;
		String g, a, v;
		Struct rtnData = new StructImpl();

		Resource dir = ((ConfigPro) pc.getConfig()).getMavenDir();
		Log log = LogUtil.getLog(pc.getConfig(), "mvn", "application");

		while (it.hasNext()) {
			sct = Caster.toStruct(it.next());

			g = Caster.toString(sct.get(KeyConstants._groupId, null), null);
			if (StringUtil.isEmpty(g)) g = Caster.toString(sct.get(KeyConstants._g, null), null);
			if (StringUtil.isEmpty(g)) {
				throw new FunctionException(pc, "MavenLoad", 1, "input", "Missing required field: groupId. Ensure that the 'groupId' key is present and not empty.");
			}

			a = Caster.toString(sct.get(KeyConstants._artifactId, null), null);
			if (StringUtil.isEmpty(a)) a = Caster.toString(sct.get(KeyConstants._a, null), null);
			if (StringUtil.isEmpty(a)) {
				throw new FunctionException(pc, "MavenLoad", 1, "input", "Missing required field: artifactId. Ensure that the 'artifactId' key is present and not empty.");
			}

			v = Caster.toString(sct.get(KeyConstants._version, null), null);
			if (StringUtil.isEmpty(v)) v = Caster.toString(sct.get(KeyConstants._v, null), null);
			if (StringUtil.isEmpty(v)) {
				throw new FunctionException(pc, "MavenLoad", 1, "input", "Missing required field: version. Ensure that the 'version' key is present and not empty.");
			}

			try {
				for (Resource r: POM.getInstance(dir, g, a, v, POM.SCOPE_COMPILE, log).getJars()) {
					rtnData.set(r.getAbsolutePath(), "");
				}
			}
			catch (IOException ioe) {
				throw Caster.toPageException(ioe);
			}
		}

		Array rtn = new ArrayImpl();
		for (Key k: rtnData.keys()) {
			rtn.append(k.getString());
		}
		return rtn;
	}
}
