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
package org.osscolib.atomichash;

import java.util.Arrays;

final class NodeBuilder {


    static <K,V> Node<K,V> build(final Level level, final NodeData<K,V> originalData, final NodeData<K,V> newData) {

        // Next, compute the new position that the two DataSlots (existing and new) will occupy
        final int originalChildPos = level.pos(originalData.hash);
        final int newChildPos = level.pos(newData.hash);

        // We initialise the new children array
        final Node<K,V>[] newChildren = new Node[level.mask + 1]; // 2^maskSize

        // If both data slots would be assigned the same child node position, then we need to drill down further
        if (originalChildPos == newChildPos) {

            // We will need a new level to be created, but applying a narrower range
            final Node<K,V> newBranchChild = build(level.next, originalData, newData);

            // Finally assign the BranchNode to its new position
            newChildren[newChildPos] = newBranchChild;

            return new Node<>(1, newChildren);

        }

        // Data slots are assigned different positions, so we need to create a normal (multi-children) branch

        // Now we have the full data, we can build the new DataSlotNodes
        final Node<K,V> originalDataSlotNode = new Node<>(originalData);
        final Node<K,V> newDataSlotNode = new Node<>(newData);

        // Finally assign the DataSlotNodes to their positions as new children
        newChildren[originalChildPos] = originalDataSlotNode;
        newChildren[newChildPos] = newDataSlotNode;

        return new Node<>(2, newChildren);

    }



    static <K,V> Node<K,V> build(final Level level,
                                 final int originalChildrenSize, final Node<K,V>[] originalChildren,
                                 final NodeData<K,V> newData) {

        // Next, compute the new position that the two DataSlots (existing and new) will occupy
        final int newChildPos = level.pos(newData.hash);

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
        final Node<K,V> newDataSlotNode = new Node<>(newData);

        // Finally assign the DataSlotNode to its position as new children
        newChildren[newChildPos] = newDataSlotNode;

        return new Node<>(originalChildrenSize + 1, newChildren);

    }




    private NodeBuilder() {
        super();
    }

}
