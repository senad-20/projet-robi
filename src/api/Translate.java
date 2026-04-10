package api;

import java.awt.Point;

import graphicLayer.GElement;
import stree.parser.SNode;

/**
 * Primitive translate pour les éléments graphiques.
 */
public class Translate implements Command {

	@Override
	public Reference run(Reference receiver, SNode method) {
		int dx = Integer.parseInt(method.get(2).contents());
		int dy = Integer.parseInt(method.get(3).contents());

		Object target = receiver.getReceiver();

		if (!(target instanceof GElement)) {
			throw new Error("translate non supporté pour " + target.getClass().getName());
		}

		((GElement) target).translate(new Point(dx, dy));
		return receiver;
	}
}