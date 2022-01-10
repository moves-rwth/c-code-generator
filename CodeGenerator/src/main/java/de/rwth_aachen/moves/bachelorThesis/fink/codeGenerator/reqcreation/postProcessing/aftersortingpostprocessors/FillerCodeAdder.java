package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.aftersortingpostprocessors;

import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.randomcodecreation.RandomCodeCreator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IVariableController;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.SimpleVariableController;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.CodeObject;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.*;


/**
 * Used for creating additional C-Code not used in any property (variables not related to any checked property)
 */
public class FillerCodeAdder implements IAfterSortingPostProcessor {

	public FillerCodeAdder() {
	}

	@Override
	public TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config) {
		return createCodeObject(templateInfo, config);
	}


	private TemplateInfo createCodeObject(TemplateInfo templateInfo, CodePropertiesConfig config) {
		List<Requirement> requirements = templateInfo.getRequirements();
		List<IFunction> functions = templateInfo.getFunctions();
		Map<CodeObject, Requirement> codeObjectLocation = new HashMap<>();
		IProgramContext programContext = templateInfo.getProgramContext();

		List<CodeObject> codeObjects = createRandomCodeObjects(config, programContext);
		for (CodeObject c : codeObjects) {
			if (requirements.isEmpty()) {
				codeObjectLocation.put(c, null);
			} else {
				switch (config.getFillerCodePosition()) {
					case "start":
						codeObjectLocation.put(c, null);
						break;
					case "end":
						codeObjectLocation.put(c, requirements.get(Math.max(requirements.size() - 1, 0)));
						break;
					case "everywhere":
						codeObjectLocation.put(c, requirements.get(RandomGenHelper.randomInt(0, Math.max(requirements.size() - 1, 0))));
				}
			}
		}

		return new TemplateInfo(templateInfo.getProperties(), requirements, functions, codeObjects, codeObjectLocation, programContext);
	}

	private List<CodeObject> createRandomCodeObjects(CodePropertiesConfig config, IProgramContext programContext) {
		Set<IVariable> dontUseTheseVariables = new HashSet<>();

		// do this because we only want new variables for fillercode without modifications
		for (ImmutableSet<IVariable> variables : programContext.getCurrentlyDefinedGlobalVariables()) {
			dontUseTheseVariables.addAll(variables);
		}

		IVariableController variableController = new SimpleVariableController(config.isUseFloats(), config.isDontForceBooleanForCondition());
		List<CodeObject> codeObjects = RandomCodeCreator.createCodeObjects(config, config.getFillerCodeNodeCount(), programContext.getVariableController(), programContext, dontUseTheseVariables);

		System.out.println("createdRequirements");

		return codeObjects;
	}
}
