package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.NoOpVariableReplacer;

import java.util.Map;
import java.util.Set;

public class VariableTreeNode extends FormulaTreeNode {
	private final IVariableWithAccessor variableWithAccessor;

	public VariableTreeNode(IVariableWithAccessor variableWithAccessor, boolean isInOutput) {
		super(isInOutput);
		assert (variableWithAccessor != null);
		this.variableWithAccessor = variableWithAccessor;
	}


	public VariableTreeNode(VariableTreeNode node, IVariableReplacer variableReplacer) {
		super(node);
		this.variableWithAccessor = variableReplacer.getReplacement(node.variableWithAccessor, node);
		assert (variableWithAccessor != null);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return new StringArray(variableWithAccessor.toString(usedVariables));
	}

	@Override
	public IVariable tryToReturnOutputVariable() {
		return variableWithAccessor.getVariable();
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		if (children.isEmpty()) {
			return copyTree(new NoOpVariableReplacer());
		}
		throw new RuntimeException("Can not replace children on VariableTreeNode!");
	}

	@Override
	public StringArray toLastICode(Variable variable, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		variable.setLastIHasToBeArray(true);
		return new StringArray("last_i_" + variable.getName().trim() + "[" + variable.toString() + "]");
	}

	@Override
	public ImmutableList<FormulaTreeNode> getChildren() {
		return ImmutableList.of();
	}

	@Override
	public void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context) {
		// This is handled by last and last_i
	}

	public IVariableWithAccessor getVariableWithAccessor() {
		return variableWithAccessor;
	}


	@Override
	public VariableTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new VariableTreeNode(this, variableReplacer);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return variableWithAccessor.getDataType();
	}


	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) {
		SimpleResultingExpressionConditionerBuilder result = new SimpleResultingExpressionConditionerBuilder(variableWithAccessor.getDataType());

		result.setRange(programContext.getCurrentlyDefinedVariables().getVariableConditioner(variableWithAccessor.getVariable()).getRange());

		return result.build();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof VariableTreeNode) {
			VariableTreeNode otherB = (VariableTreeNode) other;
			return this.getVariableWithAccessor().equals(otherB.getVariableWithAccessor());
		}
		return false;
	}

	public static VariableTreeNode findFirstVariableTreeNode(FormulaTreeNode node) {
		if (node instanceof VariableTreeNode) {
			return (VariableTreeNode) node;
		}
		VariableTreeNode result = null;
		for (FormulaTreeNode c: node.getChildren()) {
			result = findFirstVariableTreeNode(c);
			if (result != null) break;
		}
		return result;
	}

}
