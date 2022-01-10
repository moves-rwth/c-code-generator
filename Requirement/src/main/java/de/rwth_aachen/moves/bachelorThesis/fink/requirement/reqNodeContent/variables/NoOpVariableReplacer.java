package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;

import java.util.ArrayList;
import java.util.List;

public class NoOpVariableReplacer implements IVariableReplacer {
	@Override
	public IVariableWithAccessor getReplacement(IVariableWithAccessor variable, VariableTreeNode node) {
		return variable;
	}

	@Override
	public List<IVariableWithAccessor> getReplacements(List<IVariableWithAccessor> variables) {
		return new ArrayList<>(variables);
	}

	@Override
	public VariableReplacer.PreVarOperator getPreVarOperator() {
		return null;
	}

	@Override
	public boolean containsVariable(IVariableWithAccessor var) {
		return false;
	}

	@Override
	public boolean containsVariable(IVariable var) {
		return false;
	}

	@Override
	public boolean castTypes() {
		return false;
	}

}
