package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.*;

public class RequirementConnector implements IBeforeSortingPostProcessor {

	private final int USE_OWN_OUTPUT_VAR_CHANCE = 10;
	private final boolean LAST_FOR_REQUIREMENT = true;

	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {

		List<Property> propertyList = new ArrayList<>();
		List<Requirement> requirementList = new ArrayList<>();

		for (Requirement req : templateInfo.getRequirements()) {
			Map<IVariableWithAccessor, IVariableWithAccessor> replacerMapSelf = new HashMap<>();
			Map<IVariableWithAccessor, IVariableWithAccessor> replacerMapOther = new HashMap<>();

			IVariableCollector reqCollector = new VariableCollector(true, templateInfo.getProgramContext());
			req.accept(reqCollector);

			Set<IVariableWithAccessor> inputVars = reqCollector.getInputVariablesWithAccessorWithoutOutput();
			Set<IVariable> selfOutputVars = reqCollector.getOutputVariables();

			List<IVariable> potentialReplacerVars = new ArrayList<>();
			for (Requirement otherReq : templateInfo.getRequirements()) {
				if (!(req == otherReq)) {
					IVariableCollector otherReqCollector = new VariableCollector(true, templateInfo.getProgramContext());
					otherReq.accept(otherReqCollector);
					potentialReplacerVars.addAll(otherReqCollector.getOutputVariables());
				}
			}

			for (IVariableWithAccessor inputVar : inputVars) {
				List<IVariable> concreteReplacerVars = new ArrayList<>();
				for (IVariable var : potentialReplacerVars) {
					SimpleExpressionConditioner expression1 = templateInfo.getProgramContext().getCurrentlyDefinedVariables().getVariableConditioner(inputVar.getVariable());
					SimpleExpressionConditioner expression2 = templateInfo.getProgramContext().getCurrentlyDefinedVariables().getVariableConditioner(var);
					if (expression2.isCompatibleWith(expression1, templateInfo.getProgramContext().getCurrentlyDefinedTypes())) {
						concreteReplacerVars.add(var);
					}
				}


				List<IVariable> concreteSelfReplacerVars = new ArrayList<>();
				for (IVariable var : selfOutputVars) {
					SimpleExpressionConditioner expression1 = templateInfo.getProgramContext().getCurrentlyDefinedVariables().getVariableConditioner(inputVar.getVariable());
					SimpleExpressionConditioner expression2 = templateInfo.getProgramContext().getCurrentlyDefinedVariables().getVariableConditioner(var);
					if (expression2.isCompatibleWith(expression1, templateInfo.getProgramContext().getCurrentlyDefinedTypes())) {
						concreteSelfReplacerVars.add(var);
					}
				}


				if (RandomGenHelper.randomChance(USE_OWN_OUTPUT_VAR_CHANCE, 100) && !concreteSelfReplacerVars.isEmpty()) {
					IVariable chosenVar = RandomGenHelper.randomElement(concreteSelfReplacerVars);
					replacerMapSelf.put(inputVar, SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(chosenVar));
				} else if (!concreteReplacerVars.isEmpty()) {
					IVariable chosenVar = RandomGenHelper.randomElement(concreteReplacerVars);
					replacerMapOther.put(inputVar, SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(chosenVar));
				}
			}

			Requirement temp;
			if (LAST_FOR_REQUIREMENT) {
				temp = new Requirement(req, VariableReplacer.fromVariableWithAccessorReplacementMap(replacerMapSelf, VariableReplacer.PreVarOperator.LAST));
			} else {
				temp = new Requirement(req, VariableReplacer.fromVariableWithAccessorReplacementMap(replacerMapSelf));
			}
			requirementList.add(new Requirement(temp, VariableReplacer.fromVariableWithAccessorReplacementMap(replacerMapOther)));

			for (Property prop : templateInfo.getProperties()) {
				if (prop.getCorrespondingRequirementNames().contains(req.getName())) {
					Property tempProp = new Property(prop, VariableReplacer.fromVariableWithAccessorReplacementMap(replacerMapSelf, VariableReplacer.PreVarOperator.LAST));
					propertyList.add(new Property(tempProp, VariableReplacer.fromVariableWithAccessorReplacementMap(replacerMapOther)));
				}
			}
			System.out.println("Connected requirement " + req.getName());
		}

		return new TemplateInfo(propertyList, requirementList, templateInfo.getFunctions(), templateInfo.getCodeObjects(), templateInfo.getCodeObjectLocation(), templateInfo.getProgramContext());
	}
}
