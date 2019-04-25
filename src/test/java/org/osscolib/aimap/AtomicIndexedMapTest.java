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

public class AtomicIndexedMapTest {



    public static void main(String[] args) {

        FluentIndexMap<String,Object> m =
                IndexMap
                        .<String,Object>build()
                        .withMaxNodeSize(10)
//                        .withIndexing(0, 2, key -> Math.abs(key.hashCode() % 3))
                        .asFluentMap();

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("Hello", 21);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("helloworld", 23);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("iFlloworld", 52);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("j'lloworld", 31);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("lloworld", 99);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("IFllo", 423);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("n.oworld", 941);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("Aloha", 3413);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("IFllo", 987);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("Hello", 23142);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("Ola", 2341233);

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put("Hola", 2341233);

        System.out.println();
        System.out.println(m.prettyPrint());


        System.out.println();
        System.out.println(m.get("IFllo"));
        System.out.println(m.get("lloworld"));
        System.out.println(m.get("Hello"));
        System.out.println(m.get("Aloha"));

        m = m.remove("Hello");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("IFllo");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("IFllo");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("Aloha");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("j'lloworld");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("iFlloworld");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("Ola");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("helloworld");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("lloworld");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("n.oworld");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.remove("Hola");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put(null, "Something null!");

        System.out.println();
        System.out.println(m.prettyPrint());

        m = m.put(null, "Something other null!");

        System.out.println();
        System.out.println(m.prettyPrint());

        System.out.println(m.get(null));
        System.out.println(m.get("Hola"));
        System.out.println(m.get("n.oworld"));

        m = m.remove(null);

        System.out.println();
        System.out.println(m.prettyPrint());

    }



}
