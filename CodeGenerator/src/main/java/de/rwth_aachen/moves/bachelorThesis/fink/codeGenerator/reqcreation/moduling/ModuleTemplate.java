package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.moduling;

import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;

import java.util.List;
import java.util.Map;

/**
 * Information from a module-json file parsed into an object.
 */
public class ModuleTemplate {
	private final String templateName;
	private final FormulaTreeNode requirement;
	private final int conditionArgumentCount;
	private final int codeArgumentCount;
	private final int nodeCount;
	private final Map<String, FormulaTreeNode> localProperties;
	private final Map<String, FormulaTreeNode> globalProperties;
	private final List<IVariable> variables;
	private final ImmutableSet<IFunction> functions;
	private final Map<IVariable, SimpleExpressionConditioner> expressionConditioners;

	public ModuleTemplate(String templateName, FormulaTreeNode requirement, int conditionArgumentCount, int codeArgumentCount, int nodeCount, Map<String, FormulaTreeNode> localProperties, Map<String, FormulaTreeNode> globalProperties, List<IVariable> variables, ImmutableSet<IFunction> functions, Map<IVariable, SimpleExpressionConditioner> expressionConditioners) {
		this.templateName = templateName;
		this.requirement = requirement;
		this.conditionArgumentCount = conditionArgumentCount;
		this.codeArgumentCount = codeArgumentCount;
		this.nodeCount = nodeCount;
		this.localProperties = localProperties;
		this.globalProperties = globalProperties;
		this.variables = variables;
		this.functions = functions;
		this.expressionConditioners = expressionConditioners;
	}

	public ModuleTemplate() {
		this.templateName = null;
		this.requirement = null;
		this.conditionArgumentCount = 0;
		this.codeArgumentCount = 0;
		this.nodeCount = 0;
		this.localProperties = null;
		this.globalProperties = null;
		this.variables = null;
		this.functions = null;
		this.expressionConditioners = null;
	}

	public String getTemplateName() {
		return templateName;
	}

	public int getConditionArgumentCount() {
		return conditionArgumentCount;
	}

	public int getCodeArgumentCount() {
		return codeArgumentCount;
	}

	public FormulaTreeNode getRequirement() {
		return requirement;
	}

	public List<IVariable> getVariables() {
		return variables;
	}

	public ImmutableSet<IFunction> getFunctions() {
		return functions;
	}

	public Map<String, FormulaTreeNode> getLocalProperties() {
		return localProperties;
	}

	public Map<String, FormulaTreeNode> getGlobalProperties() {
		return globalProperties;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public Map<IVariable, SimpleExpressionConditioner> getExpressionConditioners() {
		return expressionConditioners;
	}
}
