package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums;

public enum OperatorScope {
	EXECUTION(true), CONDITION(false);

	private final boolean isOutput;

	OperatorScope(boolean isOutput) {
		this.isOutput = isOutput;
	}

	public boolean isOutput() {
		return isOutput;
	}
}
