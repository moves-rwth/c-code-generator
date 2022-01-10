package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd;


import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static picocli.CommandLine.*;


/**
 * Configuration class for the code-generation.
 */

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
@Command(mixinStandardHelpOptions = true, version = "v1.0.0", header = "CodeGenerator")
public class CodePropertiesConfig {

	// NOTE(Felix): Templates
	@ArgGroup(exclusive = false)
	TemplateArguments templateArguments;
	static class TemplateArguments {
		@Option(
				names = {"--createTemplates"},
				description = "Whether we create a template out of the created Requirements or not. Disabled by default"
		)
		private static boolean createTemplates = false;

		@Option(
				names = {"--inputTemplates"},
				split = ",",
				description = "File containing the templates we create."
		)
		private List<File> inputTemplates = new ArrayList<>();

		@Option(
				names = {"--outputTemplate"},
				description = "Path to the template file we create."
		)
		private File outputTemplate;
	}

	// NOTE(Xaver): Module Templates
	@ArgGroup(exclusive = false)
	ModuleTemplateArguments moduleTemplateArguments;
	static class ModuleTemplateArguments {
		@Option(
				names = {"--moduleTemplates"},
				description = "If we want to use module templates.",
				required = true
		)
		private boolean moduleTemplates = false;

		@Option(
				names = {"--moduleTemplatesPath"},
				description = "Path to the folder containing the moduleTemplates we want to use."
		)
		private Path moduleTemplatePath = Paths.get("Resources/templates");
	}

	// NOTE(Felix): Wrapper Structs
	@ArgGroup(exclusive = false)
	WrapperStructArguments wrapperStructArguments;
	static class WrapperStructArguments {
		@Option(
				names = {"--wrapperStructs"},
				description = "If we want to wrap all global variables into structs during post processing.",
				required = true
		)
		private boolean wrapperStructs = false;

		@Option(
				names = {"--wrapperStructsAmount"},
				description = "Into how many different structs we should wrap the global variables."
		)
		private int amount = 1;

		@Option(
				names = {"--wrapperStructsPercentage"},
				description = "Percentage of global variables to be wrapped by structs: [0-100]"
		)
		private int percentage = 100;
	}

	// NOTE(Felix): Wrapper Unions
	@ArgGroup(exclusive = false)
	WrapperUnionArguments wrapperUnionArguments;
	static class WrapperUnionArguments {
		@Option(
				names = {"--wrapperUnions"},
				description = "If we want to wrap all global variables into unions during post processing.",
				required = true
		)
		private boolean wrapperUnions = false;

		@Option(
				names = {"--wrapperUnionsPercentage"},
				description = "Percentage of global variables to be wrapped by unions: [0-100]"
		)
		private int percentage = 100;

		@Option(
				names = {"--wrapperUnionsAdditionalPercentage"},
				description = "What percentage of random types should be added into wrapper-unions."
		)
		private int additionalPercentage = 25;
	}


	// NOTE(Felix): Wrapper Arrays
	@ArgGroup(exclusive = false)
	WrapperArrayArguments wrapperArrayArguments;
	static class WrapperArrayArguments {
		@Option(
				names = {"--wrapperArrays"},
				description = "If we want to wrap all global variables into arrays during post processing.",
				required = true
		)
		private boolean wrapperArrays = false;

		@Option(
				names = {"--wrapperArrayPercentage"},
				description = "Percentage of global variables to be wrapped by arrays: [0-100]"
		)
		private int percentage = 100;
	}

	// NOTE(Felix): Wrapper Pointers
	@ArgGroup(exclusive = false)
	WrapperPointerArguments wrapperPointerArguments;
	static class WrapperPointerArguments {
		@Option(
				names = {"--wrapperPointers"},
				description = "If we want wrap all global variables behind pointer-variables.",
				required = true
		)
		private boolean wrapperPointers = false;

		@Option(
				names = {"--wrapperPointersUseVoidType"},
				description = "If the wrapper pointers should have the type void instead of the variable type.",
				negatable = true
		)
		private boolean useVoidType = false;

