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
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

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

        AtomicHashStore<String,String> store = new AtomicHashStore<>();

        final KeyValue<String,String>[] entries = TestUtils.generateStringStringKeyValues(size, 20, 0);

        for (int i = 0; i < entries.length; i++) {
            store = store.put(entries[i].getKey(), entries[i].getValue());
        }

        final List<KeyValue<String,String>> expected = new ArrayList<>(Arrays.asList(entries));
        expected.sort(TestUtils.HashComparator.INSTANCE);

        final List<KeyValue<String,String>> obtained = new ArrayList<>();
        for (final Map.Entry<String,String> entry : store) {
            obtained.add(new KeyValue<>(entry.getKey(), entry.getValue()));
        }

        Assert.assertEquals(expected, obtained);

    }

}
