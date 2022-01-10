package de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ArrayType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.NotApplicableExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditionerBuilder;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.RandomGenHelper;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.Variable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.OperatorReturnType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums.Operators;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.*;

import java.io.Serializable;
import java.util.*;

public class SimpleVariableController implements IVariableController, Serializable {

	private final boolean floatsAllowed;
	private final boolean dontForceBooleanForCondition;
	public static int varCount = 0;

	public SimpleVariableController(boolean floatsAllowed, boolean dontForceBooleanForCondition) {
		this.floatsAllowed = floatsAllowed;
		this.dontForceBooleanForCondition = dontForceBooleanForCondition;
	}

	@Override
	public IVariableController copy() {
		return new SimpleVariableController(floatsAllowed, dontForceBooleanForCondition);
	}

	@Override
	public IVariable createVariable(SimpleExpressionConditioner expressionConditioner, IProgramContext programContext) {
		final Set<DataType> possibleReturnTypes = expressionConditioner.getPossibleReturnTypes();
		final ImmutableList<ImmutableSet<IVariable>> definedVariables = programContext.getCurrentlyDefinedGlobalVariables();
		boolean foundName = false;
		String variableName = "var_" + definedVariables.size() + "_" + varCount;
		while (!foundName) {
			variableName = "var_" + definedVariables.size() + "_" + varCount;
			foundName = true;
			for (IVariable var : programContext.getCurrentlyDefinedVariables().getVariableConditioners().keySet()) {
				if (var.getName().equals(variableName)) {
					foundName = false;
					varCount++;
				}
			}
		}
		Variable v = VariableCreator.createVariable(null, ParameterType.SIGNAL, programContext.getCurrentlyDefinedTypes().oneOf(possibleReturnTypes), variableName, variableName, programContext.getCurrentlyDefinedTypes(), null, expressionConditioner);
		registerVariable(v, v.getDataType(), programContext, expressionConditioner);
		return v;
	}

	@Override
	public IVariable createVariable(ParameterType parameterType, DataType dataType, String name, String internalName, IProgramContext programContext, IShadowInformation shadowInformation) {
		Variable v = VariableCreator.createVariable(null, parameterType, dataType, name, internalName, programContext.getCurrentlyDefinedTypes(), shadowInformation, null);
		registerVariable(v, dataType, programContext, null);
		return v;
	}


	@Override
	public void registerVariable(IVariable variable, DataType dataType, IProgramContext programContext, SimpleExpressionConditioner expressionConditioner) {
		if (variable instanceof IPointerVariable) {
			programContext.addGlobalVariable(variable, new NotApplicableExpressionConditioner(variable.getDataType()));
		} else if (variable instanceof IArrayVariable) {
			programContext.addGlobalVariable(variable, new NotApplicableExpressionConditioner(variable.getDataType()));
			if (!variable.isShadowVariable()) {
				for (IVariable var : ((IMemberContainer) variable).getMembers()) {
					if (!programContext.getCurrentlyDefinedVariables().getVariableConditioners().containsKey(var)) {
						registerVariable(var, ((ArrayType) variable.getDataType()).getArrayType(), programContext, null);
					}
				}
			}
		} else if (dataType.isBasic()) {
			if (expressionConditioner == null) {
				expressionConditioner = new SimpleExpressionConditionerBuilder(dataType).build();
			}
			try {
				programContext.addGlobalVariable(variable, expressionConditioner.restrictToType(variable.getDataType()));
			} catch (UnsatisfiableConstraintsException e) {
				throw new RuntimeException("Could not get conditioner for variable: " + e.getLocalizedMessage());
			}
		} else if (variable instanceof IMemberContainer) {
			programContext.addGlobalVariable(variable, new NotApplicableExpressionConditioner(variable.getDataType()));
			if (!variable.isShadowVariable()) {
				final IMemberContainer memberContainer = (IMemberContainer) variable;
				final LinkedHashMap<String, String> members = dataType.getMembers();
				final IDataTypeContext dataTypeContext = programContext.getCurrentlyDefinedTypes();
				for (String memberName : members.keySet()) {
					final DataType childType = dataTypeContext.byName(members.get(memberName));
					final IVariable childVariable = memberContainer.getMemberVariableByName(memberName);
					if (!programContext.getCurrentlyDefinedVariables().getVariableConditioners().containsKey(childVariable)) {
						registerVariable(childVariable, childType, programContext, null);
					}
				}
			}
		} else {
			throw new RuntimeException("Unhandled variable type: " + variable.getClass().getSimpleName() + " (name: " + variable.getInternalName() + ")");
		}
	}

