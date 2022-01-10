package de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Is used to as a "target" in CodeGeneration: We use this class
 * to say how the generated code _should_ behave.
 * Therefore it often is a somewhat vague expression,
 * where the expression of the generated code has to be compatible with.
 */
public class SimpleChildExpressionConditioner extends SimpleExpressionConditioner implements Serializable {

	protected static final Logger logger = LogManager.getLogger(SimpleChildExpressionConditioner.class);
	/**
	 * The operator of the direct parent.
	 */
	private Operators parentOperator;

	/**
	 * Value as in constant (constant might be obfuscated slightly, e.g. "1 << 4")
	 */
	private boolean canBeASimpleValue;

	public SimpleChildExpressionConditioner(SimpleExpressionConditioner exprCond, boolean canBeASimpleValue, Operators parentOperator) {
		super(exprCond.getPossibleReturnTypes(), exprCond.getPossibleChildOperators(), exprCond.getRange());
		this.canBeASimpleValue = canBeASimpleValue;
		this.parentOperator = parentOperator;
	}

	// Getter
	@Override
	public String toString() {
		return super.toString();
	}

	public boolean isCanBeASimpleValue() {
		return canBeASimpleValue;
	}

	public Operators getParentOperator() {
		return this.parentOperator;
	}

	public SimpleExpressionConditioner clone() {
		return new SimpleChildExpressionConditioner(super.clone(), canBeASimpleValue, parentOperator);
	}

	@Override
	public SimpleChildExpressionConditioner removeFloats(IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		return new SimpleChildExpressionConditioner(super.removeFloats(dataTypeContext), canBeASimpleValue, parentOperator);
	}

	@Override
	public SimpleChildExpressionConditioner restrictToType(DataType dataType) throws UnsatisfiableConstraintsException {
		return new SimpleChildExpressionConditioner(super.restrictToType(dataType), canBeASimpleValue, parentOperator);
	}

	private int getBitsNecessary(long value) {
		if (value == 0L) {
			return 0;
		} else {
			long x = 1;
			int count = 1;
			if (value < 0L) {
				x = -1;
				while (value < x) {
					x *= 2L;
					count++;
				}
			} else {
				while (value > x) {
					x *= 2L;
					count++;
				}
			}
			return count;
		}

	}

	/**
	 * Helper functions for Operators.
	 */

	public SimpleChildExpressionConditionerBuilder bitShift(boolean isShiftRight, SimpleExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		if (parentConditioner.getRange().isRestricted()) {
			System.out.println("Warning: Constrained bitShift expressions do not work yet, this will crash...");
			throw new UnsatisfiableConstraintsException("Constrained bitShift expressions do not work yet");
		}

		final SimpleResultingExpressionConditioner valueConditioner = previousChildResultConditioners.get(0);

		final int maxBits = getBitsNecessary(DataType.getMaximumUpperLimit(valueConditioner.getPossibleReturnTypes()));
		logger.debug("There are {} bits for the resulting types.", maxBits);
		SimpleChildExpressionConditionerBuilder result = new SimpleChildExpressionConditionerBuilder(dataTypeContext.integers());
		if (isShiftRight) {
			result.setRange(new SimpleIntegerRange(dataTypeContext.integers(), 0, maxBits, Set.of()));
		} else {
			IIntegerRange integerRange = (IIntegerRange) valueConditioner.getRange();
			final int bitsLower = getBitsNecessary(integerRange.getLowerLimit());
			final int bitsUpper = getBitsNecessary(integerRange.getUpperLimit());
			final int upperLimitShiftBits = maxBits - Math.max(bitsLower, bitsUpper);
			if (upperLimitShiftBits <= 0) {
				throw new UnsatisfiableConstraintsException("Bitshift is too constrained, zero-shifts are stupid.");
			}

			result.setRange(new SimpleIntegerRange(dataTypeContext.integers(), 0, upperLimitShiftBits, Set.of()));
			logger.debug("Range {} needs {} bits for lower limit {}, {} bits for upper limit {}.", integerRange, bitsLower, integerRange.getLowerLimit(), bitsUpper, integerRange.getUpperLimit());
		}
		return result;
	}

