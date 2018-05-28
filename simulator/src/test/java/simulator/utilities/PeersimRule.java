package simulator.utilities;

import org.junit.rules.ExternalResource;
import simulator.model.Block;

/**
 * JUnit {@link org.junit.Rule} that bootstraps Peersim for testing.
 * This should be used as a @{@link org.junit.Rule}.
 */
public final class PeersimRule extends ExternalResource {

	@Override
	protected void before() {
		Block._init();
	}

	@Override
	protected void after() {
	}
}
