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

public class WhileOperator extends ControlStructureOperator {
	public static final int CODE_ARGUMENT_COUNT      = 1;
	public static final int CONDITION_ARGUMENT_COUNT = 1;
	public static final int ARGUMENT_COUNT = CODE_ARGUMENT_COUNT + CONDITION_ARGUMENT_COUNT;

	public WhileOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("for", children, isInOutput);
		throw new RuntimeException("should be unused for now!");
	}

	protected WhileOperator(ControlStructureOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected WhileOperator(ControlStructureOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		StringArray result = new StringArray();
		if (codeType == CodeType.CONDITION) {
			throw new RuntimeException("not implemented");
		} else {
			result.add("for (");
			result.add(getChildCode(0, usedVariables, context));
			result.add(getChildCode(1, usedVariables, context));
			result.add("i++");
			result.add(") {");
			result.addIndented(getExecutionChildCode(2, usedVariables, context), 1);
			result.add("}");
		}
		return result;
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
		return result.setParentOperator(Operators.WHILE).build();
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new WhileOperator(this, variableReplacer);
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new WhileOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.INSTANCE_NONE;
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		throw new RuntimeException("Not applicable.");
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return Operators.getControlStructureOperators();
	}

	@Override
	public boolean isIntegerSafe() {
		return false;
	}
}
