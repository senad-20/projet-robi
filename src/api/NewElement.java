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
			Object rawReceiver = receiver.getReceiver();

			if (!(rawReceiver instanceof Class<?>)) {
				throw new Error("Impossible de créer l'élément : " + method);
			}

			Class<?> clazz = (Class<?>) rawReceiver;

			if (!GElement.class.isAssignableFrom(clazz)) {
				throw new Error("Impossible de créer l'élément : " + method);
			}

			@SuppressWarnings("unchecked")
			Class<? extends GElement> graphicClass = (Class<? extends GElement>) clazz;

			GElement element = graphicClass.getDeclaredConstructor().newInstance();
			return GraphicReferenceFactory.createGraphicReference(element, environment);

		} catch (Error e) {
			throw e;
		} catch (Exception e) {
			throw new Error("Impossible de créer l'élément : " + method);
		}
	}
}