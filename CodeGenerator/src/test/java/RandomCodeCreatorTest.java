import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.randomcodecreation.RandomCodeCreator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RandomCodeCreatorTest {
	@Test
	public void testCodeGen() {
		// Disable debug logging for this test
		Configurator.setRootLevel(Level.WARN);

		List<String> paths = new ArrayList<>();
		paths.add("../Resources/probabilityMaps/operatorProbabilities.ini");
		paths.add("Resources/probabilityMaps/operatorProbabilities.ini");

		CodePropertiesConfig config = null;
		for (String p : paths) {
			if (Files.exists(Paths.get(p))) {
				config = new CodePropertiesConfig(p);
				break;
			}
		}

		if (config == null) {
			throw new RuntimeException("Failed to load operator probability maps!");
		}

		List<Requirement> finalRequirements = new ArrayList<>();
		List<Property> finalProperties = new ArrayList<>();

		IVariableController variableController = new SimpleVariableController(config.isUseFloats(), config.isDontForceBooleanForCondition());
		IMutableVariableContext variableContext = new SimpleMutableVariableContext();
		IDataTypeContext dataTypeContext = new DataTypeContext();
		IProgramContext programContext = new SimpleProgramContext(variableContext, dataTypeContext, variableController);

		for (int i = 0; i < config.getSplitRandomCodeIntoRequirementsNumber(); ++i) {
			final String requirementName = "RandomReq" + (i + 1);
			Pair<Requirement, List<Property>> codeAndProperties = RandomCodeCreator.createRequirementAndProperties(config, requirementName, 500, variableController, programContext, new HashSet<>());
			finalRequirements.add(codeAndProperties.getValue0());
			finalProperties.addAll(codeAndProperties.getValue1());
		}
	}
}
