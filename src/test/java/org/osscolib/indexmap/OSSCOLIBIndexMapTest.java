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

import org.junit.Assert;
import org.junit.Test;

public class OSSCOLIBIndexMapTest {


    @Test
    public void testVersion() {
        Assert.assertNotNull(OSSCOLIBIndexMap.VERSION);
        Assert.assertTrue(OSSCOLIBIndexMap.VERSION_MAJOR >= 0);
        Assert.assertTrue(OSSCOLIBIndexMap.VERSION_MINOR >= 0);
        Assert.assertTrue(OSSCOLIBIndexMap.VERSION_BUILD >= 0);
        Assert.assertTrue(OSSCOLIBIndexMap.BUILD_TIMESTAMP != null && OSSCOLIBIndexMap.BUILD_TIMESTAMP.trim().length() > 0);
    }


}
