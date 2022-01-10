package de.rwth_aachen.moves.bachelorThesis.fink.requirement;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.ControlStructureOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.ICodeTreeNodeContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.patterns.InvariantPatternTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionTarget;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionVisitor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;

import java.io.Serializable;
import java.util.*;

/**
 * This class represents a requirement with all the necessary information.
 * In the end, one instance of this class will result in two code blocks, "step()" and "property()".
 */

public class Requirement implements Serializable, IVariableCollectionTarget, ICodeTreeNodeContainer {
	private final String name;
	private final CodeTreeNode codeTreeNode;
	private final RequirementScopes scope;

	private boolean isValid;
	private int priority;

	private Requirement(String name, CodeTreeNode codeTreeNode, RequirementScopes scope, boolean isValid, int priority) {
		this.name = name;
		this.codeTreeNode = codeTreeNode;
		this.scope = scope;
		this.isValid = isValid;
		this.priority = priority;
	}

	public Requirement(String name, CodeTreeNode codeTreeNode, RequirementScopes scope) {
		this(name, codeTreeNode, scope, true, -1);
	}

	public Requirement(Requirement req, IVariableReplacer variableReplacer) {
		this(req.getName(), req.getCodeTreeNode().copyTree(variableReplacer), req.getScope(), req.isValid(), -1);
	}

	// Copy constructor
	public Requirement(Requirement req) {
		this(req, new NoOpVariableReplacer());
	}

	public Requirement(Requirement req, IProgramContext context) {
		this(req.getName(), req.getCodeTreeNode().copyTree(new NoOpVariableReplacer()), req.getScope(), req.isValid(), req.getPriority());
	}

	public Requirement replaceControlStructures(List<ControlStructureOperator> controlStructures) {
		CodeTreeNode node = buildControlStructureTree(controlStructures);
		return new Requirement(this.name, node, this.scope, this.isValid, this.priority);
	}

	public Requirement createCopyByReplacingNode(CodeTreeNode nodeToReplace, CodeTreeNode replacement) {
		Map<CodeTreeNode, CodeTreeNode> replacementMap = new HashMap<>();
		replacementMap.put(nodeToReplace, replacement);
		return createCopyByReplacingNodes(replacementMap);
	}

	public Requirement createCopyByReplacingNodes(Map<CodeTreeNode, CodeTreeNode> mapFromOriginalToReplacement) {
		// Edge case: Replace root node.
		if (mapFromOriginalToReplacement.containsKey(getCodeTreeNode())) {
			CodeTreeNode newRoot = mapFromOriginalToReplacement.get(getCodeTreeNode());
			return new Requirement(getName(), newRoot, getScope(), isValid(), getPriority());
		}

		// Pass replacement map down recursively
		CodeTreeNode newRoot = getCodeTreeNode().createCopyByReplacingNodes(mapFromOriginalToReplacement);
		return new Requirement(getName(), newRoot, getScope(), isValid(), getPriority());
	}

	public Requirement createCopyByInsertingInFront(FormulaTreeNode nodeToPlaceInFront) {
		if (getCodeTreeNode() instanceof FormulaTreeNode) {
			FormulaTreeNode oldRoot = (FormulaTreeNode) getCodeTreeNode();
			ImmutableList<FormulaTreeNode> childrenOfNewRoot = ImmutableList.of(nodeToPlaceInFront, oldRoot);
			ConcatenationOperator newRoot = new ConcatenationOperator(childrenOfNewRoot, true);
			return new Requirement(getName(), newRoot, getScope(), isValid(), getPriority());
		} else if (getCodeTreeNode() instanceof InvariantPatternTreeNode) {
			InvariantPatternTreeNode oldRoot = (InvariantPatternTreeNode) getCodeTreeNode();
			ImmutableList<FormulaTreeNode> childrenOfNewRoot = ImmutableList.of(nodeToPlaceInFront, oldRoot.getChild());
			ConcatenationOperator childOfNewInvariantTreeNode = new ConcatenationOperator(childrenOfNewRoot, true);
			return new Requirement(getName(), new InvariantPatternTreeNode(childOfNewInvariantTreeNode), getScope());
		}
		throw new RuntimeException("Requirement root node is not a FormulaTreeNode!");
	}

