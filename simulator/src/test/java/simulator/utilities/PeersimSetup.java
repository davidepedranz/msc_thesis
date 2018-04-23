package simulator.utilities;

import org.junit.rules.ExternalResource;
import peersim.config.Configuration;
import peersim.core.CommonState;

import java.util.Properties;

/**
 * JUnit {@link org.junit.Rule} that bootstraps Peersim for testing.
 */
public final class PeersimSetup extends ExternalResource {

	@SuppressWarnings("WeakerAccess")
	public static final long DEFAULT_SEED = 0;

	private final long seed;

	public PeersimSetup() {
		this.seed = DEFAULT_SEED;
	}

	@SuppressWarnings("unused")
	public PeersimSetup(long seed) {
		this.seed = seed;
	}

	@Override
	protected void before() {
		final Properties properties = new Properties();
		properties.setProperty("random.seed", Long.toString(seed));
		Configuration.setConfig(properties);
	}

	@Override
	protected void after() {
		CommonState.r = null;
	}
}
