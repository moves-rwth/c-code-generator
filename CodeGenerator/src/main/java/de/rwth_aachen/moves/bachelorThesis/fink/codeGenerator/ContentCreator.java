package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator;

import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.ccreation.CFileContent;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.IPostProcessor;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.RequirementSorter;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.aftersortingpostprocessors.*;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.*;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.randomcodecreation.RandomCodeCreator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.OperatorReturnType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.ReqTreeValidator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;

/**
 * Check the interface for documentation.
 */
public class ContentCreator implements IContentCreator {

	public static EnumMap<Operators, Integer> operatorCheckList = new EnumMap<>(Operators.class);

	@Override
	public CFileContent createCFileContent(CodePropertiesConfig config, List<TemplateInfo> templates, List<Requirement> matchTemplates, IDataTypeContext dataTypeContext) {
		CFileContent content = new CFileContent();

		IMutableVariableContext variableContext = new SimpleMutableVariableContext();
		IProgramContext programContext = new SimpleProgramContext(variableContext, dataTypeContext, new SimpleVariableController(config.isUseFloats(), config.isDontForceBooleanForCondition()));

		createForbiddenOperatorsAndTypes(config, dataTypeContext);

		// Create code from template Requirements
		TemplateInfo fromTemplateGeneration = codeFromTemplates(config, templates, programContext);
		Set<String> templateNames = new HashSet<>();
		for (Requirement req : fromTemplateGeneration.getRequirements()) {
			templateNames.add(req.getName() + '_' + config.getFileSuffix());
		}
		validateTemplateInfoExpressionConditioner(fromTemplateGeneration);

		// Create code based on random decision
		TemplateInfo fromRandomGeneration = codeFromRandomGen(config, matchTemplates, fromTemplateGeneration);
		validateTemplateInfoExpressionConditioner(fromRandomGeneration);

		// Post Processing
		TemplateInfo afterPostProcessing = postProcessing(config, fromTemplateGeneration, fromRandomGeneration, templates);
		validateTemplateInfoExpressionConditioner(afterPostProcessing);

		// Introduce errors in random requirements of the option is set
		TemplateInfo finalInfo = introduceErrors(config, afterPostProcessing, content);
		validateTemplateInfoExpressionConditioner(finalInfo);

		// add the content
		createContent(finalInfo, templateNames, content, config);

		return content;
	}

	private void createForbiddenOperatorsAndTypes(CodePropertiesConfig config, IDataTypeContext dataTypeContext) {
		Set<DataType> forbiddenTypes = new HashSet<>();

		EnumSet<Operators> availableOps = RandomGenHelper.getNonZeroOperators(config.getOperatorsToProbability());
		availableOps.removeAll(Operators.getControlStructureOperators());
		if (Collections.disjoint(availableOps, Operators.getPossibleChildOperators(OperatorReturnType.GENERAL_BOOLEAN))) {
			forbiddenTypes.addAll(dataTypeContext.bool());
		} else if (Collections.disjoint(availableOps, Operators.getPossibleChildOperators(OperatorReturnType.ARITHMETIC))) {
			forbiddenTypes.addAll(dataTypeContext.noBool());
		}

		for (DataType type : dataTypeContext.all()) {
			if (config.getForbiddenDataTypeLevels().contains(type.getTypeLevel())) {
				forbiddenTypes.add(type);
			}
		}

		if (!config.isUseFloats()) {
			for (DataType type : dataTypeContext.getKnownDataTypes()) {
				if (type.isFloatingPoint()) {
					forbiddenTypes.add(type);
				}
			}
		}

		forbiddenTypes.forEach(dataTypeContext::addForbiddenDataType);
	}

	private TemplateInfo codeFromTemplates(CodePropertiesConfig config, List<TemplateInfo> templates, IProgramContext programContext) {
		TemplateInfo fromTemplates;
		List<Property> fromTemplatesProperties = new ArrayList<>();
		List<Requirement> fromTemplatesRequirements = new ArrayList<>();
		List<IFunction> fromTemplatesFunctions = new ArrayList<>();
		IProgramContext mergedContext = programContext.copy();

		if (config.isCreateCodeFromTemplates()) {
			if (config.getDirectlyUseTemplates()) {
				for (TemplateInfo templateInfo : templates) {
					mergedContext = mergedContext.merge(templateInfo.getProgramContext());
					fromTemplatesProperties.addAll(templateInfo.getProperties());
					fromTemplatesRequirements.addAll(templateInfo.getRequirements());
					fromTemplatesFunctions.addAll(templateInfo.getFunctions());
				}
				System.out.println("Creating C code directly based on the templates...\n");
			}
		}

		fromTemplates = new TemplateInfo(fromTemplatesProperties, fromTemplatesRequirements, fromTemplatesFunctions, new ArrayList<>(), new HashMap<>(), mergedContext);
		return fromTemplates;
	}

