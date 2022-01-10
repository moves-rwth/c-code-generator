package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.RequirementPackageOptions;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.RequirementScopes;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.ControlStructureOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.patterns.InvariantPatternTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionTarget;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionVisitor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;

import java.io.Serializable;
import java.util.*;

public class Property implements Serializable, IVariableCollectionTarget, ICodeTreeNodeContainer {

	private final String name;
	private final CodeTreeNode codeTreeNode;
	private final RequirementScopes scope;
	private final List<String> correspondingRequirementNames = new ArrayList<>();

	private boolean isValid;

	public Property(String name, CodeTreeNode codeTreeNode, RequirementScopes scope, boolean isValid, List<String> correspondingRequirementNames) {
		this.name = name;
		this.codeTreeNode = codeTreeNode;
		this.scope = scope;
		this.isValid = isValid;
		this.correspondingRequirementNames.addAll(correspondingRequirementNames);
	}

	public Property(String name, CodeTreeNode codeTreeNode, RequirementScopes scope, List<String> correspondingRequirementNames) {
		this(name, codeTreeNode, scope, true, correspondingRequirementNames);
	}

	public Property(Property prop, IVariableReplacer variableReplacer) {
		this(prop.getName(), prop.getCodeTreeNode().copyTree(variableReplacer), prop.getScope(), prop.isValid(), new ArrayList<>(prop.correspondingRequirementNames));
	}

	// Copy constructor
	public Property(Property prop) {
		this(prop, new NoOpVariableReplacer());
	}

	public Property(Property prop, IProgramContext context) {
		this(prop.getName(), prop.getCodeTreeNode().copyTree(new NoOpVariableReplacer()), prop.getScope(), prop.isValid(), new ArrayList<>(prop.correspondingRequirementNames));
	}

	private static FormulaTreeNode ignorePatternTreeNode(CodeTreeNode codeTreeNode) {
		if (codeTreeNode instanceof InvariantPatternTreeNode) {
			final InvariantPatternTreeNode invariantPatternTreeNode = (InvariantPatternTreeNode) codeTreeNode;
			return invariantPatternTreeNode.getChild();
		}
		assert(codeTreeNode instanceof FormulaTreeNode);
		return (FormulaTreeNode) codeTreeNode;
	}

	public static List<Property> concatenateAll(List<Property> properties) {
		FormulaTreeNode result = null;
		StringBuilder newName = new StringBuilder("concat");
		List<String> allRequirementNames = new ArrayList<>();
		for (Property property : properties) {
			assert (property.getScope() == RequirementScopes.GLOBALLY);
			if (result == null) {
				result = ignorePatternTreeNode(property.getCodeTreeNode());
			} else {
				result = new ConcatenationOperator(ImmutableList.of(result, ignorePatternTreeNode(property.getCodeTreeNode())), false);
			}
			newName.append("_").append(property.getName());
			allRequirementNames.addAll(property.getCorrespondingRequirementNames());
		}
		// We just want a short name
		newName = new StringBuilder(properties.get(0).getName());
		return new ArrayList<>(List.of(new Property(newName.toString(), new InvariantPatternTreeNode(result), RequirementScopes.GLOBALLY, allRequirementNames)));
	}

	public Property replaceControlStructures(List<ControlStructureOperator> controlStructures) {
		CodeTreeNode node = buildControlStructureTree(controlStructures);
		return new Property(this.getName(), node, this.getScope(), this.isValid, this.correspondingRequirementNames);
	}

	public Property createCopyByReplacingNode(CodeTreeNode nodeToReplace, CodeTreeNode replacement) {
		Map<CodeTreeNode, CodeTreeNode> replacementMap = new HashMap<>();
		replacementMap.put(nodeToReplace, replacement);
		return createCopyByReplacingNodes(replacementMap);
	}

	public Property createCopyByReplacingNodes(Map<CodeTreeNode, CodeTreeNode> mapFromOriginalToReplacement) {
		// Edge case: Replace root node.
		if (mapFromOriginalToReplacement.containsKey(getCodeTreeNode())) {
			CodeTreeNode newRoot = mapFromOriginalToReplacement.get(getCodeTreeNode());
			return new Property(getName(), newRoot, getScope(), isValid(), getCorrespondingRequirementNames());
		}

		// Pass replacement map down recursively
		CodeTreeNode newRoot = getCodeTreeNode().createCopyByReplacingNodes(mapFromOriginalToReplacement);
		return new Property(getName(), newRoot, getScope(), isValid(), getCorrespondingRequirementNames());
	}

	public Property createCopyByInsertingInFront(FormulaTreeNode nodeToPlaceInFront) {
		final FormulaTreeNode oldRoot = ignorePatternTreeNode(getCodeTreeNode());
		ImmutableList<FormulaTreeNode> childrenOfNewRoot = ImmutableList.of(nodeToPlaceInFront, oldRoot);
		ConcatenationOperator newRoot = new ConcatenationOperator(childrenOfNewRoot, true);
		return new Property(getName(), newRoot, getScope(), isValid(), getCorrespondingRequirementNames());
	}

	@Override
	public void accept(IVariableCollectionVisitor visitor) {
		visitor.visit(this);
	}

	public Property addLastBeforeVariables(Set<IVariableWithAccessor> variables) {
		CodeTreeNode newRoot = addLastToVariables(variables, codeTreeNode, true);
		return new Property(name, newRoot, scope, correspondingRequirementNames);
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

	public Property replaceReqNode(CodeTreeNode newCodeTreeNode) {
		return new Property(this.name, newCodeTreeNode, this.scope, this.correspondingRequirementNames);
	}

	public StringArray toCode(IProgramContext context) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Property that = (Property) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	public Property copy(IProgramContext context) {
		return new Property(this, context);
	}

	public Property copy(String name) {
		return new Property(name, codeTreeNode.copyTree(), scope, new ArrayList<>(correspondingRequirementNames));
	}

	public Property copy(String name, String reqSuffix) {
		List<String> updatedCorrespondingNames = new ArrayList<>(correspondingRequirementNames);
		updatedCorrespondingNames.replaceAll(str -> str + reqSuffix);
		return new Property(name, codeTreeNode.copyTree(), scope, updatedCorrespondingNames);
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

	public Property copy(VariableReplacer variableReplacer) {
		return new Property(this, variableReplacer);
	}

	public List<String> getCorrespondingRequirementNames() {
		return correspondingRequirementNames;
	}

	@Override
	public String toString() {
		VariableCollector variableCollector = new VariableCollector(true, null);
		accept(variableCollector);
		String result = "Property(hasFloatingPointVariables = ";
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
