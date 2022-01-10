package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.patterns;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;

import java.util.Map;
import java.util.Set;

public class InvariantPatternTreeNode extends PatternTreeNode {

	private final FormulaTreeNode child;

	public InvariantPatternTreeNode(FormulaTreeNode child) {
		this.child = child;
	}

	public FormulaTreeNode getChild() {
		return child;
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return child.toCode(codeType, usedVariables, context);
	}

	@Override
	public void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context) {
		child.getLastVariablesAndDepths(allVariables, lastIVariables, context);
	}

	@Override
	public ImmutableList<FormulaTreeNode> getChildren() {
		return ImmutableList.of(child);
	}

	@Override
	public CodeTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		if (children.size() != 1) {
			throw new RuntimeException("InvariantPattern has exactly one child, not " + children.size() + "!");
		}
		return new InvariantPatternTreeNode(children.get(0));
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		final FormulaTreeNode childCopy = (FormulaTreeNode) child.copyTree(variableReplacer);
		return new InvariantPatternTreeNode(childCopy);
	}

}
