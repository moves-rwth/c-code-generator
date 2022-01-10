package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IMemberContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IPointerVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Check the interface for a rough outline.
 */
public class SimpleVariableWithAccessInformation implements IVariableWithAccessor {

	private static final long serialVersionUID = -2376038553635605018L;
	private final IVariable variable;
	private final IVariableAccessInformation variableAccessInformation;

	public SimpleVariableWithAccessInformation(IVariable variable, IVariableAccessInformation variableAccessInformation) {
		this.variable = variable;
		this.variableAccessInformation = variableAccessInformation;
	}

	public static SimpleVariableWithAccessInformation makeVariableWithTrivialAccessInformation(IVariable variable) {
		return new SimpleVariableWithAccessInformation(variable, SimpleVariableAccessInformation.makeTrivialVariableAccessInformation(variable));
	}

	@Override
	public IVariableWithAccessor accessPointer(IProgramContext programContext) {
		if (!(variable instanceof IPointerVariable)) {
			throw new RuntimeException("Can not accessPointer on variable that is not an IPointerVariable, but a " + variable.getClass().getCanonicalName() + "!");
		}

		final IPointerVariable pointerVariable = (IPointerVariable) variable;
		return pointerVariable.resolveMemberByName(this, programContext.getCurrentPointerAssignment(), Variable.MEMBER_NAME_POINTER, null, programContext);
	}

	@Override
	public IVariableWithAccessor accessField(String fieldName, IProgramContext programContext) {
		if (!(variable instanceof IMemberContainer)) {
			throw new RuntimeException("Can not accessField on variable that is not an IMemberContainer, but a " + variable.getClass().getCanonicalName() + "!");
		}

		final IMemberContainer memberContainer = (IMemberContainer) variable;
		return memberContainer.resolveMemberByName(this, programContext.getCurrentPointerAssignment(), fieldName, null, programContext);
	}

	// The two variants where we already have the target variables...

	@Override
	public IVariableWithAccessor accessPointer(IVariable targetVariable) {
		if (!(variable instanceof IPointerVariable)) {
			throw new RuntimeException("Can not accessPointer on variable that is not an IPointerVariable, but a " + variable.getClass().getCanonicalName() + "!");
		}

		return new SimpleVariableWithAccessInformation(targetVariable, variableAccessInformation.accessPointer(targetVariable));
	}

	@Override
	public IVariableWithAccessor accessField(IVariable targetVariable) {
		if (!(variable instanceof IMemberContainer)) {
			throw new RuntimeException("Can not accessField on variable that is not an IMemberContainer, but a " + variable.getClass().getCanonicalName() + "!");
		}

		return new SimpleVariableWithAccessInformation(targetVariable, variableAccessInformation.accessField(targetVariable));
	}

	@Override
	public IVariableWithAccessor getParent() {
		if (variableAccessInformation.getParent() == null) {
			return null;
		}
		return new SimpleVariableWithAccessInformation(variableAccessInformation.getParent().getBaseVariable(), variableAccessInformation.getParent());
	}


	@Override
	public DataType getDataType() {
		return variable.getDataType();
	}

	@Override
	public IVariable getVariable() {
		return variable;
	}

	@Override
	public IVariableAccessInformation getAccessInformation() {
		return variableAccessInformation;
	}

	@Override
	public boolean hasTrivialAccessor() {
		return variableAccessInformation.isTrivial();
	}

	@Override
	public IVariableWithAccessor rename(String name, String internalName) {
		if (variable.getParent() != null) {
			throw new RuntimeException("Can not rename a variable that is a member!");
		} else if (!variableAccessInformation.isTrivial()) {
			throw new RuntimeException("Can not rename a variable with non-trivial accessor!");
		}
		IVariable newVariable = variable.rename(name, internalName);
		return makeVariableWithTrivialAccessInformation(newVariable);
	}

	@Override
	public IVariableWithAccessor replaceParent(IMemberContainer newParent) {
		IVariable variableWithReplacedParent = getVariable().replaceParent(newParent);
		IVariableAccessInformation trivialAccessor = SimpleVariableAccessInformation.makeTrivialVariableAccessInformation(variableWithReplacedParent);
		IVariableWithAccessor result = new SimpleVariableWithAccessInformation(variableWithReplacedParent, trivialAccessor);
		return result;
	}

	@Override
	public String getName() {
		return variable.getName();
	}

	@Override
	public String getNameOfLastedVariable(String depthCode) {
		return variable.getNameOfLastedVariable(depthCode);
	}

	@Override
	public Set<IVariableWithAccessor> getMembers() {
		if (!(variable instanceof IMemberContainer)) {
			return null;
		}

		final IMemberContainer memberContainer = (IMemberContainer) variable;
		final Set<IVariable> memberVariables = memberContainer.getMembers();
		Set<IVariableWithAccessor> result = new HashSet<>();
		for (IVariable memberVariable : memberVariables) {
			result.add(this.accessField(memberVariable));
		}
		return result;
	}

	@Override
	public String toString() {
		return variable.getName() + " (" + variableAccessInformation.getVariableAccessor() + ", " + variable.getDataType() + ")";
	}

	@Override
	public String toString(Set<IVariableWithAccessor> allVariables) {
		return variableAccessInformation.getVariableAccessor();
	}

	@Override
	public String getVariableAccessor() {
		return variableAccessInformation.getVariableAccessor();
	}

	/*
		Equality, Hashing and Ordering
	 */

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SimpleVariableWithAccessInformation otherVariableWithAccessInformation = (SimpleVariableWithAccessInformation) o;
		return Objects.equals(variable, otherVariableWithAccessInformation.variable) && Objects.equals(variableAccessInformation, otherVariableWithAccessInformation.variableAccessInformation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(variable, variableAccessInformation);
	}

	@Override
	public int compareTo(IVariableWithAccessor o) {
		final int thisVariableResult = variable.compareTo(o.getVariable());
		if (thisVariableResult != 0) {
			return variableAccessInformation.compareTo(o.getAccessInformation());
		}
		return thisVariableResult;
	}
}
