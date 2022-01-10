package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.Utility;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IFloatingPointRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.NonDeterministicUpdateInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FloatingPointVariable extends Variable implements IFloatingPointVariable {
	private BigDecimal min;
	private BigDecimal max;
	private BigDecimal value;
	private boolean hasValue;

	protected static final Logger logger = LogManager.getLogger(FloatingPointVariable.class);

	public FloatingPointVariable(IMemberContainer parent, DataType datatype, ParameterType parameterType, String name, String internalName, IShadowInformation shadowInformation, BigDecimal min, BigDecimal max, BigDecimal value) {
		super(parent, datatype, parameterType, name, internalName, shadowInformation);
		this.min = min;
		this.max = max;
		this.value = value;
		this.hasValue = true;
	}

	protected FloatingPointVariable(FloatingPointVariable other, IMemberContainer newParent) {
		super(other, newParent);
		this.min = other.getMin();
		this.max = other.getMax();
		this.value = other.getValue();
		this.hasValue = other.hasValue;
	}

	protected FloatingPointVariable(FloatingPointVariable other, String newName, String internalName) {
		super(other, newName, internalName);
		this.min = other.getMin();
		this.max = other.getMax();
		this.value = other.getValue();
		this.hasValue = other.hasValue;
	}

	@Override
	public Variable copy() {
		return new FloatingPointVariable(this, getParent());
	}

	@Override
	public Variable replaceParent(IMemberContainer newParent) {
		return new FloatingPointVariable(this, newParent);
	}

	@Override
	public Variable rename(String name, String internalName) {
		return new FloatingPointVariable(this, name, internalName);
	}

	@Override
	public IVariable addNamePrefix(String prefix) {
		return new FloatingPointVariable(this, this.getName(), prefix + this.getInternalName());
	}

	@Override
	public StringArray getValueAsString() {
		return new StringArray(value.toPlainString());
	}

	@Override
	public NonDeterministicUpdateInformation buildNonDetUpdateCode(boolean useHarness, Map<IVariable, SimpleExpressionConditioner> variableConditioners) {
		String lowerLimit;
		String upperLimit;
		if ((getMin() != null) && (getMax() != null)) {
			lowerLimit = Utility.floatingPointToCString(getDataType(), getMin());
			upperLimit = Utility.floatingPointToCString(getDataType(), getMax());
		} else {
			logger.warn("Variable '{}' does not have lower and upper limit set, this is bad for a signal! Forcing to constant.", getName());
			lowerLimit = Utility.floatingPointToCString(getDataType(), getValue());
			upperLimit = Utility.floatingPointToCString(getDataType(), getValue());
		}
		Set<String> exclusions = new HashSet<>();
		if (variableConditioners.containsKey(this)) {
			final SimpleExpressionConditioner expressionConditioner = variableConditioners.get(this);
			final IRange range = expressionConditioner.getRange();
			if (range instanceof IFloatingPointRange) {
				final IFloatingPointRange floatingPointRange = (IFloatingPointRange) range;
				lowerLimit = Utility.floatingPointToCString(getDataType(), BigDecimal.valueOf(floatingPointRange.getLowerLimitFp()));
				upperLimit = Utility.floatingPointToCString(getDataType(), BigDecimal.valueOf(floatingPointRange.getUpperLimitFp()));
				for (Double d : floatingPointRange.getFpExclusions()) {
					exclusions.add(Utility.floatingPointToCString(getDataType(), BigDecimal.valueOf(d)));
				}
			} else {
				throw new RuntimeException("Unhandled Range type for FloatingPoint variable: " + range.toString());
			}
		}

		return new NonDeterministicUpdateInformation(getInternalName(), lowerLimit, upperLimit, ImmutableSet.copyOf(exclusions), this);
	}

	@Override
	public IVariable replaceDataType(DataType replacementDataType, IProgramContext programContext) {
		if (replacementDataType.isFloatingPoint()) {
			return new FloatingPointVariable(getParent(), replacementDataType, getParameterType(), getName(), getInternalName(), getShadowInformation(), BigDecimal.valueOf(replacementDataType.getLowerLimit()), BigDecimal.valueOf(replacementDataType.getUpperLimit()), getValue());
		} else {
			return new IntegerVariable(getParent(), replacementDataType, getParameterType(), getName(), getInternalName(), getShadowInformation(), replacementDataType.getLowerLimit(), replacementDataType.getUpperLimit(), getValue().longValue());
		}
	}

	@Override
	public StringArray getStorageDeclaration() {
		StringArray result = new StringArray();
		result.add(getDataType().toCTypeName() + " " + getName() + " = " + value + ";");
		return result;
	}

	@Override
	public boolean hasValue() {
		return hasValue;
	}

	@Override
	public BigDecimal getValue() {
		return value;
	}

	@Override
	public void setValue(BigDecimal value) {
		this.value = value;
		this.hasValue = true;
	}

	@Override
	public void setValue(String value) {
		this.value = new BigDecimal(value);
		this.hasValue = true;
	}

	@Override
	public BigDecimal getMin() {
		return min;
	}

	@Override
	public void setMin(BigDecimal min) {
		this.min = min;
	}

	@Override
	public BigDecimal getMax() {
		return max;
	}

	@Override
	public void setMax(BigDecimal max) {
		this.max = max;
	}

	@Override
	public String getDimensions() {
		return "[1 1]";
	}
}