		@Option(
				names = {"--wrapperPointersPercentage"},
				description = "Percentage of global variables to be wrapped by a pointer: [0-100]"
		)
		private int percentage = 100;
	}

	// NOTE(Felix): Random requirement code
	@ArgGroup(exclusive = false)
	RandomRequirementCodeArguments randomRequirementCodeArguments = new RandomRequirementCodeArguments();
	static class RandomRequirementCodeArguments {
		@Option(
				names = {"--createRandomRequirementCode"},
				description = "Whether to create random code, not covered by the requirements."
		)
		private boolean createRandomRequirementCode = false;

		@Option(
				names = {"--randomCodeAmount"},
				description = "How many operators the random code should contain."
		)
		private int nodeCount = 500;

		@Option(
				names = {"--newVariableChance"},
				description = "Chance of creating a new variable instead of using an existing. Must be between 0 and 100."
		)
		private int newVariableChance = 3;

		@Option(
				names = {"--chanceConstantVsVariable"},
				description = "Chance of using a constant value instead of a variable. Must be between 0 and 100."
		)
		private int chanceConstantVsVariable = 10;

		@Option(
				names = {"--useFullFloatRange"},
				description = "Whether to generate fully random float/double constants or just integer-like ones. Experimental feature."
		)
		private boolean useFullFloatRange = false;

		@Option(
				names = {"--useFullIntegerRange"},
				description = "Whether to generate fully random integer/long constants or just restricted ones. Experimental feature."
		)
		private boolean useFullIntegerRange = false;

		@Option(
				names = {"--connectRequirements"},
				description = "Activates a post-processing step in which signal variables are replaced by output variables. This should result in stronger dependencies between parts of the code."
		)
		private boolean connectRequirements = false;

		@Option(
				names = {"--concatenateAllProperties"},
				description = "If this is set, all properties will be merged into one using &&."
		)
		private boolean concatenateAllProperties = false;
	}

	@Option(
			names = {"--ymlForPreprocessedCode"},
			description = "If we want to create an extra yml file for preprocessed .i-Code."
	)
	private boolean ymlForPreprocessedCode = false;

	@Option(
			names = {"--sizeOfIntInBytes"},
			description = "Sets sizeof(int) of the target platform. Default: 4"
	)
	private int sizeOfIntInBytes = 4;

	@Option(
			names = {"--no-directlyUseTemplates"},
			description = "Make use of templates for C code generation. True by default.",
			negatable = true
	)
	private boolean directlyUseTemplates = true;

	@Option(
			names = {"--createCodeFromTemplates"},
			description = "Whether to use the template for code creation or just randomly create it. False by default.",
			negatable = true
	)
	private boolean createCodeFromTemplates = false;

	@Option(
			names = {"--dontUseTemplateVariables"},
			description = "Whether variables from imported template files are allowed to be used in random generation.",
			negatable = true
	)
	private boolean dontUseTemplateVariables = false;

	@Option(
			names = {"--fileSuffix"},
			description = "Specifies a suffix which is added to the created .c files"
	)
	private String fileSuffix = "";

	@Option(
			names = {"--statsFilesPath", "-s"},
			description = "Directory where the stats files for the requirements should be deposited."
	)
	private Path statsFilesPath = Paths.get("CodeGenerator/outputFiles/");

	@Option(
			names = {"--fillerCode"},
			description = "Whether to create filler code which is not a requirement.",
			negatable = true
	)
	private boolean createFillerCode = false;

	@Option(
			names = {"--fillerCodeAmount"},
			description = "How many operators the filler code should contain. Requires --fillerCode to be true to do anything."
	)
	private int fillerCodeNodeCount = 100;

	@Option(
			names = {"--fillerCodePosition"},
			description = "Where the fillerCode should be. Options 'start', 'end', 'everywhere'. Default: 'end'"
	)
	private String fillerCodePosition = "end";

	@Option(
			names = {"--fillerCodeConnection"},
			description = "How the fillerCode should be connected to the requirements. Options 'none', 'input', 'output'. Default: 'none'"
	)
	private String fillerCodeConnection = "none";

