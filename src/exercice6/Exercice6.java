package exercice6;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import api.AddElement;
import api.AddScript;
import api.Environment;
import api.Interpreter;
import api.NewElement;
import api.NewImage;
import api.NewString;
import api.Reference;
import api.SetColor;
import api.SetDim;
import api.Sleep;
import graphicLayer.GImage;
import graphicLayer.GOval;
import graphicLayer.GRect;
import graphicLayer.GSpace;
import graphicLayer.GString;
import stree.parser.SNode;
import stree.parser.SParser;

/**
 * Démo de l'exercice 6.
 *
 * Cette version permet d'enregistrer des scripts sur les objets graphiques puis
 * de les exécuter avec des paramètres.
 */
public class Exercice6 {

	private static final int SPACE_WIDTH = 320;
	private static final int SPACE_HEIGHT = 240;

	private final Environment environment = new Environment();
	private final Interpreter interpreter = new Interpreter();
	private final GSpace space = new GSpace("Exercice 6", new Dimension(SPACE_WIDTH, SPACE_HEIGHT));

	/**
	 * Script de démonstration : création d'un conteneur, ajout d'un script
	 * paramétré, puis réutilisation.
	 */
	private final String demoScript = "" + "(space setColor white) " + "(space setDim 320 240) "

			+ "(space add robi (Rect new)) " + "(space.robi setColor white) " + "(space.robi setDim 220 160) "
			+ "(space.robi translate 40 30) "

			+ "(space.robi addScript addRect ( " + "  (self name w c) " + "  (self add name (Rect new)) "
			+ "  (self.name setColor c) " + "  (self.name setDim w w) " + ")) "

			+ "(space sleep 800) " + "(space.robi addRect a 40 red) " + "(space.robi.a translate 20 20) "

			+ "(space sleep 800) " + "(space.robi addRect b 30 blue) " + "(space.robi.b translate 100 50) "

			+ "(space.robi addScript addImage ( " + "  (self filename) " + "  (self add img (Image new filename)) "
			+ ")) "

			+ "(space sleep 800) " + "(space.robi addImage alien.gif) " + "(space.robi.img translate 20 80) ";

	public Exercice6() {
		initializeEnvironment();
		space.open();
		runScript(demoScript);
	}

	/**
	 * Enregistre les références de base et les commandes disponibles.
	 */
	private void initializeEnvironment() {
		Reference spaceRef = new Reference(space);
		spaceRef.addCommand("setColor", new SetColor());
		spaceRef.addCommand("setDim", new SetDim());
		spaceRef.addCommand("sleep", new Sleep());
		spaceRef.addCommand("add", new AddElement(environment));
		spaceRef.addCommand("addScript", new AddScript(environment));

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
		new Exercice6();
	}
}