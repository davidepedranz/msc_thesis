package simulator.protocols.bitcoin.transactions.messages;

/**
 * Specify the type of payload contained in a Bitcoin message.
 * See {@link InvMessage}, {@link GetDataMessage}.
 */
public enum Type {
	BLOCK,
	TRANSACTION
}
