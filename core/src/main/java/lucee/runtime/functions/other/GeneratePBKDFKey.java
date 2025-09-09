/**
 *
 * Copyright (c) 2015, Lucee Association Switzerland
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
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.coder.Base64Coder;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class GeneratePBKDFKey extends BIF {

	private static final long serialVersionUID = -2558116913822203235L;

	public static String call(PageContext pc, String algorithm, String passPhrase, String salt) throws PageException {
		return call(pc, algorithm, passPhrase, salt, 4096, 128);
	}

	public static String call(PageContext pc, String algorithm, String passPhrase, String salt, Number iterations) throws PageException {
		return call(pc, algorithm, passPhrase, salt, iterations, 128);
	}

	public static String call(PageContext pc, String algorithm, String passPhrase, String salt, Number iterations, Number keySize) throws PageException {
		// algo
		if (StringUtil.isEmpty(algorithm)) throw new FunctionException(pc, "GeneratePBKDFKey", 1, "algorithm", "Argument [algorithm] is empty.");
		algorithm = algorithm.trim();
		if (!StringUtil.startsWithIgnoreCase(algorithm, "PBK")) throw new FunctionException(pc, "GeneratePBKDFKey", 1, "algorithm",
				"The alogrithm [" + algorithm + "] is not supported. Supported algorithms are [ " + getSupportedAlgorithmsAsList() + " ]");

		// TODO add provider to support addional keys by addin a provider that is supporting it
		SecretKeyFactory key = null;
		try {
			key = SecretKeyFactory.getInstance(algorithm);
		}
		catch (NoSuchAlgorithmException e) {
			if (!algorithm.equalsIgnoreCase("PBKDF2WithHmacSHA1")) throw new FunctionException(pc, "GeneratePBKDFKey", 1, "algorithm",
					"The alogrithm [" + algorithm + "] is not supported. Supported algorithms are [ " + getSupportedAlgorithmsAsList() + " ]");
			throw Caster.toPageException(e);
		}

		try {
			PBEKeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), salt.getBytes(), Caster.toIntValue(iterations), Caster.toIntValue(keySize));
			return Base64Coder.encode(key.generateSecret(spec).getEncoded());
		}
		catch (InvalidKeySpecException ikse) {
			throw Caster.toPageException(ikse);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 5)
			return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toDoubleValue(args[3]), Caster.toDoubleValue(args[4]));
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toDoubleValue(args[3]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));

		throw new FunctionException(pc, "GeneratePBKDFKey", 3, 5, args.length);
	}

	private static String getSupportedAlgorithmsAsList() {
		return ListUtil.toList(getSupportedAlgorithms(), ", ");
	}

	public static ArrayList<String> getSupportedAlgorithms() {
		ArrayList<String> algorithms = new ArrayList<>();
		for (Provider provider: Security.getProviders()) {
			for (Provider.Service service: provider.getServices()) {
				String algorithm = service.getAlgorithm();
				if (algorithm.startsWith("PBK")) algorithms.add(algorithm);
			}
		}
		return algorithms;
	}
}
