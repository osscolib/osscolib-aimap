package org.vexprel.standard;

import org.vexprel.Expression;
import org.vexprel.standard.step.StandardExpressionStep;

public class StandardExpression implements Expression {

    private final StandardExpressionStep[] steps;
    private final String stringRepresentation;


    StandardExpression(final StandardExpressionStep[] steps) {

        super();

        this.steps = steps;

        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < this.steps.length; i++) {
            if (i > 0) {
                strBuilder.append('.');
            }
            strBuilder.append(this.steps[i].getStringRepresentation());
        }
        this.stringRepresentation = strBuilder.toString();

    }


    StandardExpressionStep[] getSteps() {
        return this.steps;
    }


    @Override
    public String getStringRepresentation() {
        return this.stringRepresentation;
    }


    @Override
    public String toString() {
        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < this.steps.length; i++) {
            strBuilder.append(this.steps[i].toString());
        }
        return String.format("[StandardExpression: %s]",strBuilder.toString());
    }

}
