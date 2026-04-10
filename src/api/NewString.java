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
			Object rawReceiver = receiver.getReceiver();

			if (!(rawReceiver instanceof Class<?>)) {
				throw new Error("Impossible de créer le label : " + method);
			}

			Class<?> clazz = (Class<?>) rawReceiver;

			if (!GElement.class.isAssignableFrom(clazz)) {
				throw new Error("Impossible de créer le label : " + method);
			}

			@SuppressWarnings("unchecked")
			Class<? extends GElement> graphicClass = (Class<? extends GElement>) clazz;

			Constructor<? extends GElement> ctor = graphicClass.getDeclaredConstructor(String.class);
			GElement element = ctor.newInstance(text);

			return GraphicReferenceFactory.createGraphicReference(element, environment);

		} catch (Error e) {
			throw e;
		} catch (Exception e) {
			throw new Error("Impossible de créer le label : " + method);
		}
	}
}