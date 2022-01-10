package de.rwth_aachen.moves.bachelorThesis.fink.requirement.modifying;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariableReplacer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.VariableTreeNode;

import java.util.*;

public class VariableReplacer implements IVariableReplacer {
	private final Map<VariableTreeNode, IVariableWithAccessor> mVariableTreeNodeToReplacerVar = new HashMap<>();
	private final Map<IVariableWithAccessor, IVariableWithAccessor> mReplacementMapWithAccessors = new HashMap<>();
	private final Map<IVariable, IVariable> mReplacementMap = new HashMap<>();
	private int mVariableCounter = 0;

	private final String mPrefix;
	private final boolean mAllowOnlyFromMap;
	private final boolean mMakeStandAlone;
	private final PreVarOperator preVarOperator;
	private final boolean castTypes;

	public enum PreVarOperator {
		DEREFERENCE, ARRAY_ACCESS, ADDRESS_OF, LAST
	}

	private VariableReplacer(String prefix, boolean allowOnlyFromMap, boolean makeStandAlone, PreVarOperator preVarOperator, boolean castTypes) {
		this.mPrefix = prefix;
		this.mAllowOnlyFromMap = allowOnlyFromMap;
		this.mMakeStandAlone = makeStandAlone;
		this.preVarOperator = preVarOperator;
		this.castTypes = castTypes;
	}

	public static VariableReplacer fromVariableTreeNodeToReplacerVarMap(Map<VariableTreeNode, IVariableWithAccessor> mVariableTreeNodeToReplacerVar) {
		return new VariableReplacer(new HashMap<>(), new HashMap<>(), mVariableTreeNodeToReplacerVar);
	}

	public static VariableReplacer fromVariableTreeNodeToReplacerVarMap(Map<VariableTreeNode, IVariableWithAccessor> mVariableTreeNodeToReplacerVar, PreVarOperator preVarOperator) {
		return new VariableReplacer(new HashMap<>(), new HashMap<>(), mVariableTreeNodeToReplacerVar, preVarOperator);
	}


	public VariableReplacer() {
		this("", false, false, null, false);
	}

	public VariableReplacer(String prefix) {
		this(prefix, false, false, null, false);
	}

	public VariableReplacer(Map<IVariable, IVariable> mapping) {
		this("", true, false, null, false);
		mReplacementMap.putAll(mapping);
	}

	protected VariableReplacer(Map<IVariable, IVariable> mappingA, Map<IVariableWithAccessor, IVariableWithAccessor> mappingB) {
		this("", true, false, null, false);
		mReplacementMap.putAll(mappingA);
		mReplacementMapWithAccessors.putAll(mappingB);
	}

	protected VariableReplacer(Map<IVariable, IVariable> mappingA, Map<IVariableWithAccessor, IVariableWithAccessor> mappingB, PreVarOperator preVarOperator) {
		this("", true, false, preVarOperator, false);
		mReplacementMap.putAll(mappingA);
		mReplacementMapWithAccessors.putAll(mappingB);
	}

	protected VariableReplacer(Map<IVariable, IVariable> mappingA, Map<IVariableWithAccessor, IVariableWithAccessor> mappingB, boolean castTypes) {
		this("", true, false, null, castTypes);
		mReplacementMap.putAll(mappingA);
		mReplacementMapWithAccessors.putAll(mappingB);
	}

	protected VariableReplacer(Map<IVariable, IVariable> mappingA, Map<IVariableWithAccessor, IVariableWithAccessor> mappingB, Map<VariableTreeNode, IVariableWithAccessor> mappingC) {
		this("", true, false, null, false);
		mReplacementMap.putAll(mappingA);
		mReplacementMapWithAccessors.putAll(mappingB);
		mVariableTreeNodeToReplacerVar.putAll(mappingC);
	}

	protected VariableReplacer(Map<IVariable, IVariable> mappingA, Map<IVariableWithAccessor, IVariableWithAccessor> mappingB, Map<VariableTreeNode, IVariableWithAccessor> mappingC, PreVarOperator preVarOperator) {
		this("", true, false, preVarOperator, false);
		mReplacementMap.putAll(mappingA);
		mReplacementMapWithAccessors.putAll(mappingB);
		mVariableTreeNodeToReplacerVar.putAll(mappingC);
	}

	public static VariableReplacer fromVariableWithAccessorReplacementMap(Map<IVariableWithAccessor, IVariableWithAccessor> variableWithAccessorReplacementMap) {
		return new VariableReplacer(new HashMap<>(), variableWithAccessorReplacementMap);
	}

