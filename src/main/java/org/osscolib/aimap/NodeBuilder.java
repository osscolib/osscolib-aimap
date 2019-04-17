package org.osscolib.aimap;

import org.osscolib.aimap.AtomicIndexedMap.Node;
import org.osscolib.aimap.AtomicIndexedMap.Slot;

final class NodeBuilder {


    static <K,V> Node<K,V> build(final int indexLowLimit, final int indexHighLimit, final int maxSlotsPerNode) {
        return new ZeroSlotsNode(indexLowLimit, indexHighLimit, maxSlotsPerNode);
    }



    static <K,V> Node<K,V> build(final int indexLowLimit, final int indexHighLimit, final int maxSlotsPerNode, final Slot<K,V> slot) {
        return new OneSlotNode(indexLowLimit, indexHighLimit, maxSlotsPerNode, slot);
    }



    static <K,V> Node<K,V> build(final int indexLowLimit, final int indexHighLimit, final int maxSlotsPerNode, final Slot<K,V>[] slots) {

        if (slots.length == 0) {
            return build(indexLowLimit, indexHighLimit, maxSlotsPerNode);
        }

        if (slots.length == 1) {
            return build(indexLowLimit, indexHighLimit, maxSlotsPerNode, slots[0]);
        }

        if (slots.length > maxSlotsPerNode) {
            // We have reached the maximum amount of slots that can be contained in this type of node, so
            // we need to divide this node and turn it into a branch

            final Node<K,V>[] newNodes = new Node[maxSlotsPerNode];
            long rangePerNode = Utils.computeRangePerNode(indexLowLimit, indexHighLimit, maxSlotsPerNode);

            int slotsOffset = 0;
            Slot<K,V>[] nodeSlots;

            long newLowLimit, newHighLimit;
            for (int i = 0; i < maxSlotsPerNode; i++) {

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
                    nodeSlots = new Slot[n - slotsOffset];
                    System.arraycopy(slots, slotsOffset, nodeSlots, 0, (n - slotsOffset));
                }
                slotsOffset = n;

                newNodes[i] = NodeBuilder.build((int)newLowLimit, (int)newHighLimit, maxSlotsPerNode, nodeSlots);

            }

            return build(indexLowLimit, indexHighLimit, slots.length, maxSlotsPerNode, newNodes);

        }


        return new MultiSlotNode<>(indexLowLimit, indexHighLimit, maxSlotsPerNode, slots);

    }



    static <K,V> Node<K,V> build(final int indexLowLimit, final int indesHighLimit,
                   final int newSlotCount, final int maxSlotsPerNode, final Node<K,V>[] nodes) {

        if (newSlotCount <= maxSlotsPerNode) {
            // We have gone under the threshold, so we should condense this back into a slot-containing node

            final Slot<K,V>[] newSlots = new Slot[newSlotCount];
            int offset = 0;
            for (int i = 0; i < nodes.length; i++) {
                final Node<K,V> node = nodes[i];
                if (node instanceof ZeroSlotsNode) {
                    // No slots to be added
                    continue;
                } else if (node instanceof OneSlotNode) {
                    // Node contains a single slot
                    newSlots[offset] = ((OneSlotNode<K,V>)node).internalGetSlot();
                    offset++;
                } else {
                    // Node is a MultiSlotNode, containing more than 1 slot
                    final MultiSlotNode<K,V> multiSlotNode = (MultiSlotNode<K,V>) node;
                    final Slot<K,V>[] slots = multiSlotNode.internalGetSlots();
                    System.arraycopy(slots, 0, newSlots, offset, slots.length);
                    offset += slots.length;
                }
            }

            return NodeBuilder.build(indexLowLimit, indesHighLimit, maxSlotsPerNode, newSlots);

        }

        return new BranchNode<>(indexLowLimit, indesHighLimit, newSlotCount, maxSlotsPerNode, nodes);

    }


    private NodeBuilder() {
        super();
    }

}
