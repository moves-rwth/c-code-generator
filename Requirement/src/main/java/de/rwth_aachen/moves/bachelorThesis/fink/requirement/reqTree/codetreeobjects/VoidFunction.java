package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying.VariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ReturnOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollectionVisitor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class VoidFunction implements Serializable, IFunction {
	private final String name;

	private final List<IVariableWithAccessor> parameterVars;

	private final String specialKeyword;
	private final CodeTreeNode functionBody;

	public VoidFunction(String name, List<IVariableWithAccessor> parameterVars, CodeTreeNode functionBody) {
		this(name, parameterVars, "", functionBody);
	}

	public VoidFunction(VoidFunction function, VariableReplacer variableReplacer) {
		this(function.getName(), variableReplacer.getReplacements(function.getParameterVariables()), function.getSpecialKeyword(), function.getFunctionBody().copyTree(variableReplacer));
	}

	public VoidFunction(String name, List<IVariableWithAccessor> parameterVars, String specialKeyword, CodeTreeNode functionBody) {
		this.name = name;
		this.parameterVars = parameterVars;
		this.specialKeyword = specialKeyword;
		this.functionBody = functionBody;
	}

	@Override
	public void accept(IVariableCollectionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public StringArray toCCodeString(IProgramContext context) {
		// ReturnVar == null means: Return the value of the expression.
		// ReturnVar != null means: This is a void function, writing to variable.
		final String functionReturnType = "void";

		StringArray content = new StringArray();
		StringBuilder tempContent = new StringBuilder();
		tempContent.append(specialKeyword).append(specialKeyword.isEmpty() ? "" : " ").append(functionReturnType).append(" ").append(name).append("(");
		for (int i = 0; i < parameterVars.size(); i++) {
			tempContent.append(parameterVars.get(i).getDataType().toCTypeName()).append(" ").append(parameterVars.get(i).getName()).append(i < parameterVars.size() - 1 ? ", " : "");
		}
		tempContent.append(") {");
		content.add(tempContent.toString());

		if (functionBody != null) {
			content.addIndented(functionBody.toCode(CodeTreeNode.CodeType.EXECUTION, Set.of(), context));
		}
		content.add("}");

		return content;
	}

	public StringArray toCCodeStringProperty(IProgramContext context) {
		// ReturnVar == null means: Return the value of the expression.
		// ReturnVar != null means: This is a void function, writing to variable.
		final String functionReturnType = "int";

		StringArray content = new StringArray();
		StringBuilder tempContent = new StringBuilder();
		tempContent.append(specialKeyword).append(specialKeyword.isEmpty() ? "" : " ").append(functionReturnType).append(" ").append(name).append("Property").append("(");
		for (int i = 0; i < parameterVars.size(); i++) {
			tempContent.append(parameterVars.get(i).getDataType().toCTypeName()).append(" ").append(parameterVars.get(i).getName()).append(i < parameterVars.size() - 1 ? ", " : "");
		}
		tempContent.append(") {");
		content.add(tempContent.toString());

		if (functionBody != null) {
			ReturnOperator returnOperator = new ReturnOperator(ImmutableList.of((FormulaTreeNode) functionBody), false);
			content.addIndented(returnOperator.toCode(CodeTreeNode.CodeType.EXECUTION, Set.of(), context));
		}
		content.add("}");

		return content;
	}

	@Override
	public String toStringFunctionCall(List<FormulaTreeNode> parameters, Set<IVariableWithAccessor> usedVariables, CodeTreeNode.CodeType codeType, IProgramContext context) {
		StringBuilder content = new StringBuilder();

		if (codeType == CodeTreeNode.CodeType.EXECUTION) {
			content.append(name.strip()).append("(");
			for (int i = 0; i < parameters.size(); ++i) {
				final String parameter = parameters.get(i).toCode(CodeTreeNode.CodeType.CONDITION, usedVariables, context).toStringProperty();
				content.append(parameter).append(i < parameterVars.size() - 1 ? ", " : "");
			}
			content.append(");");
		} else {
			content.append(name.strip()).append("Property").append("(");
			for (int i = 0; i < parameters.size(); ++i) {
				final String parameter = parameters.get(i).toCode(CodeTreeNode.CodeType.CONDITION, usedVariables, context).toStringProperty();
				content.append(parameter).append(i < parameterVars.size() - 1 ? ", " : "");
			}
			content.append(")");
		}

		return content.toString();
	}

	@Override
	public StringArray toCCodeFunctionPrototype() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("void " + getName() + "(");

		// Parameters
		for (int parameterIndex = 0; parameterIndex < getParameterVariables().size(); ++parameterIndex) {
			if (parameterIndex > 0) {
				stringBuilder.append(", ");
			}
			IVariableWithAccessor var = getParameterVariables().get(parameterIndex);
			stringBuilder.append(var.getDataType().toCTypeName());
		}

		stringBuilder.append(");");
		return new StringArray(stringBuilder.toString());
	}

	@Override
	public SimpleResultingExpressionConditioner getResultingExpressionConditioner(IProgramContext programContext) throws UnsatisfiableConstraintsException {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DataType getReturnType() {
		return DataType.INSTANCE_NONE;
	}

	@Override
	public List<IVariableWithAccessor> getParameterVariables() {
		return parameterVars;
	}

	public String getSpecialKeyword() {
		return specialKeyword;
	}

	@Override
	public CodeTreeNode getFunctionBody() {
		return functionBody;
	}
}
