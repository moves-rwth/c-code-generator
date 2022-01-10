package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import com.google.common.collect.ImmutableMap;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SimpleMutableVariableContext implements IMutableVariableContext, Serializable {


	private final Map<IVariable, SimpleExpressionConditioner> variableConditioners = new HashMap<>();

	public SimpleMutableVariableContext() {
		//
	}

	public SimpleMutableVariableContext(IVariableContext other) {
		this.variableConditioners.putAll(other.getVariableConditioners());
	}

	public SimpleMutableVariableContext(Map<IVariable, SimpleExpressionConditioner> variableConditioners) {
		this.variableConditioners.putAll(variableConditioners);
	}

	@Override
	public IMutableVariableContext merge(IVariableContext other) {
		Map<IVariable, SimpleExpressionConditioner> mergedVariableConditioners = new HashMap<>();
		mergedVariableConditioners.putAll(this.getVariableConditioners());
		mergedVariableConditioners.putAll(other.getVariableConditioners());

		return new SimpleMutableVariableContext(mergedVariableConditioners);
	}

	@Override
	public void replaceVariable(IVariable toReplace, IVariable replacementVariable, SimpleExpressionConditioner replacementConditioner) {
		variableConditioners.remove(toReplace);
		variableConditioners.put(replacementVariable, replacementConditioner);
	}

	@Override
	public void updateVariableContext(IVariable variable, SimpleExpressionConditioner expressionConditioner) {
		variableConditioners.put(variable, expressionConditioner);
	}

	@Override
	public SimpleExpressionConditioner getVariableConditioner(IVariable variable) {
		return variableConditioners.get(variable);
	}

	@Override
	public ImmutableMap<IVariable, SimpleExpressionConditioner> getVariableConditioners() {
		HashMap<IVariable, SimpleExpressionConditioner> result = new HashMap<>();
		for (IVariable v : variableConditioners.keySet()) {
			result.put(v.copy(), variableConditioners.get(v).clone());
		}

		return ImmutableMap.copyOf(result);
	}

	@Override
	public IMutableVariableContext copy() {
		SimpleMutableVariableContext result = new SimpleMutableVariableContext();
		result.variableConditioners.putAll(getVariableConditioners());

		return result;
	}

}
