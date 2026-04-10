package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import shared.ClientRequest;
import shared.ServerResponse;

/**
 * Point d'entrée du serveur Robi.
 *
 * Le serveur reçoit une requête cliente, la délègue au gestionnaire de scène
 * puis renvoie l'état sérialisable à dessiner côté client.
 */
public class RobiServer {

	private static final int PORT = 5000;
	private static final DateTimeFormatter LOG_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) {
		SceneManager sceneManager = new SceneManager();

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			log("SERVER", "Robi server started on port " + PORT);

			while (true) {
				Socket socket = serverSocket.accept();
				log("CONNECT",
						"Client connected from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				handleClient(socket, sceneManager);
			}
		} catch (Exception e) {
			log("FATAL", "Server crashed: " + safe(e.getMessage()));
			e.printStackTrace();
		}
	}

	private static void handleClient(Socket socket, SceneManager sceneManager) {
		try (Socket client = socket;
				ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {

			log("IO", "Waiting for object from " + client.getInetAddress().getHostAddress() + ":" + client.getPort());

			Object received = in.readObject();

			if (!(received instanceof ClientRequest)) {
				log("WARN", "Received invalid object: " + (received == null ? "null" : received.getClass().getName()));
				ServerResponse response = new ServerResponse(false, "invalid request", null);
				out.writeObject(response);
				out.flush();
				log("SEND", "Response => success=false, message=\"invalid request\"");
				return;
			}

			ClientRequest request = (ClientRequest) received;
			log("RECV", formatRequest(request));

			ServerResponse response = sceneManager.handle(request);

			log("SEND", formatResponse(response));
			out.writeObject(response);
			out.flush();

			log("DISCONNECT", "Client served and connection closed: " + client.getInetAddress().getHostAddress() + ":"
					+ client.getPort());

		} catch (Exception e) {
			log("ERROR", "Exception while serving client: " + safe(e.getMessage()));
			e.printStackTrace();
		}
	}

	private static void log(String tag, String message) {
		System.out.println("[" + LocalDateTime.now().format(LOG_TIME) + "] [" + tag + "] " + message);
	}

	private static String formatRequest(ClientRequest request) {
		return "ClientRequest{" + "action=" + safe(request.getAction()) + ", type=" + safe(request.getType())
				+ ", name=" + safe(request.getName()) + ", target=" + safe(request.getTarget()) + ", x="
				+ request.getX() + ", y=" + request.getY() + ", dx=" + request.getDx() + ", dy=" + request.getDy()
				+ ", width=" + request.getWidth() + ", height=" + request.getHeight() + ", color="
				+ safe(request.getColor()) + ", text=" + safe(request.getText()) + ", imagePath="
				+ safe(request.getImagePath()) + ", path=" + safe(request.getPath()) + "}";
	}

	private static String formatResponse(ServerResponse response) {
		if (response == null) {
			return "ServerResponse{null}";
		}

		String sceneInfo = "null";
		if (response.getScene() != null) {
			sceneInfo = "SceneData{width=" + response.getScene().getWidth() + ", height="
					+ response.getScene().getHeight() + ", backgroundColor="
					+ safe(response.getScene().getBackgroundColor()) + ", topLevelElements="
					+ response.getScene().getElements().size() + "}";
		}

		return "ServerResponse{" + "success=" + response.isSuccess() + ", message=" + safe(response.getMessage())
				+ ", scene=" + sceneInfo + "}";
	}

	private static String safe(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}
}