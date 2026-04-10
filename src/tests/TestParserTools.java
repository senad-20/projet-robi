package tests;

import java.io.IOException;
import java.util.List;

import stree.parser.SNode;
import stree.parser.SParser;

/**
 * Small helper class for parsing test expressions.
 */
public final class TestParserTools {

	private TestParserTools() {
	}

	public static SNode expr(String source) {
		SParser<SNode> parser = new SParser<>();
		try {
			List<SNode> nodes = parser.parse(source);
			if (nodes == null || nodes.isEmpty()) {
				throw new AssertionError("No expression parsed from: " + source);
			}
			return nodes.get(0);
		} catch (IOException e) {
			throw new AssertionError("Parsing failed for: " + source, e);
		}
	}

	public static List<SNode> exprs(String source) {
		SParser<SNode> parser = new SParser<>();
		try {
			return parser.parse(source);
		} catch (IOException e) {
			throw new AssertionError("Parsing failed for: " + source, e);
		}
	}
}