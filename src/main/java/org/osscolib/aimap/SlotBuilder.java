package org.osscolib.aimap;

import java.util.Map;

import org.osscolib.aimap.AtomicIndexedMap.Slot;

final class SlotBuilder {


    static <K,V> Slot<K,V> build(final int index, final Map.Entry<K,V> entry) {
        return new SingleValueSlot<>(index, entry);
    }

    static <K,V> Slot<K,V> build(final int index, final Map.Entry<K,V>[] entries) {
        if (entries.length == 1) {
            return new SingleValueSlot<>(index, entries[0]);
        }
        return new MultiValueSlot<>(index, entries);
    }


    private SlotBuilder() {
        super();
    }

}
