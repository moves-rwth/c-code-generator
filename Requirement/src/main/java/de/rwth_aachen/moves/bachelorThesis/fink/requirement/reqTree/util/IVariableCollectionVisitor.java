package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util;

import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.ICodeTreeNodeContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;

public interface IVariableCollectionVisitor {
	void visit(TemplateInfo templateInfo);

	void visit(IFunction nonVoidFunction);

	void visit(FormulaTreeNode formulaTreeNode);

	void visit(ICodeTreeNodeContainer codeTreeNodeContainer);
}
