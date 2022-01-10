package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;

import java.util.Map;
import java.util.Set;

public class PlaceholderTreeNode extends FormulaTreeNode {

	public enum PlaceholderType {
		Code, Condition, Variable
	}

	private final PlaceholderType placeholderType;
	private final int index;

	public PlaceholderTreeNode(boolean isInOutput, PlaceholderType placeholderType, int index) {
		super(isInOutput);
		this.placeholderType = placeholderType;
		this.index = index;
	}

	public PlaceholderTreeNode(PlaceholderTreeNode node) {
		super(node);
		this.placeholderType = node.getPlaceholderType();
		this.index = node.getIndex();
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return new StringArray("(THIS IS A PLACEHOLDER)");
	}

	@Override
	public ImmutableList<FormulaTreeNode> getChildren() {
		return null;
	}

	@Override
	public void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context) {

	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new PlaceholderTreeNode(this);
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return null;
	}

	@Override
	public StringArray toLastICode(Variable variable, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return null;
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return null;
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		return null;
	}

	public PlaceholderType getPlaceholderType() {
		return placeholderType;
	}

	public int getIndex() {
		return index;
	}
}
