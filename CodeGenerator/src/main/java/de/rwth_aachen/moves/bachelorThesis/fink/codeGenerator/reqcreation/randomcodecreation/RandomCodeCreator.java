package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.randomcodecreation;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.moduling.ModuleFactory;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.moduling.ModuleInstance;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.moduling.ModuleTemplateInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.RequirementScopes;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IVariableController;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IBooleanRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IFloatingPointRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.OperatorReturnType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.NoOpVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.CodeObject;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.patterns.InvariantPatternTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.ReqTreeValidator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;


/**
 * Check the interface for a rough outline.
 */
public class RandomCodeCreator {
	protected static final Logger logger = LogManager.getLogger(RandomCodeCreator.class);

	protected static ModuleFactory moduleFactory = null;

	public static Pair<Requirement, List<Property>> createRequirementAndProperties(CodePropertiesConfig config, String requirementName, int nodeAmountToCreate, IVariableController variableController, IProgramContext programContext, Set<IVariable> dontUseTheseVariables) {

		System.out.println("Amount of nodes expected: " + nodeAmountToCreate);
		Pair<FormulaTreeNode, List<FormulaTreeNode>> codeTreeAndProperties = mergeControlStructures(createCodeTree(config, programContext, variableController, nodeAmountToCreate, dontUseTheseVariables));
		Requirement requirement = new Requirement(requirementName, new InvariantPatternTreeNode(codeTreeAndProperties.getValue0()), RequirementScopes.GLOBALLY);
		List<Property> properties = new ArrayList<>();
		int counter = 1;
		for (FormulaTreeNode node : codeTreeAndProperties.getValue1()) {
			properties.add(new Property(requirementName + "_Prop" + counter + "_", new InvariantPatternTreeNode(node), RequirementScopes.GLOBALLY, new ArrayList<>(List.of(requirementName))));
			counter++;
		}

		return new Pair<>(requirement, properties);
	}

	private static int countOperationsInTree(FormulaTreeNode treeNode) {
		final boolean countVariables = false;
		final boolean countConstants = false;
		if (treeNode instanceof AssignmentOperator) {
			final AssignmentOperator assignmentOperator = (AssignmentOperator) treeNode;
			return 1 + countOperationsInTree(assignmentOperator.getChildren().get(1));
		} else if (treeNode instanceof ISimpleValueTreeNode) {
			return ((countConstants) ? 1 : 0);
		} else if (treeNode instanceof VariableTreeNode) {
			return ((countVariables) ? 1 : 0);
		}

		int result = 1;
		if (treeNode instanceof EmptyControlStructure) {
			result = 0;
		}
		for (FormulaTreeNode child : treeNode.getChildren()) {
			result += countOperationsInTree(child);
		}
		return result;
	}

	public static List<Pair<Requirement, List<Property>>> createRequirementsAndProperties(CodePropertiesConfig config, List<Integer> nodeAmounts, int totalNodeAmount, IVariableController variableController, IProgramContext programContext, Set<IVariable> dontUseTheseVariables) {

		System.out.println("Amount of nodes expected: " + totalNodeAmount);
		int realNodeCount = 0;

		List<Pair<Requirement, List<Property>>> result = new ArrayList<>();
		int i = 0;
		while (realNodeCount < totalNodeAmount) {
			final int currentAmount = (i < nodeAmounts.size() && nodeAmounts.get(i) <= (totalNodeAmount - realNodeCount)) ? nodeAmounts.get(i) : (totalNodeAmount - realNodeCount);
			List<Pair<FormulaTreeNode, List<FormulaTreeNode>>> codeTreeAndProperties = createCodeTree(config, programContext, variableController, currentAmount, dontUseTheseVariables);
			int operationSum = 0;
			for (Pair<FormulaTreeNode, List<FormulaTreeNode>> pair: codeTreeAndProperties) {
				final String requirementName = "Req" + (i + 1);
				Requirement requirement = new Requirement(requirementName, new InvariantPatternTreeNode(pair.getValue0()), RequirementScopes.GLOBALLY);
				final int operationsInRequirement = countOperationsInTree(pair.getValue0());
				List<Property> properties = new ArrayList<>();
				int counter = 1;
				for (FormulaTreeNode node : pair.getValue1()) {
					properties.add(new Property(requirementName + "_Prop" + counter + "_", new InvariantPatternTreeNode(node), RequirementScopes.GLOBALLY, new ArrayList<>(List.of(requirementName))));
					++counter;
				}
				result.add(new Pair<>(requirement, properties));
				++i;
				operationSum += operationsInRequirement;
			}
			realNodeCount += operationSum;
			System.out.println("Requested " + currentAmount + " operations to be generated, got " + operationSum + ". Total count: " + realNodeCount);
		}
		return result;
	}

