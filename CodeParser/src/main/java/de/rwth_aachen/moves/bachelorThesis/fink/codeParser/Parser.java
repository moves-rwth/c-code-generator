package de.rwth_aachen.moves.bachelorThesis.fink.codeParser;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.TreeHandler;
import de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree.DeclarationList;
import de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree.FunctionDeclaration;
import de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree.ITree;
import de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree.SectionMarker;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.RequirementScopes;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ConcatenationOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ItOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.IteOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.patterns.InvariantPatternTreeNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.core.runtime.CoreException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

	protected static final Logger logger = LogManager.getLogger(Parser.class);

	public Parser() {
		// empty on purpose
	}

	public static class TreeAndContextPair {
		public final ITree tree;
		public final IProgramContext programContext;

		public TreeAndContextPair(ITree tree, IProgramContext programContext) {
			this.tree = tree;
			this.programContext = programContext;
		}
	}

	private TreeAndContextPair parseFile(String filename) throws CoreException {
		FileContent fileContent = FileContent.createForExternalFileLocation(filename);
		return parse(fileContent);
	}

	private TreeAndContextPair parseString(String contents) throws CoreException {
		FileContent fileContent = FileContent.create("IN_MEMORY_FILE", contents.toCharArray());
		return parse(fileContent);
	}

	private TreeAndContextPair parse(FileContent fileContent) throws CoreException {
		Map<String, String> definedSymbols = new HashMap<>();
		String[] includePaths = new String[0];
		IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
		IParserLogService log = new DefaultLogService();

		IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();

		int opts = 8;
		IASTTranslationUnit translationUnit = GCCLanguage.getDefault().getASTTranslationUnit(fileContent, info, emptyIncludes, null, opts, log);

		IASTPreprocessorIncludeStatement[] includes = translationUnit.getIncludeDirectives();
		for (IASTPreprocessorIncludeStatement include : includes) {
			System.out.println("include - " + include.getName());
		}

		NodeCommentMap nodeCommentMap = ASTCommenter.getCommentedNodeMap(translationUnit);
		TreeHandler treeHandler = new TreeHandler();
		return new TreeAndContextPair(treeHandler.handleTree(translationUnit, nodeCommentMap, false), treeHandler.getProgramContext());
	}

	private static List<Requirement> getRequirementsFromNodes(FormulaTreeNode formulaTreeNode, String prefix, int counter) {
		List<Requirement> result = new ArrayList<>();

		if (formulaTreeNode instanceof ConcatenationOperator) {
			ImmutableList<FormulaTreeNode> children = formulaTreeNode.getChildren();
			result.addAll(getRequirementsFromNodes(children.get(0), prefix, counter));
			result.addAll(getRequirementsFromNodes(children.get(1), prefix, counter + result.size()));
		} else if ((formulaTreeNode instanceof ItOperator) || (formulaTreeNode instanceof IteOperator)) {
			result.add(new Requirement("REQ-" + prefix + "-" + counter, new InvariantPatternTreeNode(formulaTreeNode), RequirementScopes.GLOBALLY));
		} else {
			throw new RuntimeException("Unhandled node type for auto-conversion to requirement: " + formulaTreeNode.getClass().getCanonicalName());
		}

		return result;
	}

	public static TreeAndContextPair parseStringToDeclarationList(String programAsString) {
		Parser parser = new Parser();
		try {
			TreeAndContextPair treeAndContextPair = parser.parseString(programAsString);
			assert (treeAndContextPair.tree instanceof DeclarationList);
			DeclarationList declarationList = (DeclarationList) treeAndContextPair.tree;

			// Check whether this is a pure program marked with control comments
			boolean hasControlSectionMarkers = false;
			boolean hasUnmarkedCode = false;
			for (ITree element : declarationList.getElements()) {
				if (element instanceof SectionMarker) {
					hasControlSectionMarkers = true;
				} else {
					hasUnmarkedCode = true;
				}
			}

			if (hasControlSectionMarkers && hasUnmarkedCode) {
				logger.error("Error: Program mixes code tagged with control section markers and untagged code!");
				return null;
			} else if (!hasControlSectionMarkers) {
				logger.error("Error: Program only has untagged code, no section markers!");
				return null;
			}
			return treeAndContextPair;
		} catch (CoreException ex) {
			logger.error("Encountered a CoreException when parsing program from string: " + ex.getLocalizedMessage(), ex);
			return null;
		}
	}

	public static void main(String[] args) {
		Path sourceFile;

		if (args.length >= 1) {
			final String filename = args[0];
			sourceFile = Paths.get(filename);
		} else {
			System.err.println("Unhandled arguments to program!");
			System.exit(1);
			return;
		}

		System.out.println("Loading file '" + sourceFile.toAbsolutePath().toString() + "'...");

		Parser parser = new Parser();
		try {
			TreeAndContextPair treeAndContextPair = parser.parseFile(sourceFile.toAbsolutePath().toString());

			List<Requirement> requirements = new ArrayList<>();
			assert(treeAndContextPair.tree instanceof DeclarationList);
			DeclarationList declarationList = (DeclarationList) treeAndContextPair.tree;

			for (ITree element: declarationList.getElements()) {
				if (element instanceof FunctionDeclaration) {
					FunctionDeclaration functionDeclaration = (FunctionDeclaration) element;
					requirements.addAll(getRequirementsFromNodes(functionDeclaration.getBody(), functionDeclaration.getName(), 0));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
