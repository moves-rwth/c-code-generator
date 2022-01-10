package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;

public class GlobalVariableDeclaration implements ITree {
	private final FormulaTreeNode declarations;

	public GlobalVariableDeclaration(FormulaTreeNode declarations) {
		this.declarations = declarations;
	}

	public FormulaTreeNode getDeclarations() {
		return declarations;
	}
}
