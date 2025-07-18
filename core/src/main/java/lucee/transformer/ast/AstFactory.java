package lucee.transformer.ast;

import java.math.BigDecimal;

import lucee.runtime.exp.PageException;
import lucee.transformer.Body;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.Page;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.ExprInt;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitInteger;
import lucee.transformer.expression.literal.LitLong;
import lucee.transformer.expression.literal.LitNumber;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.statement.Statement;
import lucee.transformer.util.SourceCode;

public class AstFactory extends Factory {

	@Override
	public LitBoolean TRUE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitBoolean FALSE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitString EMPTY() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitNumber NUMBER_ZERO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitNumber NUMBER_ONE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression NULL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitString createLitString(String str) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitString createLitString(String str, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitBoolean createLitBoolean(boolean b) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitBoolean createLitBoolean(boolean b, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitNumber createLitNumber(String number) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitNumber createLitNumber(String number, Position start, Position end) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitNumber createLitNumber(Number n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitNumber createLitNumber(Number n, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitNumber createLitNumber(BigDecimal bd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitNumber createLitNumber(BigDecimal bd, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitLong createLitLong(long l) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitLong createLitLong(long l, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitInteger createLitInteger(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitInteger createLitInteger(int i, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression createNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression createNull(Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression createNullConstant(Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNull(Expression expr) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Expression createEmpty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Literal createLiteral(Object obj, Literal defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataMember createDataMember(ExprString name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable createVariable(Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable createVariable(int scope, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression createStruct() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression createArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprNumber toExprNumber(Expression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprString toExprString(Expression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprBoolean toExprBoolean(Expression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprInt toExprInt(Expression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression toExpression(Expression expr, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprString opString(Expression left, Expression right) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprString opString(Expression left, Expression right, boolean concatStatic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprBoolean opBool(Expression left, Expression right, int operation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprNumber opNumber(Expression left, Expression right, int operation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprNumber opUnaryNumber(Variable var, Expression value, short type, int operation, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprString opUnaryString(Variable var, Expression value, short type, int operation, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression opNegate(Expression expr, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprNumber opNegateNumber(Expression expr, int operation, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression opContional(Expression cont, Expression left, Expression right) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExprBoolean opDecision(Expression left, Expression concatOp, int operation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression opElvis(Variable left, Expression right) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression removeCastString(Expression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerKey(Context bc, Expression name, boolean doUpperCase) throws TransformerException {
		// TODO Auto-generated method stub

	}

	@Override
	public Page createPage(SourceCode sc, long sourceLastModified, boolean returnValue, boolean ignoreScopes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Body createBody() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Statement createPrintOut(Expression expr, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

}
