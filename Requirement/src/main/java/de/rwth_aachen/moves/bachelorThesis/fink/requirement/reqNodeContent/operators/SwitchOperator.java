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

public class SwitchOperator extends ControlStructureOperator {
	// switch ( [expressionChild0] ) {
	//	[caseChild1]
	//  ...
	//  [caseChildN (default)]
	// }

	public static final int CONDITION_ARGUMENT_COUNT = 1;
	public static final int CODE_ARGUMENT_COUNT = 5;
	public static final int MIN_ARGUMENT_COUNT = CONDITION_ARGUMENT_COUNT + CODE_ARGUMENT_COUNT;
	public static final int MAX_ARGUMENT_COUNT = CONDITION_ARGUMENT_COUNT + CODE_ARGUMENT_COUNT;

	public SwitchOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("switch", children, isInOutput);
	}

	protected SwitchOperator(SwitchOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected SwitchOperator(SwitchOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	public static SimpleChildExpressionConditioner makeChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		if (childId >= MAX_ARGUMENT_COUNT) {
			throw new InvalidChildIdException("Child ID " + childId + " is invalid for this operator!");
		}

		SimpleChildExpressionConditionerBuilder result;
		if (childId == 0) {
			result = new SimpleChildExpressionConditionerBuilder(DataType.INSTANCE_BOOL).setCanBeASimpleValue(false);
		} else {
			result = new SimpleChildExpressionConditionerBuilder(parentConditioner).setCanBeASimpleValue(true);
		}
		return result.setParentOperator(Operators.SWITCH).build();
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		StringArray result = new StringArray();
		if (codeType == CodeType.CONDITION) {
			result.add("");
		} else {
			result.add(debugComment() + "switch (" + getChildCode(0, usedVariables, context).toStringProperty() + ") {");
			for (int i = 1; i < children.size(); i++) {
				result.addIndented(children.get(i).toCode(codeType, usedVariables, context));
				if (children.get(i) instanceof BreakOperator) {
					result.addEmptyLine();
				}
			}
			result.add("}");
		}

		return result;
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new SwitchOperator(this, variableReplacer);
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new SwitchOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return DataType.INSTANCE_NONE;
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		SimpleResultingExpressionConditioner result = children.get(1).getResultingExpressionConditioner(programContext);
		for (int i = 2; i < children.size(); i++) {
			result = result.or(children.get(i).getResultingExpressionConditioner(programContext));
		}
		return result;
	}

	@Override
	public EnumSet<Operators> getPossibleReplacementOperators() {
		return Operators.getControlStructureOperators();
	}

	@Override
	public boolean isIntegerSafe() {
		return true;
	}
}
