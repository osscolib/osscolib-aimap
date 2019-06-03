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

final class Node<K,V> implements Serializable {

    private static final long serialVersionUID = 6914544628900109073L;

    final NodeData<K,V> data;
    final Node<K,V>[] children; // can contain many nulls



    Node(final Node<K,V>[] children) {
        super();
        this.children = children;
        this.data = null;
    }


    Node(final NodeData<K,V> data) {
        super();
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




    Node<K,V> put(final Level level, final Entry<K, V> entry) {

        // If possible, we will delegate to the NodeData
        if (this.data != null && this.data.hash == entry.hash) {
            final NodeData<K,V> newData = this.data.put(entry);
            if (newData == this.data) {
                // Nothing was added because the entry already existed
                return this;
            }
            return new Node<>(newData);
        }

        Node<K,V>[] newChildren = this.children;
        boolean newChildrenMutable = false;
        if (newChildren == null) {
            newChildren = new Node[level.mask + 1];
            newChildren[level.pos(this.data.hash)] = new Node<>(this.data);
            newChildrenMutable = true;
        }

        final int newEntryPos = level.pos(entry.hash);
        newChildren = NodeBuilder.addChild(newChildren, newChildrenMutable, level, newEntryPos, entry);
        if (newChildren == this.children){
            return this;
        }

        return new Node<>(newChildren);

    }


    // entries comes from a Map, so we know 100% sure there are no repeated keys. That means if an entries
    // fragment to be set into a Node has > 1 entries, for sure it will mean the data needs to be put into a node



    Node<K,V> putAll(final Level level, final Entry<K, V>[] entries, final int start, final int end) {
        // TODO Then each node will either forward the corresponding part of the array to their children,
        // TODO or handle it themselves. At a node level, processing can probably be made iterative. That should
        // TODO be fine as long as we don't create more than one children array.


//        if (start == end) {
            return this;
//        }
//
//        if (start + 1 == end) {
//            // Simplify to a normal "put" operation
//            return put(level, entries[start]);
//        }
//
//
//        Node<K,V>[] children = this.children;
//        Node<K,V> child;
//
//
//        if (children == null) {
//            // This is a data node
//            // TODO convert this data node to a branch node
//            return this;
//        }
//
//
//        int newCount = this.count;
//        Node<K,V>[] newChildren = null;
//        Node<K,V> newChild;
//
//
//        int i = start;
//        int x;
//
//        int ipos = level.pos(entries[i].hash);
//        int currentPos;
//
//        while (i < end) {
//
//            x = i;
//            currentPos = ipos;
//            while (ipos == currentPos && ++i < end) {
//                ipos = level.pos(entries[i].hash);
//            }
//
//            // We determined that entries[x..i) corresponds to children[currentPos]
//
//            child = children[currentPos];
//
//            if (child != null) {
//
//                newChild = child.putAll(level.next, entries, x, i);
//
//                if (newChild != child) {
//                    if (newChildren == null) {
//                        newChildren = Arrays.copyOf(children, children.length);
//                    }
//                    newChildren[currentPos] = newChild;
//                }
//
//            } else { // currently there is no child at currentPos
//
//                if (newChildren == null) {
//                    newChildren = Arrays.copyOf(children, children.length);
//                }
//
//                newChildren = NodeBuilder.buildChildren(level, newChildren, entries, x, i);
//                newCount++;
//
//            }
//
//        }
//
//        if (newChildren == null) {
//            return this;
//        }
//
//        return new Node(newCount, newChildren);

    }




    Node<K,V> remove(final Level level, final int hash, final Object key) {

        if (this.data == null) {

            final int pos = level.pos(hash);
            final Node<K,V> child = this.children[pos];

            if (child == null) {
                // Not found
                return this;
            }

            final Node<K,V> newChild = child.remove(level.next, hash, key);
            if (newChild == child) {
                return this;
            }

            if (newChild == null && onlyOneChild(this.children)) {
                // This branch has become empty
                return null;
            }

            // Note newChild can still be null here
            final Node<K,V>[] newChildren = Arrays.copyOf(this.children, this.children.length);
            newChildren[pos] = newChild;

            return new Node<>(newChildren);

        }

        // Not a branch -- this is a Node with data

        if (this.data.hash != hash) {
            return this;
        }

        final NodeData<K,V> newData = this.data.remove(key);

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




    private static <K,V> boolean onlyOneChild(final Node<K,V>[] children) {
        boolean found = false;
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                if (found) {
                    return false;
                }
                found = true;
            }
        }
        return true;
    }

}
