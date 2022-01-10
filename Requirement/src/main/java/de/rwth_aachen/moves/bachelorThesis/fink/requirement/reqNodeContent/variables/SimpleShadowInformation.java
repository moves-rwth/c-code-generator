package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

public class SimpleShadowInformation implements IShadowInformation {

	private final IShadowParent shadowParent;

	public SimpleShadowInformation(IShadowParent shadowParent) {
		this.shadowParent = shadowParent;
	}

	@Override
	public IShadowParent getShadowParent() {
		return shadowParent;
	}
}
