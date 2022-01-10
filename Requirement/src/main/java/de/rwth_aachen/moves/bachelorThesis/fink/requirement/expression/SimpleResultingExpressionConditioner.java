package de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.RangeWrapsAroundException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

/**
 * Is used to represent properties of generated code (of an expression).
 * Small example:
 * Suppose there is an u8 "A". We know nothing about the values of A so far, so
 * 0 <= A <= 255
 * but if our generated code / expression is "A / 10", then this resulting ExpressionConditioner would indicate
 * 0 <= A <= 25
 */
public class SimpleResultingExpressionConditioner extends SimpleExpressionConditioner implements Serializable {

	protected static final Logger logger = LogManager.getLogger(SimpleResultingExpressionConditioner.class);
	private final IIntegerRange arrayIndexRange;

	public SimpleResultingExpressionConditioner(SimpleExpressionConditioner exprCond, IIntegerRange arrayIndexRange) {
		super(exprCond.getPossibleReturnTypes(), exprCond.getPossibleChildOperators(), exprCond.getRange());
		this.arrayIndexRange = arrayIndexRange;
	}

	/**
	 * Helper functions for certain operators with overlapping code.
	 */

	/*
		Constructing parent result from child results
	 */
	public static SimpleResultingExpressionConditioner combineAbsExpression(SimpleExpressionConditioner resultA) throws UnsatisfiableConstraintsException {
		logger.debug("combineAbsExpression with resultA = {}", resultA.toString());
		IRange resultRange;
		if (resultA.getRange() instanceof IFloatingPointRange) {
			IFloatingPointRange range = (IFloatingPointRange) resultA.getRange();

			final double lowerLimit = range.getLowerLimitFp();
			final double upperLimit = range.getUpperLimitFp();
			double newLowerLimit;
			double newUpperLimit;
			// Three cases: under zero, stretches over zero, over zero
			if (upperLimit < 0D) {
				newLowerLimit = Math.abs(upperLimit);
				newUpperLimit = Math.abs(lowerLimit);
				logger.debug("ll = {}, ul = {}, abs-ll = {}, abs-ul = {}, case 'under zero' FP.", lowerLimit, upperLimit, newLowerLimit, newUpperLimit);
			} else if (lowerLimit > 0D) {
				newLowerLimit = lowerLimit;
				newUpperLimit = upperLimit;
				logger.debug("ll = {}, ul = {}, abs-ll = {}, abs-ul = {}, case 'over zero' FP.", lowerLimit, upperLimit, newLowerLimit, newUpperLimit);
			} else {
				newLowerLimit = 0D;
				newUpperLimit = Math.max(Math.abs(lowerLimit), upperLimit);
				logger.debug("ll = {}, ul = {}, abs-ll = {}, abs-ul = {}, case 'stretched over zero' FP.", lowerLimit, upperLimit, newLowerLimit, newUpperLimit);
			}

			resultRange = new SimpleFloatingPointRange(range, newLowerLimit, newUpperLimit);
		} else {
			// Integer promotion as the "-" in front of e.g. signed i8 with -128 needs to be modelled correctly
			resultA = resultA.restrictToType(DataType.doTypeWidening(resultA.getRange()));
			IIntegerRange integerRange = (IIntegerRange) resultA.getRange();

			final long lowerLimit = integerRange.getLowerLimit();
			final long upperLimit = integerRange.getUpperLimit();
			long newLowerLimit;
			long newUpperLimit;
			// Three cases: under zero, stretches over zero, over zero
			if (upperLimit < 0L) {
				newLowerLimit = Math.abs(upperLimit);
				newUpperLimit = Math.abs(lowerLimit);
				logger.debug("ll = {}, ul = {}, abs-ll = {}, abs-ul = {}, case 'under zero'.", lowerLimit, upperLimit, newLowerLimit, newUpperLimit);
			} else if (lowerLimit > 0L) {
				newLowerLimit = lowerLimit;
				newUpperLimit = upperLimit;
				logger.debug("ll = {}, ul = {}, abs-ll = {}, abs-ul = {}, case 'over zero'.", lowerLimit, upperLimit, newLowerLimit, newUpperLimit);
			} else {
				newLowerLimit = 0L;
				newUpperLimit = Math.max(Math.abs(lowerLimit), upperLimit);
				logger.debug("ll = {}, ul = {}, abs-ll = {}, abs-ul = {}, case 'stretched over zero'.", lowerLimit, upperLimit, newLowerLimit, newUpperLimit);
			}
			resultRange = new SimpleIntegerRange(integerRange, newLowerLimit, newUpperLimit);
		}

		return new SimpleResultingExpressionConditionerBuilder(resultA).setRange(resultRange).build();
	}

