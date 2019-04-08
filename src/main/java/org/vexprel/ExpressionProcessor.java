package org.vexprel;

import org.vexprel.context.EmptyExpressionContext;
import org.vexprel.context.ExpressionContext;
import org.vexprel.exptarget.ExpressionTarget;
import org.vexprel.exptarget.DefaultExpressionTarget;

public interface ExpressionProcessor {

    Expression parse(final String expression);

    default Object execute(final String expression, final Object target) {
        return execute(parse(expression), target);
    }

    default Object execute(final Expression expression, final Object target) {
        return execute(EmptyExpressionContext.INSTANCE, expression, target);
    }

    default Object execute(final ExpressionContext context, final Expression expression, final Object target) {
        return execute(context, expression, new DefaultExpressionTarget(target));
    }

    Object execute(final ExpressionContext context, final Expression expression, final ExpressionTarget target);

}
