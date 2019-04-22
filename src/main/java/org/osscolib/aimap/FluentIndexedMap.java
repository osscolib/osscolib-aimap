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

import java.util.function.ToIntFunction;


public final class FluentIndexedMap<K,V> implements IndexedMap<K,V> {

    private final Node<K,V> root;
    private final ToIntFunction<Object> indexFunction;
    private final int lowestIndex;
    private final int highestIndex;
    private final int maxSlotsPerNode;



    FluentIndexedMap(
            final int lowestIndex, final int highestIndex, final ToIntFunction<Object> indexFunction,
            final int maxSlotsPerNode, final Node<K,V> root) {
        super();
        this.lowestIndex = lowestIndex;
        this.highestIndex = highestIndex;
        this.indexFunction = indexFunction;
        this.maxSlotsPerNode = maxSlotsPerNode;
        this.root = root;
    }



    @Override
    public int getLowestIndex() {
        return this.lowestIndex;
    }


    @Override
    public int getHighestIndex() {
        return this.highestIndex;
    }

    @Override
    public ToIntFunction<Object> getIndexFunction() {
        return this.indexFunction;
    }


    @Override
    public int size() {
        return this.root.size();
    }


    public V get(final Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        return this.root.get(computeIndex(key), key);
    }


    public FluentIndexedMap<K,V> put(final K key, final V value) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        final Node newRoot =
                this.root.put(computeIndex(key), Entry.build(key, value));
        if (this.root == newRoot) {
            return this;
        }
        return new FluentIndexedMap<K,V>(
                this.lowestIndex, this.highestIndex, this.indexFunction, this.maxSlotsPerNode, newRoot);
    }


    public FluentIndexedMap<K,V> remove(final Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        final Node newRoot =
                this.root.remove(computeIndex(key), key);
        if (this.root == newRoot) {
            return this;
        }
        return new FluentIndexedMap<K,V>(
                this.lowestIndex, this.highestIndex, this.indexFunction, this.maxSlotsPerNode, newRoot);
    }





    private int computeIndex(final Object key) {
        final int idx = this.indexFunction.applyAsInt(key);
        if (this.lowestIndex > idx || this.highestIndex < idx) {
            throw new IllegalStateException(
                    String.format(
                            "Map has bad indexing specification. A key was assigned index %d but " +
                                    "established limits are %d to %d", idx, this.lowestIndex, this.highestIndex));
        }
        return idx;
    }


    String prettyPrint() {
        final Visitor<K,V> visitor = new PrettyPrintVisitor();
        visitor.visitRoot(this.root);
        return visitor.toString();
    }



}
