package lucee.runtime.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.URLDecoder;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ListUtil;

/**
 * this class is to backport a fix from Lucee 6.1 without touching the existing functionality in any
 * way
 */
public class ConfigWebUtilPatch {

	public static int STYLE_NONE = 0;
	public static int STYLE_CCS = 1; // a:1;b:2
	public static int STYLE_URL = 2; // a=1&b=2

	public static Map<String, String> getAsMap(Struct input, int style, String... names) throws PageException {
		Struct sct = getAsStruct(input, style, names);
		if (sct == null) return null;
		if (sct.isEmpty()) return new HashMap<>();
		Map<String, String> map = new HashMap<>();
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			map.put(e.getKey().getString(), Caster.toString(e.getValue()));
		}
		return map;
	}

	public static Struct getAsStruct(Struct input, int style, String... names) {
		Struct sct = null;
		if (input == null) return sct;

		Object obj;
		for (String name: names) {
			obj = input.get(name, null);
			if (obj instanceof Struct && !(sct = (Struct) obj).isEmpty()) {
				break;
			}
		}
		if (sct == null) {
			if (STYLE_CCS == style) {
				for (String name: names) {
					obj = input.get(name, null);
					if (obj instanceof CharSequence && !StringUtil.isEmpty(obj.toString(), true)) {
						sct = toStruct(obj.toString().trim(), ';', ':');
						if (!sct.isEmpty()) break;
					}
				}
			}
			else if (STYLE_URL == style) {
				for (String name: names) {
					obj = input.get(name, null);
					if (obj instanceof CharSequence && !StringUtil.isEmpty(obj.toString(), true)) {
						sct = toStruct(obj.toString().trim(), '&', '=');
						if (!sct.isEmpty()) break;
					}
				}
			}
		}

		if (sct == null) {
			sct = new StructImpl(Struct.TYPE_LINKED);
			input.put(names[0], sct);
			return sct;
		}
		return (Struct) replaceConfigPlaceHolders(sct);
	}

	public static Struct toStruct(String str, char entriesSeparator, char entrySeparator) {

		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		try {
			String[] arr = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(str, entriesSeparator));
			String[] item;
			for (int i = 0; i < arr.length; i++) {
				item = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(arr[i], entrySeparator));
				if (item.length == 2) sct.setEL(KeyImpl.init(URLDecoder.decode(item[0], true).trim()), replaceConfigPlaceHolder(URLDecoder.decode(item[1], true)));
				else if (item.length == 1) sct.setEL(KeyImpl.init(URLDecoder.decode(item[0], true).trim()), "");
			}
		}
		catch (PageException ee) {
		}

		return sct;
	}

	public static Object replaceConfigPlaceHolders(Object obj) {
		if (obj == null) return obj;

		// handle simple value
		if (Decision.isSimpleValue(obj)) {
			if (obj instanceof CharSequence) return replaceConfigPlaceHolder(obj.toString());
			return obj;
		}

		// handle collection
		if (obj instanceof lucee.runtime.type.Collection) {
			return replaceConfigPlaceHolders((lucee.runtime.type.Collection) obj);
		}

		return obj;
	}

	public static lucee.runtime.type.Collection replaceConfigPlaceHolders(lucee.runtime.type.Collection data) {
		if (data == null) return data;

		lucee.runtime.type.Collection repl;
		if (data instanceof Struct) repl = new StructImpl();
		else if (data instanceof Array) repl = new ArrayImpl();
		else return data;
		Iterator<Entry<Key, Object>> it = data.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			repl.setEL(e.getKey(), replaceConfigPlaceHolders(e.getValue()));
		}
		return repl;
	}

	public static String replaceConfigPlaceHolder(String v) {
		if (StringUtil.isEmpty(v) || v.indexOf('{') == -1) return v;

		int s = -1, e = -1, d = -1;
		int prefixLen, start = -1, end;
		String _name, _prop;
		while ((s = v.indexOf("{system:", start)) != -1 | /* don't change */
				(e = v.indexOf("{env:", start)) != -1 | /* don't change */
				(d = v.indexOf("${", start)) != -1) {
			boolean isSystem = false, isDollar = false;
			// system
			if (s > -1 && (e == -1 || e > s)) {
				start = s;
				prefixLen = 8;
				isSystem = true;
			}
			// env
			else if (e > -1) {
				start = e;
				prefixLen = 5;
			}
			// dollar
			else {
				start = d;
				prefixLen = 2;
				isDollar = true;
			}

			end = v.indexOf('}', start);
			if (end > prefixLen) {
				_name = v.substring(start + prefixLen, end);
				// print.edate(_name);
				if (isDollar) {
					String[] _parts = _name.split(":");
					_prop = SystemUtil.getSystemPropOrEnvVar(_parts[0], (_parts.length > 1) ? _parts[1] : null);
				}
				else {
					_prop = isSystem ? System.getProperty(_name) : System.getenv(_name);
				}

				if (_prop != null) {
					v = new StringBuilder().append(v.substring(0, start)).append(_prop).append(v.substring(end + 1)).toString();
					start += _prop.length();
				}
				else start = end;
			}
			else start = end; // set start to end for the next round
			s = -1;
			e = -1; // reset index
			d = -1; // I don't think we need this?
		}
		return v;
	}
}
