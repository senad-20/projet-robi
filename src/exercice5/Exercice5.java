package exercice5;

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

/**
 * Démo de l'exercice 5.
 *
 * Cette version montre l'ajout d'éléments dans des conteneurs graphiques,
 * l'utilisation de noms qualifiés et la suppression d'un sous-arbre complet.
 */
public class Exercice5 {

	private static final int SPACE_WIDTH = 300;
	private static final int SPACE_HEIGHT = 220;

	private final Environment environment = new Environment();
	private final Interpreter interpreter = new Interpreter();
	private final GSpace space = new GSpace("Exercice 5", new Dimension(SPACE_WIDTH, SPACE_HEIGHT));

	/**
	 * Script de démonstration exécuté au lancement.
	 */
	private final String demoScript = "" + "(space setDim 320 240) " + "(space setColor white) "

			+ "(space sleep 800) " + "(space add robi (Rect new)) "

			+ "(space sleep 800) " + "(space.robi setColor white) " + "(space.robi setDim 200 150) "

			+ "(space sleep 800) " + "(space.robi translate 40 30) "

			+ "(space sleep 1000) " + "(space.robi add inner (Rect new)) "

			+ "(space sleep 800) " + "(space.robi.inner setColor red) " + "(space.robi.inner setDim 60 60) "

			+ "(space sleep 800) " + "(space.robi.inner translate 20 20) "

			+ "(space sleep 1000) " + "(space.robi add img (Image new alien.gif)) "

			+ "(space sleep 800) " + "(space.robi.img translate 105 25) "

			+ "(space sleep 1000) " + "(space.robi add txt (Label new DemoEx5)) "

			+ "(space sleep 800) " + "(space.robi.txt setColor black) " + "(space.robi.txt translate 20 115) "

			+ "(space sleep 1200) " + "(space.robi.inner translate 30 0) "

			+ "(space sleep 600) " + "(space.robi.inner translate 0 30) "

			+ "(space sleep 600) " + "(space.robi.inner translate -30 0) "

			+ "(space sleep 600) " + "(space.robi.inner translate 0 -30) "

			+ "(space sleep 1200) " + "(space.robi translate 20 0) "

			+ "(space sleep 800) " + "(space.robi translate 0 20) "

			+ "(space sleep 1500) " + "(space del robi)";

	public Exercice5() {
		initializeEnvironment();
		space.open();
		oneShot(demoScript);
	}

	/**
	 * Initialise l'environnement avec les références de base et les commandes
	 * nécessaires à la création d'éléments imbriqués.
	 */
	private void initializeEnvironment() {
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
		environment.addReference("Rect", rectClassRef);
		environment.addReference("Oval", ovalClassRef);
		environment.addReference("Image", imageClassRef);
		environment.addReference("Label", stringClassRef);
	}

	/**
	 * Compile puis exécute un script complet.
	 */
	public void oneShot(String script) {
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

	public Environment getEnvironment() {
		return environment;
	}

	public GSpace getSpace() {
		return space;
	}

	public static void main(String[] args) {
		new Exercice5();
	}
}