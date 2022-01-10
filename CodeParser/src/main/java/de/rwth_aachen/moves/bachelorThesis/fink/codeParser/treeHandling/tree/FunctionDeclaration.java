package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;

public class FunctionDeclaration implements ITree {
	private final String name;
	private final String returnType;
	private final FormulaTreeNode body;

	public FunctionDeclaration(String functionName, String returnType, FormulaTreeNode body) {
		this.name = functionName;
		this.returnType = returnType;
		this.body = body;
	}

	public String getName() {
		return name;
	}

	public String getReturnType() {
		return returnType;
	}

	public FormulaTreeNode getBody() {
		return body;
	}
}