	private TemplateInfo codeFromRandomGen(CodePropertiesConfig config, List<Requirement> matchTemplates, TemplateInfo templateInfo) {
		TemplateInfo afterRandomGen;
		List<Property> fromRandomGenProperties = new ArrayList<>();
		List<Requirement> fromRandomGenRequirements = new ArrayList<>();
		List<IFunction> fromRandomGenFunctions = new ArrayList<>();

		if (config.createRandomRequirementCode()) {

			System.out.println("Creating new requirements based on random decisions...");
			TemplateInfo fromRandomGen = createRandomRequirements(config, templateInfo.getProgramContext());
			fromRandomGenRequirements.addAll(fromRandomGen.getRequirements());
			fromRandomGenFunctions.addAll(fromRandomGen.getFunctions());
			fromRandomGenProperties.addAll(fromRandomGen.getProperties());


			System.out.println("Creating c code based on the new requirements...\n");
		}
		afterRandomGen = new TemplateInfo(fromRandomGenProperties, fromRandomGenRequirements, fromRandomGenFunctions, templateInfo.getCodeObjects(), templateInfo.getCodeObjectLocation(), templateInfo.getProgramContext());
		return afterRandomGen;
	}

	private TemplateInfo introduceErrors(CodePropertiesConfig config, TemplateInfo templateInfo, CFileContent content) {
		return templateInfo;
	}

	private TemplateInfo postProcessing(CodePropertiesConfig config, TemplateInfo templateInfoTemplates, TemplateInfo templateInfoRandomGen, List<TemplateInfo> templates) {
		System.out.println("Started Post-Processing...\n");

		if (config.connectRequirements()) {
			RequirementConnector requirementConnector = new RequirementConnector();
			templateInfoRandomGen = requirementConnector.apply(templateInfoRandomGen, config);
			templateInfoTemplates = requirementConnector.apply(templateInfoTemplates, config);
		}

		System.out.println("Started sorting Requirements...");
		// Sort requirements
		RequirementSorter requirementSorter = new RequirementSorter();
		Triplet<TemplateInfo, TemplateInfo, Map<String, Integer>> result = requirementSorter.apply(templateInfoTemplates, templateInfoRandomGen, config);
		templateInfoTemplates = result.getValue0();
		templateInfoRandomGen = result.getValue1();
		Map<String, Integer> ordering = result.getValue2();
		System.out.println("Finished sorting Requirements...\n");

		if (config.isRestrictToIntegerVariable()) {
			System.out.println("Started replacing floats...");
			IPostProcessor floatReplacer = new FloatReplacer();
			templateInfoTemplates = floatReplacer.apply(templateInfoTemplates, config);
			templateInfoRandomGen = floatReplacer.apply(templateInfoRandomGen, config);
			System.out.println("Finished replacing floats...\n");
		}

		System.out.println("Started variable wrapping...");
		IPostProcessor variableWrapper = new VariableWrapper();
		templateInfoTemplates = variableWrapper.apply(templateInfoTemplates, config);
		templateInfoRandomGen = variableWrapper.apply(templateInfoRandomGen, config);
		System.out.println("Finished variable wrapping...\n");

		if (config.functionizeCode()) {
			System.out.println("Started functionizing code...");
			IPostProcessor functionizer = new Functionizer();
			templateInfoTemplates = functionizer.apply(templateInfoTemplates, config);
			templateInfoRandomGen = functionizer.apply(templateInfoRandomGen, config);
			System.out.println("Finished functionizing code...\n");
		}

		TemplateInfo mergedTemplateInfo = templateInfoTemplates.merge(templateInfoRandomGen);

		if (config.createStepLocalVariables()) {
			System.out.println("Started creating step local variables...");
			List<Requirement> stepCodeRequirements = mergedTemplateInfo.getRequirements();
			stepCodeRequirements = StepLocalVariables.getStepCodeRequirementList(stepCodeRequirements, templateInfoRandomGen.getProgramContext());
			mergedTemplateInfo.setRequirements(stepCodeRequirements);
			System.out.println("Finished creating step local variables...\n");
		}

		// Sort the requirements based on the sorting
		mergedTemplateInfo.getRequirements().sort(Comparator.comparingInt(req -> ordering.get(req.getName())));

		if (config.isCreateFillerCode()) {
			System.out.println("Started creating filler code...");
			IAfterSortingPostProcessor fillerCodeAdder = new FillerCodeAdder();
			IAfterSortingPostProcessor codeObjectConnector = new CodeObjectConnector();
			// Only to random gen because we dont want to add filler code twice
			mergedTemplateInfo = fillerCodeAdder.apply(mergedTemplateInfo, config);
			mergedTemplateInfo = codeObjectConnector.apply(mergedTemplateInfo, config);
			System.out.println("Finished creating filler code...\n");
		}

		IPostProcessor requirementModifier = new RequirementModifier();
		mergedTemplateInfo = requirementModifier.apply(mergedTemplateInfo, config);

		System.out.println("Finished Post-Processing...\n");
		return mergedTemplateInfo;
	}

