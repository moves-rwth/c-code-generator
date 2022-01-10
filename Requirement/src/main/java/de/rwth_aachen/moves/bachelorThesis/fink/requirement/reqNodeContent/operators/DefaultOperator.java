package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.InvalidChildIdException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditioner;
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

public class DefaultOperator extends ControlStructureOperator {
	// case ( [constantChild0] ):
	// The content is actually part of the parents list, as case A: case B: CODE would otherwise be hard to encode.

	public static final int CONDITION_ARGUMENT_COUNT = 0;
	public static final int CODE_ARGUMENT_COUNT = 0;
	public static final int ARGUMENT_COUNT = CONDITION_ARGUMENT_COUNT + CODE_ARGUMENT_COUNT;

	public DefaultOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("default", children, isInOutput);
	}

	protected DefaultOperator(DefaultOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected DefaultOperator(DefaultOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}


	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		throw new InvalidChildIdException("Child ID " + childId + " is invalid for this operator!");
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		StringArray result = new StringArray();
		result.add("default:");
		return result;
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new DefaultOperator(this, variableReplacer);
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new DefaultOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.INSTANCE_NONE;
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		return children.get(1).getResultingExpressionConditioner(programContext);
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return EnumSet.noneOf(Operators.class);
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}
}