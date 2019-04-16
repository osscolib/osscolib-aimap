package org.vexprel.util.atomicindexedmap;

import org.vexprel.standard.action.ByteCodeGenStandardExpressionActionFactory;

public class AtomicIndexedMapTest {




    public static void main(String[] args) {

        AtomicIndexedMap<String,Object> m = AtomicIndexedMap.build(2);

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



        final long s0 = System.nanoTime();

        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName(), "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "user_1", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "name_2", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "options_3", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "isEnabled_4", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "totalCount_5", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "allProps_6", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "surname_7", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "address_8", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "zip_9", "active");
        m = m.put(ByteCodeGenStandardExpressionActionFactory.class.getName() + "state_10", "active");

        final long e0 = System.nanoTime();

        System.out.println("TIME: " + (e0 -s0));

        System.out.println();
        System.out.println(m.prettyPrint());


    }



}
