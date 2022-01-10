package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

public interface IMutableVariableContext extends IVariableContext {
	void updateVariableContext(IVariable variable, SimpleExpressionConditioner expressionConditioner);

	void replaceVariable(IVariable toReplace, IVariable replacementVariable, SimpleExpressionConditioner replacementConditioner);

	IMutableVariableContext merge(IVariableContext other);

	IMutableVariableContext copy();
}
