package simulator.junit;

import org.junit.rules.ExternalResource;
import simulator.bitcoin.core.model.Blocks;

/**
 * JUnit {@link org.junit.Rule} that bootstraps Peersim for testing.
 * This should be used as a @{@link org.junit.Rule}.
 */
public final class PeersimRule extends ExternalResource {

	@Override
	protected void before() {
		Blocks._init();
	}

	@Override
	protected void after() {
	}
}
