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

package lucee.runtime.sql.old;

import java.util.Vector;

// Referenced classes of package Zql:
//            ZExp, ZQuery, ZConstant, ZUtils

public final class ZExpression implements ZExp {

	public ZExpression(String s) {
		op_ = null;
		operands_ = null;
		op_ = new String(s);
	}

	public ZExpression(String s, ZExp zexp) {
		op_ = null;
		operands_ = null;
		op_ = new String(s);
		addOperand(zexp);
	}

	public ZExpression(String s, ZExp zexp, ZExp zexp1) {
		op_ = null;
		operands_ = null;
		op_ = new String(s);
		addOperand(zexp);
		addOperand(zexp1);
	}

	public String getOperator() {
		return op_;
	}

	public void setOperands(Vector vector) {
		operands_ = vector;
	}

	public Vector getOperands() {
		return operands_;
	}

	public void addOperand(ZExp zexp) {
		if (operands_ == null) operands_ = new Vector();
		operands_.addElement(zexp);
	}

	public ZExp getOperand(int i) {
		if (operands_ == null || i >= operands_.size()) return null;
		return (ZExp) operands_.elementAt(i);
	}

	public int nbOperands() {
		if (operands_ == null) return 0;
		return operands_.size();
	}

	public String toReversePolish() {
		StringBuilder sb = new StringBuilder("(");
		sb.append(op_);
		for (int i = 0; i < nbOperands(); i++) {
			ZExp zexp = getOperand(i);
			if (zexp instanceof ZExpression) sb.append(" " + ((ZExpression) zexp).toReversePolish());
			else if (zexp instanceof ZQuery) sb.append(" (" + zexp.toString() + ")");
			else sb.append(" " + zexp.toString());
		}

		sb.append(")");
		return sb.toString();
	}

	@Override
	public String toString() {
		if (op_.equals("?")) return op_;
		if (ZUtils.isCustomFunction(op_) > 0) return formatFunction();
		StringBuilder sb = new StringBuilder();
		if (needPar(op_)) sb.append("(");
		switch (nbOperands()) {
		case 1: // '\001'
			ZExp zexp = getOperand(0);
			if (zexp instanceof ZConstant) {
				if (ZUtils.isAggregate(op_)) sb.append(op_ + "(" + zexp.toString() + ")");
				else sb.append(op_ + " " + zexp.toString());
			}
			else if (zexp instanceof ZQuery) sb.append(op_ + " (" + zexp.toString() + ")");
			else sb.append(op_ + " " + zexp.toString());
			break;

		case 3: // '\003'
			if (op_.toUpperCase().endsWith("BETWEEN")) {
				sb.append(getOperand(0).toString() + " " + op_ + " " + getOperand(1).toString() + " AND " + getOperand(2).toString());
				break;
			}
			// fall through

		default:
			boolean flag = op_.equals("IN") || op_.equals("NOT IN");
			int i = nbOperands();
			for (int j = 0; j < i; j++) {
				if (flag && j == 1) sb.append(" " + op_ + " (");
				ZExp zexp1 = getOperand(j);
				if ((zexp1 instanceof ZQuery) && !flag) sb.append("(" + zexp1.toString() + ")");
				else sb.append(zexp1.toString());
				if (j < i - 1) if (op_.equals(",") || flag && j > 0) sb.append(", ");
				else if (!flag) sb.append(" " + op_ + " ");
			}

			if (flag) sb.append(")");
			break;
		}
		if (needPar(op_)) sb.append(")");
		return sb.toString();
	}

	private boolean needPar(String s) {
		s = s.toUpperCase();
		return !s.equals("ANY") && !s.equals("ALL") && !s.equals("UNION") && !ZUtils.isAggregate(s);
	}

	private String formatFunction() {
		StringBuilder sb = new StringBuilder(op_ + "(");
		int i = nbOperands();
		for (int j = 0; j < i; j++)
			sb.append(getOperand(j).toString() + (j >= i - 1 ? "" : ","));

		sb.append(")");
		return sb.toString();
	}

	String op_;
	Vector operands_;
}