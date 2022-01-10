import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.RangeWrapsAroundException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleIntegerRange;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;

public class RangeTests {
	//@Test
	void RangeTruncation() {
		File file = new File("../CQuirksAndNoteworthyStuff/truncationDataGenerator/input.txt");
		Scanner scanner;
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException error) {
			throw new RuntimeException("File not found");
		}

		while (scanner.hasNext()) {
			String[] types = scanner.nextLine().split(" ");
			String[] values = scanner.nextLine().split(" ");
			String empty = scanner.nextLine();

			DataType sourceType;
			switch (types[0]) {
				case "I32":
					sourceType = DataType.INSTANCE_INT32;
					break;
				case "U32":
					sourceType = DataType.INSTANCE_UINT32;
					break;
				default:
					throw new RuntimeException("Invalid string " + types[0]);
			}

			DataType targetType;
			switch (types[2]) {
				case "I8":
					targetType = DataType.INSTANCE_INT8;
					break;
				case "I16":
					targetType = DataType.INSTANCE_INT16;
					break;
				case "I32":
					targetType = DataType.INSTANCE_INT32;
					break;
				case "U8":
					targetType = DataType.INSTANCE_UINT8;
					break;
				case "U16":
					targetType = DataType.INSTANCE_UINT16;
					break;
				case "U32":
					targetType = DataType.INSTANCE_UINT32;
					break;
				default:
					throw new RuntimeException("Invalid string " + types[2]);
			}

			long sourceValue = Long.parseLong(values[0]);
			long targetValue = Long.parseLong(values[2]);

			SimpleIntegerRange toTruncate;
			try {
				toTruncate = new SimpleIntegerRange(Set.of(sourceType), sourceValue, sourceValue, Set.of());
			} catch (UnsatisfiableConstraintsException error) {
				throw new RuntimeException("Unreachable... I hope");
			}

			SimpleIntegerRange truncatedRange;
			try {
				truncatedRange = toTruncate.convertToDataTypeWithTruncation(targetType);
			} catch (RangeWrapsAroundException error) {
				throw new RuntimeException("My implementation is wrong :(");
			}

			if (truncatedRange.getLowerLimit() != truncatedRange.getUpperLimit() || targetValue != truncatedRange.getLowerLimit()) {
				String debugInfo = "Range: [" + truncatedRange.getLowerLimit() + "; " + truncatedRange.getUpperLimit() + "], but expected " + targetValue;

				// Debug: Provoke error again but this time we can step into it :)
				try {
					truncatedRange = toTruncate.convertToDataTypeWithTruncation(targetType);
				} catch (RangeWrapsAroundException error) {
					throw new RuntimeException("My implementation is wrong :(");
				}
				throw new RuntimeException(debugInfo);
			}
		}

		scanner.close();
	}

