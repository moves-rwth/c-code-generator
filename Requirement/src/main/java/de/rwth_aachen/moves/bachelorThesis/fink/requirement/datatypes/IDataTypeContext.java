package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * An instance of interface contains all defined DataTypes which can be used
 * to generate c-code.
 *
 * There are basic types: { 8/16/32-Bit Unsigned/Signed integers, float, double. boolean }
 * And any additional, custom defined ones: { structs, unions, typedefs }
 */
public interface IDataTypeContext {
	/**
	 * @return All permitted DataTypes without Booleans.
	 */
	Set<DataType> noBool();

	/**
	 * Adds a DataType to the list of forbidden DataTypes.
	 * @param forbiddenDataType Forbidden DataType to add.
	 */
	void addForbiddenDataType(DataType forbiddenDataType);

	Set<DataType> getPossibleTypes(Set<Operators> operators);

	/**
	 * @return All permitted DataTypes.
	 */
	Set<DataType> all();

	/**
	 * @return All permitted, non-void BasicTypes (int, float, double, bool), their typedefs and pointers to them.
	 */
	Set<DataType> allBasicTypes();

	/**
	 * @return All permitted ComplexTypes (structs, unions), their typedefs and pointers to them.
	 */
	Set<DataType> allComplexTypes();

	/**
	 * @return All permitted signed Integers, their typedefs and pointers to them.
	 */
	Set<DataType> signedIntegers();

	/**
	 * Chooses a random, permitted DataType from a given set under an uniform distribution.
	 * @param dataTypes Set of Operators to pick from.
	 * @return A random, permitted Operator.
	 */
	DataType oneOf(Set<DataType> dataTypes);

	/**
	 * @return All permitted unsigned Integers, their typedefs and pointers to them.
	 */
	Set<DataType> unsignedIntegers();

	/**
	 * @return A set which contains INSTANCE_BOOL iff the DataType is permitted.
	 */
	Set<DataType> bool();

	/**
	 * @return All permitted Integers, their typedefs and pointers to them.
	 */
	Set<DataType> integers();

	/**
	 * @return All permitted signed Integers (which includes bool), their typedefs and pointers to them.
	 */
	Set<DataType> integersAndBool();

	/**
	 * @param typeName Name of the type to retrieves.
	 * @return The DataType with that typeName.
	 */
	DataType byName(String typeName);

	/**
	 * Creates a typedef of given DataType.
	 * @param newTypeName Name of the typedef.
	 * @param existingType Type of the typedef.
	 */
	void addTypedef(String newTypeName, DataType existingType);

	/**
	 * Creates a struct containing the given members.
	 * @param name Name of the struct.
	 * @param members Members of the struct.
	 */
	StructType defineStruct(String name, LinkedHashMap<String, String> members);

	/**
	 * Creates an union containing the given members.
	 * @param name Name of the Union.
	 * @param members Members of the Union.
	 */
	UnionType defineUnion(String name, LinkedHashMap<String, String> members);

	/**
	 * Creates a typedef of a given struct.
	 * @param name Name of the struct.
	 * @param members Members of the struct.
	 * @param baseTypeName Name of the typedef.
	 */
	StructType defineTypedefdStruct(String name, LinkedHashMap<String, String> members, String baseTypeName);

	/**
	 * Creates a typedef of a given union.
	 * @param name Name of the union.
	 * @param members Members of the union.
	 * @param baseTypeName Name of the typedef.
	 */
	UnionType defineTypedefdUnion(String name, LinkedHashMap<String, String> members, String baseTypeName);

	/**
	 * Adds a given type to this context. Any name duplicates will be resolved
	 * by removing the old DataType.
	 * @param newType DataType to add / overwrite.
	 */
	void addType(DataType newType);

	/**
	 * Will create a new PointerType pointing to the given DataType iff such a
	 * pointer does not already exist within this context.
	 *
	 * @param pointedToType DataType to create a pointer to.
	 * @return The DataType within this context pointing to the given DataType.
	 */
	DataType addPointerType(DataType pointedToType);


	/**
	 * Will create a new ArrayType with the given baseType and array dimensions if it does not exist already
	 *
	 * @param arrayType  DataType of the array.
	 * @param dimensions Dimensions of the array.
	 * @return The DataType within this context pointing to the given DataType.
	 */
	DataType addArrayType(DataType arrayType, List<Integer> dimensions);

	/**
	 * Note that this will be a deep copy of the _members_ of this class.
	 * The DataTypes themselves are a shallow copy.
	 *
	 * @return Returns a copy of this context.
	 */
	IDataTypeContext copy();

	/**
	 * Will combine this context with a given second one to a third context,
	 * combining all known DataTypes into one.
	 * If both contexts contain DataTypes which clash (same name + typelevel),
	 * only the DataType from this instance will be added.
	 * @param other Context of the DataTypes to add.
	 * @return A new context containing DataTypes from both without conflicts.
	 */
	IDataTypeContext merge(IDataTypeContext other);

	/**
	 * @return All defined DataTypes.
	 */
	ImmutableSet<DataType> getKnownDataTypes();

	/**
	 * @return All forbidden DataTypes.
	 */
	ImmutableSet<DataType> getForbiddenDataTypes();
}
