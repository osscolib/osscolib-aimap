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

import java.util.Collections;
import java.util.Map;

import org.osscolib.aimap.AtomicIndexedMap.Node;
import org.osscolib.aimap.AtomicIndexedMap.Slot;
import org.osscolib.aimap.AtomicIndexedMap.Visitor;

final class OneSlotNode<K,V> implements Node<K,V> {

    private final int indexLowLimit;
    private final int indexHighLimit;
    private final int maxSlotsPerNode;
    private final Slot<K,V> slot;


    OneSlotNode(
            final int indexLowLimit, final int indexHighLimit,
            final int maxSlotsPerNode,
            final Slot<K,V> slot) {
        super();
        this.indexLowLimit = indexLowLimit;
        this.indexHighLimit = indexHighLimit;
        this.maxSlotsPerNode = maxSlotsPerNode;
        this.slot = slot;
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
        return 1;
    }

    @Override
    public int size() {
        return this.slot.size();
    }


    Slot<K,V> internalGetSlot() {
        return this.slot;
    }


    @Override
    public V get(final int index, final K key) {

        if (index != this.slot.getIndex()) {
            return null;
        }
        return this.slot.get(key);

    }


    @Override
    public Node<K,V> put(final int index, final Map.Entry<K,V> entry) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return this;
        }

        if (this.slot.getIndex() == index) {
            final Slot<K,V> newSlot = this.slot.put(index, entry);
            return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxSlotsPerNode, newSlot);
        }

        final Slot<K,V> newSlot = SlotBuilder.build(index, entry);
        final Slot<K,V>[] newSlots = new Slot[] { this.slot, this.slot};
        if (this.slot.getIndex() < index) {
            newSlots[1] = newSlot;
        } else {
            newSlots[0] = newSlot;
        }

        // This build call might actually return a BranchNode if we have now gone over the max size threshold
        return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxSlotsPerNode, newSlots);

    }



    @Override
    public Node<K,V> remove(final int index, final K key) {

        if (index != this.slot.getIndex()) {
            // Not found
            return this;
        }

        final Slot<K,V> newSlot = this.slot.remove(key);

        if (newSlot == this.slot) {
            // Not found (index found but not key)
            return this;
        }

        if (newSlot != null) {
            return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxSlotsPerNode, newSlot);
        }

        // newSlot is null, and so we need to actually remove it

        return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxSlotsPerNode);

    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitLeafNode(this.indexLowLimit, this.indexHighLimit, Collections.singletonList(this.slot));
    }


}
