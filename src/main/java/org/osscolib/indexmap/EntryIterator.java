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

import java.util.Iterator;

final class EntryIterator<K,V> implements AtomicHashVisitor<K,V>, Iterator<Entry<K,V>> {


    private final Node<K,V>[][] stack;
    private int stackSize = 0;
    private int[] stackPos;

    private Entry<K,V> entry;
    private Entry<K,V>[] entries;
    private int entriesPos = -1;

    private Entry<K,V> next = null;


    EntryIterator(final int maskSize) {
        super();
        this.stack = new Node[32 / maskSize][]; // max possible node nesting level
        this.stackPos = new int[this.stack.length];
        computeNext();
    }



    @Override
    public boolean hasNext() {
        return this.next != null;
    }


    @Override
    public Entry<K,V> next() {
        final Entry<K,V> n = this.next;
        computeNext();
        return n;
    }



    private void computeNext() {

        if (this.entries != null) {
            if (this.entriesPos < this.entries.length) {
                this.next = this.entries[this.entriesPos++];
                return;
            }
            this.entries = null;
            this.entriesPos = -1;
        } else if (this.entry != null) {
            this.next = this.entry;
            this.entry = null;
            return;
        }

        final int stackCurrent = this.stackSize - 1;
        final Node<K,V>[] currentChildren = this.stack[stackCurrent];
        final int currentChildrenPos = this.stackPos[stackCurrent];


    }



    @Override
    public void visitRoot(final Node rootNode) {
        if (rootNode != null) {
            rootNode.acceptVisitor(this);
        }
    }


    @Override
    public void visitNode(final Node<K,V>[] children) {
        this.stack[this.stackSize] = children;
        this.stackPos[this.stackSize] = 0;
        this.stackSize++;
    }


    @Override
    public void visitData(final int hash, final Entry<K,V> entry, final Entry<K,V>[] entries) {
        if (entry != null) {
            this.entry = entry;
        } else {
            this.entries = entries;
            this.entriesPos = 0;
        }
    }

}
