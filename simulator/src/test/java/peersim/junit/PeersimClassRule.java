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

package peersim.junit;

import org.junit.rules.ExternalResource;
import peersim.config.Configuration;

import java.util.Properties;

/**
 * JUnit {@link org.junit.Rule} that bootstraps PeerSim for testing.
 * This should be used as a {@link org.junit.ClassRule}.
 */
public final class PeersimClassRule extends ExternalResource {

	@SuppressWarnings("WeakerAccess")
	public static final long DEFAULT_SEED = 0;

	// make sure peersim is initialized only once for test suite
	private static boolean initialized = false;

	private final long seed;

	public PeersimClassRule() {
		this.seed = DEFAULT_SEED;
	}

	@SuppressWarnings("unused")
	public PeersimClassRule(long seed) {
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
