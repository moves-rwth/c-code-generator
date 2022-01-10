package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.ControlStructureOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ConcatenationOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;

import java.util.ArrayList;
import java.util.List;

public interface ICodeTreeNodeContainer {

	String getName();

	CodeTreeNode getCodeTreeNode();

	default FormulaTreeNode getFormulaTreeNode() {
		return getFormulaTreeNode(getCodeTreeNode());
	}

	default FormulaTreeNode getFormulaTreeNode(CodeTreeNode node) {
		if (node instanceof FormulaTreeNode) {
			return (FormulaTreeNode) node;
		} else {
			return getFormulaTreeNode(node.getChildren().get(0));
		}
	}

	// This function assumes our code is built like this:
	// Concat -> (
	//	CS -> ..
	//	Concat -> (
	//		CS -> ..
	//		Concat -> (
	//			...
	//	)
	// )
	default List<ControlStructureOperator> getControlStructures() {
		return getControlStructuresInternal(getCodeTreeNode());
	}

	default List<ControlStructureOperator> getControlStructuresInternal(CodeTreeNode node) {
		List<ControlStructureOperator> controlStructures = new ArrayList<>();

		if (node instanceof ControlStructureOperator && !(node instanceof ConcatenationOperator)) {
			controlStructures.add((ControlStructureOperator) node);
		} else {
			for (CodeTreeNode child : node.getChildren()) {
				controlStructures.addAll(getControlStructuresInternal(child));
			}
		}
		return controlStructures;
	}

	default CodeTreeNode buildControlStructureTree(List<ControlStructureOperator> controlStructures) {
		if (controlStructures.isEmpty()) {
			return null;
		} else if (controlStructures.size() == 1) {
			return controlStructures.get(0);
		} else {
			List<FormulaTreeNode> children = new ArrayList<>();
			children.add(controlStructures.get(0));
			controlStructures.remove(0);
			children.add((FormulaTreeNode) buildControlStructureTree(controlStructures));
			return new ConcatenationOperator(ImmutableList.copyOf(children), false);
		}
	}
}
