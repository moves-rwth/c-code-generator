package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;

public enum BinaryOperation {
	MULTIPLY,
	DIVIDE,
	MODULO,
	PLUS,
	MINUS,
	SHIFTLEFT,
	SHIFTRIGHT,
	LESSTHAN,
	GREATERTHAN,
	LESSEQUAL,
	GREATEREQUAL,
	BINARYAND,
	BINARYXOR,
	BINARYOR,
	LOGICALAND,
	LOGICALOR,
	ASSIGN,
	MULTIPLYASSIGN,
	DIVIDEASSIGN,
	MODULOASSIGN,
	PLUSASSIGN,
	MINUSASSIGN,
	SHIFTLEFTASSIGN,
	SHIFTRIGHTASSIGN,
	BINARYANDASSIGN,
	BINARYXORASSIGN,
	BINARYORASSIGN,
	EQUALS,
	NOTEQUALS,
	PMDOT,
	PMARROW,
	MAX,
	MIN,
	ELLIPSES;

	@Override
	public String toString() {
		return name();
	}

	public static BinaryOperation fromCdtBinaryOperationId(int cdtBinaryOperationId) {
		switch (cdtBinaryOperationId) {
			case IASTBinaryExpression.op_multiply:
				return MULTIPLY;
			case IASTBinaryExpression.op_divide:
				return DIVIDE;
			case IASTBinaryExpression.op_modulo:
				return MODULO;
			case IASTBinaryExpression.op_plus:
				return PLUS;
			case IASTBinaryExpression.op_minus:
				return MINUS;
			case IASTBinaryExpression.op_shiftLeft:
				return SHIFTLEFT;
			case IASTBinaryExpression.op_shiftRight:
				return SHIFTRIGHT;
			case IASTBinaryExpression.op_lessThan:
				return LESSTHAN;
			case IASTBinaryExpression.op_greaterThan:
				return GREATERTHAN;
			case IASTBinaryExpression.op_lessEqual:
				return LESSEQUAL;
			case IASTBinaryExpression.op_greaterEqual:
				return GREATEREQUAL;
			case IASTBinaryExpression.op_binaryAnd:
				return BINARYAND;
			case IASTBinaryExpression.op_binaryXor:
				return BINARYXOR;
			case IASTBinaryExpression.op_binaryOr:
				return BINARYOR;
			case IASTBinaryExpression.op_logicalAnd:
				return LOGICALAND;
			case IASTBinaryExpression.op_logicalOr:
				return LOGICALOR;
			case IASTBinaryExpression.op_assign:
				return ASSIGN;
			case IASTBinaryExpression.op_multiplyAssign:
				return MULTIPLYASSIGN;
			case IASTBinaryExpression.op_divideAssign:
				return DIVIDEASSIGN;
			case IASTBinaryExpression.op_moduloAssign:
				return MODULOASSIGN;
			case IASTBinaryExpression.op_plusAssign:
				return PLUSASSIGN;
			case IASTBinaryExpression.op_minusAssign:
				return MINUSASSIGN;
			case IASTBinaryExpression.op_shiftLeftAssign:
				return SHIFTLEFTASSIGN;
			case IASTBinaryExpression.op_shiftRightAssign:
				return SHIFTRIGHTASSIGN;
			case IASTBinaryExpression.op_binaryAndAssign:
				return BINARYANDASSIGN;
			case IASTBinaryExpression.op_binaryXorAssign:
				return BINARYXORASSIGN;
			case IASTBinaryExpression.op_binaryOrAssign:
				return BINARYORASSIGN;
			case IASTBinaryExpression.op_equals:
				return EQUALS;
			case IASTBinaryExpression.op_notequals:
				return NOTEQUALS;
			case IASTBinaryExpression.op_pmdot:
				return PMDOT;
			case IASTBinaryExpression.op_pmarrow:
				return PMARROW;
			case IASTBinaryExpression.op_max:
				return MAX;
			case IASTBinaryExpression.op_min:
				return MIN;
			case IASTBinaryExpression.op_ellipses:
				return ELLIPSES;
			default:
				throw new IllegalStateException("Unhandled binary operation id: " + cdtBinaryOperationId);
		}
	}
}
