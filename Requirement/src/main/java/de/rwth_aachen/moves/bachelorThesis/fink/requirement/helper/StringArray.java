package de.rwth_aachen.moves.bachelorThesis.fink.requirement.helper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StringArray implements Comparable<StringArray> {
	private final ArrayList<String> data = new ArrayList<>();

	public StringArray() {}

	public StringArray(String s) {
		data.add(s);
	}

	// Getter

	public ArrayList<String> get() {
		return data;
	}

	public int size() {
		return data.size();
	}

	public boolean contains(String s) {
		return toString().contains(s);
	}

	// To String methods

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String s : data) {
			sb.append(s);
			sb.append("\r\n");
		}
		return sb.toString();
	}

	public String toStringProperty() {
		StringBuilder sb = new StringBuilder();
		for (String s : data) {
			sb.append(s.trim());
		}
		return sb.toString();
	}

	// Add to list (with indentation)

	public StringArray addEmptyLine() {
		data.add("");
		return this;
	}

	public StringArray addEmptyLines(int num) {
		while (num > 0) {
			data.add("");
			--num;
		}
		return this;
	}

	public StringArray add(StringArray sa) {
		data.addAll(sa.get());
		return this;
	}

	public StringArray add(String s) {
		data.add(s);
		return this;
	}

	public StringArray addIndented(StringArray sa) {
		for (String s : sa.get()) {
			data.add("\t" + s);
		}
		return this;
	}

	public StringArray addIndented(StringArray sa, int depth) {
		final String prefix = buildIndentPrefix(depth);
		for (String s : sa.get()) {
			data.add(prefix + s);
		}
		return this;
	}

	public StringArray addIndented(String s) {
		data.add("\t" + s);
		return this;
	}

	public StringArray addIndented(String s, int depth) {
		data.add(buildIndentPrefix(depth) + s);
		return this;
	}

	private String buildIndentPrefix(int depth) {
		StringBuilder sb = new StringBuilder();
		while (depth > 0) {
			sb.append('\t');
			depth--;
		}
		return sb.toString();
	}

	public StringArray addToLastLine(String s) {
		if (data.size() == 0) {
			data.add(s);
		} else {
			final int lastIndex = data.size() - 1;
			final String currentLastLine = data.get(lastIndex);
			data.set(lastIndex, currentLastLine + s);
		}
		return this;
	}

	/*
		Allows you to do this:
		Input A = "struct a = "
		Other = "{\n test\n }"
		This will result in "struct a = {\n test\n }"
	 */
	public StringArray addAllButAppendFirstLine(StringArray other) {
		if (other.size() > 0) {
			ArrayList<String> otherList = other.get();
			addToLastLine(otherList.get(0));
			for (int i = 1; i < otherList.size(); ++i) {
				add(otherList.get(i));
			}
		}
		return this;
	}

	public StringArray replace(String search, String replace) {
		Pattern regex = null;
		try {
			regex = Pattern.compile("^(\t*)" + search + "\\s*$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL | Pattern.MULTILINE);
		} catch (PatternSyntaxException ex) {
			// Syntax error in the regular expression
			throw new RuntimeException("Failed to build regex for replace: " + ex.getLocalizedMessage(), ex);
		}

		for (int i = 0; i < data.size(); ++i) {
			String s = data.get(i);
			Matcher regexMatcher = regex.matcher(s);
			if (regexMatcher.matches()) {

			}
			data.set(i, data.get(i).replace(search, replace));
		}
		return this;
	}

	@Override
	public int compareTo(StringArray o) {
		return this.toString().compareTo(o.toString());
	}
}
