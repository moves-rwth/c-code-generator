package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.RequirementPackageOptions;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;

import java.util.*;

public abstract class OperatorTreeNode extends FormulaTreeNode {
	private final String name;
	protected ImmutableList<FormulaTreeNode> children;

	public OperatorTreeNode(String name, ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		super(isInOutput);
		this.name = name;
		this.children = children;
	}

	protected OperatorTreeNode(OperatorTreeNode node, ImmutableList<FormulaTreeNode> children) {
		super(node);
		this.name = node.name;
		this.children = children;
	}

	protected OperatorTreeNode(OperatorTreeNode node, IVariableReplacer variableReplacer) {
		super(node);
		this.name = node.name;
		List<FormulaTreeNode> newChildren = new ArrayList<>();
		for (FormulaTreeNode n : node.children) {
			VariableReplacer.PreVarOperator operator = variableReplacer.getPreVarOperator();
			if (operator != null || variableReplacer.castTypes()) {
				if (n instanceof VariableTreeNode) {
					if (variableReplacer.containsVariable(((VariableTreeNode) n).getVariableWithAccessor())) {
						FormulaTreeNode extraOperator = null;
						FormulaTreeNode castOperator = null;
						if (variableReplacer.castTypes()) {
							castOperator = new VoidPointerCastOperator(ImmutableList.of((FormulaTreeNode) n.copyTree(variableReplacer)), node.isInOutput(), ((VariableTreeNode) n).getVariableWithAccessor().getDataType());
						} else {
							castOperator = (FormulaTreeNode) n.copyTree(variableReplacer);
						}
						extraOperator = castOperator;
						if (operator != null) {
							switch (operator) {
								case ADDRESS_OF:
									extraOperator = new AddressOfOperator(ImmutableList.of(castOperator), node.isInOutput());
									break;
								case ARRAY_ACCESS:
									extraOperator = new ArrayAccessOperator(ImmutableList.of(castOperator), node.isInOutput());
									break;
								case DEREFERENCE:
									extraOperator = new DereferenceOperator(ImmutableList.of(castOperator), node.isInOutput());
									break;
								case LAST:
									extraOperator = new LastOperator(ImmutableList.of(castOperator), node.isInOutput());
							}
						}
						newChildren.add(extraOperator);
					} else {
						newChildren.add((FormulaTreeNode) n.copyTree(variableReplacer));
					}
				} else {
					newChildren.add((FormulaTreeNode) n.copyTree(variableReplacer));
				}
			} else {
				newChildren.add((FormulaTreeNode) n.copyTree(variableReplacer));
			}
		}
		this.children = ImmutableList.copyOf(newChildren);
	}

	protected boolean isWrapNecessary(String s) {
		return s.indexOf(' ') != -1;
	}

	protected String wrapIfNecessary(String s) {
		if (!isWrapNecessary(s)) {
			return s;
		}
		return "(" + s + ")";
	}

	protected StringArray debugFormat(String operator, StringArray unaryArg, boolean forceWrapping) {
		if (!RequirementPackageOptions.doDebugComments) {
			if (forceWrapping) {
				return new StringArray(operator + " (" + unaryArg.toStringProperty() + ")");
			} else {
				return new StringArray(operator + " " + wrapIfNecessary(unaryArg.toStringProperty()));
			}
		}
		StringArray result = new StringArray();
		result.add(debugComment() + "(" + operator + " (");
		result.addIndented(unaryArg);
		result.add("))");
		return result;
	}

	protected StringArray debugFormat(String operator, StringArray unaryArg) {
		return debugFormat(operator, unaryArg, false);
	}

	protected StringArray debugFormat(StringArray unaryArg, String operator, boolean forceWrapping) {
		if (!RequirementPackageOptions.doDebugComments) {
			if (forceWrapping) {
				return new StringArray("(" + unaryArg.toStringProperty() + ") " + operator);
			} else {
				return new StringArray(wrapIfNecessary(unaryArg.toStringProperty()) + " " + operator);
			}
		}
		StringArray result = new StringArray();
		result.add(debugComment() + "(" + operator + " (");
		result.addIndented(unaryArg);
		result.add("))");
		return result;
	}

