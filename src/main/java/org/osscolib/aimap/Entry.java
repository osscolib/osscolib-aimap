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

import java.util.Map;
import java.util.Objects;

final class Entry<K,V> implements Map.Entry<K,V> {

    private final K key;
    private final V value;

    static <K,V> Entry<K,V> build(final K key, final V value) {
        if (key == null) {
            throw new IllegalArgumentException("Null keys are forbidden");
        }
        return new Entry(key, value);
    }

    private Entry(final K key, final V value) {
        super();
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return this.key;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public Object setValue(final Object value) {
        // (Not) implemented, allowed at the java.util.Map.Entry specification
        throw new UnsupportedOperationException("Setting values is forbidden in this implementation");
    }

    @Override
    public boolean equals(final Object o) {
        // Implemented according to the java.util.Map.Entry specification
        if (o == this) {
            return true;
        }
        if (o instanceof Map.Entry) {
            final Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            if (Objects.equals(this.key, e.getKey()) &&
                    Objects.equals(this.value, e.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        // Implemented according to the java.util.Map.Entry specification
        return Objects.hashCode(this.key) ^ Objects.hashCode(this.value);
    }

}
