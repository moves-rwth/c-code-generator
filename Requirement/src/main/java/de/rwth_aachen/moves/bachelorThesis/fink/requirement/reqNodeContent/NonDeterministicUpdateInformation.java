package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent;

import com.google.common.collect.ImmutableSet;

public class NonDeterministicUpdateInformation {
	public final String name;
	public final String lowerLimit;
	public final String upperLimit;
	public final ImmutableSet<String> exclusions;
	public final Variable variable;

	public NonDeterministicUpdateInformation(String name, String lowerLimit, String upperLimit, ImmutableSet<String> exclusions, Variable variable) {
		this.name = name;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.exclusions = exclusions;
		this.variable = variable;
	}
}