	public static CodeObject createCodeObject(CodePropertiesConfig config, String codeObjectName, int nodeAmountToCreate, IVariableController variableController, IProgramContext programContext, Set<IVariable> dontUseTheseVariables) {


		System.out.println("Amount of nodes expected: " + nodeAmountToCreate);
		Pair<FormulaTreeNode, List<FormulaTreeNode>> codeTreeAndProperties = mergeControlStructures(createCodeTree(config, programContext, variableController, nodeAmountToCreate, dontUseTheseVariables));

		return new CodeObject(codeObjectName, codeTreeAndProperties.getValue0());
	}

	public static List<CodeObject> createCodeObjects(CodePropertiesConfig config, int totalNodeAmount, IVariableController variableController, IProgramContext programContext, Set<IVariable> dontUseTheseVariables) {
		List<Integer> nodeAmount = RandomGenHelper.splitIntoRandomParts(totalNodeAmount, 5);


		System.out.println("Amount of nodes expected: " + totalNodeAmount);
		int realNodeCount = 0;

		List<CodeObject> result = new ArrayList<>();
		int i = 0;
		while (realNodeCount < totalNodeAmount) {
			final int currentAmount = (i < nodeAmount.size()) ? nodeAmount.get(i) : (totalNodeAmount - realNodeCount);
			List<Pair<FormulaTreeNode, List<FormulaTreeNode>>> codeTreeAndProperties = createCodeTree(config, programContext, variableController, currentAmount, dontUseTheseVariables);
			int operationSum = 0;
			for (Pair<FormulaTreeNode, List<FormulaTreeNode>> pair : codeTreeAndProperties) {
				final String codeObjectName = "CodeObject" + (i + 1);
				CodeObject codeObject = new CodeObject(codeObjectName, pair.getValue0());
				final int operationsInCodeObject = countOperationsInTree(pair.getValue0());
				result.add(codeObject);
				++i;
				operationSum += operationsInCodeObject;
			}
			realNodeCount += operationSum;
			System.out.println("Requested " + currentAmount + " operations to be generated, got " + operationSum + ". Total count: " + realNodeCount);
		}

		return result;
	}


	private static Pair<FormulaTreeNode, List<FormulaTreeNode>> mergeControlStructures(List<Pair<FormulaTreeNode, List<FormulaTreeNode>>> list) {
		FormulaTreeNode treeNode = null;
		List<FormulaTreeNode> properties = new ArrayList<>();
		for (Pair<FormulaTreeNode, List<FormulaTreeNode>> pair : list) {
			if (treeNode == null) {
				treeNode = pair.getValue0();
			} else {
				treeNode = new ConcatenationOperator(ImmutableList.of(treeNode, pair.getValue0()), false);
			}
			properties.addAll(pair.getValue1());
		}
		return new Pair<>(treeNode, properties);
	}

	/**
	 * Creates the trees and returns the roots of them.
	 * Often, a single control structure (e.g. an if-else block) is not enough to satisfy the required nodeAmount.
	 * In these cases, more than one control structure will be created and returned. These can then be "concatenated" using mergeControlStructures(), if required.
	 *
	 * @return root node of the created tree cast to a FormulaTreeNode
	 */
	private static List<Pair<FormulaTreeNode, List<FormulaTreeNode>>> createCodeTree(CodePropertiesConfig config, IProgramContext programContext, IVariableController variableController, int nodeAmountToCreate, Set<IVariable> dontUseTheseVariables) {
		SimpleDepthInformation depthInformation = new SimpleDepthInformation();

		List<Pair<FormulaTreeNode, List<FormulaTreeNode>>> result = new ArrayList<>();
		do {
			IVariable outputVariable = variableController.createNewOutputVariable(config.getOperatorsToProbability(), programContext);
			IVariableWithAccessor outputVariableWithAccessor = SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(outputVariable);
			VariableTreeNode variableTreeNode = new VariableTreeNode(outputVariableWithAccessor, true);
			OperatorTreeNode variableOutputTreeNode = new OutputOperator(ImmutableList.of(variableTreeNode), true);

			Set<IVariable> doNotUseInCondition = new HashSet<>(dontUseTheseVariables);
			doNotUseInCondition.add(outputVariable);
			Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>> requirement_LocalProperties_GlobalProperties = createControlStructureOrModule(config, variableOutputTreeNode, variableController, depthInformation, nodeAmountToCreate, doNotUseInCondition, programContext);

			// Store resulting RequirementNode
			FormulaTreeNode requirementNode = requirement_LocalProperties_GlobalProperties.getValue0();
			// Sanity check
			if (checkForLoops(requirementNode, new HashSet<>())) {
				throw new RuntimeException("Error: Detected a loop in the generated tree. This should never happen!");
			}

			// Store local properties
			FormulaTreeNode localProperty = null;
			for (FormulaTreeNode property : requirement_LocalProperties_GlobalProperties.getValue1()) {
				if (localProperty == null) {
					localProperty = property;
				} else {
					localProperty = new ConcatenationOperator(ImmutableList.of(localProperty, property), false);
				}
			}

			// Directly store global properties
			List<FormulaTreeNode> properties = new ArrayList<>(requirement_LocalProperties_GlobalProperties.getValue2());
			properties.add(localProperty);

			// Assert that we "properly handled" each level of depth we created
			assert (depthInformation.getCurrentDepth() == 0);

			result.add(new Pair<>(requirementNode, properties));
		} while ((nodeAmountToCreate - depthInformation.getTotalNodeCount()) > 10);

		if (!config.getReduceConsoleOutput()) {
			System.out.println("Nodes created: " + depthInformation.getTotalNodeCount() + " Nodes to create: " + nodeAmountToCreate + " Depth: " + depthInformation.getCurrentDepth());
		}
		return result;
	}

