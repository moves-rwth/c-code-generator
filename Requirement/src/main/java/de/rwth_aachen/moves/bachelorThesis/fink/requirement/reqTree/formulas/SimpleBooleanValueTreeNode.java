package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleBooleanRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;

import java.util.Map;
import java.util.Set;

public class SimpleBooleanValueTreeNode extends FormulaTreeNode implements ISimpleValueTreeNode {
	private final boolean value;

	public SimpleBooleanValueTreeNode(boolean value, boolean isInOutput) {
		super(isInOutput);
		this.value = value;
	}

	public SimpleBooleanValueTreeNode(SimpleBooleanValueTreeNode node) {
		super(node);
		this.value = node.value;
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return new StringArray(value ? "1" : "0");
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		if (children.isEmpty()) {
			return new SimpleBooleanValueTreeNode(this);
		}
		throw new RuntimeException("Can not replace children on SimpleBooleanValueTreeNode!");
	}

	@Override
	public StringArray toLastICode(Variable variable, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		StringArray str = new StringArray("last_" + (value ? "1" : "0") + "_" + variable.getName().trim());
		return str;
	}

	@Override
	public void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context) {

	}

	@Override
	public ImmutableList<FormulaTreeNode> getChildren() {
		return ImmutableList.of();
	}

	public boolean getValue() {
		return value;
	}

	@Override
	public SimpleBooleanValueTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new SimpleBooleanValueTreeNode(this);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.INSTANCE_BOOL;
	}


	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) {
		SimpleResultingExpressionConditionerBuilder result = new SimpleResultingExpressionConditionerBuilder(programContext.getCurrentlyDefinedTypes().bool());

		result.setRange(new SimpleBooleanRange(value, !value));

		return result.build();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleBooleanValueTreeNode) {
			SimpleBooleanValueTreeNode otherB = (SimpleBooleanValueTreeNode) other;
			return this.getValue() == otherB.getValue();
		}
		return false;
	}

}
