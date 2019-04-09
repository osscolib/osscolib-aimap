package org.vexprel.standard.action;

import java.util.concurrent.atomic.AtomicReference;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import org.vexprel.exceptions.ExpressionExecutionException;
import org.vexprel.standard.step.ObjectPropertyExpressionStep;
import org.vexprel.standard.step.StandardExpressionStep;

public class ByteCodeGenStandardExpressionActionFactory implements StandardExpressionActionFactory {

    private final AtomicReference<InjectionClassLoader> objectPropertyActionsClassLoaderRef = new AtomicReference<>();


    private final boolean useSingleClassLoader;


    public ByteCodeGenStandardExpressionActionFactory(final boolean useSingleClassLoader) {
        super();
        this.useSingleClassLoader = useSingleClassLoader;
    }


    @Override
    public StandardExpressionAction build(final StandardExpressionStep step, final Class<?> targetClass) {

        if (step instanceof ObjectPropertyExpressionStep) {
            return buildObjectPropertyAction((ObjectPropertyExpressionStep) step, targetClass);
        }

        throw new ExpressionExecutionException(
                String.format("Unknown expression step class '%s'. Cannot generate action", step.getClass().getName()));

    }



    private StandardExpressionAction buildObjectPropertyAction(
            final ObjectPropertyExpressionStep step, final Class<?> targetClass) {

        final String getterMethodName = step.getGetterMethodName();

        final DynamicType.Unloaded<StandardExpressionAction> actionType =
                ByteCodeGenUtils.buildObjectPropertyType(targetClass, getterMethodName);

        final Class<? extends StandardExpressionAction> actionClass =
                this.useSingleClassLoader ?
                        obtainObjectPropertyActionClassSingleClassLoader(actionType) :
                        obtainObjectPropertyActionClassClassLoaderPerClass(actionType);

        return ByteCodeGenUtils.buildActionInstance(actionClass);

    }


    private Class<? extends StandardExpressionAction> obtainObjectPropertyActionClassSingleClassLoader(
            final DynamicType.Unloaded<StandardExpressionAction> actionType) {

        InjectionClassLoader classLoader = this.objectPropertyActionsClassLoaderRef.get();

        if (classLoader == null) {

            final Class<? extends StandardExpressionAction> actionClass =
                    ByteCodeGenUtils.loadIntoNewWrapperClassLoader(
                            actionType,
                            ByteCodeGenStandardExpressionActionFactory.class.getClassLoader(),
                            true);

            final InjectionClassLoader newClassLoader = (InjectionClassLoader) actionClass.getClassLoader();

            if (this.objectPropertyActionsClassLoaderRef.compareAndSet(null, newClassLoader)) {
                return actionClass;
            }

            // A new class loader was created concurrently by another thread. We will discard the
            // newly created class and ask for a new one using the right class loader
            classLoader = this.objectPropertyActionsClassLoaderRef.get();

        }

        return ByteCodeGenUtils.loadIntoExistingClassLoader(actionType, classLoader);

    }


    private Class<? extends StandardExpressionAction> obtainObjectPropertyActionClassClassLoaderPerClass(
            final DynamicType.Unloaded<StandardExpressionAction> actionType) {

            return ByteCodeGenUtils.loadIntoNewWrapperClassLoader(
                            actionType,
                            ByteCodeGenStandardExpressionActionFactory.class.getClassLoader(),
                            false);

    }


}