	private static Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>> createControlStructureOrModule(CodePropertiesConfig config, FormulaTreeNode outputVariable, IVariableController variableController, SimpleDepthInformation depthInformation, int nodeAmountToCreate, Set<IVariable> doNotUseInCondition, IProgramContext programContext) {
		if (config.useModuleTemplates() && RandomGenHelper.randomChance(10, 100)) {
			return createModule(config, outputVariable, variableController, depthInformation, nodeAmountToCreate, doNotUseInCondition, programContext);
		} else {
			return createControlStructure(config, outputVariable, variableController, depthInformation, nodeAmountToCreate, doNotUseInCondition, programContext);
		}
	}

	private static Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>> createModule(CodePropertiesConfig config, FormulaTreeNode outputVariable, IVariableController variableController, SimpleDepthInformation depthInformation, int nodeAmountToCreate, Set<IVariable> doNotUseInCondition, IProgramContext programContext) {
		if (moduleFactory == null) {
			moduleFactory = new ModuleFactory(config.getModuleTemplatePath(), programContext, variableController, config);
		}

		ModuleTemplateInformation moduleTemplateInformation = moduleFactory.generateModuleTemplateInformation();

		// Fill conditions if necessary
		List<FormulaTreeNode> conditionChildren = createConditionTreeNodes(moduleTemplateInformation.getCONDITION_ARGUMENT_COUNT(), depthInformation, nodeAmountToCreate, config, variableController, programContext, doNotUseInCondition, Operators.MACRO);

		// Fill code blocks (e.g. 1 code block in an if, 2 code blocks in an if-else)
		Pair<List<Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>>>, SimpleResultingExpressionConditioner> codeTreeNodeOutput = createCodeTreeNodes(moduleTemplateInformation.getCODE_ARGUMENT_COUNT(), depthInformation, nodeAmountToCreate, config, outputVariable, variableController, programContext, doNotUseInCondition);
		List<FormulaTreeNode> codeChildren = new ArrayList<>();
		for (Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>> triplet : codeTreeNodeOutput.getValue0()) {
			codeChildren.add(triplet.getValue0());
		}
		SimpleResultingExpressionConditioner resultingExpressionConditioner = codeTreeNodeOutput.getValue1();

		ModuleInstance moduleInstance = moduleFactory.generateModule(moduleTemplateInformation.getNAME(), conditionChildren, codeChildren, programContext, variableController);

		depthInformation.increaseNodeCount(moduleTemplateInformation.getNODE_COUNT());

		return new Triplet<>(moduleInstance.getCodeTree(), moduleInstance.getLocalProperties(), moduleInstance.getGlobalProperties());
	}


