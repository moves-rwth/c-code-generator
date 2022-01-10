package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IPointerAssignment;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;

public interface IShadowParent {
	IVariableWithAccessor resolveMemberByName(IVariableWithAccessor thisVariableWithAccessor, IPointerAssignment currentPointerAssignment, String memberName, IVariableWithAccessor existingShadowMember, IProgramContext programContext);
}
