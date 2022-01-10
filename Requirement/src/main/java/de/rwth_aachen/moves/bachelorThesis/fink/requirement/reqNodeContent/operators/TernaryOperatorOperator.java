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
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleBooleanRange;
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

public class TernaryOperatorOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 3;

	public TernaryOperatorOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("ternary_operator", children, isInOutput);
	}

	protected TernaryOperatorOperator(TernaryOperatorOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected TernaryOperatorOperator(TernaryOperatorOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return debugFormat(getChildCode(0, usedVariables, context), "?", getChildCode(1, usedVariables, context), ":", getChildCode(2, usedVariables, context));
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

		SimpleChildExpressionConditionerBuilder result;
		if (childId == 0) {
			result = new SimpleChildExpressionConditionerBuilder(dataTypeContext.bool()).setCanBeASimpleValue(false);
		} else {
			result = new SimpleChildExpressionConditionerBuilder(parentConditioner).setCanBeASimpleValue(true);
		}
		return result.setParentOperator(Operators.TERNARY_OPERATOR).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		final SimpleExpressionConditioner childResultConditionerA = child(0).getResultingExpressionConditioner(programContext);
		final SimpleExpressionConditioner childResultConditionerB = child(1).getResultingExpressionConditioner(programContext);
		final SimpleExpressionConditioner childResultConditionerC = child(2).getResultingExpressionConditioner(programContext);

		IBooleanRange booleanRange = (IBooleanRange) childResultConditionerA.getRange();
		if (!booleanRange.canBeFalse()) {
			return new SimpleResultingExpressionConditionerBuilder(childResultConditionerB).build();
		} else if (!booleanRange.canBeTrue()) {
			return new SimpleResultingExpressionConditionerBuilder(childResultConditionerC).build();
		}

		SimpleResultingExpressionConditionerBuilder result = new SimpleResultingExpressionConditionerBuilder(childResultConditionerA.getPossibleReturnTypes());
		result.addPossibleReturnTypes(childResultConditionerB.getPossibleReturnTypes());

		if (childResultConditionerA.getRange() instanceof IBooleanRange) {
			result.setRange(SimpleBooleanRange.ternary((IBooleanRange) childResultConditionerA.getRange(), (IBooleanRange) childResultConditionerB.getRange()));
		} else {
			result.setRange(SimpleIntegerRange.ternary((IIntegerRange) childResultConditionerA.getRange(), (IIntegerRange) childResultConditionerB.getRange()));
		}

		return result.build();
	}

	@Override
	public boolean isConstant() {
		return child(0).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new TernaryOperatorOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		final DataType a = getChildren().get(0).getDynamicReturnType(programContext);
		final DataType b = getChildren().get(1).getDynamicReturnType(programContext);
		return DataType.doTypeWidening(a, b);
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new TernaryOperatorOperator(this, variableReplacer);
	}
}
