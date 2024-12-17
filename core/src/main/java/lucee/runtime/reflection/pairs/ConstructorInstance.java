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
package lucee.runtime.reflection.pairs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiFunction;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.Constructor;
import lucee.transformer.dynamic.meta.FunctionMember;
import lucee.transformer.dynamic.meta.LegacyConstuctor;

/**
 * class holds a Constructor and the parameter to call it
 */
public final class ConstructorInstance {

	private Class clazz;
	private Object[] args;
	private Pair<FunctionMember, Object> result;
	private boolean convertComparsion;

	/**
	 * constructor of the class
	 * 
	 * @param constructor
	 * @param args
	 */
	public ConstructorInstance(Class clazz, Object[] args, boolean convertComparsion) {
		this.clazz = clazz;
		this.args = args;
		this.convertComparsion = convertComparsion;
	}

	public Object invoke() throws PageException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException, IOException {

		try {
			return ((BiFunction<Object, Object, Object>) getResult().getValue()).apply(null, args);
		}
		catch (IncompatibleClassChangeError | ClassFormatError | IllegalStateException e) {
			if (!Clazz.allowReflection()) throw e;

			// fallback to reflection
			boolean failed = false;
			try {
				DynamicInvoker di = DynamicInvoker.getExistingInstance();
				lucee.transformer.dynamic.meta.Constructor constr = Clazz.getConstructorMatch(di.getClazz(clazz, true), args, true, convertComparsion);
				return ((LegacyConstuctor) constr).getConstructor().newInstance(args);
			}
			catch (IncompatibleClassChangeError | ClassFormatError | IllegalStateException ex) {
				failed = true;
				throw e;
			}
			finally {
				// we only log the exception from direct invocation, in case reflection does not fail
				if (!failed) LogUtil.log(Log.LEVEL_DEBUG, "direct", ExceptionUtil.getStacktrace(e, true));
			}
		}
	}

	/**
	 * @return Returns the args.
	 */
	public Object[] getArgs() {
		return args;
	}

	public Constructor getConstructor() throws PageException {
		return (Constructor) getResult().getName();
	}

	public Constructor getConstructor(Constructor defaultValue) {
		try {
			return (Constructor) getResult().getName();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	private Pair<FunctionMember, Object> getResult() throws PageException {
		if (result == null) {
			try {
				result = DynamicInvoker.getExistingInstance().getInstance(clazz, (Key) null, args, true, convertComparsion);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		return result;
	}
}