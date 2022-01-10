package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;

import java.util.List;

public interface IArrayVariable {
	boolean isArray();

	StringArray getArrayValueAsString(int index);

	int getArraySize();

	List<Integer> getArrayDimensions();
}
