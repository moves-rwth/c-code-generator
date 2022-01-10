package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IPointerAssignment;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ArrayVariable extends Variable implements IArrayVariable, IMemberContainer {
	protected List<Integer> arrayDimensions = new ArrayList<>();

	protected ArrayVariable(IMemberContainer parent, DataType datatype, ParameterType parameterType, String name, String internalName, IShadowInformation shadowInformation, List<Integer> arrayDimensions) {
		super(parent, datatype, parameterType, name, internalName, shadowInformation);
		this.arrayDimensions.addAll(arrayDimensions);

		if (arrayDimensions.stream().allMatch(n -> n < 1)) {
			throw new RuntimeException("Do not use array constructor for non-array! All array dimensions smaller-equals 1!");
		}
	}

	protected ArrayVariable(ArrayVariable other, IMemberContainer newParent) {
		super(other, newParent);
		this.arrayDimensions.addAll(other.getArrayDimensions());
	}

	protected ArrayVariable(ArrayVariable other, String newName, String internalName) {
		super(other, newName, internalName);
		this.arrayDimensions.addAll(other.getArrayDimensions());
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ArrayVariable variable = (ArrayVariable) o;
		return Objects.equals(getInternalName(), variable.getInternalName());
	}

	@Override
	public List<Integer> getArrayDimensions() {
		return arrayDimensions;
	}

	@Override
	public String getDimensions() {
		String dimension = "[ ";
		for (Integer n : arrayDimensions) {
			dimension = dimension.concat(n + " ");
		}
		return dimension + "]";
	}


	@Override
	public IVariableWithAccessor resolveMemberByName(IVariableWithAccessor thisVariableWithAccessor, IPointerAssignment currentPointerAssignment, String memberName, IVariableWithAccessor existingShadowMember, IProgramContext programContext) {
		return super.resolveMemberByName(thisVariableWithAccessor, currentPointerAssignment, memberName, existingShadowMember, programContext);
	}
}
