package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.NoOpVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;

import java.io.Serializable;
import java.util.*;

/**
 * This abstract class represents a piece of the requirement based on the json file.
 */

public abstract class CodeTreeNode implements Serializable {

	// For debugging purposes, every node gets a unique ID that can be used to break in the tree
	private static long nodeIdCounter = 0L;
	private final long nodeId = getNextNodeId();
	private final Set<Long> pastNodeIds = new HashSet<>();

	private static long getNextNodeId() {
		return nodeIdCounter++;
	}

	public abstract StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context);

	public abstract ImmutableList<FormulaTreeNode> getChildren();

	public abstract CodeTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children);

	public CodeTreeNode createCopyByReplacingNode(CodeTreeNode nodeToReplace, CodeTreeNode replacement) {
		Map<CodeTreeNode, CodeTreeNode> replacementMap = new HashMap<>();
		replacementMap.put(nodeToReplace, replacement);
		return createCopyByReplacingNodes(replacementMap);
	}

	public CodeTreeNode createCopyByReplacingNodes(Map<CodeTreeNode, CodeTreeNode> mapFromOriginalToReplacement) {
		ArrayList<FormulaTreeNode> newChildren = new ArrayList<>();
		for (FormulaTreeNode child : getChildren()) {
			if (child == null) {
				throw new RuntimeException("Replacement function not implemented for other tree types!");
			}

			if (mapFromOriginalToReplacement.containsKey(child)) {
				newChildren.add((FormulaTreeNode) mapFromOriginalToReplacement.get(child));
				mapFromOriginalToReplacement.remove(child);
			} else {
				newChildren.add((FormulaTreeNode) child.createCopyByReplacingNodes(mapFromOriginalToReplacement));
			}
		}
		return replaceChildren(ImmutableList.copyOf(newChildren));
	}

	/**
	 * @return the variables which occur inside a last(param) or last_i(param) command
	 */
	public abstract void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context);

	public abstract CodeTreeNode copyTree(IVariableReplacer variableReplacer);

	public CodeTreeNode copyTree() {
		return copyTree(new NoOpVariableReplacer());
	}

	public long getNodeId() {
		return nodeId;
	}

	public Set<Long> getPastNodeIds() {
		return pastNodeIds;
	}

	protected void updatePastNodeIds(Set<Long> pastNodeIds, long nodeId) {
		this.pastNodeIds.addAll(pastNodeIds);
		this.pastNodeIds.add(nodeId);
	}

	public IVariable tryToReturnOutputVariable() {
		return getChildren().get(0).tryToReturnOutputVariable();
	}

	@Override
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}

		final CodeTreeNode codeTreeNode = (CodeTreeNode) other;
		final List<FormulaTreeNode> childrenA = this.getChildren();
		final List<FormulaTreeNode> childrenB = codeTreeNode.getChildren();
		if (childrenA.size() != childrenB.size()) {
			return false;
		}
		for (int i = 0; i < childrenA.size(); ++i) {
			final FormulaTreeNode childA = childrenA.get(i);
			final FormulaTreeNode childB = childrenB.get(i);
			if (!childA.equals(childB)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * enum deciding whether it is in the step()-code (CONDITION) or in the property()-code (EXECUTION)
	 */
	public enum CodeType {
		CONDITION, EXECUTION
	}

}
