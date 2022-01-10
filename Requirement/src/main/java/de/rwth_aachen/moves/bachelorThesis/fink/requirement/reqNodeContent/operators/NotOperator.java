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

public class NotOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 1;

	public NotOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("!", children, isInOutput);
	}

	protected NotOperator(NotOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected NotOperator(NotOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}



	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return debugFormat("!", getChildCode(0, usedVariables, context));
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.of(Operators.ABS, Operators.MINUS_UNARY, Operators.PARENTHESIS, Operators.EMPTY, Operators.NOT);
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
		if (!booleanRange.canBeFalse()) {
			result.setRange(new SimpleBooleanRange(false, true));
		} else {
			result.setRange(new SimpleBooleanRange(true, booleanRange.canBeTrue()));
		}

		return result.setCanBeASimpleValue(true).setParentOperator(Operators.NOT).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		final SimpleExpressionConditioner childResultConditionerA = child(0).getResultingExpressionConditioner(programContext);
		SimpleBooleanRange simpleBooleanRange = new SimpleBooleanRange();
		IBooleanRange resultRangeA = (IBooleanRange) childResultConditionerA.getRange();
		simpleBooleanRange = simpleBooleanRange.setCanBeTrue(resultRangeA.canBeFalse());
		simpleBooleanRange = simpleBooleanRange.setCanBeFalse(resultRangeA.canBeTrue());
		return new SimpleResultingExpressionConditionerBuilder(DataType.INSTANCE_BOOL).setRange(simpleBooleanRange).build();
	}

	@Override
	public boolean isConstant() {
		return child(0).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new NotOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.INSTANCE_BOOL;
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new NotOperator(this, variableReplacer);
	}
}
