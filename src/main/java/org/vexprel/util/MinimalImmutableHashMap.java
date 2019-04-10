package org.vexprel.util;

import java.util.Arrays;

public class MinimalImmutableHashMap<K,V> {

    private static Object[] EMPTY_ENTRIES = new Object[0];
    private static int[] EMPTY_HASHES = new int[0];

    private final Object[] entries;
    private final int[] hashes;
    private final int len;


    public MinimalImmutableHashMap() {
        super();
        this.entries = EMPTY_ENTRIES;
        this.hashes = EMPTY_HASHES;
        this.len = 0;
    }

    private MinimalImmutableHashMap(final Object[] entries, final int[] hashes) {
        super();
        this.entries = entries;
        this.hashes = hashes;
        this.len = entries.length;
    }




    public MinimalImmutableHashMap<K,V> put(final K key, final V value) {

        if (key == null) {
            throw new IllegalArgumentException("Cannot insert null keys");
        }

        int pos = -1;
        Object newEntry = null;

        final int keyHashCode = key.hashCode();
        pos = Arrays.binarySearch(this.hashes, keyHashCode);

        if (pos >= 0) {
            // There is already a key with that same hash (might be the same key or just a collision)

            final Object entryAtPos = this.entries[pos];

            if (entryAtPos instanceof Entry[]) {
                // This is a multi-valued position (there is more than one entry for this hash)
                // We need to traverse the entry array sequentially and try to match with the existing entries
                final Entry[] multiEntryAtPos = (Entry[]) entryAtPos;
                for (int i = 0; i < multiEntryAtPos.length; i++) {
                    if (key.equals(multiEntryAtPos[i].key)) {
                        // This is a replacement: we need to replace the existing entry
                        final Entry[] newMultiEntry = Arrays.copyOf(multiEntryAtPos, multiEntryAtPos.length);
                        newMultiEntry[i] = new Entry(key, value);
                        newEntry = newMultiEntry;
                        break;
                    }
                }
                if (newEntry == null) {
                    final Entry[] newMultiEntry = Arrays.copyOf(multiEntryAtPos, multiEntryAtPos.length + 1);
                    newMultiEntry[newMultiEntry.length - 1] = new Entry(key, value);
                    newEntry = newMultiEntry;
                }
            } else {
                // This is a single-valued position, let's see if we need to grow or replace
                if (key.equals(((Entry)entryAtPos).key)) {
                    newEntry = new Entry(key, value);
                } else {
                    newEntry = new Entry[] { (Entry)entryAtPos, new Entry(key, value) };
                }
            }

            // Once we computed the new entry, we compute the arrays of the new Map. We will only need
            // to change the entries array, because hashes will stay the same
            final Object[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
            newEntries[pos] = newEntry;

            return new MinimalImmutableHashMap<>(newEntries, this.hashes);

        }

        // This is a new entry, so we need to grow both entries and hashes arrays

        newEntry = new Entry(key, value);
        pos = (pos + 1) * -1;

        final Object[] newEntries = new Object[this.entries.length + 1];
        System.arraycopy(this.entries, 0, newEntries, 0, pos);
        newEntries[pos] = newEntry;
        System.arraycopy(this.entries, pos, newEntries, pos + 1, this.entries.length - pos);

        final int[] newHashes = new int[this.hashes.length + 1];
        System.arraycopy(this.hashes, 0, newHashes, 0, pos);
        newHashes[pos] = keyHashCode;
        System.arraycopy(this.hashes, pos, newHashes, pos + 1, this.hashes.length - pos);

        return new MinimalImmutableHashMap<>(newEntries, newHashes);

    }



    private static class Entry<K,V> {
        private final K key;
        private final V value;
        private Entry(final K key, final V value) {
            super();
            this.key = key;
            this.value = value;
        }
    }

}
