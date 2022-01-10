package de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ReturnOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.ControlStructureOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.AssignmentOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.NoOpVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.CodeObject;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.NonVoidFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.VoidFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FunctionCallTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.ISimpleValueTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;

import java.util.*;

public class RequirementFunctionizer {

	private static int nameCounter = 0;
	private final List<IFunction> generatedFunctions = new ArrayList<>();

	public RequirementFunctionizer(Set<IFunction> existingFunctions) {
		nameCounter += existingFunctions.size();
	}

	public ImmutableSet<IFunction> getGeneratedFunctions() {
		return ImmutableSet.copyOf(generatedFunctions);
	}

	public List<Requirement> functionizeRequirements(List<Requirement> requirements, int chanceOfFunctionizing, IProgramContext programContext) {
		List<Requirement> result = new ArrayList<>();
		for (Requirement requirement : requirements) {
			result.add(functionizeRequirement(requirement, chanceOfFunctionizing, programContext));
		}

		return result;
	}

	public Requirement functionizeRequirement(Requirement requirement, int chanceOfFunctionizing, IProgramContext programContext) {
		Requirement newRequirement = requirement.copy(programContext);

		final CodeTreeNode codeTreeNode = newRequirement.getCodeTreeNode();
		List<FormulaTreeNode> newChildren = new ArrayList<>();
		for (FormulaTreeNode formulaTreeNode : codeTreeNode.getChildren()) {
			newChildren.add(visitTreeAndFunctionizeOnChance(formulaTreeNode, chanceOfFunctionizing, programContext));
		}
		return newRequirement.replaceReqNode(codeTreeNode.replaceChildren(ImmutableList.copyOf(newChildren)));
	}


	public CodeObject functionizeCodeObject(CodeObject codeObject, int chanceOfFunctionizing, IProgramContext programContext) {
		CodeObject newCodeObject = new CodeObject(codeObject);

		final FormulaTreeNode codeNode = newCodeObject.getFormulaTreeNode();
		FormulaTreeNode modifiedTree = visitTreeAndFunctionizeOnChance(codeNode, chanceOfFunctionizing, programContext);
		return newCodeObject.replaceCodeNode(modifiedTree);
	}

	public FormulaTreeNode visitTreeAndFunctionizeOnChance(FormulaTreeNode formulaTreeNode, int chanceOfFunctionizing, IProgramContext programContext) {
		final boolean isAssignmentNode = (formulaTreeNode instanceof AssignmentOperator);
		if (isAssignmentNode) {
			final AssignmentOperator assignmentOperator = (AssignmentOperator) formulaTreeNode;
			assert (assignmentOperator.getChildren().size() == 2);
			final FormulaTreeNode left = assignmentOperator.getChildren().get(0);
			final FormulaTreeNode right = assignmentOperator.getChildren().get(1);
			return formulaTreeNode.replaceChildren(ImmutableList.of(left, visitTreeAndFunctionizeOnChance(right, chanceOfFunctionizing, programContext)));
		}

		final boolean replaceThisNodeByFunctionCall = RandomGenHelper.randomChance(chanceOfFunctionizing, 100);
		final boolean isTrivial = (formulaTreeNode instanceof VariableTreeNode) || (formulaTreeNode instanceof ISimpleValueTreeNode);
		if (replaceThisNodeByFunctionCall && !isTrivial) {
			return replaceNodeByFunctionCallAndCreateFunction(formulaTreeNode, programContext);
		} else {
			List<FormulaTreeNode> newChildren = new ArrayList<>();
			for (FormulaTreeNode childNode: formulaTreeNode.getChildren()) {
				newChildren.add(visitTreeAndFunctionizeOnChance(childNode, chanceOfFunctionizing, programContext));
			}
			return formulaTreeNode.replaceChildren(ImmutableList.copyOf(newChildren));
		}
	}

