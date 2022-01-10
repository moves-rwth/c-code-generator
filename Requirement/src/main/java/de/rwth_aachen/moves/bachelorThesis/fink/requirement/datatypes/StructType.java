package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.comparators.StringBetterNaturalOrderComparator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class StructType extends DataType implements IStructType {

	private final String structName;
	private final LinkedHashMap<String, String> members;

	public StructType(String structName, LinkedHashMap<String, String> members) {
		super(structName, members);
		this.structName = structName;
		this.members = members;
	}

	@Override
	public boolean requiresTypeDef() {
		return true;
	}

	@Override
	public boolean requiresForwardDeclaration() {
		return true;
	}

	@Override
	public LinkedHashMap<String, String> getMembers() {
		return members;
	}


	@Override
	public StringArray getTypeDef(IDataTypeContext dataTypeContext) {
		StringArray result = new StringArray();
		result.add("struct " + structName + " {");
		final Set<String> keys = members.keySet();
		List<String> sortedKeys = new ArrayList<>(keys);
		sortedKeys.sort(new StringBetterNaturalOrderComparator());
		for (String memberName : sortedKeys) {
			final DataType member = dataTypeContext.byName(members.get(memberName));
			result.addIndented(member.toCDeclarationName() + " " + memberName + member.afterInstanceNameForInitialization() + ";");
		}
		result.add("};");
		return result;
	}

	@Override
	public StringArray getForwardDeclaration() {
		StringArray result = new StringArray();
		result.add("struct " + structName + ";");
		return result;
	}

	@Override
	public String toCDeclarationName() {
		return "struct " + getTypeName();
	}
}
