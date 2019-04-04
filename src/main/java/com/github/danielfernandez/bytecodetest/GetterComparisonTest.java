package com.github.danielfernandez.bytecodetest;

import java.lang.reflect.Method;

public class GetterComparisonTest {

    private static int WARMUP_ITER = 10;
    private static int EXEC_ITER = 100;



    public static void main(final String[] args) throws Exception {

        final User obj = new User("some name", "some surname");
        final Method nameMethod = User.class.getMethod("getName");
        final GetterExecutor userNameExecutor = ExecutorFactory.buildGetterExecutor(User.class, "name");


        long totalSize = 0L;

        String name;

        for (int i = 0;  i < WARMUP_ITER; i++) {
            name = obj.getName();
            totalSize += name.length();
        }
        totalSize = 0L;
        for (int i = 0;  i < WARMUP_ITER; i++) {
            name = (String) nameMethod.invoke(obj);
            totalSize += name.length();
        }
        totalSize = 0L;
        for (int i = 0;  i < WARMUP_ITER; i++) {
            name = (String) userNameExecutor.execute(obj);
            totalSize += name.length();
        }

        totalSize = 0L;

        final long s0 = System.nanoTime();
        for (int i = 0;  i < EXEC_ITER; i++) {
            name = obj.getName();
            totalSize += name.length();
        }
        final long e0 = System.nanoTime();

        System.out.println(String.format("TIME FOR NORMAL CALLS:         %020d", Long.valueOf(e0 - s0)));
        System.out.println("TOTAL SIZE: " + totalSize);


        totalSize = 0L;

        final long s1 = System.nanoTime();
        for (int i = 0;  i < EXEC_ITER; i++) {
            name = (String) nameMethod.invoke(obj);
            totalSize += name.length();
        }
        final long e1 = System.nanoTime();

        System.out.println(String.format("TIME FOR Method CALLS:         %020d", Long.valueOf(e1 - s1)));
        System.out.println("TOTAL SIZE: " + totalSize);


        totalSize = 0L;

        final long s2 = System.nanoTime();
        for (int i = 0;  i < EXEC_ITER; i++) {
            name = (String) userNameExecutor.execute(obj);
            totalSize += name.length();
        }
        final long e2 = System.nanoTime();

        System.out.println(String.format("TIME FOR GetterExecutor CALLS: %020d", Long.valueOf(e2 - s2)));
        System.out.println("TOTAL SIZE: " + totalSize);

    }

}
