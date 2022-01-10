package de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions;

public class DataTypeException extends RuntimeException {
	public DataTypeException(String errorMessage, Throwable err) {
		super(errorMessage, err);
	}

	public DataTypeException(String errorMessage) {
		super(errorMessage);
	}
}
