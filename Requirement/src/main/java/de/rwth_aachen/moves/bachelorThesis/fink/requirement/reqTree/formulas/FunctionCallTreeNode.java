package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.AddressOfOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ArrayAccessOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.DereferenceOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.VoidPointerCastOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.NoOpVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FunctionCallTreeNode extends FormulaTreeNode {
	private final String functionNameInternal;
	private final List<FormulaTreeNode> parameters;

	public FunctionCallTreeNode(String function, List<FormulaTreeNode> parameters, boolean isInOutput) {
		super(isInOutput);
		this.functionNameInternal = function;
		this.parameters = parameters;
	}

	public FunctionCallTreeNode(FunctionCallTreeNode node, IVariableReplacer variableReplacer) {
		super(node);
		this.functionNameInternal = node.functionNameInternal;
		List<FormulaTreeNode> newParameters = new ArrayList<>();
		for (FormulaTreeNode n : node.getParameters()) {
			VariableReplacer.PreVarOperator operator = variableReplacer.getPreVarOperator();
			if (operator != null) {
				if (n instanceof VariableTreeNode) {
					if (variableReplacer.containsVariable(((VariableTreeNode) n).getVariableWithAccessor())) {
						OperatorTreeNode extraOperator = null;
						FormulaTreeNode castOperator = null;
						if (variableReplacer.castTypes()) {
							castOperator = new VoidPointerCastOperator(ImmutableList.of((FormulaTreeNode) n.copyTree(variableReplacer)), node.isInOutput(), ((VariableTreeNode) n).getVariableWithAccessor().getDataType());
						} else {
							castOperator = (FormulaTreeNode) n.copyTree(variableReplacer);
						}
						switch (operator) {
							case ADDRESS_OF:
								extraOperator = new AddressOfOperator(ImmutableList.of(castOperator), node.isInOutput());
								break;
							case ARRAY_ACCESS:
								extraOperator = new ArrayAccessOperator(ImmutableList.of(castOperator), node.isInOutput());
								break;
							case DEREFERENCE:
								extraOperator = new DereferenceOperator(ImmutableList.of(castOperator), node.isInOutput());
								break;
						}
						newParameters.add(extraOperator);
					} else {
						newParameters.add((FormulaTreeNode) n.copyTree(variableReplacer));
					}
				} else {
					newParameters.add((FormulaTreeNode) n.copyTree(variableReplacer));
				}
			} else {
				newParameters.add((FormulaTreeNode) n.copyTree(variableReplacer));
			}
		}
		this.parameters = ImmutableList.copyOf(newParameters);
	}

	@Override
	public StringArray toCode(CodeType codeType, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		return new StringArray(context.getCurrentlyDefinedFunctionByString(functionNameInternal).toStringFunctionCall(parameters, usedVariables, codeType, context));
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public FormulaTreeNode replaceChildren(ImmutableList<FormulaTreeNode> children) {
		return new FunctionCallTreeNode(this, new NoOpVariableReplacer());
	}

	@Override
	public StringArray toLastICode(Variable variable, Set<IVariableWithAccessor> usedVariables, IProgramContext context) {
		variable.setLastIHasToBeArray(true);
		StringArray str = new StringArray("last_i_" + variable.getName().trim() + "[" + variable.toString() + "]");
		return str;
	}

	@Override
	public void getLastVariablesAndDepths(Set<IVariable> allVariables, Map<IVariable, Long> lastIVariables, IProgramContext context) {
	}

	@Override
	public ImmutableList<FormulaTreeNode> getChildren() {
		return ImmutableList.of();
	}

	public String getFunctionName() {
		return functionNameInternal;
	}

	public IFunction getFunction(IProgramContext context) {
		return context.getCurrentlyDefinedFunctionByString(functionNameInternal);
	}

	public List<FormulaTreeNode> getParameters() {
		return parameters;
	}

	@Override
	public CodeTreeNode copyTree(IVariableReplacer variableReplacer) {
		return new FunctionCallTreeNode(this, variableReplacer);
	}

	@Override
	public DataType getDynamicReturnType(IProgramContext programContext) {
		return programContext.getCurrentlyDefinedFunctionByString(functionNameInternal).getReturnType();
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		return programContext.getCurrentlyDefinedFunctionByString(functionNameInternal).getResultingExpressionConditioner(programContext);
	}
}
