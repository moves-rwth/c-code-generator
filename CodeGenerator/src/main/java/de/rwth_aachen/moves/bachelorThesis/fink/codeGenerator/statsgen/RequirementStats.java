package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.statsgen;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;

public class RequirementStats {

	String requirementName = "";

	int reqTreeNodesAmount = 0;

	int cyclomaticComplexity = 0;

	int constantsAmount;
	int floatConstantAmount;
	int charConstantAmount;
	int intConstantAmount;

	int variableCallAmount = 0;
	int variableAmount = 0;
	int outputNodesAmount = 0;
	int floatVariableAmount = 0;
	int integerVariableAmount = 0;

	int additionSubtractionAmountInteger = 0;
	int divisionAmountInteger = 0;
	int multiplicationAmountInteger = 0;
	int bitwiseOperationsAmountInteger = 0;
	int pointerDereferencesAmountInteger = 0;
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
	int operatorAmount = 0;


	public RequirementStats() {
	}

	public int getOperatorAmount() {
		return operatorAmount;
	}

	public int getOperatorAmountInteger() {
		int total = additionSubtractionAmountInteger + divisionAmountInteger + multiplicationAmountInteger + bitwiseOperationsAmountInteger + pointerDereferencesAmountInteger + comparisonOperationsAmountInteger + logicOperationsAmountInteger + absOperationAmountInteger;
		return total;
	}

	public int getOperatorAmountFloat() {
		int total = additionSubtractionAmountFloat + multiplicationAmountFloat + divisionAmountFloat + bitwiseOperationsAmountFloat + comparisonOperationsAmountFloat + logicOperationsAmountFloat + absOperationAmountFloat;
		return total;
	}


	public RequirementStats(String requirementName, int cyclomaticComplexity, int reqTreeNodesAmount, int constantsAmount, int floatConstantAmount, int charConstantAmount, int intConstantAmount, int variableCallAmount, int variableAmount, int outputNodesAmount, int floatVariableAmount, int integerVariableAmount, int additionSubtractionAmountInteger, int divisionAmountInteger, int multiplicationAmountInteger, int bitwiseOperationsAmountInteger, int pointerDereferencesAmountInteger, int comparisonOperationsAmountInteger, int logicOperationsAmountInteger, int absOperationAmountInteger, int additionSubtractionAmountFloat, int multiplicationAmountFloat, int divisionAmountFloat, int bitwiseOperationsAmountFloat, int comparisonOperationsAmountFloat, int logicOperationsAmountFloat, int absOperationAmountFloat, int functionCallAmount, int operatorAmount) {
		this.requirementName = requirementName;
		this.cyclomaticComplexity = cyclomaticComplexity;
		this.reqTreeNodesAmount = reqTreeNodesAmount;
		this.constantsAmount = constantsAmount;
		this.floatConstantAmount = floatConstantAmount;
		this.charConstantAmount = charConstantAmount;
		this.intConstantAmount = intConstantAmount;
		this.variableCallAmount = variableCallAmount;
		this.variableAmount = variableAmount;
		this.outputNodesAmount = outputNodesAmount;
		this.floatVariableAmount = floatVariableAmount;
		this.integerVariableAmount = integerVariableAmount;
		this.additionSubtractionAmountInteger = additionSubtractionAmountInteger;
		this.divisionAmountInteger = divisionAmountInteger;
		this.multiplicationAmountInteger = multiplicationAmountInteger;
		this.bitwiseOperationsAmountInteger = bitwiseOperationsAmountInteger;
		this.pointerDereferencesAmountInteger = pointerDereferencesAmountInteger;
		this.comparisonOperationsAmountInteger = comparisonOperationsAmountInteger;
		this.logicOperationsAmountInteger = logicOperationsAmountInteger;
		this.absOperationAmountInteger = absOperationAmountInteger;
		this.additionSubtractionAmountFloat = additionSubtractionAmountFloat;
		this.multiplicationAmountFloat = multiplicationAmountFloat;
		this.divisionAmountFloat = divisionAmountFloat;
		this.bitwiseOperationsAmountFloat = bitwiseOperationsAmountFloat;
		this.comparisonOperationsAmountFloat = comparisonOperationsAmountFloat;
		this.logicOperationsAmountFloat = logicOperationsAmountFloat;
		this.absOperationAmountFloat = absOperationAmountFloat;
		this.functionCallAmount = functionCallAmount;
		this.operatorAmount = operatorAmount;
	}

	public void setRequirementName(String requirementName) {
		this.requirementName = requirementName;
	}

	public void setReqTreeNodesAmount(int reqTreeNodesAmount) {
		this.reqTreeNodesAmount = reqTreeNodesAmount;
	}

