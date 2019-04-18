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
import org.osscolib.aimap.AtomicIndexedMap.Slot;
import org.osscolib.aimap.AtomicIndexedMap.Visitor;

final class MultiSlotNode<K,V> implements Node<K,V> {

    private final int indexLowLimit;
    private final int indexHighLimit;
    private final int maxSlotsPerNode;
    private final Slot<K,V>[] slots;


    MultiSlotNode(
            final int indexLowLimit, final int indexHighLimit,
            final int maxSlotsPerNode,
            final Slot<K,V>[] slots) {
        super();
        this.indexLowLimit = indexLowLimit;
        this.indexHighLimit = indexHighLimit;
        this.maxSlotsPerNode = maxSlotsPerNode;
        this.slots = slots;
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
        return this.slots.length;
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < this.slots.length; i++) {
            size += this.slots[i].size();
        }
        return size;
    }


    Slot<K,V>[] internalGetSlots() {
        return this.slots;
    }


    @Override
    public V get(final int index, final K key) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return null;
        }

        int pos = Utils.binarySearchIndexInSlots(this.slots, index);
        if (pos < 0) {
            // This should never happen
            return null;
        }

        return this.slots[pos].get(key);

    }


    @Override
    public Node<K,V> put(final int index, final Map.Entry<K,V> entry) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return this;
        }

        int pos = Utils.binarySearchIndexInSlots(this.slots, index);

        if (pos >= 0) {

            final Slot<K,V> newSlot = this.slots[pos].put(index, entry);
            final Slot<K,V>[] newSlots = Arrays.copyOf(this.slots, this.slots.length);
            newSlots[pos] = newSlot;

            return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxSlotsPerNode, newSlots);

        }

        pos = (++pos * -1);

        final Slot<K,V>[] newSlots = Arrays.copyOf(this.slots, this.slots.length + 1);
        System.arraycopy(newSlots, pos, newSlots, pos + 1, newSlots.length - (pos + 1));
        newSlots[pos] = SlotBuilder.build(index, entry);

        // This build call might actually return a TreeNode if we have now gone over the max size threshold
        return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxSlotsPerNode, newSlots);

    }



    @Override
    public Node<K,V> remove(final int index, final K key) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return this;
        }

        int pos = Utils.binarySearchIndexInSlots(this.slots, index);
        if (pos < 0) {
            // Not found
            return this;
        }

        final Slot<K,V> newSlot = this.slots[pos].remove(key);

        if (newSlot == this.slots[pos]) {
            // Not found (index found but not key)
            return this;
        }

        if (newSlot != null) {

            final Slot<K,V>[] newSlots = Arrays.copyOf(this.slots, this.slots.length);
            newSlots[pos] = newSlot;

            return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxSlotsPerNode, newSlots);

        }

        // newSlot is null, and so we need to actually remove it

        if (this.slots.length == 1) {
            // We are empty now!
            return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxSlotsPerNode);
        }

        final Slot<K,V>[] newSlots = new Slot[this.slots.length - 1];
        System.arraycopy(this.slots, 0, newSlots, 0, pos);
        System.arraycopy(this.slots, pos +1, newSlots, pos, this.slots.length - (pos + 1));

        return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxSlotsPerNode, newSlots);

    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitLeafNode(this.indexLowLimit, this.indexHighLimit, Arrays.asList(this.slots));
    }

}