	private static Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>> createControlStructure(CodePropertiesConfig config, FormulaTreeNode outputVariable, IVariableController variableController, SimpleDepthInformation depthInformation, int nodeAmountToCreate, Set<IVariable> doNotUseInCondition, IProgramContext programContext) {
		// Randomly select the kind of control structure to create
		Operators selectedOperator;
		EnumSet<Operators> allowedControlStructureOperators = Operators.getControlStructureOperators();
		allowedControlStructureOperators.remove(Operators.CONCATENATION);
		// We only want to allow an empty control structure if we don't want a control structure at all
		if (depthInformation.getCurrentDepth() > 0) allowedControlStructureOperators.remove(Operators.EMPTY_CS);
		if (nodeAmountToCreate <= 5) {
			allowedControlStructureOperators.remove(Operators.ITE);
			if (nodeAmountToCreate <= 3) {
				allowedControlStructureOperators.remove(Operators.IT);
			}
		}
		selectedOperator = RandomGenHelper.getRandomElementFromMap(config.getOperatorsToProbability(), new ArrayList<>(allowedControlStructureOperators));

		// If the operator is an empty control structure then we do not want to increment as this is just a helper to create an unguarded assignment
		if (selectedOperator != Operators.EMPTY_CS) {
			depthInformation.goIntoDepthAndIncrementNodeCount();
		}

		// The base node of the resulting requirement and property control structures
		ControlStructureOperator requirementNode = null;
		ControlStructureOperator propertyNode = null;

		// Fill conditions if necessary and create copies for requirement and property
		List<FormulaTreeNode> conditionChildren = createConditionTreeNodes(selectedOperator.getConditionArgumentCount(), depthInformation, nodeAmountToCreate, config, variableController, programContext, doNotUseInCondition, selectedOperator);
		List<FormulaTreeNode> conditionChildrenRequirement = new ArrayList<>();
		List<FormulaTreeNode> conditionChildrenProperty = new ArrayList<>();
		for (FormulaTreeNode child : conditionChildren) {
			conditionChildrenRequirement.add((FormulaTreeNode) child.copyTree());
			conditionChildrenProperty.add((FormulaTreeNode) child.copyTree());
		}

		// Fill code blocks (e.g. 1 code block in an if, 2 code blocks in an if-else)
		Pair<List<Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>>>, SimpleResultingExpressionConditioner> codeTreeNodeOutput = createCodeTreeNodes(selectedOperator.getCodeArgumentCount(), depthInformation, nodeAmountToCreate, config, outputVariable, variableController, programContext, doNotUseInCondition);
		SimpleResultingExpressionConditioner resultingExpressionConditioner = codeTreeNodeOutput.getValue1();
		List<FormulaTreeNode> codeChildrenRequirement = new ArrayList<>();
		List<FormulaTreeNode> globalProperties = new ArrayList<>();
		List<FormulaTreeNode> codeChildrenProperty = new ArrayList<>();
		for (Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>> triplet : codeTreeNodeOutput.getValue0()) {
			codeChildrenRequirement.add(triplet.getValue0());
			FormulaTreeNode localChild = null;
			for (FormulaTreeNode node : triplet.getValue1()) {
				if (localChild == null) {
					localChild = node;
				} else {
					localChild = new ConcatenationOperator(ImmutableList.of(localChild, node), false);
				}
			}
			codeChildrenProperty.add(localChild);
			globalProperties.addAll(triplet.getValue2());
		}

		List<FormulaTreeNode> childrenRequirement = new ArrayList<>(conditionChildrenRequirement);
		childrenRequirement.addAll(codeChildrenRequirement);
		List<FormulaTreeNode> childrenProperty = new ArrayList<>(conditionChildrenProperty);
		childrenProperty.addAll(codeChildrenProperty);

		// Create the actual Operator instance holding all the data we generated.
		requirementNode = (ControlStructureOperator) selectedOperator.operator(ImmutableList.copyOf(childrenRequirement), false);
		propertyNode = (ControlStructureOperator) selectedOperator.operator(ImmutableList.copyOf(childrenProperty), false);

		requirementNode.setCombinedOutputConditioner(resultingExpressionConditioner);
		propertyNode.setCombinedOutputConditioner(resultingExpressionConditioner);

		// Finished with this control structure (meaning this depth as well), hence pop and return result.
		if (selectedOperator != Operators.EMPTY_CS) {
			depthInformation.goOutOfDepth();
		}
		return new Triplet<>(requirementNode, new ArrayList<>(List.of(propertyNode)), globalProperties);
	}

	private static List<FormulaTreeNode> createConditionTreeNodes(int amount, SimpleDepthInformation depthInformation, int nodeAmountToCreate, CodePropertiesConfig config, IVariableController variableController, IProgramContext programContext, Set<IVariable> doNotUseInCondition, Operators selectedOperator) {
		List<FormulaTreeNode> nodes = new ArrayList<>();
		for (int i = 0; i < amount; ++i) {
			// Get all available Operators (meaning assigned weight is > 0) which can be used within the condition
			SimpleChildExpressionConditioner simpleExpressionConditioner;
			EnumSet<Operators> availableOps = RandomGenHelper.getNonZeroOperators(config.getOperatorsToProbability());
			availableOps.removeAll(Operators.getControlStructureOperators());

			SimpleChildExpressionConditionerBuilder simpleExpressionConditionerBuilder;
			if (Collections.disjoint(availableOps, Operators.getPossibleChildOperators(OperatorReturnType.GENERAL_BOOLEAN)) || config.getForbiddenDataTypeLevels().contains(0)) {
				simpleExpressionConditionerBuilder = new SimpleChildExpressionConditionerBuilder(programContext.getCurrentlyDefinedTypes().oneOf(programContext.getCurrentlyDefinedTypes().all()));
			} else {
				simpleExpressionConditionerBuilder = new SimpleChildExpressionConditionerBuilder(DataType.INSTANCE_BOOL);
			}
			simpleExpressionConditioner = simpleExpressionConditionerBuilder.setCanBeASimpleValue(false).build();

			// (Try to) Create condition
			FormulaTreeNode child;
			try {
				do {
					child = createCondition(null, true, simpleExpressionConditioner, config, programContext, variableController, depthInformation, nodeAmountToCreate, doNotUseInCondition, new SimpleDepthInformation(), 2);
					ReqTreeValidator.validateTree(child, programContext);
					if (!child.getResultingExpressionConditioner(programContext).isCompatibleWith(simpleExpressionConditioner, programContext.getCurrentlyDefinedTypes())) {
						throw new RuntimeException("Resulting condition does not match requirements:\nRequired:\n" + simpleExpressionConditioner + "\nResult:\n" + child.getResultingExpressionConditioner(programContext).toString());
					}
				} while (!isConditionNonConstant(child));
			} catch (UnsatisfiableConstraintsException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not create a condition for control structure with type '" + selectedOperator + "'! Exception: " + e.getLocalizedMessage());
			}

			// If condition is of form "if (variable)" (internally: variable is boolean), dont use the variable in conditions afterwards
			if (child instanceof VariableTreeNode) {
				VariableTreeNode variableTreeNode = (VariableTreeNode) child;
				doNotUseInCondition.add(variableTreeNode.getVariableWithAccessor().getVariable());
			}

			// Store condition
			nodes.add(child);
		}
		return nodes;
	}


