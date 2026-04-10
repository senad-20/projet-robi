package server;

import java.awt.Color;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import api.AddElement;
import api.AddScript;
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
import shared.ClientRequest;
import shared.ElementData;
import shared.SceneData;
import shared.ServerResponse;
import stree.parser.SNode;
import stree.parser.SParser;

/**
 * Adaptateur entre les requêtes réseau et l'API de la partie 1.
 *
 * Le serveur ne reprogramme pas les opérations graphiques métier. Il traduit
 * les intentions clientes en scripts interprétés par l'API existante, puis
 * reconstruit une vue sérialisable pour le client.
 *
 * La seule logique spécifique conservée ici concerne le contrôle des bornes des
 * conteneurs, la persistance de la scène sérialisable, ainsi que l'exécution de
 * scripts textuels envoyés par le client.
 */
public class SceneManager {

	private static final DateTimeFormatter LOG_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final Interpreter interpreter = new Interpreter();
	private Environment environment;
	private int generatedNameCounter;
	private final Map<String, String> labelTexts = new LinkedHashMap<>();
	private final Map<String, String> imagePaths = new LinkedHashMap<>();
	private final Map<String, String> elementColors = new LinkedHashMap<>();

	private int spaceWidth;
	private int spaceHeight;
	private String spaceBackgroundColor;

	public SceneManager() {
		resetEnvironment();
	}

	/**
	 * Traite une requête cliente et renvoie l'état sérialisable de la scène après
	 * exécution.
	 *
	 * Les scripts enregistrés dynamiquement dans l'environnement serveur restent
	 * disponibles tant que l'environnement n'est pas réinitialisé.
	 */
	public synchronized ServerResponse handle(ClientRequest request) {
		try {
			if (request == null || request.getAction() == null || request.getAction().isBlank()) {
				log("HANDLE", "Invalid request: missing action");
				return failure("missing action");
			}

			String action = request.getAction().trim();
			log("HANDLE",
					"Action=" + action + " target=" + safe(request.getTarget()) + " name=" + safe(request.getName()));

			ServerResponse response;

			switch (action) {
			case "GET_SCENE":
				response = success("scene");
				break;
			case "ADD":
				response = handleAdd(request);
				break;
			case "DELETE":
				response = handleDelete(request);
				break;
			case "MOVE":
				response = handleMove(request);
				break;
			case "SET_POSITION":
				response = handleSetPosition(request);
				break;
			case "SET_SIZE":
				response = handleSetSize(request);
				break;
			case "SET_COLOR":
				response = handleSetColor(request);
				break;
			case "RUN_SCRIPT":
				response = handleRunScript(request);
				break;
			case "CLEAR":
				resetEnvironment();
				response = success("scene cleared");
				break;
			case "SAVE":
				response = handleSave(request);
				break;
			case "LOAD":
				response = handleLoad(request);
				break;
			default:
				response = failure("unknown action: " + action);
				break;
			}

			log("RESULT", "success=" + response.isSuccess() + ", message=" + safe(response.getMessage()));
			logSceneSummary(response.getScene());

			return response;

		} catch (Error e) {
			log("ERROR", "Server error: " + safe(e.getMessage()));
			return failure(e.getMessage() == null ? "server error" : e.getMessage());
		} catch (Exception e) {
			log("ERROR", "Server exception: " + safe(e.getMessage()));
			return failure(e.getMessage() == null ? "server error" : e.getMessage());
		}
	}

