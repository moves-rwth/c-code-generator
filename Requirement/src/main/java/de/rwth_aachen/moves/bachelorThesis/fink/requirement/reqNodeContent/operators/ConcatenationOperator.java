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
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.ControlStructureOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public class ConcatenationOperator extends ControlStructureOperator {
	public static final int CODE_ARGUMENT_COUNT      = 2;
	public static final int CONDITION_ARGUMENT_COUNT = 0;
	public static final int ARGUMENT_COUNT = CODE_ARGUMENT_COUNT + CONDITION_ARGUMENT_COUNT;

	public ConcatenationOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("concat", children, isInOutput);
	}

	protected ConcatenationOperator(ConcatenationOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected ConcatenationOperator(ConcatenationOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		if (codeType == CodeType.CONDITION) {
			return debugFormat(getChildCode(0, usedVariables, context), "&&", getChildCode(1, usedVariables, context));
		} else {
			StringArray result = new StringArray();
			result.add(getExecutionChildCode(0, usedVariables, context));
			result.add(getExecutionChildCode(1, usedVariables, context));
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

		return new SimpleChildExpressionConditionerBuilder(parentConditioner).setCanBeASimpleValue(true).setParentOperator(Operators.CONCATENATION).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) {
		throw new RuntimeException("Not applicable.");
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new ConcatenationOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.INSTANCE_NONE;
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new ConcatenationOperator(this, variableReplacer);
	}
}
