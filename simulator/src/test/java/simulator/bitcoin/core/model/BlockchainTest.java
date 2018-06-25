package simulator.bitcoin.core.model;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import peersim.core.CommonState;
import simulator.junit.PeersimClassRule;
import simulator.junit.PeersimRule;

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

	private static TransactionsWrapper randomTransactions() {
		return randomTransactions(20);
	}

	private static TransactionsWrapper randomTransactions(int n) {
		final Transaction[] transactions = IntStream
			.range(0, n)
			.mapToObj(__ -> Transactions.nextTransaction(1))
			.toArray(Transaction[]::new);
		return new TransactionsWrapper(transactions);
	}

	@Test
	public void empty() {
		final Blockchain blockchain = make(Blocks.GENESIS);
		assertEquals("At the beginning, the number of forks should be 1", 1, blockchain.forksNumber());
		assertEquals("At the beginning, the longest chain is the genesis", Blocks.GENESIS, blockchain.longestFork());
		assertArrayEquals(new Block[]{Blocks.GENESIS}, blockchain.forks());
		assertArrayEquals(new int[]{0}, blockchain.forksLengths());
	}

	@Test
	public void noForks() {
		final Block b1 = Blocks.GENESIS;
		final Block b2 = Blocks.nextBlock(b1, randomTransactions(), 2, 1);
		final Block b3 = Blocks.nextBlock(b2, randomTransactions(), 3, 2);
		final Blockchain blockchain = make(b1, b2, b3);
		assertEquals("On a linear chain, the number of forks should be 1", 1, blockchain.forksNumber());
		assertEquals("On a linear chain, the longest chain should the last block", b3, blockchain.longestFork());
		assertArrayEquals(new Block[]{b3}, blockchain.forks());
		assertArrayEquals(new int[]{0}, blockchain.forksLengths());
	}

	@Test
	public void oneFork() {
		final Block b1 = Blocks.GENESIS;
		final Block b2 = Blocks.nextBlock(b1, randomTransactions(), 2, 1);
		final Block b3a = Blocks.nextBlock(b2, randomTransactions(), 3, 1);
		final Block b3b = Blocks.nextBlock(b2, randomTransactions(), 4, 1);
		final Block b4b = Blocks.nextBlock(b3b, randomTransactions(), 4, 1);
		final Blockchain blockchain = make(b1, b2, b3a, b3b, b4b);
		assertEquals("On a non linear chain, the number of forks should be greater than 1", 2, blockchain.forksNumber());
		assertEquals(b4b, blockchain.longestFork());
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

		final Block a = Blocks.GENESIS;
		final Block b = Blocks.nextBlock(a, randomTransactions(), 2, 1);
		final Block c = Blocks.nextBlock(b, randomTransactions(), 3, 1);
		final Block d = Blocks.nextBlock(c, randomTransactions(), 3, 1);
		final Block e = Blocks.nextBlock(a, randomTransactions(), 3, 1);
		final Block f = Blocks.nextBlock(e, randomTransactions(), 3, 1);
		final Block g = Blocks.nextBlock(c, randomTransactions(), 3, 1);
		final Block h = Blocks.nextBlock(g, randomTransactions(), 3, 1);
		final Block i = Blocks.nextBlock(h, randomTransactions(), 3, 1);
		final Block j = Blocks.nextBlock(d, randomTransactions(), 3, 1);
		final Block k = Blocks.nextBlock(j, randomTransactions(), 3, 1);
		final Block l = Blocks.nextBlock(k, randomTransactions(), 3, 1);
		final Blockchain blockchain = make(a, b, c, d, e, f, g, h, i, j, k, l);

		// check forks
		assertEquals(3, blockchain.forksNumber());
		assertEquals(l, blockchain.longestFork());
		assertArrayEquals(new Block[]{l, f, i}, blockchain.forks());
		assertArrayEquals(new int[]{0, 2, 3}, blockchain.forksLengths());

		// check descendants
		assertThat(blockchain.descendants(j), containsInAnyOrder(j, k, l));
		assertThat(blockchain.descendants(c), containsInAnyOrder(c, g, h, i, d, j, k, l));
		assertThat(blockchain.descendants(a), containsInAnyOrder(a, b, c, d, e, f, g, h, i, j, k, l));

		// check common parents
		assertSame(a, blockchain.findCommonAncestor(e, l));
		assertSame(c, blockchain.findCommonAncestor(h, j));
		assertSame(k, blockchain.findCommonAncestor(k, l));
		assertSame(a, blockchain.findCommonAncestor(a, g));
	}

	@Test
	public void smokeTest() {
		final List<Block> blocks = new ArrayList<>(1001);
		blocks.add(Blocks.GENESIS);
		for (int i = 0; i < 1000; i++) {
			final int index = CommonState.r.nextInt(blocks.size());
			final Block block = Blocks.nextBlock(blocks.get(index), randomTransactions(0), 1, 1);
			blocks.add(block);
		}
		final Block[] container = new Block[blocks.size()];
		make(blocks.toArray(container));
	}

	private Blockchain make(Block... blocks) {
		final Blockchain blockchain = new Blockchain(blocks[0]);
		for (int i = 1; i < blocks.length; i++) {
			blockchain.addBlock(blocks[i]);
		}
		return blockchain;
	}
}
