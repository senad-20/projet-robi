package shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Représentation sérialisable d'un élément graphique.
 *
 * Les coordonnées sont relatives à leur conteneur direct. Un élément peut
 * éventuellement contenir des enfants lorsqu'il joue le rôle de conteneur.
 */
public class ElementData implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String type;
	private int x;
	private int y;
	private int width;
	private int height;
	private String color;
	private String text;
	private String imagePath;
	private List<ElementData> children = new ArrayList<>();

	public ElementData(String name, String type) {
		this.name = name;
		this.type = type;
		this.color = "black";
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public List<ElementData> getChildren() {
		return children;
	}
}
