package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SimplePointerAssignment implements IPointerAssignment, Serializable {
	private final Map<String, IVariable> currentPointerAssignment;

	public SimplePointerAssignment() {
		this.currentPointerAssignment = new HashMap<>();
	}

	@Override
	public boolean hasAssignmentForVariable(IVariable variable) {
		return currentPointerAssignment.containsKey(variable.getInternalName());
	}

	@Override
	public IVariableWithAccessor getCurrentTargetVariable(IVariableWithAccessor variable) {
		final IVariable fromVariable = variable.getVariable();
		final String fromVariableInternalName = fromVariable.getInternalName();
		IVariable targetVariable = currentPointerAssignment.getOrDefault(fromVariableInternalName, null);
		if (targetVariable == null) {
			return null;
		}

		return new SimpleVariableWithAccessInformation(targetVariable, variable.getAccessInformation().accessPointer(targetVariable));
	}

	@Override
	public void copyAssignment(IVariable from, IVariable to) {
		if (hasAssignmentForVariable(from)) {
			currentPointerAssignment.put(to.getInternalName(), currentPointerAssignment.get(from.getInternalName()));
		}
	}

	@Override
	public void setPointerTarget(IVariable pointerVariable, IVariable target) {
		currentPointerAssignment.put(pointerVariable.getInternalName(), target);
	}
}
