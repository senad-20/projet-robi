package api;

import graphicLayer.GContainer;
import graphicLayer.GElement;
import stree.parser.SNode;

/**
 * Ajoute dynamiquement un élément graphique dans un conteneur et l'enregistre
 * dans l'environnement avec un nom qualifié.
 *
 * Exemples : (space add robi (rect.class new)) (space.robi add im (image.class
 * new alien.gif))
 */
public class AddElement implements Command {

	private Environment environment;
	private Interpreter interpreter;

	public AddElement(Environment environment) {
		this.environment = environment;
		this.interpreter = new Interpreter();
	}

	@Override
	public Reference run(Reference receiver, SNode method) {
		String localName = method.get(2).contents();
		SNode creationExpr = method.get(3);

		Reference newReference = interpreter.compute(environment, creationExpr);

		Object container = receiver.getReceiver();
		Object element = newReference.getReceiver();

		if (!(container instanceof GContainer)) {
			throw new Error("add non supporté pour " + container.getClass().getName());
		}

		if (!(element instanceof GElement)) {
			throw new Error("L'objet créé n'est pas un élément graphique");
		}

		((GContainer) container).addElement((GElement) element);

		((GContainer) container).repaint();

		String fullName = receiver.getName() + "." + localName;
		environment.addReference(fullName, newReference);

		return newReference;
	}
}