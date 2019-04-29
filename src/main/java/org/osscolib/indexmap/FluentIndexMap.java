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

import java.util.function.ToIntFunction;


public final class FluentIndexMap<K,V> implements IndexMap<K,V> {

    private final int maskSize;
    private final ToIntFunction<Object> indexFunction;
    private final Node<K,V> root;



    FluentIndexMap(final int maskSize, final ToIntFunction<Object> indexFunction, final Node<K,V> root) {
        super();
        this.maskSize = maskSize;
        this.indexFunction = indexFunction;
        this.root = root;
    }



    @Override
    public ToIntFunction<Object> getIndexFunction() {
        return this.indexFunction;
    }


    @Override
    public int size() {
        if (this.root == null) {
            return 0;
        }
        return this.root.size();
    }



    @Override
    public boolean containsKey(final Object key) {

        final int index = computeIndex(key);

        Node<K,V> node = this.root;
        while (node != null && node.branch) {
            node = node.child(index);
        }

        return (node != null && node.dataSlotIndex == index) ? node.dataSlot.containsKey(index, key) : null;

    }


    @Override
    public V get(final Object key) {

        final int index = computeIndex(key);

        Node<K,V> node = this.root;
        while (node != null && node.branch) {
            node = node.child(index);
        }

        return (node != null && node.dataSlotIndex == index) ? node.dataSlot.get(index, key) : null;

    }


    public FluentIndexMap<K,V> put(final K key, final V value) {

        final int index = computeIndex(key);
        final Entry entry = Entry.build(key, value);

        final Node newRoot;
        if (this.root == null) {

            final DataSlot<K,V> newDataSlot = DataSlotBuilder.build(index, entry);
            newRoot = NodeBuilder.build(0, this.maskSize, index, newDataSlot);

        } else {

            newRoot = this.root.put(index, entry);
            if (this.root == newRoot) {
                return this;
            }

        }

        return new FluentIndexMap<K,V>(this.maskSize, this.indexFunction, newRoot);

    }


    public FluentIndexMap<K,V> remove(final Object key) {

        if (this.root == null) {
            return this;
        }

        final Node newRoot = this.root.remove(computeIndex(key), key);
        if (this.root == newRoot) {
            return this;
        }

        return new FluentIndexMap<K,V>(this.maskSize, this.indexFunction, newRoot);

    }





    private int computeIndex(final Object key) {
        return this.indexFunction.applyAsInt(key);
    }


    String prettyPrint() {
        final IndexMapVisitor<K,V> visitor = new PrettyPrintIndexMapVisitor();
        visitor.visitRoot(this.root);
        return visitor.toString();
    }



}
