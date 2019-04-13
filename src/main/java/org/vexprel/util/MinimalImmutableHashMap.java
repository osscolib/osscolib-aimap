package org.vexprel.util;

import java.util.Arrays;

public class MinimalImmutableHashMap<K,V> {

    private final Node root;



    public static MinimalImmutableHashMap build(final int maxNodeSize) {
        final Node root =
                new EntriesNode(Integer.MIN_VALUE, Integer.MAX_VALUE, maxNodeSize, EntriesNode.EMPTY_ENTRIES);
        return new MinimalImmutableHashMap(root);
    }


    private MinimalImmutableHashMap(final Node root) {
        super();
        this.root = root;
    }


    public V get(final K key) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        return (V) this.root.get(key.hashCode(), key);
    }




    public MinimalImmutableHashMap<K,V> put(final K key, final V value) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        final Node newRoot = this.root.put(key.hashCode(), key, value);
        if (this.root == newRoot) {
            return this;
        }
        return new MinimalImmutableHashMap<>(newRoot);
    }


    public MinimalImmutableHashMap<K,V> remove(final K key) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        final Node newRoot = this.root.remove(key.hashCode(), key);
        if (this.root == newRoot) {
            return this;
        }
        return new MinimalImmutableHashMap<>(newRoot);
    }





    String prettyPrint() {
        final StringBuilder stringBuilder = new StringBuilder();
        this.root.prettyPrint(stringBuilder, 0);
        return stringBuilder.toString();
    }





    private static int binarySearchHashCodeInEntries(final Entry[] entries, final int hashCode) {

        // TODO when < 5 probably faster to do it sequentially

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

        // TODO when < 3 probably faster to do it sequentially

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



    static long computeRangePerNode(final int lowLimit, final int highLimit, final int maxNodeSize) {
        final long totalRange = ((long)highLimit - (long)lowLimit) + 1L; // limits are inclusive => +1
        final long divider = totalRange / (long)maxNodeSize;
        if ((totalRange % (long)maxNodeSize) == 0) {
            return divider;
        }
        return divider + 1L;
    }



    private interface Node {

        Object get(final int keyHash, final Object key);
        int lowLimit();
        int highLimit();
        int totalEntries();
        Node put(final int keyHash, final Object key, final Object value);
        Node remove(final int keyHash, final Object key);
        void prettyPrint(final StringBuilder strBuilder, final int indent);

    }


    private static final class EntriesNode implements Node {

        private static final Entry[] EMPTY_ENTRIES = new Entry[0];

        private final int lowLimit;
        private final int highLimit;
        private final int maxNodeSize;
        private final Entry[] entries;


        private EntriesNode(
                final int lowLimit, final int highLimit,
                final int maxNodeSize,
                final Entry[] entries) {
            super();
            this.lowLimit = lowLimit;
            this.highLimit = highLimit;
            this.maxNodeSize = maxNodeSize;
            this.entries = entries;
        }



        static Node build(final int lowLimit, final int highLimit,
                          final int maxNodeSize, final Entry[] entries) {

            if (entries.length > maxNodeSize) {
                // We have reached the maximum amount of hashes that can be contained in an EntriesNode, so
                // we need to divide this EntriesNode and turn it into a TreeNode

                final Node[] newNodes = new Node[maxNodeSize];
                long rangePerNode = computeRangePerNode(lowLimit, highLimit, maxNodeSize);

                int entriesOffset = 0;
                Entry[] nodeEntries;

                long newLowLimit, newHighLimit;
                for (int i = 0; i < maxNodeSize; i++) {

                    newLowLimit = (long)lowLimit + (i * rangePerNode);
                    newHighLimit = newLowLimit + rangePerNode - 1;
                    if (newHighLimit > highLimit) {
                        // This can only happen for the last node
                        newHighLimit = (long)highLimit;
                    }

                    int n = entriesOffset;
                    while (n < entries.length &&
                            entries[n].entryHash() >= newLowLimit && entries[n].entryHash() <= newHighLimit) {
                        n++;
                    }

                    if (n == entriesOffset) {
                        nodeEntries = EMPTY_ENTRIES;
                    } else if (entriesOffset == 0 && n == entries.length) {
                        nodeEntries = entries;
                    } else {
                        nodeEntries = new Entry[n - entriesOffset];
                        System.arraycopy(entries, entriesOffset, nodeEntries, 0, (n - entriesOffset));
                    }
                    entriesOffset = n;

                    newNodes[i] = EntriesNode.build((int)newLowLimit, (int)newHighLimit, maxNodeSize, nodeEntries);

                }

                return TreeNode.build(lowLimit, highLimit, entries.length, maxNodeSize, newNodes);

            }


            return new EntriesNode(lowLimit, highLimit, maxNodeSize, entries);

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
        public int totalEntries() {
            return this.entries.length;
        }


        @Override
        public Object get(final int keyHash, final Object key) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return null;
            }

            int pos = binarySearchHashCodeInEntries(this.entries, keyHash);
            if (pos < 0) {
                // This should never happen
                return null;
            }

            return this.entries[pos].get(key);

        }


        @Override
        public Node put(final int keyHash, final Object key, final Object value) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return this;
            }

            int pos = binarySearchHashCodeInEntries(this.entries, keyHash);

            if (pos >= 0) {

                final Entry newEntry = this.entries[pos].put(keyHash, key, value);
                final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
                newEntries[pos] = newEntry;

                return EntriesNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntries);

            }

            pos = (++pos * -1);

            final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
            System.arraycopy(newEntries, pos, newEntries, pos + 1, newEntries.length - (pos + 1));
            newEntries[pos] = SingleEntry.build(key, value);

            // This build call might actually return a TreeNode if we have now gone over the max size threshold
            return EntriesNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntries);

        }



        @Override
        public Node remove(final int keyHash, final Object key) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return this;
            }

            int pos = binarySearchHashCodeInEntries(this.entries, keyHash);
            if (pos < 0) {
                // Not found
                return this;
            }

            final Entry newEntry = this.entries[pos].remove(key);

            if (newEntry == this.entries[pos]) {
                // Not found (hash found but not key)
                return this;
            }

            if (newEntry != null) {

                final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
                newEntries[pos] = newEntry;

                return EntriesNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntries);

            }

            // newEntry is null, and so we need to actually remove it

            if (this.entries.length == 1) {
                // We are empty now!
                return EntriesNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, EMPTY_ENTRIES);
            }

            final Entry[] newEntries = new Entry[this.entries.length - 1];
            System.arraycopy(this.entries, 0, newEntries, 0, pos);
            System.arraycopy(this.entries, pos +1, newEntries, pos, this.entries.length - (pos + 1));

            return EntriesNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntries);

        }


        @Override
        public void prettyPrint(final StringBuilder strBuilder, final int indent) {
            for (int i = 0; i < indent; i++) {
                strBuilder.append(' ');
            }
            strBuilder.append(String.format("[%11d | %11d]", this.lowLimit, this.highLimit));
            if (this.entries.length == 0) {
                strBuilder.append(" { }");
            } else {
                strBuilder.append(" {\n");
                for (int i = 0; i < this.entries.length; i++) {
                    this.entries[i].prettyPrint(strBuilder, indent + 2);
                    strBuilder.append('\n');
                }
                strBuilder.append("}");
            }
        }


    }


    private static final class TreeNode implements Node {

        private final int lowLimit;
        private final int highLimit;
        private final int totalEntries;
        private final int maxNodeSize;
        private final Node[] nodes;

        private TreeNode(
                final int lowLimit, final int highLimit,
                final int totalEntries, final int maxNodeSize,
                final Node[] nodes) {
            super();
            this.lowLimit = lowLimit;
            this.highLimit = highLimit;
            this.totalEntries = totalEntries;
            this.maxNodeSize = maxNodeSize;
            this.nodes = nodes;
        }



        static Node build(final int lowLimit, final int highLimit,
                              final int containedHashEntries, final int maxNodeSize,
                              final Node[] nodes) {

            if (containedHashEntries <= maxNodeSize) {
                // We have gone under the threshold, so we should condense this back into an EntriesNode

                final Entry[] newEntries = new Entry[containedHashEntries];
                int offset = 0;
                for (int i = 0; i < nodes.length; i++) {
                    final EntriesNode entriesNode = (EntriesNode) nodes[i];
                    System.arraycopy(entriesNode.entries, 0, newEntries, offset, entriesNode.entries.length);
                    offset += entriesNode.entries.length;
                }

                return EntriesNode.build(lowLimit, highLimit, maxNodeSize, newEntries);

            }

            return new TreeNode(lowLimit, highLimit, containedHashEntries, maxNodeSize, nodes);

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
        public int totalEntries() {
            return this.totalEntries;
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
        public Node put(final int keyHash, final Object key, final Object value) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return this;
            }

            int pos = binarySearchHashCodeInNodes(this.nodes, keyHash);
            if (pos < 0) {
                // This should never happen
                return this;
            }

            final Node newNode = this.nodes[pos].put(keyHash, key, value);

            if (newNode == this.nodes[pos]) {
                return this;
            }

            final Node[] newNodes = Arrays.copyOf(this.nodes, this.nodes.length);
            newNodes[pos] = newNode;

            final int newContainedHashEntries =
                    (this.totalEntries - this.nodes[pos].totalEntries()) + newNode.totalEntries();

            return TreeNode.build(this.lowLimit, this.highLimit, newContainedHashEntries, this.maxNodeSize, newNodes);

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
                    (this.totalEntries - this.nodes[pos].totalEntries()) + newNode.totalEntries();

            // This build call might actually return an EntriesNode if we have now gone under the max size threshold
            return TreeNode.build(this.lowLimit, this.highLimit, newContainedHashEntries, this.maxNodeSize, newNodes);

        }

        @Override
        public void prettyPrint(final StringBuilder strBuilder, final int indent) {
            for (int i = 0; i < indent; i++) {
                strBuilder.append(' ');
            }
            strBuilder.append(String.format("[%11d | %11d]", this.lowLimit, this.highLimit));
            if (this.nodes.length == 0) {
                strBuilder.append(" { }");
            } else {
                strBuilder.append(" {\n");
                for (int i = 0; i < this.nodes.length; i++) {
                    this.nodes[i].prettyPrint(strBuilder, indent + 2);
                    strBuilder.append('\n');
                }
                strBuilder.append("}");
            }
        }

    }






    private interface Entry {
        Object get(final Object key);
        Entry put(final int keyHash, final Object key, final Object value);
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
        public Entry put(final int keyHash, final Object key, final Object value) {
            if (this.entryHash != keyHash) {
                throw new IllegalStateException("Tried to group entries with different hash code!");
            }
            if (this.key == key && this.value == value) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                return this;
            }
            if (this.key.equals(key)) {
                return SingleEntry.build(key, value);
            }
            final SingleEntry[] newEntries = new SingleEntry[] { this, SingleEntry.build(key, value) };
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
        public Entry put(final int keyHash, final Object key, final Object value) {
            if (this.entryHash != keyHash) {
                throw new IllegalStateException("Tried to group entries with different hash code!");
            }
            int pos = -1;
            for (int i = 0; i < this.entries.length; i++) {
                if (this.entries[i].key.equals(key)) {
                    pos = i;
                    break;
                }
            }
            if (pos >= 0) {
                if (this.entries[pos].key == key && this.entries[pos].value == value) {
                    // No need to perform any modifications, we might avoid a rewrite of a tree path!
                    return this;
                }
                final SingleEntry[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
                newEntries[pos] = SingleEntry.build(key, value);
                return new MultiEntry(this.entryHash, newEntries);
            }
            final SingleEntry[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
            newEntries[this.entries.length] = SingleEntry.build(key, value);
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
                this.entries[i].prettyPrint(strBuilder, indent + 2);
                strBuilder.append('\n');
            }
            strBuilder.append("}");
        }

    }




    public static void main(String[] args) {

        MinimalImmutableHashMap<String,Object> m = MinimalImmutableHashMap.build(10);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("Hello", 21);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("helloworld", 23);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("iFlloworld", 52);

//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        m = m.put("j'lloworld", 31);
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        m = m.put("lloworld", 99);
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        m = m.put("IFllo", 423);
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        m = m.put("n.oworld", 941);
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        m = m.put("Aloha", 3413);
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        m = m.put("IFllo", 987);
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        m = m.put("Hello", 23142);
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        System.out.println();
//        System.out.println(m.get("IFllo"));
//        System.out.println(m.get("lloworld"));
//        System.out.println(m.get("Hello"));
//        System.out.println(m.get("Aloha"));
//
//        m = m.remove("Hello");
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        m = m.remove("IFllo");
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        m = m.remove("IFllo");
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        m = m.remove("Aloha");
//
//        System.out.println();
//        System.out.println(m.prettyPrint());
//
//        MinimalImmutableHashMap<String,Object> m2 = m.remove("iFlloworld");
//
//        System.out.println();
//        System.out.println(m2.prettyPrint());
//
//        System.out.println();
//        System.out.println(m.prettyPrint());


//        testComputeLimits(0, 900, 5);
//        testComputeLimits(0, 903, 5);
//        testComputeLimits(0, 904, 5);
//        testComputeLimits(0, 905, 5);
//        testComputeLimits(0, 906, 5);
//        testComputeLimits(Integer.MIN_VALUE, Integer.MAX_VALUE, 5);
//        testComputeLimits(Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, 5);

//        testComputeLimits(Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
//        testComputeLimits(Integer.MIN_VALUE, Integer.MAX_VALUE, 2);
//        testComputeLimits(Integer.MIN_VALUE, Integer.MAX_VALUE, 3);
//        testComputeLimits(Integer.MIN_VALUE, Integer.MAX_VALUE, 4);
//        testComputeLimits(Integer.MIN_VALUE, Integer.MAX_VALUE, 100);
//
//
//        testComputeLimits(2104533979, 2147483647, 100);

    }



    private static void testComputeLimits(final int lowLimit, final int highLimit, final int maxNodeSize) {

        long rangePerNode = computeRangePerNode(lowLimit, highLimit, maxNodeSize);

        System.out.println(rangePerNode);
        System.out.println("--------------");

        long newLowLimit, newHighLimit;
        for (int i = 0; i < maxNodeSize; i++) {

            newLowLimit = (long)lowLimit + (i * rangePerNode);
            newHighLimit = newLowLimit + rangePerNode - 1;
            if (newHighLimit > highLimit) {
                // This can only happen for the last node
                newHighLimit = (long)highLimit;
            }

            System.out.println(newLowLimit + " - " + newHighLimit + " = " + ((newHighLimit - newLowLimit) + 1));

        }
        System.out.println("--------------");

    }

}
