package org.vexprel.standard;

import org.vexprel.Expression;
import org.vexprel.context.ExpressionContext;
import org.vexprel.exptarget.ExpressionTarget;
import org.vexprel.standard.action.StandardExpressionAction;
import org.vexprel.standard.action.StandardExpressionActionFactory;
import org.vexprel.standard.step.StandardExpressionStep;

class StandardExpressionExecutor {

    private final StandardExpressionActionFactory expressionActionFactory;


    StandardExpressionExecutor(final StandardExpressionActionFactory expressionActionFactory) {
        super();
        this.expressionActionFactory = expressionActionFactory;
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
            final StandardExpressionAction action = this.expressionActionFactory.build(steps[i], current.getClass());
            current = action.execute(current);
        }

        return current;
    }

}
