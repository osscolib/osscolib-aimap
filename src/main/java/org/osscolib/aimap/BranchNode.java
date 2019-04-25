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

import java.util.Arrays;

final class BranchNode<K,V> implements Node<K,V> {

    final long indexLowLimit;
    final long indexHighLimit;
    final long rangePerChild;
    final int maxNodeSize;

    final int childrenSize; // not "childrenLen", this is the amount of non-null children
    final Node<K,V>[] children; // can contain many nulls




    BranchNode(
            final long indexLowLimit, final long indexHighLimit, final long rangePerChild,
            final int maxNodeSize, final int childrenSize, final Node<K,V>[] children) {
        super();
        this.indexLowLimit = indexLowLimit;
        this.indexHighLimit = indexHighLimit;
        this.rangePerChild = rangePerChild;
        this.maxNodeSize = maxNodeSize;
        this.childrenSize = childrenSize;
        this.children = children;
    }



    @Override
    public long getIndexLowLimit() {
        return this.indexLowLimit;
    }

    @Override
    public long getIndexHighLimit() {
        return this.indexHighLimit;
    }

    @Override
    public int size() {
        int size = 0;
        Node<K,V> child;
        for (int i = 0; i < this.children.length; i++) {
            child = this.children[i];
            if (child != null) {
                size += child.size();
            }
        }
        return size;
    }


    @Override
    public boolean containsKey(final long index, final Object key) {
        final int pos = Utils.computeChildPos(this.indexLowLimit, this.rangePerChild, index);
        final Node<K,V> child = this.children[pos];
        return child != null && child.containsKey(index, key);
    }


    @Override
    public V get(final long index, final Object key) {
        final int pos = Utils.computeChildPos(this.indexLowLimit, this.rangePerChild, index);
        final Node<K,V> child = this.children[pos];
        return (child != null ? child.get(index, key) : null);
    }


    @Override
    public Node<K,V> put(final long index, final Entry<K,V> entry) {

        final int pos = Utils.computeChildPos(this.indexLowLimit, this.rangePerChild, index);
        final Node<K,V> child = this.children[pos];

        if (child != null) {

            final Node<K,V> newNode = child.put(index, entry);

            if (newNode == child) {
                return this;
            }

            final Node<K,V>[] newNodes = Arrays.copyOf(this.children, this.children.length);
            newNodes[pos] = newNode;

            return NodeBuilder.build(
                    this.indexLowLimit, this.indexHighLimit, this.rangePerChild,
                    this.maxNodeSize, this.childrenSize, newNodes);

        }

        // Nothing currently in the selected slot, so let's add a new DataSlot
        final DataSlot<K,V> newDataSlot = DataSlotBuilder.build(index, entry);
        return NodeBuilder.build(
                this.indexLowLimit, this.indexHighLimit, this.rangePerChild,
                this.maxNodeSize, this.childrenSize, this.children, newDataSlot);

    }


    @Override
    public Node<K,V> remove(final long index, final Object key) {

        final int pos = Utils.computeChildPos(this.indexLowLimit, this.rangePerChild, index);
        final Node<K,V> child = this.children[pos];

        if (child == null) {
            // Not found
            return this;
        }

        final Node<K,V> newChild = child.remove(index, key);
        if (newChild == child) {
            return this;
        }

        if (newChild == null && this.childrenSize == 1) {
            // This branch has become empty
            return null;
        }

        // Note newChild can still be null here
        final Node<K,V>[] newChildren = Arrays.copyOf(this.children, this.children.length);
        newChildren[pos] = newChild;

        final int newChildrenSize = (newChild == null? this.childrenSize - 1 : this.childrenSize);

        return NodeBuilder.build(
                this.indexLowLimit, this.indexHighLimit, this.rangePerChild,
                this.maxNodeSize, newChildrenSize, newChildren);

    }


    @Override
    public void acceptVisitor(final IndexMapVisitor<K,V> visitor) {
        visitor.visitBranchNode(this.indexLowLimit, this.indexHighLimit, Arrays.asList(this.children));
    }

}
