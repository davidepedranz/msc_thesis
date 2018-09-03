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

import java.util.ArrayList;
import java.util.List;

/**
 * Global storage all ALL generated blocks. The store is populated when blocks
 * are generated during the simulation. Blocks are only generated when the mining
 * finishes, so this class will store only correct blocks.
 */
public final class Blocks {

    // keep track of the Genesis block
    public static final Block GENESIS;

    // keep track of all blocks, in order of generation
    private static final List<Block> BLOCKS_LIST;

    // keep track of the longest chain
    private static Block longestChain;

    // assign to each block a different progressive ID
    private static int blocksCounter;

    // keep track of the "global" blockchain
    private static Blockchain globalBlockchain;

    // initialize the static variables
    static {
        GENESIS = new Block(0, 0, null, new TransactionsWrapper(0), 0);
        BLOCKS_LIST = new ArrayList<>();
        _init();
    }

    // prevent class construction
    private Blocks() {
    }

    /**
     * Reset the state of the blocks to the original one (only the genesis exists).
     */
    public static void _init() {
        GENESIS.children = new Block[1];
        GENESIS.childrenNumber = 0;
        BLOCKS_LIST.add(GENESIS);
        longestChain = GENESIS;
        blocksCounter = 1;
        globalBlockchain = new Blockchain(GENESIS);
    }

    /**
     * Get the block with id i.
     *
     * @param i ID of the block to getBlock.
     * @return Block with the given index.
     */
    static Block getBlock(int i) {
        return BLOCKS_LIST.get(i);
    }

    /**
     * Generate a new block with the given transactions.
     *
     * @param previous     Previous block.
     * @param transactions List of transactions for the block.
     * @param timestamp    Creation timestamp.
     * @return The newly created block.
     */
    public static Block nextBlock(Block previous, TransactionsWrapper transactions, long timestamp) {
        final Block block = new Block(blocksCounter, previous.height + 1, previous, transactions, timestamp);
        blocksCounter++;
        updateChildren(previous, block);
        updateLongestChain(block);
        BLOCKS_LIST.add(block);
        globalBlockchain.addBlock(block);
        return block;
    }

    /**
     * Blocks store double pointers to the parent and the children nodes.
     * This method updated the pointers on creation of a new child.
     *
     * @param parent Parent node.
     * @param child  Child node.
     */
    private static void updateChildren(Block parent, Block child) {
        if (parent.children.length == parent.childrenNumber) {
            final int oldSize = parent.children.length;
            final int newSize = 2 * oldSize;
            final Block[] newChildren = new Block[newSize];
            System.arraycopy(parent.children, 0, newChildren, 0, oldSize);
            parent.children = newChildren;
        }
        parent.children[parent.childrenNumber] = child;
        parent.childrenNumber++;
    }

    /**
     * Keep track of the block with the greatest height.
     *
     * @param block Newly generated block.
     */
    private static void updateLongestChain(Block block) {
        if (block.height > longestChain.height) {
            longestChain = block;
        }
    }

    /**
     * @return The block that represents the current longest chain.
     */
    public static Block getLongestChain() {
        return longestChain;
    }

    /**
     * @return The instance of the global blockchain.
     */
    public static Blockchain getGlobalBlockchain() {
        return globalBlockchain;
    }
}
