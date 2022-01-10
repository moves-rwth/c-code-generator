package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.ISimpleValueTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IVariableCollector extends IVariableCollectionVisitor {
	List<CodeTreeNode> getReqTreeNodes();

	List<VariableTreeNode> getVariableTreeNodes();

	List<VariableTreeNode> getOutputVariableNodes();

	Set<IVariable> getInputVariables();

	Set<IVariable> getInputVariablesWithoutOutputVariables();

	Set<IVariable> getOutputVariables();

	Set<IVariable> getUsedVariables();

	Set<IVariableWithAccessor> getInputVariablesWithAccessor();

	Set<IVariableWithAccessor> getOutputVariablesWithAccessor();

	Set<IVariableWithAccessor> getUsedVariablesWithAccessor();

	Set<Boolean> getBooleanConstants();

	Set<Double> getFloatingPointConstants();

	Set<Long> getIntegerConstants();

	Set<ISimpleValueTreeNode> getConstantNodes();

	List<VariableTreeNode> getInputVariableNodes();

	Map<IVariable, List<VariableTreeNode>> getInputVarTreeNodesForOutputVar();

	Set<IVariableWithAccessor> getInputVariablesWithAccessorWithoutOutput();


}
