package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.ControlStructureOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.NoOpVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionTarget;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionVisitor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;

import java.io.Serializable;
import java.util.*;

public class CodeObject implements Serializable, IVariableCollectionTarget, ICodeTreeNodeContainer {
	private final String name;
	private final CodeTreeNode codeNode;

	public CodeObject(String name, CodeTreeNode codeNode) {
		this.name = name;
		this.codeNode = codeNode;
	}

	public CodeObject(CodeObject codeObject, VariableReplacer variableReplacer) {
		this(codeObject.getName(), codeObject.getCodeTreeNode().copyTree(variableReplacer));
	}

	public CodeObject(CodeObject codeObject) {
		this(codeObject.getName(), codeObject.getCodeTreeNode().copyTree(new NoOpVariableReplacer()));
	}

	public CodeObject replaceControlStructures(List<ControlStructureOperator> controlStructures) {
		CodeTreeNode node = buildControlStructureTree(controlStructures);
		return new CodeObject(this.getName(), node);
	}

	@Override
	public void accept(IVariableCollectionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CodeObject that = (CodeObject) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	public CodeObject copy(String name) {
		return new CodeObject(name, codeNode);
	}

	public CodeObject copy(VariableReplacer variableReplacer) {
		return new CodeObject(this, variableReplacer);
	}

	// Getter

	public String getName() {
		return name;
	}

	public CodeTreeNode getCodeTreeNode() {
		return codeNode;
	}

	public CodeObject replaceCodeNode(CodeTreeNode newCodeNode) {
		return new CodeObject(this.name, newCodeNode);
	}

	public StringArray toCode(IProgramContext context) {
		IVariableCollector variableCollector = new VariableCollector(false, context);
		variableCollector.visit(this);
		return codeNode.toCode(CodeTreeNode.CodeType.EXECUTION, variableCollector.getUsedVariablesWithAccessor(), context);
	}

	/**
	 * @return the variables which occur inside a last(param) or last_i(param) command
	 */
	public Map<IVariable, Long> getLastVariablesAndDepths(IProgramContext context) {
		Map<IVariable, Long> lastIVariables = new HashMap<>();
		IVariableCollector variableCollector = new VariableCollector(false, context);
		accept(variableCollector);
		codeNode.getLastVariablesAndDepths(variableCollector.getUsedVariables(), lastIVariables, context);
		return lastIVariables;
	}
}