	@Override
	public IVariable createNewOutputVariable(EnumMap<Operators, Integer> operatorProbabilities, IProgramContext programContext) {
		// No type restrictions
		SimpleExpressionConditioner expressionConditioner;
		EnumSet<Operators> availableOps = RandomGenHelper.getNonZeroOperators(operatorProbabilities);
		availableOps.removeAll(Operators.getControlStructureOperators());
		IDataTypeContext dataTypeContext = programContext.getCurrentlyDefinedTypes();
		if (Collections.disjoint(availableOps, Operators.getPossibleChildOperators(OperatorReturnType.GENERAL_BOOLEAN))) {
			expressionConditioner = new SimpleExpressionConditionerBuilder(dataTypeContext.noBool()).build();
		} else if (Collections.disjoint(availableOps, Operators.getPossibleChildOperators(OperatorReturnType.ARITHMETIC))) {
			expressionConditioner = new SimpleExpressionConditionerBuilder(dataTypeContext.bool()).build();
		} else if (dontForceBooleanForCondition) {
			expressionConditioner = new SimpleExpressionConditionerBuilder(dataTypeContext.noBool()).build();
		} else if (floatsAllowed) {
			expressionConditioner = new SimpleExpressionConditionerBuilder(dataTypeContext.allBasicTypes()).build();
		} else {
			expressionConditioner = new SimpleExpressionConditionerBuilder(dataTypeContext.integersAndBool()).build();
		}

		return createVariable(expressionConditioner, programContext);
	}

	@Override
	public boolean doWeHaveToCreateNewVariable(SimpleExpressionConditioner expressionConditioner, IVariable assignmentVar, boolean standsAlone, Set<IVariable> dontUse, IProgramContext programContext) {
		Set<IVariable> possibles = new HashSet<>();
		IDataTypeContext dataTypeContext = programContext.getCurrentlyDefinedTypes();
		IVariableContext variableContext = programContext.getCurrentlyDefinedVariables();
		for (ImmutableSet<IVariable> variables : programContext.getCurrentlyDefinedGlobalVariables()) {
			for (IVariable v : variables) {
				if (variableContext.getVariableConditioner(v).isCompatibleWith(expressionConditioner, dataTypeContext) && !(dontUse.contains(v))) {
					possibles.add(v);
				}
			}
		}

		if (standsAlone) {
			possibles.remove(assignmentVar);
		}

		return possibles.size() == 0;
	}

	@Override
	public IVariable getRandomVariableMatchingConstraintsOrNew(SimpleChildExpressionConditioner expressionConditioner, int chanceOfNew, IVariable assignmentVar, Set<IVariable> dontUse, IProgramContext programContext) {
		Set<IVariable> possibles = new HashSet<>();
		IDataTypeContext dataTypeContext = programContext.getCurrentlyDefinedTypes();
		IVariableContext variableContext = programContext.getCurrentlyDefinedVariables();
		for (ImmutableSet<IVariable> variables : programContext.getCurrentlyDefinedGlobalVariables()) {
			for (IVariable v : variables) {
				if (variableContext.getVariableConditioner(v).isCompatibleWith(expressionConditioner, dataTypeContext) && !(dontUse.contains(v))) {
					possibles.add(v);
				}
			}
		}

		if (possibles.size() == 0 || RandomGenHelper.randomChance(chanceOfNew, 100)) {
			// New
			return createVariable(expressionConditioner, programContext);
		} else {
			final int chosenIndex = RandomGenHelper.randomInt(0, possibles.size() - 1);
			int i = 0;
			for (IVariable v : possibles) {
				if (chosenIndex == i) {
					return v;
				}
				++i;
			}
		}

		throw new RuntimeException("Failed to select variable!");
	}

	public static int getVarCount() {
		return varCount;
	}
}
