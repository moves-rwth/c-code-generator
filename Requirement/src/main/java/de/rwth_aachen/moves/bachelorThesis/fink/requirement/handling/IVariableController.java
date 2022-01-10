package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IShadowInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

import java.util.EnumMap;
import java.util.Set;

public interface IVariableController {
	IVariable createVariable(SimpleExpressionConditioner expressionConditioner, IProgramContext programContext);

	IVariable createVariable(ParameterType parameterType, DataType dataType, String name, String internalName, IProgramContext programContext, IShadowInformation shadowInformation);

	void registerVariable(IVariable variable, DataType dataType, IProgramContext programContext, SimpleExpressionConditioner expressionConditioner);

	boolean doWeHaveToCreateNewVariable(SimpleExpressionConditioner expressionConditioner, IVariable assignmentVar, boolean standsAlone, Set<IVariable> dontUse, IProgramContext programContext);

	IVariable createNewOutputVariable(EnumMap<Operators, Integer> operatorProbabilities, IProgramContext programContext);

	IVariable getRandomVariableMatchingConstraintsOrNew(SimpleChildExpressionConditioner expressionConditioner, int newVariableChance, IVariable assignmentVar, Set<IVariable> doNotUseInCondition, IProgramContext programContext);

	IVariableController copy();
}
