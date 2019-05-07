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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

final class Sets {

    /*
     * Sets for an AtomicHashStore are not the same as sets for an AtomicHashMap because
     * the java.util.Map specification dictates that a Map's entry set has to reflect
     * modifications in the underlying Map, which does not apply to AtomicHashStore
     * objects as they are immutable.
     */


    final static class StoreEntrySet<K,V> extends AbstractSet<Map.Entry<K,V>> {

        private final AtomicHashStore<K,V> store;

        StoreEntrySet(final AtomicHashStore<K,V> store) {
            super();
            this.store = store;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new Iterators.EntryIterator<>(this.store.root, this.store.maskSize);
        }

        @Override
        public int size() {
            return this.store.size();
        }

    }


    final static class StoreKeySet<K,V> extends AbstractSet<K> {

        private final AtomicHashStore<K,V> store;

        StoreKeySet(final AtomicHashStore<K,V> store) {
            super();
            this.store = store;
        }

        @Override
        public Iterator<K> iterator() {
            return new Iterators.KeyIterator<>(this.store.root, this.store.maskSize);
        }

        @Override
        public int size() {
            return this.store.size();
        }

    }


    final static class StoreValueSet<K,V> extends AbstractSet<V> {

        private final AtomicHashStore<K,V> store;

        StoreValueSet(final AtomicHashStore<K,V> store) {
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






    private Sets() {
        super();
    }

}
