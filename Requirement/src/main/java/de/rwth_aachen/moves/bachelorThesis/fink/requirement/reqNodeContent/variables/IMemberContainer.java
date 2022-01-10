package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import java.util.Set;

/**
 * An instance of this interface is a Variable,
 * which holds more variables, meaning it is either:
 * - a struct
 * - an union
 * - an array
 */
public interface IMemberContainer extends IShadowParent, IParentVariable {
	@Override
	IMemberContainer getParent();
	int getMemberCount();
	IVariable getMemberVariableByName(String memberName);
	String getMemberTypeByName(String memberName);
	Set<IVariable> getMembers();
}
