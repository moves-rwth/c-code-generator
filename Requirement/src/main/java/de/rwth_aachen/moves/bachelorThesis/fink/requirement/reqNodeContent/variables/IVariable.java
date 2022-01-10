package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.NonDeterministicUpdateInformation;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * An instance of this interface represents a variable in the C-Program (ah yes).
 * It also holds information about it's parent (if it is a member of a struct/union),
 * it's type (see DataType) and more.
 */
public interface IVariable extends Comparable<IVariable>, Serializable {
	/**
	 * 	Whether this variable requires a pointer to be resolved higher up in the chain.
	 * 	Imagine a struct A with a member int i. Given a function with a parameter A* a,
	 * 	we need to generate a Variable instance representing a->i.
	 * 	This will be a shadow instance, as relies on resolving a before use.
	 */
	boolean isShadowVariable();

	String getInternalName();
	String getName();
	DataType getDataType();
	ParameterType getParameterType();

	boolean hasValue();
	StringArray getValueAsString();

	/**
 	 * @return true iff variable has a parent (is part of an union / a struct / an array)
	 */
	boolean requiresStorage();

	/**
	 * @return true iff underlying type itself is not a struct / union or a pointer/typedef to them
	 */
	boolean isBasic();

	/**
	 * @return IMemberContainer of struct/union iff this variable is part of one
	 */
	IMemberContainer getParent();

	IVariable copy();
	IVariable replaceParent(IMemberContainer newParent);

	IVariable rename(String name, String internalName);

	IVariable addNamePrefix(String prefix);

	void setValue(String newValue);

	StringArray getStorageDeclaration();

	/**
	 * Instead of building the name of the last'ed variable in the last operators, we can defer this to the variable itself.
	 * Warning: Doing this here instead of in the IVariableWithAccessor is laziness and because for now, this is not required yet - but for dynamic expressions (last(*PTR_VAR)),
	 * this simply won't do.
	 * @param depthCode A piece of code expressing the amount of steps to go back.
	 * @return A name to be used to access the given variable depthCode many steps in the past.
	 */
	String getNameOfLastedVariable(String depthCode);

	String getVariableAccessorName();

	NonDeterministicUpdateInformation buildNonDetUpdateCode(boolean useHarness, Map<IVariable, SimpleExpressionConditioner> conditionerMap);

	public static final Comparator<IVariable> NATURAL_ORDER = new Comparator<IVariable>() {

		public int naturalCompare(String a, String b, boolean ignoreCase) {
			if (ignoreCase) {
				a = a.toLowerCase();
				b = b.toLowerCase();
			}
			int aLength = a.length();
			int bLength = b.length();
			int minSize = Math.min(aLength, bLength);
			char aChar, bChar;
			boolean aNumber, bNumber;
			boolean asNumeric = false;
			int lastNumericCompare = 0;
			for (int i = 0; i < minSize; i++) {
				aChar = a.charAt(i);
				bChar = b.charAt(i);
				aNumber = aChar >= '0' && aChar <= '9';
				bNumber = bChar >= '0' && bChar <= '9';
				if (asNumeric)
					if (aNumber && bNumber) {
						if (lastNumericCompare == 0)
							lastNumericCompare = aChar - bChar;
					} else if (aNumber)
						return 1;
					else if (bNumber)
						return -1;
					else if (lastNumericCompare == 0) {
						if (aChar != bChar)
							return aChar - bChar;
						asNumeric = false;
					} else
						return lastNumericCompare;
				else if (aNumber && bNumber) {
					asNumeric = true;
					if (lastNumericCompare == 0)
						lastNumericCompare = aChar - bChar;
				} else if (aChar != bChar)
					return aChar - bChar;
			}
			if (asNumeric)
				if (aLength > bLength && a.charAt(bLength) >= '0' && a.charAt(bLength) <= '9') // as number
					return 1;  // a has bigger size, thus b is smaller
				else if (bLength > aLength && b.charAt(aLength) >= '0' && b.charAt(aLength) <= '9') // as number
					return -1;  // b has bigger size, thus a is smaller
				else if (lastNumericCompare == 0)
					return aLength - bLength;
				else
					return lastNumericCompare;
			else
				return aLength - bLength;
		}

		// Overriding the compare method to sort by name
		@Override
		public int compare(IVariable vA, IVariable vB) {
			return naturalCompare(vA.getInternalName(), vB.getInternalName(), true);
		}
	};

	boolean isLastIHasToBeArray();

	boolean isArray();

	IVariable replaceDataType(DataType replacementDataType, IProgramContext programContext);
}

