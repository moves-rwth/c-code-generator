package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.NonDeterministicUpdateInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;

import java.util.*;

public class FloatingPointArrayVariable extends ArrayVariable {
	private final List<FloatingPointVariable> arrayValues;
	private boolean hasValues;

	public FloatingPointArrayVariable(IMemberContainer parent, DataType datatype, ParameterType parameterType, String name, String internalName, IShadowInformation shadowInformation, List<Integer> arrayDimensions, List<FloatingPointVariable> members) {
		super(parent, datatype, parameterType, name, internalName, shadowInformation, arrayDimensions);
		this.arrayValues = new ArrayList<>();
		copyAndReparentMembers(members);
	}

	protected FloatingPointArrayVariable(FloatingPointArrayVariable other, IMemberContainer newParent) {
		super(other, newParent);
		this.arrayValues = new ArrayList<>();
		copyAndReparentMembers(other.getArrayValues());
		this.hasValues = other.hasValues;
	}

	protected FloatingPointArrayVariable(FloatingPointArrayVariable other, String newName, String internalName) {
		super(other, newName, internalName);
		this.arrayValues = new ArrayList<>();
		copyAndReparentMembers(other.getArrayValues());
		this.hasValues = other.hasValues;
	}

	protected FloatingPointArrayVariable(FloatingPointArrayVariable other, String prefix) {
		super(other, other.getName(), prefix + other.getInternalName());
		this.arrayValues = new ArrayList<>();
		copyAndReparentMembersAndAddPrefix(other.arrayValues, prefix);
	}

	private void copyAndReparentMembers(List<FloatingPointVariable> input) {
		for (FloatingPointVariable member : input) {
			final FloatingPointVariable newMemberVariable = (FloatingPointVariable) member.replaceParent(this);
			this.arrayValues.add(newMemberVariable);
		}
	}

	private void copyAndReparentMembersAndAddPrefix(List<FloatingPointVariable> input, String prefix) {
		for (FloatingPointVariable member : input) {
			final FloatingPointVariable newMemberVariable = (FloatingPointVariable) member.replaceParent(this).addNamePrefix(prefix);
			;
			this.arrayValues.add(newMemberVariable);
		}
	}

	@Override
	public StringArray getArrayValueAsString(int index) {
		return arrayValues.get(index).getValueAsString();
	}

	@Override
	public int getArraySize() {
		return arrayValues.size();
	}

	@Override
	public Variable copy() {
		return new FloatingPointArrayVariable(this, getParent());
	}

	@Override
	public Variable replaceParent(IMemberContainer newParent) {
		return new FloatingPointArrayVariable(this, newParent);
	}

	@Override
	public Variable rename(String name, String internalName) {
		return new FloatingPointArrayVariable(this, name, internalName);
	}

	@Override
	public Variable addNamePrefix(String prefix) {
		return new FloatingPointArrayVariable(this, prefix);
	}

	@Override
	public boolean hasValue() {
		return hasValues;
	}

	@Override
	public StringArray getValueAsString() {
		StringArray result = new StringArray();
		result.add("{");
		result.addIndented(initializationRecursive(new ArrayList<>(), arrayDimensions));
		result.add("}");
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FloatingPointArrayVariable variable = (FloatingPointArrayVariable) o;
		return Objects.equals(getInternalName(), variable.getInternalName());
	}

	@Override
	public NonDeterministicUpdateInformation buildNonDetUpdateCode(boolean useHarness, Map<IVariable, SimpleExpressionConditioner> conditionerMap) {
		return null;
	}

	@Override
	public StringArray getStorageDeclaration() {
		StringArray result = new StringArray();
		String firstLine = getDataType().toCTypeName() + " " + getName();
		for (Integer i : arrayDimensions) {
			firstLine = firstLine.concat("[" + i + "]");
		}
		firstLine += " = {";
		result.add(firstLine);
		result.addIndented(initializationRecursive(new ArrayList<>(), arrayDimensions));
		result.add("};");
		return result;
	}