//	@Test
	void Modulo() {
		final int TO_TEST_COUNT = 5000000;

		// A % B

		// A random but set to one number
		for (int testIndex = 0; testIndex < TO_TEST_COUNT; ++testIndex) {
			long valueA = RandomGenHelper.randomInt(0, Integer.MAX_VALUE - 1) - Integer.MAX_VALUE / 2;

			long valueB = RandomGenHelper.randomInt(0, Integer.MAX_VALUE - 1) - Integer.MAX_VALUE / 2;
			long valueBHalfSpan = RandomGenHelper.randomInt(-50, 50);

			SimpleIntegerRange rangeA;
			SimpleIntegerRange rangeB;
			SimpleIntegerRange rangeResult;
			try {
				rangeA = new SimpleIntegerRange(Set.of(DataType.INSTANCE_INT32), valueA, valueA, Set.of(0L));
				rangeB = new SimpleIntegerRange(Set.of(DataType.INSTANCE_INT32), valueB - valueBHalfSpan, valueB + valueBHalfSpan, Set.of(0L));
				rangeResult = (SimpleIntegerRange) SimpleIntegerRange.mod(rangeA, rangeB);
			} catch (UnsatisfiableConstraintsException error) {
				continue;
			}

			//System.out.println("[" + rangeA.getLowerLimit() + "; " + rangeA.getUpperLimit() + "] % [" + rangeB.getLowerLimit() + "; " + rangeB.getUpperLimit() + "] = [" + rangeResult.getLowerLimit() + "; " + rangeResult.getUpperLimit() + "]");
			for (long currentValueToTest = rangeB.getLowerLimit(); currentValueToTest <= rangeB.getUpperLimit(); ++currentValueToTest) {
				if (currentValueToTest == 0) { continue; }
				long result = valueA % currentValueToTest;
				if ( ! rangeResult.isValueAllowed(result)) {
					String problemString = "[" + rangeA.getLowerLimit() + "; " + rangeA.getUpperLimit() + "] % [" + rangeB.getLowerLimit() + "; " + rangeB.getUpperLimit() + "] = [" + rangeResult.getLowerLimit() + "; " + rangeResult.getUpperLimit() + "], vs " + result;
					throw new RuntimeException("Overconstrained: " + problemString);
				}
			}
		}

		// B random but set to one number
		for (int testIndex = 0; testIndex < TO_TEST_COUNT; ++testIndex) {
			long valueA = RandomGenHelper.randomInt(0, Integer.MAX_VALUE - 1) - Integer.MAX_VALUE / 2;
			long valueAHalfSpan = RandomGenHelper.randomInt(-50, 50);

			long valueB = RandomGenHelper.randomInt(0, Integer.MAX_VALUE - 1) - Integer.MAX_VALUE / 2;

			SimpleIntegerRange rangeA;
			SimpleIntegerRange rangeB;
			SimpleIntegerRange rangeResult;
			try {
				rangeA = new SimpleIntegerRange(Set.of(DataType.INSTANCE_INT32), valueA - valueAHalfSpan, valueA + valueAHalfSpan, Set.of(0L));
				rangeB = new SimpleIntegerRange(Set.of(DataType.INSTANCE_INT32), valueB, valueB, Set.of(0L));
				rangeResult = (SimpleIntegerRange) SimpleIntegerRange.mod(rangeA, rangeB);
			} catch (UnsatisfiableConstraintsException error) {
				continue;
			}

			for (long currentValueToTest = rangeA.getLowerLimit(); currentValueToTest <= rangeA.getUpperLimit(); ++currentValueToTest) {
				if (currentValueToTest == 0) { continue; }
				long result = currentValueToTest % valueB;
				if ( ! rangeResult.isValueAllowed(result)) {
					String problemString = "[" + rangeA.getLowerLimit() + "; " + rangeA.getUpperLimit() + "] % [" + rangeB.getLowerLimit() + "; " + rangeB.getUpperLimit() + "] = [" + rangeResult.getLowerLimit() + "; " + rangeResult.getUpperLimit() + "], vs " + result;
					throw new RuntimeException("Overconstrained: " + problemString);
				}
			}
		}

		// A and B random ranges
		for (int testIndex = 0; testIndex < TO_TEST_COUNT; ++testIndex) {
			long valueA = RandomGenHelper.randomInt(0, Integer.MAX_VALUE - 1) - Integer.MAX_VALUE / 2;
			long valueAHalfSpan = RandomGenHelper.randomInt(-50, 50);

			long valueB = RandomGenHelper.randomInt(0, Integer.MAX_VALUE - 1) - Integer.MAX_VALUE / 2;
			long valueBHalfSpan = RandomGenHelper.randomInt(-50, 50);

			SimpleIntegerRange rangeA;
			SimpleIntegerRange rangeB;
			SimpleIntegerRange rangeResult;
			try {
				rangeA = new SimpleIntegerRange(Set.of(DataType.INSTANCE_INT32), valueA - valueAHalfSpan, valueA + valueAHalfSpan, Set.of(0L));
				rangeB = new SimpleIntegerRange(Set.of(DataType.INSTANCE_INT32), valueB - valueBHalfSpan, valueB + valueBHalfSpan, Set.of(0L));
				rangeResult = (SimpleIntegerRange) SimpleIntegerRange.mod(rangeA, rangeB);
			} catch (UnsatisfiableConstraintsException error) {
				continue;
			}

			for (valueA = rangeA.getLowerLimit(); valueA <= rangeA.getUpperLimit(); ++valueA) {
				for (valueB = rangeB.getLowerLimit(); valueB <= rangeB.getUpperLimit(); ++valueB) {
					if (valueB == 0) { continue; }
					long result = valueA % valueB;
					if (!rangeResult.isValueAllowed(result)) {
						String problemString = "[" + rangeA.getLowerLimit() + "; " + rangeA.getUpperLimit() + "] % [" + rangeB.getLowerLimit() + "; " + rangeB.getUpperLimit() + "] = [" + rangeResult.getLowerLimit() + "; " + rangeResult.getUpperLimit() + "], vs " + result;
						throw new RuntimeException("Overconstrained: " + problemString);
					}
				}
			}
		}
	}
}
