package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

public interface IParentVariable extends IVariable {
	/**
	 * Used for construction access chains through pointers/structs/unions/arrays.
	 * @param selfAccessor The expression that refers to this object, already dereferenced if via pointer. Is null if this is the root.
	 * @param member The member of this container/pointer (name irrelevant for pointer) that should be accessed.
	 * @return An expression that refers to the specified member via the given selfAccessor.
	 */
	String getMemberAccessor(String selfAccessor, IVariable member);
}
