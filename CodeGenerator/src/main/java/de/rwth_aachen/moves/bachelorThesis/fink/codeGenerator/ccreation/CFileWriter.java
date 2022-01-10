package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.ccreation;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Output-writing class which creates the final output files.
 */
public class CFileWriter {
	private final Path outputDir;

	public CFileWriter(Path outputDir) {
		this.outputDir = outputDir;
	}

	public void writeStringToCFile(String code, Property prop) throws IOException {
		Files.createDirectories(outputDir.toAbsolutePath());
		final Path filePath = outputDir.resolve(prop.getName() + ".c");
		Files.deleteIfExists(filePath);
		try {
			BufferedWriter writer = Files.newBufferedWriter(filePath);
			writer.write(code);
			writer.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	public void writeStringToYmlFile(String code, Property prop, boolean preProcessed) throws IOException {
		Files.createDirectories(outputDir.toAbsolutePath());
		final Path filePath = outputDir.resolve(prop.getName() + (preProcessed ? "PreProcessed" : "") + ".yml");
		Files.deleteIfExists(filePath);

		BufferedWriter writer = Files.newBufferedWriter(filePath);
		writer.write(code);
		writer.close();
	}

	public void writeStringToPrpFile(String code) throws IOException {
		Files.createDirectories(outputDir.toAbsolutePath());
		final Path filePath = outputDir.resolve("reach.prp");
		Files.deleteIfExists(filePath);

		BufferedWriter writer = Files.newBufferedWriter(filePath);
		writer.write(code);
		writer.close();
	}
}
