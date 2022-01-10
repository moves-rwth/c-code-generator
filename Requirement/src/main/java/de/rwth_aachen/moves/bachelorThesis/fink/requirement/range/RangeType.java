package de.rwth_aachen.moves.bachelorThesis.fink.requirement.range;

public enum RangeType {
	BOOLEAN, INTEGER, FLOAT, DOUBLE;

	public static RangeType max(RangeType a, RangeType b) {
		if (a == DOUBLE || b == DOUBLE) {
			return DOUBLE;
		} else if (a == FLOAT || b == FLOAT) {
			return FLOAT;
		} else if (a == INTEGER || b == INTEGER) {
			return INTEGER;
		} else if (a == BOOLEAN || b == BOOLEAN) {
			return BOOLEAN;
		} else {
			throw new RuntimeException("Unhandled RangeType!");
		}
	}
}