	@Override
	public void accept(IVariableCollectionVisitor visitor) {
		visitor.visit(this);
	}

	public Requirement addLastBeforeVariables(Set<IVariableWithAccessor> variables) {
		CodeTreeNode newRoot = addLastToVariables(variables, codeTreeNode, true);
		return new Requirement(name, newRoot, scope);
	}

	private CodeTreeNode addLastToVariables(Set<IVariableWithAccessor> variables, CodeTreeNode node, boolean applyLast) {
		if (node instanceof VariableTreeNode) {
			final VariableTreeNode variableTreeNode = (VariableTreeNode) node;
			VariableTreeNode newVariableTreeNode = variableTreeNode.copyTree(new NoOpVariableReplacer());
			if (applyLast && variables.contains(variableTreeNode.getVariableWithAccessor())) {
				return new LastOperator(ImmutableList.of(newVariableTreeNode), false);
			} else {
				return newVariableTreeNode;
			}
		}

		if (node instanceof OutputOperator || node instanceof LastOperator || node instanceof LastIOperator) {
			applyLast = false;
		}
		List<FormulaTreeNode> childrenList = new ArrayList<>();
		for (FormulaTreeNode n : node.getChildren()) {
			childrenList.add((FormulaTreeNode) addLastToVariables(variables, n, applyLast));
		}
		return node.replaceChildren(ImmutableList.copyOf(childrenList));
	}

	public RequirementScopes getScope() {
		return scope;
	}

	public CodeTreeNode getCodeTreeNode() {
		return codeTreeNode;
	}

	public Requirement replaceReqNode(CodeTreeNode newCodeTreeNode) {
		return new Requirement(this.name, newCodeTreeNode, this.scope);
	}

	public StringArray toStepCode(IProgramContext context) {
		StringArray code;
		IVariableCollector variableCollector = new VariableCollector(false, context);
		accept(variableCollector);
		if (scope == RequirementScopes.GLOBALLY) {
			code = codeTreeNode.toCode(CodeTreeNode.CodeType.EXECUTION, variableCollector.getUsedVariablesWithAccessor(), context);
		} else if (scope == RequirementScopes.INITIALLY) {
			// Now handled with initially() function
			code = codeTreeNode.toCode(CodeTreeNode.CodeType.EXECUTION, variableCollector.getUsedVariablesWithAccessor(), context);
		} else {
			throw new RuntimeException("Unhandled scope: " + scope);
		}
		return code;
	}

	public StringArray toStepCode(CodeTreeNode tree, IProgramContext context) {
		StringArray code;
		IVariableCollector variableCollector = new VariableCollector(false, context);
		accept(variableCollector);
		if (scope == RequirementScopes.GLOBALLY) {
			code = tree.toCode(CodeTreeNode.CodeType.EXECUTION, variableCollector.getUsedVariablesWithAccessor(), context);
		} else if (scope == RequirementScopes.INITIALLY) {
			// Now handled with initially() function
			code = tree.toCode(CodeTreeNode.CodeType.EXECUTION, variableCollector.getUsedVariablesWithAccessor(), context);
		} else {
			throw new RuntimeException("Unhandled scope: " + scope);
		}
		return code;
	}

	public Property buildTrivialProperty() {
		return new Property(name + "_property", codeTreeNode.copyTree(), scope, isValid, new ArrayList<>(List.of(name)));
	}

