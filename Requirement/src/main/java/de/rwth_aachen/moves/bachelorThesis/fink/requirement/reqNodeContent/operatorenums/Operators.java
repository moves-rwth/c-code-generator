package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operatorenums;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.DataType;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.datatypes.IDataTypeContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.exceptions.UnsatisfiableConstraintsException;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleChildExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.expression.SimpleResultingExpressionConditioner;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.*;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.FormulaTreeNode;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.formulas.OperatorTreeNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * This enum represents a general operator of its type.
 * It is used for the creation of an OperatorTreeNode or the SimpleChildExpressionConditioner using a FunctionalInterface.
 */
public enum Operators {
	// Functions
	ABS(), MAXIMUM(), MINIMUM(),

	// Arithmetic operators
	PLUS(),
	MINUS_BINARY(), MINUS_UNARY(),
	TIMES(), DIVISION(), MODULO(),

	// Bitwise operators
	BIT_SHIFT_LEFT(), BIT_SHIFT_RIGHT(), BITWISE_AND(), BITWISE_NOT(), BITWISE_OR(), BITWISE_XOR(),

	// Logical operators
	AND(), OR(), NOT(),

	// Relational operators
	EQUALS(), NOT_EQUALS(),
	GREATER(), GREATER_EQUALS(),
	SMALLER(), SMALLER_EQUALS(),

	// Misc operators
	TERNARY_OPERATOR(),

	// Control structures
	CONCATENATION(), IT(), ITE(), EMPTY_CS(),

	// Control structures - not being used in random code generation
	SWITCH(), CASE(), DEFAULT(), FOR(), WHILE(), BREAK(),

	// Other operators
	ARRAY_ACCESS(), LAST_I(), LAST(),

	// OPERATORS BELOW ARE NOT USED FOR CODE GEN
	RETURN(),
	BIT_EXTRACTION(),
	OUTPUT(),
	PARENTHESIS(), EMPTY(),
	MACRO(),
	ASSIGNMENT(), DECL(),
	ADDRESS_OF(), DEREFERENCE(), CAST(),

	// Placeholder operators for convenience
	PLACEHOLDER_VARIABLE(), PLACEHOLDER_CONSTANT();

	// Init immutable static lists
	public final static ImmutableSet<Operators> functionOperatorsList = ImmutableSet.of(
			Operators.ABS, Operators.MAXIMUM, Operators.MINIMUM);

	public final static ImmutableSet<Operators> arithmeticOperatorsList = ImmutableSet.of(
			Operators.PLUS, Operators.MINUS_BINARY, Operators.MINUS_UNARY,
			Operators.TIMES, Operators.DIVISION, Operators.MODULO);

	public final static ImmutableSet<Operators> bitOperatorsList = ImmutableSet.of(
			Operators.BIT_SHIFT_LEFT, Operators.BIT_SHIFT_RIGHT, Operators.BITWISE_AND,
			Operators.BITWISE_NOT, Operators.BITWISE_OR, Operators.BITWISE_XOR);

	public final static ImmutableSet<Operators> logicalOperatorsList = ImmutableSet.of(
			Operators.AND, Operators.OR, Operators.NOT);

	public final static ImmutableSet<Operators> relationalOperatorsList = ImmutableSet.of(
			Operators.EQUALS, Operators.NOT_EQUALS,
			Operators.GREATER, Operators.GREATER_EQUALS,
			Operators.SMALLER, Operators.SMALLER_EQUALS);

	public final static ImmutableSet<Operators> controlStructuresList = ImmutableSet.of(
			Operators.IT, Operators.ITE,
			Operators.CONCATENATION, Operators.EMPTY_CS);

