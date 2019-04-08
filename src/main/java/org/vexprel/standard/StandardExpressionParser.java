package org.vexprel.standard;

import java.util.ArrayList;

import org.vexprel.Expression;

class StandardExpressionParser {


    StandardExpressionParser() {
        super();
    }


    private static void validateArguments(final String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Cannot parse null expression");
        }
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("Cannot parse empty expression");
        }
    }


    Expression parse(final String expression) {

        validateArguments(expression);

        final ArrayList<StandardExpressionStep> steps = new ArrayList<>(3);

        int offset = 0;
        int dotpos = -1;
        while ((dotpos = expression.indexOf('.', offset)) != -1) {
            steps.add(new ObjectPropertyExpressionStep(expression.substring(offset, dotpos)));
            offset = dotpos + 1;
        }
        steps.add(new ObjectPropertyExpressionStep(expression.substring(offset)));

        return new StandardExpression(steps.toArray(new StandardExpressionStep[steps.size()]));

    }

}
