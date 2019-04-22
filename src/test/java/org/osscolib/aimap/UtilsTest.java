package org.osscolib.aimap;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

    private static final int[] NODE_SIZES = new int[] { 1, 2, 3, 5, 7, 9, 10, 11, 13, 17, 19, 20, 25, 30, 48, 50, 60, 64, 100, 1024 };


    @Test
    public void testPositioning() {

//        for (long low = Integer.MIN_VALUE; low <= Integer.MAX_VALUE; low++) {
//            for (long high = low; high <= Integer.MAX_VALUE; high++) {
//                testPositioningWith(low, high);
//            }
//            System.out.print('.');
//            if (low == 0L) {
//                System.out.print('$');
//            }
//        }


        testPositioningWith(Integer.MIN_VALUE, Integer.MAX_VALUE);

    }




    private static void testPositioningWith(final long lowLimit, final long highLimit) {
        for (int i = 0; i < NODE_SIZES.length; i++) {
            testPositioningWith(lowLimit, highLimit, NODE_SIZES[i]);
        }
    }


    private static void testPositioningWith(final long lowLimit, final long highLimit, final int nodeSize) {
        for (long i = lowLimit; i <= highLimit; i++) {
            testPositioningWith(lowLimit, highLimit, nodeSize, i);
        }
    }


    private static void testPositioningWith(final long lowLimit, final long highLimit, final int nodeSize, final long index) {

        final long rangePerNode = computeRangePerNode(lowLimit, highLimit, nodeSize);
        int iterPos = -1;
        long fragLowLimit, fragHighLimit;

        int i = 0;
        while (iterPos < 0) {

            fragLowLimit = lowLimit + (i * rangePerNode);
            fragHighLimit = fragLowLimit + rangePerNode - 1;
            if (fragHighLimit > highLimit) {
                // This can only happen for the last node
                fragHighLimit = highLimit;
            }

            if (fragLowLimit <= index && index <= fragHighLimit) {
                iterPos = i;
            }

            i++;

        }

        final int computedPos = Utils.computePosition(lowLimit, highLimit, nodeSize, index);

        Assert.assertEquals(iterPos, computedPos);
        Assert.assertTrue(computedPos >= 0);
        Assert.assertTrue(computedPos < nodeSize);

    }


    private static long computeRangePerNode(final long lowLimit, final long highLimit, final int nodeSize) {
        final long totalRange = (highLimit - lowLimit) + 1L; // limits are inclusive => +1
        final long divider = totalRange / (long)nodeSize;
        if ((totalRange % (long)nodeSize) == 0) {
            return divider;
        }
        return divider + 1L;
    }


}
