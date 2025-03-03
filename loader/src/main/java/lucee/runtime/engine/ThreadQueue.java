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
package lucee.runtime.engine;

import java.io.IOException;

import lucee.runtime.PageContext;

public interface ThreadQueue {

	public short MODE_UNDEFINED = 0;

	/**
	 * Thread queue is disabled, so all thread pass through
	 */
	public short MODE_DISABLED = 1;

	/**
	 * thread queue is enabled
	 */
	public short MODE_ENABLED = 2;

	/**
	 * thread queue is in blocking mode, so no thread is passing until that mode is left
	 */
	public short MODE_BLOCKING = 4;

	public void enter(PageContext pc) throws IOException;

	public void exit(PageContext pc);

	public void clear();

	public int size();

	/**
	 * set the mode for the queue
	 * 
	 * @return previous mode value
	 */
	public short setMode(short mode);

	/**
	 * 
	 * @return returns the current mode of the queue
	 */
	public short getMode();
}