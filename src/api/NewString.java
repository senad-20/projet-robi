package api;

import java.lang.reflect.Constructor;

import graphicLayer.GElement;
import stree.parser.SNode;

/**
 * Crée dynamiquement un texte graphique.
 */
public class NewString implements Command {

	private Environment environment;

	public NewString(Environment environment) {
		this.environment = environment;
	}

	@Override
	public Reference run(Reference receiver, SNode method) {
		try {
			String text = method.get(2).contents();

			@SuppressWarnings("unchecked")
			Class<? extends GElement> clazz = (Class<? extends GElement>) receiver.getReceiver();

			Constructor<? extends GElement> ctor = clazz.getDeclaredConstructor(String.class);
			GElement element = ctor.newInstance(text);

			return GraphicReferenceFactory.createGraphicReference(element, environment);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Impossible de créer le label : " + method);
		}
	}
}