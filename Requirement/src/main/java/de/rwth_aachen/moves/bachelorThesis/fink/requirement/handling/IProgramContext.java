package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Represents the current context of a program during a particular line of code / scope,
 * but before the actual code is generated.
 * Helps us create the final C-code by keeping track of things like:
 * Which functions / variables exist right now?
 * What is this type and its members? What do we know about these pointers? ...
 */
public interface IProgramContext {

	void addFunction(IFunction function);

	void addGlobalVariable(IVariable createdGlobalVariable, SimpleExpressionConditioner expressionConditioner);

	IVariable addVariable(ParameterType parameterType, DataType dataType, String name, String internalName);

	IVariable addShadowVariable(ParameterType parameterType, DataType dataType, String name, String internalName, IShadowInformation shadowInformation);

	IVariable addLocalVariable(DataType dataType, String variableName, String internalName);

	ArrayVariable addArrayInstanceByCopyingGivenVariables(ArrayType arrayType, List<IVariable> variablesToCopyToArrayEntries, String name, String internalName);

	void addForbiddenDatatype(DataType type);

	IVariable renameVariable(IVariable var, String replacerName);

	IVariableContext getCurrentlyDefinedVariables();

	IMutableVariableContext getMutableVariableContext();

	IDataTypeContext getCurrentlyDefinedTypes();

	ImmutableList<ImmutableSet<IVariable>> getCurrentlyDefinedGlobalVariables();

	ImmutableSet<IFunction> getCurrentlyDefinedFunctions();

	IFunction getCurrentlyDefinedFunctionByString(String internalName);

	IProgramContext copy();

	IProgramContext merge(IProgramContext other);

	void addTypedef(String newName, DataType existingType);

	/**
	 * Adds the given type to the current DataTypeContext.
	 * @param dataType The new DataType.
	 */
	void registerType(DataType dataType);

	/* The following functions create new types and adds them to the current DataTypeContext */
	StructType addStruct(String name, LinkedHashMap<String, String> members);
	StructType addTypedefdStruct(String name, LinkedHashMap<String, String> members, String structDataTypeName);
	UnionType addUnion(String name, LinkedHashMap<String, String> members);
	UnionType addTypedefdUnion(String name, LinkedHashMap<String, String> members, String unionDataTypeName);

	DataType addPointerType(DataType pointedToType);

	DataType addArrayType(DataType arrayType, List<Integer> dimensions);

	/**
	 * Searches the existing variables for one using the internal names.
	 * @param name The internal name of the variable you are looking for.
	 * @return The variable iff it exists, null else.
	 */
	IVariable getDefinedVariableByName(String name);

	void updateVariableContext(Variable v, SimpleExpressionConditioner expressionConditioner);

	void copyVariableContextAndValues(IVariable from, IVariable to);

	/**
	 * Updates the pointer assignment table with the given information.
	 * @param pointerVariable The affected pointer.
	 * @param target The variable being pointed to.
	 */
	void updatePointerTarget(IPointerVariable pointerVariable, IVariable target);

	IPointerAssignment getCurrentPointerAssignment();

	void enterScope();
	void exitScope();
	int getCurrentScopeDepth();

	IVariableController getVariableController();
}
