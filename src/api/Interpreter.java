package api;

import stree.parser.SNode;

/**
 * Interpréteur avec support : - commandes graphiques habituelles - expressions
 * numériques et booléennes - variables - if / while / begin
 */
public class Interpreter {

	public Reference compute(Environment environment, SNode expr) {
		String head = expr.get(0).contents();

		switch (head) {
		case "set":
			return evalSet(environment, expr);
		case "if":
			return evalIf(environment, expr);
		case "while":
			return evalWhile(environment, expr);
		case "begin":
			return evalBegin(environment, expr);
		case "+":
		case "-":
		case "*":
		case "/":
			return evalArithmetic(environment, expr);
		case "<":
		case ">":
		case "=":
			return evalComparison(environment, expr);
		default:
			Reference receiver = environment.getReferenceByName(head);
			return receiver.run(expr);
		}
	}

	private Reference evalSet(Environment environment, SNode expr) {
		// (set i 0)
		// (set i (+ i 1))
		String varName = expr.get(1).contents();
		Reference value = evalAtomOrExpr(environment, expr.get(2));
		environment.addReference(varName, value);
		return value;
	}

	private Reference evalIf(Environment environment, SNode expr) {
		// (if condition exprThen exprElse)
		boolean condition = asBoolean(evalAtomOrExpr(environment, expr.get(1)));

		if (condition) {
			return evalAtomOrExpr(environment, expr.get(2));
		}

		if (expr.size() > 3) {
			return evalAtomOrExpr(environment, expr.get(3));
		}

		return new ValueReference(null);
	}

	private Reference evalWhile(Environment environment, SNode expr) {
		// (while condition body1 body2 ...)
		Reference last = new ValueReference(null);

		while (asBoolean(evalAtomOrExpr(environment, expr.get(1)))) {
			for (int i = 2; i < expr.size(); i++) {
				last = evalAtomOrExpr(environment, expr.get(i));
			}
		}

		return last;
	}

	private Reference evalBegin(Environment environment, SNode expr) {
		// (begin expr1 expr2 expr3 ...)
		Reference last = new ValueReference(null);

		for (int i = 1; i < expr.size(); i++) {
			last = evalAtomOrExpr(environment, expr.get(i));
		}

		return last;
	}

	private Reference evalArithmetic(Environment environment, SNode expr) {
		String op = expr.get(0).contents();
		int left = asInt(evalAtomOrExpr(environment, expr.get(1)));
		int right = asInt(evalAtomOrExpr(environment, expr.get(2)));

		switch (op) {
		case "+":
			return new ValueReference(left + right);
		case "-":
			return new ValueReference(left - right);
		case "*":
			return new ValueReference(left * right);
		case "/":
			return new ValueReference(left / right);
		default:
			throw new Error("Opérateur arithmétique inconnu : " + op);
		}
	}

	private Reference evalComparison(Environment environment, SNode expr) {
		String op = expr.get(0).contents();
		int left = asInt(evalAtomOrExpr(environment, expr.get(1)));
		int right = asInt(evalAtomOrExpr(environment, expr.get(2)));

		switch (op) {
		case "<":
			return new ValueReference(left < right);
		case ">":
			return new ValueReference(left > right);
		case "=":
			return new ValueReference(left == right);
		default:
			throw new Error("Opérateur de comparaison inconnu : " + op);
		}
	}

	/**
	 * Évalue soit : - une sous-expression : (+ i 1), (< i 10), ... - une variable
	 * déjà connue : i - un littéral entier : 10 - un booléen : true / false - sinon
	 * une chaîne brute
	 */
	private Reference evalAtomOrExpr(Environment environment, SNode node) {
		try {
			// si node est une liste, on peut tenter un accès à node.get(0)
			node.get(0);
			return compute(environment, node);
		} catch (Exception e) {
			String token = node.contents();

			if (isInteger(token)) {
				return new ValueReference(Integer.parseInt(token));
			}

			if ("true".equals(token)) {
				return new ValueReference(true);
			}

			if ("false".equals(token)) {
				return new ValueReference(false);
			}

			if (environment.hasReference(token)) {
				return environment.getReferenceByName(token);
			}

			return new ValueReference(token);
		}
	}

	private boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private int asInt(Reference ref) {
		Object value = ref.getReceiver();

		if (value instanceof Integer) {
			return (Integer) value;
		}

		if (value instanceof String && isInteger((String) value)) {
			return Integer.parseInt((String) value);
		}

		throw new Error("Valeur entière attendue, obtenu : " + value);
	}

	private boolean asBoolean(Reference ref) {
		Object value = ref.getReceiver();

		if (value instanceof Boolean) {
			return (Boolean) value;
		}

		if (value instanceof String) {
			return Boolean.parseBoolean((String) value);
		}

		throw new Error("Booléen attendu, obtenu : " + value);
	}
}