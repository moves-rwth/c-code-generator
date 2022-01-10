package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.moduling;


/**
 * Necessary information for generating the needed object to generate a ModuleInstance from a ModuleTemplate.
 */
public class ModuleTemplateInformation {
	private final int CONDITION_ARGUMENT_COUNT;
	private final int CODE_ARGUMENT_COUNT;
	private final int NODE_COUNT;
	private final String NAME;

	public ModuleTemplateInformation(int CONDITION_ARGUMENT_COUNT, int CODE_ARGUMENT_COUNT, int NODE_COUNT, String NAME) {
		this.CONDITION_ARGUMENT_COUNT = CONDITION_ARGUMENT_COUNT;
		this.CODE_ARGUMENT_COUNT = CODE_ARGUMENT_COUNT;
		this.NODE_COUNT = NODE_COUNT;
		this.NAME = NAME;
	}

	public int getCONDITION_ARGUMENT_COUNT() {
		return CONDITION_ARGUMENT_COUNT;
	}

	public int getCODE_ARGUMENT_COUNT() {
		return CODE_ARGUMENT_COUNT;
	}

	public String getNAME() {
		return NAME;
	}

	public int getNODE_COUNT() {
		return NODE_COUNT;
	}
}
