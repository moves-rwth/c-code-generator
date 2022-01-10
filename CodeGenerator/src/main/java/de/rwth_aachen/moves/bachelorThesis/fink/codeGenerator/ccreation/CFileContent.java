package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.ccreation;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.CodeObject;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;

import java.util.*;


/**
 * A CFileContent Object contains all the necessary information to create the final output-files.
 * It is built using the ContentCreator class and passed to the CFileCreator.
 */

public class CFileContent {
	private final List<Requirement> requirements = new ArrayList<>();
	private final List<Property> properties = new ArrayList<>();
	private final List<CodeObject> codeObjects = new ArrayList<>();
	private final Map<CodeObject, Requirement> afterWhichRequirementToPutTheCodeObject = new HashMap<>();
	private final List<Requirement> randomCodeRequirements = new ArrayList<>();
	private final VariableCollector variableCollector = new VariableCollector(true, null);
	private Map<IVariable, SimpleExpressionConditioner> variableConditioners = new HashMap<>();
	private final Map<IVariable, Long> lastVariablesAndDepths = new HashMap<>();
	private final List<StringArray> requiredTypedefs = new ArrayList<>();
	private final List<StringArray> requiredForwardDeclarations = new ArrayList<>();
	private List<IFunction> functions = new ArrayList<>();
	private IDataTypeContext dataTypeContext;
	private IProgramContext programContext;

	public CFileContent() {
	}

	public void addTypedefs(IDataTypeContext dataTypeContext) {
		this.dataTypeContext = dataTypeContext;
		Set<DataType> definedTypes = new HashSet<>(DataTypeContext.builtinDataTypes);

		Set<DataType> types = dataTypeContext.all();

		for (Iterator<DataType> it = types.iterator(); it.hasNext();) {
			final DataType dataType = it.next();
			if (definedTypes.contains(dataType)) {
				it.remove();
			} else if (!dataType.requiresTypeDef()) {
				definedTypes.add(dataType);
				it.remove();
			} else {
				Set<DataType> requiredPreDefines = dataType.getRequiredTypes(dataTypeContext);
				if (!definedTypes.containsAll(requiredPreDefines)) {
					continue;
				}
				if (dataType.requiresForwardDeclaration()) {
					requiredForwardDeclarations.add(dataType.getForwardDeclaration());
				}

				requiredTypedefs.add(dataType.getTypeDef(dataTypeContext));
				definedTypes.add(dataType);
				it.remove();
			}
		}
	}

	public void addFunctions(Set<IFunction> newFunctions, IProgramContext context) {
		functions.addAll(newFunctions);
		for (IFunction function : newFunctions) {
			variableCollector.add(function, context);
		}
	}

	public void addFunctions(List<IFunction> newFunctions, IProgramContext context) {
		functions.addAll(newFunctions);
		for (IFunction function : newFunctions) {
			variableCollector.add(function, context);
		}
	}

	public void addRequirement(Requirement requirement, IProgramContext context) {
		if (requirements.contains(requirement)) {
			throw new RuntimeException("This CFileContent already contains the requirement '" + requirement.getName() + "'");
		}

		requirements.add(requirement);
		variableCollector.add(requirement, context);

		final Map<IVariable, Long> lastVariablesAndDepths = requirement.getLastVariablesAndDepths(context);
		for (IVariable var : lastVariablesAndDepths.keySet()) {
			if (this.lastVariablesAndDepths.containsKey(var)) {
				this.lastVariablesAndDepths.replace(var, Math.max(this.lastVariablesAndDepths.get(var), lastVariablesAndDepths.get(var)));
			} else {
				this.lastVariablesAndDepths.put(var, lastVariablesAndDepths.get(var));
			}
		}
	}


	public void addProperty(Property property, IProgramContext context) {
		if (properties.contains(property)) {
			throw new RuntimeException("This CFileContent already contains the requirement '" + property.getName() + "'");
		}

		properties.add(property);
		variableCollector.add(property, context);

		final Map<IVariable, Long> lastVariablesAndDepths = property.getLastVariablesAndDepths(context);
		for (IVariable var : lastVariablesAndDepths.keySet()) {
			if (this.lastVariablesAndDepths.containsKey(var)) {
				this.lastVariablesAndDepths.replace(var, Math.max(this.lastVariablesAndDepths.get(var), lastVariablesAndDepths.get(var)));
			} else {
				this.lastVariablesAndDepths.put(var, lastVariablesAndDepths.get(var));
			}
		}
	}

	public void addCodeObjects(List<CodeObject> codeObjects, Map<CodeObject, Requirement> codeObjectLocation, IProgramContext context) {
		for (CodeObject codeObject : codeObjects) {
			addCodeObject(codeObject, codeObjectLocation.get(codeObject), context);
		}
	}

	public void addCodeObject(CodeObject codeObject, Requirement afterThisRequirement, IProgramContext context) {
		codeObjects.add(codeObject);
		variableCollector.add(codeObject, context);
		afterWhichRequirementToPutTheCodeObject.put(codeObject, afterThisRequirement);

		final Map<IVariable, Long> lastVariablesAndDepths = codeObject.getLastVariablesAndDepths(context);
		for (IVariable var : lastVariablesAndDepths.keySet()) {
			if (this.lastVariablesAndDepths.containsKey(var)) {
				this.lastVariablesAndDepths.replace(var, Math.max(this.lastVariablesAndDepths.get(var), lastVariablesAndDepths.get(var)));
			} else {
				this.lastVariablesAndDepths.put(var, lastVariablesAndDepths.get(var));
			}
		}
	}



	public void setProgramContext(IProgramContext programContext) {
		this.programContext = programContext;
	}

	public List<Requirement> getRequirements() {
		return requirements;
	}

	public Set<IVariable> getUsedVariables() {
		return variableCollector.getUsedVariables();
	}

	public Set<IVariable> getNonOutputVariables() {
		return variableCollector.getInputVariablesWithoutOutputVariables();
	}

	public Map<IVariable, Long> getLastVariablesAndDepths() {
		return lastVariablesAndDepths;
	}

	public List<Requirement> getRandomCodeRequirements() {
		return randomCodeRequirements;
	}

	public List<CodeObject> getCodeObjects() {
		return codeObjects;
	}

	public Map<CodeObject, Requirement> getAfterWhichRequirementToPutTheCodeObject() {
		return afterWhichRequirementToPutTheCodeObject;
	}

	public List<StringArray> getRequiredTypedefs() {
		return requiredTypedefs;
	}

	public void setFunctions(List<IFunction> functions) {
		this.functions = functions;
	}

	public List<IFunction> getFunctions() {
		return functions;
	}

	public int getTotalNumberOfVariables() {
		return variableCollector.getUsedVariables().size();
	}

	public IDataTypeContext getDataTypeContext() {
		return dataTypeContext;
	}

	public VariableCollector getVariableCollector() {
		return variableCollector;
	}

	public void sortRequirements() {
		requirements.sort(Comparator.comparingInt(Requirement::getPriority).reversed());
		randomCodeRequirements.sort(Comparator.comparingInt(Requirement::getPriority).reversed());
	}

	public Map<IVariable, SimpleExpressionConditioner> getVariableConditioners() {
		return variableConditioners;
	}

	public void setVariableConditioners(Map<IVariable, SimpleExpressionConditioner> variableConditioners) {
		this.variableConditioners = variableConditioners;
	}

	public IProgramContext getProgramContext() {
		return programContext;
	}

	public List<StringArray> getRequiredForwardDeclarations() {
		return requiredForwardDeclarations;
	}


	public List<Property> getProperties() {
		return properties;
	}
}
