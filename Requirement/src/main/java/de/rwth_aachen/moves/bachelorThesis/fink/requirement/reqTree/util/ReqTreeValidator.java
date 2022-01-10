package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ConcatenationOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.EmptyControlStructure;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ItOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.IteOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FunctionCallTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.patterns.InvariantPatternTreeNode;

import java.util.Set;

public class ReqTreeValidator {
	private final static boolean deactivateValidator = false;

	public static void validateTree(CodeTreeNode node, IProgramContext programContext) {
		if (node instanceof InvariantPatternTreeNode) {
			validateTree(((InvariantPatternTreeNode) node).getChild(), programContext);
		} else {
			validateTree((FormulaTreeNode) node, programContext);
		}
	}

	public static void validateTree(FormulaTreeNode node, IProgramContext programContext) {
		if (deactivateValidator) return;
		if (isControlStructure(node)) return;
		else if (! (node instanceof FunctionCallTreeNode)) {
			// Check if ResultingExpressionConditioner fail
			SimpleResultingExpressionConditioner result;
			try {
				result = node.getResultingExpressionConditioner(programContext);
			} catch (UnsatisfiableConstraintsException e) {
				throw new RuntimeException("Node: " + node + " has unsatisfiable constraints.\n" + e);
			}
			// Check constraints [based on SimpleExpressionConditionerBuilder.checkConstraints()]
			Set<DataType> returnTypes = result.getPossibleReturnTypes();
			if (returnTypes.size() == 0) throw new RuntimeException("Return types size invalid");
			IRange range = result.getRange();
			if (range.isOverconstrained()) throw new RuntimeException("Range is over constrained");
			Set<DataType> underlyingTypes = range.getUnderlyingTypes();
			if (!returnTypes.equals(underlyingTypes)) throw new RuntimeException("Return types do not equal underlying types");
		}
		for (FormulaTreeNode child : node.getChildren()) {
			validateTree(child, programContext);
		}
	}

	private static boolean isControlStructure(FormulaTreeNode node) {
		return node instanceof ConcatenationOperator || node instanceof IteOperator ||
				node instanceof ItOperator || node instanceof EmptyControlStructure;
	}
}
