package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree;

import java.util.LinkedHashMap;

public class UnionTypedefDeclaration implements ITypedefDeclaration {

	private final String name;
	private final LinkedHashMap<String, String> members;
	private final String structDataTypeName;

	public UnionTypedefDeclaration(String name, LinkedHashMap<String, String> members, String structDataTypeName) {
		this.name = name;
		this.members = members;
		this.structDataTypeName = structDataTypeName;
	}

	public String getStructDataTypeName() {
		return structDataTypeName;
	}

	public LinkedHashMap<String, String> getMembers() {
		return members;
	}

	public String getName() {
		return name;
	}
}
