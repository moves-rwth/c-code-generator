package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

public class PointerType extends DataType implements IPointerType {
	private final DataType baseType;

	public PointerType(DataType baseType) {
		super(buildResultingTypeName(baseType), baseType);
		this.baseType = baseType;
	}

	public static String buildResultingTypeName(DataType baseType) {
		return baseType.toCDeclarationName() + "*";
	}

	@Override
	public DataType getBaseType() {
		return baseType;
	}
}
