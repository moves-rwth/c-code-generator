package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleFloatingPointRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;

import java.util.Map;
import java.util.Set;

public class SimpleFloatingPointValueTreeNode extends FormulaTreeNode implements ISimpleValueTreeNode {
	private final DataType returnType;
	private final double value;

	public SimpleFloatingPointValueTreeNode(DataType returnType, double value, boolean isInOutput) {
		super(isInOutput);
		if (!returnType.isFloatingPoint()) {
			throw new RuntimeException("Wrong type for value!");
		}
		this.returnType = returnType;
		this.value = value;
	}

	public SimpleFloatingPointValueTreeNode(SimpleFloatingPointValueTreeNode node) {
		super(node);
		this.returnType = node.returnType;
		this.value = node.value;
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		String result = Double.toString(value);
		if (DataType.equal(returnType, DataType.INSTANCE_FLOAT)) {
			result += "f";
		} else if (DataType.equal(returnType, DataType.INSTANCE_DOUBLE)) {
			// no suffix needed
		} else {
			throw new RuntimeException("Invalid underlying type; did someone implement long floats?");
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
			return new SimpleFloatingPointValueTreeNode(this);
		}
		throw new RuntimeException("Can not replace children on SimpleFloatingPointValueTreeNode!");
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

	public double getValue() {
		return value;
	}

	@Override
	public SimpleFloatingPointValueTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new SimpleFloatingPointValueTreeNode(this);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return returnType;
	}


	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		SimpleResultingExpressionConditionerBuilder result = new SimpleResultingExpressionConditionerBuilder(returnType);

		if (returnType.getBitCount() <= 32) { // FLOAT
			final float v = (float) value;
			result.setRange(new SimpleFloatingPointRange(DataTypeContext.makeSet(returnType), v, v, Set.of()));
		} else { // Double
			final double v = value;
			result.setRange(new SimpleFloatingPointRange(DataTypeContext.makeSet(returnType), v, v, Set.of()));
		}

		return result.build();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleFloatingPointValueTreeNode) {
			SimpleFloatingPointValueTreeNode otherB = (SimpleFloatingPointValueTreeNode) other;
			return this.getValue() == otherB.getValue();
		}
		return false;
	}

}
