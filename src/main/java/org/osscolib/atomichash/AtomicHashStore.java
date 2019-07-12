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
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;


public class AtomicHashStore<K,V> implements Iterable<AtomicHashStore.Entry<K,V>>, Serializable {

    private static final long serialVersionUID = 6362537038828380833L;
    private static final AtomicHashStore INSTANCE = new AtomicHashStore<>();

    final Node<K,V> root;



    public AtomicHashStore() {
        this(null);
    }


    private AtomicHashStore(final Node<K,V> root) {
        super();
        this.root = root;
    }



    public int size() {
        if (this.root == null) {
            return 0;
        }
        return this.root.size();
    }




    public boolean isEmpty() {
        return this.root == null;
    }



    public boolean containsKey(final Object key) {
        return getEntry(key, this.root) != null;
    }


    public boolean containsValue(final Object value) {
        // Using an iterator here is actually not a bad-performing option, as we cannot do random access on values
        final Iterators.ValueIterator valueIterator = new Iterators.ValueIterator(this.root);
        while (valueIterator.hasNext()) {
            if (eq(valueIterator.next(), value)) {
                return true;
            }
        }
        return false;
    }



    public V get(final Object key) {
        final HashEntry<K,V> entry = getEntry(key, this.root);
        return entry != null ? entry.value : null;
    }


    public V getOrDefault(final Object key, final V defaultValue) {
        final HashEntry<K,V> entry = getEntry(key, this.root);
        return entry != null ? entry.value : defaultValue;
    }



    static <K,V> HashEntry<K,V> getEntry(final Object key, final Node<K,V> root) {

        final int hash = HashEntry.hash(key);

        Node<K,V> node = root;
        Node<K,V>[] children;

        for (Level level = Level.LEVEL0; node != null && (children = node.children) != null; level = level.next) {
            node = children[level.pos(hash)];
        }

        if (node == null) {
            return null;
        }

        final NodeData<K,V> data = node.data;

        if (data.hash != hash) {
            return null;
        }

        final HashEntry<K,V> e = data.entry;
        if (e != null) {
            return eq(e.key, key) ? e : null;
        }

        final HashEntry<K,V>[] es = data.entries;
        for (int i = 0; i < es.length; i++) {
            // TODO Performance degradation with large number of collisions -> adopt some kind of tree?
            if (eq(es[i].key, key)) {
                return es[i];
            }
        }

        return null;

    }



    public AtomicHashStore<K,V> put(final K key, final V value) {
        return put(key, value, null);
    }


    public AtomicHashStore<K,V> put(final K key, final V value, final Consumer<V> oldValueConsumer) {

        final HashEntry<K,V> entry = new HashEntry(key, value);

        final Node<K,V> newRoot;
        if (this.root == null) {

            final NodeData<K,V> newData = new NodeData<>(entry);
            newRoot = new Node<>(newData);

        } else {

            newRoot = this.root.put(Level.LEVEL0, entry, oldValueConsumer);
            if (this.root == newRoot) {
                return this;
            }

        }

        return new AtomicHashStore<>(newRoot);

    }






    public AtomicHashStore<K,V> putAll(final Map<? extends K, ? extends V> map) {

        Objects.requireNonNull(map);

        final int mapSize = map.size();
        if (mapSize == 0) {
            return this;
        }

        final Iterator<? extends Map.Entry<? extends K,? extends V>> mapIter = map.entrySet().iterator();

        if (mapSize == 1) {
            final Map.Entry<? extends K, ? extends V> singleEntry = mapIter.next();
            return put(singleEntry.getKey(), singleEntry.getValue());
        }

        Map.Entry<? extends K, ? extends V> mapEntry;
        final HashEntry<K,V>[] entries = new HashEntry[mapSize];
        for (int i = 0; i < entries.length; i++) {
            mapEntry = mapIter.next();
            entries[i] = new HashEntry<>(mapEntry.getKey(), mapEntry.getValue());
        }

        Arrays.sort(entries);

        return putAll(entries);

    }


    private AtomicHashStore<K,V> putAll(final HashEntry<K,V>[] orderedEntries) {

        if (orderedEntries.length == 0) {
            return this;
        }

        int start = 0;
        Node<K,V> newRoot = this.root;

        if (newRoot == null) {
            final NodeData<K,V> newData = new NodeData<>(orderedEntries[0]);
            newRoot = new Node<>(newData);
            start = 1;
        }

        newRoot = newRoot.putAll(Level.LEVEL0, orderedEntries, start, orderedEntries.length);
        return new AtomicHashStore<>(newRoot);

    }




