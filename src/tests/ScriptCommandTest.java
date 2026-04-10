package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import api.AddScript;
import api.Environment;
import api.Interpreter;
import api.Reference;
import api.StoredScript;
import api.ValueReference;

public class ScriptCommandTest {

	private Environment environment;
	private Interpreter interpreter;
	private Reference host;

	@BeforeEach
	void setUp() {
		environment = new Environment();
		interpreter = new Interpreter();

		host = new Reference("host");
		host.setName("obj");
		host.addCommand("addScript", new AddScript(environment));

		host.addCommand("storeName", (receiver, method) -> {
			String variableName = method.get(2).contents();
			environment.addReference(variableName, new ValueReference(receiver.getName()));
			return receiver;
		});

		host.addCommand("storeArg", (receiver, method) -> {
			String variableName = method.get(2).contents();
			String value = method.get(3).contents();
			environment.addReference(variableName, new ValueReference(value));
			return receiver;
		});

		environment.addReference("obj", host);
	}

	@Test
	void shouldRejectWrongNumberOfArguments() {
		interpreter.compute(environment, TestParserTools.expr("(obj addScript oneArg ( (self x) (self) ))"));

		Error error = assertThrows(Error.class,
				() -> interpreter.compute(environment, TestParserTools.expr("(obj oneArg a b)")));

		assertTrue(error.getMessage().contains("Nombre d'arguments invalide"));
	}

	@Test
	void shouldBindSelfToReceiverQualifiedName() {
		interpreter.compute(environment,
				TestParserTools.expr("(obj addScript saveSelf ( (self target) (self storeName target) ))"));

		interpreter.compute(environment, TestParserTools.expr("(obj saveSelf result)"));

		assertEquals("obj", environment.getReferenceByName("result").getReceiver());
	}

	@Test
	void shouldBindSimpleArguments() {
		interpreter.compute(environment,
				TestParserTools.expr("(obj addScript saveArg ( (self x) (self storeArg result x) ))"));

		interpreter.compute(environment, TestParserTools.expr("(obj saveArg hello)"));

		assertEquals("hello", environment.getReferenceByName("result").getReceiver());
	}

	@Test
	void shouldExecuteAllBodyExpressionsInOrder() {
		interpreter.compute(environment, TestParserTools
				.expr("(obj addScript twoSteps ( (self a b) (self storeArg first a) (self storeArg second b) ))"));

		interpreter.compute(environment, TestParserTools.expr("(obj twoSteps hello world)"));

		assertEquals("hello", environment.getReferenceByName("first").getReceiver());
		assertEquals("world", environment.getReferenceByName("second").getReceiver());
	}

	@Test
	void shouldReturnReceiverAfterScriptExecution() {
		interpreter.compute(environment,
				TestParserTools.expr("(obj addScript noop ( (self) (self storeArg done ok) ))"));

		Reference result = interpreter.compute(environment, TestParserTools.expr("(obj noop)"));

		assertSame(host, result);
	}

	@Test
	void shouldAllowBodyToUseInterpreterExpressions() {
		interpreter.compute(environment,
				TestParserTools.expr("(obj addScript compute ( (self n) (set result (+ n 5)) ))"));

		interpreter.compute(environment, TestParserTools.expr("(obj compute 7)"));

		assertEquals(12, environment.getReferenceByName("result").getReceiver());
	}

	@Test
	void shouldNotReplaceInsideLongerIdentifierNames() {
		StoredScript stored = new StoredScript(Arrays.asList("self", "x"),
				Arrays.asList(TestParserTools.expr("(self storeArg result x1)")));

		api.ScriptCommand command = new api.ScriptCommand(environment, stored);

		host.addCommand("manual", command);

		interpreter.compute(environment, TestParserTools.expr("(obj manual HELLO)"));

		assertEquals("x1", environment.getReferenceByName("result").getReceiver());
	}

	@Test
	void shouldWorkWithTwoDistinctCallsUsingDifferentBindings() {
		interpreter.compute(environment,
				TestParserTools.expr("(obj addScript saveArg ( (self x) (self storeArg result x) ))"));

		interpreter.compute(environment, TestParserTools.expr("(obj saveArg first)"));
		assertEquals("first", environment.getReferenceByName("result").getReceiver());

		interpreter.compute(environment, TestParserTools.expr("(obj saveArg second)"));
		assertEquals("second", environment.getReferenceByName("result").getReceiver());
	}
}