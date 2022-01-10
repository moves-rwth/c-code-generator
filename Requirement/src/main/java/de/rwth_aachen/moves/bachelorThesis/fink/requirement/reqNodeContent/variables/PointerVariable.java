package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IPointerType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IPointerAssignment;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.NonDeterministicUpdateInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;

import java.util.Map;

public class PointerVariable extends Variable implements IPointerVariable {

	private IVariableWithAccessor initialization = null;

	public PointerVariable(IMemberContainer parent, DataType datatype, ParameterType parameterType, String name, String internalName, IShadowInformation shadowInformation) {
		super(parent, datatype, parameterType, name, internalName, shadowInformation);
		if (!(datatype instanceof IPointerType)) {
			throw new RuntimeException("Can not build PointerVariable from non-pointer type: " + datatype.getClass().getSimpleName());
		}
	}

	protected PointerVariable(PointerVariable other, IMemberContainer newParent) {
		super(other, newParent);
	}

	protected PointerVariable(PointerVariable other, String newName, String internalName) {
		super(other, newName, internalName);
	}

	@Override
	public Variable copy() {
		return new PointerVariable(this, getParent());
	}

	@Override
	public Variable replaceParent(IMemberContainer newParent) {
		return new PointerVariable(this, newParent);
	}

	@Override
	public Variable rename(String name, String internalName) {
		return new PointerVariable(this, name, internalName);
	}

	@Override
	public IVariable addNamePrefix(String prefix) {
		return new PointerVariable(this, this.getName(), prefix + this.getInternalName());
	}

	@Override
	public boolean hasValue() {
		return false;
	}

	@Override
	public StringArray getValueAsString() {
		return null;
	}

	@Override
	public NonDeterministicUpdateInformation buildNonDetUpdateCode(boolean useHarness, Map<IVariable, SimpleExpressionConditioner> conditionerMap) {
		return null;
	}

	@Override
	public StringArray getStorageDeclaration() {
		StringArray result = new StringArray();
		if (initialization == null) {
			result.add(getDataType().toCTypeName() + " " + getName() + ";");
		} else {
			result.add(getDataType().toCTypeName() + " " + getName() + " = &(" + initialization.getVariableAccessor() + ");");
		}
		return result;
	}

	public StringArray getStorageDeclarationVoid() {
		StringArray result = new StringArray();
		if (initialization == null) {
			result.add("void* " + getName() + ";");
		} else {
			result.add("void* " + getName() + " = &(" + initialization.getVariableAccessor() + ");");
		}
		return result;
	}

	@Override
	public String getDimensions() {
		return null;
	}

	@Override
	public void setInitializationValue(IVariableWithAccessor value) {
		initialization = value;
	}

	@Override
	public void setValue(String value) {

	}

	@Override
	public String getPointedToType() {
		final IPointerType pointerType = (IPointerType) getDataType();
		return pointerType.getBaseType().toCTypeName();
	}

	@Override
	public IVariableWithAccessor resolveMemberByName(IVariableWithAccessor thisVariableWithAccessor, IPointerAssignment currentPointerAssignment, String memberName, IVariableWithAccessor existingShadowMember, IProgramContext programContext) {
		if (currentPointerAssignment.hasAssignmentForVariable(this)) {
			return currentPointerAssignment.getCurrentTargetVariable(thisVariableWithAccessor);
		}
		return super.resolveMemberByName(thisVariableWithAccessor, currentPointerAssignment, memberName, existingShadowMember, programContext);
	}

	@Override
	public String getMemberAccessor(String selfAccessor, IVariable member) {
		final String thisAccessor = (selfAccessor == null) ? getName() : selfAccessor;

		// Array variables would not work anymore if dereferenced, so don't
		if (member instanceof IArrayVariable) {
			return thisAccessor;
		}
		if (this.getDataType().isVoid()) {
			return "(*((" + member.getDataType().toCTypeName() + "*)" + thisAccessor + "))";
		} else {
			return "(*(" + thisAccessor + "))";
		}
	}
}
