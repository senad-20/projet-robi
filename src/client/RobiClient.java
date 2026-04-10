package client;

import javax.swing.SwingUtilities;

public class RobiClient {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new RobiClientFrame());
	}
}