package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;

public interface ISimpleValueTreeNode {
	DataType getDynamicReturnType(IProgramContext programContext);
}
