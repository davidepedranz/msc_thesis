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

package simulator.bitcoin.core;

import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.utilities.Distributions;
import simulator.bitcoin.core.events.BlockFoundEvent;
import simulator.bitcoin.core.messages.*;
import simulator.bitcoin.core.model.*;
import simulator.bitcoin.initializers.StartEvent;
import simulator.collections.CircularQueue;
import simulator.collections.IntList;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import static peersim.utilities.ConfigurationHelper.readInt;
import static peersim.utilities.ConfigurationHelper.readLong;
import static peersim.utilities.NetworkUtilities.broadcast;
import static peersim.utilities.NetworkUtilities.send;
import static peersim.utilities.TimeUnits.MINUTES;

/**
 * Bitcoin core protocol to handle transactions, blocks and mining.
 */
public final class BitcoinCore implements EDProtocol {

    // parameters
    private static final String PARAM_MEAN = "mean";
    private static final String PARAM_BLOCK_SIZE = "block_size";

    // default configuration
    private static final long DEFAULT_MEAN = 10L * MINUTES;
    private static final int DEFAULT_BLOCK_SIZE = 20;

    // configuration actual values, extracted from configuration and defaults
    private final long mean;
    private final int maxBlockSize;

    // current status of the protocol
    private final Blockchain blockchain;
    private final BitSet knownTransactions;
    private Block miningFromBlock;
    private BlockFoundEvent lastBlockFoundEvent;
    private final CircularQueue<Block> blocksToProcess;
    private final BitSet gossipedBlocks;
    private final BitSet gossipedTransactions;

    /**
     * Default constructor, following the PeerSim conventions.
     * The prefix field is automatically provided by the PeerSim engine.
     *
     * @param prefix Prefix of this protocol in the configuration file.
     */
    public BitcoinCore(String prefix) {

        // read configuration
        this.mean = readLong(prefix, PARAM_MEAN, DEFAULT_MEAN);
        this.maxBlockSize = readInt(prefix, PARAM_BLOCK_SIZE, DEFAULT_BLOCK_SIZE);

        // status
        this.blockchain = new Blockchain(Blocks.GENESIS);
        this.knownTransactions = new BitSet();
        this.miningFromBlock = Blocks.GENESIS;
        this.blocksToProcess = new CircularQueue<>();
        this.lastBlockFoundEvent = null;
        this.gossipedBlocks = new BitSet();
        this.gossipedTransactions = new BitSet();
    }

    /**
     * Copy constructor.
     *
     * @param original Instance to copy.
     */
    private BitcoinCore(BitcoinCore original) {
        this.mean = original.mean;
        this.maxBlockSize = original.maxBlockSize;
        this.blockchain = new Blockchain(original.blockchain);
        this.knownTransactions = (BitSet) original.knownTransactions.clone();
        this.miningFromBlock = original.miningFromBlock;
        this.lastBlockFoundEvent = original.lastBlockFoundEvent;
        this.blocksToProcess = new CircularQueue<>(original.blocksToProcess);
        this.gossipedBlocks = (BitSet) original.gossipedBlocks.clone();
        this.gossipedTransactions = (BitSet) original.gossipedTransactions.clone();
    }