	/**
	 * Ajoute un nouvel élément graphique dans le conteneur ciblé.
	 *
	 * La création concrète reste déléguée à l'API existante via la génération d'un
	 * script compatible avec l'interpréteur.
	 */
	private ServerResponse handleAdd(ClientRequest request) {
		String parentPath = normalizeTarget(request.getTarget());
		String type = trimToNull(request.getType());

		log("ADD", "Requested add: type=" + safe(type) + ", name=" + safe(request.getName()) + ", target="
				+ safe(parentPath));

		if (type == null) {
			return failure("missing type");
		}

		if (!"space".equals(parentPath)) {
			ElementData parent = findElementByPath(buildSceneSnapshot(), parentPath);
			if (parent == null) {
				return failure("parent not found: " + parentPath);
			}
			if (!isContainerType(parent.getType())) {
				return failure("target is not a container: " + parentPath);
			}
		}

		String name = trimToNull(request.getName());
		if (name == null) {
			name = type.toLowerCase() + generatedNameCounter++;
			log("ADD", "Auto-generated name: " + name);
		}

		if (findElementByPath(buildSceneSnapshot(), parentPath + "." + name) != null) {
			return failure("name already used in container: " + name);
		}

		ElementData candidate = createDefaultElement(name, type, request);

		if (!fitsInsideParent(parentPath, candidate.getX(), candidate.getY(), candidate.getWidth(),
				candidate.getHeight())) {
			log("ADD", "Default position does not fit; forcing (0,0)");
			candidate.setX(0);
			candidate.setY(0);
		}

		if (!fitsInsideParent(parentPath, candidate.getX(), candidate.getY(), candidate.getWidth(),
				candidate.getHeight())) {
			return failure("element too large for container");
		}

		StringBuilder script = new StringBuilder();
		script.append("(").append(parentPath).append(" add ").append(name).append(" (").append(type).append(" new");
		if ("Label".equals(type)) {
			script.append(" ")
					.append(quote(request.getText() == null || request.getText().isBlank() ? name : request.getText()));
		} else if ("Image".equals(type)) {
			String imagePath = trimToNull(request.getImagePath());
			if (imagePath == null) {
				return failure("missing image path");
			}
			script.append(" ").append(quote(imagePath));
		}
		script.append("))");

		if (supportsSetDim(type)) {
			script.append(" (").append(parentPath).append(".").append(name).append(" setDim ")
					.append(candidate.getWidth()).append(" ").append(candidate.getHeight()).append(")");
		}

		script.append(" (").append(parentPath).append(".").append(name).append(" setColor ")
				.append(candidate.getColor()).append(")");

		if (candidate.getX() != 0 || candidate.getY() != 0) {
			script.append(" (").append(parentPath).append(".").append(name).append(" translate ")
					.append(candidate.getX()).append(" ").append(candidate.getY()).append(")");
		}

		log("SCRIPT", script.toString());
		runScript(script.toString());

		String fullPath = parentPath + "." + name;
		elementColors.put(fullPath, candidate.getColor());

		if ("Label".equals(type)) {
			labelTexts.put(fullPath,
					request.getText() == null || request.getText().isBlank() ? name : request.getText());
		}
		if ("Image".equals(type)) {
			imagePaths.put(fullPath, request.getImagePath());
		}

		log("ADD", "Added element: " + fullPath);
		return success("element added");
	}

	/**
	 * Supprime un élément ciblé ainsi que ses éventuels descendants.
	 */
	private ServerResponse handleDelete(ClientRequest request) {
		String target = normalizeTarget(request.getTarget());
		log("DELETE", "Requested delete: target=" + safe(target));

		if ("space".equals(target)) {
			return failure("cannot delete space");
		}

		SceneData scene = buildSceneSnapshot();
		ElementData element = findElementByPath(scene, target);
		if (element == null) {
			return failure("element not found: " + target);
		}

		String parentPath = parentPath(target);
		String localName = localName(target);
		String script = "(" + parentPath + " del " + localName + ")";

		log("SCRIPT", script);
		runScript(script);
		removeMetadataTree(target);

		log("DELETE", "Deleted element: " + target);
		return success("element deleted");
	}

	/**
	 * Déplace un élément relativement à sa position actuelle.
	 *
	 * Si le déplacement sortirait du conteneur parent, la requête est ignorée sans
	 * provoquer d'erreur.
	 */
	private ServerResponse handleMove(ClientRequest request) {
		String target = normalizeTarget(request.getTarget());

		if ("space".equals(target)) {
			return failure("space cannot be moved");
		}

		SceneData scene = buildSceneSnapshot();
		ElementData element = findElementByPath(scene, target);
		if (element == null) {
			return failure("element not found: " + target);
		}

		int nextX = element.getX() + request.getDx();
		int nextY = element.getY() + request.getDy();

		log("MOVE", target + " from (" + element.getX() + "," + element.getY() + ") to (" + nextX + "," + nextY + ")");

		if (!fitsInsideParent(parentPath(target), nextX, nextY, element.getWidth(), element.getHeight())) {
			log("MOVE", "Ignored: out of bounds");
			return success("move ignored: out of bounds");
		}

		String script = "(" + target + " translate " + request.getDx() + " " + request.getDy() + ")";
		log("SCRIPT", script);
		runScript(script);

		return success("element moved");
	}

