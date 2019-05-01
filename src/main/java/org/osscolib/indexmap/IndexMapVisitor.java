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

import java.util.List;
import java.util.Map;


interface IndexMapVisitor<K,V> {

    void visitRoot(final int maskSize, final Node<K, V> rootNode);
    void visitBranchNode(final int level, final int maskSize, final List<Node<K, V>> nodes);
    void visitDataSlotNode(final int level, final int maskSize, final DataSlot<K, V> dataSlot);
    void visitDataSlot(final int index, final List<Map.Entry<K, V>> entries);

}
