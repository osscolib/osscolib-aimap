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

import java.util.List;
import java.util.Map;

import org.osscolib.aimap.IndexedMap.Node;
import org.osscolib.aimap.IndexedMap.Slot;
import org.osscolib.aimap.IndexedMap.Visitor;

final class PrettyPrintVisitor<K,V> implements Visitor<K,V> {

    private final StringBuilder visitorStrBuilder;
    private int level;

    PrettyPrintVisitor() {
        super();
        this.visitorStrBuilder = new StringBuilder();
        this.level = 0;
    }


    private String indentForLevel(final int level) {
        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < (level * 2); i++) {
            strBuilder.append(' ');
        }
        return strBuilder.toString();
    }


    private String writeEntry(final Map.Entry<K,V> entry) {
        return String.format("<\"%s\"> : <\"%s\">", entry.getKey(), entry.getValue());
    }



    @Override
    public void visitRoot(final Node rootNode) {
        rootNode.acceptVisitor(this);
    }


    @Override
    public void visitBranchNode(final int indexLowLimit, final int indexHighLimit, final List<Node<K,V>> nodes) {

        this.visitorStrBuilder.append(indentForLevel(this.level));
        this.visitorStrBuilder.append(String.format("[%11d | %11d] {", indexLowLimit, indexHighLimit));
        if (nodes.size() == 0) {
            this.visitorStrBuilder.append("}");
        } else {
            this.visitorStrBuilder.append('\n');
            this.level++;
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).acceptVisitor(this);
                this.visitorStrBuilder.append('\n');
            }
            this.level--;
            this.visitorStrBuilder.append(indentForLevel(this.level));
            this.visitorStrBuilder.append('}');
        }

    }


    @Override
    public void visitLeafNode(final int indexLowLimit, final int indexHighLimit, final List<Slot<K,V>> slots) {

        this.visitorStrBuilder.append(indentForLevel(this.level));
        this.visitorStrBuilder.append(String.format("[%11d | %11d] {", indexLowLimit, indexHighLimit));
        if (slots.size() == 0) {
            this.visitorStrBuilder.append("}");
        } else {
            this.visitorStrBuilder.append('\n');
            this.level++;
            for (int i = 0; i < slots.size(); i++) {
                slots.get(i).acceptVisitor(this);
                this.visitorStrBuilder.append('\n');
            }
            this.level--;
            this.visitorStrBuilder.append(indentForLevel(this.level));
            this.visitorStrBuilder.append('}');
        }

    }


    @Override
    public void visitSlot(final int index, final List<Map.Entry<K,V>> entries) {

        this.visitorStrBuilder.append(indentForLevel(this.level));
        this.visitorStrBuilder.append(String.format("[%11d] (", index));
        if (entries.size() == 1) {
            this.visitorStrBuilder.append(String.format(" %s )", writeEntry(entries.get(0))));
        } else {
            this.visitorStrBuilder.append('\n');
            for (int i = 0; i < entries.size(); i++) {
                this.visitorStrBuilder.append(indentForLevel(this.level + 1));
                this.visitorStrBuilder.append(writeEntry(entries.get(i)));
                this.visitorStrBuilder.append('\n');
            }
            this.visitorStrBuilder.append(indentForLevel(this.level));
            this.visitorStrBuilder.append(")");
        }

    }


    @Override
    public String toString() {
        return this.visitorStrBuilder.toString();
    }

}
