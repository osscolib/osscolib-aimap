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

import org.osscolib.aimap.IndexedMap.Slot;
import org.osscolib.aimap.IndexedMap.Visitor;

final class MultiValueSlot<K,V> implements Slot<K,V> {

    private final int index;
    private final Map.Entry<K,V>[] entries;


    MultiValueSlot(final int index, final Map.Entry<K,V>[] entries) {
        super();
        this.index = index;
        this.entries = entries;
    }


    @Override
    public int getIndex() {
        return this.index;
    }


    @Override
    public int size() {
        return this.entries.length;
    }


    @Override
    public V get(final Object key) {
        for (int i = 0; i < this.entries.length; i++) {
            if (this.entries[i].getKey().equals(key)) {
                return this.entries[i].getValue();
            }
        }
        return null;
    }


    @Override
    public Slot<K,V> put(final int index, final Map.Entry<K,V> entries) {
        if (this.index != index) {
            throw new IllegalStateException("Cannot put entries with different index in the same slot");
        }
        final K entryKey = entries.getKey();
        int pos = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (this.entries[i].getKey().equals(entryKey)) {
                pos = i;
                break;
            }
        }
        if (pos >= 0) {
            if (this.entries[pos].getKey() == entries.getKey() && this.entries[pos].getValue() == entries.getValue()) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                // Note this will only happen if key and value are actually the same object, not by object equality
                return this;
            }
            final Map.Entry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
            newEntries[pos] = entries;
            return SlotBuilder.build(this.index, newEntries);
        }
        final Map.Entry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
        newEntries[this.entries.length] = entries;
        return SlotBuilder.build(this.index, newEntries);
    }


    @Override
    public Slot<K,V> remove(final Object key) {
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
                final Map.Entry<K,V> remainingEntries = this.entries[pos == 0? 1 : 0];
                return SlotBuilder.build(this.index, remainingEntries);
            }
            final Map.Entry<K,V>[] newEntries = new Map.Entry[this.entries.length - 1];
            System.arraycopy(this.entries, 0, newEntries, 0, pos);
            System.arraycopy(this.entries, pos + 1, newEntries, pos, (this.entries.length - (pos + 1)));
            return SlotBuilder.build(this.index, newEntries);
        }
        return this;
    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitSlot(this.index, Arrays.asList(this.entries));
    }

}