	@Option(
			names = {"--debug"},
			description = "Whether to create debug comments.",
			negatable = true
	)
	private boolean debug = false;

	@Option(
			names = {"--k-loop"},
			description = "Specify this option with a natural number if you want the created C-Files to be executed k-times instead of in an infinite while loop. Default -1 for infinite while loop."
	)
	private int k_loop_amount = -1;


	@Option(
			names = {"--restrictToIntegerVariable"},
			description = "What variable type is allowed.",
			negatable = true
	)
	private boolean restrictToIntegerVariable = false;


	@Option(
			names = {"--no-stepLocalVariables"},
			description = "Disables local variables in the step() function. True by default.",
			negatable = true
	)
	private boolean createStepLocalVariables = true;

	@Option(
			names = {"--forbiddenDataTypeLevels"},
			split = ",",
			description = "Operators of that level and higher wont be chosen"
	)
	private List<Integer> forbiddenDataTypeLevels = new ArrayList<>();



	@Option(
			names = {"--dontForceBooleanForCondition"},
			description = "Whether the random code creates cases where the condition has to be a boolean operation (instead of also allowing a number).",
			negatable = true
	)
	private boolean dontForceBooleanForCondition = false;

	@Option(
			names = {"--functionizing"},
			description = "Whether to take parts of the code and outsource it in a function.",
			negatable = true
	)
	private boolean functionizeCode = false;

	@Option(
			names = {"--createDotFile"},
			description = "Whether to create a variable dependency dot file for the requirements or not.",
			negatable = true
	)
	private boolean createDotFiles = false;

	@Option(
			names = {"--priority"},
			description = "The priority we assign to the created requirements. The priority changes the order of the requirements in the step function. Higher value is higher priority.",
			defaultValue = "-1"
	)
	private int priority = -1;

	@Option(
			names = {"--no-useTernaryForITOperator"},
			description = "Whether to use the ternary operator when representing the it-operator in a boolean context. If false something like (!(a) || (b)) is used instead.",
			negatable = true
	)
	private boolean useTernaryForITOperator = true;

	@Option(
			names = {"--no-useFloats"},
			description = "Whether to use floating point variables in the random code generation."
	)
	private boolean useFloats = true;

	@Option(
			names = {"--splitCount"},
			description = "Into how many requirements the random code should be divided."
	)
	private int splitRandomCodeIntoRequirementsNumber = 10;

	@Option(
			names = {"--generateAsManyAsNeeded"},
			description = "Generates as many requirements as required by the requested operator count."
	)
	private boolean generateAsManyRequirementsAsNeeded = false;

	@Option(
			names = {"--no-useProbabilityMap"},
			description = "Whether to use the operator probability map ini file. True by default.",
			negatable = true
	)
	private boolean useProbabilityMap = true;

	private EnumMap<Operators, Integer> mOperatorsToProbability = getDefaultOperatorProbability();

	@Option(
			names = {"--outputDir", "-o"},
			description = "Directory where the generated C code files will be created."
	)
	private Path outputDirectoryCCode = Paths.get("CodeGenerator/outputFiles/");

	@Option(
			names = {"-c", "--configFile"},
			description = "File containing the probability configuration for the operators."
	)
	private String operatorProbabilitiesConfigFile = "Resources/probabilityMaps/defaultOperatorProbabilities.ini";

	@Option(
			names = {"--no-includeModelCheckingHarness"},
			description = "Generate Code that is directly compilable and executable.",
			negatable = true
	)
	private boolean includeModelCheckingHarness = true;

	@Option(
			names = {"--reduce-console-output"},
			description = "Disables some outputs and logger output in the console"
	)
	private boolean reduceConsoleOutput = false;

	@Option(
			names = {"--seed"},
			description = "Seed to use in RandomGen"
	)
	private int seed = -1;

	@Option(
			names = {"--disableFileCreation"},
			description = "Disables the creation of the C files. Used mainly for tests.",
			negatable = true
	)
	private boolean disableFileCreation = false;

	public CodePropertiesConfig() {	}

