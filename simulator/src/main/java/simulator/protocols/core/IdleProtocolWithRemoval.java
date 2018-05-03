package simulator.protocols.core;

import peersim.core.IdleProtocol;
import peersim.core.Node;

/**
 * Like {@link IdleProtocol}, but supports node removal.
 */
public class IdleProtocolWithRemoval extends IdleProtocol implements LinkableWithRemoval {

	public IdleProtocolWithRemoval(String prefix) {
		super(prefix);
	}

	@Override
	public int getIndexOfNeighbour(Node neighbour) {
		for (int i = 0; i < len; i++) {
			if (neighbors[i] == neighbour)
				return i;
		}
		return -1;
	}

	@Override
	public void removeNeighbor(int nodeIndex) {
		final int elementsToCopy = len - nodeIndex - 1;
		System.arraycopy(neighbors, nodeIndex + 1, neighbors, nodeIndex, elementsToCopy);
		neighbors[len - 1] = null;
		len--;
	}
}
