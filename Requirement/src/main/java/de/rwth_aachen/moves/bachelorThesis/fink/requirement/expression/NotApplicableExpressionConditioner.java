package de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression;

import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;

import java.io.Serializable;
import java.util.EnumSet;

/**
 * A placeholder ExpressionConditioner, indicating that trying to gather information about a certain
 * expression is useless. For example: Retrieving an ExpressionConditioner of a PointerVariable is useless
 * because internally we know what the pointer points to, so one should retrieve the expressionConditioner
 * of the value pointed to instead.
 */
public class NotApplicableExpressionConditioner extends SimpleExpressionConditioner implements Serializable {

	private final DataType myType;

	public NotApplicableExpressionConditioner(DataType myType) {
		super(ImmutableSet.of(myType), EnumSet.noneOf(Operators.class), null);
		this.myType = myType;
	}

	// Getter
	@Override
	public SimpleExpressionConditioner clone() {
		return new NotApplicableExpressionConditioner(myType);
	}

	@Override
	public boolean isCompatibleWith(SimpleExpressionConditioner other, IDataTypeContext dataTypeContext) {
		if (other instanceof NotApplicableExpressionConditioner) {
			NotApplicableExpressionConditioner otherOne = (NotApplicableExpressionConditioner) other;
			return (otherOne.myType == myType);
		}
		return false;
	}

	@Override
	public SimpleExpressionConditioner removeFloats(IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		throw new UnsatisfiableConstraintsException("Can not remove floats on NotApplicable conditioner!");
	}

	@Override
	public SimpleExpressionConditioner restrictToType(DataType dataType) throws UnsatisfiableConstraintsException {
		if (dataType == myType) {
			return this;
		}
		throw new UnsatisfiableConstraintsException("Can not restrict on NotApplicable conditioner!");
	}
}
