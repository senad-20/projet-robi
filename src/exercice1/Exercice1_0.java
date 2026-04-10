package exercice1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Random;

import graphicLayer.GRect;
import graphicLayer.GSpace;

/**
 * Démo de l'exercice 1.
 *
 * Un rectangle bleu parcourt les bords internes de la fenêtre dans l'ordre :
 * droite, bas, gauche, haut.
 *
 * Si la fenêtre est redimensionnée pendant l'animation, le cycle courant est
 * interrompu et reprend depuis l'origine.
 */
public class Exercice1_0 {

	/** Dimensions initiales de la fenêtre. */
	private static final int SPACE_WIDTH = 200;
	private static final int SPACE_HEIGHT = 150;

	/** Paramètres d'animation. */
	private static final int STEP = 2;
	private static final int STEP_DELAY_MS = 10;
	private static final int COLOR_INTERVAL_MS = 2000;

	private final GSpace space = new GSpace("Exercice 1", new Dimension(SPACE_WIDTH, SPACE_HEIGHT));
	private final GRect robi = new GRect();
	private final Random random = new Random();

	public Exercice1_0() {
		initializeScene();
		space.open();
		startAnimation();
	}

	/**
	 * Initialise l'état de départ.
	 */
	private void initializeScene() {
		robi.setColor(Color.BLUE);
		robi.setPosition(new Point(0, 0));
		space.addElement(robi);
	}

	/**
	 * Lance l'animation dans un thread séparé.
	 */
	private void startAnimation() {
		Thread animationThread = new Thread(() -> {
			try {
				runAnimationLoop();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		animationThread.start();
	}

	/**
	 * Boucle principale : robi suit les quatre bords du space.
	 */
	private void runAnimationLoop() throws InterruptedException {
		int lastWidth = space.getWidth();
		int lastHeight = space.getHeight();
		long lastColorChange = System.currentTimeMillis();

		while (true) {

			if (resized(lastWidth, lastHeight)) {
				lastWidth = space.getWidth();
				lastHeight = space.getHeight();
				robi.setPosition(new Point(0, 0));
			}

			/* Déplacement vers la droite */
			while (robi.getX() < space.getWidth() - robi.getWidth()) {
				if (resized(lastWidth, lastHeight)) {
					break;
				}

				robi.translate(new Point(STEP, 0));
				Thread.sleep(STEP_DELAY_MS);
				lastColorChange = updateColorIfNeeded(lastColorChange);
			}

			/* Déplacement vers le bas */
			while (robi.getY() < space.getHeight() - robi.getHeight()) {
				if (resized(lastWidth, lastHeight)) {
					break;
				}

				robi.translate(new Point(0, STEP));
				Thread.sleep(STEP_DELAY_MS);
				lastColorChange = updateColorIfNeeded(lastColorChange);
			}

			/* Déplacement vers la gauche */
			while (robi.getX() > 0) {
				if (resized(lastWidth, lastHeight)) {
					break;
				}

				robi.translate(new Point(-STEP, 0));
				Thread.sleep(STEP_DELAY_MS);
				lastColorChange = updateColorIfNeeded(lastColorChange);
			}

			/* Déplacement vers le haut */
			while (robi.getY() > 0) {
				if (resized(lastWidth, lastHeight)) {
					break;
				}

				robi.translate(new Point(0, -STEP));
				Thread.sleep(STEP_DELAY_MS);
				lastColorChange = updateColorIfNeeded(lastColorChange);
			}
		}
	}

	/**
	 * Change périodiquement la couleur pendant l'animation.
	 */
	private long updateColorIfNeeded(long lastColorChange) {
		long now = System.currentTimeMillis();

		if (now - lastColorChange >= COLOR_INTERVAL_MS) {
			robi.setColor(randomColor());
			return now;
		}

		return lastColorChange;
	}

	/**
	 * Indique si la fenêtre a été redimensionnée.
	 */
	private boolean resized(int lastWidth, int lastHeight) {
		return space.getWidth() != lastWidth || space.getHeight() != lastHeight;
	}

	/**
	 * Génère une couleur aléatoire.
	 */
	private Color randomColor() {
		return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}

	public static void main(String[] args) {
		new Exercice1_0();
	}
}