	public CodePropertiesConfig(String configFile) {
		updateProbabilityMap(configFile);
		randomRequirementCodeArguments = new RandomRequirementCodeArguments();
	}

	public void printOperatorValues() {
		for (Operators op : mOperatorsToProbability.keySet()) {
			System.out.println(op.toString() + ": " + mOperatorsToProbability.get(op));
		}
	}

	public void updateProbabilityMap() {
		updateProbabilityMap(this.operatorProbabilitiesConfigFile);
	}

	public void updateProbabilityMap(String configFile) {
		if (useProbabilityMap) {
			try {
				mOperatorsToProbability = ConfigParser.parseIni(new File(configFile));
			} catch (IOException E) {
				throw new RuntimeException(E);
			}
		} else {
			mOperatorsToProbability = getDefaultOperatorProbability();
		}
	}

	public static EnumMap<Operators, Integer> getDefaultOperatorProbability() {
		return new EnumMap<>(Map.ofEntries(
				Map.entry(Operators.ABS, 25),
				Map.entry(Operators.MAXIMUM, 50),
				Map.entry(Operators.MINIMUM, 50),

				Map.entry(Operators.PLUS, 100),
				Map.entry(Operators.MINUS_BINARY, 100),
				Map.entry(Operators.MINUS_UNARY, 100),
				Map.entry(Operators.TIMES, 100),
				Map.entry(Operators.DIVISION, 100),
				Map.entry(Operators.MODULO, 25),

				Map.entry(Operators.BIT_SHIFT_LEFT , 50),
				Map.entry(Operators.BIT_SHIFT_RIGHT , 20),
				Map.entry(Operators.BITWISE_NOT , 20),
				Map.entry(Operators.BITWISE_AND , 20),
				Map.entry(Operators.BITWISE_OR , 20),
				Map.entry(Operators.BITWISE_XOR , 20),

				Map.entry(Operators.AND, 100),
				Map.entry(Operators.OR, 100),
				Map.entry(Operators.NOT, 100),

				Map.entry(Operators.EQUALS, 100),
				Map.entry(Operators.GREATER_EQUALS, 250),
				Map.entry(Operators.GREATER, 250),
				Map.entry(Operators.NOT_EQUALS, 100),
				Map.entry(Operators.SMALLER_EQUALS, 250),
				Map.entry(Operators.SMALLER, 250),

				Map.entry(Operators.CONCATENATION, 100),
				Map.entry(Operators.IT, 100),
				Map.entry(Operators.ITE, 200),
				Map.entry(Operators.EMPTY_CS, 200)
		));
	}

	/*
		Getters
	 */

	// Target platform settings
	public int getSizeOfIntInBytes() { return sizeOfIntInBytes; }

	// Random code creation
	public int getSeed() {
		return seed;
	}

	public boolean createRandomRequirementCode() {
		return randomRequirementCodeArguments.createRandomRequirementCode;
	}

	public int getRandomCodeNodeAmount() {
		return randomRequirementCodeArguments.nodeCount;
	}

	public int getSplitRandomCodeIntoRequirementsNumber() {
		return splitRandomCodeIntoRequirementsNumber;
	}

	public boolean isGenerateAsManyRequirementsAsNeeded() {
		return generateAsManyRequirementsAsNeeded;
	}

	public boolean isUseFloats() {
		return useFloats;
	}

	public boolean isDontForceBooleanForCondition() {
		return dontForceBooleanForCondition;
	}

	public boolean isUseTernaryForITOperator() {
		return useTernaryForITOperator;
	}

	public int getNewVariableChance() {
		return randomRequirementCodeArguments.newVariableChance;
	}

	public int getChanceConstantVsVariable() {
		return randomRequirementCodeArguments.chanceConstantVsVariable;
	}

	public boolean isUseFullFloatRange() {
		return randomRequirementCodeArguments.useFullFloatRange;
	}

	public boolean isUseFullIntegerRange() {
		return randomRequirementCodeArguments.useFullIntegerRange;
	}

	public boolean connectRequirements() {
		return randomRequirementCodeArguments.connectRequirements;
	}

