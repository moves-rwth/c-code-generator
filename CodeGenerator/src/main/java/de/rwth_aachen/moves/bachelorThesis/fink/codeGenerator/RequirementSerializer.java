package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.NonVoidFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Class used to serialize and deserialize important information about a CFileContent object. Can be used to generate templates which can be used for incremental code-generation.
 */
public class RequirementSerializer {

	public RequirementSerializer() {
	}

	public TemplateInfo readTemplates(File serializedTemplatesFile) {
		File file = serializedTemplatesFile;
		TemplateInfo templateInfo = new TemplateInfo();
		try {
			System.out.println("Reading requirement templates from " + file.getAbsolutePath() + "...");
			FileInputStream fi = new FileInputStream(file);
			ObjectInputStream oi = new ObjectInputStream(fi);

			templateInfo = (TemplateInfo) oi.readObject();

			oi.close();
			fi.close();

		} catch (FileNotFoundException e) {
			System.out.println("File '" + file.getAbsolutePath() + "' not found: " + e.getLocalizedMessage());
		} catch (EOFException e) {
			// Ignore this
		} catch (IOException e) {
			System.out.println("IOException: " + e.getClass().getCanonicalName() + " - " + e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("Templates in wrong format, classNotFound");
			e.printStackTrace();
		}

		if (templateInfo.getRequirements().isEmpty()) {
			System.out.println("There were no requirement templates in the file '" + file.getAbsolutePath() + "'!");
		}

		return templateInfo;
	}

	public void writeTemplates(TemplateInfo templateInfo, File templateFile) {
		try {

			if (templateFile.getParentFile().mkdirs()) {
				System.out.println("Created parent directory.");
			}

			if (!templateFile.delete() || !templateFile.createNewFile()) {
				System.out.println("File '" + templateFile.getAbsolutePath() + "' already exists and will be overwritten!");
			}

			FileOutputStream fileOutputStreamReq = new FileOutputStream(templateFile);
			ObjectOutputStream objectOutputStreamReq = new ObjectOutputStream(fileOutputStreamReq);

			objectOutputStreamReq.writeObject(templateInfo);

			fileOutputStreamReq.close();
			objectOutputStreamReq.close();
			System.out.println("Wrote templates to " + templateFile.getAbsolutePath() + ".");
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			throw new RuntimeException("Error initializing stream" + e);
		}
	}


	public List<Requirement> readTemplateRequirements(File serializedTemplatesFile) {
		List<Requirement> templates = new ArrayList<>();
		File file = new File(serializedTemplatesFile + "Requirements");
		try {
			System.out.println("Reading requirement templates from " + file.getAbsolutePath() + "...");
			FileInputStream fi = new FileInputStream(file);
			ObjectInputStream oi = new ObjectInputStream(fi);

			boolean requirementLeft = true;
			while (requirementLeft) {
				Requirement req = (Requirement) oi.readObject();
				if (req != null) {
					templates.add(req);
				} else {
					requirementLeft = false;
				}
			}

			oi.close();
			fi.close();

		} catch (FileNotFoundException e) {
			System.out.println("File '" + file.getAbsolutePath() + "' not found: " + e.getLocalizedMessage());
		} catch (EOFException e) {
			// Ignore this
		} catch (IOException e) {
			System.out.println("IOException: " + e.getClass().getCanonicalName() + " - " + e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("Templates in wrong format, classNotFound");
			e.printStackTrace();
		}

		if (templates.isEmpty()) {
			System.out.println("There were no requirement templates in the file '" + file.getAbsolutePath() + "'!");
		}

		return templates;
	}

	public List<IFunction> readTemplateRequirementFunctions(File serializedTemplatesFile) {
		List<IFunction> templates = new ArrayList<>();
		File file = new File(serializedTemplatesFile + "Functions");
		try {
			System.out.println("Reading requirement function templates from " + file.getAbsolutePath() + "...");
			FileInputStream fi = new FileInputStream(file);
			ObjectInputStream oi = new ObjectInputStream(fi);

			boolean functionLeft = true;
			while (functionLeft) {
				IFunction func = (IFunction) oi.readObject();
				if (func != null) {
					templates.add(func);
				} else {
					functionLeft = false;
				}
			}

			oi.close();
			fi.close();

		} catch (FileNotFoundException e) {
			System.out.println("File '" + file.getAbsolutePath() + "' not found: " + e.getLocalizedMessage());
		} catch (EOFException e) {
			// Ignore this
		} catch (IOException e) {
			System.out.println("IOException: " + e.getClass().getCanonicalName() + " - " + e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("Templates in wrong format, classNotFound");
			e.printStackTrace();
		}

		if (templates.isEmpty()) {
			System.out.println("There were no requirement templates in the file '" + file.getAbsolutePath() + "'!");
		}

		return templates;
	}

	public void writeTemplateRequirements(List<IFunction> functions, List<Requirement> requirements, File templateFile) {
		try {
			File templateFileReq = new File(templateFile + "Requirements");
			File templateFileFunc = new File(templateFile + "Functions");

			if (templateFile.getParentFile().mkdirs()) {
				System.out.println("Created parent directory.");
			}

			if (!templateFileReq.delete() || !templateFileReq.createNewFile() || !templateFileFunc.delete() || !templateFileFunc.createNewFile()) {
				System.out.println("File '" + templateFile.getAbsolutePath() + "' already exists and will be overwritten!");
			}

			FileOutputStream fileOutputStreamReq = new FileOutputStream(templateFileReq);
			ObjectOutputStream objectOutputStreamReq = new ObjectOutputStream(fileOutputStreamReq);
			FileOutputStream fileOutputStreamFunc = new FileOutputStream(templateFileFunc);
			ObjectOutputStream objectOutputStreamFunc = new ObjectOutputStream(fileOutputStreamFunc);

			for (Requirement req : requirements) {
				objectOutputStreamReq.writeObject(req);
			}

			for (IFunction func : functions) {
				objectOutputStreamFunc.writeObject(func);
			}

			fileOutputStreamReq.close();
			objectOutputStreamReq.close();
			fileOutputStreamFunc.close();
			objectOutputStreamFunc.close();
			System.out.println("Wrote templates to " + templateFile.getAbsolutePath() + ".");
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			throw new RuntimeException("Error initializing stream");
		}
	}

	public List<NonVoidFunction> readTemplateFunctions(File serializedFunctionFile) {
		List<NonVoidFunction> templateFunctions = new ArrayList<>();
		try {
			System.out.println("Reading function templates from " + serializedFunctionFile.getAbsolutePath() + "...");
			FileInputStream fi = new FileInputStream(serializedFunctionFile);
			ObjectInputStream oi = new ObjectInputStream(fi);

			boolean functionLeft = true;
			while (functionLeft) {
				NonVoidFunction function = (NonVoidFunction) oi.readObject();
				if (function != null) {
					templateFunctions.add(function);
				} else {
					functionLeft = false;
				}
			}

			oi.close();
			fi.close();

		} catch (FileNotFoundException e) {
			System.out.println("File '" + serializedFunctionFile.getAbsolutePath() + "' not found: " + e.getLocalizedMessage());
		} catch (EOFException e) {
			// Ignore this
		} catch (IOException e) {
			System.out.println("IOException: " + e.getClass().getCanonicalName() + " - " + e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("Templates in wrong format, classNotFound");
			e.printStackTrace();
		}

		if (templateFunctions.isEmpty()) {
			System.out.println("There were no function templates in the file '" + serializedFunctionFile.getAbsolutePath() + "'!");
		}

		return templateFunctions;
	}

	public void serializeFunctions(List<NonVoidFunction> functions, File templateFile) {
		try {
			if (!templateFile.getParentFile().mkdirs() || !templateFile.createNewFile()) {
				System.err.println("Failed to write function template file '" + templateFile.getAbsolutePath() + "'!");
				return;
			}

			FileOutputStream fileOutputStream = new FileOutputStream(templateFile);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

			for (NonVoidFunction function : functions) {
				objectOutputStream.writeObject(function);
			}

			fileOutputStream.close();
			objectOutputStream.close();
			System.out.println("Wrote function templates to " + templateFile.getAbsolutePath() + ".");
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		}
	}
}