    private static void sendCheckLinkable(Node from, Node to, int pid, Object message) {
        final Linkable linkable = (Linkable) from.getProtocol(FastConfig.getLinkable(pid));
        assert linkable.contains(to);
        send(from, to, pid, message);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public BitcoinCore clone() {
        return new BitcoinCore(this);
    }


    // ------------------------------------------------------------------------------
    //  Events
    // ------------------------------------------------------------------------------

    @Override
    public void processEvent(Node me, int pid, Object event) {

        // messages
        if (event instanceof GetBlocksMessage) {
            onGetBlocksMessage(me, pid, (GetBlocksMessage) event);
        } else if (event instanceof InvMessage) {
            onInvMessage(me, pid, (InvMessage) event);
        } else if (event instanceof GetDataMessage) {
            onGetDataMessage(me, pid, (GetDataMessage) event);
        } else if (event instanceof BlockMessage) {
            onBlockMessage(me, pid, (BlockMessage) event);
        } else if (event instanceof TxMessage) {
            onTxMessage(me, pid, (TxMessage) event);
        }

        // events
        else if (event instanceof BlockFoundEvent) {
            onBlockFoundEvent(me, pid, (BlockFoundEvent) event);
        } else if (event instanceof StartEvent) {
            onStart(me, pid);
        }

        // no other events are possible
        else {
            assert false : "BitcoinCore got an unknown event: " + event;
        }
    }

    /**
     * This method is invoked once the simulator hasBlock been bootstrap and is ready to run the simulation.
     * We start to simulate the mining process here.
     */
    private void onStart(Node me, int pid) {
        scheduleNextBlockMining(me, pid);
    }

    /**
     * A new block hasBlock been discovered by this node. Please note that the {@link BlockFoundEvent}
     * is always scheduled on the simulator when a new mining process is started, but the mining
     * can be interrupted because some other node is faster than this one. This method will take
     * care of discriminating real {@link BlockFoundEvent} from wrong ones.
     */
    private void onBlockFoundEvent(Node me, int pid, BlockFoundEvent event) {
        if (event == lastBlockFoundEvent) {
            final Block block = Blocks.nextBlock(event.previous, event.transactions, CommonState.getTime());
            addToBlockchain(me, pid, block);
            gossipBlock(me, pid, block);
            scheduleNextBlockMining(me, pid);
        }
    }


    // ------------------------------------------------------------------------------
    //  Messages
    // ------------------------------------------------------------------------------

    /**
     * Schedule the mining of a new block as an event in the simulator. This method schedules
     * a {@link BlockFoundEvent} after an exponential time. Please note that the events in the
     * simulator cannot be ignored, so we need to check that the mining hasBlock not been interrupted
     * when receiving a{@link BlockFoundEvent}. See {@link #onBlockFoundEvent(Node, int, BlockFoundEvent)}.
     */
    private void scheduleNextBlockMining(Node me, int pid) {
        final long delay = Distributions.nextExponentialRounded(mean);
        final TransactionsWrapper transactions = selectNextTransactions();
        final BlockFoundEvent event = new BlockFoundEvent(blockchain.longestFork(), transactions);
        EDSimulator.add(delay, event, me, pid);
        lastBlockFoundEvent = event;
    }

    /**
     * On a {@link GetBlocksMessage}, the node replies with the an {@link InvMessage}
     * containing the list of requested block headers.
     */
    private void onGetBlocksMessage(Node me, int pid, GetBlocksMessage message) {

        // query the blockchain and getBlock all descendants of the requested block hashes
        final List<Block> replyBlocks = new LinkedList<>();
        final int[] blockIDs = message.blockHeaders;
        for (int id : blockIDs) {
            final Block block = blockchain.getBlock(id);
            final List<Block> descendants = blockchain.descendants(block != null ? block : Blocks.GENESIS);
            replyBlocks.addAll(descendants);
        }

        // extract the block headers
        final int[] replyBlockHeaders = replyBlocks.stream().mapToInt(block -> block.id).toArray();

        // reply to the node with an Inv message
        final InvMessage invMessage = new InvMessage(me, replyBlockHeaders);
        sendCheckLinkable(me, message.sender, pid, invMessage);
    }

    /**
     * On a {@link InvMessage}, the node checks the received inventory against the
     * local knowledge and request any missing block / transaction if needed.
     */
    private void onInvMessage(Node me, int pid, InvMessage message) {
        final IntList queue = new IntList();
        final int[] blockIDs = message.headers;
        for (int id : blockIDs) {
            if (!blockchain.hasBlock(id)) {
                queue.add(id);
            }
        }
        if (!queue.isEmpty()) {
            final GetDataMessage getDataMessage = new GetDataMessage(me, queue);
            sendCheckLinkable(me, message.sender, pid, getDataMessage);
        }
    }

    /**
     * On a {@link GetDataMessage}, the node searches the requested objects and
     * reply with one {@link BlockMessage} for each block found.
     */
    private void onGetDataMessage(Node me, int pid, GetDataMessage message) {
        for (int i = 0; i < message.headers.size(); i++) {
            final int id = message.headers.get(i);
            if (blockchain.hasBlock(id)) {
                final BlockMessage blockMessage = new BlockMessage(blockchain.getBlock(id));
                sendCheckLinkable(me, message.sender, pid, blockMessage);
            }
        }
    }

    /**
     * On a {@link BlockMessage}, the node adds the received block to the local
     * blockchain and restarts the mining from the longest fork, if needed.
     */
    private void onBlockMessage(Node me, int pid, BlockMessage message) {
        addToBlockchain(me, pid, message.block);
        restartMiningIfNeeded(me, pid);
    }

    /**
     * On a {@link TxMessage}, the node adds it to the list of transactions to process
     * and broadcasts it to all other nodes, if not done yet.
     */
    private void onTxMessage(Node me, int pid, TxMessage message) {
        knownTransactions.set(message.transaction.id, true);
        gossipTransaction(me, pid, message);
    }

    /**
     * @return View of the blockchain of this node.
     */
    public Blockchain getBlockchain() {
        return blockchain;
    }

    // ------------------------------------------------------------------------------
    //  Block Utilities
    // ------------------------------------------------------------------------------

    /**
     * Select the transactions to insert in the new block from those that are
     * known but not yet stored in the blockchain.
     */
    private TransactionsWrapper selectNextTransactions() {
        final TransactionsWrapper wrapper = new TransactionsWrapper(maxBlockSize);
        int count = 0;
        int i = 0;
        while (count < maxBlockSize && i < knownTransactions.length()) {
            final boolean isKnown = knownTransactions.get(i);
            final boolean storedInBlocks = blockchain.hasProcessedTransactions(i);
            if (isKnown && !storedInBlocks) {
                final Transaction current = Transactions.getTransaction(i);
                wrapper.transactions[count] = current;
                count++;
            }
            i++;
        }
        wrapper.transactionsNumber = count;
        return wrapper;
    }

    /**
     * Adds a new block to the blockchain. If the block is new, broadcast it to all peers.
     * This method will take care of handling duplicates.
     */
    private void addToBlockchain(Node me, int pid, Block block) {

        // try to addBlock the block to the blockchain...
        final boolean result = processBlock(me, pid, block);

        // managed to insert the block in the blockchain and gossiped it
        // now we can try to process the old blocks
        if (result) {
            boolean proceed = true;
            while (proceed && !blocksToProcess.empty()) {
                final Block next = blocksToProcess.head();
                proceed = processBlock(me, pid, next);
                if (proceed) {
                    blocksToProcess.dequeue();
                }
            }
        }

        // parent of the given block is not available...
        // we request for the node and just queue the received block
        else {

            // temporary queue this block...
            blocksToProcess.enqueue(block);

            // ... and request its parent
            final GetBlocksMessage message = new GetBlocksMessage(me, new int[]{block.previous.id});
            broadcast(me, pid, message);
        }
    }

    private boolean processBlock(Node me, int pid, Block block) {

        // try to addBlock the block to the blockchain...
        // please note that this operation may fail if the parent node is missing
        // so we need to take extra care of requesting the peers for the missing blocks
        final boolean result = blockchain.addBlock(block);

        // the block can be insert in the blockchain
        // NB: the blockchain object keeps track of the processed transactions
        if (result) {

            // gossip block to the other nodes
            gossipBlock(me, pid, block);
        }

        return result;
    }

    /**
     * Forward the given block to all neighbours if the block is new, otherwise do nothing.
     * This is done to prevent to send the same block too many times.
     */
    private void gossipBlock(Node me, int pid, Block block) {
        if (!gossipedBlocks.get(block.id)) {
            gossipedBlocks.set(block.id, true);
            broadcast(me, pid, new BlockMessage(block));
        }
    }

    /**
     * Forward the given transaction to all neighbours if the block is new, otherwise do nothing.
     * This is done to prevent to send the same block too many times.
     */
    private void gossipTransaction(Node me, int pid, TxMessage message) {
        final int id = message.transaction.id;
        if (!gossipedTransactions.get(id)) {
            gossipedTransactions.set(id, true);
            broadcast(me, pid, message);
        }
    }


    // ------------------------------------------------------------------------------
    //  Network Utilities
    // ------------------------------------------------------------------------------

    /**
     * If the node is not mining the longest chain anymore, stop the mining process
     * and restart it from the new longest chain.
     */
    private void restartMiningIfNeeded(Node me, int pid) {
        final Block longestChain = blockchain.longestFork();
        if (miningFromBlock != longestChain) {
            // somebody discovered a block before me and changed the longest chain...
            // BitCoin strategy tells to always mine from the longest chain
            miningFromBlock = longestChain;
            scheduleNextBlockMining(me, pid);
        }
        // else -> no-op: we are already mining the longest chain...
    }
}
