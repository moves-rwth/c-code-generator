package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import java.util.List;

public class ArrayType extends DataType implements IArrayType {
	private final DataType arrayType;
	private final List<Integer> dimensions;

	public ArrayType(DataType arrayType, List<Integer> dimensions) {
		super(buildResultingTypeName(arrayType, dimensions), arrayType);
		this.arrayType = arrayType;
		this.dimensions = dimensions;
	}

	public static String buildResultingTypeName(DataType arrayType, List<Integer> dimensions) {
		String arrayTypeString = arrayType.toCDeclarationName() + " " + dimensions.toString();
		if (arrayType.getTypeLevel() == DataType.INSTANCE_BOOL.getTypeLevel()) {
			arrayTypeString = "BOOL_" + arrayTypeString;
		}
		return arrayTypeString;
	}

	@Override
	public int getEntriesCount() {
		return getDimensions().stream().mapToInt(n -> n).reduce(1, (a, b) -> a * b);
	}

	@Override
	public String toCTypeName() {
		return arrayType.toCTypeName();
	}

	@Override
	public String toCDeclarationName() {
		return arrayType.toCTypeName();
	}

	@Override
	public List<Integer> getDimensions() {
		return dimensions;
	}

	@Override
	public String afterInstanceNameForInitialization() {
		String s = "";
		for (Integer n : dimensions) {
			s = s.concat("[" + n + "]");
		}
		return s;
	}

	@Override
	public DataType getArrayType() {
		return arrayType;
	}
}
