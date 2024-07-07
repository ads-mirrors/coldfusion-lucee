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

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiFunction;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.Constructor;
import lucee.transformer.dynamic.meta.LegacyConstuctor;
import lucee.transformer.dynamic.meta.dynamic.ClazzDynamic;

/**
 * class holds a Constructor and the parameter to call it
 */
public final class ConstructorInstance {

	private Class clazz;
	private Object[] args;
	private Constructor fm;
	private Object instance;

	private boolean convertComparsion;

	/**
	 * constructor of the class
	 * 
	 * @param clazz
	 * @param args
	 */
	public ConstructorInstance(Class clazz, Object[] args, boolean convertComparsion) {
		this.clazz = clazz;
		this.args = Reflector.cleanArgs(args);
		this.convertComparsion = convertComparsion;
	}

	public Object invoke()
			throws PageException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		try {
			return ((BiFunction<Object, Object, Object>) getInstance()).apply(null, args);
		}
		catch (IncompatibleClassChangeError | ClassFormatError | IllegalStateException e) {
			if (!Clazz.allowReflection()) throw e;

			// fallback to reflection
			boolean failed = false;
			try {
				DynamicInvoker di = DynamicInvoker.getExistingInstance();
				lucee.transformer.dynamic.meta.Constructor constr = di.getClazz(clazz, true).getConstructor(args, true, convertComparsion);
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

	public Constructor getConstructor(Constructor defaultValue) {
		try {
			return getConstructor();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	private Constructor getConstructor() throws PageException {
		getInstance();
		return fm;
	}

	private Object getInstance() throws PageException {
		if (instance == null) {
			try {
				DynamicInvoker di = DynamicInvoker.getExistingInstance();
				ClazzDynamic clazzz = di.toClazzDynamic(clazz);
				if (fm == null) {
					fm = clazzz.getConstructor(args, true, convertComparsion);
				}
				instance = di.getInstance(clazzz, fm, args);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw Caster.toPageException(t);
			}
		}
		return instance;
	}

	public lucee.transformer.dynamic.meta.FunctionMember getConstructor(ClazzDynamic clazzz, Object[] arguments, boolean convertComparsion) throws NoSuchMethodException {
		return clazzz.getConstructor(arguments, true, convertComparsion);
	}
}