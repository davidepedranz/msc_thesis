package simulator.utilities.unused;

import peersim.core.Linkable;
import peersim.core.Node;

/**
 * Provides the same functionality as {@link peersim.core.Linkable}
 * plus the possibility to remove nodes.
 */
public interface LinkableWithRemoval extends Linkable {

	/**
	 * Similar to {@link Linkable#contains(Node)}, but returns the index of the neighbour
	 * it is it present, -1 otherwise.
	 *
	 * @param neighbour Node to verify.
	 * @return Index of the node if present, -1 otherwise.
	 */
	int getIndexOfNeighbour(Node neighbour);

	/**
	 * Remove a neighbor from the current set of neighbors.
	 * Please note that this is an expensive operation, use it only if really needed.
	 *
	 * @param neighbour Neighbor to remove.
	 */
	void removeNeighbor(Node neighbour);

	/**
	 * Remove a neighbor from the current set of neighbors.
	 * Please note that this is an expensive operation, use it only if really needed.
	 *
	 * @param neighbourIndex Index of the neighbor to remove.
	 */
	void removeNeighbor(int neighbourIndex);
}
