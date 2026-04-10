package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import shared.ClientRequest;
import shared.ElementData;
import shared.SceneData;
import shared.ServerResponse;

/**
 * Fenêtre principale du client Robi.
 *
 * Elle permet d'explorer la scène, de manipuler les éléments graphiques et
 * d'envoyer les requêtes correspondantes au serveur.
 */
public class RobiClientFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private final DrawingPanel drawingPanel = new DrawingPanel();
	private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new TreeEntry("space", "space"));
	private final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
	private final JTree elementTree = new JTree(treeModel);

	private final JTextArea logArea = new JTextArea();

	private final JLabel nameValue = new JLabel("-");
	private final JLabel typeValue = new JLabel("-");
	private final JLabel posValue = new JLabel("-");
	private final JLabel sizeValue = new JLabel("-");
	private final JLabel colorValue = new JLabel("-");

	private SceneData currentScene;

	public RobiClientFrame() {
		super("Robi Client");

		setLayout(new BorderLayout());
		add(createToolbar(), BorderLayout.NORTH);
		add(createMainArea(), BorderLayout.CENTER);
		add(createBottomArea(), BorderLayout.SOUTH);

		elementTree.addTreeSelectionListener(e -> updatePropertiesFromSelection());
		elementTree.setRootVisible(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 700);
		setLocationRelativeTo(null);
		setVisible(true);

		refreshScene();
	}

	private JPanel createToolbar() {
		JPanel toolbar = new JPanel(new GridLayout(3, 6, 5, 5));

		JButton rectButton = new JButton("Rect");
		JButton ovalButton = new JButton("Oval");
		JButton labelButton = new JButton("Label");
		JButton imageButton = new JButton("Image");

		JButton deleteButton = new JButton("Delete");
		JButton refreshButton = new JButton("Refresh");
		JButton clearButton = new JButton("Clear");
		JButton screenshotButton = new JButton("Screenshot");
		JButton saveButton = new JButton("Save");
		JButton loadButton = new JButton("Load");

		JButton colorButton = new JButton("Color");
		JButton positionButton = new JButton("Position");
		JButton sizeButton = new JButton("Size");

		JButton upButton = new JButton("↑");
		JButton downButton = new JButton("↓");
		JButton leftButton = new JButton("←");
		JButton rightButton = new JButton("→");

		rectButton.addActionListener(e -> sendAdd("Rect"));
		ovalButton.addActionListener(e -> sendAdd("Oval"));
		labelButton.addActionListener(e -> sendAdd("Label"));
		imageButton.addActionListener(e -> sendAdd("Image"));

		deleteButton.addActionListener(e -> sendDelete());
		refreshButton.addActionListener(e -> refreshScene());
		clearButton.addActionListener(e -> clearScene());
		screenshotButton.addActionListener(e -> saveScreenshot());
		saveButton.addActionListener(e -> saveScene());
		loadButton.addActionListener(e -> loadScene());

		colorButton.addActionListener(e -> changeColor());
		positionButton.addActionListener(e -> changePosition());
		sizeButton.addActionListener(e -> changeSize());

		leftButton.addActionListener(e -> moveSelected(-10, 0));
		rightButton.addActionListener(e -> moveSelected(10, 0));
		upButton.addActionListener(e -> moveSelected(0, -10));
		downButton.addActionListener(e -> moveSelected(0, 10));

		toolbar.add(rectButton);
		toolbar.add(ovalButton);
		toolbar.add(labelButton);
		toolbar.add(imageButton);
		toolbar.add(deleteButton);
		toolbar.add(refreshButton);
		toolbar.add(clearButton);
		toolbar.add(colorButton);
		toolbar.add(positionButton);
		toolbar.add(sizeButton);
		toolbar.add(leftButton);
		toolbar.add(upButton);
		toolbar.add(downButton);
		toolbar.add(rightButton);
		toolbar.add(screenshotButton);
		toolbar.add(saveButton);
		toolbar.add(loadButton);

		return toolbar;
	}

	private JSplitPane createMainArea() {
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBorder(BorderFactory.createTitledBorder("Elements"));
		leftPanel.add(new JScrollPane(elementTree), BorderLayout.CENTER);
		leftPanel.setPreferredSize(new Dimension(280, 400));

		JPanel rightPanel = new JPanel(new BorderLayout());
		drawingPanel.setBorder(BorderFactory.createTitledBorder("Drawing"));
		rightPanel.add(new JScrollPane(drawingPanel), BorderLayout.CENTER);
		rightPanel.add(createPropertiesPanel(), BorderLayout.EAST);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		splitPane.setDividerLocation(280);
		return splitPane;
	}

	private JPanel createPropertiesPanel() {
		JPanel propertiesPanel = new JPanel(new GridLayout(5, 2, 6, 6));
		propertiesPanel.setBorder(BorderFactory.createTitledBorder("Properties"));
		propertiesPanel.setPreferredSize(new Dimension(240, 200));

		propertiesPanel.add(label("Name"));
		propertiesPanel.add(nameValue);
		propertiesPanel.add(label("Type"));
		propertiesPanel.add(typeValue);
		propertiesPanel.add(label("Position"));
		propertiesPanel.add(posValue);
		propertiesPanel.add(label("Size"));
		propertiesPanel.add(sizeValue);
		propertiesPanel.add(label("Color"));
		propertiesPanel.add(colorValue);

		return propertiesPanel;
	}

	private JScrollPane createBottomArea() {
		logArea.setEditable(false);
		logArea.setRows(7);
		logArea.setBorder(BorderFactory.createTitledBorder("Log"));
		return new JScrollPane(logArea);
	}

	private JLabel label(String text) {
		return new JLabel(text + ":", SwingConstants.RIGHT);
	}

	private void sendAdd(String type) {
		String parentPath = getSelectedElementPath();
		if (parentPath == null) {
			parentPath = "space";
		}

		String name = JOptionPane.showInputDialog(this, "Name of new " + type + ":", type.toLowerCase());
		if (name == null || name.isBlank()) {
			return;
		}

		ClientRequest request = new ClientRequest("ADD");
		request.setTarget(parentPath);
		request.setType(type);
		request.setName(name.trim());

		if ("Label".equals(type)) {
			String text = JOptionPane.showInputDialog(this, "Label text:", name.trim());
			if (text == null) {
				return;
			}
			request.setText(text);
		}

		if ("Image".equals(type)) {
			JFileChooser chooser = new JFileChooser();
			int result = chooser.showOpenDialog(this);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			request.setImagePath(chooser.getSelectedFile().getAbsolutePath());
		}

		applyResponse(sendRequest(request));
		log("ADD " + type + " into " + parentPath);
	}

	private void sendDelete() {
		String selected = getSelectedElementPath();

		if (selected == null || "space".equals(selected)) {
			JOptionPane.showMessageDialog(this, "Select an element to delete.");
			return;
		}

		ClientRequest request = new ClientRequest("DELETE");
		request.setTarget(selected);
		applyResponse(sendRequest(request));
		log("DELETE " + selected);
	}

	private void refreshScene() {
		ClientRequest request = new ClientRequest("GET_SCENE");
		applyResponse(sendRequest(request));
		log("REFRESH");
	}

	private void clearScene() {
		int result = JOptionPane.showConfirmDialog(this, "Clear the whole scene?", "Confirm clear",
				JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		ClientRequest request = new ClientRequest("CLEAR");
		applyResponse(sendRequest(request));
		log("CLEAR");
	}

	private void saveScene() {
		JFileChooser chooser = new JFileChooser();
		int result = chooser.showSaveDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		ClientRequest request = new ClientRequest("SAVE");
		request.setPath(chooser.getSelectedFile().getAbsolutePath());
		applyResponse(sendRequest(request));
		log("SAVE " + chooser.getSelectedFile().getAbsolutePath());
	}

	private void loadScene() {
		JFileChooser chooser = new JFileChooser();
		int result = chooser.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		ClientRequest request = new ClientRequest("LOAD");
		request.setPath(chooser.getSelectedFile().getAbsolutePath());
		applyResponse(sendRequest(request));
		log("LOAD " + chooser.getSelectedFile().getAbsolutePath());
	}

	private void saveScreenshot() {
		if (currentScene == null) {
			JOptionPane.showMessageDialog(this, "Nothing to capture.");
			return;
		}

		JFileChooser chooser = new JFileChooser();
		int result = chooser.showSaveDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		if (!file.getName().toLowerCase().endsWith(".png")) {
			file = new File(file.getAbsolutePath() + ".png");
		}

		try {
			BufferedImage image = new BufferedImage(drawingPanel.getWidth(), drawingPanel.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			drawingPanel.paint(image.getGraphics());
			ImageIO.write(image, "png", file);
			log("SCREENSHOT " + file.getAbsolutePath());
			JOptionPane.showMessageDialog(this, "Screenshot saved.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Unable to save screenshot: " + e.getMessage());
			log("ERROR: " + e.getMessage());
		}
	}

	private void moveSelected(int dx, int dy) {
		String selected = getSelectedElementPath();

		if (selected == null || "space".equals(selected)) {
			JOptionPane.showMessageDialog(this, "Select an element to move.");
			return;
		}

		ClientRequest request = new ClientRequest("MOVE");
		request.setTarget(selected);
		request.setDx(dx);
		request.setDy(dy);
		applyResponse(sendRequest(request));
		log("MOVE " + selected + " by (" + dx + ", " + dy + ")");
	}

	private void changeColor() {
		String selected = getSelectedElementPath();
		if (selected == null) {
			selected = "space";
		}

		String[] colors = { "black", "white", "blue", "red", "green", "yellow", "orange", "pink", "gray", "lightgray",
				"darkgray", "cyan", "magenta" };

		String chosen = (String) JOptionPane.showInputDialog(this, "Choose color:", "Set Color",
				JOptionPane.PLAIN_MESSAGE, null, colors, colors[0]);

		if (chosen == null || chosen.isBlank()) {
			return;
		}

		ClientRequest request = new ClientRequest("SET_COLOR");
		request.setTarget(selected);
		request.setColor(chosen);
		applyResponse(sendRequest(request));
		log("SET_COLOR " + selected + " = " + chosen);
	}

	private void changePosition() {
		String selected = getSelectedElementPath();

		if (selected == null || "space".equals(selected)) {
			JOptionPane.showMessageDialog(this, "Select an element first.");
			return;
		}

		JTextField xField = new JTextField();
		JTextField yField = new JTextField();
		Object[] fields = { "X:", xField, "Y:", yField };

		int result = JOptionPane.showConfirmDialog(this, fields, "Set Position", JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		try {
			ClientRequest request = new ClientRequest("SET_POSITION");
			request.setTarget(selected);
			request.setX(Integer.parseInt(xField.getText()));
			request.setY(Integer.parseInt(yField.getText()));
			applyResponse(sendRequest(request));
			log("SET_POSITION " + selected + " = (" + request.getX() + ", " + request.getY() + ")");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Invalid position values.");
		}
	}

	private void changeSize() {
		String selected = getSelectedElementPath();
		if (selected == null) {
			selected = "space";
		}

		JTextField wField = new JTextField();
		JTextField hField = new JTextField();
		Object[] fields = { "Width:", wField, "Height:", hField };

		int result = JOptionPane.showConfirmDialog(this, fields, "Set Size", JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		try {
			ClientRequest request = new ClientRequest("SET_SIZE");
			request.setTarget(selected);
			request.setWidth(Integer.parseInt(wField.getText()));
			request.setHeight(Integer.parseInt(hField.getText()));
			applyResponse(sendRequest(request));
			log("SET_SIZE " + selected + " = " + request.getWidth() + " x " + request.getHeight());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Invalid size values.");
		}
	}

	private ServerResponse sendRequest(ClientRequest request) {
		try (Socket socket = new Socket("localhost", 5000);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

			out.writeObject(request);
			out.flush();
			return (ServerResponse) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse(false, e.getMessage(), null);
		}
	}

	private void applyResponse(ServerResponse response) {
		if (response == null) {
			return;
		}

		if (!response.isSuccess()) {
			JOptionPane.showMessageDialog(this, response.getMessage());
			log("ERROR: " + response.getMessage());
			return;
		}

		currentScene = response.getScene();
		drawingPanel.setScene(currentScene);
		rebuildTree(currentScene);
		updatePropertiesFromSelection();

		if (response.getMessage() != null && !response.getMessage().isBlank()) {
			log(response.getMessage());
		}
	}

	private void rebuildTree(SceneData scene) {
		String selectedPath = getSelectedElementPath();
		rootNode.removeAllChildren();

		if (scene != null) {
			for (ElementData element : scene.getElements()) {
				rootNode.add(buildNode(element, "space"));
			}
		}

		treeModel.reload();
		expandAll();
		restoreSelection(selectedPath);
	}

	private DefaultMutableTreeNode buildNode(ElementData element, String parentPath) {
		String fullPath = parentPath + "." + element.getName();
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeEntry(element.getName(), fullPath));

		for (ElementData child : element.getChildren()) {
			node.add(buildNode(child, fullPath));
		}

		return node;
	}

	private void restoreSelection(String selectedPath) {
		if (selectedPath == null || "space".equals(selectedPath)) {
			elementTree.setSelectionRow(0);
			return;
		}

		TreePath path = findTreePath(rootNode, selectedPath);
		if (path != null) {
			elementTree.setSelectionPath(path);
		}
	}

	private TreePath findTreePath(DefaultMutableTreeNode node, String fullPath) {
		Object userObject = node.getUserObject();
		if (userObject instanceof TreeEntry) {
			TreeEntry entry = (TreeEntry) userObject;
			if (fullPath.equals(entry.getFullPath())) {
				return new TreePath(node.getPath());
			}
		}

		for (int i = 0; i < node.getChildCount(); i++) {
			TreePath path = findTreePath((DefaultMutableTreeNode) node.getChildAt(i), fullPath);
			if (path != null) {
				return path;
			}
		}

		return null;
	}

	private void expandAll() {
		for (int i = 0; i < elementTree.getRowCount(); i++) {
			elementTree.expandRow(i);
		}
	}

	private String getSelectedElementPath() {
		TreePath path = elementTree.getSelectionPath();
		if (path == null) {
			return null;
		}

		Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
		if (!(userObject instanceof TreeEntry)) {
			return null;
		}

		return ((TreeEntry) userObject).getFullPath();
	}

	private void updatePropertiesFromSelection() {
		String selectedPath = getSelectedElementPath();

		if (selectedPath == null || currentScene == null || "space".equals(selectedPath)) {
			nameValue.setText("space");
			typeValue.setText("Space");
			posValue.setText("-");
			sizeValue.setText(currentScene == null ? "-" : currentScene.getWidth() + " x " + currentScene.getHeight());
			colorValue.setText(currentScene == null ? "-" : currentScene.getBackgroundColor());
			return;
		}

		ElementData element = findElementByPath(currentScene, selectedPath);
		if (element == null) {
			nameValue.setText("-");
			typeValue.setText("-");
			posValue.setText("-");
			sizeValue.setText("-");
			colorValue.setText("-");
			return;
		}

		nameValue.setText(element.getName());
		typeValue.setText(element.getType());
		posValue.setText("(" + element.getX() + ", " + element.getY() + ")");
		sizeValue.setText(element.getWidth() + " x " + element.getHeight());
		colorValue.setText(element.getColor());
	}

	private ElementData findElementByPath(SceneData scene, String fullPath) {
		if (scene == null || fullPath == null || "space".equals(fullPath)) {
			return null;
		}

		String normalized = fullPath.startsWith("space.") ? fullPath.substring("space.".length()) : fullPath;
		String[] parts = normalized.split("\\.");
		ElementData current = null;
		java.util.List<ElementData> level = scene.getElements();

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

	private String rgbToName(Color color) {
		if (Color.BLUE.equals(color))
			return "blue";
		if (Color.RED.equals(color))
			return "red";
		if (Color.GREEN.equals(color))
			return "green";
		if (Color.YELLOW.equals(color))
			return "yellow";
		if (Color.WHITE.equals(color))
			return "white";
		if (Color.ORANGE.equals(color))
			return "orange";
		if (Color.PINK.equals(color))
			return "pink";
		if (Color.GRAY.equals(color))
			return "gray";
		if (Color.LIGHT_GRAY.equals(color))
			return "lightgray";
		if (Color.DARK_GRAY.equals(color))
			return "darkgray";
		if (Color.CYAN.equals(color))
			return "cyan";
		if (Color.MAGENTA.equals(color))
			return "magenta";
		return "black";
	}

	private void log(String message) {
		logArea.append(message + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	private static class TreeEntry {
		private final String displayName;
		private final String fullPath;

		TreeEntry(String displayName, String fullPath) {
			this.displayName = displayName;
			this.fullPath = fullPath;
		}

		String getFullPath() {
			return fullPath;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}
}
