package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionTarget;

import java.util.List;
import java.util.Set;

public interface IFunction extends IVariableCollectionTarget {
	DataType getReturnType();

	String toStringFunctionCall(List<FormulaTreeNode> parameterVariables, Set<IVariableWithAccessor> usedVariables, CodeTreeNode.CodeType codeType, IProgramContext context);

	SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException;

	StringArray toCCodeString(IProgramContext context);

	CodeTreeNode getFunctionBody();

	StringArray toCCodeFunctionPrototype();

	List<IVariableWithAccessor> getParameterVariables();

	String getName();
}
