package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Classes implementing this interface are used to modify requirements post-generation.
 */
public interface IPostProcessor {

	TemplateInfo apply(TemplateInfo templateInfo, CodePropertiesConfig config);


	default TemplateInfo replaceVariables(List<Property> properties, List<Requirement> requirements, List<IFunction> functions, List<CodeObject> codeObjects, Map<CodeObject, Requirement> codeObjectPlace, IProgramContext programContext, VariableReplacer variableReplacer) {
		List<Property> propertyList = new ArrayList<>();
		for (Property prop : properties) {
			propertyList.add(new Property(prop, variableReplacer));
		}

		List<Requirement> requirementList = new ArrayList<>();
		for (Requirement req : requirements) {
			requirementList.add(new Requirement(req, variableReplacer));
		}

		List<IFunction> functionList = new ArrayList<>();
		for (IFunction func : functions) {
			if (func instanceof VoidFunction) {
				functionList.add(new VoidFunction((VoidFunction) func, variableReplacer));
			} else {
				functionList.add(new NonVoidFunction((NonVoidFunction) func, variableReplacer));
			}
		}

		return new TemplateInfo(propertyList, requirementList, functionList, codeObjects, codeObjectPlace, programContext);
	}
}
