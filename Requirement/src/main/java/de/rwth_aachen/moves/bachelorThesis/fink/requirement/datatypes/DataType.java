package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.DataTypeException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.Utility;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IRange;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;

/**
 * An instance of DataType (and/or any instance that inherits from DataType)
 * represents a valid type usable in the final C-Code. This includes
 * all "basic" types (such as int, float, double, char, ...)
 * but also "custom" C-Types (structs, unions), as well as
 * typedefs.
 * Note that any pointer of arbitrary (and sensible) "depth" to a known type
 * is always valid in C, but in this implementation, each "pointer type"
 * has to be its own instance. For example:
 * - int,
 * - int *
 * - int **
 * will be three instances (two of which are PointerType).
 */
public class DataType implements Serializable {
	public static int SizeOfIntInBytes = 4;

	public int getTypeLevel() {
		return typeLevel;
	}

	public String getTypeName() {
		return typeName;
	}

	public long getLowerLimit() {
		return lowerLimit;
	}

	public long getUpperLimit() {
		return upperLimit;
	}

	public double getLowerLimitFp() {
		return lowerLimitFp;
	}

	public double getUpperLimitFp() {
		return upperLimitFp;
	}

	public int getBitCount() {
		return bitCount;
	}

	public boolean isBasic() {
		return this.isBasic;
	}

	public boolean isSigned() {
		return this.isSigned;
	}

	public boolean isInteger() {
		return this.isInteger;
	}

	public boolean isFloatingPoint() {
		return this.isFloatingPoint;
	}

	public boolean isBool() {
		return (isInteger() && getLowerLimit() >= 0 && getUpperLimit() <= 1);
	}

	public boolean isVoid() {
		return isBasic() && typeLevel == -2;
	}

	public boolean requiresTypeDef() {
		return false;
	}

	public boolean requiresForwardDeclaration() {
		return false;
	}

	public StringArray getForwardDeclaration() {
		return new StringArray();
	}

	public StringArray getTypeDef(IDataTypeContext dataTypeContext) {
		return new StringArray();
	}

	private final int typeLevel;
	private final String typeName;
	private final String nondeterministicGeneratorFunction;

	private final boolean isBasic; // true iff type is not union/struct (or pointer/typedef of that)
	private final boolean isSigned;
	private final boolean isInteger;
	private final boolean isFloatingPoint;

	private final long lowerLimit;
	private final double lowerLimitFp;
	private final long upperLimit;
	private final double upperLimitFp;

	private final int bitCount;