	static {
		ABS.builder = AbsOperator::new;
		ABS.childExpressionConditionerBuilder = AbsOperator::makeChildExpressionConditioner;
		ABS.argumentCount = AbsOperator.ARGUMENT_COUNT;
		MAXIMUM.builder = MaximumOperator::new;
		MAXIMUM.childExpressionConditionerBuilder = MaximumOperator::makeChildExpressionConditioner;
		MAXIMUM.argumentCount = MaximumOperator.ARGUMENT_COUNT;
		MINIMUM.builder = MinimumOperator::new;
		MINIMUM.childExpressionConditionerBuilder = MinimumOperator::makeChildExpressionConditioner;
		MINIMUM.argumentCount = MinimumOperator.ARGUMENT_COUNT;

		PLUS.builder = PlusOperator::new;
		PLUS.childExpressionConditionerBuilder = PlusOperator::makeChildExpressionConditioner;
		PLUS.argumentCount = PlusOperator.ARGUMENT_COUNT;
		MINUS_BINARY.builder = MinusBinaryOperator::new;
		MINUS_BINARY.childExpressionConditionerBuilder = MinusBinaryOperator::makeChildExpressionConditioner;
		MINUS_BINARY.argumentCount = MinusBinaryOperator.ARGUMENT_COUNT;
		MINUS_UNARY.builder = MinusUnaryOperator::new;
		MINUS_UNARY.childExpressionConditionerBuilder = MinusUnaryOperator::makeChildExpressionConditioner;
		MINUS_UNARY.argumentCount = MinusUnaryOperator.ARGUMENT_COUNT;
		TIMES.builder = TimesOperator::new;
		TIMES.childExpressionConditionerBuilder = TimesOperator::makeChildExpressionConditioner;
		TIMES.argumentCount = TimesOperator.ARGUMENT_COUNT;
		DIVISION.builder = DivisionOperator::new;
		DIVISION.childExpressionConditionerBuilder = DivisionOperator::makeChildExpressionConditioner;
		DIVISION.argumentCount = DivisionOperator.ARGUMENT_COUNT;
		MODULO.builder = ModuloOperator::new;
		MODULO.childExpressionConditionerBuilder = ModuloOperator::makeChildExpressionConditioner;
		MODULO.argumentCount = ModuloOperator.ARGUMENT_COUNT;

		BIT_SHIFT_LEFT.builder = BitShiftLeftOperator::new;
		BIT_SHIFT_LEFT.childExpressionConditionerBuilder = BitShiftLeftOperator::makeChildExpressionConditioner;
		BIT_SHIFT_LEFT.argumentCount = BitShiftLeftOperator.ARGUMENT_COUNT;
		BIT_SHIFT_RIGHT.builder = BitShiftRightOperator::new;
		BIT_SHIFT_RIGHT.childExpressionConditionerBuilder = BitShiftRightOperator::makeChildExpressionConditioner;
		BIT_SHIFT_RIGHT.argumentCount = BitShiftRightOperator.ARGUMENT_COUNT;
		BITWISE_AND.builder = BitwiseAndOperator::new;
		BITWISE_AND.childExpressionConditionerBuilder = BitwiseAndOperator::makeChildExpressionConditioner;
		BITWISE_AND.argumentCount = BitwiseAndOperator.ARGUMENT_COUNT;
		BITWISE_NOT.builder = BitwiseNotOperator::new;
		BITWISE_NOT.childExpressionConditionerBuilder = BitwiseNotOperator::makeChildExpressionConditioner;
		BITWISE_NOT.argumentCount = BitwiseNotOperator.ARGUMENT_COUNT;
		BITWISE_OR.builder = BitwiseOrOperator::new;
		BITWISE_OR.childExpressionConditionerBuilder = BitwiseOrOperator::makeChildExpressionConditioner;
		BITWISE_OR.argumentCount = BitwiseOrOperator.ARGUMENT_COUNT;
		BITWISE_XOR.builder = BitwiseXorOperator::new;
		BITWISE_XOR.childExpressionConditionerBuilder = BitwiseXorOperator::makeChildExpressionConditioner;
		BITWISE_XOR.argumentCount = BitwiseXorOperator.ARGUMENT_COUNT;

		AND.builder = AndOperator::new;
		AND.childExpressionConditionerBuilder = AndOperator::makeChildExpressionConditioner;
		AND.argumentCount = AndOperator.ARGUMENT_COUNT;
		OR.builder = OrOperator::new;
		OR.childExpressionConditionerBuilder = OrOperator::makeChildExpressionConditioner;
		OR.argumentCount = OrOperator.ARGUMENT_COUNT;
		NOT.builder = NotOperator::new;
		NOT.childExpressionConditionerBuilder = NotOperator::makeChildExpressionConditioner;
		NOT.argumentCount = NotOperator.ARGUMENT_COUNT;

		EQUALS.builder = EqualsOperator::new;
		EQUALS.childExpressionConditionerBuilder = EqualsOperator::makeChildExpressionConditioner;
		EQUALS.argumentCount = EqualsOperator.ARGUMENT_COUNT;
		NOT_EQUALS.builder = NotEqualsOperator::new;
		NOT_EQUALS.childExpressionConditionerBuilder = NotEqualsOperator::makeChildExpressionConditioner;
		NOT_EQUALS.argumentCount = NotEqualsOperator.ARGUMENT_COUNT;
		GREATER.builder = GreaterOperator::new;
		GREATER.childExpressionConditionerBuilder = GreaterOperator::makeChildExpressionConditioner;
		GREATER.argumentCount = GreaterOperator.ARGUMENT_COUNT;
		GREATER_EQUALS.builder = GreaterEqualsOperator::new;
		GREATER_EQUALS.childExpressionConditionerBuilder = GreaterEqualsOperator::makeChildExpressionConditioner;
		GREATER_EQUALS.argumentCount = GreaterEqualsOperator.ARGUMENT_COUNT;
		SMALLER.builder = SmallerOperator::new;
		SMALLER.childExpressionConditionerBuilder = SmallerOperator::makeChildExpressionConditioner;
		SMALLER.argumentCount = SmallerOperator.ARGUMENT_COUNT;
		SMALLER_EQUALS.builder = SmallerEqualsOperator::new;
		SMALLER_EQUALS.childExpressionConditionerBuilder = SmallerEqualsOperator::makeChildExpressionConditioner;
		SMALLER_EQUALS.argumentCount = SmallerEqualsOperator.ARGUMENT_COUNT;

		TERNARY_OPERATOR.builder = TernaryOperatorOperator::new;
		TERNARY_OPERATOR.childExpressionConditionerBuilder = TernaryOperatorOperator::makeChildExpressionConditioner;
		TERNARY_OPERATOR.argumentCount = TernaryOperatorOperator.ARGUMENT_COUNT;

		CONCATENATION.builder = ConcatenationOperator::new;
		CONCATENATION.childExpressionConditionerBuilder = ConcatenationOperator::makeChildExpressionConditioner;
		CONCATENATION.argumentCount = ConcatenationOperator.ARGUMENT_COUNT;
		CONCATENATION.codeArgumentCount = ConcatenationOperator.CODE_ARGUMENT_COUNT;
		CONCATENATION.conditionArgumentCount = ConcatenationOperator.CONDITION_ARGUMENT_COUNT;
		IT.builder = ItOperator::new;
		IT.childExpressionConditionerBuilder = ItOperator::makeChildExpressionConditioner;
		IT.argumentCount = ItOperator.ARGUMENT_COUNT;
		IT.codeArgumentCount = ItOperator.CODE_ARGUMENT_COUNT;
		IT.conditionArgumentCount = ItOperator.CONDITION_ARGUMENT_COUNT;
		ITE.builder = IteOperator::new;
		ITE.childExpressionConditionerBuilder = IteOperator::makeChildExpressionConditioner;
		ITE.argumentCount = IteOperator.ARGUMENT_COUNT;
		ITE.codeArgumentCount = IteOperator.CODE_ARGUMENT_COUNT;
		ITE.conditionArgumentCount = IteOperator.CONDITION_ARGUMENT_COUNT;
		EMPTY_CS.builder = EmptyControlStructure::new;
		EMPTY_CS.childExpressionConditionerBuilder = EmptyControlStructure::makeChildExpressionConditioner;
		EMPTY_CS.argumentCount = EmptyControlStructure.ARGUMENT_COUNT;
		EMPTY_CS.codeArgumentCount = EmptyControlStructure.CODE_ARGUMENT_COUNT;
		EMPTY_CS.conditionArgumentCount = EmptyControlStructure.CONDITION_ARGUMENT_COUNT;

		SWITCH.builder = SwitchOperator::new;
		SWITCH.childExpressionConditionerBuilder = SwitchOperator::makeChildExpressionConditioner;
		SWITCH.argumentCount = SwitchOperator.MAX_ARGUMENT_COUNT;
		SWITCH.codeArgumentCount = SwitchOperator.CODE_ARGUMENT_COUNT;
		SWITCH.conditionArgumentCount = SwitchOperator.CONDITION_ARGUMENT_COUNT;
		CASE.builder = CaseOperator::new;
		CASE.childExpressionConditionerBuilder = CaseOperator::makeChildExpressionConditioner;
		CASE.argumentCount = CaseOperator.ARGUMENT_COUNT;
		CASE.codeArgumentCount = CaseOperator.CODE_ARGUMENT_COUNT;
		CASE.conditionArgumentCount = CaseOperator.CONDITION_ARGUMENT_COUNT;
		DEFAULT.builder = DefaultOperator::new;
		DEFAULT.childExpressionConditionerBuilder = DefaultOperator::makeChildExpressionConditioner;
		DEFAULT.argumentCount = DefaultOperator.ARGUMENT_COUNT;
		DEFAULT.codeArgumentCount = DefaultOperator.CODE_ARGUMENT_COUNT;
		DEFAULT.conditionArgumentCount = DefaultOperator.CONDITION_ARGUMENT_COUNT;
		FOR.builder = ForOperator::new;
		FOR.childExpressionConditionerBuilder = ForOperator::makeChildExpressionConditioner;
		FOR.argumentCount = ForOperator.ARGUMENT_COUNT;
		FOR.codeArgumentCount = ForOperator.CODE_ARGUMENT_COUNT;
		FOR.conditionArgumentCount = ForOperator.CONDITION_ARGUMENT_COUNT;
		WHILE.builder = WhileOperator::new;
		WHILE.childExpressionConditionerBuilder = WhileOperator::makeChildExpressionConditioner;
		WHILE.argumentCount = WhileOperator.ARGUMENT_COUNT;
		WHILE.codeArgumentCount = WhileOperator.CODE_ARGUMENT_COUNT;
		WHILE.conditionArgumentCount = WhileOperator.CONDITION_ARGUMENT_COUNT;
		BREAK.builder = BreakOperator::new;
		BREAK.argumentCount = BreakOperator.ARGUMENT_COUNT;
		BREAK.codeArgumentCount = BreakOperator.CODE_ARGUMENT_COUNT;
		BREAK.conditionArgumentCount = BreakOperator.CONDITION_ARGUMENT_COUNT;

		ARRAY_ACCESS.builder = ArrayAccessOperator::new;
		ARRAY_ACCESS.childExpressionConditionerBuilder = ArrayAccessOperator::makeChildExpressionConditioner;
		ARRAY_ACCESS.argumentCount = ArrayAccessOperator.ARGUMENT_COUNT;
		LAST_I.builder = LastIOperator::new;
		LAST_I.childExpressionConditionerBuilder = LastIOperator::makeChildExpressionConditioner;
		LAST_I.argumentCount = LastIOperator.ARGUMENT_COUNT;
		LAST.builder = LastOperator::new;
		LAST.childExpressionConditionerBuilder = LastOperator::makeChildExpressionConditioner;
		LAST.argumentCount = LastOperator.ARGUMENT_COUNT;

		RETURN.builder = ReturnOperator::new;
		RETURN.childExpressionConditionerBuilder = ReturnOperator::makeChildExpressionConditioner;
		RETURN.argumentCount = ReturnOperator.ARGUMENT_COUNT;
		BIT_EXTRACTION.builder = BitExtractionOperator::new;
		BIT_EXTRACTION.childExpressionConditionerBuilder = BitExtractionOperator::makeChildExpressionConditioner;
		BIT_EXTRACTION.argumentCount = BitExtractionOperator.ARGUMENT_COUNT;
		OUTPUT.builder = OutputOperator::new;
		OUTPUT.childExpressionConditionerBuilder = OutputOperator::makeChildExpressionConditioner;
		OUTPUT.argumentCount = OutputOperator.ARGUMENT_COUNT;
		PARENTHESIS.builder = ParenthesisOperator::new;
		PARENTHESIS.childExpressionConditionerBuilder = ParenthesisOperator::makeChildExpressionConditioner;
		PARENTHESIS.argumentCount = ParenthesisOperator.ARGUMENT_COUNT;
		EMPTY.builder = EmptyOperator::new;
		EMPTY.childExpressionConditionerBuilder = EmptyOperator::makeChildExpressionConditioner;
		EMPTY.argumentCount = EmptyOperator.ARGUMENT_COUNT;
		MACRO.builder = MacroOperator::new;
		MACRO.childExpressionConditionerBuilder = MacroOperator::makeChildExpressionConditioner;
		MACRO.argumentCount = MacroOperator.ARGUMENT_COUNT;
		ASSIGNMENT.builder = AssignmentOperator::new;
		ASSIGNMENT.childExpressionConditionerBuilder = AssignmentOperator::makeChildExpressionConditioner;
		ASSIGNMENT.argumentCount = AssignmentOperator.ARGUMENT_COUNT;
		DECL.builder = DeclarationOperator::new;
		DECL.childExpressionConditionerBuilder = DeclarationOperator::makeChildExpressionConditioner;
		DECL.argumentCount = DeclarationOperator.ARGUMENT_COUNT;
		ADDRESS_OF.builder = AddressOfOperator::new;
		ADDRESS_OF.childExpressionConditionerBuilder = AddressOfOperator::makeChildExpressionConditioner;
		ADDRESS_OF.argumentCount = AddressOfOperator.ARGUMENT_COUNT;
		DEREFERENCE.builder = DereferenceOperator::new;
		DEREFERENCE.childExpressionConditionerBuilder = DereferenceOperator::makeChildExpressionConditioner;
		DEREFERENCE.argumentCount = DereferenceOperator.ARGUMENT_COUNT;
		CAST.builder = VoidPointerCastOperator::new;
		CAST.childExpressionConditionerBuilder = VoidPointerCastOperator::makeChildExpressionConditioner;
		CAST.argumentCount = VoidPointerCastOperator.ARGUMENT_COUNT;
	}

