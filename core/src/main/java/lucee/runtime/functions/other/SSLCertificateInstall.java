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
package lucee.runtime.functions.other;

import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.commons.io.res.Resource;
import lucee.runtime.net.http.CertificateInstaller;
import lucee.runtime.op.Caster;

public final class SSLCertificateInstall implements Function {

	private static final long serialVersionUID = -831759073098524176L;

	public static String call(PageContext pc, String host) throws PageException {
		return call(pc, host, 443);
	}

	public static String call(PageContext pc, String host, Number port) throws PageException {
		return call(pc, host, port, null, null);
	}

	public static String call(PageContext pc, String host, Number port, Object cacerts) throws PageException {
		return call(pc, host, port, cacerts, null);
	}

	public static String call(PageContext pc, String host, Number port, Object cacerts, String password) throws PageException {
		CertificateInstaller installer;
		Resource _cacerts;
		try {
			if (cacerts == null) {
				if (!SystemUtil.getSystemPropOrEnvVar("lucee.use.lucee.SSL.TrustStore", "").equalsIgnoreCase("true"))
					throw new ApplicationException("Using JVM cacerts, set lucee.use.lucee.SSL.TrustStore=true to enable"); // LDEV-917
				_cacerts = pc.getConfig().getSecurityDirectory();
			} else {
				_cacerts = Caster.toResource(pc, cacerts, true);
			}
			if (password == null) installer = new CertificateInstaller(_cacerts, host, Caster.toIntValue(port)); // use default password changeit
			else installer = new CertificateInstaller(_cacerts, host, Caster.toIntValue(port), password.toCharArray());
			installer.installAll(true);
		} catch (Exception e){
			throw Caster.toPageException(e);
		}
		return "";
	}

}