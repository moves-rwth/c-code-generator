package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IBooleanRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IFloatingPointRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.IRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

class VariableCreator implements Serializable {

	public static Variable createVariable(IMemberContainer parent, ParameterType parameterType, DataType dataType, String name, String internalName, IDataTypeContext dataTypeContext, IShadowInformation shadowInformation, SimpleExpressionConditioner expressionConditioner) {
		if (dataType instanceof IArrayType) {
			final IArrayType arrayType = (IArrayType) dataType;
			int memberAmount = arrayType.getEntriesCount();
			if (arrayType.getArrayType().isFloatingPoint()) {
				List<FloatingPointVariable> members = new ArrayList<>();
				if (shadowInformation == null) {
					for (int i = 0; i < memberAmount; i++) {
						final String memberName = "arrayVar" + i;
						final Variable memberVariable = createVariable(null, parameterType, arrayType.getArrayType(), memberName, internalName + '_' + memberName, dataTypeContext, null, null);
						members.add((FloatingPointVariable) memberVariable);
					}
				}
				return new FloatingPointArrayVariable(parent, dataType, parameterType, name, internalName, shadowInformation, arrayType.getDimensions(), members);
			} else {
				List<IntegerVariable> members = new ArrayList<>();
				if (shadowInformation == null) {
					for (int i = 0; i < memberAmount; i++) {
						final String memberName = "arrayVar" + i;
						final Variable memberVariable = createVariable(null, parameterType, arrayType.getArrayType(), memberName, internalName + '_' + memberName, dataTypeContext, null, null);
						members.add((IntegerVariable) memberVariable);
					}
				}
				return new IntegerArrayVariable(parent, dataType, parameterType, name, internalName, shadowInformation, arrayType.getDimensions(), members);
			}
		} else if (dataType instanceof IPointerType) {
			return new PointerVariable(parent, dataType, parameterType, name, internalName, shadowInformation);
		} else if (dataType.isInteger()) {
			return makeIntegerVariable(parent, dataType, parameterType, name, internalName, shadowInformation, expressionConditioner);
		} else if (dataType.isFloatingPoint()) {
			return makeFloatingPointVariable(parent, dataType, parameterType, name, internalName, shadowInformation, expressionConditioner);
		} else if (dataType instanceof IStructType) {
			final IStructType structType = (IStructType) dataType;
			final LinkedHashMap<String, String> memberTypes = structType.getMembers();
			LinkedHashMap<String, IVariable> members = new LinkedHashMap<>();
			if (shadowInformation == null) {
				for (String memberName : memberTypes.keySet()) {
					final DataType memberType = dataTypeContext.byName(memberTypes.get(memberName));
					final Variable memberVariable = createVariable(null, parameterType, memberType, memberName, internalName + '_' + memberName, dataTypeContext, null, null);
					members.put(internalName + '_' + memberName, memberVariable);
				}
			}
			return new Struct(parent, dataType, parameterType, name, internalName, members, shadowInformation);
		} else if (dataType instanceof IUnionType) {
			final IUnionType unionType = (IUnionType) dataType;
			final LinkedHashMap<String, String> memberTypes = unionType.getMembers();
			LinkedHashMap<String, IVariable> members = new LinkedHashMap<>();
			if (shadowInformation == null) {
				for (String memberName : memberTypes.keySet()) {
					final DataType memberType = dataTypeContext.byName(memberTypes.get(memberName));
					final Variable memberVariable = createVariable(null, parameterType, memberType, memberName, internalName + '_' + memberName, dataTypeContext, null, null);
					members.put(internalName + '_' + memberName, memberVariable);
				}
			}
			return new Union(parent, dataType, parameterType, name, internalName, members, shadowInformation);
		} else {
			throw new RuntimeException("Unhandled.");
		}
	}

