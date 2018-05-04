package simulator.utilities;

import org.junit.rules.ExternalResource;
import peersim.config.Configuration;

import java.util.Properties;

/**
 * JUnit {@link org.junit.Rule} that bootstraps Peersim for testing.
 */
public final class PeersimSetup extends ExternalResource {

	@SuppressWarnings("WeakerAccess")
	public static final long DEFAULT_SEED = 0;

	// make sure peersim is initialized only once for test suite
	private static boolean initialized = false;

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
		if (!initialized) {
			initialized = true;
			final Properties properties = new Properties();
			properties.setProperty("random.seed", Long.toString(seed));
			Configuration.setConfig(properties);
		}
	}

	@Override
	protected void after() {
	}
}
