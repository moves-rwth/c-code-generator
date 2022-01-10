package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.InvalidChildIdException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class BitShiftLeftOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 2;

	public BitShiftLeftOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("<<", children, isInOutput);
	}

	protected BitShiftLeftOperator(BitShiftLeftOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected BitShiftLeftOperator(BitShiftLeftOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return debugFormat(getChildCode(0, usedVariables, context), "<<", getChildCode(1, usedVariables, context));
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

		if (parentConditioner.getRange().isRestricted()) {
			System.out.println("Warning: Constrained bitShift expressions do not work yet, this will crash...");
			throw new UnsatisfiableConstraintsException("Constrained bitShift expressions do not work yet");
		}

		// shift a by b bits to the left
		SimpleChildExpressionConditionerBuilder result;
		if (childId == 0) {
			// a
			result = new SimpleChildExpressionConditionerBuilder(parentConditioner);
			result.setPossibleReturnTypes(DataTypeContext.removeBool(DataTypeContext.removeFloatingPoint(result.getPossibleReturnTypes())));
		} else {
			// b
			result = parentConditioner.bitShift(false, parentConditioner, previousChildResultConditioners, dataTypeContext);
		}
		return result.setCanBeASimpleValue(true).setParentOperator(Operators.BIT_SHIFT_LEFT).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		IRange originalRange = child(0).getResultingExpressionConditioner(programContext).getRange();
		DataType resultType = DataType.doTypeWidening(originalRange);
		IRange adjustedRange = originalRange.restrictToType(resultType);
		return new SimpleResultingExpressionConditionerBuilder(resultType).setRange(adjustedRange.removeRestrictions()).build();
	}

	@Override
	public boolean isConstant() {
		return child(0).isConstant() && child(1).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> newChildren) {
		return new BitShiftLeftOperator(this, newChildren);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.doTypeWidening(getChildren().get(0).getDynamicReturnType(programContext));
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new BitShiftLeftOperator(this, variableReplacer);
	}
}
