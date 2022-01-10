package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;

public class Declaration {
	public static class TypeAndNamePair {
		public final String typeName;
		public final String variableName;
		public final FormulaTreeNode initializer;

		public TypeAndNamePair(String typeName, String variableName, FormulaTreeNode initializer) {
			this.typeName = typeName;
			this.variableName = variableName;
			this.initializer = initializer;
		}
	}

	public final ImmutableList<TypeAndNamePair> declaredVariables;

	public Declaration(ImmutableList<TypeAndNamePair> declaredVariables) {
		this.declaredVariables = declaredVariables;
	}
}
