package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.aftersortingpostprocessors;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.IBeforeSortingPostProcessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

/**
 * Used for general modification of requirements (e.g. changing the name of requirements)
 */
public class RequirementModifier implements IBeforeSortingPostProcessor {

	public RequirementModifier() {
	}

	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {


		templateInfo.getRequirements().replaceAll(req -> req.copy(req.getName() + config.getFileSuffix()));
		templateInfo.getProperties().replaceAll(prop -> prop.copy(prop.getName() + config.getFileSuffix(), config.getFileSuffix()));

		// Add _no_mc to the requirement names if the requirements should not be for model checking
		if (!config.isIncludeModelCheckingHarness()) {
			templateInfo.getRequirements().replaceAll(req -> req.copy(req.getName() + "_no_mc"));
			templateInfo.getProperties().replaceAll(prop -> prop.copy(prop.getName() + "_no_mc"));
		}

		templateInfo.getRequirements().forEach(req -> req.setPriority(config.getPriority()));
		templateInfo.getCodeObjectLocation().replaceAll((o, r) -> r == null ? null : r.copy(r.getName() + config.getFileSuffix()));

		return templateInfo;
	}
}
