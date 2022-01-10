package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling;

import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

public enum UnaryOperation {
	PREFIXINCR,
	PREFIXDECR,
	PLUS,
	MINUS,
	STAR,
	AMPER,
	TILDE,
	NOT,
	SIZEOF,
	POSTFIXINCR,
	POSTFIXDECR,
	BRACKETEDPRIMARY,
	THROW,
	TYPEID;

	@Override
	public String toString() {
		return name();
	}

	public static UnaryOperation fromCdtUnaryOperationId(int cdtUnaryOperationId) {
		switch (cdtUnaryOperationId) {
			case IASTUnaryExpression.op_prefixIncr:
				return PREFIXINCR;
			case IASTUnaryExpression.op_prefixDecr:
				return PREFIXDECR;
			case IASTUnaryExpression.op_plus:
				return PLUS;
			case IASTUnaryExpression.op_minus:
				return MINUS;
			case IASTUnaryExpression.op_star:
				return STAR;
			case IASTUnaryExpression.op_amper:
				return AMPER;
			case IASTUnaryExpression.op_tilde:
				return TILDE;
			case IASTUnaryExpression.op_not:
				return NOT;
			case IASTUnaryExpression.op_sizeof:
				return SIZEOF;
			case IASTUnaryExpression.op_postFixIncr:
				return POSTFIXINCR;
			case IASTUnaryExpression.op_postFixDecr:
				return POSTFIXDECR;
			case IASTUnaryExpression.op_bracketedPrimary:
				return BRACKETEDPRIMARY;
			case IASTUnaryExpression.op_throw:
				return THROW;
			case IASTUnaryExpression.op_typeid:
				return TYPEID;
			default:
				throw new IllegalStateException("Unhandled unary operation id: " + cdtUnaryOperationId);
		}
	}
}
