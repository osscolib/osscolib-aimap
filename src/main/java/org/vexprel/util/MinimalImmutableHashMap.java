package org.vexprel.util;

import java.util.Arrays;
import java.util.Comparator;

public class MinimalImmutableHashMap<K,V> {

    private static final Entry[] EMPTY_ENTRIES = new Entry[0];
    private static final MinimalImmutableHashMap EMPTY_INSTANCE = new MinimalImmutableHashMap(EMPTY_ENTRIES);

    private final Entry[] entries;



    public static MinimalImmutableHashMap build() {
        return EMPTY_INSTANCE;
    }


    private MinimalImmutableHashMap(final Entry[] entries) {
        super();
        this.entries = entries;
    }


    public V get(final K key) {

        if (key == null) {
            throw new IllegalArgumentException("Null keys are forbidden");
        }

        final int hashCode = key.hashCode();
        int pos = binarySearchHashCodeInEntries(this.entries, hashCode);

        if (pos >= 0) {
            // Could exist, at least there is an entry for its hashcode
            return (V) this.entries[pos].get(key);
        }

        return null; // Not found

    }




    public MinimalImmutableHashMap<K,V> put(final K key, final V value) {

        if (key == null) {
            throw new IllegalArgumentException("Null keys are forbidden");
        }

        final SingleEntry entry = SingleEntry.build(key, value);
        int pos = Arrays.binarySearch(this.entries, entry, EntryComparator.INSTANCE);

        if (pos >= 0) {

            final Entry newEntry = this.entries[pos].put(entry);
            final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
            newEntries[pos] = newEntry;

            return new MinimalImmutableHashMap<>(newEntries);

        }

        pos = (++pos * -1);

        final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
        System.arraycopy(newEntries, pos, newEntries, pos + 1, newEntries.length - (pos + 1));
        newEntries[pos] = entry;

        return new MinimalImmutableHashMap<>(newEntries);

    }


    public MinimalImmutableHashMap<K,V> remove(final K key) {

        if (key == null) {
            throw new IllegalArgumentException("Null keys are forbidden");
        }

        final int hashCode = key.hashCode();
        int pos = binarySearchHashCodeInEntries(this.entries, hashCode);

        if (pos < 0) {
            // There isn't even an entry for this hashcode
            return this;
        }

        final Entry newEntry = this.entries[pos].remove(key);

        if (newEntry == this.entries[pos]) {
            // Key wasn't actually found (only hashcode collision)
            return this;
        }

        if (newEntry == null) {
            // Entry was completely removed

            final Entry[] newEntries = new Entry[this.entries.length - 1];
            System.arraycopy(this.entries, 0, newEntries, 0, pos);
            System.arraycopy(this.entries, pos +1, newEntries, pos, (this.entries.length - (pos + 1)));

            return new MinimalImmutableHashMap<>(newEntries);

        }

        // Entry was removed but there are still values for that hashcode
        final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
        newEntries[pos] = newEntry;

        return new MinimalImmutableHashMap<>(newEntries);

    }



    String prettyPrint() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n");
        for (int i = 0; i < this.entries.length; i++) {
            this.entries[i].prettyPrint(stringBuilder, 0);
            stringBuilder.append('\n');
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }





    private static int binarySearchHashCodeInEntries(final Entry[] entries, final int hashCode) {

        int low = 0;
        int high = entries.length - 1;

        int mid, cmp;
        int midVal;

        while (low <= high) {

            mid = (low + high) >>> 1;
            midVal = entries[mid].entryHash();

            cmp = Integer.compare(midVal, hashCode);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                // Found!!
                return mid;
            }

        }

