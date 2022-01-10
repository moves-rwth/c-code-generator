package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.OperatorReturnType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;

import java.io.Serializable;
import java.util.*;

/**
 * Check the interface for an explanation of this class.
 */
public class DataTypeContext implements IDataTypeContext, Serializable {
	public final static Set<DataType> builtinDataTypes = ImmutableSet.of(
			DataType.INSTANCE_INT8, DataType.INSTANCE_UINT8,
			DataType.INSTANCE_INT16, DataType.INSTANCE_UINT16,
			DataType.INSTANCE_INT32, DataType.INSTANCE_UINT32,
			DataType.INSTANCE_FLOAT, DataType.INSTANCE_DOUBLE,
			DataType.INSTANCE_BOOL,
			DataType.INSTANCE_VOID
	);

	private final Set<DataType> forbiddenDataTypes;
	private final Set<DataType> knownDataTypes;
	private final Map<String, DataType> typeNameToTypeMap = new HashMap<>();

	@Override
	public void addForbiddenDataType(DataType forbiddenDataType) {
		forbiddenDataTypes.add(forbiddenDataType);
	}

	private DataTypeContext(Set<DataType> knownDataTypes, Set<DataType> forbiddenDataTypes) {
		this.knownDataTypes = new HashSet<>();
		this.knownDataTypes.addAll(knownDataTypes);
		this.forbiddenDataTypes = forbiddenDataTypes;
		rebuildTypeMap();
	}

	public DataTypeContext() {
		this(builtinDataTypes, new HashSet<>());
	}

	public DataTypeContext(IDataTypeContext other) {
		this(other.getKnownDataTypes(), other.getForbiddenDataTypes());
	}

	public DataTypeContext(Set<DataType> knownDataTypes) {
		this(knownDataTypes, new HashSet<>());
	}

	@Override
	public IDataTypeContext merge(IDataTypeContext other) {
		Set<DataType> mergedKnownDataTypes = new HashSet<>(this.getKnownDataTypes());
		for (DataType dataTypeOther : other.getKnownDataTypes()) {
			boolean shouldAdd = true;
			for (DataType dataTypeThis : this.getKnownDataTypes()) {
				if (DataType.equal(dataTypeOther, dataTypeThis)) {
					shouldAdd = false;
					break;
				}
			}
			if (shouldAdd) {
				mergedKnownDataTypes.add(dataTypeOther);
			}
		}
		return new DataTypeContext(mergedKnownDataTypes);
	}

	private void rebuildTypeMap() {
		typeNameToTypeMap.clear();
		for (DataType dataType : knownDataTypes) {
			typeNameToTypeMap.put(dataType.getTypeName(), dataType);
		}
	}

	@Override
	public Set<DataType> noBool() {
		Set<DataType> result = new HashSet<>(knownDataTypes);
		result.remove(DataType.INSTANCE_BOOL);
		result.remove(DataType.INSTANCE_NONE);

		return result;
	}

	@Override
	public Set<DataType> getPossibleTypes(Set<Operators> operators) {
		Set<DataType> result = new HashSet<>();
		if (Collections.disjoint(operators, Operators.getPossibleChildOperators(OperatorReturnType.GENERAL_BOOLEAN))) {
			result.addAll(noBool());
		} else if (Collections.disjoint(operators, Operators.getPossibleChildOperators(OperatorReturnType.ARITHMETIC))) {
			result.addAll(bool());
		}

		return result;
	}

	@Override
	public Set<DataType> all() {
		Set<DataType> result = new HashSet<>(knownDataTypes);
		result.remove(DataType.INSTANCE_NONE);
		result.removeAll(forbiddenDataTypes);
		return result;
	}

	@Override
	public Set<DataType> allBasicTypes() {
		Set<DataType> result = new HashSet<>();
		for (DataType dataType : knownDataTypes) {
			if (dataType.isBasic() && dataType.getTypeLevel() >= 0) {
				result.add(dataType);
			}
		}
		result.remove(DataType.INSTANCE_NONE);
		result.removeAll(forbiddenDataTypes);
		return result;
	}

	@Override
	public Set<DataType> allComplexTypes() {
		Set<DataType> result = new HashSet<>();
		for (DataType dataType : knownDataTypes) {
			if (!dataType.isBasic()) {
				result.add(dataType);
			}
		}
		result.remove(DataType.INSTANCE_NONE);
		result.removeAll(forbiddenDataTypes);
		return result;
	}

	@Override
	public Set<DataType> signedIntegers() {
		Set<DataType> result = new HashSet<>();
		final Set<DataType> all = all();
		for (DataType d : all) {
			if (d.isInteger() && d.isSigned()) {
				result.add(d);
			}
		}
		return result;
	}

	@Override
	public Set<DataType> unsignedIntegers() {
		Set<DataType> result = new HashSet<>();
		final Set<DataType> all = all();
		for (DataType d: all) {
			if (d.isInteger() && !d.isSigned()) {
				result.add(d);
			}
		}
		return result;
	}

	@Override
	public Set<DataType> bool() {
		Set<DataType> result = new HashSet<>();
		result.add(DataType.INSTANCE_BOOL);
		result.removeAll(forbiddenDataTypes);
		return result;
	}

	@Override
	public Set<DataType> integers() {
		Set<DataType> result = new HashSet<>();
		final Set<DataType> all = all();
		for (DataType d: all) {
			if (d.isInteger()) {
				result.add(d);
			}
		}
		return result;
	}

	@Override
	public Set<DataType> integersAndBool() {
		Set<DataType> result = new HashSet<>();
		final Set<DataType> all = all();
		for (DataType d: all) {
			if (d.isInteger() && d.isSigned()) {
				result.add(d);
			}
		}
		return result;
	}

