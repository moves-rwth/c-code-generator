package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.ControlStructureOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IMemberContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.CodeObject;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.ICodeTreeNodeContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class VariableCollector implements Serializable, IVariableCollector {

	private final List<CodeTreeNode> codeTreeNodes = new ArrayList<>();
	private final List<VariableTreeNode> variableTreeNodes = new ArrayList<>();
	private final List<VariableTreeNode> outputVariableNodes = new ArrayList<>();

	private final Map<IVariable, List<VariableTreeNode>> inputVarTreeNodesForOutputVar = new HashMap<>();

	private final Set<IVariable> inputVariables = new HashSet<>();
	private final Set<IVariable> outputVariables = new HashSet<>();
	private final Set<IVariable> usedVariables = new HashSet<>();

	private final Map<IVariable, Set<IVariable>> hardDependencyMap = new HashMap<>();

	private final Set<IVariableWithAccessor> inputVariablesWithAccessor = new HashSet<>();
	private final Set<IVariableWithAccessor> outputVariablesWithAccessor = new HashSet<>();
	private final Set<IVariableWithAccessor> usedVariablesWithAccessor = new HashSet<>();

	private final Set<Boolean> booleanConstants = new HashSet<>();
	private final Set<Double> floatingPointConstants = new HashSet<>();
	private final Set<Long> integerConstants = new HashSet<>();

	private final Set<ISimpleValueTreeNode> constantNodes = new HashSet<>();

	private final boolean includeParentsAndSiblings;
	private final IProgramContext programContext;

	public VariableCollector(boolean includeParentsAndSiblings, IProgramContext programContext) {
		this.includeParentsAndSiblings = includeParentsAndSiblings;
		this.programContext = programContext;
	}

	public void clear() {
		codeTreeNodes.clear();
		variableTreeNodes.clear();
		outputVariableNodes.clear();

		inputVariables.clear();
		outputVariables.clear();
		usedVariables.clear();

		booleanConstants.clear();
		floatingPointConstants.clear();
		integerConstants.clear();

		constantNodes.clear();
	}

	public static Set<IVariableWithAccessor> getAllVariableDeclarationsWithinTree(FormulaTreeNode tree) {
		if (tree instanceof DeclarationOperator) {
			DeclarationOperator declarationNode = (DeclarationOperator) tree;
			IVariableWithAccessor variable = ((VariableTreeNode)declarationNode.getChildren().get(0)).getVariableWithAccessor();
			return Set.of(variable);
		}

		Set<IVariableWithAccessor> result = new HashSet<>();
		ImmutableList<FormulaTreeNode> children = tree.getChildren();
		if (tree instanceof ConcatenationOperator) {
			result.addAll(getAllVariableDeclarationsWithinTree(children.get(0)));
			result.addAll(getAllVariableDeclarationsWithinTree(children.get(1)));
		} else if (tree instanceof ItOperator) {
			result.addAll(getAllVariableDeclarationsWithinTree(children.get(1)));
		} else if (tree instanceof IteOperator) {
			result.addAll(getAllVariableDeclarationsWithinTree(children.get(1)));
			result.addAll(getAllVariableDeclarationsWithinTree(children.get(2)));
		}
		return result;
	}


	@Override
	public void visit(ICodeTreeNodeContainer codeTreeNodeContainer) {
		add(codeTreeNodeContainer, programContext);
	}

	@Override
	public void visit(TemplateInfo templateInfo) {
		for (Requirement req : templateInfo.getRequirements()) {
			add(req, programContext);
		}
		for (IFunction func : templateInfo.getFunctions()) {
			add(func, programContext);
		}
		for (CodeObject codeObject : templateInfo.getCodeObjects()) {
			add(codeObject, programContext);
		}
	}

	@Override
	public void visit(IFunction function) {
		add(function, programContext);
	}

	@Override
	public void visit(FormulaTreeNode formulaTreeNode) {
		add(formulaTreeNode, programContext);
	}

	public void add(ICodeTreeNodeContainer codeTreeNodeContainer, IProgramContext context) {
		add(codeTreeNodeContainer.getCodeTreeNode(), context);
	}

	public void add(CodeTreeNode codeTreeNode, IProgramContext context) {
		for (FormulaTreeNode formulaTreeNode : codeTreeNode.getChildren()) {
			add(formulaTreeNode, context);
		}
	}

	/*
		This only adds the function as part of the whole program, if you want to inspect only the function, use add(ReqTreeNode)!
	 */
	public void add(IFunction function, IProgramContext context) {
		final Set<IVariable> toIgnore = function.getParameterVariables().stream().map(IVariableWithAccessor::getVariable).collect(Collectors.toSet());
		if (function.getFunctionBody() instanceof AssignmentOperator) {
			collectTreeNodes(function.getFunctionBody().getChildren().get(0), false, null, toIgnore, context, false);
			collectTreeNodes(function.getFunctionBody().getChildren().get(1), true, null, toIgnore, context, false);
		} else {
			for (FormulaTreeNode formulaTreeNode : function.getFunctionBody().getChildren()) {
				collectTreeNodes(formulaTreeNode, false, null, toIgnore, context, false);
			}
		}
	}

	public void add(FormulaTreeNode formulaTreeNode, IProgramContext context) {
		collectTreeNodes(formulaTreeNode, false, null, Set.of(), context, false);
	}

	private void addParentAndSiblingsRecursiveUp(IVariableWithAccessor parent, Set<Object> visited) {
		if (parent != null) {
			if (!visited.contains(parent)) {
				visited.add(parent);
				final IVariable variable = parent.getVariable();
				usedVariables.add(variable);
				usedVariablesWithAccessor.add(parent);
				final Set<IVariableWithAccessor> siblings = parent.getMembers();
				if (siblings != null) {
					usedVariablesWithAccessor.addAll(siblings);
					for (IVariableWithAccessor sibling : siblings) {
						final IVariable siblingVariable = sibling.getVariable();
						usedVariables.add(siblingVariable);
						addParentAndSiblingsRecursiveDown(sibling, visited);
					}
				}

				addParentAndSiblingsRecursiveUp(parent.getParent(), visited);
			}
		}
	}

	private void addParentAndSiblingsRecursiveDown(IVariableWithAccessor v, Set<Object> visited) {
		final IVariable variable = v.getVariable();
		if (variable instanceof IMemberContainer) {
			final IMemberContainer memberContainer = (IMemberContainer) variable;
			if (!visited.contains(v)) {
				visited.add(v);
				final Set<IVariableWithAccessor> children = v.getMembers();
				for (IVariableWithAccessor child : children) {
					final IVariable childVariable = child.getVariable();
					usedVariablesWithAccessor.add(child);
					usedVariables.add(childVariable);
					addParentAndSiblingsRecursiveDown(child, visited);
				}
			}
		}
	}

	private void addPointerTargetRecursiveUp(IVariableWithAccessor v) {
		if (v != null) {
			if (includeParentsAndSiblings) {
				addParentAndSiblingsRecursiveUp(v.getParent(), new HashSet<>());
			}
			final IVariable variable = v.getVariable();
			usedVariablesWithAccessor.add(v);
			usedVariables.add(variable);
		}
	}


	private void addMemberContainerRecursiveUp(IMemberContainer m, Set<Object> visited) {
		if (m != null) {
			if (!visited.contains(m)) {
				visited.add(m);
				usedVariables.add(m);
				addMemberContainerRecursiveUp(m.getParent(), visited);
			}
		}
	}

	private void collectTreeNodes(CodeTreeNode node, boolean isOutput, IVariable outputVariable, Set<IVariable> toIgnore, IProgramContext context, boolean controlStructureSeen) {
		codeTreeNodes.add(node);

		if (node instanceof VariableTreeNode) {
			final VariableTreeNode variableTreeNode = (VariableTreeNode) node;
			final IVariableWithAccessor variableWithAccessor = variableTreeNode.getVariableWithAccessor();
			final IVariable variable = variableWithAccessor.getVariable();
			if (outputVariable != null) {
				if (hardDependencyMap.containsKey(outputVariable)) {
					hardDependencyMap.get(outputVariable).add(((VariableTreeNode) node).getVariableWithAccessor().getVariable());
				} else {
					hardDependencyMap.put(outputVariable, new HashSet<>(Set.of(((VariableTreeNode) node).getVariableWithAccessor().getVariable())));
				}
			}
			if (!toIgnore.contains(variable)) {
				variableTreeNodes.add(variableTreeNode);
				if (includeParentsAndSiblings) {
					addMemberContainerRecursiveUp(variable.getParent(), new HashSet<>());
					addParentAndSiblingsRecursiveUp(variableWithAccessor.getParent(), new HashSet<>());
				}

				usedVariables.add(variable);
				usedVariablesWithAccessor.add(variableWithAccessor);
				if (isOutput) {
					outputVariableNodes.add(variableTreeNode);
					outputVariables.add(variable);
					outputVariablesWithAccessor.add(variableWithAccessor);
				} else {
					inputVariables.add(variable);
					inputVariablesWithAccessor.add(variableWithAccessor);
				}
			}
		} else if (node instanceof ISimpleValueTreeNode) {
			constantNodes.add((ISimpleValueTreeNode) node);
			if (node instanceof SimpleBooleanValueTreeNode) {
				final SimpleBooleanValueTreeNode booleanValueTreeNode = (SimpleBooleanValueTreeNode) node;
				booleanConstants.add(booleanValueTreeNode.getValue());
			} else if (node instanceof SimpleFloatingPointValueTreeNode) {
				final SimpleFloatingPointValueTreeNode floatingPointValueTreeNode = (SimpleFloatingPointValueTreeNode) node;
				floatingPointConstants.add(floatingPointValueTreeNode.getValue());
			} else if (node instanceof SimpleIntegerValueTreeNode) {
				final SimpleIntegerValueTreeNode integerValueTreeNode = (SimpleIntegerValueTreeNode) node;
				integerConstants.add(integerValueTreeNode.getValue());
			} else {
				throw new RuntimeException("Unhandled SimpleValueTreeNode type: " + node.getClass().getCanonicalName());
			}
		} else if (node instanceof FunctionCallTreeNode) {
			add(((FunctionCallTreeNode) node).getFunction(context), context);
			final FunctionCallTreeNode functionCallTreeNode = (FunctionCallTreeNode) node;
			for (FormulaTreeNode formulaTreeNode : functionCallTreeNode.getParameters()) {
				add(formulaTreeNode, context);
			}
		} else if (node instanceof OperatorTreeNode) {
			final OperatorTreeNode otn = (OperatorTreeNode) node;


			if (node instanceof ControlStructureOperator && !controlStructureSeen) {
				if (!(node instanceof ConcatenationOperator)) {
					IVariable outVar = null;
					List<VariableTreeNode> varNodes = new ArrayList<>();
					for (FormulaTreeNode n : otn.getChildren()) {
						outVar = collectInputVarTreeNodes(n, varNodes, false);
					}
					inputVarTreeNodesForOutputVar.put(outVar, varNodes);
					controlStructureSeen = true;
				}
			}

			if (otn instanceof OutputOperator) {
				isOutput = true;
			}

			if (otn instanceof AssignmentOperator) {
				collectTreeNodes(otn.getChildren().get(0), isOutput, null, toIgnore, context, controlStructureSeen);
				collectTreeNodes(otn.getChildren().get(1), isOutput, otn.getChildren().get(0).tryToReturnOutputVariable(), toIgnore, context, controlStructureSeen);
			} else {
				for (FormulaTreeNode n : otn.getChildren()) {

					collectTreeNodes(n, isOutput, outputVariable, toIgnore, context, controlStructureSeen);
				}
			}
		}
	}

	public IVariable collectInputVarTreeNodes(CodeTreeNode node, List<VariableTreeNode> inputVarTreeNodes, boolean isInOutput) {
		if (node instanceof VariableTreeNode) {
			if (!(isInOutput)) {
				inputVarTreeNodes.add((VariableTreeNode) node);
			}
			return null;
		} else {
			IVariable outVar = null;
			for (FormulaTreeNode n : node.getChildren()) {
				if (n instanceof OutputOperator) {
					collectInputVarTreeNodes(n, inputVarTreeNodes, true);
					outVar = n.tryToReturnOutputVariable();
				} else {
					IVariable temp = collectInputVarTreeNodes(n, inputVarTreeNodes, isInOutput);
					if (outVar == null) {
						outVar = temp;
					}
				}
			}
			return outVar;
		}
	}

	@Override
	public List<CodeTreeNode> getReqTreeNodes() {
		return codeTreeNodes;
	}

	@Override
	public List<VariableTreeNode> getVariableTreeNodes() {
		return variableTreeNodes;
	}

	@Override
	public List<VariableTreeNode> getOutputVariableNodes() {
		return outputVariableNodes;
	}

	@Override
	public List<VariableTreeNode> getInputVariableNodes() {
		List<VariableTreeNode> nodes = new ArrayList<>(variableTreeNodes);
		nodes.removeAll(outputVariableNodes);
		return nodes;
	}

	@Override
	public Map<IVariable, List<VariableTreeNode>> getInputVarTreeNodesForOutputVar() {
		return inputVarTreeNodesForOutputVar;
	}


	@Override
	public Set<IVariable> getInputVariables() {
		return inputVariables;
	}

	@Override
	public Set<IVariable> getInputVariablesWithoutOutputVariables() {
		Set<IVariable> result = new HashSet<>(inputVariables);
		result.removeAll(outputVariables);
		return result;
	}

	@Override
	public Set<IVariable> getOutputVariables() {
		return outputVariables;
	}

	@Override
	public Set<IVariable> getUsedVariables() {
		return usedVariables;
	}

	@Override
	public Set<IVariableWithAccessor> getInputVariablesWithAccessor() {
		return inputVariablesWithAccessor;
	}

	@Override
	public Set<IVariableWithAccessor> getInputVariablesWithAccessorWithoutOutput() {
		Set<IVariableWithAccessor> inputVariablesWithAccessorWithoutOutput = new HashSet<>(inputVariablesWithAccessor);
		inputVariablesWithAccessorWithoutOutput.removeAll(outputVariablesWithAccessor);
		return inputVariablesWithAccessorWithoutOutput;
	}

	@Override
	public Set<IVariableWithAccessor> getOutputVariablesWithAccessor() {
		return outputVariablesWithAccessor;
	}

	@Override
	public Set<IVariableWithAccessor> getUsedVariablesWithAccessor() {
		return usedVariablesWithAccessor;
	}

	@Override
	public Set<Boolean> getBooleanConstants() {
		return booleanConstants;
	}

	@Override
	public Set<Double> getFloatingPointConstants() {
		return floatingPointConstants;
	}

	@Override
	public Set<Long> getIntegerConstants() {
		return integerConstants;
	}

	@Override
	public Set<ISimpleValueTreeNode> getConstantNodes() {
		return constantNodes;
	}
}