	private IOperatorTreeNodeBuilder builder = null;
	private IChildExpressionConditionerBuilder childExpressionConditionerBuilder = null;
	private int argumentCount = -1;
	private int conditionArgumentCount = 0;
	private int codeArgumentCount = 0;

	/**
	 * @return EnumSet containing all control structure enum values, which are used in random code gen
	 */
	public static EnumSet<Operators> getControlStructureOperators() { return EnumSet.copyOf(Operators.controlStructuresList); }

	public static EnumSet<Operators> all() {
		return EnumSet.allOf(Operators.class);
	}

	public static EnumSet<Operators> none() {
		return EnumSet.noneOf(Operators.class);
	}

	public static EnumSet<Operators> getPossibleChildOperatorsByType(Set<DataType> allowedReturnTypes, boolean isRangeRestricted) {
		EnumSet<Operators> result = EnumSet.noneOf(Operators.class);
		for (DataType d : allowedReturnTypes) {
			if (DataType.equal(d, DataType.INSTANCE_NONE)) {
				throw new RuntimeException("Type none should never be allowed as return type - I think?!");
			} else if (d.isBool()) {
				result.addAll(logicalOperatorsList);
				result.addAll(relationalOperatorsList);
			} else if (d.isInteger()) {
				result.addAll(functionOperatorsList);
				result.addAll(arithmeticOperatorsList);
				if (d.isSigned()) result.remove(Operators.MINUS_UNARY);
				if (!d.isBool() && !isRangeRestricted) result.addAll(bitOperatorsList);
			} else if (d.isFloatingPoint()) {
				result.addAll(functionOperatorsList);
				result.addAll(arithmeticOperatorsList);
				result.remove(Operators.MODULO);
			}
		}
		return result;
	}

