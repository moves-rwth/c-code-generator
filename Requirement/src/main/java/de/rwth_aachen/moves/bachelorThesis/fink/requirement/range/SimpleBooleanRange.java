package de.rwth_aachen.moves.bachelorThesis.fink.requirement.range;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;

import java.io.Serializable;
import java.util.Set;

public class SimpleBooleanRange implements IBooleanRange, Serializable {

	private final boolean canBeTrue;
	private final boolean canBeFalse;

	public SimpleBooleanRange(boolean canBeTrue, boolean canBeFalse) {
		this.canBeTrue = canBeTrue;
		this.canBeFalse = canBeFalse;
	}

	public SimpleBooleanRange() {
		this(true, true);
	}

	public SimpleBooleanRange(IBooleanRange other) {
		this(other.canBeTrue(), other.canBeFalse());
	}


	public static IBooleanRange fromIRange(IRange other) {
		if (other instanceof IBooleanRange) {
			return new SimpleBooleanRange((IBooleanRange) other);
		} else {
			throw new RuntimeException("Can not initialize boolean range from unknown range!");
		}
	}

	public static IBooleanRange ternary(IBooleanRange a, IBooleanRange b) {
		return new SimpleBooleanRange(a.canBeTrue() || b.canBeTrue(), a.canBeFalse() || b.canBeFalse());
	}

	@Override
	public boolean canBeTrue() {
		return canBeTrue;
	}

	@Override
	public boolean canBeFalse() {
		return canBeFalse;
	}

	@Override
	public boolean isDefinite() {
		return (!canBeTrue || !canBeFalse);
	}

	@Override
	public IRange cloneRange() { return new SimpleBooleanRange(this); }

	@Override
	public boolean isRestricted() {
		return (!canBeFalse || !canBeTrue);
	}

	@Override
	public boolean isOverconstrained() {
		return (!canBeFalse && !canBeTrue);
	}

	@Override
	public IRange removeRestrictions() {
		return new SimpleBooleanRange();
	}

	public SimpleBooleanRange setCanBeTrue(boolean canBeTrue) {
		return new SimpleBooleanRange(canBeTrue, canBeFalse);
	}

	public SimpleBooleanRange setCanBeFalse(boolean canBeFalse) {
		return new SimpleBooleanRange(canBeTrue, canBeFalse);
	}

	@Override
	public boolean isCompatibleWith(IRange other) {
		if (other instanceof IBooleanRange) {
			IBooleanRange booleanRange = (IBooleanRange) other;
			if (canBeTrue && !booleanRange.canBeTrue()) {
				return false;
			} else return !canBeFalse || booleanRange.canBeFalse();
		}
		return false;
	}

	@Override
	public RangeType getRangeType() {
		return RangeType.BOOLEAN;
	}

	@Override
	public IRange restrictToType(DataType dataType) {
		if (dataType.isBool()) {
			return new SimpleBooleanRange(this);
		}
		throw new RuntimeException("Can not restrict to types other than boolean.");
	}

	@Override
	public Set<DataType> getUnderlyingTypes() {
		return DataTypeContext.makeSet(DataType.INSTANCE_BOOL);
	}

	@Override
	public IRange or(IRange otherRange) {
		if (otherRange instanceof SimpleBooleanRange) {
			final SimpleBooleanRange other = (SimpleBooleanRange) otherRange;

			return new SimpleBooleanRange(canBeTrue || other.canBeTrue, canBeFalse || other.canBeFalse);
		}
		throw new RuntimeException("Unsupported type of Range for or!");
	}

	@Override
	public String toString() {
		return "SimpleBooleanRange(canBeTrue: " + canBeTrue + ", canBeFalse: " + canBeFalse + ")";
	}
}