    public AtomicHashStore<K,V> remove(final Object key) {
        return remove(key, null);
    }


    public AtomicHashStore<K,V> remove(final Object key, final Consumer<V> oldValueConsumer) {

        if (this.root == null) {
            return this;
        }

        final Node newRoot = this.root.remove(Level.LEVEL0, HashEntry.hash(key), key, oldValueConsumer);
        if (this.root == newRoot) {
            return this;
        }

        if (newRoot == null) {
            return INSTANCE;
        }

        return new AtomicHashStore<K,V>(newRoot);

    }




    public AtomicHashStore<K,V> remove(final Object key, final Object value) {
        return remove(key, value, null);
    }


    public AtomicHashStore<K,V> remove(final Object key, final Object value, final Consumer<Boolean> successConsumer) {

        final HashEntry<K,V> entry = getEntry(key, this.root);
        if (entry == null || !eq(entry.value, value)) {
            if (successConsumer != null) {
                successConsumer.accept(Boolean.FALSE);
            }
            return this;
        }
        if (successConsumer != null) {
            successConsumer.accept(Boolean.TRUE);
        }
        return remove(key);

    }




    public void forEach(final BiConsumer<? super K, ? super V> action) {
        // NOTE There will be an additional forEach function (based on Entry) coming from Iterable interface
        // This method returns void because that's what forEach is meant to return, and this is consistent with the
        // other (Iterable's) forEach.
        Objects.requireNonNull(action);
        HashEntry<K,V> entry;
        final Iterator<Entry<K,V>> iter = iterator();
        while (iter.hasNext()) {
            entry = (HashEntry<K,V>) iter.next();
            action.accept(entry.key, entry.value);
        }
    }




    public AtomicHashStore<K,V> replace(final K key, final V value) {
        return replace(key, value, (Consumer<V>)null);
    }


    public AtomicHashStore<K,V> replace(final K key, final V value, final Consumer<V> oldValueConsumer) {
        final HashEntry<K,V> entry = getEntry(key, this.root);
        if (entry == null) {
            if (oldValueConsumer != null) {
                oldValueConsumer.accept(null);
            }
            return this;
        }
        return put(key, value, oldValueConsumer);
    }




    public AtomicHashStore<K,V> replace(final K key, final V oldValue, final V newValue) {
        return replace(key, oldValue, newValue, null);
    }


    public AtomicHashStore<K,V> replace(final K key, final V oldValue, final V newValue, final Consumer<Boolean> successConsumer) {
        final HashEntry<K,V> entry = getEntry(key, this.root);
        if (entry == null || !eq(entry.value, oldValue)) {
            if (successConsumer != null) {
                successConsumer.accept(Boolean.FALSE);
            }
            return this;
        }
        if (successConsumer != null) {
            successConsumer.accept(Boolean.TRUE);
        }
        return put(key, newValue);
    }




    public <W> AtomicHashStore<K,W> replaceAll(final BiFunction<? super K, ? super V, ? extends W> function) {
        // NOTE that, as a difference with Map#replaceAll, in this case we can have our "action" function
        // change the type of the values because we will be returning a different AtomicHashStore instance.

        Objects.requireNonNull(function);

        HashEntry<K,V> entry;
        final Iterator<Entry<K,V>> iter = iterator();

        // The newly created array will still be considered to be ordered because keys won't change, and Entry
        // ordering (Entry#compareTo()) is based entirely on keys.
        final HashEntry<K,W>[] newOrderedEntries = new HashEntry[size()];
        for (int i = 0; i < newOrderedEntries.length; i++) {
            entry = (HashEntry<K,V>) iter.next();
            newOrderedEntries[i] = new HashEntry<>(entry.key, function.apply(entry.key, entry.value));
        }

        final AtomicHashStore<K,W> store = of();
        return store.putAll(newOrderedEntries);

    }




    public AtomicHashStore<K,V> putIfAbsent(final K key, final V value) {
        return putIfAbsent(key, value, null);
    }


