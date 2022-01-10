package de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions;

/**
 * We do not support "wrap around ranges".
 * E.g. when converting an int32 to an int8 range we wont model: [-128, -70] U [120, 127]
 */
public class RangeWrapsAroundException extends Exception {
	public RangeWrapsAroundException(String message) {
		super(message);
	}
}
