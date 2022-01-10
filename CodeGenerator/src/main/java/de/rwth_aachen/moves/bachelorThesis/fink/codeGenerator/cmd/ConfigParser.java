package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Parses commandline options into a CodePropertiesConfig-Object.
 */
public class ConfigParser {

	/**
	 * Initialize a probability map with the Operators and Integer as values.
	 * Probability of an Operator is the value divided by the total of all values.
	 * @param file A .ini file containing a probability map for the operators
	 * @return A probability (Enum-)Map with the Operators and Integer values
	 * @throws IOException
	 */
	public static EnumMap<Operators, Integer> parseIni(File file) throws IOException {
		Ini ini = new Ini(file);
		Ini.Section section = ini.get("probabilities");
		EnumMap<Operators, Integer> mOperatorsToProbability = new EnumMap<Operators, Integer>(Operators.class);

		for (Map.Entry<String, String> entry: section.entrySet()) {
			Integer value = Integer.parseInt(entry.getValue());
			switch (entry.getKey()) {
				case "ABS":
					mOperatorsToProbability.put(Operators.ABS, value);
					break;
				case "AND":
					mOperatorsToProbability.put(Operators.AND, value);
					break;
				case "ARRAY_ACCESS":
					mOperatorsToProbability.put(Operators.ARRAY_ACCESS, value);
					break;
				case "BIT_EXTRACTION":
					mOperatorsToProbability.put(Operators.BIT_EXTRACTION, value);
					break;
				case "DIVISION":
					mOperatorsToProbability.put(Operators.DIVISION, value);
					break;
				case "MODULO":
					mOperatorsToProbability.put(Operators.MODULO, value);
					break;
				case "EMPTY":
					mOperatorsToProbability.put(Operators.EMPTY, value);
					break;
				case "EQUALS":
					mOperatorsToProbability.put(Operators.EQUALS, value);
					break;
				case "GREATER_EQUALS":
					mOperatorsToProbability.put(Operators.GREATER_EQUALS, value);
					break;
				case "GREATER":
					mOperatorsToProbability.put(Operators.GREATER, value);
					break;
				case "ITE":
					mOperatorsToProbability.put(Operators.ITE, value);
					break;
				case "IT":
					mOperatorsToProbability.put(Operators.IT, value);
					break;
				case "CONCATENATION":
					mOperatorsToProbability.put(Operators.CONCATENATION, value);
					break;
				case "EMPTY_CS":
					mOperatorsToProbability.put(Operators.EMPTY_CS, value);
					break;
				case "LAST_I":
					mOperatorsToProbability.put(Operators.LAST_I, value);
					break;
				case "LAST":
					mOperatorsToProbability.put(Operators.LAST, value);
					break;
				case "MACRO":
					mOperatorsToProbability.put(Operators.MACRO, value);
					break;
				case "MINIMUM":
					mOperatorsToProbability.put(Operators.MINIMUM, value);
					break;
				case "MAXIMUM":
					mOperatorsToProbability.put(Operators.MAXIMUM, value);
					break;
				case "MINUS_BINARY":
					mOperatorsToProbability.put(Operators.MINUS_BINARY, value);
					break;
				case "MINUS_UNARY":
					mOperatorsToProbability.put(Operators.MINUS_UNARY, value);
					break;
				case "NOT_EQUALS":
					mOperatorsToProbability.put(Operators.NOT_EQUALS, value);
					break;
				case "NOT":
					mOperatorsToProbability.put(Operators.NOT, value);
					break;
				case "OR":
					mOperatorsToProbability.put(Operators.OR, value);
					break;
				case "OUTPUT":
					mOperatorsToProbability.put(Operators.OUTPUT, value);
					break;
				case "PARENTHESIS":
					mOperatorsToProbability.put(Operators.PARENTHESIS, value);
					break;
				case "PLUS":
					mOperatorsToProbability.put(Operators.PLUS, value);
					break;
				case "SMALLER_EQUALS":
					mOperatorsToProbability.put(Operators.SMALLER_EQUALS, value);
					break;
				case "SMALLER":
					mOperatorsToProbability.put(Operators.SMALLER, value);
					break;
				case "TIMES":
					mOperatorsToProbability.put(Operators.TIMES, value);
					break;
				case "BIT_SHIFT_LEFT":
					mOperatorsToProbability.put(Operators.BIT_SHIFT_LEFT, value);
					break;
				case "BIT_SHIFT_RIGHT":
					mOperatorsToProbability.put(Operators.BIT_SHIFT_RIGHT, value);
					break;
				case "BITWISE_NOT":
					mOperatorsToProbability.put(Operators.BITWISE_NOT, value);
					break;
				case "BITWISE_AND":
					mOperatorsToProbability.put(Operators.BITWISE_AND, value);
					break;
				case "BITWISE_OR":
					mOperatorsToProbability.put(Operators.BITWISE_OR, value);
					break;
				case "BITWISE_XOR":
					mOperatorsToProbability.put(Operators.BITWISE_XOR, value);
					break;
				case "TERNARY_OPERATOR":
					mOperatorsToProbability.put(Operators.TERNARY_OPERATOR, value);
					break;
			}
		}
		return mOperatorsToProbability;
	}
}