	public void setConstantsAmount(int constantsAmount) {
		this.constantsAmount = constantsAmount;
	}

	public void setFloatConstantAmount(int floatConstantAmount) {
		this.floatConstantAmount = floatConstantAmount;
	}

	public void setCharConstantAmount(int charConstantAmount) {
		this.charConstantAmount = charConstantAmount;
	}

	public void setIntConstantAmount(int intConstantAmount) {
		this.intConstantAmount = intConstantAmount;
	}

	public void setVariableCallAmount(int variableCallAmount) {
		this.variableCallAmount = variableCallAmount;
	}

	public void setVariableAmount(int variableAmount) {
		this.variableAmount = variableAmount;
	}

	public void setOutputNodesAmount(int outputNodesAmount) {
		this.outputNodesAmount = outputNodesAmount;
	}

	public void setFloatVariableAmount(int floatVariableAmount) {
		this.floatVariableAmount = floatVariableAmount;
	}

	public void setIntegerVariableAmount(int integerVariableAmount) {
		this.integerVariableAmount = integerVariableAmount;
	}

	public void setAdditionSubtractionAmountInteger(int additionSubtractionAmountInteger) {
		this.additionSubtractionAmountInteger = additionSubtractionAmountInteger;
	}

	public void setDivisionAmountInteger(int divisionAmountInteger) {
		this.divisionAmountInteger = divisionAmountInteger;
	}

	public void setMultiplicationAmountInteger(int multiplicationAmountInteger) {
		this.multiplicationAmountInteger = multiplicationAmountInteger;
	}

	public void setBitwiseOperationsAmountInteger(int bitwiseOperationsAmountInteger) {
		this.bitwiseOperationsAmountInteger = bitwiseOperationsAmountInteger;
	}

	public void setPointerDereferencesAmountInteger(int pointerDereferencesAmountInteger) {
		this.pointerDereferencesAmountInteger = pointerDereferencesAmountInteger;
	}

	public void setComparisonOperationsAmountInteger(int comparisonOperationsAmountInteger) {
		this.comparisonOperationsAmountInteger = comparisonOperationsAmountInteger;
	}

	public void setLogicOperationsAmountInteger(int logicOperationsAmountInteger) {
		this.logicOperationsAmountInteger = logicOperationsAmountInteger;
	}

	public void setAbsOperationAmountInteger(int absOperationAmountInteger) {
		this.absOperationAmountInteger = absOperationAmountInteger;
	}

	public void setAdditionSubtractionAmountFloat(int additionSubtractionAmountFloat) {
		this.additionSubtractionAmountFloat = additionSubtractionAmountFloat;
	}

	public void setMultiplicationAmountFloat(int multiplicationAmountFloat) {
		this.multiplicationAmountFloat = multiplicationAmountFloat;
	}

	public void setDivisionAmountFloat(int divisionAmountFloat) {
		this.divisionAmountFloat = divisionAmountFloat;
	}

	public void setBitwiseOperationsAmountFloat(int bitwiseOperationsAmountFloat) {
		this.bitwiseOperationsAmountFloat = bitwiseOperationsAmountFloat;
	}

	public void setComparisonOperationsAmountFloat(int comparisonOperationsAmountFloat) {
		this.comparisonOperationsAmountFloat = comparisonOperationsAmountFloat;
	}

	public void setLogicOperationsAmountFloat(int logicOperationsAmountFloat) {
		this.logicOperationsAmountFloat = logicOperationsAmountFloat;
	}

	public void setAbsOperationAmountFloat(int absOperationAmountFloat) {
		this.absOperationAmountFloat = absOperationAmountFloat;
	}

	public void setFunctionCallAmount(int functionCallAmount) {
		this.functionCallAmount = functionCallAmount;
	}


	public void writeToJsonFile(Path outputDir) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
			objectMapper.writeValue(outputDir.resolve(requirementName + "Stats.json").toFile(), this);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}


	public JSONObject writeToJsonObject() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		try {
			return new JSONObject(objectMapper.writeValueAsString(this));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}


	public static <T> T mergeObjects(T first, T second) {
		Class<?> clas = first.getClass();
		Field[] fields = clas.getDeclaredFields();
		Object result = null;
		try {
			result = clas.getDeclaredConstructor().newInstance();
			for (Field field : fields) {
				field.setAccessible(true);
				Object value1 = field.get(first);
				Object value2 = field.get(second);
				Object value = (value1 instanceof Integer) ? (int) value1 + (int) value2 : value1;
				field.set(result, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (T) result;
	}

	public int getReqTreeNodesAmount() {
		return reqTreeNodesAmount;
	}
}
