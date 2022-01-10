package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;


public interface IPointerAssignment {
	boolean hasAssignmentForVariable(IVariable variable);
	IVariableWithAccessor getCurrentTargetVariable(IVariableWithAccessor variable);

	void copyAssignment(IVariable from, IVariable to);

	/**
	 * Updates the pointer assignment table with the given information.
	 * @param pointerVariable The affected pointer.
	 * @param target The variable being pointed to.
	 */
	void setPointerTarget(IVariable pointerVariable, IVariable target);
}
