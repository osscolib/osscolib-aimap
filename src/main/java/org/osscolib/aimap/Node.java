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

interface Node<K,V> {

    boolean containsKey(final long index, final Object key);
    V get(final long index, final Object key);
    Node<K,V> put(final long index, final Entry<K, V> entry);
    Node<K,V> remove(final long index, final Object key);
    int size();

    long getIndexLowLimit();
    long getIndexHighLimit();

    void acceptVisitor(final IndexMapVisitor<K, V> visitor);

}
