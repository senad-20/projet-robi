package api;

import java.awt.Dimension;

import graphicLayer.GBounded;
import graphicLayer.GSpace;
import stree.parser.SNode;

/**
 * Primitive setDim pour space et les éléments bornés.
 */
public class SetDim implements Command {

	@Override
	public Reference run(Reference receiver, SNode method) {
		if (method.size() < 4) {
			throw new Error("setDim attend 2 arguments : " + method);
		}

		int width = Integer.parseInt(method.get(2).contents());
		int height = Integer.parseInt(method.get(3).contents());

		Object target = receiver.getReceiver();

		if (target instanceof GSpace) {
			((GSpace) target).setDimension(new Dimension(width, height));
		} else if (target instanceof GBounded) {
			((GBounded) target).setDimension(new Dimension(width, height));
		} else {
			throw new Error("setDim non supporté pour " + target.getClass().getName());
		}

		return receiver;
	}
}