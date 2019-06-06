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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AtomicHashStoreEqualsHashCodeTest {

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
        st = st.put("one", "ONE");
        check(st);
        st = st.put("one", "ONE");
        check(st);
        st = st.put( new String("one"), "ONE"); // Different String with same value checked on purpose
        check(st);
        Assert.assertEquals(1, st.size());
        st = st.put("two", "ANOTHER VALUE");
        check(st);
        st = st.put("three", "A THIRD ONE");
        check(st);
        Assert.assertEquals(3, st.size());
        st = st.put("one", "ONE");
        check(st);
        st = st.put("one", "ONE");
        check(st);
        Assert.assertEquals(3, st.size());
        st = st.put("pOe", "ONE COLLISION");
        check(st);
        Assert.assertEquals(4, st.size());
        st = st.put("q0e", "ANOTHER COLLISION");
        check(st);
        Assert.assertEquals(5, st.size());
        st = st.put("pOe", "ONE COLLISION");
        check(st);
        Assert.assertEquals(5, st.size());
        st = st.put("pOe", "ONE COLLISION, BUT NEW ENTRY");
        check(st);
        Assert.assertEquals(5, st.size());
        st = st.put(new String("q0e"), "ANOTHER COLLISION");
        check(st);
        Assert.assertEquals(5, st.size());

    }


    @Test
    public void test01() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assert.assertEquals(0, st.size());
        st = st.put("one", "ONE");
        check(st);
        Assert.assertEquals(1, st.size());

        st = st.put("one", "ONE");
        check(st);
        Assert.assertEquals(1, st.size());

    }


    @Test
    public void test02() throws Exception {

        AtomicHashStore<String,String> st = this.store;
        check(st);

        Assert.assertEquals(0, st.size());
        st = st.put(null, null);
        check(st);
        Assert.assertEquals(1, st.size());

    }


    @Test
    public void test03() throws Exception {

        final KeyValue<String,String>[] entries =
                TestUtils.generateStringStringKeyValues(10000, 30, 100);

        AtomicHashStore<String,String> st = this.store;

        for (int i = 0; i < entries.length; i++) {
            st = st.put(entries[i].getKey(), entries[i].getValue());
        }

        check(st);

    }




    private static void check(final AtomicHashStore<String,String> store) {

        final List<Map.Entry<String,String>> entries = new ArrayList<>(store.entrySet());

        final int[] positions = new int[entries.size()];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = i;
        }

        TestUtils.randomizeArray(positions);

        AtomicHashStore<String,String> store2 = new AtomicHashStore<>();
        for (int i = 0; i < positions.length; i++) {
            final Map.Entry<String,String> entry = entries.get(positions[i]);
            store2 = store2.put(entry.getKey(), entry.getValue());
        }

        Assert.assertTrue(store.equals(store2));

        if (entries.size() > 0) {

            final int rand = RandomUtils.nextInt(0, entries.size());
            AtomicHashStore<String,String> store3 = store2.remove(entries.get(rand).getKey());

            Assert.assertFalse(store.equals(store3));

        }

        final String key = TestUtils.generateKey();
        final String value = TestUtils.generateValue();

        AtomicHashStore<String,String> store3 = store2.put(key, value);

        Assert.assertFalse(store.equals(store3));


    }


}
