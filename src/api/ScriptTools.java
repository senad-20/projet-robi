package api;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import stree.parser.SNode;
import stree.parser.SParser;

/**
 * Outils pour instancier un script en remplaçant ses paramètres par leurs
 * valeurs effectives, puis en re-parser l'expression.
 */
public final class ScriptTools {

	private ScriptTools() {
	}

	public static SNode instantiate(SNode expression, Map<String, String> bindings) {
		String source = toSource(expression);

		for (Map.Entry<String, String> entry : bindings.entrySet()) {
			source = replaceToken(source, entry.getKey(), entry.getValue());
		}

		SParser<SNode> parser = new SParser<>();
		try {
			return parser.parse(source).get(0);
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error("Impossible de parser l'expression instanciée : " + source);
		}
	}

	private static String toSource(SNode node) {
		if (node.size() == 0) {
			return node.contents();
		}

		StringBuilder builder = new StringBuilder();
		builder.append("(");

		for (int i = 0; i < node.size(); i++) {
			if (i > 0) {
				builder.append(" ");
			}
			builder.append(toSource(node.get(i)));
		}

		builder.append(")");
		return builder.toString();
	}

	private static String replaceToken(String source, String token, String value) {
	    String regex = "(?<![A-Za-z0-9_])" + Pattern.quote(token) + "(?![A-Za-z0-9_])";
	    return source.replaceAll(regex, value);
	}
}