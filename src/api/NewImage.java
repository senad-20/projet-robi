package api;

import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;

import graphicLayer.GImage;
import stree.parser.SNode;

/**
 * Crée dynamiquement une image à partir d'un fichier.
 */
public class NewImage implements Command {

	@Override
	public Reference run(Reference receiver, SNode method) {
		String filename = method.get(2).contents();

		File file = new File(filename);
		if (!file.exists()) {
			throw new Error("Fichier image introuvable : " + file.getAbsolutePath());
		}

		ImageIcon icon = new ImageIcon(file.getAbsolutePath());

		if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
			throw new Error("Impossible de charger l'image : " + file.getAbsolutePath());
		}

		Image image = icon.getImage();
		GImage gimage = new GImage(image);

		return GraphicReferenceFactory.createImageReference(gimage);
	}
}