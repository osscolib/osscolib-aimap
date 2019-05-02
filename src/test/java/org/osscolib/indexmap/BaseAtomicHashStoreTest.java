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

        m = testPut(m, "one", "ONE");
//        m = testPut(m, new String("one"), "ONE");

        Assert.assertEquals(1, m.size());


    }


    private AtomicHashStore<String,String> testPut(final AtomicHashStore<String,String> map, final String key, final String value) {

        AtomicHashStore<String,String> map2;

        final boolean oldContainsKey = map.containsKey(key);
        final String oldValue = map.get(key);
        final int oldSize = map.size();

        if (!oldContainsKey) {
            Assert.assertNull(oldValue);
        }

        map2 = map.put(key, value);

        if (oldContainsKey) {
            Assert.assertEquals(oldSize, map2.size());
            // TODO This is wrong, because containsKey is based on object equality and they will only be the same if there is REFERENCE equality
            if (oldValue == value) {
                Assert.assertSame(map, map2);
                Assert.assertSame(oldValue, map2.get(key));
            } else {
                Assert.assertNotSame(map, map2);
            }
        }

        Assert.assertEquals(oldContainsKey, map.containsKey(key));
        Assert.assertEquals(oldSize, map.size());
        Assert.assertSame(oldValue, map.get(key));

        Assert.assertTrue(map2.containsKey(key));
        Assert.assertEquals((oldContainsKey) ? oldSize : (oldSize + 1), map2.size());
        Assert.assertSame(value, map2.get(key));

        Assert.assertNull(map2.get(null));

        return map2;

    }


}
