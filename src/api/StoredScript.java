package api;

import java.util.List;

import stree.parser.SNode;

/**
 * Représente un script utilisateur enregistré sur une référence.
 *
 * Un script contient : - une liste de paramètres, dont "self" en premier ; -
 * une liste d'expressions à exécuter lors de l'appel du script.
 */
public class StoredScript {

	private List<String> parameterNames;
	private List<SNode> bodyExpressions;

	public StoredScript(List<String> parameterNames, List<SNode> bodyExpressions) {
		this.parameterNames = parameterNames;
		this.bodyExpressions = bodyExpressions;
	}

	public List<String> getParameterNames() {
		return parameterNames;
	}

	public List<SNode> getBodyExpressions() {
		return bodyExpressions;
	}
}