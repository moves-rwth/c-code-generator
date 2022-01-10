package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.exceptions;

public class InvalidCodeException extends RuntimeException {
	public InvalidCodeException(String errorMessage) {
		super(errorMessage);
	}
	public InvalidCodeException(String errorMessage, Throwable err) {
		super(errorMessage, err);
	}
}
