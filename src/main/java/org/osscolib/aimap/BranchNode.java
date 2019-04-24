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

import org.osscolib.aimap.IndexedMap.DataSlot;
import org.osscolib.aimap.IndexedMap.Node;
import org.osscolib.aimap.IndexedMap.Visitor;

final class BranchNode<K,V> implements Node<K,V> {

    private final long indexLowLimit;
    private final long indexHighLimit;
    private final long indexRangePerSlot;
    private final int maxNodeSize;

    private final int childrenSize;
    private final Node<K,V>[] children;




    BranchNode(
            final long indexLowLimit, final long indexHighLimit, final long indexRangePerSlot,
            final int maxNodeSize, final int childrenSize, final Node<K,V>[] children) {
        super();
        this.indexLowLimit = indexLowLimit;
        this.indexHighLimit = indexHighLimit;
        this.indexRangePerSlot = indexRangePerSlot;
        this.maxNodeSize = maxNodeSize;
        this.childrenSize = childrenSize;
        this.children = children;
    }



    @Override
    public long getIndexLowLimit() {
        return this.indexLowLimit;
    }

    @Override
    public long getIndexHighLimit() {
        return this.indexHighLimit;
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < this.children.length; i++) {
            size += this.children[i].size();
        }
        return size;
    }


    @Override
    public boolean containsKey(final long index, final Object key) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return false;
        }

        int pos = Utils.computeChildPos(this.indexLowLimit, this.indexRangePerSlot, index);
        return this.children[pos].containsKey(index, key);

    }


    @Override
    public V get(final long index, final Object key) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return null;
        }

        int pos = Utils.computeChildPos(this.indexLowLimit, this.indexRangePerSlot, index);
        return this.children[pos].get(index, key);

    }


    @Override
    public Node<K,V> put(final long index, final Map.Entry<K,V> entry) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return this;
        }

        int pos = Utils.computeChildPos(this.indexLowLimit, this.indexRangePerSlot, index);

        if (this.children[pos] != null) {

            final Node<K,V> newNode = this.children[pos].put(index, entry);

            if (newNode == this.children[pos]) {
                return this;
            }

            final Node<K,V>[] newNodes = Arrays.copyOf(this.children, this.children.length);
            newNodes[pos] = newNode;

            return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxNodeSize, this.childrenSize, newNodes);

        }

        // Nothing currently in the slot, so just add it

        final long nodeIndexRange = (this.indexHighLimit - this.indexLowLimit) + 1;
        final long nodeRangePerPosition = Utils.computeRangePerChild(nodeIndexRange, this.maxNodeSize);

        final DataSlot<K,V> dataSlot = DataSlotBuilder.build(entry);
        final long newIndexLowLimit = Utils.computeLowLimitForChild(this.indexLowLimit, nodeRangePerPosition, pos);
        final long newIndexHighLimit = Utils.computeHighLimitForChild(this.indexLowLimit, nodeRangePerPosition, pos);

        final Node<K,V> newNode =
                NodeBuilder.build(newIndexLowLimit, newIndexHighLimit, this.maxNodeSize, index, dataSlot);

        final Node<K,V>[] newSlots = Arrays.copyOf(this.children, this.children.length);
        newSlots[pos] = newNode;

        return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxNodeSize, this.childrenSize + 1, newSlots);

    }


    @Override
    public Node<K,V> remove(final long index, final Object key) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return this;
        }

        int pos = Utils.computeChildPos(this.indexLowLimit, this.indexRangePerSlot, index);

        if (this.children[pos] == null) {
            // Not found
            return this;
        }

        final Node<K,V> newNode = this.children[pos].remove(index, key);
        if (newNode == this.children[pos]) {
            return this;
        }

        if (newNode == null && this.childrenSize == 2) {
            // We need to turn this into a forwarder

            Node<K,V> onlySlot = null;
            for (int i = 0; onlySlot == null && i < this.children.length; i++) {
                if (this.children[i] != null) {
                    onlySlot = this.children[i];
                }
            }
            return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxNodeSize, onlySlot);

        }

        // Note newNode can still be null here
        final Node<K,V>[] newNodes = Arrays.copyOf(this.children, this.children.length);
        newNodes[pos] = newNode;

        final int newUsedSlots = (newNode == null? this.childrenSize - 1 : this.childrenSize);

        return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxNodeSize, newUsedSlots, newNodes);

    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitBranchNode(this.indexLowLimit, this.indexHighLimit, Arrays.asList(this.children));
    }

}
