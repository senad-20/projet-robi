package api;

import stree.parser.SNode;

/**
 * Commande primitive exécutable sur une référence.
 */
public interface Command {
	abstract public Reference run(Reference receiver, SNode method);
}