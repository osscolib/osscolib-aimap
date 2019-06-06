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

final class Level {

    static final int LEVEL_COUNT;
    static final Level LEVEL0;
    static final Level[] LEVELS;

    final int mask;
    final int shift;
    final Level next;


    static {

        // Direct references should give us better performance than an array (no boundaries checks!)
        final Level level5 = new Level(0xFF, 24,   null); // mask size = 8
        final Level level4 = new Level(0xFF, 16, level5); // mask size = 8
        final Level level3 = new Level(0x3F, 10, level4); // mask size = 6
        final Level level2 = new Level( 0xF,  6, level3); // mask size = 4
        final Level level1 = new Level( 0xF,  2, level2); // mask size = 4
        LEVEL0 =             new Level( 0x3,  0, level1); // mask size = 2


        int count = 1;
        Level l = LEVEL0;
        while ((l = l.next) != null) count++;

        LEVEL_COUNT = count;

        l = LEVEL0;
        LEVELS = new Level[LEVEL_COUNT];
        for (int i = 0; i < LEVELS.length; i++) {
            LEVELS[i] = l;
            l = l.next;
        }

    }



    Level(final int mask, final int shift, final Level next) {
        this.mask = mask;
        this.shift = shift;
        this.next = next;
    }

    int pos(final int hash) {
        return (hash >>> this.shift) & this.mask;
    }


}
