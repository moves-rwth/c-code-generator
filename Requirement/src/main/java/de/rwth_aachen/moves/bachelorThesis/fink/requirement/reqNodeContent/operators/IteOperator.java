package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.InvalidChildIdException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.ControlStructureOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class IteOperator extends ControlStructureOperator {
	public static final int CODE_ARGUMENT_COUNT      = 2;
	public static final int CONDITION_ARGUMENT_COUNT = 1;
	public static final int ARGUMENT_COUNT = CODE_ARGUMENT_COUNT + CONDITION_ARGUMENT_COUNT;

	static boolean useTernaryOperator = true;

	public IteOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("ite", children, isInOutput);
	}

	protected IteOperator(IteOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected IteOperator(IteOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		if (codeType == CodeType.CONDITION) {
			if (useTernaryOperator) {
				return debugFormat(getChildCode(0, usedVariables, context), "?", getChildCode(1, usedVariables, context), ":", getChildCode(2, usedVariables, context));
			} else {
				return debugFormat(debugFormat("!", getChildCode(0, usedVariables, context)), "&&", getChildCode(2, usedVariables, context), "||", getChildCode(1, usedVariables, context));
			}

		} else {
			StringArray result = new StringArray();
			result.add(debugComment() + "if (" + getChildCode(0, usedVariables, context).toStringProperty() + ") {");
			result.addIndented(getExecutionChildCode(1, usedVariables, context));
			result.add("} else {");
			result.addIndented(getExecutionChildCode(2, usedVariables, context));
			result.add("}");

			return result;
		}
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return Operators.getControlStructureOperators();
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
			result = new SimpleChildExpressionConditionerBuilder(DataType.INSTANCE_BOOL).setCanBeASimpleValue(false);
		} else {
			result = new SimpleChildExpressionConditionerBuilder(parentConditioner).setCanBeASimpleValue(true);
		}
		return result.setParentOperator(Operators.ITE).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		SimpleResultingExpressionConditioner conditionTrueExpressionConditioner =  children.get(1).getResultingExpressionConditioner(programContext);
		SimpleResultingExpressionConditioner conditionFalseExpressionConditioner = children.get(2).getResultingExpressionConditioner(programContext);
		return new SimpleResultingExpressionConditioner(
				conditionTrueExpressionConditioner.or(conditionFalseExpressionConditioner),
				(IIntegerRange) conditionTrueExpressionConditioner.getArrayIndexRange().or(conditionFalseExpressionConditioner.getRange()));
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new IteOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.INSTANCE_NONE;
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new IteOperator(this, variableReplacer);
	}

	public static void setUseTernaryOperator(boolean pUseTernaryOperator) {
		useTernaryOperator = pUseTernaryOperator;
	}
}
