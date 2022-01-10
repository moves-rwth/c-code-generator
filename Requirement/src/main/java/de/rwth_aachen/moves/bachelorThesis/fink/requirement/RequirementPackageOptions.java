package de.rwth_aachen.moves.bachelorThesis.fink.requirement;

public class RequirementPackageOptions {
	public static boolean doDebugComments = true;

	public static void debugComments(boolean doDebugComments) {
		RequirementPackageOptions.doDebugComments = doDebugComments;
	}
}
