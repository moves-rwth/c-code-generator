package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import java.util.LinkedHashMap;

public interface IUnionType extends IContainerType {
	LinkedHashMap<String, String> getMembers();
}
