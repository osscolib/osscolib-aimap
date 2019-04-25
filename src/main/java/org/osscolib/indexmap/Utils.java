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

final class Utils {


    static long computeRangePerChild(final long nodeRange, final int maxNodeSize) {
        return (nodeRange / maxNodeSize) + (nodeRange % maxNodeSize == 0 ? 0 : 1);
    }

    static int computeChildrenSize(final long nodeRange, final long rangePerChild) {
        return (int) ( (nodeRange / rangePerChild) + (nodeRange % rangePerChild == 0 ? 0 : 1) );
    }

    static int computeChildPos(final long lowLimit, final long rangePerChild, final long index) {
        return (int) ((index - lowLimit) / rangePerChild);
    }

    static long computeLowLimitForChild(final long indexLowLimit, final long rangePerChild, final int childPos) {
        return indexLowLimit + (childPos * rangePerChild);
    }

    static long computeHighLimitForChild(final long indexLowLimit, final long rangePerChild, final int childPos) {
        return (indexLowLimit + ((childPos + 1) * rangePerChild)) - 1;
    }


    private Utils() {
        super();
    }


}