	public FormulaTreeNode replaceNodeByFunctionCallAndCreateFunction(FormulaTreeNode formulaTreeNode, IProgramContext programContext) {
		// Goal: build a function call to a function containing this functionality.
		// 1. Decide which values and variables are passed via parameters and which via globals
		final VariableCollector variableCollector = new VariableCollector(false, programContext);
		formulaTreeNode.accept(variableCollector);

		final Set<IVariableWithAccessor> variablesWithoutOutput = new HashSet<>(variableCollector.getUsedVariablesWithAccessor());
		variablesWithoutOutput.removeAll(variableCollector.getOutputVariablesWithAccessor());
		final Set<IVariableWithAccessor> variablesAsParameters = RandomGenHelper.randomSubset(variablesWithoutOutput, 50, 100);
		final Set<ISimpleValueTreeNode> constantNodes = RandomGenHelper.randomSubset(variableCollector.getConstantNodes(), 25, 100);

		final VariableReplacer variableReplacer = new VariableReplacer(new HashSet<>(), variablesAsParameters, "functionized" + nameCounter + "_localFunction");
		List<IVariableWithAccessor> functionParameters = new ArrayList<>();
		List<FormulaTreeNode> functionCallParameters = new ArrayList<>();
		for (IVariableWithAccessor v : variablesAsParameters) {
			IVariableWithAccessor replacement = variableReplacer.getReplacement(v, null);
			functionParameters.add(replacement);

			FormulaTreeNode parameterNode = new VariableTreeNode(v, false);
			functionCallParameters.add(parameterNode);

			// NOTE(Felix): We have to add this variable to the programContext, because otherwise we get some
			//              errors when trying to call "getResultingExpressionConditioner" on a node for this function.
			try {
				programContext.addGlobalVariable(replacement.getVariable(), parameterNode.getResultingExpressionConditioner(programContext));
			} catch (Exception e) {
				throw new RuntimeException("Something horrible happened when trying to make this variable accessible by name.");
			}
		}

		Map<FormulaTreeNode, IVariableWithAccessor> nodeReplacements = new HashMap<>();
		int currentParameterCount = variableReplacer.getVariableCounter();
		for (ISimpleValueTreeNode simpleValueTreeNode : constantNodes) {
			IVariable newParameter = programContext.addVariable(ParameterType.INTERNAL_CONTROL, simpleValueTreeNode.getDynamicReturnType(programContext), "localFunctionVar" + currentParameterCount, "functionized" + nameCounter + "localFunctionVar" + currentParameterCount);
			IVariableWithAccessor newParameterWithAccessor = SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(newParameter);
			++currentParameterCount;
			nodeReplacements.put((FormulaTreeNode) simpleValueTreeNode, newParameterWithAccessor);
			functionParameters.add(newParameterWithAccessor);
			functionCallParameters.add((FormulaTreeNode) simpleValueTreeNode);
		}

		IFunction function;
		final boolean isControlStructureOperator = formulaTreeNode instanceof ControlStructureOperator;
		if (isControlStructureOperator) {
			// This will be a void function
			function = new VoidFunction("functionized" + nameCounter, functionParameters, replaceNodesByFunctionParameters(formulaTreeNode, variableReplacer, nodeReplacements));
		} else {
			// Non-Void
			FormulaTreeNode returnValueNode = replaceNodesByFunctionParameters(formulaTreeNode, variableReplacer, nodeReplacements);
			ReturnOperator returnNode = new ReturnOperator(ImmutableList.of(returnValueNode), false);
			function = new NonVoidFunction("functionized" + nameCounter, functionParameters, returnNode, formulaTreeNode.getDynamicReturnType(programContext));
		}
		++nameCounter;
		generatedFunctions.add(function);
		programContext.addFunction(function);

		return new FunctionCallTreeNode(function.getName(), functionCallParameters, formulaTreeNode.isInOutput());
	}

	private static FormulaTreeNode replaceNodesByFunctionParameters(FormulaTreeNode formulaTreeNode, IVariableReplacer variableReplacer, Map<FormulaTreeNode, IVariableWithAccessor> nodeReplacements) {
		if ((formulaTreeNode instanceof VariableTreeNode)) {
			final VariableTreeNode variableTreeNode = (VariableTreeNode) formulaTreeNode;
			if (formulaTreeNode.isInOutput()) {
				return variableTreeNode.copyTree(new NoOpVariableReplacer());
			}
			return variableTreeNode.copyTree(variableReplacer);
		} else if (nodeReplacements.containsKey(formulaTreeNode)) {
			final IVariableWithAccessor replacementVariable = nodeReplacements.get(formulaTreeNode);
			return new VariableTreeNode(replacementVariable, false);
		}

		List<FormulaTreeNode> newChildren = new ArrayList<>();
		for (FormulaTreeNode child: formulaTreeNode.getChildren()) {
			newChildren.add(replaceNodesByFunctionParameters(child, variableReplacer, nodeReplacements));
		}
		return formulaTreeNode.replaceChildren(ImmutableList.copyOf(newChildren));
	}

}
