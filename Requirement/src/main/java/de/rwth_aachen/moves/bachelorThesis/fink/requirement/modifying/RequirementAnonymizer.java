package de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RequirementAnonymizer {

	public RequirementAnonymizer() {
	}

	public List<Requirement> anonymizeRequirements(List<Requirement> requirements) {
		List<Requirement> result = new ArrayList<>();
		VariableReplacer variableReplacer = new VariableReplacer();

		for (int i = 0; i < requirements.size(); ++i) {
			Requirement requirement = requirements.get(i);

			result.add(anonymizeRequirement(requirement, variableReplacer, i));
		}

		return result;
	}

	public List<Requirement> anonymizeRequirementTrees(List<Requirement> requirements) {
		List<Requirement> result = new ArrayList<>();
		for (Requirement requirement : requirements) {
			result.add(anonymizeTreeSimple(requirement));
		}

		return result;
	}


	private Requirement anonymizeTreeSimple(Requirement requirement) {
		Requirement newRequirement = requirement.copy((IProgramContext) null);
		final CodeTreeNode codeTreeNode = newRequirement.getCodeTreeNode();
		final List<FormulaTreeNode> firstRealNodes = codeTreeNode.getChildren();

		List<FormulaTreeNode> newChildren = new ArrayList<>();
		for (FormulaTreeNode child : firstRealNodes) {
			newChildren.add(anonymizeNodeSimple(child));
		}
		return newRequirement.replaceReqNode(codeTreeNode.replaceChildren(ImmutableList.copyOf(newChildren)));
	}

	private FormulaTreeNode anonymizeNodeSimple(FormulaTreeNode node) {
		if (node instanceof OperatorTreeNode) {
			OperatorTreeNode otn = (OperatorTreeNode) node;
			ArrayList<Operators> ops = new ArrayList<>(otn.getPossibleReplacementOperators());

			Set<Operators> toRemove = new HashSet<>();
			for (Operators op : ops) {
				if (op.getArgumentCount() != otn.getChildren().size()) {
					toRemove.add(op);
				}
			}
			ops.removeAll(toRemove);

			List<FormulaTreeNode> newChildren = new ArrayList<>();
			for (FormulaTreeNode child : otn.getChildren()) {
				final FormulaTreeNode newChild = anonymizeNodeSimple(child);
				newChildren.add(newChild);
			}
			return ops.get(RandomGenHelper.randomInt(0, ops.size() - 1)).operator(ImmutableList.copyOf(newChildren), node.isInOutput());
		}
		return node;
	}


	private Requirement anonymizeRequirement(Requirement requirement, VariableReplacer variableReplacer, int counter) {
		Requirement result = requirement.copy(variableReplacer);

		// Anonymize name
		result = result.copy("Template" + counter);

		return result;
	}
}
