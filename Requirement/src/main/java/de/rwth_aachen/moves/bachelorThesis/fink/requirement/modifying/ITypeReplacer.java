package de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

/**
 * Given the current DataType, the replacer may decide to replace it by an arbitrary type.
 */
public interface ITypeReplacer {
	DataType getReplacementType(IVariable variable, IProgramContext programContext);

	/**
	 * Returns true iff this type will be replaced.
	 * Does perform recursive checks.
	 * @param dataType The Datatype to check.
	 * @return True iff getReplacementType() will return a different type.
	 */
	boolean willReplace(DataType dataType, IProgramContext programContext);

	/**
	 * Returns true iff this variable, due to its type or ancestry in a struct/union, will be replaced.
	 * Does perform recursive checks.
	 * @param variable The Variable to check.
	 * @return True iff getReplacementType() will return a different type.
	 */
	boolean willReplace(IVariable variable, IProgramContext programContext);
}
