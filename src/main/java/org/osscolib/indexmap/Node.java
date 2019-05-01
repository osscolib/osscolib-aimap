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
package org.osscolib.indexmap;

import java.util.Arrays;

final class Node<K,V> {

    final int childrenSize;
    final Node<K,V>[] children; // can contain many nulls

    final int index;
    final DataSlot<K,V> dataSlot;



    static <K,V> Node<K,V> buildBranchNode(final int childrenSize, final Node<K,V>[] children) {
        return new Node<>(childrenSize, children, -1, null);
    }


    static <K,V> Node<K,V> buildDataSlotNode(final int dataSlotIndex, final DataSlot<K,V> dataSlot) {
        return new Node<>(0, null, dataSlotIndex, dataSlot);
    }




    private Node(final int childrenSize, final Node<K,V>[] children,
                 final int index, final DataSlot<K,V> dataSlot) {

        super();

        this.childrenSize = childrenSize;
        this.children = children;
        this.index = index;
        this.dataSlot = dataSlot;

    }



    int size() {

        if (this.dataSlot != null) {
            return this.dataSlot.size();
        }

        Node child;
        int size = 0;
        for (int i = 0; i < this.children.length; i++) {
            child = this.children[i];
            if (child != null) {
                size += child.size();
            }
        }
        return size;

    }


    static int pos(final int shift, final int mask, final int index) {
        return (index >> shift) & mask;
    }

    static int incShift(final int shift, final int mask) {
        final int maskSize = (31 - Integer.numberOfLeadingZeros(mask + 1)); // maskSize = log2(mask + 1)
        return shift + maskSize;
    }



    Node<K,V> put(final int index, final int shift, final int mask, final Entry<K, V> entry) {

        if (this.dataSlot == null) {

            final int pos = pos(shift, mask, index);
            final Node<K,V> child = this.children[pos];

            if (child != null) {

                final Node<K,V> newNode = child.put(index, incShift(shift, mask), mask, entry);

                if (newNode == child) {
                    return this;
                }

                final Node<K,V>[] newNodes = Arrays.copyOf(this.children, this.children.length);
                newNodes[pos] = newNode;

                return NodeBuilder.build(this.childrenSize, newNodes);

            }

            // Nothing currently in the selected slot, so let's add a new DataSlot
            final DataSlot<K,V> newDataSlot = DataSlotBuilder.build(index, entry);
            return NodeBuilder.build(shift, mask, this.childrenSize, this.children, newDataSlot);

        }

        // Not a branch -- this is a DataSlot node

        if (this.index == index) {

            final DataSlot<K,V> newDataSlot = this.dataSlot.put(index, entry);
            if (newDataSlot == this.dataSlot) {
                // Nothing was added because the entry already existed
                return this;
            }

            return NodeBuilder.build(this.index, newDataSlot);

        }

        // We need to add a new slot in the same range, so this has to be converted into a branch

        final DataSlot<K,V> newDataSlot = DataSlotBuilder.build(index, entry);
        return NodeBuilder.build(shift, mask, this.dataSlot, newDataSlot);

    }




    Node<K,V> remove(final int index, final int shift, final int mask, final Object key) {

        if (this.dataSlot == null) {

            final int pos = pos(shift, mask, index);
            final Node<K,V> child = this.children[pos];

            if (child == null) {
                // Not found
                return this;
            }

            final Node<K,V> newChild = child.remove(index, incShift(shift, mask), mask, key);
            if (newChild == child) {
                return this;
            }

            if (newChild == null && this.childrenSize == 1) {
                // This branch has become empty
                return null;
            }

            // Note newChild can still be null here
            final Node<K,V>[] newChildren = Arrays.copyOf(this.children, this.children.length);
            newChildren[pos] = newChild;

            final int newChildrenSize = (newChild == null? this.childrenSize - 1 : this.childrenSize);

            return NodeBuilder.build(newChildrenSize, newChildren);

        }

        // Not a branch -- this is a DataSlot node

        if (this.index != index) {
            return this;
        }

        final DataSlot<K,V> newDataSlot = this.dataSlot.remove(index, key);

        if (newDataSlot == this.dataSlot) {
            // No changes needed (key not found)
            return this;
        }

        if (newDataSlot != null) {
            // There is still data at the slot - we need a new container node
            return NodeBuilder.build(this.index, newDataSlot);
        }

        // All data removed -> should remove this container too
        return null;

    }




    void acceptVisitor(final int level, final int maskSize, final IndexMapVisitor<K, V> visitor) {
        if (this.dataSlot == null) {
            visitor.visitBranchNode(level, maskSize, Arrays.asList(this.children));
        } else {
            visitor.visitDataSlotNode(level, maskSize, this.dataSlot);
        }
    }

}