        return -(low + 1);  // Not Found!! We return (-(insertion point) - 1), to guarantee all non-founds are < 0

    }




    private static int binarySearchHashCodeInNodes(final Node[] nodes, final int hashCode) {

        int low = 0;
        int high = nodes.length - 1;

        int mid, cmp;
        Node midVal;

        while (low <= high) {

            mid = (low + high) >>> 1;
            midVal = nodes[mid];

            cmp = (midVal.highLimit() < hashCode ? -1 : midVal.lowLimit() > hashCode? 1 : 0);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                // Found!!
                return mid;
            }

        }

        return -(low + 1);  // Not Found!! We return (-(insertion point) - 1), to guarantee all non-founds are < 0

    }



    private interface Node {

        Object get(final int keyHash, final Object key);
        int lowLimit();
        int highLimit();
        int containedHashEntries();
        Node put(final SingleEntry entry);
        Node remove(final int keyHash, final Object key);

    }


    private static final class EntriesNode implements Node {

        private final int lowLimit;
        private final int highLimit;
        private final int containedHashEntries;
        private final int maxUnnestedHashEntries;
        private final Entry[] entries;

        private EntriesNode(
                final int lowLimit, final int highLimit,
                final int containedHashEntries, final int maxUnnestedHashEntries,
                final Entry[] entries) {
            super();
            this.lowLimit = lowLimit;
            this.highLimit = highLimit;
            this.containedHashEntries = containedHashEntries;
            this.maxUnnestedHashEntries = maxUnnestedHashEntries;
            this.entries = entries;
        }

        @Override
        public int lowLimit() {
            return this.lowLimit;
        }

        @Override
        public int highLimit() {
            return this.highLimit;
        }

        @Override
        public int containedHashEntries() {
            return this.containedHashEntries;
        }

        public Node put(final SingleEntry entry) {

        }

    }


    private static final class TreeNode implements Node {

        private final int lowLimit;
        private final int highLimit;
        private final int containedHashEntries;
        private final int maxUnnestedHashEntries;
        private final Node[] nodes;

        private TreeNode(
                final int lowLimit, final int highLimit,
                final int containedHashEntries, final int maxUnnestedHashEntries,
                final Node[] nodes) {
            super();
            this.lowLimit = lowLimit;
            this.highLimit = highLimit;
            this.containedHashEntries = containedHashEntries;
            this.maxUnnestedHashEntries = maxUnnestedHashEntries;
            this.nodes = nodes;
        }

        @Override
        public int lowLimit() {
            return this.lowLimit;
        }

        @Override
        public int highLimit() {
            return this.highLimit;
        }

        @Override
        public int containedHashEntries() {
            return this.containedHashEntries;
        }


        @Override
        public Object get(final int keyHash, final Object key) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return null;
            }

            int pos = binarySearchHashCodeInNodes(this.nodes, keyHash);
            if (pos < 0) {
                // This should never happen
                return null;
            }

            return this.nodes[pos].get(keyHash, key);

        }


        @Override
        public Node put(final SingleEntry entry) {

            final int h = entry.entryHash;

            if (h < this.lowLimit || h > this.highLimit) {
                return this;
            }

            int pos = binarySearchHashCodeInNodes(this.nodes, h);
            if (pos < 0) {
                // This should never happen
                return this;
            }

            final Node newNode = this.nodes[pos].put(entry);

            if (newNode == this.nodes[pos]) {
                return this;
            }

            final Node[] newNodes = Arrays.copyOf(this.nodes, this.nodes.length);
            newNodes[pos] = newNode;

            final int newContainedHashEntries =
                    (this.containedHashEntries - this.nodes[pos].containedHashEntries()) + newNode.containedHashEntries();

            return new TreeNode(this.lowLimit, this.highLimit, newContainedHashEntries, this.maxUnnestedHashEntries, newNodes);

        }


        @Override
        public Node remove(final int keyHash, final Object key) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return this;
            }

            int pos = binarySearchHashCodeInNodes(this.nodes, keyHash);
            if (pos < 0) {
                // This should never happen
                return null;
            }

            final Node newNode = this.nodes[pos].remove(keyHash, key);

            if (newNode == this.nodes[pos]) {
                return this;
            }

            final Node[] newNodes = Arrays.copyOf(this.nodes, this.nodes.length);
            newNodes[pos] = newNode;

            final int newContainedHashEntries =
                    (this.containedHashEntries - this.nodes[pos].containedHashEntries()) + newNode.containedHashEntries();

            if (newContainedHashEntries <= this.maxUnnestedHashEntries) {
                // We have gone under the threshold, so we should turn this back into an EntriesNode

// TODO condense structures

            }


            return new TreeNode(this.lowLimit, this.highLimit, newContainedHashEntries, this.maxUnnestedHashEntries, newNodes);

        }

    }






    private interface Entry {
        Object get(final Object key);
        Entry put(final SingleEntry entry);
        Entry remove(final Object key);
        int entryHash();
        void prettyPrint(final StringBuilder strBuilder, final int indent);
    }

    private static final class SingleEntry implements Entry {

        private final int entryHash;
        private final Object key;
        private final Object value;

        static SingleEntry build(final Object key, final Object value) {
            return new SingleEntry(key.hashCode(), key, value);
        }

        private SingleEntry(final int entryHash, final Object key, final Object value) {
            super();
            this.entryHash = entryHash;
            this.key = key;
            this.value = value;
        }

        @Override
        public Object get(final Object key) {
            if (this.key.equals(key)) {
                return this.value;
            }
            return null;
        }

        @Override
        public Entry put(final SingleEntry entry) {
            if (this.entryHash != entry.entryHash) {
                throw new IllegalStateException("Tried to group entries with different hash code!");
            }
            if (this.key == entry.key && this.value == entry.value) {
                // This will allow avoiding whole hierarchy updates in this special case
                return this;
            }
            if (this.key.equals(entry.key)) {
                return entry;
            }
            final SingleEntry[] newEntries = new SingleEntry[] { this, entry };
            return new MultiEntry(this.entryHash, newEntries);
        }

        @Override
        public Entry remove(final Object key) {
            if (!this.key.equals(key)) {
                return this;
            }
            return null;
        }

        @Override
        public int entryHash() {
            return this.entryHash;
        }

        @Override
        public void prettyPrint(final StringBuilder strBuilder, final int indent) {
            for (int i = 0; i < indent; i++) {
                strBuilder.append(' ');
            }
            strBuilder.append(String.format("[%11d] \"%s\" : \"%s\")", this.entryHash, this.key, this.value));
        }

    }


    private static final class MultiEntry implements Entry {

        private final int entryHash;
        private final SingleEntry[] entries;


        private MultiEntry(final int entryHash, final SingleEntry[] entries) {
            super();
            this.entryHash = entryHash;
            this.entries = entries;
        }

        @Override
        public Object get(final Object key) {
            for (int i = 0; i < this.entries.length; i++) {
                if (this.entries[i].key.equals(key)) {
                    return this.entries[i].value;
                }
            }
            return null;
        }

        @Override
        public Entry put(final SingleEntry entry) {
            if (this.entryHash != entry.entryHash) {
                throw new IllegalStateException("Tried to group entries with different hash code!");
            }
            int pos = -1;
            for (int i = 0; i < this.entries.length; i++) {
                if (this.entries[i].key.equals(entry.key)) {
                    pos = i;
                    break;
                }
            }
            if (pos >= 0) {
                if (this.entries[pos].key == entry.key && this.entries[pos].value == entry.value) {
                    return this;
                }
                final SingleEntry[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
                newEntries[pos] = entry;
                return new MultiEntry(this.entryHash, newEntries);
            }
            final SingleEntry[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
            newEntries[this.entries.length] = entry;
            return new MultiEntry(this.entryHash, newEntries);
        }

        @Override
        public Entry remove(final Object key) {
            int pos = -1;
            for (int i = 0; i < this.entries.length; i++) {
                if (this.entries[i].key.equals(key)) {
                    pos = i;
                    break;
                }
            }
            if (pos >= 0) {
                if (this.entries.length == 2) {
                    return this.entries[pos == 0? 1 : 0];
                }
                final SingleEntry[] newEntries = new SingleEntry[this.entries.length - 1];
                System.arraycopy(this.entries, 0, newEntries, 0, pos);
                System.arraycopy(this.entries, pos + 1, newEntries, pos, (this.entries.length - (pos + 1)));
                return new MultiEntry(this.entryHash, newEntries);
            }
            return this;
        }

        @Override
        public int entryHash() {
            return this.entryHash;
        }

        @Override
        public void prettyPrint(final StringBuilder strBuilder, final int indent) {
            for (int i = 0; i < indent; i++) {
                strBuilder.append(' ');
            }
            strBuilder.append(String.format("[%11d] {\n", this.entryHash));
            for (int i = 0; i < this.entries.length; i++) {
                this.entries[i].prettyPrint(strBuilder, indent + 4);
                strBuilder.append('\n');
            }
            strBuilder.append("}");
        }

    }


    private static final class EntryComparator implements Comparator<Entry> {

        private static final EntryComparator INSTANCE = new EntryComparator();

        @Override
        public int compare(final Entry o1, final Entry o2) {
            return Integer.compare(o1.entryHash(), o2.entryHash());
        }

    }



    public static void main(String[] args) {

        MinimalImmutableHashMap<String,Object> m = MinimalImmutableHashMap.build();

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("Hello", 21);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("helloworld", 23);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("iFlloworld", 52);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("j'lloworld", 31);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("lloworld", 99);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("IFllo", 423);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("n.oworld", 941);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("Aloha", 3413);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("IFllo", 987);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("Hello", 23142);

        System.out.println();
        System.out.println(m.prettyPrint());

        System.out.println();
        System.out.println(m.get("IFllo"));
        System.out.println(m.get("lloworld"));
        System.out.println(m.get("Hello"));
        System.out.println(m.get("Aloha"));

        m = m.remove("Hello");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("IFllo");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("IFllo");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("Aloha");

        System.out.println();
        System.out.println(m.prettyPrint());

        MinimalImmutableHashMap<String,Object> m2 = m.remove("iFlloworld");

        System.out.println();
        System.out.println(m2.prettyPrint());

        System.out.println();
        System.out.println(m.prettyPrint());

    }

}