	protected StringArray debugFormat(StringArray unaryArg, String operator) {
		return debugFormat(unaryArg, operator, false);
	}

	protected StringArray debugFormat(StringArray argA, String operator, StringArray argB) {
		if (!RequirementPackageOptions.doDebugComments) {
			return new StringArray(wrapIfNecessary(argA.toStringProperty()) + " " + operator + " " + wrapIfNecessary(argB.toStringProperty()));
		}
		final boolean isAssignment = Objects.equals(operator, "=");
		final boolean isList = Objects.equals(operator, ",");

		final boolean doOuterWrap = !isAssignment && !isList;
		StringArray result = new StringArray();
		if (isAssignment && !isWrapNecessary(argA.toStringProperty())) {
			result.add(debugComment());
			result.addAllButAppendFirstLine(argA);
		} else {
			result.add(debugComment() + (doOuterWrap ? "(" : "") + "(");
			result.addIndented(argA);
			result.add(")");
		}
		result.addToLastLine(" " + operator + " (");
		result.addIndented(argB);
		result.add(")" + (doOuterWrap ? ")" : ""));
		return result;
	}

	protected StringArray debugFormat(StringArray argA, String operatorA, StringArray argB, String operatorB, StringArray argC) {
		if (!RequirementPackageOptions.doDebugComments) {
			return new StringArray(wrapIfNecessary(argA.toStringProperty()) + " " + operatorA + " " + wrapIfNecessary(argB.toStringProperty()) + " " + operatorB + " " + wrapIfNecessary(argC.toStringProperty()));
		}
		StringArray result = new StringArray();
		result.add(debugComment() + "((");
		result.addIndented(argA);
		result.add(") " + operatorA + " (");
		result.addIndented(argB);
		result.add(") " + operatorB + " (");
		result.addIndented(argC);
		result.add("))");
		return result;
	}

	protected StringArray debugFormatArrayAccess(StringArray arrayVariable, StringArray indexExpression) {
		if (!RequirementPackageOptions.doDebugComments) {
			return new StringArray(arrayVariable.toStringProperty() + " [ (int) " + indexExpression.toStringProperty() + "]");
		}
		StringArray result = new StringArray();
		result.add(debugComment() + "(");
		result.addIndented(arrayVariable);
		result.add("[ (int) (");
		result.addIndented(indexExpression);
		result.add(")])");
		return result;
	}

	public abstract EnumSet<Operators> getPossibleReplacementOperators();

	public String getName() {
		return name;
	}

	protected FormulaTreeNode child(int childId) {
		if (!((childId >= 0) && (childId < children.size()))) {
			throw new RuntimeException("Child ID out of range: " + childId + "!");
		}
		return getChildren().get(childId);
	}

	public boolean hasFloatSubtree(IProgramContext programContext) {
		for (FormulaTreeNode child : children) {
			if (child.getDynamicReturnType(programContext).isFloatingPoint()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public StringArray toLastICode(Variable variable, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		variable.setLastIHasToBeArray(true);
		return new StringArray("last_i_" + variable.getName().trim() + "[" + toCode(CodeType.CONDITION, usedVariables, context) + "]");
	}

	@Override
	public void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context) {
		for (FormulaTreeNode child : children) {
			child.getLastVariablesAndDepths(allVariables, lastIVariables, context);
		}
	}

	public StringArray getChildCode(int index, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		if (index > children.size() - 1) {
			throw new RuntimeException("This child is (not yet) defined!");
		}
		return children.get(index).toCode(CodeType.CONDITION, usedVariables, context);
	}

	public StringArray getExecutionChildCode(int index, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return children.get(index).toCode(CodeType.EXECUTION, usedVariables, context);
	}

	public ImmutableList<FormulaTreeNode> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return "Operator '" + getName() + "' (ID = " + getNodeId() + ")";
	}

	public abstract boolean isIntegerSafe();

}
