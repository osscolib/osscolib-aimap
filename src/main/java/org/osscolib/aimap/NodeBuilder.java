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

import org.osscolib.aimap.IndexedMap.DataSlot;
import org.osscolib.aimap.IndexedMap.Node;

final class NodeBuilder {



            /*



        NOTE the difference between a DS and a DSN is that the DSN does have a low, high

        - DSN: One existing DS, we have a new DS  -> Branch(DSN,DSN)
        - FWD: One existing node, we have a new DS -> Branch(NODE,DSN)
        - BRANCH: Several existing nodes, we have a new DS - Branch(NODE...,DSN)

        Node<K,V> build(low, high, maxSize, [original], newDS)

             */


    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final int maxNodeSize,
                                 final Node<K,V> slot) {
        return new ForwarderNode(indexLowLimit, indexHighLimit, maxNodeSize, slot);
    }


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

        // If both data slots would be assigned the same child node position, then we need to drill down further
        if (originalChildPos == newChildPos) {

            final long childIndexLow = Utils.computeLowLimitForChild(indexLowLimit, rangePerChild, originalChildPos);
            final long childIndexHigh = Utils.computeHighLimitForChild(indexLowLimit, rangePerChild, originalChildPos);

            // We will need a new level to be created, but applying a narrower range
            final Node<K,V> forwarded =
                    build(childIndexLow, childIndexHigh, maxNodeSize, originalDataSlot, newDataSlot);

            return build(indexLowLimit, indexHighLimit, maxNodeSize, forwarded);

        }

        // Data slots are assigned different positions, so we need to create a normal (multi-children) branch

        final int childrenSize = Utils.computeChildrenSize(nodeRange, rangePerChild);
        final Node<K,V>[] newChildren = new Node[childrenSize];

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

        // Finally assignd the DataSlotNodes to their positions as new children
        newChildren[originalChildPos] = originalDataSlotNode;
        newChildren[newChildPos] = newDataSlotNode;

        return build(indexLowLimit, indexHighLimit, rangePerChild, maxNodeSize, 2, newChildren);

    }



    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final int maxNodeSize,
                                 final Node<K,V> originalChild, final DataSlot<K,V> newDataSlot) {




    }








/*
    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final int maxNodeSize,
                                 final int newUsedSlots, final Node<K,V>[] slots) {

        if (slots.length == 0) {
            return build(indexLowLimit, indexHighLimit, maxNodeSize);
        }

        if (slots.length == 1) {
            return build(indexLowLimit, indexHighLimit, maxNodeSize, slots[0]);
        }

        if (slots.length > maxNodeSize) {
            // We have reached the maximum amount of slots that can be contained in this type of node, so
            // we need to divide this node and turn it into a branch

            final Node<K,V>[] newNodes = new Node[maxNodeSize];
            long rangePerNode = Utils.computeRangePerNode(indexLowLimit, indexHighLimit, maxNodeSize);

            int slotsOffset = 0;
            DataSlot<K,V>[] nodeSlots;

            long newLowLimit, newHighLimit;
            for (int i = 0; i < maxNodeSize; i++) {

                newLowLimit = (long)indexLowLimit + (i * rangePerNode);
                newHighLimit = newLowLimit + rangePerNode - 1;
                if (newHighLimit > indexHighLimit) {
                    // This can only happen for the last node
                    newHighLimit = (long)indexHighLimit;
                }

                int n = slotsOffset;
                while (n < slots.length &&
                        slots[n].getIndex() >= newLowLimit && slots[n].getIndex() <= newHighLimit) {
                    n++;
                }

                if (n == slotsOffset) {
                    nodeSlots = Utils.emptySlots();
                } else if (slotsOffset == 0 && n == slots.length) {
                    nodeSlots = slots;
                } else {
                    nodeSlots = new DataSlot[n - slotsOffset];
                    System.arraycopy(slots, slotsOffset, nodeSlots, 0, (n - slotsOffset));
                }
                slotsOffset = n;

                newNodes[i] = NodeBuilder.build((int)newLowLimit, (int)newHighLimit, maxNodeSize, nodeSlots);

            }

            return build(indexLowLimit, indexHighLimit, slots.length, maxNodeSize, newNodes);

        }


        return new MultiSlotNode<>(indexLowLimit, indexHighLimit, maxNodeSize, slots);

    }



    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit,
                   final long newSlotCount, final int maxNodeSize, final Node<K,V>[] nodes) {

        if (newSlotCount <= maxNodeSize) {
            // We have gone under the threshold, so we should condense this back into a slot-containing node

            final Slot<K,V>[] newSlots = new Slot[(int)newSlotCount];
            int offset = 0;
            for (int i = 0; i < nodes.length; i++) {
                final Node<K,V> node = nodes[i];
                if (node instanceof EmptyNode) {
                    // No slots to be added
                    continue;
                } else if (node instanceof ForwarderNode) {
                    // Node contains a single slot
                    newSlots[offset] = ((ForwarderNode<K,V>)node).internalGetSlot();
                    offset++;
                } else {
                    // Node is a MultiSlotNode, containing more than 1 slot
                    final MultiSlotNode<K,V> multiSlotNode = (MultiSlotNode<K,V>) node;
                    final Slot<K,V>[] slots = multiSlotNode.internalGetSlots();
                    System.arraycopy(slots, 0, newSlots, offset, slots.length);
                    offset += slots.length;
                }
            }

            return NodeBuilder.build(indexLowLimit, indexHighLimit, maxNodeSize, newSlots);

        }

        return new BranchNode<>(indexLowLimit, indexHighLimit, newSlotCount, maxNodeSize, nodes);

    }
*/

    private NodeBuilder() {
        super();
    }

}
