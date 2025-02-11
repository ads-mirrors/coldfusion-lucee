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
/**
 * Implements the CFML Function gettempdirectory
 */
package lucee.runtime.functions.system;

import java.io.IOException;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.op.Decision;
import lucee.runtime.op.Caster;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.commons.io.SystemUtil;

public final class GetTempDirectory implements Function {

	private static final long serialVersionUID = -166719664831864953L;
	private static final int MAX_RETRY_TEMP = 3;

	public static String call(PageContext pc) {
		String fs = System.getProperty("file.separator");
		String path = pc.getConfig().getTempDirectory().getAbsolutePath();

		if (path.lastIndexOf(fs) != path.length() - 1) path += fs;
		return path;
	}

	public static String call(PageContext pc, String unique) throws PageException{
		boolean isUnique = Decision.isCastableToBoolean(unique);
		if (isUnique && !Caster.toBoolean(unique, false)) return call(pc);

		if (isUnique) unique = "";
		else if (!StringUtil.isEmpty(unique)) unique = unique.trim() + "_";
		else unique = "";

		String fs = System.getProperty("file.separator");
		Resource path = pc.getConfig().getTempDirectory();

		int max = MAX_RETRY_TEMP;
		while (max-- > 0) {
			Resource dir = path.getRealResource(fs + unique + Long.toString(System.currentTimeMillis(), Character.MAX_RADIX) + "_" + CreateUniqueId.invoke() );
			synchronized (SystemUtil.createToken("", dir.getAbsolutePath())) {
				try {
					if (dir.exists()) continue;
					dir.createDirectory(false);
					String uniqueDir = dir.getCanonicalPath();
					if (uniqueDir.lastIndexOf(fs) != uniqueDir.length() - 1) uniqueDir += fs;
					return uniqueDir;
				}
				catch (IOException e) {
				}
			}
		}
		throw new ExpressionException("Unable to create unique temporary directory");
	}

}