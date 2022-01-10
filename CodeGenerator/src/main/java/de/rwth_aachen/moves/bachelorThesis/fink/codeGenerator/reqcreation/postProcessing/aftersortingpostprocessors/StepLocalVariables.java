package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.aftersortingpostprocessors;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IVariableController;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.SimpleVariableController;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.patterns.InvariantPatternTreeNode;

import java.util.*;

public class StepLocalVariables {
	public static List<Requirement> getStepCodeRequirementList(List<Requirement> requirements, IProgramContext programContext) {
		// Create local copy of Requirements. Step code tree is different from the rest
		List<Requirement> stepCodeRequirements = new ArrayList<>();

		// Create unrestricted variable controller which will create local variables according to their conditions (meaning it is restricted by the conditions anyway)
		IVariableController variableController = new SimpleVariableController(true, true);
		long variableCounter = 0;

		for (Requirement requirement : requirements) {
			// Store which nodes we want to replace with what
			// This is used so we minimize the amount of tree rebuilding we have to do
			Map<CodeTreeNode, CodeTreeNode> mapFromOriginalToReplacement = new HashMap<>();
			List<FormulaTreeNode> variableDeclarationAndAssignmentNodes = new ArrayList<>();

			// Get all conditions
			List<CodeTreeNode> allConditions = new ArrayList<>();
			retrieveAllConditions(requirement.getCodeTreeNode(), allConditions);

			// Get all output variables. We will filter our potential local variables with that,
			// so they dont become "stale"
			List<IVariable> allVariablesWhichWillBeAssigned = new ArrayList<>();
			retrieveAllAssignments(requirement.getCodeTreeNode(), allVariablesWhichWillBeAssigned);

			final int LOCAL_VARIABLES_TO_CREATE = allConditions.size();
			for (int localVariableCount = 0; localVariableCount < LOCAL_VARIABLES_TO_CREATE; localVariableCount++) {
				// Get condition to modify
				int conditionToPullOutIndex = RandomGenHelper.randomInt(0, allConditions.size() - 1);
				CodeTreeNode conditionToModify = allConditions.remove(conditionToPullOutIndex); // Do not choose the same condition multiple times

				FormulaTreeNode toReplace = null;
				{
					// Pick random side
					int randomSideChildIndex = RandomGenHelper.randomInt(0, 1);
					toReplace = conditionToModify.getChildren().get(randomSideChildIndex);

					// Check that the condition does not contain an output variable nor is a floating point
					if (conditionUsesVariables(toReplace, allVariablesWhichWillBeAssigned) || toReplace.getDynamicReturnType(programContext).isFloatingPoint()) {
						// Cant pick that side, pick the other one
						randomSideChildIndex = (randomSideChildIndex+1) % 2;
						toReplace = conditionToModify.getChildren().get(randomSideChildIndex);

						if (conditionUsesVariables(toReplace, allVariablesWhichWillBeAssigned) || toReplace.getDynamicReturnType(programContext).isFloatingPoint()) {
							// Does not work either, skip this condition
							continue;
						}
					}
				}

				// Create local variable
				VariableTreeNode variableTreeNode = null;
				{
					DataType typeOfStepLocalVariable = toReplace.getDynamicReturnType(programContext);
					String variableName = "stepLocal_" + variableCounter++;
					IVariable localVariable = variableController.createVariable(ParameterType.INTERNAL_SHADOW, typeOfStepLocalVariable, variableName, variableName, programContext, null);
					IVariableWithAccessor outputVariableWithAccessor = SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(localVariable);
					variableTreeNode = new VariableTreeNode(outputVariableWithAccessor, false);
				}

				// Create declaration node with assignment for our local variable
				FormulaTreeNode assignmentValueNode = (FormulaTreeNode) toReplace.copyTree();
				DeclarationOperator declarationOperator = new DeclarationOperator(ImmutableList.of(variableTreeNode, assignmentValueNode), false);

				// Store replacement and local variable declaration
				mapFromOriginalToReplacement.put(toReplace, variableTreeNode);
				variableDeclarationAndAssignmentNodes.add(declarationOperator);
			}

			Requirement modifiedRequirement = null;
			if ( ! mapFromOriginalToReplacement.isEmpty()) {
				// Rebuild tree with replacements
				modifiedRequirement = requirement.createCopyByReplacingNodes(mapFromOriginalToReplacement);

				// Chain all variable declarations together
				FormulaTreeNode rootOfDeclarations = variableDeclarationAndAssignmentNodes.remove(0);
				while (!variableDeclarationAndAssignmentNodes.isEmpty()) {
					rootOfDeclarations = new ConcatenationOperator(ImmutableList.of(variableDeclarationAndAssignmentNodes.remove(0), rootOfDeclarations), false);
				}

				// Finally, put variable declarations in front of requirement to get the result
				modifiedRequirement = modifiedRequirement.createCopyByInsertingInFront(rootOfDeclarations);
			} else {
				// Could not do any replacements, simply create copy
				modifiedRequirement = new Requirement(requirement);
			}
			stepCodeRequirements.add(modifiedRequirement);
		}

		return stepCodeRequirements;
	}

