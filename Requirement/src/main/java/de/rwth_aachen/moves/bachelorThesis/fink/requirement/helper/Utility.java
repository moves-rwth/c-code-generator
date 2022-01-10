package de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utility {
	private static final int MAX_LENGTH_BEFORE_EXP = 7;

	public static String floatingPointToCString(DataType dataType, BigDecimal d) {
		String result = d.toPlainString();
		if (!result.contains(".")) {
			if (result.length() > MAX_LENGTH_BEFORE_EXP) {
				String first = result.substring(0, MAX_LENGTH_BEFORE_EXP);
				String second = result.substring(MAX_LENGTH_BEFORE_EXP);

				result = first + "." + second + "e+" + second.length();
			} else {
				result = result + ".0";
			}
		}

		if (dataType.isFloatingPoint()) {
			result = result + "F";
		} else {
			result = result + "";
		}

		return result;
	}

	public static List<Integer> primeFactors(int numbers) {
		int n = numbers;
		if (numbers == 1) {
			return new ArrayList<>(Collections.singletonList(1));
		}
		List<Integer> factors = new ArrayList<Integer>();
		for (int i = 2; i <= n / i; i++) {
			while (n % i == 0) {
				factors.add(i);
				n /= i;
			}
		}
		if (n > 1) {
			factors.add(n);
		}
		return factors;
	}

	public static long euclideanModulus(long number, long mod) {
		long result = number % mod;
		if (result < 0) {
			result += mod;
		}
		return (result);
	}

}
