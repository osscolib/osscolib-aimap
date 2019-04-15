package org.vexprel.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.vexprel.standard.action.ByteCodeGenStandardExpressionActionFactory;

public class ImmutableTreeHashMap<K,V> {

    private static final TreeHashMapEntry[] EMPTY_ENTRIES = new TreeHashMapEntry[0];

    private final TreeHashMapNode root;



    public static ImmutableTreeHashMap build(final int maxNodeSize) {
        final TreeHashMapNode root =
                LeafTreeHashMapNode.build(Integer.MIN_VALUE, Integer.MAX_VALUE, maxNodeSize, EMPTY_ENTRIES);
        return new ImmutableTreeHashMap(root);
    }


    private ImmutableTreeHashMap(final TreeHashMapNode root) {
        super();
        this.root = root;
    }


    public V get(final K key) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        return (V) this.root.get(key.hashCode(), key);
    }




    public ImmutableTreeHashMap<K,V> put(final K key, final V value) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        final TreeHashMapNode newRoot = this.root.put(key.hashCode(), KeyValue.build(key, value));
        if (this.root == newRoot) {
            return this;
        }
        return new ImmutableTreeHashMap<>(newRoot);
    }


    public ImmutableTreeHashMap<K,V> remove(final K key) {
        if (key == null) {
            throw new IllegalArgumentException("Null not allowed as a key");
        }
        final TreeHashMapNode newRoot = this.root.remove(key.hashCode(), key);
        if (this.root == newRoot) {
            return this;
        }
        return new ImmutableTreeHashMap<>(newRoot);
    }


    public void acceptVisitor(final ImmutableTreeHashMapVisitor visitor) {
        visitor.visitRoot(this.root);
    }


    String prettyPrint() {
        final ImmutableTreeHashMapVisitor visitor = new PrettyPrintVisitor();
        visitor.visitRoot(this.root);
        return visitor.toString();
    }





    private static int binarySearchHashCodeInEntries(final TreeHashMapEntry[] entries, final int hashCode) {

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




    private static int binarySearchHashCodeInNodes(final TreeHashMapNode[] nodes, final int hashCode) {

        // TODO when < 3 probably faster to do it sequentially

        int low = 0;
        int high = nodes.length - 1;

        int mid, cmp;
        TreeHashMapNode midVal;

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



    private interface TreeHashMapNode {

        Object get(final int keyHash, final Object key);
        int lowLimit();
        int highLimit();
        int totalEntries();
        TreeHashMapNode put(final int keyHash, final KeyValue keyValue);
        TreeHashMapNode remove(final int keyHash, final Object key);
        void acceptVisitor(final ImmutableTreeHashMapVisitor visitor);

    }


    private interface LeafTreeHashMapNode extends TreeHashMapNode {


        static TreeHashMapNode build(final int lowLimit, final int highLimit,
                                     final int maxNodeSize) {
            return new EmptyLeafTreeHashMapNode(lowLimit, highLimit, maxNodeSize);
        }



        static TreeHashMapNode build(final int lowLimit, final int highLimit,
                                     final int maxNodeSize, final TreeHashMapEntry entry) {
            return new SingleEntryLeafTreeHashMapNode(lowLimit, highLimit, maxNodeSize, entry);
        }



        static TreeHashMapNode build(final int lowLimit, final int highLimit,
                                     final int maxNodeSize, final TreeHashMapEntry[] entries) {

            if (entries.length == 0) {
                return build(lowLimit, highLimit, maxNodeSize);
            }

            if (entries.length == 1) {
                return build(lowLimit, highLimit, maxNodeSize, entries[0]);
            }

            if (entries.length > maxNodeSize) {
                // We have reached the maximum amount of hashes that can be contained in an EntriesNode, so
                // we need to divide this EntriesNode and turn it into a TreeNode

                final TreeHashMapNode[] newNodes = new TreeHashMapNode[maxNodeSize];
                long rangePerNode = computeRangePerNode(lowLimit, highLimit, maxNodeSize);

                int entriesOffset = 0;
                TreeHashMapEntry[] nodeEntries;

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
                        nodeEntries = new TreeHashMapEntry[n - entriesOffset];
                        System.arraycopy(entries, entriesOffset, nodeEntries, 0, (n - entriesOffset));
                    }
                    entriesOffset = n;

                    newNodes[i] = LeafTreeHashMapNode.build((int)newLowLimit, (int)newHighLimit, maxNodeSize, nodeEntries);

                }

                return BranchTreeHashMapNode.build(lowLimit, highLimit, entries.length, maxNodeSize, newNodes);

            }


            return new EntriesLeafTreeHashMapNode(lowLimit, highLimit, maxNodeSize, entries);

        }


    }


    private static final class EntriesLeafTreeHashMapNode implements LeafTreeHashMapNode {

        private final int lowLimit;
        private final int highLimit;
        private final int maxNodeSize;
        private final TreeHashMapEntry[] entries;


        private EntriesLeafTreeHashMapNode(
                final int lowLimit, final int highLimit,
                final int maxNodeSize,
                final TreeHashMapEntry[] entries) {
            super();
            this.lowLimit = lowLimit;
            this.highLimit = highLimit;
            this.maxNodeSize = maxNodeSize;
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

        TreeHashMapEntry[] entries() {
            return this.entries;
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
        public TreeHashMapNode put(final int keyHash, final KeyValue keyValue) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return this;
            }

            int pos = binarySearchHashCodeInEntries(this.entries, keyHash);

            if (pos >= 0) {

                final TreeHashMapEntry newEntry = this.entries[pos].put(keyHash, keyValue);
                final TreeHashMapEntry[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
                newEntries[pos] = newEntry;

                return LeafTreeHashMapNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntries);

            }

            pos = (++pos * -1);

            final TreeHashMapEntry[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
            System.arraycopy(newEntries, pos, newEntries, pos + 1, newEntries.length - (pos + 1));
            newEntries[pos] = TreeHashMapEntry.build(keyHash, keyValue);

            // This build call might actually return a TreeNode if we have now gone over the max size threshold
            return LeafTreeHashMapNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntries);

        }



        @Override
        public TreeHashMapNode remove(final int keyHash, final Object key) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return this;
            }

            int pos = binarySearchHashCodeInEntries(this.entries, keyHash);
            if (pos < 0) {
                // Not found
                return this;
            }

            final TreeHashMapEntry newEntry = this.entries[pos].remove(key);

            if (newEntry == this.entries[pos]) {
                // Not found (hash found but not key)
                return this;
            }

            if (newEntry != null) {

                final TreeHashMapEntry[] newEntries = Arrays.copyOf(this.entries, this.entries.length);
                newEntries[pos] = newEntry;

                return LeafTreeHashMapNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntries);

            }

            // newEntry is null, and so we need to actually remove it

            if (this.entries.length == 1) {
                // We are empty now!
                return LeafTreeHashMapNode.build(this.lowLimit, this.highLimit, this.maxNodeSize);
            }

            final TreeHashMapEntry[] newEntries = new TreeHashMapEntry[this.entries.length - 1];
            System.arraycopy(this.entries, 0, newEntries, 0, pos);
            System.arraycopy(this.entries, pos +1, newEntries, pos, this.entries.length - (pos + 1));

            return LeafTreeHashMapNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntries);

        }


        @Override
        public void acceptVisitor(final ImmutableTreeHashMapVisitor visitor) {
            visitor.visitLeafNode(this.lowLimit, this.highLimit, Arrays.asList(this.entries));
        }

    }


    private static final class EmptyLeafTreeHashMapNode implements LeafTreeHashMapNode {

        private final int lowLimit;
        private final int highLimit;
        private final int maxNodeSize;


        private EmptyLeafTreeHashMapNode(final int lowLimit, final int highLimit, final int maxNodeSize) {
            super();
            this.lowLimit = lowLimit;
            this.highLimit = highLimit;
            this.maxNodeSize = maxNodeSize;
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
            return 0;
        }


        @Override
        public Object get(final int keyHash, final Object key) {
            return null;
        }


        @Override
        public TreeHashMapNode put(final int keyHash, final KeyValue keyValue) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return this;
            }

            final TreeHashMapEntry newEntry = TreeHashMapEntry.build(keyHash, keyValue);
            return LeafTreeHashMapNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntry);

        }

        @Override
        public TreeHashMapNode remove(final int keyHash, final Object key) {
            return this;
        }


        @Override
        public void acceptVisitor(final ImmutableTreeHashMapVisitor visitor) {
            visitor.visitLeafNode(this.lowLimit, this.highLimit, Collections.emptyList());
        }


    }


    private static final class SingleEntryLeafTreeHashMapNode implements LeafTreeHashMapNode {

        private final int lowLimit;
        private final int highLimit;
        private final int maxNodeSize;
        private final TreeHashMapEntry entry;


        private SingleEntryLeafTreeHashMapNode(
                final int lowLimit, final int highLimit,
                final int maxNodeSize,
                final TreeHashMapEntry entry) {
            super();
            this.lowLimit = lowLimit;
            this.highLimit = highLimit;
            this.maxNodeSize = maxNodeSize;
            this.entry = entry;
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
            return 1;
        }


        @Override
        public Object get(final int keyHash, final Object key) {

            if (keyHash != this.entry.entryHash()) {
                return null;
            }
            return this.entry.get(key);

        }


        @Override
        public TreeHashMapNode put(final int keyHash, final KeyValue keyValue) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return this;
            }

            if (this.entry.entryHash() == keyHash) {
                final TreeHashMapEntry newEntry = this.entry.put(keyHash, keyValue);
                return LeafTreeHashMapNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntry);
            }

            final TreeHashMapEntry newEntry = TreeHashMapEntry.build(keyHash, keyValue);
            final TreeHashMapEntry[] newEntries = new TreeHashMapEntry[] { this.entry, this.entry };
            if (this.entry.entryHash() < keyHash) {
                newEntries[1] = newEntry;
            } else {
                newEntries[0] = newEntry;
            }

            // This build call might actually return a TreeNode if we have now gone over the max size threshold
            return LeafTreeHashMapNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntries);

        }



        @Override
        public TreeHashMapNode remove(final int keyHash, final Object key) {

            if (keyHash != this.entry.entryHash()) {
                // Not found
                return this;
            }

            final TreeHashMapEntry newEntry = this.entry.remove(key);

            if (newEntry == this.entry) {
                // Not found (hash found but not key)
                return this;
            }

            if (newEntry != null) {
                return LeafTreeHashMapNode.build(this.lowLimit, this.highLimit, this.maxNodeSize, newEntry);
            }

            // newEntry is null, and so we need to actually remove it

            return LeafTreeHashMapNode.build(this.lowLimit, this.highLimit, this.maxNodeSize);

        }


        @Override
        public void acceptVisitor(final ImmutableTreeHashMapVisitor visitor) {
            visitor.visitLeafNode(this.lowLimit, this.highLimit, Collections.singletonList(this.entry));
        }


    }


    private static final class BranchTreeHashMapNode implements TreeHashMapNode {

        private final int lowLimit;
        private final int highLimit;
        private final int totalEntries;
        private final int maxNodeSize;
        private final TreeHashMapNode[] nodes;

        private BranchTreeHashMapNode(
                final int lowLimit, final int highLimit,
                final int totalEntries, final int maxNodeSize,
                final TreeHashMapNode[] nodes) {
            super();
            this.lowLimit = lowLimit;
            this.highLimit = highLimit;
            this.totalEntries = totalEntries;
            this.maxNodeSize = maxNodeSize;
            this.nodes = nodes;
        }



        static TreeHashMapNode build(final int lowLimit, final int highLimit,
                                     final int containedHashEntries, final int maxNodeSize,
                                     final TreeHashMapNode[] nodes) {

            if (containedHashEntries <= maxNodeSize) {
                // We have gone under the threshold, so we should condense this back into an EntriesNode

                final TreeHashMapEntry[] newEntries = new TreeHashMapEntry[containedHashEntries];
                int offset = 0;
                for (int i = 0; i < nodes.length; i++) {
                    final TreeHashMapNode node = nodes[i];
                    if (node instanceof ImmutableTreeHashMap.EmptyLeafTreeHashMapNode) {
                        // No entries to be added
                        continue;
                    } else if (node instanceof ImmutableTreeHashMap.SingleEntryLeafTreeHashMapNode) {
                        // Node contains a single entry
                        newEntries[offset] = ((SingleEntryLeafTreeHashMapNode)node).entry;
                        offset++;
                    } else {
                        // Node is a MultiEntriesNode, containing more than 1 entry
                        final EntriesLeafTreeHashMapNode entriesNode = (EntriesLeafTreeHashMapNode) node;
                        System.arraycopy(entriesNode.entries, 0, newEntries, offset, entriesNode.entries.length);
                        offset += entriesNode.entries.length;
                    }
                }

                return LeafTreeHashMapNode.build(lowLimit, highLimit, maxNodeSize, newEntries);

            }

            return new BranchTreeHashMapNode(lowLimit, highLimit, containedHashEntries, maxNodeSize, nodes);

        }



        @Override
        public int lowLimit() {
            return this.lowLimit;
        }

        @Override
        public int highLimit() {
            return this.highLimit;
        }

        TreeHashMapNode[] nodes() {
            return this.nodes;
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
        public TreeHashMapNode put(final int keyHash, final KeyValue keyValue) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return this;
            }

            int pos = binarySearchHashCodeInNodes(this.nodes, keyHash);
            if (pos < 0) {
                // This should never happen
                return this;
            }

            final TreeHashMapNode newNode = this.nodes[pos].put(keyHash, keyValue);

            if (newNode == this.nodes[pos]) {
                return this;
            }

            final TreeHashMapNode[] newNodes = Arrays.copyOf(this.nodes, this.nodes.length);
            newNodes[pos] = newNode;

            final int newContainedHashEntries =
                    (this.totalEntries - this.nodes[pos].totalEntries()) + newNode.totalEntries();

            return BranchTreeHashMapNode.build(this.lowLimit, this.highLimit, newContainedHashEntries, this.maxNodeSize, newNodes);

        }


        @Override
        public TreeHashMapNode remove(final int keyHash, final Object key) {

            if (keyHash < this.lowLimit || keyHash > this.highLimit) {
                return this;
            }

            int pos = binarySearchHashCodeInNodes(this.nodes, keyHash);
            if (pos < 0) {
                // This should never happen
                return this;
            }

            final TreeHashMapNode newNode = this.nodes[pos].remove(keyHash, key);

            if (newNode == this.nodes[pos]) {
                return this;
            }

            // Note that no implementation of Node can return null after remove

            final TreeHashMapNode[] newNodes = Arrays.copyOf(this.nodes, this.nodes.length);
            newNodes[pos] = newNode;

            final int newTotalEntries =
                    (this.totalEntries - this.nodes[pos].totalEntries()) + newNode.totalEntries();

            // This build call might actually return an EntriesNode if we have now gone under the max size threshold
            return BranchTreeHashMapNode.build(this.lowLimit, this.highLimit, newTotalEntries, this.maxNodeSize, newNodes);

        }


        @Override
        public void acceptVisitor(final ImmutableTreeHashMapVisitor visitor) {
            visitor.visitBranchNode(this.lowLimit, this.highLimit, Arrays.asList(this.nodes));
        }

    }






    private interface TreeHashMapEntry {

        static TreeHashMapEntry build(final int entryHash, final KeyValue keyValue) {
            return new SingleValueTreeHashMapEntry(entryHash, keyValue);
        }

        Object get(final Object key);
        TreeHashMapEntry put(final int keyHash, final KeyValue keyValue);
        TreeHashMapEntry remove(final Object key);
        int entryHash();
        void acceptVisitor(final ImmutableTreeHashMapVisitor visitor);

    }

    private static final class SingleValueTreeHashMapEntry implements TreeHashMapEntry {

        private final int entryHash;
        private final KeyValue keyValue;

        private SingleValueTreeHashMapEntry(final int entryHash, final KeyValue keyValue) {
            super();
            this.entryHash = entryHash;
            this.keyValue = keyValue;
        }

        @Override
        public int entryHash() {
            return this.entryHash;
        }

        @Override
        public Object get(final Object key) {
            if (this.keyValue.getKey().equals(key)) {
                return this.keyValue.getValue();
            }
            return null;
        }

        @Override
        public TreeHashMapEntry put(final int keyHash, final KeyValue keyValue) {
            if (this.entryHash != keyHash) {
                throw new IllegalStateException("Tried to group entries with different hash code!");
            }
            if (this.keyValue.getKey() == keyValue.getKey() && this.keyValue.getValue() == keyValue.getValue()) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                return this;
            }
            if (this.keyValue.getKey().equals(keyValue.getKey())) {
                // We are replacing the previous value for a new one
                return new SingleValueTreeHashMapEntry(keyHash, keyValue);
            }
            // There is a hash collision, but this is a different entry, so we need to go MultiValue
            final KeyValue[] newKeyValues = new KeyValue[] { this.keyValue, keyValue };
            return new MultiValueTreeHashMapEntry(this.entryHash, newKeyValues);
        }

        @Override
        public TreeHashMapEntry remove(final Object key) {
            if (!this.keyValue.getKey().equals(key)) {
                return this;
            }
            return null;
        }

        @Override
        public void acceptVisitor(final ImmutableTreeHashMapVisitor visitor) {
            visitor.visitEntry(this.entryHash, Collections.singletonList(this.keyValue));
        }

    }


    private static final class MultiValueTreeHashMapEntry implements TreeHashMapEntry {

        private final int entryHash;
        private final KeyValue[] keyValues;


        private MultiValueTreeHashMapEntry(final int entryHash, final KeyValue[] keyValues) {
            super();
            this.entryHash = entryHash;
            this.keyValues = keyValues;
        }

        @Override
        public int entryHash() {
            return this.entryHash;
        }

        @Override
        public Object get(final Object key) {
            for (int i = 0; i < this.keyValues.length; i++) {
                if (this.keyValues[i].getKey().equals(key)) {
                    return this.keyValues[i].getValue();
                }
            }
            return null;
        }

        @Override
        public TreeHashMapEntry put(final int keyHash, final KeyValue keyValue) {
            if (this.entryHash != keyHash) {
                throw new IllegalStateException("Tried to group entries with different hash code!");
            }
            final Object keyValueKey = keyValue.getKey();
            int pos = -1;
            for (int i = 0; i < this.keyValues.length; i++) {
                if (this.keyValues[i].getKey().equals(keyValueKey)) {
                    pos = i;
                    break;
                }
            }
            if (pos >= 0) {
                if (this.keyValues[pos].getKey() == keyValue.getKey() && this.keyValues[pos].getValue() == keyValue.getValue()) {
                    // No need to perform any modifications, we might avoid a rewrite of a tree path!
                    // Note this will only happen if key and value are actually the same object, not by object equality
                    return this;
                }
                final KeyValue[] newKeyValues = Arrays.copyOf(this.keyValues, this.keyValues.length);
                newKeyValues[pos] = keyValue;
                return new MultiValueTreeHashMapEntry(this.entryHash, newKeyValues);
            }
            final KeyValue[] newKeyValues = Arrays.copyOf(this.keyValues, this.keyValues.length + 1);
            newKeyValues[this.keyValues.length] = keyValue;
            return new MultiValueTreeHashMapEntry(this.entryHash, newKeyValues);
        }

        @Override
        public TreeHashMapEntry remove(final Object key) {
            int pos = -1;
            for (int i = 0; i < this.keyValues.length; i++) {
                if (this.keyValues[i].getKey().equals(key)) {
                    pos = i;
                    break;
                }
            }
            if (pos >= 0) {
                if (this.keyValues.length == 2) {
                    // There are only two items in the multivalue, and we are removing one, so now its SingleValue
                    final KeyValue remainingvalue = this.keyValues[pos == 0? 1 : 0];
                    return new SingleValueTreeHashMapEntry(this.entryHash, remainingvalue);
                }
                final KeyValue[] newKeyValues = new KeyValue[this.keyValues.length - 1];
                System.arraycopy(this.keyValues, 0, newKeyValues, 0, pos);
                System.arraycopy(this.keyValues, pos + 1, newKeyValues, pos, (this.keyValues.length - (pos + 1)));
                return new MultiValueTreeHashMapEntry(this.entryHash, newKeyValues);
            }
            return this;
        }

        @Override
        public void acceptVisitor(final ImmutableTreeHashMapVisitor visitor) {
            visitor.visitEntry(this.entryHash, Arrays.asList(this.keyValues));
        }

    }


    private static final class KeyValue {

        private final Object key;
        private final Object value;

        static KeyValue build(final Object key, final Object value) {
            if (key == null) {
                throw new IllegalArgumentException("Null keys are forbidden");
            }
            return new KeyValue(key, value);
        }

        private KeyValue(final Object key, final Object value) {
            super();
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return this.key;
        }

        public Object getValue() {
            return this.value;
        }

    }


    // TODO either rename those low/high limit to "hashLowLimit", or generalise the concept bc it seems generalisable (index?)
    private interface ImmutableTreeHashMapVisitor {
        void visitRoot(final TreeHashMapNode rootNode);
        void visitBranchNode(final int lowLimit, final int highLimit, final List<TreeHashMapNode> nodes);
        void visitLeafNode(final int lowLimit, final int highLimit, final List<TreeHashMapEntry> entries);
        void visitEntry(final int entryHash, final List<KeyValue> keyValues);
    }

    public class PrettyPrintVisitor implements ImmutableTreeHashMapVisitor {

        private final StringBuilder visitorStrBuilder;
        private int level;

        public PrettyPrintVisitor() {
            super();
            this.visitorStrBuilder = new StringBuilder();
            this.level = 0;
        }

        protected String indentForLevel(final int level) {
            final StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < (level * 2); i++) {
                strBuilder.append(' ');
            }
            return strBuilder.toString();
        }


        protected String nodeStart(final int lowLimit, final int highLimit, final boolean isEmpty) {
            final String formatStr = (isEmpty? "[%11d | %11d] { " : "[%11d | %11d] {\n");
            return String.format(formatStr, lowLimit, highLimit);
        }

        protected String nodeEnd(final boolean isEmpty) {
            return (isEmpty? " }" : "}");
        }

        protected String separateNodes() {
            return "\n";
        }

        protected String entryStart(final int entryHash, final boolean isSingleValued) {
            final String formatStr = (isSingleValued? "[%11d | %11d] { " : "[%11d | %11d] {\n");
            return String.format(formatStr, entryHash);
        }

        protected String entryEnd(final boolean isSingleValued) {
            return (isSingleValued? " }" : "}");
        }




        protected String indentUnit() {
            return " ";
        }


        protected String writeEntry(final int entryHash, final List<String> writtenKeyValues, final int level) {
            final StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(indentForLevel(level));
            strBuilder.append(String.format("[%11d] {", entryHash));
            if (writtenKeyValues.size() == 1) {
                strBuilder.append(String.format(" %s }", writtenKeyValues.get(0)));
            } else {
                strBuilder.append('\n');
                for (int i = 0; i < writtenKeyValues.size(); i++) {
                    strBuilder.append(indentForLevel(level + 1));
                    strBuilder.append(writtenKeyValues.get(i));
                    strBuilder.append('\n');
                }
                strBuilder.append(indentForLevel(level));
                strBuilder.append('}');
            }
            return strBuilder.toString();
        }



        protected String writeKeyValue(final KeyValue keyValue, final int level) {
            return String.format("(\"%s\" : \"%s\")", keyValue.getKey(), keyValue.getValue());
        }



        @Override
        public void visitRoot(final TreeHashMapNode rootNode) {
            rootNode.acceptVisitor(this);
        }


        @Override
        public void visitBranchNode(final int lowLimit, final int highLimit, final List<TreeHashMapNode> nodes) {

        }

        @Override
        public void visitLeafNode(final int lowLimit, final int highLimit, final List<TreeHashMapEntry> entries) {

        }

        @Override
        public void visitEntry(final int entryHash, final List<KeyValue> keyValues) {

            this.indentForLevel(this.level);
            entryStart(entryHash, (keyValues.size() == 1));

            this.visitorStrBuilder.append(String.format(" (\"%s\" : \"%s\") }", singleValueEntry.key(), singleValueEntry.value()));

            this.visitorStrBuilder.append("{\n");

            this.indent += 8;

            for (final KeyValue : keyValues) {
                visit(entries[i], false);
                visitorStrBuilder.append('\n');
            }

            this.indent -= 8;

        }



        @Override
        public void visit(final TreeHashMapNode node) {

            for (int i = 0; i < this.indent; i++) {
                this.visitorStrBuilder.append(' ');
            }
            this.visitorStrBuilder.append(String.format("[%11d | %11d] ", node.lowLimit(), node.highLimit()));

            if (node instanceof ImmutableTreeHashMap.EmptyLeafTreeHashMapNode) {

                this.visitorStrBuilder.append("{}");

            } else if (node instanceof ImmutableTreeHashMap.SingleEntryLeafTreeHashMapNode || node instanceof ImmutableTreeHashMap.EntriesLeafTreeHashMapNode || node instanceof ImmutableTreeHashMap.BranchTreeHashMapNode){

                this.visitorStrBuilder.append("{\n");

                this.indent += 2;

                if (node instanceof ImmutableTreeHashMap.SingleEntryLeafTreeHashMapNode) {

                    final SingleEntryLeafTreeHashMapNode oneEntryNode = (SingleEntryLeafTreeHashMapNode) node;
                    visit(oneEntryNode.entry());
                    this.visitorStrBuilder.append('\n');

                } else if (node instanceof ImmutableTreeHashMap.EntriesLeafTreeHashMapNode){

                    final EntriesLeafTreeHashMapNode multiEntriesNode = (EntriesLeafTreeHashMapNode) node;
                    final TreeHashMapEntry[] entries = multiEntriesNode.entries();
                    for (int i = 0; i < entries.length; i++) {
                        visit(entries[i]);
                        this.visitorStrBuilder.append('\n');
                    }

                } else { // TreeNode

                    final BranchTreeHashMapNode multiEntriesNode = (BranchTreeHashMapNode) node;
                    final TreeHashMapNode[] nodes = multiEntriesNode.nodes();
                    for (int i = 0; i < nodes.length; i++) {
                        visit(nodes[i]);
                        this.visitorStrBuilder.append('\n');
                    }

                }

                this.indent -= 2;

                for (int i = 0; i < this.indent; i++) {
                    this.visitorStrBuilder.append(' ');
                }
                this.visitorStrBuilder.append("}");

            } else {
                throw new IllegalStateException("Unknown type of node: " + node.getClass().getName());
            }
        }


        @Override
        public void visit(final TreeHashMapEntry entry) {
            visit(entry, true);
        }

        public void visit(final TreeHashMapEntry entry, final boolean outputHash) {

            for (int i = 0; i < this.indent; i++) {
                this.visitorStrBuilder.append(' ');
            }
            if (outputHash) {
                this.visitorStrBuilder.append(String.format("[%11d] ", entry.entryHash()));
            }

            if (entry instanceof ImmutableTreeHashMap.SingleValueTreeHashMapEntry) {
                final SingleValueTreeHashMapEntry singleValueEntry = (SingleValueTreeHashMapEntry)entry;
                if (outputHash) {
                    this.visitorStrBuilder.append(String.format("{ (\"%s\" : \"%s\") }", singleValueEntry.key(), singleValueEntry.value()));
                } else {
                    this.visitorStrBuilder.append(String.format("(\"%s\" : \"%s\")", singleValueEntry.key(), singleValueEntry.value()));
                }

            } else if (entry instanceof ImmutableTreeHashMap.MultiValueTreeHashMapEntry) {

                final MultiValueTreeHashMapEntry multiValueEntry = (MultiValueTreeHashMapEntry)entry;
                final TreeHashMapEntry[] entries = multiValueEntry.entries();

                this.visitorStrBuilder.append("{\n");

                this.indent += 16;

                for (int i = 0; i < entries.length; i++) {
                    visit(entries[i], false);
                    visitorStrBuilder.append('\n');
                }

                this.indent -= 16;

                for (int i = 0; i < this.indent; i++) {
                    visitorStrBuilder.append(' ');
                }
                visitorStrBuilder.append("}");

            } else {
                throw new IllegalStateException("Unknown type of entry: " + entry.getClass().getName());
            }

        }


        @Override
        public String toString() {
            return this.visitorStrBuilder.toString();
        }

    }





    public static void main(String[] args) {

        ImmutableTreeHashMap<String,Object> m = ImmutableTreeHashMap.build(2);

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

        m = m.put("Ola", 2341233);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("Hola", 2341233);

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

        m = m.remove("j'lloworld");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("iFlloworld");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("Ola");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("helloworld");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("lloworld");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("n.oworld");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("Hola");

        System.out.println();
        System.out.println(m.prettyPrint());



        final long s0 = System.nanoTime();

        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName(), "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "user_1", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "name_2", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "options_3", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "isEnabled_4", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "totalCount_5", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "allProps_6", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "surname_7", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "address_8", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "zip_9", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "state_10", "active");

        final long e0 = System.nanoTime();

        System.out.println("TIME: " + (e0 -s0));

        System.out.println();
        System.out.println(m.prettyPrint());


    }



}
