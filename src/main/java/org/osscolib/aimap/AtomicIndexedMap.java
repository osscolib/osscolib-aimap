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

public final class AtomicIndexedMap<K,V> {

    private final Node root;


    public static AtomicIndexedMap build(final int maxSlotsPerNode) {
        final Node root =
                NodeBuilder.build(Integer.MIN_VALUE, Integer.MAX_VALUE, maxSlotsPerNode, Utils.emptySlots());
        return new AtomicIndexedMap(root);
    }


    private AtomicIndexedMap(final Node root) {
        super();
        this.root = root;
    }


    protected int computeIndex(final K key) {
        return key.hashCode();
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
        return new AtomicIndexedMap<>(newRoot);
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
        return new AtomicIndexedMap<>(newRoot);
    }


    String prettyPrint() {
        final Visitor visitor = new PrettyPrintVisitor();
        visitor.visitRoot(this.root);
        return visitor.toString();
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
