package org.osscolib.aimap;

import org.osscolib.aimap.AtomicIndexedMap.Node;
import org.osscolib.aimap.AtomicIndexedMap.Slot;

final class Utils {

    private static final Slot[] EMPTY_SLOTS = new AtomicIndexedMap.Slot[0];

    static <K,V> Slot<K,V>[] emptySlots() {
        return EMPTY_SLOTS;
    }


    static <K,V> int binarySearchIndexInSlots(final Slot<K,V>[] slots, final int index) {

        // TODO when < 5 probably faster to do it sequentially

        int low = 0;
        int high = slots.length - 1;

        int mid, cmp;
        int midVal;

        while (low <= high) {

            mid = (low + high) >>> 1;
            midVal = slots[mid].getIndex();

            cmp = Integer.compare(midVal, index);

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




    static <K,V> int binarySearchIndexInNodes(final Node<K,V> [] nodes, final int index) {

        // TODO when < 3 probably faster to do it sequentially

        int low = 0;
        int high = nodes.length - 1;

        int mid, cmp;
        Node midVal;

        while (low <= high) {

            mid = (low + high) >>> 1;
            midVal = nodes[mid];

            cmp = (midVal.getIndexHighLimit() < index ? -1 : midVal.getIndexLowLimit() > index? 1 : 0);

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


    private Utils() {
        super();
    }


}
