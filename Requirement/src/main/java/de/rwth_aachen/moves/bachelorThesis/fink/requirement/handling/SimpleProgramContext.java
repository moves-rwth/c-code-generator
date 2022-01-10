package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;

import java.io.Serializable;
import java.util.*;

public class SimpleProgramContext implements IProgramContext, Serializable {

	private final IMutableVariableContext variableContext;
	private final IDataTypeContext dataTypeContext;

	private final IVariableController variableController;

	private final List<Set<IVariable>> definedVariables = new ArrayList<>();
	private final Set<IFunction> definedFunctions = new HashSet<>();

	private final List<HashMap<String, IVariable>> variablesByName = new ArrayList<>();

	private final IPointerAssignment pointerAssignment = new SimplePointerAssignment();

	private IVariableContext variableContextCache = null;
	private void invalidateCache() {
		variableContextCache = null;
	}

	private void buildRootScope() {
		definedVariables.add(new HashSet<>());
		variablesByName.add(new HashMap<>());
	}

	public SimpleProgramContext() {
		this.variableContext = new SimpleMutableVariableContext();
		this.dataTypeContext = new DataTypeContext();
		this.variableController = new SimpleVariableController(true, true);

		buildRootScope();
	}

	public SimpleProgramContext(IMutableVariableContext variableContext, IDataTypeContext dataTypeContext, IVariableController variableController) {
		this.variableContext = variableContext;
		this.dataTypeContext = dataTypeContext;
		this.variableController = variableController;

		buildRootScope();
	}


	public SimpleProgramContext(IProgramContext other) {
		this.variableContext = other.getMutableVariableContext().copy();
		this.dataTypeContext = other.getCurrentlyDefinedTypes().copy();
		this.variableController = other.getVariableController().copy();
		for (ImmutableSet<IVariable> variables : other.getCurrentlyDefinedGlobalVariables()) {
			this.definedVariables.add(new HashSet<>(variables));
			HashMap<String, IVariable> nameToVariableMap = new HashMap<>();
			for (IVariable variable : variables) {
				nameToVariableMap.put(variable.getInternalName(), variable);
			}
			this.variablesByName.add(nameToVariableMap);
		}

		this.definedFunctions.addAll(other.getCurrentlyDefinedFunctions());
	}

	public SimpleProgramContext(IMutableVariableContext variableContext, IDataTypeContext dataTypeContext, IVariableController variableController, Set<IVariable> definedVariables, Set<IFunction> definedFunctions) {
		this.variableContext = variableContext;
		this.dataTypeContext = dataTypeContext;
		this.variableController = variableController;
		this.definedVariables.add(new HashSet<>(definedVariables));
		HashMap<String, IVariable> nameToVariableMap = new HashMap<>();
		for (IVariable variable : definedVariables) {
			nameToVariableMap.put(variable.getInternalName(), variable);
		}
		this.variablesByName.add(nameToVariableMap);
		this.definedFunctions.addAll(definedFunctions);
	}

	@Override
	public SimpleProgramContext merge(IProgramContext other) {
		// Merging only works when in the root scope
		assert (this.getCurrentScopeDepth() == 1);
		assert (other.getCurrentScopeDepth() == 1);

		IMutableVariableContext mergedVariableContext = this.getMutableVariableContext().merge(other.getCurrentlyDefinedVariables());
		IDataTypeContext mergedDataTypeContext = this.getCurrentlyDefinedTypes().merge(other.getCurrentlyDefinedTypes());

		Set<IVariable> mergedVariables = new HashSet<>();
		mergedVariables.addAll(this.getCurrentlyDefinedGlobalVariables().get(0));
		mergedVariables.addAll(other.getCurrentlyDefinedGlobalVariables().get(0));

		Set<IFunction> mergedFunctions = new HashSet<>();
		mergedFunctions.addAll(this.getCurrentlyDefinedFunctions());
		mergedFunctions.addAll(other.getCurrentlyDefinedFunctions());

		return new SimpleProgramContext(mergedVariableContext, mergedDataTypeContext, this.getVariableController(), mergedVariables, mergedFunctions);
	}

	@Override
	public void addFunction(IFunction function) {
		definedFunctions.add(function);
	}

