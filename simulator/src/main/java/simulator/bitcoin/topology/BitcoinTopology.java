package simulator.bitcoin.topology;

import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import simulator.bitcoin.topology.events.ScheduleConnectTimeoutEvent;
import simulator.bitcoin.topology.events.ScheduleFlushAddrEvent;
import simulator.bitcoin.topology.events.SchedulePingEvent;
import simulator.bitcoin.topology.events.ScheduleQueueAddrEvent;
import simulator.bitcoin.topology.messages.*;
import simulator.initializers.StartEvent;
import simulator.utilities.structures.ArrayUtilities;
import simulator.utilities.structures.CircularQueue;
import simulator.utilities.structures.ObjectQueue;

import static simulator.utilities.peersim.ConfigurationHelper.readInt;
import static simulator.utilities.peersim.NetworkUtilities.send;

/**
 * Bitcoin underlining P2P protocol to discover peers and construct the network topology:
 * - https://eprint.iacr.org/2015/263.pdf
 * - https://dl.acm.org/citation.cfm?id=2660379
 * - https://www.cs.umd.edu/projects/coinscope/coinscope.pdf
 */
public final class BitcoinTopology implements EDProtocol, Linkable, Cloneable {

	// configuration parameters
	private static final String PARAMETER_MAX_INCOMING_CONNECTIONS = "max_incoming_connections";
	private static final String PARAMETER_MAX_OUTGOING_CONNECTIONS = "max_outgoing_connections";
	private static final String PARAMETER_MAX_PEER_ADDRESSES = "max_peer_addresses";
	private static final String PARAMETER_PING_INTERVAL = "ping_interval";
	private static final String PARAMETER_PONG_TIMEOUT = "pong_timeout";
	private static final String PARAMETER_QUEUE_ADDR_INTERVAL = "queue_addr_interval";
	private static final String PARAMETER_FLUSH_ADDR_INTERVAL = "flush_addr_interval";
	private static final String PARAMETER_CONNECT_TIMEOUT_INTERVAL = "connect_timeout_internal";
	private static final String PARAMETER_MAX_IPS_ADDR_MESSAGE_FOR_GOSSIP = "max_addr_message_for_gossip";
	private static final String PARAMETER_NODES_TO_GOSSIP_ADDR_MESSAGES = "nodes_to_gossip_addr_messages";

	// default configuration
	private static final int DEFAULT_MAX_INCOMING_CONNECTIONS = 117;
	private static final int DEFAULT_MAX_OUTGOING_CONNECTIONS = 8;
	private static final int DEFAULT_MAX_PEER_ADDRESSES = 20480;
	private static final int DEFAULT_PING_INTERVAL = 2 * 60 * 1000;                 // 2 minutes
	private static final int DEFAULT_PONG_TIMEOUT = 20 * 60 * 1000;                 // 20 minutes
	private static final int DEFAULT_QUEUE_ADDR_INTERVAL = 24 * 60 * 60 * 1000;     // 24 hours
	private static final int DEFAULT_FLUSH_ADDR_INTERVAL = 100;                     // 100 ms
	private static final int DEFAULT_CONNECT_TIMEOUT_INTERVAL = 11 * 1000;          // 11 s
	private static final int DEFAULT_MAX_IPS_ADDR_MESSAGE_FOR_GOSSIP = 10;
	private static final int DEFAULT_NODES_TO_GOSSIP_ADDR_MESSAGES = 2;

	// configuration actual values, extracted from configuration and defaults
	private final int maxIncomingConnections;
	private final int maxOutgoingConnections;
	private final int maxPeerAddresses;
	private final int pingInterval;
	private final int pongTimeout;
	private final int queueAddrInterval;
	private final int flushAddrInterval;
	private final int connectTimeoutInterval;
	private final int maxIpsAddrMessageForGossip;
	private final int nodesToGossipAddrMessages;

	// metrics
	public long versionMessages;
	public long verAckMessages;
	public long getAddrMessages;
	public long addrMessages;
	public long pingMessages;
	public long pongMessages;
	public long nodesRemovedForPongTimeout;

	// status of the outgoing and incoming connections
	private int outgoingConnectionsNumber;
	private Connection[] outgoingConnections;
	private int incomingConnectionsNumber;
	private Connection[] incomingConnections;

	// list of known peers
	private ObjectQueue<Node> peers;

