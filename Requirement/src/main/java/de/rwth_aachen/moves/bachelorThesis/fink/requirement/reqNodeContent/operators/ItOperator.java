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

public class ItOperator extends ControlStructureOperator {
	public static final int CODE_ARGUMENT_COUNT      = 1;
	public static final int CONDITION_ARGUMENT_COUNT = 1;
	public static final int ARGUMENT_COUNT = CODE_ARGUMENT_COUNT + CONDITION_ARGUMENT_COUNT;


	static boolean useTernaryOperator = true;

	public ItOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("it", children, isInOutput);
	}

	protected ItOperator(ItOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected ItOperator(ItOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		if (codeType == CodeType.CONDITION) {
			if (useTernaryOperator) {
				return debugFormat(getChildCode(0, usedVariables, context), "?", getChildCode(1, usedVariables, context), ":", new StringArray("1"));
			} else {
				return debugFormat(debugFormat("!", getChildCode(0, usedVariables, context)), "||", getChildCode(1, usedVariables, context));
			}
		} else {
			StringArray result = new StringArray();
			result.add(debugComment() + "if (" + getChildCode(0, usedVariables, context).toStringProperty() + ") {");
			result.addIndented(getExecutionChildCode(1, usedVariables, context));
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
		return result.setParentOperator(Operators.IT).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		return children.get(1).getResultingExpressionConditioner(programContext);
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new ItOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.INSTANCE_NONE;
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new ItOperator(this, variableReplacer);
	}

	public static void setUseTernaryOperator(boolean pUseTernaryOperator) {
		useTernaryOperator = pUseTernaryOperator;
	}
}
