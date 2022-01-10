package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.comparators.StringBetterNaturalOrderComparator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IUnionType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IPointerAssignment;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.NonDeterministicUpdateInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;

import java.util.*;

/**
 * NOTE(Felix): This class is one big hack. It's basically a copy of struct,
 * meaning we dont have any "union logic" in here whatsoever.
 * We still use these things in Union-Wrapping though.
 * Here, only one variable of the union is actually used.
 * The underlying UnionType describes the union layout,
 * The "members" _should_ only consist of one member, which is the one we want to access.
 */
public class Union extends Variable implements IMemberContainer {
	private final LinkedHashMap<String, IVariable> members;

	public Union(IMemberContainer parent, DataType datatype, ParameterType parameterType, String name, String internalName, LinkedHashMap<String, IVariable> members, IShadowInformation shadowInformation) {
		super(parent, datatype, parameterType, name, internalName, shadowInformation);
		this.members = new LinkedHashMap<>();
		copyAndReparentMembers(members);

		if (!(datatype instanceof IUnionType)) {
			throw new RuntimeException("Can not initialize union with data type that is not a struct type!");
		}

		if (this.members.size() != 1) {
			throw new RuntimeException("This Union implementation is super wacky. Having more than one member will break this implementation. Define filler members in the UnionType.");
		}
	}

	protected Union(Union other, IMemberContainer newParent) {
		super(other, newParent);
		this.members = new LinkedHashMap<>();
		copyAndReparentMembers(other.members);
	}

	protected Union(Union other, String newName, String internalName) {
		super(other, newName, internalName);
		this.members = new LinkedHashMap<>();
		copyAndReparentMembers(other.members);
	}

	protected Union(Union other, String prefix) {
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
		return new Union(this, getParent());
	}

	@Override
	public Variable replaceParent(IMemberContainer newParent) {
		return new Union(this, newParent);
	}

	@Override
	public Variable rename(String name, String internalName) {
		return new Union(this, name, internalName);
	}

	@Override
	public IVariable addNamePrefix(String prefix) {
		return new Union(this, prefix);
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
			// Using a custom string for union as it would not assign the correct value with the normal (from struct) implementation
			String resultingName = "." + member.getName() + " = " + member.getValueAsString().toStringProperty();
			result.addIndented(resultingName);
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
		if (!(replacementDataType instanceof IUnionType)) {
			throw new RuntimeException("Can not replaceDataType a Union to type '" + replacementDataType.getClass().getName() + "', which is not an IUnionType!");
		}
		IUnionType unionType = (IUnionType) replacementDataType;
		final LinkedHashMap<String, String> replacementMembers = unionType.getMembers();
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

		return new Union(getParent(), replacementDataType, getParameterType(), getName(), getInternalName(), newMembers, getShadowInformation());
	}

	@Override
	public StringArray getStorageDeclaration() {
		StringArray result = new StringArray();
		result.add("union " + getDataType().toCTypeName() + " " + getName() + " = ");
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
