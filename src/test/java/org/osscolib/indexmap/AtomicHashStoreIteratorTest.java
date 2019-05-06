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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.osscolib.indexmap.testutil.KeyValue;
import org.osscolib.indexmap.testutil.TestUtils;

public class AtomicHashStoreIteratorTest {


    @Test
    public void testEntryIterator() throws Exception {
        testIterator(1);
        testIterator(2);
        testIterator(3);
        testIterator(5);
        testIterator(8);
        testIterator(16);
        testIterator(32);
        testIterator(64);
        testIterator(100000);
        testIterator(1000000);
    }


    private void testIterator(final int size) {
        testIterator(size, IndexMap.<String, String>build().withSmallSize().asFluentMap(), 2);
        testIterator(size, IndexMap.<String, String>build().withMediumSize().asFluentMap(), 4);
        testIterator(size, IndexMap.<String, String>build().withLargeSize().asFluentMap(), 8);
    }


    private void testIterator(final int size, final AtomicHashStore<String,String> st, final int maskSize) {

        AtomicHashStore<String,String> store = st;

        final KeyValue<String,String>[] entries = TestUtils.generateStringStringKeyValues(size);

        for (int i = 0; i < entries.length; i++) {
            store = store.put(entries[i].getKey(), entries[i].getValue());
        }

        final List<KeyValue<String,String>> expected = new ArrayList<>(Arrays.asList(entries));
        expected.sort(new HashComparator(maskSize));

        final List<KeyValue<String,String>> obtained = new ArrayList<>();
        for (final Map.Entry<String,String> entry : store) {
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
