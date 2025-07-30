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

package lucee.runtime.sql.old;

public final class TokenMgrError extends Error {

	protected static final String addEscapes(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c;
			switch (s.charAt(i)) {
			case 0: // '\0'
				break;

			case 8: // '\b'
				sb.append("\\b");
				break;

			case 9: // '\t'
				sb.append("\\t");
				break;

			case 10: // '\n'
				sb.append("\\n");
				break;

			case 12: // '\f'
				sb.append("\\f");
				break;

			case 13: // '\r'
				sb.append("\\r");
				break;

			case 34: // '"'
				sb.append("\\\"");
				break;

			case 39: // '\''
				sb.append("\\'");
				break;

			case 92: // '\\'
				sb.append("\\\\");
				break;

			default:
				if ((c = s.charAt(i)) < ' ' || c > '~') {
					String s1 = "0000" + Integer.toString(c, 16);
					sb.append("\\u" + s1.substring(s1.length() - 4, s1.length()));
				}
				else {
					sb.append(c);
				}
				break;
			}
		}

		return sb.toString();
	}

	private static final String LexicalError(boolean flag, int i, int j, int k, String s, char c) {
		return "Lexical error at line " + j + ", column " + k + ".  Encountered: " + (flag ? "<EOF> " : "\"" + addEscapes(String.valueOf(c)) + "\"" + " (" + (int) c + "), ")
				+ "after : \"" + addEscapes(s) + "\"";
	}

	@Override
	public String getMessage() {
		return super.getMessage();
	}

	public TokenMgrError() {
	}

	public TokenMgrError(String s, int i) {
		super(s);
		errorCode = i;
	}

	public TokenMgrError(boolean flag, int i, int j, int k, String s, char c, int l) {
		this(LexicalError(flag, i, j, k, s, c), l);
	}

	static final int LEXICAL_ERROR = 0;
	static final int STATIC_LEXER_ERROR = 1;
	static final int INVALID_LEXICAL_STATE = 2;
	static final int LOOP_DETECTED = 3;
	int errorCode;
}