	public static SimpleResultingExpressionConditioner combineMathExpression(Class op, SimpleExpressionConditioner resultA, SimpleExpressionConditioner resultB) throws UnsatisfiableConstraintsException {
		// Type widening
		IIntegerRange rangeA = (IIntegerRange) resultA.getRange();
		IIntegerRange rangeB = (IIntegerRange) resultB.getRange();
		DataType newType = DataType.doTypeWidening(rangeA, rangeB);
		try {
			// On integers: We might need to do truncation
			if (newType.isInteger()) {
				rangeA = ((SimpleIntegerRange)rangeA).convertToDataTypeWithTruncation(newType);
				rangeB = ((SimpleIntegerRange)rangeB).convertToDataTypeWithTruncation(newType);
			}
		} catch (RangeWrapsAroundException error) {
			throw new UnsatisfiableConstraintsException("Range would split");
		}
		rangeA = (IIntegerRange) rangeA.restrictToType(newType);
		rangeB = (IIntegerRange) rangeB.restrictToType(newType);

		SimpleResultingExpressionConditionerBuilder result = new SimpleResultingExpressionConditionerBuilder(newType);
		if (op == DivisionOperator.class) {
			if (newType.isFloatingPoint()) {
				result.setRange(SimpleFloatingPointRange.div(rangeA, rangeB));
			} else {
				result.setRange(SimpleIntegerRange.div(rangeA, rangeB));
			}
		} else if (op == ModuloOperator.class) {
			if (newType.isFloatingPoint()) {
				throw new UnsatisfiableConstraintsException("Invalid type with operand %");
			} else {
				result.setRange(SimpleIntegerRange.mod(rangeA, rangeB));
			}
		} else if (op == TimesOperator.class) {
			if (newType.isFloatingPoint()) {
				result.setRange(SimpleFloatingPointRange.times(rangeA, rangeB));
			} else {
				result.setRange(SimpleIntegerRange.times(rangeA, rangeB));
			}
		} else if (op == PlusOperator.class) {
			if (newType.isFloatingPoint()) {
				result.setRange(SimpleFloatingPointRange.plus(rangeA, rangeB));
			} else {
				result.setRange(SimpleIntegerRange.plus(rangeA, rangeB));
			}
		} else if (op == MinusBinaryOperator.class) {
			if (newType.isFloatingPoint()) {
				result.setRange(SimpleFloatingPointRange.minus(rangeA, rangeB));
			} else {
				result.setRange(SimpleIntegerRange.minus(rangeA, rangeB));
			}
		} else if (op == MaximumOperator.class) {
			if (newType.isFloatingPoint()) {
				result.setRange(SimpleFloatingPointRange.max(rangeA, rangeB));
			} else {
				result.setRange(SimpleIntegerRange.max(rangeA, rangeB));
			}
		} else if (op == MinimumOperator.class) {
			if (newType.isFloatingPoint()) {
				result.setRange(SimpleFloatingPointRange.min(rangeA, rangeB));
			} else {
				result.setRange(SimpleIntegerRange.min(rangeA, rangeB));
			}
		} else {
			throw new RuntimeException("Unhandled MathOpType " + op);
		}
		logger.debug("Math range after: {}", result.getRange().toString());
		return result.build();
	}

	// Getter
	@Override
	public String toString() {
		return super.toString() + "Array Index range: " + arrayIndexRange.toString() + System.lineSeparator();
	}
	public IIntegerRange getArrayIndexRange() {
		return arrayIndexRange;
	}
	@Override
	public SimpleExpressionConditioner clone() {
		return new SimpleResultingExpressionConditioner(super.clone(), arrayIndexRange);
	}

	@Override
	public SimpleResultingExpressionConditioner removeFloats(IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		return new SimpleResultingExpressionConditioner(super.removeFloats(dataTypeContext), arrayIndexRange);
	}

	@Override
	public SimpleResultingExpressionConditioner restrictToType(DataType dataType) throws UnsatisfiableConstraintsException {
		return new SimpleResultingExpressionConditioner(super.restrictToType(dataType), arrayIndexRange);
	}

	public SimpleResultingExpressionConditioner or(SimpleResultingExpressionConditioner expressionConditioner) throws UnsatisfiableConstraintsException {
		return new SimpleResultingExpressionConditionerBuilder(super.or(expressionConditioner)).build();
	}
}
