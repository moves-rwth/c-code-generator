package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.comparators.StringBetterNaturalOrderComparator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IStructType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IPointerAssignment;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.NonDeterministicUpdateInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;

import java.util.*;

public class Struct extends Variable implements IMemberContainer {

	private final LinkedHashMap<String, IVariable> members;

	public Struct(IMemberContainer parent, DataType datatype, ParameterType parameterType, String name, String internalName, LinkedHashMap<String, IVariable> members, IShadowInformation shadowInformation) {
		super(parent, datatype, parameterType, name, internalName, shadowInformation);
		this.members = new LinkedHashMap<>();
		copyAndReparentMembers(members);

		if (!(datatype instanceof IStructType)) {
			throw new RuntimeException("Can not initialize struct with data type that is not a struct type!");
		} else if ((!isShadowVariable() && members.size() != datatype.getMembers().size()) || (isShadowVariable() && members.size() != 0)) {
			throw new RuntimeException("Can not initialize struct with " + members.size() + " members when the Datatype requires " + datatype.getMembers().size() + " members!");
		}
	}

	protected Struct(Struct other, IMemberContainer newParent) {
		super(other, newParent);
		this.members = new LinkedHashMap<>();
		copyAndReparentMembers(other.members);
	}

	protected Struct(Struct other, String newName, String newInternalName) {
		super(other, newName, newInternalName);
		this.members = new LinkedHashMap<>();
		copyAndReparentMembers(other.members);
	}

	protected Struct(Struct other, String prefix) {
		super(other, other.getName(), prefix + other.getInternalName());
		this.members = new LinkedHashMap<>();
		copyAndReparentMembersAndAddPrefix(other.members, prefix);
	}

	private void copyAndReparentMembers(LinkedHashMap<String, IVariable> input) {
		for (String memberName : input.keySet()) {
			final IVariable newMemberVariable = input.get(memberName).replaceParent(this);
			this.members.put(memberName, newMemberVariable);
		}
	}

	private void copyAndReparentMembersAndAddPrefix(LinkedHashMap<String, IVariable> input, String prefix) {
		for (String memberName : input.keySet()) {
			final IVariable newMemberVariable = input.get(memberName).replaceParent(this).addNamePrefix(prefix);
			this.members.put(memberName, newMemberVariable);
		}
	}

	@Override
	public Variable copy() {
		return new Struct(this, getParent());
	}

	@Override
	public Variable replaceParent(IMemberContainer newParent) {
		return new Struct(this, newParent);
	}

	@Override
	public Variable rename(String name, String internalName) {
		return new Struct(this, name, internalName);
	}

	@Override
	public IVariable addNamePrefix(String prefix) {
		return new Struct(this, prefix);
	}

	@Override
	public boolean hasValue() {
		return false;
	}

	@Override
	public StringArray getValueAsString() {
		if (isShadowVariable()) {
			throw new RuntimeException("Can not getValueAsString on shadow variable!");
		}

		StringArray result = new StringArray();
		result.add("{");
		final Set<String> keys = members.keySet();
		List<String> sortedKeys = new ArrayList<>(keys);
		sortedKeys.sort(new StringBetterNaturalOrderComparator());
		for (Iterator<String> it = sortedKeys.iterator(); it.hasNext(); ) {
			final String memberName = it.next();
			final boolean hasNext = it.hasNext();
			final IVariable member = members.get(memberName);
			final StringArray subValue = member.getValueAsString();
			result.addIndented(hasNext ? subValue.addToLastLine(",") : subValue);
		}
		result.add("}");
		return result;
	}

	@Override
	public NonDeterministicUpdateInformation buildNonDetUpdateCode(boolean useHarness, Map<IVariable, SimpleExpressionConditioner> conditionerMap) {
		return null;
	}

	@Override
	public IVariable replaceDataType(DataType replacementDataType, IProgramContext programContext) {
		if (!(replacementDataType instanceof IStructType)) {
			throw new RuntimeException("Can not replaceDataType a Struct to type '" + replacementDataType.getClass().getName() + "', which is not an IStructType!");
		}
		IStructType structType = (IStructType) replacementDataType;
		final LinkedHashMap<String, String> replacementMembers = structType.getMembers();
		final IDataTypeContext dataTypeContext = programContext.getCurrentlyDefinedTypes();

		LinkedHashMap<String, IVariable> newMembers = new LinkedHashMap<>();
		for (String internalMemberName : members.keySet()) {
			final IVariable oldMember = members.get(internalMemberName);
			final String memberName = oldMember.getName();
			final DataType oldType = oldMember.getDataType();
			final DataType newType = dataTypeContext.byName(replacementMembers.get(memberName));
			if (oldType == newType) {
				newMembers.put(internalMemberName, oldMember);
			} else {
				newMembers.put(internalMemberName, oldMember.replaceDataType(newType, programContext));
			}
		}

		return new Struct(getParent(), replacementDataType, getParameterType(), getName(), getInternalName(), newMembers, getShadowInformation());
	}

	@Override
	public StringArray getStorageDeclaration() {
		StringArray result = new StringArray();
		result.add("struct " + getDataType().toCTypeName() + " " + getName() + " = ");
		result.addAllButAppendFirstLine(getValueAsString());
		result.addToLastLine(";");
		return result;
	}

	@Override
	public String getDimensions() {
		return "[1 1]";
	}

	@Override
	public void setValue(String value) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public String getMemberAccessor(String selfAccessor, IVariable member) {
		final String thisAccessor = (selfAccessor == null) ? getName() : selfAccessor;
		return thisAccessor + "." + member.getName();
	}

	@Override
	public int getMemberCount() {
		return members.size();
	}

	@Override
	public IVariable getMemberVariableByName(String memberName) {
		if (isShadowVariable()) {
			throw new RuntimeException("Can not getMemberVariableByName on shadow variable!");
		}
		return members.get(getInternalName() + '_' + memberName);
	}

	@Override
	public String getMemberTypeByName(String memberName) {
		return getDataType().getMembers().get(memberName);
	}

	@Override
	public Set<IVariable> getMembers() {
		return new HashSet<>(members.values());
	}

	public LinkedHashMap<String, IVariable> getNameToMemberMapping() {
		return this.members;
	}

	@Override
	public IVariableWithAccessor resolveMemberByName(IVariableWithAccessor thisVariableWithAccessor, IPointerAssignment currentPointerAssignment, String memberName, IVariableWithAccessor existingShadowMember, IProgramContext programContext) {
		return super.resolveMemberByName(thisVariableWithAccessor, currentPointerAssignment, memberName, existingShadowMember, programContext);
	}
}
