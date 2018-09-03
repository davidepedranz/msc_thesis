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

package simulator.collections;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public final class ObjectListTest {

    private static final Object ELEMENT_0 = new Object();
    private static final Object ELEMENT_1 = new Object();
    private static final Object ELEMENT_2 = new Object();
    private static final Object ELEMENT_3 = new Object();
    private static final Object ELEMENT_4 = new Object();

    private final ObjectList<Object> queue;

    public ObjectListTest() {
        this.queue = new ObjectList<>(1);
    }

    @Test
    public void testEmptyList() {
        assertEquals(0, queue.size());
        assertEquals(1, queue._arraySize());
    }

    @Test
    public void testAddOneElement() {
        assertTrue(queue.add(ELEMENT_0));
        assertEquals(1, queue.size());

        assertFalse(queue.add(ELEMENT_0));
        assertEquals(1, queue.size());

        assertEquals(1, queue._arraySize());
        assertSame(ELEMENT_0, queue.random(new Random()));
    }

    @Test
    public void testAddTwoElements() {
        assertTrue(queue.add(ELEMENT_0));
        assertTrue(queue.add(ELEMENT_1));
        assertEquals(2, queue.size());

        assertFalse(queue.add(ELEMENT_0));
        assertFalse(queue.add(ELEMENT_1));
        assertEquals(2, queue.size());

        assertEquals(2, queue._arraySize());
    }

    @Test
    public void testAddThreeElements() {
        assertTrue(queue.add(ELEMENT_0));
        assertTrue(queue.add(ELEMENT_1));
        assertTrue(queue.add(ELEMENT_2));
        assertEquals(3, queue.size());

        assertFalse(queue.add(ELEMENT_0));
        assertFalse(queue.add(ELEMENT_1));
        assertFalse(queue.add(ELEMENT_2));
        assertEquals(3, queue.size());

        assertEquals(4, queue._arraySize());
    }

    @Test
    public void testAddFourElements() {
        assertTrue(queue.add(ELEMENT_0));
        assertTrue(queue.add(ELEMENT_1));
        assertTrue(queue.add(ELEMENT_2));
        assertTrue(queue.add(ELEMENT_3));
        assertEquals(4, queue.size());

        assertFalse(queue.add(ELEMENT_0));
        assertFalse(queue.add(ELEMENT_1));
        assertFalse(queue.add(ELEMENT_2));
        assertFalse(queue.add(ELEMENT_3));
        assertEquals(4, queue.size());

        assertEquals(4, queue._arraySize());
    }

    @Test
    public void testAddFiveElements() {
        assertTrue(queue.add(ELEMENT_0));
        assertTrue(queue.add(ELEMENT_1));
        assertTrue(queue.add(ELEMENT_2));
        assertTrue(queue.add(ELEMENT_3));
        assertTrue(queue.add(ELEMENT_4));
        assertEquals(5, queue.size());

        assertFalse(queue.add(ELEMENT_0));
        assertFalse(queue.add(ELEMENT_1));
        assertFalse(queue.add(ELEMENT_2));
        assertFalse(queue.add(ELEMENT_3));
        assertFalse(queue.add(ELEMENT_4));
        assertEquals(5, queue.size());

        assertEquals(8, queue._arraySize());
    }

    @Test
    public void testDump() {
        testAddFiveElements();

        final Object[] dump = queue.dump(new Object[0]);
        assertEquals(5, dump.length);
        assertSame(ELEMENT_0, dump[0]);
        assertSame(ELEMENT_1, dump[1]);
        assertSame(ELEMENT_2, dump[2]);
        assertSame(ELEMENT_3, dump[3]);
        assertSame(ELEMENT_4, dump[4]);
    }

    @Test
    public void testClear() {
        testAddThreeElements();

        queue.clear();
        assertEquals(0, queue.size());
        assertEquals(1, queue._arraySize());

        testAddThreeElements();
    }

    @Test
    public void testCopyConstructor() {
        testAddThreeElements();

        final ObjectList<Object> copy = new ObjectList<>(queue);
        assertEquals(3, copy.size());
        assertEquals(4, copy._arraySize());

        assertFalse(copy.add(ELEMENT_0));
        assertFalse(copy.add(ELEMENT_1));
        assertFalse(copy.add(ELEMENT_2));
        assertTrue(copy.add(ELEMENT_3));
        assertTrue(copy.add(ELEMENT_4));
        assertFalse(copy.add(ELEMENT_3));
        assertFalse(copy.add(ELEMENT_4));
        assertEquals(5, copy.size());
        assertEquals(8, copy._arraySize());
    }
}
