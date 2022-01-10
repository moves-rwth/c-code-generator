package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IPointerType;
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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DereferenceOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 1;

	public DereferenceOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("dereference", children, isInOutput);
	}

	protected DereferenceOperator(DereferenceOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected DereferenceOperator(DereferenceOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return debugFormat("*", getChildCode(0, usedVariables, context));
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.of(Operators.DEREFERENCE);
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}


	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		if (childId >= ARGUMENT_COUNT) {
			throw new InvalidChildIdException("Child ID " + childId + " is invalid for this operator!");
		}

		// *a
		// this is "a"
		Set<DataType> types = new HashSet<>();
		for (DataType type : parentConditioner.getPossibleReturnTypes()) {
			final DataType pointeredType = dataTypeContext.addPointerType(type);
			types.add(pointeredType);
		}

		return new SimpleChildExpressionConditionerBuilder(types).setCanBeASimpleValue(false).setParentOperator(Operators.DEREFERENCE).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		final SimpleExpressionConditioner childResultConditionerA = child(0).getResultingExpressionConditioner(programContext);
		Set<DataType> types = new HashSet<>();
		for (DataType type : childResultConditionerA.getPossibleReturnTypes()) {
			final IPointerType pointerType = (IPointerType) type;
			types.add(pointerType.getBaseType());
		}
		SimpleResultingExpressionConditionerBuilder result = new SimpleResultingExpressionConditionerBuilder(types);
		return result.build();
	}

	@Override
	public boolean isConstant() {
		return child(0).isConstant();
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new DereferenceOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		final DataType childType = this.getChildren().get(0).getDynamicReturnType(programContext);
		final IPointerType pointerType = (IPointerType) childType;
		return DataType.doTypeWidening(pointerType.getBaseType());
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new DereferenceOperator(this, variableReplacer);
	}
}
