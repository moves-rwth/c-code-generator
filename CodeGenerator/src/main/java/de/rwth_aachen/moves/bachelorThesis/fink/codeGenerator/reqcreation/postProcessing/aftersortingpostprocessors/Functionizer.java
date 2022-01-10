package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.aftersortingpostprocessors;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.RequirementFunctionizer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.CodeObject;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Outsources parts of the code used in the step()-function of the output C-File into additional functions.
 */
public class Functionizer implements IAfterSortingPostProcessor {

	public Functionizer() {
	}

	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {
		IProgramContext programContext = templateInfo.getProgramContext();
		List<Requirement> requirements = templateInfo.getRequirements();
		List<CodeObject> codeObjects = templateInfo.getCodeObjects();
		List<IFunction> functions = templateInfo.getFunctions();

		List<Requirement> functionizedRequirements = new ArrayList<>();
		List<CodeObject> functionizedCodeObjects = new ArrayList<>();


		RequirementFunctionizer requirementFunctionizer = new RequirementFunctionizer(new HashSet<>(functions));
		for (Requirement r : requirements) {
			functionizedRequirements.add(requirementFunctionizer.functionizeRequirement(r, 20, programContext));
		}
		for (CodeObject c : codeObjects) {
			functionizedCodeObjects.add(requirementFunctionizer.functionizeCodeObject(c, 20, programContext));
		}
		functions.addAll(requirementFunctionizer.getGeneratedFunctions());

		return new TemplateInfo(templateInfo.getProperties(), functionizedRequirements, functions, functionizedCodeObjects, templateInfo.getCodeObjectLocation(), programContext);
	}


}
