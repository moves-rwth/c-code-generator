package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.RequirementPackageOptions;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionTarget;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionVisitor;

import java.util.Set;

public abstract class FormulaTreeNode extends CodeTreeNode implements IVariableCollectionTarget {

	private SimpleChildExpressionConditioner preCondition = null;
	private final boolean isInOutput;

	protected FormulaTreeNode(boolean isInOutput) {
		this.isInOutput = isInOutput;
	}

	protected FormulaTreeNode(FormulaTreeNode other) {
		isInOutput = other.isInOutput;
		preCondition = other.preCondition;
		updatePastNodeIds(other.getPastNodeIds(), other.getNodeId());
	}

	@Override
	public void accept(IVariableCollectionVisitor visitor) {
		visitor.visit(this);
	}

	public boolean isInOutput() {
		return isInOutput;
	}

	public abstract boolean isConstant();

	@Override
	public abstract FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children);

	public abstract StringArray toLastICode(Variable variable, Set<IVariableWithAccessor> usedVariables, IProgramContext context);

	public abstract DataType getDynamicReturnType(IProgramContext programContext);

	public abstract SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException;

	public SimpleChildExpressionConditioner getPreCondition() {
		return preCondition;
	}

	public void setPreCondition(SimpleChildExpressionConditioner preCondition) {
		this.preCondition = preCondition;
	}

	protected String debugComment() {
		if (RequirementPackageOptions.doDebugComments) {
			StringBuilder result = new StringBuilder();
			result.append("/* ");
			result.append(getNodeId());
			result.append(" (");
			boolean isFirst = true;
			for (Long l : getPastNodeIds()) {
				if (!isFirst) {
					result.append(", ");
				}
				isFirst = false;
				result.append(l);
			}
			result.append(")");
			result.append(" */ ");
			return result.toString();
		}
		return "";
	}

	public StringArray getPropertyDebugCode(boolean isInAnd, boolean negated, boolean returnAtLeastOne, boolean isInCondition, boolean isOnlyDebugHelp, Set<IVariableWithAccessor> usedVariables, IProgramContext programContext) {
		StringArray result = new StringArray();
		if ((this instanceof AndOperator) || ((this instanceof ConcatenationOperator) && isInCondition)) {
			final ImmutableList<FormulaTreeNode> children = this.getChildren();
			if (isInAnd && !negated) {
				// All must hold, no negation present, a && b
				result.add(children.get(0).getPropertyDebugCode(true, false, false, true, isOnlyDebugHelp, usedVariables, programContext));
				result.add(children.get(1).getPropertyDebugCode(true, false, false, true, isOnlyDebugHelp, usedVariables, programContext));
			} else if (isInAnd) {
				// All must hold, negation present, effectively (!a) || (!b)
				result.add(children.get(0).getPropertyDebugCode(false, true, false, true, isOnlyDebugHelp, usedVariables, programContext));
				result.add(children.get(1).getPropertyDebugCode(false, true, true, true, isOnlyDebugHelp, usedVariables, programContext));
			} else if (!isInAnd && !negated) {
				// At least one must hold, no negation present, effectively X || (a && b)
				result.add(buildPropertyDebugCodeFromNode(false, false, isOnlyDebugHelp, usedVariables, programContext));
			} else if (!isInAnd) {
				// At least one must hold, negation present, effectively (!a) || (!b)
				result.add(children.get(0).getPropertyDebugCode(false, true, false, true, isOnlyDebugHelp, usedVariables, programContext));
				result.add(children.get(1).getPropertyDebugCode(false, true, returnAtLeastOne, true, isOnlyDebugHelp, usedVariables, programContext));
			} else {
				throw new RuntimeException("Missing debug case?!");
			}
		} else if (this instanceof OrOperator) {
			final OrOperator orOperator = (OrOperator) this;
			final ImmutableList<FormulaTreeNode> children = orOperator.getChildren();
			if (isInAnd && !negated) {
				// All must hold, no negation present, a || b
				result.add(children.get(0).getPropertyDebugCode(false, false, false, true, isOnlyDebugHelp, usedVariables, programContext));
				result.add(children.get(1).getPropertyDebugCode(false, false, true, true, isOnlyDebugHelp, usedVariables, programContext));
			} else if (isInAnd) {
				// All must hold, negation present, effectively (!a) && (!b)
				result.add(children.get(0).getPropertyDebugCode(true, true, false, true, isOnlyDebugHelp, usedVariables, programContext));
				result.add(children.get(1).getPropertyDebugCode(true, true, false, true, isOnlyDebugHelp, usedVariables, programContext));
			} else if (!isInAnd && !negated) {
				// At least one must hold, no negation present, effectively X || (a || b)
				result.add(children.get(0).getPropertyDebugCode(false, false, false, true, isOnlyDebugHelp, usedVariables, programContext));
				result.add(children.get(1).getPropertyDebugCode(false, false, returnAtLeastOne, true, isOnlyDebugHelp, usedVariables, programContext));
			} else if (!isInAnd) {
				// At least one must hold, negation present, effectively (!a) && (!b)
				result.add(children.get(0).getPropertyDebugCode(true, true, false, true, isOnlyDebugHelp, usedVariables, programContext));
				result.add(children.get(1).getPropertyDebugCode(true, true, false, true, isOnlyDebugHelp, usedVariables, programContext));
			} else {
				throw new RuntimeException("Missing debug case?!");
			}
		} else if (this instanceof NotOperator) {
			final NotOperator notOperator = (NotOperator) this;
			final ImmutableList<FormulaTreeNode> children = notOperator.getChildren();
			return children.get(0).getPropertyDebugCode(isInAnd, !negated, returnAtLeastOne, true, isOnlyDebugHelp, usedVariables, programContext);
		} else if (this instanceof EmptyControlStructure) {
			return getChildren().get(0).getPropertyDebugCode(isInAnd, negated, returnAtLeastOne, isInCondition, isOnlyDebugHelp, usedVariables, programContext);
		} else if (!isInCondition && (this instanceof ConcatenationOperator)) {
			final ImmutableList<FormulaTreeNode> children = this.getChildren();
			for (FormulaTreeNode child: children) {
				result.add(child.getPropertyDebugCode(true, false, false, false, isOnlyDebugHelp, usedVariables, programContext));
			}
		} else if (!isInCondition && ((this instanceof ItOperator) || (this instanceof IteOperator))) {
			final ImmutableList<FormulaTreeNode> children = this.getChildren();
			result.add(children.get(0).getPropertyDebugCode(true, false, false, true, true, usedVariables, programContext));
			final String expression = children.get(0).toCode(CodeType.CONDITION, usedVariables, programContext).toStringProperty();
			result.add("if (" + expression + ") {");
			result.addIndented(children.get(1).getPropertyDebugCode(true, false, false, false, false, usedVariables, programContext));
			if (this instanceof IteOperator) {
				result.add("} else {");
				result.addIndented(children.get(2).getPropertyDebugCode(true, false, false, false, false, usedVariables, programContext));
			}
			result.add("}");
		} else {
			if (isInAnd || returnAtLeastOne) {
				result.add(buildPropertyDebugCodeFromNode(isInAnd, negated, isOnlyDebugHelp, usedVariables, programContext));
			}
		}
		return result;
	}

	protected static boolean showAllDebugMessages = false;

	protected StringArray buildPropertyDebugCodeFromNode(boolean isInAnd, boolean negated, boolean isOnlyDebugHelp, Set<IVariableWithAccessor> usedVariables, IProgramContext programContext) {
		StringArray result = new StringArray();
		final String expression = this.toCode(CodeType.CONDITION, usedVariables, programContext).toStringProperty();
		if (!isOnlyDebugHelp || showAllDebugMessages) {
			result.add("if (" + (negated ? "" : "!") + "(" + expression + ")) {");
			result.addIndented("printf(\"" + (isOnlyDebugHelp ? "(DEBUG)" : "(VIOLATION)") + " Expression '" + expression + "' " + (isInAnd ? "needed to" : "should(?)") + " be " + (negated ? "false" : "true") + ", but was not.\\n\");");
			result.add("}");
		}
		return result;
	}
}
