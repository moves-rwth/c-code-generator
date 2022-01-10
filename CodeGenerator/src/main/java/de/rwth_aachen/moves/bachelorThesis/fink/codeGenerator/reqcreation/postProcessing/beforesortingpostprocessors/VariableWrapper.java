package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.IPostProcessor;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.wrappers.ArrayWrapper;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.wrappers.PointerWrapper;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.wrappers.StructWrapper;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors.wrappers.UnionWrapper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;


/**
 * PostProcessor-class for application of all wrapper-PostProcessors as the interaction might not be clearly defined.
 */
public class VariableWrapper implements IBeforeSortingPostProcessor {

	public VariableWrapper() {
	}

	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {
		TemplateInfo modifiedInfo = templateInfo;
		IPostProcessor structWrapper = new StructWrapper();
		IPostProcessor unionWrapper = new UnionWrapper();
		IPostProcessor pointerWrapper = new PointerWrapper();
		IPostProcessor arrayWrapper = new ArrayWrapper(modifiedInfo.getProgramContext());

		if (config.useWrapperArrays()) {
			modifiedInfo = arrayWrapper.apply(modifiedInfo, config);
		}

		if (config.useWrapperStructs() && config.useWrapperPointers()) {
			modifiedInfo = structWrapper.apply(modifiedInfo, config);
			modifiedInfo = pointerWrapper.apply(modifiedInfo, config);
		} else if (config.useWrapperStructs()) {
			modifiedInfo = structWrapper.apply(modifiedInfo, config);
		} else if (config.useWrapperPointers()) {
			modifiedInfo = pointerWrapper.apply(modifiedInfo, config);
		} else if (config.useWrapperUnions()) {
			modifiedInfo = unionWrapper.apply(modifiedInfo, config);
		}


		return modifiedInfo;
	}
}
