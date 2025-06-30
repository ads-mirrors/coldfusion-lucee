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
package lucee.runtime.debug;

import java.util.HashSet;
import java.util.Set;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class DebuggerUtil {

	public Struct pointOutClosuresInPersistentScopes(PageContext pc) {
		Struct sct = new StructImpl();
		Set<Object> done = new HashSet<Object>();
		// Application Scope
		try {
			sct.set(KeyConstants._application, _pointOutClosuresInPersistentScopes(pc, pc.applicationScope(), done));
		}
		catch (PageException e) {
		}

		// Session Scope
		try {
			sct.set(KeyConstants._application, _pointOutClosuresInPersistentScopes(pc, pc.sessionScope(), done));
		}
		catch (PageException e) {
		}

		// Server Scope
		try {
			sct.set(KeyConstants._application, _pointOutClosuresInPersistentScopes(pc, pc.serverScope(), done));
		}
		catch (PageException e) {
		}

		return null;
	}

	private Struct _pointOutClosuresInPersistentScopes(PageContext pc, Struct sct, Set<Object> done) {

		return null;
	}

	public static boolean debugQueryUsage(PageContext pageContext, Query query) {
		if (((PageContextImpl) pageContext).hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE)) {
			query.enableShowQueryUsage();
			return true;
		}
		return false;
	}

	public static boolean hasCustomDebugEntry(PageContext pc) {

		String addr = pc.getHttpServletRequest().getRemoteAddr();
		lucee.runtime.config.DebugEntry debugEntry = ((ConfigPro) pc.getConfig()).getDebugEntry(addr, null);
		return debugEntry != null;
	}

	public static boolean hasDebugOptions(PageContext pc) {
		PageContextImpl pci = (PageContextImpl) pc;
		return pci.hasDebugOptions(ConfigPro.DEBUG_DATABASE) || pci.hasDebugOptions(ConfigPro.DEBUG_DUMP) || pci.hasDebugOptions(ConfigPro.DEBUG_EXCEPTION)
				|| pci.hasDebugOptions(ConfigPro.DEBUG_IMPLICIT_ACCESS) || pci.hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE) || pci.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)
				|| pci.hasDebugOptions(ConfigPro.DEBUG_THREAD) || pci.hasDebugOptions(ConfigPro.DEBUG_TIMER) || pci.hasDebugOptions(ConfigPro.DEBUG_TRACING);
	}

	public static void show(PageContext pc, boolean show) {
		ApplicationContextSupport acs = (ApplicationContextSupport) pc.getApplicationContext();
		if (acs != null) {
			acs.setShowDebug(show);
			acs.setShowDoc(show);
			acs.setShowMetric(show);
			acs.setShowTest(show);
		}
	}
}