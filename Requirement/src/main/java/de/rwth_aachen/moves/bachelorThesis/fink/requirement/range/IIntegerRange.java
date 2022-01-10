package de.rwth_aachen.moves.bachelorThesis.fink.requirement.range;

import java.util.Set;

public interface IIntegerRange extends IRange {

	long getUpperLimit();

	long getLowerLimit();

	boolean canBeNegative();

	boolean canBePositive();

	boolean canBeZero();

	boolean isValueAllowed(long value);

	IIntegerRange cloneIntegerRange();

	Set<Long> getExclusions();
}
