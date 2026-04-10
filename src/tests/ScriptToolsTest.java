package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import api.ScriptTools;
import stree.parser.SNode;

public class ScriptToolsTest {

	@Test
	void instantiateShouldReplaceSimpleToken() {
		SNode expr = TestParserTools.expr("(self add x (Rect new))");

		Map<String, String> bindings = new LinkedHashMap<>();
		bindings.put("self", "space");

		SNode result = ScriptTools.instantiate(expr, bindings);

		assertEquals("space", result.get(0).contents());
		assertEquals("add", result.get(1).contents());
		assertEquals("x", result.get(2).contents());
	}

	@Test
	void instantiateShouldReplaceMultipleTokens() {
		SNode expr = TestParserTools.expr("(self setDim w h)");

		Map<String, String> bindings = new LinkedHashMap<>();
		bindings.put("self", "space.robi");
		bindings.put("w", "40");
		bindings.put("h", "50");

		SNode result = ScriptTools.instantiate(expr, bindings);

		assertEquals("space.robi", result.get(0).contents());
		assertEquals("setDim", result.get(1).contents());
		assertEquals("40", result.get(2).contents());
		assertEquals("50", result.get(3).contents());
	}

	@Test
	void instantiateShouldReplaceInsideNestedExpressions() {
		SNode expr = TestParserTools.expr("(self add name (Rect new))");

		Map<String, String> bindings = new LinkedHashMap<>();
		bindings.put("self", "space");
		bindings.put("name", "robi");

		SNode result = ScriptTools.instantiate(expr, bindings);

		assertEquals("space", result.get(0).contents());
		assertEquals("robi", result.get(2).contents());
	}

	@Test
	void instantiateShouldNotReplaceInsideLongerIdentifiers() {
		SNode expr = TestParserTools.expr("(self capture x1)");

		Map<String, String> bindings = new LinkedHashMap<>();
		bindings.put("x", "VALUE");

		SNode result = ScriptTools.instantiate(expr, bindings);

		assertEquals("x1", result.get(2).contents());
	}

	@Test
	void instantiateShouldAllowBindingOrderToStayStable() {
		SNode expr = TestParserTools.expr("(self set a b)");

		Map<String, String> bindings = new LinkedHashMap<>();
		bindings.put("self", "obj");
		bindings.put("a", "x");
		bindings.put("b", "10");

		SNode result = ScriptTools.instantiate(expr, bindings);

		assertEquals("obj", result.get(0).contents());
		assertEquals("x", result.get(2).contents());
		assertEquals("10", result.get(3).contents());
	}

	@Test
	void instantiateShouldKeepUnboundTokensUntouched() {
		SNode expr = TestParserTools.expr("(self set x y)");

		Map<String, String> bindings = new LinkedHashMap<>();
		bindings.put("self", "obj");

		SNode result = ScriptTools.instantiate(expr, bindings);

		assertEquals("obj", result.get(0).contents());
		assertEquals("x", result.get(2).contents());
		assertEquals("y", result.get(3).contents());
	}

	@Test
	void instantiateShouldPreserveExpressionStructure() {
		SNode expr = TestParserTools.expr("(begin (self set a 1) (self set b 2))");

		Map<String, String> bindings = new LinkedHashMap<>();
		bindings.put("self", "obj");

		SNode result = ScriptTools.instantiate(expr, bindings);

		assertEquals("begin", result.get(0).contents());
		assertEquals("obj", result.get(1).get(0).contents());
		assertEquals("obj", result.get(2).get(0).contents());
	}
}