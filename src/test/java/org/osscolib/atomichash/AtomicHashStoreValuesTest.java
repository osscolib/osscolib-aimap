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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class AtomicHashStoreValuesTest {


    @Test
    public void testValues() throws Exception {
        testValues(1);
        testValues(2);
        testValues(3);
        testValues(5);
        testValues(8);
        testValues(16);
        testValues(32);
        testValues(64);
        testValues(10000);
    }


    private void testValues(final int size) {

        AtomicHashStore<String,String> st = AtomicHash.<String,String>build().asFluentMap();
        final int maskSize = 4;

        final KeyValue<String,String>[] kvs = TestUtils.generateStringStringKeyValues(size, 20, 0);

        for (int i = 0; i < kvs.length; i++) {
            st = st.put(kvs[i].getKey(), kvs[i].getValue());
        }

        final Collection<String> values = st.values();

        for (int i = 0; i < kvs.length; i++) {
            Assert.assertTrue(values.contains(kvs[i].getValue()));
        }


        final int oldSize = values.size();
        st = st.put(null, "some null");
        // The values of a Store are not affected by modifications on that store (because it is immutable). Note this
        // is the contrary of what should happen with a Map
        Assert.assertEquals(oldSize, values.size());
        st = st.remove(null);
        Assert.assertEquals(oldSize, values.size());

        testIterator(kvs, values, maskSize);
    }



    private void testIterator(KeyValue<String,String>[] entries, final Collection<String> values, final int maskSize) {

        final List<KeyValue<String,String>> expectedEntries = new ArrayList<>(Arrays.asList(entries));
        expectedEntries.sort(new HashComparator(maskSize));

        final List<String> expectedValues = new ArrayList<>();
        for (final KeyValue<String,String> expectedEntry : expectedEntries) {
            expectedValues.add(expectedEntry.getValue());
        }

        final List<String> obtainedValues = new ArrayList<>();
        for (final String value : values) {
            obtainedValues.add(value);
        }

        Assert.assertEquals(expectedValues, obtainedValues);

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
