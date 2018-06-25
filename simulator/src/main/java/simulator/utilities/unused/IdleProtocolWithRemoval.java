package simulator.utilities.unused;

import peersim.core.IdleProtocol;
import peersim.core.Node;

/**
 * Like {@link IdleProtocol}, but supports node removal.
 */
public class IdleProtocolWithRemoval extends IdleProtocol implements LinkableWithRemoval {

	IdleProtocolWithRemoval(String prefix) {
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
	public void removeNeighbor(Node neighbour) {
		final int nodeIndex = getIndexOfNeighbour(neighbour);
		assert nodeIndex >= 0;
		removeNeighbor(nodeIndex);
	}

	@Override
	public void removeNeighbor(int neighbourIndex) {
		final int elementsToCopy = len - neighbourIndex - 1;
		System.arraycopy(neighbors, neighbourIndex + 1, neighbors, neighbourIndex, elementsToCopy);
		neighbors[len - 1] = null;
		len--;
	}
}
