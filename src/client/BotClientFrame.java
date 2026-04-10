package client;

import java.awt.BorderLayout;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

import shared.ClientRequest;
import shared.SceneData;
import shared.ServerResponse;

/**
 * Fenêtre cliente dédiée à l'observation des bots.
 *
 * Le client ne calcule aucun comportement : il interroge régulièrement le
 * BotServer, reçoit la scène sérialisable, puis la redessine.
 */
public class BotClientFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private final DrawingPanel drawingPanel = new DrawingPanel();
	private final JTextArea logArea = new JTextArea();

	public BotClientFrame() {
		super("Bot Client");

		setLayout(new BorderLayout());

		drawingPanel.setBorder(BorderFactory.createTitledBorder("Bots"));
		logArea.setEditable(false);
		logArea.setRows(4);
		logArea.setBorder(BorderFactory.createTitledBorder("Log"));

		add(new JScrollPane(drawingPanel), BorderLayout.CENTER);
		add(new JScrollPane(logArea), BorderLayout.SOUTH);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(900, 700);
		setLocationRelativeTo(null);
		setVisible(true);

		Timer timer = new Timer(200, e -> refreshScene());
		timer.start();

		refreshScene();
	}

	/**
	 * Demande un nouvel instantané de scène au BotServer.
	 */
	private void refreshScene() {
		try (Socket socket = new Socket("localhost", 5001);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

			ClientRequest request = new ClientRequest("GET_SCENE");
			out.writeObject(request);
			out.flush();

			ServerResponse response = (ServerResponse) in.readObject();
			if (response != null && response.isSuccess()) {
				SceneData scene = response.getScene();
				drawingPanel.setScene(scene);
			}
		} catch (Exception e) {
			log("Connection error: " + e.getMessage());
		}
	}

	/**
	 * Ajoute un message dans le journal client.
	 */
	private void log(String message) {
		logArea.setText(message);
	}
}