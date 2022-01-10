package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.ccreation;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.RequirementScopes;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.NonDeterministicUpdateInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.ArrayVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.PointerVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.CodeObject;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.VoidFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This class is given a CFileContent Object, from which it creates the final C-File String which it passes to the CFileWriter.
 */

public class CFileCreator {
	private final Path outputDir;
	private final boolean includeModelCheckingHarness;
	private final int k_loop_amount;
	private final CodePropertiesConfig config;
	private IProgramContext programContext;

	public CFileCreator(CodePropertiesConfig config) {
		this.outputDir = config.getOutputDirectoryCCode();
		this.includeModelCheckingHarness = config.isIncludeModelCheckingHarness();
		k_loop_amount = config.getK_loop_amount();
		this.config = config;
	}

	public void createCCode(CFileContent content) {
		CFileWriter writer = new CFileWriter(outputDir);
		System.out.println("C Code output directory: " + outputDir.toString());

		programContext = content.getProgramContext();
		List<Property> properties = config.isConcatenateAllProperties() && content.getProperties().size() > 1 ? Property.concatenateAll(new ArrayList<>(content.getProperties())) : new ArrayList<>(content.getProperties());
		for (int i = 0, size = properties.size(); i < size; ++i) {
			StringArray code = new StringArray();
			StringArray ymlCode = new StringArray();
			StringArray ymlCodePre = new StringArray();
			StringArray prpCode = new StringArray();

			Property prop = properties.get(i);
			System.out.println("Creating C-File " + (i + 1) + "/" + size + "...");

			// Used and sorted variables
			List<IVariable> sortedUsedVariables = new ArrayList<>(content.getUsedVariables());
			sortedUsedVariables.sort(IVariable.NATURAL_ORDER);

			code.add(createExternVerifierFunctions(includeModelCheckingHarness, prop.getName() + ".c"));
			code.addEmptyLine();
			code.add(createDefineOperators());
			code.addEmptyLine();
			code.add(createForwardDeclarations(content.getRequiredForwardDeclarations()));
			code.addEmptyLine();
			code.add(createTypedefs(content.getRequiredTypedefs()));
			code.addEmptyLine();
			code.add(createFunctionPrototypes(content.getFunctions()));
			code.addEmptyLine();
			code.add(createInternalVariables(sortedUsedVariables));
			code.addEmptyLine();
			code.add(createExternVariables(sortedUsedVariables, config.isWrapperPointersUseVoidType()));
			code.addEmptyLine();
			code.add(createCalibrationVariables(sortedUsedVariables));
			code.addEmptyLine();
			code.add(createLastVariablesInitialization(content.getLastVariablesAndDepths()));
			code.addEmptyLine();
			code.add(createFunctions(content.getFunctions()));
			code.add(createInitially(content.getRequirements()));

			code.add(createStep(content.getRequirements(), content.getCodeObjects(), content.getAfterWhichRequirementToPutTheCodeObject()));

			code.add(createUpdateVariables(content.getNonOutputVariables(), content.getVariableConditioners(), includeModelCheckingHarness, content.getDataTypeContext()));
			code.add(createUpdateLastVariables(content.getLastVariablesAndDepths()));
			code.add(createProperty(prop));
			code.add(createMainMethod(k_loop_amount));

			ymlCode.add(createYmlCode(false, prop));
			ymlCodePre.add(createYmlCode(true, prop));

			prpCode.add(createReachPrp());
			if (config.isCreateFiles()) {
				try {
					writer.writeStringToYmlFile(ymlCode.toString(), prop, false);
					if (config.isYmlForPreprocessedCode()) {
						writer.writeStringToYmlFile(ymlCodePre.toString(), prop, true);
					}
					writer.writeStringToCFile(code.toString(), prop);
					writer.writeStringToPrpFile(prpCode.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Code generation finished successfully!");
	}


	private StringArray createMainMethod(int k_loop_amount) {
		StringArray result = new StringArray();
		result.add("int main(void) {");
		result.addIndented("isInitial = 1;", 1);
		result.addIndented("initially();", 1);
		result.addEmptyLine();

		if (k_loop_amount >= 0) {
			result.addIndented("int k_loop;", 1);
			result.addIndented("for (k_loop = 0; k_loop < " + k_loop_amount + "; k_loop++) {", 1);
		} else {
			result.addIndented("while (1) {", 1);
		}
		result.addIndented("updateLastVariables();", 2);
		result.addEmptyLine();
		result.addIndented("updateVariables();", 2);
		result.addIndented("step();", 2);
		result.addIndented("__VERIFIER_assert(property());", 2);
		result.addIndented("isInitial = 0;", 2);
		result.addIndented("}", 1);
		result.addEmptyLine();
		result.addIndented("return 0;");
		result.add("}");

		return result;
	}

	private StringArray createLastVariablesInitialization(Map<IVariable, Long> maxLastIValues) {
		StringArray code = new StringArray();
		code.add("// Last'ed variables");

		List<IVariable> variables = new ArrayList<>(maxLastIValues.keySet());
		variables.sort(IVariable.NATURAL_ORDER);
		for (IVariable lastIVar : variables) {
			String name = lastIVar.getVariableAccessorName().replaceAll("[.\\[\\]]", "_");
			if (lastIVar.isLastIHasToBeArray()) {
				if (lastIVar.isArray()) {
					final ArrayVariable arrayVariable = (ArrayVariable) lastIVar;
					final int arraySize = arrayVariable.getArraySize();
					code.add(lastIVar.getDataType().toCTypeName() + " last_i_" + name + "[" + maxLastIValues.get(lastIVar) + "][" + arraySize + "] = {");
					for (int i = 1; i <= maxLastIValues.get(lastIVar); i++) {
						code.add("\t");
						for (int j = 0; j < arraySize; j++) {
							code.add(arrayVariable.getArrayValueAsString(j) + (((j < (arraySize - 1)) && i < (maxLastIValues.get(lastIVar) - 1)) ? ", " : ""));
						}
					}
					code.add("};");
				} else {
					code.add(lastIVar.getDataType().toCTypeName() + " " + "last_i_" + name + "[" + maxLastIValues.get(lastIVar) + "]" + " = {");
					for (int i = 1; i <= maxLastIValues.get(lastIVar); ++i) {
						code.addIndented(lastIVar.getValueAsString());
						if (i < (maxLastIValues.get(lastIVar) - 1)) {
							code.addToLastLine(", ");
						}
					}
					code.add("};");
				}
			}

			if (lastIVar.isArray()) {
				final ArrayVariable arrayVariable = (ArrayVariable) lastIVar;
				final int arraySize = arrayVariable.getArraySize();
				for (long i = 1L; i <= maxLastIValues.get(lastIVar); i++) {
					code.add(lastIVar.getDataType().toCTypeName() + " " + "last_" + i + "_" + name + "[" + arraySize + "]" + " = {");
					for (int j = 0; j < arraySize; ++j) {
						code.addIndented(arrayVariable.getArrayValueAsString(j));
						if (i < (arraySize - 1)) {
							code.addToLastLine(", ");
						}
					}
					code.add("};");
				}
			} else {
				for (long i = 1L; i <= maxLastIValues.get(lastIVar); i++) {
					code.add(lastIVar.getDataType().toCTypeName() + " " + "last_" + i + "_" + name + " = " + lastIVar.getValueAsString().toStringProperty() + ";");
				}

			}
		}

		return code;
	}

	private StringArray createForwardDeclarations(List<StringArray> declarations) {
		StringArray code = new StringArray();
		List<StringArray> declarationsSorted = new ArrayList<>(declarations);
		Collections.sort(declarationsSorted);

		for (StringArray declaration : declarationsSorted) {
			code.add(declaration);
		}

		return code;
	}

	private StringArray createTypedefs(List<StringArray> typedefs) {
		StringArray code = new StringArray();
		List<StringArray> typedefsSorted = new ArrayList<>(typedefs);
		Collections.sort(typedefsSorted);

		for (StringArray typedef : typedefsSorted) {
			if (!typedef.contains("struct")) {
				code.add(typedef);
			}
		}
		for (StringArray typedef : typedefsSorted) {
			if (typedef.contains("struct")) {
				code.add(typedef);
			}
		}

		return code;
	}

	private StringArray createUpdateLastVariables(Map<IVariable, Long> maxLastIValues) {
		StringArray code = new StringArray();
		code.addEmptyLines(2);
		code.add("void updateLastVariables() {");

		List<IVariable> variables = new ArrayList<>(maxLastIValues.keySet());
		variables.sort(IVariable.NATURAL_ORDER);
		for (IVariable lastIVar : variables) {
			final long max = maxLastIValues.get(lastIVar);
			String name = lastIVar.getVariableAccessorName().replaceAll("[.\\[\\]]", "_");
			String name2 = lastIVar.getVariableAccessorName();
			if (lastIVar.isLastIHasToBeArray()) {
				for (long i = max; i > 0L; i--) {
					code.addIndented("last_i_" + name + "[" + i + "] = last_i_" + name + "[" + (i - 1) + "] ;");
				}
				code.addIndented("last_i_" + name + "[0] = " + name2 + ";");
			}
			for (long i = max; i > 1L; i--) {
				code.addIndented("last_" + i + "_" + name + " = " + "last_" + (i - 1) + "_" + name + ";");
			}
			code.addIndented("last_1_" + name + " = " + name2 + ";");
		}

		code.add("}");
		code.addEmptyLines(1);
		return code;
	}

	private StringArray createFunctions(List<IFunction> functions) {
		StringArray code = new StringArray();
		code.add("// Additional functions");
		for (IFunction function : functions) {
			code.add(function.toCCodeString(programContext));
		}
		return code;
	}

	private StringArray createVerifierFunctions(boolean isHarness) {
		StringArray code = new StringArray();
		if (!isHarness) {
			// Linked from random.cpp, properly using the C++ <random>-implementations.
			code.add("extern signed char __VERIFIER_nondet_char(signed char x, signed char y);");
			code.add("extern unsigned char __VERIFIER_nondet_uchar(unsigned char x, unsigned char y);");
			code.add("extern signed short __VERIFIER_nondet_short(signed short x, signed short y);");
			code.add("extern unsigned short __VERIFIER_nondet_ushort(unsigned short x, unsigned short y);");
			code.add("extern signed long int __VERIFIER_nondet_long(signed long int x, signed long int y);");
			code.add("extern unsigned long int __VERIFIER_nondet_ulong(unsigned long int x, unsigned long int y);");
			code.add("extern float __VERIFIER_nondet_float(float x, float y);");
			code.add("extern double __VERIFIER_nondet_double(double x, double y);");
		}
		return code;
	}

	private void addAssumptions(StringArray code, boolean isHarness, IVariable variable, String lowerLimit, String upperLimit, Set<String> exclusions, IDataTypeContext dataTypeContext) {
		final IVariableWithAccessor variableWithAccessor = SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(variable);
		final String variableName = variableWithAccessor.getVariableAccessor();
		if (isHarness) {
			code.addIndented(variableName + " = " + variable.getDataType().getVerifierNonDetFunction(dataTypeContext) + "();");
			if (variable.getDataType().isFloatingPoint()) {
				code.addIndented("assume_abort_if_not((" + variableName + " >= " + lowerLimit + " && " + variableName + " <= -1.0e-20F) || (" + variableName + " <= " + upperLimit + " && " + variableName + " >= 1.0e-20F ));");
			} else {
				code.addIndented("assume_abort_if_not(" + variableName + " >= " + lowerLimit + ");");
				code.addIndented("assume_abort_if_not(" + variableName + " <= " + upperLimit + ");");
			}
			for (String s : exclusions) {
				code.addIndented("assume_abort_if_not(" + variableName + " != " + s + ");");
			}
		} else {
			// For execution
			StringBuilder condition = new StringBuilder();
			boolean isFirst = true;
			for (String s : exclusions) {
				if (Objects.equals(lowerLimit, s) && variable.getDataType().isInteger()) {
					final long oldLowerLimitValue = Long.parseLong(lowerLimit);
					lowerLimit = Long.toString(oldLowerLimitValue + 1);
					continue;
				}
				if (Objects.equals(upperLimit, s) && variable.getDataType().isInteger()) {
					final long oldUpperLimitValue = Long.parseLong(upperLimit);
					upperLimit = Long.toString(oldUpperLimitValue - 1);
					continue;
				}
				if (!isFirst) {
					condition.append(" || ");
				}
				condition.append("(").append(variableName).append(" == ").append(s).append(")");
				isFirst = false;
			}
			if (isFirst) {
				code.addIndented(variableName + " = " + variable.getDataType().getVerifierNonDetFunction(dataTypeContext) + "(" + lowerLimit + ", " + upperLimit + ");");
			} else {
				code.addIndented("do {", 1);
				code.addIndented(variableName + " = " + variable.getDataType().getVerifierNonDetFunction(dataTypeContext) + "(" + lowerLimit + ", " + upperLimit + ");", 2);
				code.addIndented(" } while (" + condition.toString() + ");", 1);
			}
		}
	}

	private StringArray createUpdateVariables(Set<IVariable> nonOutputVars, Map<IVariable, SimpleExpressionConditioner> variableConditioners, boolean includeModelCheckingHarness, IDataTypeContext dataTypeContext) {
		StringArray code = new StringArray();
		code.addEmptyLines(2);
		code.add("void updateVariables() {");

		List<IVariable> sortedNonOutputVariables = new ArrayList<>(nonOutputVars);
		sortedNonOutputVariables.sort(IVariable.NATURAL_ORDER);
		// This set exist so that we dont create assumptions for a variable twice if both the var and their pointer occurs in the code
		Set<IVariable> alreadyMade = new HashSet<>();
		for (IVariable variable : sortedNonOutputVariables) {
			if (variable.getParameterType() != ParameterType.SIGNAL) {
				continue;
			}

			NonDeterministicUpdateInformation info = variable.buildNonDetUpdateCode(includeModelCheckingHarness, variableConditioners);
			if (info != null) {
				if (!alreadyMade.contains(info.variable)) {
					addAssumptions(code, includeModelCheckingHarness, info.variable, info.lowerLimit, info.upperLimit, info.exclusions, dataTypeContext);
					alreadyMade.add(info.variable);
				}
			}
		}

		code.add("}");
		code.addEmptyLines(1);
		return code;
	}

	private StringArray createInitially(List<Requirement> requirements) {
		StringArray code = new StringArray();
		code.addEmptyLines(2);
		code.add("void initially(void) {");

		boolean isFirst = true;
		for (Requirement req : requirements) {
			if (req.getScope() == RequirementScopes.INITIALLY) {
				if (!isFirst) {
					code.addEmptyLines(2);
				}
				code.addIndented("// From: " + req.getName());
				code.addIndented(req.toStepCode(programContext));
				isFirst = false;
			}
		}
		code.add("}");
		code.addEmptyLines(1);
		return code;
	}

	private StringArray createStep(List<Requirement> requirements, List<CodeObject> codeObjects, Map<CodeObject, Requirement> placeOfCodeObject) {
		StringArray code = new StringArray();
		code.addEmptyLines(2);
		code.add("void step(void) {");

		boolean isFirst = true;
		List<CodeObject> nonNullLocationCodeObjects = new ArrayList<>(codeObjects);

		for (CodeObject codeObject : codeObjects) {
			if (placeOfCodeObject.get(codeObject) == null) {
				if (!isFirst) {
					code.addEmptyLines(2);
				}
				code.addIndented("// From: " + codeObject.getName());
				code.addIndented(codeObject.toCode(programContext));
				isFirst = false;
				nonNullLocationCodeObjects.remove(codeObject);
			}
		}

		for (Requirement req : requirements) {
			if (req.getScope() != RequirementScopes.INITIALLY) {
				if (!isFirst) {
					code.addEmptyLines(2);
				}
				code.addIndented("// From: " + req.getName());
				code.addIndented(req.toStepCode(programContext));
				isFirst = false;

				for (CodeObject codeObject : nonNullLocationCodeObjects) {
					if (placeOfCodeObject.get(codeObject).equals(req)) {
						code.addEmptyLines(2);
						code.addIndented("// From: " + codeObject.getName());
						code.addIndented(codeObject.toCode(programContext));
					}
				}
			}
		}
		code.add("}");
		code.addEmptyLines(1);
		return code;
	}

	private StringArray createGetLastIMethod(List<IVariable> lastIVars, Map<IVariable, Integer> maxLastIValues) {
		StringArray code = new StringArray();
		code.addEmptyLines(2);
		code.add("void last_i(char var[]) {");


		code.add("}");
		code.addEmptyLines(1);
		return code;
	}

	private StringArray createInternalVariables(List<IVariable> usedVariables) {
		StringArray result = new StringArray();
		result.add("// Internal control logic variables");

		for (IVariable variable : usedVariables) {
			if (variable.getParameterType() == ParameterType.INTERNAL_CONTROL) {
				result.add(variable.getStorageDeclaration());
			}
		}

		return result;
	}

	private StringArray createExternVariables(List<IVariable> usedVariables, boolean useVoidTypeForPointers) {
		StringArray result = new StringArray();
		result.add("// Signal variables");

		for (IVariable variable : usedVariables) {
			if ((variable.getParameterType() == ParameterType.SIGNAL) && (variable.requiresStorage())) {
				if (variable instanceof PointerVariable && useVoidTypeForPointers) {
					result.add(((PointerVariable) variable).getStorageDeclarationVoid());
				} else {
					result.add(variable.getStorageDeclaration());
				}
			}
		}

		return result;
	}

	private StringArray createCalibrationVariables(List<IVariable> usedVariables) {
		StringArray result = new StringArray();
		result.add("// Calibration values");

		for (IVariable variable: usedVariables) {
			if (variable.getParameterType() == ParameterType.CALIBRATION_VALUE) {
				// If the variable is no array
				if (variable.isArray()) {
					final ArrayVariable arrayVariable = (ArrayVariable) variable;
					final int arraySize = arrayVariable.getArraySize();
					result.add("volatile const " + variable.getDataType().toCTypeName() + " " + variable.getName() + "[" + arraySize + "]" + " = {");
					for (int i = 0; i < arraySize; ++i) {
						result.addIndented(arrayVariable.getArrayValueAsString(i));
						if (i < (arraySize - 1)) {
							result.addToLastLine(", ");
						}
					}
					result.add("};");
				} else {
					result.add("volatile const " + variable.getDataType().toCTypeName() + " " + variable.getName() + " = " + variable.getValueAsString().toStringProperty() + ";");
				}

			}
		}

		return result;
	}

	private StringArray createProperty(Property prop) {
		StringArray result = new StringArray();
		result.add("int property() {");
		result.addIndented(prop.getPropertyDebugCode(programContext));
		result.addIndented("return " + prop.toCode(programContext) + ";");
		result.add("}");
		return result;
	}


	private StringArray createExternVerifierFunctions(boolean includeModelCheckingHarness, String fileName) {
		StringArray result = new StringArray();
		if (includeModelCheckingHarness) {
			result.add("// Prototype declarations of the functions used to communicate with the model checkers");
			result.add("extern unsigned long __VERIFIER_nondet_ulong();");
			result.add("extern long __VERIFIER_nondet_long();");
			result.add("extern unsigned char __VERIFIER_nondet_uchar();");
			result.add("extern char __VERIFIER_nondet_char();");
			result.add("extern unsigned short __VERIFIER_nondet_ushort();");
			result.add("extern short __VERIFIER_nondet_short();");
			result.add("extern float __VERIFIER_nondet_float();");
			result.add("extern double __VERIFIER_nondet_double();");
			result.addEmptyLines(1);
			result.add("extern void abort(void);");
			result.add("extern void __assert_fail(const char *, const char *, unsigned int, const char *) __attribute__ ((__nothrow__ , __leaf__)) __attribute__ ((__noreturn__));");
			result.add("void reach_error() { __assert_fail(\"0\", \"" + fileName + "\", 13, \"reach_error\"); }");
			result.add("void __VERIFIER_assert(int cond) { if(!(cond)) { ERROR: {reach_error();abort();} } return; }");
			result.add("void assume_abort_if_not(int cond) { if(!cond) { abort(); } }");
		} else {
			result.add("// Required includes");
			result.add("#include <stdlib.h>");
			result.add("#include <stdio.h>");
			result.addEmptyLines(1);
			result.add("// Define implementations for the signaling functions used in the code");
			result.add("#define abort() do { printf(\"ERROR!\\n\"); exit(-1); } while (0)");
			result.add("void __VERIFIER_assert(int cond) { if(!(cond)) { ERROR: {abort();} } return; }");
			result.add(createVerifierFunctions(includeModelCheckingHarness));
		}

		result.addEmptyLines(2);

		return result;

	}

	private StringArray createDefineOperators() {
		StringArray result = new StringArray();
		result.add("#define max(a,b) (((a) > (b)) ? (a) : (b))");
		result.add("#define min(a,b) (((a) < (b)) ? (a) : (b))");
		result.add("#define abs(a) (((a) < 0 ) ? -(a) : (a))");
		result.addEmptyLines(2);

		return result;

	}

	private StringArray createFunctionPrototypes(List<IFunction> functions) {
		StringArray result = new StringArray();
		result.add("// Function prototypes");
		for (IFunction function : functions) {
			result.add(function.toCCodeFunctionPrototype());
		}
		result.addEmptyLine();
		return result;
	}

	public StringArray createYmlCode(boolean preProcess, Property prop) {
		StringArray result = new StringArray();
		result.add("format_version: '2.0'");
		result.addEmptyLines(1);
		result.add("input_files: '" + prop.getName() + (preProcess ? ".i'" : ".c'"));
		result.addEmptyLines(1);
		result.add("properties:");
		result.add("  - property_file: reach.prp");
		result.add("    expected_verdict: " + prop.isValid());
		result.addEmptyLines(1);
		result.add("options:");
		result.add("  language: C");
		result.add("  data_model: ILP32");
		return result;
	}

	public StringArray createReachPrp() {
		StringArray result = new StringArray();
		result.add("CHECK( init(main()), LTL(G ! call(reach_error())) )");
		return result;
	}

	public void createDotFile(CFileContent content) throws IOException {
		StringBuilder code = new StringBuilder();
		code.append("digraph graphname {\r\n");
		HashSet<IVariable> declaredVars = new HashSet<>();

		for (Requirement req : content.getRequirements()) {
			IVariableCollector variableCollector = new VariableCollector(false, programContext);
			req.accept(variableCollector);

			final String nodeName = req.getName().trim();
			code.append(nodeName).append(" [shape=box,label=\"").append(nodeName).append("\"];\r\n");

			for (IVariable inputVar : variableCollector.getInputVariables()) {
				final String inputVarName = inputVar.getName().trim();
				if (!declaredVars.contains(inputVar)) {
					code.append(inputVarName).append("[label=\"").append(inputVarName).append("\"]");
					declaredVars.add(inputVar);
				}
				code.append(nodeName).append(" -> ").append(inputVarName).append(";\r\n");
			}

			for (IVariable outputVar : variableCollector.getOutputVariables()) {
				final String outputVarName = outputVar.getName().trim();
				if (!declaredVars.contains(outputVar)) {
					code.append(outputVarName).append("[label=\"").append(outputVarName).append("\"]");
					declaredVars.add(outputVar);
				}
				code.append(outputVarName).append(" -> ").append(nodeName).append(";\r\n");
			}

			code.append("\r\n");
		}
		code.append("}\r\n");


		Files.createDirectories(outputDir.toAbsolutePath());
		final Path filePath = outputDir.resolve("req.dot");
		Files.deleteIfExists(filePath);

		BufferedWriter writer = Files.newBufferedWriter(filePath);
		writer.write(code.toString());
		writer.close();
	}
}









