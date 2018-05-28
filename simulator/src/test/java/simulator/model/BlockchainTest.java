package simulator.model;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import peersim.core.CommonState;
import simulator.utilities.GlobalState;
import simulator.utilities.PeersimClassRule;
import simulator.utilities.PeersimRule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

/**
 * Unit test for {@link Blockchain}.
 */
public final class BlockchainTest {

	@ClassRule
	public static final PeersimClassRule peersimClassRule = new PeersimClassRule();

	@Rule
	public final PeersimRule peersimRule = new PeersimRule();

	@Test
	public void empty() {
		final Blockchain blockchain = make(Block.GENESIS);
		assertEquals("At the beginning, the number of forks should be 1", 1, blockchain.forksNumber());
		assertEquals("At the beginning, the longest chain is the genesis", Block.GENESIS, blockchain.longestChain());
		assertArrayEquals(new Block[]{Block.GENESIS}, blockchain.forks());
		assertArrayEquals(new int[]{0}, blockchain.forksLengths());
	}

	@Test
	public void noForks() {
		final Block b1 = Block.GENESIS;
		final Block b2 = Block.create(b1, randomTransactions(), 2);
		final Block b3 = Block.create(b2, randomTransactions(), 3);
		final Blockchain blockchain = make(b1, b2, b3);
		assertEquals("On a linear chain, the number of forks should be 1", 1, blockchain.forksNumber());
		assertEquals("On a linear chain, the longest chain should the last block", b3, blockchain.longestChain());
		assertArrayEquals(new Block[]{b3}, blockchain.forks());
		assertArrayEquals(new int[]{0}, blockchain.forksLengths());
	}

	@Test
	public void oneFork() {
		final Block b1 = Block.GENESIS;
		final Block b2 = Block.create(b1, randomTransactions(), 2);
		final Block b3a = Block.create(b2, randomTransactions(), 3);
		final Block b3b = Block.create(b2, randomTransactions(), 4);
		final Block b4b = Block.create(b3b, randomTransactions(), 4);
		final Blockchain blockchain = make(b1, b2, b3a, b3b, b4b);
		assertEquals("On a non linear chain, the number of forks should be greater than 1", 2, blockchain.forksNumber());
		assertEquals(b4b, blockchain.longestChain());
		assertArrayEquals(new Block[]{b3a, b4b}, blockchain.forks());
		assertArrayEquals(new int[]{1, 0}, blockchain.forksLengths());
	}

	@Test
	public void multipleForks() {

		//                 g <- h <- i
		//               /
		//  a <- b <- c <- d <- j <- k <- l
		//	   \
		//		 e <- f

		final Block a = Block.GENESIS;
		final Block b = Block.create(a, randomTransactions(), 2);
		final Block c = Block.create(b, randomTransactions(), 3);
		final Block d = Block.create(c, randomTransactions(), 3);
		final Block e = Block.create(a, randomTransactions(), 3);
		final Block f = Block.create(e, randomTransactions(), 3);
		final Block g = Block.create(c, randomTransactions(), 3);
		final Block h = Block.create(g, randomTransactions(), 3);
		final Block i = Block.create(h, randomTransactions(), 3);
		final Block j = Block.create(d, randomTransactions(), 3);
		final Block k = Block.create(j, randomTransactions(), 3);
		final Block l = Block.create(k, randomTransactions(), 3);
		final Blockchain blockchain = make(a, b, c, d, e, f, g, h, i, j, k, l);

		// check forks
		assertEquals(3, blockchain.forksNumber());
		assertEquals(l, blockchain.longestChain());
		assertArrayEquals(new Block[]{l, f, i}, blockchain.forks());
		assertArrayEquals(new int[]{0, 2, 3}, blockchain.forksLengths());

		// check descendants
//		assertThat(blockchain.descendants(j), containsInAnyOrder(j, k, l));
//		assertThat(blockchain.descendants(c), containsInAnyOrder(c, g, h, i, d, j, k, l));
		assertThat(blockchain.descendants(a), containsInAnyOrder(a, b, c, d, e, f, g, h, i, j, k, l));
	}

	@Test
	public void smokeTest() {
		final List<Block> blocks = new ArrayList<>(1001);
		blocks.add(Block.GENESIS);
		for (int i = 0; i < 1000; i++) {
			final int index = CommonState.r.nextInt(blocks.size());
			final Block block = Block.create(blocks.get(index), new Transaction[]{}, 1);
			blocks.add(block);
		}
		final Block[] container = new Block[blocks.size()];
		make(blocks.toArray(container));
	}

	private Blockchain make(Block... blocks) {
		final Blockchain blockchain = new Blockchain(blocks[0]);
		for (int i = 1; i < blocks.length; i++) {
			blockchain.add(blocks[i]);
		}
		return blockchain;
	}

	private static Transaction[] randomTransactions() {
		return randomTransactions(20);
	}

	@SuppressWarnings("SameParameterValue")
	private static Transaction[] randomTransactions(int n) {
		return IntStream.range(0, n).mapToObj(__ -> GlobalState.nextTransaction()).toArray(Transaction[]::new);
	}
}
