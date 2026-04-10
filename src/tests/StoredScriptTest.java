package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import api.StoredScript;
import stree.parser.SNode;

public class StoredScriptTest {

	@Test
	void shouldStoreParameterNames() {
		List<String> params = Arrays.asList("self", "x", "y");
		List<SNode> body = Arrays.asList(TestParserTools.expr("(set a 1)"));

		StoredScript script = new StoredScript(params, body);

		assertEquals(params, script.getParameterNames());
	}

	@Test
	void shouldStoreBodyExpressions() {
		List<String> params = Arrays.asList("self");
		List<SNode> body = Arrays.asList(TestParserTools.expr("(set a 1)"), TestParserTools.expr("(set b 2)"));

		StoredScript script = new StoredScript(params, body);

		assertEquals(2, script.getBodyExpressions().size());
		assertEquals("set", script.getBodyExpressions().get(0).get(0).contents());
		assertEquals("set", script.getBodyExpressions().get(1).get(0).contents());
	}
}