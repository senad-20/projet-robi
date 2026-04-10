package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import api.AddElement;
import api.DelElement;
import api.Environment;
import api.NewElement;
import api.Reference;
import api.ValueReference;
import graphicLayer.GRect;
import graphicLayer.GSpace;

public class DelElementTest {

	private Environment environment;
	private Reference spaceRef;

	@BeforeEach
	void setUp() {
		environment = new Environment();

		GSpace space = new GSpace("test-space", new Dimension(300, 200));
		spaceRef = new Reference(space);
		spaceRef.setName("space");
		spaceRef.addCommand("add", new AddElement(environment));
		spaceRef.addCommand("del", new DelElement(environment));

		environment.addReference("space", spaceRef);

		Reference rectClassRef = new Reference(GRect.class);
		rectClassRef.addCommand("new", new NewElement(environment));
		environment.addReference("Rect", rectClassRef);
	}

	@Test
	void shouldDeleteLeafElement() {
		spaceRef.run(TestParserTools.expr("(space add robi (Rect new))"));

		spaceRef.run(TestParserTools.expr("(space del robi)"));

		assertFalse(environment.hasReference("space.robi"));
	}

	@Test
	void shouldDeleteWholeQualifiedSubtree() {
		spaceRef.run(TestParserTools.expr("(space add robi (Rect new))"));

		Reference robi = environment.getReferenceByName("space.robi");
		robi.run(TestParserTools.expr("(space.robi add arm (Rect new))"));

		Reference arm = environment.getReferenceByName("space.robi.arm");
		arm.run(TestParserTools.expr("(space.robi.arm add hand (Rect new))"));

		spaceRef.run(TestParserTools.expr("(space del robi)"));

		assertFalse(environment.hasReference("space.robi"));
		assertFalse(environment.hasReference("space.robi.arm"));
		assertFalse(environment.hasReference("space.robi.arm.hand"));
	}

	@Test
	void shouldNotDeleteSiblingBranches() {
		spaceRef.run(TestParserTools.expr("(space add a (Rect new))"));
		spaceRef.run(TestParserTools.expr("(space add b (Rect new))"));

		Reference a = environment.getReferenceByName("space.a");
		a.run(TestParserTools.expr("(space.a add child (Rect new))"));

		spaceRef.run(TestParserTools.expr("(space del a)"));

		assertFalse(environment.hasReference("space.a"));
		assertFalse(environment.hasReference("space.a.child"));
		assertTrue(environment.hasReference("space.b"));
	}

	@Test
	void shouldReturnReceiverReference() {
		spaceRef.run(TestParserTools.expr("(space add robi (Rect new))"));

		Reference result = spaceRef.run(TestParserTools.expr("(space del robi)"));

		assertSame(spaceRef, result);
	}

	@Test
	void shouldThrowWhenReceiverIsNotAContainer() {
		Reference nonContainer = new ValueReference(10);
		nonContainer.setName("value");
		nonContainer.addCommand("del", new DelElement(environment));

		Error error = assertThrows(Error.class, () -> nonContainer.run(TestParserTools.expr("(value del x)")));

		assertTrue(error.getMessage().contains("del non supporté"));
	}

	@Test
	void shouldThrowWhenTargetReferenceDoesNotExist() {
		Error error = assertThrows(Error.class, () -> spaceRef.run(TestParserTools.expr("(space del missing)")));

		assertTrue(error.getMessage().contains("Référence inconnue"));
	}

	@Test
	void shouldThrowWhenTargetIsNotGraphicElement() {
		environment.addReference("space.bad", new ValueReference(12));

		Error error = assertThrows(Error.class, () -> spaceRef.run(TestParserTools.expr("(space del bad)")));

		assertTrue(error.getMessage().contains("n'est pas un élément graphique"));
	}
}