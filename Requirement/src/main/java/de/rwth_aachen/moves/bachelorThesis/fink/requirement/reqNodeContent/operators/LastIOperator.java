package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.InvalidChildIdException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.SimpleIntegerValueTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LastIOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 2;

	public LastIOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("last_i(param, steps)", children, isInOutput);
	}

	protected LastIOperator(LastIOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected LastIOperator(LastIOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		VariableTreeNode variableTreeNode = VariableTreeNode.findFirstVariableTreeNode(this);
		if (variableTreeNode == null) {
			throw new RuntimeException("Failed to find variable under last operator!");
		}
		IVariableWithAccessor var = variableTreeNode.getVariableWithAccessor();
		FormulaTreeNode i = getChildren().get(1);
		if (i instanceof SimpleIntegerValueTreeNode) {
			SimpleIntegerValueTreeNode simpleIntegerValueTreeNode = (SimpleIntegerValueTreeNode) i;
			return new StringArray(var.getNameOfLastedVariable(String.valueOf(simpleIntegerValueTreeNode.getValue())));
		}

		throw new RuntimeException("Unsupported type for last_i i-value: " + i.getClass().getCanonicalName());
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.of(Operators.LAST_I, Operators.ABS, Operators.DIVISION, Operators.MAXIMUM, Operators.MINIMUM, Operators.MINUS_BINARY, Operators.MINUS_UNARY, Operators.PLUS, Operators.TIMES);
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}


	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		if (childId >= ARGUMENT_COUNT) {
			throw new InvalidChildIdException("Child ID " + childId + " is invalid for this operator!");
		}

		SimpleChildExpressionConditionerBuilder result;
		// last_i(var a, steps b)
		if (childId == 0) {
			// a
			result = new SimpleChildExpressionConditionerBuilder(parentConditioner).setCanBeASimpleValue(true);
		} else {
			result = new SimpleChildExpressionConditionerBuilder(dataTypeContext.integers());
			result.setRange(new SimpleIntegerRange(dataTypeContext.integers(), 0L, 32L, Set.of()));
			result.setCanBeASimpleValue(true);
		}
		return result.setParentOperator(Operators.LAST_I).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		final SimpleExpressionConditioner childResultConditionerA = child(0).getResultingExpressionConditioner(programContext);
		return new SimpleResultingExpressionConditionerBuilder(childResultConditionerA).build();
	}

	@Override
	public boolean isConstant() {
		return child(0).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new LastIOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.doTypeWidening(getChildren().get(0).getDynamicReturnType(programContext));
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new LastIOperator(this, variableReplacer);
	}

	@Override
	public void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context) {
		VariableCollector variableCollector = new VariableCollector(false, context);

		// Get variables in left subtree (what is read)
		final FormulaTreeNode leftChild = child(0);
		leftChild.accept(variableCollector);
		if (variableCollector.getUsedVariables().size() != 1) {
			throw new RuntimeException("Failed to identify last'ed variable! Found " + variableCollector.getUsedVariables().size() + " variables.");
		}
		final IVariable v = variableCollector.getUsedVariables().iterator().next();

		// Get maximum value of the right subtree (value of i)
		final FormulaTreeNode rightChild = child(1);
		long maxValue = 1L;

		variableCollector.clear();
		rightChild.accept(variableCollector);
		if (variableCollector.getUsedVariables().size() == 0 && variableCollector.getConstantNodes().size() == 1) {
			if (variableCollector.getIntegerConstants().size() == 1) {
				maxValue = variableCollector.getIntegerConstants().iterator().next();
			} else {
				throw new RuntimeException("Can not deal with non-integer constant in last_i!");
			}
		} else {
			throw new RuntimeException("Can not deal with non-trivial expression in last_i!");
		}

		if (lastIVariables.containsKey(v)) {
			maxValue = Math.max(maxValue, lastIVariables.get(v));
		}
		lastIVariables.put(v, maxValue);
	}
}
