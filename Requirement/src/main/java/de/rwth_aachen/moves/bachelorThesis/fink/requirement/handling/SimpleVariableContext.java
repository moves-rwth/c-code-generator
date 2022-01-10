package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import com.google.common.collect.ImmutableMap;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SimpleVariableContext implements IVariableContext, Serializable {


	private final Map<IVariable, SimpleExpressionConditioner> variableConditioners;

	public SimpleVariableContext() {
		this.variableConditioners = ImmutableMap.of();
	}

	public SimpleVariableContext(IVariableContext other) {
		this.variableConditioners = ImmutableMap.copyOf(other.getVariableConditioners());
	}

	public SimpleVariableContext(Map<IVariable, SimpleExpressionConditioner> variableConditioners) {
		this.variableConditioners = ImmutableMap.copyOf(variableConditioners);
	}

	@Override
	public IVariableContext merge(IVariableContext other) {
		Map<IVariable, SimpleExpressionConditioner> mergedVariableConditioners = new HashMap<>();
		mergedVariableConditioners.putAll(this.getVariableConditioners());
		mergedVariableConditioners.putAll(other.getVariableConditioners());

		return new SimpleVariableContext(mergedVariableConditioners);
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
	public IVariableContext copy() {
		return new SimpleVariableContext(getVariableConditioners());
	}

}
