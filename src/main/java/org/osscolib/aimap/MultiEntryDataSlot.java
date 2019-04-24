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
import java.util.Map;

import org.osscolib.aimap.IndexedMap.DataSlot;
import org.osscolib.aimap.IndexedMap.Visitor;

final class MultiEntryDataSlot<K,V> implements DataSlot<K,V> {

    private final long index;
    private final Map.Entry<K,V>[] entries;


    MultiEntryDataSlot(final long index, final Map.Entry<K,V>[] entries) {
        super();
        this.index = index;
        this.entries = entries;
    }


    @Override
    public long getIndex() {
        return this.index;
    }


    @Override
    public int size() {
        return this.entries.length;
    }


    @Override
    public boolean containsKey(final long index, final Object key) {
        if (this.index != index) {
            return false;
        }
        for (int i = 0; i < this.entries.length; i++) {
            if (this.entries[i].getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public V get(final long index, final Object key) {
        if (this.index != index) {
            return null;
        }
        for (int i = 0; i < this.entries.length; i++) {
            if (this.entries[i].getKey().equals(key)) {
                return this.entries[i].getValue();
            }
        }
        return null;
    }


    @Override
    public DataSlot<K,V> put(final long index, final Map.Entry<K,V> entry) {

        if (this.index != index) {
            return this;
        }

        // TODO We should improve this to avoid linear performance depending on the amount of collisions. This was also fixed in HashMap in Java 8 to avoid DoS

        final K entryKey = entry.getKey();
        int pos = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (this.entries[i].getKey().equals(entryKey)) {
                pos = i;
                break;
            }
        }
        if (pos >= 0) {
            if (this.entries[pos].getKey() == entry.getKey() && this.entries[pos].getValue() == entry.getValue()) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                // Note this will only happen if key and value are actually the same object, not by object equality
                return this;
            }
            final Map.Entry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
            newEntries[pos] = entry;
            return DataSlotBuilder.build(index, newEntries);
        }

        final Map.Entry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
        newEntries[this.entries.length] = entry;
        return DataSlotBuilder.build(index, newEntries);

    }


    @Override
    public DataSlot<K,V> remove(final long index, final Object key) {

        if (this.index != index) {
            return this;
        }

        int pos = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (this.entries[i].getKey().equals(key)) {
                pos = i;
                break;
            }
        }
        if (pos >= 0) {
            if (this.entries.length == 2) {
                // There are only two items in the multi value, and we are removing one, so now its single value
                final Map.Entry<K,V> remainingEntry = this.entries[pos == 0? 1 : 0];
                return DataSlotBuilder.build(index, remainingEntry);
            }
            final Map.Entry<K,V>[] newEntries = new Map.Entry[this.entries.length - 1];
            System.arraycopy(this.entries, 0, newEntries, 0, pos);
            System.arraycopy(this.entries, pos + 1, newEntries, pos, (this.entries.length - (pos + 1)));
            return DataSlotBuilder.build(index, newEntries);
        }

        return this;

    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitDataSlot(Arrays.asList(this.entries));
    }

}
