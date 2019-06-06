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
package org.osscolib.atomichash;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

final class NodeData<K,V> implements Serializable {

    private static final long serialVersionUID = -3351803095783536070L;

    final int hash;
    final Entry<K,V> entry;
    final Entry<K,V>[] entries;



    NodeData(final Entry<K,V> entry) {
        super();
        this.hash = entry.hash;
        this.entry = entry;
        this.entries = null;
    }


    NodeData(final Entry<K,V>[] entries) {
        super();
        this.hash = entries[0].hash;
        this.entry = null;
        this.entries = entries;
    }




    int size() {
        return (this.entry == null) ? this.entries.length : 1;
    }




    NodeData<K,V> put(final Entry<K,V> newEntry) {

        if (this.entry != null) {
            // This is single-valued

            if (this.entry.key == newEntry.key && this.entry.value == newEntry.value) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                return this;
            }
            if (Objects.equals(this.entry.key, newEntry.key)) {
                // We are replacing the previous value for a new one
                return new NodeData<>(newEntry);
            }
            // There is an hash collision, but this is a different slot, so we need to go multi value
            final Entry<K,V>[] newEntries = new Entry[] { this.entry, newEntry};
            return new NodeData<>(newEntries);

        }

        // TODO We should improve this to avoid linear performance depending on the amount of collisions. This was also fixed in HashMap in Java 8 to avoid DoS

        int pos = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (Objects.equals(this.entries[i].key, newEntry.key)) {
                pos = i;
                break;
            }
        }
        if (pos >= 0) {
            if (this.entries[pos].key == newEntry.key && this.entries[pos].value == newEntry.value) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                // Note this will only happen if key and value are actually the same object, not by object equality
                return this;
            }
            final Entry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
            newEntries[pos] = newEntry;
            return new NodeData<>(newEntries);
        }

        final Entry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
        newEntries[this.entries.length] = newEntry;
        return new NodeData<>(newEntries);

    }




    NodeData<K,V> remove(final Object key) {

        if (this.entry != null) {
            // This is single-valued
            return Objects.equals(this.entry.key,key) ? null : this;
        }

        int pos = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (Objects.equals(this.entries[i].key, key)) {
                pos = i;
                break;
            }
        }
        if (pos >= 0) {
            if (this.entries.length == 2) {
                // There are only two items in the multi value, and we are removing one, so now its single value
                final Entry<K,V> remainingEntry = this.entries[pos == 0? 1 : 0];
                return new NodeData<>(remainingEntry);
            }
            final Entry<K,V>[] newEntries = new Entry[this.entries.length - 1];
            System.arraycopy(this.entries, 0, newEntries, 0, pos);
            System.arraycopy(this.entries, pos + 1, newEntries, pos, (this.entries.length - (pos + 1)));
            return new NodeData<>(newEntries);
        }

        return this;

    }




}
