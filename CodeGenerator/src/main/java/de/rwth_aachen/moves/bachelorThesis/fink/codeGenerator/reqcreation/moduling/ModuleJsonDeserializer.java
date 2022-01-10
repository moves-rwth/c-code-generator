package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.moduling;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.rwth_aachen.moves.bachelorThesis.fink.codeParser.Parser;
import de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleFloatingPointRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.range.SimpleIntegerRange;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.DeclarationOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IFloatingPointVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IIntegerVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ModuleJsonDeserializer extends StdDeserializer<ModuleTemplate> {
	public ModuleJsonDeserializer() {
		this(null);
	}

	public ModuleJsonDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public ModuleTemplate deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {
		});
		ObjectCodec codec = parser.getCodec();
		JsonNode node = null;
		node = codec.readTree(parser);

		Path cFilePath = Path.of(node.get("c-file").asText());
		String cFileAsString = Files.readString(cFilePath);
		Parser.TreeAndContextPair pair = Parser.parseStringToDeclarationList(cFileAsString);
		assert(pair != null && pair.tree instanceof DeclarationList);
		DeclarationList declarationList = (DeclarationList) pair.tree;

		FormulaTreeNode requirement = null;
		Map<String, FormulaTreeNode> localProperties = new HashMap<>();
		Map<String, FormulaTreeNode> globalProperties = new HashMap<>();

		for (ITree element : declarationList.getElements()) {
			switch (((SectionMarker) element).getSectionName()) {
				case "CODE":
					requirement = ((FunctionDeclaration) ((DeclarationList) ((SectionMarker) element).getChild()).getElements().get(0)).getBody();
					System.out.println(requirement.toCode(CodeTreeNode.CodeType.EXECUTION, Set.of(), pair.programContext));
					break;
				case "LOCAL_PROPERTIES":
					FormulaTreeNode property_node_local = ((GlobalVariableDeclaration) ((DeclarationList) ((SectionMarker) element).getChild()).getElements().get(0)).getDeclarations().getChildren().get(1);
					VariableTreeNode variable_node_local = (VariableTreeNode) ((GlobalVariableDeclaration) ((DeclarationList) ((SectionMarker) element).getChild()).getElements().get(0)).getDeclarations().getChildren().get(0);
					localProperties.put(variable_node_local.getVariableWithAccessor().getName(), property_node_local);
					System.out.println(property_node_local.toCode(CodeTreeNode.CodeType.EXECUTION, Set.of(), pair.programContext));
					break;
				case "GLOBAL_PROPERTIES":
					FormulaTreeNode property_node_global = ((GlobalVariableDeclaration) ((DeclarationList) ((SectionMarker) element).getChild()).getElements().get(0)).getDeclarations().getChildren().get(1);
					VariableTreeNode variable_node_global = (VariableTreeNode) ((GlobalVariableDeclaration) ((DeclarationList) ((SectionMarker) element).getChild()).getElements().get(0)).getDeclarations().getChildren().get(0);
					globalProperties.put(variable_node_global.getVariableWithAccessor().getName(), property_node_global);
					System.out.println(property_node_global.toCode(CodeTreeNode.CodeType.EXECUTION, Set.of(), pair.programContext));
					break;
				default:
					break;
			}
		}

		assert (requirement != null);

		// NOTE(Felix): Set Min/Max and init values of variables correctly
		for (ITree sectionElement : ((DeclarationList) pair.tree).getElements()) {
			assert(sectionElement instanceof SectionMarker);
			SectionMarker section = (SectionMarker)sectionElement;
			String sectionName = section.getSectionName();
			if (sectionName.equals("SIGNAL_INPUT") ||
				sectionName.equals("INTERNAL_VARS") ||
				sectionName.equals("CALIBRATABLES")) {
				for (ITree declarationElement : ((DeclarationList)section.getChild()).getElements()) {
					DeclarationOperator declarationNode = (DeclarationOperator) ((GlobalVariableDeclaration)declarationElement).getDeclarations();
					Variable variableToEdit = (Variable) ((VariableTreeNode)declarationNode.getChildren().get(0)).getVariableWithAccessor().getVariable();

					// Fallback / default values
					DataType type = variableToEdit.getDataType();
					BigDecimal lowerLimitFloatingPoint = BigDecimal.valueOf(type.getLowerLimitFp());
					BigDecimal upperLimitFloatingPoint = BigDecimal.valueOf(type.getUpperLimitFp());
					long lowerLimitInteger = type.getLowerLimit();
					long upperLimitInteger = type.getUpperLimit();

					BigDecimal initValueFloatingPoint = BigDecimal.ZERO;
					long initValueInteger = 0;
					if (declarationNode.getChildren().size() > 1) {
						// Declaration has assignment, meaning there is an initialization constant / range
						FormulaTreeNode assignmentNode = declarationNode.getChildren().get(1);
						if (assignmentNode instanceof ISimpleValueTreeNode) {
							// Only has init value
							if (variableToEdit instanceof IIntegerVariable) {
								initValueInteger = ((SimpleIntegerValueTreeNode)assignmentNode).getValue();
							} else if (variableToEdit instanceof IFloatingPointVariable) {
								initValueFloatingPoint = BigDecimal.valueOf(((SimpleFloatingPointValueTreeNode)assignmentNode).getValue());
							} else {
								throw new RuntimeException("Cannot determine variable type and therefore cannot set variable bounds");
							}
						} else if (assignmentNode instanceof FunctionCallTreeNode) {
							// Has "range(min, max)"
							FunctionCallTreeNode functionCallNode = (FunctionCallTreeNode) assignmentNode;
							assert(functionCallNode.getFunctionName().equals("range")); // Might want to support more function in the future, this is it for now
							assert(functionCallNode.getParameters().size() == 2); // Min and max (inclusive)

							FormulaTreeNode minValueNode = functionCallNode.getParameters().get(0);
							FormulaTreeNode maxValueNode = functionCallNode.getParameters().get(1);
							if (variableToEdit instanceof IIntegerVariable) {
								lowerLimitInteger = ((SimpleIntegerValueTreeNode)minValueNode).getValue();
								upperLimitInteger = ((SimpleIntegerValueTreeNode)maxValueNode).getValue();
							} else if (variableToEdit instanceof IFloatingPointVariable) {
								lowerLimitFloatingPoint = BigDecimal.valueOf(((SimpleFloatingPointValueTreeNode)minValueNode).getValue());
								upperLimitFloatingPoint = BigDecimal.valueOf(((SimpleFloatingPointValueTreeNode)maxValueNode).getValue());
							}
						} else {
							throw new RuntimeException("Unexpected assignment node");
						}
					}

					// NOTE(Felix): The programContext we get from the parser already
					// has expressionConditioners for these variable within them.
					// And if a variable has an expressionConditioner, their min/max values for the update() functions will be ignored.
					// So we have to also update the expressionConditioner of the program context accordingly.
					SimpleExpressionConditioner updatedExpressionConditioner = null;
					try {
						if (variableToEdit instanceof IIntegerVariable) {
							IIntegerVariable variableToEditAsInteger = (IIntegerVariable) variableToEdit;
							variableToEditAsInteger.setValue(initValueInteger);
							variableToEditAsInteger.setMin(lowerLimitInteger);
							variableToEditAsInteger.setMax(upperLimitInteger);
							updatedExpressionConditioner = new SimpleResultingExpressionConditionerBuilder(type)
									.setRange(new SimpleIntegerRange(Set.of(type), lowerLimitInteger, upperLimitInteger, Set.of()))
									.build();
						} else if (variableToEdit instanceof IFloatingPointVariable) {
							IFloatingPointVariable variableToEditAsFloatingPoint = (IFloatingPointVariable) variableToEdit;
							variableToEditAsFloatingPoint.setValue(initValueFloatingPoint);
							variableToEditAsFloatingPoint.setMin(lowerLimitFloatingPoint);
							variableToEditAsFloatingPoint.setMax(upperLimitFloatingPoint);
							updatedExpressionConditioner = new SimpleResultingExpressionConditionerBuilder(type)
									.setRange(new SimpleFloatingPointRange(Set.of(type), lowerLimitFloatingPoint.doubleValue(), upperLimitFloatingPoint.doubleValue(), Set.of()))
									.build();
						} else {
							throw new RuntimeException("Cannot determine variable type and therefore cannot set variable bounds");
						}
					} catch (UnsatisfiableConstraintsException error) {
						throw new RuntimeException("Min/max bounds were not well formed.");
					}
					pair.programContext.updateVariableContext(variableToEdit, updatedExpressionConditioner);
				}
			}
		}

		IVariableCollector collector = new VariableCollector(false, pair.programContext.copy());
		localProperties.values().forEach(prop -> prop.accept(collector));
		globalProperties.values().forEach(prop -> prop.accept(collector));
		requirement.accept(collector);

		IVariableCollector collector2 = new VariableCollector(false, pair.programContext.copy());
		requirement.accept(collector2);

		return new ModuleTemplate(node.get("templateName").asText(), requirement, node.get("conditionArgumentCount").asInt(), node.get("codeArgumentCount").asInt(), collector2.getReqTreeNodes().size(), localProperties, globalProperties, new ArrayList<>(collector.getUsedVariables()), pair.programContext.getCurrentlyDefinedFunctions(), pair.programContext.getCurrentlyDefinedVariables().getVariableConditioners());
	}
}