    public AtomicHashStore<K,V> putIfAbsent(final K key, final V value, final Consumer<V> oldValueConsumer) {
        // This is implemented according to the spec of Map#putIfAbsent(), but in order to keep streaming API
        // capabilities, a consumer can be specified for what the equivalent method in java.util.Map would return.
        final V oldValue = get(key);
        if (oldValueConsumer != null) {
            oldValueConsumer.accept(oldValue);
        }
        if (oldValue == null) {
            return put(key, value);
        }
        return this;
    }




    public AtomicHashStore<K,V> computeIfAbsent(
            final K key, final Function<? super K, ? extends V> mappingFunction) {
        return computeIfAbsent(key, mappingFunction, null);
    }


    public AtomicHashStore<K,V> computeIfAbsent(
                final K key, final Function<? super K, ? extends V> mappingFunction, final Consumer<V> valueConsumer) {
        // This is implemented according to the spec of Map#computeIfAbsent(), but in order to keep streaming API
        // capabilities, a consumer can be specified for what the equivalent method in java.util.Map would return.
        Objects.requireNonNull(mappingFunction);
        final V oldValue = get(key);
        final V value = (oldValue != null) ? oldValue : mappingFunction.apply(key);
        if (valueConsumer != null) {
            valueConsumer.accept(value);
        }
        if (oldValue != value) {
            return put(key, value);
        }
        return this;
    }




    public AtomicHashStore<K,V> computeIfPresent(
            final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return computeIfPresent(key, remappingFunction, null);
    }


    public AtomicHashStore<K,V> computeIfPresent(
            final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction,
            final Consumer<V> valueConsumer) {
        // This is implemented according to the spec of Map#computeIfPresent(), but in order to keep streaming API
        // capabilities, a consumer can be specified for what the equivalent method in java.util.Map would return.
        Objects.requireNonNull(remappingFunction);
        final V oldValue = get(key);
        if (oldValue != null) {
            final V newValue = remappingFunction.apply(key, oldValue);
            if (valueConsumer != null) {
                valueConsumer.accept(newValue);
            }
            if (newValue != null) {
                return put(key, newValue);
            }
            return remove(key);
        }
        if (valueConsumer != null) {
            valueConsumer.accept(null);
        }
        return this;
    }




    public AtomicHashStore<K,V> compute(
            final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return compute(key, remappingFunction, null);
    }

    public AtomicHashStore<K,V> compute(
            final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction,
            final Consumer<V> valueConsumer) {
        // This is implemented according to the spec of Map#compute(), but in order to keep streaming API
        // capabilities, a consumer can be specified for what the equivalent method in java.util.Map would return.

        Objects.requireNonNull(remappingFunction);

        final HashEntry<K,V> entry = getEntry(key, this.root);
        final V oldValue = (entry != null) ? entry.value : null;

        final V newValue = remappingFunction.apply(key, oldValue);

        if (valueConsumer != null) {
            valueConsumer.accept(newValue);
        }

        if (newValue == null) {
            if (entry != null) {
                return remove(key);
            }
            return this;
        }
        return put(key, newValue);

    }




    public AtomicHashStore<K,V> merge(
            final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return merge(key, value, remappingFunction, null);
    }


    public AtomicHashStore<K,V> merge(
            final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction,
            final Consumer<V> valueConsumer) {
        // This is implemented according to the spec of Map#merge(), but in order to keep streaming API
        // capabilities, a consumer can be specified for what the equivalent method in java.util.Map would return.

        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);