	private static Pair<List<Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>>>, SimpleResultingExpressionConditioner> createCodeTreeNodes(int amount, SimpleDepthInformation depthInformation, int nodeAmountToCreate, CodePropertiesConfig config, FormulaTreeNode outputVariable, IVariableController variableController, IProgramContext programContext, Set<IVariable> doNotUseInCondition) {
		List<Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>>> nodes = new ArrayList<>();
		SimpleResultingExpressionConditioner resultingExpressionConditioner = null;
		for (int i = 0; i < amount; ++i) {
			// We might, by accident/chance, e.g. generate a chain of if-conditions which are satisfiable
			// on their own, but not all of them at the same time.
			// If that happens, we just re-try the latest component.
			while (true) {
				Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>> child;

				// Randomly decide whether we will create the "end of this branch" by inserting an assignment or keep stacking control structures
				boolean createAssignment = !goIntoDepth(depthInformation, nodeAmountToCreate);
				if (!createAssignment) {
					child = createControlStructureOrModule(config, outputVariable, variableController, depthInformation, nodeAmountToCreate, doNotUseInCondition, programContext);
				} else {
					child = createAssignment(config, programContext, outputVariable, variableController, depthInformation, nodeAmountToCreate, doNotUseInCondition);
				}

				try {
					// Get conditioner of child we just created
					final SimpleResultingExpressionConditioner childConditioner;
					if (createAssignment) {
						childConditioner = child.getValue0().getResultingExpressionConditioner(programContext);
					} else {
						if (child.getValue0() instanceof ControlStructureOperator) {
							final ControlStructureOperator c = (ControlStructureOperator) child.getValue0();
							childConditioner = c.getCombinedOutputConditioner();
						} else {
							// If we are here we created a module
							childConditioner = null;
						}
					}

					// Combine previous conditioners with new child conditioner (if possible)
					if (resultingExpressionConditioner == null) {
						resultingExpressionConditioner = childConditioner;
					} else {
						resultingExpressionConditioner = resultingExpressionConditioner.or(childConditioner);
					}
				} catch (UnsatisfiableConstraintsException e) {
					logger.info("Redoing child creation for ControlStructure, failed to create resulting expression conditioner: " + e.getLocalizedMessage());
					continue;
				}

				// If we reached this point, this iteration of generation went well. Store result (and hence break out of infinite loop)
				ReqTreeValidator.validateTree(child.getValue0(), programContext);
				nodes.add(child);
				break;
			}
		}
		return new Pair<>(nodes, resultingExpressionConditioner);
	}

	private static boolean isConditionNonConstant(FormulaTreeNode node) {
		if (node instanceof VariableTreeNode) {
			return true;
		}

		for (FormulaTreeNode n : node.getChildren()) {
			if (isConditionNonConstant(n)) {
				return true;
			}
		}

		return false;
	}

	/*
		Should produce an expression of the form a = EXPR; or a[b] = EXPR;
	 */
	private static Triplet<FormulaTreeNode, List<FormulaTreeNode>, List<FormulaTreeNode>> createAssignment(CodePropertiesConfig config, IProgramContext programContext, FormulaTreeNode outputVariable, IVariableController variableController, SimpleDepthInformation depthInformation, int nodeAmountToCreate, Set<IVariable> doNotUseInCondition) {
		depthInformation.goIntoDepthAndIncrementNodeCount(); // Because we return an AssignmentOperator with children at the and

		// Left side: our output variable.
		FormulaTreeNode leftChild = (FormulaTreeNode) outputVariable.copyTree(new NoOpVariableReplacer());


		// Right side: some assignment code.

		// Prepare the kind of value we expect to be getting from the right side
		SimpleChildExpressionConditionerBuilder simpleChildExpressionConditionerBuilder = new SimpleChildExpressionConditionerBuilder(outputVariable.getDynamicReturnType(programContext));
		if (!config.isUseFloats()) {
			simpleChildExpressionConditionerBuilder.removePossibleReturnType(DataType.INSTANCE_FLOAT);
			simpleChildExpressionConditionerBuilder.removePossibleReturnType(DataType.INSTANCE_DOUBLE);
		}
		simpleChildExpressionConditionerBuilder.restrictRangeForOutputVariable();
		SimpleChildExpressionConditioner simpleExpressionConditioner = simpleChildExpressionConditionerBuilder.build();

		// Generate the expression (by abusing createCondition)
		FormulaTreeNode rightChild;
		try {
			rightChild = createCondition(VariableTreeNode.findFirstVariableTreeNode(outputVariable).getVariableWithAccessor().getVariable(), true, simpleExpressionConditioner, config, programContext, variableController, depthInformation, nodeAmountToCreate, doNotUseInCondition, new SimpleDepthInformation(), 2);
		} catch (UnsatisfiableConstraintsException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not create an assignment for output variable with type '" + outputVariable.getDynamicReturnType(programContext) + "'! Exception: " + e.getLocalizedMessage());
		}

		// Combine left and right side into an assignment operator used within our tree
		FormulaTreeNode result = new AssignmentOperator(ImmutableList.of(leftChild, rightChild), false);
		depthInformation.goOutOfDepth();
		return new Triplet<>(result, new ArrayList<>(List.of(result)), new ArrayList<>());
	}

