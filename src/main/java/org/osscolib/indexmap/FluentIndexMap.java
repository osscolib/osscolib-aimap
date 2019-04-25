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

import java.util.function.ToIntFunction;


public final class FluentIndexMap<K,V> implements IndexMap<K,V> {

    private final Node<K,V> root;
    private final ToIntFunction<Object> indexFunction;
    private final long lowestIndex;
    private final long highestIndex;
    private final int maxNodeSize;



    FluentIndexMap(
            final long lowestIndex, final long highestIndex, final ToIntFunction<Object> indexFunction,
            final int maxNodeSize, final Node<K,V> root) {
        super();
        this.lowestIndex = lowestIndex;
        this.highestIndex = highestIndex;
        this.indexFunction = indexFunction;
        this.maxNodeSize = maxNodeSize;
        this.root = root;
    }



    @Override
    public int getLowestIndex() {
        return (int) this.lowestIndex;
    }


    @Override
    public int getHighestIndex() {
        return (int) this.highestIndex;
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

        final long index = computeIndex(key);

        Node<K,V> node = this.root;
        int pos;

        while (node != null && node.branch) {
            pos = Utils.computeChildPos(node.indexLowLimit, node.rangePerChild, index);
            node = node.children[pos];
        }

        return (node != null && node.dataSlotIndex == index) ? node.dataSlot.containsKey(index, key) : null;

    }


    @Override
    public V get(final Object key) {

        final long index = computeIndex(key);

        Node<K,V> node = this.root;
        int pos;

        while (node != null && node.branch) {
            pos = Utils.computeChildPos(node.indexLowLimit, node.rangePerChild, index);
            node = node.children[pos];
        }

        return (node != null && node.dataSlotIndex == index) ? node.dataSlot.get(index, key) : null;

    }


    public FluentIndexMap<K,V> put(final K key, final V value) {

        final long index = computeIndex(key);
        final Entry entry = Entry.build(key, value);

        final Node newRoot;
        if (this.root == null) {

            final DataSlot<K,V> newDataSlot = DataSlotBuilder.build(index, entry);
            newRoot = NodeBuilder.build(this.lowestIndex, this.highestIndex, this.maxNodeSize, index, newDataSlot);

        } else {

            newRoot = this.root.put(index, entry);
            if (this.root == newRoot) {
                return this;
            }

        }

        return new FluentIndexMap<K,V>(
                this.lowestIndex, this.highestIndex, this.indexFunction, this.maxNodeSize, newRoot);

    }


    public FluentIndexMap<K,V> remove(final Object key) {

        if (this.root == null) {
            return this;
        }

        final Node newRoot = this.root.remove(computeIndex(key), key);
        if (this.root == newRoot) {
            return this;
        }

        return new FluentIndexMap<K,V>(
                this.lowestIndex, this.highestIndex, this.indexFunction, this.maxNodeSize, newRoot);

    }





    private long computeIndex(final Object key) {
        // Even if we only allow index functions to return int, we will internally use indexes as a long for convenience
        final long idx = (long) this.indexFunction.applyAsInt(key);
        if (this.lowestIndex > idx || this.highestIndex < idx) {
            throw new IllegalStateException(
                    String.format(
                            "Map has bad indexing specification. A key was assigned index %d but " +
                            "established limits are %d to %d", idx, this.lowestIndex, this.highestIndex));
        }
        return idx;
    }


    String prettyPrint() {
        final IndexMapVisitor<K,V> visitor = new PrettyPrintIndexMapVisitor();
        visitor.visitRoot(this.root);
        return visitor.toString();
    }



}
