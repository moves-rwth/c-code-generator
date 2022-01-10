package de.rwth_aachen.moves.bachelorThesis.fink.requirement.range;

import java.util.Set;

public interface IFloatingPointRange extends IIntegerRange {
	double getUpperLimitFp();

	double getLowerLimitFp();

	boolean isValueAllowed(double value);

	IFloatingPointRange cloneFloatingPointRange();

	Set<Double> getFpExclusions();
}
