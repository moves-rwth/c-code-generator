package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import java.util.List;

public interface IArrayType extends IContainerType {
	DataType getArrayType();

	int getEntriesCount();

	List<Integer> getDimensions();
}
