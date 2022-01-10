package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;

public interface IOperatorTreeNodeBuilder {
	OperatorTreeNode create(ImmutableList<FormulaTreeNode> children, boolean isInOutput);
}
