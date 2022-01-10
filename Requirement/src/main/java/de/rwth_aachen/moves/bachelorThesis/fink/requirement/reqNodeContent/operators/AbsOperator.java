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

public class AbsOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 1;

	public AbsOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("abs", children, isInOutput);
	}

	protected AbsOperator(AbsOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected AbsOperator(AbsOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		// Needs wrap, otherwise C preprocessor will not convert macro
		return debugFormat("abs", getChildCode(0, usedVariables, context), true);
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.of(Operators.ABS, Operators.MINUS_UNARY, Operators.PARENTHESIS, Operators.EMPTY);
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

		return result.setCanBeASimpleValue(true).setParentOperator(Operators.ABS).build();
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
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> newChildren) {
		return new AbsOperator(this, newChildren);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.doTypeWidening(this.getChildren().get(0).getDynamicReturnType(programContext));
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new AbsOperator(this, variableReplacer);
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}
}
