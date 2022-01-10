package de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.AliasType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.RangeWrapsAroundException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * An instance of this class represents properties of an expression, mainly
 * - A range representing the actual value expression can have
 * - What kind of type the expression can have
 * - What kind of operators can be used on / with this expression
 */
public class SimpleExpressionConditioner implements Serializable {

	private final Set<DataType> possibleReturnTypes;
	private final EnumSet<Operators> possibleChildOperators;
	private final IRange range;

	public SimpleExpressionConditioner(
			Set<DataType> possibleReturnTypes,
			EnumSet<Operators> possibleChildOperators,
			IRange range
	) {
		this.possibleReturnTypes = possibleReturnTypes;
		this.possibleChildOperators = possibleChildOperators;
		this.range = range;
	}

	// Getter
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Possible return types:");
		for (DataType d : possibleReturnTypes) {
			sb.append("\t").append(d).append("\n");
		}

		sb.append("Range: ").append(range.toString()).append("\n");

		return sb.toString();
	}
	public Set<DataType> getPossibleReturnTypes() {
		return new HashSet<>(possibleReturnTypes);
	}
	public EnumSet<Operators> getPossibleChildOperators() {
		return possibleChildOperators.clone();
	}
	public IRange getRange() {
		return range;
	}
	@Override
	public SimpleExpressionConditioner clone() {
		return new SimpleExpressionConditioner(possibleReturnTypes, possibleChildOperators, range);
	}

	/*
		Meaning: We match all expectations of other
		("this" has to fit "within target")
	 */
	public boolean isCompatibleWith(SimpleExpressionConditioner target, IDataTypeContext dataTypeContext) {
		assert(this.getPossibleReturnTypes().size() == 1);
		assert(this.getRange().getUnderlyingTypes().size() == 1);
		DataType resultType = DataTypeContext.getSingle(getPossibleReturnTypes());
		SimpleExpressionConditioner result = this;

		// NOTE(Felix): While int <-> float conversion is completely fine in C, we have decided to ban float->int conversion
		// (while allowing int->float promotion)
		Set<DataType> compatibleTypes = new HashSet<>();
		{
			for (DataType targetType : target.getPossibleReturnTypes()) {
				if (targetType instanceof AliasType) { targetType = ((AliasType)targetType).getTrueBaseType(); }
				assert(targetType.isBasic());
				boolean typesMatch = (targetType == resultType); // This also covers booleans
				boolean areBothInts = (targetType.isInteger() && resultType.isInteger());
				boolean areBothFloats = (targetType.isFloatingPoint() && resultType.isFloatingPoint());
				if (typesMatch || areBothInts || areBothFloats) {
					compatibleTypes.add(targetType);
				}
			}
		}

		// Types can be implicitly converted, but now we have to check whether the underlying (resulting)
		// ranges are compatible as well
		// if (result.getRange() instanceof IntegerRange)
		// ... else if (result.getRange() instanceof FloatingRange)
		// and so on
		IRange targetRange = target.getRange();
		for (DataType targetType : compatibleTypes) {
			try {
				if (targetRange instanceof SimpleIntegerRange) {
					if (result.getRange() instanceof SimpleIntegerRange) {
						SimpleIntegerRange truncatedRange = ((SimpleIntegerRange) result.getRange()).convertToDataTypeWithTruncation(targetType);
						if (truncatedRange.isCompatibleWith(targetRange)) {
							return true;
						}
					}
				} else {
					if (result.getRange().isCompatibleWith(targetRange)) {
						return true;
					}
				}
			} catch (RangeWrapsAroundException error) {
				// NOTE(Felix): Truncation of range would result in two ranges (e.g. [125; 127] U [-128; -150])
				// which is not supported.
			}
		}
		return false;
	}

	public SimpleExpressionConditioner or(SimpleExpressionConditioner expressionConditioner) throws UnsatisfiableConstraintsException {
		if (!this.possibleReturnTypes.equals(expressionConditioner.possibleReturnTypes)) {
			throw new RuntimeException("Return types do not match!");
		}
		return new SimpleExpressionConditioner(possibleReturnTypes, possibleChildOperators, range.or(expressionConditioner.getRange()));
	}

	public SimpleExpressionConditioner removeFloats(IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		SimpleExpressionConditionerBuilder simpleExpressionConditionerBuilder = new SimpleExpressionConditionerBuilder(this).removeFloatingPointReturnTypes().addPossibleReturnTypes(dataTypeContext.integersAndBool());
		if (this.range instanceof SimpleFloatingPointRange) {
			simpleExpressionConditionerBuilder.setRange(((SimpleFloatingPointRange) range).removeFloats());
		}
		return simpleExpressionConditionerBuilder.build();
	}

	public SimpleExpressionConditioner restrictToType(DataType dataType) throws UnsatisfiableConstraintsException {
		return new SimpleExpressionConditionerBuilder(this).setPossibleReturnTypes(DataTypeContext.makeSet(dataType)).setRange(range.restrictToType(dataType)).build();
	}
}
