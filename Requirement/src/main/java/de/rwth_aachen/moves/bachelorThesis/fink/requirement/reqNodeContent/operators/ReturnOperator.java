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
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IFloatingPointRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleFloatingPointRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ReturnOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 1;

	public ReturnOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("return", children, isInOutput);
	}

	protected ReturnOperator(ReturnOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected ReturnOperator(ReturnOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}



	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return debugFormat("return", getChildCode(0, usedVariables, context)).addToLastLine(";");
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.noneOf(Operators.class);	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}


	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		if (childId >= ARGUMENT_COUNT) {
			throw new InvalidChildIdException("Child ID " + childId + " is invalid for this operator!");
		}

		SimpleChildExpressionConditionerBuilder result = new SimpleChildExpressionConditionerBuilder(parentConditioner);
		if (parentConditioner.getRange() instanceof IFloatingPointRange) {
			result.setRange(SimpleFloatingPointRange.childAbs((IFloatingPointRange) parentConditioner.getRange()));
		} else {
			result.setRange(SimpleIntegerRange.childAbs((IIntegerRange) parentConditioner.getRange()));
		}

		return result.setCanBeASimpleValue(true).setParentOperator(Operators.RETURN).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		final SimpleExpressionConditioner childResultConditioner = child(0).getResultingExpressionConditioner(programContext);
		return SimpleResultingExpressionConditioner.combineAbsExpression(childResultConditioner);
	}

	@Override
	public boolean isConstant() {
		return child(0).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new ReturnOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return this.getChildren().get(0).getDynamicReturnType(programContext);
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new ReturnOperator(this, variableReplacer);
	}
}
