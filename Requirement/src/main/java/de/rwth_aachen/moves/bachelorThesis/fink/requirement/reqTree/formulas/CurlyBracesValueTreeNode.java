package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;

import java.util.Map;
import java.util.Set;

public class CurlyBracesValueTreeNode extends FormulaTreeNode implements ISimpleValueTreeNode {
	private final DataType variableType;
	private final ImmutableList<FormulaTreeNode> children;

	public CurlyBracesValueTreeNode(DataType targetVariableType, ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super(isInOutput);
		this.variableType = targetVariableType;
		this.children = children;
	}

	public CurlyBracesValueTreeNode(CurlyBracesValueTreeNode node) {
		super(node);
		this.variableType = node.variableType;
		this.children = node.children;
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		StringArray result = new StringArray();
		result.add("{");
		boolean isFirst = true;
		for (FormulaTreeNode child : children) {
			if (!isFirst) {
				result.addToLastLine(", ");
			}
			isFirst = false;
			result.addIndented(child.toCode(codeType, usedVariables, context));
		}
		result.add("}");
		return result;
	}

	@Override
	public boolean isConstant() {
		for (FormulaTreeNode child : children) {
			if (!child.isConstant()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		if (children.isEmpty()) {
			return new CurlyBracesValueTreeNode(this);
		}
		throw new RuntimeException("Can not replace children on CurlyBracesValueTreeNode!");
	}

	@Override
	public StringArray toLastICode(Variable variable, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		throw new RuntimeException("This should not be used.");
	}

	@Override
	public void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context) {

	}

	@Override
	public ImmutableList<FormulaTreeNode> getChildren() {
		return children;
	}

	@Override
	public CurlyBracesValueTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new CurlyBracesValueTreeNode(this);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return variableType;
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		SimpleResultingExpressionConditionerBuilder result = new SimpleResultingExpressionConditionerBuilder(variableType);
		return result.build();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CurlyBracesValueTreeNode) {
			CurlyBracesValueTreeNode otherB = (CurlyBracesValueTreeNode) other;
			return (variableType == otherB.variableType) && (this.children == otherB.children);
		}
		return false;
	}

}
