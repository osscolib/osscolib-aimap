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
