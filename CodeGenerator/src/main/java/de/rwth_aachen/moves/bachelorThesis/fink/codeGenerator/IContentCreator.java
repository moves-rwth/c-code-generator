package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.ccreation.CFileContent;
import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.util.List;


/**
 * Class used to generate a complete CFileContent-Object using just configuration-options. This class is called directly from the main-class.
 */
public interface IContentCreator {
	/**
	 * @param config          Code generation configuration options given by command line.
	 * @param templates       A list of earlier created and deserialized Requirement objects. These can be modified with PostProcessors and their code will occur in new randomly generated requirements.
	 * @param matchTemplates  A list of earlier created and deserialized Requirement objects. The newly created requirements should be similar to these.
	 * @param dataTypeContext A DataTypeContext object (i.e. a dictionary of all defined datatypes to be used for the code-generation).
	 * @return A complete and fully functional CFileContent-Object which can be written into output-Files.
	 */
	CFileContent createCFileContent(CodePropertiesConfig config, List<TemplateInfo> templates, List<Requirement> matchTemplates, IDataTypeContext dataTypeContext);
}
