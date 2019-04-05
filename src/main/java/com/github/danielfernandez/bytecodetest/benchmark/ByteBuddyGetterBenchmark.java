package com.github.danielfernandez.bytecodetest.benchmark;

import com.github.danielfernandez.bytecodetest.ExecutorFactory;
import com.github.danielfernandez.bytecodetest.GetterExecutor;
import com.github.danielfernandez.bytecodetest.User;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;

public class ByteBuddyGetterBenchmark extends BaseBenchmark {

    private User user;
    private GetterExecutor getterExecutor;


    @Setup
    public void setup() throws Exception {
        this.user = buildUser();
        this.getterExecutor = ExecutorFactory.buildGetterExecutor(User.class, "name");
    }


    @Benchmark
    public String benchmark() throws Exception {
        return (String) this.getterExecutor.execute(this.user);
    }



}
