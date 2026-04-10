package api;

import graphicLayer.GBounded;
import graphicLayer.GContainer;
import graphicLayer.GElement;
import graphicLayer.GImage;

/**
 * Fabrique de références configurées pour les éléments graphiques.
 */
public final class GraphicReferenceFactory {

	private GraphicReferenceFactory() {
	}

	public static Reference createGraphicReference(GElement element, Environment environment) {
		Reference ref = new Reference(element);

		ref.addCommand("setColor", new SetColor());
		ref.addCommand("translate", new Translate());
		ref.addCommand("addScript", new AddScript(environment));

		if (element instanceof GBounded) {
			ref.addCommand("setDim", new SetDim());
		}

		if (element instanceof GContainer) {
			ref.addCommand("add", new AddElement(environment));
			ref.addCommand("del", new DelElement(environment));
		}

		return ref;
	}

	public static Reference createImageReference(GImage image) {
		Reference ref = new Reference(image);
		ref.addCommand("translate", new ImageTranslate());
		return ref;
	}
}