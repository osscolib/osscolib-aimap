/*
 * =============================================================================
 *
 *   Copyright (c) 2019, The OSSCOLIB team (http://www.osscolib.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.osscolib.indexmap;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BaseAtomicHashStoreTest {

    private AtomicHashStore<String,String> map;


    @Before
    public void initMap() {
        this.map = IndexMap.<String,String>build().asFluentMap();
    }


    @Test
    public void test00() throws Exception {

        AtomicHashStore<String,String> m = this.map;

        Assert.assertEquals(0, m.size());
        Assert.assertNull(m.get(null));

        m = add(m, "one", "ONE");
        m = add(m, "one", "ONE");
        m = add(m, new String("one"), "ONE"); // Different String with same value checked on purpose

        Assert.assertEquals(1, m.size());

        m = add(m, "two", "ANOTHER VALUE");
        m = add(m, "three", "A THIRD ONE");

        Assert.assertEquals(3, m.size());

        m = add(m, "one", "ONE");
        m = add(m, "one", "ONE");

        Assert.assertEquals(3, m.size());

        m = add(m, "pOe", "ONE COLLISION");

        Assert.assertEquals(4, m.size());

        m = add(m, "q0e", "ANOTHER COLLISION");

        Assert.assertEquals(5, m.size());
        
    }


    private static <K,V> AtomicHashStore<K,V> add(final AtomicHashStore<K,V> store, final K key, final V value) {

        AtomicHashStore<K,V> store2, store3;

        final boolean oldContainsKey = store.containsKey(key);
        final V oldValue = store.get(key);
        final int oldSize = store.size();

        if (!oldContainsKey) {
            Assert.assertNull(oldValue);
        }

        store2 = store.put(key, value);

        if (oldContainsKey) {
            Assert.assertEquals(oldSize, store2.size());
            if (existsEntryByReference(store, key, value)) {
                Assert.assertSame(store, store2);
                Assert.assertSame(oldValue, store2.get(key));
            } else {
                Assert.assertNotSame(store, store2);
            }
        }

        Assert.assertEquals(oldContainsKey, store.containsKey(key));
        Assert.assertEquals(oldSize, store.size());
        Assert.assertSame(oldValue, store.get(key));

        final boolean newContainsKey = store2.containsKey(key);
        final V newValue = store2.get(key);
        final int newSize = store2.size();

        Assert.assertTrue(newContainsKey);
        Assert.assertEquals((oldContainsKey) ? oldSize : (oldSize + 1), newSize);
        Assert.assertSame(value, newValue);


        store3 = store2.remove(key);

        Assert.assertTrue(store2.containsKey(key));
        Assert.assertEquals(newSize, store2.size());
        Assert.assertSame(newValue, store2.get(key));

        Assert.assertFalse(store3.containsKey(key));
        Assert.assertEquals((oldContainsKey)? (oldSize - 1) : oldSize, store3.size());
        Assert.assertNull(store3.get(key));


        return store2;

    }


    private static <K,V> boolean existsEntryByReference(final AtomicHashStore<K,V> store, final K key, final V value) {
        for (final Map.Entry<K,V> entry : store) {
            if (key == entry.getKey() && value == entry.getValue()) {
                return true;
            }
        }
        return false;
    }

}
