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

        final NodeData<K,V> data = this.data;

        // If possible, we will delegate to the NodeData
        if (data != null && data.hash == entry.hash) {
            final NodeData<K,V> newData = data.put(entry);
            if (newData == data) {
                // Nothing was added because the entry already existed
                return this;
            }
            return new Node<>(newData);
        }

        Node<K,V>[] newChildren = this.children;
        boolean newChildrenMutable = false;
        if (newChildren == null) {
            newChildren = new Node[level.mask + 1];
            newChildren[level.pos(data.hash)] = new Node<>(data);
            newChildrenMutable = true;
        }

        final int newEntryPos = level.pos(entry.hash);
        newChildren = NodeBuilder.addChild(newChildren, newChildrenMutable, level, newEntryPos, entry);
        if (newChildren == this.children){
            return this;
        }

        return new Node<>(newChildren);

    }



    Node<K,V> putAll(final Level level, final Entry<K, V>[] entries, final int start, final int end) {

        if (start == end) {
            return this;
        }

        if (start + 1 == end) {
            // Re-route to a normal "put" operation
            return put(level, entries[start]);
        }

        Node<K,V>[] newChildren = this.children;
        boolean newChildrenMutable = false;

        if (newChildren == null) {
            // This is a data node, and we know there are at least two different keys that need to be inserted here.
            // Unless all the entries we are adding have the same hash as the existing data entry, we will need to
            // turn that data entry into a Node.

            final NodeData<K,V> data = this.data;
            if (allHashesMatch(data.hash, entries, start, end)) {
                // All hashes match! so we need to delegate entirely to the NodeData
                NodeData<K,V> newData = data;
                for (int i = start; i < end; i++) {
                    newData = newData.put(entries[i]);
                }
                if (newData == data) {
                    // Nothing was added because all entries already existed -- this should actually never happen
                    return this;
                }
                return new Node<>(newData);
            }

            // Not all hashes matched (usual case), so given we have > 1 keys to be inserted in this node, we
            // are sure we will need to turn this Node's data into a nested node
            newChildren = new Node[level.mask + 1];
            newChildren[level.pos(data.hash)] = new Node<>(data);
            newChildrenMutable = true;

        }

        // We will need to segment all the selected entries, determining the position to be assigned to each segment

        int i = start;
        int x;

        int ipos = level.pos(entries[i].hash);
        int currentPos;

        while (i < end) {

            x = i;
            currentPos = ipos;
            while (ipos == currentPos && ++i < end) {
                ipos = level.pos(entries[i].hash);
            }

            // We determined that entries[x..i) corresponds to children[currentPos]

            newChildren = NodeBuilder.addChildren(newChildren, newChildrenMutable, level, currentPos, entries, x, i);
            if (newChildren != this.children){
                newChildrenMutable = true;
            }

        }

        if (newChildren == this.children) {
            return this;
        }

        return new Node(newChildren);

    }



    private static <K,V> boolean allHashesMatch(final int hash, final Entry<K,V>[] entries, final int start, final int end) {
        for (int i = start; i < end; i++) {
            if (entries[i].hash != hash) {
                return false;
            }
        }
        return true;
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
