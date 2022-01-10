package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.wrappers;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.IBeforeSortingPostProcessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ArrayType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.Utility;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.ArrayVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.FloatingPointArrayVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IntegerArrayVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.*;
import java.util.stream.Collectors;


public class ArrayWrapper implements IBeforeSortingPostProcessor {

	private HashMap<DataType, Integer> arrayCounterPerDatatype = new HashMap<>();

	public ArrayWrapper(IProgramContext programContext) {
		for (DataType type : programContext.getCurrentlyDefinedTypes().all()) {
			arrayCounterPerDatatype.put(type, 0);
		}
	}

	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {
		List<IFunction> functions = templateInfo.getFunctions();
		List<Requirement> requirements = templateInfo.getRequirements();
		IProgramContext programContext = templateInfo.getProgramContext();
		int percentageIntoArrays = config.getWrapperArraysPercentage();

		// Collect all variables.
		IVariableCollector variableCollector = new VariableCollector(true, programContext);
		templateInfo.accept(variableCollector);
		// Explanation: This should be all IVariables, not divided to IVariableWithAccessors, since we want to change storage locations.
		Set<IVariable> existingVars = new HashSet<>(variableCollector.getUsedVariables());
		// We only want to use the outermost variables because wrapping a struct around a variable inside a struct makes no sense
		existingVars.removeIf(var -> var.getParent() != null);
		existingVars.removeIf(var -> !var.isBasic());
		existingVars.removeIf(var -> var.getParameterType().equals(ParameterType.INTERNAL_SHADOW));
		int amountToRemove = existingVars.size() * (100 - percentageIntoArrays) / 100;
		// Group variables by Datatype
		Map<DataType, List<IVariable>> variablesByDatatype = existingVars.stream()
				.collect(Collectors.groupingBy(IVariable::getDataType));

		while (amountToRemove > 0) {
			List<DataType> removeCopy = new ArrayList<>(variablesByDatatype.keySet());
			removeCopy.sort(Comparator.comparingInt(a -> variablesByDatatype.get(a).size()));
			amountToRemove -= variablesByDatatype.get(removeCopy.get(0)).size();
			variablesByDatatype.remove(removeCopy.get(0));
			removeCopy.remove(0);
		}


		// First, create all pointers
		Map<IVariable, IVariable> variableReplacementMap = new HashMap<>();

		for (List<IVariable> varList : variablesByDatatype.values()) {
			DataType arrayEntryDataType = varList.get(0).getDataType();
			List<Integer> arrayDimensions = Utility.primeFactors(varList.size());
			ArrayType arrayType = (ArrayType) programContext.addArrayType(arrayEntryDataType, arrayDimensions);
			String arrayVarName = arrayEntryDataType.toCTypeName().replaceAll(" ", "_") + "_Array_" + arrayCounterPerDatatype.get(arrayEntryDataType);
			if (arrayEntryDataType.getTypeLevel() == DataType.INSTANCE_BOOL.getTypeLevel()) {
				arrayVarName = "BOOL_" + arrayVarName;
			}

			ArrayVariable arrayInstance = programContext.addArrayInstanceByCopyingGivenVariables(arrayType, varList, arrayVarName, arrayVarName);
			List<IVariable> arrayMembers = null;
			if (arrayInstance instanceof IntegerArrayVariable) {
				arrayMembers = new ArrayList<>(((IntegerArrayVariable) arrayInstance).getArrayEntries());
			} else if (arrayInstance instanceof FloatingPointArrayVariable) {
				arrayMembers = new ArrayList<>(((FloatingPointArrayVariable) arrayInstance).getArrayEntries());
			} else {
				throw new RuntimeException("Unhandled array type.");
			}

			assert (arrayMembers.size() == varList.size());
			for (int variableIndex = 0; variableIndex < arrayMembers.size(); variableIndex++) {
				variableReplacementMap.put(varList.get(variableIndex), arrayMembers.get(variableIndex));
			}

			arrayCounterPerDatatype.put(arrayEntryDataType, arrayCounterPerDatatype.get(arrayEntryDataType) + 1);
		}

		return replaceVariables(templateInfo.getProperties(),
				requirements, functions, templateInfo.getCodeObjects(), templateInfo.getCodeObjectLocation(), programContext, new VariableReplacer(variableReplacementMap));
	}
}
