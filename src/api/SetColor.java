package api;

import java.awt.Color;

import graphicLayer.GElement;
import graphicLayer.GSpace;
import stree.parser.SNode;

/**
 * Primitive setColor pour space et les éléments graphiques.
 */
public class SetColor implements Command {

	@Override
	public Reference run(Reference receiver, SNode method) {
		if (method.size() < 3) {
			throw new Error("setColor attend 1 argument : " + method);
		}

		String colorName = method.get(2).contents().toLowerCase();
		Color color = toColor(colorName);

		Object target = receiver.getReceiver();

		if (target instanceof GSpace) {
			((GSpace) target).setColor(color);
		} else if (target instanceof GElement) {
			((GElement) target).setColor(color);
		} else {
			throw new Error("setColor non supporté pour " + target.getClass().getName());
		}

		return receiver;
	}

	private Color toColor(String colorName) {
		switch (colorName) {
		case "black":
			return Color.BLACK;
		case "blue":
			return Color.BLUE;
		case "cyan":
			return Color.CYAN;
		case "darkgray":
		case "dark_gray":
		case "dark-grey":
			return Color.DARK_GRAY;
		case "gray":
		case "grey":
			return Color.GRAY;
		case "green":
			return Color.GREEN;
		case "lightgray":
		case "light_gray":
		case "light-grey":
			return Color.LIGHT_GRAY;
		case "magenta":
			return Color.MAGENTA;
		case "orange":
			return Color.ORANGE;
		case "pink":
			return Color.PINK;
		case "red":
			return Color.RED;
		case "white":
			return Color.WHITE;
		case "yellow":
			return Color.YELLOW;
		default:
			throw new Error("Couleur inconnue : " + colorName);
		}
	}
}