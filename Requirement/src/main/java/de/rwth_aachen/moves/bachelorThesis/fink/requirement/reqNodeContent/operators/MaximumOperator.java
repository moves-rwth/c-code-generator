package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.InvalidChildIdException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class MaximumOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 2;

	public MaximumOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("max", children, isInOutput);
	}

	protected MaximumOperator(MaximumOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected MaximumOperator(MaximumOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return debugFormat("max", debugFormat(getChildCode(0, usedVariables, context), ",", getChildCode(1, usedVariables, context)));
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		EnumSet<Operators> replacementOperators = EnumSet.noneOf(Operators.class);
		replacementOperators.addAll(Operators.functionOperatorsList);
		replacementOperators.addAll(Operators.arithmeticOperatorsList);
		return replacementOperators;
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}


	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		if (childId >= ARGUMENT_COUNT) {
			throw new InvalidChildIdException("Child ID " + childId + " is invalid for this operator!");
		}

		SimpleChildExpressionConditionerBuilder result = parentConditioner.mathExpr(MaximumOperator.class, parentConditioner, previousChildResultConditioners);
		return result.setCanBeASimpleValue(true).setParentOperator(Operators.MAXIMUM).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		final SimpleExpressionConditioner childResultConditionerA = child(0).getResultingExpressionConditioner(programContext);
		final SimpleExpressionConditioner childResultConditionerB = child(1).getResultingExpressionConditioner(programContext);
		return SimpleResultingExpressionConditioner.combineMathExpression(MaximumOperator.class, childResultConditionerA, childResultConditionerB);
	}

	@Override
	public boolean isConstant() {
		return child(0).isConstant() && child(1).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new MaximumOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		final DataType a = getChildren().get(0).getDynamicReturnType(programContext);
		final DataType b = getChildren().get(1).getDynamicReturnType(programContext);
		return DataType.doTypeWidening(a, b);
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new MaximumOperator(this, variableReplacer);
	}
}
