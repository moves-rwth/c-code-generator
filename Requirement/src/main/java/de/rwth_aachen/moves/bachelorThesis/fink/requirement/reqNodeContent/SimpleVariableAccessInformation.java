package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IMemberContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IParentVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IPointerVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

import java.util.*;

public class SimpleVariableAccessInformation implements IVariableAccessInformation {

	private static final long serialVersionUID = 425631932582810776L;
	private final IVariable baseVariable;
	private final Map<IVariable, IParentVariable> parentVariableMap;
	private final String variableAccessor;

	public SimpleVariableAccessInformation(Map<IVariable, IParentVariable> variables, IVariable baseVariable) {
		if (baseVariable == null) {
			throw new RuntimeException("Base variable can not be null!");
		}

		this.baseVariable = baseVariable;
		this.parentVariableMap = ImmutableMap.copyOf(variables);
		this.variableAccessor = buildVariableAccessor();
	}

	/**
	 * Creates a trivial access information for the given variable, using only the direct parent relationships.
	 * @param variable The variable for which to generate the access information.
	 * @return A trivial access information object.
	 */
	public static SimpleVariableAccessInformation makeTrivialVariableAccessInformation(IVariable variable) {
		Map<IVariable, IParentVariable> map = new HashMap<>();
		IVariable currentVariable = variable;
		while (currentVariable.getParent() != null) {
			map.put(currentVariable, currentVariable.getParent());
			currentVariable = currentVariable.getParent();
		}
		return new SimpleVariableAccessInformation(map, variable);
	}

	@Override
	public IVariableAccessInformation accessPointer(IVariable subVariable) {
		if (!(baseVariable instanceof IPointerVariable)) {
			throw new RuntimeException("Can not accessPointer on variable that is not an IPointerVariable, but a " + baseVariable.getClass().getCanonicalName() + "!");
		}

		final IPointerVariable pointerVariable = (IPointerVariable) baseVariable;
		Map<IVariable, IParentVariable> extendedMap = new HashMap<>(parentVariableMap);
		extendedMap.put(subVariable, pointerVariable);

		return new SimpleVariableAccessInformation(extendedMap, subVariable);
	}

	@Override
	public IVariableAccessInformation accessField(IVariable subVariable) {
		if (!(baseVariable instanceof IMemberContainer)) {
			throw new RuntimeException("Can not accessField on variable that is not an IMemberContainer, but a " + baseVariable.getClass().getCanonicalName() + "!");
		}

		final IMemberContainer memberContainer = (IMemberContainer) baseVariable;
		Map<IVariable, IParentVariable> extendedMap = new HashMap<>(parentVariableMap);
		extendedMap.put(subVariable, memberContainer);

		return new SimpleVariableAccessInformation(extendedMap, subVariable);
	}

	@Override
	public String getVariableAccessor() {
		return variableAccessor;
	}

	private String buildVariableAccessor() {
		// First, create a sorted list
		List<IVariable> orderedVariables = new ArrayList<>();
		{
			IVariable variable = baseVariable;
			while (variable != null) {
				orderedVariables.add(variable);
				variable = parentVariableMap.get(variable);
			}
			orderedVariables = Lists.reverse(orderedVariables);
		}

		boolean isNextAlreadyHandledIfBasic = false;
		final int orderedVariablesCount = orderedVariables.size();
		String selfAccessor = null;
		for (int i = 0; i < orderedVariablesCount; ++i) {
			final boolean isLast = (i == (orderedVariablesCount - 1));
			final IVariable variable = orderedVariables.get(i);
			assert (variable != null);
			if (!isLast) {
				final IParentVariable parentVariable = (IParentVariable) variable;
				if (parentVariable == null) {
					throw new IllegalStateException("Entry in variable reference chain is not last, but not an IParentVariable: " + variable.getClass().getSimpleName());
				}
				selfAccessor = parentVariable.getMemberAccessor(selfAccessor, orderedVariables.get(i + 1));
				isNextAlreadyHandledIfBasic = true;
			} else {
				if (!isNextAlreadyHandledIfBasic) {
					selfAccessor = variable.getName();
				}
			}
		}

		assert (selfAccessor != null);
		return selfAccessor;
	}

	@Override
	public IParentVariable getParent(Variable variable) {
		return parentVariableMap.get(variable);
	}

	@Override
	public Map<IVariable, IParentVariable> getParentVariableMap() {
		return ImmutableMap.copyOf(parentVariableMap);
	}

	@Override
	public IVariable getBaseVariable() {
		return baseVariable;
	}

	@Override
	public boolean isTrivial() {
		return parentVariableMap.size() == 0;
	}

	@Override
	public IVariableAccessInformation replaceVariables(Map<IVariable, IVariable> variableReplacementMap) {
		Map<IVariable, IParentVariable> newParents = new HashMap<>();

		for (IVariable variable: parentVariableMap.keySet()) {
			final IParentVariable parentVariable = parentVariableMap.get(variable);

			final IVariable newVariable = variableReplacementMap.getOrDefault(variable, variable);
			final IVariable newParentVariable = variableReplacementMap.getOrDefault(parentVariable, parentVariable);
			if (!(newParentVariable instanceof IParentVariable)) {
				throw new RuntimeException("Replacement of IParentVariable '" + parentVariable.getInternalName() + "' is NOT an IParentVariable!");
			}
			newParents.put(newVariable, (IParentVariable) newParentVariable);
		}

		return new SimpleVariableAccessInformation(newParents, variableReplacementMap.getOrDefault(baseVariable, baseVariable));
	}

	@Override
	public IVariableAccessInformation getParent() {
		final IParentVariable parentVariable = parentVariableMap.get(baseVariable);
		if (parentVariable == null) {
			return null;
		}

		final Map<IVariable, IParentVariable> newParents = new HashMap<>(parentVariableMap);
		newParents.remove(baseVariable);
		return new SimpleVariableAccessInformation(newParents, parentVariable);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SimpleVariableAccessInformation otherAccessInformation = (SimpleVariableAccessInformation) o;
		return Objects.equals(baseVariable, otherAccessInformation.baseVariable) && Objects.equals(parentVariableMap, otherAccessInformation.parentVariableMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash(baseVariable, parentVariableMap);
	}

	@Override
	public int compareTo(IVariableAccessInformation o) {
		return variableAccessor.compareTo(o.getVariableAccessor());
	}
}