	private final LinkedHashMap<String, String> members = new LinkedHashMap<>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DataType(name = ").append(typeName).append(", isBasic = ").append(isBasic).append(", members = {");
		boolean isFirst = true;
		for (String memberName : members.keySet()) {
			if (!isFirst) {
				sb.append(", ");
			}
			isFirst = false;
			sb.append("(").append(memberName).append(" -> ").append(members.get(memberName)).append(")");
		}
		sb.append("})");
		return sb.toString();
	}

	private DataType(
			int typeLevel, String typeName, String nondeterministicGeneratorFunction,
			boolean isBasic, boolean isSigned, boolean isInteger, boolean isFloatingPoint,
			long lowerLimit, double lowerLimitFp, long upperLimit, double upperLimitFp,
			int bitCount,
			LinkedHashMap<String, String> members) {
		this.typeLevel = typeLevel;
		this.typeName = typeName;
		this.nondeterministicGeneratorFunction = nondeterministicGeneratorFunction;
		this.isBasic = isBasic;
		this.isSigned = isSigned;
		this.isInteger = isInteger;
		this.isFloatingPoint = isFloatingPoint;
		this.lowerLimit = lowerLimit;
		this.lowerLimitFp = lowerLimitFp;
		this.upperLimit = upperLimit;
		this.upperLimitFp = upperLimitFp;
		this.bitCount = bitCount;
		this.members.putAll(members);

		if (this.nondeterministicGeneratorFunction.isEmpty() && isBasic && (typeLevel >= 0)) {
			throw new DataTypeException("Can not create basic data type without generator function.");
		}
	}

	protected DataType(int typeLevel, String typeName, String nondeterministicGeneratorFunction, boolean isBasic, boolean isSigned, boolean isInteger, boolean isFloatingPoint, long lowerLimit, double lowerLimitFp, long upperLimit, double upperLimitFp, int bitCount) {
		this(typeLevel, typeName, nondeterministicGeneratorFunction,
				isBasic, isSigned, isInteger, isFloatingPoint,
				lowerLimit, lowerLimitFp, upperLimit, upperLimitFp,
				bitCount,
				new LinkedHashMap<>());
	}

	protected DataType(String typeName, LinkedHashMap<String, String> members) {
		this(-1, typeName, "",
				false, false, false, false,
				0, 0D, 0, 0D,
				-1,
				members);
	}

	protected DataType(String aliasName, DataType base) {
		this(base.typeLevel, aliasName, base.nondeterministicGeneratorFunction,
				base.isBasic, base.isSigned, base.isInteger, base.isFloatingPoint,
				base.lowerLimit, base.lowerLimitFp, base.upperLimit, base.upperLimitFp,
				base.bitCount,
				base.members);
	}

	public LinkedHashMap<String, String> getMembers() {
		return new LinkedHashMap<>(members);
	}

	public static final DataType INSTANCE_NONE = new DataType(-1, "INVALID", "", true, false, false, false, 0, 0D, 0, 0D, -1);
	public static final DataType INSTANCE_VOID = new DataType(-2, "void", "", true, false, false, false, 0, 0D, 0, 0D, -1);
	public static final DataType INSTANCE_BOOL = new BooleanType();
	public static final DataType INSTANCE_INT8 = new DataType(1, "signed char", "__VERIFIER_nondet_char", true, true, true, false, -128, -128D, 127, 127D, 8);
	public static final DataType INSTANCE_UINT8 = new DataType(2, "unsigned char", "__VERIFIER_nondet_uchar", true, false, true, false, 0, 0D, 255, 255D, 8);
	public static final DataType INSTANCE_INT16 = new DataType(3, "signed short int", "__VERIFIER_nondet_short", true, true, true, false, -32768, -32768D, 32767, 32767D, 16);
	public static final DataType INSTANCE_UINT16 = new DataType(4, "unsigned short int", "__VERIFIER_nondet_ushort", true, false, true, false, 0, 0D, 65535, 65535D, 16);
	public static final DataType INSTANCE_INT32 = new DataType(5, "signed long int", "__VERIFIER_nondet_long", true, true, true, false, -2147483648L, -2147483648D, 2147483647L, 2147483647D, 32);
	public static final DataType INSTANCE_UINT32 = new DataType(6, "unsigned long int", "__VERIFIER_nondet_ulong", true, false, true, false, 0, 0D, 4294967295L, 4294967295D, 32);
	public static final DataType INSTANCE_FLOAT = new DataType(7, "float", "__VERIFIER_nondet_float", true, true, false, true, (long) -Float.MAX_VALUE, -Float.MAX_VALUE, (long) Float.MAX_VALUE, Float.MAX_VALUE, 32);
	public static final DataType INSTANCE_DOUBLE = new DataType(8, "double", "__VERIFIER_nondet_double", true, true, false, true, (long) -Double.MAX_VALUE, -Double.MAX_VALUE, (long) Double.MAX_VALUE, Double.MAX_VALUE, 64);


	private static DataType fromTypeLevel(int level) {
		switch (level) {
			case -2:
				return INSTANCE_VOID;
			case -1:
				return INSTANCE_NONE;
			case 0:
				return INSTANCE_BOOL;
			case 1:
				return INSTANCE_INT8;
			case 2:
				return INSTANCE_UINT8;
			case 3:
				return INSTANCE_INT16;
			case 4:
				return INSTANCE_UINT16;
			case 5:
				return INSTANCE_INT32;
			case 6:
				return INSTANCE_UINT32;
			case 7:
				return INSTANCE_FLOAT;
			case 8:
				return INSTANCE_DOUBLE;
			default:
				throw new RuntimeException("Failed to convert level '" + level + "' to a DataType!");
		}
	}

	public static long getMaximumUpperLimit(Set<DataType> dataTypes) {
		long maximum = Long.MIN_VALUE;
		for (DataType d : dataTypes) {
			if (!d.isBasic()) {
				throw new DataTypeException("Type '" + d + "' is not basic, can not determine upper limit.");
			}
			maximum = Math.max(maximum, d.getUpperLimit());
		}

		return maximum;
	}

	public static long getMinimumLowerLimit(Set<DataType> dataTypes) {
		long minimum = Long.MAX_VALUE;
		for (DataType d: dataTypes) {
			if (!d.isBasic()) {
				throw new DataTypeException("Type '" + d + "' is not basic, can not determine lower limit.");
			}
			minimum = Math.min(minimum, d.getLowerLimit());
		}

		return minimum;
	}

	public static boolean isAllUnsigned(Set<DataType> dataTypes) {
		for (DataType d: dataTypes) {
			if (d.isSigned()) {
				if (!d.isBasic()) {
					throw new DataTypeException("Type '" + d + "' is not basic, can not determine signedness!");
				}
				return false;
			}
		}
		return true;
	}

	public String toCTypeName() {
		return typeName;
	}

	public String toCDeclarationName() {
		return typeName;
	}

	public String afterInstanceNameForInitialization() {
		return "";
	}

	public String getVerifierNonDetFunction(IDataTypeContext dataTypeContext) {
		if (isBasic) {
			if (isFloatingPoint) {
				return String.format(nondeterministicGeneratorFunction, getLowerLimitFp(), getUpperLimitFp());
			}
			return String.format(nondeterministicGeneratorFunction, getLowerLimit(), getUpperLimit());
		}
		StringBuilder stringBuilder = new StringBuilder();
		boolean isFirst = true;
		stringBuilder.append("{ ");
		for (String memberName: members.keySet()) {
			if (!isFirst) {
				stringBuilder.append(", ");
			}
			isFirst = false;
			final DataType childType = dataTypeContext.byName(members.get(memberName));
			stringBuilder.append(".").append(memberName).append(" = ").append(childType.getVerifierNonDetFunction(dataTypeContext));
		}
		stringBuilder.append(" }");
		return stringBuilder.toString();
	}

	/*
		Adds all types that are required to be defined to the set and returns it.
		Detects recursion and does not fall for it.
		Resulting set will _not_ contain the initial this.
	 */
	public Set<DataType> getRequiredTypes(IDataTypeContext dataTypeContext) {
		Set<DataType> alreadyKnownTypes = new HashSet<>();
		alreadyKnownTypes.add(this);
		alreadyKnownTypes = getRequiredTypesInternal(alreadyKnownTypes, dataTypeContext);
		alreadyKnownTypes.remove(this);
		return alreadyKnownTypes;
	}

	protected Set<DataType> getRequiredTypesInternal(Set<DataType> alreadyKnownTypes, IDataTypeContext dataTypeContext) {
		if (alreadyKnownTypes.contains(this)) {
			return alreadyKnownTypes;
		}
		for (String childTypeName : members.values()) {
			final DataType childType = dataTypeContext.byName(childTypeName);
			alreadyKnownTypes = childType.getRequiredTypesInternal(alreadyKnownTypes, dataTypeContext);
		}
		return alreadyKnownTypes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DataType that = (DataType) o;
		return (Objects.equals(typeName, that.typeName) && Objects.equals(typeLevel, that.typeLevel));
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeName);
	}

