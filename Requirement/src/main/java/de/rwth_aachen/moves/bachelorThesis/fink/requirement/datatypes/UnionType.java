package de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.comparators.StringBetterNaturalOrderComparator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class UnionType extends DataType implements IUnionType {

	private final String unionName;
	private final LinkedHashMap<String, String> members;

	public UnionType(String unionName, LinkedHashMap<String, String> members) {
		super(unionName, members);
		this.unionName = unionName;
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
		result.add("union " + unionName + " {");
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
		result.add("union " + unionName + ";");
		return result;
	}

	@Override
	public String toCDeclarationName() {
		return "union " + getTypeName();
	}

}
