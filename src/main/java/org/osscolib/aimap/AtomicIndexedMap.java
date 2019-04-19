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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class AtomicIndexedMap<K,V> {

    private final static int DEFAULT_MAX_SLOTS_PER_NODE = 64;
    private final static int DEFAULT_LOWEST_INDEX = Integer.MIN_VALUE;
    private final static int DEFAULT_HIGHEST_INDEX = Integer.MAX_VALUE;
    private final static HashCodeFunction DEFAULT_INDEX_FUNCTION = new HashCodeFunction();

    private final static AtomicIndexedMap DEFAULT_INSTANCE;

    private final Node<K,V> root;
    private final Function<? super K,Integer> indexFunction;
    private final int lowestIndex;
    private final int highestIndex;
    private final int maxSlotsPerNode;




    static {
        final Node root =
                NodeBuilder.build(
                        DEFAULT_LOWEST_INDEX, DEFAULT_HIGHEST_INDEX, DEFAULT_MAX_SLOTS_PER_NODE, Utils.emptySlots());
        DEFAULT_INSTANCE =
                new AtomicIndexedMap(
                        DEFAULT_LOWEST_INDEX, DEFAULT_HIGHEST_INDEX, DEFAULT_INDEX_FUNCTION,
                        DEFAULT_MAX_SLOTS_PER_NODE, root);
    }


    public static <K,V> AtomicIndexedMap<K,V> build() {
        return DEFAULT_INSTANCE;
    }




    private AtomicIndexedMap(
            final int lowestIndex, final int highestIndex, final Function<? super K,Integer> indexFunction,
            final int maxSlotsPerNode, final Node<K,V> root) {
        super();
        this.lowestIndex = lowestIndex;
        this.highestIndex = highestIndex;
        this.indexFunction = indexFunction;
        this.maxSlotsPerNode = maxSlotsPerNode;
        this.root = root;
    }


    public AtomicIndexedMap<K,V> withMaxSlotsPerNode(final int maxSlotsPerNode) {

        if (this.root.getSlotCount() != 0) {
            throw new IllegalStateException("Cannot change configuration once the map is in use");
        }

        final Node<K,V> newRoot =
                NodeBuilder.build(this.lowestIndex, this.highestIndex, maxSlotsPerNode, Utils.emptySlots());
        return new AtomicIndexedMap(
                this.lowestIndex, this.highestIndex, this.indexFunction, maxSlotsPerNode, newRoot);

    }


    public AtomicIndexedMap<K,V> withIndexing(
            final int lowestIndex, final int highestIndex, final Function<? super K,Integer> indexFunction) {

        if (this.root.getSlotCount() != 0) {
            throw new IllegalStateException("Cannot change configuration once the map is in use");
        }

        final Node<K,V> newRoot =
                NodeBuilder.build(lowestIndex, highestIndex, this.maxSlotsPerNode, Utils.emptySlots());
        return new AtomicIndexedMap(
                lowestIndex, highestIndex, indexFunction, this.maxSlotsPerNode, newRoot);

    }




    public int getLowestIndex() {
        return this.lowestIndex;
    }


    public int getHighestIndex() {
        return this.highestIndex;
    }

    public Function<? super K, Integer> getIndexFunction() {
        return this.indexFunction;
    }



    private int computeIndex(final K key) {
        final int idx = this.indexFunction.apply(key);
        if (this.lowestIndex > idx || this.highestIndex < idx) {
            throw new IllegalStateException(
                    String.format(
                            "Map has bad indexing specification. A key was assigned index %d but " +
                            "established limits are %d to %d", idx, this.lowestIndex, this.highestIndex));
        }
        return idx;
    }



    public V get(final K key) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        return (V) this.root.get(computeIndex(key), key);
    }




    public AtomicIndexedMap<K,V> put(final K key, final V value) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        final Node newRoot =
                this.root.put(computeIndex(key), Entry.build(key, value));
        if (this.root == newRoot) {
            return this;
        }
        return new AtomicIndexedMap<>(
                this.lowestIndex, this.highestIndex, this.indexFunction, this.maxSlotsPerNode, newRoot);
    }


    public AtomicIndexedMap<K,V> remove(final K key) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        final Node newRoot =
                this.root.remove(computeIndex(key), key);
        if (this.root == newRoot) {
            return this;
        }
        return new AtomicIndexedMap<>(
                this.lowestIndex, this.highestIndex, this.indexFunction, this.maxSlotsPerNode, newRoot);
    }


    String prettyPrint() {
        final Visitor<K,V> visitor = new PrettyPrintVisitor();
        visitor.visitRoot(this.root);
        return visitor.toString();
    }


    private static class HashCodeFunction implements Function<Object,Integer> {

        @Override
        public Integer apply(final Object k) {
            return k.hashCode();
        }

    }



    public interface Slot<K,V> {

        V get(final K key);
        Slot<K,V> put(final int index, final Map.Entry<K,V> entries);
        Slot<K,V> remove(final K key);
        int size();

        int getIndex();

        void acceptVisitor(final Visitor<K,V> visitor);

    }


    public interface Node<K,V> {

        V get(final int index, final K key);
        Node<K,V> put(final int index, final Map.Entry<K,V> entry);
        Node<K,V> remove(final int index, final K key);
        int size();

        int getIndexLowLimit();
        int getIndexHighLimit();
        int getSlotCount();

        void acceptVisitor(final Visitor<K,V> visitor);

    }


    public interface Visitor<K,V> {

        void visitRoot(final Node<K,V> rootNode);
        void visitBranchNode(final int indexLowLimit, final int indexHighLimit, final List<Node<K,V>> nodes);
        void visitLeafNode(final int indexLowLimit, final int indexHighLimit, final List<Slot<K,V>> slots);
        void visitSlot(final int index, final List<Map.Entry<K,V>> entries);

    }

}
