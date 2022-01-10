package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree;

public class SimpleTypedefDeclaration implements ITypedefDeclaration {
	private final String newTypeName;
	private final String oldTypeName;

	public SimpleTypedefDeclaration(String newTypeName, String oldTypeName) {
		this.newTypeName = newTypeName;
		this.oldTypeName = oldTypeName;
	}

	public String getNewTypeName() {
		return newTypeName;
	}

	public String getOldTypeName() {
		return oldTypeName;
	}
}
