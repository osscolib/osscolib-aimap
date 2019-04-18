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

import java.util.Arrays;
import java.util.Map;

import org.osscolib.aimap.AtomicIndexedMap.Node;
import org.osscolib.aimap.AtomicIndexedMap.Visitor;

final class BranchNode<K,V> implements Node<K,V> {

    private final int indexLowLimit;
    private final int indexHighLimit;
    private final int slotCount;
    private final int maxSlotsPerNode;
    private final Node<K,V>[] nodes;

    BranchNode(
            final int indexLowLimit, final int indexHighLimit,
            final int slotCount, final int maxSlotsPerNode,
            final Node<K,V>[] nodes) {
        super();
        this.indexLowLimit = indexLowLimit;
        this.indexHighLimit = indexHighLimit;
        this.slotCount = slotCount;
        this.maxSlotsPerNode = maxSlotsPerNode;
        this.nodes = nodes;
    }



    @Override
    public int getIndexLowLimit() {
        return this.indexLowLimit;
    }

    @Override
    public int getIndexHighLimit() {
        return this.indexHighLimit;
    }

    @Override
    public int getSlotCount() {
        return this.slotCount;
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < this.nodes.length; i++) {
            size += this.nodes[i].size();
        }
        return size;
    }


    @Override
    public V get(final int index, final K key) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return null;
        }

        int pos = Utils.binarySearchIndexInNodes(this.nodes, index);
        if (pos < 0) {
            // This should never happen
            return null;
        }

        return this.nodes[pos].get(index, key);

    }


    @Override
    public Node<K,V> put(final int index, final Map.Entry<K,V> entry) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return this;
        }

        int pos = Utils.binarySearchIndexInNodes(this.nodes, index);
        if (pos < 0) {
            // This should never happen
            return this;
        }

        final Node<K,V> newNode = this.nodes[pos].put(index, entry);

        if (newNode == this.nodes[pos]) {
            return this;
        }

        final Node<K,V>[] newNodes = Arrays.copyOf(this.nodes, this.nodes.length);
        newNodes[pos] = newNode;

        final int newSlotCount =
                (this.slotCount - this.nodes[pos].getSlotCount()) + newNode.getSlotCount();

        return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, newSlotCount, this.maxSlotsPerNode, newNodes);

    }


    @Override
    public Node<K,V> remove(final int index, final K key) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return this;
        }

        int pos = Utils.binarySearchIndexInNodes(this.nodes, index);
        if (pos < 0) {
            // This should never happen
            return this;
        }

        final Node<K,V> newNode = this.nodes[pos].remove(index, key);

        if (newNode == this.nodes[pos]) {
            return this;
        }

        // Note that no implementation of Node can return null after remove

        final Node<K,V>[] newNodes = Arrays.copyOf(this.nodes, this.nodes.length);
        newNodes[pos] = newNode;

        final int newSlotCount =
                (this.slotCount - this.nodes[pos].getSlotCount()) + newNode.getSlotCount();

        // This build call might actually return a slot-containing node if we have now gone under the max size threshold
        return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, newSlotCount, this.maxSlotsPerNode, newNodes);

    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitBranchNode(this.indexLowLimit, this.indexHighLimit, Arrays.asList(this.nodes));
    }

}