	/**
	 * Fills a list from a requirement with conditions,
	 * where all conditions are of the form (??? COMPARISON_OPERATOR ???)
	 */
	private static void retrieveAllConditions(CodeTreeNode treeToCheck, List<CodeTreeNode> listToFill) {
		if (treeToCheck instanceof InvariantPatternTreeNode) {
			retrieveAllConditions(((InvariantPatternTreeNode) treeToCheck).getChild(), listToFill);
		} else if (treeToCheck instanceof ConcatenationOperator) {
			for (FormulaTreeNode child : treeToCheck.getChildren()) {
				retrieveAllConditions(child, listToFill);
			}
		} else if (treeToCheck instanceof ItOperator || treeToCheck instanceof IteOperator) {
			ImmutableList<FormulaTreeNode> children = treeToCheck.getChildren();

			// First child is condition
			{
				// Only get parts of the condition which have the form
				// (??? COMPARISON_OPERATOR ???)
				FormulaTreeNode condition = children.get(0);
				if (condition instanceof OperatorTreeNode && condition.getChildren().size() == 2) {
					listToFill.add(condition);
				}
			}

			// Second child might be anything, needs to be checked
			retrieveAllConditions(children.get(1), listToFill);

			// Third child only exists in an if-else block (and might be anything)
			if (treeToCheck instanceof IteOperator) {
				retrieveAllConditions(children.get(2), listToFill);
			}
		}
	}

	private static void retrieveAllAssignments(CodeTreeNode treeToCheck, List<IVariable> listToFill) {
		if (treeToCheck instanceof InvariantPatternTreeNode) {
			retrieveAllAssignments(((InvariantPatternTreeNode) treeToCheck).getChild(), listToFill);
		} else if (treeToCheck instanceof OutputOperator) {
			listToFill.add(treeToCheck.getChildren().get(0).tryToReturnOutputVariable());
		} else {
			for (FormulaTreeNode child : treeToCheck.getChildren()) {
				retrieveAllAssignments(child, listToFill);
			}
		}
	}

	private static boolean conditionUsesVariables(CodeTreeNode treeToCheck, List<IVariable> listToCheckAgainst) {
		boolean result = false;
		if (treeToCheck instanceof InvariantPatternTreeNode) {
			result = conditionUsesVariables(((InvariantPatternTreeNode) treeToCheck).getChild(), listToCheckAgainst);
		} else if (treeToCheck instanceof VariableTreeNode) {
			VariableTreeNode variableNode = (VariableTreeNode) treeToCheck;
			IVariable variableToCheck = variableNode.getVariableWithAccessor().getVariable();
			result = listToCheckAgainst.contains(variableToCheck);
		} else {
			for (FormulaTreeNode child : treeToCheck.getChildren()) {
				result |= conditionUsesVariables(child, listToCheckAgainst);
			}
		}
		return result;
	}
}