	public SimpleChildExpressionConditionerBuilder equalsExpr(Class op, SimpleExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		SimpleChildExpressionConditionerBuilder result = new SimpleChildExpressionConditionerBuilder(dataTypeContext.allBasicTypes());
		if (!(parentConditioner.getRange() instanceof IBooleanRange)) {
			throw new RuntimeException("Expected a boolean range: " + parentConditioner.getRange().toString());
		}

		IBooleanRange booleanRange = (IBooleanRange) parentConditioner.getRange();

		if (parentConditioner.getRange().isRestricted()) {
			throw new UnsatisfiableConstraintsException("Constrained equality expressions are stupid.");
		}

		final boolean isFirst = previousChildResultConditioners.isEmpty();
		IRange range = null;
		if (op == EqualsOperator.class) {
			if (!booleanRange.canBeFalse()) {
				// Has to be true, force both to zero
				range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes(), 0D, 0D, Set.of());
			} else if (!booleanRange.canBeTrue()) {
				// Has to be false, force one to zero, the other to != zero
				if (isFirst) {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).addExclusion(0D);
				} else {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes(), 0D, 0D, Set.of());
				}
			}
		} else if (op == NotEqualsOperator.class) {
			if (!booleanRange.canBeFalse()) {
				// Has to be not equal, force one to zero, the other to != zero
				if (isFirst) {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).addExclusion(0D);
				} else {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes(), 0L, 0L, Set.of());
				}
			} else if (!booleanRange.canBeTrue()) {
				// Has to be equal, force both to zero
				range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes(), 0L, 0L, Set.of());
			}
		} else if (op == GreaterOperator.class) {
			if (!booleanRange.canBeFalse()) {
				// Has to be true, force a > b
				if (isFirst) {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setLowerLimit(1D);
				} else {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setUpperLimit(-1D);
				}
			} else if (!booleanRange.canBeTrue()) {
				// Has to be false, force a <= b
				if (isFirst) {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setUpperLimit(0D);
				} else {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setLowerLimit(0D);
				}
			}
		} else if (op == GreaterEqualsOperator.class) {
			if (!booleanRange.canBeFalse()) {
				// Has to be true, force a >= b
				if (isFirst) {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setLowerLimit(0D);
				} else {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setUpperLimit(0D);
				}
			} else if (!booleanRange.canBeTrue()) {
				// Has to be false, force a < b
				if (isFirst) {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setUpperLimit(-1D);
				} else {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setLowerLimit(1D);
				}
			}
		} else if (op == SmallerOperator.class) {
			if (!booleanRange.canBeFalse()) {
				// Has to be true, force a < b
				if (isFirst) {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setUpperLimit(-1D);
				} else {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setLowerLimit(1D);
				}
			} else if (!booleanRange.canBeTrue()) {
				// Has to be false, force a >= b
				if (isFirst) {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setLowerLimit(0D);
				} else {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setUpperLimit(0D);
				}
			}
		} else if (op == SmallerEqualsOperator.class) {
			if (!booleanRange.canBeFalse()) {
				// Has to be true, force a <= b
				if (isFirst) {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setUpperLimit(0D);
				} else {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setLowerLimit(0D);
				}
			} else if (!booleanRange.canBeTrue()) {
				// Has to be false, force a > b
				if (isFirst) {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setLowerLimit(1D);
				} else {
					range = new SimpleFloatingPointRange(dataTypeContext.allBasicTypes()).setUpperLimit(-1D);
				}
			}
		} else {
			throw new RuntimeException("Unhandled EqualsOpType: " + op);
		}

		if (!isFirst) {
			DataType type = DataTypeContext.getSingle(previousChildResultConditioners.get(0).getPossibleReturnTypes());
			result.setPossibleReturnTypes(DataTypeContext.makeSet(type));
			if (range != null) range = range.restrictToType(type);
		}
		if (range != null) result.setRange(range);
		return result;
	}

	public SimpleChildExpressionConditionerBuilder mathExpr(Class op, SimpleExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners) throws UnsatisfiableConstraintsException {
		SimpleChildExpressionConditionerBuilder result = new SimpleChildExpressionConditionerBuilder(parentConditioner);
		IIntegerRange parentRange = (IIntegerRange) parentConditioner.getRange();
		IFloatingPointRange parentFpRange = null;
		IEditableRange resultRange;
		final boolean useFp = parentConditioner.getRange() instanceof IFloatingPointRange;
		if (useFp) {
			parentFpRange = (IFloatingPointRange) parentConditioner.getRange();
			resultRange = new SimpleFloatingPointRange((IFloatingPointRange) parentConditioner.getRange());
			logger.debug("Using FP range, parent range: {}", parentFpRange.toString());
		} else {
			resultRange = new SimpleIntegerRange(parentRange);
			logger.debug("Using Integer range, parent range: {}", parentRange.toString());
		}
		final boolean isFirst = previousChildResultConditioners.isEmpty();
		if (op == DivisionOperator.class) {
			if (parentRange.isRestricted()) {
				throw new UnsatisfiableConstraintsException("Can not yet do restricted division.");
			}
			if (!isFirst) {
				if (useFp) {
					resultRange = resultRange.addExclusion(0.0D);
				} else {
					resultRange = resultRange.addExclusion(0L);
				}
			}
		} else if (op == ModuloOperator.class) {
			if (parentRange.isRestricted()) {
				throw new UnsatisfiableConstraintsException("Can not yet do restricted modulo division.");
			}
			if (!isFirst) {
				if (useFp) {
					throw new UnsatisfiableConstraintsException("Invalid type with operand %");
				} else {
					resultRange = resultRange.addExclusion(0L);
				}
			}
		} else if (op == PlusOperator.class) {
			if (parentConditioner.getRange().isRestricted()) {
				logger.debug("Op PLUS is restricted.");
				if (useFp) {
					double diff = parentFpRange.getUpperLimitFp() - parentFpRange.getLowerLimitFp();
					if (diff == 0D) {
						throw new UnsatisfiableConstraintsException("Can not use addition for fixed value!");
					}

					final double diffSplitPoint = RandomGenHelper.randomDouble(0.01D, 0.99D);
					final double inverseSplitPoint = 1D - diffSplitPoint;


					final double lowerLimit = parentFpRange.getLowerLimitFp() / 2D;
					final double upperLimit = parentFpRange.getUpperLimitFp() / 2D;
					logger.debug("Diff = {}, lower limit = {}, upper limit = {} (parent lower = {}, upper = {})", diff, lowerLimit, upperLimit, parentFpRange.getLowerLimitFp(), parentFpRange.getUpperLimitFp());

					for (Double d : parentFpRange.getFpExclusions()) {
						if (d == parentFpRange.getLowerLimitFp()) {
							resultRange = resultRange.addExclusion(lowerLimit);
							logger.debug("Adding lower limit as exclusion point.");
						} else if (d == parentFpRange.getUpperLimitFp()) {
							resultRange = resultRange.addExclusion(upperLimit);
							logger.debug("Adding upper limit as exclusion point.");
						} else {
							throw new UnsatisfiableConstraintsException("Can not pass exclusions not on boundaries!");
						}
					}

					resultRange = resultRange.setLowerLimit(lowerLimit);
					resultRange = resultRange.setUpperLimit(upperLimit);
				} else {
					long diff = parentRange.getUpperLimit() - parentRange.getLowerLimit();
					if (diff == 0L) {
						throw new UnsatisfiableConstraintsException("Can not use addition for fixed value!");
					}

					long lowerLimit = parentRange.getLowerLimit() / 2L;
					long upperLimit = parentRange.getUpperLimit() / 2L;
					if ((lowerLimit + lowerLimit) < parentRange.getLowerLimit()) {
						if (!isFirst) {
							lowerLimit += 1L;
						}
					}
					if ((upperLimit + upperLimit) < parentRange.getUpperLimit()) {
						if (isFirst) {
							upperLimit += 1L;
						}
					}

					for (Long l : parentRange.getExclusions()) {
						if (l == parentRange.getLowerLimit()) {
							resultRange = resultRange.addExclusion(lowerLimit);
						} else if (l == parentRange.getUpperLimit()) {
							resultRange = resultRange.addExclusion(upperLimit);
						} else {
							throw new UnsatisfiableConstraintsException("Can not pass exclusions not on boundaries!");
						}
					}

					resultRange = resultRange.setLowerLimit(lowerLimit);
					resultRange = resultRange.setUpperLimit(upperLimit);
				}
			}
		} else if (op == MinusBinaryOperator.class) {
			if (useFp) {
				double diff = parentFpRange.getUpperLimitFp() - parentFpRange.getLowerLimitFp();
				if (diff == 0D) {
					throw new UnsatisfiableConstraintsException("Can not use subtraction for fixed value!");
				}

				final double diffSplitPoint = RandomGenHelper.randomDouble(0.01D, 0.99D);
				final double inverseSplitPoint = 1D - diffSplitPoint;

				double lowerLimit;
				double upperLimit;
				final double halfDiff = diff / 2D;
				if (isFirst) {
					lowerLimit = parentFpRange.getLowerLimitFp() + halfDiff;
					upperLimit = parentFpRange.getUpperLimitFp();
				} else {
					lowerLimit = 0D;
					upperLimit = halfDiff;
				}

				logger.debug("Diff = {}, lower limit = {}, upper limit = {} (parent lower = {}, upper = {})", diff, lowerLimit, upperLimit, parentFpRange.getLowerLimitFp(), parentFpRange.getUpperLimitFp());
				for (Double d : parentFpRange.getFpExclusions()) {
					if (d == parentFpRange.getLowerLimitFp()) {
						if (!isFirst) {
							resultRange = resultRange.addExclusion(upperLimit);
							logger.debug("Adding upper limit as exclusion point.");
						}
					} else if (d == parentFpRange.getUpperLimitFp()) {
						if (!isFirst) {
							resultRange = resultRange.addExclusion(lowerLimit);
							logger.debug("Adding lower limit as exclusion point.");
						}
					} else {
						throw new UnsatisfiableConstraintsException("Can not pass exclusions not on boundaries!");
					}
				}

				resultRange = resultRange.setLowerLimit(lowerLimit);
				resultRange = resultRange.setUpperLimit(upperLimit);
			} else {
				long diff = parentRange.getUpperLimit() - parentRange.getLowerLimit();
				if (diff == 0L) {
					throw new UnsatisfiableConstraintsException("Can not use subtraction for fixed value!");
				}

				long lowerLimit;
				long upperLimit;
				final boolean isOdd = ((diff % 2) != 0);
				final long halfDiff = diff / 2L;
				if (isFirst) {
					lowerLimit = parentRange.getLowerLimit() + halfDiff;
					upperLimit = parentRange.getUpperLimit();
				} else {
					lowerLimit = 0L;
					upperLimit = halfDiff;
				}

				for (Long l : parentRange.getExclusions()) {
					if (l == parentRange.getLowerLimit()) {
						if (!isFirst) {
							resultRange = resultRange.addExclusion(upperLimit);
							logger.debug("Adding upper limit as exclusion point.");
						}
					} else if (l == parentRange.getUpperLimit()) {
						if (!isFirst) {
							resultRange = resultRange.addExclusion(lowerLimit);
							logger.debug("Adding lower limit as exclusion point.");
						}
					} else {
						throw new UnsatisfiableConstraintsException("Can not pass exclusions not on boundaries!");
					}
				}

				resultRange = resultRange.setLowerLimit(lowerLimit);
				resultRange = resultRange.setUpperLimit(upperLimit);
			}
		} else if (op == TimesOperator.class) {
			if (parentRange.isRestricted()) {
				throw new UnsatisfiableConstraintsException("Can not yet do restricted times.");
			}
		} else if (op == MinusUnaryOperator.class) {
			if (parentRange.isRestricted()) {
				throw new UnsatisfiableConstraintsException("Can not yet do restricted unary minus.");
			}
		} else if (op == MinimumOperator.class) {

		} else if (op == MaximumOperator.class) {

		} else {
			throw new RuntimeException("Unhandled MathOp: " + op);
		}
		result.setRange(resultRange);
		return result;
	}
}
