package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;

public interface IPointerVariable extends IShadowParent, IParentVariable {

	String getPointedToType();

	/**
	 * Sets to what variable we initialize the pointer at declaration.
	 */
	void setInitializationValue(IVariableWithAccessor value);

	@Override
	String getName();

	@Override
	String getInternalName();
}
