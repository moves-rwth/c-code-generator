package de.rwth_aachen.moves.bachelorThesis.fink.codeParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class KnowledgeState {


	public void addType(String existingName, String newName) {
		stack.peek().definedTypes.put(existingName, newName);
	}

	private static class KnowledgeStateInternal {
		public Map<String, String> definedTypes;

		public KnowledgeStateInternal() {
			this.definedTypes = new HashMap<>();
		}

		public KnowledgeStateInternal(KnowledgeStateInternal other) {
			this.definedTypes = new HashMap<>(other.definedTypes);
		}
	}

	private final Stack<KnowledgeStateInternal> stack = new Stack<>();

	public KnowledgeState() {
		stack.push(new KnowledgeStateInternal());
	}

	public void push(int endPosition) {
		stack.push(new KnowledgeStateInternal(stack.peek()));
	}

	public void pop() {
		if (stack.isEmpty()) {
			throw new RuntimeException("Stack is empty, can not pop!");
		}
		stack.pop();
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Knowledge State {");
		stringBuilder.append("\tKnown types: ");
		for (String type: stack.peek().definedTypes.keySet()) {
			stringBuilder.append(type).append(" => ").append(stack.peek().definedTypes.get(type)).append(", ");
		}
		stringBuilder.append("}\n");
		return stringBuilder.toString();
	}
}
