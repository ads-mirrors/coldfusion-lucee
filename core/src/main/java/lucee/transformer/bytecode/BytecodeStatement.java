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
package lucee.transformer.bytecode;

import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.statement.Statement;

/**
 * A single Statement
 */
public interface BytecodeStatement extends Statement {

	/**
	 * @return return the label where the finally block of this tags starts, IF there is a finally
	 *         block, otherwise return null;
	 */
	public FlowControlFinal getFlowControlFinal();
}