package server;

/**
 * États possibles d'un bot.
 *
 * Chaque état représente une direction diagonale de déplacement dans le plan :
 * - dx = direction horizontale - dy = direction verticale
 */
public enum BotState {
	UP_LEFT(-1, -1), UP_RIGHT(1, -1), DOWN_LEFT(-1, 1), DOWN_RIGHT(1, 1);

	private final int dx;
	private final int dy;

	BotState(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}

	/**
	 * Retourne le coefficient horizontal associé à l'état.
	 */
	public int getDx() {
		return dx;
	}

	/**
	 * Retourne le coefficient vertical associé à l'état.
	 */
	public int getDy() {
		return dy;
	}
}