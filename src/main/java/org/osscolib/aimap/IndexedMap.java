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
        private final static int DEFAULT_LOWEST_INDEX = Integer.MIN_VALUE;
        private final static int DEFAULT_HIGHEST_INDEX = Integer.MAX_VALUE;
        private final static HashCodeFunction DEFAULT_INDEX_FUNCTION = new HashCodeFunction();

        private final static Builder DEFAULT_BUILDER =
                new Builder(DEFAULT_LOWEST_INDEX, DEFAULT_HIGHEST_INDEX, DEFAULT_INDEX_FUNCTION, DEFAULT_MAX_SLOTS_PER_NODE);

        private final ToIntFunction<Object> indexFunction;
        private final int lowestIndex;
        private final int highestIndex;
        private final int maxSlotsPerNode;


        private Builder(final int lowestIndex, final int highestIndex, final ToIntFunction<Object> indexFunction,
                final int maxSlotsPerNode) {
            super();
            this.lowestIndex = lowestIndex;
            this.highestIndex = highestIndex;
            this.indexFunction = indexFunction;
            this.maxSlotsPerNode = maxSlotsPerNode;
        }


        public Builder<K,V> withMaxSlotsPerNode(final int maxSlotsPerNode) {
            return new Builder<>(this.lowestIndex, this.highestIndex, this.indexFunction, maxSlotsPerNode);
        }


        public Builder<K,V> withIndexing(
                final int lowestIndex, final int highestIndex, final ToIntFunction<Object> indexFunction) {
            return new Builder<>(lowestIndex, highestIndex, indexFunction, this.maxSlotsPerNode);
        }


        public FluentIndexedMap<K,V> asFluentMap() {
            final Node<K,V> root = NodeBuilder.build(this.lowestIndex, this.highestIndex, this.maxSlotsPerNode);
            return new FluentIndexedMap<>(this.lowestIndex, this.highestIndex, this.indexFunction, this.maxSlotsPerNode, root);
        }


        private static class HashCodeFunction implements ToIntFunction<Object> {

            @Override
            public int applyAsInt(final Object k) {
                return k.hashCode();
            }

        }

    }




    // TODO maybe add pretty print functions here? probably better in an additional package-protected intf...

    interface Slot<K,V> {

        V get(final Object key);
        Slot<K,V> put(final int index, final Map.Entry<K, V> entries);
        Slot<K,V> remove(final Object key);
        int size();

        int getIndex();

        void acceptVisitor(final Visitor<K, V> visitor);

    }


    interface Node<K,V> {

        V get(final int index, final Object key);
        Node<K,V> put(final int index, final Map.Entry<K, V> entry);
        Node<K,V> remove(final int index, final Object key);
        int size();

        int getIndexLowLimit();
        int getIndexHighLimit();
        int getSlotCount();

        void acceptVisitor(final Visitor<K, V> visitor);

    }


    // TODO maybe this can be package protected? in such case, Node and Slot could be package-protected too
    interface Visitor<K,V> {

        void visitRoot(final Node<K, V> rootNode);
        void visitBranchNode(final int indexLowLimit, final int indexHighLimit, final List<Node<K, V>> nodes);
        void visitLeafNode(final int indexLowLimit, final int indexHighLimit, final List<Slot<K, V>> slots);
        void visitSlot(final int index, final List<Map.Entry<K, V>> entries);

    }

}
