package org.vexprel.util.atomicindexedmap;

import java.util.Collections;
import java.util.Map;

import org.vexprel.util.atomicindexedmap.AtomicIndexedMap.Node;
import org.vexprel.util.atomicindexedmap.AtomicIndexedMap.Slot;
import org.vexprel.util.atomicindexedmap.AtomicIndexedMap.Visitor;

final class ZeroSlotsNode<K,V> implements Node<K,V> {

    private final int indexLowLimit;
    private final int indexHighLimit;
    private final int maxSlotsPerNode;


    ZeroSlotsNode(final int indexLowLimit, final int indexHighLimit, final int maxSlotsPerNode) {
        super();
        this.indexLowLimit = indexLowLimit;
        this.indexHighLimit = indexHighLimit;
        this.maxSlotsPerNode = maxSlotsPerNode;
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
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }


    @Override
    public V get(final int index, final K key) {
        return null;
    }


    @Override
    public Node<K,V> put(final int index, final Map.Entry<K,V> entry) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return this;
        }

        final Slot<K,V> newSlot = SlotBuilder.build(index, entry);
        return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxSlotsPerNode, newSlot);

    }

    @Override
    public Node<K,V> remove(final int index, final K key) {
        return this;
    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitLeafNode(this.indexLowLimit, this.indexHighLimit, Collections.emptyList());
    }


}
