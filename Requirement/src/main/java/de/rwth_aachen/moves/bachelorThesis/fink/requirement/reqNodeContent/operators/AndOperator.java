package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.InvalidChildIdException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IBooleanRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleBooleanRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class AndOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 2;

	public AndOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("&&", children, isInOutput);
	}

	protected AndOperator(AndOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected AndOperator(AndOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return debugFormat(getChildCode(0, usedVariables, context), "&&", getChildCode(1, usedVariables, context));
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		EnumSet<Operators> replacementOperators = EnumSet.noneOf(Operators.class);
		replacementOperators.addAll(Operators.logicalOperatorsList);
		replacementOperators.addAll(Operators.relationalOperatorsList);
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

		SimpleChildExpressionConditionerBuilder result = new SimpleChildExpressionConditionerBuilder(dataTypeContext.bool());
		IBooleanRange booleanRange = (IBooleanRange) parentConditioner.restrictToType(DataType.INSTANCE_BOOL).getRange();
		if ( ! booleanRange.canBeFalse()) {
			result.setRange(new SimpleBooleanRange(true, false));
		} else if ( ! booleanRange.canBeTrue()) {
			result.setRange(new SimpleBooleanRange(previousChildResultConditioners.isEmpty(), true));
		} else {
			result.setRange(new SimpleBooleanRange(true, true));
		}

		return result.setCanBeASimpleValue(false).setParentOperator(Operators.AND).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		final SimpleExpressionConditioner childResultConditionerA = child(0).getResultingExpressionConditioner(programContext);
		final SimpleExpressionConditioner childResultConditionerB = child(1).getResultingExpressionConditioner(programContext);
		IBooleanRange resultRangeA = (IBooleanRange) childResultConditionerA.getRange();
		IBooleanRange resultRangeB = (IBooleanRange) childResultConditionerB.getRange();
		SimpleBooleanRange simpleBooleanRange = new SimpleBooleanRange();
		simpleBooleanRange = simpleBooleanRange.setCanBeTrue(resultRangeA.canBeTrue() && resultRangeB.canBeTrue());
		simpleBooleanRange = simpleBooleanRange.setCanBeFalse(resultRangeA.canBeFalse() || resultRangeB.canBeFalse());
		return new SimpleResultingExpressionConditionerBuilder(DataType.INSTANCE_BOOL).setRange(simpleBooleanRange).build();
	}

	@Override
	public boolean isConstant() {
		return child(0).isConstant() && child(1).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new AndOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.INSTANCE_BOOL;
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new AndOperator(this, variableReplacer);
	}
}