	/*
		Should produce an expression to be used in if(C) or as assignment expression or similar.
	 */
	private static FormulaTreeNode createCondition(IVariable assignmentVar, boolean standsAlone, SimpleChildExpressionConditioner expressionConditioner, CodePropertiesConfig config, IProgramContext programContext, IVariableController variableController, SimpleDepthInformation depthInformation, int nodeAmountToCreate, Set<IVariable> doNotUseInCondition, SimpleDepthInformation conditionDepth, int maxConditionDepth) throws UnsatisfiableConstraintsException {
		final boolean requiresConstant = expressionConditioner.getPossibleChildOperators().contains(Operators.PLACEHOLDER_CONSTANT);
		final boolean requiresVariable = expressionConditioner.getPossibleChildOperators().contains(Operators.PLACEHOLDER_VARIABLE);
		final boolean requiresNewVar = variableController.doWeHaveToCreateNewVariable(expressionConditioner, assignmentVar, standsAlone, doNotUseInCondition, programContext);
		final boolean goIntoDepth = goIntoDepth(depthInformation, conditionDepth, nodeAmountToCreate, requiresNewVar, maxConditionDepth);
		if (requiresConstant || requiresVariable || !goIntoDepth) {
			conditionDepth.incrementNodeCount();
			FormulaTreeNode result;
			if (requiresConstant) {
				result = createSimpleValueTreeNode(config, programContext, expressionConditioner);
			} else if (requiresVariable) {
				result = createVariable(config, programContext, assignmentVar, variableController, expressionConditioner, doNotUseInCondition);
			} else if (RandomGenHelper.randomChance(config.getChanceConstantVsVariable() + ((requiresNewVar ? 1 : 0) * config.getChanceConstantVsVariable() / 2), 100) && expressionConditioner.isCanBeASimpleValue()) {
				result = createSimpleValueTreeNode(config, programContext, expressionConditioner);
			} else {
				result = createVariable(config, programContext, assignmentVar, variableController, expressionConditioner, doNotUseInCondition);
			}
			// Validate created node
			ReqTreeValidator.validateTree(result, programContext);
			return result;
		} else {
			Set<DataType> possibleTypes = expressionConditioner.getPossibleReturnTypes();
			while (true) {
				if (possibleTypes.size() == 0) {
					throw new UnsatisfiableConstraintsException("Could not build expression based on constraints!");
				}

				// Select a type
				final DataType selectedType = programContext.getCurrentlyDefinedTypes().oneOf(possibleTypes);

				// The result must be negative, so if an unsigned type is selected, it will not work.
				if (!selectedType.isSigned()) {
					IIntegerRange integerRange = (IIntegerRange) expressionConditioner.getRange();
					if (!integerRange.canBeZero() && !integerRange.canBePositive()) {
						possibleTypes.remove(selectedType);
						continue;
					}
				}

				SimpleChildExpressionConditioner restrictedChildExpressionConditioner = expressionConditioner.restrictToType(selectedType);
				EnumSet<Operators> possibleChildOperators = Operators.getPossibleChildOperatorsByType(restrictedChildExpressionConditioner.getPossibleReturnTypes(), restrictedChildExpressionConditioner.getRange().isRestricted());

				boolean wasSatisfiable = false;
				while (true) {
					if (possibleChildOperators.size() == 0) {
						possibleTypes.remove(selectedType);
						break;
					}

					Operators selectedOperator;
					selectedOperator = RandomGenHelper.getRandomElementFromMap(config.getOperatorsToProbability(), new ArrayList<>(possibleChildOperators));

					SimpleDepthInformation oldDepthInformation = depthInformation.copy();
					depthInformation.goIntoDepthAndIncrementNodeCount();
					SimpleDepthInformation oldConditionDepth = conditionDepth.copy();
					conditionDepth.goIntoDepthAndIncrementNodeCount();

					List<SimpleResultingExpressionConditioner> previousChildConditioners = new ArrayList<>();
					List<FormulaTreeNode> children = new ArrayList<>();
					// This is refined with all variables used in the left sub-tree preventing 1 + var1 < var1
					Set<IVariable> internalDoNotUseInCondition = new HashSet<>(doNotUseInCondition);
					for (int i = 0; i < selectedOperator.getArgumentCount(); ++i) {
						try {
							SimpleChildExpressionConditioner childExpressionConditioner = selectedOperator.getChildExpressionConditioner(i, restrictedChildExpressionConditioner, previousChildConditioners, programContext.getCurrentlyDefinedTypes());
							if (!config.isUseFloats()) {
								childExpressionConditioner = childExpressionConditioner.removeFloats(programContext.getCurrentlyDefinedTypes());
							}
							FormulaTreeNode child;
							boolean continueGeneration;
							SimpleDepthInformation oldDepthInformation2 = depthInformation.copy();
							SimpleDepthInformation oldConditionDepth2 = conditionDepth.copy();
							do {
								depthInformation.revertState(oldDepthInformation2);
								conditionDepth.revertState(oldConditionDepth2);
								child = createCondition(assignmentVar, false, childExpressionConditioner, config, programContext, variableController, depthInformation, nodeAmountToCreate, internalDoNotUseInCondition, conditionDepth, maxConditionDepth);
								continueGeneration = false;

								// Check that no argument is a duplicate
								for (int j = 0; j < i; ++j) {
									if (child.equals(children.get(j))) {
										logger.debug("Generating new child for operator {} since children {} and {} are equal.", selectedOperator, j, i);
										continueGeneration = true;
										break;
									}
								}
								// Check that both sides of an && and || are non-static
								// There is only a small chance that it will return with a constant on one (or both) side(s)
								if (child instanceof AndOperator || child instanceof OrOperator) {
									final boolean leftIsConstant = child.getChildren().get(0).isConstant();
									final boolean rightIsConstant = child.getChildren().get(1).isConstant();
									if ((leftIsConstant || rightIsConstant) && RandomGenHelper.randomChance(95, 100)) {
										continueGeneration = true;
									}
								}
							} while (continueGeneration);

							final SimpleResultingExpressionConditioner resultingExpressionConditioner;
							try {
								resultingExpressionConditioner = child.getResultingExpressionConditioner(programContext);
								if ( ! resultingExpressionConditioner.isCompatibleWith(childExpressionConditioner, programContext.getCurrentlyDefinedTypes())) {
									throw new UnsatisfiableConstraintsException("Generated ourselves into a corner");
								}
							} catch (UnsatisfiableConstraintsException error) {
								// We are either running into a wall or something we cant model at the moment,
								// so retry generation
								depthInformation.revertState(oldDepthInformation);
								conditionDepth.revertState(oldConditionDepth);
								wasSatisfiable = false;
								break;
							}


							children.add(child);
							previousChildConditioners.add(resultingExpressionConditioner);
							wasSatisfiable = true;

							VariableCollector variableCollector = new VariableCollector(false, programContext);
							child.accept(variableCollector);
							internalDoNotUseInCondition.addAll(variableCollector.getUsedVariables());
						} catch (UnsatisfiableConstraintsException e) {
							// Try again!
							possibleChildOperators.remove(selectedOperator);
							depthInformation.revertState(oldDepthInformation);
							conditionDepth.revertState(oldConditionDepth);
							wasSatisfiable = false;
							break;
						}
					}

					if (wasSatisfiable) {
						depthInformation.goOutOfDepth();
						conditionDepth.goOutOfDepth();
						OperatorTreeNode node = selectedOperator.operator(ImmutableList.copyOf(children), false);
						node.setPreCondition(expressionConditioner);
						return node;
					}
				}
			}
		}
	}

