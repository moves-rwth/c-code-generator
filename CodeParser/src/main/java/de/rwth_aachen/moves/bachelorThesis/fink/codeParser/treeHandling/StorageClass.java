package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

public enum StorageClass {
	UNSPECIFIED,
	TYPEDEF,
	EXTERN,
	STATIC,
	AUTO,
	REGISTER,
	MUTABLE;

	@Override
	public String toString() {
		switch (this) {
			case UNSPECIFIED:
				return "UNSPECIFIED";
			case TYPEDEF:
				return "TYPEDEF";
			case EXTERN:
				return "EXTERN";
			case STATIC:
				return "STATIC";
			case AUTO:
				return "AUTO";
			case REGISTER:
				return "REGISTER";
			case MUTABLE:
				return "MUTABLE";
			default:
				throw new IllegalStateException("The storage class '" + this.name() + " is unhandled!");
		}
	}

	public static StorageClass fromCdtStorageClass(int cdtStorageClass) {
		switch (cdtStorageClass) {
			case IASTDeclSpecifier.sc_unspecified:
				return UNSPECIFIED;
			case IASTDeclSpecifier.sc_typedef:
				return TYPEDEF;
			case IASTDeclSpecifier.sc_extern:
				return EXTERN;
			case IASTDeclSpecifier.sc_static:
				return STATIC;
			case IASTDeclSpecifier.sc_auto:
				return AUTO;
			case IASTDeclSpecifier.sc_register:
				return REGISTER;
			case IASTDeclSpecifier.sc_mutable:
				return MUTABLE;
			default:
				throw new IllegalStateException("Unexpected value: " + cdtStorageClass);
		}
	}
}
