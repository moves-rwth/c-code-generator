package de.rwth_aachen.moves.bachelorThesis.fink.requirement.range;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.OverflowException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnderflowException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SimpleFloatingPointRange implements IFloatingPointRange, IEditableRange, Serializable {

	protected static final Logger logger = LogManager.getLogger(SimpleFloatingPointRange.class);

	private final Set<DataType> underlyingTypes;
	private final double typeLowerLimit;
	private final double typeUpperLimit;

	private final double lowerLimit;
	private final double upperLimit;

	private final Set<Double> exclusions = new HashSet<>();

	public SimpleFloatingPointRange(Set<DataType> allowedDataTypes) {
		this.underlyingTypes = new HashSet<>(allowedDataTypes);
		checkTypes(underlyingTypes);
		typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
		typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

		lowerLimit = typeLowerLimit;
		upperLimit = typeUpperLimit;
	}

	public SimpleFloatingPointRange(Set<DataType> allowedDataTypes, boolean forOutputVar) {
		this.underlyingTypes = new HashSet<>(allowedDataTypes);
		checkTypes(underlyingTypes);
		typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
		typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

		lowerLimit = typeLowerLimit + 10000;
		upperLimit = typeUpperLimit - 10000;
	}

	public SimpleFloatingPointRange(Set<DataType> allowedDataTypes, double lowerLimit, double upperLimit, Set<Double> exclusions) throws UnsatisfiableConstraintsException {
		this.underlyingTypes = new HashSet<>(allowedDataTypes);
		checkTypes(underlyingTypes);
		typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
		typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

		this.lowerLimit = checkLowerLimit(lowerLimit);
		this.upperLimit = checkUpperLimit(upperLimit);
		this.exclusions.addAll(exclusions);
	}

	public SimpleFloatingPointRange(IFloatingPointRange other, double lowerLimit, double upperLimit) throws UnsatisfiableConstraintsException {
		this.underlyingTypes = new HashSet<>(other.getUnderlyingTypes());
		checkTypes(underlyingTypes);
		typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
		typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

		this.lowerLimit = checkLowerLimit(lowerLimit);
		this.upperLimit = checkUpperLimit(upperLimit);
		for (Double l : other.getFpExclusions()) {
			if (l >= lowerLimit && l <= upperLimit) {
				exclusions.add(l);
			}
		}
	}

	public SimpleFloatingPointRange(IFloatingPointRange other) {
		underlyingTypes = new HashSet<>(other.getUnderlyingTypes());
		checkTypes(underlyingTypes);
		typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
		typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

		lowerLimit = other.getLowerLimitFp();
		upperLimit = other.getUpperLimitFp();
		exclusions.addAll(other.getFpExclusions());
	}

	public SimpleFloatingPointRange(IRange other, IDataTypeContext dataTypeContext) {
		if (other instanceof IBooleanRange) {
			IBooleanRange booleanRange = (IBooleanRange) other;
			underlyingTypes = dataTypeContext.bool();
			typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
			typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

			lowerLimit = (booleanRange.canBeFalse()) ? 0D : 1D;
			upperLimit = (booleanRange.canBeTrue()) ? 1D : 0D;
		} else if (other instanceof IFloatingPointRange) {
			IFloatingPointRange floatingPointRange = (IFloatingPointRange) other;
			underlyingTypes = new HashSet<>(floatingPointRange.getUnderlyingTypes());
			checkTypes(underlyingTypes);
			typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
			typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

			lowerLimit = floatingPointRange.getLowerLimitFp();
			upperLimit = floatingPointRange.getUpperLimitFp();
			exclusions.addAll(floatingPointRange.getFpExclusions());
		} else if (other instanceof IIntegerRange) {
			IIntegerRange integerRange = (IIntegerRange) other;
			underlyingTypes = new HashSet<>(integerRange.getUnderlyingTypes());
			typeLowerLimit = DataType.getMinimumLowerLimit(underlyingTypes);
			typeUpperLimit = DataType.getMaximumUpperLimit(underlyingTypes);

			lowerLimit = integerRange.getLowerLimit();
			upperLimit = integerRange.getUpperLimit();
			for (Long l : integerRange.getExclusions()) {
				exclusions.add((double) l);
			}
		} else {
			throw new RuntimeException("Can not initialize integer range from unknown range!");
		}
	}

	private static void checkTypes(Set<DataType> types) {
		if (!DataTypeContext.hasFloatingPoint(types)) {
			throw new RuntimeException("FP Range used with types that are not FP!");
		}
	}

	private static Set<DataType> combineTypes(Set<DataType> a, Set<DataType> b) {
		Set<DataType> result = new HashSet<>(a);
		result.addAll(b);
		return result;
	}

	public static IFloatingPointRange div(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Double> exclusions = new HashSet<>();
		if (a instanceof IFloatingPointRange) {
			if (!((IFloatingPointRange) a).isValueAllowed(0D)) {
				exclusions.add(0D);
			}
		} else if (!a.isValueAllowed(0L)) {
			exclusions.add(0D);
		}

		double aLowerLimit = a.getLowerLimit();
		double aUpperLimit = a.getUpperLimit();
		if (a instanceof IFloatingPointRange) {
			aLowerLimit = ((IFloatingPointRange) a).getLowerLimitFp();
			aUpperLimit = ((IFloatingPointRange) a).getUpperLimitFp();
		}
		double bLowerLimit = b.getLowerLimit();
		double bUpperLimit = b.getUpperLimit();
		if (b instanceof IFloatingPointRange) {
			bLowerLimit = ((IFloatingPointRange) b).getLowerLimitFp();
			bUpperLimit = ((IFloatingPointRange) b).getUpperLimitFp();
			if (bLowerLimit == 0D) {
				bLowerLimit = Double.MIN_VALUE;
			}
			if (bUpperLimit == 0D) {
				bUpperLimit = -Double.MIN_VALUE;
			}
		} else {
			if (bLowerLimit == 0D) {
				bLowerLimit = 1D;
			}
			if (bUpperLimit == 0D) {
				bUpperLimit = -1D;
			}
		}

		final double res1 = aLowerLimit / (bLowerLimit);
		final double res2 = aLowerLimit / (bUpperLimit);
		final double res3 = aUpperLimit / (bLowerLimit);
		final double res4 = aUpperLimit / (bUpperLimit);

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		double lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		double upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		if (DataTypeContext.hasFloatingPoint(combinedTypes)) {
			// Div by 0.00000...
			if (((IFloatingPointRange) b).getLowerLimitFp() < 1D && ((IFloatingPointRange) b).getUpperLimitFp() > 0D) {
				upperLimit = DataType.getMaximumUpperLimit(combinedTypes);
			}

			// Div by 0.00000...
			if (((IFloatingPointRange) b).getLowerLimitFp() < 0D && ((IFloatingPointRange) b).getUpperLimitFp() > -1D) {
				lowerLimit = DataType.getMinimumLowerLimit(combinedTypes);
			}
		}

		return new SimpleFloatingPointRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IFloatingPointRange times(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Double> exclusions = new HashSet<>();
		final boolean isAFP = a instanceof IFloatingPointRange;
		final boolean isBFP = b instanceof IFloatingPointRange;
		final boolean canABeZero = isAFP ? ((IFloatingPointRange) a).isValueAllowed(0D) : a.isValueAllowed(0L);
		final boolean canBBeZero = isBFP ? ((IFloatingPointRange) b).isValueAllowed(0D) : b.isValueAllowed(0L);
		if (!canABeZero && !canBBeZero) {
			exclusions.add(0D);
		}

		double aLowerLimit = a.getLowerLimit();
		double aUpperLimit = a.getUpperLimit();
		if (isAFP) {
			aLowerLimit = ((IFloatingPointRange) a).getLowerLimitFp();
			aUpperLimit = ((IFloatingPointRange) a).getUpperLimitFp();
		}
		double bLowerLimit = b.getLowerLimit();
		double bUpperLimit = b.getUpperLimit();
		if (isBFP) {
			bLowerLimit = ((IFloatingPointRange) b).getLowerLimitFp();
			bUpperLimit = ((IFloatingPointRange) b).getUpperLimitFp();
		}

		final double res1 = aLowerLimit * bLowerLimit;
		final double res2 = aLowerLimit * bUpperLimit;
		final double res3 = aUpperLimit * bLowerLimit;
		final double res4 = aUpperLimit * bUpperLimit;

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		double lowerLimit = Math.max(Math.min(Math.min(res1, res2), Math.min(res3, res4)), DataType.getMinimumLowerLimit(combinedTypes));
		double upperLimit = Math.min(Math.max(Math.max(res1, res2), Math.max(res3, res4)), DataType.getMaximumUpperLimit(combinedTypes));

		return new SimpleFloatingPointRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IFloatingPointRange plus(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Double> exclusions = new HashSet<>();
		double aLowerLimit = a.getLowerLimit();
		double aUpperLimit = a.getUpperLimit();
		if (a instanceof IFloatingPointRange) {
			aLowerLimit = ((IFloatingPointRange) a).getLowerLimitFp();
			aUpperLimit = ((IFloatingPointRange) a).getUpperLimitFp();
		}
		double bLowerLimit = b.getLowerLimit();
		double bUpperLimit = b.getUpperLimit();
		if (b instanceof IFloatingPointRange) {
			bLowerLimit = ((IFloatingPointRange) b).getLowerLimitFp();
			bUpperLimit = ((IFloatingPointRange) b).getUpperLimitFp();
		}

		final double res1 = aLowerLimit + bLowerLimit;
		final double res2 = aLowerLimit + bUpperLimit;
		final double res3 = aUpperLimit + bLowerLimit;
		final double res4 = aUpperLimit + bUpperLimit;

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		double lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		double upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		return new SimpleFloatingPointRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IFloatingPointRange minus(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Double> exclusions = new HashSet<>();
		double aLowerLimit = a.getLowerLimit();
		double aUpperLimit = a.getUpperLimit();
		if (a instanceof IFloatingPointRange) {
			aLowerLimit = ((IFloatingPointRange) a).getLowerLimitFp();
			aUpperLimit = ((IFloatingPointRange) a).getUpperLimitFp();
		}
		double bLowerLimit = b.getLowerLimit();
		double bUpperLimit = b.getUpperLimit();
		if (b instanceof IFloatingPointRange) {
			bLowerLimit = ((IFloatingPointRange) b).getLowerLimitFp();
			bUpperLimit = ((IFloatingPointRange) b).getUpperLimitFp();
		}

		final double res1 = aLowerLimit - bLowerLimit;
		final double res2 = aLowerLimit - bUpperLimit;
		final double res3 = aUpperLimit - bLowerLimit;
		final double res4 = aUpperLimit - bUpperLimit;

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		double lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		double upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		return new SimpleFloatingPointRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IFloatingPointRange unaryMinus(IFloatingPointRange a) throws UnsatisfiableConstraintsException {
		Set<Double> exclusions = new HashSet<>();
		if (!a.isValueAllowed(0D)) {
			exclusions.add(0D);
		}

		final double res1 = -a.getLowerLimitFp();
		final double res2 = -a.getUpperLimitFp();

		double lowerLimit = Math.min(res1, res2);
		double upperLimit = Math.max(res1, res2);
		return new SimpleFloatingPointRange(a.getUnderlyingTypes(), lowerLimit, upperLimit, exclusions);
	}

	private static Set<Double> exclusionsMinMax(IIntegerRange a, IIntegerRange b) {
		Set<Double> exclusions = new HashSet<>();
		final boolean isAFp = (a instanceof IFloatingPointRange);
		final boolean isBFp = (b instanceof IFloatingPointRange);

		if (isAFp && isBFp) {
			final IFloatingPointRange aFp = (IFloatingPointRange) a;
			final IFloatingPointRange bFp = (IFloatingPointRange) b;
			for (Double d : aFp.getFpExclusions()) {
				if (!bFp.isValueAllowed(d)) {
					exclusions.add(d);
				}
			}
			for (Double d : bFp.getFpExclusions()) {
				if (!aFp.isValueAllowed(d)) {
					exclusions.add(d);
				}
			}
		} else if (isAFp || isBFp) {
			final IFloatingPointRange localA = (IFloatingPointRange) (isAFp ? a : b);
			final IIntegerRange localB = (isAFp ? b : a);
			for (Double d : localA.getFpExclusions()) {
				final double dd = d;
				if (dd == Math.rint(dd)) {
					final long dAsLong = Math.round(dd);
					if (!localB.isValueAllowed(dAsLong)) {
						exclusions.add(d);
					}
				} else {
					exclusions.add(d);
				}
			}
			for (Long l : localB.getExclusions()) {
				final Double d = Double.valueOf(l);
				if (!localA.isValueAllowed(d)) {
					exclusions.add(d);
				}
			}
		} else {
			throw new RuntimeException("This case should never happen?!");
		}
		return exclusions;
	}

	public static IFloatingPointRange max(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Double> exclusions = exclusionsMinMax(a, b);

		final boolean isAFp = (a instanceof IFloatingPointRange);
		final boolean isBFp = (b instanceof IFloatingPointRange);

		double aLowerLimit = a.getLowerLimit();
		double aUpperLimit = a.getUpperLimit();
		if (isAFp) {
			IFloatingPointRange aFp = (IFloatingPointRange) a;
			aLowerLimit = aFp.getLowerLimitFp();
			aUpperLimit = aFp.getUpperLimitFp();
		}
		double bLowerLimit = b.getLowerLimit();
		double bUpperLimit = b.getUpperLimit();
		if (isBFp) {
			IFloatingPointRange bFp = (IFloatingPointRange) b;
			bLowerLimit = bFp.getLowerLimitFp();
			bUpperLimit = bFp.getUpperLimitFp();
		}

		final double res1 = Math.max(aLowerLimit, bLowerLimit);
		final double res2 = Math.max(aLowerLimit, bUpperLimit);
		final double res3 = Math.max(aUpperLimit, bLowerLimit);
		final double res4 = Math.max(aUpperLimit, bUpperLimit);

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		double lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		double upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		return new SimpleFloatingPointRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IFloatingPointRange min(IIntegerRange a, IIntegerRange b) throws UnsatisfiableConstraintsException {
		Set<Double> exclusions = exclusionsMinMax(a, b);

		final boolean isAFp = (a instanceof IFloatingPointRange);
		final boolean isBFp = (b instanceof IFloatingPointRange);

		double aLowerLimit = a.getLowerLimit();
		double aUpperLimit = a.getUpperLimit();
		if (isAFp) {
			IFloatingPointRange aFp = (IFloatingPointRange) a;
			aLowerLimit = aFp.getLowerLimitFp();
			aUpperLimit = aFp.getUpperLimitFp();
		}
		double bLowerLimit = b.getLowerLimit();
		double bUpperLimit = b.getUpperLimit();
		if (isBFp) {
			IFloatingPointRange bFp = (IFloatingPointRange) b;
			bLowerLimit = bFp.getLowerLimitFp();
			bUpperLimit = bFp.getUpperLimitFp();
		}

		final double res1 = Math.min(aLowerLimit, bLowerLimit);
		final double res2 = Math.min(aLowerLimit, bUpperLimit);
		final double res3 = Math.min(aUpperLimit, bLowerLimit);
		final double res4 = Math.min(aUpperLimit, bUpperLimit);

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		double lowerLimit = Math.min(Math.min(res1, res2), Math.min(res3, res4));
		double upperLimit = Math.max(Math.max(res1, res2), Math.max(res3, res4));
		return new SimpleFloatingPointRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IFloatingPointRange ternary(IFloatingPointRange a, IFloatingPointRange b) throws UnsatisfiableConstraintsException {
		Set<Double> exclusions = new HashSet<>();
		for (Double l : a.getFpExclusions()) {
			if (b.getFpExclusions().contains(l)) {
				exclusions.add(l);
			}
		}

		final Set<DataType> combinedTypes = combineTypes(a.getUnderlyingTypes(), b.getUnderlyingTypes());
		double lowerLimit = Math.min(a.getLowerLimitFp(), b.getLowerLimitFp());
		double upperLimit = Math.max(a.getUpperLimitFp(), b.getUpperLimitFp());
		return new SimpleFloatingPointRange(combinedTypes, lowerLimit, upperLimit, exclusions);
	}

	public static IFloatingPointRange childAbs(IFloatingPointRange a) throws UnsatisfiableConstraintsException {
		Set<Double> exclusions = new HashSet<>();
		for (Double l : a.getFpExclusions()) {
			if (l > 0D) {
				exclusions.add(-l);
				exclusions.add(l);
			} else if (l == 0D) {
				exclusions.add(l);
			}
		}

		double newLowerLimit;
		double newUpperLimit;
		final double lowerLimit = a.getLowerLimitFp();
		final double upperLimit = a.getUpperLimitFp();
		// Three cases: under zero, stretches over zero, over zero
		if (upperLimit < 0D) {
			throw new UnsatisfiableConstraintsException("Abs will always return a positive value, this is overconstrained.");
		}

		if (lowerLimit > 0D) {
			newLowerLimit = -upperLimit;
			newUpperLimit = -lowerLimit;
		} else {
			newLowerLimit = -upperLimit;
			newUpperLimit = upperLimit;
		}

		return new SimpleFloatingPointRange(a.getUnderlyingTypes(), newLowerLimit, newUpperLimit, exclusions);
	}

	private double checkLowerLimit(double lowerLimit) throws OverflowException {
		if (lowerLimit < typeLowerLimit) {
			logger.debug("Lower limit {} is below type limit {}.", lowerLimit, typeLowerLimit);
			return typeLowerLimit;
		} else if (lowerLimit > typeUpperLimit) {
			logger.debug("Overflow: Lower limit {} is above type limit {}.", lowerLimit, typeUpperLimit);
			throw new OverflowException("Lower limit " + lowerLimit + " is above the types upper limit " + typeUpperLimit + ", this can not be represented!");
		}
		return lowerLimit;
	}

	private double checkUpperLimit(double upperLimit) throws UnderflowException {
		if (upperLimit < typeLowerLimit) {
			logger.debug("Underflow: Upper limit {} is below type limit {}.", upperLimit, typeLowerLimit);
			throw new UnderflowException("Upper limit " + upperLimit + " is below the types lower limit " + typeLowerLimit + ", this can not be represented!");
		} else if (upperLimit > typeUpperLimit) {
			logger.debug("Upper limit {} is above type limit {}.", upperLimit, typeUpperLimit);
			return typeUpperLimit;
		}
		return upperLimit;
	}

	@Override
	public long getUpperLimit() {
		return (long) upperLimit;
	}

	@Override
	public SimpleFloatingPointRange setUpperLimit(double newUpperLimit) throws UnsatisfiableConstraintsException {
		return new SimpleFloatingPointRange(this, lowerLimit, newUpperLimit);
	}

	@Override
	public long getLowerLimit() {
		return (long) lowerLimit;
	}

	@Override
	public SimpleFloatingPointRange setLowerLimit(double newLowerLimit) throws UnsatisfiableConstraintsException {
		return new SimpleFloatingPointRange(this, newLowerLimit, upperLimit);
	}

	@Override
	public double getUpperLimitFp() {
		return upperLimit;
	}

	@Override
	public double getLowerLimitFp() {
		return lowerLimit;
	}

	@Override
	public SimpleIntegerRange setLowerLimit(long newLowerLimit) {
		throw new RuntimeException("Can not set integer lower limit in FloatingPoint range!");
	}

	@Override
	public SimpleIntegerRange setUpperLimit(long newUpperLimit) {
		throw new RuntimeException("Can not set integer upper limit in FloatingPoint range!");
	}

	public SimpleFloatingPointRange setCanBeZero(boolean canBeZero) throws UnsatisfiableConstraintsException {
		Set<Double> newExclusions = new HashSet<>(exclusions);
		if (newExclusions.contains(0D) && canBeZero) {
			newExclusions.remove(0D);
		} else if (!newExclusions.contains(0D) && !canBeZero) {
			newExclusions.add(0D);
		}
		return new SimpleFloatingPointRange(underlyingTypes, lowerLimit, upperLimit, newExclusions);
	}

	public SimpleFloatingPointRange setCanBeNegative(boolean canBeNegative) throws UnsatisfiableConstraintsException {
		double newLowerLimit;
		if (canBeNegative) {
			newLowerLimit = (lowerLimit < 0D) ? Math.max(typeLowerLimit, lowerLimit) : typeLowerLimit;
		} else {
			newLowerLimit = Math.max(lowerLimit, 0D);
		}
		return new SimpleFloatingPointRange(this, newLowerLimit, upperLimit);
	}

	public SimpleFloatingPointRange setCanBePositive(boolean canBePositive) throws UnsatisfiableConstraintsException {
		double newUpperLimit;
		if (canBePositive) {
			newUpperLimit = (upperLimit > 0D) ? Math.min(typeUpperLimit, upperLimit) : typeUpperLimit;
		} else {
			newUpperLimit = Math.min(upperLimit, 0D);
		}
		return new SimpleFloatingPointRange(this, lowerLimit, newUpperLimit);
	}

	@Override
	public boolean canBeNegative() {
		return lowerLimit < 0D;
	}

	@Override
	public boolean canBePositive() {
		return upperLimit > 0D;
	}

	@Override
	public boolean canBeZero() {
		return (lowerLimit <= 0D) && (upperLimit >= 0D) && !exclusions.contains(0D);
	}

	@Override
	public boolean isValueAllowed(double value) {
		if (value < lowerLimit) {
			return false;
		} else if (value > upperLimit) {
			return false;
		} else return !exclusions.contains(value);
	}

	@Override
	public boolean isValueAllowed(long value) {
		if (value < lowerLimit) {
			return false;
		} else if (value > upperLimit) {
			return false;
		} else return !exclusions.contains((double) value);
	}

	@Override
	public SimpleFloatingPointRange addExclusion(long value) {
		throw new RuntimeException("Can not add double exclusion on Integer range!");
	}

	@Override
	public SimpleFloatingPointRange addExclusion(double value) throws UnsatisfiableConstraintsException {
		Set<Double> newExclusions = new HashSet<>(exclusions);
		newExclusions.add(value);
		return new SimpleFloatingPointRange(underlyingTypes, lowerLimit, upperLimit, newExclusions);
	}

	@Override
	public IIntegerRange cloneIntegerRange() {
		return new SimpleFloatingPointRange(this);
	}

	@Override
	public IFloatingPointRange cloneFloatingPointRange() {
		return new SimpleFloatingPointRange(this);
	}

	@Override
	public Set<DataType> getUnderlyingTypes() {
		return underlyingTypes;
	}

	@Override
	public IRange or(IRange otherRange) throws UnsatisfiableConstraintsException {
		if (otherRange instanceof SimpleFloatingPointRange) {
			final SimpleFloatingPointRange other = (SimpleFloatingPointRange) otherRange;

			if (!this.underlyingTypes.equals(other.underlyingTypes)) {
				throw new RuntimeException("Underlying types do not match!");
			}

			Set<Double> jointExclusions = new HashSet<>();
			for (Double l : exclusions) {
				if (other.exclusions.contains(l) || l > other.upperLimit || l < other.lowerLimit) {
					jointExclusions.add(l);
				}
			}
			for (Double l : other.exclusions) {
				if (exclusions.contains(l) || l > upperLimit || l < lowerLimit) {
					jointExclusions.add(l);
				}
			}

			return new SimpleFloatingPointRange(underlyingTypes, Math.min(lowerLimit, other.lowerLimit), Math.max(upperLimit, other.upperLimit), jointExclusions);
		}
		throw new RuntimeException("Unsupported type of Range for or!");
	}

	@Override
	public Set<Long> getExclusions() {
		Set<Long> result = new HashSet<>();
		for (Double d : exclusions) {
			if (d == Math.rint(d)) {
				long l = (long) ((double) d);
				result.add(l);
			}
		}
		return result;
	}

	@Override
	public Set<Double> getFpExclusions() {
		return exclusions;
	}

	@Override
	public IRange removeRestrictions() {
		return new SimpleFloatingPointRange(underlyingTypes);
	}

	@Override
	public IRange cloneRange() { return new SimpleFloatingPointRange(this); }

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
		if (other instanceof IFloatingPointRange) {
			IFloatingPointRange floatingPointRange = (IFloatingPointRange) other;

			if (lowerLimit < floatingPointRange.getLowerLimitFp()) {
				logger.debug("Failing compatibility test - Difference in lower limit was: {}, we: {}, other: {}", floatingPointRange.getLowerLimitFp() - lowerLimit, this, other);
				return false;
			} else if (upperLimit > floatingPointRange.getUpperLimitFp()) {
				logger.debug("Failing compatibility test - Difference in upper limit was: {}, we: {}, other: {}", upperLimit - floatingPointRange.getUpperLimitFp(), this, other);
				return false;
			}
			for (Double d : floatingPointRange.getFpExclusions()) {
				if (isValueAllowed(d)) {
					logger.debug("Failing compatibility test - Allowed value: {}", d);
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public RangeType getRangeType() {
		return RangeType.DOUBLE;
	}

	public IRange removeFloats() throws UnsatisfiableConstraintsException {
		Set<DataType> newTypeSet = DataTypeContext.removeFloatingPoint(getUnderlyingTypes());
		return new SimpleIntegerRange(newTypeSet, Math.max(getLowerLimit(), DataType.getMinimumLowerLimit(newTypeSet)), Math.min(getUpperLimit(), DataType.getMaximumUpperLimit(newTypeSet)), getExclusions());
	}

	@Override
	public IRange restrictToType(DataType dataType) throws UnsatisfiableConstraintsException {
		Set<DataType> newTypeSet = DataTypeContext.makeSet(dataType);
		if (dataType.isBool()) {
			return new SimpleBooleanRange(canBePositive(), canBeZero());
		} else if (!dataType.isFloatingPoint()) {
			return new SimpleIntegerRange(newTypeSet, Math.max(Math.min(getLowerLimit(), DataType.getMaximumUpperLimit(newTypeSet)), DataType.getMinimumLowerLimit(newTypeSet)), Math.min(Math.max(getUpperLimit(), DataType.getMinimumLowerLimit(newTypeSet)), DataType.getMaximumUpperLimit(newTypeSet)), getExclusions());
		}
		return new SimpleFloatingPointRange(newTypeSet, Math.max(getLowerLimitFp(), DataType.getMinimumLowerLimit(newTypeSet)), Math.min(getUpperLimitFp(), DataType.getMaximumUpperLimit(newTypeSet)), exclusions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SimpleFloatingPointRange(allowedDataTypes: {");
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
			for (Double d : exclusions) {
				if (!isFirst) {
					sb.append(", ");
				}
				sb.append(d);
				isFirst = false;
			}
		}
		sb.append("})");

		return sb.toString();
	}
}
