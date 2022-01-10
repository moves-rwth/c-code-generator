package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;

public abstract class ControlStructureOperator extends OperatorTreeNode {

	private SimpleResultingExpressionConditioner mCombinedOutputConditioner;

	public ControlStructureOperator(String name, ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super(name, children, isInOutput);
	}

	protected ControlStructureOperator(ControlStructureOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected ControlStructureOperator(ControlStructureOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	public SimpleResultingExpressionConditioner getCombinedOutputConditioner() {
		return mCombinedOutputConditioner;
	}

	public void setCombinedOutputConditioner(SimpleResultingExpressionConditioner combinedOutputConditioner) {
		this.mCombinedOutputConditioner = combinedOutputConditioner;
	}
}
