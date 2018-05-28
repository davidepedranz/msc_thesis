package simulator.protocols.core;

import org.junit.ClassRule;
import org.junit.Test;
import peersim.core.Node;
import simulator.utilities.FakeNode;
import simulator.utilities.IdleProtocolWithRemoval;
import simulator.utilities.PeersimClassRule;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class IdleProtocolWithRemovalTest {

	private static final Node n0 = new FakeNode();
	private static final Node n1 = new FakeNode();
	private static final Node n2 = new FakeNode();
	private static final Node n3 = new FakeNode();
	private static final Node n4 = new FakeNode();

	@ClassRule
	public static final PeersimClassRule setup = new PeersimClassRule();

	@Test
	public void removal() {
		final IdleProtocolWithRemoval links = new IdleProtocolWithRemoval("prefix");
		assertEquals(0, links.degree());
		links.addNeighbor(n0);
		links.addNeighbor(n1);
		links.addNeighbor(n2);
		links.addNeighbor(n3);
		links.addNeighbor(n4);
		assertEquals(5, links.degree());
		links.removeNeighbor(1);
		assertEquals(4, links.degree());
		assertEquals(n0, links.getNeighbor(0));
		assertEquals(n2, links.getNeighbor(1));
		assertEquals(n3, links.getNeighbor(2));
		assertEquals(n4, links.getNeighbor(3));
		nullOrIndexOutOfBounds(() -> links.getNeighbor(4));
	}

	private void nullOrIndexOutOfBounds(Supplier<Object> supplier) {
		try {
			final Object object = supplier.get();
			assertNull(object);
		} catch (IndexOutOfBoundsException e) {
			// no-op
		}
	}
}
