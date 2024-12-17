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

import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.FunctionMember;
import lucee.transformer.dynamic.meta.LegacyMethod;
import lucee.transformer.dynamic.meta.Method;

/**
 * class holds a Method and the parameter to call it
 */
public final class MethodInstance {

	private Class clazz;
	private Key methodName;
	private Object[] args;
	private Pair<FunctionMember, Object> result;
	private boolean convertComparsion;
	private boolean nameCaseSensitive;
	private FunctionMember fm;

	public MethodInstance(Class clazz, FunctionMember fm, Object[] args, boolean nameCaseSensitive, boolean convertComparsion) {
		this.clazz = clazz;
		this.fm = fm;
		this.args = args;
		this.convertComparsion = convertComparsion;
		this.nameCaseSensitive = nameCaseSensitive;
	}

	public MethodInstance(Class clazz, Key methodName, Object[] args, boolean nameCaseSensitive, boolean convertComparsion) {
		this.clazz = clazz;
		this.methodName = methodName;
		this.args = args;
		this.convertComparsion = convertComparsion;
		this.nameCaseSensitive = nameCaseSensitive;
	}

	public Object invoke(Object o) throws PageException {
		// if (Clazz.allowReflection()) print.e(Clazz.allowReflection());
		try {
			return ((BiFunction<Object, Object, Object>) getResult().getValue()).apply(o, args);
		}
		catch (IncompatibleClassChangeError | ClassFormatError | ClassCastException e) { // java.lang.ClassCastException
			if (!Clazz.allowReflection()) throw e;
			LogUtil.log("dynamic", e);
			DynamicInvoker di = DynamicInvoker.getExistingInstance();
			try {
				lucee.transformer.dynamic.meta.Method method = Clazz.getMethodMatch(di.getClazz(clazz, true), methodName, args, nameCaseSensitive, true, true);
				return ((LegacyMethod) method).getMethod().invoke(o, args);
			}
			catch (Exception e1) {
				if (e1 instanceof InvocationTargetException) {
					Throwable t = ((InvocationTargetException) e1).getTargetException();
					ExceptionUtil.initCauseEL(e, t);
					throw e;
				}
				ExceptionUtil.initCauseEL(e, e1);
				throw e;
			}
		}
	}

	public static Object invoke(Object obj, Key methodName, Object[] args, boolean nameCaseSensitive, boolean convertComparsion) throws PageException {
		// if (Clazz.allowReflection()) print.e(Clazz.allowReflection());
		try {
			return ((BiFunction<Object, Object, Object>) DynamicInvoker.getExistingInstance().getInstance(obj.getClass(), methodName, args, nameCaseSensitive, convertComparsion)
					.getValue()).apply(obj, args);
		}
		catch (IncompatibleClassChangeError | ClassFormatError | ClassCastException e) { // java.lang.ClassCastException
			if (!Clazz.allowReflection()) throw e;
			LogUtil.log("dynamic", e);
			DynamicInvoker di = DynamicInvoker.getExistingInstance();
			try {
				lucee.transformer.dynamic.meta.Method method = Clazz.getMethodMatch(di.getClazz(obj.getClass(), true), methodName, args, nameCaseSensitive, true, true);
				return ((LegacyMethod) method).getMethod().invoke(obj, args);
			}
			catch (Exception e1) {
				if (e1 instanceof InvocationTargetException) {
					Throwable t = ((InvocationTargetException) e1).getTargetException();
					ExceptionUtil.initCauseEL(e, t);
					throw e;
				}
				ExceptionUtil.initCauseEL(e, e1);
				throw e;
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * @return Returns the args.
	 */
	public Object[] getArgs() {
		return args;
	}

	public Method getMethod() throws PageException {
		return (Method) getResult().getName();
	}

	public Method getMethod(Method defaultValue) {
		try {
			return (Method) getResult().getName();
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	public boolean hasMethod() {
		if (args.length == 0 && "toString".equals(methodName.getString())) {
			return true;
		}
		else if (args.length == 1 && "equals".equals(methodName.getString())) {
			return true;
		}

		try {
			FunctionMember fm = getResult().getName();
			return fm != null;
		}
		catch (PageException e) {
			return false;
		}
	}

	private Pair<FunctionMember, Object> getResult() throws PageException {
		if (result == null) {
			try {
				result = fm != null ?

						DynamicInvoker.getExistingInstance().getInstance(clazz, fm, args, nameCaseSensitive, convertComparsion) :

						DynamicInvoker.getExistingInstance().getInstance(clazz, methodName, args, nameCaseSensitive, convertComparsion);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw Caster.toPageException(t);
			}
		}
		return result;
	}
}