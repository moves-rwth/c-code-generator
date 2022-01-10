package de.rwth_aachen.moves.bachelorThesis.fink.requirement.range;

public interface IBooleanRange extends IRange {
	boolean canBeTrue();

	boolean canBeFalse();

	boolean isDefinite();
}
