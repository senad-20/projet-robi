package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import api.Environment;
import api.NewElement;
import api.Reference;
import graphicLayer.GOval;
import graphicLayer.GRect;

public class NewElementTest {

	private Environment environment;

	@BeforeEach
	void setUp() {
		environment = new Environment();
	}

	@Test
	void shouldCreateGRectInstance() {
		Reference classRef = new Reference(GRect.class);
		classRef.addCommand("new", new NewElement(environment));

		Reference result = classRef.run(TestParserTools.expr("(Rect new)"));

		assertNotNull(result);
		assertTrue(result.getReceiver() instanceof GRect);
	}

	@Test
	void shouldCreateGOvalInstance() {
		Reference classRef = new Reference(GOval.class);
		classRef.addCommand("new", new NewElement(environment));

		Reference result = classRef.run(TestParserTools.expr("(Oval new)"));

		assertNotNull(result);
		assertTrue(result.getReceiver() instanceof GOval);
	}

	@Test
	void shouldConfigureCreatedReferenceWithExpectedCommands() {
		Reference classRef = new Reference(GRect.class);
		classRef.addCommand("new", new NewElement(environment));

		Reference result = classRef.run(TestParserTools.expr("(Rect new)"));

		assertTrue(result.hasCommand("setColor"));
		assertTrue(result.hasCommand("addScript"));
		assertTrue(result.hasCommand("translate"));
		assertTrue(result.hasCommand("setDim"));
		assertTrue(result.hasCommand("add"));
		assertTrue(result.hasCommand("del"));
	}

	@Test
	void shouldThrowWhenReceiverIsNotAClass() {
		Reference invalid = new Reference("notAClass");
		invalid.addCommand("new", new NewElement(environment));

		Error error = assertThrows(Error.class, () -> invalid.run(TestParserTools.expr("(Bad new)")));

		assertTrue(error.getMessage().contains("Impossible de créer l'élément"));
	}
}