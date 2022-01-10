package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.FloatTypeReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IMemberContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * PostProcessor used to replace all usages of float variables in a requirement with non-float variables (integer).
 */
public class FloatReplacer implements IBeforeSortingPostProcessor {

	public FloatReplacer() {
	}

	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {
		List<IFunction> functions = templateInfo.getFunctions();
		List<Requirement> requirements = templateInfo.getRequirements();
		IProgramContext programContext = templateInfo.getProgramContext();

		FloatTypeReplacer floatTypeReplacer = new FloatTypeReplacer(false);

		// Collect all floating point variables.
		IVariableCollector variableCollector = new VariableCollector(true, programContext);
		templateInfo.accept(variableCollector);
		for (IFunction func : functions) {
			func.accept(variableCollector);
		}
		Set<IVariableWithAccessor> toReplace = variableCollector.getUsedVariablesWithAccessor();
		toReplace.removeIf(var -> !floatTypeReplacer.willReplace(var.getVariable(), programContext));

		// First, create all new variables
		Map<IVariable, IVariable> variableReplacementMap = new HashMap<>();
		for (IVariableWithAccessor variableWithAccessor: toReplace) {
			createReplacementVariableAndDependants(variableWithAccessor.getVariable(), variableReplacementMap, floatTypeReplacer, programContext);
		}

		// Create replacement map for toReplace-variables
		Map<IVariableWithAccessor, IVariableWithAccessor> oldToReplacementMap = new HashMap<>();
		for (IVariableWithAccessor variableWithAccessor: toReplace) {
			final IVariable oldVariable = variableWithAccessor.getVariable();
			final IVariable variableReplacementResult = variableReplacementMap.get(oldVariable);
			IVariableAccessInformation replacedVariableAccessInformation = variableWithAccessor.getAccessInformation().replaceVariables(variableReplacementMap);
			IVariableWithAccessor replacedVariable = new SimpleVariableWithAccessInformation(variableReplacementResult, replacedVariableAccessInformation);
			oldToReplacementMap.put(variableWithAccessor, replacedVariable);
		}

		return replaceVariables(templateInfo.getProperties(), requirements, functions, templateInfo.getCodeObjects(), templateInfo.getCodeObjectLocation(), programContext, VariableReplacer.fromVariableWithAccessorReplacementMap(oldToReplacementMap));
	}

	private void createReplacementVariableAndDependants(IVariable variable, Map<IVariable, IVariable> variableReplacementMap, FloatTypeReplacer floatTypeReplacer, IProgramContext programContext) {
		final DataType replacementDataType = floatTypeReplacer.getReplacementType(variable, programContext);
		final IVariable oldVariable = variable;
		IVariable replacementVariable;
		if (replacementDataType != oldVariable.getDataType()) {
			replacementVariable = oldVariable.replaceDataType(replacementDataType, programContext);
			programContext.getVariableController().registerVariable(replacementVariable, replacementDataType, programContext, programContext.getCurrentlyDefinedVariables().getVariableConditioner(oldVariable));
		} else {
			replacementVariable = oldVariable.copy();
		}

		if (replacementVariable.getParent() != null) {
			final IVariable parent = replacementVariable.getParent();
			if (!variableReplacementMap.containsKey(parent)) {
				createReplacementVariableAndDependants(parent, variableReplacementMap, floatTypeReplacer, programContext);
			}
			final IMemberContainer newParent = (IMemberContainer) variableReplacementMap.get(parent);
			assert(newParent != null);
			replacementVariable = replacementVariable.replaceParent(newParent);
		}

		variableReplacementMap.put(oldVariable, replacementVariable);
	}
}
