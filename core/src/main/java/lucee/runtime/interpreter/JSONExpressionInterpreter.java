/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.interpreter;

import lucee.commons.lang.NumberUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.literal.LStringBuffer;

public final class JSONExpressionInterpreter extends CFMLExpressionInterpreter {

	public static final int FORMAT_JSON = 1;
	public static final int FORMAT_JSON5 = 2;

	public JSONExpressionInterpreter() {
		this(false, FORMAT_JSON5);
	}

	public JSONExpressionInterpreter(boolean strict) {// strict is set to true, it should not be compatible with CFMLExpressionInterpreter
		this(strict, FORMAT_JSON5);
	}

	public JSONExpressionInterpreter(boolean strict, int format) {// strict is set to true, it should not be compatible with CFMLExpressionInterpreter
		this.allowNullConstant = true;
		this.allowComments = format == FORMAT_JSON5;
		this.allowAllowUnquotedNames = format == FORMAT_JSON5;
	}

	@Override
	protected Ref string() throws PageException {

		// Init Parameter
		char quoter = cfml.getCurrentLower();
		LStringBuffer str = new LStringBuffer();

		while (cfml.hasNext()) {
			cfml.next();
			// check sharp
			if (cfml.isCurrent('\\')) {
				if (cfml.isNext(quoter)) {
					cfml.next();
					str.append(quoter);
				}
				else if (cfml.isNext('\\')) {
					cfml.next();
					str.append('\\');
				}
				else if (cfml.isNext('"')) {
					cfml.next();
					str.append('"');
				}
				else if (cfml.isNext('\'')) {
					cfml.next();
					str.append('\'');
				}
				else if (cfml.isNext('t')) {
					cfml.next();
					str.append('\t');
				}
				else if (cfml.isNext('n')) {
					cfml.next();
					str.append('\n');
				}
				else if (cfml.isNext('b')) {
					cfml.next();
					str.append('\b');
				}
				else if (cfml.isNext('f')) {
					cfml.next();
					str.append('\f');
				}
				else if (cfml.isNext('r')) {
					cfml.next();
					str.append('\r');
				}
				else if (cfml.isNext('u')) {
					cfml.next();
					StringBuilder sb = new StringBuilder();
					int i = 0;

					for (; i < 4 && cfml.hasNext(); i++) {
						cfml.next();
						sb.append(cfml.getCurrent());
					}
					if (i < 4) {
						str.append("\\u");
						str.append(sb.toString());
					}
					else {
						int asc = NumberUtil.hexToInt(sb.toString(), -1);
						if (asc != -1) str.append((char) asc);
						else {
							str.append("\\u");
							str.append(sb.toString());
						}
					}

				}
				else if (cfml.isNext('/')) {
					cfml.next();
					str.append('/');
				}
				else {
					str.append('\\');
				}
			}
			else if (cfml.isCurrent(quoter)) {
				break;
			}
			// all other character
			else {
				str.append(cfml.getCurrent());
			}
		}
		if (!cfml.forwardIfCurrent(quoter)) throw new InterpreterException("Invalid String Literal Syntax Closing [" + quoter + "] not found");
		comments();
		mode = STATIC;
		/*
		 * Ref value=null; if(value!=null) { if(str.isEmpty()) return value; return new
		 * Concat(pc,value,str); }
		 */
		return str;
	}

	public static int toFormat(String format) throws ApplicationException {
		int f = toFormat(format, 0);
		if (f != 0) return f;

		if (!StringUtil.isEmpty(format, true)) throw new ApplicationException("invalid format defintion [" + format + "], valid format names are [json, json5].");
		throw new ApplicationException("invalid format defintion, an empty string or null is not allowed.");
	}

	public static int toFormat(String format, int defaultValue) {
		if (!StringUtil.isEmpty(format, true)) {
			format = format.trim().toLowerCase();
			if ("json".equals(format)) return FORMAT_JSON;
			if ("json5".equals(format)) return FORMAT_JSON5;
		}
		return defaultValue;
	}

}