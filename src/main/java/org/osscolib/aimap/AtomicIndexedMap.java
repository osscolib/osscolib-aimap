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
package org.osscolib.aimap;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public final class AtomicIndexedMap<K,V> implements IndexedMap<K,V> {

    private final AtomicReference<FluentIndexedMap<K,V>> innerMap;



    AtomicIndexedMap(
            final int lowestIndex, final int highestIndex, final Function<Object,Integer> indexFunction,
            final int maxSlotsPerNode, final Node<K,V> root) {
        super();
        this.innerMap = new AtomicReference<>();
        this.innerMap.set(
                new FluentIndexedMap<K,V>(lowestIndex, highestIndex, indexFunction, maxSlotsPerNode, root));
    }


    @Override
    public int getLowestIndex() {
        return this.innerMap.get().getLowestIndex();
    }

    @Override
    public int getHighestIndex() {
        return this.innerMap.get().getHighestIndex();
    }

    @Override
    public Function<Object, Integer> getIndexFunction() {
        return this.innerMap.get().getIndexFunction();
    }



    @Override
    public int size() {
        return this.innerMap.get().size();
    }

    @Override
    public V get(final Object key) {
        return this.innerMap.get().get(key);
    }


}
