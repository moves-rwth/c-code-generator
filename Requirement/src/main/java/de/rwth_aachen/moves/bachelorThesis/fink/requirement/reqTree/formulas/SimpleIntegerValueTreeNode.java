package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;

import java.util.Map;
import java.util.Set;

public class SimpleIntegerValueTreeNode extends FormulaTreeNode implements ISimpleValueTreeNode {
	private final DataType returnType;
	private final long value;

	public SimpleIntegerValueTreeNode(DataType returnType, long value, boolean isInOutput) {
		super(isInOutput);
		if (!returnType.isInteger()) {
			throw new RuntimeException("Invalid type for integer constant: " + returnType);
		}
		this.returnType = returnType;
		this.value = value;
	}

	public SimpleIntegerValueTreeNode(SimpleIntegerValueTreeNode node) {
		super(node);
		this.returnType = node.returnType;
		this.value = node.value;
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		String result = Long.toString(value);
		if (DataType.equal(returnType, DataType.getSignedInt())) {
			// Default, no postfix needed
		} else if (DataType.equal(returnType, DataType.getUnsignedInt())) {
			result += "u";
		} else if (DataType.equal(returnType, DataType.getSignedLong())) {
			result += "l";
		} else if (DataType.equal(returnType, DataType.getUnsignedLong())) {
			result += "ul";
		} else {
			throw new RuntimeException("Unexpected underlying type " + returnType);
		}
		return new StringArray(result);
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		if (children.isEmpty()) {
			return new SimpleIntegerValueTreeNode(this);
		}
		throw new RuntimeException("Can not replace children on SimpleIntegerValueTreeNode!");
	}

	@Override
	public StringArray toLastICode(Variable variable, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		StringArray str = new StringArray("last_" + value + "_" + variable.getName().trim());
		return str;
	}

	@Override
	public void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context) {

	}

	@Override
	public ImmutableList<FormulaTreeNode> getChildren() {
		return ImmutableList.of();
	}

	public long getValue() {
		return value;
	}

	@Override
	public SimpleIntegerValueTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new SimpleIntegerValueTreeNode(this);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return returnType;
	}


	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		SimpleResultingExpressionConditionerBuilder result = new SimpleResultingExpressionConditionerBuilder(returnType);

		result.setRange(new SimpleIntegerRange(DataTypeContext.makeSet(returnType), value, value, Set.of()));

		return result.build();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleIntegerValueTreeNode) {
			SimpleIntegerValueTreeNode otherB = (SimpleIntegerValueTreeNode) other;
			return this.getValue() == otherB.getValue();
		}
		return false;
	}

}
