package api;

/**
 * Référence simple pour transporter une valeur calculée
 * (entier, booléen, chaîne...).
 */
public class ValueReference extends Reference {

	public ValueReference(Object value) {
		super(value);
	}

	public Object getValue() {
		return getReceiver();
	}
}