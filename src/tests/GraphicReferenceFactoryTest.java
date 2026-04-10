package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import api.Environment;
import api.GraphicReferenceFactory;
import api.Reference;
import graphicLayer.GImage;
import graphicLayer.GRect;
import graphicLayer.GString;

public class GraphicReferenceFactoryTest {

	@Test
	void createGraphicReferenceForBoundedContainerShouldExposeExpectedCommands() {
		Environment environment = new Environment();
		Reference ref = GraphicReferenceFactory.createGraphicReference(new GRect(), environment);

		assertTrue(ref.hasCommand("setColor"));
		assertTrue(ref.hasCommand("addScript"));
		assertTrue(ref.hasCommand("translate"));
		assertTrue(ref.hasCommand("setDim"));
		assertTrue(ref.hasCommand("add"));
		assertTrue(ref.hasCommand("del"));
	}

	@Test
	void createGraphicReferenceForBoundedNonContainerShouldNotExposeAddDel() {
		Environment environment = new Environment();
		Reference ref = GraphicReferenceFactory.createGraphicReference(new GString("hello"), environment);

		assertTrue(ref.hasCommand("setColor"));
		assertTrue(ref.hasCommand("addScript"));
		assertTrue(ref.hasCommand("translate"));
		assertFalse(ref.hasCommand("add"));
		assertFalse(ref.hasCommand("del"));
	}

	@Test
	void createImageReferenceShouldExposeTranslateOnly() {
		GImage image = new GImage(null);
		Reference ref = GraphicReferenceFactory.createImageReference(image);

		assertTrue(ref.hasCommand("translate"));
		assertFalse(ref.hasCommand("setColor"));
		assertFalse(ref.hasCommand("setDim"));
		assertFalse(ref.hasCommand("add"));
		assertFalse(ref.hasCommand("del"));
	}
}