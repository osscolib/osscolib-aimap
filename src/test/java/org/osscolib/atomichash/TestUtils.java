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


import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public final class TestUtils {


    public static String generateString(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

    public static String generateKey() {
        return generateString(20);
    }

    public static String generateValue() {
        return generateString(150);
    }


    public static KeyValue<String,String>[] generateStringStringKeyValues(
            final int numElements) {
        final KeyValue<String,String>[] entries = new KeyValue[numElements];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new KeyValue<>(generateKey(), generateValue());
        }
        return entries;
    }


    public static int[] generateInts(final int numElements, final int minValue, final int maxValue) {
        final int[] accessOrder = new int[numElements];
        for (int i = 0; i < accessOrder.length; i++) {
            accessOrder[i] = RandomUtils.nextInt(minValue, maxValue);
        }
        return accessOrder;
    }


    private TestUtils() {
        super();
    }

}
