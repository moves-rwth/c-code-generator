package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import java.util.LinkedHashMap;

public class TypedefdStructType extends StructType {

	private final String baseTypeName;

	public TypedefdStructType(String structName, LinkedHashMap<String, String> members, String baseTypeName) {
		super(structName, members);
		this.baseTypeName = baseTypeName;
	}

	public String getBaseTypeName() {
		return baseTypeName;
	}
}