	public static EnumSet<Operators> getPossibleReplaceOperators(Operators operator) {
		EnumSet<Operators> result = EnumSet.noneOf(Operators.class);

		switch (operator) {
			case ABS:
				result.add(MINUS_UNARY);
				break;
			case PLUS:
				result.add(MINUS_BINARY);
				result.add(TIMES);
				result.add(DIVISION);
				result.add(MODULO);
				break;
			case MINUS_BINARY:
				result.add(PLUS);
				result.add(TIMES);
				result.add(DIVISION);
				result.add(MODULO);
				break;
			case TIMES:
				result.add(MINUS_BINARY);
				result.add(PLUS);
				result.add(DIVISION);
				result.add(MODULO);
				break;
			case DIVISION:
			case MODULO:
				result.add(MINUS_BINARY);
				result.add(TIMES);
				result.add(PLUS);
				break;
			case OR:
				result.add(AND);
				break;
			case EQUALS:
				result.add(NOT_EQUALS);
				result.add(SMALLER);
				result.add(GREATER);
				break;
			case NOT_EQUALS:
				result.add(EQUALS);
				break;
			case GREATER:
				result.add(EQUALS);
				result.add(NOT_EQUALS);
				result.add(SMALLER_EQUALS);
				result.add(SMALLER);
			case GREATER_EQUALS:
				result.add(GREATER);
				break;
			case SMALLER:
				result.add(GREATER);
				result.add(EQUALS);
				result.add(NOT_EQUALS);
				result.add(GREATER_EQUALS);
			case SMALLER_EQUALS:
				result.add(SMALLER);
				break;
			default:
				break;
		}
		return result;
	}

