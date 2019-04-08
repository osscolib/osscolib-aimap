package org.vexprel.standard;

import org.vexprel.model.User;

public class IntensiveClassCreationTest {


    public static void main(final String[] args) throws Exception {

        final long s0 = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            StandardExpressionAction actiokn = ByteCodeGenUtils.buildObjectPropertyAction(User.class, "surname");
        }

        final long e0 = System.nanoTime();

        System.out.printf("TOTAL TIME: " +  (e0 - s0));

    }

}
