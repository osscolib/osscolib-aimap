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

import java.util.Objects;
import java.util.function.ToIntFunction;


public final class FluentIndexMap<K,V> implements IndexMap<K,V> {

    private final int mask;
    private final ToIntFunction<Object> indexFunction;
    private final Node<K,V> root;



    FluentIndexMap(final int mask, final ToIntFunction<Object> indexFunction, final Node<K,V> root) {
        super();
        this.mask = mask;
        this.indexFunction = indexFunction;
        this.root = root;
    }



    @Override
    public ToIntFunction<Object> getIndexFunction() {
        return this.indexFunction;
    }


    @Override
    public int size() {
        if (this.root == null) {
            return 0;
        }
        return this.root.size();
    }



    @Override
    public boolean containsKey(final Object key) {
        final int index = computeIndex(key);
        final Entry<K,V> entry = getEntry(index, key);
        return entry != null;
    }


    @Override
    public V get(final Object key) {
        final int index = computeIndex(key);
        final Entry<K,V> entry = getEntry(index, key);
        return entry != null ? entry.value : null;
    }



    Entry<K,V> getEntry(final int index, final Object key) {
        int m, msize, shift;
        Node<K,V> node;
        NodeData<K,V> data = null;
        Object entryKey;

        m = this.mask;
        msize = (31 - Integer.numberOfLeadingZeros(m + 1)); // maskSize = log2(mask + 1)

        for (shift = 0, node = this.root; node != null && (data = node.data) == null; shift += msize) {
            node = node.children[(index >> shift) & m];
        }

        if (data == null || data.index != index) {
            return null;
        }

        if (!data.multi) {
            entryKey = data.entry.key;
            return (entryKey == key || (entryKey != null && entryKey.equals(key))) ? data.entry : null;
        }

        return getEntryFromMulti(data, key);

    }


    Entry<K,V> getEntryFromMulti(final NodeData<K,V> data, final Object key) {
        for (int i = 0; i < data.entries.length; i++) {
            if (Objects.equals(data.entries[i].key, key)) {
                return data.entries[i];
            }
        }
        return null;
    }




    public FluentIndexMap<K,V> put(final K key, final V value) {

        final int index = computeIndex(key);
        final Entry entry = Entry.build(key, value);

        final Node newRoot;
        if (this.root == null) {

            final NodeData<K,V> newData = new NodeData<>(index, entry);
            newRoot = NodeBuilder.build(newData);

        } else {

            newRoot = this.root.put(index, 0, this.mask, entry);
            if (this.root == newRoot) {
                return this;
            }

        }

        return new FluentIndexMap<K,V>(this.mask, this.indexFunction, newRoot);

    }


    public FluentIndexMap<K,V> remove(final Object key) {

        if (this.root == null) {
            return this;
        }

        final Node newRoot = this.root.remove(computeIndex(key), 0, this.mask, key);
        if (this.root == newRoot) {
            return this;
        }

        return new FluentIndexMap<K,V>(this.mask, this.indexFunction, newRoot);

    }





    private int computeIndex(final Object key) {
        return this.indexFunction.applyAsInt(key);
    }


    String prettyPrint() {
        final IndexMapVisitor<K,V> visitor = new PrettyPrintIndexMapVisitor();
        visitor.visitRoot(this.mask, this.root);
        return visitor.toString();
    }



}
