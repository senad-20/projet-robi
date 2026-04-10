package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import shared.ElementData;
import shared.SceneData;

/**
 * Zone de rendu de la scène reçue du serveur.
 *
 * Le dessin est effectué récursivement en tenant compte des positions
 * relatives des éléments dans leurs conteneurs.
 */
public class DrawingPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final Map<String, Image> imageCache = new HashMap<>();
	private SceneData scene;

	public void setScene(SceneData scene) {
		this.scene = scene;

		if (scene != null) {
			setPreferredSize(new Dimension(scene.getWidth(), scene.getHeight()));
		}

		revalidate();
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (scene == null) {
			return;
		}

		g.setColor(toColor(scene.getBackgroundColor()));
		g.fillRect(0, 0, getWidth(), getHeight());
		drawElements(g, scene.getElements(), 0, 0);
	}

	private void drawElements(Graphics g, List<ElementData> elements, int parentX, int parentY) {
		for (ElementData element : elements) {
			int drawX = parentX + element.getX();
			int drawY = parentY + element.getY();

			g.setColor(toColor(element.getColor()));

			if ("Rect".equals(element.getType())) {
				g.fillRect(drawX, drawY, element.getWidth(), element.getHeight());
			} else if ("Oval".equals(element.getType())) {
				g.fillOval(drawX, drawY, element.getWidth(), element.getHeight());
			} else if ("Label".equals(element.getType())) {
				String text = element.getText() == null ? element.getName() : element.getText();
				g.drawString(text, drawX, drawY);
			} else if ("Image".equals(element.getType())) {
				drawImage(g, element, drawX, drawY);
			}

			drawElements(g, element.getChildren(), drawX, drawY);
		}
	}

	private void drawImage(Graphics g, ElementData element, int x, int y) {
		String imagePath = element.getImagePath();

		if (imagePath != null && !imagePath.isBlank()) {
			Image image = imageCache.computeIfAbsent(imagePath, key -> Toolkit.getDefaultToolkit().getImage(key));

			if (new File(imagePath).exists()) {
				g.drawImage(image, x, y, element.getWidth(), element.getHeight(), this);
				return;
			}
		}

		g.drawRect(x, y, element.getWidth(), element.getHeight());
		g.drawString("Image", x + 8, y + 18);
	}

	private Color toColor(String name) {
		if ("blue".equalsIgnoreCase(name))
			return Color.BLUE;
		if ("red".equalsIgnoreCase(name))
			return Color.RED;
		if ("green".equalsIgnoreCase(name))
			return Color.GREEN;
		if ("yellow".equalsIgnoreCase(name))
			return Color.YELLOW;
		if ("white".equalsIgnoreCase(name))
			return Color.WHITE;
		if ("orange".equalsIgnoreCase(name))
			return Color.ORANGE;
		if ("pink".equalsIgnoreCase(name))
			return Color.PINK;
		if ("gray".equalsIgnoreCase(name) || "grey".equalsIgnoreCase(name))
			return Color.GRAY;
		if ("lightgray".equalsIgnoreCase(name))
			return Color.LIGHT_GRAY;
		if ("darkgray".equalsIgnoreCase(name))
			return Color.DARK_GRAY;
		if ("cyan".equalsIgnoreCase(name))
			return Color.CYAN;
		if ("magenta".equalsIgnoreCase(name))
			return Color.MAGENTA;
		return Color.BLACK;
	}
}
