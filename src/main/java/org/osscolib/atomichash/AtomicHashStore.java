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
import java.util.function.Consumer;


// TODO implement equals and hashCode()
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

        final Entry<K,V> entry = new Entry(key, value);

        final Node<K,V> newRoot;
        if (this.root == null) {

            final NodeData<K,V> newData = new NodeData<>(entry);
            newRoot = new Node<>(newData);

        } else {

            newRoot = this.root.put(Level.LEVEL0, entry);
            if (this.root == newRoot) {
                return this;
            }

        }

        return new AtomicHashStore<>(newRoot);

    }





    public AtomicHashStore<K,V> putIfAbsent(final K key, final V value) {
        return putIfAbsent(key, value, null);
    }


    public AtomicHashStore<K,V> putIfAbsent(final K key, final V value, final Consumer<V> oldValueConsumer) {
        // This is implemented according to the spec of Map#putIfAbsent(), which specifies that the new
        // entry should be established only if the key doesn't currently exist or is mapped to null.
        // Note however that Map#putIfAbsent() returns the old value associated with the key if there is
        // such value, and this method cannot do that in order to keep streaming API capabilities. Instead,
        // the oldValueConsumer will be called on the old value (be it null or non-null).
        final V oldValue = get(key);
        if (oldValueConsumer != null) {
            oldValueConsumer.accept(oldValue);
        }
        if (oldValue == null) {
            return put(key, value);
        }
        return this;
    }




    public AtomicHashStore<K,V> putAll(final Map<? extends K, ? extends V> map) {

        final int mapSize = map.size();
        if (mapSize == 0) {
            return this;
        }

        if (mapSize == 1) {
            final Map.Entry<? extends K, ? extends V> singleEntry = map.entrySet().iterator().next();
            return put(singleEntry.getKey(), singleEntry.getValue());
        }

        final Entry<K,V>[] entries =
                map.entrySet().stream()
                        .map(e -> new Entry<>(e.getKey(), e.getValue()))
                        .sorted()
                        .toArray(Entry[]::new);

        int start = 0;
        Node<K,V> newRoot = this.root;

        if (newRoot == null) {
            final NodeData<K,V> newData = new NodeData<>(entries[0]);
            newRoot = new Node<>(newData);
            start = 1;
        }

        newRoot = newRoot.putAll(Level.LEVEL0, entries, start, entries.length);
        return new AtomicHashStore<>(newRoot);

    }




    public AtomicHashStore<K,V> remove(final Object key) {

        if (this.root == null) {
            return this;
        }

        final Node newRoot = this.root.remove(Level.LEVEL0, Entry.hash(key), key);
        if (this.root == newRoot) {
            return this;
        }

        return new AtomicHashStore<K,V>(newRoot);

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


}