	public static DataType getSignedInt() {
		switch (SizeOfIntInBytes) {
			case 2: return INSTANCE_INT16;
			case 4: return INSTANCE_INT32;
			default: throw new RuntimeException("Invalid SizeOfIntInBytes value " + SizeOfIntInBytes);
		}
	}

	public static DataType getUnsignedInt() {
		switch (SizeOfIntInBytes) {
			case 2: return INSTANCE_UINT16;
			case 4: return INSTANCE_UINT32;
			default: throw new RuntimeException("Invalid SizeOfIntInBytes value " + SizeOfIntInBytes);
		}
	}

	public static DataType getSignedLong() { return INSTANCE_INT32;	}
	public static DataType getUnsignedLong() { return INSTANCE_UINT32; }

	private static DataType doIntegralPromotionIfApplicable(DataType type) {
		// NOTE(Felix): From the C standard, 6.2.2.1:
		// "If an int can represent all values of the original type, the value is converted to an int;
		//  otherwise, it is converted to an unsigned int".
		// Note that "conversion to an unsigned int" only happens if "unsigned short == unsigned int",
		// meaning "in reality" (on our platform), no conversion takes place.

		if ((SizeOfIntInBytes == 4 && (equal(type, INSTANCE_INT8) || equal(type, INSTANCE_UINT8) || equal(type, INSTANCE_INT16) || equal(type, INSTANCE_UINT16))) ||
			(SizeOfIntInBytes == 2 && (equal(type, INSTANCE_INT8) || equal(type, INSTANCE_UINT8)))) {
			// Promotion to signed int
			return getSignedInt();
		}

		// Else: No promotion.
		return type;
	}

