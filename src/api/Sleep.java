package api;

import stree.parser.SNode;

/**
 * Primitive sleep.
 */
public class Sleep implements Command {

	@Override
	public Reference run(Reference receiver, SNode method) {
		int duration = Integer.parseInt(method.get(2).contents());

		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new Error("Interruption pendant sleep");
		}

		return receiver;
	}
}