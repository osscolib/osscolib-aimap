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

import org.osscolib.aimap.IndexedMap.Node;
import org.osscolib.aimap.IndexedMap.Slot;

final class Utils {

    // If size is under this threshold, a sequential search probably performs better
    private static final int SEQUENTIAL_SEARCH_THRESHOLD = 8;

    private static final Slot[] EMPTY_SLOTS = new Slot[0];

    static <K,V> Slot<K,V>[] emptySlots() {
        return EMPTY_SLOTS;
    }


    static <K,V> int binarySearchIndexInSlots(final Slot<K,V>[] slots, final int index) {

        // If under the threshold, try sequential search
        if (slots.length <= SEQUENTIAL_SEARCH_THRESHOLD) {
            int idx;
            for (int i = 0; i < slots.length; i++) {
                idx = slots[i].getIndex();
                if (idx > index) {
                    return -(i + 1);
                } else if (idx == index) {
                    return i;
                }
            }
            return -(slots.length + 1);
        }

        // Over the threshold so binary search it is

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

        // If under the threshold, try sequential search
        if (nodes.length <= SEQUENTIAL_SEARCH_THRESHOLD) {
            int idxLow, idxHigh, cmp;
            for (int i = 0; i < nodes.length; i++) {
                idxLow = nodes[i].getIndexLowLimit();
                idxHigh = nodes[i].getIndexHighLimit();
                cmp = (idxHigh < index ? -1 : idxLow > index? 1 : 0);
                if (cmp > 0) {
                    return -(i + 1);
                } else if (cmp == 0) {
                    return i;
                }
            }
            return -(nodes.length + 1);
        }

        // Over the threshold so binary search it is

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