	public StringArray toPropertyCode(IProgramContext context) {
		StringArray code;
		IVariableCollector variableCollector = new VariableCollector(false, context);
		accept(variableCollector);
		if (scope == RequirementScopes.GLOBALLY) {
			code = codeTreeNode.toCode(CodeTreeNode.CodeType.CONDITION, variableCollector.getUsedVariablesWithAccessor(), context);
		} else if (scope == RequirementScopes.INITIALLY) {
			code = new StringArray();
			code.add("((isInitial == 0) || (");
			code.addIndented(codeTreeNode.toCode(CodeTreeNode.CodeType.CONDITION, variableCollector.getUsedVariablesWithAccessor(), context));
			code.add("))");
		} else {
			throw new RuntimeException("Unhandled scope: " + scope);
		}
		return code;
	}

	/**
	 * @return the variables which occur inside a last(param) or last_i(param) command
	 */
	public Map<IVariable, Long> getLastVariablesAndDepths(IProgramContext context) {
		Map<IVariable, Long> lastIVariables = new HashMap<>();
		IVariableCollector variableCollector = new VariableCollector(false, context);
		accept(variableCollector);
		codeTreeNode.getLastVariablesAndDepths(variableCollector.getUsedVariables(), lastIVariables, context);
		return lastIVariables;
	}

	private static FormulaTreeNode ignorePatternTreeNode(CodeTreeNode codeTreeNode) {
		if (codeTreeNode instanceof InvariantPatternTreeNode) {
			final InvariantPatternTreeNode invariantPatternTreeNode = (InvariantPatternTreeNode) codeTreeNode;
			return invariantPatternTreeNode.getChild();
		}
		assert (codeTreeNode instanceof FormulaTreeNode);
		return (FormulaTreeNode) codeTreeNode;
	}

	public static List<Requirement> concatenateAll(List<Requirement> requirements) {
		FormulaTreeNode result = null;
		StringBuilder newName = new StringBuilder("concat");
		for (Requirement requirement : requirements) {
			assert (requirement.getScope() == RequirementScopes.GLOBALLY);
			if (result == null) {
				result = ignorePatternTreeNode(requirement.getCodeTreeNode());
			} else {
				result = new ConcatenationOperator(ImmutableList.of(result, ignorePatternTreeNode(requirement.getCodeTreeNode())), false);
			}
			newName.append("_").append(requirement.getName());
		}
		newName = new StringBuilder(requirements.get(0).getName());
		return new ArrayList<>(List.of(new Requirement(newName.toString(), new InvariantPatternTreeNode(result), RequirementScopes.GLOBALLY)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Requirement that = (Requirement) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	public Requirement copy(IProgramContext context) {
		return new Requirement(this, context);
	}

	public Requirement copy(String name) {
		return new Requirement(name, codeTreeNode, scope);
	}

	public String getName() {
		return name;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean valid) {
		isValid = valid;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Requirement copy(VariableReplacer variableReplacer) {
		return new Requirement(this, variableReplacer);
	}

	@Override
	public String toString() {
		VariableCollector variableCollector = new VariableCollector(true, null);
		accept(variableCollector);
		String result = "Requirement(hasFloatingPointVariables = ";
		boolean hasFloatingPointVariable = false;
		for (IVariable variable : variableCollector.getUsedVariables()) {
			if (variable instanceof FloatingPointVariable || variable instanceof FloatingPointArrayVariable) {
				hasFloatingPointVariable = true;
				break;
			}
		}
		result += (hasFloatingPointVariable ? "yes" : "no");
		result += ")";

		return result;
	}

	public StringArray getPropertyDebugCode(IProgramContext programContext) {
		if (RequirementPackageOptions.doDebugComments) {
			if (codeTreeNode instanceof InvariantPatternTreeNode) {
				IVariableCollector variableCollector = new VariableCollector(false, programContext);
				accept(variableCollector);
				final InvariantPatternTreeNode patternTreeNode = (InvariantPatternTreeNode) codeTreeNode;
				return patternTreeNode.getChild().getPropertyDebugCode(true, false, false, false, false, variableCollector.getUsedVariablesWithAccessor(), programContext);
			} else {
				throw new RuntimeException("Unhandled ReqTreeNode type '" + codeTreeNode.getClass().getCanonicalName() + "' for property debugging!");
			}
		}

		return new StringArray();
	}
}