	/**
	 * Positionne explicitement un élément à des coordonnées données.
	 *
	 * Si la nouvelle position sortirait du conteneur parent, la requête est ignorée
	 * sans erreur.
	 */
	private ServerResponse handleSetPosition(ClientRequest request) {
		String target = normalizeTarget(request.getTarget());

		if ("space".equals(target)) {
			return failure("space has no position");
		}

		SceneData scene = buildSceneSnapshot();
		ElementData element = findElementByPath(scene, target);
		if (element == null) {
			return failure("element not found: " + target);
		}

		log("SET_POSITION", target + " from (" + element.getX() + "," + element.getY() + ") to (" + request.getX() + ","
				+ request.getY() + ")");

		if (!fitsInsideParent(parentPath(target), request.getX(), request.getY(), element.getWidth(),
				element.getHeight())) {
			log("SET_POSITION", "Ignored: out of bounds");
			return success("position ignored: out of bounds");
		}

		int dx = request.getX() - element.getX();
		int dy = request.getY() - element.getY();
		String script = "(" + target + " translate " + dx + " " + dy + ")";

		log("SCRIPT", script);
		runScript(script);

		return success("position updated");
	}

	/**
	 * Modifie la taille du space ou d'un élément redimensionnable.
	 *
	 * La modification est refusée si elle rendrait la scène incohérente vis-à-vis
	 * des bornes ou des enfants déjà présents.
	 */
	private ServerResponse handleSetSize(ClientRequest request) {
		String target = normalizeTarget(request.getTarget());
		int width = request.getWidth();
		int height = request.getHeight();

		log("SET_SIZE", "target=" + safe(target) + ", width=" + width + ", height=" + height);

		if (width <= 0 || height <= 0) {
			return failure("invalid size");
		}

		SceneData scene = buildSceneSnapshot();

		if ("space".equals(target)) {
			if (!allTopLevelElementsFit(scene, width, height)) {
				log("SET_SIZE", "Ignored: existing top-level elements would be out of bounds");
				return success("size ignored: existing elements would be out of bounds");
			}

			String script = "(space setDim " + width + " " + height + ")";
			log("SCRIPT", script);
			runScript(script);

			spaceWidth = width;
			spaceHeight = height;

			return success("space size updated");
		}

		ElementData element = findElementByPath(scene, target);
		if (element == null) {
			return failure("element not found: " + target);
		}

		if (!supportsSetDim(element.getType())) {
			return failure("size not supported for type: " + element.getType());
		}

		if (!fitsInsideParent(parentPath(target), element.getX(), element.getY(), width, height)) {
			log("SET_SIZE", "Ignored: resized element would be out of bounds");
			return success("size ignored: out of bounds");
		}

		if (!childrenStillFitInside(element, width, height)) {
			log("SET_SIZE", "Ignored: children would be out of bounds");
			return success("size ignored: children would be out of bounds");
		}

		String script = "(" + target + " setDim " + width + " " + height + ")";
		log("SCRIPT", script);
		runScript(script);

		return success("size updated");
	}

	/**
	 * Modifie la couleur du space ou d'un élément graphique.
	 */
	private ServerResponse handleSetColor(ClientRequest request) {
		String target = normalizeTarget(request.getTarget());
		String color = trimToNull(request.getColor());

		log("SET_COLOR", "target=" + safe(target) + ", color=" + safe(color));

		if (color == null) {
			return failure("missing color");
		}

		String script = "(" + target + " setColor " + color + ")";
		log("SCRIPT", script);
		runScript(script);

		if ("space".equals(target)) {
			spaceBackgroundColor = color;
		} else {
			elementColors.put(target, color);
		}

		return success("color updated");
	}

	/**
	 * Exécute un script textuel envoyé par le client.
	 *
	 * Cette exécution se fait dans l'environnement serveur courant. Les scripts
	 * ajoutés dynamiquement via addScript sont donc conservés et réutilisables lors
	 * des appels suivants, exactement comme dans l'API locale.
	 */
	private ServerResponse handleRunScript(ClientRequest request) {
		String script = trimToNull(request.getScript());
		log("RUN_SCRIPT", "script=" + safe(script));

		if (script == null) {
			return failure("missing script");
		}

		try {
			runScript(script);
			syncMetadataFromScene(buildSceneSnapshot());
			return success("script executed");
		} catch (Error e) {
			return failure(e.getMessage() == null ? "invalid script" : e.getMessage());
		}
	}

