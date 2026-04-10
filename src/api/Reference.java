package api;

import java.util.HashMap;
import java.util.Map;

import stree.parser.SNode;

/**
 * Référence un objet Java, son nom qualifié dans l'environnement et les
 * commandes qu'il comprend.
 */
public class Reference {

	private String name;
	private Object receiver;
	private Map<String, Command> primitives;

	public Reference(Object receiver) {
		this.receiver = receiver;
		this.primitives = new HashMap<>();
	}

	public Object getReceiver() {
		return receiver;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addCommand(String name, Command command) {
		primitives.put(name, command);
	}

	public boolean hasCommand(String name) {
		return primitives.containsKey(name);
	}

	public Reference run(SNode expr) {
		String commandName = expr.get(1).contents();
		Command command = primitives.get(commandName);

		if (command == null) {
			throw new Error("Commande inconnue : " + commandName + " pour " + name);
		}

		return command.run(this, expr);
	}
}