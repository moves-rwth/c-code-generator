package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.moduling;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;

import java.util.List;

/**
 * A generated Instance with fixed variables of a Module-Template. Generated by the ModuleFactory.
 */
public class ModuleInstance {
	private final FormulaTreeNode codeTree;
	private final List<FormulaTreeNode> localProperties;
	private final List<FormulaTreeNode> globalProperties;

	public ModuleInstance(FormulaTreeNode codeTree, List<FormulaTreeNode> localProperties, List<FormulaTreeNode> globalProperties) {
		this.codeTree = codeTree;
		this.localProperties = localProperties;
		this.globalProperties = globalProperties;
	}

	public FormulaTreeNode getCodeTree() {
		return codeTree;
	}

	public List<FormulaTreeNode> getLocalProperties() {
		return localProperties;
	}

	public List<FormulaTreeNode> getGlobalProperties() {
		return globalProperties;
	}
}