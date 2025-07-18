/**
 * Copyright (c) 2014, the Railo Company Ltd.
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
 */
package lucee.transformer.bytecode.expression.var;

import org.objectweb.asm.Type;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Argument;

public class ArgumentImpl extends ExpressionBase implements Argument {

	private Expression raw;
	private String type;

	public ArgumentImpl(Expression value, String type) {
		super(value.getFactory(), value.getStart(), value.getEnd());
		this.raw = value;// Cast.toExpression(value,type);
		this.type = type;
	}

	/**
	 * @return the value
	 */
	@Override
	public Expression getValue() {
		return raw.getFactory().toExpression(raw, type);
	}

	/**
	 * return the uncasted value
	 * 
	 * @return
	 */
	@Override
	public Expression getRawValue() {
		return raw;
	}

	@Override
	public void setValue(Expression value, String type) {
		this.raw = value;
		this.type = type;

	}

	/**
	 *
	 * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(lucee.transformer.bytecode.BytecodeContext,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		return ((ExpressionBase) getValue()).writeOutAsType(bc, mode);
	}

	public Type writeOutValue(BytecodeContext bc, int mode) throws TransformerException {
		bc.visitLine(getStart());
		Type t = ((ExpressionBase) getValue()).writeOutAsType(bc, mode);
		bc.visitLine(getEnd());
		return t;
	}

	/**
	 * @return the type
	 */
	@Override
	public String getStringType() {
		return type;
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, type);
		Struct val = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._value, val);
		raw.dump(val);
	}
}