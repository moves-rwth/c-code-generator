package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.wrappers;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.IBeforeSortingPostProcessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.UnionType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.NotApplicableExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IMemberContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.Union;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.*;

/**
 * Post-Processor used for creating a union, containing a corresponding union-variable for each used variable in the C-Code, and enforcing the usage of them. E.g. instead of var1, wrapperUnion.var1_pointer will be used.
 */
public class UnionWrapper implements IBeforeSortingPostProcessor {

	public UnionWrapper() {
	}

	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {
		List<IFunction> functions = templateInfo.getFunctions();
		List<Requirement> requirements = templateInfo.getRequirements();
		IProgramContext programContext = templateInfo.getProgramContext();
		final int percentageIntoUnions = config.getWrapperUnionsPercentage();

		// Collect all variables.
		IVariableCollector variableCollector = new VariableCollector(true, programContext);
		templateInfo.accept(variableCollector);
		// Explanation: This should be all IVariables, not divided to IVariableWithAccessors, since we want to change storage locations.
		Set<IVariable> existingVars = new HashSet<>(variableCollector.getUsedVariables());
		// We only want to use the outermost variables because wrapping a union around a variable inside a union makes no sense
		existingVars.removeIf(var -> var.getParent() != null);

		// Create sets with variables for each union
		List<IVariable> chosenVars = new ArrayList<>(RandomGenHelper.randomSubset(new ArrayList<>(existingVars), existingVars.size() * percentageIntoUnions / 100));
		if (chosenVars.size() == 0) {
			return templateInfo;
		}

		// Create wrapper unions (with new variables) and replacement map for toReplace-variables
		Map<IVariable, IVariable> oldToReplacementMap = new HashMap<>();
		int maxDigits = ("" + chosenVars.size()).length();
		for (int wrapperStructIndex = 0; wrapperStructIndex < chosenVars.size(); ++wrapperStructIndex) {
			String unionName = String.format("WrapperUnion%0" + maxDigits + "d", wrapperStructIndex);
			IVariable variableToWrap = chosenVars.get(wrapperStructIndex);
			LinkedHashMap<String, String> unionMemberToTypeString = new LinkedHashMap<>();

			// Add variable we are wrapping
			unionMemberToTypeString.put(variableToWrap.getName(), variableToWrap.getDataType().getTypeName());

			// Adding other, already registered types, which should have no actual influence
			List<DataType> fillerTypes = new ArrayList<>(RandomGenHelper.randomSubset(programContext.getCurrentlyDefinedTypes().all(), config.getWrapperUnionsAdditionalPercentage(), 100));
			fillerTypes.removeIf(DataType::isVoid);
			// fillerVars.removeIf(var -> ! var.isBasic()); // Uncomment this line to disable union stacking
			for (int fillerVarIndex = 0; fillerVarIndex < fillerTypes.size(); ++fillerVarIndex) {
				unionMemberToTypeString.put("unionFillerVar" + fillerVarIndex, fillerTypes.get(fillerVarIndex).getTypeName());
			}

			// Create type and instance of type
			UnionType unionType = programContext.addUnion(unionName, unionMemberToTypeString);
			Union unionInstance = new Union(
					null, unionType, ParameterType.SIGNAL, unionName, unionName,
					new LinkedHashMap<>(){{put(unionName + '_' + variableToWrap.getName(), variableToWrap.addNamePrefix(unionName + '_'));}}, // Rename (sole) member into correct format
					null);

			// Register instance we created as well as variable member we will use in our contexts
			programContext.addGlobalVariable(unionInstance, new NotApplicableExpressionConditioner(unionType));
			IVariable memberToReRegister = unionInstance.getMemberVariableByName(variableToWrap.getName());
			programContext.getVariableController().registerVariable(memberToReRegister, memberToReRegister.getDataType(), programContext, programContext.getCurrentlyDefinedVariables().getVariableConditioner(variableToWrap));

			// Create mapping from original variable to our replacement union member
			addToMapRecursively(oldToReplacementMap, variableToWrap, memberToReRegister, programContext, unionName);
		}

		return replaceVariables(templateInfo.getProperties(), requirements, functions, templateInfo.getCodeObjects(), templateInfo.getCodeObjectLocation(), programContext, new VariableReplacer(oldToReplacementMap));
	}

	private void addToMapRecursively(Map<IVariable, IVariable> oldToReplacementMap, IVariable var, IVariable replacerUnionVar, IProgramContext programContext, String toPrefix) {
		oldToReplacementMap.put(var, replacerUnionVar);
		if (var instanceof IMemberContainer) {
			Set<IVariable> members = ((IMemberContainer) var).getMembers();
			members.forEach(member -> addToMapRecursively(oldToReplacementMap, member, programContext.getDefinedVariableByName(toPrefix + '_' + member.getInternalName()), programContext, toPrefix));
		}
	}
}


