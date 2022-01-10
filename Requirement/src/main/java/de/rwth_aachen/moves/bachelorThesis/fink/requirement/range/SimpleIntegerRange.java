package de.rwth_aachen.moves.bachelorThesis.fink.requirement.range;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.OverflowException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.RangeWrapsAroundException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnderflowException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SimpleIntegerRange implements IIntegerRange, IEditableRange, Serializable {

	protected static final Logger logger = LogManager.getLogger(SimpleIntegerRange.class);

	private final Set<DataType> underlyingTypes;
	private final long typeLowerLimit;
	private final long typeUpperLimit;

	private final long lowerLimit;
	private final long upperLimit;

	private final Set<Long> exclusions = new HashSet<>();

	public SimpleIntegerRange(Set<DataType> allowedDataTypes) {
		this.underlyingTypes = new HashSet<>(allowedDataTypes);
		typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
		typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

		lowerLimit = typeLowerLimit;
		upperLimit = typeUpperLimit;
	}

	public SimpleIntegerRange(Set<DataType> allowedDataTypes, boolean forOutputVar) {
		this.underlyingTypes = new HashSet<>(allowedDataTypes);
		typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
		typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

		if (typeLowerLimit == 0) {
			lowerLimit = typeLowerLimit;
		} else {
			lowerLimit = typeLowerLimit + 1;
		}
		upperLimit = typeUpperLimit - 1;
	}

	public SimpleIntegerRange(Set<DataType> allowedDataTypes, long lowerLimit, long upperLimit, Set<Long> exclusions) throws UnsatisfiableConstraintsException {
		this.underlyingTypes = new HashSet<>(allowedDataTypes);
		typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
		typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

		this.lowerLimit = checkLowerLimit(lowerLimit);
		this.upperLimit = checkUpperLimit(upperLimit);

		while (exclusions.contains(lowerLimit)) {
			++lowerLimit;
		}
		while (exclusions.contains(upperLimit)) {
			--upperLimit;
		}

		this.exclusions.addAll(exclusions);
	}

	public SimpleIntegerRange(IIntegerRange other, long lowerLimit, long upperLimit) throws UnsatisfiableConstraintsException {
		this.underlyingTypes = new HashSet<>(other.getUnderlyingTypes());
		typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
		typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

		long localLowerLimit = checkLowerLimit(lowerLimit);
		long localUpperLimit = checkUpperLimit(upperLimit);
		for (Long l : other.getExclusions()) {
			if (l >= lowerLimit && l <= upperLimit) {
				exclusions.add(l);
			}
		}

		while (exclusions.contains(localLowerLimit)) {
			++localLowerLimit;
		}
		while (exclusions.contains(localUpperLimit)) {
			--localUpperLimit;
		}
		this.lowerLimit = localLowerLimit;
		this.upperLimit = localUpperLimit;
	}

	public SimpleIntegerRange(IIntegerRange other) {
		underlyingTypes = new HashSet<>(other.getUnderlyingTypes());
		typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
		typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

		lowerLimit = other.getLowerLimit();
		upperLimit = other.getUpperLimit();

		exclusions.addAll(other.getExclusions());
	}

	public SimpleIntegerRange(IRange other, IDataTypeContext dataTypeContext) {
		if (other instanceof IBooleanRange) {
			IBooleanRange booleanRange = (IBooleanRange) other;
			underlyingTypes = dataTypeContext.bool();
			typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
			typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

			lowerLimit = (booleanRange.canBeFalse()) ? 0L : 1L;
			upperLimit = (booleanRange.canBeTrue()) ? 1L : 0L;
		} else if (other instanceof IIntegerRange) {
			IIntegerRange integerRange = (IIntegerRange) other;
			underlyingTypes = new HashSet<>(integerRange.getUnderlyingTypes());
			typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
			typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

			lowerLimit = clamp(typeLowerLimit, integerRange.getLowerLimit(), typeUpperLimit);
			upperLimit = clamp(typeLowerLimit, integerRange.getUpperLimit(), typeUpperLimit);
			exclusions.addAll(integerRange.getExclusions());
		} else {
			throw new RuntimeException("Can not initialize integer range from unknown range!");
		}
	}

	private static Set<DataType> combineTypes(Set<DataType> a, Set<DataType> b) {
		Set<DataType> result = new HashSet<>(a);
		result.addAll(b);
		return result;
	}

	public static IIntegerRange div(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Long> exclusions = new HashSet<>();

		if (a instanceof IFloatingPointRange || b instanceof IFloatingPointRange) {
			return SimpleFloatingPointRange.div(a, b);
		}

		final long res1 = a.getLowerLimit() / (b.getLowerLimit() == 0L ? 1L : b.getLowerLimit());
		final long res2 = a.getLowerLimit() / (b.getUpperLimit() == 0L ? -1L : b.getUpperLimit());
		final long res3 = a.getUpperLimit() / (b.getLowerLimit() == 0L ? 1L : b.getLowerLimit());
		final long res4 = a.getUpperLimit() / (b.getUpperLimit() == 0L ? -1L : b.getUpperLimit());

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		long lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		long upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		if (DataTypeContext.hasFloatingPoint(combinedTypes)) {
			// Div by 0.00000...
			if (b.getLowerLimit() <= 0L && b.getUpperLimit() >= 1L) {
				upperLimit = DataType.getMaximumUpperLimit(combinedTypes);
			}

			// Div by 0.00000...
			if (b.getLowerLimit() <= -1L && b.getUpperLimit() >= 0L) {
				lowerLimit = DataType.getMinimumLowerLimit(combinedTypes);
			}
		}

		return new SimpleIntegerRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IIntegerRange mod(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Long> exclusions = new HashSet<>();

		if (a.isOverconstrained() || b.isOverconstrained()) throw new UnsatisfiableConstraintsException("Overconstrained");

		long lowerLimit;
		long upperLimit;

		long maxDist = Math.max(Math.abs(b.getLowerLimit()), Math.abs(b.getUpperLimit()));

		if (a.getLowerLimit() >= 0) {
			lowerLimit = 0;
			upperLimit = Math.min(a.getUpperLimit(), maxDist - 1);
		} else if (a.getUpperLimit() <= 0) {
			lowerLimit = Math.max(a.getLowerLimit(), -maxDist + 1);
			upperLimit = 0;
		} else {
			lowerLimit = Math.max(a.getLowerLimit(), -maxDist + 1);
			upperLimit = Math.max(a.getUpperLimit(), maxDist - 1);
		}

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());

		return new SimpleIntegerRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IIntegerRange times(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Long> exclusions = new HashSet<>();
		if (!a.isValueAllowed(0L) && !b.isValueAllowed(0L)) {
			exclusions.add(0L);
		}

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		final long res1 = a.getLowerLimit() * b.getLowerLimit();
		final long res2 = a.getLowerLimit() * b.getUpperLimit();
		final long res3 = a.getUpperLimit() * b.getLowerLimit();
		final long res4 = a.getUpperLimit() * b.getUpperLimit();

		long lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		long upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		if ((lowerLimit < DataType.getMinimumLowerLimit(combinedTypes)) || (upperLimit > DataType.getMaximumUpperLimit(combinedTypes))) {
			// Un-restrict due to overflow
			lowerLimit = DataType.getMinimumLowerLimit(combinedTypes);
			upperLimit = DataType.getMaximumUpperLimit(combinedTypes);
			exclusions.clear();
		}
		return new SimpleIntegerRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IIntegerRange plus(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Long> exclusions = new HashSet<>();
		if (DataType.isAllUnsigned(a.getUnderlyingTypes()) && DataType.isAllUnsigned(b.getUnderlyingTypes())) {
			if (!a.isValueAllowed(0L) && !b.isValueAllowed(0L)) {
				exclusions.add(0L);
			}
		}

		final long res1 = a.getLowerLimit() + b.getLowerLimit();
		final long res2 = a.getLowerLimit() + b.getUpperLimit();
		final long res3 = a.getUpperLimit() + b.getLowerLimit();
		final long res4 = a.getUpperLimit() + b.getUpperLimit();

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		long lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		long upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		if ((lowerLimit < DataType.getMinimumLowerLimit(combinedTypes)) || (upperLimit > DataType.getMaximumUpperLimit(combinedTypes))) {
			// Un-restrict due to overflow
			lowerLimit = DataType.getMinimumLowerLimit(combinedTypes);
			upperLimit = DataType.getMaximumUpperLimit(combinedTypes);
			exclusions.clear();
		}
		return new SimpleIntegerRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IIntegerRange minus(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Long> exclusions = new HashSet<>();
		final long res1 = a.getLowerLimit() - b.getLowerLimit();
		final long res2 = a.getLowerLimit() - b.getUpperLimit();
		final long res3 = a.getUpperLimit() - b.getLowerLimit();
		final long res4 = a.getUpperLimit() - b.getUpperLimit();

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		long lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		long upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		if ((lowerLimit < DataType.getMinimumLowerLimit(combinedTypes)) || (upperLimit > DataType.getMaximumUpperLimit(combinedTypes))) {
			// Un-restrict due to overflow

			logger.debug("Removing exclusions due to overflow.");
			lowerLimit = DataType.getMinimumLowerLimit(combinedTypes);
			upperLimit = DataType.getMaximumUpperLimit(combinedTypes);
		}
		return new SimpleIntegerRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IIntegerRange unaryMinus(IIntegerRange a) throws UnsatisfiableConstraintsException, RangeWrapsAroundException {
		DataType underlyingType = DataTypeContext.getSingle(a.getUnderlyingTypes());
		Set<Long> exclusions = new HashSet<>();
		if (underlyingType.isSigned()) {
			exclusions = new HashSet<>(a.getExclusions());
		} else {
			for (long exclusion : a.getExclusions()) {
				exclusions.add(DataType.truncateValue(-exclusion, underlyingType));
			}
		}

		// Putting "-" in front of an unsigned datatype
		long rangeWidth = a.getUpperLimit() - a.getLowerLimit() + 1;
		if ( ! underlyingType.isSigned() && a.getLowerLimit() == 0) {
			if (rangeWidth == underlyingType.getUpperLimit() + 1) {
				// Whole range gets negated -> whole range is possible
				// We just need to adjust exclusions
				return new SimpleIntegerRange(a.getUnderlyingTypes(), a.getLowerLimit(), a.getUpperLimit(), exclusions);
			} else if (rangeWidth > 1) {
				// If a "limited range" with an unsigned datatype starts at 0, meaning the range looks like
				// [0; max < UNSIGNED_DATATYPE_MAX]
				// "-Value" will cause every value to wrap around _except for zero_. This means that it will result
				// in a split range.
				throw new RangeWrapsAroundException("Unary minus results in split range, which is not supported");
			}
		}

		long newLowerLimit;
		long newUpperLimit;
		if (underlyingType.isSigned()) {
			// Signed, just get min and max as normal
			newLowerLimit = Math.min(-a.getLowerLimit(), -a.getUpperLimit());
			newUpperLimit = Math.max(-a.getLowerLimit(), -a.getUpperLimit());
		} else {
			// Unsigned. -1 wraps to the highest possible unsigned number, -2 to second highest, ...
			// Therefore, "-upperLimit" results in new lower limit (and vice versa)
			newLowerLimit = DataType.truncateValue(-a.getUpperLimit(), underlyingType);
			newUpperLimit = DataType.truncateValue(-a.getLowerLimit(), underlyingType);
		}
		return new SimpleIntegerRange(a.getUnderlyingTypes(), newLowerLimit, newUpperLimit, exclusions);
	}

	public static IIntegerRange max(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Long> exclusions = new HashSet<>();
		for (Long l : a.getExclusions()) {
			if (!b.isValueAllowed(l)) {
				exclusions.add(l);
			}
		}
		for (Long l : b.getExclusions()) {
			if (!a.isValueAllowed(l)) {
				exclusions.add(l);
			}
		}

		final long res1 = Math.max(a.getLowerLimit(), b.getLowerLimit());
		final long res2 = Math.max(a.getLowerLimit(), b.getUpperLimit());
		final long res3 = Math.max(a.getUpperLimit(), b.getLowerLimit());
		final long res4 = Math.max(a.getUpperLimit(), b.getUpperLimit());

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		long lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		long upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		return new SimpleIntegerRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IIntegerRange min(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Long> exclusions = new HashSet<>();
		for (Long l : a.getExclusions()) {
			if (!b.isValueAllowed(l)) {
				exclusions.add(l);
			}
		}
		for (Long l : b.getExclusions()) {
			if (!a.isValueAllowed(l)) {
				exclusions.add(l);
			}
		}

		final long res1 = Math.min(a.getLowerLimit(), b.getLowerLimit());
		final long res2 = Math.min(a.getLowerLimit(), b.getUpperLimit());
		final long res3 = Math.min(a.getUpperLimit(), b.getLowerLimit());
		final long res4 = Math.min(a.getUpperLimit(), b.getUpperLimit());

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		long lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		long upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		return new SimpleIntegerRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IIntegerRange ternary(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Long> exclusions = new HashSet<>();
		for (Long l : a.getExclusions()) {
			if (b.getExclusions().contains(l)) {
				exclusions.add(l);
			}
		}

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		long lowerLimit = Math.min(a.getLowerLimit(), b.getLowerLimit());
		long upperLimit = Math.max(a.getUpperLimit(), b.getUpperLimit());
		return new SimpleIntegerRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IIntegerRange childAbs(IIntegerRange a) throws UnsatisfiableConstraintsException {
		Set<Long> exclusions = new HashSet<>();
		for (Long l : a.getExclusions()) {
			if (l > 0L) {
				exclusions.add(-l);
				exclusions.add(l);
			} else if (l == 0L) {
				exclusions.add(l);
			}
		}

		long newLowerLimit;
		long newUpperLimit;
		final long lowerLimit = a.getLowerLimit();
		final long upperLimit = a.getUpperLimit();
		// Three cases: under zero, stretches over zero, over zero
		if (upperLimit < 0L) {
			throw new UnsatisfiableConstraintsException("Abs will always return a positive value, this is overconstrained.");
		}

		if (DataTypeContext.getSingle(a.getUnderlyingTypes()).isSigned()) {
			if (lowerLimit > 0L) {
				newLowerLimit = -upperLimit;
				newUpperLimit = -lowerLimit;
			} else {
				newLowerLimit = -upperLimit;
				newUpperLimit = upperLimit;
			}
		} else {
			if (lowerLimit > 0L) {
				newLowerLimit = lowerLimit;
				newUpperLimit = upperLimit;
			} else {
				newLowerLimit = 0L;
				newUpperLimit = upperLimit;
			}
		}

		return new SimpleIntegerRange(a.getUnderlyingTypes(), newLowerLimit, newUpperLimit, exclusions);
	}

	private long checkLowerLimit(long lowerLimit) throws OverflowException {
		if (lowerLimit < typeLowerLimit) {
			logger.debug("Lower limit {} is below type limit {}.", lowerLimit, typeLowerLimit);
			return typeLowerLimit;
		} else if (lowerLimit > typeUpperLimit) {
			logger.debug("Overflow: Lower limit {} is above type limit {}.", lowerLimit, typeUpperLimit);
			throw new OverflowException("Lower limit " + lowerLimit + " is above the types upper limit " + typeUpperLimit + ", this can not be represented!");
		}
		return lowerLimit;
	}

	private long checkUpperLimit(long upperLimit) throws UnderflowException {
		if (upperLimit < typeLowerLimit) {
			logger.debug("Upper limit {} is below type limit {}.", upperLimit, typeLowerLimit);
			if (upperLimit == -125703006L) {
				logger.debug("Hello");
			}
			throw new UnderflowException("Underflow: Upper limit " + upperLimit + " is below the types lower limit " + typeLowerLimit + ", this can not be represented!");
		} else if (upperLimit > typeUpperLimit) {
			logger.debug("Upper limit {} is above type limit {}.", upperLimit, typeUpperLimit);
			return typeUpperLimit;
		}
		return upperLimit;
	}

	private long clamp(long lowerBound, long valueToClamp, long upperBound) {
		return Math.min(upperBound, Math.max(lowerBound, valueToClamp));
	}

	@Override
	public long getUpperLimit() {
		return upperLimit;
	}

	@Override
	public SimpleIntegerRange setUpperLimit(long newUpperLimit) throws UnsatisfiableConstraintsException {
		return new SimpleIntegerRange(this, lowerLimit, newUpperLimit);
	}

	@Override
	public SimpleIntegerRange setUpperLimit(double newUpperLimit) {
		throw new RuntimeException("Can not set double upper limit in Integer range!");
	}

	@Override
	public long getLowerLimit() {
		return lowerLimit;
	}

	@Override
	public SimpleIntegerRange setLowerLimit(long newLowerLimit) throws UnsatisfiableConstraintsException {
		return new SimpleIntegerRange(this, newLowerLimit, upperLimit);
	}

	@Override
	public SimpleIntegerRange setLowerLimit(double newLowerLimit) {
		throw new RuntimeException("Can not set double lower limit in Integer range!");
	}

	@Override
	public SimpleIntegerRange setCanBeZero(boolean canBeZero) throws UnsatisfiableConstraintsException {
		Set<Long> newExclusions = new HashSet<>(exclusions);
		if (newExclusions.contains(0L) && canBeZero) {
			newExclusions.remove(0L);
		} else if (!newExclusions.contains(0L) && !canBeZero) {
			newExclusions.add(0L);
		}
		return new SimpleIntegerRange(underlyingTypes, lowerLimit, upperLimit, newExclusions);
	}

	@Override
	public SimpleIntegerRange setCanBeNegative(boolean canBeNegative) throws UnsatisfiableConstraintsException {
		long newLowerLimit;
		if (canBeNegative) {
			newLowerLimit = (lowerLimit < 0L) ? Math.max(typeLowerLimit, lowerLimit) : typeLowerLimit;
		} else {
			newLowerLimit = Math.max(lowerLimit, 0L);
		}

		return new SimpleIntegerRange(this, newLowerLimit, upperLimit);
	}

	@Override
	public SimpleIntegerRange setCanBePositive(boolean canBePositive) throws UnsatisfiableConstraintsException {
		long newUpperLimit;
		if (canBePositive) {
			newUpperLimit = (upperLimit > 0L) ? Math.min(typeUpperLimit, upperLimit) : typeUpperLimit;
		} else {
			newUpperLimit = Math.min(upperLimit, 0L);
		}

		return new SimpleIntegerRange(this, lowerLimit, newUpperLimit);
	}

	@Override
	public SimpleIntegerRange addExclusion(long value) throws UnsatisfiableConstraintsException {
		Set<Long> newExclusions = new HashSet<>(exclusions);
		newExclusions.add(value);
		return new SimpleIntegerRange(underlyingTypes, lowerLimit, upperLimit, newExclusions);
	}

	@Override
	public SimpleIntegerRange addExclusion(double value) {
		throw new RuntimeException("Can not add double exclusion on Integer range!");
	}

	@Override
	public boolean canBeNegative() {
		return lowerLimit < 0L;
	}

	@Override
	public boolean canBePositive() {
		return upperLimit > 0L;
	}

	@Override
	public boolean canBeZero() {
		return (lowerLimit <= 0L) && (upperLimit >= 0L) && !exclusions.contains(0L);
	}

	@Override
	public boolean isValueAllowed(long value) {
		if (value < lowerLimit) {
			return false;
		} else if (value > upperLimit) {
			return false;
		} else return !exclusions.contains(value);
	}

	@Override
	public IIntegerRange cloneIntegerRange() {
		return new SimpleIntegerRange(this);
	}

	@Override
	public Set<DataType> getUnderlyingTypes() {
		return underlyingTypes;
	}

	@Override
	public IRange or(IRange otherRange) throws UnsatisfiableConstraintsException {
		if (otherRange instanceof SimpleIntegerRange) {
			final SimpleIntegerRange other = (SimpleIntegerRange) otherRange;

			if (!this.underlyingTypes.equals(other.underlyingTypes)) {
				throw new RuntimeException("Underlying types do not match!");
			}

			Set<Long> jointExclusions = new HashSet<>();
			for (Long l : exclusions) {
				if (other.exclusions.contains(l) || l > other.upperLimit || l < other.lowerLimit) {
					jointExclusions.add(l);
				}
			}
			for (Long l : other.exclusions) {
				if (exclusions.contains(l) || l > upperLimit || l < lowerLimit) {
					jointExclusions.add(l);
				}
			}

			return new SimpleIntegerRange(underlyingTypes, Math.min(lowerLimit, other.lowerLimit), Math.max(upperLimit, other.upperLimit), jointExclusions);
		}
		throw new RuntimeException("Unsupported type of Range for or!");
	}

	@Override
	public Set<Long> getExclusions() {
		return exclusions;
	}

	@Override
	public IRange removeRestrictions() {
		return new SimpleIntegerRange(underlyingTypes);
	}

	@Override
	public IRange cloneRange() { return new SimpleIntegerRange(this); }

	@Override
	public boolean isRestricted() {
		if (lowerLimit > typeLowerLimit) {
			return true;
		} else if (upperLimit < typeUpperLimit) {
			return true;
		} else return exclusions.size() > 0;
	}

	@Override
	public boolean isOverconstrained() {
		if (lowerLimit > upperLimit) {
			return true;
		} else return (lowerLimit == upperLimit) && (exclusions.contains(lowerLimit));
	}

	@Override
	public boolean isCompatibleWith(IRange other) {
		if (other instanceof IIntegerRange) {
			IIntegerRange integerRange = (IIntegerRange) other;

			if (lowerLimit < integerRange.getLowerLimit()) {
				return false;
			} else if (upperLimit > integerRange.getUpperLimit()) {
				return false;
			}
			for (Long l : integerRange.getExclusions()) {
				if (isValueAllowed(l)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public SimpleIntegerRange convertToDataTypeWithTruncation(DataType dataType) throws RangeWrapsAroundException {
		assert(dataType.isInteger());
		long newLowerLimit = 0;
		long newUpperLimit = 0;
		{
			if (getUpperLimit() - getLowerLimit() >= dataType.getUpperLimit() - dataType.getLowerLimit()) {
				// Current range is larger than range after conversion -> All values possible
				newLowerLimit = dataType.getLowerLimit();
				newUpperLimit = dataType.getUpperLimit();
			} else if (dataType.truncateValue(getUpperLimit()) < dataType.truncateValue(getLowerLimit())) {
				// Due to conversion, new range "wraps around".
				throw new RangeWrapsAroundException("Type conversion is in need of range wrapping around type bounds, which wont model.");
			} else {
				newLowerLimit = dataType.truncateValue(getLowerLimit());
				newUpperLimit = dataType.truncateValue(getUpperLimit());
			}
		}

		Set<Long> newExclusions = new HashSet<>();
		for (Long exclusion : exclusions) {
			newExclusions.add(dataType.truncateValue(exclusion));
		}

		try {
			return new SimpleIntegerRange(Set.of(dataType), newLowerLimit, newUpperLimit, newExclusions);
		} catch (UnsatisfiableConstraintsException error) {
			throw new RuntimeException("Error when trying to convert the type!");
		}
	}

	@Override
	public RangeType getRangeType() {
		return RangeType.INTEGER;
	}

	@Override
	public IRange restrictToType(DataType dataType) throws UnsatisfiableConstraintsException {
		if (dataType.isBool()) {
			return new SimpleBooleanRange(canBePositive(), canBeZero());
		} else if (!dataType.isFloatingPoint()) {
			Set<DataType> newTypeSet = DataTypeContext.makeSet(dataType);
			return new SimpleIntegerRange(newTypeSet, Math.max(lowerLimit, DataType.getMinimumLowerLimit(newTypeSet)), Math.min(upperLimit, DataType.getMaximumUpperLimit(newTypeSet)), exclusions);
		}
		throw new RuntimeException("Can not restrict to types other than boolean and integer: " + dataType);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SimpleIntegerRange(allowedDataTypes: {");
		{
			boolean isFirst = true;
			for (DataType d : underlyingTypes) {
				if (!isFirst) {
					sb.append(", ");
				}
				sb.append(d.getTypeName());
				isFirst = false;
			}
		}
		sb.append("}, lowerLimit: ").append(lowerLimit);
		sb.append(", upperLimit: ").append(upperLimit);
		sb.append(", exclusions: {");
		{
			boolean isFirst = true;
			for (Long l : exclusions) {
				if (!isFirst) {
					sb.append(", ");
				}
				sb.append(l);
				isFirst = false;
			}
		}
		sb.append("})");

		return sb.toString();
	}
}