	public static EnumSet<Operators> getPossibleChildOperators(OperatorReturnType childReturnType) {
		EnumSet<Operators> result = EnumSet.noneOf(Operators.class);

		switch (childReturnType) {
			case ARITHMETIC:
				result.addAll(functionOperatorsList);
				result.addAll(arithmeticOperatorsList);
				result.addAll(bitOperatorsList);
				break;
			case GENERAL_BOOLEAN:
				result.addAll(logicalOperatorsList);
				result.addAll(relationalOperatorsList);
				result.add(Operators.IT);
				result.add(Operators.ITE);
				result.add(Operators.TERNARY_OPERATOR);
				break;
			case EXECUTABLE_BOOLEAN:
				result.addAll(logicalOperatorsList);
				result.add(Operators.EQUALS);
				result.add(Operators.NOT_EQUALS);
				result.add(Operators.IT);
				result.add(Operators.ITE);
				break;
			case VARIABLE:
				result.add(Operators.PLACEHOLDER_VARIABLE);
				break;
			case CONSTANT:
				result.add(Operators.PLACEHOLDER_CONSTANT);
				break;
			case ANY:
				result.addAll(functionOperatorsList);
				result.addAll(arithmeticOperatorsList);
				result.addAll(bitOperatorsList);
				result.addAll(logicalOperatorsList);
				result.addAll(relationalOperatorsList);
				result.add(Operators.IT);
				result.add(Operators.ITE);
				result.add(Operators.EMPTY_CS);
				result.add(Operators.TERNARY_OPERATOR);
				break;
		}

		return result;
	}