        final V oldValue = get(key);
        final V newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);

        if (valueConsumer != null) {
            valueConsumer.accept(newValue);
        }

        if (newValue == null) {
            return remove(key);
        }

        return put(key, newValue);

    }




    public AtomicHashStore<K,V> clear() {
        return of();
    }



    @Override
    public Iterator<Entry<K,V>> iterator() {
        return new Iterators.StoreEntryIterator<>(this.root);
    }




    /**
     * Equivalent to Objects.equals(), but by being called only from
     * HashEntry we might benefit from runtime profile information on the
     * type of o1. See java.util.AbstractMap#eq().
     *
     * Do not replace with Object.equals until JDK-8015417 is resolved.
     */
    private static boolean eq(final Object o1, final Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }




    @Override
    public boolean equals(final Object o) {

        if (o == this) {
            return true;
        }

        if (!(o instanceof AtomicHashStore)) {
            return false;
        }

        final AtomicHashStore<?,?> other = (AtomicHashStore<?,?>) o;

        if (this.root == null) {
            return other.root == null;
        } else if (other.root == null) {
            return false;
        }

        final Iterator<Entry<K,V>> thisIter = this.iterator();

        int count = 0;
        HashEntry<K,V> thisEntry;
        HashEntry<?,?> otherEntry;
        while (thisIter.hasNext()) {

            thisEntry = (HashEntry<K,V>) thisIter.next();
            otherEntry = getEntry(thisEntry.key, other.root);

            if (otherEntry == null) {
                return false;
            }

            if (!eq(thisEntry.key, otherEntry.key) || !eq(thisEntry.value, otherEntry.value)) {
                return false;
            }

            count++;

        }

        return count == other.size();

    }


    @Override
    public int hashCode() {
        int h = 0;
        for (final Entry<K, V> entry : this) {
            h += entry.hashCode(); // HashEntry#hashCode() is properly implemented
        }
        return h;
    }




    public static <K,V> AtomicHashStore<K, V> of() {
        return INSTANCE;
    }


    public static <K, V> AtomicHashStore<K, V> of(
            final K k1, final V v1) {

        final AtomicHashStore<K,V> base = of();
        return base.put(k1, v1);

    }


    public static <K,V> AtomicHashStore<K, V> of(
            final K k1, final V v1, final K k2, final V v2) {

        final HashEntry<K,V>[] entries = new HashEntry[2];
        entries[0] = new HashEntry<>(k1,v1);
        entries[1] = new HashEntry<>(k2,v2);

        Arrays.sort(entries);

        final AtomicHashStore<K,V> base = of();
        return base.putAll(entries);

    }


    public static <K,V> AtomicHashStore<K, V> of(
            final K k1, final V v1, final K k2, final V v2, final K k3, final V v3) {

        final HashEntry<K,V>[] entries = new HashEntry[3];
        entries[0] = new HashEntry<>(k1,v1);
        entries[1] = new HashEntry<>(k2,v2);
        entries[2] = new HashEntry<>(k3,v3);

        Arrays.sort(entries);

        final AtomicHashStore<K,V> base = of();
        return base.putAll(entries);

    }


    public static <K,V> AtomicHashStore<K, V> of(
            final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4) {

        final HashEntry<K,V>[] entries = new HashEntry[4];
        entries[0] = new HashEntry<>(k1,v1);
        entries[1] = new HashEntry<>(k2,v2);
        entries[2] = new HashEntry<>(k3,v3);
        entries[3] = new HashEntry<>(k4,v4);

        Arrays.sort(entries);

        final AtomicHashStore<K,V> base = of();
        return base.putAll(entries);

    }


    public static <K,V> AtomicHashStore<K, V> of(
            final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4,
            final K k5, final V v5) {

        final HashEntry<K,V>[] entries = new HashEntry[5];
        entries[0] = new HashEntry<>(k1,v1);
        entries[1] = new HashEntry<>(k2,v2);
        entries[2] = new HashEntry<>(k3,v3);
        entries[3] = new HashEntry<>(k4,v4);
        entries[4] = new HashEntry<>(k5,v5);

        Arrays.sort(entries);

        final AtomicHashStore<K,V> base = of();
        return base.putAll(entries);

    }


    public static <K,V> AtomicHashStore<K, V> of(
            final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4,
            final K k5, final V v5, final K k6, final V v6) {

        final HashEntry<K,V>[] entries = new HashEntry[6];
        entries[0] = new HashEntry<>(k1,v1);
        entries[1] = new HashEntry<>(k2,v2);
        entries[2] = new HashEntry<>(k3,v3);
        entries[3] = new HashEntry<>(k4,v4);
        entries[4] = new HashEntry<>(k5,v5);
        entries[5] = new HashEntry<>(k6,v6);

        Arrays.sort(entries);

        final AtomicHashStore<K,V> base = of();
        return base.putAll(entries);

    }


    public static <K,V> AtomicHashStore<K, V> of(
            final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4,
            final K k5, final V v5, final K k6, final V v6, final K k7, final V v7) {

        final HashEntry<K,V>[] entries = new HashEntry[7];
        entries[0] = new HashEntry<>(k1,v1);
        entries[1] = new HashEntry<>(k2,v2);
        entries[2] = new HashEntry<>(k3,v3);
        entries[3] = new HashEntry<>(k4,v4);
        entries[4] = new HashEntry<>(k5,v5);
        entries[5] = new HashEntry<>(k6,v6);
        entries[6] = new HashEntry<>(k7,v7);

        Arrays.sort(entries);

        final AtomicHashStore<K,V> base = of();
        return base.putAll(entries);

    }


    public static <K,V> AtomicHashStore<K, V> of(
            final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4,
            final K k5, final V v5, final K k6, final V v6, final K k7, final V v7, final K k8, final V v8) {

        final HashEntry<K,V>[] entries = new HashEntry[8];
        entries[0] = new HashEntry<>(k1,v1);
        entries[1] = new HashEntry<>(k2,v2);
        entries[2] = new HashEntry<>(k3,v3);
        entries[3] = new HashEntry<>(k4,v4);
        entries[4] = new HashEntry<>(k5,v5);
        entries[5] = new HashEntry<>(k6,v6);
        entries[6] = new HashEntry<>(k7,v7);
        entries[7] = new HashEntry<>(k8,v8);

        Arrays.sort(entries);

        final AtomicHashStore<K,V> base = of();
        return base.putAll(entries);

    }


    public static <K,V> AtomicHashStore<K, V> of(
            final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4,
            final K k5, final V v5, final K k6, final V v6, final K k7, final V v7, final K k8, final V v8,
            final K k9, final V v9) {

        final HashEntry<K,V>[] entries = new HashEntry[9];
        entries[0] = new HashEntry<>(k1,v1);
        entries[1] = new HashEntry<>(k2,v2);
        entries[2] = new HashEntry<>(k3,v3);
        entries[3] = new HashEntry<>(k4,v4);
        entries[4] = new HashEntry<>(k5,v5);
        entries[5] = new HashEntry<>(k6,v6);
        entries[6] = new HashEntry<>(k7,v7);
        entries[7] = new HashEntry<>(k8,v8);
        entries[8] = new HashEntry<>(k9,v9);

        Arrays.sort(entries);

        final AtomicHashStore<K,V> base = of();
        return base.putAll(entries);

    }


    public static <K,V> AtomicHashStore<K, V> of(
            final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4,
            final K k5, final V v5, final K k6, final V v6, final K k7, final V v7, final K k8, final V v8,
            final K k9, final V v9, final K k10, final V v10) {

        final HashEntry<K,V>[] entries = new HashEntry[10];
        entries[0] = new HashEntry<>(k1,v1);
        entries[1] = new HashEntry<>(k2,v2);
        entries[2] = new HashEntry<>(k3,v3);
        entries[3] = new HashEntry<>(k4,v4);
        entries[4] = new HashEntry<>(k5,v5);
        entries[5] = new HashEntry<>(k6,v6);
        entries[6] = new HashEntry<>(k7,v7);
        entries[7] = new HashEntry<>(k8,v8);
        entries[8] = new HashEntry<>(k9,v9);
        entries[9] = new HashEntry<>(k10,v10);

        Arrays.sort(entries);

        final AtomicHashStore<K,V> base = of();
        return base.putAll(entries);

    }


    public static <K,V> AtomicHashStore<K, V> ofEntries(final Entry<? extends K, ? extends V>... entries) {

        if (entries.length == 0) {
            return of();
        }

        if (entries.length == 1) {
            return of(entries[0].getKey(), entries[0].getValue());
        }

        final HashEntry<K,V>[] entriesArr = new HashEntry[entries.length];
        Entry<? extends K,? extends V> entry;
        for (int i = 0; i < entries.length; i++) {
            entry = entries[i];
            if (entry instanceof HashEntry) {
                entriesArr[i] = (HashEntry<K,V>) entry;
            } else {
                entriesArr[i] = new HashEntry<>(entry.getKey(), entry.getValue());
            }
        }

        Arrays.sort(entriesArr);

        final AtomicHashStore<K,V> base = of();
        return base.putAll(entriesArr);

    }




    public static <K,V> Entry<K,V> entry(final K key, final V value) {
        return new HashEntry<>(key, value);
    }



    public interface Entry<K,V> extends Map.Entry<K,V> {
        // Nothing to be added to the Map.Entry interface. This is just meant to make
        // the API of the AtomicHashStore independent from Map.
    }

}