	private static boolean checkForLoops(FormulaTreeNode node, Set<FormulaTreeNode> knownNodes) {
		if (knownNodes.contains(node)) return true;
		else knownNodes.add(node);
		for (FormulaTreeNode n : node.getChildren()) {
			if (checkForLoops(n, knownNodes)) return true;
		}
		return false;
	}

	private static VariableTreeNode createVariable(CodePropertiesConfig config, IProgramContext programContext, IVariable assignmentVar, IVariableController variableController, SimpleChildExpressionConditioner expressionConditioner, Set<IVariable> doNotUseInCondition) {
		IVariable v = variableController.getRandomVariableMatchingConstraintsOrNew(expressionConditioner, config.getNewVariableChance(), assignmentVar, doNotUseInCondition, programContext);
		IVariableWithAccessor variableWithAccessor = SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(v);
		VariableTreeNode node = new VariableTreeNode(variableWithAccessor, false);
		node.setPreCondition(expressionConditioner);
		return node;
	}

	private static FormulaTreeNode createSimpleValueTreeNode(CodePropertiesConfig config, IProgramContext programContext, SimpleChildExpressionConditioner expressionConditioner) throws UnsatisfiableConstraintsException {
		Set<DataType> possibleTypes = expressionConditioner.getPossibleReturnTypes();
		while (possibleTypes.size() > 0) {
			// Select a type
			final DataType selectedType = DataType.doTypeWidening(programContext.getCurrentlyDefinedTypes().oneOf(possibleTypes));
			SimpleChildExpressionConditioner restrictedChildExpressionConditioner = expressionConditioner.restrictToType(selectedType);

			if (DataType.equal(selectedType, DataType.INSTANCE_NONE)) {
				throw new RuntimeException("Got type NONE for new constant!");
			} else {
				IRange range = restrictedChildExpressionConditioner.getRange();
				if (range.isOverconstrained()) {
					possibleTypes.remove(selectedType);
					continue;
				}

				if (selectedType.isBool()) {
					if (range instanceof IBooleanRange) {
						IBooleanRange booleanRange = (IBooleanRange) range;
						boolean value;
						if (!booleanRange.canBeTrue()) value = false;
						else if (!booleanRange.canBeTrue()) value = true;
						else value = RandomGenHelper.randomChance(50, 100);
						return new SimpleBooleanValueTreeNode(value, false);
					}
				}
				// Excluding values that should not appear for these operators (in this case the value '0')
				Set<Long> exceptions = new HashSet<>();
				List<Operators> ops = new ArrayList<>(Operators.arithmeticOperatorsList);
				ops.add(Operators.ABS);
				ops.add(Operators.BIT_SHIFT_LEFT);
				ops.add(Operators.BIT_SHIFT_RIGHT);
				ops.add(Operators.BITWISE_OR);
				if (ops.contains(expressionConditioner.getParentOperator())) exceptions.add(0L);

				if (selectedType.isInteger()) {
					if (range instanceof IIntegerRange) {
						IIntegerRange integerRange = (IIntegerRange) range;
						if (config.isUseFullIntegerRange()) return new SimpleIntegerValueTreeNode(selectedType, RandomGenHelper.randomLong(integerRange.getLowerLimit(), integerRange.getUpperLimit()), false);
						else return new SimpleIntegerValueTreeNode(selectedType, RandomGenHelper.getRandomPrettyLong(integerRange, exceptions), false);
					}
				} else if (selectedType.isFloatingPoint()) {
					if (range instanceof IFloatingPointRange) {
						IFloatingPointRange floatingPointRange = (IFloatingPointRange) range;
						if (config.isUseFullFloatRange()) return new SimpleFloatingPointValueTreeNode(selectedType, RandomGenHelper.randomDouble(floatingPointRange.getLowerLimit(), floatingPointRange.getUpperLimit()), false);
						else return new SimpleFloatingPointValueTreeNode(selectedType, RandomGenHelper.getRandomPrettyDouble(floatingPointRange, exceptions), false);
					}
				}
				throw new RuntimeException("Unhandled data type: " + selectedType + " or range: " + range);
			}
		}
		throw new UnsatisfiableConstraintsException("Could not build expression based on constraints!");
	}

