package api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import stree.parser.SNode;

/**
 * Commande correspondant à un script utilisateur déjà enregistré.
 *
 * Lors de l'exécution : - self est lié au nom qualifié du receveur ; - les
 * autres paramètres sont liés aux arguments d'appel ; - chaque expression du
 * corps est instanciée puis rejouée via l'interpréteur normal.
 */
public class ScriptCommand implements Command {

	private Environment environment;
	private StoredScript storedScript;
	private Interpreter interpreter;

	public ScriptCommand(Environment environment, StoredScript storedScript) {
		this.environment = environment;
		this.storedScript = storedScript;
		this.interpreter = new Interpreter();
	}

	@Override
	public Reference run(Reference receiver, SNode method) {
		List<String> parameterNames = storedScript.getParameterNames();

		if (method.size() - 2 != parameterNames.size() - 1) {
			throw new Error("Nombre d'arguments invalide pour le script : " + method);
		}

		Map<String, String> bindings = new LinkedHashMap<>();
		bindings.put("self", receiver.getName());

		for (int i = 1; i < parameterNames.size(); i++) {
			String parameterName = parameterNames.get(i);
			String argumentValue = method.get(i + 1).contents();
			bindings.put(parameterName, argumentValue);
		}

		for (SNode expr : storedScript.getBodyExpressions()) {
			SNode instantiatedExpr = ScriptTools.instantiate(expr, bindings);
			interpreter.compute(environment, instantiatedExpr);
		}

		return receiver;
	}
}