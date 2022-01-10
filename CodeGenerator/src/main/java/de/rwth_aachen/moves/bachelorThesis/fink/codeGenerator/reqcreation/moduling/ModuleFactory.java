package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.moduling;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IVariableController;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.NonVoidFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.VoidFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


/**
 * A factory class for generating ModuleInstances from parsed ModuleTemplates.
 */
public class ModuleFactory {

	private static int moduleCounter = 0;
	final private List<ModuleTemplate> moduleTemplateFiles = new ArrayList<>();
	final private Map<ModuleTemplate, ModuleTemplateInformation> moduleTemplateInformationMap = new HashMap<>();

	public ModuleFactory(Path templateDirectory, IProgramContext programContext, IVariableController variableController, CodePropertiesConfig config) {
		List<File> templateFiles;
		try {
			templateFiles = Files.list(templateDirectory)
					.filter(Files::isRegularFile)
					.map(Path::toFile)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException("Something went wrong when collecting the moduleTemplates!");
		}

		for (File file : templateFiles) {
			loadTemplateFile(file);
		}

		for (ModuleTemplate template : moduleTemplateFiles) {
			moduleTemplateInformationMap.put(template, new ModuleTemplateInformation(template.getConditionArgumentCount(), template.getCodeArgumentCount(), template.getNodeCount(), template.getTemplateName()));
		}
	}

