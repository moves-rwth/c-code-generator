package de.rwth_aachen.moves.bachelorThesis.fink.requirement.range;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.AliasType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;

import java.util.Set;

public interface IRange {
	/**
	 * @return new, cloned IRange object
	 */
	IRange cloneRange();

	/**
	 *
	 * @return basic new IRange object
	 */
	IRange removeRestrictions();

	IRange restrictToType(DataType dataType) throws UnsatisfiableConstraintsException;

	IRange or(IRange arrayIndexRange) throws UnsatisfiableConstraintsException;

	// Getter

	Set<DataType> getUnderlyingTypes();

	RangeType getRangeType();

	boolean isRestricted();

	boolean isOverconstrained();

	/**
	 * Range "this" has to "fit within" "other"
	 */
	boolean isCompatibleWith(IRange other);

	default void assertUnderlyingTypesAreUnambiguous() {
		if (getUnderlyingTypes().size() == 1) {
			return;
		}

		if (getUnderlyingTypes().isEmpty()) {
			throw new RuntimeException("Range has no underlying types at all!");
		}

		DataType type = null;
		for (DataType toCheck : getUnderlyingTypes()) {
			// We don't care about typedefs
			toCheck = (toCheck instanceof AliasType) ? ((AliasType)toCheck).getTrueBaseType() : toCheck;

			if (type == null) {
				type = toCheck;
			}

			if (type != toCheck) {
				throw new RuntimeException("Range has multiple, different datatypes, which are (partially) not typedefs of another!");
			}
		}
	}
}
