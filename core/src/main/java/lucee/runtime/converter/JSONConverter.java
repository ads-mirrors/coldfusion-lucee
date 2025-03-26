/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.converter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.w3c.dom.Node;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.PageContext;
import lucee.runtime.coder.Base64Coder;
import lucee.runtime.component.Property;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.engine.Controler;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.java.JavaObject;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.ISerializationSettings;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;

/**
 * class to serialize and desirilize WDDX Packes
 */
public final class JSONConverter extends ConverterSupport {

	private static final Collection.Key REMOTING_FETCH = KeyConstants._remotingFetch;

	private static final Key TO_JSON = KeyConstants.__toJson;
	private static final String NULL_STRING = "";

	private boolean ignoreRemotingFetch;

	private CharsetEncoder charsetEncoder;

	private String pattern;

	private boolean compact;
	private String eol;
	private String indent1;
	private String indent2;
	private String indent3;
	private String indent4;
	private String indent5;
	private String indent6;
	private String indent7;
	private String indent8;
	private String indent9;
	private String indent10;
	private int level = 0;

	/**
	 * @param ignoreRemotingFetch
	 * @param charset if set, characters not supported by the charset are escaped.
	 */
	public JSONConverter(boolean ignoreRemotingFetch, Charset charset) {
		this(ignoreRemotingFetch, charset, JSONDateFormat.PATTERN_CF, true, null);
	}

	public JSONConverter(boolean ignoreRemotingFetch, Charset charset, String pattern) {
		this(ignoreRemotingFetch, charset, pattern, true, null);
	}

	public JSONConverter(boolean ignoreRemotingFetch, Charset charset, String pattern, boolean compact) {
		this(ignoreRemotingFetch, charset, pattern, compact, null);
	}

	public JSONConverter(boolean ignoreRemotingFetch, Charset charset, String pattern, boolean compact, String indent) {
		this.ignoreRemotingFetch = ignoreRemotingFetch;
		charsetEncoder = charset != null ? charset.newEncoder() : null;// .canEncode("string");
		this.pattern = pattern;
		this.compact = compact;

		this.eol = compact ? "" : SystemUtil.getOSSpecificLineSeparator();
		this.indent1 = compact ? "" : (indent == null ? "  " : indent);
		this.indent2 = indent1 + indent1;
		this.indent3 = indent1 + indent1 + indent1;
		this.indent4 = indent1 + indent1 + indent1 + indent1;
		this.indent5 = indent1 + indent1 + indent1 + indent1 + indent1;
		this.indent6 = indent1 + indent1 + indent1 + indent1 + indent1 + indent1;
		this.indent7 = indent1 + indent1 + indent1 + indent1 + indent1 + indent1 + indent1;
		this.indent8 = indent1 + indent1 + indent1 + indent1 + indent1 + indent1 + indent1 + indent1;
		this.indent9 = indent1 + indent1 + indent1 + indent1 + indent1 + indent1 + indent1 + indent1 + indent1;
		this.indent10 = indent1 + indent1 + indent1 + indent1 + indent1 + indent1 + indent1 + indent1 + indent1 + indent1;

	}

	/**
	 * serialize Serializable class
	 * 
	 * @param serializable
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 * @throws IOException
	 */

