package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IMemberContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

import java.io.Serializable;
import java.util.Set;

/**
 * An instance of this interface contains a variable with information on how to access it.
 * It offers a variety of functions which are only to be used in the right context
 * (e.g. accessField can only be used if the variable contained is a struct or union).
 */
public interface IVariableWithAccessor extends Comparable<IVariableWithAccessor>, Serializable {
	DataType getDataType();
	IVariable getVariable();
	IVariableAccessInformation getAccessInformation();
	boolean hasTrivialAccessor();

	/**
	 * If this is a pointer variable, generates a new variable with access information following the current access information down the pointer.
	 * @param programContext For looking up children and generating shadow variables.
	 * @return A variable with access information based on this variable, following a pointer.
	 */
	IVariableWithAccessor accessPointer(IProgramContext programContext);

	/**
	 * If this is a MemberContainer variable, generates a new variable with access information following the current access information down the field.
	 * @param fieldName Which field to access.
	 * @param programContext For looking up children and generating shadow variables.
	 * @return A variable with access information based on this variable, following the given field.
	 */
	IVariableWithAccessor accessField(String fieldName, IProgramContext programContext);

	/**
	 * If this is a pointer variable, uses the given variable to build a variable with access information following the current access information down the pointer to the given target.
	 * @param targetVariable The variable representing the pointer target.
	 * @return A variable with access information based on this variable, following a pointer.
	 */
	IVariableWithAccessor accessPointer(IVariable targetVariable);

	/**
	 * If this is a MemberContainer variable, uses the given variable to build a variable with access information following the current access information down the field to the given target.
	 * @param targetVariable The variable representing the member variable.
	 * @return A variable with access information based on this variable, following the given field.
	 */
	IVariableWithAccessor accessField(IVariable targetVariable);

	/**
	 * Uses this variables access information to go one level up.
	 * @return A variable with access information representing the parent of this variable with access information.
	 */
	IVariableWithAccessor getParent();

	/**
	 * Renames the underlying variable, only possible iff it has a trivial accessor.
	 * @param name The new variable name.
	 * @param internalName The new internal variable name.
	 * @return A copy of this object, with the variable renamed and its trivial accessor adapted.
	 */
	IVariableWithAccessor rename(String name, String internalName);

	/**
	 * Replaces the parent of the contained variable. A new, trivial accessor info will be generated.
	 * @param newParent The new parent of the contained variable.
	 * @return A copy of this object, with the variable re-parented and a new trivial accessor.
	 */
	IVariableWithAccessor replaceParent(IMemberContainer newParent);

	String getName();

	/**
	 * Returns the name of the last'ed version of this variable.
	 * @param depthCode How many steps back we should go, should be >= 1, can be dynamic code.
	 * @return The accessor name to use for the last'ed versions of this variable.
	 */
	String getNameOfLastedVariable(String depthCode);
	String toString(Set<IVariableWithAccessor> allVariables);

	String getVariableAccessor();

	/**
	 * Returns a set of members iff this variable is an IMemberContainer.
	 * @return A set of members, or null if no members exist.
	 */
	Set<IVariableWithAccessor> getMembers();
}
