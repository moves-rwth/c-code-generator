package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IPointerAssignment;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper.StringArray;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.*;

import java.util.Map;
import java.util.Objects;

/**
 * Check the interface for a rough outline
 */
public abstract class Variable implements IVariable {
	private static final long serialVersionUID = 4485174503939100574L;

	public static final String MEMBER_NAME_POINTER = "pointerField";

	private final IMemberContainer parent;
	private final DataType dataType;
	private final ParameterType parameterType;
	private final String name;
	private final String internalName;

	private final boolean isShadow;
	private final IShadowInformation shadowInformation;

	private String description = "";
	private boolean isAssignedAValue = false;
	private boolean lastIHasToBeArray = false;

	protected Variable(IMemberContainer parent, DataType datatype, ParameterType parameterType, String name, String internalName, IShadowInformation shadowInformation) {
		this.parent = parent;
		this.dataType = datatype;
		this.parameterType = parameterType;
		this.name = name;
		this.internalName = internalName;
		this.isShadow = shadowInformation != null;
		this.shadowInformation = shadowInformation;
	}

	protected Variable(Variable other, IMemberContainer newParent) {
		this.parent = newParent;
		this.dataType = other.dataType;
		this.parameterType = other.parameterType;
		this.name = other.name;
		this.internalName = other.internalName;
		this.isShadow = other.isShadow;
		this.shadowInformation = other.shadowInformation;
		this.description = other.description;
		this.isAssignedAValue = other.isAssignedAValue;
		this.lastIHasToBeArray = other.lastIHasToBeArray;
	}

	protected Variable(Variable other, String newName, String newInternalName) {
		this.parent = other.parent;
		this.dataType = other.dataType;
		this.parameterType = other.parameterType;
		this.name = newName;
		this.internalName = newInternalName;
		this.isShadow = other.isShadow;
		this.shadowInformation = other.shadowInformation;
		this.description = other.description;
		this.isAssignedAValue = other.isAssignedAValue;
		this.lastIHasToBeArray = other.lastIHasToBeArray;
	}

	protected Variable(Variable other, DataType newDataType) {
		this.parent = other.parent;
		this.dataType = newDataType;
		this.parameterType = other.parameterType;
		this.name = other.name;
		this.internalName = other.internalName;
		this.isShadow = other.isShadow;
		this.shadowInformation = other.shadowInformation;
		this.description = other.description;
		this.isAssignedAValue = other.isAssignedAValue;
		this.lastIHasToBeArray = other.lastIHasToBeArray;
	}

	@Override
	public IMemberContainer getParent() {
		return parent;
	}

	@Override
	public abstract Variable copy();

	@Override
	public abstract Variable replaceParent(IMemberContainer newParent);

	@Override
	public abstract Variable rename(String name, String internalName);

	@Override
	public abstract boolean hasValue();

	@Override
	public abstract StringArray getValueAsString();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Variable variable = (Variable) o;
		return Objects.equals(internalName, variable.internalName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(internalName);
	}

	public String toString() {
		return name;
	}

	/*
		Storage and update/Init related functionality
	 */
	@Override
	public abstract NonDeterministicUpdateInformation buildNonDetUpdateCode(boolean useHarness, Map<IVariable, SimpleExpressionConditioner> conditionerMap);
	// Whether this variable needs a declaration with storage (i.e. type varName;) or, if this is a member of something else
	@Override
	public boolean requiresStorage() {
		return parent == null;
	}
	public abstract StringArray getStorageDeclaration();
	public boolean isBasic() {
		return dataType.isBasic();
	}

	@Override
	@Deprecated
	public String getVariableAccessorName() {
		if (parent == null) {
			return getName();
		}
		return parent.getMemberAccessor(null, this);
	}

