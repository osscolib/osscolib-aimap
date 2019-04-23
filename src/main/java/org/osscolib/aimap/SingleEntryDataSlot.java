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
import java.util.Map;

import org.osscolib.aimap.IndexedMap.DataSlot;
import org.osscolib.aimap.IndexedMap.Visitor;

final class SingleEntryDataSlot<K,V> implements DataSlot<K,V> {

    private final Map.Entry<K,V> entry;




    SingleEntryDataSlot(final Map.Entry<K,V> entry) {
        super();
        this.entry = entry;
    }


    @Override
    public int size() {
        return 1;
    }


    @Override
    public boolean containsKey(final Object key) {
        return this.entry.getKey().equals(key);
    }


    @Override
    public V get(final Object key) {
        if (this.entry.getKey().equals(key)) {
            return this.entry.getValue();
        }
        return null;
    }


    @Override
    public DataSlot<K,V> put(final Map.Entry<K,V> entry) {

        if (this.entry.getKey() == entry.getKey() && this.entry.getValue() == entry.getValue()) {
            // No need to perform any modifications, we might avoid a rewrite of a tree path!
            return this;
        }
        if (this.entry.getKey().equals(entry.getKey())) {
            // We are replacing the previous value for a new one
            return DataSlotBuilder.build(entry);
        }
        // TODO We should improve this to avoid linear performance depending on the amount of collisions. This was also fixed in HashMap in Java 8 to avoid DoS
        // There is an index collision, but this is a different slot, so we need to go multi value
        final Map.Entry<K,V>[] newEntries = new Map.Entry[] { this.entry, entry};
        return DataSlotBuilder.build(newEntries);

    }



    @Override
    public DataSlot<K,V> remove(final Object key) {

        if (!this.entry.getKey().equals(key)) {
            return this;
        }
        return null;

    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitDataSlot(Collections.singletonList(this.entry));
    }

}
