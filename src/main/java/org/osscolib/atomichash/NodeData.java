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
import java.util.function.Consumer;

final class NodeData<K,V> implements Serializable {

    private static final long serialVersionUID = -3351803095783536070L;

    final int hash;
    final HashEntry<K,V> entry;
    final HashEntry<K,V>[] entries;



    NodeData(final HashEntry<K,V> entry) {
        super();
        this.hash = entry.hash;
        this.entry = entry;
        this.entries = null;
    }


    NodeData(final HashEntry<K,V>[] entries) {
        super();
        this.hash = entries[0].hash;
        this.entry = null;
        this.entries = entries;
    }




    int size() {
        return (this.entry == null) ? this.entries.length : 1;
    }




    NodeData<K,V> put(final HashEntry<K,V> newEntry, final Consumer<V> oldValueConsumer) {

        if (this.entry != null) {
            // This is single-valued

            if (this.entry.key == newEntry.key && this.entry.value == newEntry.value) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                if (oldValueConsumer != null) {
                    oldValueConsumer.accept(this.entry.value);
                }
                return this;
            }

            if (Objects.equals(this.entry.key, newEntry.key)) {
                // We are replacing the previous value for a new one
                if (oldValueConsumer != null) {
                    oldValueConsumer.accept(this.entry.value);
                }
                return new NodeData<>(newEntry);
            }

            if (oldValueConsumer != null) {
                oldValueConsumer.accept(null);
            }

            // There is an hash collision, but this is a different slot, so we need to go multi value
            final HashEntry<K,V>[] newEntries = new HashEntry[] { this.entry, newEntry};

            // We will keep this array sorted in order to ease searches in large multi-valued nodes
            Arrays.sort(newEntries);

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

            if (oldValueConsumer != null) {
                oldValueConsumer.accept(this.entries[pos].value);
            }

            if (this.entries[pos].key == newEntry.key && this.entries[pos].value == newEntry.value) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                // Note this will only happen if key and value are actually the same object, not by object equality
                return this;
            }

            final HashEntry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
            newEntries[pos] = newEntry;

            // We will keep this array sorted in order to ease searches in large multi-valued nodes
            Arrays.sort(newEntries);

            return new NodeData<>(newEntries);

        }

        if (oldValueConsumer != null) {
            oldValueConsumer.accept(null);
        }

        final HashEntry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
        newEntries[this.entries.length] = newEntry;

        // We will keep this array sorted in order to ease searches in large multi-valued nodes
        Arrays.sort(newEntries);

        return new NodeData<>(newEntries);

    }




    NodeData<K,V> remove(final Object key, final Consumer<V> oldValueConsumer) {

        if (this.entry != null) {
            // This is single-valued

            if (Objects.equals(this.entry.key,key)) {
                if (oldValueConsumer != null) {
                    oldValueConsumer.accept(this.entry.value);
                }
                return null;
            }

            if (oldValueConsumer != null) {
                oldValueConsumer.accept(null);
            }
            return this;

        }

        int pos = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (Objects.equals(this.entries[i].key, key)) {
                pos = i;
                break;
            }
        }

        if (pos >= 0) {

            if (oldValueConsumer != null) {
                oldValueConsumer.accept(this.entries[pos].value);
            }

            if (this.entries.length == 2) {
                // There are only two items in the multi value, and we are removing one, so now its single value
                final HashEntry<K,V> remainingEntry = this.entries[pos == 0? 1 : 0];
                return new NodeData<>(remainingEntry);
            }

            final HashEntry<K,V>[] newEntries = new HashEntry[this.entries.length - 1];
            System.arraycopy(this.entries, 0, newEntries, 0, pos);
            System.arraycopy(this.entries, pos + 1, newEntries, pos, (this.entries.length - (pos + 1)));

            return new NodeData<>(newEntries);

        }

        if (oldValueConsumer != null) {
            oldValueConsumer.accept(null);
        }

        return this;

    }

}
