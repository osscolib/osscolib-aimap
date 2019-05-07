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

import java.math.BigInteger;
import java.util.Map;

final class PrettyPrinter {

    
    static <K,V> String prettyPrint(final AtomicHashStore<K,V> store) {
        final StringBuilder stringBuilder = new StringBuilder();
        if (store.root != null) {
            if (store.root.children == null) {
                printData(0, store.maskSize, stringBuilder, store.root.data.hash, store.root.data.entry, store.root.data.entries);
            } else {
                printNode(0, store.maskSize, stringBuilder, store.root.children);
            }
        }
        return stringBuilder.toString();
    }
    
    

    private static String indentForLevel(final int level) {
        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < (level * 2); i++) {
            strBuilder.append(' ');
        }
        return strBuilder.toString();
    }


    private static <K,V> String printEntry(final Map.Entry<K,V> entry) {
        return String.format("<\"%s\"> : <\"%s\">", entry.getKey(), entry.getValue());
    }



    private static <K,V> void printNode(
            final int level, final int maskSize, final StringBuilder stringBuilder, final Node<K,V>[] children) {

        stringBuilder.append(indentForLevel(level));
        stringBuilder.append(
                String.format("[%2d | %032d] {",
                        level,
                        new BigInteger(Integer.toBinaryString(((1 << maskSize) - 1) << (level * maskSize)))));
        if (children.length == 0) {
            stringBuilder.append("}");
        } else {
            stringBuilder.append('\n');
            for (int i = 0; i < children.length; i++) {
                final Node<K,V> child = children[i];
                if (child != null) {
                    if (child.children == null) {
                        printData(level + 1, maskSize, stringBuilder, child.data.hash, child.data.entry, child.data.entries);
                    } else {
                        printNode(level + 1, maskSize, stringBuilder, child.children);
                    }
                    stringBuilder.append('\n');
                }
            }
            stringBuilder.append(indentForLevel(level));
            stringBuilder.append('}');
        }

    }


    private static <K,V> void printData(
            final int level, final int maskSize, final StringBuilder stringBuilder,
            final int hash, final Entry<K,V> entry, final Entry<K,V>[] entries) {

        stringBuilder.append(indentForLevel(level));
        stringBuilder.append(
                String.format("[%2d | %032d] {\n",
                        level,
                        new BigInteger(Integer.toBinaryString(((1 << maskSize) - 1) << (level * maskSize)))));

        printEntries(level + 1, stringBuilder, hash, entry, entries);
        stringBuilder.append('\n');

        stringBuilder.append(indentForLevel(level));
        stringBuilder.append('}');

    }


    private static <K,V> void printEntries(
            final int level, final StringBuilder stringBuilder,
            final int hash, final Entry<K,V> entry, final Map.Entry<K,V>[] entries) {

        stringBuilder.append(indentForLevel(level));
        stringBuilder.append(String.format("[%032d] (", new BigInteger(Integer.toBinaryString(hash))));
        if (entries == null) {
            stringBuilder.append(String.format(" %s )", printEntry(entry)));
        } else {
            stringBuilder.append('\n');
            for (int i = 0; i < entries.length; i++) {
                stringBuilder.append(indentForLevel(level + 1));
                stringBuilder.append(printEntry(entries[i]));
                stringBuilder.append('\n');
            }
            stringBuilder.append(indentForLevel(level));
            stringBuilder.append(")");
        }

    }



    private PrettyPrinter() {
        super();
    }
    
}