	public boolean getVariableByName(String name) {
		return this.name.equals(name);
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public DataType getDataType() {
		return dataType;
	}

	@Override
	public ParameterType getParameterType() {
		return parameterType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getInternalName() {
		return internalName;
	}

	@Override
	public boolean isLastIHasToBeArray() {
		return lastIHasToBeArray;
	}

	public void setLastIHasToBeArray(boolean lastIHasToBeArray) {
		this.lastIHasToBeArray = lastIHasToBeArray;
	}

	public abstract String getDimensions();

	public String getDescription() {
		return description;
	}

	@Override
	public abstract void setValue(String value);

	@Override
	public IVariable replaceDataType(DataType replacementDataType, IProgramContext programContext) {
		// If this is hit, a container subclass is probably missing its implementation.
		throw new RuntimeException("Unhandled/unimplemented replaceDataType() for variable " + getClass().getCanonicalName() + " with type " + getDataType().getClass().getCanonicalName() + "!");
	}

	protected IVariableWithAccessor resolveMemberByName(IVariableWithAccessor thisVariableWithAccessor, IPointerAssignment currentPointerAssignment, String memberName, IVariableWithAccessor existingShadowMember, IProgramContext programContext) {
		if (!isShadow) {
			// A Pointer, even if not a shadow object itself, can be the root cause of new shadow objects
			if (this instanceof IPointerVariable) {
				final IPointerVariable pointerVariable = (IPointerVariable) this;
				if (currentPointerAssignment.hasAssignmentForVariable(pointerVariable)) {
					// The pointer is resolved, return the real object it points to.
					return currentPointerAssignment.getCurrentTargetVariable(thisVariableWithAccessor);
				}

				// The pointer is not resolvable, so we need a shadow object.
				if (existingShadowMember != null) {
					return existingShadowMember;
				}
				return makeShadowVariable(thisVariableWithAccessor, memberName, programContext);
			} else if (this instanceof IMemberContainer) {
				final IMemberContainer memberContainer = (IMemberContainer) this;
				final IVariable memberVariable = memberContainer.getMemberVariableByName(memberName);
				return thisVariableWithAccessor.accessField(memberVariable);
			}

			// Once getMemberVariableByName is merged with this, return child here
			return null;
		}

		final IVariableWithAccessor parentVariable = thisVariableWithAccessor.getParent();
		final IVariableWithAccessor resolvedSelf = shadowInformation.getShadowParent().resolveMemberByName(parentVariable, currentPointerAssignment, getName(), thisVariableWithAccessor, programContext);
		if (resolvedSelf != thisVariableWithAccessor) {
			final IMemberContainer memberContainer = (IMemberContainer) resolvedSelf.getVariable();
			final IVariable memberVariable = memberContainer.getMemberVariableByName(memberName);
			if (this instanceof IMemberContainer) {
				return resolvedSelf.accessField(memberVariable);
			} else if (this instanceof IPointerVariable) {
				return resolvedSelf.accessPointer(memberVariable);
			} else {
				throw new RuntimeException("Unhandled type of variable is calling resolveMemberByName: " + this.getClass().getSimpleName());
			}
		}

		// Being here means the pointer could not be resolved, so return shadow object
		if (existingShadowMember != null) {
			return existingShadowMember;
		}

		return makeShadowVariable(thisVariableWithAccessor, memberName, programContext);
	}
	private IVariableWithAccessor makeShadowVariable(IVariableWithAccessor thisVariableWithAccessor, String memberName, IProgramContext programContext) {
		DataType childType;
		if (this instanceof IMemberContainer) {
			final IMemberContainer memberContainer = (IMemberContainer) this;
			childType = programContext.getCurrentlyDefinedTypes().byName(memberContainer.getMemberTypeByName(memberName));
		} else if (this instanceof IPointerVariable) {
			final IPointerVariable pointerVariable = (IPointerVariable) this;
			childType = programContext.getCurrentlyDefinedTypes().byName(pointerVariable.getPointedToType());
		} else {
			throw new RuntimeException("Unhandled type of variable is calling resolveMemberByName: " + this.getClass().getSimpleName());
		}
		final IShadowInformation shadowInformation = new SimpleShadowInformation((IShadowParent) this);
		final IVariable shadowVariable = programContext.addShadowVariable(ParameterType.INTERNAL_SHADOW, childType, memberName, internalName + '_' + memberName, shadowInformation);
		if (this instanceof IMemberContainer) {
			return thisVariableWithAccessor.accessField(shadowVariable);
		} else if (this instanceof IPointerVariable) {
			return thisVariableWithAccessor.accessPointer(shadowVariable);
		} else {
			throw new RuntimeException("Unhandled type of variable is calling resolveMemberByName: " + this.getClass().getSimpleName());
		}
	}

	public IShadowInformation getShadowInformation() {
		return shadowInformation;
	}

	@Override
	public boolean isShadowVariable() {
		return isShadow;
	}

	@Override
	public String getNameOfLastedVariable(String depthCode) {
		return "last_" + depthCode + "_" + getVariableAccessorName().replaceAll("[.\\[\\]]", "_");
	}

	@Override
	public int compareTo(IVariable o) {
		return internalName.compareTo(o.getInternalName());
	}
}
