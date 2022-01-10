package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator;


import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.ccreation.CFileContent;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.ccreation.CFileCreator;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.statsgen.RequirementStats;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.statsgen.StatsGenerator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.RequirementPackageOptions;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.SimpleVariableController;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ItOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.IteOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static picocli.CommandLine.ParameterException;


/**
 * Acts as the main class which coordinates all other classes.
 */

public class RequirementToC {


	/**
	 * The original requirement requirements parsed into java objects .
	 * Hand over this List to a class which permutes them and concatenates the input/output variables.
	 */
	private static final List<Requirement> matchTemplateRequirements = new ArrayList<>();
	private static final List<TemplateInfo> templatesList = new ArrayList<>();


	public static void main(String[] args) {

		CodePropertiesConfig config = new CodePropertiesConfig();
		try {
			// Handling command line arguments with imported package picocli.CommandLine.*
			CommandLine.ParseResult parseResult = new CommandLine(config).parseArgs(args);
			if (!CommandLine.printHelpIfRequested(parseResult)) {
				SimpleVariableController.varCount = 1;

				// Set properties for target architecture
				DataType.SizeOfIntInBytes = config.getSizeOfIntInBytes();

				if (config.getSeed() > -1) RandomGenHelper.randomize(config.getSeed());

				config.updateProbabilityMap();
				config.printOperatorValues();

				if (config.getReduceConsoleOutput()) {
					Configurator.setRootLevel(Level.OFF);
				}

				// Set options for requirement package
				if (!config.debug()) {
					RequirementPackageOptions.debugComments(false);
				}

				if (!config.getFileSuffix().isEmpty() && config.getSeed() == -1) {
					String seed = config.getFileSuffix();
					RandomGenHelper.randomize(seed.hashCode());
				}

				if (!config.isUseTernaryForITOperator()) {
					ItOperator.setUseTernaryOperator(false);
					IteOperator.setUseTernaryOperator(false);
				}

				// Parse the requirement templates txt into Requirement objects
				RequirementSerializer reader = new RequirementSerializer();

				if (config.isCreateCodeFromTemplates()) {
					for (File templateFile : config.getInputTemplates()) {
						templatesList.add(reader.readTemplates(templateFile));
					}
				}

				StringBuilder requirementNames = new StringBuilder();
				int noOfRequirements = 0;
				for (TemplateInfo templateInfo : templatesList) {
					for (Requirement req : templateInfo.getRequirements()) {
						noOfRequirements++;
						requirementNames.append(" '");
						requirementNames.append(req.getName());
						requirementNames.append("'");
					}
				}
				System.out.println("\nParsed " + noOfRequirements + " requirement templates:" + requirementNames + "\n");


				// Create the CFileContent
				Set<Requirement> allRequirements = new HashSet<>();
				ContentCreator contentCreator = new ContentCreator();
				IDataTypeContext dataTypeContext = new DataTypeContext();

				CFileContent content = contentCreator.createCFileContent(config, templatesList, matchTemplateRequirements, dataTypeContext);
				content.sortRequirements();
				System.out.println("Total amount of variables: " + content.getTotalNumberOfVariables() + "\n");

				// Create the C code and dot file
				CFileCreator creator = new CFileCreator(config);

				creator.createCCode(content);
				if (config.isCreateDotFiles()) {
					try {
						creator.createDotFile(content);
					} catch (IOException e) {
						System.out.println("Error creating the dot file: " + e.getClass().getCanonicalName() + " - " + e.getLocalizedMessage());
					}
				}

				// Create Stats
				if (config.createStatsFiles()) {
					StatsGenerator statsGenerator = new StatsGenerator(config.getStatsFilesPath().resolve("stats/"), config.getFileSuffix(), content.getProgramContext());
					List<Requirement> requirements = config.isConcatenateAllProperties() && content.getRequirements().size() > 1 ? Requirement.concatenateAll(new ArrayList<>(content.getRequirements())) : new ArrayList<>(content.getRequirements());
					Map<Requirement, RequirementStats> stats = statsGenerator.generateStats(requirements);
					for (Requirement req : requirements) {
						if (!config.getReduceConsoleOutput()) System.out.println("Requirement " + req.getName() + " Operator Amount: " + stats.get(req).getOperatorAmount());
					}
				}

				// Serialize if needed
				if (config.isCreateTemplates()) {
					TemplateInfo info = new TemplateInfo(content.getProperties(), content.getRequirements(), content.getFunctions(), content.getCodeObjects(), content.getAfterWhichRequirementToPutTheCodeObject(), content.getProgramContext());
					reader.writeTemplates(info, config.getOutputTemplate());
				}
			}
		} catch (ParameterException ex) { // command line arguments could not be parsed
			System.err.println(ex.getMessage());
			ex.getCommandLine().usage(System.err);
		}

		System.out.println("Random call count: " + RandomGenHelper.getCurrentCallCount());
	}
}
