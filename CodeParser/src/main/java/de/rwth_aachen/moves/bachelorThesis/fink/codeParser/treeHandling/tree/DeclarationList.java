package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree;

import java.util.List;

public class DeclarationList implements ITree {
	private final List<ITree> elements;

	public DeclarationList(List<ITree> elements) {
		this.elements = elements;
	}

	public List<ITree> getElements() {
		return elements;
	}
}