	public boolean isConcatenateAllProperties() {
		return randomRequirementCodeArguments.concatenateAllProperties;
	}

	public List<Integer> getForbiddenDataTypeLevels() {
		return forbiddenDataTypeLevels;
	}

	public int getK_loop_amount() {
		// How many times the main loop is executed, if 0 it is a while(true)
		return k_loop_amount;
	}

	// Operators / probability map
	public boolean isUseProbabilityMap() {
		return useProbabilityMap;
	}

	public EnumMap<Operators, Integer> getOperatorsToProbability() {
		return mOperatorsToProbability;
	}

	public String getConfigDirectory() {
		return operatorProbabilitiesConfigFile;
	}

	// File management
	public Path getOutputDirectoryCCode() {
		return outputDirectoryCCode;
	}

	public boolean isCreateDotFiles() {
		return createDotFiles;
	}

	public boolean isCreateFiles () {
		return ! disableFileCreation;
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	// Stats File
	public boolean createStatsFiles() {
		return statsFilesPath != null;
	}

	public Path getStatsFilesPath() {
		return statsFilesPath;
	}

	// Template management
	public boolean isCreateTemplates() {
		return templateArguments.createTemplates;
	}

	public List<File> getInputTemplates() {
		return templateArguments.inputTemplates;
	}

	public boolean isCreateCodeFromTemplates() {
		return createCodeFromTemplates;
	}

	public boolean getDirectlyUseTemplates() {
		return directlyUseTemplates;
	}

	public File getOutputTemplate() {
		return templateArguments.outputTemplate;
	}

	public boolean isDontUseTemplateVariables() {
		return dontUseTemplateVariables;
	}

	// Module Templates
	public boolean useModuleTemplates() {
		return moduleTemplateArguments != null;
	}

	public Path getModuleTemplatePath() {
		return moduleTemplateArguments.moduleTemplatePath;
	}

	// Postprocessing
	public boolean createStepLocalVariables() {
		return createStepLocalVariables;
	}

	public boolean functionizeCode() {
		return functionizeCode;
	}

	public boolean isCreateFillerCode() {
		return createFillerCode;
	}

	public boolean isRestrictToIntegerVariable() {
		return restrictToIntegerVariable;
	}

	public boolean isIncludeModelCheckingHarness() {
		// Renames the files with ..._no_mc
		return includeModelCheckingHarness;
	}

	public int getPriority() {
		return priority;
	}


	// Wrapper stuff
	public boolean useWrapperArrays() {
		return wrapperArrayArguments != null;
	}

	public int getWrapperArraysPercentage() {
		return wrapperArrayArguments.percentage;
	}

	public boolean useWrapperStructs() {
		return wrapperStructArguments != null;
	}

	public int getWrapperStructsAmount() {
		return wrapperStructArguments.amount;
	}

	public int getWrapperStructsPercentage() {
		return wrapperStructArguments.percentage;
	}

	public boolean useWrapperUnions() {
		return wrapperUnionArguments != null;
	}

	public int getWrapperUnionsPercentage() {
		return wrapperUnionArguments.percentage;
	}

	public int getWrapperUnionsAdditionalPercentage() {
		return wrapperUnionArguments.additionalPercentage;
	}

	public boolean useWrapperPointers() {
		return wrapperPointerArguments != null;
	}

	public boolean isWrapperPointersUseVoidType() {
		return useWrapperPointers() && wrapperPointerArguments.useVoidType;
	}

	public int getWrapperPointersPercentage() {
		return wrapperPointerArguments.percentage;
	}

	// Debug comments / console output
	public boolean debug() {
		return debug;
	}

	public boolean getReduceConsoleOutput() {
		return reduceConsoleOutput;
	}

	public int getFillerCodeNodeCount() {
		return fillerCodeNodeCount;
	}

	public String getFillerCodePosition() {
		return fillerCodePosition;
	}

	public String getFillerCodeConnection() {
		return fillerCodeConnection;
	}

	public boolean isYmlForPreprocessedCode() {
		return ymlForPreprocessedCode;
	}
}
