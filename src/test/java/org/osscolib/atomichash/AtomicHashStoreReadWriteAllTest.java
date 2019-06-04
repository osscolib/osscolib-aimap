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
package org.osscolib.atomichash;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AtomicHashStoreReadWriteAllTest {

    private AtomicHashStore<String,String> store;


    @Before
    public void initStore() {
        this.store = new AtomicHashStore<>();
    }


    @Test
    public void test00() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assert.assertEquals(0, st.size());
        Assert.assertNull(st.get(null));
        st = addAll(st, "one", "ONE");
        st = addAll(st, "one", "ONE");
        st = addAll(st, new String("one"), "ONE"); // Different String with same value checked on purpose
        Assert.assertEquals(1, st.size());
        st = addAll(st, "two", "ANOTHER VALUE");
        st = addAll(st, "three", "A THIRD ONE");
        Assert.assertEquals(3, st.size());
        st = addAll(st, "one", "ONE");
        st = addAll(st, "one", "ONE");
        Assert.assertEquals(3, st.size());
        st = addAll(st, "pOe", "ONE COLLISION");
        Assert.assertEquals(4, st.size());
        st = addAll(st, "q0e", "ANOTHER COLLISION");
        Assert.assertEquals(5, st.size());
        st = addAll(st, "pOe", "ONE COLLISION");
        Assert.assertEquals(5, st.size());
        st = addAll(st, "pOe", "ONE COLLISION, BUT NEW ENTRY");
        Assert.assertEquals(5, st.size());
        st = addAll(st, new String("q0e"), "ANOTHER COLLISION");
        Assert.assertEquals(5, st.size());

    }


    @Test
    public void test01() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assert.assertEquals(0, st.size());
        Assert.assertNull(st.get(null));
        st = addAll(st, "one", "ONE", "one", "ONE", "two", "ANOTHER VALUE", "three", "A THIRD ONE");
        Assert.assertEquals(3, st.size());
        st = addAll(st, "pOe", "ONE COLLISION", "q0e", "ANOTHER COLLISION");
        Assert.assertEquals(5, st.size());
        st = addAll(st, "pOe", "ONE COLLISION, BUT NEW ENTRY", new String("q0e"), "ANOTHER COLLISION");
        Assert.assertEquals(5, st.size());
        st = addAll(st, "three", "SOMETHING ELSE", "q0e", "ANOTHER COLLISION");
        Assert.assertEquals(5, st.size());
        st = addAll(st, "q0e", "ANOTHER COLLISION", "four", "SOMETHING ELSE 4");
        Assert.assertEquals(6, st.size());

    }


    @Test
    public void test02() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assert.assertEquals(0, st.size());
        st = addAll(st, null, null, "one", "ONE", "two", "TWO");
        Assert.assertEquals(3, st.size());

    }


    @Test
    public void test03() throws Exception {

        final KeyValue<String,String>[] entries =
                TestUtils.generateStringStringKeyValues(10000, 30, 100);

        final Map<String,String> entriesMap = new HashMap<>();
        for (int i = 0; i < entries.length; i++) {
            entriesMap.put(entries[i].getKey(), entries[i].getValue());
        }

        AtomicHashStore<String,String> st = this.store;

        st = addAll(st, entriesMap);

        final int[] accesses = TestUtils.generateInts(1000000, 0, entries.length);

        int pos;
        for (int i = 0; i < accesses.length; i++) {
            pos = accesses[i];
            Assert.assertTrue(st.containsKey(entries[pos].getKey()));
            Assert.assertEquals(entriesMap.get(entries[pos].getKey()), st.get(entries[pos].getKey()));
        }

    }




    private static <K,V> AtomicHashStore<K,V> addAll(final AtomicHashStore<K,V> store, final Map<K,V> map) {

        AtomicHashStore<K,V> store2, store3;

        final int oldSize = store.size();
        final Map<K,TestStatus<V>> oldStatus = createTestStatusMap(map, store);

        oldStatus.forEach((k,st) -> {
            if (!st.containsKey) {
                Assert.assertNull(st.value);
            }
        });

        final int oldContainedKeysCount = containsHowManyKeys(map, store);

        final String snap11 = PrettyPrinter.prettyPrint(store);

        store2 = store.putAll(map);

        final String snap12 = PrettyPrinter.prettyPrint(store);
        Assert.assertEquals(snap11, snap12);

        TestUtils.validateStoreWellFormed(store2);

        Assert.assertEquals(oldContainedKeysCount, containsHowManyKeys(map, store));
        Assert.assertEquals(map.size(), containsHowManyKeys(map, store2));

        store3 = store;
        final String snap31 = PrettyPrinter.prettyPrint(store2);

        for (final Map.Entry<K,V> entry : map.entrySet()) {
            store3 = store3.put(entry.getKey(), entry.getValue());
        }

        final String snap32 = PrettyPrinter.prettyPrint(store3);
        Assert.assertEquals(snap31, snap32); // Result of putAll is the same as many put


        final Map<K,TestStatus<V>> newStatus = createTestStatusMap(map, store2);

        if (existAllEntriesByReference(store, map)) {
            Assert.assertSame(store, store2);
        } else {
            Assert.assertNotSame(store, store2);
        }

        oldStatus.forEach((ok,ost) -> {
            if (ost.containsKey) {
                final TestStatus<V> nst = newStatus.get(ok);
                if (existsEntryByReference(store, ok, map.get(ok))) {
                    Assert.assertSame(ost.value, nst.value);
                }
            }
        });

        oldStatus.forEach((ok,ost) -> {
            Assert.assertEquals(ost.containsKey, store.containsKey(ok));
            Assert.assertEquals(ost.containsValue, store.containsValue(map.get(ok)));
            Assert.assertSame(ost.value, store.get(ok));
        });
        Assert.assertEquals(oldSize, store.size());

        newStatus.forEach((nk,nst) -> {
            final TestStatus<V> ost = oldStatus.get(nk);
            Assert.assertTrue(nst.containsKey);
            Assert.assertTrue(nst.containsValue);
            Assert.assertSame(nst.value, map.get(nk));
        });

        Assert.assertEquals(store.size() + (map.size() - oldContainedKeysCount), store2.size());

        final String snap21 = PrettyPrinter.prettyPrint(store2);

        store3 = store2;
        for (final Map.Entry<K,V> entry : map.entrySet()) {
            store3 = store3.remove(entry.getKey());
        }

        final String snap22 = PrettyPrinter.prettyPrint(store2);
        Assert.assertEquals(snap21, snap22);

        TestUtils.validateStoreWellFormed(store3);

        newStatus.forEach((nk,nst) -> {
            final TestStatus<V> ost = oldStatus.get(nk);
            Assert.assertTrue(nst.containsKey);
            Assert.assertTrue(nst.containsValue);
            Assert.assertSame(nst.value, map.get(nk));
        });

        Assert.assertEquals(store.size() + (map.size() - oldContainedKeysCount), store2.size());
        Assert.assertEquals(store.size() - oldContainedKeysCount, store3.size());


        final Map<K,TestStatus<V>> thirdStatus = createTestStatusMap(map, store3);

        thirdStatus.forEach((tk,tst) -> {
            Assert.assertFalse(tst.containsKey);
            Assert.assertNull(tst.value);
        });

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

    private static <K,V> boolean existAllEntriesByReference(final AtomicHashStore<K,V> store, final Map<K,V> map) {
        for (final Map.Entry<K,V> entry : map.entrySet()) {
            if (!existsEntryByReference(store, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }




    private static <K,V> AtomicHashStore<K,V> addAll(final AtomicHashStore<K,V> store,
                                                     final K k0, V v0) {
        final Map<K,V> map = new HashMap<>();
        map.put(k0, v0);
        return addAll(store, map);
    }
    private static <K,V> AtomicHashStore<K,V> addAll(final AtomicHashStore<K,V> store,
                                                     final K k0, V v0, final K k1, V v1) {
        final Map<K,V> map = new HashMap<>();
        map.put(k0, v0); map.put(k1, v1);
        return addAll(store, map);
    }
    private static <K,V> AtomicHashStore<K,V> addAll(final AtomicHashStore<K,V> store,
                                                     final K k0, V v0, final K k1, V v1, final K k2, V v2) {
        final Map<K,V> map = new HashMap<>();
        map.put(k0, v0); map.put(k1, v1); map.put(k2, v2);
        return addAll(store, map);
    }
    private static <K,V> AtomicHashStore<K,V> addAll(final AtomicHashStore<K,V> store,
                                                     final K k0, V v0, final K k1, V v1, final K k2, V v2,
                                                     final K k3, V v3) {
        final Map<K,V> map = new HashMap<>();
        map.put(k0, v0); map.put(k1, v1); map.put(k2, v2);
        map.put(k3, v3);
        return addAll(store, map);
    }






    private static <K,V> int containsHowManyKeys(final Map<K,V> map, final AtomicHashStore<K,V> store) {
        int c = 0;
        for (final K key : map.keySet()) {
            if (store.containsKey(key)) {
                c++;
            }
        }
        return c;
    }


    private static <K,V> Map<K,TestStatus<V>> createTestStatusMap(final Map<K,V> map, final AtomicHashStore<K,V> store) {
        final Map<K,TestStatus<V>> statusMap = new HashMap<>();
        for (final Map.Entry<K,V> entry : map.entrySet()) {
            final K key = entry.getKey();
            final V value = entry.getValue();
            statusMap.put(
                    key,
                    new TestStatus(
                        store.containsKey(key),
                        store.containsValue(value),
                        store.get(key)));
        }
        return statusMap;
    }

    private static class TestStatus<V> {
        final boolean containsKey;
        final boolean containsValue;
        final V value;

        public TestStatus(final boolean containsKey, final boolean containsValue, final V value) {
            this.containsKey = containsKey;
            this.containsValue = containsValue;
            this.value = value;
        }

    }

}
