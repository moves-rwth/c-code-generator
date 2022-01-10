package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import com.google.common.collect.ImmutableMap;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

public interface IVariableContext {

	/*
		This is used to always reflect the variable context at the current point in the program, to make it easy to build onto
	 */
	SimpleExpressionConditioner getVariableConditioner(IVariable variable);

	ImmutableMap<IVariable, SimpleExpressionConditioner> getVariableConditioners();

	IVariableContext copy();

	IVariableContext merge(IVariableContext other);

}