	private static Variable makeIntegerVariable(IMemberContainer parent, DataType dataType, ParameterType parameterType, String name, String internalName, IShadowInformation shadowInformation, SimpleExpressionConditioner expressionConditioner) {
		if (expressionConditioner != null) {
			SimpleExpressionConditioner restrictedChildExpressionConditioner = null;
			try {
				restrictedChildExpressionConditioner = expressionConditioner.restrictToType(dataType);
			} catch (UnsatisfiableConstraintsException e) {
				e.printStackTrace();
			}
			IRange range = restrictedChildExpressionConditioner.getRange();
			if (range instanceof IBooleanRange) {
				IBooleanRange booleanRange = (IBooleanRange) range;
				long value;
				if (!booleanRange.canBeTrue()) value = 0;
				else if (!booleanRange.canBeTrue()) value = 1;
				else value = RandomGenHelper.randomInt(0, 1);
				return new IntegerVariable(parent, dataType, parameterType, name, internalName, shadowInformation, dataType.getLowerLimit(), dataType.getUpperLimit(), value);
			} else if (range instanceof IIntegerRange) {
				long value = RandomGenHelper.getRandomPrettyLong((IIntegerRange) range, ((IIntegerRange) range).getExclusions());
				return new IntegerVariable(parent, dataType, parameterType, name, internalName, shadowInformation, dataType.getLowerLimit(), dataType.getUpperLimit(), value);
			} else {
				throw new RuntimeException("Why is there a non-Integer range here?");
			}
		} else {
			return new IntegerVariable(parent, dataType, parameterType, name, internalName, shadowInformation, dataType.getLowerLimit(), dataType.getUpperLimit(), 0L);
		}
	}

	private static Variable makeFloatingPointVariableByGranularity(IMemberContainer parent, DataType dataType, ParameterType parameterType, String name, String internalName, BigDecimal lowerLimit, BigDecimal upperLimit, long divisions, IShadowInformation shadowInformation, SimpleExpressionConditioner expressionConditioner) {
		if (expressionConditioner != null) {
			SimpleExpressionConditioner restrictedChildExpressionConditioner = null;
			try {
				restrictedChildExpressionConditioner = expressionConditioner.restrictToType(dataType);
			} catch (UnsatisfiableConstraintsException e) {
				e.printStackTrace();
			}
			IRange range = restrictedChildExpressionConditioner.getRange();
			if (range instanceof IFloatingPointRange) {
				IFloatingPointRange floatingPointRange = ((IFloatingPointRange) range);
				double value = RandomGenHelper.getRandomPrettyDouble(floatingPointRange, ((IFloatingPointRange) range).getExclusions());
				return new FloatingPointVariable(parent, dataType, parameterType, name, internalName, shadowInformation, BigDecimal.valueOf(floatingPointRange.getLowerLimitFp()), BigDecimal.valueOf(floatingPointRange.getUpperLimitFp()), BigDecimal.valueOf(value));
			}
		} else {
			final BigDecimal range = upperLimit.subtract(lowerLimit);
			final long choice = RandomGenHelper.randomLong(0L, divisions);
			final BigDecimal additive = range.divide(BigDecimal.valueOf(divisions), RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(choice));
			return new FloatingPointVariable(parent, dataType, parameterType, name, internalName, shadowInformation, lowerLimit, upperLimit, lowerLimit.add(additive));
		}
		throw new RuntimeException("We should never arrive here");
	}

	private static Variable makeFloatingPointVariable(IMemberContainer parent, DataType dataType, ParameterType parameterType, String name, String internalName, IShadowInformation shadowInformation, SimpleExpressionConditioner expressionConditioner) {
		final int choice = RandomGenHelper.randomInt(0, 5);
		switch (choice) {
			case 0:
				return makeFloatingPointVariableByGranularity(parent, dataType, parameterType, name, internalName, BigDecimal.ZERO, BigDecimal.ONE, 4L, shadowInformation, expressionConditioner);
			case 1:
				return makeFloatingPointVariableByGranularity(parent, dataType, parameterType, name, internalName, BigDecimal.ZERO, BigDecimal.ONE, 8L, shadowInformation, expressionConditioner);
			case 2:
				return makeFloatingPointVariableByGranularity(parent, dataType, parameterType, name, internalName, BigDecimal.valueOf(-1L), BigDecimal.ONE, 8L, shadowInformation, expressionConditioner);
			case 3:
				return makeFloatingPointVariableByGranularity(parent, dataType, parameterType, name, internalName, BigDecimal.valueOf(0L), BigDecimal.valueOf(100L), 200L, shadowInformation, expressionConditioner);
			case 4:
				return makeFloatingPointVariableByGranularity(parent, dataType, parameterType, name, internalName, BigDecimal.valueOf(-100L), BigDecimal.valueOf(100L), 400L, shadowInformation, expressionConditioner);
			case 5:
				return makeFloatingPointVariableByGranularity(parent, dataType, parameterType, name, internalName, BigDecimal.ZERO, BigDecimal.ONE, 1000L, shadowInformation, expressionConditioner);
			default:
				throw new RuntimeException("Failed to generate floating point variable!");
		}
	}

}
