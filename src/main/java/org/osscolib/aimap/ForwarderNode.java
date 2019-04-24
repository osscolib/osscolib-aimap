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

import java.util.Collections;
import java.util.Map;

import org.osscolib.aimap.IndexedMap.Node;
import org.osscolib.aimap.IndexedMap.Visitor;

final class ForwarderNode<K,V> implements Node<K,V> {

    private final long indexLowLimit;
    private final long indexHighLimit;
    private final int maxNodeSize;

    private final long childIndexLowLimit;
    private final long childIndexHighLimit;
    private final Node<K,V> child;


    ForwarderNode(
            final long indexLowLimit, final long indexHighLimit,
            final int maxNodeSize,
            final Node<K,V> child) {
        super();
        this.indexLowLimit = indexLowLimit;
        this.indexHighLimit = indexHighLimit;
        this.maxNodeSize = maxNodeSize;
        this.child = child;
        this.childIndexLowLimit = this.child.getIndexLowLimit();
        this.childIndexHighLimit = this.child.getIndexHighLimit();
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
        return this.child.size();
    }


    @Override
    public boolean containsKey(final long index, final Object key) {
        if (index < this.childIndexLowLimit || index > this.childIndexHighLimit) {
            return false;
        }
        return this.child.containsKey(index, key);
    }


    @Override
    public V get(final long index, final Object key) {
        if (index < this.childIndexLowLimit || index > this.childIndexHighLimit) {
            return null;
        }
        return this.child.get(index, key);
    }


    @Override
    public Node<K,V> put(final long index, final Map.Entry<K,V> entry) {

        if (index < this.indexLowLimit || index > this.indexHighLimit) {
            return this;
        }

        // Let's check if putting this would be the entire responsibility of the forwarded node
        if (index >= this.childIndexLowLimit && index <= this.childIndexHighLimit) {
            // This should be delegated

            final Node<K, V> newNode = this.node.put(index, entry);
            if (newNode == this.child) {
                // Not found
                return this;
            }

            // This put operation caused the modification of the forwarded node, so we need to rebuild the forwarder
            return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxNodeSize, newNode);

        }

        // We will need to convert this forwarder into a branch node

        final Node<K,V> newNode = DataSlotBuilder.build(index, entry);
        return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxNodeSize, this.child, newNode);

    }



    @Override
    public Node<K,V> remove(final long index, final Object key) {

        if (index < this.childIndexLowLimit || index > this.childIndexHighLimit) {
            return this;
        }

        final Node<K,V> newNode = this.child.remove(index, key);
        if (newNode == this.child) {
            // Not found
            return this;
        }

        if (newNode != null) {
            return NodeBuilder.build(this.indexLowLimit, this.indexHighLimit, this.maxNodeSize, newNode);
        }

        // newSlot is null, and so we need to actually remove the forwarder too
        return null;

    }


    @Override
    public void acceptVisitor(final Visitor<K,V> visitor) {
        visitor.visitLeafNode(this.indexLowLimit, this.indexHighLimit, Collections.singletonList(this.child));
    }


}