	public static DataType doTypeWidening(IRange range) {
		range.assertUnderlyingTypesAreUnambiguous();;
		DataType type = (DataType) range.getUnderlyingTypes().toArray()[0];
		return doTypeWidening(type);
	}

	public static DataType doTypeWidening(IRange rangeA, IRange rangeB) {
		DataType typeA = DataTypeContext.getSingle(rangeA.getUnderlyingTypes());
		DataType typeB = DataTypeContext.getSingle(rangeB.getUnderlyingTypes());
		return doTypeWidening(typeA, typeB);
	}

	public static DataType doTypeWidening(DataType type) {
		return doIntegralPromotionIfApplicable((type instanceof AliasType) ? ((AliasType)type).getTrueBaseType() : type);
	}

	public static DataType doTypeWidening(DataType typeA, DataType typeB) {
		// NOTE(Felix): We need to work with our "C"-base types, not typedef'd stuff.
		if (typeA instanceof AliasType) { typeA = ((AliasType)typeA).getTrueBaseType(); }
		if (typeB instanceof AliasType) { typeB = ((AliasType)typeB).getTrueBaseType(); }

		typeA = doIntegralPromotionIfApplicable(typeA);
		typeB = doIntegralPromotionIfApplicable(typeB);

		// Check C89 standard 6.2.1.5 - "Usual arithmetic conversion"
		DataType typeToPromoteTo = null;
		if (equal(typeA, INSTANCE_DOUBLE) || equal(typeB, INSTANCE_DOUBLE)) {
			typeToPromoteTo = INSTANCE_DOUBLE;
		} else if (equal(typeA, INSTANCE_FLOAT) || equal(typeB, INSTANCE_FLOAT)) {
			typeToPromoteTo = INSTANCE_FLOAT;
		} else if (equal(typeA, getUnsignedLong()) || equal(typeB, getUnsignedLong())) {
			typeToPromoteTo = getUnsignedLong();
		} else if ((equal(typeA, getSignedLong()) || equal(typeB, getSignedLong()))
			&& (equal(typeA, getUnsignedInt()) || equal(typeB, getUnsignedInt()))) {
			if (getSignedLong().getUpperLimit() >= getUnsignedInt().getUpperLimit()) {
				typeToPromoteTo = getSignedLong();
			} else {
				typeToPromoteTo = getUnsignedLong();
			}
		} else if (equal(typeA, getSignedLong()) || equal(typeB, getSignedLong())) {
			typeToPromoteTo = getSignedLong();
		} else if (equal(typeA, getUnsignedInt()) || equal(typeB, getUnsignedInt())) {
			typeToPromoteTo = getUnsignedInt();
		} else {
			typeToPromoteTo = getSignedInt();
		}

		return (typeToPromoteTo);
	}

	public long truncateValue(long value) {
		return truncateValue(value, this);
	}

	public static long truncateValue(long value, DataType type) {
		assert(type.isInteger());
		long rangeOfPossibleValues = type.getUpperLimit() - type.getLowerLimit() + 1;
		long offsetFromZero = type.getLowerLimit();
		long result = Utility.euclideanModulus(value + offsetFromZero, rangeOfPossibleValues) + offsetFromZero;
		return result;
	}

	public static boolean equal(DataType a, DataType b) {
		if (a.getTypeName().equals(b.getTypeName()) && a.getTypeLevel() == b.getTypeLevel()) {
			return true;
		}
		return false;
	}
}
