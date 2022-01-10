import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.ContentCreator;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.RequirementToC;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class ArgumentsTest {

	private final static int amountOfSeeds = 5;

	@Test
	public void defaultTestEnvironment() { testArgumentInDifferentSizes(new String[] {}); }

	@Test
	public void wrapperStructs_Default() { testArgumentInDifferentSizes(new String[] {"--wrapperStructs"}); }
	@Test
	public void wrapperStructs_Amount5() { testArgumentInDifferentSizes(new String[] {"--wrapperStructs", "--wrapperStructsAmount=5"}); }
	@Test
	public void wrapperStructs_Percentage10() { testArgumentInDifferentSizes(new String[] {"--wrapperStructs", "--wrapperStructsPercentage=10"}); }
	@Test
	public void wrapperStructs_Percentage50() { testArgumentInDifferentSizes(new String[] {"--wrapperStructs", "--wrapperStructsPercentage=50"}); }

	@Test
	public void wrapperUnions_Default() { testArgumentInDifferentSizes(new String[] {"--wrapperUnions"}); }
	@Test
	public void wrapperUnions_Percentage10() { testArgumentInDifferentSizes(new String[] {"--wrapperUnions", "--wrapperUnionsPercentage=10"}); }
	@Test
	public void wrapperUnions_Percentage50() { testArgumentInDifferentSizes(new String[] {"--wrapperUnions", "--wrapperUnionsPercentage=50"}); }
	@Test
	public void wrapperUnions_AdditionalPercentage10() { testArgumentInDifferentSizes(new String[] {"--wrapperUnions", "--wrapperUnionsAdditionalPercentage=10"}); }
	@Test
	public void wrapperUnions_AdditionalPercentage50() { testArgumentInDifferentSizes(new String[] {"--wrapperUnions", "--wrapperUnionsAdditionalPercentage=50"}); }

	@Test
	public void wrapperPointers_Default() { testArgumentInDifferentSizes(new String[] {"--wrapperPointers"}); }
	@Test
	public void wrapperPointers_UseVoidType() { testArgumentInDifferentSizes(new String[] {"--wrapperPointers", "--wrapperPointersUseVoidType"}); }
	@Test
	public void wrapperPointers_Percentage() { testArgumentInDifferentSizes(new String[] {"--wrapperPointers", "--wrapperPointersPercentage=50"}); }
	@Test
	public void wrapperPointers_UseVoidTypeAndPercentage() { testArgumentInDifferentSizes(new String[] {"--wrapperPointers", "--wrapperPointersUseVoidType", "--wrapperPointersPercentage=50"}); }

	@Test
	public void wrapperArrays() {
		String[][] configurations = {
				{"--wrapperArrays"}
		};
		testMultipleConfigurationsInDifferentSizes(configurations);
	}

	//	@Test
	public void restrictions_NoUseFloats() { testArgumentInDifferentSizes(new String[] {"--no-useFloats"}); }
	//	@Test
	public void restrictions_DontForceBooleanForCondition() { testArgumentInDifferentSizes(new String[] {"--dontForceBooleanForCondition"}); }
	@Test
	public void restrictions_UseFullFloatRange() { testArgumentInDifferentSizes(new String[] {"--useFullFloatRange"}); }
	@Test
	public void restrictions_UseFullIntegerRange() { testArgumentInDifferentSizes(new String[] {"--useFullIntegerRange"}); }
	@Test
	public void restrictions_RestrictToIntegerVariable() { testArgumentInDifferentSizes(new String[] {"--restrictToIntegerVariable"}); }
	@Test
	public void restrictions_NoUseTernary() { testArgumentInDifferentSizes(new String[] {"--no-useTernaryForITOperator"}); }

	@Test
	public void functionizing() {
		String[][] configurations = {
				{"--functionizing"},
				{"--functionizing", "--no-stepLocalVariables"},
		};
		testMultipleConfigurationsInDifferentSizes(configurations);
	}

	@Test
	public void misc_createFillerCode() {
		testArgumentInDifferentSizes(new String[] {"--fillerCode"});
	}

	@Test
	public void misc_KLoop() {
		testArgumentInDifferentSizes(new String[] {"--k-loop=50"});
	}

	@Test
	/**
	 * This test checks the usage of the Operators.
	 * Mainly in order to check, if a certain operator is not being used due to changes.
	 * Only works if executed after the usage of other tests as it uses the static list,
	 * which is only filled when RequirementToC.main() is executed before this test (therefore the prefix 'z_' using the Alphanumeric order of tests).
	 * If this test is to be disregarded due to other means, the list in ContentCreator.java and the according method should be deleted as well.
	 */
	public void z_checkOperators() {
		EnumSet<Operators> ops = RandomGenHelper.getNonZeroOperators(CodePropertiesConfig.getDefaultOperatorProbability());
		// Check if there is an operator that is not being used at all
		for (Operators op : ops) {
			if (null == ContentCreator.operatorCheckList.get(op)) {
				throw new RuntimeException("Operator '" + op.toString() + "' was not used.");
			}
		}
		// Print statistics and highlight if there are operators being used that are not in the probability map
		System.out.println("Statistics: ");
		for (Operators op : ContentCreator.operatorCheckList.keySet()) {
			System.out.println("Operator '" + op + "' was used " + ContentCreator.operatorCheckList.get(op) + " times.");
			if (!ops.contains(op)) {
				System.err.println("Operator '" + op + "' was not in probability map.");
			}
		}
	}

	private void testMultipleConfigurationsInDifferentSizes(String[][] configurations) {
		for (String[] programArguments : configurations) {
			testArgumentInDifferentSizes(programArguments);
		}
	}

	private void testArgumentInDifferentSizes(String[] argumentsToUseInTest) {
		String[][] configurations = {
				// NOTE(Felix): The runtime of the tests is within the "CodeGenerators" folder, and not
				// our whole project folder.
				{"--statsFilesPath=./outputFiles/", "--outputDir=./outputFiles/", "--reduce-console-output", "--no-useProbabilityMap", "--createRandomRequirementCode", "--randomCodeAmount=50", "--splitCount=5", "--disableFileCreation", "--connectRequirements", "--generateAsManyAsNeeded", "--concatenateAllProperties"},
				{"--statsFilesPath=./outputFiles/", "--outputDir=./outputFiles/", "--reduce-console-output", "--no-useProbabilityMap", "--createRandomRequirementCode", "--randomCodeAmount=100", "--splitCount=1", "--disableFileCreation", "--connectRequirements", "--generateAsManyAsNeeded", "--concatenateAllProperties"},
				{"--statsFilesPath=./outputFiles/", "--outputDir=./outputFiles/", "--reduce-console-output", "--no-useProbabilityMap", "--createRandomRequirementCode", "--randomCodeAmount=250", "--splitCount=2", "--disableFileCreation", "--connectRequirements", "--generateAsManyAsNeeded", "--concatenateAllProperties"},
				{"--statsFilesPath=./outputFiles/", "--outputDir=./outputFiles/", "--reduce-console-output", "--no-useProbabilityMap", "--createRandomRequirementCode", "--randomCodeAmount=500", "--splitCount=10", "--disableFileCreation", "--connectRequirements", "--generateAsManyAsNeeded", "--concatenateAllProperties"},
				{"--statsFilesPath=./outputFiles/", "--outputDir=./outputFiles/", "--reduce-console-output", "--no-useProbabilityMap", "--createRandomRequirementCode", "--randomCodeAmount=1000", "--splitCount=10", "--disableFileCreation", "--connectRequirements", "--generateAsManyAsNeeded", "--concatenateAllProperties"},
		};

		for (String[] argumentsToCombineWith : configurations) {
			// List contains a configuration entry, the parameter array and the seed to use (at the last index!)
			// Alternative idea would be to use List<String>
			String[] argumentsToUse = new String[argumentsToCombineWith.length + argumentsToUseInTest.length + 1];
			System.arraycopy(argumentsToCombineWith, 0, argumentsToUse, 0, argumentsToCombineWith.length);
			System.arraycopy(argumentsToUseInTest, 0, argumentsToUse, argumentsToCombineWith.length, argumentsToUseInTest.length);

			try {
				for (int i = 0; i < Math.max(amountOfSeeds, 1); i++) {
					argumentsToUse[argumentsToUse.length-1] = "--seed=" + RandomGenHelper.randomInt(0, 99999999);
					System.out.println("================================================================================");
					System.out.println("Testing arguments: " + String.join(" ", argumentsToUse));
					RequirementToC.main(argumentsToUse);
				}
			} catch (RuntimeException runtimeException){
				System.err.println("Caught a RuntimeException in Test: " + runtimeException.getLocalizedMessage());
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				runtimeException.printStackTrace(pw);
				System.err.println(sw);
				throw runtimeException;
			}
			System.out.println("================================================================================\n");
		}
	}
}
