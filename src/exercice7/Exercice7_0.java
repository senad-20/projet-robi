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

/**
 * Démo de l'exercice 7.
 *
 * Cette version ajoute des expressions, des conditionnelles et des boucles dans
 * le langage de script.
 */
public class Exercice7_0 {

	private static final int SPACE_WIDTH = 400;
	private static final int SPACE_HEIGHT = 250;

	private final Environment environment = new Environment();
	private final Interpreter interpreter = new Interpreter();
	private final GSpace space = new GSpace("Exercice 7", new Dimension(SPACE_WIDTH, SPACE_HEIGHT));

	/**
	 * Script de démonstration : déplacement répété de robi, puis changement de
	 * couleur selon le résultat.
	 */
	private final String demoScript = "" + "(space add robi (Rect new)) " + "(space.robi setColor blue) "
			+ "(space.robi setDim 40 40) " + "(space.robi translate 20 80) "

			+ "(set i 0) "

			+ "(while (< i 12) " + "   (space.robi translate 10 0) " + "   (space sleep 80) " + "   (set i (+ i 1)) "
			+ ") "

			+ "(if (= i 12) " + "   (space.robi setColor green) " + "   (space.robi setColor red) " + ") ";

	public Exercice7_0() {
		initializeEnvironment();
		space.open();
		runScript(demoScript);
	}

	/**
	 * Enregistre les références et les commandes disponibles dans le script.
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
	 * Compile puis exécute un script complet.
	 */
	private void runScript(String script) {
		SParser<SNode> parser = new SParser<>();

		try {
			List<SNode> expressions = parser.parse(script);

			for (SNode expr : expressions) {
				interpreter.compute(environment, expr);
			}
		} catch (IOException e) {
			throw new RuntimeException("Erreur de parsing du script.", e);
		}
	}

	public static void main(String[] args) {
		new Exercice7_0();
	}
}