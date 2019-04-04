package com.github.danielfernandez.bytecodetest;

import java.lang.reflect.Method;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;

public class ExecutorFactory {


    public static GetterExecutor buildGetterExecutor(final Class<?> targetClass, final String propertyName) throws Exception {

        final StringBuilder getterNameBuilder = new StringBuilder();
        getterNameBuilder.append("get");
        getterNameBuilder.append(Character.toUpperCase(propertyName.charAt(0)));
        getterNameBuilder.append(propertyName.substring(1));
        final Method getterMethod = User.class.getMethod(getterNameBuilder.toString(), null);

        final Class<?> executorClass =
                new ByteBuddy()
                        .subclass(Object.class)
                        .implement(GetterExecutor.class)
                        .name("bytecodetest.executors." + targetClass.getName() + "_getter_" + propertyName)
                        .method(ElementMatchers.named("execute"))
                        .intercept(
                                MethodCall.invoke(getterMethod)
                                        .onArgument(0)
                                        .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                        .make()
                        .load(ExecutorFactory.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                        .getLoaded();

        return (GetterExecutor) executorClass.getConstructor(null).newInstance(null);

    }




}
