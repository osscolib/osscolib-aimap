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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;


public class AtomicHashStore<K,V> implements Iterable<Map.Entry<K,V>>, Serializable {

    private static final long serialVersionUID = 6362537038828380833L;

    final Node<K,V> root;

    transient Sets.StoreEntrySet<K,V> entrySet = null;
    transient Sets.StoreKeySet<K,V> keySet = null;
    transient Collections.StoreValueCollection<K,V> valueCollection = null;



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
            if (Objects.equals(valueIterator.next(), value)) {
                return true;
            }
        }
        return false;
    }



    public V get(final Object key) {
        final Entry<K,V> entry = getEntry(key, this.root);
        return entry != null ? entry.value : null;
    }


    public V getOrDefault(final Object key, final V defaultValue) {
        final Entry<K,V> entry = getEntry(key, this.root);
        return entry != null ? entry.value : defaultValue;
    }



    static <K,V> Entry<K,V> getEntry(final Object key, final Node<K,V> root) {

        final int hash = Entry.hash(key);

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

        final Entry<K,V> e = data.entry;
        if (e != null) {
            return Objects.equals(e.key, key) ? e : null;
        }

        final Entry<K,V>[] es = data.entries;
        for (int i = 0; i < es.length; i++) {
            // TODO Performance degradation with large number of collisions -> adopt some kind of tree?
            if (Objects.equals(es[i].key, key)) {
                return es[i];
            }
        }

        return null;

    }



    public AtomicHashStore<K,V> put(final K key, final V value) {
        return put(key, value, null);
    }


    public AtomicHashStore<K,V> put(final K key, final V value, final Consumer<V> oldValueConsumer) {

        final Entry<K,V> entry = new Entry(key, value);

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

        if (mapSize == 1) {
            final Map.Entry<? extends K, ? extends V> singleEntry = map.entrySet().iterator().next();
            return put(singleEntry.getKey(), singleEntry.getValue());
        }

        final Entry<K,V>[] orderedEntries =
                map.entrySet().stream()
                        .map(e -> new Entry<>(e.getKey(), e.getValue()))
                        .sorted()
                        .toArray(Entry[]::new);

        return putAll(orderedEntries);

    }


    private AtomicHashStore<K,V> putAll(final Entry<K,V>[] orderedEntries) {

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

        final Node newRoot = this.root.remove(Level.LEVEL0, Entry.hash(key), key, oldValueConsumer);
        if (this.root == newRoot) {
            return this;
        }

        return new AtomicHashStore<K,V>(newRoot);

    }




    public AtomicHashStore<K,V> remove(final K key, final V value) {
        return remove(key, value, null);
    }


    public AtomicHashStore<K,V> remove(final K key, final V value, final Consumer<Boolean> successConsumer) {

        final Entry<K,V> entry = getEntry(key, this.root);
        if (entry == null || !Objects.equals(entry.value, value)) {
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
        // NOTE There will be an additional forEach function (based on Map.Entry) coming from Iterable interface
        // This method returns void because that's what forEach is meant to return, and this is consistent with the
        // other (Iterable's) forEach.
        Objects.requireNonNull(action);
        Entry<K,V> entry;
        final Iterator<Map.Entry<K,V>> iter = iterator();
        while (iter.hasNext()) {
            entry = (Entry<K,V>) iter.next();
            action.accept(entry.key, entry.value);
        }
    }




    public AtomicHashStore<K,V> replace(final K key, final V value) {
        return replace(key, value, (Consumer<V>)null);
    }


    public AtomicHashStore<K,V> replace(final K key, final V value, final Consumer<V> oldValueConsumer) {
        final Entry<K,V> entry = getEntry(key, this.root);
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
        final Entry<K,V> entry = getEntry(key, this.root);
        if (entry == null || !Objects.equals(entry.value, oldValue)) {
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

        Entry<K,V> entry;
        final Iterator<Map.Entry<K,V>> iter = iterator();

        // The newly created array will still be considered to be ordered because keys won't change, and Entry
        // ordering (Entry#compareTo()) is based entirely on keys.
        final Entry<K,W>[] newOrderedEntries = new Entry[size()];
        for (int i = 0; i < newOrderedEntries.length; i++) {
            entry = (Entry<K,V>) iter.next();
            newOrderedEntries[i] = new Entry<>(entry.key, function.apply(entry.key, entry.value));
        }

        final AtomicHashStore<K,W> store = new AtomicHashStore<>();
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
            final K key, final Function<? super K, ? extends V> mappingFunction) {
        return computeIfPresent(key, mappingFunction, null);
    }


    public AtomicHashStore<K,V> computeIfPresent(
            final K key, final Function<? super K, ? extends V> mappingFunction, final Consumer<V> valueConsumer) {
        // This is implemented according to the spec of Map#computeIfPresent(), but in order to keep streaming API
        // capabilities, a consumer can be specified for what the equivalent method in java.util.Map would return.
        Objects.requireNonNull(mappingFunction);
        final V oldValue = get(key);
        if (oldValue != null) {
            final V newValue = mappingFunction.apply(key);
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

        final Entry<K,V> entry = getEntry(key, this.root);
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
        return new AtomicHashStore<>();
    }



    @Override
    public Iterator<Map.Entry<K,V>> iterator() {
        return new Iterators.EntryIterator<>(this.root);
    }



    public Set<Map.Entry<K,V>> entrySet() {
        Sets.StoreEntrySet<K,V> entrySet;
        if ((entrySet = this.entrySet) != null) {
            return entrySet;
        }
        this.entrySet = new Sets.StoreEntrySet<>(this);
        return this.entrySet;
    }


    public Set<K> keySet() {
        Sets.StoreKeySet<K,V> keySet;
        if ((keySet = this.keySet) != null) {
            return keySet;
        }
        this.keySet = new Sets.StoreKeySet<>(this);
        return this.keySet;
    }


    public Collection<V> values() {
        Collections.StoreValueCollection<K,V> valueCollection;
        if ((valueCollection = this.valueCollection) != null) {
            return valueCollection;
        }
        this.valueCollection = new Collections.StoreValueCollection<>(this);
        return this.valueCollection;
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

        final Iterator<Map.Entry<K,V>> thisIter = this.iterator();

        int count = 0;
        Entry<K,V> thisEntry;
        Entry<?,?> otherEntry;
        while (thisIter.hasNext()) {

            thisEntry = (Entry<K,V>) thisIter.next();
            otherEntry = getEntry(thisEntry.key, other.root);

            if (otherEntry == null) {
                return false;
            }

            if (!Objects.equals(thisEntry.key, otherEntry.key) || !Objects.equals(thisEntry.value, otherEntry.value)) {
                return false;
            }

            count++;

        }

        return count == other.size();

    }


    @Override
    public int hashCode() {
        int h = 0;
        for (final Map.Entry<K, V> entry : entrySet()) {
            h += entry.hashCode(); // Entry#hashCode() is properly implemented
        }
        return h;
    }

}

