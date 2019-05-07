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

import java.util.AbstractCollection;
import java.util.Iterator;

final class Collections {

    /*
     * Collections for an AtomicHashStore are not the same as collections for an AtomicHashMap because
     * the java.util.Map specification dictates that a Map's values collection has to reflect
     * modifications in the underlying Map, which does not apply to AtomicHashStore objects
     * due to the fact they are immutable.
     */



    final static class StoreValueCollection<K,V> extends AbstractCollection<V> {

        private final AtomicHashStore<K,V> store;

        StoreValueCollection(final AtomicHashStore<K,V> store) {
            super();
            this.store = store;
        }

        @Override
        public Iterator<V> iterator() {
            return new Iterators.ValueIterator<>(this.store.root, this.store.maskSize);
        }

        @Override
        public int size() {
            return this.store.size();
        }

    }






    private Collections() {
        super();
    }

}
