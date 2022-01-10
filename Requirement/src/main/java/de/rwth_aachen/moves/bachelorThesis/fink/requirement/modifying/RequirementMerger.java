package de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.RequirementScopes;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.patterns.InvariantPatternTreeNode;

import java.util.ArrayList;
import java.util.List;

public class RequirementMerger {

	private static int nameCounter = 0;

	public RequirementMerger() {
	}

	public Requirement mergeRequirements(List<Requirement> requirements) {
		CodeTreeNode codeTreeNode = null;

		List<Requirement> mergeableRequirements = new ArrayList<>();
		for (Requirement req : requirements) {
			if (req.getScope().equals(RequirementScopes.GLOBALLY) && req.getCodeTreeNode() instanceof InvariantPatternTreeNode) {
				mergeableRequirements.add(req);
			} else if (req.getScope().equals(RequirementScopes.INITIALLY)) {
				System.out.println("Could not merge requirement '" + req.getName() + "' because merging of initial requirements is not supported yet");
			} else {
				System.out.println("Could not merge requirement '" + req.getName() + "' because merging of trigger-response requirements is not supported yet");
			}
		}

		StringBuilder requirementNames = new StringBuilder();
		for (Requirement req : mergeableRequirements) {
			requirementNames.append(" '");
			requirementNames.append(req.getName());
			requirementNames.append("'");
		}
		System.out.println("Begin merging of " + mergeableRequirements.size() + " requirements:" + requirementNames.toString() + "...\n");

		System.out.println("Merging complete!\n");
		final String name = "MergedReq" + nameCounter;
		++nameCounter;

		return new Requirement(name, codeTreeNode, RequirementScopes.GLOBALLY);
	}
}
