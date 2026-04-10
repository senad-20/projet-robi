package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import api.AddScript;
import api.Environment;
import api.Reference;

public class AddScriptTest {

	private Environment environment;
	private Reference host;

	@BeforeEach
	void setUp() {
		environment = new Environment();
		host = new Reference("host");
		host.setName("obj");
		host.addCommand("addScript", new AddScript(environment));
	}

	@Test
	void shouldRegisterScriptAsNewCommand() {
		host.run(TestParserTools.expr("(obj addScript ping ( (self) (self) ))"));

		assertTrue(host.hasCommand("ping"));
	}

	@Test
	void shouldReturnReceiverReference() {
		Reference result = host.run(TestParserTools.expr("(obj addScript ping ( (self) (self) ))"));

		assertSame(host, result);
	}

	@Test
	void shouldRejectWhenDefinitionIsMissing() {
		Error error = assertThrows(Error.class, () -> host.run(TestParserTools.expr("(obj addScript ping)")));

		assertTrue(error.getMessage().contains("Syntaxe invalide"));
	}

	@Test
	void shouldRejectWhenScriptDefinitionIsEmpty() {
		Error error = assertThrows(Error.class, () -> host.run(TestParserTools.expr("(obj addScript ping ())")));

		assertTrue(error.getMessage().contains("Définition de script vide"));
	}

	@Test
	void shouldRejectWhenParameterListIsEmpty() {
		Error error = assertThrows(Error.class,
				() -> host.run(TestParserTools.expr("(obj addScript ping ( () (self) ))")));

		assertTrue(error.getMessage().contains("self"));
	}

	@Test
	void shouldRejectWhenFirstParameterIsNotSelf() {
		Error error = assertThrows(Error.class,
				() -> host.run(TestParserTools.expr("(obj addScript ping ( (x y) (x) ))")));

		assertTrue(error.getMessage().contains("premier paramètre"));
	}

	@Test
	void shouldAcceptSelfOnlyScript() {
		assertDoesNotThrow(() -> host.run(TestParserTools.expr("(obj addScript noop ( (self) (self) ))")));
		assertTrue(host.hasCommand("noop"));
	}

	@Test
	void shouldAcceptSelfPlusOtherParameters() {
		assertDoesNotThrow(() -> host.run(TestParserTools.expr("(obj addScript demo ( (self x y) (self) ))")));
		assertTrue(host.hasCommand("demo"));
	}
}