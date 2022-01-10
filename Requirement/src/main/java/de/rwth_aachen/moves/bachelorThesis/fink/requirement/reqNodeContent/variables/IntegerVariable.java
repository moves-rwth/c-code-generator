package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IBooleanRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.NonDeterministicUpdateInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IntegerVariable extends Variable implements IIntegerVariable {
	private Long min;
	private Long max;
	private Long value;
	private boolean hasValue;


	public IntegerVariable(IMemberContainer parent, DataType datatype, ParameterType parameterType, String name, String internalName, IShadowInformation shadowInformation, Long min, Long max, Long value) {
		super(parent, datatype, parameterType, name, internalName, shadowInformation);
		this.min = min;
		this.max = max;
		this.value = value;
		this.hasValue = true;
	}

	protected IntegerVariable(IntegerVariable other, IMemberContainer newParent) {
		super(other, newParent);
		this.min = other.getMin();
		this.max = other.getMax();
		this.value = other.getValue();
		this.hasValue = other.hasValue;
	}

	protected IntegerVariable(IntegerVariable other, String newName, String internalName) {
		super(other, newName, internalName);
		this.min = other.getMin();
		this.max = other.getMax();
		this.value = other.getValue();
		this.hasValue = other.hasValue;
	}

	protected IntegerVariable(FloatingPointVariable other, IMemberContainer newParent) {
		super(newParent, DataType.INSTANCE_INT32, other.getParameterType(), other.getName(), other.getInternalName(), other.getShadowInformation());
		this.min = other.getMin().longValue();
		this.max = other.getMax().longValue();
		this.value = other.getValue().longValue();
		this.hasValue = other.hasValue();

	}

	@Override
	public Variable copy() {
		return new IntegerVariable(this, getParent());
	}

	@Override
	public Variable replaceParent(IMemberContainer newParent) {
		return new IntegerVariable(this, newParent);
	}

	@Override
	public Variable rename(String name, String internalName) {
		return new IntegerVariable(this, name, internalName);
	}

	@Override
	public IVariable addNamePrefix(String prefix) {
		return new IntegerVariable(this, this.getName(), prefix + this.getInternalName());
	}

	@Override
	public boolean hasValue() {
		return hasValue;
	}

	@Override
	public StringArray getValueAsString() {
		return new StringArray(String.valueOf(value));
	}

	@Override
	public NonDeterministicUpdateInformation buildNonDetUpdateCode(boolean useHarness, Map<IVariable, SimpleExpressionConditioner> variableConditioners) {
		String lowerLimit = Long.toString(getMin());
		String upperLimit = Long.toString(getMax());
		Set<String> exclusions = new HashSet<>();
		if (variableConditioners.containsKey(this)) {
			final SimpleExpressionConditioner expressionConditioner = variableConditioners.get(this);
			final IRange range = expressionConditioner.getRange();
			if (range instanceof IBooleanRange) {
				final IBooleanRange booleanRange = (IBooleanRange) range;
				lowerLimit = (booleanRange.canBeFalse() ? "0" : "1");
				upperLimit = (booleanRange.canBeTrue() ? "1" : "0");
			} else if (range instanceof IIntegerRange) {
				final IIntegerRange integerRange = (IIntegerRange) range;
				lowerLimit = Long.toString(integerRange.getLowerLimit());
				upperLimit = Long.toString(integerRange.getUpperLimit());
				for (Long l : integerRange.getExclusions()) {
					exclusions.add(Long.toString(l));
				}
			} else {
				throw new RuntimeException("Unhandled Range type for Integer variable: " + range.toString());
			}
		}

		return new NonDeterministicUpdateInformation(getName(), lowerLimit, upperLimit, ImmutableSet.copyOf(exclusions), this);
	}

	@Override
	public IVariable replaceDataType(DataType replacementDataType, IProgramContext programContext) {
		return new IntegerVariable(getParent(), replacementDataType, getParameterType(), getName(), getInternalName(), getShadowInformation(), replacementDataType.getLowerLimit(), replacementDataType.getUpperLimit(), getValue());
	}

	@Override
	public StringArray getStorageDeclaration() {
		StringArray result = new StringArray();
		result.add(getDataType().toCTypeName() + " " + getName() + " = " + value + ";");
		return result;
	}

	@Override
	public Long getValue() {
		return value;
	}

	@Override
	public void setValue(Long value) {
		this.value = value;
		this.hasValue = true;
	}

	@Override
	public void setValue(String value) {
		this.value = Long.valueOf(value);
	}

	@Override
	public Long getMin() {
		return min;
	}

	@Override
	public void setMin(Long min) {
		this.min = min;
	}

	@Override
	public Long getMax() {
		return max;
	}

	@Override
	public void setMax(Long max) {
		this.max = max;
	}

	@Override
	public String getDimensions() {
		return "[1 1]";
	}
}
