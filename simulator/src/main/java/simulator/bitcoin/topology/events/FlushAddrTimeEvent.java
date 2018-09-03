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

package simulator.bitcoin.topology.events;

/**
 * Event that represents a time window where to flush the `Addr` messages
 * to the randomly selected peer.
 */
public final class FlushAddrTimeEvent {

    // singleton instance -> spare memory
    public static final FlushAddrTimeEvent INSTANCE = new FlushAddrTimeEvent();

    // prevent class construction from outside and force to use the singleton
    private FlushAddrTimeEvent() {
    }
}