	private void loadTemplateFile(File file) {
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module =
				new SimpleModule("ModuleJsonDeserializer", new Version(1, 0, 0, null, null, null));
		module.addDeserializer(ModuleTemplate.class, new ModuleJsonDeserializer());
		mapper.registerModule(module);
		ModuleTemplate template = null;
		try {
			template = mapper.readValue(file, ModuleTemplate.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert template != null;
		moduleTemplateFiles.add(template);
		moduleTemplateInformationMap.put(template, new ModuleTemplateInformation(template.getConditionArgumentCount(), template.getCodeArgumentCount(), template.getNodeCount(), template.getTemplateName()));
	}

	public ModuleTemplateInformation generateModuleTemplateInformation() {
		ModuleTemplate chosenTemplate = RandomGenHelper.randomElement(moduleTemplateFiles);
		return moduleTemplateInformationMap.get(chosenTemplate);
	}

	public ModuleInstance generateModule(String moduleTemplateName, List<FormulaTreeNode> conditionChildren, List<FormulaTreeNode> codeChildren, IProgramContext context, IVariableController variableController) {
		ModuleTemplate template = moduleTemplateFiles.stream().filter(module -> module.getTemplateName().equals(moduleTemplateName)).findFirst().orElseGet(() -> null);
		if (template == null) {
			throw new RuntimeException("Module " + moduleTemplateName + " not found!");
		}

		// Namespacing for functions
		Map<String, String> functionRenameMapping = new HashMap<>();
		{
			// First: Replace variables used within functions (skip parameters because they dont need to be replaced)
			List<IFunction> functionsWithRenamedVariables = new ArrayList<>();
			for (IFunction function : template.getFunctions()) {
				IProgramContext tempContext = context.copy();
				for (IFunction f : template.getFunctions()) {
					tempContext.addFunction(f);
				}

				// Create function rename mapping
				functionRenameMapping.put(function.getName(), function.getName() + "_" + moduleCounter);

				Set<IVariableWithAccessor> variablesNotToReplace = new HashSet<>(function.getParameterVariables());
				FormulaTreeNode functionBody = (FormulaTreeNode)function.getFunctionBody();
				FormulaTreeNode replacedFunctionBody = replaceVariables(functionBody, tempContext, variableController, template.getExpressionConditioners(), variablesNotToReplace);
				if (function instanceof NonVoidFunction) {
					functionsWithRenamedVariables.add(new NonVoidFunction(function.getName(), function.getParameterVariables(), replacedFunctionBody, function.getReturnType()));
				} else if (function instanceof VoidFunction) {
					functionsWithRenamedVariables.add(new VoidFunction(function.getName(), function.getParameterVariables(), replacedFunctionBody));
				} else {
					throw new RuntimeException("unsupported function type");
				}
			}

			// Create renamed functions with adjusted function bodies (as functions might call other functions
			List<IFunction> namespacedFunctions = new ArrayList<>();
			for (IFunction function : functionsWithRenamedVariables) {
				FormulaTreeNode adjustedFunctionBody = (FormulaTreeNode) replaceFunctionCalls(function.getFunctionBody(), functionRenameMapping);
				if (function instanceof NonVoidFunction) {
					namespacedFunctions.add(new NonVoidFunction(functionRenameMapping.get(function.getName()), function.getParameterVariables(), adjustedFunctionBody, function.getReturnType()));
				} else if (function instanceof VoidFunction) {
					namespacedFunctions.add(new VoidFunction(functionRenameMapping.get(function.getName()), function.getParameterVariables(), adjustedFunctionBody));
				} else {
					throw new RuntimeException("unsupported function type");
				}
			}

			// Add namespaced functions to programContext
			for (IFunction function : namespacedFunctions) {
				context.addFunction(function);
			}
		}

		// Replace function calls within rest of the module
		FormulaTreeNode requirement = (FormulaTreeNode) replaceFunctionCalls(template.getRequirement(), functionRenameMapping);
		List<FormulaTreeNode> localProperties = new ArrayList<>();
		for (FormulaTreeNode property : template.getLocalProperties().values()) {
			localProperties.add((FormulaTreeNode)replaceFunctionCalls(property, functionRenameMapping));
		}
		List<FormulaTreeNode> globalProperties = new ArrayList<>();
		for (FormulaTreeNode property : template.getGlobalProperties().values()) {
			globalProperties.add((FormulaTreeNode)replaceFunctionCalls(property, functionRenameMapping));
		}

		// Replace variables
		requirement = replaceVariables(requirement, context, variableController, template.getExpressionConditioners());
		for (FormulaTreeNode property : localProperties) {
			int index = localProperties.indexOf(property);
			localProperties.set(index, replaceVariables(property, context, variableController, template.getExpressionConditioners()));
		}
		for (FormulaTreeNode property : globalProperties) {
			int index = globalProperties.indexOf(property);
			globalProperties.set(index, replaceVariables(property, context, variableController, template.getExpressionConditioners()));
		}

		// Replace Placeholders
		requirement = replacePlaceholders(requirement, conditionChildren, codeChildren);
		for (FormulaTreeNode property : localProperties) {
			int index = localProperties.indexOf(property);
			localProperties.set(index, replacePlaceholders(property, conditionChildren, codeChildren));
		}
		for (FormulaTreeNode property : globalProperties) {
			int index = globalProperties.indexOf(property);
			globalProperties.set(index, replacePlaceholders(property, conditionChildren, codeChildren));
		}
		// Increase module counter for naming
		moduleCounter++;
		// Return finished module instance
		return new ModuleInstance(requirement, localProperties, globalProperties);
	}

	private CodeTreeNode replaceFunctionCalls(CodeTreeNode root, Map<String, String> functionRenameMapping) {
		List<FunctionCallTreeNode> originalFunctionCallNodes = getAllFunctionCallNodes(root);
		Map<CodeTreeNode, CodeTreeNode> replacementMapping = new HashMap<>();
		for (FunctionCallTreeNode toReplace : originalFunctionCallNodes) {
			if (functionRenameMapping.containsKey(toReplace.getFunctionName())) {
				FunctionCallTreeNode replacement = new FunctionCallTreeNode(functionRenameMapping.get(toReplace.getFunctionName()), toReplace.getParameters(), toReplace.isInOutput());
				replacementMapping.put(toReplace, replacement);
			}
		}
		return root.createCopyByReplacingNodes(replacementMapping);
	}

	private List<FunctionCallTreeNode> getAllFunctionCallNodes(CodeTreeNode node) {
		List<FunctionCallTreeNode> result = new ArrayList<>();
		if (node instanceof FunctionCallTreeNode) {
			result.add((FunctionCallTreeNode) node);
		}
		for (FormulaTreeNode child : node.getChildren()) {
			result.addAll(getAllFunctionCallNodes(child));
		}
		return (result);
	}


	private FormulaTreeNode replaceVariables(FormulaTreeNode node, IProgramContext context, IVariableController variableController, Map<IVariable, SimpleExpressionConditioner> expressionConditionerMap) {
		return replaceVariables(node, context, variableController, expressionConditionerMap, new HashSet<>());
	}

	private FormulaTreeNode replaceVariables(FormulaTreeNode node, IProgramContext context, IVariableController variableController, Map<IVariable, SimpleExpressionConditioner> expressionConditionerMap, Set<IVariableWithAccessor> exclusions) {
		IVariableCollector collector = new VariableCollector(false, context.copy());
		node.accept(collector);
		Set<IVariableWithAccessor> toReplace = collector.getUsedVariablesWithAccessor();
		toReplace.removeAll(exclusions);

		// First, create all new variables
		Map<IVariable, IVariable> variableReplacementMap = new HashMap<>();
		for (IVariableWithAccessor variableWithAccessor : toReplace) {
			IVariable replacer = variableWithAccessor.getVariable().rename(variableWithAccessor.getVariable().getName() + "_" + moduleCounter, variableWithAccessor.getVariable().getInternalName() + "_" + moduleCounter);
			variableReplacementMap.put(variableWithAccessor.getVariable(), replacer);
			variableController.registerVariable(replacer, replacer.getDataType(), context, expressionConditionerMap.get(variableWithAccessor.getVariable()));
		}
		// Create replacement map for toReplace-variables
		Map<IVariableWithAccessor, IVariableWithAccessor> oldToReplacementMap = new HashMap<>();
		for (IVariableWithAccessor variableWithAccessor : toReplace) {
			final IVariable oldVariable = variableWithAccessor.getVariable();
			final IVariable variableReplacementResult = variableReplacementMap.get(oldVariable);
			IVariableAccessInformation replacedVariableAccessInformation = variableWithAccessor.getAccessInformation().replaceVariables(variableReplacementMap);
			IVariableWithAccessor replacedVariable = new SimpleVariableWithAccessInformation(variableReplacementResult, replacedVariableAccessInformation);
			oldToReplacementMap.put(variableWithAccessor, replacedVariable);
		}

		return (FormulaTreeNode) node.copyTree(VariableReplacer.fromVariableWithAccessorReplacementMap(oldToReplacementMap));
	}

	private FormulaTreeNode replacePlaceholders(FormulaTreeNode currentNode, List<FormulaTreeNode> conditionChildren, List<FormulaTreeNode> codeChildren) {
		List<FormulaTreeNode> replacerChildren = new ArrayList<>();
		for (int i = 0; i < currentNode.getChildren().size(); i++) {
			FormulaTreeNode child = currentNode.getChildren().get(i);
			if (child instanceof PlaceholderTreeNode) {
				if (((PlaceholderTreeNode) child).getPlaceholderType() == PlaceholderTreeNode.PlaceholderType.Condition) {
					replacerChildren.add(replacePlaceholders((FormulaTreeNode) conditionChildren.get(((PlaceholderTreeNode) child).getIndex()).copyTree(), conditionChildren, codeChildren));
				} else {
					replacerChildren.add(replacePlaceholders((FormulaTreeNode) codeChildren.get(((PlaceholderTreeNode) child).getIndex()).copyTree(), conditionChildren, codeChildren));
				}
			} else {
				replacerChildren.add(replacePlaceholders(child, conditionChildren, codeChildren));
			}
		}
		return currentNode.replaceChildren(ImmutableList.copyOf(replacerChildren));
	}
}
