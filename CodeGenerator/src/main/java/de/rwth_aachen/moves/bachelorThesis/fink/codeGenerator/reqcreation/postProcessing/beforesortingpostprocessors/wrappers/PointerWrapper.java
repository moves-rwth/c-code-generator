package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.wrappers;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.IBeforeSortingPostProcessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IPointerVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.*;


/**
 * Post-Processor used for creating pointers for each used variable in the C-Code and enforcing the usage of them. E.g. instead of var1 *(var1_pointer) will be used.
 */
public class PointerWrapper implements IBeforeSortingPostProcessor {

	public PointerWrapper() {
	}

	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {
		List<IFunction> functions = templateInfo.getFunctions();
		List<Requirement> requirements = templateInfo.getRequirements();
		IProgramContext programContext = templateInfo.getProgramContext();
		int percentageIntoPointers = config.getWrapperPointersPercentage();

		IVariableCollector variableCollector = new VariableCollector(true, programContext);
		templateInfo.accept(variableCollector);
		functions.forEach(func -> func.accept(variableCollector));
		Set<IVariableWithAccessor> toReplace = RandomGenHelper.randomSubset(new ArrayList<>(variableCollector.getUsedVariablesWithAccessor()), variableCollector.getUsedVariablesWithAccessor().size() * percentageIntoPointers / 100);
		toReplace.removeIf(var -> !var.getVariable().isBasic());
		toReplace.removeIf(var -> var.getVariable().getParameterType().equals(ParameterType.INTERNAL_SHADOW));

		// First, create all pointers
		Map<IVariableWithAccessor, IVariableWithAccessor> variableReplacementMap = new HashMap<>();
		toReplace.forEach(variableWithAccessor -> createVariablesToReplacerMap(variableWithAccessor, variableReplacementMap, programContext, config));

		VariableReplacer replacer = VariableReplacer.fromVariableWithAccessorReplacementMap(variableReplacementMap);

		return replaceVariables(templateInfo.getProperties(), requirements, functions, templateInfo.getCodeObjects(), templateInfo.getCodeObjectLocation(), programContext, replacer);
	}


	public void createVariablesToReplacerMap(IVariableWithAccessor varA, Map<IVariableWithAccessor, IVariableWithAccessor> variableReplacementMap, IProgramContext programContext, CodePropertiesConfig config) {
		IVariable var = varA.getVariable();
		DataType pointerType = config.isWrapperPointersUseVoidType() ? programContext.addPointerType(DataType.INSTANCE_VOID) : programContext.addPointerType(var.getDataType());
		IVariable pointer = programContext.addVariable(var.getParameterType(), pointerType, var.getInternalName() + "_Pointer", var.getInternalName() + "_Pointer");
		((IPointerVariable) pointer).setInitializationValue(varA);
		final IPointerVariable pointerVariable = (IPointerVariable) pointer;
		programContext.updatePointerTarget(pointerVariable, var);

		IVariableWithAccessor pointerWithAccessInformation = SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(pointerVariable);
		IVariableWithAccessor replacingAccessInformation = pointerWithAccessInformation.accessPointer(programContext);

		variableReplacementMap.put(varA, replacingAccessInformation);

	}

}



