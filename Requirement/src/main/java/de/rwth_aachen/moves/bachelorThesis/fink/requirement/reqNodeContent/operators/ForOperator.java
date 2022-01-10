package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
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

public class ForOperator extends ControlStructureOperator {
	// for (int i = 0; i < a; i++) {
	// 	 childA
	// }
	// property:
	// if (0 < a) childA[i/0] && if (1 < a) childA[i/1] ...

	// what is needed?
	// 1 new local variable with standard value assignment (e.g. int i = 0)
	// 1 condition with comparator [depending on comparator it is i++ or i--] (e.g. i < 10)
	// 1 codeTreeNode child
	// make sure i and a are not being used

	public static final int CODE_ARGUMENT_COUNT      = 1;
	public static final int CONDITION_ARGUMENT_COUNT = 1;
	public static final int ARGUMENT_COUNT = CODE_ARGUMENT_COUNT + CONDITION_ARGUMENT_COUNT;

	public ForOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("for", children, isInOutput);
	}

	protected ForOperator(ForOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected ForOperator(ForOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		StringArray result = new StringArray();
		if (codeType == CodeType.CONDITION) {
			throw new RuntimeException("not implemented");
		} else {
			result.add("for (");
			result.add(getChildCode(0, usedVariables, context) + " ;"); // i = value
			result.add(getChildCode(1, usedVariables, context) + " ;"); // loop condition
			result.add(getChildCode(2, usedVariables, context)); // step, e.g. ++i
			result.add(") {");
			result.addIndented(getExecutionChildCode(3, usedVariables, context), 1);
			result.add("}");
		}
		return result;
	}

	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		throw new RuntimeException("not implemented");
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new ForOperator(this, variableReplacer);
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new ForOperator(this, children);
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
