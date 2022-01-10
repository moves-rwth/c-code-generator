package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.InvalidChildIdException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ArrayAccessOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 2;

	public ArrayAccessOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("arrayAccess", children, isInOutput);
	}

	protected ArrayAccessOperator(ArrayAccessOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected ArrayAccessOperator(ArrayAccessOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return debugFormatArrayAccess(getChildCode(0, usedVariables, context), getChildCode(1, usedVariables, context));
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.of(Operators.ARRAY_ACCESS);
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}


	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		if (childId >= ARGUMENT_COUNT) {
			throw new InvalidChildIdException("Child ID " + childId + " is invalid for this operator!");
		}

		// a[b]
		SimpleChildExpressionConditionerBuilder childExpressionConditionerBuilder;
		if (childId == 0) {
			// this is "a"
			childExpressionConditionerBuilder = new SimpleChildExpressionConditionerBuilder(parentConditioner);
		} else {
			// this is "b"
			childExpressionConditionerBuilder = new SimpleChildExpressionConditionerBuilder(dataTypeContext.integers());
			childExpressionConditionerBuilder.setRange(previousChildResultConditioners.get(0).getArrayIndexRange());
		}
		return childExpressionConditionerBuilder.setCanBeASimpleValue(true).setParentOperator(Operators.ARRAY_ACCESS).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		final SimpleExpressionConditioner childResultConditionerA = child(0).getResultingExpressionConditioner(programContext);
		return new SimpleResultingExpressionConditionerBuilder(childResultConditionerA).build();
	}

	@Override
	public boolean isConstant() {
		return child(0).isConstant() && child(1).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new ArrayAccessOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		VariableTreeNode vtn = (VariableTreeNode) this.getChildren().get(0);
		return DataType.doTypeWidening(vtn.getVariableWithAccessor().getDataType());
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new ArrayAccessOperator(this, variableReplacer);
	}
}
