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
package org.osscolib.indexmap;

final class DataSlotBuilder {


    static <K,V> DataSlot<K,V> build(final int index, final Entry<K,V> entry) {
        return new SingleEntryDataSlot<>(index, entry);
    }


    static <K,V> DataSlot<K,V> build(final int index, final Entry<K,V>[] entries) {
        if (entries.length == 1) {
            return new SingleEntryDataSlot<>(index, entries[0]);
        }
        return new MultiEntryDataSlot<>(index, entries);
    }


    private DataSlotBuilder() {
        super();
    }

}
