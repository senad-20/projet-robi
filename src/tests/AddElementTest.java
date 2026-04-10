package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import api.AddElement;
import api.Environment;
import api.NewElement;
import api.Reference;
import api.ValueReference;
import graphicLayer.GRect;
import graphicLayer.GSpace;

public class AddElementTest {

	private Environment environment;
	private Reference spaceRef;

	@BeforeEach
	void setUp() {
		environment = new Environment();

		GSpace space = new GSpace("test-space", new Dimension(300, 200));
		spaceRef = new Reference(space);
		spaceRef.setName("space");
		spaceRef.addCommand("add", new AddElement(environment));

		environment.addReference("space", spaceRef);

		Reference rectClassRef = new Reference(GRect.class);
		rectClassRef.addCommand("new", new NewElement(environment));
		environment.addReference("Rect", rectClassRef);
	}

	@Test
	void shouldAddNewElementAndRegisterQualifiedName() {
		Reference result = spaceRef.run(TestParserTools.expr("(space add robi (Rect new))"));

		assertTrue(environment.hasReference("space.robi"));
		assertSame(result, environment.getReferenceByName("space.robi"));
		assertEquals("space.robi", result.getName());
		assertTrue(result.getReceiver() instanceof GRect);
	}

	@Test
	void shouldReturnCreatedReference() {
		Reference result = spaceRef.run(TestParserTools.expr("(space add robi (Rect new))"));

		assertNotNull(result);
		assertTrue(result.getReceiver() instanceof GRect);
	}

	@Test
	void shouldAllowAddingInsideQualifiedContainerName() {
		spaceRef.run(TestParserTools.expr("(space add outer (Rect new))"));

		Reference outer = environment.getReferenceByName("space.outer");
		outer.run(TestParserTools.expr("(space.outer add inner (Rect new))"));

		assertTrue(environment.hasReference("space.outer.inner"));
	}

	@Test
	void shouldOverwriteExistingReferenceIfSameQualifiedNameIsAddedAgain() {
		spaceRef.run(TestParserTools.expr("(space add robi (Rect new))"));
		Reference first = environment.getReferenceByName("space.robi");

		spaceRef.run(TestParserTools.expr("(space add robi (Rect new))"));
		Reference second = environment.getReferenceByName("space.robi");

		assertNotSame(first, second);
	}

	@Test
	void shouldThrowWhenReceiverIsNotAContainer() {
		Reference nonContainer = new ValueReference(12);
		nonContainer.setName("value");
		nonContainer.addCommand("add", new AddElement(environment));

		Error error = assertThrows(Error.class,
				() -> nonContainer.run(TestParserTools.expr("(value add x (Rect new))")));

		assertTrue(error.getMessage().contains("add non supporté"));
	}

	@Test
	void shouldThrowWhenCreationExpressionDoesNotReturnGraphicElement() {
		Reference fakeFactory = new Reference("factory");
		fakeFactory.addCommand("new", (receiver, method) -> new ValueReference(999));
		environment.addReference("Bad", fakeFactory);

		Error error = assertThrows(Error.class, () -> spaceRef.run(TestParserTools.expr("(space add x (Bad new))")));

		assertTrue(error.getMessage().contains("n'est pas un élément graphique"));
	}

	@Test
	void shouldThrowWhenFactoryReferenceDoesNotExist() {
		Error error = assertThrows(Error.class,
				() -> spaceRef.run(TestParserTools.expr("(space add x (Missing new))")));

		assertTrue(error.getMessage().contains("Référence inconnue"));
	}
}