package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;

public abstract class ComparisonOperator extends OperatorTreeNode {
	public ComparisonOperator(String name, ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super(name, children, isInOutput);
	}

	protected ComparisonOperator(OperatorTreeNode node, ImmutableList<FormulaTreeNode> children) {
		super(node, children);
	}

	protected ComparisonOperator(OperatorTreeNode node, IVariableReplacer variableReplacer) {
		super(node, variableReplacer);
	}
}