	/**
	 * Sauvegarde la scène sérialisable dans un fichier.
	 */
	private ServerResponse handleSave(ClientRequest request) throws IOException {
		String path = trimToNull(request.getPath());
		log("SAVE", "path=" + safe(path));

		if (path == null) {
			return failure("missing path");
		}

		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
			out.writeObject(buildSceneSnapshot());
		}

		return success("scene saved");
	}

	/**
	 * Recharge une scène sérialisable depuis un fichier puis reconstruit l'état
	 * serveur via l'API.
	 */
	private ServerResponse handleLoad(ClientRequest request) {
		String path = trimToNull(request.getPath());
		log("LOAD", "path=" + safe(path));

		if (path == null) {
			return failure("missing path");
		}

		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
			Object loaded = in.readObject();

			if (!(loaded instanceof SceneData)) {
				return failure("invalid scene file");
			}

			SceneData scene = (SceneData) loaded;

			resetEnvironment();

			spaceWidth = scene.getWidth();
			spaceHeight = scene.getHeight();
			spaceBackgroundColor = scene.getBackgroundColor();

			Object oldSpace = environment.getReferenceByName("space").getReceiver();
			applySpaceState(oldSpace, spaceWidth, spaceHeight, spaceBackgroundColor);

			StringBuilder script = new StringBuilder();
			script.append("(space setDim ").append(scene.getWidth()).append(" ").append(scene.getHeight()).append(")");
			script.append(" (space setColor ").append(scene.getBackgroundColor()).append(")");

			for (ElementData element : scene.getElements()) {
				appendElementScript(script, "space", element);
			}

			log("SCRIPT", script.toString());
			runScript(script.toString());
			rebuildMetadata(scene, "space");

			log("LOAD", "Scene loaded successfully");
			return success("scene loaded");

		} catch (IOException | ClassNotFoundException e) {
			return failure("unable to load scene: " + e.getMessage());
		}
	}

	/**
	 * Reconstruit récursivement un script équivalent à la scène sérialisée.
	 */
	private void appendElementScript(StringBuilder script, String parentPath, ElementData element) {
		String fullPath = parentPath + "." + element.getName();

		script.append(" (").append(parentPath).append(" add ").append(element.getName()).append(" (")
				.append(element.getType()).append(" new");

		if ("Label".equals(element.getType())) {
			script.append(" ").append(quote(element.getText() == null ? element.getName() : element.getText()));
		} else if ("Image".equals(element.getType())) {
			script.append(" ").append(quote(element.getImagePath() == null ? "" : element.getImagePath()));
		}

		script.append("))");

		if (supportsSetDim(element.getType())) {
			script.append(" (").append(fullPath).append(" setDim ").append(element.getWidth()).append(" ")
					.append(element.getHeight()).append(")");
		}

		script.append(" (").append(fullPath).append(" setColor ").append(element.getColor()).append(")");

		if (element.getX() != 0 || element.getY() != 0) {
			script.append(" (").append(fullPath).append(" translate ").append(element.getX()).append(" ")
					.append(element.getY()).append(")");
		}

		for (ElementData child : element.getChildren()) {
			appendElementScript(script, fullPath, child);
		}
	}

	/**
	 * Réinitialise complètement l'environnement serveur avec les références de base
	 * nécessaires à l'interpréteur.
	 */
	private void resetEnvironment() {
		log("RESET", "Rebuilding server API environment");

		environment = new Environment();
		generatedNameCounter = 1;
		labelTexts.clear();
		imagePaths.clear();
		elementColors.clear();

		spaceWidth = 700;
		spaceHeight = 500;
		spaceBackgroundColor = "white";

		Object space = createSpace();

		Reference spaceRef = new Reference(space);
		spaceRef.addCommand("setColor", new SetColor());
		spaceRef.addCommand("setDim", new SetDim());
		spaceRef.addCommand("sleep", new Sleep());
		spaceRef.addCommand("add", new AddElement(environment));
		spaceRef.addCommand("del", new DelElement(environment));
		spaceRef.addCommand("addScript", new AddScript(environment));
		environment.addReference("space", spaceRef);

		Reference rectRef = new Reference(loadClass("graphicLayer.GRect"));
		rectRef.addCommand("new", new NewElement(environment));
		environment.addReference("Rect", rectRef);

		Reference ovalRef = new Reference(loadClass("graphicLayer.GOval"));
		ovalRef.addCommand("new", new NewElement(environment));
		environment.addReference("Oval", ovalRef);

		Reference imageRef = new Reference(loadClass("graphicLayer.GImage"));
		imageRef.addCommand("new", new NewImage());
		environment.addReference("Image", imageRef);

		Reference labelRef = new Reference(loadClass("graphicLayer.GString"));
		labelRef.addCommand("new", new NewString(environment));
		environment.addReference("Label", labelRef);
	}

	/**
	 * Crée le conteneur racine space utilisé côté serveur.
	 */
	private Object createSpace() {
		try {
			Class<?> spaceClass = loadClass("graphicLayer.GSpace");
			Constructor<?> ctor = spaceClass.getConstructor(String.class, Dimension.class);
			Object space = ctor.newInstance("Robi Server", new Dimension(spaceWidth, spaceHeight));
			applySpaceState(space, spaceWidth, spaceHeight, spaceBackgroundColor);
			return space;
		} catch (Exception e) {
			throw new Error("unable to create space");
		}
	}

	/**
	 * Applique les propriétés principales du space par réflexivité.
	 */
	private void applySpaceState(Object space, int width, int height, String backgroundColor) {
		try {
			Method setDimension = space.getClass().getMethod("setDimension", Dimension.class);
			setDimension.invoke(space, new Dimension(width, height));
		} catch (Exception e) {
		}

		try {
			Method setColor = space.getClass().getMethod("setColor", Color.class);
			setColor.invoke(space, toAwtColor(backgroundColor));
		} catch (Exception e) {
		}
	}

	/**
	 * Charge une classe graphique par son nom complet.
	 */
	private Class<?> loadClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new Error("missing class: " + name);
		}
	}

	/**
	 * Parse et exécute une ou plusieurs S-expressions dans l'environnement courant.
	 */
	private void runScript(String script) {
		try {
			SParser<SNode> parser = new SParser<>();
			List<SNode> nodes = parser.parse(script);

			for (SNode node : nodes) {
				interpreter.compute(environment, node);
			}
		} catch (IOException e) {
			throw new Error("invalid script: " + script);
		}
	}

	/**
	 * Construit une vue sérialisable de la scène actuelle à partir de
	 * l'environnement serveur.
	 */
	private SceneData buildSceneSnapshot() {
		SceneData scene = new SceneData();
		scene.setWidth(spaceWidth);
		scene.setHeight(spaceHeight);
		scene.setBackgroundColor(spaceBackgroundColor);

		Map<String, Reference> references = environment.getReferences();
		Map<String, ElementData> created = new LinkedHashMap<>();

		for (Map.Entry<String, Reference> entry : references.entrySet()) {
			String name = entry.getKey();

			if (!name.startsWith("space.")) {
				continue;
			}

			ElementData element = createSerializableElement(name, entry.getValue().getReceiver());
			if (element != null) {
				created.put(name, element);
			}
		}

		for (Map.Entry<String, ElementData> entry : created.entrySet()) {
			String fullPath = entry.getKey();
			ElementData element = entry.getValue();

			String parent = parentPath(fullPath);
			if ("space".equals(parent)) {
				scene.getElements().add(element);
			} else {
				ElementData parentElement = created.get(parent);
				if (parentElement != null) {
					parentElement.getChildren().add(element);
				}
			}
		}

		return scene;
	}

	/**
	 * Crée une représentation sérialisable d'un élément graphique référencé dans
	 * l'environnement.
	 */
	private ElementData createSerializableElement(String fullPath, Object receiver) {
		String type = toElementType(receiver);

		if (type == null) {
			return null;
		}

		ElementData data = new ElementData(localName(fullPath), type);
		data.setX(readInt(receiver, "getX", 0));
		data.setY(readInt(receiver, "getY", 0));
		data.setWidth(defaultWidthFor(type, readInt(receiver, "getWidth", 0)));
		data.setHeight(defaultHeightFor(type, readInt(receiver, "getHeight", 0)));
		data.setColor(elementColors.getOrDefault(fullPath, "black"));

		if ("Label".equals(type)) {
			data.setText(labelTexts.get(fullPath));
		}

		if ("Image".equals(type)) {
			data.setImagePath(imagePaths.get(fullPath));
		}

		return data;
	}

	/**
	 * Tente de reconnaître le type logique d'un objet graphique concret.
	 */
	private String toElementType(Object receiver) {
		String simpleName = receiver.getClass().getSimpleName();

		if ("GRect".equals(simpleName)) {
			return "Rect";
		}
		if ("GOval".equals(simpleName)) {
			return "Oval";
		}
		if ("GImage".equals(simpleName)) {
			return "Image";
		}
		if ("GString".equals(simpleName)) {
			return "Label";
		}

		return null;
	}

	/**
	 * Vérifie si une boîte englobante tient dans son conteneur parent.
	 */
	private boolean fitsInsideParent(String parentPath, int x, int y, int width, int height) {
		if (x < 0 || y < 0 || width <= 0 || height <= 0) {
			return false;
		}

		SceneData scene = buildSceneSnapshot();

		if ("space".equals(parentPath)) {
			return x + width <= scene.getWidth() && y + height <= scene.getHeight();
		}

		ElementData parent = findElementByPath(scene, parentPath);
		return parent != null && x + width <= parent.getWidth() && y + height <= parent.getHeight();
	}

	/**
	 * Vérifie si tous les éléments de premier niveau restent dans le space après un
	 * redimensionnement.
	 */
	private boolean allTopLevelElementsFit(SceneData scene, int width, int height) {
		for (ElementData element : scene.getElements()) {
			if (element.getX() < 0 || element.getY() < 0 || element.getX() + element.getWidth() > width
					|| element.getY() + element.getHeight() > height) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Vérifie si tous les enfants d'un conteneur tiennent encore après
	 * redimensionnement de ce conteneur.
	 */
	private boolean childrenStillFitInside(ElementData parent, int width, int height) {
		for (ElementData child : parent.getChildren()) {
			if (child.getX() < 0 || child.getY() < 0 || child.getX() + child.getWidth() > width
					|| child.getY() + child.getHeight() > height) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Crée une configuration par défaut pour un nouvel élément selon son type.
	 */
	private ElementData createDefaultElement(String name, String type, ClientRequest request) {
		ElementData element = new ElementData(name, type);
		element.setX(10);
		element.setY(10);

		if ("Rect".equals(type)) {
			element.setWidth(60);
			element.setHeight(40);
			element.setColor("blue");
		} else if ("Oval".equals(type)) {
			element.setWidth(60);
			element.setHeight(40);
			element.setColor("red");
		} else if ("Label".equals(type)) {
			element.setWidth(100);
			element.setHeight(20);
			element.setColor("black");
			element.setText(request.getText() == null || request.getText().isBlank() ? name : request.getText());
		} else if ("Image".equals(type)) {
			element.setWidth(80);
			element.setHeight(80);
			element.setColor("black");
			element.setImagePath(request.getImagePath());
		} else {
			throw new Error("unknown element type: " + type);
		}

		return element;
	}

	/**
	 * Recherche un élément dans la scène sérialisable à partir de son nom qualifié.
	 */
	private ElementData findElementByPath(SceneData scene, String fullPath) {
		if (scene == null || fullPath == null || "space".equals(fullPath)) {
			return null;
		}

		String normalized = fullPath.startsWith("space.") ? fullPath.substring("space.".length()) : fullPath;
		String[] parts = normalized.split("\\.");
		List<ElementData> level = scene.getElements();
		ElementData current = null;

		for (String part : parts) {
			current = null;
			for (ElementData candidate : level) {
				if (part.equals(candidate.getName())) {
					current = candidate;
					break;
				}
			}
			if (current == null) {
				return null;
			}
			level = current.getChildren();
		}

		return current;
	}

	/**
	 * Reconstruit les métadonnées non directement lisibles sur certains objets
	 * graphiques à partir d'une scène sérialisable.
	 */
	private void rebuildMetadata(SceneData scene, String parentPath) {
		for (ElementData element : scene.getElements()) {
			rebuildMetadata(element, parentPath);
		}
	}

	/**
	 * Reconstruit récursivement les métadonnées d'un élément.
	 */
	private void rebuildMetadata(ElementData element, String parentPath) {
		String fullPath = parentPath + "." + element.getName();

		elementColors.put(fullPath, element.getColor());

		if ("Label".equals(element.getType()) && element.getText() != null) {
			labelTexts.put(fullPath, element.getText());
		}
		if ("Image".equals(element.getType()) && element.getImagePath() != null) {
			imagePaths.put(fullPath, element.getImagePath());
		}

		for (ElementData child : element.getChildren()) {
			rebuildMetadata(child, fullPath);
		}
	}

	/**
	 * Resynchronise les métadonnées à partir de la scène courante.
	 *
	 * Cette étape est utile après l'exécution de scripts directs envoyés par le
	 * client afin de conserver les textes, chemins d'image et couleurs côté vue
	 * sérialisable.
	 */
	private void syncMetadataFromScene(SceneData scene) {
		labelTexts.clear();
		imagePaths.clear();
		elementColors.clear();
		rebuildMetadata(scene, "space");
	}

	/**
	 * Supprime toutes les métadonnées associées à un sous-arbre supprimé.
	 */
	private void removeMetadataTree(String rootPath) {
		labelTexts.keySet().removeIf(key -> key.equals(rootPath) || key.startsWith(rootPath + "."));
		imagePaths.keySet().removeIf(key -> key.equals(rootPath) || key.startsWith(rootPath + "."));
		elementColors.keySet().removeIf(key -> key.equals(rootPath) || key.startsWith(rootPath + "."));
	}

	/**
	 * Tente d'appeler sans argument une méthode par son nom.
	 */
	private Object invoke(Object target, String methodName) {
		try {
			Method method = target.getClass().getMethod(methodName);
			return method.invoke(target);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Lit une valeur entière par réflexion, avec valeur par défaut.
	 */
	private int readInt(Object target, String methodName, int defaultValue) {
		Object value = invoke(target, methodName);
		return value instanceof Number ? ((Number) value).intValue() : defaultValue;
	}

	/**
	 * Fournit une largeur logique par défaut selon le type.
	 */
	private int defaultWidthFor(String type, int width) {
		if (width > 0) {
			return width;
		}
		if ("Rect".equals(type) || "Oval".equals(type)) {
			return 60;
		}
		if ("Label".equals(type)) {
			return 100;
		}
		if ("Image".equals(type)) {
			return 80;
		}
		return 0;
	}

	/**
	 * Fournit une hauteur logique par défaut selon le type.
	 */
	private int defaultHeightFor(String type, int height) {
		if (height > 0) {
			return height;
		}
		if ("Rect".equals(type) || "Oval".equals(type)) {
			return 40;
		}
		if ("Label".equals(type)) {
			return 20;
		}
		if ("Image".equals(type)) {
			return 80;
		}
		return 0;
	}

	/**
	 * Convertit un nom de couleur logique en couleur AWT.
	 */
	private Color toAwtColor(String colorName) {
		if (colorName == null) {
			return Color.WHITE;
		}

		switch (colorName.toLowerCase()) {
		case "black":
			return Color.BLACK;
		case "blue":
			return Color.BLUE;
		case "cyan":
			return Color.CYAN;
		case "darkgray":
			return Color.DARK_GRAY;
		case "gray":
		case "grey":
			return Color.GRAY;
		case "green":
			return Color.GREEN;
		case "lightgray":
			return Color.LIGHT_GRAY;
		case "magenta":
			return Color.MAGENTA;
		case "orange":
			return Color.ORANGE;
		case "pink":
			return Color.PINK;
		case "red":
			return Color.RED;
		case "yellow":
			return Color.YELLOW;
		case "white":
		default:
			return Color.WHITE;
		}
	}

	/**
	 * Indique si un type logique peut contenir des éléments enfants.
	 */
	private boolean isContainerType(String type) {
		return "Rect".equals(type) || "Oval".equals(type);
	}

	/**
	 * Indique si un type logique supporte la redimension.
	 */
	private boolean supportsSetDim(String type) {
		return "Rect".equals(type) || "Oval".equals(type);
	}

	/**
	 * Normalise une cible éventuelle en retournant "space" par défaut.
	 */
	private String normalizeTarget(String target) {
		String normalized = trimToNull(target);
		return normalized == null ? "space" : normalized;
	}

	/**
	 * Trim une chaîne et retourne null si elle devient vide.
	 */
	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	/**
	 * Retourne le chemin du parent d'un nom qualifié.
	 */
	private String parentPath(String fullPath) {
		int index = fullPath.lastIndexOf('.');
		return index < 0 ? "space" : fullPath.substring(0, index);
	}

	/**
	 * Retourne le nom local d'un nom qualifié.
	 */
	private String localName(String fullPath) {
		int index = fullPath.lastIndexOf('.');
		return index < 0 ? fullPath : fullPath.substring(index + 1);
	}

	/**
	 * Entoure une chaîne de guillemets en échappant les caractères sensibles.
	 */
	private String quote(String value) {
		String safe = value == null ? "" : value;
		return "\"" + safe.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

	/**
	 * Construit une réponse serveur de succès avec l'instantané courant de la
	 * scène.
	 */
	private ServerResponse success(String message) {
		return new ServerResponse(true, message, buildSceneSnapshot());
	}

	/**
	 * Construit une réponse serveur d'échec avec l'instantané courant de la scène.
	 */
	private ServerResponse failure(String message) {
		return new ServerResponse(false, message, buildSceneSnapshot());
	}

	/**
	 * Écrit une ligne dans les logs serveur.
	 */
	private void log(String tag, String message) {
		System.out.println("[" + LocalDateTime.now().format(LOG_TIME) + "] [" + tag + "] " + message);
	}

	/**
	 * Affiche un résumé lisible de la scène pour le debug et la démonstration.
	 */
	private void logSceneSummary(SceneData scene) {
		if (scene == null) {
			log("SCENE", "null");
			return;
		}

		System.out.println("[" + LocalDateTime.now().format(LOG_TIME) + "] [SCENE] " + "size=" + scene.getWidth() + "x"
				+ scene.getHeight() + ", background=" + scene.getBackgroundColor() + ", topLevelElements="
				+ scene.getElements().size());

		for (ElementData element : scene.getElements()) {
			logElement(element, "  ");
		}
	}

	/**
	 * Affiche récursivement un élément et ses descendants dans les logs.
	 */
	private void logElement(ElementData element, String indent) {
		System.out.println(indent + "- " + element.getName() + " [" + element.getType() + "]" + " pos=("
				+ element.getX() + "," + element.getY() + ")" + " size=(" + element.getWidth() + "x"
				+ element.getHeight() + ")" + " color=" + element.getColor()
				+ (element.getText() != null ? " text=\"" + element.getText() + "\"" : "")
				+ (element.getImagePath() != null ? " imagePath=\"" + element.getImagePath() + "\"" : ""));

		for (ElementData child : element.getChildren()) {
			logElement(child, indent + "  ");
		}
	}

	/**
	 * Protège l'affichage des chaînes potentiellement nulles dans les logs.
	 */
	private String safe(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}
	
	/**
	 * Retourne un instantané sérialisable de la scène courante.
	 *
	 * Cette méthode permet aux bots et aux serveurs spécialisés de lire l'état
	 * courant sans exposer directement les objets graphiques internes.
	 */
	public synchronized SceneData getSceneSnapshot() {
		return buildSceneSnapshot();
	}

	/**
	 * Recherche un élément dans un instantané courant à partir de son nom qualifié.
	 */
	public synchronized ElementData findElementSnapshot(String fullPath) {
		return findElementByPath(buildSceneSnapshot(), fullPath);
	}

	/**
	 * Retourne la largeur logique du conteneur parent de l'élément ciblé.
	 */
	public synchronized int getParentWidth(String fullPath) {
		String parent = parentPath(fullPath);
		SceneData scene = buildSceneSnapshot();

		if ("space".equals(parent)) {
			return scene.getWidth();
		}

		ElementData parentElement = findElementByPath(scene, parent);
		return parentElement == null ? 0 : parentElement.getWidth();
	}

	/**
	 * Retourne la hauteur logique du conteneur parent de l'élément ciblé.
	 */
	public synchronized int getParentHeight(String fullPath) {
		String parent = parentPath(fullPath);
		SceneData scene = buildSceneSnapshot();

		if ("space".equals(parent)) {
			return scene.getHeight();
		}

		ElementData parentElement = findElementByPath(scene, parent);
		return parentElement == null ? 0 : parentElement.getHeight();
	}
}