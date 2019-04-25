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

final class NodeBuilder {


    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final int maxNodeSize,
                                 final DataSlot<K,V> dataSlot) {
        return new DataSlotNode<>(indexLowLimit, indexHighLimit, maxNodeSize, dataSlot);
    }




    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final long rangePerChild,
                                 final int maxNodeSize, final int childrenSize, final Node<K,V>[] children) {
        return new BranchNode<>(indexLowLimit, indexHighLimit, rangePerChild, maxNodeSize, childrenSize, children);
    }




    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final int maxNodeSize,
                                 final DataSlot<K,V> originalDataSlot, final DataSlot<K,V> newDataSlot) {

        // We need to compute the range per child, and the amount of children to be created
        final long nodeRange = (indexHighLimit - indexLowLimit) + 1;
        final long rangePerChild = Utils.computeRangePerChild(nodeRange, maxNodeSize);

        // Next, compute the new position that the two DataSlots (existing and new) will occupy
        final int originalChildPos = Utils.computeChildPos(indexLowLimit, rangePerChild, originalDataSlot.getIndex());
        final int newChildPos = Utils.computeChildPos(indexLowLimit, rangePerChild, newDataSlot.getIndex());

        // We initialise the new children array
        final int childrenSize = Utils.computeChildrenSize(nodeRange, rangePerChild);
        final Node<K,V>[] newChildren = new Node[childrenSize];

        // If both data slots would be assigned the same child node position, then we need to drill down further
        if (originalChildPos == newChildPos) {

            final long childIndexLow = Utils.computeLowLimitForChild(indexLowLimit, rangePerChild, originalChildPos);
            final long childIndexHigh = Utils.computeHighLimitForChild(indexLowLimit, rangePerChild, originalChildPos);

            // We will need a new level to be created, but applying a narrower range
            final Node<K,V> newBranchChild =
                    build(childIndexLow, childIndexHigh, maxNodeSize, originalDataSlot, newDataSlot);

            // Finally assign the BranchNode to its new position
            newChildren[newChildPos] = newBranchChild;

            return build(indexLowLimit, indexHighLimit, rangePerChild, maxNodeSize, 1, newChildren);

        }

        // Data slots are assigned different positions, so we need to create a normal (multi-children) branch

        // In order to create the DataSlotNodes for the DataSlots, we will need to compute their limits
        final long originalChildIndexLow = Utils.computeLowLimitForChild(indexLowLimit, rangePerChild, originalChildPos);
        final long originalChildIndexHigh = Utils.computeHighLimitForChild(indexLowLimit, rangePerChild, originalChildPos);
        final long newChildIndexLow = Utils.computeLowLimitForChild(indexLowLimit, rangePerChild, newChildPos);
        final long newChildIndexHigh = Utils.computeHighLimitForChild(indexLowLimit, rangePerChild, newChildPos);

        // Now we have the full data, we can build the new DataSlotNodes
        final Node<K,V> originalDataSlotNode =
                build(originalChildIndexLow, originalChildIndexHigh, maxNodeSize, originalDataSlot);
        final Node<K,V> newDataSlotNode =
                build(newChildIndexLow, newChildIndexHigh, maxNodeSize, newDataSlot);

        // Finally assign the DataSlotNodes to their positions as new children
        newChildren[originalChildPos] = originalDataSlotNode;
        newChildren[newChildPos] = newDataSlotNode;

        return build(indexLowLimit, indexHighLimit, rangePerChild, maxNodeSize, 2, newChildren);

    }



    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final int maxNodeSize,
                                 final Node<K,V> originalChild, final DataSlot<K,V> newDataSlot) {

        // We need to compute the range per child, and the amount of children to be created
        final long nodeRange = (indexHighLimit - indexLowLimit) + 1;
        final long rangePerChild = Utils.computeRangePerChild(nodeRange, maxNodeSize);

        // Next, compute the new position that the two DataSlots (existing and new) will occupy
        final int originalChildPos = Utils.computeChildPos(indexLowLimit, rangePerChild, originalChild.getIndexLowLimit());
        final int newChildPos = Utils.computeChildPos(indexLowLimit, rangePerChild, newDataSlot.getIndex());

        // If both data slots would be assigned the same child node position, then ranges weren't properly computed
        // and the "put" operation that originated this should have been delegated to the originalChild
        if (originalChildPos == newChildPos) {
            throw new IllegalStateException(
                    "Children ranges have not been properly computed: adding a data slot as a sibling to a node " +
                    "that should actually contain it");
        }

        // Data slots are assigned different positions, so we need to create a normal (multi-children) branch

        final int childrenSize = Utils.computeChildrenSize(nodeRange, rangePerChild);
        final Node<K,V>[] newChildren = new Node[childrenSize];

        // In order to create the DataSlotNode for the DataSlot, we will need to compute their limits
        final long newChildIndexLow = Utils.computeLowLimitForChild(indexLowLimit, rangePerChild, newChildPos);
        final long newChildIndexHigh = Utils.computeHighLimitForChild(indexLowLimit, rangePerChild, newChildPos);

        // Now we have the full data, we can build the new DataSlotNode
        final Node<K,V> newDataSlotNode = build(newChildIndexLow, newChildIndexHigh, maxNodeSize, newDataSlot);

        // Finally assign the children nodes to their positions as new children
        newChildren[originalChildPos] = originalChild;
        newChildren[newChildPos] = newDataSlotNode;

        return build(indexLowLimit, indexHighLimit, rangePerChild, maxNodeSize, 2, newChildren);

    }



    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final long rangePerChild,
                                 final int maxNodeSize, final int originalChildrenSize,
                                 final Node<K,V>[] originalChildren, final DataSlot<K,V> newDataSlot) {

        // Next, compute the new position that the two DataSlots (existing and new) will occupy
        final int newChildPos = Utils.computeChildPos(indexLowLimit, rangePerChild, newDataSlot.getIndex());

        // If the data slot would be assigned an already used position, then ranges weren't properly computed
        // and the "put" operation that originated this should have been delegated to the child in that position
        if (originalChildren[newChildPos] != null) {
            throw new IllegalStateException(
                    "Children ranges have not been properly computed: adding a data slot as a sibling to a node " +
                    "that should actually contain it");
        }

        // Data slots are assigned different positions, so we need to create a new children array
        final Node<K,V>[] newChildren = Arrays.copyOf(originalChildren, originalChildren.length);

        // In order to create the DataSlotNode for the DataSlot, we will need to compute their limits
        final long newChildIndexLow = Utils.computeLowLimitForChild(indexLowLimit, rangePerChild, newChildPos);
        final long newChildIndexHigh = Utils.computeHighLimitForChild(indexLowLimit, rangePerChild, newChildPos);

        // Now we have the full data, we can build the new DataSlotNode
        final Node<K,V> newDataSlotNode = build(newChildIndexLow, newChildIndexHigh, maxNodeSize, newDataSlot);

        // Finally assign the DataSlotNode to its position as new children
        newChildren[newChildPos] = newDataSlotNode;

        return build(indexLowLimit, indexHighLimit, rangePerChild, maxNodeSize, originalChildrenSize + 1, newChildren);

    }




    private NodeBuilder() {
        super();
    }

}
