package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.InvalidChildIdException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LastOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 1;

	public LastOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("last", children, isInOutput);
	}

	protected LastOperator(LastOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected LastOperator(LastOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}



	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		VariableTreeNode variableTreeNode = VariableTreeNode.findFirstVariableTreeNode(this);
		if (variableTreeNode == null) {
			throw new RuntimeException("Failed to find variable under last operator!");
		}

		IVariableWithAccessor var = variableTreeNode.getVariableWithAccessor();
		return new StringArray(var.getNameOfLastedVariable("1"));
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.of(Operators.LAST, Operators.ABS, Operators.MINUS_UNARY, Operators.PARENTHESIS, Operators.EMPTY);
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}


	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		if (childId >= ARGUMENT_COUNT) {
			throw new InvalidChildIdException("Child ID " + childId + " is invalid for this operator!");
		}

		// last(var a)
		return new SimpleChildExpressionConditionerBuilder(parentConditioner).setCanBeASimpleValue(true).setParentOperator(Operators.LAST).build();
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
		return new LastOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.doTypeWidening(getChildren().get(0).getDynamicReturnType(programContext));
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new LastOperator(this, variableReplacer);
	}

	@Override
	public void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context) {
		VariableCollector variableCollector = new VariableCollector(false, context);
		final FormulaTreeNode leftChild = child(0);

		// Get variables in left subtree (what is read)
		leftChild.accept(variableCollector);
		if (variableCollector.getUsedVariables().size() != 1) {
			throw new RuntimeException("Failed to identify last'ed variable! Found " + variableCollector.getUsedVariables().size() + " variables.");
		}
		final IVariable v = variableCollector.getUsedVariables().iterator().next();
		long maxValue = 1L;
		if (lastIVariables.containsKey(v)) {
			maxValue = Math.max(maxValue, lastIVariables.get(v));
		}
		lastIVariables.put(v, maxValue);
	}
}
