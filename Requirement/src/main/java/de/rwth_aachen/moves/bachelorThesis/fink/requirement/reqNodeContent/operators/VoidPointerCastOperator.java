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

public class VoidPointerCastOperator extends OperatorTreeNode {
	public static final int ARGUMENT_COUNT = 1;
	private final DataType castType;

	public VoidPointerCastOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput, DataType castType) {
		super("cast", children, isInOutput);
		this.castType = castType;
	}

	protected VoidPointerCastOperator(VoidPointerCastOperator other, IVariableReplacer variableReplacer, DataType castType) {
		super(other, variableReplacer);
		this.castType = castType;
	}

	protected VoidPointerCastOperator(VoidPointerCastOperator other, ImmutableList<FormulaTreeNode> newChildren, DataType castType) {
		super(other, newChildren);
		this.castType = castType;
	}

	public VoidPointerCastOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("cast", children, isInOutput);
		this.castType = null;
	}


	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		final String castTypeName = "(" + castType.toCTypeName() + "*)";
		return debugFormat(castTypeName, getChildCode(0, usedVariables, context));
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.of(Operators.CAST);
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}


	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		if (childId >= ARGUMENT_COUNT) {
			throw new InvalidChildIdException("Child ID " + childId + " is invalid for this operator!");
		}

		Set<DataType> types = new HashSet<>();
		for (DataType type : parentConditioner.getPossibleReturnTypes()) {
			types.add(type);
		}

		return new SimpleChildExpressionConditionerBuilder(types).setCanBeASimpleValue(true).build();
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
		return new VoidPointerCastOperator(this, children, castType);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return this.getChildren().get(0).getDynamicReturnType(programContext);
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new VoidPointerCastOperator(this, variableReplacer, castType);
	}
}
