package api;

import java.util.ArrayList;
import java.util.List;

import stree.parser.SNode;

/**
 * Commande permettant d'enregistrer un script nommé sur une référence.
 *
 * Exemple : (space addScript addImage ( (self filename) (self add im (Image new
 * filename)) ))
 */
public class AddScript implements Command {

	private Environment environment;

	public AddScript(Environment environment) {
		this.environment = environment;
	}

	@Override
	public Reference run(Reference receiver, SNode method) {
		if (method.size() < 4) {
			throw new Error("Syntaxe invalide pour addScript : " + method);
		}

		String scriptName = method.get(2).contents();
		SNode scriptDefinition = method.get(3);

		if (scriptDefinition.size() < 1) {
			throw new Error("Définition de script vide : " + method);
		}

		SNode parametersNode = scriptDefinition.get(0);
		List<String> parameterNames = new ArrayList<>();

		for (int i = 0; i < parametersNode.size(); i++) {
			parameterNames.add(parametersNode.get(i).contents());
		}

		if (parameterNames.isEmpty()) {
			throw new Error("Un script doit au moins définir le paramètre self : " + method);
		}

		if (!"self".equals(parameterNames.get(0))) {
			throw new Error("Le premier paramètre d'un script doit être self : " + method);
		}

		List<SNode> bodyExpressions = new ArrayList<>();
		for (int i = 1; i < scriptDefinition.size(); i++) {
			bodyExpressions.add(scriptDefinition.get(i));
		}

		StoredScript storedScript = new StoredScript(parameterNames, bodyExpressions);
		receiver.addCommand(scriptName, new ScriptCommand(environment, storedScript));

		return receiver;
	}
}