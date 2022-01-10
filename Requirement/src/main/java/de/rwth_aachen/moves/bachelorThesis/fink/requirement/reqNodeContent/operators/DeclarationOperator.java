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
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class DeclarationOperator extends ControlStructureOperator {
	public static final int ARGUMENT_COUNT = 2;

	public DeclarationOperator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super("decl", children, isInOutput);
		final FormulaTreeNode child = children.get(0);
		if (!(child instanceof VariableTreeNode)) {
			throw new RuntimeException("Child of declaration has to be a variable tree node!");
		}
	}

	protected DeclarationOperator(DeclarationOperator other, IVariableReplacer variableReplacer) {
		super(other, variableReplacer);
	}

	protected DeclarationOperator(DeclarationOperator other, ImmutableList<FormulaTreeNode> newChildren) {
		super(other, newChildren);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		if (codeType == CodeType.CONDITION) {
			throw new IllegalStateException("Can not use variable declaration in condition code!");
		}


		final FormulaTreeNode child = getChildren().get(0);

		if (!(child instanceof VariableTreeNode)) {
			throw new RuntimeException("Child of declaration has to be a variable tree node!");
		}
		final VariableTreeNode variableTreeNode = (VariableTreeNode) child;
		final IVariableWithAccessor variableWithAccessor = variableTreeNode.getVariableWithAccessor();
		final IVariable variable = variableWithAccessor.getVariable();

		if (getChildren().size() == 1) {
			return variable.getStorageDeclaration(); // Init with 0
		} else if (getChildren().size() == 2) {
			FormulaTreeNode assignmentValue = getChildren().get(1);
			String result = variableWithAccessor.getDataType().toCTypeName() + " " // "Type"
			+ variableWithAccessor.getName() + " = " // "Name = "
			+ assignmentValue.toCode(codeType, usedVariables, context).toStringProperty() + ";"; // "value;"
			return new StringArray(result);
		} else {
			throw new RuntimeException("Unexpected amount of children in declaration!");
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
			result = new SimpleChildExpressionConditionerBuilder(parentConditioner).setCanBeASimpleValue(false);
		} else {
			result = new SimpleChildExpressionConditionerBuilder(previousChildResultConditioners.get(0).getPossibleReturnTypes()).setCanBeASimpleValue(true);
		}
		return result.setParentOperator(Operators.DECL).build();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		return children.get(0).getResultingExpressionConditioner(programContext);
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new DeclarationOperator(this, children);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return getChildren().get(0).getDynamicReturnType(programContext);
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new DeclarationOperator(this, variableReplacer);
	}
}
