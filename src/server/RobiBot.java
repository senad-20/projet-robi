package server;

import java.util.Random;

import shared.ClientRequest;
import shared.ElementData;
import shared.SceneData;

/**
 * Bot graphique piloté côté serveur.
 *
 * Chaque bot contrôle un élément graphique identifié par son nom qualifié (ex.
 * space.robi1). Son comportement est décrit par une machine à états finie dont
 * les états correspondent aux quatre directions diagonales.
 *
 * À chaque tick : - le bot lit la position courante de sa cible - il anticipe
 * le déplacement suivant - s'il touche un bord, il change d'état - à chaque
 * changement d'état, il change aussi de couleur - enfin il applique le
 * déplacement via le SceneManager
 *
 * Le bot ne manipule jamais directement l'affichage client.
 */
public class RobiBot {

	private final String targetPath;
	private BotState state;
	private final int step;
	private final Random random = new Random();

	public RobiBot(String targetPath, BotState initialState, int step) {
		this.targetPath = targetPath;
		this.state = initialState;
		this.step = step;
	}

	/**
	 * Retourne la cible pilotée par ce bot.
	 */
	public String getTargetPath() {
		return targetPath;
	}

	/**
	 * Retourne l'état courant du bot.
	 */
	public BotState getState() {
		return state;
	}

	/**
	 * Exécute un tick de simulation.
	 *
	 * Le bot lit la scène courante fournie par le serveur, décide d'une éventuelle
	 * transition d'état puis demande au SceneManager d'appliquer le déplacement.
	 */
	public void tick(SceneManager sceneManager) {
		SceneData scene = sceneManager.getSceneSnapshot();
		ElementData element = sceneManager.findElementSnapshot(targetPath);

		if (scene == null || element == null) {
			return;
		}

		int parentWidth = sceneManager.getParentWidth(targetPath);
		int parentHeight = sceneManager.getParentHeight(targetPath);

		if (parentWidth <= 0 || parentHeight <= 0) {
			return;
		}

		int x = element.getX();
		int y = element.getY();
		int w = element.getWidth();
		int h = element.getHeight();

		int nextX = x + state.getDx() * step;
		int nextY = y + state.getDy() * step;

		boolean hitX = nextX < 0 || nextX + w > parentWidth;
		boolean hitY = nextY < 0 || nextY + h > parentHeight;

		boolean changed = false;

		if (hitX) {
			state = flipX(state);
			changed = true;
		}

		if (hitY) {
			state = flipY(state);
			changed = true;
		}

		if (changed) {
			changeColor(sceneManager);
		}

		move(sceneManager, state.getDx() * step, state.getDy() * step);
	}

	/**
	 * Inverse la composante horizontale de l'état.
	 */
	private BotState flipX(BotState current) {
		switch (current) {
		case UP_LEFT:
			return BotState.UP_RIGHT;
		case UP_RIGHT:
			return BotState.UP_LEFT;
		case DOWN_LEFT:
			return BotState.DOWN_RIGHT;
		case DOWN_RIGHT:
			return BotState.DOWN_LEFT;
		default:
			return current;
		}
	}

	/**
	 * Inverse la composante verticale de l'état.
	 */
	private BotState flipY(BotState current) {
		switch (current) {
		case UP_LEFT:
			return BotState.DOWN_LEFT;
		case UP_RIGHT:
			return BotState.DOWN_RIGHT;
		case DOWN_LEFT:
			return BotState.UP_LEFT;
		case DOWN_RIGHT:
			return BotState.UP_RIGHT;
		default:
			return current;
		}
	}

	/**
	 * Demande au SceneManager de déplacer l'élément piloté.
	 */
	private void move(SceneManager sceneManager, int dx, int dy) {
		ClientRequest request = new ClientRequest("MOVE");
		request.setTarget(targetPath);
		request.setDx(dx);
		request.setDy(dy);
		sceneManager.handle(request);
	}

	/**
	 * Choisit une nouvelle couleur aléatoire et l'applique à la cible.
	 */
	private void changeColor(SceneManager sceneManager) {
		String[] colors = { "red", "blue", "green", "yellow", "orange", "pink", "cyan", "magenta", "white" };
		String color = colors[random.nextInt(colors.length)];

		ClientRequest request = new ClientRequest("SET_COLOR");
		request.setTarget(targetPath);
		request.setColor(color);
		sceneManager.handle(request);
	}
}