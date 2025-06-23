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
package lucee.runtime.crypt;

import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.coder.Coder;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

/**
 * Cryptor
 */
public class Cryptor {

	public final static String DEFAULT_CHARSET = "UTF-8";
	public final static String DEFAULT_ALGORITHM;
	public final static String DEFAULT_ENCODING = "UU";
	public final static int DEFAULT_ITERATIONS = 1000; // minimum recommended per NIST

	private final static SecureRandom secureRandom = new SecureRandom();

	static {
		DEFAULT_ALGORITHM = SystemUtil.getSystemPropOrEnvVar("lucee.encryption.algorithm", "cfmx_compat");
	}

	/**
	 * @param input - the clear-text input to be encrypted, or the encrypted input to be decrypted
	 * @param key - the encryption key
	 * @param algorithm - algorithm in JCE scheme
	 * @param ivOrSalt - Initialization Vector for algorithms with Feedback Mode that is not ECB, or
	 *            Salt for Password Based Encryption algorithms
	 * @param iterations - number of Iterations for Password Based Encryption algorithms (recommended
	 *            minimum value is 1000)
	 * @param doDecrypt - the Operation Type, pass false for Encrypt or true for Decrypt
	 * @return
	 * @throws PageException
	 */
	static byte[] crypt(byte[] input, String key, String algorithm, byte[] ivOrSalt, int iterations, boolean doDecrypt, boolean precise) throws PageException {

		try {
			return _crypt(input, key, algorithm, ivOrSalt, iterations, doDecrypt, precise);
		}
		// this is an ugly patch but it looks lime that ACF simply double to short keys
		catch (PageException pe) {
			String msg = pe.getMessage();
			if (msg != null && key.length() == 4 && msg.indexOf(" 40 ") != -1 && msg.indexOf(" 1024 ") != -1) {
				return _crypt(input, key + key, algorithm, ivOrSalt, iterations, doDecrypt, precise);
			}
			if (msg != null && key.length() > 4 && key.length() % 4 == 0 && msg.indexOf("Illegal key size") != -1) {
				return crypt(input, key.substring(0, key.length() - 4), algorithm, ivOrSalt, iterations, doDecrypt, precise);
			}
			throw pe;
		}
	}