	private String initializationRecursive(List<Integer> done, List<Integer> remaining) {
		String temp = "";
		if (remaining.size() == 1) {
			for (int i = 0; i < remaining.get(0); i++) {
				boolean isLast = i >= (remaining.get(0) - 1);
				List<Integer> position = new ArrayList<>(done);
				position.add(i);
				temp = temp.concat(arrayValues.get(calculateIndexPosition(position, arrayDimensions)).getValue() + (isLast ? "" : ", "));
			}
			return temp;
		} else {
			for (int i = 0; i < remaining.get(0); i++) {
				boolean isLast = i >= (remaining.get(0) - 1);
				List<Integer> position = new ArrayList<>(done);
				position.add(i);
				List<Integer> remainingTemp = new ArrayList<>(remaining);
				remainingTemp.remove(0);
				temp = temp.concat("{" + initializationRecursive(position, remainingTemp) + (isLast ? "}" : "}, "));
			}
			return temp;
		}
	}

	@Override
	public void setValue(String value) {

	}

	public List<FloatingPointVariable> getArrayValues() {
		return arrayValues;
	}

	@Override
	public String getMemberAccessor(String selfAccessor, IVariable member) {
		final String thisAccessor = (selfAccessor == null) ? getName() : selfAccessor;
		for (int i = 0; i < arrayValues.size(); ++i) {
			if (arrayValues.get(i) == member) {
				List<Integer> arrayPosition = calculateArrayPosition(i, arrayDimensions);
				String accessor = thisAccessor;
				for (Integer position : arrayPosition) {
					accessor = accessor.concat("[" + position + "]");
				}
				return accessor;
			}
		}
		throw new RuntimeException("Failed to find member variable '" + member.getName() + "' in array '" + getName() + "'!");
	}

	private List<Integer> calculateArrayPosition(int i, List<Integer> arrayDimensions) {
		Map<Integer, Integer> resultInts = new HashMap<>();
		for (int n = 0; n < arrayDimensions.size(); n++) {
			resultInts.put(n, 0);
		}

		int currentValue = i;
		while (currentValue > 0) {
			int highestHigherPrevious = 1;
			int highestHigher = arrayDimensions.get(0);
			int highestHigherIndex = 0;
			while (currentValue > highestHigher - 1) {
				highestHigherPrevious *= arrayDimensions.get(highestHigherIndex);
				highestHigher *= arrayDimensions.get(highestHigherIndex + 1);
				highestHigherIndex += 1;
			}
			while (currentValue >= highestHigherPrevious) {
				currentValue -= highestHigherPrevious;
				resultInts.put(highestHigherIndex, resultInts.get(highestHigherIndex) + 1);
			}
		}

		return new ArrayList<>(resultInts.values());
	}

	private int calculateIndexPosition(List<Integer> arrayPosition, List<Integer> arrayDimensions) {
		int total = 0;
		int current = 1;
		for (int i = 0; i < arrayPosition.size(); i++) {
			total += arrayPosition.get(i) * current;
			current *= arrayDimensions.get(i);
		}
		return total;
	}

	@Override
	public int getMemberCount() {
		return arrayValues.size();
	}

	@Override
	public IVariable getMemberVariableByName(String memberName) {
		int index = Integer.parseInt(memberName);
		return arrayValues.get(index);
	}

	@Override
	public String getMemberTypeByName(String memberName) {
		return getDataType().getTypeName();
	}

	@Override
	public Set<IVariable> getMembers() {
		return new HashSet<>(arrayValues);
	}

	public List<FloatingPointVariable> getArrayEntries() {
		return new ArrayList<>(this.arrayValues);
	}

	@Override
	public IVariable replaceDataType(DataType replacementDataType, IProgramContext programContext) {
		return this;
	}
}
