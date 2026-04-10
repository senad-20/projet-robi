package api;

import graphicLayer.GContainer;
import graphicLayer.GElement;
import stree.parser.SNode;

/**
 * Supprime un élément d'un conteneur et retire sa référence ainsi que toutes
 * ses sous-références de l'environnement.
 *
 * Exemple : (space del robi)
 */
public class DelElement implements Command {

	private Environment environment;

	public DelElement(Environment environment) {
		this.environment = environment;
	}

	@Override
	public Reference run(Reference receiver, SNode method) {
		Object container = receiver.getReceiver();

		if (!(container instanceof GContainer)) {
			throw new Error("del non supporté pour " + container.getClass().getName());
		}

		String localName = method.get(2).contents();
		String fullName = receiver.getName() + "." + localName;

		Reference refToDelete = environment.getReferenceByName(fullName);
		Object element = refToDelete.getReceiver();

		if (!(element instanceof GElement)) {
			throw new Error("L'objet à supprimer n'est pas un élément graphique");
		}

		((GContainer) container).removeElement((GElement) element);
		environment.removeReferenceTree(fullName);
		((GContainer) container).repaint();

		return receiver;
	}
}