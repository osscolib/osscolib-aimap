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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class AtomicHashStoreEntrySetTest {


    @Test
    public void testEntrySet() throws Exception {
        testEntrySet(1);
        testEntrySet(2);
        testEntrySet(3);
        testEntrySet(5);
        testEntrySet(8);
        testEntrySet(16);
        testEntrySet(32);
        testEntrySet(64);
        testEntrySet(10000);
    }


    private void testEntrySet(final int size) {

        AtomicHashStore<String,String> st = AtomicHash.<String,String>build().asFluentMap();
        final int maskSize = 4;

        final KeyValue<String,String>[] kvs = TestUtils.generateStringStringKeyValues(size);

        for (int i = 0; i < kvs.length; i++) {
            st = st.put(kvs[i].getKey(), kvs[i].getValue());
        }

        final Set<Map.Entry<String,String>> entrySet = st.entrySet();
        Assert.assertEquals(kvs.length, entrySet.size());

        for (int i = 0; i < kvs.length; i++) {
            Assert.assertTrue(entrySet.contains(new Entry(kvs[i].getKey(), kvs[i].getValue())));
        }


        final int oldSize = entrySet.size();
        st = st.put(null, "some null");
        // The entrySet of a Store is not affected by modifications on that store (because it is immutable). Note this
        // is the contrary of what should happen with a Map
        Assert.assertEquals(oldSize, entrySet.size());
        st = st.remove(null);
        Assert.assertEquals(oldSize, entrySet.size());

        testIterator(kvs, entrySet, maskSize);
    }



    private void testIterator(KeyValue<String,String>[] entries, final Set<Map.Entry<String,String>> entrySet, final int maskSize) {

        final List<KeyValue<String,String>> expected = new ArrayList<>(Arrays.asList(entries));
        expected.sort(new HashComparator(maskSize));

        final List<KeyValue<String,String>> obtained = new ArrayList<>();
        for (final Map.Entry<String,String> entry : entrySet) {
            obtained.add(new KeyValue<>(entry.getKey(), entry.getValue()));
        }

        Assert.assertEquals(expected, obtained);

    }




    private static final class HashComparator implements Comparator<KeyValue<String,String>> {

        private final int maskSize;
        private HashComparator(final int maskSize) {
            super();
            this.maskSize = maskSize;
        }

        @Override
        public int compare(final KeyValue<String, String> o1, final KeyValue<String, String> o2) {

            final int h1 = AtomicHashStore.hash(o1.getKey());
            final int h2 = AtomicHashStore.hash(o2.getKey());

            if (h1 == h2) {
                return 0;
            }

            final int mask = (1 << maskSize) - 1;

            int level = 0;
            while (true) {
                final int s1 = (h1 >>> (level * maskSize)) & mask;
                final int s2 = (h2 >>> (level * maskSize)) & mask;
                final int comp = Integer.compare(s1, s2);
                if (comp != 0) {
                    return comp;
                }
                level++;
            }

        }

    }

}
