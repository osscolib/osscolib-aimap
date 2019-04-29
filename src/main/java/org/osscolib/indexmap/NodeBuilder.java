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

final class NodeBuilder {


    static <K,V> Node<K,V> build(final int level, final int maskSize,
                                 final int dataSlotIndex, final DataSlot<K,V> dataSlot) {
        return Node.buildDataSlotNode(level, maskSize, dataSlotIndex, dataSlot);
    }




    static <K,V> Node<K,V> build(final int level, final int maskSize,
                                 final int childrenSize, final Node<K,V>[] children) {
        return Node.buildBranchNode(level, maskSize, childrenSize, children);
    }




    static <K,V> Node<K,V> build(final int level, final int maskSize,
                                 final DataSlot<K,V> originalDataSlot, final DataSlot<K,V> newDataSlot) {

        // Compute shift, mask and childrenSize
        final int childrenSize = (1 << maskSize); // 2^maskSize
        final int shift = level * maskSize;
        final int mask = childrenSize - 1;

        // Obtain the DataSlot indices
        final int originalDataSlotIndex = originalDataSlot.getIndex();
        final int newDataSlotIndex = newDataSlot.getIndex();

        // Next, compute the new position that the two DataSlots (existing and new) will occupy
        final int originalChildPos = Node.pos(shift, mask, originalDataSlotIndex);
        final int newChildPos = Node.pos(shift, mask, newDataSlotIndex);

        // We initialise the new children array
        final Node<K,V>[] newChildren = new Node[childrenSize];

        // If both data slots would be assigned the same child node position, then we need to drill down further
        if (originalChildPos == newChildPos) {

            // We will need a new level to be created, but applying a narrower range
            final Node<K,V> newBranchChild =
                    build(level + 1, maskSize, originalDataSlot, newDataSlot);

            // Finally assign the BranchNode to its new position
            newChildren[newChildPos] = newBranchChild;

            return build(level, maskSize, 1, newChildren);

        }

        // Data slots are assigned different positions, so we need to create a normal (multi-children) branch

        // Now we have the full data, we can build the new DataSlotNodes
        final Node<K,V> originalDataSlotNode = build(level + 1, maskSize, originalDataSlotIndex, originalDataSlot);
        final Node<K,V> newDataSlotNode = build(level + 1, maskSize, newDataSlotIndex, newDataSlot);

        // Finally assign the DataSlotNodes to their positions as new children
        newChildren[originalChildPos] = originalDataSlotNode;
        newChildren[newChildPos] = newDataSlotNode;

        return build(level, maskSize, 2, newChildren);

    }



    static <K,V> Node<K,V> build(final int level, final int maskSize,
                                 final int originalChildrenSize, final Node<K,V>[] originalChildren,
                                 final DataSlot<K,V> newDataSlot) {

        // Compute shift, mask and childrenSize
        final int childrenSize = (1 << maskSize); // 2^maskSize
        final int shift = level * maskSize;
        final int mask = childrenSize - 1;

        // Obtain the DataSlot index
        final int newDataSlotIndex = newDataSlot.getIndex();

        // Next, compute the new position that the two DataSlots (existing and new) will occupy
        final int newChildPos = Node.pos(shift, mask, newDataSlotIndex);

        // If the data slot would be assigned an already used position, then ranges weren't properly computed
        // and the "put" operation that originated this should have been delegated to the child in that position
        if (originalChildren[newChildPos] != null) {
            throw new IllegalStateException(
                    "Children ranges have not been properly computed: adding a data slot as a sibling to a node " +
                    "that should actually contain it");
        }

        // Data slots are assigned different positions, so we need to create a new children array
        final Node<K,V>[] newChildren = Arrays.copyOf(originalChildren, originalChildren.length);

        // Now we have the full data, we can build the new DataSlotNode
        final Node<K,V> newDataSlotNode = build(level + 1, maskSize, newDataSlotIndex, newDataSlot);

        // Finally assign the DataSlotNode to its position as new children
        newChildren[newChildPos] = newDataSlotNode;

        return build(level, maskSize, originalChildrenSize + 1, newChildren);

    }




    private NodeBuilder() {
        super();
    }

}