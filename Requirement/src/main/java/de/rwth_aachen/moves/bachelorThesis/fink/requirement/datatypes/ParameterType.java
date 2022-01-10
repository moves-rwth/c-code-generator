package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

public enum ParameterType {
	SIGNAL, // extern value changing during runtime
	CALIBRATION_VALUE, // intern value changing only at startup, not during runtime
	MACRO, // combination of any parameter types (?)
	CONSTANT, // simple constant
	INTERNAL_CONTROL, // counter variable
	INTERNAL_SHADOW, // variable in function, which is not yet determined
}
