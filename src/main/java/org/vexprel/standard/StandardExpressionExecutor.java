package org.vexprel.standard;

import org.vexprel.Expression;
import org.vexprel.context.ExpressionContext;
import org.vexprel.exptarget.ExpressionTarget;

class StandardExpressionExecutor {


    StandardExpressionExecutor() {
        super();
    }


    private static void validateArguments(
            final ExpressionContext context, final Expression expression, final ExpressionTarget target) {

        if (context == null) {
            throw new IllegalArgumentException("Cannot execute on null context");
        }
        if (expression == null) {
            throw new IllegalArgumentException("Cannot execute null expression");
        }
        if (target == null) {
            throw new IllegalArgumentException("Cannot execute on null target");
        }

    }


    Object execute(final ExpressionContext context, final StandardExpression expression, final ExpressionTarget target) {

        validateArguments(context, expression, target);

        final StandardExpressionStep[] steps = expression.getSteps();

        Object current = target.getTargetObject();

        for (int i = 0; i < steps.length; i++) {
            
        }

        return null;
    }

}
