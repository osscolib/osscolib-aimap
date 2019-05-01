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

import java.util.Objects;
import java.util.function.ToIntFunction;

public interface IndexMap<K,V> {

    int size();
//    boolean isEmpty();
    boolean containsKey(Object key);
//    boolean containsValue(Object value);
    V get(final Object key);


//    Set<K> keySet();
//    Collection<V> values();
//    Set<Map.Entry<K, V>> entrySet();


    ToIntFunction<Object> getIndexFunction();


    static <K,V> Builder<K,V> build() {
        return Builder.DEFAULT_BUILDER;
    }



    final class Builder<K,V> {

        private final static int SMALL_MASK_SIZE = 2; // node size = 4, max levels = 16
        private final static int MEDIUM_MASK_SIZE = 4; // node size = 16, max levels = 8
        private final static int LARGE_MASK_SIZE = 8; // node size = 256, max levels = 4
        private final static HashCodeFunction DEFAULT_INDEX_FUNCTION = new HashCodeFunction();

        private final static Builder DEFAULT_BUILDER = new Builder(MEDIUM_MASK_SIZE, DEFAULT_INDEX_FUNCTION);

        private final ToIntFunction<Object> indexFunction;
        private final int maskSize;


        private Builder(final int maskSize, final ToIntFunction<Object> indexFunction) {
            super();
            this.maskSize = maskSize;
            this.indexFunction = indexFunction;
        }


        public Builder<K,V> withSmallSize() {
            return new Builder<>(SMALL_MASK_SIZE, this.indexFunction);
        }
        public Builder<K,V> withMediumSize() {
            return new Builder<>(MEDIUM_MASK_SIZE, this.indexFunction);
        }
        public Builder<K,V> withLargeSize() {
            return new Builder<>(LARGE_MASK_SIZE, this.indexFunction);
        }


        public Builder<K,V> withIndexFunction(final ToIntFunction<Object> indexFunction) {
            return new Builder<>(this.maskSize, indexFunction);
        }


        public FluentIndexMap<K,V> asFluentMap() {
            // The map is initialized with a null root (no DataSlots)
            final int mask = (1 << this.maskSize) - 1;
            return new FluentIndexMap<>(mask, this.indexFunction, null);
        }


        private static class HashCodeFunction implements ToIntFunction<Object> {
            @Override
            public int applyAsInt(final Object k) {
                return Objects.hashCode(k);
            }
        }

    }


}
