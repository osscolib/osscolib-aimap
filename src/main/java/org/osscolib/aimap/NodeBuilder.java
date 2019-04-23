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

import org.osscolib.aimap.IndexedMap.Node;
import org.osscolib.aimap.IndexedMap.DataSlot;

final class NodeBuilder {



    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final int maxNodeSize,
                                 final long dataSlotIndex, final DataSlot<K,V> dataSlot) {
        return new DataSlotNode<>(indexLowLimit, indexHighLimit, maxNodeSize, dataSlotIndex, dataSlot);
    }



    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final int maxNodeSize,
                                 final Node<K,V> node) {
        return new ForwarderNode(indexLowLimit, indexHighLimit, maxNodeSize, node);
    }



    static <K,V> Node<K,V> build(final long indexLowLimit, final long indexHighLimit, final int maxNodeSize,
                                 final Node<K,V> node1, final Node<K,V> node2) {

        final long nodeIndexRange = (indexHighLimit - indexLowLimit) + 1;
        final long nodeRangePerPosition = Utils.computeRangePerSlot(nodeIndexRange, maxNodeSize);
        final int nodePositions = Utils.computeNeededSlots(nodeIndexRange, nodeRangePerPosition);

        final int node1Pos = Utils.computeSlot(indexLowLimit, nodeRangePerPosition, node1.getIndexLowLimit());
        final int node2Pos = Utils.computeSlot(indexLowLimit, nodeRangePerPosition, node2.getIndexLowLimit());

        if (node1Pos == node2Pos) {
            // Both nodes would be assigned the same position, so we need to build a forwarder and try a smaller range
            final long forwardedIndexLowLimit = Utils.computeLowLimitForSlot(indexLowLimit, nodeRangePerPosition, node1Pos);
            final long forwardedIndexHighLimit = Utils.computeHighLimitForSlot(indexLowLimit, nodeRangePerPosition, node1Pos);
            final Node<K,V> forwardedNode = build(forwardedIndexLowLimit, forwardedIndexHighLimit, maxNodeSize, node1, node2);
            return new ForwarderNode<>(indexLowLimit, indexHighLimit, maxNodeSize, forwardedNode);
        }

        final Node<K,V>[] newNodes = new Node[nodePositions];
        newNodes[node1Pos] = node1;
        newNodes[node2Pos] = node2;

        return new BranchNode(indexLowLimit, indexHighLimit, nodeRangePerPosition, maxNodeSize, 2, newNodes);

    }



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


/*
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