	private static byte[] _crypt(byte[] input, String key, String algorithm, byte[] ivOrSalt, int iterations, boolean doDecrypt, boolean precise) throws PageException {

		byte[] result = null;
		Key secretKey = null;
		AlgorithmParameterSpec params = null;

		String algo = algorithm;
		boolean isFBM = false, isPBE = StringUtil.startsWithIgnoreCase(algo, "PBE");
		int ivsLen = 0, algoDelimPos = algorithm.indexOf('/');

		if (algoDelimPos > -1) {

			algo = algorithm.substring(0, algoDelimPos);
			isFBM = !StringUtil.startsWithIgnoreCase(algorithm.substring(algoDelimPos + 1), "ECB");
		}

		try {

			Cipher cipher = Cipher.getInstance(algorithm);

			if (ivOrSalt == null) {

				if (isPBE || isFBM) {

					ivsLen = cipher.getBlockSize();
					ivOrSalt = new byte[ivsLen];

					if (doDecrypt) System.arraycopy(input, 0, ivOrSalt, 0, ivsLen);
					else secureRandom.nextBytes(ivOrSalt);
				}
			}

			if (isPBE) {

				secretKey = SecretKeyFactory.getInstance(algorithm).generateSecret(new PBEKeySpec(key.toCharArray()));
				params = new PBEParameterSpec(ivOrSalt, iterations > 0 ? iterations : DEFAULT_ITERATIONS); // set Salt and Iterations for PasswordBasedEncryption
			}
			else {
				byte[] keyBytes;

				if ("AES".equalsIgnoreCase(algo) || "DES".equalsIgnoreCase(algo) || "DESEDE".equalsIgnoreCase(algo) || "BLOWFISH".equalsIgnoreCase(algo)) {
					// Try to determine if this is a Base64-encoded key or a raw string
					try {
						// First, try to decode as Base64
						byte[] decodedKey = Coder.decode(Coder.ENCODING_BASE64, key, precise);

						// Check if the decoded length makes sense for the algorithm
						boolean isValidLength = false;
						if ("DES".equalsIgnoreCase(algo) && decodedKey.length == 8) isValidLength = true;
						if ("DESEDE".equalsIgnoreCase(algo) && (decodedKey.length == 16 || decodedKey.length == 24)) isValidLength = true;
						if ("AES".equalsIgnoreCase(algo) && (decodedKey.length == 16 || decodedKey.length == 24 || decodedKey.length == 32)) isValidLength = true;
						if ("BLOWFISH".equalsIgnoreCase(algo) && decodedKey.length >= 4 && decodedKey.length <= 56) isValidLength = true;

						if (isValidLength) {
							// This looks like a properly generated Base64 key
							keyBytes = decodedKey;
						}
						else {
							// Base64 decoded but wrong length - treat as raw UTF-8
							keyBytes = key.getBytes(DEFAULT_CHARSET);

							// Apply padding/truncation for raw keys
							if ("AES".equalsIgnoreCase(algo)) {
								keyBytes = adjustKeyLength(keyBytes, 16); // Pad to 16 bytes
							}
							else if ("DES".equalsIgnoreCase(algo)) {
								keyBytes = adjustKeyLength(keyBytes, 8); // Pad to 8 bytes
							}
							else if ("DESEDE".equalsIgnoreCase(algo)) {
								keyBytes = adjustKeyLength(keyBytes, 24); // Pad to 24 bytes
							}
						}
					}
					catch (Exception e) {
						// Base64 decode failed - definitely a raw string key
						keyBytes = key.getBytes(DEFAULT_CHARSET);

						// Apply padding/truncation for raw keys
						if ("AES".equalsIgnoreCase(algo)) {
							keyBytes = adjustKeyLength(keyBytes, 16);
						}
						else if ("DES".equalsIgnoreCase(algo)) {
							keyBytes = adjustKeyLength(keyBytes, 8);
						}
						else if ("DESEDE".equalsIgnoreCase(algo)) {
							keyBytes = adjustKeyLength(keyBytes, 24);
						}
					}
				}
				else {
					// For other algorithms, keep the original Base64 decoding behavior
					keyBytes = Coder.decode(Coder.ENCODING_BASE64, key, precise);
				}

				secretKey = new SecretKeySpec(keyBytes, algo);
				if (isFBM) params = new IvParameterSpec(ivOrSalt);
			}

			if (doDecrypt) {

				cipher.init(Cipher.DECRYPT_MODE, secretKey, params);

				result = cipher.doFinal(input, ivsLen, input.length - ivsLen);
			}
			else {

				cipher.init(Cipher.ENCRYPT_MODE, secretKey, params);

				result = new byte[ivsLen + cipher.getOutputSize(input.length)];

				if (ivsLen > 0) System.arraycopy(ivOrSalt, 0, result, 0, ivsLen);

				cipher.doFinal(input, 0, input.length, result, ivsLen);
			}

			return result;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	private static byte[] adjustKeyLength(byte[] originalKey, int targetLength) {
		if (originalKey.length == targetLength) {
			return originalKey;
		}
		else if (originalKey.length < targetLength) {
			// Pad with zeros
			byte[] paddedKey = new byte[targetLength];
			System.arraycopy(originalKey, 0, paddedKey, 0, originalKey.length);
			return paddedKey;
		}
		else {
			// Truncate
			byte[] truncatedKey = new byte[targetLength];
			System.arraycopy(originalKey, 0, truncatedKey, 0, targetLength);
			return truncatedKey;
		}
	}

	/**
	 * an encrypt method that takes a byte-array for input and returns an encrypted byte-array
	 */
	public static byte[] encrypt(byte[] input, String key, String algorithm, byte[] ivOrSalt, int iterations, boolean precise) throws PageException {
		return crypt(input, key, algorithm, ivOrSalt, iterations, false, precise);
	}

	/**
	 * an encrypt method that takes a clear-text String for input and returns an encrypted, encoded,
	 * String
	 */
	public static String encrypt(String input, String key, String algorithm, byte[] ivOrSalt, int iterations, String encoding, String charset, boolean precise)
			throws PageException {

		try {

			if (charset == null) charset = DEFAULT_CHARSET;
			if (encoding == null) encoding = DEFAULT_ENCODING;

			byte[] baInput = input.getBytes(charset);
			byte[] encrypted = encrypt(baInput, key, algorithm, ivOrSalt, iterations, precise);

			return Coder.encode(encoding, encrypted);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	/**
	 * a decrypt method that takes an encrypted byte-array for input and returns an unencrypted
	 * byte-array
	 */
	public static byte[] decrypt(byte[] input, String key, String algorithm, byte[] ivOrSalt, int iterations, boolean precise) throws PageException {
		return crypt(input, key, algorithm, ivOrSalt, iterations, true, precise);
	}

	/**
	 * a decrypt method that takes an encrypted, encoded, String for input and returns a clear-text
	 * String
	 */
	public static String decrypt(String input, String key, String algorithm, byte[] ivOrSalt, int iterations, String encoding, String charset, boolean precise)
			throws PageException {

		try {

			if (charset == null) charset = DEFAULT_CHARSET;
			if (encoding == null) encoding = DEFAULT_ENCODING;

			byte[] baInput = Coder.decode(encoding, input, precise);
			byte[] decrypted = decrypt(baInput, key, algorithm, ivOrSalt, iterations, precise);

			return new String(decrypted, charset);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}
}