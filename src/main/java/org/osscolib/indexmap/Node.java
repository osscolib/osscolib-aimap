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

import java.io.Serializable;
import java.util.Arrays;

final class Node<K,V> implements Serializable {

    private static final long serialVersionUID = 6914544628900109073L;

    private final int count;
    final NodeData<K,V> data;
    final Node<K,V>[] children; // can contain many nulls



    Node(final int count, final Node<K,V>[] children) {
        super();
        this.count = count;
        this.children = children;
        this.data = null;
    }


    Node(final NodeData<K,V> data) {
        super();
        this.count = 0;
        this.children = null;
        this.data = data;
    }




    int size() {

        if (this.data != null) {
            return this.data.size();
        }

        Node child;
        int size = 0;
        for (int i = 0; i < this.children.length; i++) {
            child = this.children[i];
            if (child != null) {
                size += child.size();
            }
        }
        return size;

    }


    static int pos(final int shift, final int mask, final int hash) {
        return (hash >> shift) & mask;
    }

    static int incShift(final int shift, final int mask) {
        final int maskSize = (31 - Integer.numberOfLeadingZeros(mask + 1)); // maskSize = log2(mask + 1)
        return shift + maskSize;
    }



    Node<K,V> put(final int hash, final int shift, final int mask, final Entry<K, V> entry) {

        if (this.data == null) {

            final int pos = pos(shift, mask, hash);
            final Node<K,V> child = this.children[pos];

            if (child != null) {

                final Node<K,V> newNode = child.put(hash, incShift(shift, mask), mask, entry);

                if (newNode == child) {
                    return this;
                }

                final Node<K,V>[] newNodes = Arrays.copyOf(this.children, this.children.length);
                newNodes[pos] = newNode;

                return new Node<>(this.count, newNodes);

            }

            // Nothing currently in the selected node, so let's add the new data
            final NodeData<K,V> newData = new NodeData<>(hash, entry);
            return NodeBuilder.build(shift, mask, this.count, this.children, newData);

        }

        // Not a branch -- this is a node with data

        if (this.data.hash == hash) {

            final NodeData<K,V> newData = this.data.put(hash, entry);
            if (newData == this.data) {
                // Nothing was added because the entry already existed
                return this;
            }

            return new Node<>(newData);

        }

        // We need to add a new node in the same range, so this has to be converted into a branch

        final NodeData<K,V> newData = new NodeData<>(hash, entry);
        return NodeBuilder.build(shift, mask, this.data, newData);

    }




    Node<K,V> remove(final int hash, final int shift, final int mask, final Object key) {

        if (this.data == null) {

            final int pos = pos(shift, mask, hash);
            final Node<K,V> child = this.children[pos];

            if (child == null) {
                // Not found
                return this;
            }

            final Node<K,V> newChild = child.remove(hash, incShift(shift, mask), mask, key);
            if (newChild == child) {
                return this;
            }

            if (newChild == null && this.count == 1) {
                // This branch has become empty
                return null;
            }

            // Note newChild can still be null here
            final Node<K,V>[] newChildren = Arrays.copyOf(this.children, this.children.length);
            newChildren[pos] = newChild;

            final int newChildrenSize = (newChild == null? this.count - 1 : this.count);

            return new Node<>(newChildrenSize, newChildren);

        }

        // Not a branch -- this is a Node with data

        if (this.data.hash != hash) {
            return this;
        }

        final NodeData<K,V> newData = this.data.remove(hash, key);

        if (newData == this.data) {
            // No changes needed (key not found)
            return this;
        }

        if (newData != null) {
            // There is still data at the node - we need a new container node
            return new Node<>(newData);
        }

        // All data removed -> should remove this container too
        return null;

    }




    void acceptVisitor(final AtomicHashVisitor<K, V> visitor) {
        if (this.data == null) {
            visitor.visitNode(this.children);
        } else {
            visitor.visitData(this.data.hash, this.data.entry, this.data.entries);
        }
    }

}