	private void createContent(TemplateInfo templateInfo, Set<String> templateNames, CFileContent content, CodePropertiesConfig config) {
		content.setVariableConditioners(templateInfo.getProgramContext().getCurrentlyDefinedVariables().getVariableConditioners());

		IVariableCollector variableCollector = new VariableCollector(true, templateInfo.getProgramContext());
		templateInfo.accept(variableCollector);

		SimpleProgramContext updatedContext = new SimpleProgramContext(new SimpleMutableVariableContext(templateInfo.getProgramContext().getCurrentlyDefinedVariables()), templateInfo.getProgramContext().getCurrentlyDefinedTypes(), templateInfo.getProgramContext().getVariableController(), variableCollector.getUsedVariables(), new HashSet<>(templateInfo.getFunctions()));
		for (Requirement req : templateInfo.getRequirements()) {
			content.addRequirement(req, updatedContext);
		}

		for (Property property : templateInfo.getProperties()) {
			content.addProperty(property, updatedContext);
		}

		content.addCodeObjects(templateInfo.getCodeObjects(), templateInfo.getCodeObjectLocation(), updatedContext);
		content.addFunctions(templateInfo.getFunctions(), updatedContext);
		// Variable for tracking initial state
		content.getUsedVariables().add(updatedContext.addVariable(ParameterType.INTERNAL_CONTROL, DataType.INSTANCE_BOOL, "isInitial", "isInitial"));
		content.addTypedefs(templateInfo.getProgramContext().getCurrentlyDefinedTypes());
		content.setProgramContext(updatedContext);
	}

	private TemplateInfo createRandomRequirements(CodePropertiesConfig config, IProgramContext programContext) {
		List<Property> finalProperties = new ArrayList<>();
		List<Requirement> finalRequirements = new ArrayList<>();
		List<IFunction> finalFunctions = new ArrayList<>();
		List<Integer> nodeAmount = RandomGenHelper.splitIntoRandomPartsLessVariance(config.getRandomCodeNodeAmount(), Math.min(config.getSplitRandomCodeIntoRequirementsNumber(), (int) config.getRandomCodeNodeAmount() / 8));
		Set<IVariable> dontUseTheseVariables = new HashSet<>();

		if (config.isDontUseTemplateVariables()) {
			for (ImmutableSet<IVariable> variables : programContext.getCurrentlyDefinedGlobalVariables()) {
				dontUseTheseVariables.addAll(variables);
			}
		}

		if (config.isGenerateAsManyRequirementsAsNeeded()) {
			final List<Pair<Requirement, List<Property>>> requirementsAndProperties = RandomCodeCreator.createRequirementsAndProperties(config, nodeAmount, config.getRandomCodeNodeAmount(), programContext.getVariableController(), programContext, dontUseTheseVariables);
			for (Pair<Requirement, List<Property>> pair: requirementsAndProperties) {
				finalRequirements.add(pair.getValue0());
				finalProperties.addAll(pair.getValue1());
			}
		} else {
			for (int i = 0; i < config.getSplitRandomCodeIntoRequirementsNumber(); i++) {
				final String requirementName = "Req" + (i + 1);
				final Pair<Requirement, List<Property>> requirementAndProperties = RandomCodeCreator.createRequirementAndProperties(config, requirementName, nodeAmount.get(i), programContext.getVariableController(), programContext, dontUseTheseVariables);

				finalRequirements.add(requirementAndProperties.getValue0());
				finalProperties.addAll(requirementAndProperties.getValue1());
			}
		}
		finalFunctions.addAll(programContext.getCurrentlyDefinedFunctions());
		return new TemplateInfo(finalProperties, finalRequirements, finalFunctions, new ArrayList<>(), new HashMap<>(), programContext);
	}

	private void validateTemplateInfoExpressionConditioner(TemplateInfo ti) {
		// Check the underlying requirements for valid trees
		for (Requirement req : ti.getRequirements()) {
			ReqTreeValidator.validateTree(req.getCodeTreeNode(), ti.getProgramContext());
			// Add the operators of the tree of the Requirement to the operatorList
			addNodeToOperatorTestList(req.getCodeTreeNode());
		}
	}

	private void addNodeToOperatorTestList(CodeTreeNode node) {
		if (node instanceof OperatorTreeNode) {
			Operators op = Operators.getOperatorByString(((OperatorTreeNode) node).getName());
			if (operatorCheckList.containsKey(op)) operatorCheckList.put(op, operatorCheckList.get(op) + 1);
			else operatorCheckList.put(op, 1);
		}
		for (CodeTreeNode child : node.getChildren()) {
			addNodeToOperatorTestList(child);
		}
	}
}
