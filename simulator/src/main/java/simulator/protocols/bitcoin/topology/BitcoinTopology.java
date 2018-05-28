package simulator.protocols.bitcoin.topology;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import simulator.events.StartEvent;
import simulator.observers.ControlMessagesMetric;
import simulator.protocols.bitcoin.topology.events.ScheduleAddrEvent;
import simulator.protocols.bitcoin.topology.events.SchedulePongTimeouts;
import simulator.protocols.bitcoin.topology.messages.AddrMessage;
import simulator.protocols.bitcoin.topology.messages.PingMessage;
import simulator.protocols.bitcoin.topology.messages.PongMessage;
import simulator.utilities.IdleProtocolWithRemoval;
import simulator.utilities.Shuffler;

import java.util.HashMap;
import java.util.Map;

/**
 * Bitcoin underlining P2P protocol to discover peers and construct
 * the network topology. See the references for the constants:
 * - https://github.com/bitcoin/bitcoin/blob/master/src/net.h
 * - https://github.com/bitcoin/bitcoin/blob/master/src/net.cpp
 */
public final class BitcoinTopology extends IdleProtocolWithRemoval implements ControlMessagesMetric, CDProtocol, EDProtocol {

	// TODO: keep track also of "known peers" (they are 1000, vs 125 that can have active connections)
	// TODO: bitcoin only keeps a limited number of peers... are they rotated somehow?
	// TODO: how do I select the peers to add? --> for now I just shuffle them!

	// configuration
	private static final String PARAMETER_MAX_PEER_CONNECTIONS = "max_peers";
	private static final String PARAMETER_FEELER_SLEEP_WINDOW = "feeler_sleep";
	private static final String PARAMETER_FEELER_INTERVAL = "feeler_interval";
	private static final String PARAMETER_PONG_TIMEOUT = "pong_timeout";

	// Bitcoin constants, defined in the Bitcoin source code... see references
	private final int DEFAULT_MAX_PEER_CONNECTIONS;
	private final long FEELER_SLEEP_WINDOW;
	private final long FEELER_INTERVAL;
	private final long PONG_TIMEOUT;

	// ping-pong status
	private Map<Long, Long> lastPongs;

	// metrics
	private long controlMessages;


	@SuppressWarnings("unused")
	public BitcoinTopology(String prefix) {
		super(prefix);

		// read configuration
		final String completePrefix = prefix + ".";
		this.DEFAULT_MAX_PEER_CONNECTIONS = Configuration.getInt(completePrefix + PARAMETER_MAX_PEER_CONNECTIONS);
		this.FEELER_SLEEP_WINDOW = Configuration.getLong(completePrefix + PARAMETER_FEELER_SLEEP_WINDOW);
		this.FEELER_INTERVAL = Configuration.getLong(completePrefix + PARAMETER_FEELER_INTERVAL);
		this.PONG_TIMEOUT = Configuration.getLong(completePrefix + PARAMETER_PONG_TIMEOUT);

		// keep track of the last time I saw a peer
		this.lastPongs = new HashMap<>();

		// metrics: keep track of the number of messages used to construct and maintain the topology
		this.controlMessages = 0;
	}


	// --------------------------------------------------
	//  ControlMessagesMetric: track protocol messages
	// --------------------------------------------------

	@Override
	public long controlMessages() {
		return controlMessages;
	}


	// --------------------------------------------------
	//  CDProtocol: schedule periodic actions
	// --------------------------------------------------

	// this is used to schedule Ping messages and handle pong timeouts
	@Override
	public void nextCycle(Node node, int pid) {
		sendPingMessages(node, pid);
	}

	private void sendPingMessages(Node node, int pid) {
		final PingMessage message = new PingMessage(node);
		final int numberOfMessages = sendToNeighbours(message, node, pid);
		controlMessages += numberOfMessages;
	}


	// --------------------------------------------------
	//  EDProtocol: receive and handle messages
	// --------------------------------------------------

