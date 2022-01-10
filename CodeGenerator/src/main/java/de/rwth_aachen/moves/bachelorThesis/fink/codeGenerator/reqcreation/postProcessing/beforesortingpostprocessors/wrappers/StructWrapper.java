package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.wrappers;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.IBeforeSortingPostProcessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.StructType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.NotApplicableExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IMemberContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.Struct;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.*;

/**
 * Post-Processor used for creating a struct, containing a corresponding struct-variable for each used variable in the C-Code, and enforcing the usage of them. E.g. instead of var1, wrapperStruct.var1_pointer will be used.
 */
public class StructWrapper implements IBeforeSortingPostProcessor {

	public StructWrapper() {
	}

	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {
		List<IFunction> functions = templateInfo.getFunctions();
		List<Requirement> requirements = templateInfo.getRequirements();
		IProgramContext programContext = templateInfo.getProgramContext();
		final int numberOfStructs = config.getWrapperStructsAmount();
		final int percentageIntoStructs = config.getWrapperStructsPercentage();

		// Collect all variables.
		IVariableCollector variableCollector = new VariableCollector(true, programContext);
		templateInfo.accept(variableCollector);
		// Explanation: This should be all IVariables, not divided to IVariableWithAccessors, since we want to change storage locations.
		Set<IVariable> existingVars = new HashSet<>(variableCollector.getUsedVariables());
		// We only want to use the outermost variables because wrapping a struct around a variable inside a struct makes no sense
		existingVars.removeIf(var -> var.getParent() != null);
		existingVars.removeIf(var -> var.getParameterType().equals(ParameterType.INTERNAL_SHADOW));

		// Create sets with variables for each struct
		Set<IVariable> chosenVars = RandomGenHelper.randomSubset(new ArrayList<>(existingVars), existingVars.size() * percentageIntoStructs / 100);
		if (chosenVars.size() == 0) {
			return templateInfo;
		}
		List<IVariable> chosenVarsList = new ArrayList<>(chosenVars);
		List<Set<IVariable>> chosenVarSets = new ArrayList<>();
		List<Integer> amountPerStruct = RandomGenHelper.splitIntoRandomPartsLessVariance(chosenVars.size(), numberOfStructs);
		for (int i = 0; i < numberOfStructs; i++) {
			chosenVarSets.add(i, RandomGenHelper.randomSubset(chosenVarsList, amountPerStruct.get(i)));
			chosenVarsList.removeAll(chosenVarSets.get(i));
		}

		// Create wrapper structs (with new variables) and replacement map for toReplace-variables
		Map<IVariable, IVariable> oldToReplacementMap = new HashMap<>();
		int maxDigits = ("" + chosenVars.size()).length();
		for (int i = 0; i < numberOfStructs; i++) {
			String structName = String.format("WrapperStruct%0" + maxDigits + "d", i);

			StructType structType;
			{
				LinkedHashMap<String, String> structMemberToTypeString = new LinkedHashMap<>();
				for (IVariable var : chosenVarSets.get(i)) {
					structMemberToTypeString.put(var.getName(), var.getDataType().getTypeName());
				}
				structType = programContext.addStruct(structName, structMemberToTypeString);
			}

			// Properly (re-)name struct members
			// These are separate lists because the struct constructor needs a linkedHashMap.
			LinkedHashMap<String, IVariable> structMembersToOriginalVariables = new LinkedHashMap<>();
			LinkedHashMap<String, IVariable> structMembersToRenamedVariables = new LinkedHashMap<>();
			for (IVariable var : chosenVarSets.get(i)) {
				IVariable renamedMemberVariable = var.addNamePrefix(structName + '_');
				String memberName = structName + '_' + var.getName();
				structMembersToOriginalVariables.put(memberName, var);
				structMembersToRenamedVariables.put(memberName, renamedMemberVariable);
			}

			// Instance creation
			Struct resultStruct = new Struct(null, structType, ParameterType.SIGNAL, structName, structName, structMembersToRenamedVariables, null);

			// Register struct and re-register children by copying the expression conditioners from the variables we are wrapping
			programContext.addGlobalVariable(resultStruct, new NotApplicableExpressionConditioner(structType));
			for (String memberName : structType.getMembers().keySet()) {
				IVariable memberToReRegister = resultStruct.getMemberVariableByName(memberName);
				IVariable memberToCopyFrom = structMembersToOriginalVariables.get(memberToReRegister.getInternalName());
				programContext.getVariableController().registerVariable(memberToReRegister, memberToReRegister.getDataType(), programContext, programContext.getCurrentlyDefinedVariables().getVariableConditioner(memberToCopyFrom));
			}

			// Create mapping from original variable to our replacement struct member
			for (Map.Entry<String, IVariable> entry : resultStruct.getNameToMemberMapping().entrySet()) {
				IVariable toReplace = structMembersToOriginalVariables.get(entry.getKey());
				IVariable replacement = entry.getValue();
				addToMapRecursively(oldToReplacementMap, toReplace, replacement, programContext, structName);
			}
		}

		return replaceVariables(templateInfo.getProperties(), requirements, functions, templateInfo.getCodeObjects(), templateInfo.getCodeObjectLocation(), programContext, new VariableReplacer(oldToReplacementMap));
	}

	private void addToMapRecursively(Map<IVariable, IVariable> oldToReplacementMap, IVariable var, IVariable replacerStructVar, IProgramContext programContext, String toPrefix) {
		oldToReplacementMap.put(var, replacerStructVar);
		if (var instanceof IMemberContainer) {
			Set<IVariable> members = ((IMemberContainer) var).getMembers();
			members.forEach(member -> addToMapRecursively(oldToReplacementMap, member, programContext.getDefinedVariableByName(toPrefix + '_' + member.getInternalName()), programContext, toPrefix));
		}
	}
}