	@Override
	public void addGlobalVariable(IVariable createdGlobalVariable, SimpleExpressionConditioner expressionConditioner) {
		definedVariables.get(definedVariables.size() - 1).add(createdGlobalVariable);
		variablesByName.get(variablesByName.size() - 1).put(createdGlobalVariable.getInternalName(), createdGlobalVariable);
		variableContext.updateVariableContext(createdGlobalVariable, expressionConditioner);
		invalidateCache();
	}

	@Override
	public IVariable addVariable(ParameterType parameterType, DataType dataType, String name, String internalName) {
		IVariable result = variableController.createVariable(parameterType, dataType, name, internalName, this, null);
		invalidateCache();
		return result;
	}

	@Override
	public IVariable addShadowVariable(ParameterType parameterType, DataType dataType, String name, String internalName, IShadowInformation shadowInformation) {
		IVariable result = variableController.createVariable(parameterType, dataType, name, internalName, this, shadowInformation);
		invalidateCache();
		return result;
	}

	@Override
	public IVariable addLocalVariable(DataType dataType, String variableName, String internalName) {
		IVariable variable = variableController.createVariable(ParameterType.INTERNAL_CONTROL, dataType, variableName, internalName, this, null);
		invalidateCache();
		return variable;
	}

	@Override
	public IVariable renameVariable(IVariable var, String replacerName) {
		IVariable replacerVariable = var.rename(replacerName, var.getInternalName() + "_renamedTo_" + replacerName);
		addGlobalVariable(replacerVariable, variableContext.getVariableConditioner(replacerVariable));
		return replacerVariable;
	}

	@Override
	public IVariableContext getCurrentlyDefinedVariables() {
		if (variableContextCache == null) {
			variableContextCache = new SimpleVariableContext(variableContext);
		}
		return variableContextCache;
	}

	@Override
	public IMutableVariableContext getMutableVariableContext() {
		return variableContext.copy();
	}

	@Override
	public IDataTypeContext getCurrentlyDefinedTypes() {
		return dataTypeContext.copy();
	}

	@Override
	public ImmutableList<ImmutableSet<IVariable>> getCurrentlyDefinedGlobalVariables() {
		ArrayList<ImmutableSet<IVariable>> result = new ArrayList<>();
		for (Set<IVariable> definedVariable : definedVariables) {
			Set<IVariable> filteredVariables = new HashSet<>(definedVariable);
			filteredVariables.removeIf(v -> v.getParameterType() == ParameterType.INTERNAL_SHADOW);
			result.add(ImmutableSet.copyOf(filteredVariables));
		}
		return ImmutableList.copyOf(result);
	}

	@Override
	public IFunction getCurrentlyDefinedFunctionByString(String internalName) {
		for (IFunction func : definedFunctions) {
			if (func.getName().equals(internalName)) {
				return func;
			}
		}
		return null;
	}

	@Override
	public ImmutableSet<IFunction> getCurrentlyDefinedFunctions() {
		return ImmutableSet.copyOf(definedFunctions);
	}

	@Override
	public ArrayVariable addArrayInstanceByCopyingGivenVariables(ArrayType arrayType, List<IVariable> variablesToCopyToArrayEntries, String name, String internalName) {
		if (arrayType.getEntriesCount() != variablesToCopyToArrayEntries.size()) {
			throw new RuntimeException("Struct type and supplied members count mismatch.");
		}

		ArrayVariable arrayInstance;
		if (arrayType.isInteger()) {
			List<IntegerVariable> members = new ArrayList<>();
			for (int memberIndex = 0; memberIndex < arrayType.getEntriesCount(); memberIndex++) {
				final String memberName = "array" + name + "Var" + memberIndex;
				IntegerVariable member = (IntegerVariable) variablesToCopyToArrayEntries.get(memberIndex).rename(memberName, internalName + '_' + memberName);
				members.add(member);
				variableController.registerVariable(member, member.getDataType(), this, getCurrentlyDefinedVariables().getVariableConditioner(variablesToCopyToArrayEntries.get(memberIndex)));
			}
			arrayInstance = new IntegerArrayVariable(null, arrayType, ParameterType.SIGNAL, name, internalName, null, arrayType.getDimensions(), members);
		} else if (arrayType.isFloatingPoint()) {
			List<FloatingPointVariable> members = new ArrayList<>();
			for (int memberIndex = 0; memberIndex < arrayType.getEntriesCount(); memberIndex++) {
				final String memberName = "array" + name + "arrayVar" + memberIndex;
				FloatingPointVariable member = (FloatingPointVariable) variablesToCopyToArrayEntries.get(memberIndex).rename(memberName, internalName + '_' + memberName);
				members.add(member);
				variableController.registerVariable(member, member.getDataType(), this, getCurrentlyDefinedVariables().getVariableConditioner(variablesToCopyToArrayEntries.get(memberIndex)));
			}
			arrayInstance = new FloatingPointArrayVariable(null, arrayType, ParameterType.SIGNAL, name, internalName, null, arrayType.getDimensions(), members);
		} else {
			throw new RuntimeException("Not implemented (yet)!");
		}

		variableController.registerVariable(arrayInstance, arrayType, this, null);
		return arrayInstance;
	}

