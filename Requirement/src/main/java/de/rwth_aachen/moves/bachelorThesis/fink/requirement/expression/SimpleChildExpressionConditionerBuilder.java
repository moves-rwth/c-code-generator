package de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;

import java.util.EnumSet;
import java.util.Set;

public class SimpleChildExpressionConditionerBuilder {

	private final SimpleExpressionConditionerBuilder simpleExpressionConditionerBuilder;
	private boolean canBeASimpleValue = true;
	private Operators parentOperator;

	public SimpleChildExpressionConditionerBuilder(SimpleChildExpressionConditioner other) {
		simpleExpressionConditionerBuilder = new SimpleExpressionConditionerBuilder(other);
		canBeASimpleValue = other.isCanBeASimpleValue();
		parentOperator = other.getParentOperator();
	}

	public SimpleChildExpressionConditionerBuilder(SimpleExpressionConditioner other) {
		simpleExpressionConditionerBuilder = new SimpleExpressionConditionerBuilder(other);
	}

	public SimpleChildExpressionConditionerBuilder(DataType allowedReturnType) {
		simpleExpressionConditionerBuilder = new SimpleExpressionConditionerBuilder(allowedReturnType);
	}

	public SimpleChildExpressionConditionerBuilder(Set<DataType> allowedReturnTypes) {
		simpleExpressionConditionerBuilder = new SimpleExpressionConditionerBuilder(allowedReturnTypes);
	}

	public SimpleChildExpressionConditionerBuilder setPossibleChildOperators(EnumSet<Operators> possibleChildOperators) {
		simpleExpressionConditionerBuilder.setPossibleChildOperators(possibleChildOperators);
		return this;
	}

	public SimpleChildExpressionConditionerBuilder addPossibleReturnTypes(Set<DataType> possibleReturnTypes) {
		simpleExpressionConditionerBuilder.addPossibleReturnTypes(possibleReturnTypes);
		return this;
	}

	public SimpleChildExpressionConditionerBuilder removePossibleReturnType(DataType returnType) {
		simpleExpressionConditionerBuilder.removePossibleReturnType(returnType);
		return this;
	}

	public SimpleChildExpressionConditionerBuilder setCanBeASimpleValue(boolean canBeASimpleValue) {
		this.canBeASimpleValue = canBeASimpleValue;
		return this;
	}

	public SimpleChildExpressionConditionerBuilder setParentOperator(Operators parentOperator) {
		this.parentOperator = parentOperator;
		return this;
	}

	public SimpleChildExpressionConditioner build() {
		return new SimpleChildExpressionConditioner(simpleExpressionConditionerBuilder.build(), canBeASimpleValue, parentOperator);
	}

	public Set<DataType> getPossibleReturnTypes() {
		return simpleExpressionConditionerBuilder.getPossibleReturnTypes();
	}

	public SimpleChildExpressionConditionerBuilder setPossibleReturnTypes(Set<DataType> possibleReturnTypes) {
		simpleExpressionConditionerBuilder.setPossibleReturnTypes(possibleReturnTypes);
		return this;
	}

	public void restrictRangeForOutputVariable() {
		simpleExpressionConditionerBuilder.restrictRangeForOutputVariable();
	}

	public IRange getRange() {
		return simpleExpressionConditionerBuilder.getRange();
	}

	public SimpleChildExpressionConditionerBuilder setRange(IRange range) {
		simpleExpressionConditionerBuilder.setRange(range);
		return this;
	}
}
