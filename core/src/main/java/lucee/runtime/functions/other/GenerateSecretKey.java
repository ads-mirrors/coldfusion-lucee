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

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;

import javax.crypto.KeyGenerator;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.coder.Coder;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

/**
 * Generates a Secret Key
 */
public final class GenerateSecretKey implements Function {

	public static String call(PageContext pc, String algorithm) throws PageException {
		return call(pc, algorithm, 0);
	}

	public static String call(PageContext pc, String algorithm, Number keySize) throws PageException {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm.toUpperCase());
			int kz = Caster.toIntValue(keySize);
			if (kz > 0) keyGenerator.init(Caster.toIntValue(keySize));
			return Coder.encode(Coder.ENCODING_BASE64, keyGenerator.generateKey().getEncoded());
		} catch (NoSuchAlgorithmException nsae) {
			FunctionException fe = new FunctionException(pc, "GenerateSecretKey", 1, "algorithm", "The alogrithm [" + algorithm + "] is not supported. Supported algorithms are [ " + getAvailableSecretKeyAlgorithms() + " ]");
			ExceptionUtil.initCauseEL(fe, nsae);
			throw fe;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static String getAvailableSecretKeyAlgorithms() {
		ArrayList<String> algorithms = new ArrayList<>();
		for (Provider provider : Security.getProviders()) {
			for (Provider.Service service : provider.getServices()) {
				if ("KeyGenerator".equalsIgnoreCase(service.getType())) {
					if (!service.getAlgorithm().toLowerCase().contains("tls")){
						algorithms.add(service.getAlgorithm()); // tls requires extra setup
					}
				}
			}
		}
		return ListUtil.toList(algorithms, ", ");
	}

}