	@Override
	public IProgramContext copy() {
		return new SimpleProgramContext(this);
	}

	@Override
	public void addTypedef(String newName, DataType existingType) {
		dataTypeContext.addTypedef(newName, existingType);
	}

	@Override
	public void registerType(DataType dataType) {
		dataTypeContext.addType(dataType);
	}

	@Override
	public IVariable getDefinedVariableByName(String name) {
		for (int i = variablesByName.size() - 1; i >= 0; --i) {
			if (variablesByName.get(i).containsKey(name)) {
				return variablesByName.get(i).get(name);
			}
		}
		return null;
	}

	@Override
	public void updateVariableContext(Variable v, SimpleExpressionConditioner expressionConditioner) {
		variableContext.updateVariableContext(v, expressionConditioner);
	}

	@Override
	public void copyVariableContextAndValues(IVariable from, IVariable to) {
		if (from instanceof IMemberContainer) {
			IMemberContainer memberContainerFrom = (IMemberContainer) from;
			IMemberContainer memberContainerTo = (IMemberContainer) to;
			List<IVariable> membersFrom = new ArrayList<>(memberContainerFrom.getMembers());
			List<IVariable> membersTo = new ArrayList<>(memberContainerTo.getMembers());
			for (int i = 0; i < membersFrom.size(); i++) {
				copyVariableContextAndValues(membersFrom.get(i), membersTo.get(i));
			}
		}
		if ((to instanceof IPointerVariable) && (from instanceof IPointerVariable)) {
			if (pointerAssignment.hasAssignmentForVariable(to)) {
				pointerAssignment.copyAssignment(from, to);
			}
		}
		if (to == null) {
			throw new RuntimeException("To null");
		}
		to.setValue(from.getValueAsString().toString().trim());
		variableContext.updateVariableContext(to, getCurrentlyDefinedVariables().getVariableConditioner(from));
	}

	@Override
	public StructType addStruct(String name, LinkedHashMap<String, String> members) {
		return dataTypeContext.defineStruct(name, members);
	}

	@Override
	public StructType addTypedefdStruct(String name, LinkedHashMap<String, String> members, String structDataTypeName) {
		return dataTypeContext.defineTypedefdStruct(name, members, structDataTypeName);
	}

	@Override
	public UnionType addUnion(String name, LinkedHashMap<String, String> members) {
		 return dataTypeContext.defineUnion(name, members);
	}

	@Override
	public void addForbiddenDatatype(DataType type) {
		dataTypeContext.addForbiddenDataType(type);
	}

	@Override
	public UnionType addTypedefdUnion(String name, LinkedHashMap<String, String> members, String unionDataTypeName) {
		return dataTypeContext.defineTypedefdUnion(name, members, unionDataTypeName);
	}

	@Override
	public DataType addPointerType(DataType pointedToType) {
		return dataTypeContext.addPointerType(pointedToType);
	}

	@Override
	public DataType addArrayType(DataType arrayType, List<Integer> dimensions) {
		return dataTypeContext.addArrayType(arrayType, dimensions);
	}

	@Override
	public void updatePointerTarget(IPointerVariable pointerVariable, IVariable target) {
		pointerAssignment.setPointerTarget(pointerVariable, target);
	}

	@Override
	public IPointerAssignment getCurrentPointerAssignment() {
		return pointerAssignment;
	}

	@Override
	public void enterScope() {
		definedVariables.add(new HashSet<>());
		variablesByName.add(new HashMap<>());
	}

	@Override
	public void exitScope() {
		assert (definedVariables.size() > 1);
		definedVariables.remove(definedVariables.size() - 1);
		variablesByName.remove(variablesByName.size() - 1);
	}

	@Override
	public int getCurrentScopeDepth() {
		return definedVariables.size();
	}

	@Override
	public IVariableController getVariableController() {
		return variableController;
	}
}
