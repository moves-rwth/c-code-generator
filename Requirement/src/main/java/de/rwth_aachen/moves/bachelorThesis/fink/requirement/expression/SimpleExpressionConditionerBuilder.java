package de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class SimpleExpressionConditionerBuilder {

	private final Set<DataType> possibleReturnTypes = new HashSet<>();
	private EnumSet<Operators> possibleChildOperators;
	private IRange range;

	private boolean wasRangeChanged = false;

	public SimpleExpressionConditionerBuilder(SimpleExpressionConditioner other) {
		possibleReturnTypes.addAll(other.getPossibleReturnTypes());
		range = other.getRange().cloneRange();
		if (other instanceof SimpleChildExpressionConditioner) {
			SimpleChildExpressionConditioner childExpressionConditioner = (SimpleChildExpressionConditioner) other;
			possibleChildOperators = childExpressionConditioner.getPossibleChildOperators().clone();
		} else {
			possibleChildOperators = Operators.getPossibleChildOperatorsByType(possibleReturnTypes, range.isRestricted());
		}
	}

	public SimpleExpressionConditionerBuilder(DataType allowedReturnType) {
		possibleReturnTypes.add(allowedReturnType);
		setRangeFromPossibleReturnTypes();
		possibleChildOperators = Operators.getPossibleChildOperatorsByType(possibleReturnTypes, range.isRestricted());
	}

	public SimpleExpressionConditionerBuilder(Set<DataType> allowedReturnTypes) {
		possibleReturnTypes.addAll(allowedReturnTypes);
		setRangeFromPossibleReturnTypes();
		possibleChildOperators = Operators.getPossibleChildOperatorsByType(possibleReturnTypes, range.isRestricted());
	}

	private void setRangeFromPossibleReturnTypes() {
		if (wasRangeChanged) {
			throw new RuntimeException("Range change will be eliminated by change to return types!");
		} else if (!DataTypeContext.isBasic(possibleReturnTypes)) {
			throw new RuntimeException("Return types are not basic!");
		}

		if (DataTypeContext.isBool(possibleReturnTypes)) {
			range = new SimpleBooleanRange();
		} else if (DataTypeContext.hasFloatingPoint(possibleReturnTypes)) {
			range = new SimpleFloatingPointRange(possibleReturnTypes);
		} else {
			range = new SimpleIntegerRange(possibleReturnTypes);
		}
	}

	public void restrictRangeForOutputVariable() {
		if (DataTypeContext.isBool(possibleReturnTypes)) {
			if (RandomGenHelper.randomChance(1, 2)) {
				range = new SimpleBooleanRange(true, false);
			} else {
				range = new SimpleBooleanRange(false, true);
			}
		} else if (DataTypeContext.hasFloatingPoint(possibleReturnTypes)) {
			range = new SimpleFloatingPointRange(possibleReturnTypes, true);
		} else {
			range = new SimpleIntegerRange(possibleReturnTypes, true);
		}
	}

	public SimpleExpressionConditionerBuilder setPossibleChildOperators(EnumSet<Operators> possibleChildOperators) {
		this.possibleChildOperators = possibleChildOperators.clone();
		return this;
	}

	public SimpleExpressionConditionerBuilder addPossibleReturnTypes(Set<DataType> possibleReturnTypes) {
		this.possibleReturnTypes.addAll(possibleReturnTypes);
		possibleChildOperators = Operators.getPossibleChildOperatorsByType(possibleReturnTypes, range.isRestricted());
		setRangeFromPossibleReturnTypes();

		return this;
	}

	public SimpleExpressionConditionerBuilder removePossibleReturnType(DataType returnType) {
		this.possibleReturnTypes.remove(returnType);
		possibleChildOperators = Operators.getPossibleChildOperatorsByType(possibleReturnTypes, range.isRestricted());
		setRangeFromPossibleReturnTypes();
		return this;
	}

	public SimpleExpressionConditionerBuilder removeFloatingPointReturnTypes() {
		Set<DataType> toRemove = new HashSet<>();
		for (DataType dataType: possibleReturnTypes) {
			if (dataType.isFloatingPoint()) {
				toRemove.add(dataType);
			}
		}
		this.possibleReturnTypes.removeAll(toRemove);

		possibleChildOperators = Operators.getPossibleChildOperatorsByType(possibleReturnTypes, range.isRestricted());
		setRangeFromPossibleReturnTypes();
		return this;
	}

	private void checkConstraints() {
		if (range.isOverconstrained()) {
			throw new RuntimeException("Condition constraints are unsolvable: " + range.toString());
		} else if (possibleReturnTypes.size() == 0) {
			throw new RuntimeException("No return types left for expression!");
		} else if (!possibleReturnTypes.equals(range.getUnderlyingTypes())) {
			throw new RuntimeException("Types of range and expression do not match!");
		} else if (DataTypeContext.hasFloatingPoint(possibleReturnTypes) && !(range instanceof IFloatingPointRange)) {
			throw new RuntimeException("Types of range and expression do not match!");
		}
	}

	public SimpleExpressionConditioner build() {
		checkConstraints();

		return new SimpleExpressionConditioner(possibleReturnTypes, possibleChildOperators, range);
	}

	public Set<DataType> getPossibleReturnTypes() {
		return possibleReturnTypes;
	}

	public SimpleExpressionConditionerBuilder setPossibleReturnTypes(Set<DataType> possibleReturnTypes) {
		this.possibleReturnTypes.clear();
		this.possibleReturnTypes.addAll(possibleReturnTypes);
		possibleChildOperators = Operators.getPossibleChildOperatorsByType(possibleReturnTypes, range.isRestricted());
		setRangeFromPossibleReturnTypes();
		return this;
	}

	public IRange getRange() {
		return range;
	}

	public SimpleExpressionConditionerBuilder setRange(IRange range) {
		wasRangeChanged = true;
		this.range = range;
		return this;
	}
}
