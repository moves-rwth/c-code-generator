package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;

public interface IContainerType {
	StringArray getForwardDeclaration();
	String toCDeclarationName();
}
