package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

public class BooleanType extends DataType implements IBooleanType {
	public BooleanType() {
		super(0, "boolean", "__VERIFIER_nondet_uchar", true, true, true, false, 0, 0D, 1, 1D, 8);
	}

	@Override
	public String toCTypeName() {
		return "unsigned char";
	}

	@Override
	public String toCDeclarationName() {
		return "unsigned char";
	}
}