	// tells if this node is currently opening a connection with another node
	private boolean connecting;

	public BitcoinTopology(String prefix) {

		// read configuration
		this.maxIncomingConnections = readInt(prefix, PARAMETER_MAX_INCOMING_CONNECTIONS, DEFAULT_MAX_INCOMING_CONNECTIONS);
		this.maxOutgoingConnections = readInt(prefix, PARAMETER_MAX_OUTGOING_CONNECTIONS, DEFAULT_MAX_OUTGOING_CONNECTIONS);
		this.maxPeerAddresses = readInt(prefix, PARAMETER_MAX_PEER_ADDRESSES, DEFAULT_MAX_PEER_ADDRESSES);
		this.pingInterval = readInt(prefix, PARAMETER_PING_INTERVAL, DEFAULT_PING_INTERVAL);
		this.pongTimeout = readInt(prefix, PARAMETER_PONG_TIMEOUT, DEFAULT_PONG_TIMEOUT);
		this.queueAddrInterval = readInt(prefix, PARAMETER_QUEUE_ADDR_INTERVAL, DEFAULT_QUEUE_ADDR_INTERVAL);
		this.flushAddrInterval = readInt(prefix, PARAMETER_FLUSH_ADDR_INTERVAL, DEFAULT_FLUSH_ADDR_INTERVAL);
		this.connectTimeoutInterval = readInt(prefix, PARAMETER_CONNECT_TIMEOUT_INTERVAL, DEFAULT_CONNECT_TIMEOUT_INTERVAL);
		this.maxIpsAddrMessageForGossip = readInt(prefix, PARAMETER_MAX_IPS_ADDR_MESSAGE_FOR_GOSSIP, DEFAULT_MAX_IPS_ADDR_MESSAGE_FOR_GOSSIP);
		this.nodesToGossipAddrMessages = readInt(prefix, PARAMETER_NODES_TO_GOSSIP_ADDR_MESSAGES, DEFAULT_NODES_TO_GOSSIP_ADDR_MESSAGES);

		// keep track of the neighbors
		this.outgoingConnectionsNumber = 0;
		this.outgoingConnections = new Connection[maxOutgoingConnections];
		this.incomingConnectionsNumber = 0;
		this.incomingConnections = new Connection[maxIncomingConnections];

		// keep track of the known peers
		this.peers = new ObjectQueue<>();

		// keep track if I am trying to establish a connection
		this.connecting = false;

		// metrics: keep track of the number of messages used to construct and maintain the topology
		this.versionMessages = 0;
		this.verAckMessages = 0;
		this.getAddrMessages = 0;
		this.addrMessages = 0;
		this.pingMessages = 0;
		this.pongMessages = 0;

		// metrics: keep track of interesting events
		this.nodesRemovedForPongTimeout = 0;
	}

