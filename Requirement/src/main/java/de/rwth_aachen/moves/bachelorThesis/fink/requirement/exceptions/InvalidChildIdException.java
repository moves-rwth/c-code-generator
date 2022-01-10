package de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions;

public class InvalidChildIdException extends RuntimeException {
	public InvalidChildIdException(String errorMessage, Throwable err) {
		super(errorMessage, err);
	}

	public InvalidChildIdException(String errorMessage) {
		super(errorMessage);
	}
}
