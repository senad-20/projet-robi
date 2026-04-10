package shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Représentation sérialisable de la scène complète.
 *
 * La scène correspond au conteneur racine « space » avec sa couleur de fond,
 * ses dimensions logiques et la liste de ses éléments de premier niveau.
 */
public class SceneData implements Serializable {
	private static final long serialVersionUID = 1L;

	private String backgroundColor = "white";
	private int width = 700;
	private int height = 500;
	private List<ElementData> elements = new ArrayList<>();

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
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

	public List<ElementData> getElements() {
		return elements;
	}
}
