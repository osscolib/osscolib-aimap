package org.vexprel.standard.action;

import org.vexprel.standard.step.StandardExpressionStep;

public interface StandardExpressionActionFactory {

    StandardExpressionAction build(final StandardExpressionStep step, final Class<?> targetClass);

}