	public static Operators getOperatorByString(String operator) {
		switch (operator) {
			case "abs": return Operators.ABS;
			case "max":
			case "maximum": return Operators.MAXIMUM;
			case "min":
			case "minimum": return Operators.MINIMUM;
			case "+": return Operators.PLUS;
			case "minusBinary":
			case "minus_binary": return Operators.MINUS_BINARY;
			case "minusUnary":
			case "minus_unary": return Operators.MINUS_UNARY;
			case "*": return Operators.TIMES;
			case "/": return Operators.DIVISION;
			case "%": return Operators.MODULO;
			case "<<": return Operators.BIT_SHIFT_LEFT;
			case ">>": return Operators.BIT_SHIFT_RIGHT;
			case "~": return Operators.BITWISE_NOT;
			case "&": return Operators.BITWISE_AND;
			case "|": return Operators.BITWISE_OR;
			case "^": return Operators.BITWISE_XOR;
			case "&&": return Operators.AND;
			case "||": return Operators.OR;
			case "!": return Operators.NOT;
			case "==":
			case "equals": return Operators.EQUALS;
			case "!=": return Operators.NOT_EQUALS;
			case ">": return Operators.GREATER;
			case ">=": return Operators.GREATER_EQUALS;
			case "<": return Operators.SMALLER;
			case "<=": return Operators.SMALLER_EQUALS;
			case "ternary_operator": return Operators.TERNARY_OPERATOR;
			case "concat":
			case "concatenation": return Operators.CONCATENATION;
			case "it": return Operators.IT;
			case "ite": return Operators.ITE;
			case "empty_cs": return Operators.EMPTY_CS;
			case "switch": return Operators.SWITCH;
			case "case": return Operators.CASE;
			case "default": return Operators.DEFAULT;
			case "break": return Operators.BREAK;
			case "for": return Operators.FOR;
			case "while": return Operators.WHILE;
			case "return": return Operators.RETURN;
			case "arrayAccess":
			case "array_access": return Operators.ARRAY_ACCESS;
			case "last_i": return Operators.LAST_I;
			case "last": return Operators.LAST;
			case "bit_extraction": return Operators.BIT_EXTRACTION;
			case "out":
			case "output": return Operators.OUTPUT;
			case "()":
			case "(param)": return Operators.PARENTHESIS;
			case "empty": return Operators.EMPTY;
			case "macro": return Operators.MACRO;
			case "assignment": return Operators.ASSIGNMENT;
			case "decl": return Operators.DECL;
			case "dereference": return Operators.DEREFERENCE;
			case "cast": return Operators.CAST;
			default:
				throw new RuntimeException("Could not find fitting operator to string: \"" + operator + "\" !");
		}
	}

	/**
	 * @param children The list of children for the newly created node
	 * @return A new instance of OperatorTreeNode of the same enum value
	 */
	public OperatorTreeNode operator(ImmutableList<FormulaTreeNode> children, boolean isInOutput) {
		return builder.create(children, isInOutput);
	}

	public int getArgumentCount() {
		return argumentCount;
	}

	public int getCodeArgumentCount() {
		return codeArgumentCount;
	}

	public int getConditionArgumentCount() {
		return conditionArgumentCount;
	}

	public SimpleChildExpressionConditioner getChildExpressionConditioner(int childId, SimpleChildExpressionConditioner parentConditioner, List<SimpleResultingExpressionConditioner> previousChildResultConditioners, IDataTypeContext dataTypeContext) throws UnsatisfiableConstraintsException {
		return childExpressionConditionerBuilder.getChildExpressionConditioner(childId, parentConditioner, previousChildResultConditioners, dataTypeContext);
	}
}
