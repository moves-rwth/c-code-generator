package de.rwth_aachen.moves.bachelorThesis.fink.requirement.range;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;

public interface IEditableRange extends IRange {
	IEditableRange setCanBeZero(boolean canBeZero) throws UnsatisfiableConstraintsException;

	IEditableRange setCanBeNegative(boolean canBeNegative) throws UnsatisfiableConstraintsException;

	IEditableRange setCanBePositive(boolean canBePositive) throws UnsatisfiableConstraintsException;

	IEditableRange addExclusion(long value) throws UnsatisfiableConstraintsException;

	IEditableRange addExclusion(double value) throws UnsatisfiableConstraintsException;

	IEditableRange setLowerLimit(long value) throws UnsatisfiableConstraintsException;

	IEditableRange setLowerLimit(double value) throws UnsatisfiableConstraintsException;

	IEditableRange setUpperLimit(long value) throws UnsatisfiableConstraintsException;

	IEditableRange setUpperLimit(double value) throws UnsatisfiableConstraintsException;
}