	private static boolean goIntoDepth(SimpleDepthInformation depthInformation, int maximumNodeCount) {
		return goIntoDepth(depthInformation, depthInformation, maximumNodeCount, false, -1);
	}

	private static boolean goIntoDepth(SimpleDepthInformation depthInformation, int maximumNodeCount, final boolean weWouldHaveToCreateVar) {
		return goIntoDepth(depthInformation, depthInformation, maximumNodeCount, weWouldHaveToCreateVar, -1);
	}

	/**
	 * Used to manage depth by using a given chance to decide whether to go further or stop at this depth
	 *
	 * @param maxDepth               Max depth limit. Use -1 if there is no limit.
	 */
	private static boolean goIntoDepth(SimpleDepthInformation depthInformation, SimpleDepthInformation conditionDepthInformation, int maximumNodeCount, final boolean weWouldHaveToCreateVar, final int maxDepth) {
		final boolean haveEnough = depthInformation.getTotalNodeCount() >= (0.9 * maximumNodeCount);
		// Ranges most times from 1 to 4
		final int depthMinOne = conditionDepthInformation.getCurrentDepth() + 1;
		int chosenChance = 0;

		// Check if there is a limit (not -1) and if there is check if it is surpassed
		if (maxDepth > -1 && conditionDepthInformation.getCurrentDepth() > maxDepth) {
			return false;
		}

		if (maximumNodeCount <= 3) {
			return false;
		}

		final int CHANCE_HAS_ENOUGH_NODES_AND_WILL_CREATE_NEW_VARIABLE = 0;
		final int CHANCE_HAS_ENOUGH_NODES_AND_WONT_CREATE_NEW_VARIABLE = 0;
		final int CHANCE_NEEDS_MORE_NODES_AND_WILL_CREATE_NEW_VARIABLE = 85;
		final int CHANCE_NEEDS_MORE_NODES_AND_WONT_CREATE_NEW_VARIABLE = 85;

		if (haveEnough) {
			if (weWouldHaveToCreateVar) {
				chosenChance = CHANCE_HAS_ENOUGH_NODES_AND_WILL_CREATE_NEW_VARIABLE;
			} else {
				chosenChance = CHANCE_HAS_ENOUGH_NODES_AND_WONT_CREATE_NEW_VARIABLE;
			}
		} else {
			if (weWouldHaveToCreateVar) {
				chosenChance = CHANCE_NEEDS_MORE_NODES_AND_WILL_CREATE_NEW_VARIABLE;
			} else {
				chosenChance = CHANCE_NEEDS_MORE_NODES_AND_WONT_CREATE_NEW_VARIABLE;
			}
			chosenChance /= depthMinOne;
		}
		return RandomGenHelper.randomChance(chosenChance, 100);
	}
}
