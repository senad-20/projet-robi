package exercice4;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import api.AddElement;
import api.DelElement;
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
import tools.Tools;

/*
 * (space setColor white)
 * (space add robi (Rect new))
 * (robi setColor blue)
 * (robi setDim 50 50)
 * (robi translate 60 20)
 *
 * (space add cercle (Oval new))
 * (cercle setColor red)
 * (cercle translate 120 40)
 *
 * (space sleep 1000)
 * (space del cercle)
 */

/**
 * Démo de l'exercice 4.2.
 *
 * Cette version permet de créer dynamiquement des éléments graphiques puis de
 * les ajouter dans le space à partir de S-expressions.
 */
public class Exercice4_2_0 {

	private static final int SPACE_WIDTH = 200;
	private static final int SPACE_HEIGHT = 100;

	/** Environnement global utilisé par l'interpréteur. */
	private final Environment environment = new Environment();

	/** Interpréteur des S-expressions compilées. */
	private final Interpreter interpreter = new Interpreter();

	public Exercice4_2_0() {
		initializeEnvironment();
		mainLoop();
	}

	/**
	 * Prépare le space et enregistre les références disponibles dans les scripts.
	 */
	private void initializeEnvironment() {
		GSpace space = new GSpace("Exercice 4.2", new Dimension(SPACE_WIDTH, SPACE_HEIGHT));
		space.open();

		Reference spaceRef = new Reference(space);
		Reference rectClassRef = new Reference(GRect.class);
		Reference ovalClassRef = new Reference(GOval.class);
		Reference imageClassRef = new Reference(GImage.class);
		Reference stringClassRef = new Reference(GString.class);

		spaceRef.addCommand("setColor", new SetColor());
		spaceRef.addCommand("setDim", new SetDim());
		spaceRef.addCommand("sleep", new Sleep());
		spaceRef.addCommand("add", new AddElement(environment));
		spaceRef.addCommand("del", new DelElement(environment));

		rectClassRef.addCommand("new", new NewElement(environment));
		ovalClassRef.addCommand("new", new NewElement(environment));
		imageClassRef.addCommand("new", new NewImage());
		stringClassRef.addCommand("new", new NewString(environment));

		environment.addReference("space", spaceRef);
		environment.addReference("rect.class", rectClassRef);
		environment.addReference("oval.class", ovalClassRef);
		environment.addReference("image.class", imageClassRef);
		environment.addReference("label.class", stringClassRef);
	}

	/**
	 * Lit les commandes au clavier, les compile puis les exécute.
	 */
	private void mainLoop() {
		SParser<SNode> parser = new SParser<>();

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

	public static void main(String[] args) {
		new Exercice4_2_0();
	}
}