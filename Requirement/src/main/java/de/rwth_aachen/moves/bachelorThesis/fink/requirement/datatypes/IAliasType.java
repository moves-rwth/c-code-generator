package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;

public interface IAliasType {
	boolean requiresTypeDef();
	StringArray getTypeDef(IDataTypeContext dataTypeContext);
	DataType getBaseType();

	// NOTE(Felix): Technically you can
	// typedef typedefs, so this is a helper function
	DataType getTrueBaseType();
}
