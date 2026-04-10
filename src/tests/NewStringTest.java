package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import api.Environment;
import api.NewString;
import api.Reference;
import graphicLayer.GString;

public class NewStringTest {

	private Environment environment;

	@BeforeEach
	void setUp() {
		environment = new Environment();
	}

	@Test
	void shouldCreateStringGraphicElement() {
		Reference classRef = new Reference(GString.class);
		classRef.addCommand("new", new NewString(environment));

		Reference result = classRef.run(TestParserTools.expr("(Label new hello)"));

		assertNotNull(result);
		assertTrue(result.getReceiver() instanceof GString);
	}

	@Test
	void shouldConfigureCreatedStringReference() {
		Reference classRef = new Reference(GString.class);
		classRef.addCommand("new", new NewString(environment));

		Reference result = classRef.run(TestParserTools.expr("(Label new hello)"));

		assertTrue(result.hasCommand("setColor"));
		assertTrue(result.hasCommand("addScript"));
		assertTrue(result.hasCommand("translate"));
	}

	@Test
	void shouldThrowWhenReceiverHasNoStringConstructor() {
		Reference classRef = new Reference(graphicLayer.GRect.class);
		classRef.addCommand("new", new NewString(environment));

		Error error = assertThrows(Error.class, () -> classRef.run(TestParserTools.expr("(BadLabel new hello)")));

		assertTrue(error.getMessage().contains("Impossible de créer le label"));
	}

	@Test
	void shouldThrowWhenReceiverIsNotAClass() {
		Reference invalid = new Reference("notAClass");
		invalid.addCommand("new", new NewString(environment));

		Error error = assertThrows(Error.class, () -> invalid.run(TestParserTools.expr("(BadLabel new hello)")));

		assertTrue(error.getMessage().contains("Impossible de créer le label"));
	}
}