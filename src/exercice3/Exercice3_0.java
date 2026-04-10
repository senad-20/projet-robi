package exercice3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.util.List;

import graphicLayer.GRect;
import graphicLayer.GSpace;
import stree.parser.SNode;
import stree.parser.SParser;

/**
 * Démo de l'exercice 3.
 *
 * Chaque S-expression est d'abord convertie en objet Command, puis exécutée sur
 * le space ou sur robi.
 */
public class Exercice3_0 {

	private static final int SPACE_WIDTH = 200;
	private static final int SPACE_HEIGHT = 100;

	private final GSpace space = new GSpace("Exercice 3", new Dimension(SPACE_WIDTH, SPACE_HEIGHT));
	private final GRect robi = new GRect();

	private final String script = "" + "(space setColor black) " + "(robi setColor yellow) " + "(space sleep 1000) "
			+ "(space setColor white) " + "(space sleep 1000) " + "(robi setColor red) " + "(space sleep 1000) "
			+ "(robi translate 100 0) " + "(space sleep 1000) " + "(robi translate 0 50) " + "(space sleep 1000) "
			+ "(robi translate -100 0) " + "(space sleep 1000) " + "(robi translate 0 -40)";

	public Exercice3_0() {
		space.addElement(robi);
		space.open();
		runScript();
	}

	/**
	 * Compile le script puis exécute chaque S-expression racine.
	 */
	private void runScript() {
		SParser<SNode> parser = new SParser<>();

		try {
			List<SNode> rootNodes = parser.parse(script);

			for (SNode expr : rootNodes) {
				run(expr);
			}
		} catch (IOException e) {
			throw new RuntimeException("Erreur de parsing du script.", e);
		}
	}

	/**
	 * Traduit une S-expression en commande puis l'exécute.
	 */
	private void run(SNode expr) {
		Command command = getCommandFromExpr(expr);

		if (command == null) {
			throw new IllegalArgumentException("Impossible d'interpréter : " + expr);
		}

		command.run();
	}

	/**
	 * Associe une S-expression à l'objet Command correspondant.
	 */
	private Command getCommandFromExpr(SNode expr) {
		String target = expr.get(0).contents();
		String action = expr.get(1).contents();

		if ("space".equals(target)) {
			if ("setColor".equals(action)) {
				return new SpaceChangeColor(parseColor(expr.get(2).contents()));
			}
			if ("sleep".equals(action)) {
				return new SpaceSleep(Integer.parseInt(expr.get(2).contents()));
			}
		}

		if ("robi".equals(target)) {
			if ("setColor".equals(action)) {
				return new RobiChangeColor(parseColor(expr.get(2).contents()));
			}
			if ("translate".equals(action)) {
				int dx = Integer.parseInt(expr.get(2).contents());
				int dy = Integer.parseInt(expr.get(3).contents());
				return new RobiTranslate(dx, dy);
			}
		}

		return null;
	}

	/**
	 * Convertit un nom de couleur du script en couleur AWT.
	 */
	private Color parseColor(String colorName) {
		switch (colorName.toLowerCase()) {
		case "black":
			return Color.BLACK;
		case "white":
			return Color.WHITE;
		case "yellow":
			return Color.YELLOW;
		case "red":
			return Color.RED;
		case "blue":
			return Color.BLUE;
		case "green":
			return Color.GREEN;
		default:
			throw new IllegalArgumentException("Couleur inconnue : " + colorName);
		}
	}

	public static void main(String[] args) {
		new Exercice3_0();
	}

	/**
	 * Représente une commande exécutable produite à partir d'une S-expression.
	 */
	public interface Command {
		void run();
	}

	/**
	 * Commande de changement de couleur du space.
	 */
	public class SpaceChangeColor implements Command {

		private final Color newColor;

		public SpaceChangeColor(Color newColor) {
			this.newColor = newColor;
		}

		@Override
		public void run() {
			space.setColor(newColor);
		}
	}

	/**
	 * Commande de pause dans l'exécution du script.
	 */
	public class SpaceSleep implements Command {

		private final int duration;

		public SpaceSleep(int duration) {
			this.duration = duration;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(duration);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Commande de changement de couleur de robi.
	 */
	public class RobiChangeColor implements Command {

		private final Color newColor;

		public RobiChangeColor(Color newColor) {
			this.newColor = newColor;
		}

		@Override
		public void run() {
			robi.setColor(newColor);
		}
	}

	/**
	 * Commande de déplacement de robi.
	 */
	public class RobiTranslate implements Command {

		private final int dx;
		private final int dy;

		public RobiTranslate(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}

		@Override
		public void run() {
			robi.translate(new Point(dx, dy));
		}
	}
}