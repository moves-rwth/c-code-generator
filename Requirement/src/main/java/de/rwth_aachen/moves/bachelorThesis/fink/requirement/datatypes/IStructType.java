package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import java.util.LinkedHashMap;

public interface IStructType extends IContainerType {
	LinkedHashMap<String, String> getMembers();
}
