package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;

public enum BaseType {
	UNSPECIFIED,
	VOID,
	CHAR,
	INT,
	FLOAT,
	DOUBLE,
	BOOL,
	wchar_t,
	TYPEOF,
	DECLTYPE,
	AUTO,
	char16_t,
	char32_t,
	int128,
	float128,
	decimal32,
	decimal64,
	decimal128,
	decltype_auto;

	@Override
	public String toString() {
		return this.name();
	}

	public String toTypeName() {
		switch (this) {
			case UNSPECIFIED:
				throw new IllegalStateException("UNSPECIFIED does not have a type name!");
			case VOID:
				return "void";
			case CHAR:
				return DataType.INSTANCE_INT8.getTypeName();
			case INT:
				return DataType.INSTANCE_INT32.getTypeName();
			case FLOAT:
				return DataType.INSTANCE_FLOAT.getTypeName();
			case DOUBLE:
				return DataType.INSTANCE_DOUBLE.getTypeName();
			case BOOL:
				throw new IllegalStateException("C does not have bool :/");
			case wchar_t:
			case TYPEOF:
			case DECLTYPE:
			case AUTO:
			case char16_t:
			case char32_t:
			case int128:
			case float128:
			case decimal32:
			case decimal64:
			case decimal128:
			case decltype_auto:
			default:
				throw new IllegalStateException("Unhandled type for typename: " + this.toString());
		}
	}

	public static BaseType fromCdtTypeId(int cdtTypeId) {
		switch (cdtTypeId) {
			case IASTSimpleDeclSpecifier.t_unspecified:
				return UNSPECIFIED;
			case IASTSimpleDeclSpecifier.t_void:
				return VOID;
			case IASTSimpleDeclSpecifier.t_char:
				return CHAR;
			case IASTSimpleDeclSpecifier.t_int:
				return INT;
			case IASTSimpleDeclSpecifier.t_float:
				return FLOAT;
			case IASTSimpleDeclSpecifier.t_double:
				return DOUBLE;
			case IASTSimpleDeclSpecifier.t_bool:
				return BOOL;
			default:
				throw new IllegalStateException("Unexpected Type ID value: " + cdtTypeId);
		}
	}
}