	public static VariableReplacer fromVariableWithAccessorReplacementMap(Map<IVariableWithAccessor, IVariableWithAccessor> variableWithAccessorReplacementMap, PreVarOperator preVarOperator) {
		return new VariableReplacer(new HashMap<>(), variableWithAccessorReplacementMap, preVarOperator);
	}

	public static VariableReplacer fromVariableWithAccessorReplacementMap(Map<IVariableWithAccessor, IVariableWithAccessor> variableWithAccessorReplacementMap, boolean castTypes) {
		return new VariableReplacer(new HashMap<>(), variableWithAccessorReplacementMap, castTypes);
	}

	public VariableReplacer(Map<IVariable, IVariable> mapping, PreVarOperator preVarOperator) {
		this("", true, false, preVarOperator, false);
		mReplacementMap.putAll(mapping);
	}

	public VariableReplacer(Map<IVariable, IVariable> mapping, PreVarOperator preVarOperator, boolean castTypes) {
		this("", true, false, preVarOperator, castTypes);
		mReplacementMap.putAll(mapping);
	}

	public VariableReplacer(Set<IVariable> variablesToBeReplaced, Set<IVariableWithAccessor> variablesWithAccessorsToBeReplaced, String prefix) {
		this(prefix, true, true, null, false);

		for (IVariable v : variablesToBeReplaced) {
			mReplacementMap.put(v, makeReplacement(v));
		}

		for (IVariableWithAccessor v : variablesWithAccessorsToBeReplaced) {
			mReplacementMapWithAccessors.put(v, makeReplacement(v));
		}
	}


	public int getVariableCounter() {
		return mVariableCounter;
	}

	private IVariable makeReplacement(IVariable v) {
		IVariable anonV = v.rename(mPrefix + "Var" + mVariableCounter, mPrefix + "Var" + mVariableCounter);
		if (mMakeStandAlone) {
			anonV = anonV.replaceParent(null);
		}
		++mVariableCounter;
		return anonV;
	}

	private IVariableWithAccessor makeReplacement(IVariableWithAccessor v) {
		IVariableWithAccessor anonV = v;
		if (mMakeStandAlone) {
			anonV = anonV.replaceParent(null);
		}

		if (anonV == null) {
			throw new RuntimeException("Variable null");
		}

		if (!anonV.hasTrivialAccessor()) {
			throw new RuntimeException("Can not rename a variable with non-trivial accessor!");
		}

		anonV = anonV.rename(mPrefix + "Var" + mVariableCounter, mPrefix + "Var" + mVariableCounter);

		++mVariableCounter;
		return anonV;
	}

	@Override
	public IVariableWithAccessor getReplacement(IVariableWithAccessor variable, VariableTreeNode node) {
		if (!mAllowOnlyFromMap && !mReplacementMapWithAccessors.containsKey(variable)) {
			mReplacementMapWithAccessors.put(variable, makeReplacement(variable));
		}

		if (mReplacementMapWithAccessors.containsKey(variable)) {
			return mReplacementMapWithAccessors.get(variable);
		} else if ((mReplacementMapWithAccessors.size() == 0) && mReplacementMap.containsKey(variable.getVariable())) {
			// In cases where we only have an IVariable -> IVariable map, use trivial accessors.
			return SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(mReplacementMap.get(variable.getVariable()));
		} else if (mVariableTreeNodeToReplacerVar.containsKey(node)) {
			return mVariableTreeNodeToReplacerVar.get(node);
		}

		// This is for cases where only a predefined set of variables is to be replaced and the rest stays as is
		return variable;
	}

	@Override
	public List<IVariableWithAccessor> getReplacements(List<IVariableWithAccessor> variables) {
		List<IVariableWithAccessor> result = new ArrayList<>();
		for (IVariableWithAccessor v : variables) {
			result.add(getReplacement(v, null));
		}
		return result;
	}

	@Override
	public boolean containsVariable(IVariableWithAccessor var) {
		return (mReplacementMapWithAccessors.containsKey(var));
	}

	@Override
	public boolean containsVariable(IVariable var) {
		return (mReplacementMap.containsKey(var));
	}

	@Override
	public PreVarOperator getPreVarOperator() {
		return preVarOperator;
	}

	@Override
	public boolean castTypes() {
		return castTypes;
	}
}
