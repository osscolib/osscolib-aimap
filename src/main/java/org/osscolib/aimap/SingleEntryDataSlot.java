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

import java.util.Collections;
import java.util.Objects;

final class SingleEntryDataSlot<K,V> implements DataSlot<K,V> {

    private final long index;
    private final Entry<K,V> entry;




    SingleEntryDataSlot(final long index, final Entry<K,V> entry) {
        super();
        this.index = index;
        this.entry = entry;
    }


    @Override
    public long getIndex() {
        return this.index;
    }


    @Override
    public int size() {
        return 1;
    }


    @Override
    public boolean containsKey(final long index, final Object key) {
        return Objects.equals(this.entry.key, key);
    }


    @Override
    public V get(final long index, final Object key) {
        return Objects.equals(this.entry.key, key) ? this.entry.value : null;
    }


    @Override
    public DataSlot<K,V> put(final long index, final Entry<K,V> newEntry) {

        if (this.entry.key == newEntry.key && this.entry.value == newEntry.value) {
            // No need to perform any modifications, we might avoid a rewrite of a tree path!
            return this;
        }
        if (Objects.equals(this.entry.key, newEntry.key)) {
            // We are replacing the previous value for a new one
            return DataSlotBuilder.build(index, newEntry);
        }
        // TODO We should improve this to avoid linear performance depending on the amount of collisions. This was also fixed in HashMap in Java 8 to avoid DoS
        // There is an index collision, but this is a different slot, so we need to go multi value
        final Entry<K,V>[] newEntries = new Entry[] { this.entry, newEntry};
        return DataSlotBuilder.build(index, newEntries);

    }



    @Override
    public DataSlot<K,V> remove(final long index, final Object key) {
        return Objects.equals(this.entry.key,key) ? null : this;
    }


    @Override
    public void acceptVisitor(final IndexMapVisitor<K,V> visitor) {
        visitor.visitDataSlot(this.index, Collections.singletonList(this.entry));
    }

}
