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
package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public final class Sleep {

	public static String call(PageContext pc, Number nduration) throws PageException {
		long dur = Caster.toLongValue(nduration);
		if (dur >= 0L) {
			try {
				Thread.sleep(dur);
			}
			catch (InterruptedException e) {
				throw Caster.toPageException(e);
			}
		}
		else throw new FunctionException(pc, "sleep", 1, "duration", "attribute interval must be greater or equal to 0, now [" + (dur) + "]");
		return null;

	}
}