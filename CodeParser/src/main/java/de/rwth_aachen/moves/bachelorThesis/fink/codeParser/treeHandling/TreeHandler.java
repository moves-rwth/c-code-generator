package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling;

import com.google.common.collect.ImmutableList;
import de.rwth_aachen.moves.bachelorThesis.fink.codeParser.exceptions.InvalidCodeException;
import de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IAliasType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IArrayType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.ParameterType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.SimpleProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.SimpleVariableWithAccessInformation;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IMemberContainer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IPointerVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.NonVoidFunction;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.*;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TreeHandler {
	protected static final Logger logger = LogManager.getLogger(TreeHandler.class);
	private ParameterType currentParameterType;
	private static final Pattern regexControlComment = Pattern.compile("^// CONTROL:(.+)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);

	public IProgramContext getProgramContext() {
		return programContext;
	}

	public static class FunctionInfo {
		public final String returnType;
		public final FormulaTreeNode body;

		public FunctionInfo(String returnType, FormulaTreeNode body) {
			this.returnType = returnType;
			this.body = body;
		}
	}

	private final Map<String, String> knownTypeNames = new HashMap<>();
	private final Map<String, FunctionInfo> knownFunctionNames = new HashMap<>();

	private final IProgramContext programContext = new SimpleProgramContext();

	private int counterForUnnamedTypes = 0;

	private boolean translateSpecialFunctions;

	public TreeHandler() {
		translateSpecialFunctions = true;
	}

	public ITree handleTree(IASTNode node, NodeCommentMap nodeCommentMap, boolean commentHasBeenHandled) {
		List<IASTComment> comments = nodeCommentMap.getAllCommentsForNode(node);
		if ((comments.size() > 0) && !commentHasBeenHandled) {
			logger.info("Found " + comments.size() + " comments for a node.");
			for (IASTComment comment: comments) {
				final String commentString = String.copyValueOf(comment.getComment());
				logger.info("Comment: " + commentString);

				Matcher regexMatcher = regexControlComment.matcher(commentString);
				if (regexMatcher.matches()) {
					final String sectionName = regexMatcher.group(1);

					return SectionMarker.createSectionMarker(sectionName, handleTree(node, nodeCommentMap, true));
				} else {
					logger.info("Ignoring comment that is not a section marker.");
				}
			}
		}

		if (node instanceof CASTSimpleDeclaration) {
			CASTSimpleDeclaration simpleDeclaration = (CASTSimpleDeclaration) node;
			if (isSimpleDeclarationPureTypedef(simpleDeclaration)) {
				return handleSimpleDeclarationTypedef(simpleDeclaration);
			}
			Declaration varDecl = handleSimpleDeclaration(simpleDeclaration);
			if (varDecl == null) {
				logger.error("Found global declaration, but the handler returned NULL!");
				throw new IllegalStateException("Found global declaration, but the handler returned NULL!");
			}
			return new GlobalVariableDeclaration(handleDeclaration(varDecl, true));
		} else if (node instanceof CASTFunctionDefinition) {
			CASTFunctionDefinition functionDefinition = (CASTFunctionDefinition) node;
			return handleFunctionDefinition(functionDefinition);
		} else if (node instanceof CASTTranslationUnit) {
			CASTTranslationUnit translationUnit = (CASTTranslationUnit) node;
			return handleTranslationUnit(translationUnit, nodeCommentMap);
		} else if (node instanceof CASTProblemDeclaration) {
			CASTProblemDeclaration problemDeclaration = (CASTProblemDeclaration) node;
			final IASTProblem problem = problemDeclaration.getProblem();
			assert(problem instanceof CASTProblem);
			final CASTProblem castProblem = (CASTProblem) problem;
			if (castProblem.isError()) {
				logger.error("Detected an error while parsing file: " + castProblem.getMessageWithLocation());
			} else {
				logger.error("Detected a warning while parsing file: " + castProblem.getMessageWithLocation());
			}
			return null;
		} else {
			throw new IllegalStateException("Unhandled node type: " + node.getClass().getSimpleName());
		}
	}

	private List<String> getControlCommentSectionName(IASTNode node, NodeCommentMap nodeCommentMap) {
		List<IASTComment> comments = nodeCommentMap.getAllCommentsForNode(node);
		List<String> result = new ArrayList<>();
		if (comments.size() > 0) {
			for (IASTComment comment: comments) {
				final String commentString = String.copyValueOf(comment.getComment());
				Matcher regexMatcher = regexControlComment.matcher(commentString);
				if (regexMatcher.matches()) {
					final String sectionName = regexMatcher.group(1);
					result.add(sectionName);
				}
			}
		}
		return result;
	}

	private List<ITree> addList(List<ITree> list, ITree tree) {
		if (tree instanceof DeclarationList) {
			DeclarationList declarationList = (DeclarationList) tree;
			list.addAll(declarationList.getElements());
		} else {
			list.add(tree);
		}
		return list;
	}

	private DeclarationList handleTranslationUnit(CASTTranslationUnit translationUnit, NodeCommentMap nodeCommentMap) {
		IASTDeclaration[] declarations = translationUnit.getDeclarations();
		List<ITree> finalElements = new ArrayList<>();

		String currentSectionName = null;
		List<ITree> elements = new ArrayList<>();

		for (IASTDeclaration iastDeclaration : declarations) {
			final List<String> controlSectionNames = getControlCommentSectionName(iastDeclaration, nodeCommentMap);
			if (controlSectionNames.size() > 0) {
				// Old section over
				if (elements.size() > 0) {
					if (currentSectionName != null) {
						final ITree sectionMarker = SectionMarker.createSectionMarker(currentSectionName, new DeclarationList(ImmutableList.copyOf(elements)));
						finalElements.add(sectionMarker);
					} else {
						// Unmarked, no section so far. Does this make sense? Don't know.
						finalElements.addAll(elements);
					}
					elements.clear();
				}

				// New Section
				currentSectionName = controlSectionNames.get(controlSectionNames.size() - 1);
				switch (currentSectionName) {
					case "SIGNAL_INPUT":
					case "SIGNAL_OUTPUT":
					case "CODE":
					case "LOCAL_PROPERTIES":
					case "GLOBAL_PROPERTIES":
						currentParameterType = ParameterType.SIGNAL;
						break;

					case "LOCALS":
					case "INTERNAL_VARS":
						currentParameterType = ParameterType.INTERNAL_CONTROL;
						break;

					case "FUNCTIONS":
						currentParameterType = ParameterType.INTERNAL_SHADOW;
						break;

					case "CALIBRATABLES":
						currentParameterType = ParameterType.CALIBRATION_VALUE;
						break;

					default:
						throw new RuntimeException("Unknown section comment");
				}
			}

			ITree tree = handleTree(iastDeclaration, nodeCommentMap, true);
			if (tree == null) {
				continue;
			}

			if (tree instanceof DeclarationList) {
				DeclarationList declarationList = (DeclarationList) tree;
				elements.addAll(declarationList.getElements());
			} else {
				elements.add(tree);
			}
		}

		// And the final section
		if (elements.size() > 0) {
			if (currentSectionName != null) {
				final ITree sectionMarker = SectionMarker.createSectionMarker(currentSectionName, new DeclarationList(ImmutableList.copyOf(elements)));
				finalElements.add(sectionMarker);
			} else {
				finalElements.addAll(elements);
			}
			elements.clear();
		}

		return new DeclarationList(finalElements);
	}

	private DataType translateType(IType type) {
		String typeString = ASTTypeUtil.getType(type, true);
		if (typeString.equalsIgnoreCase("short int")) {
			typeString = "signed short int";
		} else if (typeString.equalsIgnoreCase("long int")) {
			typeString = "signed long int";
		}

		return programContext.getCurrentlyDefinedTypes().byName(typeString);
	}


	private LinkedHashMap<String, String> parseMembers(IASTDeclaration[] declarations) {
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for (IASTDeclaration declaration : declarations) {
			logger.info("declaration has type " + declaration.getClass().getSimpleName());
			Declaration memberDeclaration = parseMember(declaration);
			for (Declaration.TypeAndNamePair typeAndNamePair: memberDeclaration.declaredVariables) {
				result.put(typeAndNamePair.variableName, typeAndNamePair.typeName);
			}
		}
		return result;
	}

	private Declaration parseMember(IASTDeclaration iDeclaration) {
		Declaration declaration;
		if (iDeclaration instanceof CASTSimpleDeclaration) {
			declaration = handleSimpleDeclaration((CASTSimpleDeclaration) iDeclaration);
		} else {
			throw new IllegalStateException("Unhandled type of member declaration: " + iDeclaration.getClass().getSimpleName());
		}

		return declaration;
	}

	public ITypedefDeclaration handleSimpleDeclarationTypedef(CASTSimpleDeclaration simpleDeclaration) {
		final IASTDeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
		final IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();
		final StorageClass storageClass = StorageClass.fromCdtStorageClass(declSpecifier.getStorageClass());
		final boolean isTypedef = (storageClass == StorageClass.TYPEDEF);
		final boolean isCompositeTypeSpecifier = declSpecifier instanceof CASTCompositeTypeSpecifier;
		assert (isTypedef);

		if (isCompositeTypeSpecifier) {
			CompositeTypeSpecifierResult result = handleCompositeTypeSpecifier(declSpecifier, declarators, true);
			return result.typedefDeclaration;
		} else {
			assert (declarators.length == 1);
			assert (declarators[0].getName() instanceof CASTName);
			final IASTName astName = declarators[0].getName();
			IBinding binding = astName.resolveBinding();
			if (binding instanceof CTypedef) {
				final CTypedef cTypedef = (CTypedef) binding;
				logger.info(astName.toString() + " is a typedef with name '" + cTypedef.getName() + "'!");
				final String newName = cTypedef.getName();
				final DataType oldType = translateType(cTypedef);
				final String oldName = oldType.getTypeName();

				logger.info("Found typedef, new type '" + newName + "' is defined as '" + oldName + "'.");
				knownTypeNames.put(newName, oldName);
				programContext.addTypedef(newName, oldType);

				return new SimpleTypedefDeclaration(newName, oldName);
			} else {
				throw new IllegalStateException("Binding has unexpected type " + binding.getClass().getSimpleName());
			}
		}
	}

	public boolean isSimpleDeclarationPureTypedef(CASTSimpleDeclaration simpleDeclaration) {
		final IASTDeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
		final StorageClass storageClass = StorageClass.fromCdtStorageClass(declSpecifier.getStorageClass());
		final boolean isTypedef = (storageClass == StorageClass.TYPEDEF);
		final boolean isCompositeTypeSpecifier = declSpecifier instanceof CASTCompositeTypeSpecifier;
		return isTypedef;
	}

	private static final class CompositeTypeSpecifierResult {
		public final String preTypedefedName;
		public final ITypedefDeclaration typedefDeclaration;

		public CompositeTypeSpecifierResult(String preTypedefedName, ITypedefDeclaration typedefDeclaration) {
			this.preTypedefedName = preTypedefedName;
			this.typedefDeclaration = typedefDeclaration;
		}
	}

	private CompositeTypeSpecifierResult handleCompositeTypeSpecifier(IASTDeclSpecifier declSpecifier, IASTDeclarator[] declarators, boolean isTypedef) {
		ITypedefDeclaration typedefDeclaration = null;

		// Could be a union or struct, this needs to be added to the type system
		CASTCompositeTypeSpecifier compositeTypeSpecifier = (CASTCompositeTypeSpecifier) declSpecifier;
		IASTDeclaration[] members = compositeTypeSpecifier.getMembers();
		LinkedHashMap<String, String> parsedMembers = parseMembers(members);

		IASTName astName = compositeTypeSpecifier.getName();
		String preTypedefedName = astName.toString();
		if (preTypedefedName.isEmpty()) {
			preTypedefedName = "_INTERNAL_UNNAMED_TYPE_" + Integer.toString(counterForUnnamedTypes++);
		}

		String typedefTargetName = "";
		if (isTypedef) {
			assert (declarators.length == 1);
			assert (declarators[0].getName() instanceof CASTName);
			final IASTName typedefAstName = declarators[0].getName();
			IBinding typedefBinding = typedefAstName.resolveBinding();
			if (typedefBinding instanceof CTypedef) {
				final CTypedef cTypedef = (CTypedef) typedefBinding;
				logger.info(astName.toString() + " is a typedef with name '" + cTypedef.getName() + "'!");
				typedefTargetName = cTypedef.getName();
			} else {
				throw new IllegalStateException("Binding has unexpected type " + typedefBinding.getClass().getSimpleName());
			}
		}

		final int compositeType = compositeTypeSpecifier.getKey();
		if (compositeType == IASTCompositeTypeSpecifier.k_struct) {
			logger.info("Its a struct, named " + preTypedefedName + ".");
			programContext.addStruct(preTypedefedName, parsedMembers);
			if (isTypedef) {
				programContext.addTypedefdStruct(typedefTargetName, parsedMembers, preTypedefedName);
				typedefDeclaration = new StructTypedefDeclaration(typedefTargetName, parsedMembers, preTypedefedName);
			}
		} else if (compositeType == IASTCompositeTypeSpecifier.k_union) {
			logger.info("Its a union, named " + preTypedefedName + ".");
			programContext.addUnion(preTypedefedName, parsedMembers);
			if (isTypedef) {
				programContext.addTypedefdUnion(typedefTargetName, parsedMembers, preTypedefedName);
				typedefDeclaration = new UnionTypedefDeclaration(typedefTargetName, parsedMembers, preTypedefedName);
			}
		} else {
			throw new IllegalStateException("Unhandled composite type: " + compositeType);
		}
		return new CompositeTypeSpecifierResult(preTypedefedName, typedefDeclaration);
	}

	private Declaration handleDeclaration(IASTDeclSpecifier declSpecifier, IASTDeclarator[] declarators) {
		final StorageClass storageClass = StorageClass.fromCdtStorageClass(declSpecifier.getStorageClass());
		final String storageClassName = storageClass.toString();
		final boolean isTypedef = (storageClass == StorageClass.TYPEDEF);
		final boolean isCompositeTypeSpecifier = declSpecifier instanceof CASTCompositeTypeSpecifier;

		String preTypedefedName = null;
		if (isCompositeTypeSpecifier) {
			CompositeTypeSpecifierResult result = handleCompositeTypeSpecifier(declSpecifier, declarators, isTypedef);
			preTypedefedName = result.preTypedefedName;
		}

		if (isTypedef) {
			logger.error("This should not be reached anymore!");
			throw new RuntimeException("This should not be reached anymore!");
		} else {
			// Variable declaration
			final String typeName = (preTypedefedName != null) ? preTypedefedName : getTypeName(declSpecifier);
			final DataType type = programContext.getCurrentlyDefinedTypes().byName(typeName); // This will  trigger an exception if the type is unknown.

			FormulaTreeNode result = null;
			logger.info("Looking at " + declarators.length + " variable declarations of type '" + typeName + "'.");
			List<Declaration.TypeAndNamePair> members = new ArrayList<>();
			for (int i = 0; i < declarators.length; ++i) {
				DataType finalType = type;
				final IASTDeclarator declarator = declarators[i];
				final IASTPointerOperator[] pointerOperators = declarator.getPointerOperators();
				logger.info("Declaration has " + pointerOperators.length + " pointer operations attached.");
				final int pointerDepth = pointerOperators.length;
				final boolean isPointer = (pointerDepth > 0);
				for (IASTPointerOperator pop : pointerOperators) {
					if (pop instanceof CASTPointer) {
						finalType = programContext.addPointerType(finalType);
					} else {
						throw new IllegalStateException("IASTPointerOperator has unexpected type " + pop.getClass().getSimpleName());
					}
				}

				final IASTName astName = declarator.getName();
				IBinding binding = astName.resolveBinding();
				if (binding instanceof IFunction) {
					logger.info(astName.toString() + " is a function!");
					logger.warn("MISSING IMPLEMENTATION: Function declaration is ignored!");
				} else if (binding instanceof IVariable) {
					logger.info(astName.toString() + " is a variable!");

					final String variableName = astName.toString();
					logger.info("Found variable declaration, name '" + variableName + "' is defined as '" + finalType.getTypeName() + "' (storage: " + storageClassName + ").");

					// Handle initializer
					final IASTInitializer initializer = declarator.getInitializer();
					FormulaTreeNode initializerNode = null;
					if (initializer != null) {
						if (initializer instanceof CASTEqualsInitializer) {
							CASTEqualsInitializer equalsInitializer = (CASTEqualsInitializer) initializer;
							initializerNode = handleInitializerClause(equalsInitializer.getInitializerClause(), programContext.getCurrentlyDefinedTypes().byName(finalType.getTypeName()), false);
						} else {
							throw new IllegalStateException("IASTInitializer has unexpected type " + initializer.getClass().getSimpleName());
						}
					}

					members.add(new Declaration.TypeAndNamePair(finalType.getTypeName(), variableName, initializerNode));
				} else {
					logger.warn("Unhandled Binding of " + astName.toString() + " has type " + binding.getClass().getSimpleName() + ".");
					throw new RuntimeException("Unhandled Binding of " + astName.toString() + " has type " + binding.getClass().getSimpleName() + ".");
				}
			}
			return new Declaration(ImmutableList.copyOf(members));
		}
	}

	public Declaration handleSimpleDeclaration(CASTSimpleDeclaration simpleDeclaration) {
		final IASTDeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
		final IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();
		return handleDeclaration(declSpecifier, declarators);
	}

	private DataType getTypeOfMember(DataType type) {
		if (type instanceof IArrayType) {
			final IArrayType arrayType = (IArrayType) type;
			return arrayType.getArrayType();
		} else if (type instanceof IAliasType) {
			final IAliasType aliasType = (IAliasType) type;
			return getTypeOfMember(aliasType.getBaseType());
		}
		return type;
	}

	private FormulaTreeNode handleInitializerClause(IASTInitializerClause initializerClause, DataType targetType, boolean isInOutput) {
		if (initializerClause instanceof CASTInitializerList) {
			CASTInitializerList initializerList = (CASTInitializerList) initializerClause;
			List<FormulaTreeNode> childInitializers = new ArrayList<>();

			final DataType childType = getTypeOfMember(targetType);
			for (IASTInitializerClause subClause: initializerList.getClauses()) {
				childInitializers.add(handleInitializerClause(subClause, childType, isInOutput));
			}

			return new CurlyBracesValueTreeNode(targetType, ImmutableList.copyOf(childInitializers), isInOutput);
		} else if (initializerClause instanceof IASTExpression){
			IASTExpression expression = (IASTExpression) initializerClause;
			return handleExpression(expression, isInOutput);
		} else {
			throw new IllegalStateException("Unhandled type of initializer clause: " + initializerClause.getClass().getSimpleName());
		}
	}

	private String getTypeName(IASTDeclSpecifier declSpecifier) {
		if (declSpecifier instanceof CASTTypedefNameSpecifier) {
			CASTTypedefNameSpecifier typedefNameSpecifier = (CASTTypedefNameSpecifier) declSpecifier;
			IASTName astName = typedefNameSpecifier.getName();
			final String result = astName.toString();
			assert (!result.isEmpty());
			return result;
		} else if (declSpecifier instanceof CASTSimpleDeclSpecifier) {
			CASTSimpleDeclSpecifier simpleDeclSpecifier = (CASTSimpleDeclSpecifier) declSpecifier;
			int typeId = simpleDeclSpecifier.getType();
			final String result = BaseType.fromCdtTypeId(typeId).toTypeName();
			assert (!result.isEmpty());
			return result;
		} else if (declSpecifier instanceof ICASTCompositeTypeSpecifier) {
			ICASTCompositeTypeSpecifier compositeTypeSpecifier = (ICASTCompositeTypeSpecifier) declSpecifier;
			IASTName astName = compositeTypeSpecifier.getName();
			final String result = astName.toString();
			assert (!result.isEmpty());
			return result;
		} else {
			throw new IllegalStateException("Unhandled type of declSpecifier: " + declSpecifier.getClass().getSimpleName());
		}
	}

	public FunctionDeclaration handleFunctionDefinition(CASTFunctionDefinition functionDefinition) {
		final String returnTypeString = functionDefinition.getDeclSpecifier().getRawSignature();
		final String functionName = functionDefinition.getDeclarator().getRawSignature();
		final IASTStatement functionBody = functionDefinition.getBody();

		programContext.enterScope();

		// Parse the parameters and make them known to the context
		List<IVariableWithAccessor> parameterVariables = new ArrayList<>();
		CASTFunctionDeclarator functionDeclarator = (CASTFunctionDeclarator) functionDefinition.getDeclarator();
		for (IASTParameterDeclaration parameterDeclaration : functionDeclarator.getParameters()) {
			IASTDeclSpecifier declSpecifier = parameterDeclaration.getDeclSpecifier();
			IASTDeclarator[] declarators = new IASTDeclarator[] { parameterDeclaration.getDeclarator() };
			Declaration declaration = handleDeclaration(declSpecifier, declarators);
			if ((declaration.declaredVariables.size() == 1) && (declaration.declaredVariables.get(0).variableName.isEmpty()) && (Objects.equals(declaration.declaredVariables.get(0).typeName, "void"))) {
				// skip the declaration, as a function declared with func(void) has no variables declared.
				continue;
			}
			FormulaTreeNode argumentNode = handleDeclaration(declaration, false);
			parameterVariables.addAll(VariableCollector.getAllVariableDeclarationsWithinTree(argumentNode));
		}

		FormulaTreeNode parsedBody = handleStatement(functionBody);
		logger.info("Found function '" + functionName + "' with return type '" + returnTypeString + "'.");
		knownFunctionNames.put(functionName, new FunctionInfo(returnTypeString, parsedBody));

		de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.IFunction functionToAddToContext = null;
		if ( ! functionName.equals("code()")) {
			// Add this function to the programContext
			if (returnTypeString.equals("void")) {
				throw new RuntimeException("Not implemented yet!");
			} else {
				String functionNameStringWithoutParameters;
				{
					int indexOfParametersStart = functionName.indexOf("(");
					functionNameStringWithoutParameters = functionName.substring(0, indexOfParametersStart);
				}
				DataType returnType = programContext.getCurrentlyDefinedTypes().byName(returnTypeString);
				functionToAddToContext = new NonVoidFunction(functionNameStringWithoutParameters, parameterVariables, parsedBody, returnType);
			}
		}

		programContext.exitScope();
		if (functionToAddToContext != null) {
			programContext.addFunction(functionToAddToContext);
		}

		return new FunctionDeclaration(functionName, returnTypeString, parsedBody);
	}

	protected FormulaTreeNode handleStatement(IASTStatement statement) {
		if (statement instanceof CASTCompoundStatement) {
			CASTCompoundStatement compoundStatement = (CASTCompoundStatement) statement;
			List<FormulaTreeNode> nodes = handleCompoundStatement(compoundStatement);
			FormulaTreeNode result = null;
			for (FormulaTreeNode node: nodes) {
				if (result == null) {
					result = node;
				} else {
					result = new ConcatenationOperator(ImmutableList.of(result, node), false);
				}
			}
			if (result == null) {
				result = new EmptyOperator(ImmutableList.of(), false);
			}
			return result;
		} else if (statement instanceof CASTDeclarationStatement) {
			CASTDeclarationStatement declarationStatement = (CASTDeclarationStatement) statement;
			return handleDeclarationStatement(declarationStatement);
		} else if (statement instanceof CASTExpressionStatement) {
			CASTExpressionStatement expressionStatement = (CASTExpressionStatement) statement;
			return handleExpressionStatement(expressionStatement);
		} else if (statement instanceof CASTReturnStatement) {
			CASTReturnStatement returnStatement = (CASTReturnStatement) statement;
			return handleReturnStatement(returnStatement);
		} else if (statement instanceof CASTIfStatement) {
			CASTIfStatement ifStatement = (CASTIfStatement) statement;
			return handleIfStatement(ifStatement);
		} else if (statement instanceof CASTSwitchStatement) {
			CASTSwitchStatement switchStatement = (CASTSwitchStatement) statement;
			return handleSwitchStatement(switchStatement);
		} else if (statement instanceof CASTCaseStatement) {
			CASTCaseStatement caseStatement = (CASTCaseStatement) statement;
			return handleCaseStatement(caseStatement);
		} else if (statement instanceof CASTBreakStatement) {
			CASTBreakStatement breakStatement = (CASTBreakStatement) statement;
			return handleBreakStatement(breakStatement);
		} else if (statement instanceof CASTContinueStatement) {
			CASTContinueStatement continueStatementStatement = (CASTContinueStatement) statement;
			return handleContinueStatement(continueStatementStatement);
		} else if (statement instanceof CASTDefaultStatement) {
			CASTDefaultStatement defaultStatement = (CASTDefaultStatement) statement;
			return handleDefaultStatement(defaultStatement);
		} else if (statement instanceof CASTForStatement) {
			CASTForStatement forStatement = (CASTForStatement) statement;
			return handleForStatement(forStatement);
		}

		else {
			throw new IllegalStateException("Unhandled statement type: " + statement.getClass().getSimpleName());
		}
	}

	private FormulaTreeNode handleForStatement(CASTForStatement forStatement) {
		final FormulaTreeNode initializer = handleStatement(forStatement.getInitializerStatement());

		if (!(initializer instanceof AssignmentOperator)) {
			throw new IllegalStateException("Initializer is not assignment, unsupported!");
		} else if (!(initializer.getChildren().get(0) instanceof VariableTreeNode)) {
			throw new IllegalStateException("Initializer does not contain variable as left-hand side, unsupported!");
		} else if (!(initializer.getChildren().get(1) instanceof ISimpleValueTreeNode)) {
			throw new IllegalStateException("Initializer does not contain constant as right-hand side, unsupported!");
		}

		final FormulaTreeNode body = handleStatement(forStatement.getBody());
		final FormulaTreeNode iterationExpression = handleExpression(forStatement.getIterationExpression(), false);
		final FormulaTreeNode conditionExpression = handleExpression(forStatement.getConditionExpression(), false);
		return new ForOperator(ImmutableList.<FormulaTreeNode>builder().add(initializer).add(conditionExpression).add(iterationExpression).add(body).build(), false);
	}

	private FormulaTreeNode handleDefaultStatement(CASTDefaultStatement defaultStatement) {
		return new DefaultOperator(ImmutableList.of(), false);
	}

	private FormulaTreeNode handleContinueStatement(CASTContinueStatement continueStatementStatement) {
		return new ContinueOperator(ImmutableList.of(), false);
	}

	private FormulaTreeNode handleBreakStatement(CASTBreakStatement breakStatement) {
		return new BreakOperator(ImmutableList.of(), false);
	}

	private FormulaTreeNode handleCaseStatement(CASTCaseStatement caseStatement) {
		final FormulaTreeNode caseExpression = handleExpression(caseStatement.getExpression(), false);
		return new CaseOperator(ImmutableList.of(caseExpression), false);
	}

	private FormulaTreeNode handleSwitchStatement(CASTSwitchStatement switchStatement) {
		final FormulaTreeNode switchControlStatement = handleExpression(switchStatement.getControllerExpression(), false);
		assert(switchStatement.getBody() instanceof CASTCompoundStatement);
		final List<FormulaTreeNode> body = handleCompoundStatement((CASTCompoundStatement) switchStatement.getBody());
		return new SwitchOperator(ImmutableList.<FormulaTreeNode>builder().add(switchControlStatement).addAll(body).build(), false);
	}

	private FormulaTreeNode handleIfStatement(CASTIfStatement ifStatement) {
		final FormulaTreeNode condition = handleExpression(ifStatement.getConditionExpression(), false);
		final FormulaTreeNode thenClause = handleStatement(ifStatement.getThenClause());
		final boolean hasElse = ifStatement.getElseClause() != null;
		if (hasElse) {
			final FormulaTreeNode elseClause = handleStatement(ifStatement.getElseClause());
			return new IteOperator(ImmutableList.of(condition, thenClause, elseClause), false);
		} else {
			return new ItOperator(ImmutableList.of(condition, thenClause), false);
		}
	}

	private FormulaTreeNode handleReturnStatement(CASTReturnStatement returnStatement) {
		final FormulaTreeNode child = handleExpression(returnStatement.getReturnValue(), false);
		return new ReturnOperator(ImmutableList.of(child), false);
	}

	protected FormulaTreeNode handleExpressionStatement(CASTExpressionStatement expressionStatement) {
		final IASTExpression expression = expressionStatement.getExpression();
		return handleExpression(expression, false);
	}

	private FormulaTreeNode handleExpression(IASTExpression expression, boolean isInOutput) {
		if (expression instanceof CASTBinaryExpression) {
			CASTBinaryExpression binaryExpression = (CASTBinaryExpression) expression;
			return handleBinaryExpression(binaryExpression, isInOutput);
		} else if (expression instanceof CASTUnaryExpression) {
			CASTUnaryExpression unaryExpression = (CASTUnaryExpression) expression;
			return handleUnaryExpression(unaryExpression, isInOutput);
		} else if (expression instanceof CASTIdExpression) {
			CASTIdExpression idExpression = (CASTIdExpression) expression;
			return handleIdExpression(idExpression, isInOutput);
		} else if (expression instanceof CASTLiteralExpression) {
			CASTLiteralExpression literalExpression = (CASTLiteralExpression) expression;
			return handleLiteralExpression(literalExpression, isInOutput);
		} else if (expression instanceof CASTFieldReference) {
			CASTFieldReference fieldReference = (CASTFieldReference) expression;
			return handleFieldReference(fieldReference, isInOutput);
		} else if (expression instanceof CASTConditionalExpression) {
			CASTConditionalExpression conditionalExpression = (CASTConditionalExpression) expression;
			return handleConditionalExpression(conditionalExpression, isInOutput);
		} else if (expression instanceof CASTFunctionCallExpression) {
			CASTFunctionCallExpression functionCallExpression = (CASTFunctionCallExpression) expression;
			return handleFunctionCallExpression(functionCallExpression, isInOutput);
		} else if (expression instanceof CASTCastExpression) {
			CASTCastExpression castExpression = (CASTCastExpression) expression;
			return handleCastExpression(castExpression, isInOutput);
		} else if (expression instanceof CASTArraySubscriptExpression) {
			CASTArraySubscriptExpression arraySubscriptExpression = (CASTArraySubscriptExpression) expression;
			return handleArraySubscriptExpression(arraySubscriptExpression, isInOutput);
		} else {
			throw new IllegalStateException("Unhandled expression type: " + expression.getClass().getSimpleName());
		}
	}

	private FormulaTreeNode handleArraySubscriptExpression(CASTArraySubscriptExpression arraySubscriptExpression, boolean isInOutput) {
		final FormulaTreeNode array = handleExpression(arraySubscriptExpression.getArrayExpression(), isInOutput);
		final FormulaTreeNode subscript = handleExpression(arraySubscriptExpression.getSubscriptExpression(), isInOutput);
		return new ArrayAccessOperator(ImmutableList.of(array, subscript), isInOutput);
	}

	private FormulaTreeNode handleCastExpression(CASTCastExpression castExpression, boolean isInOutput) {
		final IASTExpression subExpression = castExpression.getOperand();
		final FormulaTreeNode child = handleExpression(subExpression, isInOutput);
		final IASTTypeId typeId = castExpression.getTypeId();
		final IASTDeclSpecifier declSpecifier = typeId.getDeclSpecifier();
		final String typeName = getTypeName(declSpecifier);
		final DataType type = programContext.getCurrentlyDefinedTypes().byName(typeName);
		return new CastOperator(ImmutableList.of(child), type, isInOutput);
	}

	private IVariableWithAccessor resolveVariable(IASTExpression iastExpression) {
		if (iastExpression instanceof CASTIdExpression) {
			CASTIdExpression idExpression = (CASTIdExpression) iastExpression;
			final IASTName astName = idExpression.getName();
			final String name = astName.toString();
			final de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable result = programContext.getDefinedVariableByName(name);
			assert (result != null);
			return SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(result);
		} else if (iastExpression instanceof CASTFieldReference) {
			CASTFieldReference fieldReference = (CASTFieldReference) iastExpression;
			final String fieldName = fieldReference.getFieldName().toString();
			final IASTExpression fieldOwner = fieldReference.getFieldOwner();

			final IVariableWithAccessor owner = resolveVariable(fieldOwner);
			if (owner.getVariable() instanceof IPointerVariable) {
				return owner.accessPointer(programContext);
			} else if (owner.getVariable() instanceof IMemberContainer) {
				return owner.accessField(fieldName, programContext);
			} else {
				throw new IllegalStateException("Unhandled field owner type: " + owner.getClass().getSimpleName());
			}
		} else {
			throw new IllegalStateException("Unhandled expression type to resolve: " + iastExpression.getClass().getSimpleName());
		}
	}

	private FormulaTreeNode handleConditionalExpression(CASTConditionalExpression conditionalExpression, boolean isInOutput) {
		final FormulaTreeNode condition = handleExpression(conditionalExpression.getLogicalConditionExpression(), false);
		final FormulaTreeNode thenClause = handleExpression(conditionalExpression.getPositiveResultExpression(), isInOutput);
		final boolean hasElse = conditionalExpression.getNegativeResultExpression() != null;
		if (hasElse) {
			final FormulaTreeNode elseClause = handleExpression(conditionalExpression.getNegativeResultExpression(), isInOutput);
			return new IteOperator(ImmutableList.of(condition, thenClause, elseClause), false);
		} else {
			return new ItOperator(ImmutableList.of(condition, thenClause), false);
		}
	}

	private FormulaTreeNode handleFunctionCallExpression(CASTFunctionCallExpression functionCallExpression, boolean isInOutput) {

		String functionName;
		final IASTExpression nameExpression = functionCallExpression.getFunctionNameExpression();
		if (nameExpression instanceof CASTIdExpression) {
			final CASTIdExpression idExpression = (CASTIdExpression) nameExpression;
			functionName = idExpression.getName().toString();
		} else {
			throw new IllegalStateException("Unhandled expression type for function name in call: " + nameExpression.getClass().getSimpleName());
		}

		final IASTInitializerClause[] arguments = functionCallExpression.getArguments();
		List<FormulaTreeNode> argumentList = new ArrayList<>();
		final boolean childIsInOutput = isInOutput || (translateSpecialFunctions && Objects.equals(functionName, "out"));
		for (IASTInitializerClause argument : arguments) {
			argumentList.add(handleInitializerClause(argument, null, childIsInOutput));
		}

		// Handle calls to min/max/last/out
		if (translateSpecialFunctions) {
			if (Objects.equals(functionName, "min")) {
				if (argumentList.size() != 2) {
					throw new InvalidCodeException("Operator min requires exactly two arguments, found " + argumentList.size() + "!");
				}
				return new MinimumOperator(ImmutableList.copyOf(argumentList), isInOutput);
			} else if (Objects.equals(functionName, "max")) {
				if (argumentList.size() != 2) {
					throw new InvalidCodeException("Operator max requires exactly two arguments, found " + argumentList.size() + "!");
				}
				return new MaximumOperator(ImmutableList.copyOf(argumentList), isInOutput);
			} else if (Objects.equals(functionName, "out")) {
				if (argumentList.size() != 1) {
					throw new InvalidCodeException("Operator out requires exactly one argument, found " + argumentList.size() + "!");
				}
				return new OutputOperator(ImmutableList.copyOf(argumentList), isInOutput);
			} else if (Objects.equals(functionName, "last")) {
				if (argumentList.size() != 1) {
					throw new InvalidCodeException("Operator last requires exactly one argument, found " + argumentList.size() + "!");
				}
				return new LastOperator(ImmutableList.copyOf(argumentList), isInOutput);
			} else if (Objects.equals(functionName, "last_i")) {
				if (argumentList.size() != 2) {
					throw new InvalidCodeException("Operator last_i requires exactly two arguments, found " + argumentList.size() + "!");
				}
				return new LastIOperator(ImmutableList.copyOf(argumentList), isInOutput);
			}
		}

		return new FunctionCallTreeNode(functionName, argumentList, isInOutput);
	}

	private FormulaTreeNode handleFieldReference(CASTFieldReference fieldReference, boolean isInOutput) {
		final IVariableWithAccessor child = resolveVariable(fieldReference);

		return new VariableTreeNode(child, isInOutput);
	}

	private FormulaTreeNode handleLiteralExpression(CASTLiteralExpression literalExpression, boolean isInOutput) {
		final String value = new String (literalExpression.getValue());
		assert (value.length() > 0);
		logger.info("Found Literal expression: '" + value + "'.");

		final String lowerValue = value.toLowerCase(Locale.getDefault());
		final boolean isUnsigned = lowerValue.indexOf('u') != -1;
		final boolean isLong = lowerValue.indexOf('l') != -1;
		final boolean isFloat = lowerValue.endsWith("f");
		final boolean isFloatingPoint = (lowerValue.indexOf('.') != -1) || (lowerValue.indexOf('e') != -1) || isFloat;

		if (isFloatingPoint) {
			final String cleanedValue = lowerValue.replaceAll("f", "");
			return new SimpleFloatingPointValueTreeNode(isFloat ? DataType.INSTANCE_FLOAT : DataType.INSTANCE_DOUBLE, Double.parseDouble(cleanedValue), isInOutput);
		}

		if (isLong) {
			final String cleanedValue = lowerValue.replaceAll("l", "").replaceAll("u", "");
			return new SimpleIntegerValueTreeNode(isUnsigned ? DataType.INSTANCE_UINT32 : DataType.INSTANCE_INT32, Long.decode(cleanedValue), isInOutput);
		}

		final String cleanedValue = lowerValue.replaceAll("l", "").replaceAll("u", "");
		return new SimpleIntegerValueTreeNode(isUnsigned ? DataType.INSTANCE_UINT32 : DataType.INSTANCE_INT32, Long.decode(cleanedValue), isInOutput);
	}

	private static boolean isInteger(String s) {
		return isInteger(s,10);
	}

	private static boolean isInteger(String s, int radix) {
		if (s.isEmpty()) return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if(s.length() == 1) return false;
				else continue;
			}
			if (Character.digit(s.charAt(i),radix) < 0) return false;
		}
		return true;
	}

	private FormulaTreeNode handleIdExpression(CASTIdExpression idExpression, boolean isInOutput) {
		IASTName astName = idExpression.getName();
		IBinding binding = astName.resolveBinding();
		final String name = astName.toString();
		if (binding instanceof IVariable) {
			logger.info(astName + " is a variable!");
			return new VariableTreeNode(SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(programContext.getDefinedVariableByName(name)), isInOutput);
		}
		throw new IllegalStateException("Unhandled binding type: " + binding.getClass().getSimpleName());
	}

	private FormulaTreeNode handleUnaryExpression(CASTUnaryExpression unaryExpression, boolean isInOutput) {
		final int operatorId = unaryExpression.getOperator();
		final UnaryOperation unaryOperation = UnaryOperation.fromCdtUnaryOperationId(operatorId);
		final FormulaTreeNode child = handleExpression(unaryExpression.getOperand(), isInOutput);

		switch (unaryOperation) {
			case PREFIXINCR:
				return new PrefixIncrementOperator(ImmutableList.of(child), isInOutput);
			case PREFIXDECR:
				return new PrefixDecrementOperator(ImmutableList.of(child), isInOutput);
			case PLUS:
				break;
			case MINUS:
				return new MinusUnaryOperator(ImmutableList.of(child), isInOutput);
			case STAR:
				return new DereferenceOperator(ImmutableList.of(child), isInOutput);
			case AMPER:
				return new AddressOfOperator(ImmutableList.of(child), isInOutput);
			case TILDE:
				return new BitwiseNotOperator(ImmutableList.of(child), isInOutput);
			case NOT:
				return new NotOperator(ImmutableList.of(child), isInOutput);
			case SIZEOF:
				break;
			case POSTFIXINCR:
				return new PostfixIncrementOperator(ImmutableList.of(child), isInOutput);
			case POSTFIXDECR:
				return new PostfixDecrementOperator(ImmutableList.of(child), isInOutput);
			case BRACKETEDPRIMARY:
				return new ParenthesisOperator(ImmutableList.of(child), isInOutput);
			case THROW:
				break;
			case TYPEID:
				break;
		}

		throw new IllegalStateException("Unhandled unary operation type: " + unaryOperation.name());
	}

	private FormulaTreeNode handleBinaryExpression(CASTBinaryExpression binaryExpression, boolean isInOutput) {
		final int operatorId = binaryExpression.getOperator();
		final BinaryOperation binaryOperation = BinaryOperation.fromCdtBinaryOperationId(operatorId);
		final FormulaTreeNode childA = handleExpression(binaryExpression.getOperand1(), isInOutput || (binaryOperation == BinaryOperation.ASSIGN));
		final FormulaTreeNode childB = handleExpression(binaryExpression.getOperand2(), isInOutput);

		switch (binaryOperation) {
			case MULTIPLY:
				return new TimesOperator(ImmutableList.of(childA, childB), isInOutput);
			case DIVIDE:
				return new DivisionOperator(ImmutableList.of(childA, childB), isInOutput);
			case MODULO:
				break;
			case PLUS:
				return new PlusOperator(ImmutableList.of(childA, childB), isInOutput);
			case MINUS:
				return new MinusBinaryOperator(ImmutableList.of(childA, childB), isInOutput);
			case SHIFTLEFT:
				return new BitShiftLeftOperator(ImmutableList.of(childA, childB), isInOutput);
			case SHIFTRIGHT:
				return new BitShiftRightOperator(ImmutableList.of(childA, childB), isInOutput);
			case LESSTHAN:
				return new SmallerOperator(ImmutableList.of(childA, childB), isInOutput);
			case GREATERTHAN:
				return new GreaterOperator(ImmutableList.of(childA, childB), isInOutput);
			case LESSEQUAL:
				return new SmallerEqualsOperator(ImmutableList.of(childA, childB), isInOutput);
			case GREATEREQUAL:
				return new GreaterEqualsOperator(ImmutableList.of(childA, childB), isInOutput);
			case BINARYAND:
				return new BitwiseAndOperator(ImmutableList.of(childA, childB), isInOutput);
			case BINARYXOR:
				return new BitwiseXorOperator(ImmutableList.of(childA, childB), isInOutput);
			case BINARYOR:
				return new BitwiseOrOperator(ImmutableList.of(childA, childB), isInOutput);
			case LOGICALAND:
				return new AndOperator(ImmutableList.of(childA, childB), isInOutput);
			case LOGICALOR:
				return new OrOperator(ImmutableList.of(childA, childB), isInOutput);
			case ASSIGN:
				return new AssignmentOperator(ImmutableList.of(childA, childB), isInOutput);
			case MULTIPLYASSIGN:
				break;
			case DIVIDEASSIGN:
				break;
			case MODULOASSIGN:
				break;
			case PLUSASSIGN:
				break;
			case MINUSASSIGN:
				break;
			case SHIFTLEFTASSIGN:
				break;
			case SHIFTRIGHTASSIGN:
				break;
			case BINARYANDASSIGN:
				break;
			case BINARYXORASSIGN:
				break;
			case BINARYORASSIGN:
				break;
			case EQUALS:
				return new EqualsOperator(ImmutableList.of(childA, childB), isInOutput);
			case NOTEQUALS:
				return new NotEqualsOperator(ImmutableList.of(childA, childB), isInOutput);
			case PMDOT:
				break;
			case PMARROW:
				break;
			case MAX:
				break;
			case MIN:
				break;
			case ELLIPSES:
				break;
		}

		throw new IllegalStateException("Unhandled binary operation type: " + binaryOperation.name());
	}

	private FormulaTreeNode handleDeclaration(Declaration declaration, boolean isGlobal) {
		FormulaTreeNode result = null;
		for (Declaration.TypeAndNamePair typeAndNamePair : declaration.declaredVariables) {
			final DataType dataType = programContext.getCurrentlyDefinedTypes().byName(typeAndNamePair.typeName);
			de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable variable;
			assert !isGlobal || (programContext.getCurrentScopeDepth() == 1);
			// NOTE(Felix): Oh god why do we create the variable using programContext?
			//              This will create a variable with random properties, which we then have to "correct"
			variable = programContext.addVariable(currentParameterType, dataType, typeAndNamePair.variableName, typeAndNamePair.variableName);
			final VariableTreeNode variableTreeNode = new VariableTreeNode(SimpleVariableWithAccessInformation.makeVariableWithTrivialAccessInformation(variable), false);
			final FormulaTreeNode initializer = typeAndNamePair.initializer;
			final FormulaTreeNode newChild = new DeclarationOperator(initializer != null ? ImmutableList.of(variableTreeNode, initializer) : ImmutableList.of(variableTreeNode), false);
			if (result == null) {
				result = newChild;
			} else {
				result = new ConcatenationOperator(ImmutableList.of(result, newChild), false);
			}
		}
		return result;
	}

	protected FormulaTreeNode handleDeclarationStatement(CASTDeclarationStatement declarationStatement) {
		final IASTDeclaration declaration = declarationStatement.getDeclaration();
		if (declaration instanceof CASTSimpleDeclaration) {
			CASTSimpleDeclaration simpleDeclaration = (CASTSimpleDeclaration) declaration;
			final Declaration varDecl = handleSimpleDeclaration(simpleDeclaration);
			if (varDecl == null) {
				throw new IllegalStateException("Declaration in program resulted in null result: " + simpleDeclaration.getRawSignature());
			}

			return handleDeclaration(varDecl, false);
		} else {
			throw new IllegalStateException("Unhandled declaration type: " + declaration.getClass().getSimpleName());
		}
	}

	protected List<FormulaTreeNode> handleCompoundStatement(CASTCompoundStatement compoundStatement) {
		final IASTStatement[] statements = compoundStatement.getStatements();

		List<FormulaTreeNode> resultList = new ArrayList<>();
		for (int i = 0; i < statements.length; ++i) {
			FormulaTreeNode newChild = handleStatement(statements[i]);
			if (newChild == null) {
				continue;
			}
			resultList.add(newChild);
		}
		return resultList;
	}
}
