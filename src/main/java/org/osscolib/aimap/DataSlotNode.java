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

import org.osscolib.aimap.IndexedMap.DataSlot;
import org.osscolib.aimap.IndexedMap.Node;
import org.osscolib.aimap.IndexedMap.Visitor;

final class DataSlotNode<K,V> implements Node<K,V> {

    private final long indexLowLimit;
    private final long indexHighLimit;
    private final int maxNodeSize;

    private final long dataSlotIndex;
    private final DataSlot<K,V> dataSlot;




    DataSlotNode(final long indexLowLimit, final long indexHighLimit,
                 final int maxNodeSize, final long dataSlotIndex, final DataSlot<K,V> dataSlot) {
        super();
        this.indexLowLimit = indexLowLimit;
        this.indexHighLimit = indexHighLimit;
        this.maxNodeSize = maxNodeSize;
        this.dataSlotIndex = dataSlotIndex;
        this.dataSlot = dataSlot;
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
        return this.dataSlot.size();
    }


    @Override
    public boolean containsKey(final long index, final Object key) {
        return this.dataSlotIndex == index && this.dataSlot.containsKey(key);
    }


    @Override
    public V get(final long index, final Object key) {
        if (this.dataSlotIndex == index) {
            return this.dataSlot.get(key);
        }
        return null;
    }


    @Override
    public Node<K,V> put(final long index, final Map.Entry<K,V> entry) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return this;
        }

        if (this.dataSlotIndex == index) {

            final DataSlot<K,V> newDataSlot = this.dataSlot.put(entry);
            if (newDataSlot == this.dataSlot) {
                // Nothing was added because the entry already existed
                return this;
            }

            return NodeBuilder.build(
                    this.indexLowLimit, this.indexHighLimit, this.maxNodeSize, this.dataSlotIndex, newDataSlot);

        }

        // We need to add a new slot in the same range, so this has to be converted into a branch


    }



    @Override
    public Node<K,V> remove(final long index, final Object key) {

        if (this.dataSlotIndex != index) {
            return this;
        }

        final DataSlot<K,V> newDataSlot = this.dataSlot.remove(key);

        if (newDataSlot == this.dataSlot) {
            // No changes needed (key not found)
            return this;
        }

        if (newDataSlot != null) {
            // There is still data at the slot - we need a new container node
            return NodeBuilder.build(
                    this.indexLowLimit, this.indexHighLimit, this.maxNodeSize, this.dataSlotIndex, newDataSlot);
        }

        // All data removed -> should remove this container too
        return null;

    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitDataSlotNode(this.indexLowLimit, this.indexHighLimit, this.dataSlotIndex, this.dataSlot);
    }

}
