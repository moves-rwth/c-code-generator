package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.InvalidChildIdException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.RangeWrapsAroundException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class PrefixDecrementOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 1;

	public PrefixDecrementOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("prefixDecrement", children, isInOutput);
	}

	protected PrefixDecrementOperator(PrefixDecrementOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected PrefixDecrementOperator(PrefixDecrementOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return debugFormat("--", getChildCode(0, usedVariables, context));
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.of(Operators.ABS, Operators.MINUS_UNARY, Operators.PARENTHESIS, Operators.EMPTY);
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}


	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		if (childId >= ARGUMENT_COUNT) {
			throw new InvalidChildIdException("Child ID " + childId + " is invalid for this operator!");
		}

		SimpleChildExpressionConditionerBuilder result = parentConditioner.mathExpr(PrefixDecrementOperator.class, parentConditioner, previousChildResultConditioners);
		return result.setCanBeASimpleValue(true).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		final SimpleExpressionConditioner childResultConditioner = child(0).getResultingExpressionConditioner(programContext);
		IRange range = childResultConditioner.getRange();
		IRange typeWidenedRange = range.restrictToType(DataType.doTypeWidening(range));
		SimpleResultingExpressionConditionerBuilder result = new SimpleResultingExpressionConditionerBuilder(typeWidenedRange.getUnderlyingTypes());
		if (typeWidenedRange instanceof IFloatingPointRange) {
			result.setRange(SimpleFloatingPointRange.unaryMinus((IFloatingPointRange) typeWidenedRange));
		} else {
			try {
				result.setRange(SimpleIntegerRange.unaryMinus((IIntegerRange) typeWidenedRange));
			} catch (RangeWrapsAroundException error) {
				throw new UnsatisfiableConstraintsException("Would result in split range, aborting...");
			}
		}
		return result.build();
	}

	@Override
	public boolean isConstant() {
		return child(0).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new PrefixDecrementOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		// NOTE(Felix):
		// i8 variableWithTruncation = -128;
		// i32 testVariable = --variableWithTruncation; -> testVariable = 127
		// Therefore no type widening here!
		return getChildren().get(0).getDynamicReturnType(programContext);
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new PrefixDecrementOperator(this, variableReplacer);
	}
}
