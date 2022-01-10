package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;

import java.util.List;

public interface IVariableReplacer {
	/**
	 * This function can be used to replace a variable used in a VariableTreeNode.
	 * The reason can be renaming or creating an independent copy of a piece of code.
	 *
	 * @param variable The variable to be replaced.
	 * @return A replacement of the given variable if available, else the old variable.
	 */
	IVariableWithAccessor getReplacement(IVariableWithAccessor variable, VariableTreeNode node);

	List<IVariableWithAccessor> getReplacements(List<IVariableWithAccessor> variables);

	VariableReplacer.PreVarOperator getPreVarOperator();

	boolean containsVariable(IVariableWithAccessor var);

	boolean containsVariable(IVariable var);

	boolean castTypes();
}
