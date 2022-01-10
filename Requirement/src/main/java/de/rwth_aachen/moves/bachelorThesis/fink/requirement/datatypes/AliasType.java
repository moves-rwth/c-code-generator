package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;

public class AliasType extends DataType implements IAliasType {

	private final String aliasName;
	private final DataType baseType;

	public AliasType(String aliasName, DataType baseType) {
		super(aliasName, baseType);
		this.aliasName = aliasName;
		this.baseType = baseType;
	}

	@Override
	public boolean requiresTypeDef() {
		return true;
	}

	@Override
	public StringArray getTypeDef(IDataTypeContext dataTypeContext) {
		// typedef unsigned char U8;
		return new StringArray("typedef " + baseType.toCTypeName() + " " + aliasName + ";");
	}

	@Override
	public DataType getBaseType() {
		return baseType;
	}

	@Override
	public DataType getTrueBaseType() {
		if (baseType instanceof AliasType) {
			return ((AliasType)baseType).getTrueBaseType();
		}
		return getBaseType();
	}
}
