package api;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Environnement d'exécution : associe un nom qualifié à une référence.
 */
public class Environment {

	private Map<String, Reference> variables;

	public Environment() {
		this.variables = new HashMap<>();
	}

	public void addReference(String name, Reference reference) {
		reference.setName(name);
		variables.put(name, reference);
	}

	public Reference getReferenceByName(String name) {
		Reference ref = variables.get(name);

		if (ref == null) {
			throw new Error("Référence inconnue : " + name);
		}

		return ref;
	}

	public void removeReference(String name) {
		variables.remove(name);
	}

	/**
	 * Supprime une référence et tous ses descendants. Exemple : supprimer
	 * "space.robi" supprime aussi "space.robi.im".
	 */
	public void removeReferenceTree(String rootName) {
		variables.keySet().removeIf(key -> key.equals(rootName) || key.startsWith(rootName + "."));
	}

	public boolean hasReference(String name) {
		return variables.containsKey(name);
	}
	
	public Map<String, Reference> getReferences() {
		return new LinkedHashMap<>(variables);
	}
}