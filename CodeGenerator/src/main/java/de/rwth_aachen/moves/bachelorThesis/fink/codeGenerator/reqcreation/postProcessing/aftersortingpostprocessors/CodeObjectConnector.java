package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.aftersortingpostprocessors;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.aftersortingpostprocessors.IAfterSortingPostProcessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.CodeObject;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.*;

public class CodeObjectConnector implements IAfterSortingPostProcessor {


	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {

		List<CodeObject> codeObjectList = new ArrayList<>();

		if (!config.getFillerCodeConnection().equals("none")) {
			for (CodeObject codeObject : templateInfo.getCodeObjects()) {
				Map<IVariableWithAccessor, IVariableWithAccessor> replacerMap = new HashMap<>();

				IVariableCollector codeObjectCollector = new VariableCollector(true, templateInfo.getProgramContext());
				codeObject.accept(codeObjectCollector);

				Set<IVariableWithAccessor> inputVars = codeObjectCollector.getInputVariablesWithAccessorWithoutOutput();

				List<IVariable> potentialReplacerVars = new ArrayList<>();
				for (Requirement requirement : templateInfo.getRequirements()) {
					IVariableCollector otherReqCollector = new VariableCollector(true, templateInfo.getProgramContext());
					requirement.accept(otherReqCollector);
					if (config.getFillerCodeConnection().equals("input")) {
						potentialReplacerVars.addAll(otherReqCollector.getInputVariablesWithoutOutputVariables());
					} else if (config.getFillerCodeConnection().equals("output")) {
						potentialReplacerVars.addAll(otherReqCollector.getOutputVariables());
					}
				}

				potentialReplacerVars.removeIf(var -> var.getParameterType().equals(ParameterType.INTERNAL_SHADOW));

				for (IVariableWithAccessor inputVar : inputVars) {
					List<IVariable> concreteReplacerVars = new ArrayList<>();
					for (IVariable var : potentialReplacerVars) {
						SimpleExpressionConditioner expression1 = templateInfo.getProgramContext().getCurrentlyDefinedVariables().getVariableConditioner(inputVar.getVariable());
						SimpleExpressionConditioner expression2 = templateInfo.getProgramContext().getCurrentlyDefinedVariables().getVariableConditioner(var);
						if (expression2.isCompatibleWith(expression1, templateInfo.getProgramContext().getCurrentlyDefinedTypes())) {
							concreteReplacerVars.add(var);
						}
					}

					if (!concreteReplacerVars.isEmpty()) {
						IVariable chosenVar = RandomGenHelper.randomElement(concreteReplacerVars);
						replacerMap.put(inputVar, SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(chosenVar));
					}
				}

				codeObjectList.add(new CodeObject(codeObject, VariableReplacer.fromVariableWithAccessorReplacementMap(replacerMap)));

				System.out.println("Connected requirement " + codeObject.getName());
			}
		} else {
			codeObjectList = templateInfo.getCodeObjects();
		}


		return new TemplateInfo(templateInfo.getProperties(), templateInfo.getRequirements(), templateInfo.getFunctions(), codeObjectList, templateInfo.getCodeObjectLocation(), templateInfo.getProgramContext());
	}
}