	@Override
	public DataType byName(String typeName) {
		if (!typeNameToTypeMap.containsKey(typeName)) {
			throw new RuntimeException("There is no type '" + typeName + "' known to this type context!");
		}
		return typeNameToTypeMap.get(typeName);
	}

	@Override
	public void addType(DataType newType) {
		if (typeNameToTypeMap.containsKey(newType.toCTypeName())) {
			final DataType knownType = typeNameToTypeMap.get(newType.toCTypeName());
			knownDataTypes.remove(knownType);
		}
		knownDataTypes.add(newType);
		rebuildTypeMap();
	}

	@Override
	public void addTypedef(String newTypeName, DataType existingType) {
		final AliasType newType = new AliasType(newTypeName, existingType);
		knownDataTypes.add(newType);
		rebuildTypeMap();
	}

	@Override
	public StructType defineStruct(String name, LinkedHashMap<String, String> members) {
		final StructType newType = new StructType(name, members);
		knownDataTypes.add(newType);
		rebuildTypeMap();
		return newType;
	}

	@Override
	public UnionType defineUnion(String name, LinkedHashMap<String, String> members) {
		final UnionType newType = new UnionType(name, members);
		knownDataTypes.add(newType);
		rebuildTypeMap();
		return newType;
	}

	@Override
	public StructType defineTypedefdStruct(String name, LinkedHashMap<String, String> members, String baseTypeName) {
		final StructType newType = new TypedefdStructType(name, members, baseTypeName);
		knownDataTypes.add(newType);
		rebuildTypeMap();
		return newType;
	}

	@Override
	public UnionType defineTypedefdUnion(String name, LinkedHashMap<String, String> members, String baseTypeName) {
		final UnionType newType = new TypedefdUnionType(name, members, baseTypeName);
		knownDataTypes.add(newType);
		rebuildTypeMap();
		return newType;
	}

	@Override
	public IDataTypeContext copy() {
		return new DataTypeContext(this);
	}

	@Override
	public DataType addPointerType(DataType pointedToType) {
		final String typeName = PointerType.buildResultingTypeName(pointedToType);
		if (!typeNameToTypeMap.containsKey(typeName)) {
			final PointerType newType = new PointerType(pointedToType);
			knownDataTypes.add(newType);
			rebuildTypeMap();
		}
		return typeNameToTypeMap.get(typeName);
	}

	@Override
	public DataType addArrayType(DataType arrayType, List<Integer> dimensions) {
		final String typeName = ArrayType.buildResultingTypeName(arrayType, dimensions);
		if (!typeNameToTypeMap.containsKey(typeName)) {
			final ArrayType newType = new ArrayType(arrayType, dimensions);
			knownDataTypes.add(newType);
			rebuildTypeMap();
		}
		return typeNameToTypeMap.get(typeName);
	}

	@Override
	public DataType oneOf(Set<DataType> dataTypes) {
		List<DataType> copy = new ArrayList<>(dataTypes);
		copy.removeAll(forbiddenDataTypes);
		final int chosenIndex = RandomGenHelper.randomInt(0, copy.size() - 1);
		return copy.get(chosenIndex);
	}

	public static DataType getSingle(Set<DataType> dataTypes) {
		if (dataTypes.size() != 1) {
			for (DataType d : dataTypes) {
				for (DataType d2 : dataTypes) {
					DataType type1 = (d instanceof AliasType) ? ((AliasType) d).getTrueBaseType() : d;
					DataType type2 = (d2 instanceof AliasType) ? ((AliasType) d2).getTrueBaseType() : d2;
					if (!type1.equals(type2)) {
						throw new RuntimeException("Expected data types to only contain one, but there were more: " + dataTypes);
					}
				}
			}
		}
		for (DataType d : dataTypes) {
			return (d instanceof AliasType) ? ((AliasType) d).getTrueBaseType() : d;
		}
		throw new RuntimeException("This should be unreachable!");
	}

	public static boolean isBool(Set<DataType> dataTypes) {
		if (dataTypes.size() == 0) {
			return false;
		}
		for (DataType d: dataTypes) {
			if (!d.isBool()) {
				return false;
			}
		}

		return true;
	}

	public static boolean isBasic(Set<DataType> dataTypes) {
		for (DataType d: dataTypes) {
			if (!d.isBasic()) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasFloatingPoint(Set<DataType> dataTypes) {
		if (dataTypes.size() == 0) {
			return false;
		}
		for (DataType d: dataTypes) {
			if (d.isFloatingPoint()) {
				return true;
			}
		}
		return false;
	}

	public static Set<DataType> removeBool(Set<DataType> dataTypes) {
		Set<DataType> result = new HashSet<>();
		for (DataType d: dataTypes) {
			if (!d.isBool()) {
				result.add(d);
			}
		}
		return result;
	}

	public static Set<DataType> removeFloatingPoint(Set<DataType> dataTypes) {
		Set<DataType> result = new HashSet<>();
		for (DataType d: dataTypes) {
			if (!d.isFloatingPoint()) {
				result.add(d);
			}
		}
		return result;
	}

	public static Set<DataType> makeSet(DataType... dataTypes) {
		return new HashSet<>(Arrays.asList(dataTypes));
	}

	@Override
	public ImmutableSet<DataType> getKnownDataTypes() {
		return ImmutableSet.copyOf(knownDataTypes);
	}

	@Override
	public ImmutableSet<DataType> getForbiddenDataTypes() {
		return ImmutableSet.copyOf(forbiddenDataTypes);
	}
}
