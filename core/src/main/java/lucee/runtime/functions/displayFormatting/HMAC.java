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
package lucee.runtime.functions.displayFormatting;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lucee.commons.digest.MD5;
import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.util.ListUtil;

public final class HMAC implements Function {

	private static final long serialVersionUID = -1999122154087043893L;

	public static String call(PageContext pc, Object oMessage, Object oKey) throws PageException {
		return call(pc, oMessage, oKey, null, null);
	}

	public static String call(PageContext pc, Object oMessage, Object oKey, String algorithm) throws PageException {
		return call(pc, oMessage, oKey, algorithm, null);
	}

	public static String call(PageContext pc, Object oMessage, Object oKey, String algorithm, String charset) throws PageException {
		// charset
		Charset cs;
		if (StringUtil.isEmpty(charset, true)) cs = pc.getWebCharset();
		else cs = CharsetUtil.toCharset(charset);

		// message
		byte[] msg = toBinary(oMessage, cs);

		// message
		byte[] key = toBinary(oKey, cs);

		// algorithm
		if (StringUtil.isEmpty(algorithm, true)) algorithm = "HmacMD5";
		
		try {
			SecretKey sk = new SecretKeySpec(key, algorithm);
			Mac mac = Mac.getInstance(algorithm);
			mac.init(sk);
			mac.reset();
			mac.update(msg);
			msg = mac.doFinal();
			return MD5.stringify(msg).toUpperCase();
		} catch (NoSuchAlgorithmException nsae) {
			FunctionException fe = new FunctionException(pc, "HMAC", 1, "algorithm", "The alogrithm [" + algorithm + "] is not supported. Supported algorithms are [ " + getSupportedHMacAlgorithms() + " ]");
			ExceptionUtil.initCauseEL(fe, nsae);
			throw fe;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static byte[] toBinary(Object obj, Charset cs) throws PageException {
		if (Decision.isBinary(obj)) {
			return Caster.toBinary(obj);
		}
		return Caster.toString(obj).getBytes(cs);
	}

	private static String getSupportedHMacAlgorithms() {
		ArrayList<String> algorithms = new ArrayList<>();
		for (Provider provider : Security.getProviders()) {
			for (Provider.Service service : provider.getServices()) {
				if ("Mac".equalsIgnoreCase(service.getType())) {
					if (!service.getAlgorithm().contains("PBE")){
						algorithms.add(service.getAlgorithm());
					}
				}
			}
		}
		return ListUtil.toList(algorithms, ", ");
	}

}