	@Override
	public BitcoinTopology clone() {
		try {
			final BitcoinTopology clone = (BitcoinTopology) super.clone();

			clone.outgoingConnections = new Connection[this.outgoingConnections.length];
			for (int i = 0; i < outgoingConnectionsNumber; i++) {
				clone.outgoingConnections[i] = this.outgoingConnections[i].clone();
			}

			clone.incomingConnections = new Connection[this.incomingConnections.length];
			for (int i = 0; i < incomingConnectionsNumber; i++) {
				clone.incomingConnections[i] = this.incomingConnections[i].clone();
			}

			clone.peers = new ObjectQueue<>(this.peers);

			return clone;

		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);    // never happens
		}
	}

	@Override
	public void onKill() {
		this.outgoingConnectionsNumber = 0;
		this.outgoingConnections = null;
		this.incomingConnectionsNumber = 0;
		this.incomingConnections = null;
		this.peers = null;
		this.connecting = false;
	}

	@Override
	public void processEvent(Node me, int pid, Object event) {

		// handle events
		if (event instanceof ScheduleFlushAddrEvent) {
			onFlushAddrEvent(me, pid);
		} else if (event instanceof ScheduleQueueAddrEvent) {
			onQueueAddrEvent(me, pid);
		} else if (event instanceof SchedulePingEvent) {
			onPingEvent(me, pid);
		} else if (event instanceof ScheduleConnectTimeoutEvent) {
			onConnectTimeout(me, pid, (ScheduleConnectTimeoutEvent) event);
		}

		// handle messages
		else if (event instanceof VersionMessage) {
			onVersionMessage(me, pid, (VersionMessage) event);
		} else if (event instanceof VerAckMessage) {
			onVerAckMessage(me, pid, (VerAckMessage) event);
		} else if (event instanceof GetAddrMessage) {
			onGetAddrMessage((GetAddrMessage) event);
		} else if (event instanceof AddrMessage) {
			onAddrMessage(me, pid, (AddrMessage) event);
		} else if (event instanceof PingMessage) {
			onPingMessage(me, pid, (PingMessage) event);
		} else if (event instanceof PongMessage) {
			onPongMessage((PongMessage) event);
		}

		// start the protocol
		else if (event instanceof StartEvent) {
			onStart(me, pid);
		}

		// no other events are possible
		else {
			assert false : "BitcoinTopology got an unknown event: " + event;
		}
	}


	// -------------------------------------------------------------------------
	//  EDProtocol: handle events and received messages
	// -------------------------------------------------------------------------

	/**
	 * This method is invoked once the simulator hasBlock been bootstrap and is ready to run the simulation.
	 * We schedule all periodic events and initialize the gossip protocol here.
	 */
	private void onStart(Node me, int pid) {
		scheduleNextFlushAddrEvent(me, pid);
		scheduleNextQueueAddrEvent(me, pid);
		scheduleNextPingEvent(me, pid);
		onCheckOutgoingConnections(me, pid);
	}


	// -----------------------------------------------------------------------------------------------------
	//  Events
	// -----------------------------------------------------------------------------------------------------

	/**
	 * Schedule the next round in which to flush one of the addr queues.
	 */
	private void scheduleNextFlushAddrEvent(Node me, int pid) {
		EDSimulator.add(flushAddrInterval, ScheduleFlushAddrEvent.INSTANCE, me, pid);
	}

	/**
	 * Schedules the need to send addr messages to all neighbors.
	 */
	private void scheduleNextQueueAddrEvent(Node me, int pid) {
		EDSimulator.add(queueAddrInterval, ScheduleQueueAddrEvent.INSTANCE, me, pid);
	}

	/**
	 * Schedules the need to ping the connected peers to verify they are alive.
	 */
	private void scheduleNextPingEvent(Node me, int pid) {
		EDSimulator.add(pingInterval, SchedulePingEvent.INSTANCE, me, pid);
	}

	/**
	 * Check if the number of outgoing connections hasBlock been reached.
	 * If not, try to connect to a new peer.
	 */
	private void onCheckOutgoingConnections(Node me, int pid) {

		// this method can be called even if the limit of connections is reached
		// or the node is already connecting to another node
		if (!connecting && outgoingConnectionsNumber < maxOutgoingConnections) {

			// select a random node to connect to
			// NB: this is an approximation of the real Bitcoin protocol
			final Node[] nodes = peers.dump(new Node[0]);
			ArrayUtilities.shuffleInPlace(nodes);
			Node node = null;
			for (Node current : nodes) {
				final boolean contains = contains(current);
				if (!contains) {
					node = current;
					break;
				}
			}

			// a node which I am not already connected to hasBlock been found... try to connect
			// else: no node to connect to was found... SKIP
			if (node != null) {
				assert node != me : "My peers should never contain myself";

				// try to connect to the node == send the first message
				connecting = true;
				send(me, node, pid, new VersionMessage(me));

				// schedule a timeout to simulate a possibly refused connection
				EDSimulator.add(connectTimeoutInterval, new ScheduleConnectTimeoutEvent(node), me, pid);

				// update metrics
				versionMessages++;
			}
		}
	}

	/**
	 * Addr messages are not immediately sent to the target... instead they are
	 * queued and delivered in the round that corresponds to the target node.
	 */
	private void onFlushAddrEvent(Node me, int pid) {
		clearKnownLists();
		flushAddrMessages(me, pid);
		scheduleNextFlushAddrEvent(me, pid);
	}

	/**
	 * Clear the content of the knownList lists, that keep track of the IP addresses
	 * learned and sent to each neighbor. The lists are cleared every 24 hours.
	 */
	private void clearKnownLists() {
		for (int i = 0; i < outgoingConnectionsNumber; i++) {
			outgoingConnections[i].knownList.clear();
		}
		for (int i = 0; i < incomingConnectionsNumber; i++) {
			incomingConnections[i].knownList.clear();
		}
	}

	/**
	 * Every 100ms, a Bitcoin node randomly selects one of its peer and flushes
	 * its addr queue messages, i.e. sends to the selected peer all addr messages
	 * that where previously scheduled for it.
	 */
	private void flushAddrMessages(Node me, int pid) {
		final int degree = degree();
		if (degree > 0) {

			// pick a random neighbour
			final int random = CommonState.r.nextInt(degree);
			final Connection connection = getConnection(random);

			// flush all messages
			AddrMessage message;
			while ((message = connection.addrQueue.dequeue()) != null) {
				send(me, connection.neighbour, pid, message);
				addrMessages += Math.ceil(1.0 * message.peers.length / 1000);
			}
		}
	}

	/**
	 * Once every 24 hours, a Bitcoin node sends its own address to all neighbours, which
	 * will gossip it to their peers. This is the way Bitcoin implements peers discovery.
	 */
	private void onQueueAddrEvent(Node me, int pid) {
		broadcastMyAddress(me);
		scheduleNextQueueAddrEvent(me, pid);
	}

	/**
	 * Broadcast my address to all neighbors.
	 */
	private void broadcastMyAddress(Node me) {
		final AddrMessage message = new AddrMessage(me);
		for (int i = 0; i < outgoingConnectionsNumber; i++) {
			outgoingConnections[i].addrQueue.enqueue(message);
		}
		for (int i = 0; i < incomingConnectionsNumber; i++) {
			incomingConnections[i].addrQueue.enqueue(message);
		}
	}

	/**
	 * Once every 2 minutes, a Bitcoin node will ping its nodes to check they are alive.
	 * If a peer does not reply for a long time, the node will drop the connection.
	 */
	private void onPingEvent(Node me, int pid) {
		sendPingMessages(me, pid);
		checkPongTimeouts(me, pid);
		scheduleNextPingEvent(me, pid);
	}

	/**
	 * Send a ping message to all neighbours. They are expected to reply with a pong
	 * message to keep the connection open.
	 */
	private void sendPingMessages(Node me, int pid) {
		final PingMessage message = new PingMessage(me);
		multicast(me, pid, message);
		pingMessages += degree();
	}

	/**
	 * Periodically, we need to check that the neighbours are alive. If one of them does
	 * not reply since some fixed timeout, we assume it is dead and remove it.
	 */
	private void checkPongTimeouts(Node me, int pid) {
		final long currentTime = CommonState.getTime();
		for (int i = 0; i < degree(); i++) {
			final Connection connection = getConnection(i);
			if (currentTime - connection.lastPong > pongTimeout) {
				removeNeighborBothSides(me, pid, i);
				nodesRemovedForPongTimeout++;
			}
		}
	}

	/**
	 * Remove a link between this node and the node with the given index.
	 * This method will make sure to remove the link from both nodes.
	 */
	private void removeNeighborBothSides(Node node, int pid, int indexToRemove) {

		// remove link from current node to peer
		final Node peer = getNeighbor(indexToRemove);
		removeNeighbor(indexToRemove);

		// remove link from peer to current node
		final BitcoinTopology peerProtocol = (BitcoinTopology) peer.getProtocol(pid);
		peerProtocol.removeNeighbor(node);
	}

	/**
	 * Handles the case that a node refuses our request to connect.
	 * Please note that this method is called even if the connection is successful,
	 * since it is not possible to remove scheduled events from the simulator.
	 */
	private void onConnectTimeout(Node me, int pid, ScheduleConnectTimeoutEvent event) {

		// check if the node was already added... in this case do nothing!
		// else, this is really a timeout and we should go for the next node!
		final boolean present = containsOutgoing(event.peer);
		if (!present) {
			onCheckOutgoingConnections(me, pid);
		}
	}

	/**
	 * On a {@link VersionMessage} the node replies with a {@link VerAckMessage}
	 * to accept the incomingConnections connection, or does not reply to refuse it
	 * (eg. it hasBlock already the maximum number of allowed connections).
	 */
	private void onVersionMessage(Node me, int pid, VersionMessage event) {
		assert me != event.sender : "A node should not send messages to itself";

		// check if both nodes can create the connection
		final Node peer = event.sender;
		final BitcoinTopology peerTopology = (BitcoinTopology) peer.getProtocol(pid);
		assert peerTopology.canCreateOutgoingConnection(peer) : "Received a version message from a node that cannot open an outgoing connection.";

		// still space available... accept the connection
		// otherwise, simply do NOT send any messagges back... the sender hasBlock a timeout to handle this case
		if (this.canAcceptIncomingConnection(peer)) {

			// establish the link
			this.addIncomingConnection(peer);
			peerTopology.createOutgoingConnection(peer, pid, me);

			// reply with VerAck
			send(me, peer, pid, new VerAckMessage(me));
			verAckMessages++;

			// send my address to a random peer (see coinscope.pdf)
			final int randomIndex = CommonState.r.nextInt(degree());
			final Node neighbour = getConnection(randomIndex).neighbour;
			send(me, neighbour, pid, new AddrMessage(me));
		}
	}

	// -----------------------------------------------------------------------------------------------------
	//  Messages
	// -----------------------------------------------------------------------------------------------------

	/**
	 * On a {@link VerAckMessage} the node knows that the peer hasBlock accepted
	 * the connection. At this point, the nodes can begin to exchange messages.
	 * The node that opened the outgoing connection requests the new peer a
	 * list of knownList addresses of other peer in the network.
	 * To keep the connection alive, both nodes should send {@link PingMessage}s.
	 */
	private void onVerAckMessage(Node me, int pid, VerAckMessage event) {
		send(me, event.sender, pid, new GetAddrMessage(me));
		getAddrMessages++;
	}

	/**
	 * On a {@link GetAddrMessage} the peer should reply with its list of peers.
	 */
	private void onGetAddrMessage(GetAddrMessage event) {

		// the number of peers to send is randomly selected from min(23%, 2500)
		// addr messages are limited to 1000 addresses in reality...
		// here we do not implement this but rather increment the counter
		// to simulate the real number of messages

		// create the message
		// we need to shuffle the peers before sending them
		final Node[] peersToShuffle = peers.dump(new Node[0]);
		ArrayUtilities.shuffleInPlace(peersToShuffle);

		// select only a subset of peers to gossip
		final int peersToSendNumber = Math.min((int) Math.ceil(1.0 * peersToShuffle.length * 23 / 100), 2500);
		final Node[] peersToSend = new Node[peersToSendNumber];
		System.arraycopy(peersToShuffle, 0, peersToSend, 0, peersToSendNumber);

		// create and schedule the message
		final AddrMessage message = new AddrMessage(peersToSend);
		final Connection connection = getConnection(getIndexOfNeighbour(event.sender));
		connection.addrQueue.enqueue(message);

		// keep track of the nodes send to this peer... for the next 24 hour
		// they won't be forwarded to this node again, to avoid loop of messages
		final ObjectQueue<Node> knownList = connection.knownList;
		for (int i = 0; i < peersToSendNumber; i++) {
			knownList.add(peersToSend[i]);
		}
	}

	/**
	 * On a {@link AddrMessage} the peer receives a list of other peers present
	 * in the network. If needed, it can choose some nodes to connect to.
	 */
	private void onAddrMessage(Node me, int pid, AddrMessage receivedMessage) {

		// handle table of peers is full
		if (peers.size() >= maxPeerAddresses) {
			throw new RuntimeException("Got more peers that the maximum number (" + maxPeerAddresses + "). " +
				"This feature is not yet implemented since it was not used in small simulations.");
		}

		// addBlock the received nodes to my peers table
		final Node[] receivedPeers = receivedMessage.peers;
		for (Node peer : receivedPeers) {
			// never addBlock myself to the list of known peers
			if (peer != me) {
				peers.add(peer);
			}
		}

		// if the addr message contains few addresses (<= 10) we need to gossip them
		if (receivedPeers.length <= maxIpsAddrMessageForGossip) {

			// process each address in the message individually
			for (Node peer : receivedPeers) {

				// select up to 2 random peers to gossip the message to
				final int[] selectedPeersIndexes = ArrayUtilities.selectNRandomIntegers(nodesToGossipAddrMessages, degree());
				for (int i = 0; i < selectedPeersIndexes.length; i++) {

					// keep track of the sent addresses
					final Connection connection = getConnection(i);
					final boolean alreadySent = connection.knownList.add(peer);

					// enqueue the addr message
					if (!alreadySent) {
						final AddrMessage message = new AddrMessage(peer);
						connection.addrQueue.enqueue(message);
					}
				}
			}
		}

		// connect to new peers if the node hasBlock not reached the limit of outgoing connections
		onCheckOutgoingConnections(me, pid);
	}

	/**
	 * On a {@link PingMessage} the node immediately replies to the sender with a pong message.
	 */
	private void onPingMessage(Node node, int pid, PingMessage message) {
		send(node, message.sender, pid, new PongMessage(node));
		pongMessages++;
	}

	/**
	 * On a {@link PongMessage}: the node keeps track that the sender is alive.
	 */
	private void onPongMessage(PongMessage event) {
		final int index = getIndexOfNeighbour(event.sender);
		final Connection connection = getConnection(index);
		connection.lastPong = CommonState.getTime();
	}

	@Override
	public int degree() {
		return outgoingConnectionsNumber + incomingConnectionsNumber;
	}


	// -------------------------------------------------------------------------
	//  LinkableWithRemoval: keep track of outgoing and incoming connections
	// -------------------------------------------------------------------------

	public int degreeOutgoing() {
		return outgoingConnectionsNumber;
	}

	public int degreeIncoming() {
		return incomingConnectionsNumber;
	}

	public int peers() {
		return peers.size();
	}

	@Override
	public boolean contains(Node neighbour) {
		final int index = getIndexOfNeighbour(neighbour);
		return index >= 0;
	}

	/**
	 * Check if the current node as an outgoing connection with the given neighbour.
	 */
	private boolean containsOutgoing(Node neighbour) {
		for (int i = 0; i < outgoingConnectionsNumber; i++) {
			if (outgoingConnections[i].neighbour == neighbour) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the status of the connection with the given index.
	 */
	private Connection getConnection(int index) {
		assert index >= 0 : "Cannot request a connection with a negative index.";
		if (index < outgoingConnectionsNumber) {
			return outgoingConnections[index];
		} else {
			return incomingConnections[index - outgoingConnectionsNumber];
		}
	}

	/**
	 * Returns the index of the given neighbour it is it present, -1 otherwise.
	 */
	private int getIndexOfNeighbour(Node neighbour) {

		// search in the outgoing connections
		for (int i = 0; i < outgoingConnectionsNumber; i++) {
			if (outgoingConnections[i].neighbour == neighbour) {
				return i;
			}
		}

		// search in the incoming connections
		for (int i = 0; i < incomingConnectionsNumber; i++) {
			if (incomingConnections[i].neighbour == neighbour) {
				return outgoingConnectionsNumber + i;
			}
		}

		// if here, the node was not found...
		return -1;
	}

	@Override
	public Node getNeighbor(int i) {
		return getConnection(i).neighbour;
	}

	/**
	 * Verify if a node can create an outgoing connection to the given neighbour.
	 * Returns true if the node is able to create an outgoing connection, false otherwise.
	 * Please call this method before trying to call {@link #addOutgoingConnection(Node)}}.
	 */
	private boolean canCreateOutgoingConnection(Node neighbour) {

		// enforce the maximum number of connection allowed
		if (outgoingConnectionsNumber >= maxOutgoingConnections) {
			return false;
		}

		// do not allow duplicates (2 nodes can only have a single connection between each other)
		final boolean alreadyPresent = contains(neighbour);
		if (alreadyPresent) {
			return false;
		}

		// all checks passed
		return true;
	}

	/**
	 * Verify if a node can accept an incoming connection from the given neighbour.
	 * Returns true if the node is able to create an outgoing connection, false otherwise.
	 * Please call this method before trying to call {@link #addIncomingConnection(Node)}}.
	 */
	private boolean canAcceptIncomingConnection(Node neighbour) {

		// enforce the maximum number of connection allowed
		if (incomingConnectionsNumber >= maxIncomingConnections) {
			return false;
		}

		// do not allow duplicates (2 nodes can only have a single connection between each other)
		final boolean alreadyPresent = contains(neighbour);
		if (alreadyPresent) {
			return false;
		}

		// all checks passed
		return true;
	}

	private void createOutgoingConnection(Node me, int pid, Node neighbour) {

		// addBlock the node
		addOutgoingConnection(neighbour);

		// change the node state, since the connection hasBlock been established
		connecting = false;

		// schedule the next connection attempt
		onCheckOutgoingConnections(me, pid);
	}

	/**
	 * Add a node to the list of outgoing connections for this node.
	 * This method will make sure there are no duplicates!
	 * Returns true if the node is added, false if is already present and is not added.
	 */
	@SuppressWarnings("Duplicates")
	private void addOutgoingConnection(Node neighbour) {
		assert connecting : "Some node invoked #addOutgoingConnection on a node which was not connecting to anybody...";
		assert canCreateOutgoingConnection(neighbour) : "The node cannot create the outgoing connection.";

		// addBlock the connection
		outgoingConnections[outgoingConnectionsNumber] = new Connection(neighbour);
		outgoingConnectionsNumber++;
	}

	/**
	 * Add a node to the list of incoming connections for this node.
	 * This method will make sure there are no duplicates!
	 * Returns true if the node is added, false if is already present and is not added.
	 */
	@SuppressWarnings("Duplicates")
	private void addIncomingConnection(Node neighbour) {
		assert canAcceptIncomingConnection(neighbour) : "The node cannot accept the incoming connection.";

		// addBlock the peer
		incomingConnections[incomingConnectionsNumber] = new Connection(neighbour);
		incomingConnectionsNumber++;
	}

	/**
	 * NB: this method will NOT addBlock the nodes to the list of connected peers, but just to the list
	 * of knownList peers of the given node. This can be useful to initialize the protocol using classes
	 * already available in Peersim, such as {@link peersim.dynamics.WireKOut} and similar.
	 *
	 * @param neighbour Node to addBlock to the knownList peers.
	 * @return True if the peer was added, false if the peer was already present and cannot be added.
	 */
	@Override
	public boolean addNeighbor(Node neighbour) {
		return peers.add(neighbour);
	}

	/**
	 * Remove the given neighbour from the list of connected nodes.
	 */
	private void removeNeighbor(Node neighbour) {
		final int nodeIndex = getIndexOfNeighbour(neighbour);
		assert nodeIndex >= 0;
		removeNeighbor(nodeIndex);
	}

	/**
	 * Remove the given neighbour with the given index from the list of connected nodes.
	 */
	@SuppressWarnings("Duplicates")
	private void removeNeighbor(int index) {
		if (index < outgoingConnectionsNumber) {
			final int elementsToCopy = outgoingConnectionsNumber - index - 1;
			System.arraycopy(outgoingConnections, index + 1, outgoingConnections, index, elementsToCopy);
			final int indexToRemove = outgoingConnectionsNumber - 1;
			outgoingConnections[indexToRemove] = null;
			outgoingConnectionsNumber--;
		} else {
			final int elementsToCopy = incomingConnectionsNumber - index - 1;
			System.arraycopy(incomingConnections, index + 1, incomingConnections, index, elementsToCopy);
			final int indexToRemove = incomingConnectionsNumber - 1;
			incomingConnections[indexToRemove] = null;
			incomingConnectionsNumber--;
		}
	}

	@Override
	public void pack() {
		// no-op: nothing to compact here!
	}

	/**
	 * Send a message to all neighbors, both the incoming and outgoing ones.
	 */
	private void multicast(Node from, int pid, Object message) {
		final Transport transport = (Transport) from.getProtocol(FastConfig.getTransport(pid));
		for (int i = 0; i < outgoingConnectionsNumber; i++) {
			final Node to = outgoingConnections[i].neighbour;
			transport.send(from, to, message, pid);
			assert from != to;
		}
		for (int i = 0; i < incomingConnectionsNumber; i++) {
			final Node to = incomingConnections[i].neighbour;
			transport.send(from, to, message, pid);
			assert from != to;
		}
	}


	// -----------------------------------------------------------------------------------------------------
	//  Utilities
	// -----------------------------------------------------------------------------------------------------

	/**
	 * Structure used to keep track of all information about a single connection
	 * with a peer, such as the last pong time or the queue of pending addr messages.
	 */
	private static class Connection implements Cloneable {

		Node neighbour;
		CircularQueue<AddrMessage> addrQueue;
		ObjectQueue<Node> knownList;
		long lastPong;

		Connection(Node neighbour) {
			this.neighbour = neighbour;
			this.addrQueue = new CircularQueue<>();
			this.knownList = new ObjectQueue<>();
			this.lastPong = CommonState.getTime();
		}

		@Override
		protected Connection clone() throws CloneNotSupportedException {
			final Connection clone = (Connection) super.clone();
			clone.addrQueue = new CircularQueue<>(this.addrQueue);
			clone.knownList = new ObjectQueue<>(this.knownList);
			return clone;
		}
	}
}
