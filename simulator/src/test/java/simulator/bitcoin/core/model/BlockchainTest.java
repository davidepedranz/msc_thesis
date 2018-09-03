/*
 * Copyright (c) 2018 Davide Pedranz. All rights reserved.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package simulator.bitcoin.core.model;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import peersim.core.CommonState;
import peersim.junit.PeersimClassRule;
import peersim.junit.PeersimRule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

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
    public void testEmptyBlockchain() {
        final Blockchain blockchain = make(Blocks.GENESIS);
        assertEquals("At the beginning, the number of forks should be 1", 1, blockchain.forksNumber());
        assertEquals("At the beginning, the longest chain is the genesis", Blocks.GENESIS, blockchain.longestFork());
        assertArrayEquals(new Block[]{Blocks.GENESIS}, blockchain._forks());
        assertArrayEquals(new int[]{0}, blockchain.forksLengths());
    }

    @Test
    public void testBlockchainWithoutForks() {
        final Block b1 = Blocks.GENESIS;
        final Block b2 = Blocks.nextBlock(b1, randomTransactions(), 1);
        final Block b3 = Blocks.nextBlock(b2, randomTransactions(), 2);
        final Blockchain blockchain = make(b1, b2, b3);
        assertEquals("On a linear chain, the number of forks should be 1", 1, blockchain.forksNumber());
        assertEquals("On a linear chain, the longest chain should the last block", b3, blockchain.longestFork());
        assertArrayEquals(new Block[]{b3}, blockchain._forks());
        assertArrayEquals(new int[]{0}, blockchain.forksLengths());
    }

    @Test
    public void testBlockchainWithOneFork() {
        final Block b1 = Blocks.GENESIS;
        final Block b2 = Blocks.nextBlock(b1, randomTransactions(), 1);
        final Block b3a = Blocks.nextBlock(b2, randomTransactions(), 1);
        final Block b3b = Blocks.nextBlock(b2, randomTransactions(), 1);
        final Block b4b = Blocks.nextBlock(b3b, randomTransactions(), 1);
        final Blockchain blockchain = make(b1, b2, b3a, b3b, b4b);
        assertEquals("On a non linear chain, the number of forks should be greater than 1", 2, blockchain.forksNumber());
        assertEquals(b4b, blockchain.longestFork());
        assertArrayEquals(new Block[]{b3a, b4b}, blockchain._forks());
        assertArrayEquals(new int[]{1, 0}, blockchain.forksLengths());
    }

    @Test
    public void testBlockchainWithMultipleForks() {

        //                 g <- h <- i
        //               /
        //  a <- b <- c <- d <- j <- k <- l
        //	   \
        //		 e <- f

        final Block a = Blocks.GENESIS;
        final Block b = Blocks.nextBlock(a, randomTransactions(), 1);
        final Block c = Blocks.nextBlock(b, randomTransactions(), 1);
        final Block d = Blocks.nextBlock(c, randomTransactions(), 1);
        final Block e = Blocks.nextBlock(a, randomTransactions(), 1);
        final Block f = Blocks.nextBlock(e, randomTransactions(), 1);
        final Block g = Blocks.nextBlock(c, randomTransactions(), 1);
        final Block h = Blocks.nextBlock(g, randomTransactions(), 1);
        final Block i = Blocks.nextBlock(h, randomTransactions(), 1);
        final Block j = Blocks.nextBlock(d, randomTransactions(), 1);
        final Block k = Blocks.nextBlock(j, randomTransactions(), 1);
        final Block l = Blocks.nextBlock(k, randomTransactions(), 1);
        final Blockchain blockchain = make(a, b, c, d, e, f, g, h, i, j, k, l);

        // check _forks
        assertEquals(3, blockchain.forksNumber());
        assertEquals(l, blockchain.longestFork());
        assertArrayEquals(new Block[]{l, f, i}, blockchain._forks());
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
            final Block block = Blocks.nextBlock(blocks.get(index), randomTransactions(0), 1);
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
