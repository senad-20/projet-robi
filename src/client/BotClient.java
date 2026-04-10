package client;

import javax.swing.SwingUtilities;

/**
 * Point d'entrée du client de visualisation des bots.
 */
public class BotClient {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new BotClientFrame());
	}
}