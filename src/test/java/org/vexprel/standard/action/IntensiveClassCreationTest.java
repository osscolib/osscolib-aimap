package org.vexprel.standard.action;

import org.vexprel.model.User;
import org.vexprel.standard.step.ObjectPropertyExpressionStep;

public class IntensiveClassCreationTest {


    public static void main(final String[] args) throws Exception {

        final ObjectPropertyExpressionStep step = new ObjectPropertyExpressionStep("name");
        final ByteCodeGenStandardExpressionActionFactory actionFactory = new ByteCodeGenStandardExpressionActionFactory(true);

        final long s0 = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            StandardExpressionAction action = actionFactory.build(step, User.class);
        }

        final long e0 = System.nanoTime();

        System.out.println("TOTAL TIME: " +  (e0 - s0));

        final ByteCodeGenStandardExpressionActionFactory actionFactory2 = new ByteCodeGenStandardExpressionActionFactory(false);

        final long s1 = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            StandardExpressionAction action = actionFactory2.build(step, User.class);
        }

        final long e1 = System.nanoTime();

        System.out.println("TOTAL TIME: " +  (e1 - s1));

        final ByteCodeGenStandardExpressionActionFactory actionFactory3 = new ByteCodeGenStandardExpressionActionFactory(true);

        final long s2 = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            StandardExpressionAction action = actionFactory3.build(step, User.class);
        }

        final long e2 = System.nanoTime();

        System.out.println("TOTAL TIME: " +  (e2 - s2));

    }

}
