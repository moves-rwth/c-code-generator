package de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.SimpleProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.CodeObject;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionTarget;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionVisitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class just holds lots of data. It represents a template / stencil of
 * requirements, functions, filler code, ...
 * This class can be used as an input file in our generator,
 * and it can also be result of our code generator.
 */
public class TemplateInfo implements Serializable, IVariableCollectionTarget {

	private List<Property> properties;
	private List<Requirement> requirements;
	private List<IFunction> functions;
	private List<CodeObject> codeObjects;
	private final Map<CodeObject, Requirement> codeObjectLocation;
	private IProgramContext programContext;
	private Map<Requirement, Boolean> createCFile;

	public TemplateInfo(List<Property> properties, List<Requirement> requirements, List<IFunction> functions, List<CodeObject> codeObjects, Map<CodeObject, Requirement> codeObjectLocation, IProgramContext programContext) {
		this.properties = properties;
		this.requirements = requirements;
		this.functions = functions;
		this.codeObjects = codeObjects;
		this.programContext = programContext;
		this.createCFile = new HashMap<>();
		this.codeObjectLocation = codeObjectLocation;
	}

	public TemplateInfo(List<Property> properties, List<Requirement> requirements, List<IFunction> functions, List<CodeObject> codeObjects, Map<CodeObject, Requirement> codeObjectLocation, IProgramContext programContext, Map<Requirement, Boolean> createCFile) {
		this.properties = properties;
		this.requirements = requirements;
		this.functions = functions;
		this.codeObjects = codeObjects;
		this.programContext = programContext;
		this.createCFile = createCFile;
		this.codeObjectLocation = codeObjectLocation;
	}

	public TemplateInfo() {
		this.properties = new ArrayList<>();
		this.requirements = new ArrayList<>();
		this.functions = new ArrayList<>();
		this.codeObjects = new ArrayList<>();
		this.createCFile = new HashMap<>();
		this.programContext = new SimpleProgramContext();
		this.codeObjectLocation = new HashMap<>();
	}

	@Override
	public void accept(IVariableCollectionVisitor visitor) {
		visitor.visit(this);
	}

	public IProgramContext getProgramContext() {
		return programContext;
	}

	public List<Requirement> getRequirements() {
		return requirements;
	}

	public List<IFunction> getFunctions() {
		return functions;
	}

	public Map<CodeObject, Requirement> getCodeObjectLocation() {
		return codeObjectLocation;
	}

	public List<CodeObject> getCodeObjects() {
		return codeObjects;
	}

	public void setCodeObjects(List<CodeObject> codeObjects) {
		this.codeObjects = codeObjects;
	}

	public void setRequirements(List<Requirement> requirements) {
		this.requirements = requirements;
	}

	public void setFunctions(List<IFunction> functions) {
		this.functions = functions;
	}


	public void setProgramContext(IProgramContext programContext) {
		this.programContext = programContext;
	}

	public Map<Requirement, Boolean> getCreateCFile() {
		return createCFile;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public TemplateInfo merge(TemplateInfo other) {
		this.properties.addAll(other.properties);
		this.codeObjects.addAll(other.codeObjects);
		this.functions.addAll(other.functions);
		this.requirements.addAll(other.requirements);
		this.codeObjectLocation.putAll(other.codeObjectLocation);
		this.createCFile.putAll(other.createCFile);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("TemplateInfo(");
		boolean isFirst = true;
		for (Requirement req : requirements) {
			if (!isFirst) {
				result.append(", ");
			}
			isFirst = false;
			result.append("Requirement '").append(req.getName()).append("' (");
			result.append(req.toStepCode(programContext));
			result.append(")");
		}
		result.append(")");
		return result.toString();
	}
}
