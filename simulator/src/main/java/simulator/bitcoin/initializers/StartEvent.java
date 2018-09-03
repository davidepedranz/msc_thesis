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

package simulator.bitcoin.initializers;

/**
 * Event used to signal a protocol to start,
 * eg. {@link simulator.bitcoin.initializers.ProtocolInitializer}.
 */
public final class StartEvent {

    // singleton instance -> spare memory
    static final StartEvent INSTANCE = new StartEvent();

    // prevent class construction from outside and force to use the singleton
    private StartEvent() {
    }
}
