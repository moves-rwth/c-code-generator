package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IParentVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

import java.io.Serializable;
import java.util.Map;

/**
 * For every variable, there can be many ways to access it, with pointers aliasing being the worst offender.
 * So, every time a variable is referenced, we need to know how it is to be accessed.
 */
public interface IVariableAccessInformation extends Comparable<IVariableAccessInformation>, Serializable {
	int compareTo(IVariableAccessInformation o);

	String getVariableAccessor();
	IParentVariable getParent(Variable variable);

	Map<IVariable, IParentVariable> getParentVariableMap();
	IVariable getBaseVariable();
	boolean isTrivial();

	IVariableAccessInformation accessPointer(IVariable subVariable);
	IVariableAccessInformation accessField(IVariable subVariable);

	IVariableAccessInformation replaceVariables(Map<IVariable, IVariable> variableReplacementMap);

	IVariableAccessInformation getParent();
}
