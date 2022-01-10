package de.rwth_aachen.moves.bachelorThesis.fink.codeParser;

import de.rwth_aachen.moves.bachelorThesis.fink.codeParser.exceptions.InvalidCodeException;

import java.util.Stack;

public class ParserState {
	private static class ParserStateInternal {
		public final String input;
		public int position;
		public final int positionEnd;

		public ParserStateInternal(String input) {
			this.input = input;
			this.position = 0;
			this.positionEnd = input.length();
		}

		public ParserStateInternal(ParserStateInternal other, int positionEnd) {
			this.input = other.input;
			this.position = other.position;
			this.positionEnd = positionEnd;
		}
	}
	
	private final Stack<ParserStateInternal> stack = new Stack<>();
	
	public ParserState(String input) {
		stack.push(new ParserStateInternal(input));
	}
	
	public void push(int endPosition) {
		stack.push(new ParserStateInternal(stack.peek(), endPosition));
	}
	
	public void pop() {
		if (stack.isEmpty()) {
			throw new RuntimeException("Stack is empty, can not pop!");
		}
		stack.pop();
	}

	public String getInput() {
		return stack.peek().input;
	}

	public int getPosition() {
		return stack.peek().position;
	}

	public void setPosition(int position) {
		stack.peek().position = position;
	}

	public int getPositionEnd() {
		return stack.peek().positionEnd;
	}
	
	public boolean hasAvailable(int charCount) {
		return ((getPosition() + charCount - 1) < getPositionEnd());
	}

	public boolean notAtEnd() {
		return getPosition() < getPositionEnd();
	}

	public boolean matchesNext(String needle) {
		if (!hasAvailable(needle.length())) {
			return false;
		}

		return getInput().substring(getPosition(), getPosition() + needle.length()).compareTo(needle) == 0;
	}

	public int findNext(String needle) {
		return getInput().indexOf(needle, getPosition());
	}

	public int requireNext(String needle) {
		int endPos = getInput().indexOf(needle, getPosition());
		if (endPos > getPositionEnd()) {
			throw new InvalidCodeException("Required next sequence '" + needle + "' found after end!");
		} else if (endPos == -1) {
			throw new InvalidCodeException("Required next sequence '\" + needle + \"' not found!");
		}
		return endPos;
	}

	public String getCurrentLine() {
		final int endPos = getInput().indexOf('\n', getPosition());
		if (endPos > getPositionEnd()) {
			throw new InvalidCodeException("Line ends after current sequence end!");
		} else if (endPos == -1) {
			final String result = getInput().substring(getPosition());
			setPosition(getPositionEnd());
			return result;
		}
		final String result = getInput().substring(getPosition(), endPos);
		setPosition(endPos + 1);
		return result;
	}

	public boolean skipComment() {
		if (hasAvailable(2)) {
			// At least two chars
			if (matchesNext("//")) {
				// Single line comment
				int endPos = findNext("\n");
				if (endPos > getPositionEnd()) {
					throw new InvalidCodeException("Single line comment ends after sequence end");
				} else if (endPos == -1) {
					setPosition(getPositionEnd());
					return true;
				}
				setPosition(endPos + 1);
				return true;
			} else if (matchesNext("/*")) {
				// Multi-line
				int endPos = requireNext("*/");
				setPosition(endPos + 2);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean skipWhitespace() {
		boolean didSkip = false;
		while (notAtEnd()) {
			final char c = getInput().charAt(getPosition());
			if ((c == ' ') || (c == '\t') || (c == '\n')) {
				setPosition(getPosition() + 1);
				didSkip = true;
				continue;
			}
			break;
		}
		return didSkip;
	}

	public String remainder() {
		return getInput().substring(getPosition());
	}

}
