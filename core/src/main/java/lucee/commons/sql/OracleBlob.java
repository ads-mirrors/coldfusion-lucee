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
package lucee.commons.sql;

import java.sql.Blob;
import java.sql.Connection;

import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.transformer.dynamic.meta.Method;

public class OracleBlob {

	private static Integer duration;
	private static Integer mode;
	private static Method createTemporary;
	private static Method open;
	private static Method setBytes;

	public static Blob createBlob(Connection conn, byte[] barr, Blob defaultValue) {
		try {
			Class clazz = ClassUtil.loadClass("oracle.sql.BLOB");

			// BLOB.DURATION_SESSION
			if (duration == null) duration = Caster.toInteger(clazz.getField("DURATION_SESSION").getInt(null));
			// BLOB.MODE_READWRITE
			if (mode == null) mode = Caster.toInteger(clazz.getField("MODE_READWRITE").getInt(null));

			// BLOB blob = BLOB.createTemporary(conn, false, BLOB.DURATION_SESSION);
			if (createTemporary == null || createTemporary.getDeclaringClass() != clazz) {
				createTemporary = Reflector.getMethod(clazz, "createTemporary", new Class[] { Connection.class, boolean.class, int.class }, true);
			}
			Object blob = createTemporary.invoke(null, new Object[] { conn, Boolean.FALSE, duration });

			// blob.open(BLOB.MODE_READWRITE);
			if (open == null || open.getDeclaringClass() != clazz) {
				open = Reflector.getMethod(clazz, "open", new Class[] { int.class }, true);
			}
			open.invoke(blob, new Object[] { mode });

			// blob.setBytes(1,barr);
			if (setBytes == null || setBytes.getDeclaringClass() != clazz) {
				setBytes = Reflector.getMethod(clazz, "setBytes", new Class[] { long.class, byte[].class }, true);
			}
			setBytes.invoke(blob, new Object[] { Long.valueOf(1), barr });

			return (Blob) blob;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			// print.printST(t);
		}
		return defaultValue;
	}

}