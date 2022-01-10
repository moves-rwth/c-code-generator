package de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * Replaces all floats with INT32.
 */
public class FloatTypeReplacer implements ITypeReplacer {

	private final boolean useSameName;

	public FloatTypeReplacer(boolean useSameName) {
		this.useSameName = useSameName;
	}

	@Override
	public DataType getReplacementType(IVariable variable, IProgramContext programContext) {
		if (willReplace(variable, programContext)) {
			final String typeName = replaceTypes(variable.getDataType(), programContext, new HashMap<>());
			IDataTypeContext dataTypeContext = programContext.getCurrentlyDefinedTypes();
			return dataTypeContext.byName(typeName);
		}

		return variable.getDataType();
	}

	private String getReplacementTypeName(DataType dataType) {
		if (useSameName) {
			return dataType.getTypeName();
		} else {
			return dataType.getTypeName() + "_FLREP";
		}
	}

	private LinkedHashMap<String, String> replaceMembers(LinkedHashMap<String, String> members, IProgramContext programContext, HashMap<DataType, String> typesBeingWorkedOn) {
		IDataTypeContext dataTypeContext = programContext.getCurrentlyDefinedTypes();
		LinkedHashMap<String, String> newMembers = new LinkedHashMap<>();
		for (String memberName : members.keySet()) {
			final DataType member = dataTypeContext.byName(members.get(memberName));
			if (willReplace(member, programContext)) {
				newMembers.put(memberName, replaceTypes(member, programContext, typesBeingWorkedOn));
			} else {
				newMembers.put(memberName, members.get(memberName));
			}
		}
		return newMembers;
	}

	/**
	 * We assume that this function is only called for types for which willReplace(type) = true.
	 * @param dataType A type that itself needs to be replaced or has a member which does.
	 * @param programContext The program context.
	 * @param typesBeingWorkedOn A list of types that are already being worked on, for recursive types.
	 * @return The name of the replaced type.
	 */
	private String replaceTypes(DataType dataType, IProgramContext programContext, HashMap<DataType, String> typesBeingWorkedOn) {
		if (dataType.isFloatingPoint()) {
			return DataType.INSTANCE_INT32.getTypeName();
		}

		// Do not fall into recursion holes.
		if (typesBeingWorkedOn.containsKey(dataType)) {
			return typesBeingWorkedOn.get(dataType);
		}

		// If this is a type that does not need replacing, return the type itself
		if (!willReplace(dataType, programContext)) {
			return dataType.getTypeName();
		}

		final String myNewName = getReplacementTypeName(dataType);
		typesBeingWorkedOn.put(dataType, myNewName);

		DataType newType = null;
		if (dataType instanceof IStructType) {
			IStructType structType = (IStructType) dataType;
			final LinkedHashMap<String, String> members = structType.getMembers();
			final LinkedHashMap<String, String> newMembers = replaceMembers(members, programContext, typesBeingWorkedOn);

			if (dataType instanceof TypedefdStructType) {
				TypedefdStructType typedefdStructType = (TypedefdStructType) dataType;
				newType = new TypedefdStructType(myNewName, newMembers, typedefdStructType.getBaseTypeName());
			} else if (dataType instanceof StructType) {
				newType = new StructType(myNewName, newMembers);
			} else {
				throw new RuntimeException("Unhandled IStructType: " + dataType.getClass().getCanonicalName());
			}
		} else if (dataType instanceof IUnionType) {
			IUnionType unionType = (IUnionType) dataType;
			final LinkedHashMap<String, String> members = unionType.getMembers();
			final LinkedHashMap<String, String> newMembers = replaceMembers(members, programContext, typesBeingWorkedOn);

			if (dataType instanceof TypedefdUnionType) {
				TypedefdUnionType typedefdUnionType = (TypedefdUnionType) dataType;
				newType = new TypedefdUnionType(myNewName, newMembers, typedefdUnionType.getBaseTypeName());
			} else if (dataType instanceof UnionType) {
				newType = new UnionType(myNewName, newMembers);
			} else {
				throw new RuntimeException("Unhandled IUnionType: " + dataType.getClass().getCanonicalName());
			}
		} else {
			throw new RuntimeException("Unhandled DataType: " + dataType.getClass().getCanonicalName());
		}
		programContext.registerType(newType);

		return myNewName;
	}

	private boolean checkMembers(LinkedHashMap<String, String> members, IProgramContext programContext, HashSet<DataType> seenTypes) {
		IDataTypeContext dataTypeContext = programContext.getCurrentlyDefinedTypes();
		boolean needsReplacement = false;
		for (String memberName : members.keySet()) {
			final DataType member = dataTypeContext.byName(members.get(memberName));
			if (willReplace(member, programContext, seenTypes)) {
				needsReplacement = true;
				break;
			}
		}
		return needsReplacement;
	}

	private boolean willReplace(DataType dataType, IProgramContext programContext, HashSet<DataType> seenTypes) {
		// Do not fall into recursion holes.
		if (seenTypes.contains(dataType)) {
			return false;
		}
		seenTypes.add(dataType);

		if (dataType instanceof IStructType) {
			IStructType structType = (IStructType) dataType;
			final LinkedHashMap<String, String> members = structType.getMembers();
			final boolean memberResult = checkMembers(members, programContext, seenTypes);
			if (memberResult) {
				return true;
			}
		} else if (dataType instanceof IUnionType) {
			IUnionType unionType = (IUnionType) dataType;
			final LinkedHashMap<String, String> members = unionType.getMembers();
			final boolean memberResult = checkMembers(members, programContext, seenTypes);
			if (memberResult) {
				return true;
			}
		}

		return dataType.isFloatingPoint();
	}

	private boolean willReplace(IVariable variable, IProgramContext programContext, HashSet<DataType> seenTypes) {
		final DataType dataType = variable.getDataType();

		final boolean dataTypeResult = willReplace(dataType, programContext, seenTypes);
		if (variable.getParent() != null) {
			// We might be in a Struct's member which itself is fine, but the whole struct will be replaced and this member would otherwise not be picked up.
			final boolean parentResult = willReplace(variable.getParent(), programContext, seenTypes);
			if (parentResult) {
				return true;
			}
		}

		return dataTypeResult;
	}

	@Override
	public boolean willReplace(IVariable variable, IProgramContext programContext) {
		return willReplace(variable, programContext, new HashSet<>());
	}

	@Override
	public boolean willReplace(DataType dataType, IProgramContext programContext) {
		return willReplace(dataType, programContext, new HashSet<>());
	}
}
