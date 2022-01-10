package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.statsgen;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IntegerVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FunctionCallTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsGenerator {

	private final Path outputDir;
	private final String fileSuffix;
	private final IProgramContext programContext;

	public StatsGenerator(Path outputDir, String fileSuffix, IProgramContext programContext) {
		this.outputDir = outputDir;
		this.fileSuffix = fileSuffix;
		this.programContext = programContext;
	}


	public Map<Requirement, RequirementStats> generateStats(List<Requirement> requirements) {
		Map<Requirement, RequirementStats> stats_list = new HashMap<>();
		JSONArray obj = new JSONArray();
		try {

			Files.createDirectories(outputDir.toAbsolutePath());


			BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir.resolve(fileSuffix + "Stats.json").toFile()));

			for (Requirement req : requirements) {
				RequirementStats stats = generateStats(req);
				stats_list.put(req, stats);
				JSONObject jsonString = stats.writeToJsonObject();
				obj.put(jsonString);
			}

			writer.write(obj.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stats_list;

	}


	public RequirementStats generateStats(Requirement requirement) {
		IVariableCollector collector = new VariableCollector(false, programContext);
		requirement.accept(collector);

		CFGGen cfgGen = new CFGGen();
		DefaultDirectedGraph<String, DefaultEdge> cfg = cfgGen.createCFG(requirement);
		int cyclomaticComplexity = cfg.edgeSet().size() - cfg.vertexSet().size() + 2;

		String requirementName = requirement.getName() + ".yml";

		int reqTreeNodesAmount = collector.getReqTreeNodes().size();

		int constantsAmount = collector.getConstantNodes().size();
		int floatConstantAmount = collector.getFloatingPointConstants().size();
		int charConstantAmount = collector.getBooleanConstants().size();
		int intConstantAmount = collector.getIntegerConstants().size();

		int variableCallAmount = collector.getVariableTreeNodes().size();
		int variableAmount = collector.getUsedVariables().size();
		int outputNodesAmount = collector.getOutputVariableNodes().size();
		int floatVariableAmount = 0;
		int integerVariableAmount = 0;
		for (IVariable var : collector.getUsedVariables()) {
			if (var.getDataType().isFloatingPoint()) {
				floatVariableAmount++;
			} else if (var instanceof IntegerVariable) {
				integerVariableAmount++;
			}
		}

		int operatorAmount = 0;

		int additionSubtractionAmountInteger = 0;
		int divisionAmountInteger = 0;
		int multiplicationAmountInteger = 0;
		int bitwiseOperationsAmountInteger = 0;
		int pointerDereferencesAmount = 0;
		int comparisonOperationsAmountInteger = 0;
		int logicOperationsAmountInteger = 0;
		int absOperationAmountInteger = 0;

		int additionSubtractionAmountFloat = 0;
		int multiplicationAmountFloat = 0;
		int divisionAmountFloat = 0;
		int bitwiseOperationsAmountFloat = 0;
		int comparisonOperationsAmountFloat = 0;
		int logicOperationsAmountFloat = 0;
		int absOperationAmountFloat = 0;

		int functionCallAmount = 0;
		for (CodeTreeNode node : collector.getReqTreeNodes()) {
			if (node instanceof OperatorTreeNode) {
				if (!(node instanceof ConcatenationOperator || node instanceof OutputOperator || node instanceof EmptyControlStructure || node instanceof DeclarationOperator || node instanceof LastOperator || node instanceof LastIOperator)) {
					operatorAmount++;
				}
				boolean hasFloatChild = ((OperatorTreeNode) node).hasFloatSubtree(programContext);
				switch (Operators.getOperatorByString(((OperatorTreeNode) node).getName())) {
					case MINUS_BINARY:
					case MINUS_UNARY:
					case PLUS:
						if (hasFloatChild) {
							additionSubtractionAmountFloat++;
						} else {
							additionSubtractionAmountInteger++;
						}
						break;
					case TIMES:
						if (hasFloatChild) {
							multiplicationAmountFloat++;
						} else {
							multiplicationAmountInteger++;
						}
						break;
					case DIVISION:
					case MODULO:
						if (hasFloatChild) {
							divisionAmountFloat++;
						} else {
							divisionAmountInteger++;
						}
						break;
					case BITWISE_XOR:
					case BITWISE_NOT:
					case BITWISE_AND:
					case BITWISE_OR:
					case BIT_SHIFT_LEFT:
					case BIT_SHIFT_RIGHT:
						if (hasFloatChild) {
							bitwiseOperationsAmountFloat++;
						} else {
							bitwiseOperationsAmountInteger++;
						}
						break;
					case DEREFERENCE:
						pointerDereferencesAmount++;
						break;
					case GREATER:
					case GREATER_EQUALS:
					case SMALLER:
					case SMALLER_EQUALS:
					case EQUALS:
					case NOT_EQUALS:
					case MAXIMUM:
					case MINIMUM:
						if (hasFloatChild) {
							comparisonOperationsAmountFloat++;
						} else {
							comparisonOperationsAmountInteger++;
						}
						break;
					case AND:
					case OR:
					case NOT:
						if (hasFloatChild) {
							logicOperationsAmountFloat++;
						} else {
							logicOperationsAmountInteger++;
						}
						break;
					case ABS:
						if (hasFloatChild) {
							absOperationAmountFloat++;
						} else {
							absOperationAmountInteger++;
						}
						break;
					case CONCATENATION:
					case IT:
					case ITE:
					case EMPTY_CS:
					case LAST:
					case ASSIGNMENT:
					case OUTPUT:
					case DECL:
					case CAST:
						break;
					default:
						System.out.println(Operators.getOperatorByString(((OperatorTreeNode) node).getName()) + " not handled!");
						break;
				}

			} else if (node instanceof FunctionCallTreeNode) {
				functionCallAmount++;
			}
		}


		RequirementStats requirementStats = new RequirementStats(requirementName, cyclomaticComplexity, reqTreeNodesAmount, constantsAmount, floatConstantAmount, charConstantAmount, intConstantAmount, variableCallAmount, variableAmount, outputNodesAmount, floatVariableAmount, integerVariableAmount, additionSubtractionAmountInteger, divisionAmountInteger, multiplicationAmountInteger, bitwiseOperationsAmountInteger, pointerDereferencesAmount, comparisonOperationsAmountInteger, logicOperationsAmountInteger, absOperationAmountInteger, additionSubtractionAmountFloat, multiplicationAmountFloat, divisionAmountFloat, bitwiseOperationsAmountFloat, comparisonOperationsAmountFloat, logicOperationsAmountFloat, absOperationAmountFloat, functionCallAmount, operatorAmount);

		return requirementStats;

	}
}
