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
import java.util.function.ToIntFunction;

public interface IndexedMap<K,V> {

    int size();
//    boolean isEmpty();
//    boolean containsKey(Object key);
//    boolean containsValue(Object value);
    V get(final Object key);


//    Set<K> keySet();
//    Collection<V> values();
//    Set<Map.Entry<K, V>> entrySet();


    int getLowestIndex();
    int getHighestIndex();
    ToIntFunction<Object> getIndexFunction();


    static <K,V> Builder<K,V> build() {
        return Builder.DEFAULT_BUILDER;
    }



    final class Builder<K,V> {

        private final static int DEFAULT_MAX_SLOTS_PER_NODE = 64;
        private final static long DEFAULT_LOWEST_INDEX = (long)Integer.MIN_VALUE;
        private final static long DEFAULT_HIGHEST_INDEX = (long)Integer.MAX_VALUE;
        private final static HashCodeFunction DEFAULT_INDEX_FUNCTION = new HashCodeFunction();

        private final static Builder DEFAULT_BUILDER =
                new Builder(DEFAULT_LOWEST_INDEX, DEFAULT_HIGHEST_INDEX, DEFAULT_INDEX_FUNCTION, DEFAULT_MAX_SLOTS_PER_NODE);

        private final ToIntFunction<Object> indexFunction;
        private final long lowestIndex;
        private final long highestIndex;
        private final int maxNodeSize;


        private Builder(final long lowestIndex, final long highestIndex, final ToIntFunction<Object> indexFunction,
                final int maxNodeSize) {
            super();
            this.lowestIndex = lowestIndex;
            this.highestIndex = highestIndex;
            this.indexFunction = indexFunction;
            this.maxNodeSize = maxNodeSize;
        }


        public Builder<K,V> withMaxNodeSize(final int maxNodeSize) {
            return new Builder<>(this.lowestIndex, this.highestIndex, this.indexFunction, maxNodeSize);
        }


        public Builder<K,V> withIndexing(
                final int lowestIndex, final int highestIndex, final ToIntFunction<Object> indexFunction) {
            // Indexing will be defined in terms of int, but internally long will be used to avoid overflow issues
            return new Builder<>((long)lowestIndex, (long)highestIndex, indexFunction, this.maxNodeSize);
        }


        public FluentIndexedMap<K,V> asFluentMap() {
            final Node<K,V> root = NodeBuilder.build(this.lowestIndex, this.highestIndex, this.maxNodeSize);
            return new FluentIndexedMap<>(this.lowestIndex, this.highestIndex, this.indexFunction, this.maxNodeSize, root);
        }


        private static class HashCodeFunction implements ToIntFunction<Object> {
            @Override
            public int applyAsInt(final Object k) {
                return k.hashCode();
            }
        }

    }




    // TODO maybe add pretty print functions here? probably better in an additional package-protected intf...

    interface DataSlot<K,V> {

        boolean containsKey(final Object key);
        V get(final Object key);
        DataSlot<K,V> put(final Map.Entry<K, V> entry);
        DataSlot<K,V> remove(final Object key);
        int size();

        void acceptVisitor(final Visitor<K, V> visitor);

    }


    interface Node<K,V> {

        boolean containsKey(final long index, final Object key);
        V get(final long index, final Object key);
        Node<K,V> put(final long index, final Map.Entry<K, V> entry);
        Node<K,V> remove(final long index, final Object key);
        int size();

        long getIndexLowLimit();
        long getIndexHighLimit();

        void acceptVisitor(final Visitor<K, V> visitor);

    }


    // TODO maybe this can be package protected? in such case, Node and Slot could be package-protected too
    interface Visitor<K,V> {

        void visitRoot(final Node<K, V> rootNode);
        void visitBranchNode(final long indexLowLimit, final long indexHighLimit, final List<Node<K, V>> nodes);
        void visitDataSlotNode(final long indexLowLimit, final long indexHighLimit, final long dataSlotIndex, final DataSlot dataSlot);
        void visitDataSlot(final List<Map.Entry<K, V>> entries);

    }

}
