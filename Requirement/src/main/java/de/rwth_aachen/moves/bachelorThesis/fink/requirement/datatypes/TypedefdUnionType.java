package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import java.util.LinkedHashMap;

public class TypedefdUnionType extends UnionType {

	private final String baseTypeName;

	public TypedefdUnionType(String unionName, LinkedHashMap<String, String> members, String baseTypeName) {
		super(unionName, members);
		this.baseTypeName = baseTypeName;
	}

	public String getBaseTypeName() {
		return baseTypeName;
	}
}
