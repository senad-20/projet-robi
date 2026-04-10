package exercice2;

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
 * Démo de l'exercice 2.
 *
 * Cette version interprète une petite série de S-expressions pour piloter le
 * space et le rectangle robi.
 *
 * Commandes prises en charge : - (space setColor couleur) - (robi setColor
 * couleur) - (robi translate dx dy) - (space sleep duree)
 */
public class Exercice2_1_0 {

	private static final int SPACE_WIDTH = 200;
	private static final int SPACE_HEIGHT = 100;

	private final GSpace space = new GSpace("Exercice 2_1", new Dimension(SPACE_WIDTH, SPACE_HEIGHT));
	private final GRect robi = new GRect();

	private final String script = "" + "(space setColor white) " + "(robi setColor red) " + "(robi translate 40 0) "
			+ "(space sleep 1000) " + "(robi translate 0 40) " + "(space sleep 1000) " + "(robi translate -40 0) "
			+ "(space sleep 1000) " + "(robi translate 0 -40)";

	public Exercice2_1_0() {
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
	 * Interprète une S-expression et exécute la commande correspondante.
	 */
	private void run(SNode expr) {
		String target = expr.get(0).contents();
		String command = expr.get(1).contents();

		switch (command) {
		case "setColor":
			runSetColor(target, expr);
			break;

		case "translate":
			runTranslate(target, expr);
			break;

		case "sleep":
			runSleep(target, expr);
			break;

		default:
			throw new RuntimeException("Commande inconnue : " + command);
		}
	}

	/**
	 * Exécute une commande de changement de couleur.
	 */
	private void runSetColor(String target, SNode expr) {
		Color color = parseColor(expr.get(2).contents());

		if ("space".equals(target)) {
			space.setColor(color);
		} else if ("robi".equals(target)) {
			robi.setColor(color);
		} else {
			throw new RuntimeException("Cible inconnue pour setColor : " + target);
		}
	}

	/**
	 * Exécute une translation de robi.
	 */
	private void runTranslate(String target, SNode expr) {
		if (!"robi".equals(target)) {
			throw new RuntimeException("La commande translate ne s'applique qu'à robi.");
		}

		int dx = Integer.parseInt(expr.get(2).contents());
		int dy = Integer.parseInt(expr.get(3).contents());
		robi.translate(new Point(dx, dy));
	}

	/**
	 * Suspend l'exécution du script pendant un certain temps.
	 */
	private void runSleep(String target, SNode expr) {
		if (!"space".equals(target)) {
			throw new RuntimeException("La commande sleep ne s'applique qu'à space.");
		}

		int duration = Integer.parseInt(expr.get(2).contents());

		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interruption pendant sleep.", e);
		}
	}

	/**
	 * Convertit un nom de couleur simple en instance de Color.
	 */
	private Color parseColor(String name) {
		switch (name.toLowerCase()) {
		case "black":
			return Color.BLACK;
		case "white":
			return Color.WHITE;
		case "red":
			return Color.RED;
		case "yellow":
			return Color.YELLOW;
		case "blue":
			return Color.BLUE;
		case "green":
			return Color.GREEN;
		default:
			throw new RuntimeException("Couleur inconnue : " + name);
		}
	}

	public static void main(String[] args) {
		new Exercice2_1_0();
	}
}