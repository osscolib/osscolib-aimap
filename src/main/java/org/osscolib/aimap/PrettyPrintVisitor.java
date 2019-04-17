package org.osscolib.aimap;

import java.util.List;
import java.util.Map;

import org.osscolib.aimap.AtomicIndexedMap.Node;
import org.osscolib.aimap.AtomicIndexedMap.Slot;
import org.osscolib.aimap.AtomicIndexedMap.Visitor;

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