	private void _serializeClass(PageContext pc, Class clazz, Object obj, Appendable sb, int queryFormat, Boolean preserveCase, Set<Object> done)
			throws ConverterException, IOException {

		Set<Object> tmp = new HashSet<>();

		Struct sct = new StructImpl(Struct.TYPE_LINKED);

		// Fields
		Field[] fields = clazz.getFields();
		Field field;

		for (int i = 0; i < fields.length; i++) {
			field = fields[i];
			if (obj != null || (field.getModifiers() & Modifier.STATIC) > 0) try {
				sct.setEL(field.getName(), testRecusrion(field.get(obj), tmp));
			}
			catch (Exception e) {
				LogUtil.log(pc, Controler.class.getName(), e);
			}
		}
		if (obj != null) {
			// setters
			List<lucee.transformer.dynamic.meta.Method> setters = Reflector.getSetters(clazz);
			for (lucee.transformer.dynamic.meta.Method setter: setters) {
				sct.setEL(setter.getName().substring(3), CollectionUtil.NULL);
			}
			// getters
			List<lucee.transformer.dynamic.meta.Method> getters = Reflector.getGetters(clazz);
			for (lucee.transformer.dynamic.meta.Method getter: getters) {
				try {
					sct.setEL(getter.getName().substring(3), testRecusrion(getter.invoke(obj, ArrayUtil.OBJECT_EMPTY), tmp));

				}
				catch (Exception e) {
				}
			}
		}

		try {
			Iterator<Object> iterator = tmp.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				if (done.contains(o)) {
					iterator.remove();
				}
				else {
					done.add(o);
				}
			}

			_serializeStruct(pc, sct, sb, queryFormat, preserveCase, true, done);
		}
		finally {
			for (Object o: tmp) {
				done.remove(o);
			}
		}
	}

	private Object testRecusrion(Object obj, Set<Object> tmp) {
		tmp.add(obj);
		return obj;
	}

	/**
	 * serialize a Date
	 * 
	 * @param date Date to serialize
	 * @param sb
	 * @throws IOException
	 * @throws ConverterException
	 */
	private void _serializeDate(Date date, Appendable sb) throws IOException {
		_serializeDateTime(new DateTimeImpl(date), sb);
	}

	/**
	 * serialize a DateTime
	 * 
	 * @param dateTime DateTime to serialize
	 * @param sb
	 * @throws IOException
	 * @throws ConverterException
	 */
	private void _serializeDateTime(DateTime dateTime, Appendable sb) throws IOException {

		sb.append(StringUtil.escapeJS(JSONDateFormat.format(dateTime, null, pattern), '"', charsetEncoder));

		/*
		 * try { sb.append(goIn()); sb.append("createDateTime(");
		 * sb.append(DateFormat.call(null,dateTime,"yyyy,m,d")); sb.append(' ');
		 * sb.append(TimeFormat.call(null,dateTime,"HH:mm:ss")); sb.append(')'); } catch (PageException e) {
		 * throw new ConverterException(e); }
		 */
		// Januar, 01 2000 01:01:01
	}

	/**
	 * serialize an Array
	 * 
	 * @param array Array to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 * @throws IOException
	 */
	private void _serializeArray(PageContext pc, Array array, Appendable sb, int queryFormat, Boolean preserveCase, Set<Object> done) throws ConverterException, IOException {
		_serializeList(pc, array.toList(), sb, queryFormat, preserveCase, done);
	}

	/**
	 * serialize a List (as Array)
	 * 
	 * @param list List to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 * @throws IOException
	 */
	private void _serializeList(PageContext pc, List list, Appendable sb, int queryFormat, Boolean preserveCase, Set<Object> done) throws ConverterException, IOException {
		sb.append("[");
		sb.append(eol);
		right();

		boolean doIt = false;
		ListIterator it = list.listIterator();
		while (it.hasNext()) {
			if (doIt) {
				sb.append(',');
				sb.append(eol);
				sb.append(indent());
			}
			else {
				sb.append(indent());
			}
			doIt = true;
			_serialize(pc, it.next(), sb, queryFormat, preserveCase, done);
		}

		sb.append(eol);
		left();
		sb.append(indent());
		sb.append(']');
	}

	private void _serializeArray(PageContext pc, Object[] arr, Appendable sb, int queryFormat, Boolean preserveCase, Set<Object> done) throws ConverterException, IOException {
		sb.append("[");
		sb.append(eol);
		right();

		for (int i = 0; i < arr.length; i++) {
			if (i > 0) {
				sb.append(',');
				sb.append(eol);
				sb.append(indent());
			}
			else {
				sb.append(indent());
			}

			_serialize(pc, arr[i], sb, queryFormat, preserveCase, done);
		}

		sb.append(eol);
		left();
		sb.append(indent());
		sb.append(']');
	}

	/**
	 * serialize a Struct
	 * 
	 * @param pc
	 * @param test
	 * @param struct Struct to serialize
	 * @param sb
	 * @param queryFormat
	 * @param preserveCase
	 * @param addUDFs
	 * @param done
	 * @throws ConverterException
	 * @throws IOException
	 */
	public void _serializeStruct(PageContext pc, Struct struct, Appendable sb, int queryFormat, Boolean preserveCase, boolean addUDFs, Set<Object> done)
			throws ConverterException, IOException {

		// preserve case by default for Struct
		boolean preCase = getPreserveCase(pc, preserveCase, false);
		// Component
		if (struct instanceof Component) {
			String res = castToJson(pc, (Component) struct, NULL_STRING);
			if (res != NULL_STRING) {
				sb.append(res);
				return;
			}
		}

		sb.append("{");
		sb.append(eol);
		right();
		Iterator<Entry<Key, Object>> it = struct.entryIterator();
		Entry<Key, Object> e;
		String k;
		Object value;
		boolean doIt = false;
		while (it.hasNext()) {

			e = it.next();
			k = e.getKey().getString();
			if (!preCase) k = k.toUpperCase();
			value = e.getValue();

			if (!addUDFs && (value instanceof UDF || value == null)) continue;
			if (doIt) {
				sb.append(',');
				sb.append(eol);
				sb.append(indent());
			}
			else {
				sb.append(indent());
			}

			doIt = true;
			sb.append(StringUtil.escapeJS(k, '"', charsetEncoder));
			sb.append(compact ? ":" : ": ");
			_serialize(pc, value, sb, queryFormat, preserveCase, done);
		}

		if (struct instanceof Component) {
			Boolean remotingFetch;
			Component comp = (Component) struct;
			boolean isPeristent = false;
			isPeristent = comp.isPersistent();
			ApplicationContextSupport acs = (ApplicationContextSupport) pc.getApplicationContext();
			boolean triggerDataMember = acs.getTriggerComponentDataMember();

			Property[] props = comp.getProperties(false, true, false, false);
			ComponentScope scope = comp.getComponentScope();
			for (int i = 0; i < props.length; i++) {
				if (!ignoreRemotingFetch) {
					remotingFetch = Caster.toBoolean(props[i].getDynamicAttributes().get(REMOTING_FETCH, null), null);
					if (remotingFetch == null) {
						if (isPeristent && ORMUtil.isRelated(props[i])) continue;
					}
					else if (!remotingFetch.booleanValue()) continue;

				}
				Key key = KeyImpl.init(props[i].getName());
				if (triggerDataMember) value = comp.get(pc, key, null);
				else value = scope.get(key, null);

				if (!addUDFs && (value instanceof UDF || value == null)) continue;
				if (doIt) sb.append(',');
				doIt = true;
				sb.append(StringUtil.escapeJS(key.getString(), '"', charsetEncoder));
				sb.append(compact ? ":" : ": ");
				_serialize(pc, value, sb, queryFormat, preserveCase, done);
			}
		}
		sb.append(eol);
		left();
		sb.append(indent());
		sb.append('}');
	}

	private static String castToJson(PageContext pc, Component c, String defaultValue) throws ConverterException {
		Object o = c.get(TO_JSON, null);
		if (!(o instanceof UDF)) return defaultValue;
		UDF udf = (UDF) o;
		if (udf.getReturnType() != CFTypes.TYPE_VOID && udf.getFunctionArguments().length == 0) {
			try {
				return Caster.toString(c.call(pc, TO_JSON, new Object[0]));
			}
			catch (PageException e) {
				throw toConverterException(e);
			}
		}
		return defaultValue;
	}

	/**
	 * serialize a Map (as Struct)
	 * 
	 * @param map Map to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 * @throws IOException
	 */
	private void _serializeMap(PageContext pc, Map map, Appendable sb, int queryFormat, Boolean preserveCase, Set<Object> done) throws ConverterException, IOException {
		sb.append("{");
		sb.append(eol);
		right();

		Iterator it = map.keySet().iterator();
		boolean doIt = false;
		while (it.hasNext()) {
			Object key = it.next();
			if (doIt) {
				sb.append(',');
				sb.append(eol);
				sb.append(indent());
			}
			else {
				sb.append(indent());
			}
			doIt = true;
			sb.append(StringUtil.escapeJS(key.toString(), '"', charsetEncoder));
			sb.append(compact ? ":" : ": ");
			_serialize(pc, map.get(key), sb, queryFormat, preserveCase, done);
		}
		sb.append(eol);
		left();
		sb.append(indent());
		sb.append('}');
	}

	/**
	 * serialize a Component
	 * 
	 * @param component Component to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 * @throws IOException
	 */
	private void _serializeComponent(PageContext pc, Component component, Appendable sb, int queryFormat, Boolean preserveCase, Set<Object> done)
			throws ConverterException, IOException {
		ComponentSpecificAccess cw = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, component);
		_serializeStruct(pc, cw, sb, queryFormat, preserveCase, false, done);
	}

	private void _serializeUDF(PageContext pc, UDF udf, Appendable sb, int queryFormat, Boolean preserveCase, Set<Object> done) throws ConverterException, IOException {
		Struct sct = new StructImpl();
		try {
			// Meta
			Struct meta = udf.getMetaData(pc);
			sct.setEL("Metadata", meta);

			// Parameters
			sct.setEL("MethodAttributes", meta.get("PARAMETERS"));
		}
		catch (PageException e) {
			throw toConverterException(e);
		}

		sct.setEL("Access", ComponentUtil.toStringAccess(udf.getAccess(), "public"));
		sct.setEL("Output", Caster.toBoolean(udf.getOutput()));
		sct.setEL("ReturnType", udf.getReturnTypeAsString());
		try {
			sct.setEL("PagePath", udf.getSource());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		_serializeStruct(pc, sct, sb, queryFormat, preserveCase, true, done);
		// TODO key SuperScope and next?
	}

	/**
	 * serialize a Query
	 * 
	 * @param query Query to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 * @throws IOException
	 */
	private void _serializeQuery(PageContext pc, Query query, Appendable sb, int queryFormat, Boolean preserveCase, Set<Object> done) throws ConverterException, IOException {

		boolean preCase = getPreserveCase(pc, preserveCase, true); // UPPERCASE column keys by default for Query

		Collection.Key[] _keys = CollectionUtil.keys(query);

		if (queryFormat == SerializationSettings.SERIALIZE_AS_STRUCT) {
			sb.append(indent());
			sb.append("[");
			int rc = query.getRecordcount();
			for (int row = 1; row <= rc; row++) {
				if (row > 1) sb.append(',');
				sb.append("{");
				for (int col = 0; col < _keys.length; col++) {
					if (col > 0) sb.append(',');
					sb.append(StringUtil.escapeJS(preCase ? _keys[col].getString() : _keys[col].getUpperString(), '"', charsetEncoder));
					sb.append(compact ? ":" : ": ");
					try {
						_serialize(pc, query.getAt(_keys[col], row), sb, queryFormat, preserveCase, done);
					}
					catch (PageException e) {
						_serialize(pc, e.getMessage(), sb, queryFormat, preserveCase, done);
					}
				}

				sb.append("}");
			}
			sb.append("]");

			return;
		}

		sb.append("{");
		sb.append(eol);
		right();
		// Rowcount
		if (queryFormat == SerializationSettings.SERIALIZE_AS_COLUMN) {
			sb.append(indent());
			sb.append("\"ROWCOUNT\"" + (compact ? ":" : ": "));
			sb.append(Caster.toString(query.getRecordcount()));
			sb.append(',');
			sb.append(eol);
		}

		// Columns
		sb.append(indent());
		sb.append("\"COLUMNS\"" + (compact ? ":" : ": ") + "[");
		sb.append(eol);
		right();
		String[] cols = query.getColumns();
		for (int i = 0; i < cols.length; i++) {
			if (i > 0) {
				sb.append(',');
				sb.append(eol);
				sb.append(indent());
			}
			else {
				sb.append(indent());
			}

			sb.append(StringUtil.escapeJS(preCase ? cols[i] : cols[i].toUpperCase(), '"', charsetEncoder));
		}
		sb.append(eol);
		left();
		sb.append(indent());
		sb.append("],");

		// Data
		sb.append(eol);
		sb.append(indent());
		sb.append("\"DATA\"" + (compact ? ":" : ": "));
		if (queryFormat == SerializationSettings.SERIALIZE_AS_COLUMN) {
			sb.append('{');
			sb.append(eol);
			right();
			boolean oDoIt = false;
			int len = query.getRecordcount();
			pc = ThreadLocalPageContext.get(pc);
			boolean upperCase = false;
			if (pc != null) upperCase = ((ConfigWebPro) pc.getConfig()).getDotNotationUpperCase();

			for (int i = 0; i < _keys.length; i++) {
				if (oDoIt) {
					sb.append(',');
					sb.append(eol);
					sb.append(indent());
				}
				else {
					sb.append(indent());
				}
				oDoIt = true;

				sb.append(StringUtil.escapeJS(upperCase ? _keys[i].getUpperString() : _keys[i].getString(), '"', charsetEncoder));
				sb.append((compact ? ":" : ": ") + "[");
				sb.append(eol);
				right();
				boolean doIt = false;
				for (int y = 1; y <= len; y++) {
					if (doIt) {
						sb.append(',');
						sb.append(eol);
						sb.append(indent());
					}
					else {
						sb.append(indent());
					}
					doIt = true;
					try {
						_serialize(pc, query.getAt(_keys[i], y), sb, queryFormat, preserveCase, done);
					}
					catch (PageException e) {
						_serialize(pc, e.getMessage(), sb, queryFormat, preserveCase, done);
					}
				}
				sb.append(eol);
				left();
				sb.append(indent());
				sb.append(']');
			}
			sb.append(eol);
			left();
			sb.append(indent());
			sb.append('}');
		}
		else {
			sb.append('[');
			sb.append(eol);
			right();
			boolean oDoIt = false;
			int len = query.getRecordcount();
			for (int row = 1; row <= len; row++) {
				if (oDoIt) {
					sb.append(',');
					sb.append(eol);
					sb.append(indent());
				}
				else {
					sb.append(indent());
				}
				oDoIt = true;

				sb.append("[");
				sb.append(eol);
				right();
				boolean doIt = false;
				for (int col = 0; col < _keys.length; col++) {
					if (doIt) {
						sb.append(',');
						sb.append(eol);
						sb.append(indent());
					}
					else {
						sb.append(indent());
					}
					doIt = true;
					try {
						_serialize(pc, query.getAt(_keys[col], row), sb, queryFormat, preserveCase, done);
					}
					catch (PageException e) {
						_serialize(pc, e.getMessage(), sb, queryFormat, preserveCase, done);
					}
				}
				sb.append(eol);
				left();
				sb.append(indent());
				sb.append(']');
			}
			sb.append(eol);
			left();
			sb.append(indent());
			sb.append(']');
		}

		sb.append(eol);
		left();
		sb.append(indent());
		sb.append('}');
	}

	/**
	 * serialize an Object to his xml Format represenation
	 * 
	 * @param object Object to serialize
	 * @param sb Appendable to write data
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 * @throws IOException
	 */
	private void _serialize(PageContext pc, Object object, Appendable sb, int queryFormat, Boolean preserveCase, Set done) throws ConverterException, IOException {
		// NULL
		if (object == null || object == CollectionUtil.NULL) {
			sb.append("null");
			return;
		}
		// String
		if (object instanceof CharSequence) {
			sb.append(StringUtil.escapeJS(object.toString(), '"', charsetEncoder));
			return;
		}
		// TimeZone
		if (object instanceof TimeZone) {
			sb.append(StringUtil.escapeJS(((TimeZone) object).getID(), '"', charsetEncoder));
			return;
		}
		// Locale
		if (object instanceof Locale) {
			sb.append(StringUtil.escapeJS(LocaleFactory.toString((Locale) object), '"', charsetEncoder));
			return;
		}
		// Character
		if (object instanceof Character) {
			sb.append(StringUtil.escapeJS(String.valueOf(((Character) object).charValue()), '"', charsetEncoder));
			return;
		}
		// Number
		if (object instanceof Number) {
			sb.append(Caster.toString(((Number) object)));
			return;
		}
		// Boolean
		if (object instanceof Boolean) {
			sb.append(Caster.toString(((Boolean) object).booleanValue()));
			return;
		}
		// DateTime
		if (object instanceof DateTime) {
			_serializeDateTime((DateTime) object, sb);
			return;
		}
		// Date
		if (object instanceof Date) {
			_serializeDate((Date) object, sb);
			return;
		}
		if (object instanceof Node) {
			_serializeXML((Node) object, sb);
			return;
		}
		// Timespan
		if (object instanceof TimeSpan) {
			_serializeTimeSpan((TimeSpan) object, sb);
			return;
		}
		// File
		if (object instanceof File) {
			_serialize(pc, ((File) object).getAbsolutePath(), sb, queryFormat, preserveCase, done);
			return;
		}
		// String Converter
		if (object instanceof ScriptConvertable) {
			sb.append(((ScriptConvertable) object).serialize());
			return;
		}
		// byte[]
		if (object instanceof byte[]) {
			sb.append("\"" + Base64Coder.encode((byte[]) object) + "\"");
			return;
		}
		Object raw = LazyConverter.toRaw(object);
		if (done.contains(raw)) {
			sb.append("null");
			return;
		}
		done.add(raw);
		try {
			// Component
			if (object instanceof Component) {
				_serializeComponent(pc, (Component) object, sb, queryFormat, preserveCase, done);
				return;
			}
			// UDF
			if (object instanceof UDF) {
				_serializeUDF(pc, (UDF) object, sb, queryFormat, preserveCase, done);
				return;
			}
			// Struct
			if (object instanceof Struct) {
				_serializeStruct(pc, (Struct) object, sb, queryFormat, preserveCase, true, done);
				return;
			}
			// Map
			if (object instanceof Map) {
				_serializeMap(pc, (Map) object, sb, queryFormat, preserveCase, done);
				return;
			}
			// Array
			if (object instanceof Array) {
				_serializeArray(pc, (Array) object, sb, queryFormat, preserveCase, done);
				return;
			}
			// List
			if (object instanceof List) {
				_serializeList(pc, (List) object, sb, queryFormat, preserveCase, done);
				return;
			}
			// Query
			if (object instanceof Query) {
				_serializeQuery(pc, (Query) object, sb, queryFormat, preserveCase, done);
				return;
			}
			// Native Array
			if (Decision.isNativeArray(object)) {
				if (object instanceof char[]) _serialize(pc, new String((char[]) object), sb, queryFormat, preserveCase, done);
				else {
					_serializeArray(pc, ArrayUtil.toReferenceType(object, ArrayUtil.OBJECT_EMPTY), sb, queryFormat, preserveCase, done);
				}
				return;
			}
			// ObjectWrap
			if (object instanceof ObjectWrap) {
				try {
					_serialize(pc, ((ObjectWrap) object).getEmbededObject(), sb, queryFormat, preserveCase, done);
				}
				catch (PageException e) {
					if (object instanceof JavaObject) {
						_serializeClass(pc, ((JavaObject) object).getClazz(), null, sb, queryFormat, preserveCase, done);
					}
					else throw new ConverterException("can't serialize Object of type [ " + Caster.toClassName(object) + " ]");
				}
				return;
			}

			_serializeClass(pc, object.getClass(), object, sb, queryFormat, preserveCase, done);
		}
		finally {
			done.remove(raw);
		}
	}

	private void _serializeXML(Node node, Appendable sb) throws IOException {
		node = XMLCaster.toRawNode(node);
		sb.append(indent());
		sb.append(StringUtil.escapeJS(XMLCaster.toString(node, ""), '"', charsetEncoder));
	}

	private void _serializeTimeSpan(TimeSpan ts, Appendable sb) throws ConverterException, IOException {
		sb.append(indent());
		try {
			sb.append(Caster.toString(ts.castToDoubleValue()));
		}
		catch (PageException e) {// should never happen because TimeSpanImpl does not throw an exception
			throw new ConverterException(e.getMessage());
		}
	}

	/**
	 * serialize an Object to his literal Format
	 * 
	 * @param pc
	 * @param object Object to serialize
	 * @param queryFormat
	 * @return serialized wddx package
	 * @throws ConverterException
	 */
	public String serialize(PageContext pc, Object object, int queryFormat) throws ConverterException {
		return serialize(pc, object, queryFormat, null);
	}

	public String serialize(PageContext pc, Object object, int queryFormat, Boolean preserveCase) throws ConverterException {
		StringBuilder sb = new StringBuilder(256);
		try {
			_serialize(pc, object, sb, queryFormat, preserveCase, Collections.newSetFromMap(new IdentityHashMap<>()));
		}
		catch (IOException ioe) {
			ConverterException ce = new ConverterException("Failed to serialize JSON");
			ExceptionUtil.initCauseEL(ce, ioe);
			throw ce;
		}
		catch (OutOfMemoryError ome) {
			ConverterException ce = new ConverterException("Failed to serialize JSON: resulting string (current size: " + (sb.length() / 1024 / 1024)
					+ "mb) would exceed memory limits. Consider breaking down the data into smaller chunks or increasing the heap size.");
			ExceptionUtil.initCauseEL(ce, ome);
			throw ce;
		}
		return sb.toString();
	}

	public void serialize(PageContext pc, Object object, Resource target, Charset charset, int queryFormat, Boolean preserveCase) throws ConverterException {
		OutputStream os = null;
		Writer writer = null;
		try {
			os = target.getOutputStream();
			writer = IOUtil.getWriter(os, charset);
			_serialize(pc, object, writer, queryFormat, preserveCase, Collections.newSetFromMap(new IdentityHashMap<>()));
		}
		catch (IOException ioe) {
			ConverterException ce = new ConverterException("Failed to serialize JSON");
			ExceptionUtil.initCauseEL(ce, ioe);
			throw ce;
		}
		finally {
			IOUtil.closeEL(writer);
		}
	}

	public void serialize(PageContext pc, Object object, Writer writer, int queryFormat, Boolean preserveCase) throws ConverterException {
		try {
			_serialize(pc, object, writer, queryFormat, preserveCase, Collections.newSetFromMap(new IdentityHashMap<>()));
		}
		catch (IOException ioe) {
			ConverterException ce = new ConverterException("Failed to serialize JSON");
			ExceptionUtil.initCauseEL(ce, ioe);
			throw ce;
		}
	}

	@Override
	public void writeOut(PageContext pc, Object source, Writer writer) throws ConverterException, IOException {
		writer.write(serialize(pc, source, SerializationSettings.SERIALIZE_AS_ROW, null));
		writer.flush();
	}

	/**
	 * @return return current blockquote
	 */

	private void right() {
		level++;
	}

	private void left() {
		level--;
	}

	private String indent() {
		if (compact || level == 0) return "";

		switch (level) {
		case 1:
			return indent1;
		case 2:
			return indent2;
		case 3:
			return indent3;
		case 4:
			return indent4;
		case 5:
			return indent5;
		case 6:
			return indent6;
		case 7:
			return indent7;
		case 8:
			return indent8;
		case 9:
			return indent9;
		case 10:
			return indent10;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			sb.append(indent1);
		}
		return sb.toString();
	}

	public static String serialize(PageContext pc, Object o) throws ConverterException {
		JSONConverter converter = new JSONConverter(false, null);
		return converter.serialize(pc, o, SerializationSettings.SERIALIZE_AS_ROW, null);
	}

	public static int toQueryFormat(Object options, int defaultValue) {
		Boolean b = Caster.toBoolean(options, null);
		if (Boolean.TRUE.equals(b)) return SerializationSettings.SERIALIZE_AS_COLUMN;
		if (Boolean.FALSE.equals(b)) return SerializationSettings.SERIALIZE_AS_ROW;

		String str = Caster.toString(options, null);
		if ("row".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_ROW;
		if ("col".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_COLUMN;
		if ("column".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_COLUMN;
		if ("struct".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_STRUCT;

		return defaultValue;
	}

	private boolean getPreserveCase(PageContext pc, Boolean preserveCase, boolean forQuery) {
		if (preserveCase != null) {
			return preserveCase.booleanValue();
		}

		ApplicationContextSupport acs = pc == null ? null : (ApplicationContextSupport) pc.getApplicationContext();
		if (acs != null) {
			ISerializationSettings ss = acs.getSerializationSettings();
			return forQuery ? ss.getPreserveCaseForQueryColumn() : ss.getPreserveCaseForStructKey();
		}

		return true;
	}
}