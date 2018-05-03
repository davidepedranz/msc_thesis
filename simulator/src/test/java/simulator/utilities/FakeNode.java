package simulator.utilities;

import peersim.core.Node;
import peersim.core.Protocol;

/**
 * Fake implementation of {@link peersim.core.Node} that can be
 * used for testing purposes.
 */
public final class FakeNode implements Node {

	@Override
	public Protocol getProtocol(int i) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public int protocolSize() {
		return 0;
	}

	@Override
	public void setIndex(int index) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public int getIndex() {
		return 0;
	}

	@Override
	public long getID() {
		return 0;
	}

	@Override
	public Object clone() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public int getFailState() {
		return 0;
	}

	@Override
	public void setFailState(int failState) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public boolean isUp() {
		return true;
	}
}
