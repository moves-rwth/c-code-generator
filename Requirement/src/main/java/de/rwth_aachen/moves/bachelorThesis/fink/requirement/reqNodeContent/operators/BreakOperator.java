package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
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
import java.util.Set;

public class BreakOperator extends ControlStructureOperator {
	public static final int CONDITION_ARGUMENT_COUNT = 0;
	public static final int CODE_ARGUMENT_COUNT = 0;
	public static final int ARGUMENT_COUNT = CONDITION_ARGUMENT_COUNT + CODE_ARGUMENT_COUNT;

	public BreakOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("break", children, isInOutput);
	}

	protected BreakOperator(ControlStructureOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected BreakOperator(ControlStructureOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		StringArray result = new StringArray();
		if (codeType == CodeType.CONDITION) {
			result.add("");
		} else {
			result.add("break;");
		}
		return result;
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new BreakOperator(this, variableReplacer);
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new BreakOperator(this, children);
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
		return EnumSet.noneOf(Operators.class);
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}
}
