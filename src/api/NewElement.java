package api;

import graphicLayer.GElement;
import stree.parser.SNode;

/**
 * Crée dynamiquement un élément graphique sans argument.
 */
public class NewElement implements Command {

	private Environment environment;

	public NewElement(Environment environment) {
		this.environment = environment;
	}

	@Override
	public Reference run(Reference receiver, SNode method) {
		try {
			@SuppressWarnings("unchecked")
			Class<? extends GElement> clazz = (Class<? extends GElement>) receiver.getReceiver();

			GElement element = clazz.getDeclaredConstructor().newInstance();
			return GraphicReferenceFactory.createGraphicReference(element, environment);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Impossible de créer l'élément : " + method);
		}
	}
}