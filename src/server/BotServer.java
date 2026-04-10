package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import shared.ClientRequest;
import shared.ServerResponse;

/**
 * Serveur dédié à la démonstration des bots.
 *
 * Ce serveur possède :
 * - un SceneManager autoritaire
 * - une liste de bots mis à jour périodiquement
 * - une boucle réseau pour servir les clients
 *
 * Les clients ne font que demander des instantanés de scène et les dessiner.
 * Toute la logique des bots reste centralisée ici.
 */
public class BotServer {

	private static final int PORT = 5001;
	private static final int TICK_MS = 120;
	private static final DateTimeFormatter LOG_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final SceneManager sceneManager = new SceneManager();
	private final List<RobiBot> bots = new ArrayList<>();
	private final Random random = new Random();

	public static void main(String[] args) {
		new BotServer().start();
	}

	/**
	 * Démarre le serveur de bots.
	 */
	public void start() {
		setupInitialScene();
		startBotLoop();

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			log("SERVER", "Bot server started on port " + PORT);

			while (true) {
				Socket socket = serverSocket.accept();
				log("CONNECT",
						"Client connected from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				handleClient(socket);
			}
		} catch (Exception e) {
			log("FATAL", "Bot server crashed: " + safe(e.getMessage()));
			e.printStackTrace();
		}
	}

	/**
	 * Crée une scène initiale contenant plusieurs robots.
	 */
	private void setupInitialScene() {
		sceneManager.handle(makeScriptRequest(
				"(space setDim 700 500) "
						+ "(space setColor white) "
						+ "(space add robi1 (Rect new)) "
						+ "(space.robi1 setDim 60 40) "
						+ "(space add robi2 (Rect new)) "
						+ "(space.robi2 setDim 60 40) "
						+ "(space add robi3 (Rect new)) "
						+ "(space.robi3 setDim 60 40)"));

		randomizeBot("space.robi1");
		randomizeBot("space.robi2");
		randomizeBot("space.robi3");
	}

	/**
	 * Place un bot à une position aléatoire, lui assigne une couleur aléatoire et
	 * une direction initiale aléatoire.
	 */
	private void randomizeBot(String path) {
		int width = sceneManager.getSceneSnapshot().getWidth();
		int height = sceneManager.getSceneSnapshot().getHeight();

		int x = random.nextInt(Math.max(1, width - 80));
		int y = random.nextInt(Math.max(1, height - 80));

		ClientRequest pos = new ClientRequest("SET_POSITION");
		pos.setTarget(path);
		pos.setX(x);
		pos.setY(y);
		sceneManager.handle(pos);

		ClientRequest color = new ClientRequest("SET_COLOR");
		color.setTarget(path);
		color.setColor(randomColor());
		sceneManager.handle(color);

		BotState[] states = BotState.values();
		BotState initial = states[random.nextInt(states.length)];
		bots.add(new RobiBot(path, initial, 10));

		log("BOT", "Created bot on " + path + " at (" + x + "," + y + ") with state " + initial);
	}

	/**
	 * Lance la boucle d'animation des bots.
	 */
	private void startBotLoop() {
		Thread loop = new Thread(() -> {
			while (true) {
				try {
					for (RobiBot bot : bots) {
						bot.tick(sceneManager);
					}
					Thread.sleep(TICK_MS);
				} catch (Exception e) {
					log("ERROR", "Bot loop error: " + safe(e.getMessage()));
					e.printStackTrace();
				}
			}
		});

		loop.setDaemon(true);
		loop.start();
	}

	/**
	 * Traite une connexion cliente.
	 *
	 * Le client peut demander la scène, ou envoyer n'importe quelle requête déjà
	 * comprise par SceneManager.
	 */
	private void handleClient(Socket socket) {
		try (Socket client = socket;
				ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {

			Object received = in.readObject();
			ServerResponse response;

			if (received instanceof ClientRequest) {
				ClientRequest request = (ClientRequest) received;
				log("RECV", "action=" + safe(request.getAction()));
				response = sceneManager.handle(request);
			} else {
				response = new ServerResponse(false, "invalid request", null);
			}

			out.writeObject(response);
			out.flush();

		} catch (Exception e) {
			log("ERROR", "Client handling error: " + safe(e.getMessage()));
			e.printStackTrace();
		}
	}

	/**
	 * Construit une requête de script à envoyer au SceneManager.
	 */
	private ClientRequest makeScriptRequest(String script) {
		ClientRequest request = new ClientRequest("RUN_SCRIPT");
		request.setScript(script);
		return request;
	}

	/**
	 * Retourne une couleur logique aléatoire.
	 */
	private String randomColor() {
		String[] colors = { "red", "blue", "green", "yellow", "orange", "pink", "cyan", "magenta" };
		return colors[random.nextInt(colors.length)];
	}

	/**
	 * Écrit une ligne de log horodatée.
	 */
	private void log(String tag, String message) {
		System.out.println("[" + LocalDateTime.now().format(LOG_TIME) + "] [" + tag + "] " + message);
	}

	/**
	 * Protège l'affichage des chaînes potentiellement nulles dans les logs.
	 */
	private String safe(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}
}