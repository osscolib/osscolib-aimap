package org.vexprel.util.atomicindexedmap;

import java.util.Collections;
import java.util.Map;

import org.vexprel.util.atomicindexedmap.AtomicIndexedMap.Slot;
import org.vexprel.util.atomicindexedmap.AtomicIndexedMap.Visitor;

final class SingleValueSlot<K,V> implements Slot<K,V> {

    private final int index;
    private final Map.Entry<K,V> entry;

    SingleValueSlot(final int index, final Map.Entry<K,V> entry) {
        super();
        this.index = index;
        this.entry = entry;
    }


    @Override
    public int getIndex() {
        return this.index;
    }


    @Override
    public int size() {
        return 1;
    }


    @Override
    public V get(final K key) {
        if (this.entry.getKey().equals(key)) {
            return this.entry.getValue();
        }
        return null;
    }


    @Override
    public Slot<K,V> put(final int index, final Map.Entry<K,V> entries) {
        if (this.index != index) {
            throw new IllegalStateException("Cannot put entries with different index in the same slot");
        }
        if (this.entry.getKey() == entries.getKey() && this.entry.getValue() == entries.getValue()) {
            // No need to perform any modifications, we might avoid a rewrite of a tree path!
            return this;
        }
        if (this.entry.getKey().equals(entries.getKey())) {
            // We are replacing the previous value for a new one
            return SlotBuilder.build(index, entries);
        }
        // There is an index collision, but this is a different slot, so we need to go multi value
        final Map.Entry<K,V>[] newEntries = new Map.Entry[] { this.entry, entries};
        return SlotBuilder.build(this.index, newEntries);
    }


    @Override
    public Slot<K,V> remove(final K key) {
        if (!this.entry.getKey().equals(key)) {
            return this;
        }
        return null;
    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitSlot(this.index, Collections.singletonList(this.entry));
    }

}
