package de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;

import java.util.EnumSet;
import java.util.Set;

public class SimpleResultingExpressionConditionerBuilder {

	private final SimpleExpressionConditionerBuilder simpleExpressionConditionerBuilder;
	private final IIntegerRange arrayIndexRange;

	public SimpleResultingExpressionConditionerBuilder(SimpleResultingExpressionConditioner other) {
		simpleExpressionConditionerBuilder = new SimpleExpressionConditionerBuilder(other);

		arrayIndexRange = other.getArrayIndexRange().cloneIntegerRange();
	}

	public SimpleResultingExpressionConditionerBuilder(SimpleExpressionConditioner other) {
		simpleExpressionConditionerBuilder = new SimpleExpressionConditionerBuilder(other);

		arrayIndexRange = new SimpleIntegerRange(simpleExpressionConditionerBuilder.getPossibleReturnTypes());
	}

	public SimpleResultingExpressionConditionerBuilder(DataType allowedReturnType) {
		simpleExpressionConditionerBuilder = new SimpleExpressionConditionerBuilder(allowedReturnType);
		arrayIndexRange = new SimpleIntegerRange(simpleExpressionConditionerBuilder.getPossibleReturnTypes());
	}

	public SimpleResultingExpressionConditionerBuilder(Set<DataType> allowedReturnTypes) {
		simpleExpressionConditionerBuilder = new SimpleExpressionConditionerBuilder(allowedReturnTypes);
		arrayIndexRange = new SimpleIntegerRange(simpleExpressionConditionerBuilder.getPossibleReturnTypes());
	}

	public SimpleResultingExpressionConditionerBuilder setPossibleChildOperators(EnumSet<Operators> possibleChildOperators) {
		simpleExpressionConditionerBuilder.setPossibleChildOperators(possibleChildOperators);
		return this;
	}

	public SimpleResultingExpressionConditionerBuilder addPossibleReturnTypes(Set<DataType> possibleReturnTypes) {
		simpleExpressionConditionerBuilder.addPossibleReturnTypes(possibleReturnTypes);
		return this;
	}

	public SimpleResultingExpressionConditionerBuilder removePossibleReturnType(DataType returnType) {
		simpleExpressionConditionerBuilder.removePossibleReturnType(returnType);
		return this;
	}

	private void checkConstraints() {
		if (arrayIndexRange.isOverconstrained()) {
			throw new RuntimeException("Condition constraints are unsolvable: " + arrayIndexRange.toString());
		}
	}

	public SimpleResultingExpressionConditioner build() {
		checkConstraints();

		return new SimpleResultingExpressionConditioner(simpleExpressionConditionerBuilder.build(), arrayIndexRange);
	}

	public Set<DataType> getPossibleReturnTypes() {
		return simpleExpressionConditionerBuilder.getPossibleReturnTypes();
	}

	public SimpleResultingExpressionConditionerBuilder setPossibleReturnTypes(Set<DataType> possibleReturnTypes) {
		simpleExpressionConditionerBuilder.setPossibleReturnTypes(possibleReturnTypes);
		return this;
	}

	public IRange getRange() {
		return simpleExpressionConditionerBuilder.getRange();
	}

	public SimpleResultingExpressionConditionerBuilder setRange(IRange range) {
		simpleExpressionConditionerBuilder.setRange(range);
		return this;
	}
}
