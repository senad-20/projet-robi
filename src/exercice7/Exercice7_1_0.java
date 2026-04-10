package exercice7;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import api.AddElement;
import api.AddScript;
import api.DelElement;
import api.Environment;
import api.Interpreter;
import api.NewElement;
import api.NewImage;
import api.NewString;
import api.Reference;
import api.SetColor;
import api.Sleep;
import graphicLayer.GImage;
import graphicLayer.GOval;
import graphicLayer.GRect;
import graphicLayer.GSpace;
import graphicLayer.GString;
import stree.parser.SNode;
import stree.parser.SParser;
import tools.Tools;

/**
 * Démo interactive de l'exercice 7.
 *
 * Cette version lit les S-expressions au clavier afin de tester les
 * expressions, les conditionnelles et les boucles en direct.
 */
public class Exercice7_1_0 {

	private static final int SPACE_WIDTH = 400;
	private static final int SPACE_HEIGHT = 250;

	private final Environment environment = new Environment();
	private final Interpreter interpreter = new Interpreter();
	private final GSpace space = new GSpace("Exercice 7 - Console", new Dimension(SPACE_WIDTH, SPACE_HEIGHT));

	public Exercice7_1_0() {
		initializeEnvironment();
		space.open();
		mainLoop();
	}

	/**
	 * Enregistre les références et les commandes disponibles dans l'environnement.
	 */
	private void initializeEnvironment() {
		Reference spaceRef = new Reference(space);
		spaceRef.addCommand("setColor", new SetColor());
		spaceRef.addCommand("addScript", new AddScript(environment));
		spaceRef.addCommand("add", new AddElement(environment));
		spaceRef.addCommand("del", new DelElement(environment));
		spaceRef.addCommand("sleep", new Sleep());

		Reference rectClassRef = new Reference(GRect.class);
		rectClassRef.addCommand("new", new NewElement(environment));

		Reference ovalClassRef = new Reference(GOval.class);
		ovalClassRef.addCommand("new", new NewElement(environment));

		Reference imageClassRef = new Reference(GImage.class);
		imageClassRef.addCommand("new", new NewImage());

		Reference labelClassRef = new Reference(GString.class);
		labelClassRef.addCommand("new", new NewString(environment));

		environment.addReference("space", spaceRef);
		environment.addReference("Rect", rectClassRef);
		environment.addReference("Oval", ovalClassRef);
		environment.addReference("Image", imageClassRef);
		environment.addReference("Label", labelClassRef);
	}

	/**
	 * Lit des commandes au clavier, les compile, puis les exécute.
	 */
	private void mainLoop() {
		SParser<SNode> parser = new SParser<>();

		printExamples();

		while (true) {
			System.out.print("> ");
			String input = Tools.readKeyboard();

			try {
				List<SNode> expressions = parser.parse(input);

				for (SNode expr : expressions) {
					interpreter.compute(environment, expr);
				}
			} catch (IOException e) {
				System.err.println("Erreur de parsing : " + e.getMessage());
			} catch (RuntimeException e) {
				System.err.println("Erreur d'exécution : " + e.getMessage());
			}
		}
	}

	/**
	 * Affiche quelques exemples utiles pour la démonstration.
	 */
	private void printExamples() {
		System.out.println("Exemples de commandes :");
		System.out.println("(space add robi (Rect new))");
		System.out.println("(space.robi setColor blue)");
		System.out.println("(space.robi setDim 40 40)");
		System.out.println("(space.robi translate 20 80)");
		System.out.println("(set i 0)");
		System.out.println("(while (< i 12) (space.robi translate 10 0) (space sleep 80) (set i (+ i 1)))");
		System.out.println("(if (= i 12) (space.robi setColor green) (space.robi setColor red))");
		System.out.println();
	}

	public static void main(String[] args) {
		new Exercice7_1_0();
	}
}