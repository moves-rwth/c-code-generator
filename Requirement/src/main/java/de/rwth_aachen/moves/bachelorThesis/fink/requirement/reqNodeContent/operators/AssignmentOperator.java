package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.RangeWrapsAroundException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.SimpleProgramContext;
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

public class AssignmentOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 2;

	public AssignmentOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("assignment", children, isInOutput);
	}

	protected AssignmentOperator(AssignmentOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected AssignmentOperator(AssignmentOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		if (codeType == CodeType.CONDITION) {
			final String typeName = "(" + getChildren().get(0).getDynamicReturnType(new SimpleProgramContext()).toCTypeName() + ")";
			return debugFormat(getChildCode(0, usedVariables, context), "==", debugFormat(typeName, getChildCode(1, usedVariables, context)));
		} else {
			return debugFormat(getChildCode(0, usedVariables, context), "=", getChildCode(1, usedVariables, context)).addToLastLine(";");
		}
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.noneOf(Operators.class);
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}


	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		throw new RuntimeException("This is currently unused and should never be called.");
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		IRange variableNodeRange = child(0).getResultingExpressionConditioner(programContext).getRange();
		final SimpleExpressionConditioner valueToAssign = child(1).getResultingExpressionConditioner(programContext);

		IRange resultRange = valueToAssign.getRange();
		DataType targetType = DataTypeContext.getSingle(variableNodeRange.getUnderlyingTypes());
		if (variableNodeRange instanceof IBooleanRange) {
			// Internally: Booleans cannot be cast.
			assert(valueToAssign.getRange() instanceof IBooleanRange);
		} else if (variableNodeRange instanceof IFloatingPointRange) {
			assert(valueToAssign.getRange() instanceof IFloatingPointRange);

			// We might need to cast downwards (double -> float)
			resultRange = resultRange.restrictToType(targetType);
		} else if (variableNodeRange instanceof IIntegerRange) {
			assert(valueToAssign.getRange() instanceof IIntegerRange);
			try {
				// Do integer truncation if necessary
				resultRange = ((SimpleIntegerRange) variableNodeRange).convertToDataTypeWithTruncation(targetType);
			} catch (RangeWrapsAroundException error) {
				throw new RuntimeException("Assignment results in a range we cant model -> We screwed up generation.");
			}
		} else {
			throw new RuntimeException("Something unexpected happened");
		}
		SimpleResultingExpressionConditionerBuilder resultBuilder = new SimpleResultingExpressionConditionerBuilder(targetType);
		resultBuilder.setRange(resultRange);
		return resultBuilder.build();
	}

	@Override
	public boolean isConstant() {
		return child(1).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new AssignmentOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return getChildren().get(0).getDynamicReturnType(programContext);
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new AssignmentOperator(this, variableReplacer);
	}
}