	@Override
	public void processEvent(Node node, int pid, Object event) {

		// TODO: check it node is alive

		// got a ping... should reply with a pong if alive
		if (event instanceof PingMessage) {
			handlePingMessage((PingMessage) event, node, pid);
		}

		// got a pong... the node is alive
		else if (event instanceof PongMessage) {
			handlePongMessage((PongMessage) event);
		}

		// TODO: schedule SchedulePongTimeouts
		// time to check for pongs timeout
		else if (event instanceof SchedulePongTimeouts) {
			checkPongTimeouts();
		}

		// need to exchange peers with other nodes
		else if (event instanceof ScheduleAddrEvent) {
			sendAddrMessages(node, pid);
			scheduleNextAddrEvent(node, pid);
		}

		// got an addr message... need to update my peers
		else if (event instanceof AddrMessage) {
			handleAddrMessage((AddrMessage) event, node);
		}

		// start the protocol
		else if (event instanceof StartEvent) {
			scheduleNextAddrEvent(node, pid);
		}

		// no other events are possible
		else {
			assert false : "BitcoinTopology got an unknown event: " + event;
		}
	}

	/**
	 * Handle a ping message: immediately reply to the sender with a pong message.
	 */
	private void handlePingMessage(PingMessage message, Node node, int pid) {
		final Transport transport = (Transport) node.getProtocol(FastConfig.getTransport(pid));
		transport.send(node, message.sender, new PongMessage(node), pid);
		controlMessages++;
	}

	/**
	 * Handle a pong message: nothing to do here, just keep track that the node is alive.
	 */
	private void handlePongMessage(PongMessage event) {
		lastPongs.put(event.sender.getID(), CommonState.getTime());
	}

	/**
	 * Periodically, we need to check that the neighbours are alive. If one of them does
	 * not reply since some fixed timeout, we assume it is dead and remove it.
	 */
	private void checkPongTimeouts() {
		for (int i = 0; i < len; i++) {
			final Node current = getNeighbor(i);
			final long lastPongTime = lastPongs.get(current.getID());
			if (CommonState.getTime() - lastPongTime > PONG_TIMEOUT) {
				removeNeighbor(i);
			}
		}
	}

	/**
	 * Periodically, we need to push our peers list to all neighbors, in order
	 * to construct and maintain the p2p topology.
	 */
	private void sendAddrMessages(Node node, int pid) {
		final Node[] realNeighbors = new Node[len];
		System.arraycopy(neighbors, 0, realNeighbors, 0, len);
		final AddrMessage message = new AddrMessage(realNeighbors);
		final int numberOfMessages = sendToNeighbours(message, node, pid);
		controlMessages += numberOfMessages;
	}

	/**
	 * Schedules the need to send addr messages to all neighbors.
	 * This method is needed since we use the cycle-based model for ping messages.
	 */
	private void scheduleNextAddrEvent(Node node, int pid) {
		final long delay = FEELER_INTERVAL + CommonState.r.nextLong(FEELER_SLEEP_WINDOW);
		EDSimulator.add(delay, ScheduleAddrEvent.INSTANCE, node, pid);
	}

	/**
	 * Handle an addr message: update the list of peers, until the maximum capacity is reached.
	 */
	private void handleAddrMessage(AddrMessage message, Node me) {

		// optimization: do not shuffle the elements if not needed
		if (len >= DEFAULT_MAX_PEER_CONNECTIONS) {
			return;
		}

		// randomly pick some nodes to add to my list
		final Node[] peers = Shuffler.shuffle(message.peers);
		for (Node peer : peers) {
			if (len >= DEFAULT_MAX_PEER_CONNECTIONS) {
				break;
			}
			if (peer == me) {
				continue;
			}
			addNeighbor(peer);
		}
	}

	/**
	 * Utility method used to send the given message to all neighbors.
	 */
	private int sendToNeighbours(Object message, Node node, int pid) {
		final Transport transport = (Transport) node.getProtocol(FastConfig.getTransport(pid));
		final int size = degree();
		for (int i = 0; i < size; i++) {
			final Node peer = getNeighbor(i);
			transport.send(node, peer, message, pid);
		}
		return size;
	}

	// --------------------------------------------------
	//  Protocol: clone node
	// --------------------------------------------------

	@Override
	public BitcoinTopology clone() {
		final BitcoinTopology clone = (BitcoinTopology) super.clone();
		clone.lastPongs = new HashMap<>(lastPongs);
		return clone;
	}
}
