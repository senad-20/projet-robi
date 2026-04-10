package exercice4;

// Examples
//(space setColor black)  
//(robi setColor yellow) 
//(space sleep 2000) 
//(space setColor white)  
//(space sleep 1000) 	
//(robi setColor red)		  
//(space sleep 1000)
//(robi translate 100 0)
//(space sleep 1000)
//(robi translate 0 50)
//(space sleep 1000)
//(robi translate -100 0)
//(space sleep 1000)
//(robi translate 0 -40)
//

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import api.Environment;
import api.Reference;
import api.SetColor;
import api.Sleep;
import api.Translate;
import graphicLayer.GRect;
import graphicLayer.GSpace;
import stree.parser.SNode;
import stree.parser.SParser;
import tools.Tools;

/**
 * Démo de l'exercice 4.1.
 *
 * L'interpréteur s'appuie désormais sur un environnement de références. Chaque
 * nom du script désigne une Reference, et chaque Reference connaît les
 * commandes qu'elle peut exécuter.
 */
public class Exercice4_1_0 {

	private static final int SPACE_WIDTH = 200;
	private static final int SPACE_HEIGHT = 100;

	/** Références accessibles par nom dans les scripts. */
	private final Environment environment = new Environment();

	public Exercice4_1_0() {
		initializeEnvironment();
		mainLoop();
	}

	/**
	 * Prépare la scène graphique et enregistre les références de départ.
	 */
	private void initializeEnvironment() {
		GSpace space = new GSpace("Exercice 4.1", new Dimension(SPACE_WIDTH, SPACE_HEIGHT));
		GRect robi = new GRect();

		space.addElement(robi);
		space.open();

		Reference spaceRef = new Reference(space);
		Reference robiRef = new Reference(robi);

		spaceRef.addCommand("setColor", new SetColor());
		spaceRef.addCommand("sleep", new Sleep());

		robiRef.addCommand("setColor", new SetColor());
		robiRef.addCommand("translate", new Translate());

		environment.addReference("space", spaceRef);
		environment.addReference("robi", robiRef);
	}

	/**
	 * Lit des S-expressions au clavier, les compile, puis les exécute.
	 */
	private void mainLoop() {
		SParser<SNode> parser = new SParser<>();

		while (true) {
			System.out.print("> ");
			String input = Tools.readKeyboard();

			try {
				List<SNode> expressions = parser.parse(input);

				for (SNode expr : expressions) {
					run(expr);
				}
			} catch (IOException e) {
				System.err.println("Erreur de parsing : " + e.getMessage());
			} catch (RuntimeException e) {
				System.err.println("Erreur d'exécution : " + e.getMessage());
			}
		}
	}

	/**
	 * Résout le nom du receveur puis délègue l'exécution à sa référence.
	 */
	private void run(SNode expr) {
		String receiverName = expr.get(0).contents();
		Reference receiver = environment.getReferenceByName(receiverName);
		receiver.run(expr);
	}

	public static void main(String[] args) {
		new Exercice4_1_0();
	}
}