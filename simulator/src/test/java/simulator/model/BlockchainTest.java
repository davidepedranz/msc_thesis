package simulator.model;

import org.junit.ClassRule;
import org.junit.Test;
import simulator.utilities.GlobalState;
import simulator.utilities.PeersimSetup;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Blockchain}.
 */
public final class BlockchainTest {

	@ClassRule
	public static final PeersimSetup setup = new PeersimSetup();

	@Test
	public void empty() {
		final Blockchain blockchain = new Blockchain(Block.GENESIS);
		assertEquals("At the beginning, the number of forks should be 1", 1, blockchain.forks());
		assertEquals("At the beginning, the longest chain is the genesis", Block.GENESIS, blockchain.longestChain());
	}

	@Test
	public void noForks() {
		final Block b1 = Block.GENESIS;
		final Block b2 = Block.create(b1, randomTransactions(), 2);
		final Block b3 = Block.create(b2, randomTransactions(), 3);
		final Blockchain blockchain = new Blockchain(b1);
		blockchain.add(b2);
		blockchain.add(b3);
		assertEquals("On a linear chain, the number of forks should be 1", 1, blockchain.forks());
		assertEquals("On a linear chain, the longest chain should the last block", b3, blockchain.longestChain());
	}

	@Test
	public void oneFork() {
		final Block b1 = Block.GENESIS;
		final Block b2 = Block.create(b1, randomTransactions(), 2);
		final Block b3a = Block.create(b2, randomTransactions(), 3);
		final Block b3b = Block.create(b2, randomTransactions(), 4);
		final Block b4b = Block.create(b3b, randomTransactions(), 4);
		final Blockchain blockchain = new Blockchain(b1);
		blockchain.add(b2);
		blockchain.add(b3a);
		blockchain.add(b3b);
		blockchain.add(b4b);
		assertEquals("On a non linear chain, the number of forks should be greater than 1", 2, blockchain.forks());
		assertEquals(b4b, blockchain.longestChain());

	}

	private static List<Transaction> randomTransactions() {
		return randomTransactions(20);
	}

	@SuppressWarnings("SameParameterValue")
	private static List<Transaction> randomTransactions(int n) {
		return IntStream.range(0, n).mapToObj(__ -> GlobalState.nextTransaction()).collect(Collectors.toList());
	}
}
